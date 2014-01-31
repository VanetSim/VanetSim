/*
 * Copyright 2003-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */


package vanetsim.routing.A_Star;

import java16.util.Arrays;

/**
 * This class is almost the same as <code>java.util.PriorityQueue</code> from Sun OpenJDK 1.7 but is
 * simplified for the specific usage as a high-performance queue for the <code>A_Star_Algo</code>.
 * It also has an added feature to change the value of an element without needing to readd.
 * It only includes the functions needed for this algorithm and thus cannot be used as a multi-purpose
 * container anymore!
 */
public final class A_Star_Queue{
	
	/** The queue is represented by an array (binary heap). */
	private A_Star_Node[] queue_;

	/** The number of elements in the priority queue. */
	private int size_ = 0;

	/**
	 * Creates a new A_Star_Queue with 100 elements starting capacity.
	 */
	public A_Star_Queue() {
		queue_ = new A_Star_Node[100];
	}

	/**
	 * Inserts the specified element into this priority queue.
	 * 
	 * @param node	the node to add
	 */
	public void add(A_Star_Node node) {
		int i = size_;
		if (i >= queue_.length){
			if (i + 1 < 0) throw new OutOfMemoryError();// overflow
			int oldCapacity = queue_.length;
			// Double size if small; else grow by 50%
			int newCapacity = ((oldCapacity < 64)?((oldCapacity + 1) * 2):((oldCapacity / 2) * 3));
			if (newCapacity < 0) newCapacity = Integer.MAX_VALUE; // overflow            
			if (newCapacity < i + 1) newCapacity = i + 1;
			queue_ = Arrays.copyOf(queue_, newCapacity);
		}
		size_ = i + 1;
		if (i == 0) queue_[0] = node;
		else {
			// code from siftup-function
			A_Star_Node e;
			int parent;
			while (i > 0) {
				parent = (i - 1) >>> 1;
				e = queue_[parent];
				if (node.getF() >= e.getF()) break;
				queue_[i] = e;
				i = parent;
			}
			queue_[i] = node;
		}
	}

	/**
	 * Call this function after you have set a node to a smaller f-value. This saves from first removing
	 * and later adding the element.
	 * 
	 * @param node	the node which has a smaller f-value
	 */
	public void signalDecreasedF(A_Star_Node node) {
		for (int i = 0; i < size_; ++i) {
			if (node == queue_[i]){
				// code from siftup-function
				A_Star_Node e;
				int parent;
				while (i > 0) {
					parent = (i - 1) >>> 1;
					e = queue_[parent];
					if (node.getF() >= e.getF()) break;
					queue_[i] = e;
					i = parent;
				}
				queue_[i] = node;
				break;
			}
		}
	}
	
	/**
	 * Checks if this queue is empty.
	 * 
	 * @return <code>true</code> if it's empty, else <code>false</code>
	 */
	public boolean isEmpty(){
		return (size_==0?true:false);
	}

	/**
	 * Polls (get and remove) the first element.
	 * 
	 * @return the node
	 */
	public A_Star_Node poll() {
		if (size_ == 0) return null;
		int s = --size_;
		A_Star_Node result = queue_[0];
		A_Star_Node node = queue_[s];
		queue_[s] = null;
		if (s != 0){
			// code from siftdown-function
			int pos = 0;
			int half = size_ >>> 1;        // loop while a non-leaf
			A_Star_Node c;
			int child;
			while (pos < half) {
				child = (pos << 1) + 1; // assume left child is least
				c = queue_[child];
				int right = child + 1;
				if (right < size_ && c.getF() > queue_[right].getF()) c = queue_[child = right];
				if (node.getF() <= c.getF()) break;
				queue_[pos] = c;
				pos = child;
			}
			queue_[pos] = node;
		}
		return result;
	}
	
	/**
	 * Removes a node from this queue.
	 * 
	 * @param node	the node to be removed from this queue, if present
	 */
	public void remove(A_Star_Node node) {
		for (int i = 0; i < size_; ++i) {
			if (node == queue_[i]){
				int s = --size_;
				if (s == i) queue_[i] = null;// removed last element	
				else {
					A_Star_Node moved = queue_[s];
					queue_[s] = null;
					siftDown(i, moved);
					if (queue_[i] == moved) siftUp(i, moved);
				}
			}
		}
	}

	/**
	 * Inserts a node at position <code>pos</code>, maintaining heap invariant by
	 * promoting the node up the tree until its f-value is greater than or equal to
	 * its parent, or is the root.
	 * 
	 * @param pos	the position to fill
	 * @param node	the item to insert
	 */
	private void siftUp(int pos, A_Star_Node node) {
		A_Star_Node e;
		int parent;
		while (pos > 0) {
			parent = (pos - 1) >>> 1;
			e = queue_[parent];
			if (node.getF() >= e.getF()) break;
			queue_[pos] = e;
			pos = parent;
		}
		queue_[pos] = node;
	}

	/**
	 * Inserts a node at position <code>pos</code>, maintaining heap invariant by
	 * demoting the node down the tree repeatedly until its f-value is less than or
	 * equal to its children or is a leaf.
	 * 
	 * @param pos	the position to fill
	 * @param node	the item to insert
	 */
	private void siftDown(int pos, A_Star_Node node) {
		int half = size_ >>> 1;        // loop while a non-leaf
		A_Star_Node c;
		int child;
		while (pos < half) {
			child = (pos << 1) + 1; // assume left child is least
			c = queue_[child];
			int right = child + 1;
			if (right < size_ && c.getF() > queue_[right].getF()) c = queue_[child = right];
			if (node.getF() <= c.getF()) break;
			queue_[pos] = c;
			pos = child;
		}
		queue_[pos] = node;
	}
}
