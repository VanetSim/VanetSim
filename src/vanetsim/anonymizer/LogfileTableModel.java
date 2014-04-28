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
package vanetsim.anonymizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

public class LogfileTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -8866885988921022260L;
	
	private String filePath;
	private String format;
	
	private final int MAX_ROWS = 50;
	
	private ArrayList<Class<? extends Object>> classes;
	private ArrayList<ArrayList<Object>> data;
	private String[] columnNames;

	private Character delimiter;
	
	public LogfileTableModel(String filePath, String format) {
//		this.filePath = filePath;
//		this.format = format;
		
		parseFile(filePath, format);
	}
	
	public static String getFirstLine(String filePath) {
		try (BufferedReader in = new BufferedReader(new FileReader(filePath))) {
			return in.readLine();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	private void parseFile(String filePath, String format) {
		String line;
		StringTokenizer tok;

		analyseFormatString(format);
				
		try (BufferedReader in = new BufferedReader(new FileReader(filePath))) {
			line = in.readLine();
			
			columnNames = new String[classes.size()];
			data = new ArrayList<>(classes.size());

			tok = new StringTokenizer(line, delimiter.toString());		
			for (int i = 0; i < classes.size(); i++) {
				columnNames[i] = tok.nextToken();
				data.add(i, new ArrayList<>());
			}
			
			for (int i = 0; i < MAX_ROWS; i++) {
				if ((line = in.readLine()) == null) {
					break;
				} 
				tok = new StringTokenizer(line, delimiter.toString());		
				for (int j = 0; j < classes.size(); j++) {
					data.get(j).add(tok.nextToken());
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void analyseFormatString(String format) throws IllegalArgumentException {
		classes = new ArrayList<>();
		int index = 0;
		Character delimiter = null;
		
		while (index < format.length()) {
			/* all odd indices should be the delimiter  */
			if (index % 2 == 1) {
				if (delimiter == null) {
					delimiter = new Character(format.charAt(index));
				} else {
					if (!delimiter.equals(format.charAt(index))) {
						throw new IllegalArgumentException();
					}
				}
			} else {
				switch (format.charAt(index)) {
				case 's':
				case 'S':
					classes.add(String.class);
					break;
				case 'c':
				case 'C':
					classes.add(Character.class);
					break;
				case 'i':
				case 'I':
					classes.add(Integer.class);
					break;
				case 'd':
				case 'D':
					classes.add(Double.class);
					break;
				default:
					throw new IllegalArgumentException();
				}
			}
			index++;
		}
		
		this.delimiter = delimiter;
	}
	
	public String[] getColumnNames() {
		return columnNames;
	}

	@Override
	public int getRowCount() {
		return data.get(0).size();
	}

	@Override
	public int getColumnCount() {
		return data.size();
	}
	
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(columnIndex).get(rowIndex);
	}
	
    public Class<? extends Object> getColumnClass(int c) {
        return classes.get(c);
    }
}
