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
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author andreastomandl
 *
 */
public class WekaHelper {
	public static ArrayList<Double> readWekaCentroids(String filePath, String axis){
		ArrayList<Double> returnArray = new ArrayList<Double>();
		
		String[] tmpArray = null;
		
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(filePath);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read file line By line
			
			while ((strLine = br.readLine()) != null) {
				if(strLine.length() > 0){
					System.out.println(strLine.substring(0,1) +  ":::"  + axis);
					if(strLine.substring(0,1).equals(axis)){
						tmpArray = strLine.split(" ");
						
						for(int i = 0; i < tmpArray.length; i++) if(!tmpArray[i].equals("") && !tmpArray[i].equals(" ") && !tmpArray[i].equals(axis)) returnArray.add(Double.parseDouble(tmpArray[i]));
						
						break;
					}
				}

				
			}
			  
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		 
		return returnArray;
	}
	
}
