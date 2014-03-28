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

import java.util.ArrayDeque;

import vanetsim.map.Node;
import vanetsim.map.Street;
import vanetsim.routing.RoutingAlgorithm;

/**
 * An implementation of the A*-algorithm. A* uses an heuristic to limit the distance calculations
 * and thus results in a much faster routing compared with Dijkstra. It also always finds the optimal
 * results like Dijkstra if the heuristic is correctly chosen (which it is in this implementation).
 * The implementation is based on the pseudocode from
 * <a href=http://de.wikipedia.org/wiki/A*-Algorithmus>German Wikipedia</a>
 * (link last time checked on 15.08.2008). However, it is modified and expanded with performance
 * optimizations and necessary changes for usage in a vanet-simulator (one-way-routes, barring...).
 * An own data structure is used which is basically the official <code>PriorityQueue</code> implementation but with
 * unnecessary function calls and casts removed. This uses about 25% less cpu than the original <code>PriorityQueue</code>
 * and about 40% less than a <code>TreeSet</code>. A basic <code>ArrayList</code> would take about 4x the performance.
 * 
 * Note for developers: It makes no sense to try to process streets which only have 2 crossings (no real junctions!) as one large street.
 * It surely saves some sqrt-operations but you trade this with lots of necessary checks and lookups and (what is a larger problem) you need
 * to rebuild the successors later because the vehicle needs a full path! Furthermore, it's also not a real A* anymore as you traverse nodes
 * which a real A* would not have checked because their f-value is too high. In a (not fully optimized) test scenario, this concept resulted 
 * in about 30% lower (!) performance so it's really not worth thinking about it.
 */
public final class A_Star_Algorithm implements RoutingAlgorithm{
	
	/**
	 * Instantiates a new A_Star_Algo.
	 */
	public A_Star_Algorithm(){
	}
	
