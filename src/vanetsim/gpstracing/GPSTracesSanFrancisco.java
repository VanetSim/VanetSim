package vanetsim.gpstracing;

import java.util.ArrayList;

import vanetsim.gui.helpers.VehicleType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class GPSTracesSanFrancisco {

	
	/** The ArrayList types collects all GPSDATA*/
	public ArrayList<String> sfTraces_;
	
	/** The last Line the Parser reads */
	private int maxLine_;
	
	/** The first Line the Parser reads */
	private int minLine_;
	
	/** The only instance of this class (singleton). */
	private static final GPSTracesSanFrancisco INSTANCE = new GPSTracesSanFrancisco();
	
	/**Instance for San Francisco Traces. */
	public static GPSTracesSanFrancisco getInstance(){
		return INSTANCE;
	}
	

	public ArrayList<String> getSanFranciscoTraces(int minLine_, int maxLine_){
		 sfTraces_ = new ArrayList<String>();
		 
		 System.out.println("Hier bin ich. ich bin die 4");
		 
		 //Lese alle Dateien im San Francisco Verzeichnis ein
		 File f = new File("../VanetSim/GPX_Data/SanFrancisco_Traces");
		 File[] fileArray = f.listFiles();
		 
		 if (fileArray != null) { 
			    for (int i = 0; i < fileArray.length; i++) {
			    	File actualFile_ = fileArray[i];
			    	 //File actualFile_ = new File(fileArray[i])
			      System.out.print(fileArray[i].getAbsolutePath());
			      UUID TaxiID = UUID.randomUUID();
			      
			      BufferedReader br = null;
			        String sCurrentLine = null;
			        try
			        {
			          br = new BufferedReader(
			          new FileReader(actualFile_));
			            while ((sCurrentLine = br.readLine()) != null){
			            	//Parse here 
			            	String[] columns = sCurrentLine.split(" ");
			            	//Structure of Files latitude, longitude, occupancy, time
			            	String latitude = columns[0]; 
			            	String longitude = columns[1]; 
			            	// String occupancy = columns[2]; 
			            	String time = columns[3]; 
		                 
		                 //Convert String time to Long
		                long long_time = Long.parseLong(time);
		                Date date = new Date(long_time*1000L); // *1000 is to convert seconds to milliseconds
		                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
		                 sdf.setTimeZone(TimeZone.getTimeZone("GMT-7"));
		                 String formattedDate = sdf.format(date);
		                 //System.out.println(formattedDate);
                 
		                //Add to Array List 
		                 
		                 System.out.println("Taxi ID" + TaxiID);
		                 System.out.println("Lon" + longitude);
		                 System.out.println("Lat" + latitude);
		                 System.out.println("Date" + formattedDate);
		                 
		                 
		                 sfTraces_.add(TaxiID.toString());
		                 sfTraces_.add(latitude);
		                 sfTraces_.add(longitude);
		                 sfTraces_.add(formattedDate);
		                 

			                //System.out.println(sCurrentLine);
			            }
			        }
			        catch (IOException e)
			        {
			            e.printStackTrace();
			        }
			        finally
			        {
			            try
			            {
			                if (br != null)
			                br.close();
			            } catch (IOException ex)
			            {
			                ex.printStackTrace();
			            }
			        }	 	 
			      
			      
			     // if (fileArray[i].isDirectory()) {		    	  
			     //   System.out.print(" (Ordner)\n");
			    //  }
			 //     else {
			   //     System.out.print(" (Datei)\n");
			   //   }
			    }
			  }
			  
			  

		 
		 return sfTraces_;
		 
	}
	
	
	
}
