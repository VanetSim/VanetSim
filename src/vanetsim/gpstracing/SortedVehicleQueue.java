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
package vanetsim.gpstracing;

import vanetsim.scenario.Vehicle;

/**
 * SortedVehicleQueue is a Double-Linked-List, which is automatically sorted by
 * the given StartTime of a inserted Vehicle. Die smallest StartTime is on the
 * Head of the List.
 *
 */
public class SortedVehicleQueue {

	private Node head_;
	private Node tail_;
	private int counter_;

	public SortedVehicleQueue() {
		head_ = new Node(null, null, null, -1);
		tail_ = new Node(null, head_, null, -1);
		head_.setSuccessor(tail_);
		counter_ = 0;
	}

	/**
	 * Inserts a new Vehicle into the SortedVehicleQueue
	 * Therefore a Vehicle and its StartTime is needed.
	 * @param vehicle
	 * @param startTime
	 */
	public void add(Vehicle vehicle, long startTime) {

		Node predecessorNode = head_;
		int i = -1;
		do {
			predecessorNode.getSuccessor();
			i++;
		} while (predecessorNode.getStartTime() < startTime && i < counter_);

		Node newNode = new Node(predecessorNode.getSuccessor(),
				predecessorNode, vehicle, startTime);
		predecessorNode.getSuccessor().setPredecessor(newNode);
		predecessorNode.setSuccessor(newNode);

		counter_++;

	}

	/**
	 * This Function remove the first element of the SortedVehicleQueue
	 * and return it.
	 * The function is equivalent to the poll() function the Queue
	 * @return firstElement
	 */
	public Node remove() {

		Node returnNode = null;

		if (!isEmpty()) {
			returnNode = head_.getSuccessor();
			head_.setSuccessor(returnNode.getSuccessor());
			returnNode.getSuccessor().setPredecessor(head_);
			counter_--;
		}

		return returnNode;
	}

	/**
	 * This Function get the first element of the SortedVehicleQueue.
	 * The function is equivalent to the peek() function the Queue
	 * @return firstElement
	 */
	public Node peek() {
		Node result = null;

		if (!isEmpty()) {
			result = head_.getSuccessor();
		}

		return result;
	}

	/**
	 * Return whether the SortedVehicleQueue is empty 
	 * @return isEmpty
	 */
	public boolean isEmpty() {
		return (counter_ == 0);
	}

	/**
	 * Nested Node class for the SortedVehicleQueue
	 * 
	 */
	class Node {
		private Node successor_;
		private Node predecessor_;
		private Vehicle vehicle_;
		private long startTime_;

		/**
		 * 
		 * @param successor
		 * @param predecessor
		 * @param vehicle
		 * @param startTime
		 */
		public Node(Node successor, Node predecessor, Vehicle vehicle,
				long startTime) {
			successor_ = successor;
			predecessor_ = predecessor;
			vehicle_ = vehicle;
			startTime_ = startTime;
		}

		/**
		 * Function for setting the Successor
		 * @param node
		 */
		public void setSuccessor(Node node) {
			successor_ = node;
		}

		/**
		 * Function for setting the Predecessor
		 * @param node
		 */
		public void setPredecessor(Node node) {
			predecessor_ = node;
		}

		/**
		 * Function for getting the Successor
		 * @return Successor
		 */
		public Node getSuccessor() {
			return successor_;
		}

		/**
		 * Function for getting the Predecessor
		 * @return Predecessor
		 */
		public Node getPredecessor() {
			return predecessor_;
		}

		/**
		 * Function for getting the StartTime of a Vehicle
		 * @return StartTime
		 */
		public long getStartTime() {
			return startTime_;
		}

		/**
		 * Function for getting the Vehicle
		 * @return Vehicle
		 */
		public Vehicle getVehicle() {
			return vehicle_;
		}
	}
}
