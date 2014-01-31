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
package vanetsim.map;

import vanetsim.scenario.Vehicle;

/**
 * This class holds some geometric functions needed for various calculations on the map. It's just a helper
 * class to make the map class smaller and as this class has no variables, all functions are declared static.
 */
public final class MapHelper{

	/**
	 * Calculate the distance between a point and a street.
	 * 
	 * @param street	the street given
	 * @param x 		x coordinate of the point
	 * @param y 		y coordinate of the point
	 * @param sqrt		if set to <code>true</code>, the correct distance is returned; if set to <code>false</code> the square of the distance is returned
	 * 					(little bit faster as it saves a call to <code>Math.sqrt()</code>; however, use only if you can handle this!)
	 * @param result	an array holding the point on the street so that it can be returned. <code>result[0]</code> holds the x coordinate,
	 * 					<code>result[1]</code> the y coordinate. Make sure the array has the correct size (2 elements)!
	 * 
	 * @return the distance as a <code>double</code>. If nothing was found, <code>Double.MAX_VALUE</code> is returned.
	 */
	public static double calculateDistancePointToStreet(Street street, int x, int y, boolean sqrt, int[] result){
		if(findNearestPointOnStreet(street, x, y, result)){
			// we got the nearest point on the line. Now calculate the distance between this nearest point and the given point
			long tmp1 = (long)result[0] - x;	//long because x could be smaller 0 and result[0] could be Integer.MAX_VALUE!
			long tmp2 = (long)result[1] - y;
			if(sqrt) return Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2); 	// Pythagorean theorem: a^2 + b^2 = c^2	
			else return (tmp1 * tmp1 + tmp2 * tmp2);
		} else return Double.MAX_VALUE;
	}

	/**
	 * Calculates the point ON a street which is nearest to a given point (for snapping or such things).
	 * This code was inspired by <a href="http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/">
	 * Paul Bourke's homepage</a>, especially the Delphi sourcecode (link last visited on 15.08.2008). See
	 * there for the mathematical background of this calculation!
	 * 
	 * @param street the street
	 * @param x 		the x coordinate of the point
	 * @param y 		the y coordinate of the point
	 * @param result	an array for the result. <code>result[0]</code> holds the x coordinate, <code>result[1]</code> the y coordinate. Make sure
	 * 					the array has the correct size (2 elements), otherwise you will not get a result!
	 * 
	 * @return <code>true</code> if calculation was successful, else <code>false</code>
	 */
	public static boolean findNearestPointOnStreet(Street street, int x, int y, int[] result){	        
		if(result.length == 2){
			int p1_x = street.getStartNode().getX();
			int p1_y = street.getStartNode().getY();
			int p2_x = street.getEndNode().getX();
			int p2_y = street.getEndNode().getY();
			long tmp1 = p2_x-p1_x;
			long tmp2 = p2_y-p1_y;
			long tmp3 = (tmp1*tmp1 + tmp2*tmp2);
			if(tmp3 != 0){
				double u = (((double)x-p1_x)*((double)p2_x-p1_x)+((double)y-p1_y)*((double)p2_y-p1_y))/tmp3;			        
				if (u >= 1.0){		//point is "outside" the line and nearest to the EndNode
					result[0] = p2_x;
					result[1] = p2_y;
				}
				else if (u <= 0.0){		//point is "outside" the line and nearest to the StartNode
					result[0] = p1_x;
					result[1] = p1_y;
				}
				else{
					double tmp4 = p1_x + u * tmp1;
					result[0] = (int) (tmp4 + 0.5);	//manual rounding
					tmp4 = p1_y + u * tmp2;
					result[1] = (int) (tmp4 + 0.5);
				}
			} else {	// not a real street...EndNode and StartNode have the same coordinates!
				result[0] = p1_x;
				result[1] = p1_y;
			}
			return true;
		} else return false;
	}

	/**
	 * Returns the nearest street to a given point. First all regions are calculated which are within <code>maxDistance</code>. Then, ALL
	 * streets in these regions are checked if they are within this <code>maxDistance</code> and the best one is returned (if any exists).
	 * 
	 * @param x 			the x coordinate of the given point
	 * @param y 			the x coordinate of the given point
	 * @param maxDistance	the maximum distance; use <code>Integer.MAX_VALUE</code> if you just want to get any nearest street but note
	 * 						that this costs a lot of performance because ALL regions and ALL streets are checked!
	 * @param distance 		an array used to return the distance between the nearest point and the point given. This should be a <code>double[1]</code> array!
	 * @param nearestPoint 	an array used to return the x-coordinate (<code>nearestpoint[0]</code>) and y-coordinate (<code>nearestpoint[1]</code>)
	 * 						on the street.
	 * 
	 * @return the nearest street or <code>null</code> if none was found or an error occured
	 */
	public static Street findNearestStreet(int x, int y, int maxDistance, double[] distance, int[] nearestPoint){
		Map map = Map.getInstance();
		Region[][] Regions = map.getRegions();
		if(Regions != null && nearestPoint.length >1 && distance.length > 0){
			int mapMinX, mapMinY, mapMaxX, mapMaxY, regionMinX, regionMinY, regionMaxX, regionMaxY;
			int[] tmpPoint = new int[2];
			Street[] streets;
			int i, j, k, size;
			Street bestStreet = null;
			double tmpDistance, bestDistance = Double.MAX_VALUE;
			long maxDistanceSquared = (long)maxDistance * maxDistance;

			// Minimum x coordinate to be considered
			long tmp = x - maxDistance;
			if (tmp < 0) mapMinX = 0;		// Map stores only positive coordinates
			else if(tmp < Integer.MAX_VALUE) mapMinX = (int) tmp;
			else mapMinX = Integer.MAX_VALUE;

			// Maximum x coordinate to be considered
			tmp = x + (long)maxDistance;
			if (tmp < 0) mapMaxX = 0;
			else if(tmp < Integer.MAX_VALUE) mapMaxX = (int) tmp;
			else mapMaxX = Integer.MAX_VALUE;

			// Minimum y coordinate to be considered
			tmp = y - maxDistance;
			if (tmp < 0) mapMinY = 0;
			else if(tmp < Integer.MAX_VALUE) mapMinY = (int) tmp;
			else mapMinY = Integer.MAX_VALUE;

			// Maximum y coordinate to be considered
			tmp = y + (long)maxDistance;
			if (tmp < 0) mapMaxY = 0;
			else if(tmp < Integer.MAX_VALUE) mapMaxY = (int) tmp;
			else mapMaxY = Integer.MAX_VALUE;

			// Get the regions to be considered
			Region tmpregion = map.getRegionOfPoint(mapMinX, mapMinY);
			regionMinX = tmpregion.getX();
			regionMinY = tmpregion.getY();

			tmpregion = map.getRegionOfPoint(mapMaxX, mapMaxY);
			regionMaxX = tmpregion.getX();
			regionMaxY = tmpregion.getY();

			// only iterate through those regions which are within the distance
			for(i = regionMinX; i <= regionMaxX; ++i){
				for(j = regionMinY; j <= regionMaxY; ++j){
					streets = Regions[i][j].getStreets();
					size = streets.length;
					for(k = 0; k < size; ++k){
						tmpDistance = calculateDistancePointToStreet(streets[k], x, y, false, tmpPoint);
						if(tmpDistance < maxDistanceSquared && tmpDistance < bestDistance){
							bestDistance = tmpDistance;
							bestStreet = streets[k];
							nearestPoint[0] = tmpPoint[0];
							nearestPoint[1] = tmpPoint[1];
						}
					}
				}
			}
			distance[0] = bestDistance;
			return bestStreet;
		} else return null;
	}

	/**
	 * Returns the nearest vehicle to a given point. First all regions are calculated which are within <code>maxDistance</code>. Then, ALL
	 * vehicles in these regions are checked if they are within this <code>maxDistance</code> and the best one is returned (if any exists).
	 * Note that only vehicles which are currently active will be returned!
	 * 
	 * @param x 			the x coordinate of the given point
	 * @param y 			the x coordinate of the given point
	 * @param maxDistance	the maximum distance; use <code>Integer.MAX_VALUE</code> if you just want to get any nearest vehicle but note
	 * 						that this costs a lot of performance because ALL regions and ALL vehicles are checked!
	 * @param distance		an array used to return the distance between the nearest point and the point given. This should be a <code>long[1]</code> array!
	 * 
	 * @return the nearest street or <code>null</code> if none was found or an error occured
	 */
	public static Vehicle findNearestVehicle(int x, int y, int maxDistance, long[] distance){
		Map map = Map.getInstance();
		Region[][] Regions = map.getRegions();
		if(Regions != null && distance.length > 0){
			int mapMinX, mapMinY, mapMaxX, mapMaxY, regionMinX, regionMinY, regionMaxX, regionMaxY;
			Vehicle[] vehicles;
			int i, j, k, size;
			long dx, dy;
			Vehicle tmpVehicle, bestVehicle = null;
			long tmpDistance, bestDistance = Long.MAX_VALUE;
			long maxDistanceSquared = (long)maxDistance * maxDistance;

			// Minimum x coordinate to be considered
			long tmp = x - maxDistance;
			if (tmp < 0) mapMinX = 0;		// Map stores only positive coordinates
			else if(tmp < Integer.MAX_VALUE) mapMinX = (int) tmp;
			else mapMinX = Integer.MAX_VALUE;

			// Maximum x coordinate to be considered
			tmp = x + (long)maxDistance;
			if (tmp < 0) mapMaxX = 0;
			else if(tmp < Integer.MAX_VALUE) mapMaxX = (int) tmp;
			else mapMaxX = Integer.MAX_VALUE;

			// Minimum y coordinate to be considered
			tmp = y - maxDistance;
			if (tmp < 0) mapMinY = 0;
			else if(tmp < Integer.MAX_VALUE) mapMinY = (int) tmp;
			else mapMinY = Integer.MAX_VALUE;

			// Maximum y coordinate to be considered
			tmp = y + (long)maxDistance;
			if (tmp < 0) mapMaxY = 0;
			else if(tmp < Integer.MAX_VALUE) mapMaxY = (int) tmp;
			else mapMaxY = Integer.MAX_VALUE;

			// Get the regions to be considered
			Region tmpregion = map.getRegionOfPoint(mapMinX, mapMinY);
			regionMinX = tmpregion.getX();
			regionMinY = tmpregion.getY();

			tmpregion = map.getRegionOfPoint(mapMaxX, mapMaxY);
			regionMaxX = tmpregion.getX();
			regionMaxY = tmpregion.getY();

			// only iterate through those regions which are within the distance
			for(i = regionMinX; i <= regionMaxX; ++i){
				for(j = regionMinY; j <= regionMaxY; ++j){
					vehicles = Regions[i][j].getVehicleArray();
					size = vehicles.length;
					for(k = 0; k < size; ++k){
						tmpVehicle = vehicles[k];
						if(tmpVehicle.isActive() && tmpVehicle.getX() >= mapMinX && tmpVehicle.getX() <= mapMaxX && tmpVehicle.getY() >= mapMinY && tmpVehicle.getY() <= mapMaxY){	// precheck with a recangular bounding box should sort out most cases
							dx = tmpVehicle.getX() - x;
							dy = tmpVehicle.getY() - y;
							tmpDistance = dx * dx + dy * dy;
							if(tmpDistance < maxDistanceSquared && tmpDistance < bestDistance){
								bestDistance = tmpDistance;
								bestVehicle = tmpVehicle;
							}
						}

					}
				}
			}
			distance[0] = bestDistance;
			return bestVehicle;
		} else return null;
	}

	/**
	 * Returns the nearest node to a given point. First all regions are calculated which are within <code>maxDistance</code>. Then, ALL
	 * nodes in these regions are checked if they are within this <code>maxDistance</code> and the best one is returned (if any exists).
	 * 
	 * @param x 			the x coordinate of the given point
	 * @param y 			the x coordinate of the given point
	 * @param maxDistance 	the maximum distance; use <code>Integer.MAX_VALUE</code> if you just want to get any nearest vehicle but note
	 * 						that this costs a lot of performance because ALL regions and ALL nodes are checked!
	 * @param distance 		an array used to return the distance between the nearest point and the point given. This should be a <code>long[1]</code> array!
	 * 
	 * @return the nearest node or <code>null</code> if none was found or an error occured
	 */
	public static Node findNearestNode(int x, int y, int maxDistance, long[] distance){
		Map map = Map.getInstance();
		Region[][] Regions = map.getRegions();
		if(Regions != null && distance.length > 0){
			int mapMinX, mapMinY, mapMaxX, mapMaxY, regionMinX, regionMinY, regionMaxX, regionMaxY;
			Node[] nodes;
			int i, j, k;
			Node tmpNode, bestNode = null;
			long tmpDistance, bestDistance = Long.MAX_VALUE;
			long maxDistanceSquared = (long)maxDistance * maxDistance;

			// Minimum x coordinate to be considered
			long tmp = x - maxDistance;
			if (tmp < 0) mapMinX = 0;	// Map stores only positive coordinates
			else if(tmp < Integer.MAX_VALUE) mapMinX = (int) tmp;
			else mapMinX = Integer.MAX_VALUE;

			// Maximum x coordinate to be considered
			tmp = x + (long)maxDistance;
			if (tmp < 0) mapMaxX = 0;
			else if(tmp < Integer.MAX_VALUE) mapMaxX = (int) tmp;
			else mapMaxX = Integer.MAX_VALUE;

			// Minimum y coordinate to be considered
			tmp = y - maxDistance;
			if (tmp < 0) mapMinY = 0;
			else if(tmp < Integer.MAX_VALUE) mapMinY = (int) tmp;
			else mapMinY = Integer.MAX_VALUE;

			// Maximum y coordinate to be considered
			tmp = y + (long)maxDistance;
			if (tmp < 0) mapMaxY = 0;
			else if(tmp < Integer.MAX_VALUE) mapMaxY = (int) tmp;
			else mapMaxY = Integer.MAX_VALUE;

			// Get the regions to be considered
			Region tmpregion = map.getRegionOfPoint(mapMinX, mapMinY);
			regionMinX = tmpregion.getX();
			regionMinY = tmpregion.getY();

			tmpregion = map.getRegionOfPoint(mapMaxX, mapMaxY);
			regionMaxX = tmpregion.getX();
			regionMaxY = tmpregion.getY();

			long dx, dy;

			// only iterate through those regions which are within the distance
			for(i = regionMinX; i <= regionMaxX; ++i){
				for(j = regionMinY; j <= regionMaxY; ++j){
					nodes = Regions[i][j].getNodes();
					for(k = 0; k < nodes.length; ++k){
						tmpNode = nodes[k];
						if(tmpNode.getX() >= mapMinX && tmpNode.getX() <= mapMaxX && tmpNode.getY() >= mapMinY && tmpNode.getY() <= mapMaxY){	// precheck with a recangular bounding box should sort out most cases
							dx = tmpNode.getX() - x;
							dy = tmpNode.getY() - y;
							tmpDistance = dx * dx + dy * dy;
							if(tmpDistance < maxDistanceSquared && tmpDistance < bestDistance){
								bestDistance = tmpDistance;
								bestNode = tmpNode;
							}
						}
					}
				}
			}
			distance[0] = bestDistance;
			return bestNode;
		} else return null;
	}

	/**
	 * Finds an intersection between two segments.<br>
	 * Code basically from <a href="http://workshop.evolutionzone.com/2007/09/10/code-2d-line-intersection/">
	 * CODE & FORM blog from Marius Watz</a> (link last visited: 23.09.2008).
	 * 
	 * @param x1 		the x coordinate of the first point of the first segment
	 * @param y1 		the y coordinate of the first point of the first segment
	 * @param x2 		the x coordinate of the second point of the first segment
	 * @param y2 		the y coordinate of the second point of the first segment
	 * @param x3 		the x coordinate of the first point of the second segment
	 * @param y3 		the y coordinate of the first point of the second segment
	 * @param x4 		the x coordinate of the second point of the second segment
	 * @param y4 		the y coordinate of the second point of the second segment
	 * @param result	an array to return the intersection point. The x coordinate will be in <code>result[0]</code>,
	 * 					the y coordinate in <code>result[1]</code>.
	 * 
	 * @return <code>true</code> if lines intersect, <code>false</code> if lines don't intersect or if you didn't supply
	 * a valid <code>result</code> array.
	 */
	public static boolean findSegmentIntersection(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, double[] result){   
		if(result.length == 2){
			// calculate differences   
			double dx1 = x2 - x1;   
			double dx2 = x4 - x3;   
			double dy1 = y2 - y1;   
			double dy2 = y4 - y3;   
			double dx3 = x1 - x3;   
			double dy3 = y1 - y3;     

			// calculate the lengths of the two lines   
			double len1 = Math.sqrt(dx1*dx1 + dy1*dy1);   
			double len2 = Math.sqrt(dx2*dx2 + dy2*dy2);   

			if(Math.abs((dx1*dx2 + dy1*dy2)/(len1*len2))==1) return false;   //lines are parallel => no intersection 

			// find intersection point between two lines   
			double div = dy2*dx1 - dx2*dy1;   
			double ua = (dx2*dy3 - dy2*dx3)/div;  
			double resX = x1 + ua*dx1;   
			double resY = y1 + ua*dy1;   

			// calculate the combined length of the two segments between point 1 and 2   
			dx1 = resX - x1;   
			dx2 = resX - x2;   
			dy1 = resY - y1;   
			dy2 = resY - y2; 
			double segmentLen = Math.sqrt(dx1*dx1 + dy1*dy1) + Math.sqrt(dx2*dx2 + dy2*dy2);   
			if(Math.abs(len1-segmentLen)>0.01) return false; 

			// calculate the combined length of the two segments between point 3 and 5  
			dx1 = resX - x3;   
			dx2 = resX - x4;   
			dy1 = resY - y3;   
			dy2 = resY - y4;   
			segmentLen = Math.sqrt(dx1*dx1 + dy1*dy1) + Math.sqrt(dx2*dx2 + dy2*dy2);   
			if(Math.abs(len2-segmentLen)>0.01) return false;   

			result[0] = resX;
			result[1] = resY;
			return true;
		} else return false;
	} 
	
	/**
	 * Finds an intersection between a line and a circle. Code basically from <a href="http://www.vb-helper.com/howto_net_line_circle_intersections.html">
	 * VB Helper: Find the points where a line intersects a circle in Visual Basic .NET</a> (link last visited: 31.10.2008).
	 * 
	 * @param circleX		The x coordinate of the middle of the circle
	 * @param circleY		The y coordinate of the middle of the circle
	 * @param circleRadius	The radius of the circle
	 * @param x1			The x coordinate of the start point of the line
	 * @param y1			The y coordinate of the start point of the line
	 * @param x2			The x coordinate of the end point of the line
	 * @param y2			The y coordinate of the end point of the line
	 * @param onlyOnSegment	If <code>true</code>, only intersection points are found which are on the line segment, else intersections on the extension
	 * 						of the line segment are also found! 
	 * @param result		An array to return the result. Should be an int[4]. The x coordinate of the first point will be in <code>result[0]</code>,
	 * 						the y coordinate in <code>result[1]</code>. The x coordinate of the second point will be in <code>result[2]</code>,
	 * 						the y coordinate in <code>result[3]</code>.
	 * 
	 * @return The amount of intersections found (0-2).
	 */
	public static int findLineCircleIntersections(int circleX, int circleY, int circleRadius, int x1, int y1, int x2, int y2, boolean onlyOnSegment, int[] result){
		if(result.length == 4){
			double dx1 = x2 - x1;
			double dy1 = y2 - y1;
			double dx2 = x1 - circleX;
			double dy2 = y1 - circleY;
			double a = dx1 * dx1 + dy1 * dy1;
			double b = 2 * (dx1 * dx2 + dy1 * dy2);
			double c = dx2 * dx2 + dy2 * dy2 - ((double)circleRadius * circleRadius);
			//solve ax^2 + bx + c = 0
			double det = b * b - 4 * a * c;
		    if (a <= 0.0000001 || det < 0){		//No solution
		        return 0;
		    } else if (det == 0){	//One solution
		    	double t = -b / (2 * a);
		    	if(!onlyOnSegment || (t >= 0 && t <= 1)){
			    	result[0] = (int)StrictMath.floor(0.5 + x1 + t * dx1);
			    	result[1] = (int)StrictMath.floor(0.5 + y1 + t * dy1);
			        return 1;
		    	} else return 0;
		    } else {	//Two solutions.
		    	int found = 0;
		    	double t = (-b + Math.sqrt(det)) / (2 * a);
		    	if(!onlyOnSegment || (t >= 0 && t <= 1)){
		    		result[0] = (int)StrictMath.floor(0.5 + x1 + t * dx1);
		    		result[1] = (int)StrictMath.floor(0.5 + y1 + t * dy1);
		    		found = 1;
		    	}		        
		        t = (-b - Math.sqrt(det)) / (2 * a);
		        if(!onlyOnSegment || (t >= 0 && t <= 1)){
		        	if(found == 0){
		        		result[0] = (int)StrictMath.floor(0.5 + x1 + t * dx1);
				        result[1] = (int)StrictMath.floor(0.5 + y1 + t * dy1);
				        found = 1;
		        	} else {
		        		result[2] = (int)StrictMath.floor(0.5 + x1 + t * dx1);
				        result[3] = (int)StrictMath.floor(0.5 + y1 + t * dy1);
				        found = 2;
		        	}
		        }
		        return found;
		    }
		} else return 0;
	}

	/**
	 * Gets the x and y coordinate difference of a parallel line on the right side (seen from first point to second point).
	 * 
	 * @param x1 		the x coordinate of the first point
	 * @param y1 		the y coordinate of the first point
	 * @param x2 		the x coordinate of the second point
	 * @param y2 		the y coordinate of the second point
	 * @param distance	the distance
	 * @param result	an array to return the coordinate differences of the parallel. The x coordinate difference will
	 * 					be in <code>result[0]</code>, the y coordinate difference in <code>result[1]</code>.
	 * 
	 * @return <code>true</code> if calculation was successful, else <code>false</code>
	 */
	public static boolean getXYParallelRight(int x1, int y1, int x2, int y2, int distance, double[] result){
		if(result.length == 2){
			int dx = x2 - x1;
			int dy = y2 - y1;
			if(dx == 0){
				if(dy < 0){
					result[0] = distance;
					result[1] = 0;
				} else {
					result[0] = -distance;
					result[1] = 0;
				}
				return true;
			} else if (dy == 0){
				if(dx > 0){
					result[0] = 0;
					result[1] = distance;
				} else {
					result[0] = 0;
					result[1] = -distance;
				}
				return true;
			} else {
				//line parameter of this street (y = ax+b). b is unneeded here.
				double a = ((double)y1 - y2) / ((double)x1 - x2);

				//the line parameters for the normal
				double a2 = -1.0/a;
				double b2 = y1 - a2 * x1;

				//create a relatively long line with the normal's parameters
				double endX2 = x1 + 200000.0;
				double endY2 = a2 * endX2 + b2;

				//calculate the length of this created line
				double dx2 = endX2 - x1;
				double dy2 = endY2 - y1;
				double length2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

				//the difference we need can be calculated relative to the length of the created line
				double tmp = distance / length2;
				result[0] = dx2*tmp;
				result[1] = dy2*tmp;
				if(dy > 0){		//opposite direction
					result[0] = -result[0];
					result[1] = -result[1];
				}
				return true;
			}
		} else return false;
	}

	/**
	 * Checks for intersections on the maps and creates bridges if necessary (only for display purposes). The bridge is added to
	 * the <code>lowerspeedStreet</code>.
	 * This function takes quite a long time to process if the map is large.
	 * 
	 * @param bridgeStreet 	the street which will be above <code>otherStreet</code> if both intersect
	 * @param otherStreet 	the other street
	 */
	public static void calculateBridges(Street bridgeStreet, Street otherStreet){	
		//intersecting is only checked if two lines don't have a common point!
		if(bridgeStreet.getStartNode() != otherStreet.getStartNode() && bridgeStreet.getEndNode() != otherStreet.getEndNode() && bridgeStreet.getStartNode() != otherStreet.getEndNode() && bridgeStreet.getEndNode() != otherStreet.getStartNode()){
			int width;

			//calculate the parallel lines (like used while drawing). These are the right and left boundary lines of the street
			double[] result = new double[2];
			if(bridgeStreet.isOneway()) width = (bridgeStreet.getLanesCount() * Map.LANE_WIDTH) + 45;
			else width = (2 * bridgeStreet.getLanesCount() * Map.LANE_WIDTH) + 45;
			getXYParallelRight(bridgeStreet.getStartNode().getX(), bridgeStreet.getStartNode().getY(), bridgeStreet.getEndNode().getX(), bridgeStreet.getEndNode().getY(), width/2, result);
			int x1 = (int)Math.round(bridgeStreet.getStartNode().getX() + result[0]);
			int y1 = (int)Math.round(bridgeStreet.getStartNode().getY() + result[1]);
			int x2 = (int)Math.round(bridgeStreet.getEndNode().getX() + result[0]);
			int y2 = (int)Math.round(bridgeStreet.getEndNode().getY() + result[1]);
			int x3 = (int)Math.round(bridgeStreet.getStartNode().getX() - result[0]);
			int y3 = (int)Math.round(bridgeStreet.getStartNode().getY() - result[1]);
			int x4 = (int)Math.round(bridgeStreet.getEndNode().getX() - result[0]);
			int y4 = (int)Math.round(bridgeStreet.getEndNode().getY() - result[1]);

			//calculate the boundaries of the second street
			double[] result2 = new double[2];
			if(otherStreet.isOneway()) width = (otherStreet.getLanesCount() * Map.LANE_WIDTH) + 45;
			else width = (2 * otherStreet.getLanesCount() * Map.LANE_WIDTH) + 45;
			getXYParallelRight(otherStreet.getStartNode().getX(), otherStreet.getStartNode().getY(), otherStreet.getEndNode().getX(), otherStreet.getEndNode().getY(), width/2, result2);
			int x5 = (int)Math.round(otherStreet.getStartNode().getX() + result2[0]);
			int y5 = (int)Math.round(otherStreet.getStartNode().getY() + result2[1]);
			int x6 = (int)Math.round(otherStreet.getEndNode().getX() + result2[0]);
			int y6 = (int)Math.round(otherStreet.getEndNode().getY() + result2[1]);
			int x7 = (int)Math.round(otherStreet.getStartNode().getX() - result2[0]);
			int y7 = (int)Math.round(otherStreet.getStartNode().getY() - result2[1]);
			int x8 = (int)Math.round(otherStreet.getEndNode().getX() - result2[0]);
			int y8 = (int)Math.round(otherStreet.getEndNode().getY() - result2[1]);

			double[] firstIntersect = new double[2];
			double[] secondIntersect = new double[2];
			double[] thirdIntersect = new double[2];
			double[] fourthIntersect = new double[2];
			int intersectsfound = 0;
			boolean foundFirstIntersect = false, foundSecondIntersect = false, foundThirdIntersect = false, foundFourthIntersect = false;

			//check for intersections...
			if(findSegmentIntersection(x1, y1, x2, y2, x5, y5, x6, y6, firstIntersect)){
				foundFirstIntersect = true;
				++intersectsfound;
			}
			if(findSegmentIntersection(x1, y1, x2, y2, x7, y7, x8, y8, secondIntersect)){
				foundSecondIntersect = true;
				++intersectsfound;
			}
			if(findSegmentIntersection(x3, y3, x4, y4, x5, y5, x6, y6, thirdIntersect)){
				foundThirdIntersect = true;
				++intersectsfound;
			}
			if(findSegmentIntersection(x3, y3, x4, y4, x7, y7, x8, y8, fourthIntersect)){
				foundFourthIntersect = true;
				++intersectsfound;
			}
			if(intersectsfound > 0){	//found some kind of intersection
				if (intersectsfound == 3){	//reconstruct the fourth point...rest is done in the '4' case. 
					if(!foundFirstIntersect){
						double dx = thirdIntersect[0] + result[0] - bridgeStreet.getStartNode().getX();
						double dy = thirdIntersect[1] + result[1] - bridgeStreet.getStartNode().getY();
						double lengthSquared = dx*dx + dy*dy;
						dx = thirdIntersect[0] + result[0] - bridgeStreet.getEndNode().getX();
						dy = thirdIntersect[1] + result[1] - bridgeStreet.getEndNode().getY();
						if(lengthSquared < width*width*4){
							firstIntersect[0] = bridgeStreet.getStartNode().getX() + result[0];
							firstIntersect[1] = bridgeStreet.getStartNode().getY() + result[1];
						} else if ((dx*dx + dy*dy) < width*width*4){
							firstIntersect[0] = bridgeStreet.getEndNode().getX() + result[0];
							firstIntersect[1] = bridgeStreet.getEndNode().getY() + result[1];
						} else {
							firstIntersect[0] = thirdIntersect[0] + 2*result[0];
							firstIntersect[1] = thirdIntersect[1] + 2*result[1];
						}						
					} else if(!foundSecondIntersect){
						double dx = fourthIntersect[0] + result[0] - bridgeStreet.getStartNode().getX();
						double dy = fourthIntersect[1] + result[1] - bridgeStreet.getStartNode().getY();
						double lengthSquared = dx*dx + dy*dy;
						dx = fourthIntersect[0] + result[0] - bridgeStreet.getEndNode().getX();
						dy = fourthIntersect[1] + result[1] - bridgeStreet.getEndNode().getY();
						if(lengthSquared < width*width*4){
							secondIntersect[0] = bridgeStreet.getStartNode().getX() + result[0];
							secondIntersect[1] = bridgeStreet.getStartNode().getY() + result[1];
						} else if ((dx*dx + dy*dy) < width*width*4){
							secondIntersect[0] = bridgeStreet.getEndNode().getX() + result[0];
							secondIntersect[1] = bridgeStreet.getEndNode().getY() + result[1];
						} else {
							secondIntersect[0] = fourthIntersect[0] + 2*result[0];
							secondIntersect[1] = fourthIntersect[1] + 2*result[1];
						}
					} else if(!foundThirdIntersect){
						double dx = firstIntersect[0] - result[0] - bridgeStreet.getStartNode().getX();
						double dy = firstIntersect[1] - result[1] - bridgeStreet.getStartNode().getY();
						double lengthSquared = dx*dx + dy*dy;
						dx = firstIntersect[0] - result[0] - bridgeStreet.getEndNode().getX();
						dy = firstIntersect[1] - result[1] - bridgeStreet.getEndNode().getY();
						if(lengthSquared < width*width*4){
							thirdIntersect[0] = bridgeStreet.getStartNode().getX() - result[0];
							thirdIntersect[1] = bridgeStreet.getStartNode().getY() - result[1];
						} else if ((dx*dx + dy*dy) < width*width*4){
							thirdIntersect[0] = bridgeStreet.getEndNode().getX() - result[0];
							thirdIntersect[1] = bridgeStreet.getEndNode().getY() - result[1];
						} else {
							thirdIntersect[0] = firstIntersect[0] - 2*result[0];
							thirdIntersect[1] = firstIntersect[1] - 2*result[1];
						}
					} else {
						double dx = secondIntersect[0] - result[0] - bridgeStreet.getStartNode().getX();
						double dy = secondIntersect[1] - result[1] - bridgeStreet.getStartNode().getY();
						double lengthSquared = dx*dx + dy*dy;
						dx = secondIntersect[0] - result[0] - bridgeStreet.getEndNode().getX();
						dy = secondIntersect[1] - result[1] - bridgeStreet.getEndNode().getY();
						if(lengthSquared < width*width*4){
							fourthIntersect[0] = bridgeStreet.getStartNode().getX() - result[0];
							fourthIntersect[1] = bridgeStreet.getStartNode().getY() - result[1];
						} else if ((dx*dx + dy*dy) < width*width*4){
							fourthIntersect[0] = bridgeStreet.getEndNode().getX() - result[0];
							fourthIntersect[1] = bridgeStreet.getEndNode().getY() - result[1];
						} else {
							fourthIntersect[0] = secondIntersect[0] - 2*result[0];
							fourthIntersect[1] = secondIntersect[1] - 2*result[1];
						}
					}
					intersectsfound = 4;
				}
				if(intersectsfound == 4){	//the "normal" case
					bridgeStreet.addBridgePaintPolygon(firstIntersect[0], firstIntersect[1], secondIntersect[0], secondIntersect[1], thirdIntersect[0], thirdIntersect[1], fourthIntersect[0], fourthIntersect[1]);
				} else {	//1 or 2 intersections found
					if(intersectsfound == 1){
						if(foundFirstIntersect){	//reconstruct the second point...saves some code as the rest is done in the '2' case. 
							thirdIntersect[0] = firstIntersect[0] - 2*result[0];
							thirdIntersect[1] = firstIntersect[1] - 2*result[1];
							foundThirdIntersect = true;
						} else if(foundSecondIntersect){
							fourthIntersect[0] = secondIntersect[0] - 2*result[0];
							fourthIntersect[1] = secondIntersect[1] - 2*result[1];
							foundFourthIntersect = true;
						} else if(foundThirdIntersect){
							firstIntersect[0] = thirdIntersect[0] + 2*result[0];
							firstIntersect[1] = thirdIntersect[1] + 2*result[1];
							foundFirstIntersect = true;
						} else {
							secondIntersect[0] = fourthIntersect[0] + 2*result[0];
							secondIntersect[1] = fourthIntersect[1] + 2*result[1];
							foundSecondIntersect = true;
						}
						intersectsfound = 2;
					}
					if (intersectsfound == 2){
						if(foundFirstIntersect && foundSecondIntersect){
							bridgeStreet.addBridgePaintLine(firstIntersect[0], firstIntersect[1], secondIntersect[0], secondIntersect[1]);
						} else if (foundThirdIntersect && foundFourthIntersect){
							bridgeStreet.addBridgePaintLine(thirdIntersect[0], thirdIntersect[1], fourthIntersect[0], fourthIntersect[1]);
						} else {
							Street[] crossingStreets = otherStreet.getStartNode().getCrossingStreets();
							boolean isNearCrossing = false;	//if other street is near crossing
							for(int i = 0; i < crossingStreets.length; ++i){	//check start node
								if(crossingStreets[i].getStartNode() == bridgeStreet.getStartNode() || crossingStreets[i].getStartNode() == bridgeStreet.getEndNode() || crossingStreets[i].getEndNode() == bridgeStreet.getStartNode() || crossingStreets[i].getEndNode() == bridgeStreet.getEndNode()){
									isNearCrossing = true;
									break;
								}
							}
							if(!isNearCrossing){	//check end node
								crossingStreets = otherStreet.getEndNode().getCrossingStreets();
								for(int i = 0; i < crossingStreets.length; ++i){
									if(crossingStreets[i].getStartNode() == bridgeStreet.getStartNode() || crossingStreets[i].getStartNode() == bridgeStreet.getEndNode() || crossingStreets[i].getEndNode() == bridgeStreet.getStartNode() || crossingStreets[i].getEndNode() == bridgeStreet.getEndNode()){
										isNearCrossing = true;
										break;
									}
								}
							}
							if(!isNearCrossing){
								if ((foundFirstIntersect && foundThirdIntersect) || (foundSecondIntersect && foundFourthIntersect)){	//just two points but no end point...use start or end node								
									if (foundSecondIntersect && foundFourthIntersect){	//the same case...change variables so that we can use it the same way
										firstIntersect[0] = secondIntersect[0];
										firstIntersect[1] = secondIntersect[1];
										thirdIntersect[0] = fourthIntersect[0];
										thirdIntersect[1] = fourthIntersect[1];
									}
									double middleX = (thirdIntersect[0]-firstIntersect[0])/2 + firstIntersect[0];
									double middleY = (thirdIntersect[1]-firstIntersect[1])/2 + firstIntersect[1];
									double dx = middleX - bridgeStreet.getStartNode().getX();
									double dy = middleY - bridgeStreet.getStartNode().getY();
									double lengthSquared = dx*dx + dy*dy;
									dx = middleX - bridgeStreet.getEndNode().getX();
									dy = middleY - bridgeStreet.getEndNode().getY();
									if(lengthSquared < (dx*dx + dy*dy)){
										if(bridgeStreet.getStartNode().getCrossingStreetsCount() < 3) bridgeStreet.addBridgePaintPolygon(firstIntersect[0], firstIntersect[1], bridgeStreet.getStartNode().getX() + result[0], bridgeStreet.getStartNode().getY() + result[1], thirdIntersect[0], thirdIntersect[1], bridgeStreet.getStartNode().getX() - result[0], bridgeStreet.getStartNode().getY() - result[1]);
									} else {
										if(bridgeStreet.getEndNode().getCrossingStreetsCount() < 3) bridgeStreet.addBridgePaintPolygon(firstIntersect[0], firstIntersect[1], bridgeStreet.getEndNode().getX() + result[0], bridgeStreet.getEndNode().getY() + result[1], thirdIntersect[0], thirdIntersect[1], bridgeStreet.getEndNode().getX() - result[0], bridgeStreet.getEndNode().getY() - result[1]);
									}
								} else {	//all other cases should be extremely rare...we would produce more garbage than we correct
								}
							} 
						}
					}
				}
			}
		}
	}


	/**
	 * Recalculates start and end points of a line so that the line is shorter or longer than before.
	 * 
	 * @param startPoint	the start point. x coordinate is expected in <code>startPoint[0]</code>, y in <code>startPoint[1]</code>.
	 * 						Will be used to return the result.
	 * @param endPoint		the end point. x coordinate is expected in <code>endPoint[0]</code>, y in <code>endPoint[1]</code>.
	 * 						Will be used to return the result.
	 * @param correction	the amount of length correction. Use a positive value to makes the line shorter, a negative to make it longer. If 
	 * 						<code>correctStart</code> and <code>correctEnd</code> are both <code>true</code>, the total correction is double 
	 * 						of this value, if both are <code>false</code> no correction is done.
	 * @param correctStart	if the <code>startPoint</code> shall be corrected.
	 * @param correctEnd	if the <code>endPoint</code> shall be corrected.
	 */
	public static void calculateResizedLine(int[] startPoint, int[] endPoint, double correction, boolean correctStart, boolean correctEnd){
		if(startPoint.length == 2 && endPoint.length == 2){
			if(startPoint[0] == endPoint[0]){	// horizontal line
				if(startPoint[1] > endPoint[1]){
					startPoint[1] -= correction;
					endPoint[1] += + correction;
				} else {
					startPoint[1] += correction;
					endPoint[1] -= correction;    
				}
			} else if (startPoint[1] == endPoint[1]){	// vertical line
				if(startPoint[0] > endPoint[0]){
					startPoint[0] -= correction;
					endPoint[0] += correction;
				} else {
					startPoint[0] += correction;
					endPoint[0] -= correction;     
				}
			} else {	// diagonal line. this will be called most of the time!
				// calculate line parameters: y = ax + b
				double a = ((double)startPoint[1] - endPoint[1]) / ((double)startPoint[0] - endPoint[0]);
				double b = startPoint[1] - a * startPoint[0];
				// Pythagorean theorem (d = delta): linelength^2 = dx^2 - dy^2
				// with line parameters:            linelength^2 = dx^2 + a^2*dx^2
				// after some mathematics this leads to:      dx = sqrt(linelength^2/(1+a^2)
				if(startPoint[0] > endPoint[0]) {
					if(correctStart) startPoint[0] -= (int)Math.round(Math.sqrt(correction*correction/(1 + a*a)));
					if(correctEnd) endPoint[0] += (int)Math.round(Math.sqrt(correction*correction/(1 + a*a)));
				} else {
					if(correctStart) startPoint[0] += (int)Math.round(Math.sqrt(correction*correction/(1 + a*a)));
					if(correctEnd) endPoint[0] -= (int)Math.round(Math.sqrt(correction*correction/(1 + a*a)));
				}
				if(correctStart) startPoint[1] = (int)Math.round(a*startPoint[0] + b);
				if(correctEnd) endPoint[1] = (int)Math.round(a*endPoint[0] + b);
			}
		}
	}
}