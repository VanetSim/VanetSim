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
	
	//private int lontitude_;
	//private int latitude_;
	//private float speed_;
	//private int ele_;
	private static final SimpleDateFormat gpxDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	
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
		    	
		    	File[] sf = fileChooser.getSelectedFiles();
		    	String filelist = "nothing";
		     	String filePath = "";

		     		if(sf.length>0){
		     			filelist = sf[0].getName();
		     			filePath = sf[0].getPath();
		            
		            
		     			for(int i=1;i<sf.length;i++){
		     				filelist += "," + sf[i].getName();
		     				
		     				//Parse File
		             
		     				File file = new File(filePath);
		             
		     				System.out.println(filePath);
		     				List<Location> points = null;
		     				try{
		     					if(file.isFile()){
		     						//Create ID per File
		     						UUID idOne = UUID.randomUUID();
		     						
		     						/*
		     						// Initial setup
		     						FileInputStream inputStream = new FileInputStream(file);
		     						InputStreamReader inputStreamReader = new InputStreamReader(inputStream);			
		     						BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		     						StringBuilder stringBuilder = new StringBuilder();
		      
		     						// Read everything into a StringBuilder
		     						stringBuilder.append(bufferedReader.readLine());
		     						while(bufferedReader.ready())
		     						{
		     						stringBuilder.append("\r\n");
		     						stringBuilder.append(bufferedReader.readLine());				
		     						}*/
		     						
		     						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		     						DocumentBuilder builder = factory.newDocumentBuilder();

		     						FileInputStream fis = new FileInputStream(file);
		     						org.w3c.dom.Document dom = builder.parse(fis);
		     						Element root = (Element) dom.getDocumentElement();
		     						NodeList items = root.getElementsByTagName("trkpt");

		     						for(int j = 0; j < items.getLength(); j++){
			     						
		     							Node item = items.item(j);
		     							NamedNodeMap attrs = item.getAttributes();
		     							NodeList props = item.getChildNodes();

		     							Float longtitude_ = (float) Double.parseDouble(attrs.getNamedItem("lat").getTextContent());
		     							Float latitude_ = (float) Double.parseDouble(attrs.getNamedItem("lon").getTextContent());
		     							//The element ele is measuring the height in meters - not relevant at the moment
		     							//Float ele_ = (float) Double.parseDouble(attrs.getNamedItem("ele").getTextContent());
		     							//Float speed_ = (float) Double.parseDouble(attrs.getNamedItem("speed").getTextContent());

		     							for(int k = 0; k<props.getLength(); k++){
		     								Node item2 = props.item (k);
		     								String name = item2.getNodeName();
		     								if(!name.equalsIgnoreCase("time")) continue;
		     								try{
		     									Long time_ = (getDateFormatter().parse(item2.getFirstChild().getNodeValue())).getTime();
		     									System.out.println("Time" + time_);
		     								}catch(ParseException ex){
		     									ex.printStackTrace();
		     								}
		     								//This can be adapted if the speed is necessary. In this case, the speed will be calculated by the IDM traffic model
		     								/*
		     								for(int z = 0; z<props.getLength(); z++){
		     								Node item3 = props.item(z);
		     								String speed = item3.getNodeName();
		     								if(!name.equalsIgnoreCase("extensions")) continue;
		     									System.out.println("TEST");
		     									if(!name.equalsIgnoreCase("speed")) continue;
		     									Float speed_ = (float) Double.parseDouble(item3.getFirstChild().getNodeValue());
												System.out.println("Speed" + speed_);
		     								}*/
		     	                
		     							}
		     							/*
		     							for(int y = 0; y<props.getLength(); y++){
		     								Node item3 = props.item(y);
		     								String name = item3.getNodeName();
		     								if(!name.equalsIgnoreCase("speed")) continue;
		     								Float speed_ = (float) Double.parseDouble(item3.getFirstChild().getNodeValue());
		     								
		     							}*/
		     							System.out.println("UUID One: " + idOne);
		     							System.out.println("lon" + longtitude_);
		     							System.out.println("lat" + latitude_);
		     							//System.out.println(ele_);
		     							//System.out.println("Speed" + speed_);
		     							
		     							//Add elements to ArrayList
		     							
		     							GPXTraces_.add(idOne.toString());
		     							GPXTraces_.add(longtitude_.toString());
		     							GPXTraces_.add(latitude_.toString());
		     						}
		     						fis.close();
		     					}
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
	

	 
	 