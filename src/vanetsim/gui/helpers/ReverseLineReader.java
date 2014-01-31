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
 
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;
 
/************************************************************************************************************************************
 * <p>Title: Reads text files line per line but from end to previous</p>
 *
 * <p>Description: It uses RandomAccessFile to read from the end to the beginning</p>
 *
 * <p>Copyright: Copyright (c) 2009 class is under LGPL</p>
 *
 * <p>Company: Taschek Joerg</p>
 *
 * @author <a href="mailto:behaveu@gmail.com">Taschek Joerg</a>
 * @version 1.0
 ***********************************************************************************************************************************/
public class ReverseLineReader
{
  private RandomAccessFile realReader = null;
  private int aproxBytesPerLine = 0;
  private long lastPosition = -1;
  private boolean fileStartReached = false;
 
  /**********************************************************************************************************************************
   * Constructor to open the file that should be read out
   * @param file which should be opened
   * @throws FileNotFoundException
   * @throws IOException
   *********************************************************************************************************************************/
  public ReverseLineReader(String file) throws FileNotFoundException, IOException
  {
    this(file, 100);
  }
 
  /**********************************************************************************************************************************
   * Constructor to open the file that should be read out
   * @param file which should be opened
   * @throws FileNotFoundException
   * @throws IOException
   *********************************************************************************************************************************/
  public ReverseLineReader(String file, long startPosition) throws FileNotFoundException, IOException
  {
    this(file, 100, startPosition);
  }
 
  /**********************************************************************************************************************************
   * Constructor to open the file that should be read out
   * @param file which should be opened
   * @param aproxBytesPerLine how many bytes should be read out to find the next line - default is 100
   * @throws FileNotFoundException
   * @throws IOException
   *********************************************************************************************************************************/
  public ReverseLineReader(String file, int aproxBytesPerLine) throws FileNotFoundException, IOException
  {
    super();
    realReader = new RandomAccessFile(file, "r");
    this.aproxBytesPerLine = aproxBytesPerLine;
    lastPosition = realReader.length();
  }
  
  /**********************************************************************************************************************************
   * Constructor to open the file that should be read out
   * @param file which should be opened
   * @param aproxBytesPerLine how many bytes should be read out to find the next line - default is 100
   * @throws FileNotFoundException
   * @throws IOException
   *********************************************************************************************************************************/
  public ReverseLineReader(String file, int aproxBytesPerLine, long startPosition) throws FileNotFoundException, IOException
  {
    super();
    realReader = new RandomAccessFile(file, "r");
    this.aproxBytesPerLine = aproxBytesPerLine;
    lastPosition = startPosition;
  }
 
  /**********************************************************************************************************************************
   * Reads the previous line
   * @return String the previous line or null if file start is reached
   * @throws IOException
   *********************************************************************************************************************************/
  public String readPreviousLine() throws IOException
  {
    if(fileStartReached)
      return null;
    String ret = null;
    boolean abort = false;
    int count = 0;
    while(!abort)
    {
      count++;
      int byteReads = aproxBytesPerLine * count;
      while(lastPosition - byteReads < 0)
        byteReads --;
      realReader.seek(lastPosition - byteReads);
      byte buf[] = new byte[byteReads];
      int read = realReader.read(buf, 0, buf.length);
      String tmp = new String(buf, 0, read);
      int position = -1;
      if(tmp.indexOf("\r\n") != -1) //windows linebreak
      {
        position = tmp.lastIndexOf("\r\n") + 2;
        abort = true;
      }
      else if(tmp.indexOf("\n") != -1) //linux, mac os X linebreak
      {
        position = tmp.lastIndexOf("\n") + 1;
        abort = true;
      }
      else if(lastPosition - read <= 0)
      {
        ret = tmp;
        abort = true;
        fileStartReached = true;
      }
      if(abort && position != -1)
      {
        lastPosition = lastPosition - (byteReads  - position + 1);
        ret = tmp.substring(position);
      }
    }
    return ret;
  }
 
  /**********************************************************************************************************************************
   * Method resets the markers to start reading the file out from the end again
   * @throws IOException
   *********************************************************************************************************************************/
  public void reset() throws IOException
  {
    fileStartReached = false;
    lastPosition = realReader.length();
  }
 
  /**********************************************************************************************************************************
   * closes the file handle
   * @throws IOException
   *********************************************************************************************************************************/
  public void close() throws IOException
  {
    realReader.close();
  }
}

