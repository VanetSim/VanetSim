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
package vanetsim.gui.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author andreastomandl
 *
 */
public class AnonymizeLogHelper {
	public static void main(String args[]){
		AnonymizeLogHelper ano = new AnonymizeLogHelper();
		
		if(args.length > 0 && args[0].equals("0")) {
			if(args.length == 8) {
				ano.perturbation(args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), args[6], Integer.parseInt(args[7]));
			}
			else System.out.println("wrong parameters");
		}


		if(args.length > 0 && args[0].equals("1")) {
			if(args.length == 10) {
				ano.attackLog(args[1], Integer.parseInt(args[2]), Integer.parseInt((args[3])), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), args[7], Integer.parseInt(args[8]), Integer.parseInt(args[9]));
			}
			else System.out.println("wrong parameters");
		}
		

	}
	
	public void perturbation(String inputFile, String outputFile, int minPerturbation, int maxPerturbation, int index, String seperator, int skipLine){
        BufferedReader reader;
        
        try{
            reader = new BufferedReader(new FileReader(inputFile));
            String line = reader.readLine();
            String newLine = "";
            String data[];
            
            FileWriter fstream;
            fstream = new FileWriter(outputFile, false);
			BufferedWriter out = new BufferedWriter(fstream);
			
            //check if the log is a silent-period or a mix-zone log
            while(line != null){
            	if(skipLine > 0){
            		out.write(line + "\n");
            		skipLine--;
            	}
            	else{
            		data = line.split(seperator);
            		newLine = "";
            		
            		//perturbate
            		data[index] = String.valueOf(Integer.parseInt(data[index]) + (int)(Math.random() * ((maxPerturbation+1) - minPerturbation) + minPerturbation));
            		
            		for(int i = 0; i < data.length; i++){
            			if(i == data.length-1)  newLine += data[i];
            			else newLine += data[i] + seperator;
            		}
            		out.write(newLine + "\n");
            	}
        		
            	line = reader.readLine();
            }
            out.close();
		} catch (FileNotFoundException e) {
		    System.err.println("FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
		    System.err.println("Caught IOException: " + e.getMessage());
		}
        
       
	}
	
	public void attackLog(String inputFile, int idIndex, int timestampId, int xId, int yId, int speedId, String seperator, int skipLine, int beaconIntervalTime){
		BufferedReader reader;
        
		ArrayList<String> vehicleAttacked = new ArrayList<String>();
		ArrayList<String> vehicleIds = new ArrayList<String>();

		
        try{
            reader = new BufferedReader(new FileReader(inputFile));
            String line = reader.readLine();
            String data[];
            String data2[];
            String data3[];
            double v1 = 0;
            double v2 = 0;
            double normalize;
            
            int x1;
            int x2;
            int y1;
            int y2;
            
            double dx;
            double dy;
            
            double tmpFactor;
            int timeBetweenBeacons = 0;
            
            double expectedWay = 0;
            		
            double savedFactor;
            
            int savedSkipLine = skipLine;
            String savedID = "";
            
            int firstTimestamp = -1;
            
            int successCount = 0;
            int failCount = 0;
            
            double calibration = 1.1;
            
            @SuppressWarnings("unused")
			double factorToRealVehicle = 0;
            
            //check if the log is a silent-period or a mix-zone log
            while(line != null){
            	if(skipLine > 0){
            		skipLine--;
            	}
            	else{
            		data = line.split(seperator);
            		if(firstTimestamp == -1) firstTimestamp = Integer.parseInt(data[timestampId]);
                	if(vehicleIds.contains(data[idIndex])) break;
                	else{
                		vehicleIds.add(data[idIndex]);
                		vehicleAttacked.add(line);
                	}
            		
                	
            	}
            	line = reader.readLine();
            	
            }
            
            skipLine = savedSkipLine;
            
            
            for(String vehicleLine:vehicleAttacked){
            	
            	reader = new BufferedReader(new FileReader(inputFile));
                line = reader.readLine();
                data = vehicleLine.split(seperator);
                
                //find line two
                while(line != null){
                	if(skipLine > 0) skipLine--;
                	else{
                		data2 = line.split(seperator);
                    	
                    	//second beacon found
                    	if(data[idIndex].equals(data2[idIndex]) && Integer.parseInt(data2[timestampId]) > Integer.parseInt(data[timestampId])){
                    		//time between beacons
                    		
                    		timeBetweenBeacons = Integer.parseInt(data2[timestampId]) - Integer.parseInt(data[timestampId]);
                    				
                    		//calculate expected way
                            expectedWay = (double)(timeBetweenBeacons/1000 * Double.parseDouble(data[speedId]))*calibration;

                            
                            //coords
                            x1 = Integer.parseInt(data[xId]);
                            y1 = Integer.parseInt(data[yId]);
                            x2 = Integer.parseInt(data2[xId]);
                            y2 = Integer.parseInt(data2[yId]);
                            
                           //calculate vector
    						v1 = x2 - x1;
    						v2 = y2 - y1;
    						if(v1 == 0 && v2 == 0) normalize = 1;
    						else normalize = (double) (1/Math.sqrt((double)((v1*v1)+(v2*v2))));

    						v1 *= normalize * expectedWay;
    						v2 *= normalize * expectedWay;
    						v1 += x2;
    						v2 += y2;

    						break;
                    	}
                	}
                	
                	line = reader.readLine();
                }

                skipLine = savedSkipLine;
                
                savedFactor = 999999999;
            	
            	
            	reader = new BufferedReader(new FileReader(inputFile));
                line = reader.readLine();
                
                //find line two
                while(line != null){
                	data3 = line.split(seperator);
                	
                	if(skipLine > 0) skipLine--;
                	else{
                		//start searching as soon as the 2 first Beacons are passed
                    	if(Integer.parseInt(data3[timestampId]) > (beaconIntervalTime + firstTimestamp)) {
        					dx = v1 - Integer.parseInt(data3[xId]);
        					dy = v2 - Integer.parseInt(data3[yId]);
        						
        					tmpFactor = (dx*dx + dy*dy);
        					
        					 if(data[idIndex].equals(data3[idIndex])) factorToRealVehicle = tmpFactor;
        					
        					//save smallest factor
        					if(savedFactor > tmpFactor) {
        						savedFactor = tmpFactor;
        						savedID = data3[idIndex];        						
        					}
                    	}
                	}

                	line = reader.readLine();
                }
                
                if(data[idIndex].equals(savedID)){
                	successCount++;
                }
                else{
                	failCount++;
                }
            }
        	System.out.println(successCount + ":" + failCount + ":x:BTI:" + beaconIntervalTime + " Name:" + inputFile);
        	
        	FileWriter fstream;
            fstream = new FileWriter("attackResult.log", true);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(successCount + ":" + failCount + " :BTI:" + beaconIntervalTime + " Name:" + inputFile + "\n");
 			out.flush();
 			out.close();

		} catch (FileNotFoundException e) {
		    System.err.println("FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
		    System.err.println("Caught IOException: " + e.getMessage());
		}
		
    
	}
}
