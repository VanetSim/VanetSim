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

import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;

import vanetsim.localization.Messages;

public class LogfileTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -8866885988921022260L;
	
//	private final int MAX_ROWS = 50;
	
	private Data data;
	

	public LogfileTableModel() {
	}
	
	public boolean initializeTM(String filePath, String format) {
		data = new Data();
		if (!data.parseFile(filePath, format)) {
			/* parsing was not successful */
			data = null;
			return false;
		}
		return true;
	}

	public Data getData() {
		return data;
	}
	
	public String[] getColumnNames() {
		return data.getColumnNames();
	}

	@Override
	public int getRowCount() {
		return data.getRowCount();
	}

	@Override
	public int getColumnCount() {
		return data.getColumnCount();
	}
	
	@Override
	public String getColumnName(int column) {
		return data.getColumnName(column);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.getValueAt(rowIndex, columnIndex);
	}

	public void removeRow(int rowIndex) {
		data.removeRow(rowIndex);
	}
	
}
