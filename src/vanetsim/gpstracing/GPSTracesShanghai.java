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
package vanetsim.gpstracing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import vanetsim.map.Map;

public class GPSTracesShanghai {

	/** The ArrayList types collects all GPSDATA */
	public ArrayList<String> shTraces_;

	private List<long[]> traceInfo_  = new ArrayList<long[]>();

	/** The only instance of this class (singleton). */
	private static final GPSTracesShanghai INSTANCE = new GPSTracesShanghai();

	public static GPSTracesShanghai getInstance() {
		return INSTANCE;
	}

	/**
	 * Constructor
	 */
	public GPSTracesShanghai() {
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
				"../VanetSim/GPX_Data/traceInfoFileShang.txt");
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
			File f = new File("../VanetSim/GPX_Data/Shanghai_Traces/");
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
							"../VanetSim/GPX_Data/traceInfoFileShang.txt",
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

	// Function parses Trace File for Shanghai
	// Returns ArrayList
	public ArrayList<String> getShanghaiTraces(int minLine, int maxLine) {

		shTraces_ = new ArrayList<String>();

		File ShanghaiFile_ = new File(
				"../VanetSim/GPX_Data/Shanghai_Traces/Shanghai_Taxi_traces.txt");
		BufferedReader br = null;
		String sCurrentLine = null;
		try {
			br = new BufferedReader(new FileReader(ShanghaiFile_));
			int Counter = 0;
			//read file until line is null
			while ((sCurrentLine = br.readLine()) != null) {
				//read inbetween min/max Line area
				if ((minLine <= Counter) && (maxLine >= Counter)) {
					//CSV File so split at ,
					String[] columns = sCurrentLine.split(",");

					// String ID = columns[0];
					String TaxiID = columns[1];
					String Lon = columns[2];
					String Lat = columns[3];
					// String Speed = columns[4];
					// String Angle = columns[5];
					String Time = columns[6];
					// String Status = columns[7];
					// String EStatus = columns[8];
					// String Reversed = columns[9];
					
					// Add to Array List
					shTraces_.add(TaxiID);
					shTraces_.add(Lon);
					shTraces_.add(Lat);
					shTraces_.add(Time);
				}
				// Counter for maxLine
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
		// Return Array List
		return shTraces_;
	}
}
