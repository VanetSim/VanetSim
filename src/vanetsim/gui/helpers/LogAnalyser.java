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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


import vanetsim.gui.controlpanels.LogAnalyserDialog;
import vanetsim.localization.Messages;

public class LogAnalyser {
	/** gui mode or console mode */
	boolean guiMode = false;
	
	/** GUI */
	LogAnalyserDialog logDialog_;

	/** flag if file path has been changed */
	private boolean filePathChanged = true;

	/** Saves jobs. The Parameters will be the same. Useful if you want to analyze scenarios many times */
	private ArrayList<String> jobs = new ArrayList<String>();
	
	/** saves actual job path*/
	private String actualJob = "";
	
	/** saves actual plain job*/
	private String plainJob = "";
	
	/** thread for big calculations */
	private LogOperations operation_;
	
	/** String to save log output to file */
	private String logOutput_ = "";
	
	/** helps to name the output file of the log correctly */
	private String logName = "";
	
	/** saves log type */
	private String logType = "";
	
	/** saves attack type */
	private String attackType = "";
	
	/** flag if file path has been changed */
	private boolean advancedFilePathChanged = true;
	
	/** file path for advanced mix attack */
	private String advancedFilePath_ = "";
	
	/** elements to control the simulation */
	private double timeBufferValue = 0;
	private double tuneTimeValue = 0;
	private double biggerStreetValue = 0;
	private double smallerStreetValue = 0;
	private double drivesStraigthValue = 0;
	private double turnsValue = 0;
	private double makesUTurnValue = 0;
	private double limitToAngle = 0;
	private double maxSlowSearchTime = 0;
	private boolean useProbability = false;
	
	/**
	 * Constructor for GUI-Mode
	 * @param logDialog
	 */
	public LogAnalyser(LogAnalyserDialog logDialog){
		logDialog_ = logDialog;
		guiMode = true;
	}
	
	public static void main(String[] args){
		new LogAnalyser();
	}
	
	/**
	 * Constructor for Console Mode. The class will look for the file jobs.txt, read it and calculate the jobs
	 */
	public LogAnalyser(){
		System.out.println("Starting log analyser in console mode...");
		logDialog_ = null;
		guiMode = false;
		readJobs();
		startNextJob(true);
	}
	
	/**
	 * Handles the control elements of the GUI
	 * @param element
	 * @param value
	 */
	public void guiControl(String element, boolean value){
		if(guiMode){
			if(element.equals("startBtn"))logDialog_.getBtnStart_().setEnabled(value);
			if(element.equals("stopBtn"))logDialog_.getBtnStop_().setEnabled(value);
			if(element.equals("progressBar"))logDialog_.getProgressBar().setVisible(value);
			if(element.equals("copyBtn"))logDialog_.getCopyResultsBtn().setVisible(value);
		}
	}
	
	/**
	 * Updates the progress bar to the given value
	 * @param value
	 */
	public void updateProgressBar(int value){
		if(guiMode){
			logDialog_.getProgressBar().setValue(value);
		}
	}
	
	/**
	 * Adds a value to the progress bar. Also supports the console mode.
	 * @param value
	 */
	public void addToProgressBar(int value){
		if(guiMode){
			logDialog_.getProgressBar().setValue(logDialog_.getProgressBar().getValue() + value);
		}
		else System.out.print(".");
	}
	
	/**
	 * Returns a GUI element. Needed because of MVC
	 * @param name
	 * @return gui element
	 */
	public double getGuiElement(String name){
		if(name.equals("timeBufferValue")) return timeBufferValue;
		else if(name.equals("tuneTimeValue")) return tuneTimeValue;
		else if(name.equals("biggerStreetValue")) return biggerStreetValue;
		else if(name.equals("smallerStreetValue")) return smallerStreetValue;
		else if(name.equals("drivesStraigthValue")) return drivesStraigthValue;
		else if(name.equals("turnsValue")) return turnsValue;
		else if(name.equals("makesUTurnValue")) return makesUTurnValue;
		else if(name.equals("limitToAngle")) return limitToAngle;
		else if(name.equals("maxSlowSearchTime")) return maxSlowSearchTime;
		return 9999;
	}
	
