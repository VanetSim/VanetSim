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

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.ArrayList;

import de.erichseifert.vectorgraphics2d.PDFGraphics2D;
/**
 * @author andreastomandl
 *
 */
public class VectorCanvas {
	public static void saveCanvas(ArrayList<String> locationInformationMDS, boolean MDSMode, int width, int height){
        // Create a new PDF document with a width of 210 and a height of 297
        PDFGraphics2D g = new PDFGraphics2D(0.0, 0.0, width*10, height*10);
        System.out.println(width + ":" + height);
     // Draw a red ellipse at the position (20, 30) with a width of 100 and a height of 150
        g.setColor(Color.RED);
        g.fillOval(20, 30, 100, 150);
        
        
        if(locationInformationMDS != null){
			String[] data;
			for(String location:locationInformationMDS){
				data = location.split(":");
				if(MDSMode){
					// TP
					if(data[8].equals("true") && data[10].equals("true"))g.setColor(Color.LIGHT_GRAY);
					//FN
					else if(data[8].equals("false") && data[10].equals("false"))g.setColor(Color.BLACK);
				}
				else{
					// TN
					if(data[8].equals("false") && data[10].equals("true"))g.setColor(Color.LIGHT_GRAY);
					//FP
					else if(data[8].equals("true") && data[10].equals("false"))g.setColor(Color.BLACK);
				}
				g.drawOval(Integer.parseInt(data[5])/100-25, Integer.parseInt(data[6])/100-25,50,50);
			}
		}
        

        // Write the PDF output to a file
        FileOutputStream file = null;
		try {
			file = new FileOutputStream("ellipse.pdf");
			System.out.println("file written");
			file.write(g.getBytes());
			file.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
