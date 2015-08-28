/*
 * Copyright (C) 2014 - 2015 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.util.EnumSet;

/**
 * Data descriptor that can be used when no more specific data descriptor exists.
 *
 * @author bastafidli
 */
public class UndefinedDataDescriptor extends DataDescriptorImpl
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Desired value for the data type code. This can be reconfigured if there 
    * are multiple data objects which desire the same value. The rest of the 
    * constants in this class can safely use the desired value since they are 
    * valid only in the context of the data type and therefore it doesn't matter
    * what the real value is. 
    * Protected since it can be reconfigured by the framework and the real value
    * can be different.
    */
   protected static final int UNDEFINED_DATA_TYPE_DESIRED_VALUE = 1;

   /**
    * Displayable name for specified data type code object. 
    * Protected since it can be customized and therefore code should use method
    * exposing it rather than the constants.
    */
   protected static final String UNDEFINED_DATA_TYPE_NAME = "Undefined Data Type";

   /**
    * Logical name identifying the default view for the specified data 
    * type object. Data type objects can be displayed in multiple various ways
    * called views. This constant identifies the default one. This constant
    * should have a value, that can be used to construct various identifiers, 
    * which means no special characters, no spaces, etc.
    * Protected since it can be customized and therefore code should use method
    * exposing it rather than the constants.
    */
   protected static final String UNDEFINED_TYPE_VIEW = "undefineddatatype";

	/**
	 * Definition of all fields that represent meaningful data for users.
	 * The order is important since it is used to retrieve all data from the
	 * persistence store efficiently so do not modify it unless you make
	 * changes to other places as well.
	 * Protected since derived classes can add more attributes and therefore code
	 * should use method exposing it rather than the constants.
	 */
	public enum UndefinedFields {
		// Every object has at least ID
		UNDEFINED_DATA_TYPE_DESIRED_VALUE_DATA_ID(UNDEFINED_DATA_TYPE_DESIRED_VALUE + 1),
		;

		private final int iValue;
		UndefinedFields(int id) { this.iValue = id; }
		public int getValue() { return iValue; }
	}

	// Attributes ////////////////////////////////////////////////////////////

   // Constructors //////////////////////////////////////////////////////////

   /**
    * Default constructor.
    */
   public UndefinedDataDescriptor()
   {
      super(UNDEFINED_DATA_TYPE_DESIRED_VALUE, 
            UNDEFINED_DATA_TYPE_NAME, 
            UNDEFINED_TYPE_VIEW,
				EnumSet.allOf(UndefinedFields.class));
   }

   // Logic /////////////////////////////////////////////////////////////////

}
