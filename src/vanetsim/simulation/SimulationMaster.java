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
package vanetsim.simulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.gui.controlpanels.SlowPanel;
import vanetsim.gui.helpers.GeneralLogWriter;
import vanetsim.gui.helpers.IDSLogWriter;
import vanetsim.gui.helpers.PrivacyLogWriter;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Region;
import vanetsim.scenario.IDSProcessor;
import vanetsim.scenario.IDSProcessorList;
import vanetsim.scenario.KnownEventSourcesList;
import vanetsim.scenario.KnownVehicle;
import vanetsim.scenario.KnownVehiclesList;
import vanetsim.scenario.KnownRSUsList;
import vanetsim.scenario.Scenario;
import vanetsim.scenario.Vehicle;
import vanetsim.scenario.events.EventList;
import vanetsim.scenario.events.EventSpotList;

/**
 * This thread delegates the simulation processing to subthreads and then calls a
 * repaint on the drawing area.
 */
public final class SimulationMaster extends Thread{

	/** How much time passes in one step (in milliseconds). 40ms results in a smooth animation with 25fps. */
	public static final int TIME_PER_STEP = 40;
	
	/** The list with all events */
	private static final EventList eventList_ = EventList.getInstance();
	
	/** Indicates if this simulation should run. If this flag is updated to false the current simulation step 
	 * is finished and afterwards the simulation stops */
	private volatile boolean running_ = false;

	/** The do one step. */
	private volatile boolean doOneStep_ = false;
	
	/** The time, one step should have in realtime. Decrease to get a faster simulation, increase to get a slower simulation. */
	private volatile int targetStepTime_ = TIME_PER_STEP;

	/** If the mode to jump to a specific time is enabled or not. */
	private volatile boolean jumpTimeMode_ = false;
	
	/** A target time to jump to */
	private volatile int jumpTimeTarget_ = -1;

	/** An array holding all worker threads. */
	private WorkerThread[] workers_ = null;

	/** Synchronization barrier for the start of the working threads. */
	private CyclicBarrier barrierStart_ = null;
	
	/** Synchronization barrier for the worker threads. */
	private CyclicBarrier barrierDuringWork_ = null;

	/** Synchronization barrier for the end of one step in the working process. */
	private CyclicBarrier barrierFinish_ = null;
	
	/** GUI disabled or enabled */
	private boolean guiEnabled = true;
	
	/** Flag to log silent period header once */
	private boolean logSilentPeriodHeader_ = true;
	
	/** the timer for the event spots */
	private static int eventSpotCountdown_ = -1;
	
	/** a flag to activate the general log */
	private boolean generalLogWriterActivated_ = true;
	
	/** the header of the general log writer */
	private String generalLogWriterHeader_ = "timestamp:x:y:v:id";

	
	/**
	 * Instantiates a new simulation master.
	 */
	public SimulationMaster(){
	}

	/**
	 * Method to let this thread start delegating work to subthreads. Work in the main function is resumed, the
	 * subthreads (workers) will wake up again and the Renderer is notified to get active again.
	 */  
	public synchronized void startThread(){
		// write silent period log header
		if(Vehicle.isSlowOn()) SlowPanel.writeSlowHeader();
		
		if(Vehicle.isSilentPeriodsOn() && logSilentPeriodHeader_) {
			logSilentPeriodHeader_ = false;
			PrivacyLogWriter.log("Silent Period:Duration:" + Vehicle.getTIME_OF_SILENT_PERIODS() + ":Frequency:" + Vehicle.getTIME_BETWEEN_SILENT_PERIODS());
		}
		
		
		
		Renderer.getInstance().notifySimulationRunning(true);
		ErrorLog.log(Messages.getString("SimulationMaster.simulationStarted"), 2, SimulationMaster.class.getName(), "startThread", null); //$NON-NLS-1$ //$NON-NLS-2$
		Renderer.getInstance().ReRender(true, false);
		running_ = true;		
	}

