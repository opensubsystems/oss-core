/*
 * Copyright (C) 2003 - 2015 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.util.EnumSet;
import org.opensubsystems.core.data.impl.UndefinedDataDescriptor;

/**
 * Interface representing collection of metadata elements that describe data 
 * objects and their attributes. 
 * 
 * The metadata include
 * - constant (data type) identifying the data object 
 * - constants identifying each attribute of the data object
 * - constants identifying various sets of attributes, such as all attributes, 
 *   filterable attributes, attributes to display in list
 * - constants identifying default values of attributes 
 *
 * @param <E> - enumeration representing fields of the described data
 * @author bastafidli
 */
public interface DataDescriptor<E extends Enum<E>>
{
   /**
    * Constant that can be used at times when no data descriptor was defined
    * but yet one is required to be specified. This will allow us to identify
    * all such places at later time and define the data descriptors if needed.
    */
   DataDescriptor NO_DATA_DESCRIPTOR = UndefinedDataDescriptor.getInstance();
   
   /**
    * Constant that can be used at times when no data descriptor was defined
    * but yet one is required to be specified. This will allow us to identify
    * all such places at later time and define the data descriptors if needed.
    */
   Class<? extends DataDescriptor> NO_DATA_DESCRIPTOR_CLASS = UndefinedDataDescriptor.class;
   
   /**
    * Class identifying parent data descriptor for which this data descriptor 
    * provides just a different view for the same data objects. These two data 
    * descriptors share the same desired and actual data types.
    * 
    * @return Class - class identifying parent data descriptor or null if this
    *                 is independent descriptor
    */
   Class<DataDescriptor> getParentDescriptorClass(
   );
   
   /**
    * Desired value of data type assigned to the data object described by this 
    * descriptor during development. This value can be accepted or changed by
    * DataDescriptorManager if there is a conflict between various data types of 
    * object from which the application is constructed of.
    * 
    * @return int - desired value of data type or DataObject.NO_DATA_TYPE if this
    *               data descriptor has a parent data descriptor
    */
   int getDesiredDataType(
   );
   
   /**
    * Get the real value of data type code assigned to this class by 
    * DataDescriptorManager when application is assembled and any potential 
    * conflicts between desired values for all assembled data types are detected 
    * and resolved.
    * 
    * @return int - real value of data type or DataObject.NO_DATA_TYPE if this
    *               data descriptor has not the data type yet set
    */
   int getDataType(
   );
   
   /**
    * DataDescriptorManager uses this method to set the real value of data type 
    * code assigned to this class when application is assembled and any potential 
    * conflicts between desired values for all assembled data types are detected 
    * and resolved.
    *
    * @param iDataType - the real value of data type assigned to this class
    */
   void setDataType(
      int iDataType
   );

   /**
    * Get the data type code uniquely identifying this type of data as object.
    * 
    * @return Integer
    */
   Integer getDataTypeAsObject(
   );

   /**
    * Get the displayable name for the view represented by this data descriptor.
    *  
    * @return String
    */
   String getDisplayableViewName(
   );

   /**
    * Get the logical name identifying the view for the specified type of data  
    * represented by this data descriptor. This name is independent of the 
    * language and therefore can be used to identify the view under any 
    * circumstances.
    * 
    * @return String
    */
   String getViewName(
   );
   
   /**
    * Get fields of the described data.
    * 
    * @return EnumSet<E> - set of fields of the described data
    */
   EnumSet<E> getFields();
   
	/**
	 * Get maximal length of data that can be stored in a specified field.
	 * 
	 * @param field - field to get a maximal length of data for
	 * @return Integer - maximal length of data for the specified field. This will
    *                   never be null.
	 */
	Integer getFieldMaxLength(
	   E field
	);
	
	/**
	 * Set maximal length of data that can be stored in a specified field.
	 * 
	 * @param field - field to get a maximal length of data for
	 * @param iMaxLength - maximal length of data for the specified field. If this
    *                     is null, it will be saved as 0.
	 */
	void setFieldMaxLength(
	   E		  field, 
		Integer iMaxLength
   );
}