	/**
	 * Main calculation function.
	 * 
	 * @param mode				The mode in which to operate. <code>0</code> means calculating with street lengths, <code>1</code> means calculating based on speed/time 
	 * @param direction			<code>0</code>=don't care about direction, <code>-1</code>=from startNode to endNode, <code>1</code>=from endNode to startNode
	 * @param startStreet		the street on which the start point lies
	 * @param startStreetPos	the position measured in cm from the startNode of the <code>startStreet</code>
	 * @param targetX			the x coordinate of the target point
	 * @param targetY			the y coordinate of the target point
	 * @param targetStreet		the street on which the target point lies
	 * @param targetStreetPos	the position measured in cm from the startNode of the <code>targetStreet</code>
	 * @param penaltyStreets	an array with all streets which have penalties.
	 * @param penaltyDirections	an array with directions corresponding to penaltyStreets. <code>1</code> in the array means from endNode to startNode, 
	 * 							<code>0</code> means both directions and <code>-1</code> means from startNode to endNode
	 * @param penalties			an array with all penalties measured in cm.
	 * @param penaltySize		how many penalties exist.
	 * @param additionalVar 	can be used to set the maximum speed for calculations in <code>mode=1</code>
	 * 
	 * @return an A_Star_Node which allows reconstructing the optimal path by going through the predecessors!
	 */
	private A_Star_Node computeRoute(int mode, int direction, Street startStreet, double startStreetPos, int targetX, int targetY, Street targetStreet, double targetStreetPos, Street[] penaltyStreets, int[] penaltyDirections, int[] penalties, int penaltySize, int additionalVar){
		int distanceAdd;
		long dx, dy;
		double f, g, distance;
		boolean target1found = false, target2found = false, endNodeMayBeDestination;
		int speed;
		Node tmpNode;
		int i, j;
		A_Star_Node currentNode, successor, startNode;
		Street[] outgoingStreets;
		Street tmpStreet;
		A_Star_Queue openList = new A_Star_Queue();
		
		// get LookupTable from factory. The LookupTable is needed for a mapping between our normal map nodes and the nodes for routing
		int[] tmp = new int[1];
		A_Star_LookupTable<Node, A_Star_Node> lookupTable = A_Star_LookupTableFactory.getTable(tmp);
		int counter = tmp[0];
		
		if(targetStreet.isOneway()) endNodeMayBeDestination = false;
		else endNodeMayBeDestination = true;
		// penalties are not considered for the first node as it should not be possible to escape from them 
		if(direction > -1){
			startNode = lookupTable.get(startStreet.getStartNode());
			if(startNode == null){		// not yet in table => put it
				startNode = new A_Star_Node(startStreet.getStartNode(), counter);
				lookupTable.put(startStreet.getStartNode(), startNode);
			} else {
				startNode.reset(counter);
				startNode.setPredecessor(null);
			}
			if(mode == 0){
				startNode.setF(startStreetPos);
				startNode.setG(startStreetPos);
			}
			else {	//time calculation
				if(startStreet.getSpeed() > additionalVar) speed = additionalVar;
				else speed = startStreet.getSpeed();
				startNode.setF(startStreetPos/speed);
				startNode.setG(startStreetPos/speed);
			}	
			startNode.setInOpenList(true);
			openList.add(startNode);
		}
		if(direction < 1){
			startNode = lookupTable.get(startStreet.getEndNode());
			if(startNode == null){		// not yet in table => put it
				startNode = new A_Star_Node(startStreet.getEndNode(), counter);
				lookupTable.put(startStreet.getEndNode(), startNode);
			} else {
				startNode.reset(counter);
				startNode.setPredecessor(null);
			}
			if(mode == 0){
				startNode.setF(startStreet.getLength() - startStreetPos);
				startNode.setG(startStreet.getLength() - startStreetPos);
			}
			else {	//time calculation
				if(startStreet.getSpeed() > additionalVar) speed = additionalVar;
				else speed = startStreet.getSpeed();
				startNode.setF((startStreet.getLength() - startStreetPos)/speed);
				startNode.setG((startStreet.getLength() - startStreetPos)/speed);
			}
			startNode.setInOpenList(true);
			openList.add(startNode);
		}
		do{
			// take and remove node with smallest f value (=first element)
			currentNode = openList.poll();
			// found target?
			if (endNodeMayBeDestination && currentNode.getRealNode() == targetStreet.getEndNode()){
				if(target1found){
					A_Star_LookupTableFactory.putTable(counter, lookupTable);
					return currentNode;
				}
				else {	//we're near the end but didn't add the costs for the last street yet
					if(mode == 0) f = currentNode.getF() + (targetStreet.getLength() - targetStreetPos);
					else {
						if(targetStreet.getSpeed() > additionalVar) speed = additionalVar;
						else speed = targetStreet.getSpeed();
						f = currentNode.getF() + ((targetStreet.getLength() - targetStreetPos)/speed);
					}
					currentNode.setF(f);
					currentNode.setG(f);
					openList.add(currentNode);	//the poll() has removed it but we need it again!
					target1found = true;
				}
			} else if(currentNode.getRealNode() == targetStreet.getStartNode()){
				if(target2found){
					A_Star_LookupTableFactory.putTable(counter, lookupTable);
					return currentNode;
				}
				else {	//we're near the end but didn't add the costs for the last street yet
					if(mode == 0) f = currentNode.getF() + targetStreetPos;
					else {	//time calculation
						if(targetStreet.getSpeed() > additionalVar) speed = additionalVar;
						else speed = targetStreet.getSpeed();
						f = currentNode.getF() + (targetStreetPos/speed);
					}
					currentNode.setF(f);
					currentNode.setG(f);
					openList.add(currentNode);	//the poll() has removed it but we need it again!
					target2found = true;
				}
			// not yet at target. Check all streets going out from this node
			} else {
				outgoingStreets = currentNode.getRealNode().getOutgoingStreets();	// takes automatically care of one-way-routes as the list only contains correct streets!
				for(i = 0; i < outgoingStreets.length; ++i){
					tmpStreet = outgoingStreets[i];
					tmpNode = tmpStreet.getStartNode();
					if (tmpNode == currentNode.getRealNode()) tmpNode = tmpStreet.getEndNode();		//get the next node and not the same again
					
					successor = lookupTable.get(tmpNode);		// get an A_Star_Node from an ordinary node
					if(successor == null){		// not yet in table => put it
						successor = new A_Star_Node(tmpNode, counter);
						lookupTable.put(tmpNode, successor);
					} else {
						if(successor.getCounter() != counter) successor.reset(counter);
					}
					
					// only treat this node when not already on ClosedList!
					if (successor.isInClosedList() == false){
						// find penalties
						distanceAdd = 0;
						if(penaltySize > 0){
							if(tmpStreet.getStartNode() == currentNode.getRealNode()){
								for(j = 0; j < penaltySize; ++j){
									if(penaltyStreets[j] == tmpStreet && penaltyDirections[j] < 1){
										if(distanceAdd < penalties[j]) distanceAdd = penalties[j];
									}
								}
							} else {
								for(j = 0; j < penaltySize; ++j){
									if(penaltyStreets[j] == tmpStreet && penaltyDirections[j] > -1){
										if(distanceAdd < penalties[j]) distanceAdd = penalties[j];
									}
								}
							}
							
						}
						
						dx = targetX - tmpNode.getX();
						dy = targetY - tmpNode.getY();
						distance = distanceAdd + Math.sqrt(dx * dx + dy * dy); 	// Pythagorean theorem: a^2 + b^2 = c^2
						
						if(mode == 0){	//distance calculation
							g = currentNode.getG() + tmpStreet.getLength();
							f = g + distance;
						} else {	//time calculation
							if(tmpStreet.getSpeed() > additionalVar) g = currentNode.getG() + (tmpStreet.getLength()/additionalVar);
							else g = currentNode.getG() + (tmpStreet.getLength()/tmpStreet.getSpeed());
							f = g + (distance/additionalVar);	//approximation based on maxspeed (stored in additionalVar) so that real time is always underestimated!
						}
						if(!successor.isInOpenList()){		// not yet investigated...
							successor.setPredecessor(currentNode);
							successor.setF(f);
							successor.setG(g);
							successor.setInOpenList(true);
							openList.add(successor);
						} else if (successor.getF() > f){		// previously found but now has better value
							if(target1found && successor.getRealNode() == targetStreet.getEndNode()){	//if the target street has a low speed we might overwrite it here with a wrong guessed value => calculate it precise!
								if(mode == 0) f = g + (targetStreet.getLength() - targetStreetPos);
								else {
									if(targetStreet.getSpeed() > additionalVar) speed = additionalVar;
									else speed = targetStreet.getSpeed();
									f = g + ((targetStreet.getLength() - targetStreetPos)/speed);
								}
								if(successor.getF() > f){
									successor.setPredecessor(currentNode);
									successor.setF(f);
									successor.setG(g);
									openList.signalDecreasedF(successor);
								}
							} else if(target2found && successor.getRealNode() == targetStreet.getStartNode()){
								if(mode == 0) f = g + targetStreetPos;
								else {	//time calculation
									if(targetStreet.getSpeed() > additionalVar) speed = additionalVar;
									else speed = targetStreet.getSpeed();
									f = g + (targetStreetPos/speed);
								}
								if(successor.getF() > f){
									successor.setPredecessor(currentNode);
									successor.setF(f);
									successor.setG(g);
									openList.signalDecreasedF(successor);
								}
							} else {	// the "normal" case is this one!
								successor.setPredecessor(currentNode);
								successor.setF(f);
								successor.setG(g);
								openList.signalDecreasedF(successor);
							}
						}
					}
				}
		        // current node has been completely investigated
				currentNode.setInClosedList(true);
				currentNode.setInOpenList(false);
			}
	    } while (!openList.isEmpty());
	    // there's no route to the destination!
		A_Star_LookupTableFactory.putTable(counter, lookupTable);
		return null;	    
	}
	