	/**
	 * Method to let this thread stop delegating work to subthreads. Work in the main function is suspended, the
	 * subthreads (workers) will go to sleep and the Renderer is notified to get inactive.
	 */  
	public synchronized void stopThread(){
		if(running_) ErrorLog.log(Messages.getString("SimulationMaster.simulationStopped"), 2, SimulationMaster.class.getName(), "stopThread", null); //$NON-NLS-1$ //$NON-NLS-2$
		running_ = false;
		if ((Map.getInstance().getReadyState() == false || Scenario.getInstance().getReadyState() == false) && workers_ != null){
			//wait till all workers get to the start barrier
			while(barrierStart_.getParties() - barrierStart_.getNumberWaiting() != 1){
				try{
					sleep(1);
				} catch (Exception e){}
			}
			//now interrupt the first one. The first will exit with an InterruptedException, all other workers will exit through a BrokenBarrierException
			workers_[0].interrupt();
		
			workers_ = null;
		}		
		Renderer.getInstance().notifySimulationRunning(false);
	}
	
	/**
	 * Allows to jump to a specific time. While this mode is active, no display and statistics update 
	 * is done.
	 * 
	 * @param time	the target time in milliseconds
	 */
	public void jumpToTime(int time){
		jumpTimeMode_ = true;
		jumpTimeTarget_ = time;
		if(!Renderer.getInstance().isConsoleStart())VanetSimStart.setProgressBar(true);
		startThread();
	}
	
	/**
	 * Sets the target step time. Decrease to get a faster simulation, increase to get a slower.
	 * 
	 * @param time
	 */
	public void setTargetStepTime(int time){
		if(time > 0) targetStepTime_ = time;
	}

	/**
	 * Proceed one single step forward.
	 */
	public void doOneStep(){
		if(!running_){
			Renderer.getInstance().notifySimulationRunning(true);
			doOneStep_ = true;
		}
	}

