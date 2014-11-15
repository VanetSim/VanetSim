package vanetsim.gpstracing;

import java.util.ArrayList;

import vanetsim.gui.helpers.VehicleType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class GPSTracesNY {

	/** The ArrayList types collects all GPSDATA */
	public ArrayList<String> nyTraces_;

	private List<long[]> traceInfo_ = new ArrayList<long[]>();

	/** The only instance of this class (singleton). */
	private static final GPSTracesNY INSTANCE = new GPSTracesNY();

	/** Instance for NY Traces. */
	public static GPSTracesNY getInstance() {
		return INSTANCE;
	}

	/**
	 * Constructor
	 */
	public GPSTracesNY() {
		loadTraceInfoFromFile();
	}

	/**
	 * Function for determine the amount of lines within the data set/traces,
	 * which are needed for further calculation and precalculations
	 */
	public void loadTraceInfoFromFile() {

		// If the trace info file exists, it will be read and the information
		// will be stored in traceInfo_
		File traceInfoFile = new File(
				"../VanetSim/GPX_Data/traceInfoFileNY.txt");
		if (traceInfoFile.exists() && !traceInfoFile.isDirectory()) {
			Scanner sc;
			try {
				sc = new Scanner(traceInfoFile);
				while (sc.hasNextLine()) {
					String[] parsedLine = sc.nextLine().split(";");
					long[] parsedNumbers = new long[2];
					parsedNumbers[0] = Long.parseLong(parsedLine[0]);
					parsedNumbers[1] = Long.parseLong(parsedLine[1]);
					traceInfo_.add(parsedNumbers);
				}
				sc.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		// If the trace info file doesn't exist, the information will be parsed
		// from the trace files
		// and will be stored in traceInfo_
		else {
			File f = new File("../VanetSim/GPX_Data/NY_Traces");
			File[] fileArray = f.listFiles();

			if (fileArray != null) {
				long lines = -1;
				for (int i = 0; i < fileArray.length; i++) {
					try {
						Scanner sc = new Scanner(fileArray[i]);
						lines++;
						long[] parsedNumbers = new long[2];
						parsedNumbers[0] = lines;
						while (sc.hasNextLine()) {
							sc.nextLine();
							lines++;
						}
						sc.close();
						parsedNumbers[1] = lines;
						traceInfo_.add(parsedNumbers);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}

				// After parsing the trace info from the trace files, it will be
				// written into a trace file to store
				// the information persistantly.
				try {
					PrintWriter writer = new PrintWriter(
							"../VanetSim/GPX_Data/traceInfoFileNY.txt", "UTF-8");
					for (int i = 0; i < traceInfo_.size(); i++) {
						writer.println(traceInfo_.get(i)[0] + ";"
								+ traceInfo_.get(i)[1]);
					}
					writer.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Getter function for the TraceFileInfo
	 * 
	 * @return trace infos
	 */
	public List<long[]> getTraceFileInfo() {
		return traceInfo_;
	}

	public ArrayList<String> getNYTraces(int minLine, int maxLine) {

		nyTraces_ = new ArrayList<String>();

		int Counter = 0;
		// Parse CSV File
		// List is structured as followed: medallion, hack_licence, vendor_id
		// rate_code, store_and_foward_flag, pickup_datetime, dropoff_datetime,
		// passenger_count, trip_time_in_secs, trip_distance, pickup_longitude,
		// pickup_latitude, dropoff_longitude, dropoff_latitude

		File f = new File("../VanetSim/GPX_Data/NY_Traces");
		File[] fileArray = f.listFiles();

		if (fileArray != null) {
			for (int i = 0; i < fileArray.length; i++) {
				File actualFile_ = fileArray[i];

				BufferedReader br = null;
				String sCurrentLine = null;
				try {
					br = new BufferedReader(new FileReader(actualFile_));

					while (((sCurrentLine = br.readLine()) != null)) {

						if ((minLine <= Counter)
								&& (maxLine >= Counter)) {
							// Parse here
							UUID TaxiID = UUID.randomUUID();
							String[] columns = sCurrentLine.split(",");
							
							//String medallion = columns[0]; // ID
							// String hack_licence = columns[0];
							// String vendor_id = columns[0];
							// String rate_code = columns[0];
							// String store_and_foward_flag = columns[0];
							String pickup_datetime = columns[6];
							String dropoff_datetime = columns[7];
							// String passenger_count = columns[0];
							String trip_time_in_secs = columns[9];
							// String trip_distance = columns[0];
							String pickup_longitude = columns[10];
							String pickup_latitude = columns[11];
							String dropoff_longitude = columns[12];
							String dropoff_latitude = columns[13];

							// Add to Array List

							nyTraces_.add(TaxiID.toString());
							nyTraces_.add(pickup_longitude);
							nyTraces_.add(pickup_latitude);
							nyTraces_.add(pickup_datetime);
							nyTraces_.add(trip_time_in_secs);
							nyTraces_.add(dropoff_longitude);
							nyTraces_.add(dropoff_latitude);
						}

						Counter++;
						if(Counter >= maxLine){
							break;
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (br != null)
							br.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}

			}
		}

		// Return Array List
		return nyTraces_;
	}

}