	/**
	 * check if probabilities are used for the attack
	 * @return the probability
	 */
	public boolean isProbabilitiesOn(){
		if(guiMode) return logDialog_.getUseProbability_().isSelected();
		else return useProbability;
	}
	
	/** 
	 * Starts the next job and handles documentation
	 * 
	 */
	public void startNextJob(boolean firstJob){
		System.out.println("\nTime:" + new Date() + "\n");
		
		//only remove and document jobs if they have been completed fully
		if(jobs != null && jobs.size() > 0 && !firstJob) {
			documentJobsToFile(jobs.get(jobs.size() -1));
			jobs.remove(jobs.size() -1);
		}

		//get the next job
		if(jobs != null && jobs.size() > 0) {
			//skip commented lines
			if(jobs.get(jobs.size()-1).substring(0, 1).equals("#")){
				System.out.println("skipped job");
				startNextJob(false);
				return;
			}
			
			//save job
			actualJob = jobs.get(jobs.size()-1).split(":")[0];
			
			plainJob = jobs.get(jobs.size()-1);
			updateInformationArea("\nNext job: " + plainJob + "\n");
			
			filePathChanged = true;
			advancedFilePathChanged = true;
			
			//get parameters of the job
			logType = plainJob.split(":")[1];
			attackType = plainJob.split(":")[2];
			timeBufferValue = Double.parseDouble(plainJob.split(":")[3]);
			tuneTimeValue = Double.parseDouble(plainJob.split(":")[4]);
			biggerStreetValue = Double.parseDouble(plainJob.split(":")[5]);
			smallerStreetValue = Double.parseDouble(plainJob.split(":")[6]);
			drivesStraigthValue = Double.parseDouble(plainJob.split(":")[7]);
			turnsValue = Double.parseDouble(plainJob.split(":")[8]);
			makesUTurnValue = Double.parseDouble(plainJob.split(":")[9]);
			limitToAngle = Double.parseDouble(plainJob.split(":")[10]);
			useProbability = Boolean.parseBoolean(plainJob.split(":")[11]);
			advancedFilePath_ = plainJob.split(":")[12];
			if(plainJob.length() > 13) 	maxSlowSearchTime = Double.parseDouble(plainJob.split(":")[13]);
			
			//Used to update job status on web. Deactivated in this version. Check your simulation status anywhere ;)
			/*
			URL url;
			InputStream is;
			String s = new Date() + ": starting job:" + plainJob;
			try {
				url = new URL("http://url.de/updateJobs.php?job=" + s.replace(" ", "%20"));
				is = url.openStream();
				is.close();
			} catch (Exception e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		else {
			//if jobs list is empty
			updateInformationArea("\nno jobs in list...\n");
			return;
		}
		
		//update GUI
		guiControl("copyBtn", false);
		guiControl("progressBar", true);
		guiControl("startBtn", false);
		guiControl("stopBtn", true);
		logOutput_="";
		
		//now work the job ;-)
		//check which analysis should be used
		if(logType.equals("mixzone")){
			if(attackType.equals("standard")){
				System.out.println("Starting standard mix attack...");
				
				logName = "standard";
				operation_ = new LogOperations(this);
				operation_.setOperation("MixStandard");
				operation_.start();
			}
			else if(attackType.equals("advanced")){
				System.out.println("Starting advanced mix attack...");
				
				logName = "advanced";
				operation_ = new LogOperations(this);
				operation_.setOperation("MixAdvanced");
				operation_.start();					
			}		
		}
		else if(logType.equals("silentperiod")){
			if(attackType.equals("standard")){
				System.out.println("Starting standard silent period attack...");
				
				logName = "standard";
				operation_ = new LogOperations(this);
				operation_.setOperation("SilentStandard");
				operation_.start();					
			}
			else if(attackType.equals("advanced")){
				System.out.println("Starting advanced silent period attack...");
				
				logName = "advanced";
				operation_ = new LogOperations(this);
				operation_.setOperation("SilentAdvanced");
				operation_.start();					
			}	
		}
		else if(logType.equals("slow")){
			if(attackType.equals("standard")){
				System.out.println("Starting standard slow attack...");
				
				logName = "standard";
				operation_ = new LogOperations(this);
				operation_.setOperation("SlowStandard");
				operation_.start();					
			}
			else if(attackType.equals("advanced")){
				System.out.println("Starting advanced slow attack...");
				
				logName = "advanced";
				operation_ = new LogOperations(this);
				operation_.setOperation("SlowAdvanced");
				operation_.start();					
			}	
		}
		else System.out.println("all jobs finished");
		
		
		//check if there a new jobs in the queue which can be loaded
		addNewJobs();
		
		System.out.println("Time:" + new Date());
	}
	
	/**
	 * Adds a job with all parameters
	 * @param filePath
	 */
	public void addJob(String filePath){
		jobs.add(filePath + ":" + logType + ":" + attackType + ":" + getGuiElement("timeBufferValue") + ":" + getGuiElement("tuneTimeValue") + ":" + getGuiElement("biggerStreetValue") + ":" + getGuiElement("smallerStreetValue") + ":" + getGuiElement("drivesStraigthValue") + ":" + getGuiElement("turnsValue") + ":" + getGuiElement("makesUTurnValue") + ":" + getGuiElement("limitToAngle") + ":" + isProbabilitiesOn() + ":" + advancedFilePath_ + ":" + maxSlowSearchTime);
		if(guiMode) updateInformationArea("\n" + filePath  + ":" +  logType + ":" + attackType + ":" + ":" + getGuiElement("timeBufferValue") + ":" + getGuiElement("tuneTimeValue") + ":" + getGuiElement("biggerStreetValue") + ":" + getGuiElement("smallerStreetValue") + ":" + getGuiElement("drivesStraigthValue") + ":" + getGuiElement("turnsValue") + ":" + getGuiElement("makesUTurnValue") + ":" + getGuiElement("limitToAngle") + ":" + isProbabilitiesOn() + ":" + advancedFilePath_ + ":" + maxSlowSearchTime);
	}
	
	/**
	 * Checks if there is a file named addJobs.txt. If yes it adds this jobs to the queue.
	 */
	public void addNewJobs(){
		//check if the file exists
		File file=new File(System.getProperty("user.dir") + "/addJobs.txt");
		
		//add data to jobs und to job.txt for documentation
		if (file.exists()) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/jobs.txt", true));
				BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/addJobs.txt"));        
				String line = "";
				while ((line = reader.readLine()) != null) {
					out.write( "\n" + line);
					jobs.add(line);
				}
				out.close();
				reader.close();
			} catch(Exception e) {
				System.out.println("Error while adding new job");
				e.printStackTrace();
			}
			//now delete the addJobs.txt file
			file.delete();			
		}
	}
	