	/**
	 * Function to set up the worker threads with their corresponding regions. Each thread gets an equal amount of regions
	 * (some might get one more because of rounding). The amount of items in a region is not used as a method to improve equality
	 * between threads as this value might change over time (moving vehicles!).
	 * This function expects to get 2x the amount of CPU cores as threads so that the negative effects of an unequal allocation
	 * (some threads finishing faster as others => unused cpu power) are reduced.
	 * 
	 * @param timePerStep	the time per step in milliseconds
	 * @param threads		the amount of threads that shall be created
	 * 
	 * @return the worker thread array
	 */
	public WorkerThread[] createWorkers(int timePerStep, int threads){
		ArrayList<WorkerThread> tmpWorkers = new ArrayList<WorkerThread>();
		WorkerThread tmpWorker = null;
		Region[][] regions = Map.getInstance().getRegions();
		ArrayList<Region> tmpRegions = new ArrayList<Region>();
		long regionCountX = Map.getInstance().getRegionCountX();
		long regionCountY = Map.getInstance().getRegionCountY();		
		double regionsPerThread = regionCountX * regionCountY / (double)threads;		
		long count = 0;
		double target = regionsPerThread;
		threads = 0;	// reset to 0, perhaps we're getting more/less because of rounding so we calculate this later!

		for(int i = 0; i < regionCountX; ++i){
			for(int j = 0; j < regionCountY; ++j){
				++count;
				tmpRegions.add(regions[i][j]);
				if(count >= Math.round(target)){					
					try{
						tmpWorker = new WorkerThread(tmpRegions.toArray(new Region[0]), timePerStep);			
						++threads;
						tmpWorkers.add(tmpWorker);
						tmpWorker.start();
					} catch (Exception e){
						ErrorLog.log(Messages.getString("SimulationMaster.errorWorkerThread"), 7, SimulationMaster.class.getName(), "createWorkers", e); //$NON-NLS-1$ //$NON-NLS-2$
					}	
					tmpRegions = new ArrayList<Region>();
					target += regionsPerThread;
				}
			}
		}
		if(tmpRegions.size() > 0){	// remaining items, normally this should never happen!
			ErrorLog.log(Messages.getString("SimulationMaster.regionsRemained"), 6, SimulationMaster.class.getName(), "createWorkers", null); //$NON-NLS-1$ //$NON-NLS-2$
			try{
				tmpWorker = new WorkerThread(tmpRegions.toArray(new Region[0]), timePerStep);			
				++threads;
				tmpWorkers.add(tmpWorker);
				tmpWorker.start();
			} catch (Exception e){
				ErrorLog.log(Messages.getString("SimulationMaster.errorAddingRemainingRegions"), 7, SimulationMaster.class.getName(), "createWorkers", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		barrierStart_ = new CyclicBarrier(threads + 1);
		barrierDuringWork_ = new CyclicBarrier(threads);
		barrierFinish_ = new CyclicBarrier(threads + 1);
		Iterator<WorkerThread> iterator = tmpWorkers.iterator();
		while(iterator.hasNext() ) { 
			iterator.next().setBarriers(barrierStart_, barrierDuringWork_, barrierFinish_);
		}
		return tmpWorkers.toArray(new WorkerThread[0]);
	}


	/**
	 * The main method for the simulation master initializes the worker threads, manages them and 
	 * initiates the render process and statistics updates.
	 */
	public void run() {
		setName("SimulationMaster"); //$NON-NLS-1$
		int time, threads;
		long renderTime;
		Renderer renderer = Renderer.getInstance();
		CyclicBarrier barrierRender = new CyclicBarrier(2);
		renderer.setBarrierForSimulationMaster(barrierRender);
		ReportingControlPanel statsPanel = null;
		if(!Renderer.getInstance().isConsoleStart()) statsPanel = VanetSimStart.getMainControlPanel().getReportingPanel();
		long timeOld = 0;
		long timeNew = 0;
		long timeDistance = 0;
		boolean consoleStart = Renderer.getInstance().isConsoleStart();
		KnownVehiclesList.setTimePerStep_(TIME_PER_STEP);
		if(generalLogWriterActivated_){
			GeneralLogWriter.setLogPath(System.getProperty("user.dir"));
			GeneralLogWriter.log(generalLogWriterHeader_);
		}
	
		while(true){
			try{
				if(running_ || doOneStep_){
					renderTime = System.nanoTime();
					barrierRender.reset();
					
					while(workers_ == null){
						if (Map.getInstance().getReadyState() == true && Scenario.getInstance().getReadyState() == true){	// wait until map is ready
							if(Runtime.getRuntime().availableProcessors() < 2) threads = 1;	// on single processor systems or if system reports wrong (smaller 1) amount of CPUs => fallback to 1 CPU and 1 thread
							else threads = Runtime.getRuntime().availableProcessors() * 2;		// on multiprocessor systems use double the amount of threads to use ressources more efficiently
							long max_heap = Runtime.getRuntime().maxMemory()/1048576;		// Heap memory in MB
							ErrorLog.log(Messages.getString("SimulationMaster.preparingSimulation") + threads + Messages.getString("SimulationMaster.threadsDetected") + max_heap + Messages.getString("SimulationMaster.heapMemory"), 3, SimulationMaster.class.getName(), "run", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							// Prepare multiple worker threads to gain advantage of multi-core processors
							workers_ = createWorkers(TIME_PER_STEP, threads);		
						} else {
							sleep(50);
						}
					}					
					time = renderer.getTimePassed() + TIME_PER_STEP;

					//process events
					eventList_.processEvents(time);	

					// (re)start the working threads
					barrierStart_.await();

					// wait for all working threads to finish to prevent drawing an inconsistent state!
					barrierFinish_.await();	

					// Rendering itself can't be multithreaded and thus must be done here and not in the workers!
					KnownVehiclesList.setTimePassed(time);
					IDSProcessorList.setTimePassed(time);
					KnownRSUsList.setTimePassed(time);
					renderer.setTimePassed(time);		
					KnownEventSourcesList.setTimePassed(time);
					if(eventSpotCountdown_ < time) eventSpotCountdown_ = EventSpotList.getInstance().doStep(time);
									
					if(!jumpTimeMode_){
						
						renderer.ReRender(false, true);
	
						statsPanel.checkUpdates(TIME_PER_STEP);
						// wait until rendering has completed
						Thread.yield();
						barrierRender.await(3, TimeUnit.SECONDS);
	
						// wait so that we get near the desired frames per second (no waiting if processing power wasn't enough!)
						renderTime = ((System.nanoTime() - renderTime)/1000000);
						if(renderTime > 0) renderTime = targetStepTime_ - renderTime;
						else renderTime = targetStepTime_ + renderTime;	//nanoTime might overflow
						if(renderTime > 0 && renderTime <= targetStepTime_){
							sleep(renderTime);
						}
					} else {
						if(consoleStart && time%5000 == 0){
							timeNew = System.currentTimeMillis();
							timeDistance = timeNew-timeOld;
							System.out.println("Time:::" + timeDistance);
							timeOld = timeNew;
							System.out.println(time);

						}
						if(time >= jumpTimeTarget_){
							jumpTimeTarget_ = -1;
							jumpTimeMode_ = false;
							stopThread();
							if(consoleStart){
								System.out.println("Time:" + new Date());
								System.out.println(Messages.getString("ConsoleStart.SimulationEnded"));
								if(IDSProcessor.isLogIDS_()){
									IDSLogWriter.log("**********************************");
									IDSLogWriter.log(IDSProcessor.getReport());
								}
								System.out.println("\n" + IDSProcessor.getReport());
									

								System.exit(0);
							}
							if(!consoleStart){
								VanetSimStart.setProgressBar(false);
								renderer.ReRender(false, true);	
								statsPanel.checkUpdates(TIME_PER_STEP);
							}
						}
					}
					if(doOneStep_){
						doOneStep_ = false;
						renderer.notifySimulationRunning(false);
					}
				} else {
					sleep(50);
				}
			} catch (Exception e){};
		}
	}

	/**
	 * Returns if a simulation is currently running or not.
	 * 
	 * @return <code>true</code> if a simulation is running, else <code>false</code>
	 */
	public boolean isSimulationRunning(){
		return running_;
	}

	public boolean isGuiEnabled() {
		return guiEnabled;
	}

	public void setGuiEnabled(boolean guiEnabled) {
		this.guiEnabled = guiEnabled;
	}

	/**
	 * @return the eventSpotCountdown_
	 */
	public static  int getEventSpotCountdown_() {
		return eventSpotCountdown_;
	}

	/**
	 * @param eventSpotCountdown_ the eventSpotCountdown_ to set
	 */
	public static void setEventSpotCountdown_(int eventSpotCountdown) {
		eventSpotCountdown_ = eventSpotCountdown;
	}
	
	/**
	 * Writes any data to any filepath (used to save data after simulation end)
	 */
	public void writeToFile(String text, String filePath, String fileName){
		
		
		System.out.println("writing file...");
		System.out.println(filePath + "/" + System.currentTimeMillis() + "_" + fileName);
		try {
			
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath + "/" + System.currentTimeMillis() + "_" + fileName));
			if(text != null){
				out.write(text);
				out.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();		
		}
	}
	
	/**
	 * measures vehicle fluctuation
	 */
	public void vehicleFluctuation() throws Exception{
		Region[][] Regions = Map.getInstance().getRegions();
		int Region_max_x = Map.getInstance().getRegionCountX();
		int Region_max_y = Map.getInstance().getRegionCountY();
		int i, j;
		
	
		//get array size
		for(i = 0; i < Region_max_x; ++i){
			for(j = 0; j < Region_max_y; ++j){
				Vehicle[] vehiclesArray = Regions[i][j].getVehicleArray();
				for(int k = 0; k < vehiclesArray.length; ++k){
					Vehicle vehicle = vehiclesArray[k];
					vehicle.getKnownEventSourcesList_().writeOutputFile();
				}		
			}
		}
		
		
		
		//here is the place to call a last method and save some data!
		FileInputStream fstream = new FileInputStream(GeneralLogWriter.getFile_());
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		String line = "";
		int timestamp = 0;
		int tmpTime = 0;
		String[] lineSplit = null;
		int createCounter = 0;
		int updateCounter = 0;
		int fakeMessageInterval = Vehicle.getFakeMessagesInterval_();
		
		//a array to aggregate the vehicles with updates of one event in %
		int[] amountOfSecondContactsInPercent = new int[6];
		
		
		//read all different IDs!
		ArrayList<String> theIDs = new ArrayList<String>();
		int maxUpdatesCounter = 0;
		String[] updateArray;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));        
		try {
			//read vehicle data line by line and check every vehicle
			while ((line = reader.readLine()) != null) {  
			
				if (line.length() > 0){
					//get log data of the vehicle we want to check now
					lineSplit = line.split(":");
					
					if(lineSplit != null && lineSplit.length > 1 && lineSplit[0].substring(0, 3).equals("***")){
						updateArray = lineSplit[1].split("#");
						for(int o = 0; o < updateArray.length; o++){
						
							if(Integer.parseInt(updateArray[o]) > maxUpdatesCounter) maxUpdatesCounter = Integer.parseInt(updateArray[o]);
						}
					}
					else if(lineSplit != null && lineSplit.length > 2){
						if(!theIDs.contains(lineSplit[1])) theIDs.add(lineSplit[1]);
					}
				}
			}
		} catch(IOException e) {
			System.out.println("Error while doing accumulating data");
			e.printStackTrace();
		}
		
		//get the creates and updates per event
		for(String senderID:theIDs){
			fstream = new FileInputStream(GeneralLogWriter.getFile_());
			// Get the object of DataInputStream
			in = new DataInputStream(fstream);
			updateCounter = 0;
			createCounter = 0;
			line = "";
			timestamp = 0;
			tmpTime = 0;
			
			reader = new BufferedReader(new InputStreamReader(in));        
			try {
				//read vehicle data line by line and check every vehicle
				while ((line = reader.readLine()) != null) {  

					if (line.length() > 0){
						//get log data of the vehicle we want to check now
						lineSplit = line.split(":");
						
						//if it is the wrong id skip!
						
						if(!lineSplit[0].substring(0, 3).equals("***") && senderID.equals(lineSplit[1])){
							tmpTime = Integer.parseInt(lineSplit[0]);
							
							if(lineSplit != null && lineSplit.length > 2){
								
								if(tmpTime >= (timestamp + 1000)){	
																		
									//write results in array
									//if vehicles near by are 0 skip:
									if(createCounter > 0 || updateCounter > 0){
										
										double percentage = (double)updateCounter/(double)(createCounter+updateCounter);
										if(percentage == 0){
											amountOfSecondContactsInPercent[0]++;
										}
										else if(percentage <= 0.2){
											amountOfSecondContactsInPercent[1]++;
										}
										else if(percentage <= 0.4){
											amountOfSecondContactsInPercent[2]++;
										}
										else if(percentage <= 0.6){
											amountOfSecondContactsInPercent[3]++;
										}
										else if(percentage <= 0.8){
											amountOfSecondContactsInPercent[4]++;
										}
										else if(percentage <= 1){
											amountOfSecondContactsInPercent[5]++;
										}
										else System.out.println("error");
									}
									
									updateCounter = 0;
									createCounter = 0;
									while(tmpTime >= (timestamp + 1000 + fakeMessageInterval)){
									
										timestamp += (fakeMessageInterval + 40);
									}
									timestamp = tmpTime;
									
									
									
								}
								
								if(lineSplit[2].equals("update")) updateCounter++;
								else if(lineSplit[2].equals("create")) createCounter++;
							}
						}
						
					}
					
				}
			} catch(IOException e) {
				System.out.println("Error while doing accumulating data");
				e.printStackTrace();
			}
			
		}
		
		
		
		
		//percentages to output string
		int eventsWithoutZeroTotal = 0;
		
		for(int g = 0; g < amountOfSecondContactsInPercent.length; g++) eventsWithoutZeroTotal += amountOfSecondContactsInPercent[g];
		
		String percentageStats = "";
		String normalStats = "";
		for(int h = 0; h < amountOfSecondContactsInPercent.length; h++) {
			
			percentageStats += (double)amountOfSecondContactsInPercent[h]/eventsWithoutZeroTotal*100 + "\n";
			normalStats += amountOfSecondContactsInPercent[h] + "\n";
		}
	
		
		//here is the place to call a last method and save some data!
		fstream = new FileInputStream(GeneralLogWriter.getFile_());
		// Get the object of DataInputStream
		in = new DataInputStream(fstream);
		line = "";
		lineSplit = null;

		int creates = 0;
		int updates = 0;
		int[] updatesArray = new int[maxUpdatesCounter + 1];
		
		reader = new BufferedReader(new InputStreamReader(in));        
		try {
			//read vehicle data line by line and check every vehicle
			while ((line = reader.readLine()) != null) {  
			
				if (line.length() > 0){
					//get log data of the vehicle we want to check now
					lineSplit = line.split(":");
					
					if(lineSplit != null && lineSplit.length > 1 && lineSplit[0].substring(0, 3).equals("***")){
						updateArray = lineSplit[1].split("#");
						
						for(int o = 0; o < updateArray.length; o++){
							
							creates++;
							
							updates += Integer.parseInt(updateArray[o]);
							updatesArray[Integer.parseInt(updateArray[o])]++;
						}
					}
					
				}
			}
		} catch(IOException e) {
			System.out.println("Error while doing accumulating data");
			e.printStackTrace();
		}
		

		int tmpCreates = creates;
		String stats = creates + "\n";
		for(int l = 0; l < updatesArray.length; l++){
			tmpCreates -=  updatesArray[l];
			stats += tmpCreates + "\n";
		}
		
		String statsInPercent = "1\n";
		tmpCreates = creates;
		for(int l = 0; l < updatesArray.length; l++){
			tmpCreates -=  updatesArray[l];
			statsInPercent += ((double)tmpCreates/creates)*100 + "\n";
		}

		double[] knownVehicleData = returnAverageKnownVehiclesAndTimes();
		if(Scenario.getInstance().getScenarioName().equals(""))writeToFile("\n\nAverage known vehicles:" + knownVehicleData[0] + "\nAverage known time:" + knownVehicleData[1] + "\nVehiclesWithContact:" + creates + ":EventsSeen:" + (updates + creates) + ":UpdatesTotal:" + updates + ":AverageEventsPerLoggedVehicle:" + ((double)(updates + creates)/(double)creates) + "\n\nUpdateStats:\n" + stats   + "\n\nUpdateStatsInPercent:\n" + statsInPercent + "\n\nPercentageStats:\n" + percentageStats + "\n\n\nNormalStats:\n" + normalStats, System.getProperty("user.dir"), "neighbordata.txt");
		else writeToFile("\n\nAverage known vehicles:" + knownVehicleData[0] + "\nAverage known time:" + knownVehicleData[1] + "\nVehiclesWithContact:" + creates + ":EventsSeen:" + (updates + creates) + ":UpdatesTotal:" + updates + ":AverageEventsPerLoggedVehicle:" + ((double)(updates + creates)/(double)creates) + "\n\nUpdateStats:\n" + stats   + "\n\nUpdateStatsInPercent:\n" + statsInPercent   + "\n\nPercentageStats:\n" + percentageStats + "\n\n\nNormalStats:\n" + normalStats, System.getProperty("user.dir"), Scenario.getInstance().getScenarioName() + ".txt");
		
	}
	
	public void createAndSaveSpamData(){
		int spamAmount = 0;
		int fakeMessageCounter = 0;

		Region[][] Regions = Map.getInstance().getRegions();
		int Region_max_x = Map.getInstance().getRegionCountX();
		int Region_max_y = Map.getInstance().getRegionCountY();
		int i, j;
		
		//get array size
		for(i = 0; i < Region_max_x; ++i){
			for(j = 0; j < Region_max_y; ++j){
				Vehicle[] vehiclesArray = Regions[i][j].getVehicleArray();
				for(int k = 0; k < vehiclesArray.length; ++k){
					Vehicle vehicle = vehiclesArray[k];
					vehicle.getKnownEventSourcesList_().clear();
					spamAmount += vehicle.getKnownEventSourcesList_().getSpamCount();
					fakeMessageCounter += vehicle.getFakeMessagesCreated_();
				}		
			}
		}
		
		
		try {
			writeToFile("Fake Messages:" + fakeMessageCounter + ":SpamDectected:" + spamAmount, System.getProperty("user.dir"), "_spammerData_" + Scenario.getInstance().getScenarioName().substring(0, (Scenario.getInstance().getScenarioName().length() - 4)) + ".txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	

	/**
	 * measures vehicle fluctuation
	 */
	public int spam() throws Exception{
		
		//here is the place to call a last method and save some data!
		FileInputStream fstream = new FileInputStream(GeneralLogWriter.getFile_());
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		

		int updatesCounter = 0;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));        
		try {
			//read vehicle data line by line and check every vehicle
			while (reader.readLine() != null) {  
				updatesCounter++;			
			}
		} catch(IOException e) {
			System.out.println("Error while doing accumulating data");
			e.printStackTrace();
		}
		reader.close();
		return updatesCounter;
	}
	

	/**
	 * writes the average knwon vehicles time
	 */
	public void writeAverageKnownVehiclesTime() throws Exception{
		
		//here is the place to call a last method and save some data!
		FileInputStream fstream = new FileInputStream(GeneralLogWriter.getFile_());
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		
		String line = "";

		double accumulatedTime = 0;
		double knownVehiclesCounter = 0;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));        
		try {
			//read vehicle data line by line and check every vehicle
			while ((line = reader.readLine()) != null) {  
				
				if(line != null && !line.equals("")){
					knownVehiclesCounter++;
					accumulatedTime += Long.parseLong(line);
				}
			}
		} catch(IOException e) {
			System.out.println("Error while doing accumulating data");
			e.printStackTrace();
		}
		
		try {
			writeToFile(String.valueOf(accumulatedTime/knownVehiclesCounter), System.getProperty("user.dir"), "_knownVehiclesTimeData_" + Scenario.getInstance().getScenarioName().substring(0, (Scenario.getInstance().getScenarioName().length() - 4)) + ".txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public double[] returnAverageKnownVehiclesAndTimes(){		
		double[] returnArray = new double[2];
		Region[][] regions = Map.getInstance().getRegions();
		Vehicle[] vehicles;
		Vehicle vehicle;
		int i, j, k;
	
		double knownTimeTotal = 0;
		double counter = 0;
		double knownVehiclesCounter = 0;
		double knownVehiclesTotal = 0;

		for(i = 0; i < regions.length; ++i){
			for(j = 0; j < regions[i].length; ++j){
				vehicles = regions[i][j].getVehicleArray();
				for(k = 0; k < vehicles.length; ++k){
					vehicle = vehicles[k];
					
					if(vehicle.isActive() && vehicle.isWiFiEnabled()){
						++counter;
						
						knownVehiclesTotal += vehicle.getKnownVehiclesList().getSize();
						
						KnownVehicle next;
						for(int l = 0; l < KnownVehiclesList.getHashSize(); ++l){
							next = vehicle.getKnownVehiclesList().getFirstKnownVehicle()[l];
							while(next != null){
								knownVehiclesCounter++;
								knownTimeTotal +=  (next.getLastUpdate() - next.getFirstContact_());
								next = next.getNext();
							}
						}		
					}
				}
			}
		}
		
		returnArray[0] = (knownVehiclesTotal/counter);
		returnArray[1] = (knownTimeTotal/knownVehiclesCounter);
		return returnArray;
	}
}