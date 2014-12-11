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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.Location;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * 
 * This Class is to import GPX Files and parse them to enter GPS Traces to the System.
 *
 */
public class GPSTracesXML {
	/**Simple Date Format for Time Parsing */
	private static final SimpleDateFormat gpxDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	/** The only instance of this class (singleton). */
	private static final GPSTracesXML INSTANCE = new GPSTracesXML();
	
	/**Return Instance */
	public static GPSTracesXML getInstance(){
		return INSTANCE;
	}
	
	/** The ArrayList types collects all GPX Traces*/
	public ArrayList<String> GPXTraces_;

	// Method Parses GPX Files via a FileChooses so that Random GPX Files can be selected.
	// Data Input must have the ending .GPX
	public ArrayList<String> getGpxTraces(){
		GPXTraces_ = new ArrayList<String>();
		 
			//FileChooser
		    JFileChooser fileChooser = new JFileChooser(".");
		    fileChooser.setMultiSelectionEnabled(true);

		    fileChooser.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        System.out.println("Action");
		      }
		    });
		    //Set endings for gpx, as only gpx format should be parsed
		    FileNameExtensionFilter filter = new FileNameExtensionFilter("GPX", "gpx");
		    fileChooser.addChoosableFileFilter(filter);
		    fileChooser.setDialogTitle("Open GPX File");

		    int status = fileChooser.showOpenDialog(null);
		   
		    if (status == JFileChooser.APPROVE_OPTION) {
		    	//File selectedFile = fileChooser.getSelectedFile();
		    	
		    	File[] fileArray = fileChooser.getSelectedFiles();
		    		//Check if files were selected
		     		if(fileArray != null){
		     			//Read over all selected files
		     			for(int i=0;i<fileArray.length;i++){
		     				File actualFile_ = fileArray[i];
		     				List<Location> points = null;
		     				try{
		     						String time_ = null;
		     					
		     						//Create one UUID per File as one File is one Vehicle
		     						UUID idOne = UUID.randomUUID();
		     						
		     						//Make a new document builder
		     						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		     						DocumentBuilder builder = factory.newDocumentBuilder();
		     						//Set Input stream for current file
		     						FileInputStream fis = new FileInputStream(actualFile_);
		     						org.w3c.dom.Document dom = builder.parse(fis);	
		     						Element root = (Element) dom.getDocumentElement();
		     						//First node is always "trkpt"
		     						NodeList items = root.getElementsByTagName("trkpt");
		     						//go over all nodes in GPX file
		     						for(int j = 0; j < items.getLength(); j++){		
		     							Node item = items.item(j);
		     							NamedNodeMap attrs = item.getAttributes();
		     							NodeList props = item.getChildNodes();

		     							String longtitude_;
		     							String latitude_;
		     							//Select latitude and longtitude
		     							latitude_ = (attrs.getNamedItem("lat").getTextContent());
		     							longtitude_ = (attrs.getNamedItem("lon").getTextContent());
		     							//The element ele is measuring the height in meters - not relevant at the moment
		     							//Float ele_ = (float) Double.parseDouble(attrs.getNamedItem("ele").getTextContent());
		     							//Float speed_ = (float) Double.parseDouble(attrs.getNamedItem("speed").getTextContent());

		     							//get time to waypoint
		     							for(int k = 0; k<props.getLength(); k++){
		     								Node item2 = props.item (k);
		     								String name = item2.getNodeName();
		     								if(!name.equalsIgnoreCase("time")) continue;
		     								time_ = (item2.getFirstChild().getNodeValue());					
		     							}	
		     							
		     							//Add elements to ArrayList   							
		     							GPXTraces_.add(idOne.toString());
		     							GPXTraces_.add(longtitude_);
		     							GPXTraces_.add(latitude_);
		     							GPXTraces_.add(time_);
		     						}
		     						//Close File
		     						fis.close();
		     					
		     				}catch (Exception e) { 
		     					System.out.println("Error while Parsing GPX File");
		     					e.printStackTrace(); 
		     				} 
		     			}
	 
		     		}	   
		    }else if (status == JFileChooser.CANCEL_OPTION) {
		    	//System.out.println("calceled");
		    }
		    //return arrayList
		    return GPXTraces_;
	 }

	public static SimpleDateFormat getDateFormatter(){
		return (SimpleDateFormat)gpxDate.clone();
		}
}
	

	 
	 