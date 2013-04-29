/*
 * Copyright (C) 2012 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Class containing methods for working with objects.
 * 
 * @author bastafidli
 */
public final class ObjectUtils extends OSSObject
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private ObjectUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Compare two objects that can be null for equality. 
    *
    * @param first - first object to compare, it can be null
    * @param second - second object to compare, it can be null 
    * @return boolean - true if the two objects are equal, false otherwise
    */
   public static boolean equals(
      Object first,
      Object second
   )
   {
      return (((first == null) && (second == null))
             || ((first != null) && (first.equals(second))));
   }

   /**
    * Convert collection of objects to map keyed by some value from the object.
    * 
    * @param colSource - collection of data objects to convert
    * @return Map<K, V> - converted map of data objects keyed by values from the 
    *                     objects
    */
   public static <K, V> Map<K, V> convertCollectionToMap(
      Collection<V>           colSource,
      ObjectValueReader<K, V> reader
   ) 
   {
      Map<K, V> mpDestination = Collections.emptyMap();
      
      if ((colSource != null) && (!colSource.isEmpty()))
      {
         mpDestination = new HashMap<>(colSource.size());
         for (V data : colSource)
         {
            mpDestination.put(reader.getValue(data), data);
         }
      }
      return mpDestination;  
   }

   /**
    * Convert collection of objects to map keyed by some value from the object.
    * 
    * @param colSource - collection of data objects to convert
    * @return Map<K, V> - converted map of data objects keyed by values from the 
    *                     objects
    */
   public static <K, V> Map<K, Collection<V>> convertCollectionToMultiValueMap(
      Collection<V>           colSource,
      ObjectValueReader<K, V> reader
   ) 
   {
      Map<K, Collection<V>> mpDestination = Collections.emptyMap();
      
      if ((colSource != null) && (!colSource.isEmpty()))
      {
         K             key;
         Collection<V> colExisting;
         
         mpDestination = new HashMap<>(colSource.size());
         for (V data : colSource)
         {
            key = reader.getValue(data);
            colExisting = mpDestination.get(key);
            if (colExisting == null)
            {
               colExisting = new ArrayList<>();
               mpDestination.put(key, colExisting);
            }
            colExisting.add(data);
         }
      }
      return mpDestination;  
   }

   /**
    * Convert collection of objects to map keyed by some value from the object.
    * 
    * @param colSource - collection of data objects to convert
    * @return Map<K, V> - converted map of data objects keyed by values from the 
    *                     objects
    */
   public static <K, V> Map<K, Collection<V>> convertCollectionToMultiKeyValueMap(
      Collection<V>                colSource,
      ObjectMultiValueReader<K, V> reader
   ) 
   {
      Map<K, Collection<V>> mpDestination = Collections.emptyMap();
      
      if ((colSource != null) && (!colSource.isEmpty()))
      {
         Collection<K> colKeys;
         Collection<V> colExisting;
         
         mpDestination = new HashMap<>(colSource.size());
         for (V data : colSource)
         {
            colKeys = reader.getValues(data);
            for (K key: colKeys)
            {
               colExisting = mpDestination.get(key);
               if (colExisting == null)
               {
                  colExisting = new ArrayList<>();
                  mpDestination.put(key, colExisting);
               }
               colExisting.add(data);
            }
         }
      }
      return mpDestination;  
   }
}
