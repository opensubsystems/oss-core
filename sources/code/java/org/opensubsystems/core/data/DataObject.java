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
 
package org.opensubsystems.core.data;

/**
 * Interface that should be implemented by all data objects regardless of how 
 * they are persisted. Data object is here to implement Transfer Object pattern 
 * as described in 
 * http://java.sun.com/blueprints/corej2eepatterns/Patterns/TransferObject.html
 * The main goal is to encapsulate set of related attributes as one object
 * so they can be easily passed between different modules and tiers. 
 *
 * @author bastafidli
 */
public interface DataObject 
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * ID used when creating new object for which the real ID wasn't generated yet.
    * HSQLDB at some time autogenerated first entry as 0 and it wasn't possible
    * to change it. Therefore we used -1 as value which won't be generated by DB.
    * Then HSQLDB made it configurable but I was afraid that -1 was already used 
    * at some places as hardcoded value and not at constant that it is too risky 
    * to change it to 0 (which is what would it be if the value it not initialized)
    */
   long NEW_ID = -1;

   /**
    * Object representation for easy reuse.
    */
   Long NEW_ID_OBJ = new Long(NEW_ID);
   
   /**
    * String representation for easy reuse.
    */
   String NEW_ID_STR = Long.toString(NEW_ID);
   
   /**
    * Code to use when the data type is not known.
    */
   int NO_DATA_TYPE =  0;
   
   /**
    * Data type code. 
    */
   Integer NO_DATA_TYPE_OBJ = new Integer(NO_DATA_TYPE);

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Get the id uniquely identifying the instance of data.
    * 
    * @return long
    */
   long getId(
   );

   /**
    * Get the id uniquely identifying the instance of data as object. 
    * This method allows the implementing class to cache this object if desired 
    * to improve performance.
    * 
    * @return Long
    */
   Long getIdAsObject(
   ); 
   
   /**
    * Set the unique id for this instance of the data if it wasn't set yet.
    * 
    * @param lNewId - new id of the data object
    */
   void setId(
      long lNewId
   ); 

   /**
    * Get class identifying the data descriptor describing the current instance 
    * of data.
    * 
    * @return Class
    */
   Class<? extends DataDescriptor> getDataDescriptorClass(
   );
   
   /**
    * Get instance of the data descriptor describing the current instance of 
    * data.
    * 
    * @return DataDescriptor
    */
   DataDescriptor getDataDescriptor(
   );
   
   /**
    * Get the data type constant uniquely identifying this type of data. All 
    * objects of the same type should have the same data type constant. This 
    * constant is specified by data descriptor and may change between 
    * installations of this software. 
    * 
    * @return int
    */
   int getDataType(
   );

   /**
    * Get the data type constant uniquely identifying this type of data as 
    * object. All objects of the same type should have the same data type 
    * constant. This constant is specified by data descriptor and may change 
    * between installations of this software. 
    * This method allows to  the implementing class to cache this object if 
    * desired to improve performance.
    * 
    * @return Integer
    */
   Integer getDataTypeAsObject(
   );
   
   /**
    * Compare all data attributes of two objects to figure out, if their 
    * attributes are the same. The objects can for example differ by database 
    * generated id and creation or modification timestamp but if other properties
    * which represents the real data are the same then the objects are considered
    * to be the same. This is weaker constraint that equals() method since not 
    * all attributes has to be the same, only the one which really represents 
    * the business data should match.
    * 
    * @param oObject - Object to compare
    * @return boolean - true if same, false otherwise
    */
   boolean isSame(
      Object oObject
   );
}
