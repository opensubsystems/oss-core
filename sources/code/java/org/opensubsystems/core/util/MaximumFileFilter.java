/*
 * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

/**
 * File filter implementation to retrieve at most predefined number of files
 * which are older than specified date.
 * 
 * @author bastafidli
 */
public class MaximumFileFilter extends OSSObject
                               implements FilenameFilter
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Current number of files in list
    */
   protected int m_currentFileCount = 0;
   
   /**
    * Maximum number of files in output list
    */
   
   protected int m_iMaximum = 0;
   
   /**
    * Last modification time of file have to be before this date, if it is null, 
    * modification date is not checked
    */
   protected Date m_dtOlderThan;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor with maximum count of file in the output list.
    *  
    * @param iMax - maximum number of files in output list. If it is 0 length of 
    * @param dtOlderThen  - file last modification time heve to by before this 
    *                       date, if it is null modification time is not checked
    *                       output list is unlimited.
    */
   
   public MaximumFileFilter(
      int  iMax,
      Date dtOlderThen
   )
   {
      m_iMaximum = iMax;
      m_dtOlderThan = dtOlderThen; 
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean accept(
      File   dir, 
      String name
   )
   {
      File currentFile = new File(dir, name);
      
      if (currentFile.isDirectory())
      {
         return false;
      }
      if ((m_dtOlderThan != null) 
         && (currentFile.lastModified() > m_dtOlderThan.getTime()))
      {
         return false;
      }
      if ((m_iMaximum == 0) || (m_currentFileCount <= m_iMaximum))
      {
         m_currentFileCount++;
      }
      return  m_currentFileCount <= m_iMaximum;
   }
}

