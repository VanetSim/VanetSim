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
package vanetsim.gui.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.SMOutputFactory;
import org.codehaus.staxmate.in.SMInputCursor;
import org.codehaus.staxmate.out.SMOutputDocument;
import org.codehaus.staxmate.out.SMOutputElement;

import vanetsim.ErrorLog;
import vanetsim.localization.Messages;

/**
 * This class provides functions to read (getVehicleTypes()) and save (saveVehiclesTypes()) in xml
 */
public class VehicleTypeXML {
	
	/** The path of the xml file*/
	private String xmlPath_;
	
	/** The default path and filename, used if no path is set*/
	private String defaultPath_ = "vehicleTypes.xml";
	
	/** The ArrayList types collects all VehicleTypes*/
	public ArrayList<VehicleType> types_;
	
	/** If no path is set, the default path is used*/
	public VehicleTypeXML(String path){
		if(path == null) xmlPath_ = defaultPath_;
		else xmlPath_ = path;
			
	}
	
	/**
	 * Returns a ArrayList with all vehicle types found.
	 * 
	 * @return Arraylist with all vehicle types
	 */
	public ArrayList<VehicleType> getVehicleTypes(){
		 types_ = new ArrayList<VehicleType>();

		//read vehicle types
		try{

			SMInputCursor childCrsr, typCrsr;
			XMLInputFactory factory = XMLInputFactory.newInstance();

			// configure some factory options...
			factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
			factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
			factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
			factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);

			InputStream filestream;
			filestream = new FileInputStream(xmlPath_);
			XMLStreamReader sr = factory.createXMLStreamReader(filestream);	
			SMInputCursor rootCrsr = SMInputFactory.rootElementCursor(sr);
			rootCrsr.getNext();
			if(rootCrsr.getLocalName().toLowerCase().equals("vehicletypes")){ //$NON-NLS-1$
				childCrsr = rootCrsr.childElementCursor();
				while(childCrsr.getNext() != null){
					if(childCrsr.getLocalName().toLowerCase().equals("vehicle")){	//$NON-NLS-1$
						typCrsr = childCrsr.childElementCursor();
						String tmpName = "";
						int vehicleLength = 0, tmpMaxSpeed = 0, tmpMinSpeed = 0, tmpMaxCommDist = 0, tmpMinCommDist = 0, tmpMaxWaittime = 0, tmpMinWaittime = 0, tmpMaxBraking_rate = 0, tmpMinBraking_rate = 0, tmpMaxAcceleration_rate = 0, tmpMinAcceleration_rate = 0, tmpMinTimeDistance = 0, tmpMaxTimeDistance = 0, tmpMinPoliteness = 0, tmpMaxPoliteness = 0, tmpVehiclesDeviatingMaxSpeed = 0, tmpDeviationFromSpeedLimit = 0, color = 0;
						boolean wifi = false, emergencyVehicle = false; 
						
						while (typCrsr.getNext() != null){		
							if(typCrsr.getLocalName().toLowerCase().equals("name")){ //$NON-NLS-1$
								tmpName = typCrsr.getElemStringValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("vehiclelength")){ //$NON-NLS-1$
								vehicleLength = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("maxspeed")){ //$NON-NLS-1$
								tmpMaxSpeed = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("minspeed")){ //$NON-NLS-1$
								tmpMinSpeed = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("maxcommdist")){ //$NON-NLS-1$
								tmpMaxCommDist = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("mincommdist")){ //$NON-NLS-1$
								tmpMinCommDist = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("maxbraking_rate")){ //$NON-NLS-1$
								tmpMaxBraking_rate = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("minbraking_rate")){ //$NON-NLS-1$
								tmpMinBraking_rate = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("maxacceleration_rate")){ //$NON-NLS-1$
								tmpMaxAcceleration_rate = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("minacceleration_rate")){ //$NON-NLS-1$
								tmpMinAcceleration_rate = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("mintimedistance")){ //$NON-NLS-1$
								tmpMinTimeDistance = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("maxtimedistance")){ //$NON-NLS-1$
								tmpMaxTimeDistance = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("minpoliteness")){ //$NON-NLS-1$
								tmpMinPoliteness = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("maxpoliteness")){ //$NON-NLS-1$
								tmpMaxPoliteness = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("vehiclesdeviatingmaxspeed")){ //$NON-NLS-1$
								tmpVehiclesDeviatingMaxSpeed = typCrsr.getElemIntValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("deviationfromspeedlimit")){ //$NON-NLS-1$
								tmpDeviationFromSpeedLimit = typCrsr.getElemIntValue();	
							} else if(typCrsr.getLocalName().toLowerCase().equals("maxwaittime")){ //$NON-NLS-1$
								tmpMaxWaittime = typCrsr.getElemIntValue();	
							} else if(typCrsr.getLocalName().toLowerCase().equals("minwaittime")){ //$NON-NLS-1$
								tmpMinWaittime = typCrsr.getElemIntValue();	
							} else if(typCrsr.getLocalName().toLowerCase().equals("wifi")){ //$NON-NLS-1$
								wifi = typCrsr.getElemBooleanValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("emergencyvehicle")){ //$NON-NLS-1$
								emergencyVehicle = typCrsr.getElemBooleanValue();
							} else if(typCrsr.getLocalName().toLowerCase().equals("color")){ //$NON-NLS-1$
								color = typCrsr.getElemIntValue();	
							}
						}
						types_.add(new VehicleType(tmpName, vehicleLength, tmpMaxSpeed, tmpMinSpeed, tmpMaxCommDist, tmpMinCommDist, tmpMaxBraking_rate, tmpMinBraking_rate, tmpMaxAcceleration_rate, tmpMinAcceleration_rate, tmpMinTimeDistance, tmpMaxTimeDistance, tmpMinPoliteness, tmpMaxPoliteness, tmpVehiclesDeviatingMaxSpeed, tmpDeviationFromSpeedLimit, tmpMaxWaittime, tmpMinWaittime, wifi, emergencyVehicle, color));
					}
				}
			
			}
		} catch (Exception e) { 
			ErrorLog.log(Messages.getString("ErrorLog.loadType"), 6, getClass().getName(), "load", e); //$NON-NLS-1$ //$NON-NLS-2$
			e.printStackTrace(); 
		}
		
