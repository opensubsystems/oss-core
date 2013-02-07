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

import java.util.Comparator;

import org.opensubsystems.core.data.DataObject;

/**
 * Comparator that determines order of DataObjects based on some order of ids
 * specified in the constructor. 
 * 
 * @author OpenSubsystems
 */
public class DataObjectOrderingComparator extends    OSSObject
                                           implements Comparator<DataObject> 
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Array of ordered IDs which will determine the order of data objects  
    */
   protected int[] m_arrOrderedIDs = null;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor.
    * 
    * @param arrOrderedIDs - ordered IDs which will determine the order of data 
    *                        objects  
    */
   public DataObjectOrderingComparator(
      int[] arrOrderedIDs
   )
   {
      super();
      
      m_arrOrderedIDs = arrOrderedIDs;
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public int compare(
         DataObject o1, 
      DataObject o2
   ) 
   {
      int iReturn = 0;
      
      if ((o1 instanceof DataObject) && (o2 instanceof DataObject))
      {
         if (m_arrOrderedIDs != null) 
         {
            long l1 = o1.getId();
            long l2 = o2.getId();
            
            if (l1 != l2)
            {
               int iCounter = 0;
               
               while ((iReturn == 0) && (iCounter < m_arrOrderedIDs.length))
               {
                  if (m_arrOrderedIDs[iCounter] == l1)
                  {
                     iReturn = -1;
                  }
                  else if (m_arrOrderedIDs[iCounter] == l2)
                  {
                     iReturn = 1;
                  }
                  iCounter++;
               }
            }
         }
         else 
         {
            long l1 = o1.getId();
            long l2 = o2.getId();
            
            if (l1 < l2)
            {
               iReturn = -1;
            }
            else if (l1 > l2)
            {
               iReturn = 1;
            }
            else
            {
               iReturn = 0;
            }
         }
      }

      return iReturn;
   }
}
