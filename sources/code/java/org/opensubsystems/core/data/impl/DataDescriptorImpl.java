/*
 * Copyright (C) 2008 - 2015 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.data.impl;

import java.util.EnumMap;
import java.util.EnumSet;
import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInvalidDataException;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.OSSObject;

/**
 * Implementation of interface representing collection of metadata elements that 
 * describe data objects and their attributes. 
 * 
 * The metadata include
 * - constant (data type) identifying the data object 
 * - constants identifying each attribute of the data object
 * - constants identifying various sets of attributes, such as all attributes, 
 *   filterable attributes, attributes to display in list
 * - constants identifying default values of attributes 
 *
 * The use of enumeration for fields is inspired by
 * http://stackoverflow.com/questions/19155405/java-enum-extends-workaround
 * 
 * @param <E> - enumeration representing fields of the described data
 * @author bastafidli
 */
public class DataDescriptorImpl<E extends Enum<E>> extends OSSObject
															      implements DataDescriptor<E>
{
   // Attributes ///////////////////////////////////////////////////////////////

   /**
    * Class identifying parent data descriptor for which this data descriptor 
    * provides just a different view for the same data objects. These two data 
    * descriptors share the same desired and actual data types and describe the 
    * same underlying data but may describe them differently since they represent
    * different views of the data.
    */
   protected Class<DataDescriptor> m_clsParentDescriptor;
   
   /**
    * Desired value of data type assigned to the data object described by this 
    * descriptor during development. This value can be accepted or changed by 
    * DataDescriptorManager if there is a conflict between various data types of 
    * objects from which the application is constructed of. 
    */
   protected int m_iDesiredDataType;
   
   /**
    * Real value of data type assigned to this class by DataDescriptorManager 
    * when application is assembled and any potential conflicts between desired 
    * values for all assembled data types are detected and resolved.
    */
   protected int m_iDataType;
   
   /**
    * Logical name identifying the view for the specified data type object 
    * represented by this data descriptor. This name is independent of the 
    * language and therefore can be used to identify the view under any 
    * circumstances.
    */
   protected String m_strViewName;
   
   /**
    * Displayable name for the view represented by this data descriptor. This 
    * name allows different views of the same data objects to be called 
    * differently to make them more user friendly.  
    */
   protected String m_strDisplayableViewName;
   
	/**
	 * Enumeration representing data fields for the class
	 */
	protected EnumSet<E>	m_setDataFields;
	
	/**
	 * Maximal length of the folder field.
	 * The value depends on the underlying persistence mechanism and it is set
	 * once the persistence layer is initialized.
	 */
	protected EnumMap<E, Integer> m_mpDataFieldLengths;

	// Cached values ////////////////////////////////////////////////////////////
   
   /**
    * This is constructed only when somebody asks for it. It is private so that
    * we can keep it in sync with the real id. 
    */
   private Integer m_iDataTypeObject;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor.
    * 
    * @param iDesiredDataType - desired value of data type assigned to the data 
    *                           object described by this descriptor during 
    *                           development. This value can be accepted or 
    *                           changed by DataDescriptorManager if there is a 
    *                           conflict between various data types of object 
    *                           from which the application is constructed. Each 
    *                           data descriptor defines code for its desired 
    *                           data type. In distributed development it is 
    *                           possible that two module developers use the same 
    *                           data type code for two different data types. 
    *                           When an application developer tries to construct 
    *                           an application using these two modules, he would 
    *                           encounter conflict because the data type codes 
    *                           are the same. Instance of DataDescriptorManager 
    *                           class allows the application developer to remap 
    *                           one of the modules to use a different data type 
    *                           to avoid the conflict. Using this scenario the 
    *                           first module would use as its real data type the 
    *                           value of the desired one specified by the data 
    *                           descriptor while the second module would use the 
    *                           remapped value and instead of the desired value 
    *                           specified in thats module data descriptor the 
    *                           module will use the value specified in this 
    *                           mapping in the DataDescriptorManager instance. 
    * @param strDisplayableViewName - displayable name for the view represented 
    *                                 by this data descriptor. This name allows 
    *                                 different views of the same data objects 
    *                                 to be called differently to make them more 
    *                                 user friendly.
    * @param strViewName - logical name identifying the view for the specified 
    *                      data type object represented by this data descriptor. 
    *                      This name is independent of the language and 
    *                      therefore can be used to identify the view under any 
    *                      circumstances.
	 * @param setDataFields - enumeration representing data fields for the class
	 * @throws OSSException - an error has occurred
    */
   public DataDescriptorImpl(
      int        iDesiredDataType,
      String	  strDisplayableViewName,
      String	  strViewName,
		EnumSet<E> setDataFields
   ) throws OSSException
   {
      m_clsParentDescriptor = null;
      m_iDesiredDataType = iDesiredDataType;
      m_iDataType = DataObject.NO_DATA_TYPE;
      m_strDisplayableViewName = strDisplayableViewName;
      m_strViewName = strViewName;
      m_iDataTypeObject = null;
		m_setDataFields = setDataFields;

		// Here we need to pass value of the element representing single field
		if (setDataFields.isEmpty())
		{
			throw new OSSInvalidDataException("setDataFields must contain at least one field");
		}
	   m_mpDataFieldLengths = new EnumMap(setDataFields.iterator().next().getClass());
   }
   
   /**
    * Constructor.
    * 
    * @param clsParentDescriptor - class identifying parent data descriptor for
    *                              which this data descriptor provides just a 
    *                              different view for the same data objects. 
    *                              These two data descriptors will share the 
    *                              same desired and actual data types and 
    *                              describe the same underlying data but may 
    *                              describe them differently since they 
    *                              represent different views of the data.
    * @param strDisplayableViewName - displayable name for the view represented 
    *                                 by this data descriptor. This name allows 
    *                                 different views of the same data objects 
    *                                 to be called differently to make them more 
    *                                 user friendly.
    * @param strViewName - logical name identifying the view for the specified 
    *                      data type object represented by this data descriptor. 
    *                      This name is independent of the language and 
    *                      therefore can be used to identify the view under any 
    *                      circumstances.
	 * @param setDataFields - enumeration representing data fields for the class
    */
   public DataDescriptorImpl(
      Class<DataDescriptor> clsParentDescriptor,
      String                strDisplayableViewName,
      String                strViewName,
		EnumSet<E>				 setDataFields
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert clsParentDescriptor != null
                : "Parent data descriptor class cannot be null";
      }
      
      m_clsParentDescriptor = clsParentDescriptor;
      m_iDesiredDataType = DataObject.NO_DATA_TYPE;
      m_iDataType = DataObject.NO_DATA_TYPE;
      m_strDisplayableViewName = strDisplayableViewName;
      m_strViewName = strViewName;
      m_iDataTypeObject = null;
		m_setDataFields = setDataFields;
		// TODO: Review this: Should we be pulling the data fields from the parent descriptor?
		m_mpDataFieldLengths = new EnumMap(setDataFields.getClass());
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Class<DataDescriptor> getParentDescriptorClass()
   {
      return m_clsParentDescriptor;   
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int getDesiredDataType(
   )
   {
      return m_iDesiredDataType;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int getDataType(
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert m_iDataType != 0
                : "Real value of data type is not set yet";
      }
      return m_iDataType;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setDataType(
      int iDataType
   )
   {
      m_iDataType = iDataType;
      m_iDataTypeObject = null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Integer getDataTypeAsObject(
   )
   {
      // This doesn't have to be synchronized since it really doesn't matter
      // if we create two objects in case of concurrent access
      if (m_iDataTypeObject == null)
      {
          m_iDataTypeObject = getDataType();
      }
      
      return m_iDataTypeObject;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getDisplayableViewName()
   {
      return m_strDisplayableViewName;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getViewName()
   {
      return m_strViewName;
   }

   /**
    * {@inheritDoc}
    */
   @Override
	public Integer getFieldMaxLength(
	   E field
	)
	{
	   return m_mpDataFieldLengths.get(field);
	}
	
   /**
    * {@inheritDoc}
    */
   @Override
	public void setFieldMaxLength(
	   E		  field, 
		Integer iMaxLength)
	{
	   m_mpDataFieldLengths.put(field, iMaxLength);
	}

	/**
    * {@inheritDoc}
	 * 
	 * @param sb {@inheritDoc}
	 * @param ind {@inheritDoc}
    */
   @Override
   public void toString(
      StringBuilder sb,
      int           ind
   )
   {
      append(sb, ind + 0, "DataDescriptorImpl[", true);
      append(sb, ind + 1, "m_clsParentDescriptor = ", m_clsParentDescriptor);
      append(sb, ind + 1, "m_iDesiredDataType = ", m_iDesiredDataType);
      append(sb, ind + 1, "m_iDataType = ", m_iDataType);
      append(sb, ind + 1, "m_strViewName = ", m_strViewName);
      append(sb, ind + 1, "m_strDisplayableViewName = ", m_strDisplayableViewName);
      append(sb, ind + 1, "m_iDataTypeObject = ", m_iDataTypeObject);
      append(sb, ind + 1, "m_enumDataFields = ", m_setDataFields);
      super.toString(sb, ind + 1);
      append(sb, ind + 0, "]", true);
   }
}