	/**
	 * Gets a routing result.
	 * 
	 * @param mode				The mode in which to operate. <code>0</code> means calculating with street lengths, <code>1</code> means calculating based on speed/time 
	 * @param direction			<code>0</code>=don't care about direction, <code>-1</code>=from startNode to endNode, <code>1</code>=from endNode to startNode
	 * @param startX			the x coordinate of the start point
	 * @param startY			the y coordinate of the start point
	 * @param startStreet		the street on which the start point lies
	 * @param startStreetPos	the position measured in cm from the startNode of the <code>startStreet</code>
	 * @param targetX			the x coordinate of the target point
	 * @param targetY			the y coordinate of the target point
	 * @param targetStreet		the street on which the target point lies
	 * @param targetStreetPos	the position measured in cm from the startNode of the <code>targetStreet</code>
	 * @param penaltyStreets	an array with all streets which have penalties.
	 * @param penaltyDirections	an array with directions corresponding to penaltyStreets. <code>1</code> in the array means from endNode to startNode, 
	 * 							<code>0</code> means both directions and <code>-1</code> means from startNode to endNode
	 * @param penalties			an array with all penalties measured in cm.
	 * @param penaltySize		how many penalties exist.
	 * @param additionalVar		can be used to set the maximum speed for calculations in <code>mode=1</code>
	 *
	 * 
	 * @return An <code>ArrayDeque</code> for returning the result. The first element will be the start node and the last will be the end node of the routing.
	 * 
	 * @see	vanetsim.routing.RoutingAlgorithm#getRouting(int, int, int, int, Street, double, int, int, Street, double, Street[], int[], int[], int, int)
	 */
	public ArrayDeque<Node> getRouting(int mode, int direction, int startX, int startY, Street startStreet, double startStreetPos, int targetX, int targetY, Street targetStreet, double targetStreetPos, Street[] penaltyStreets, int[] penaltyDirections, int[] penalties, int penaltySize, int additionalVar){
		A_Star_Node curNode = computeRoute(mode, direction, startStreet, startStreetPos, targetX, targetY, targetStreet, targetStreetPos, penaltyStreets, penaltyDirections, penalties, penaltySize, additionalVar);
		ArrayDeque<Node> result = new ArrayDeque<Node>(255);
		while(curNode != null){
			result.addFirst(curNode.getRealNode());
			curNode = curNode.getPredecessor();
		}
		return result;
	}
}