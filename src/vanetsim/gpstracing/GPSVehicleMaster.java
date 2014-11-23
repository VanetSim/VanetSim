package vanetsim.gpstracing;

import vanetsim.map.Map;
import vanetsim.scenario.Vehicle;


public class GPSVehicleMaster {
	
	private static GPSVehicleMaster instance_;
	
	private SortedVehicleQueue vehicles_;
	private boolean simulationIsRunning_ = false;
	private long minimumAtStart_;
	
	/**
	 * 
	 */
	private GPSVehicleMaster(){
		vehicles_ = new SortedVehicleQueue();
	}
	
	/**
	 * 
	 * @return
	 */
	public static GPSVehicleMaster getInstance(){
		
		if(instance_ == null){
			instance_ = new GPSVehicleMaster();
		}
		
		return instance_;
	}
	
	public void addVehicle(Vehicle vehicle, long startTime){
		if (!simulationIsRunning_){
			vehicles_.add(vehicle, startTime);
		}
	}
	
	/**
	 * 
	 */
	public void startSim() {
		simulationIsRunning_ = true;
		minimumAtStart_ = vehicles_.peek().getStartTime();
	}
	
	/**
	 * 
	 * @param simTime
	 */
	public void receiveSimulationTime(long simTime){
		while(vehicles_.peek().getStartTime() < minimumAtStart_ + simTime && !vehicles_.isEmpty()){
			Map.getInstance().addVehicle(vehicles_.remove().getVehicle());
		}
	}
	
	
}
