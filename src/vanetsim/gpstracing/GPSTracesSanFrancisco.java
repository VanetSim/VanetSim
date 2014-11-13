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
import java.util.TimeZone;
import java.util.UUID;

public class GPSTracesSanFrancisco {

	/** The ArrayList types collects all GPSDATA */
	public ArrayList<String> sfTraces_;

	private List<long[]> traceInfo_  = new ArrayList<long[]>();

	/** The only instance of this class (singleton). */
	private static final GPSTracesSanFrancisco INSTANCE = new GPSTracesSanFrancisco();

	/** Instance for San Francisco Traces. */
	public static GPSTracesSanFrancisco getInstance() {
		return INSTANCE;
	}

	/**
	 * Constructor
	 */
	public GPSTracesSanFrancisco() {
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
				"../VanetSim/GPX_Data/traceInfoFileSanFran.txt");
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
			File f = new File("../VanetSim/GPX_Data/SanFrancisco_Traces");
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
							"../VanetSim/GPX_Data/traceInfoFileSanFran.txt",
							"UTF-8");
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

	public ArrayList<String> getSanFranciscoTraces(int minLine, int maxLine) {
		sfTraces_ = new ArrayList<String>();

		int Counter = 0;

		System.out.println("Hier bin ich. ich bin die 4");

		// Lese alle Dateien im San Francisco Verzeichnis ein
		File f = new File("../VanetSim/GPX_Data/SanFrancisco_Traces");
		File[] fileArray = f.listFiles();

		if (fileArray != null) {
			for (int i = 0; i < fileArray.length; i++) {
				File actualFile_ = fileArray[i];
				//System.out.print(fileArray[i].getAbsolutePath());
				UUID TaxiID = UUID.randomUUID();

				BufferedReader br = null;
				String sCurrentLine = null;
				try {
					br = new BufferedReader(new FileReader(actualFile_));
					while ((sCurrentLine = br.readLine()) != null) {

						if ((minLine <= Counter) && (maxLine >= Counter)) {
							// Parse here
							String[] columns = sCurrentLine.split(" ");
							// Structure of Files latitude, longitude,
							// occupancy, time
							String latitude = columns[0];
							String longitude = columns[1];
							// String occupancy = columns[2];
							String time = columns[3];

							// Convert String time to Long
							long long_time = Long.parseLong(time);
							Date date = new Date(long_time * 1000L); // *1000 is
																		// to
																		// convert
																		// seconds
																		// to
																		// milliseconds
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss z"); // the format of
																// your date
							sdf.setTimeZone(TimeZone.getTimeZone("GMT-7"));
							String formattedDate = sdf.format(date);

							// Add to Array List

							System.out.println("Taxi ID" + TaxiID);

							sfTraces_.add(TaxiID.toString());
							sfTraces_.add(longitude);
							sfTraces_.add(latitude);
							sfTraces_.add(formattedDate);
						}
						Counter++;
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

		return sfTraces_;

	}

}
