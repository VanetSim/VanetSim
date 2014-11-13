package vanetsim.gpstracing;

import vanetsim.scenario.Vehicle;

/**
 * 
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
	 * 
	 * @param vehicle
	 * @param startTime
	 */
	public void add(Vehicle vehicle, int startTime) {

		Node predecessorNode = head_;
		int i = 0;
		while (predecessorNode.getStartTime() < startTime && i < counter_) {
			predecessorNode.getSuccessor();
		}

		Node newNode = new Node(predecessorNode.getSuccessor(),
				predecessorNode, vehicle, startTime);
		predecessorNode.getSuccessor().setPredecessor(newNode);
		predecessorNode.setSuccessor(newNode);

		counter_++;
	}

	/**
	 * 
	 * @return
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
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return (counter_ == 0);
	}

	
	
	/**
	 * Nested Node class
	 *
	 */
	class Node {
		private Node successor_;
		private Node predecessor_;
		private Vehicle vehicle_;
		private int startTime_;

		public Node(Node successor, Node predecessor, Vehicle vehicle,
				int startTime) {
			successor_ = successor;
			predecessor_ = predecessor;
			vehicle_ = vehicle;
			startTime_ = startTime;
		}

		public void setSuccessor(Node node) {
			successor_ = node;
		}

		public void setPredecessor(Node node) {
			predecessor_ = node;
		}

		public Node getSuccessor() {
			return successor_;
		}

		public Node getPredecessor() {
			return predecessor_;
		}

		public int getStartTime() {
			return startTime_;
		}

		public Vehicle getVehicle() {
			return vehicle_;
		}
	}
}
