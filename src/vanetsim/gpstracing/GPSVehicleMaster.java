package vanetsim.gpstracing;


public class GPSVehicleMaster {
	
	private static GPSVehicleMaster instance_;
	
	private SortedVehicleQueue vehicles_;
	
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
}
