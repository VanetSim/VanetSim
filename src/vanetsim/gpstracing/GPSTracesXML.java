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

//TODO: Fix Bug, Test and upload - Change to read all files ?

/**
 * 
 * This Class is to import GPX Files and parse them to enter GPS Traces to the System.
 *
 */
public class GPSTracesXML {
	
	private static final SimpleDateFormat gpxDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	

	/** The only instance of this class (singleton). */
	private static final GPSTracesXML INSTANCE = new GPSTracesXML();
	
	
	public static GPSTracesXML getInstance(){
		return INSTANCE;
	}
	
	
	/** The ArrayList types collects all GPX Traces*/
	public ArrayList<String> GPXTraces_;

	// public static void main(String[] a) {
	public ArrayList<String> getGpxTraces(){
		GPXTraces_ = new ArrayList<String>();
		 
		    JFileChooser fileChooser = new JFileChooser(".");
		    fileChooser.setMultiSelectionEnabled(true);

		    fileChooser.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        System.out.println("Action");
		      }
		    });
		    
		    FileNameExtensionFilter filter = new FileNameExtensionFilter("GPX", "gpx");
		    fileChooser.addChoosableFileFilter(filter);
		    fileChooser.setDialogTitle("Open GPX File");

		    int status = fileChooser.showOpenDialog(null);
		   
		    if (status == JFileChooser.APPROVE_OPTION) {
		    	//File selectedFile = fileChooser.getSelectedFile();
		    	
		    	File[] fileArray = fileChooser.getSelectedFiles();


		     		if(fileArray != null){
  
		     			for(int i=0;i<fileArray.length;i++){
		     				System.out.println(fileArray.length);
		     				System.out.println(fileArray[i]);
		     				
		     				//Parse File dsd
		             
		     				File actualFile_ = fileArray[i];
		             
		     				List<Location> points = null;
		     				try{
		     						String time_ = null;
		     					
		     						//Create ID per File
		     						UUID idOne = UUID.randomUUID();
		     						
	
		     						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		     						DocumentBuilder builder = factory.newDocumentBuilder();

		     						FileInputStream fis = new FileInputStream(actualFile_);
		     						org.w3c.dom.Document dom = builder.parse(fis);	
		     						Element root = (Element) dom.getDocumentElement();
		     						NodeList items = root.getElementsByTagName("trkpt");

		     						for(int j = 0; j < items.getLength(); j++){
			     						
		     							Node item = items.item(j);
		     							NamedNodeMap attrs = item.getAttributes();
		     							NodeList props = item.getChildNodes();

		     							String longtitude_;
		     							String latitude_;
		     							
		     							longtitude_ = (attrs.getNamedItem("lat").getTextContent());
		     							latitude_ = (attrs.getNamedItem("lon").getTextContent());
		     							//The element ele is measuring the height in meters - not relevant at the moment
		     							//Float ele_ = (float) Double.parseDouble(attrs.getNamedItem("ele").getTextContent());
		     							//Float speed_ = (float) Double.parseDouble(attrs.getNamedItem("speed").getTextContent());

		     							for(int k = 0; k<props.getLength(); k++){
		     								Node item2 = props.item (k);
		     								String name = item2.getNodeName();
		     								if(!name.equalsIgnoreCase("time")) continue;
		     								time_ = (item2.getFirstChild().getNodeValue());					
		     					
		     	                
		     							}
		     						
		     							System.out.println("UUID One: " + idOne);
		     							System.out.println("lon" + longtitude_);
		     							System.out.println("lat" + latitude_);
		     							System.out.println("TIME " + time_);
		     							//System.out.println(ele_);
		     							//System.out.println("Speed" + speed_);
		     							
		     							
		     							
		     							
		     							//Add elements to ArrayList
		     							     							
		     							GPXTraces_.add(idOne.toString());
		     							GPXTraces_.add(longtitude_);
		     							GPXTraces_.add(latitude_);
		     							GPXTraces_.add(time_);
		     						}
		     						fis.close();
		     					
		     				}catch (Exception e) { 
		     					System.out.println("Uuups this went wrong");
		     					e.printStackTrace(); 
		     				} 
		     			}
	 
		     		}	   
		    }else if (status == JFileChooser.CANCEL_OPTION) {
		    	System.out.println("calceled");
		    }
		    return GPXTraces_;
	 }


	public static SimpleDateFormat getDateFormatter(){
		return (SimpleDateFormat)gpxDate.clone();
		}
	 
		
	}
	

	 
	 