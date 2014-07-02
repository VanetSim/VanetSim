package vanetsim.anonymizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.sound.sampled.Port.Info;

public class Data {
	private List<List<Object>> data;
	private ArrayList<Class<? extends Object>> classes;
	private String[] columnNames;

	private Character delimiter;
	
	/**
	 * Get the first line of a file to display it to the user
	 * helping him to specify a correct format string.
	 * @param filePath	path to the file from which the first line is read
	 * @return			the first line or null in case of an error
	 */
	public static String getFirstLine(String filePath) {
		try (BufferedReader in = new BufferedReader(new FileReader(filePath))) {
			return in.readLine();
		} catch (IOException e1) {
			return null;
		}
	}
	
	/**
	 * Parse a file and load its content
	 * @param filePath	a path to a file to be parsed
	 * @param format	a format string specifying the content of the chosen file
	 */
	public boolean parseFile(String filePath, String format) {
		String line;
		StringTokenizer tok;

		try (BufferedReader in = new BufferedReader(new FileReader(filePath))) {
			analyseFormatString(format);
			/* read the first line, which should contain the column names */
			line = in.readLine();
			
			columnNames = new String[classes.size()];
			data = new ArrayList<>(classes.size());

			tok = new StringTokenizer(line, delimiter.toString());		
			for (int i = 0; i < classes.size(); i++) {
				columnNames[i] = tok.nextToken();
				data.add(i, new LinkedList<>());
			}

			/* all other lines contain data. Read that into 'data' */				
			while ((line = in.readLine()) != null) {
				tok = new StringTokenizer(line, delimiter.toString());		
				for (int j = 0; j < classes.size(); j++) {
					if (classes.get(j) == String.class) {
						String element = tok.nextToken();
						data.get(j).add(element);
					} else if (classes.get(j) == Character.class) {
						char element = tok.nextToken().charAt(0);
						data.get(j).add(element);
					} else if (classes.get(j) == Integer.class) {
						int element = Integer.parseInt(tok.nextToken());
						data.get(j).add(element);
					} else if (classes.get(j) == Double.class) {
						double element = Double.parseDouble(tok.nextToken());
						data.get(j).add(element);
					} 
				}
			}
		} catch (NoSuchElementException | IllegalArgumentException e) {
			/* NoSuchElementException: thrown by tok.nextToken() if there is no next token */
			/* IllegalArgumentException: thrown by analyseFormatString */
			/* NumberFormatException: thrown by parseInt & parseDouble, caught by IllegalArguementException */

			/* format string or data was wrong, abort operation */
			columnNames = null;
			data = null;
			return false;
		} catch (IOException e1) {
			/* it should be made sure, that the filename is valid */
			assert(false);
		}
		
		return true;
	}
	
	/**
	 * Check whether a format string is valid and if so, create an ArrayList of the classes.
	 * Valid classes are:
	 * 		String 		[s|S]
	 * 		Character 	[c|C]
	 * 		Integer 	[i|I]
	 * 		Double 		[d|D]
	 * and an arbitrary delimiter between those classes. 
	 * Every other format results in an IllegalArgumentException.
	 * 
	 * @param format	a string specifying the format of a log file 
	 * @throws IllegalArgumentException
	 */
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
	
	public List<List<Object>> getData() {
		return data;
	}

	public String[] getColumnNames() {
		return columnNames;
	}
	
	public int getRowCount() {
		return data.get(0).size();
	}
	
	public int getColumnCount() {
		return data.size();
	}
	
    public Class<? extends Object> getColumnClass(int c) {
        return classes.get(c);
    }
    
	public String getColumnName(int column) {
		return columnNames[column];
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(columnIndex).get(rowIndex);
	}
	
	public void removeRow(int rowIndex) {
		for (List<Object> al : data) {
			al.remove(rowIndex);
		}
	}
}
