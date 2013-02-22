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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.opensubsystems.core.data.DataObject;

/**
 * Utility methods for DataObject manipulation.
 * 
 * @author OpenSubsystems
 */
public final class DataObjectUtils extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////
      
   /**
    * Constant for compare result flag - nothing has changed.
    */
   public static final byte COMPARE_ORDER_NO_CHANGE = 0;
   
   /**
    * Constant for compare result flag - order changed.
    */
   public static final byte COMPARE_ORDER_CHANGED = 1;
   
   /**
    * Constant for compare result flag - attributes changed. 
    */
   public static final byte COMPARE_ATTRIBUTES_CHANGED = 2;
   
   /**
    * Constant for compare result flag - lists changed (added or removed objects).
    */
   public static final byte COMPARE_CHANGED = 4;

   // Constructor //////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private DataObjectUtils(
   )
   {
      // Do nothing
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Parse ids from collection of Data Objects to String.
    * 
    * @param colDataObjects - collection of data objects to parse
    * @param strDel - String delimiter
    * @return String - string containing ids of the data objects separated by
    *                  the delimiter
    */
   public static String parseCollectionIdsToString(
      Collection<DataObject> colDataObjects,
      String                 strDel
   ) 
   {
      StringBuilder strbInts = new StringBuilder();
      if ((colDataObjects != null) && (!colDataObjects.isEmpty()))
      {
         for (Iterator<DataObject> items = colDataObjects.iterator(); items.hasNext();)
         {
            if (strbInts.length() > 0)
            {
               strbInts.append(strDel);
            }
            strbInts.append((items.next()).getId());
         }
      }
      return strbInts.toString();  
   }

   /**
    * Convert collection of data objects to map keyed by their ids.
    * 
    * @param colDataObjects - collection of data objects to parse
    * @return Map<Long, DataObject> - map of data objects keyed by ids
    */
   public static Map<Long, DataObject> convertCollectionToMap(
      Collection<DataObject> colDataObjects
   ) 
   {
      Map<Long, DataObject> mpData = Collections.emptyMap();
      
      if ((colDataObjects != null) && (!colDataObjects.isEmpty()))
      {
         mpData = new HashMap<>(colDataObjects.size());
         for (DataObject data : colDataObjects)
         {
            mpData.put(data.getIdAsObject(), data);
         }
      }
      return mpData;  
   }
}