		return types_;
	}
	
	/** 
	 *Save all vehicle types in <code>tmpList</code> to the <code>xmlPath_</code>
	 **/
	public void saveVehicleTypes(ArrayList<VehicleType> tmpList){
		try{
			File file = new File(xmlPath_);
			OutputStream filestream = new FileOutputStream(file);
			XMLStreamWriter xw = XMLOutputFactory.newInstance().createXMLStreamWriter(filestream);
			SMOutputDocument doc = SMOutputFactory.createOutputDocument(xw);
			doc.setIndentation("\n\t\t\t\t\t\t\t\t", 2, 1); ;  //$NON-NLS-1$
			doc.addComment("Generated on " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date())); //$NON-NLS-1$ //$NON-NLS-2$
			
			SMOutputElement root = doc.addElement("VehicleTypes");			 //$NON-NLS-1$
			
			for(VehicleType type : tmpList){
				SMOutputElement vehicle = root.addElement("Vehicle");
				vehicle.addElement("Name").addCharacters(type.getName());
				vehicle.addElement("VehicleLength").addValue(type.getVehicleLength());
				vehicle.addElement("MinSpeed").addValue(type.getMinSpeed()); 
				vehicle.addElement("MaxSpeed").addValue(type.getMaxSpeed()); 
				vehicle.addElement("MinCommDist").addValue(type.getMinCommDist()); 
				vehicle.addElement("MaxCommDist").addValue(type.getMaxCommDist()); 
				vehicle.addElement("MinBraking_Rate").addValue(type.getMinBrakingRate()); 
				vehicle.addElement("MaxBraking_Rate").addValue(type.getMaxBrakingRate()); 
				vehicle.addElement("MinAcceleration_Rate").addValue(type.getMinAccelerationRate()); 
				vehicle.addElement("MaxAcceleration_Rate").addValue(type.getMaxAccelerationRate()); 
				vehicle.addElement("MinTimeDistance").addValue(type.getMinTimeDistance()); 
				vehicle.addElement("MaxTimeDistance").addValue(type.getMaxTimeDistance()); 
				vehicle.addElement("MinPoliteness").addValue(type.getMinPoliteness()); 
				vehicle.addElement("MaxPoliteness").addValue(type.getMaxPoliteness()); 
				vehicle.addElement("vehiclesDeviatingMaxSpeed").addValue(type.getVehiclesDeviatingMaxSpeed_()); 
				vehicle.addElement("deviationFromSpeedLimit").addValue(type.getDeviationFromSpeedLimit_()); 
				vehicle.addElement("MinWaitTime").addValue(type.getMinWaittime()); 
				vehicle.addElement("MaxWaitTime").addValue(type.getMaxWaittime()); 
				vehicle.addElement("Wifi").addValue(type.isWifi()); 
				vehicle.addElement("EmergencyVehicle").addValue(type.isEmergencyVehicle()); 
				vehicle.addElement("Color").addValue(type.getColor());
			}
			doc.closeRoot();
			xw.close();
			filestream.close();
		}catch (Exception e) { ErrorLog.log(Messages.getString("ErrorLog.saveType"), 6, getClass().getName(), "save", e);} //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Gets the defaultPath.
	 * 
	 * @return String defaultPath_: the default path to save the vehicle types.
	 */
	public String getDefaultPath() {
		return defaultPath_;
	}
}

