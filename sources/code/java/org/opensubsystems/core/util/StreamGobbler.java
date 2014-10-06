/*
 * Copyright (C) 2003 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
 * 
 * This file is part of OpenSubsystems.
 *
 * OpenSubsystems is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.opensubsystems.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Class to read or capture specified input stream. This class was inspired by
 * article http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 *  
 * @author bastafidli
 */
public class StreamGobbler extends Thread
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Input stream to read. 
    */
   protected InputStream m_inputStream;

   /**
    * Name of the stream. 
    */
   protected String m_streamName;
   
   /**
    * If true then the stream will be captured (careful about memory) and can 
    * be read.
    */
   protected boolean m_bCapture;
   
   /**
    * Maximal size of the stream to be held in memory.
    */
   protected long m_lMaxCaptureSize;
   
   /**
    * Content of the stream if it should be captured
    */
   protected StringBuffer m_sbStreamContent;
    
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(StreamGobbler.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor. The stream will not be captured.
    * 
    * @param is - input stream to read or capture
    * @param name - name of the stream
    */
   public StreamGobbler(
      InputStream is, 
      String      name
   )
   {
      this(is, name, false, 0);
   }

   /**
    * Constructor
    * 
    * @param is - input stream to read or capture
    * @param name - name of the stream
    * @param bCapture - if true then the stream will be captured (carefull about
    *                   memory) and can be read
    * @param lMaxCaptureSize - maximal size of the stream to be held in memory.
    */
   public StreamGobbler(
      InputStream is, 
      String      name,
      boolean     bCapture,
      long        lMaxCaptureSize
   )
   {
      m_inputStream = is;
      m_streamName = name;
      m_bCapture = bCapture;
      m_lMaxCaptureSize = lMaxCaptureSize;
      if (m_bCapture)
      {
         m_sbStreamContent = new StringBuffer();
      }
      else
      {
         m_sbStreamContent = null;
      }
   }
    
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void run()
   {
      try
      {
         InputStreamReader isr = new InputStreamReader(m_inputStream);
         BufferedReader br = new BufferedReader(isr);

         String       line;
         StringBuilder debugger = new StringBuilder(m_streamName);
         int          iDebuggerDefaultLength;
         String       strLineSeparator = GlobalConstants.getLineSeparator();
         int          iLineSeparatorLength = strLineSeparator.length();
         int          iEndOfLine;
         
         debugger.append(": ");
         iDebuggerDefaultLength = debugger.length();
         while ((line = br.readLine()) != null)
         {
            debugger.delete(0, iDebuggerDefaultLength);
            debugger.append(line);
            s_logger.finest(debugger.toString());
            if (m_bCapture)
            {
               while (m_sbStreamContent.length() + line.length() > m_lMaxCaptureSize)
               {
                  // Lets stream the content so that new line fits in
                  iEndOfLine = m_sbStreamContent.indexOf(strLineSeparator);
                  if (iEndOfLine == -1)
                  {
                     m_sbStreamContent.delete(0, m_sbStreamContent.length());
                  }
                  else
                  {
                     m_sbStreamContent.delete(0, iEndOfLine + iLineSeparatorLength);
                  }
               }
               m_sbStreamContent.append(line);
            }
         }
      } 
      catch (IOException ioExc)
      {
         s_logger.log(Level.WARNING, 
                               "Unexpected exception while reading stream " 
                               + m_streamName, ioExc);
      }
   }

   /**
    * @return String - the content of the captured stream or null if you choose
    *                  to do not capture it.
    */
   public String getStreamContent()
   {
      return m_sbStreamContent.toString();
   }
}
