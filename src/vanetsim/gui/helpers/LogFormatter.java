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

import java.io.PrintWriter; 
import java.io.StringWriter; 
import java.util.logging.Formatter; 
import java.util.logging.LogRecord; 

public final class LogFormatter extends Formatter { 
    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); 
    @Override 
    public String format(LogRecord record) { 
        StringBuilder sb = new StringBuilder(); 
        sb.append(formatMessage(record)) 
            .append(LINE_SEPARATOR); 
        if (record.getThrown() != null) { 
            try { 
                StringWriter sw = new StringWriter(); 
                PrintWriter pw = new PrintWriter(sw); 
                record.getThrown().printStackTrace(pw); 
                pw.close(); 
                sb.append(sw.toString()); 
            } catch (Exception ex) { 
                // ignore 
            } 
        } 
        return sb.toString(); 
    } 
} 