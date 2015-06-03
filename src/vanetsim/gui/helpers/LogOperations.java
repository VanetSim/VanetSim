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
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import vanetsim.localization.Messages;

public class LogOperations extends Thread {
	
	/** interface between dialog and operations */
	LogAnalyser logAnalyser_;
	
	/** type of operation */
	private String operation_ = "";

	/** variable to save loaded log */
	private static String[] savedLog = new String[2];
	
	/** variable to save loaded advanced-log */
	private static String[] savedAdvancedLog = new String[2];

	/** variable to save silent period header */
	private static String silentPeriodHeader;
	
	/** variable to save to advanced location information */
	private String locationInformation_ = "";
	
	
	public LogOperations(LogAnalyser logAnalyser){
		logAnalyser_ = logAnalyser;
	}
	
	public void run(){
		//start analyzing (the attack type has to be set before)
		if(operation_.equals("MixStandard")) standardAttackMixZones();
		else if(operation_.equals("MixAdvanced")) advancedAttackMixZones();
		else if(operation_.equals("SilentStandard")) standardAttackSilentPeriod();
		else if(operation_.equals("SilentAdvanced")) advancedAttackSilentPeriod();
		else if(operation_.equals("SlowStandard")) standardAttackSlow();
		//else if(operation_.equals("SlowAdvanced")) advancedAttackSlow();
		
		logAnalyser_.guiControl("startBtn", true);
		logAnalyser_.guiControl("stopBtn", false);
		logAnalyser_.guiControl("progressBar", false);
		logAnalyser_.guiControl("copyBtn", true);
		
		//start next job
		logAnalyser_.startNextJob(false);
	}


	
	/**
	 * Standard attack for mix-zones
	 */
	public void standardAttackMixZones(){
		logAnalyser_.updateProgressBar(0);
		logAnalyser_.updateInformationArea(0, 0, false);
		
		//hash map to follow vehicles through more mix zones
		HashMap<String,String> successLog = new HashMap<String,String>();
		
		logAnalyser_.updateProgressBar(5);
		//save and load data (analysis will be faster the second time, e.g. after changing variables)
		if(logAnalyser_.isFilePathChanged()){
			savedLog = readFileAndHeader(logAnalyser_.getActualJob());
			logAnalyser_.setFilePathChanged(false);
		}	
		
		logAnalyser_.updateProgressBar(10);
		
		//check if we have a correct log -> if yes quit analysis
		if(savedLog[0] == null || savedLog[1] == null || !savedLog[0].substring(0, 8).equals("Mix-Zone")){
			JOptionPane.showMessageDialog(null, Messages.getString("LogOperations.WrongLogType"), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		writeLocationInformationToFile(savedLog[0] + "******************\n", false);
		
		String[] mixZoneHeaderArray = savedLog[0].split("\n");
		int numberOfMixZones  = mixZoneHeaderArray.length;
		
		//variables for statistics
		int[] numberOfVehicles = new int[numberOfMixZones];
		int[] numberOfCorrectVehicles = new int[numberOfMixZones];
		int numberOfVehiclesTotal = 0;
		int numberOfCorrectVehiclesTotal = 0;	
		
		System.out.println("file loaded...");
		
		//temp line variable
		String line = "";
		
		//variable to save split line of log (":")
		String[] logData;
	
		
		//index of log data (useful if log structure changes)
		int timestamp = 0;
		int steadyID = 2;
		int nodeID = 10;
		int direction = 12;
		int streetName = 14;
		int streetSpeed = 16;
		int port = 20;
		
		//values for variables set in GUI	
		double timeBufferValue = logAnalyser_.getGuiElement("timeBufferValue");
		double biggerStreetValue = 1-logAnalyser_.getGuiElement("biggerStreetValue");
		double tuneTimeValue =  logAnalyser_.getGuiElement("tuneTimeValue");
		double smallerStreetValue = 1-logAnalyser_.getGuiElement("smallerStreetValue");
		double drivesStraigthValue = 1-logAnalyser_.getGuiElement("drivesStraigthValue");
		double turnsValue = 1-logAnalyser_.getGuiElement("turnsValue");
		double makesUTurnValue = 1-logAnalyser_.getGuiElement("makesUTurnValue");
		
		//initializing variables
		int nodeRadius = 0;
		double expectedTime = 0;
		String savedSteadyID = "";
		double savedFactor = 0;
		
		double streetFactor = 0;
		double drivingFactor = 0;
		
		double factor = 0;
		
		int numberTotal = 0;
		int numberSuccessInRow = 0;

		
		//variables for the progressbar
		long amountOfLines = savedLog[1].split("\n").length;
		long updateEveryNLine = amountOfLines/80;
		long counter = 0;
		if(updateEveryNLine == 0) updateEveryNLine = 1;
		
		BufferedReader reader = new BufferedReader(new StringReader(savedLog[1]));        
		try {
			//read vehicle data line by line and check every vehicle
			while ((line = reader.readLine()) != null) {  
				counter++;
				//update progressbar
				if(counter%updateEveryNLine == 0) logAnalyser_.addToProgressBar(1);
				
				if (line.length() > 0){
					//get log data of the vehicle we want to check now
					logData = line.split(":");
					
					if(logData != null && logData.length > 18){
						//only attack vehicles which drive into a mix zone
						if(logData[direction].equals("IN")){
							//counter for all vehicles
							numberOfVehiclesTotal++;
							
							//update vehicle counter of the current mix zone
							for(int i = 0; i < numberOfMixZones; i++){
								if(logData[nodeID].equals(mixZoneHeaderArray[i].split(":")[2])) numberOfVehicles[i]++;
							}
							
							
							//returns to node radius of the mix zone
							nodeRadius = getNodeRadius(logData[nodeID], savedLog[0]);
							
							//expected time to leave mix zone in ms (use tuneTimeValue to influence this time)
							expectedTime = (((2*nodeRadius)/Integer.parseInt(logData[streetSpeed]))*1000)*tuneTimeValue;
							
							//now check every vehicle that left the mix zone in the time (expectedTime + buffer)
							//set mark for reader (performance tweak, we can resume our buffer on this position later)
							reader.mark(savedLog[1].length());
							
							//tmp variable to save steadyID and the factor
							savedSteadyID = "";
							savedFactor = 999999999;
							
							try {
								//read vehicle data line by line
								while ((line = reader.readLine()) != null) {   
									String[] logData2 = line.split(":");
									
									//we only check vehicles until a selected time (performance tweak, calculated expected time + buffer)
									if((Integer.parseInt(logData[timestamp]) + expectedTime + timeBufferValue) < Integer.parseInt(logData2[timestamp])){
										break;
									}
									
									//only check vehicles that leave the same mix-zone
									if(logData[nodeID].equals(logData2[nodeID]) && logData2[direction].equals("OUT")){										
										streetFactor = 0;
										drivingFactor = 0;
										
										//calculate the right factors
										if(logData[streetName].equals(logData2[streetName])){
											if(logData[port].equals(logData2[port])){
												drivingFactor = makesUTurnValue;
											}
											else drivingFactor = drivesStraigthValue;
										}
										else{
											drivingFactor = turnsValue;
										}
										
										
										if(Integer.parseInt(logData[streetSpeed]) > Integer.parseInt(logData2[streetSpeed])){
											streetFactor = smallerStreetValue;
										}
										else{
											streetFactor = biggerStreetValue;
										}
										
										//calculate factor:
										if(drivingFactor == 0) drivingFactor = 0.0000000000000000000000000001;
										if(streetFactor == 0) streetFactor = 0.0000000000000000000000000001;
										factor = drivingFactor * streetFactor * Math.abs(((Integer.parseInt(logData[timestamp]) + expectedTime) - Integer.parseInt(logData2[timestamp])));

										//always save the smallest vehicle with the smallest factor
										if(factor < savedFactor){
											savedFactor = factor;
											savedSteadyID = logData2[steadyID];
										}
									}
								}
							} catch(IOException e) {
								System.out.println("Error while doing standard attack");
								e.printStackTrace();
							}
							
							
							//jump to mark
							reader.reset();
							
							//update states to log the movement of vehicles through more mix-zones. Format: Number:Number:Boolean -> NumberOfPassedMixZones:NumerOfSuccessfulPassedZones(only in a row):BooleanValueToShowIfVehicleWasFollowedCorrectlyUntilNow
							String[] successData = {"0","0","true"};
							if(successLog.get(logData[steadyID]) != null) successData = successLog.get(logData[steadyID]).split(":");			
							
							numberTotal = Integer.parseInt(successData[0]);
							numberSuccessInRow = Integer.parseInt(successData[1]);
							if(savedSteadyID.equals(logData[steadyID])){
								locationInformation_ += "true:" + logData[nodeID] + ":" + logData[port] + "\n";
								successLog.remove(logData[steadyID]);
								
								if(successData[2].equals("true")) {
									successLog.put(logData[steadyID], (numberTotal+1) + ":" + (numberSuccessInRow+1) + ":true");
								}
								else{
									successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":false");
								}
								
								for(int i = 0; i < numberOfMixZones; i++){
									if(logData[nodeID].equals(mixZoneHeaderArray[i].split(":")[2]))numberOfCorrectVehicles[i]++;
								}
								numberOfCorrectVehiclesTotal++;

							}
							else{
								locationInformation_ += "false:" + logData[nodeID] + ":" + logData[port] + "\n";
								successLog.remove(logData[steadyID]);
								successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":false");
							}

							//update status field in GUI
							if(numberOfVehiclesTotal%10 == 0)logAnalyser_.updateInformationArea(numberOfVehiclesTotal, numberOfCorrectVehiclesTotal, false);
						}
					}
				}
			}
		} catch(IOException e) {
			System.out.println("Error while doing standard attack");
			e.printStackTrace();
		}
		logAnalyser_.updateInformationArea(numberOfVehiclesTotal, numberOfCorrectVehiclesTotal, true);

		int maxOfRow = 0;
		//calculate max crossed mix zones
		for(Map.Entry<String, String> e : successLog.entrySet()){
			if(maxOfRow < Integer.parseInt(e.getValue().split(":")[0])) maxOfRow = Integer.parseInt(e.getValue().split(":")[0]);
		}
		
		maxOfRow++;
		int[][] dataCollection = new int[maxOfRow][maxOfRow];

		//calculate advanced states
		for(Map.Entry<String, String> e : successLog.entrySet()){
			dataCollection[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[1])] = dataCollection[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[1])] + 1;
		}
		
		logAnalyser_.updateInformationArea("\n\n", false);
		logAnalyser_.updateInformationArea("#GNU-Plot Data Simple\n", true);
		logAnalyser_.updateInformationArea("#Privacy VehiclesTotal Probabilities k-Anonymity\n", true);
		//calculate anonymity set for the silent period and log the results
		float[] kAnonymityValues = new float[numberOfMixZones];

		float numberOfVeh = 0;
		float numberOfCorrVeh = 0;
		float numberOfKAnoVehTotal = 0;
		float numberOfKAnoVeh = 0;
		
		float[] tmpKAno = null;
		for(int i = 0; i < mixZoneHeaderArray.length; i++){
			tmpKAno = getKAnonymityInMix(mixZoneHeaderArray[i].split(":")[2],savedLog[1]);
			kAnonymityValues[i] = tmpKAno[0];
			
			numberOfKAnoVehTotal += tmpKAno[1];
			numberOfKAnoVeh += tmpKAno[2];
			
			numberOfVeh += numberOfVehicles[i];
			numberOfCorrVeh += numberOfCorrectVehicles[i];
			
			logAnalyser_.updateInformationArea("Mix-Zone" + (i+1) + " " + numberOfVehicles[i] + " " + (float)100*numberOfCorrectVehicles[i]/numberOfVehicles[i] + " " + kAnonymityValues[i] + "\n", true);
		}
		logAnalyser_.updateInformationArea("Total " + numberOfVeh + " " + (numberOfCorrVeh*100/numberOfVeh) + " " + (numberOfKAnoVehTotal/numberOfKAnoVeh) + "\n", true);
		logAnalyser_.writeResultsToFile("simple");
		logAnalyser_.updateInformationArea("\n\n", false);
		logAnalyser_.updateInformationArea("#GNU-Plot Data Detail\n", true);
		logAnalyser_.updateInformationArea("#Success/Crossed", true);
		for(int i = 1; i < maxOfRow; i++) logAnalyser_.updateInformationArea(" " + i, true);
		logAnalyser_.updateInformationArea(" VehiclesLeft", true);
		for(int l = 0; l < maxOfRow;l++){
			logAnalyser_.updateInformationArea("\n" + l, true);	
			for(int m = 1; m < maxOfRow;m++){
				logAnalyser_.updateInformationArea(" " + dataCollection[m][l], true);
			}
			
		}
		
		
		
		
		logAnalyser_.updateProgressBar(100);
		logAnalyser_.guiControl("progressBar", false);
		logAnalyser_.updateProgressBar(0);
		logAnalyser_.writeResultsToFile("detail");
		writeLocationInformationToFile(null, true);

	}
	
	/**
	 * Advanced attack for mix-zones
	 */
	public void advancedAttackMixZones(){
		logAnalyser_.updateProgressBar(0);
		logAnalyser_.updateInformationArea(0, 0, false);
				
		//hash map to follow vehicles through more mix zones
		HashMap<String,String> successLog = new HashMap<String,String>();

		//analyse traffic first
		logAnalyser_.updateProgressBar(5);
		
		//save and load data (analysis will be faster the second time, e.g. after changing variables)
		if(logAnalyser_.isAdvancedFilePathChanged()){
			savedAdvancedLog = readFileAndHeader(logAnalyser_.getAdvancedFilePath_());
			logAnalyser_.setAdvancedFilePathChanged(false);
		}	

		//check if there is a correct header and log is cleaned
		if(savedAdvancedLog.length < 2){
			JOptionPane.showMessageDialog(null, Messages.getString("LogOperations.LogsNeedsCleaning"), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//check if we have a correct log -> if yes quit analysis
		if(savedAdvancedLog[0] == null || savedAdvancedLog[1] == null || !savedAdvancedLog[0].substring(0, 8).equals("Mix-Zone")){
			JOptionPane.showMessageDialog(null, Messages.getString("LogOperations.WrongLogType"), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		logAnalyser_.updateProgressBar(10);
		
		//Hash Map for traffic data about the mix zones
		HashMap<String,Object[]> trafficDataMap = new HashMap<String,Object[]>();
		
		//analyse all mix zones if second log
		String tmpHeaders[] = savedAdvancedLog[0].split("\n");
		for(int i = 0; i < tmpHeaders.length; i++){
				String[] tmpHeader = tmpHeaders[i].split(":");
				trafficDataMap.put(tmpHeader[2], getDataOfMixZoneLog(savedAdvancedLog, tmpHeader[2], tmpHeaders.length));
		}
		
		Object[] tmpObject = null;

		
		if(logAnalyser_.isFilePathChanged()){
			savedLog = readFileAndHeader(logAnalyser_.getActualJob());
			logAnalyser_.setFilePathChanged(false);
		}	
		
		//check if we have a correct log -> if yes quit analysis
		if(savedLog[0] == null || savedLog[1] == null || !savedLog[0].substring(0, 8).equals("Mix-Zone")){
			JOptionPane.showMessageDialog(null, Messages.getString("LogOperations.WrongLogType"), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		
		writeLocationInformationToFile(savedLog[0] + "******************\n", false);
		
		System.out.println("file loaded...");
		
		//tmp variable for read lines
		String line = "";
		//variable for split log line
		String[] logData;
		String[] logData2;
		
		//variables for statistics
		String[] mixZoneHeaderArray = savedLog[0].split("\n");
		int numberOfMixZones  = mixZoneHeaderArray.length;
		
		int[] numberOfVehicles = new int[numberOfMixZones];
		int[] numberOfCorrectVehicles = new int[numberOfMixZones];
		int numberOfVehiclesTotal = 0;
		int numberOfCorrectVehiclesTotal = 0;	
		
		//index of log data (useful if log structure changes)
		int timestamp = 0;
		int steadyID = 2;
		int nodeID = 10;
		int direction = 12;
		int port = 20;

		//values for variables set in gui	
		double timeBufferValue = logAnalyser_.getGuiElement("timeBufferValue");

		//variables
		String savedSteadyID ="";
		double savedFaktor = 0;
		int maxExpectedTime = 0;
		double factor = 1;
		
		//variables for the progressbar
		long amountOfLines = savedLog[1].split("\n").length;
		long updateEveryNLine = amountOfLines/50;
		long counter = 0;
		
		BufferedReader reader = new BufferedReader(new StringReader(savedLog[1]));        
		try {
			//read vehicle data line by line
			while ((line = reader.readLine()) != null) {
				counter++;
				//update progress bar
				if(counter%updateEveryNLine ==0) logAnalyser_.addToProgressBar(1);
				if (line.length() > 0){
					logData = line.split(":");
					
					if(logData != null && logData.length > 18){
						//only attack vehicles which drive into a mix zone
						if(logData[direction].equals("IN")){
							//counter for all vehicles
							numberOfVehiclesTotal++;
							for(int i = 0; i < numberOfMixZones; i++){
								if(logData[nodeID].equals(mixZoneHeaderArray[i].split(":")[2])) numberOfVehicles[i]++;
							}
							
							
							tmpObject = trafficDataMap.get(logData[nodeID]);
							//now check every vehicle that left the mix zone in the timespan (expectedTime + buffer)
							//set mark for reader (performance tweak, we can resume our buffer on this position later)
							reader.mark(savedLog[1].length());
						
							
							savedSteadyID = "";
							savedFaktor = 999999999;
							
							try {
								//read vehicle data line by line
								while ((line = reader.readLine()) != null) {   
									logData2 = line.split(":");
									
									maxExpectedTime = 0;
									//get max expected time (searches through all possible combinations of entrances and exits
									for(int i = 0; i < ((int[][])tmpObject[0]).length; i++){
										for(int j = 0; j< ((int[][])tmpObject[0]).length; j++){
											if(maxExpectedTime < ((int[][])tmpObject[0])[i][j]) maxExpectedTime = ((int[][])tmpObject[0])[i][j];
										}
									}
									
									//we only check vehicles until a selected time (performance tweak, calculated expected time + buffer)
									if((Integer.parseInt(logData[timestamp]) + maxExpectedTime + timeBufferValue) < Integer.parseInt(logData2[timestamp])){
										break;
									}
									
									//only check vehicles that leave the same mix-zone
									if(logData2[direction].equals("OUT") && logData2[nodeID].equals(logData[nodeID])){										
										//calculate the right factors
										factor = 1;
										
										//if that in GUI use also the calculated probabilities for this mix zone
										if(logAnalyser_.isProbabilitiesOn()){
											factor = ((double)1- ((double[][])tmpObject[1])[Integer.parseInt(logData[port])-1][Integer.parseInt(logData2[port])-1]) * Math.abs(((Integer.parseInt(logData[timestamp]) + ((int[][])tmpObject[0])[Integer.parseInt(logData[port])-1][Integer.parseInt(logData2[port])-1]) - Integer.parseInt(logData2[timestamp]))) ;
										}
										
										//use the expected path of this mix zone to calculate the factor (the smaller the better)
										factor += Math.abs(((Integer.parseInt(logData[timestamp]) + ((int[][])tmpObject[0])[Integer.parseInt(logData[port])-1][Integer.parseInt(logData2[port])-1]) - Integer.parseInt(logData2[timestamp])));

										//always save the smallest factor
										if(factor < savedFaktor){
											savedFaktor = factor;
											savedSteadyID = logData2[steadyID];
										}
									}
								}
							} catch(IOException e) {
								System.out.println("Error while doing advanced attack");
								e.printStackTrace();
							}
							
							
							//jump to mark
							reader.reset();
							
							//update states to log the movement of vehicles through more mix-zones. Format: Number:Number:Boolean -> NumberOfPassedMixZones:NumerOfSuccessfulPassedZones(only in a row):BooleanValueToShowIfVehicleWasFollowedCorrectlyUntilNow
							String[] successData = {"0","0","true"};
							if(successLog.get(logData[steadyID]) != null) successData = successLog.get(logData[steadyID]).split(":");			
							
							int numberTotal = Integer.parseInt(successData[0]);
							int numberSuccessInRow = Integer.parseInt(successData[1]);
							if(savedSteadyID.equals(logData[steadyID])){
								locationInformation_ += "true:" + logData[nodeID] + ":" + logData[port] + "\n";

								successLog.remove(logData[steadyID]);
								
								if(successData[2].equals("true")) {
									successLog.put(logData[steadyID], (numberTotal+1) + ":" + (numberSuccessInRow+1) + ":true");
								}
								else{
									successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":false");
								}
								
								for(int i = 0; i < numberOfMixZones; i++){
									if(logData[nodeID].equals(mixZoneHeaderArray[i].split(":")[2]))numberOfCorrectVehicles[i]++;
								}
								
								numberOfCorrectVehiclesTotal++;
							}
							else{
								locationInformation_ += "false:" + logData[nodeID] + ":" + logData[port] + "\n";

								successLog.remove(logData[steadyID]);
								successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":false");
							}

							//update status field in GUI
							if(numberOfVehiclesTotal%10 == 0)logAnalyser_.updateInformationArea(numberOfVehiclesTotal, numberOfCorrectVehiclesTotal, false);
						}
					}
				}
			}
		} catch(IOException e) {
			System.out.println("Error while doing advanced attack");
			e.printStackTrace();
		}
		if(numberOfVehiclesTotal%10 == 0)logAnalyser_.updateInformationArea(numberOfVehiclesTotal, numberOfCorrectVehiclesTotal, false);

		int maxOfRow = 0;
		//calculate max crossed mix zones
		for(Map.Entry<String, String> e : successLog.entrySet()){
			if(maxOfRow < Integer.parseInt(e.getValue().split(":")[0])) maxOfRow = Integer.parseInt(e.getValue().split(":")[0]);
		}
		
		maxOfRow++;
		int[][] dataCollection = new int[maxOfRow][maxOfRow];
		
		//calculate advanced stats
		//float vehiclesTotal = 0;
		for(Map.Entry<String, String> e : successLog.entrySet()){
			//vehiclesTotal++;
			dataCollection[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[1])] = dataCollection[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[1])] + 1;
		}

		
		//calculate entropy and kAnonymity
		float entroTotal = 0;
		float numberOfVeh = 0;
		float numberOfCorrVeh = 0;
		float numberOfKAnoVehTotal = 0;
		float numberOfKAnoVeh = 0;
		float[] tmpKAno = null;
		float[] kAnonymityValues = new float[numberOfMixZones];
		logAnalyser_.updateInformationArea("\n\n", false);
		logAnalyser_.updateInformationArea("#GNU-Plot Data Simple\n", true);
		logAnalyser_.updateInformationArea("Privacy TotalVehicles Probabilities k-Anonymity Entropy\n", true);
		for(int i = 0; i < tmpHeaders.length; i++){
			//k-Anonymity
			String[] tmpHeader = tmpHeaders[i].split(":");
			
			tmpKAno = getKAnonymityInMix(mixZoneHeaderArray[i].split(":")[2],savedLog[1]);
			kAnonymityValues[i] = tmpKAno[0];
			
			numberOfKAnoVehTotal += tmpKAno[1];
			numberOfKAnoVeh += tmpKAno[2];
			
			numberOfVeh += numberOfVehicles[i];
			numberOfCorrVeh += numberOfCorrectVehicles[i];
		
			//Entropy
			tmpObject = trafficDataMap.get(tmpHeader[2]);
			double[][] probabilties = (double[][]) tmpObject[1];
			float probabiltiesTotal = 0;
			
			float totalProbabilities = 0;
			for(int m = 0; m < probabilties.length;m++){
				for(int n = 0; n < probabilties.length;n++){
					totalProbabilities += probabilties[m][n];
				}
			}
			

			for(int m = 0; m < probabilties.length;m++){
				for(int n = 0; n < probabilties.length;n++){
					if(probabilties[m][n] > 0) probabiltiesTotal += (probabilties[m][n]/totalProbabilities*((float)Math.log10(probabilties[m][n]/totalProbabilities)/Math.log10(2)));
				}
			}
			
			entroTotal += (numberOfVehicles[i]*-probabiltiesTotal);
			
			logAnalyser_.updateInformationArea("Mix-Zone" +  (i+1) + " " + numberOfVehicles[i] + " " +  (float)100*numberOfCorrectVehicles[i]/numberOfVehicles[i] + " " + kAnonymityValues[i]  + " " + -probabiltiesTotal +  "\n", true);
		}
		logAnalyser_.updateInformationArea("Total " + numberOfVeh + " " + (numberOfCorrVeh*100/numberOfVeh) + " " + (numberOfKAnoVehTotal/numberOfKAnoVeh) + " " + (entroTotal/numberOfVeh) + "\n", true);

		logAnalyser_.writeResultsToFile("simple");
		logAnalyser_.updateInformationArea("\n\n", false);
		logAnalyser_.updateInformationArea("#GNU-Plot Data Detail\n", true);
		logAnalyser_.updateInformationArea("#Success/Crossed", true);
		for(int i = 1; i < maxOfRow; i++) logAnalyser_.updateInformationArea(" " + i, true);
		logAnalyser_.updateInformationArea(" VehiclesLeft", true);
		//float tmpSumLine = 0;
		//float vehiclesTotal2 = 0;
		for(int l = 0; l < maxOfRow;l++){
			logAnalyser_.updateInformationArea("\n" + l, true);	
			for(int m = 1; m < maxOfRow;m++){
				//tmpSumLine += dataCollection[m][l];
				logAnalyser_.updateInformationArea(" " + dataCollection[m][l], true);
			}
			//this is the old version of the detailed calculator. Now there is a script -> accumulateDetailedLogFiles() in ReportingControlPanel
			/*
			if(l == 0){
				logAnalyser_.updateInformationArea(" " + (tmpSumLine/vehiclesTotal), true);
			}
			else if(l == 1){
				logAnalyser_.updateInformationArea(" " + (tmpSumLine/vehiclesTotal), true);
			}
			else{
				vehiclesTotal2 = vehiclesTotal;
				for(int n = 0; n < l;n++){
					for(int o = 1; o < l;o++){
						vehiclesTotal2 -= dataCollection[o][n];
					}
				}
				logAnalyser_.updateInformationArea(" " + (tmpSumLine/vehiclesTotal2), true);
			}
			
			tmpSumLine = 0;
			*/
		}

		
		logAnalyser_.updateProgressBar(100);
		logAnalyser_.guiControl("progressBar", false);
		logAnalyser_.updateProgressBar(0);
		
		//save results to file
		logAnalyser_.writeResultsToFile("detail");
		//save location information to file
		writeLocationInformationToFile(null, true);
	}

	
	public void standardAttackSilentPeriod(){
		logAnalyser_.updateProgressBar(1);

		logAnalyser_.updateInformationArea(0, 0, false);
		
		//hash map to follow vehicles through mix zones
		HashMap<String,String> successLog = new HashMap<String,String>();
		
		//save and load data (analysis will be faster the second time, e.g. after changing variables)
		if(logAnalyser_.isFilePathChanged()){
			savedLog[0] = readFile(logAnalyser_.getActualJob());
			logAnalyser_.setFilePathChanged(false);
		}	
	
		logAnalyser_.updateProgressBar(1);

		System.out.println("file loaded...");
		
		//temp variables to iterate through data
		String line = "";
		String[] logData;
		String[] logData2;

		//variables for statistics
		int numberOfVehicles = 0;
		int numberOfCorrectVehicles = 0;
		
		//index of log data (useful if log structure changes)
		int timestamp = 0;
		int steadyID = 2;
		int streetSpeed = 16;
		int x = 20;
		int y = 22;

		//values for variables set in GUI	
		double tuneTimeValue =  logAnalyser_.getGuiElement("tuneTimeValue");

		String tmpString1 = "";
		String tmpString2 = "";

		int actualTime = 0;
		int oldTime = 0;
		
		
		//read time of silent period
		int silentPeriodTime = 0;
		
		//this arrays a just to save the beacons-blocks before and after the silent period
		String dataOne[] = null;
		String dataTwo[] = null;
		
		//String to save the guessed steadyID
		String steadyIDSave = "";
		
		//other variables for calculation
		float x1 = 0;
		float y1 = 0;
		float expectedWay = 0;
		float factor = 0;
		float dx = 0;
		float dy = 0;	
		float tmpFactor = 0;
		String[] successData = null;
		String[] successDataReset = {"0","0","true"};
		int numberTotal = 0;
		int numberSuccessInRow = 0;
		
		boolean firstLine = true;
		long amountOfLines = savedLog[0].split("\n").length;
		long updateEveryNLine = amountOfLines/98;
		long counter2 = 0;
		
		writeLocationInformationToFile("Silent Period\n******************\n", false);

		//variables for kAnonymity und Entropy
		//constant variable (max. driving speed of a vehicle)
		double maxDrivingSpeed =  6944.44444444444444444444444444444444444;
		double maxDistance = 0;
		double maxDistanceSquared = 0;
		long vehicleX = 0;
		long vehicleY = 0;
		long dx2 = 0;
		long dy2 = 0;
		double expectedWay2 = 0;
		double tmpDistance2 = 0;
		double tmpDistances = 0;
		double entropiesAdded = 0;
		int numberOfCheckedVehicles = 0;
		long numberOfFoundVehicles = 0;
		float prob = 0;
		
		double distanceInTotal = 0;
		ArrayList<Double> distances = new ArrayList<Double>();
		
		
		//if the file path has changed we have to read the log again
		System.out.println("starting analysing...");
		//read file into array separated by silent periods
		BufferedReader reader = new BufferedReader(new StringReader(savedLog[0]));        
		try {
			//read vehicle data line by line
			while ((line = reader.readLine()) != null) { 
				counter2++;
				//update progress bar
				if(counter2%updateEveryNLine ==0) logAnalyser_.addToProgressBar(1);
				
				//check if it's the correct log type
				if(firstLine){
					firstLine = false;
					silentPeriodHeader = line;
					
					silentPeriodTime = Integer.parseInt(silentPeriodHeader.split(":")[2]);
					
					maxDistance = maxDrivingSpeed*(double)(silentPeriodTime/1000);
					maxDistanceSquared = maxDistance*maxDistance;
					
					if(!silentPeriodHeader.substring(0, 8).equals("Silent P")){
						JOptionPane.showMessageDialog(null, Messages.getString("LogOperations.WrongLogType"), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				//split log after every silent period and put data into array list. Even it takes long to split up the data, we get a performance boost later for the attack
				else{
					actualTime = Integer.parseInt(line.split(":")[timestamp]);
					
					if(oldTime != 0 && Math.abs(actualTime - oldTime) > 1000){
						if(tmpString2.equals("")){
							tmpString2 = tmpString1;
							tmpString1 = "";
						}
						else{
							//read the 2 beacon blocks
							dataOne = tmpString2.split("\n");
							dataTwo = tmpString1.split("\n");

							steadyIDSave = "";
							//iterate through the first block
							for(int j = 0; j < dataOne.length-1; j+=2){
								numberOfVehicles++;
								
								//first beacon
								logData = dataOne[j+1].split(":");
												
								//calculate estimated way
								x1 = Integer.parseInt(logData[x]);
								y1 = Integer.parseInt(logData[y]);
								//calculates how far the vehicle could drive at full speed
								expectedWay = (float)((float)(silentPeriodTime/1000 * Integer.parseInt(logData[streetSpeed]))*tuneTimeValue);
					
								factor = 999999999;
								
								//now iterate through the second block 
								for(int k = 0; k < dataTwo.length-1; k+=2){
									//read first beacon
									logData2= dataTwo[k].split(":");
									
									dx = x1 - Integer.parseInt(logData2[x]);
									dy = y1 - Integer.parseInt(logData2[y]);
									
									//calculate distance between estimated and real way (the smaller the better) 
									tmpFactor = Math.abs((expectedWay*expectedWay) -(dx*dx + dy*dy));
									
									//save smallest factor
									if(factor > tmpFactor) {
										factor = tmpFactor;
										steadyIDSave = logData2[steadyID];
									}
										
								}
								
								//update states to log the movement of vehicles through more mix-zones. Format: Number:Number:Boolean -> NumberOfPassedMixZones:NumerOfSuccessfulPassedZones(only in a row):BooleanValueToShowIfVehicleWasFollowedCorrectlyUntilNow
								successData = successDataReset.clone();
								if(successLog.get(logData[steadyID]) != null) successData = successLog.get(logData[steadyID]).split(":");			
								
								numberTotal = Integer.parseInt(successData[0]);
								numberSuccessInRow = Integer.parseInt(successData[1]);
								if(steadyIDSave.equals(logData[steadyID])){
									locationInformation_ += "true:" + logData[x] + ":" + logData[y] + "\n";
									successLog.remove(logData[steadyID]);
									
									if(successData[2].equals("true")) {
										successLog.put(logData[steadyID], (numberTotal+1) + ":" + (numberSuccessInRow+1) + ":true");
									}
									else{
										successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":false");
									}
									
									numberOfCorrectVehicles++;
								}
								else{
									locationInformation_ += "false:" + logData[x] + ":" + logData[y] + "\n";
									successLog.remove(logData[steadyID]);
									successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":false");
								}
								
								if(numberOfVehicles%10 == 0)logAnalyser_.updateInformationArea(numberOfVehicles, numberOfCorrectVehicles, false);
								
							}
							writeLocationInformationToFile(null, true);
							locationInformation_ = "";
							
							
							//kAnonymity and entropy
							for(int j = 0; j < dataOne.length-1; j+=2){
								numberOfCheckedVehicles++;
								
								logData = dataOne[j+1].split(":");
								
								//calculate expected way using the street speed the silent period time and a gui element to tune the value
								expectedWay2 = (double)((double)(silentPeriodTime/1000 * Integer.parseInt(logData[streetSpeed]))*tuneTimeValue);
								tmpDistance2 = maxDistance - expectedWay2;

								vehicleX = Long.parseLong( logData[x]);
								vehicleY = Long.parseLong( logData[y]);
								
								for(int k = 0; k < dataTwo.length-1; k+=2){
									logData2= dataTwo[k].split(":");
									
									dx2 = vehicleX - Long.parseLong(logData2[x]);
									dy2 = vehicleY - Long.parseLong(logData2[y]);
									
									//count all vehicles which are inside the reach of the first vehicle (for anonymity set) and get the distances (to the expected way) to calculate the probabilities later
									if((dx2*dx2 + dy2*dy2) <= maxDistanceSquared){
										tmpDistances = tmpDistance2 - Math.abs(expectedWay2 - Math.sqrt(dx2*dx2 + dy2*dy2));
										distanceInTotal +=  tmpDistances;
										distances.add(tmpDistances);
										numberOfFoundVehicles++;
									}
								}
								
								//get anonymity set and entropy
								//calculate probabilities for anonymity set. 
								prob = 0;
								for(Double l: distances){
									prob =  (float)(l.longValue()/distanceInTotal);
									if(prob >= 0 && prob < 1)entropiesAdded += ((1-prob)*((float)Math.log10((1-prob))/Math.log10(2)));
								}	
								distances.clear();
								distanceInTotal = 0;
							}
							
							
							tmpString1 = "";
							tmpString2 = "";
						}
					}
					tmpString1 += line + "\n";
					
					oldTime = actualTime;
				}	
			}
			
			tmpString1 = "";
			tmpString2 = "";
		} catch(Exception e) {
			e.printStackTrace();
		}

		
		logAnalyser_.updateInformationArea(numberOfVehicles, numberOfCorrectVehicles, false);
		
		int maxOfRow = 0;
		//calculate max crossed silent period
		for(Map.Entry<String, String> e : successLog.entrySet()){
			if(maxOfRow < Integer.parseInt(e.getValue().split(":")[0])) maxOfRow = Integer.parseInt(e.getValue().split(":")[0]);
		}
		
		maxOfRow++;
		int[][] dataCollection = new int[maxOfRow][maxOfRow];
		for(Map.Entry<String, String> e : successLog.entrySet()){
			dataCollection[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[1])] = dataCollection[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[1])] + 1;
		}

		float[] kAnoAndEntrArray = {(float)numberOfFoundVehicles/numberOfCheckedVehicles, (float) (-entropiesAdded/numberOfCheckedVehicles)};
		
		logAnalyser_.updateInformationArea("\n\n", false);
		logAnalyser_.updateInformationArea("#GNU-Plot Data Simple\n", true);
		logAnalyser_.updateInformationArea("#Privacy Probabilites k-Anonymity Entropy\n", true);
		logAnalyser_.updateInformationArea("Silent-Period " + (float)100*numberOfCorrectVehicles/numberOfVehicles + " " + kAnoAndEntrArray[0] + " " + kAnoAndEntrArray[1] + "\n", true);

		logAnalyser_.writeResultsToFile("simple");
		logAnalyser_.updateInformationArea("\n\n", false);
		logAnalyser_.updateInformationArea("#GNU-Plot Data Detail\n", true);
		logAnalyser_.updateInformationArea("#Success/Crossed", true);
		for(int i = 1; i < maxOfRow; i++) logAnalyser_.updateInformationArea(" " + i, true);
		logAnalyser_.updateInformationArea(" VehiclesLeft", true);
		for(int l = 0; l < maxOfRow;l++){
			logAnalyser_.updateInformationArea("\n" + l, true);	
			for(int m = 1; m < maxOfRow;m++){
				logAnalyser_.updateInformationArea(" " + dataCollection[m][l], true);
			}
		}
		
		logAnalyser_.updateProgressBar(100);
		logAnalyser_.guiControl("progressBar", false);
		logAnalyser_.updateProgressBar(0);
		
		//write results to file
		logAnalyser_.writeResultsToFile("detail");
	}

	/**
	 * advanced attack for silent periods
	 */
	public void advancedAttackSilentPeriod(){
		logAnalyser_.updateProgressBar(1);

		logAnalyser_.updateInformationArea(0, 0, false);
		
		//hash map to follow vehicles through mix zones
		HashMap<String,String> successLog = new HashMap<String,String>();

		
		//save and load data (analysis will be faster the second time, e.g. after changing variables)
		if(logAnalyser_.isFilePathChanged()){
			savedLog[0] = readFile(logAnalyser_.getActualJob());
			logAnalyser_.setFilePathChanged(false);
		}	
	
		logAnalyser_.updateProgressBar(2);

		writeLocationInformationToFile("Silent Period\n******************\n", false);
		
		System.out.println("file loaded...");
		
		//temp variables to iterate through data
		String line = "";
		String[] logData;
		String[] logData2;
		String[] logData3;
		String[] logData4;

		//variables for statistics
		int numberOfVehicles = 0;
		int numberOfCorrectVehicles = 0;
		
		//index of log data (useful if log structure changes)
		int timestamp = 0;
		int steadyID = 2;
		int streetSpeed = 16;
		int x = 20;
		int y = 22;

		
		//values for variables set in GUI			
		double tuneTimeValue =  logAnalyser_.getGuiElement("tuneTimeValue");
		double limitToAngle = logAnalyser_.getGuiElement("limitToAngle");

		
		String tmpString1 = "";
		String tmpString2 = "";
		
		//this arrays a just to save the beacons-blocks before and after the silent period
		String dataOne[] = null;
		String dataTwo[] = null;
		
		int silentPeriodTime = 0;
		
		int actualTime = 0;
		int oldTime = 0;
		
		boolean firstLine = true;
		
		//variables for calculation
		float x1 = 0;
		float x2 = 0;
		float y1 = 0;
		float y2 = 0;
		double expectedWay = 0;
		float v1 = 0;
		float v2 = 0;
		float normalize = 0;
		float factor = 999999999;
		float dx = 0;
		float dy = 0;
		float x3 = 0;
		float x4 = 0;
		float y3 = 0;
		float y4 = 0;
		double vector1x = 0;
		double vector1y = 0;
			
		double vector2x = 0;
		double vector2y = 0;
			
		double vector = 0;
		double vector2 = 0; 
			
		double cos = 0;
		double winkelBogenmass = 0;
		
		float tmpFactor = 0;
		
		//variables for kAnonymity and Entropy
		//constant variable (max. driving speed of a vehicle)
		double maxDrivingSpeed =  6944.44444444444444444444444444444444444;
		double maxDistance = 0;
		double maxDistanceSquared = 0;
		long vehicleX = 0;
		long vehicleY = 0;
		long dx2 = 0;
		long dy2 = 0;
		double expectedWay2 = 0;
		int numberOfCheckedVehicles = 0;
		long numberOfFoundVehicles = 0;
		double distanceInTotal = 0;
		ArrayList<Double> distances = new ArrayList<Double>();
		double tmpDistance2 = 0;
		double tmpDistances = 0;
		double entropiesAdded = 0;
		float prob = 0;
		
		//variables for the progress bar
		long amountOfLines = savedLog[0].split("\n").length;
		long updateEveryNLine = amountOfLines/98;
		long counter2 = 0;

		//read file into array separated by silent periods
		BufferedReader reader = new BufferedReader(new StringReader(savedLog[0]));        
		try {
			while ((line = reader.readLine()) != null) { 
				counter2++;
				//update progress bar
				if(counter2%updateEveryNLine ==0) logAnalyser_.addToProgressBar(1);
				//check if it's the correct log type
				if(firstLine){
					firstLine = false;
					silentPeriodHeader = line;
					
					silentPeriodTime = Integer.parseInt(silentPeriodHeader.split(":")[2]);
					
					maxDistance = maxDrivingSpeed*(double)(silentPeriodTime/1000);
					maxDistanceSquared = maxDistance*maxDistance;
					
					if(!silentPeriodHeader.substring(0, 8).equals("Silent P")){
						JOptionPane.showMessageDialog(null, Messages.getString("LogOperations.WrongLogType"), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				//split log after every silent period and put data into array list. Even it takes long to split up the data, we get a performance boost later for the attack
				else{
					actualTime = Integer.parseInt(line.split(":")[timestamp]);
					
					if(oldTime != 0 && Math.abs(actualTime - oldTime) > 1000){
						if(tmpString2.equals("")){
							tmpString2 = tmpString1;
							tmpString1 = "";
						}
						else{
							//read the 2 beacon blocks
							dataOne = tmpString2.split("\n");
							dataTwo = tmpString1.split("\n");
							
						
							String steadyIDSave = "";
							//iterate through the first block
							for(int j = 0; j < dataOne.length-1; j+=2){
								numberOfVehicles++;
								
								//first beacon and second beacon (needed to calculate a vector)
								logData = dataOne[j].split(":");
								logData2 = dataOne[j+1].split(":");
												
								//calculate estimated way
								x1 = Integer.parseInt(logData[x]);
								x2 = Integer.parseInt(logData2[x]);
								y1 = Integer.parseInt(logData[y]);
								y2 = Integer.parseInt(logData2[y]);
								
								//calculates how far the vehicle could drive at full speed
								expectedWay = (double)(silentPeriodTime/1000 * Integer.parseInt(logData2[streetSpeed]))*tuneTimeValue;
									
								//calculate vector
								v1 = x2 - x1;
								v2 = y2 - y1;
								normalize = (float) (1/Math.sqrt((double)((v1*v1)+(v2*v2))));
								v1 *= normalize * expectedWay;
								v2 *= normalize * expectedWay;
								v1 += x2;
								v2 += y2;
												
								factor = 999999999;
								
								//now iterate through the second block 
								for(int k = 0; k < dataTwo.length-1; k+=2){
									//first beacon and second beacon (needed to calculate a vector)
									logData3= dataTwo[k].split(":");
									logData4= dataTwo[k+1].split(":");
									
									dx = v1 - Integer.parseInt(logData3[x]);
									dy = v2 - Integer.parseInt(logData3[y]);
									
									
									x3 = Integer.parseInt(logData3[x]);
									x4 = Integer.parseInt(logData4[x]);
									y3 = Integer.parseInt(logData3[y]);
									y4 = Integer.parseInt(logData4[y]);
									
									//calculate 2. vector and get the angle (it's more likely that the vehicle won't change his vector. So we estimate that the vehicle drives straight ahead and is in this area)
									vector1x = (x2 - x1);
									vector1y = (y2 - y1);
									
									vector2x = (x4 - x3);
									vector2y = (y4 - y3);
									
									vector = vector1x*vector2x + vector1y*vector2y;
									vector2 = Math.sqrt(vector1x*vector1x + vector1y*vector1y) * Math.sqrt(vector2x*vector2x + vector2y*vector2y); 
									
									cos = vector / vector2;
									winkelBogenmass = Math.acos(cos);
								
									tmpFactor = (dx*dx + dy*dy);
									
									//save smallest factor
									if(factor > tmpFactor && Math.toDegrees(winkelBogenmass) < limitToAngle) {
										factor = tmpFactor;
										steadyIDSave = logData3[steadyID];
									}
								}
								
								//update states to log the movement of vehicles through more mix-zones. Format: Number:Number:Boolean -> NumberOfPassedMixZones:NumerOfSuccessfulPassedZones(only in a row):BooleanValueToShowIfVehicleWasFollowedCorrectlyUntilNow
								String[] successData = {"0","0","true"};
								if(successLog.get(logData[steadyID]) != null) successData = successLog.get(logData[steadyID]).split(":");			
								
								int numberTotal = Integer.parseInt(successData[0]);
								int numberSuccessInRow = Integer.parseInt(successData[1]);
								if(steadyIDSave.equals(logData[steadyID])){
									locationInformation_ += "true:" + logData[x] + ":" + logData[y] + "\n";

									successLog.remove(logData[steadyID]);
									
									if(successData[2].equals("true")) {
										successLog.put(logData[steadyID], (numberTotal+1) + ":" + (numberSuccessInRow+1) + ":true");
									}
									else{
										successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":false");
									}
									
									numberOfCorrectVehicles++;
								}
								else{
									locationInformation_ += "false:" + logData[x] + ":" + logData[y] + "\n";
									successLog.remove(logData[steadyID]);
									successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":false");
								}
								
								if(numberOfVehicles%10 == 0)logAnalyser_.updateInformationArea(numberOfVehicles, numberOfCorrectVehicles, false);
								
							}	
							writeLocationInformationToFile(null, true);
							locationInformation_ = "";
							
							
							//kAnonymity and entropy
							for(int j = 0; j < dataOne.length-1; j+=2){
								numberOfCheckedVehicles++;
								
								logData = dataOne[j+1].split(":");
								
								//calculate expected way using the street speed the silent period time and a gui element to tune the value
								expectedWay2 = (double)((double)(silentPeriodTime/1000 * Integer.parseInt(logData[streetSpeed]))*tuneTimeValue);
								tmpDistance2 = maxDistance - expectedWay2;

								vehicleX = Long.parseLong( logData[x]);
								vehicleY = Long.parseLong( logData[y]);
								
								for(int k = 0; k < dataTwo.length-1; k+=2){
									logData2= dataTwo[k].split(":");
									
									dx2 = vehicleX - Long.parseLong(logData2[x]);
									dy2 = vehicleY - Long.parseLong(logData2[y]);
									
									//count all vehicles which are inside the reach of the first vehicle (for anonymity set) and get the distances (to the expected way) to calculate the probabilities later
									if((dx2*dx2 + dy2*dy2) <= maxDistanceSquared){
										tmpDistances = tmpDistance2 - Math.abs(expectedWay2 - Math.sqrt(dx2*dx2 + dy2*dy2));
										distanceInTotal += tmpDistances;
										distances.add(tmpDistances);
										numberOfFoundVehicles++;
									}
								}	
								
								//get anonymity set and entropy
								//calculate probabilities for anonymity set. 
								prob = 0;
								for(Double l: distances){
									prob =  (float)(l.longValue()/distanceInTotal);
									if(prob >= 0 && prob < 1)entropiesAdded += ((1-prob)*((float)Math.log10((1-prob))/Math.log10(2)));
								}	
								distances.clear();
								distanceInTotal = 0;
							}
							
							
							tmpString1 = "";
							tmpString2 = "";
						}
					}
					tmpString1 += line + "\n";
					
					oldTime = actualTime;						
				}	
			}
			tmpString1 = "";
			tmpString2 = "";
		} catch(Exception e) {
			e.printStackTrace();
		}

		int maxOfRow = 0;
		//calculate max crossed mix zones
		for(Map.Entry<String, String> e : successLog.entrySet()){
			if(maxOfRow < Integer.parseInt(e.getValue().split(":")[0])) maxOfRow = Integer.parseInt(e.getValue().split(":")[0]);
		}
		
		maxOfRow++;
		int[][] dataCollection = new int[maxOfRow][maxOfRow];
		for(Map.Entry<String, String> e : successLog.entrySet()){
			dataCollection[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[1])] = dataCollection[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[1])] + 1;
		}
		
		float[] kAnoAndEntrArray = {(float)numberOfFoundVehicles/numberOfCheckedVehicles, (float) (-entropiesAdded/numberOfCheckedVehicles)};
		logAnalyser_.updateInformationArea("\n\n", false);
		logAnalyser_.updateInformationArea("#GNU-Plot Data Simple\n", true);
		logAnalyser_.updateInformationArea("#Privacy Probabilites k-Anonymity Entropy\n", true);
		logAnalyser_.updateInformationArea("Silent-Period " + (float)100*numberOfCorrectVehicles/numberOfVehicles + " " + kAnoAndEntrArray[0] + " " + kAnoAndEntrArray[1] + "\n", true);
		
		logAnalyser_.writeResultsToFile("simple");
		logAnalyser_.updateInformationArea("\n\n", false);
		logAnalyser_.updateInformationArea("#GNU-Plot Data Detail\n", true);
		logAnalyser_.updateInformationArea("#Success/Crossed", true);
		for(int i = 1; i < maxOfRow; i++) logAnalyser_.updateInformationArea(" " + i, true);
		logAnalyser_.updateInformationArea(" VehiclesLeft", true);

		for(int l = 0; l < maxOfRow;l++){
			logAnalyser_.updateInformationArea("\n" + l, true);	
			for(int m = 1; m < maxOfRow;m++){
				logAnalyser_.updateInformationArea(" " + dataCollection[m][l], true);
			}
		}
		
		
		
		logAnalyser_.updateProgressBar(100);
		logAnalyser_.guiControl("progressBar", false);
		logAnalyser_.updateProgressBar(0);
		logAnalyser_.writeResultsToFile("detail");
	}
	

	
	/**
	 * return the radius of a node written in the header of a log file
	 */
	public int getNodeRadius(String nodeID, String header){
		int returnRadius = -1;
		String line = "";
		String[] informationArray;
		
		BufferedReader reader = new BufferedReader(new StringReader(header));        
		try {
			//read vehicle header line by line
			while ((line = reader.readLine()) != null) { 
				informationArray = line.split(":");
				
				if(informationArray != null && informationArray.length > 4){
					if(informationArray[2].equals(nodeID)){
						return Integer.parseInt(informationArray[4]);
					}
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return returnRadius;
	}
	
	
	public void standardAttackSlow(){
		logAnalyser_.updateProgressBar(0);
		logAnalyser_.updateInformationArea(0, 0, false);
		
		//hash map to follow vehicles through more mix zones
		HashMap<String,String> successLog = new HashMap<String,String>();
		
		logAnalyser_.updateProgressBar(5);
		String completeFile = "";
		//save and load data (analysis will be faster the second time, e.g. after changing variables)
		if(logAnalyser_.isFilePathChanged()){
			completeFile = readFile(logAnalyser_.getActualJob());
			savedLog = completeFile.split("\n");
			logAnalyser_.setFilePathChanged(false);
		}	
		
		logAnalyser_.updateProgressBar(10);
		
		//check if we have a correct log -> if yes quit analysis
		if(savedLog[0] == null || !savedLog[0].contains("Slow")){
			JOptionPane.showMessageDialog(null, Messages.getString("LogOperations.WrongLogType"), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		writeLocationInformationToFile("Slow-Modell\n******************\n", false);
		
		String[] slowHeaderArray = savedLog[0].split(":");
		
		//variables for statistics
		int numberOfVehicles = 0;
		int numberOfVehiclesLeftTooSoon = 0;
		int numberOfCorrectVehicles = 0;
		
		System.out.println("file loaded...");
		
		//temp line variable
		String line = "";
		
		//variable to save split line of log (":")
		String[] logData;
		String[] logData2;
		
		//index of log data (useful if log structure changes)
		int timestamp = 0;
		int steadyID = 2;
		int pseudonym = 4;
		int direction = 12;
		int vehicleSpeed = 18;
		int x = 20;
		int y = 22;
		
		boolean vehicleLeftTooSoon = false;
		
		//success log
		String[] successData = null;
		String[] successDataReset = {"0","0", "0", "true"};
		int numberTotal = 0;
		int numberSuccessInRow = 0;
		int numberSuccessInRowLeftTooSoon = 0;
		
		//values for variables set in GUI	
		double tuneTimeValue =  logAnalyser_.getGuiElement("tuneTimeValue");
		double maxSlowSearchTime = logAnalyser_.getGuiElement("maxSlowSearchTime");
		double timeToChangePseudo = Double.parseDouble(slowHeaderArray[3]);

		//initializing variables
		float expectedWay = 0;
		String savedSteadyID = "";
		double savedScore = 0;

		
		
		long dx = 0;
		long dy = 0;
		
		double score = 0;
		
		double maxDrivingDistance = Integer.parseInt(slowHeaderArray[1])*(maxSlowSearchTime/1000);
		
		//variables for the progressbar
		long amountOfLines = savedLog.length;
		long updateEveryNLine = amountOfLines/80;
		long counter = 0;
		if(updateEveryNLine == 0) updateEveryNLine = 1;
		
		//a ArrayList for all vehicles which reached destination.
		ArrayList<String> vehiclesReachedGoalArray = new ArrayList<String>();
				
		BufferedReader reader = new BufferedReader(new StringReader(completeFile));     
	
		ArrayList<String> blacklist = new ArrayList<String>();

		try {
			//read vehicle data line by line and check every vehicle
			while ((line = reader.readLine()) != null) {  		
				//skip empty lines and header lines
				if (line.length() > 8 && !line.substring(0, 4).equals("Slow")){
					//in this loop we look for all vehicles which started a slow period. --> skip vehicles with OUT direction
					logData = line.split(":");
					
					if(logData[0].equals("VehicleReachedDestination")){
						
						vehiclesReachedGoalArray.add(logData[1] + ":" + logData[2]);	
					}
					else if(logData[direction].equals("IN")){
						//take the second beacon. It is more actual
						line = reader.readLine();
						logData = line.split(":");
																		
								
						//now check every vehicle that left the mix zone in the time (expectedTime + buffer)
						//set mark for reader (performance tweak, we can resume our buffer on this position later)
						reader.mark(completeFile.length());							
												
						try {
							//read vehicle data line by line
							while ((line = reader.readLine()) != null) {   
								logData2 = line.split(":");
								
								

								if(logData2[0].equals("VehicleReachedDestination")){
									
								}
								//we are looking for vehicles leaving the slow period, skip IN!
								else if(logData2[direction].equals("IN")){
									//always two beacons, skip 1 more
									reader.readLine();
								}
								else{
									reader.readLine();
									if(logData[pseudonym].equals(logData2[pseudonym])) {
										blacklist.add(line);
										
										break;
									}
									
									//we only check vehicles until a selected time (performance tweak, calculated expected time + buffer)
									if((Integer.parseInt(logData[timestamp]) + timeToChangePseudo) < Integer.parseInt(logData2[timestamp])){
										break;
									}
								}
							}
						} catch(IOException e) {
							System.out.println("Error while doing standard slow attack");
							e.printStackTrace();
						}

						//jump to mark
						reader.reset();	
					}
				}
			}
		} catch(IOException e) {
			System.out.println("Error while doing standard slow attack");
			e.printStackTrace();
		}
		
		reader = new BufferedReader(new StringReader(completeFile));  
		try {
			//read vehicle data line by line and check every vehicle
			while ((line = reader.readLine()) != null) {  
				counter++;
				//update progressbar
				if(counter%updateEveryNLine == 0) logAnalyser_.addToProgressBar(1);
				
				//skip empty lines and header lines
				if (line.length() > 8 && !line.substring(0, 4).equals("Slow")){
					//in this loop we look for all vehicles which started a slow period. --> skip vehicles with OUT direction
					logData = line.split(":");
					
					if(logData[0].equals("VehicleReachedDestination")){
						
					}
					else if(logData[direction].equals("OUT")){
						
					}
					else if(logData[direction].equals("IN") && !vehiclesReachedGoalArray.contains(logData[steadyID] + ":" + logData[pseudonym])){
	
						//take the second beacon. It is more actual
						line = reader.readLine();
						logData = line.split(":");
																		
								
						//now check every vehicle that left the mix zone in the time (expectedTime + buffer)
						//set mark for reader (performance tweak, we can resume our buffer on this position later)
						reader.mark(completeFile.length());
								
						//tmp variable to save steadyID and the factor
						savedSteadyID = "";
						savedScore = 999999999;
							
												
						try {
							//read vehicle data line by line
							while ((line = reader.readLine()) != null) {   
								logData2 = line.split(":");
								
							

								if(logData2[0].equals("VehicleReachedDestination")){
									
								}
								//we are looking for vehicles leaving the slow period, skip IN!
								else if(logData2[direction].equals("IN")){
									//always two beacons, skip 1 more
									reader.readLine();
								}
								else{
									reader.readLine();

									//we only check vehicles until a selected time (performance tweak, calculated expected time + buffer)
									if((Integer.parseInt(logData[timestamp]) + maxSlowSearchTime) < Integer.parseInt(logData2[timestamp])){
										break;
									}
									
									//check if vehicle was under the min time to chance pseudonym
									if(logData[pseudonym].equals(logData2[pseudonym])){
										savedSteadyID = logData2[steadyID];
										numberOfVehiclesLeftTooSoon++;
										vehicleLeftTooSoon = true;
										
										
										break;
									}
									
									if(!blacklist.contains(line)){
										dx = Integer.parseInt(logData[x]) - Integer.parseInt(logData2[x]);
										dy = Integer.parseInt(logData[y]) - Integer.parseInt(logData2[y]);

										if((maxDrivingDistance*maxDrivingDistance) > (dx*dx + dy*dy)){

											expectedWay = (float) ((float)((Integer.parseInt(logData2[timestamp]) - Integer.parseInt(logData[timestamp]))/1000 * Integer.parseInt(logData[vehicleSpeed])) / tuneTimeValue);

											score = Math.abs((dx*dx + dy*dy) - (expectedWay*expectedWay));
											
											//always save the smallest vehicle with the smallest factor
											
											if(score < savedScore){
												savedScore = score;
												savedSteadyID = logData2[steadyID];
											}
		
										}
									}
								}
							}
						} catch(IOException e) {
							System.out.println("Error while doing standard slow attack");
							e.printStackTrace();
						}
						

						//update states to log the movement of vehicles through more slows. Format:
						successData = successDataReset.clone();
						if(successLog.get(logData[steadyID]) != null) successData = successLog.get(logData[steadyID]).split(":");			
						
						numberTotal = Integer.parseInt(successData[0]);
						numberSuccessInRow = Integer.parseInt(successData[1]);
						numberSuccessInRowLeftTooSoon = Integer.parseInt(successData[2]);
						
						numberOfVehicles++;
						//Test ob in LeftTooSoon ein Fehler ist :
						if(vehicleLeftTooSoon){ 
							locationInformation_ += "slow:" + logData[x] + ":" + logData[y] + "\n";
							numberOfCorrectVehicles++; 
							
							successLog.remove(logData[steadyID]);
							
							if(successData[3].equals("true")) {
								successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":" + (numberSuccessInRowLeftTooSoon+1) + ":true");
							}
							else{
								successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow  + ":" + numberSuccessInRowLeftTooSoon + ":false");
							}
							
						}
						else if(savedSteadyID.equals(logData[steadyID])){
							locationInformation_ += "true:" + logData[x] + ":" + logData[y] + "\n";
							successLog.remove(logData[steadyID]);
							
							if(successData[3].equals("true")) {
								successLog.put(logData[steadyID], (numberTotal+1) + ":" + (numberSuccessInRow+1) + ":" + (numberSuccessInRowLeftTooSoon+1) + ":true");
							}
							else{
								successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":" + numberSuccessInRowLeftTooSoon  + ":false");
							}
							
							numberOfCorrectVehicles++;
						}
						else{
							locationInformation_ += "false:" + logData[x] + ":" + logData[y] + "\n";
							successLog.remove(logData[steadyID]);
							successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":" + numberSuccessInRowLeftTooSoon + ":false");
						}
						//Sicherungskopie
						/*
						if(vehicleLeftTooSoon){ 
							locationInformation_ += "slow:" + logData[x] + ":" + logData[y] + "\n";
							numberOfCorrectVehicles++; 
							
							successLog.remove(logData[steadyID]);
							
							if(successData[3].equals("true")) {
								successLog.put(logData[steadyID], (numberTotal+1) + ":" + (numberSuccessInRow+1) + ":" + (numberSuccessInRowLeftTooSoon+1) + ":true");
							}
							else{
								successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow  + ":" + (numberSuccessInRowLeftTooSoon) + ":false");
							}
							
						}
						else if(savedSteadyID.equals(logData[steadyID])){
							locationInformation_ += "true:" + logData[x] + ":" + logData[y] + "\n";
							successLog.remove(logData[steadyID]);
							
							if(successData[3].equals("true")) {
								successLog.put(logData[steadyID], (numberTotal+1) + ":" + (numberSuccessInRow+1) + ":" + numberSuccessInRowLeftTooSoon + ":true");
							}
							else{
								successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":" + numberSuccessInRowLeftTooSoon  + ":false");
							}
							
							numberOfCorrectVehicles++;
						}
						else{
							locationInformation_ += "false:" + logData[x] + ":" + logData[y] + "\n";
							successLog.remove(logData[steadyID]);
							successLog.put(logData[steadyID], (numberTotal+1) + ":" + numberSuccessInRow + ":" + numberSuccessInRowLeftTooSoon + ":false");
						}
						*/
						
						if(numberOfVehicles%10 == 0)logAnalyser_.updateInformationArea(numberOfVehicles, numberOfVehiclesLeftTooSoon, numberOfCorrectVehicles, false);
						
						writeLocationInformationToFile(null, true);
						locationInformation_ = "";
	
						vehicleLeftTooSoon = false;	

						
						//jump to mark
						reader.reset();	
					}
				}
					
			}
		} catch(IOException e) {
			System.out.println("Error while doing standard slow attack");
			e.printStackTrace();
		}

		logAnalyser_.updateInformationArea(numberOfVehicles, numberOfVehiclesLeftTooSoon, numberOfCorrectVehicles, true);
/*
		System.out.println("durchscnitt ano:" + (anoCounter  / anoTotal));
		System.out.println("total:" + numberOfVehicles + "tosoon: " + numberOfVehiclesLeftTooSoon + "numberOfCorrect:" + numberOfCorrectVehicles);
		System.out.println("countertest" + countertest);
		System.out.println("time in slow <= 3000:" + timeInSlowCounter);
		*/
		int maxOfRow = 0;
		//calculate max crossed slow
		for(Map.Entry<String, String> e : successLog.entrySet()){
			if(maxOfRow < Integer.parseInt(e.getValue().split(":")[0])) maxOfRow = Integer.parseInt(e.getValue().split(":")[0]);
		}
		
		maxOfRow++;
		int[][] dataCollection = new int[maxOfRow][maxOfRow];
		int[][] dataCollection2 = new int[maxOfRow][maxOfRow];
		//calculate advanced stats
		for(Map.Entry<String, String> e : successLog.entrySet()){
			dataCollection[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[1])] = dataCollection[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[1])] + 1;
			dataCollection2[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[2])] = dataCollection2[Integer.parseInt(e.getValue().split(":")[0])][Integer.parseInt(e.getValue().split(":")[2])] + 1;
		}

		logAnalyser_.updateInformationArea("\n\n", false);
		logAnalyser_.updateInformationArea("#GNU-Plot Data Simple\n", true);
		logAnalyser_.updateInformationArea("#Privacy NumberOfVehicles NumberOfCorrectVehicles NumberOfVehiclesLeftToSoon ProbabilityAll ProbabiltyWithoutLeftToSoon\n", true);
		logAnalyser_.updateInformationArea("Slow " + numberOfVehicles + " " +  numberOfCorrectVehicles + " " + numberOfVehiclesLeftTooSoon + " " + (((float)((float)numberOfCorrectVehicles/(float)numberOfVehicles))*100) + " " + (float)((numberOfCorrectVehicles-numberOfVehiclesLeftTooSoon)*100)/(numberOfVehicles-numberOfVehiclesLeftTooSoon)  + "\n", true);

		logAnalyser_.writeResultsToFile("simple");
		logAnalyser_.updateInformationArea("\n\n", false);
		logAnalyser_.updateInformationArea("#GNU-Plot Data Detail\n", true);
		logAnalyser_.updateInformationArea("#Success/Crossed", true);
		for(int i = 1; i < maxOfRow; i++) logAnalyser_.updateInformationArea(" " + i, true);
		logAnalyser_.updateInformationArea(" VehiclesLeft", true);
	

		for(int l = 0; l < maxOfRow;l++){
			logAnalyser_.updateInformationArea("\n" + l, true);	
			for(int m = 1; m < maxOfRow;m++){

				logAnalyser_.updateInformationArea(" " + dataCollection[m][l], true);
			}
		}
		
		//write results to file
		logAnalyser_.writeResultsToFile("detail");
		
		logAnalyser_.updateInformationArea("#GNU-Plot Data Detail WithVehiclesLeftTooSoon\n", true);
		logAnalyser_.updateInformationArea("#Success/Crossed", true);
		for(int i = 1; i < maxOfRow; i++) logAnalyser_.updateInformationArea(" " + i, true);
		logAnalyser_.updateInformationArea(" VehiclesLeft", true);
	

		for(int l = 0; l < maxOfRow;l++){
			logAnalyser_.updateInformationArea("\n" + l, true);	
			for(int m = 1; m < maxOfRow;m++){

				logAnalyser_.updateInformationArea(" " + dataCollection2[m][l], true);
			}
			//this is the old version of the detailed calculator. Now there is a script -> accumulateDetailedLogFiles() in ReportingControlPanel

		}
		
		logAnalyser_.updateProgressBar(100);
		logAnalyser_.guiControl("progressBar", false);
		logAnalyser_.updateProgressBar(0);
		
		//write results to file
		logAnalyser_.writeResultsToFile("withVehiclesLeftTooSoon");
		
		calculateAverageSlowTime(completeFile);
	}
	

	public void calculateAverageSlowTime(String file){
		String line = "";
		String[] array;

		BufferedReader reader = new BufferedReader(new StringReader(file));        
		try {
			//read vehicle data line by line and check every vehicle
			while ((line = reader.readLine()) != null) { 
				array = line.split(":");
				if(array.length == 25){

				}
			}
		} catch(IOException e) {
			System.out.println("Error while doing standard slow attack");
			e.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * return k-anonymity of a mix log (for a chosen node id)
	 */
	public float[] getKAnonymityInMix(String theNodeID, String log){
		String line = "";
		String[]logData;
		String[]logData2;
		
		//index of log data (useful if log structure changes)
		int steadyID = 2;
		int nodeID = 10;
		int direction = 12;

		//variables for statistics
		int numberOfVehicles = 0;
		int totalNumberOfVehicles = 0;
		int tmpNumberOfVehicles = 0;
		ArrayList<String> savedIDs = new ArrayList<String>();
		ArrayList<String> savedInMixZone = new ArrayList<String>();
		boolean endOfFile = true;
		
		//iterate through beacons
		BufferedReader reader = new BufferedReader(new StringReader(log));        
		try {
			//read vehicle data line by line
			while ((line = reader.readLine()) != null) {  
				if (line.length() > 0){
					logData = line.split(":");
					
					if(logData != null && logData.length > 18){
						//lets find the OUTs belonging to the INs
						if(logData[direction].equals("IN") && logData[nodeID].equals(theNodeID)){
							
							//log all vehicles which drive into the mix-zone
							savedInMixZone.add(logData[steadyID]);
							//set mark for reader (performance tweak)
							reader.mark(log.length());
							numberOfVehicles++;
							
							try {
								//read vehicle data line by line
								while ((line = reader.readLine()) != null) {   
									logData2 = line.split(":");
									
									//delete all vehicles which drive out of the mix-zone
									if(logData2[direction].equals("OUT") && logData[nodeID].equals(theNodeID)) if(savedInMixZone.contains(logData2[steadyID])) savedInMixZone.remove(logData2[steadyID]);
										
									//vehicles leaves mix zone
									if(logData[steadyID].equals(logData2[steadyID])){
										if(logData2[direction].equals("OUT")) endOfFile = false;
										break;
									}
									
									//vehicle is still in mix zone
									if(logData2[nodeID].equals(theNodeID)){
										//a vehicle could enter and leave mix zone while the other vehicle is still in the zone. Saving logged vehicle IDs to avoid counting vehicles double
										if(!savedIDs.contains(logData2[steadyID])){
											savedIDs.add(logData2[steadyID]);
											tmpNumberOfVehicles++;
										}
									}
								}
							} catch(IOException e) {
									e.printStackTrace();
							}
							
							//only use the data if end of file has not been reached
							if(endOfFile) {
								numberOfVehicles--;
								tmpNumberOfVehicles = 0;
							}
							savedIDs.clear();
							totalNumberOfVehicles += tmpNumberOfVehicles + savedInMixZone.size();
							tmpNumberOfVehicles = 0;
							endOfFile = true;
							//jump to mark
							reader.reset();
						}	
					}
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		float[] arr = {(float)totalNumberOfVehicles/numberOfVehicles, totalNumberOfVehicles, numberOfVehicles};
		return arr;
	}
	
	
	/**
	 * Method to read a file
	 */
	public String readFile(String filePath){		
		byte[] data = null;
			try {
				java.io.FileInputStream f1;
				f1 = new java.io.FileInputStream(filePath);
			    FileChannel fc = f1.getChannel();
			    data = new byte[(int) fc.size()];
			    MappedByteBuffer b = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			    
			    b.get(data);
			    b.clear();
			    fc.close();
			    f1.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return new String(data);
	}
	
	/**
	 * Method to read a file. Reads a file and returns the header and the content split in an String array
	 */
	public String[] readFileAndHeader(String filePath){
		byte[] data = null;
		try {
			java.io.FileInputStream f1;
			f1 = new java.io.FileInputStream(filePath);
		    FileChannel fc = f1.getChannel();
		    data = new byte[(int) fc.size()];
		    MappedByteBuffer b = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    
		    b.get(data);
		    b.clear();
		    fc.close();
		    f1.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new String(data).split(java.util.regex.Pattern.quote("*******************"));
	}
	

	/**
	 * Method to read a file without the header of the log
	 */
	public String readFileWithoutHeader(String filePath){
		String returnString = "";
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(filePath);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			boolean headerFound = false;
			//Read file line By line
			while ((strLine = br.readLine()) != null) {
				if(headerFound)returnString += "\n" +strLine;
				if(strLine.equals("*******************")) headerFound = true;
			}
			  
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		 
		return returnString;
	}
	

	/**
	 * Analyze traffic data to get information for an attack (for a mix node
	 */
	public Object[] getDataOfMixZoneLog(String[] file, String mixNodeID, int totalNumberOfAnalysedZones){		
		//read log. Be careful with big logs!!!
		String header = file[0];
		String log = file[1];
		
		//temp variables
		String line = "";
		String[] logData;
	
		//variables for statistics
		
		//index of log data (useful if log structure changes)
		int timestamp = 0;
		int steadyID = 2;
		int nodeID = 10;
		int direction = 12;
		int port = 20;
		
		//mix Zone Information
		String[] mixInfo;
		int mixZonePorts = 0;
		
		//get amount of ports in mix zone
		String[] headers = header.split("\n");
		for(int i = 0; i < headers.length; i++){
			String[] lineSplit = headers[i].split(":");
			if(lineSplit[2].equals(mixNodeID)){
				mixInfo = lineSplit;
				mixZonePorts = mixInfo.length - 5;
				break;
			}
		}
		
		int portToPortSpeed[][] = new int[mixZonePorts][mixZonePorts];
		double portToPortProbability[][] = new double[mixZonePorts][mixZonePorts];
		int portToPortFrequency[][] = new int[mixZonePorts][mixZonePorts];

		Object[] returnValue = {portToPortSpeed, portToPortProbability, portToPortFrequency};

		//variables for progress bar status
		String[] tmpLines = log.split("\n");
		long amountOfLines = tmpLines.length;
		long updateEveryNLine = (amountOfLines/40)*totalNumberOfAnalysedZones;
		long counter = 0;
		
		BufferedReader reader = new BufferedReader(new StringReader(log));        
		try {
			//read vehicle data line by line
			while ((line = reader.readLine()) != null) {         
				counter++;
				//Update progress bar
				if(counter%updateEveryNLine ==0) logAnalyser_.addToProgressBar(1);
					
				if (line.length() > 0){
					logData = line.split(":");
					
					if(logData != null && logData.length > 18){
						//only attack vehicles which drive into a mix zone
						if(logData[direction].equals("IN") && logData[nodeID].equals(mixNodeID)){
							//counter for all vehicles
							
							//set mark for reader
							reader.mark(log.length());
							
							try {
								//read vehicle data line by line
								while ((line = reader.readLine()) != null) {   
									String[] logData2 = line.split(":");
									
									//only check vehicles that left the mix zone ("OUT") after our first vehicle went "IN". Also check if it's the same node.
									if(logData2[direction].equals("OUT") && logData2[nodeID].equals(mixNodeID)){
										//check if it's the same vehicle, if yes log the time to cross the mix zone.
										if(logData[steadyID].equals(logData2[steadyID])){
											portToPortSpeed[Integer.parseInt(logData[port])-1][Integer.parseInt(logData2[port])-1] = (int) (Long.parseLong(logData2[timestamp]) - Long.parseLong(logData[timestamp]));
											portToPortFrequency[Integer.parseInt(logData[port])-1][Integer.parseInt(logData2[port])-1] += 1; 
											
											break;
										}
									}
								}
							} catch(IOException e) {
									e.printStackTrace();
							}	
							
							//jump to mark
							reader.reset();
						}
					}
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		//calculate the probabilities 
		for(int i = 0; i < mixZonePorts; i++){
			int totalFrequenciesOfPort = 0;
			for(int j = 0; j < mixZonePorts; j++){
				totalFrequenciesOfPort += portToPortFrequency[i][j]; 
			}
			for(int k = 0; k < mixZonePorts; k++){
				if(totalFrequenciesOfPort != 0)portToPortProbability[i][k] = (double)portToPortFrequency[i][k] / totalFrequenciesOfPort; 
				else portToPortProbability[i][k] = 0;
			}
		}
		
		return returnValue;
	}
	
	public String getOperation() {
		return operation_;
	}

	public void setOperation(String operation) {
		this.operation_ = operation;
	}
	
	/**
	 * Writes advanced analyzed information to a file
	 */
	public void writeLocationInformationToFile(String header, boolean append){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(logAnalyser_.getActualJob().substring(0,logAnalyser_.getActualJob().length()-4) + "_" + logAnalyser_.getLogName() + "_locationInformation.log", append));
			if(header != null){
				out.write(header);
				out.close();
			}
			else{
				out.write(locationInformation_);
				out.close();
				locationInformation_ = "";
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();		
		}
	}
	
}
