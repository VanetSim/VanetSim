/*
 * VANETsim open source project - http://www.vanet-simulator.org
 * Copyright (C) 2008 - 2013  Andreas Tomandl, Florian Scheuer, Bernhard Gruber
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vanetsim.routing.A_Star;

import vanetsim.ErrorLog;
import vanetsim.localization.Messages;
import vanetsim.map.Node;

/**
 * A node specific for the computation in an A*-algorithm.
 */ 
public final class A_Star_Node implements Comparable<Object>{
	
	/** The real node in the map corresponding to this A*-node. */
	private final Node realNode_;
	
	/** If this node is in the so-called OpenList (=list containing nodes which need to be evaluated). */
	private boolean inOpenList_ = false;
	
	/** If this node is in the so-called ClosedList (=list containing nodes which have been fully evaluated. */
	private boolean inClosedList_ = false;
	
	/** The approximate distance/time/penalty/... from this point to the target. Called f-value in the docs to this algorithm! */
	private double f_;
	
	/** The distance/time/penalty... from this point to the beginning. Called g-value in the docs to this algorithm! */
	private double g_;
	
	/** A link to the predecessor in the path found. This is necessary to reconstruct the result path at the end of the whole routing calculation! */
	private A_Star_Node predecessor_ = null;
	
	/** A value to determine if this node is valid for the current routing calculation (if not, this node must be reset). */
	private int counter_ = 0;
	
	/**
	 * Instantiates a new A_Star_Node from a node existing on the map.
	 * 
	 * @param realNode	the real node from the map associated with this A*-specific node
	 * @param f		the initial value for the approximate distance/time/penalty... to the target
	 * @param counter	a value to determine if this node is valid for the current routing calculation
	 */
	public A_Star_Node(Node realNode, double f, int counter){
		counter_ = counter;
		realNode_ = realNode;
		f_ = f;
	}
	
	/**
	 * Instantiates a new A_Star_Node from a node existing on the map. The f value is set to 0.
	 * 
	 * @param realNode	the real node from the map associated with this A*-specific node
	 * @param counter	a value to determine if this node is valid for the current routing calculation
	 */
	public A_Star_Node(Node realNode, int counter){
		counter_ = counter;
		realNode_ = realNode;
		f_ = 0;
	}
	
	/**
	 * Resets the values of the node to the default values so that it can be reused. If the node is already valid
	 * for the current routing run, nothing is done.
	 * 
	 * @param counter	a value to determine if this node is valid for the current routing calculation
	 */
	public void reset(int counter){
		counter_ = counter;
		f_ = 0;
		inOpenList_ = false;
		inClosedList_ = false;
	}
	
	/**
	 * Returns the current value for the counter
	 * 
	 * @return the counter value
	 */
	public int getCounter(){
		return counter_;
	}
	
	/**
	 * Returns if this node is in the ClosedList.
	 * 
	 * @return <code>true</code> if node is already in ClosedList, else <code>false</code>
	 */
	public boolean isInClosedList(){
		return inClosedList_;
	}
	
	/**
	 * Sets if this node is in the ClosedList or not.
	 * 
	 * @param state	<code>true</code> if this node shall be in the ClosedList, else <code>false</code>
	 */
	public void setInClosedList(boolean state){
		inClosedList_ = state;
	}
	
	/**
	 * Returns if this node is in the OpenList.
	 * 
	 * @return <code>true</code> if node is already in OpenList, else <code>false</code>
	 */
	public boolean isInOpenList(){
		return inOpenList_;
	}
	
	/**
	 * Sets if this node is in the OpenList or not.
	 * 
	 * @param state	<code>true</code> if this node shall be in the OpenList, else <code>false</code>
	 */
	public void setInOpenList(boolean state){
		inOpenList_ = state;
	}
	
	/**
	 * Gets the f value (=approximated distance/time/penalty... from this point to the target).
	 * 
	 * @return the f value
	 */
	public double getF(){
		return f_;
	}
	
	/**
	 * Sets the f value (=approximated distance/time/penalty... from this point to the target).
	 * 
	 * @param f	the new f value
	 */
	public void setF(double f){
		f_ = f;
	}

	/**
	 * Gets the g value (=distance/time/penalty... from this point to the beginning).
	 * 
	 * @return the g value
	 */
	public double getG(){
		return g_;
	}
	
	/**
	 * Sets the g value (=distance/time/penalty... from this point to the beginning).
	 * 
	 * @param g	the new g value
	 */
	public void setG(double g){
		g_ = g;
	}
	
	/**
	 * Gets the predecessor of this node.
	 * 
	 * @return the predecessor
	 */
	public A_Star_Node getPredecessor(){
		return predecessor_;
	}
	
	/**
	 * Sets the predecessor of this node.
	 * 
	 * @param predecessor	the new predecessor
	 */
	public void setPredecessor(A_Star_Node predecessor){
		predecessor_ = predecessor;
	}
	
	/**
	 * Gets the "real" node in the map associated with this A*-specific node.
	 * 
	 * @return the real node
	 */
	public Node getRealNode(){
		return realNode_;
	}
	
	/**
	 * Function needed to implement the <code>Comparable</code> interface. This allows automatic sorting of this node
	 * in a <code>SortedSet</code> like a <code>TreeSet</code> based on it's f-value.
	 * 
	 * @param other	object to compare this node with
	 * 
	 * @return <code>0</code> if <code>other</code> is the same, <code>1</code> if this is greater than <code>other</code> and <code>-1</code> if this is less than <code>other</code>
	 */
	public int compareTo(Object other){
		if(this == other) return 0;
		A_Star_Node otherNode = (A_Star_Node)other;  //throws a ClassCastException like expected by Comparable so we don't need an additional check!
		if(f_ > otherNode.getF()) return 1;
		else if(f_ < otherNode.getF()) return -1;
		else {		// should almost never happen but just returning 0 would not allow storing two in reality different nodes with the same f in a tree!
			if(this.hashCode() < other.hashCode()) return -1;
			else if(this.hashCode() > other.hashCode()) return 1;
			else{		// should happen even less probable as hashcode is normally built from the internal address of the object according to javadoc...
				if(realNode_.getX() > otherNode.getRealNode().getX()) return -1;
				else if(realNode_.getX() < otherNode.getRealNode().getX()) return 1;
				else{
					if(realNode_.getY() > otherNode.getRealNode().getY()) return -1;
					else if(realNode_.getY() < otherNode.getRealNode().getY()) return 1;
					else{		//should really never happen...would mean these are nodes in the absolute same position stacked on each other and that both also accidentally got the same hashcode!
						ErrorLog.log(Messages.getString("A_Star_Node.NodeCompareError"), 7, A_Star_Node.class.getName(), "compareTo", null); //$NON-NLS-1$ //$NON-NLS-2$
						return 0;
					}
				}
			}
		}
	}
}