	/**
	 * reads the jobs in jobs.txt
	 */
	public void readJobs(){
		String line = "";
		jobs = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/jobs.txt"));        

			while ((line = reader.readLine()) != null) jobs.add(line);
			reader.close();
		} catch(Exception e) {
			System.out.println("Error while reading job file");
			e.printStackTrace();
		}
	}
	
	/** 
	 * saves jobs to file
	 */
	public void saveJobsToFile(){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/jobs.txt"));
			if(jobs != null){
				for(String j:jobs)out.write(j + "\n");
				updateInformationArea("\n" + Messages.getString("LogAnalyser.savedJobs") + System.getProperty("user.dir") + "/jobs.txt" + "\n");
			}
			out.close();
		}
		catch (IOException e)
		{
			System.out.println("Error while saving job file");		
		}
	}
	
	/** 
	 * saves jobs to jobs_done.txt file for documentation
	 */
	public void documentJobsToFile(String job){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/jobs_done.txt", true));
			if(job != null){
				out.write(job + "\n");
			}
			out.close();
		}
		catch (IOException e)
		{
			System.out.println("Error while documenting job");		
		}
	}
	
	
	/**
	 * Updates status text area
	 */
	public void updateInformationArea(int numberOfCarsChecked, int numberOfCorrectVehiclesFound, boolean finished){
		if(guiMode){
			String finishedMsg = "";
			if(finished) finishedMsg = "\n" + Messages.getString("LogAnalyserDialog.done");
			
			double percentage = 0;
			
			if(numberOfCarsChecked != 0) percentage =  (double)(numberOfCorrectVehiclesFound*100)/numberOfCarsChecked;
			
			logDialog_.getInformationTextArea_().setText(Messages.getString("LogAnalyserDialog.loadingLogFiles") + "\n" + Messages.getString("LogAnalyserDialog.NumberOfCheckedCars") + numberOfCarsChecked + "\n" + Messages.getString("LogAnalyserDialog.NumberOfFoundCars") + numberOfCorrectVehiclesFound + "\n" + Messages.getString("LogAnalyserDialog.PercentageOfFoundCars") + percentage + finishedMsg);		
		}
	}
	
	/**
	 * Updates status text area
	 */
	public void updateInformationArea(int numberOfCarsChecked, int numberOfVehiclesLeftTooSoon, int numberOfCorrectVehiclesFound, boolean finished){
		if(guiMode){
			String finishedMsg = "";
			if(finished) finishedMsg = "\n" + Messages.getString("LogAnalyserDialog.done");
			
			double percentage = 0;
			
			if(numberOfCarsChecked != 0) percentage =  (double)(numberOfCorrectVehiclesFound*100)/numberOfCarsChecked;
			
			double percentage2 = 0;
			if(numberOfCarsChecked != 0) percentage2 =  (double)((numberOfCorrectVehiclesFound-numberOfVehiclesLeftTooSoon)*100)/(numberOfCarsChecked-numberOfVehiclesLeftTooSoon);
			logDialog_.getInformationTextArea_().setText(Messages.getString("LogAnalyserDialog.loadingLogFiles") + "\n" + Messages.getString("LogAnalyserDialog.NumberOfCheckedCars") + numberOfCarsChecked + "\n" + "Vehicles Leff too soon: " + numberOfVehiclesLeftTooSoon + "\n" + Messages.getString("LogAnalyserDialog.NumberOfFoundCars") + numberOfCorrectVehiclesFound + "\n" + Messages.getString("LogAnalyserDialog.PercentageOfFoundCars") + percentage + "\nPercentage without too soon:" + percentage2 + finishedMsg);		
		}
	}
	
	/**
	 * Updates status text area in GUI and console mode
	 */
	public void updateInformationArea(String text){
		if(guiMode){
			if(text != null){
				logDialog_.getInformationTextArea_().append(text);
			}
		}
		else{
			if(text != null){
				System.out.println(text);
			}
		}
	}
	
	/**
	 * Updates status text area. If true the data will be written to a file
	 */
	public void updateInformationArea(String text, boolean b){
		if(guiMode){
			if(text != null){
				logDialog_.getInformationTextArea_().append(text);
				if(text != null && b)logOutput_ += text;
			}
		}
		else{
			if(text != null){
				System.out.print(text);
				if(text != null && b)logOutput_ += text;
			}
		}
	}
	
	/**
	 * writes the log to file
	 * @param fileName
	 */
	public void writeResultsToFile(String fileName){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(actualJob.substring(0,actualJob.length()-4) + "_" + logName + "_" + fileName + "_analyzed.txt"));
			out.write(logOutput_);
			out.close();
			logOutput_ = "";
		}
		catch (IOException e)
		{
			System.out.println("Error while writing job documentation");
			e.printStackTrace();	
		}
	}
	
	public boolean isFilePathChanged() {
		return filePathChanged;
	}

	public void setFilePathChanged(boolean filePathChanged) {
		this.filePathChanged = filePathChanged;
	}

	public ArrayList<String> getJobs() {
		return jobs;
	}

	public void setJobs(ArrayList<String> jobs) {
		this.jobs = jobs;
	}

	public String getLogOutput_() {
		return logOutput_;
	}

	public void setLogOutput_(String logOutput_) {
		this.logOutput_ = logOutput_;
	}

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getAttackType() {
		return attackType;
	}

	public void setAttackType(String attackType) {
		this.attackType = attackType;
	}

	public LogOperations getOperation_() {
		return operation_;
	}

	public void setOperation_(LogOperations operation_) {
		this.operation_ = operation_;
	}

	public String getActualJob() {
		return actualJob;
	}

	public void setActualJob(String actualJob) {
		this.actualJob = actualJob;
	}

	public boolean isAdvancedFilePathChanged() {
		return advancedFilePathChanged;
	}

	public void setAdvancedFilePathChanged(boolean advancedFilePathChanged) {
		this.advancedFilePathChanged = advancedFilePathChanged;
	}

	public String getAdvancedFilePath_() {
		return advancedFilePath_;
	}

	public void setAdvancedFilePath_(String advancedFilePath_) {
		this.advancedFilePath_ = advancedFilePath_;
	}

	public String getPlainJob() {
		return plainJob;
	}

	public void setPlainJob(String plainJob) {
		this.plainJob = plainJob;
	}

	public double getTimeBufferValue() {
		return timeBufferValue;
	}

	public void setTimeBufferValue(double timeBufferValue) {
		this.timeBufferValue = timeBufferValue;
	}

	public double getTuneTimeValue() {
		return tuneTimeValue;
	}

	public void setTuneTimeValue(double tuneTimeValue) {
		this.tuneTimeValue = tuneTimeValue;
	}

	public double getBiggerStreetValue() {
		return biggerStreetValue;
	}

	public void setBiggerStreetValue(double biggerStreetValue) {
		this.biggerStreetValue = biggerStreetValue;
	}

	public double getSmallerStreetValue() {
		return smallerStreetValue;
	}

	public void setSmallerStreetValue(double smallerStreetValue) {
		this.smallerStreetValue = smallerStreetValue;
	}

	public double getDrivesStraigthValue() {
		return drivesStraigthValue;
	}

	public void setDrivesStraigthValue(double drivesStraigthValue) {
		this.drivesStraigthValue = drivesStraigthValue;
	}

	public double getTurnsValue() {
		return turnsValue;
	}

	public void setTurnsValue(double turnsValue) {
		this.turnsValue = turnsValue;
	}

	public double getMakesUTurnValue() {
		return makesUTurnValue;
	}

	public void setMakesUTurnValue(double makesUTurnValue) {
		this.makesUTurnValue = makesUTurnValue;
	}

	public double getLimitToAngle() {
		return limitToAngle;
	}

	public void setLimitToAngle(double limitToAngle) {
		this.limitToAngle = limitToAngle;
	}

	public boolean isUseProbability() {
		return useProbability;
	}

	public void setUseProbability(boolean useProbability) {
		this.useProbability = useProbability;
	}

	/**
	 * @return the maxSlowSearchTime
	 */
	public double getMaxSlowSearchTime() {
		return maxSlowSearchTime;
	}

	/**
	 * @param maxSlowSearchTime the maxSlowSearchTime to set
	 */
	public void setMaxSlowSearchTime(double maxSlowSearchTime) {
		this.maxSlowSearchTime = maxSlowSearchTime;
	}
}
