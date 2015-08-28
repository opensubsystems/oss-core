/*
 * Copyright (C) 2015 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.sql.Timestamp;
import java.util.EnumSet;

import org.opensubsystems.core.data.impl.DataDescriptorImpl;
import org.opensubsystems.core.data.impl.ModifiableDataObjectImpl;
import org.opensubsystems.core.error.OSSException;

/**
 * Data object to test ModifiableDataObjectImpl
 *
 * @author bastafidli
 */
public class TestModifiableDataObject extends ModifiableDataObjectImpl
{
	// Inner classes ////////////////////////////////////////////////////////////
	
	public static class TestModifiableDataObjectDataDescriptor 
	   extends DataDescriptorImpl<TestModifiableDataObjectDataDescriptor.TestDataFields>
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
		protected static final int TEST_DATA_TYPE_DESIRED_VALUE = 2345;

		/**
		 * Displayable name for specified data type code object.
		 * Protected since it can be customized and therefore code should use method
		 * exposing it rather than the constants.
		 */
		protected static final String TEST_DATA_TYPE_NAME = "Test Modifiable Data Object";

		/**
		 * Logical name identifying the default view for the specified data
		 * type object. Data type objects can be displayed in multiple various ways
		 * called views. This constant identifies the default one. This constant
		 * should have a value, that can be used to construct various identifiers,
		 * which means no special characters, no spaces, etc.
		 * Protected since it can be customized and therefore code should use method
		 * exposing it rather than the constants.
		 */
		protected static final String TEST_TYPE_VIEW = "testmodifiabledataobject";

		/**
		 * Definition of all fields that represent meaningful data for users.
		 * The order is important since it is used to retrieve all data from the
		 * persistence store efficiently so do not modify it unless you make
		 * changes to other places as well.
		 * Protected since derived classes can add more attributes and therefore code
		 * should use method exposing it rather than the constants.
		 */
		public enum TestDataFields {
			TEST_ID(TEST_DATA_TYPE_DESIRED_VALUE + 1),
			TEST_FIELD1(TEST_DATA_TYPE_DESIRED_VALUE + 2),
			TEST_FIELD2(TEST_DATA_TYPE_DESIRED_VALUE + 3),
			TEST_FIELD3(TEST_DATA_TYPE_DESIRED_VALUE + 4),
			TEST_CREATION_DATE(TEST_DATA_TYPE_DESIRED_VALUE + 5),
			TEST_MODIFICATION_DATE(TEST_DATA_TYPE_DESIRED_VALUE + 6),
			;

			private final int iValue;
			TestDataFields(int id) { this.iValue = id; }
			public int getValue() { return iValue; }
		}

      // Constructors //////////////////////////////////////////////////////////
      
		public TestModifiableDataObjectDataDescriptor(
		   int	  iDesiredDataType,
			String  strDisplayableViewName,
			String  strViewName,
			EnumSet setDataFields)
		{
			super(TEST_DATA_TYPE_DESIRED_VALUE, TEST_DATA_TYPE_NAME, TEST_TYPE_VIEW, 
					EnumSet.allOf(TestDataFields.class));
		}
	}

	// Constants ////////////////////////////////////////////////////////////////

	// Cached values ////////////////////////////////////////////////////////////
	
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * First test field
    */
   protected String m_strField1;
   
   /**
    * Second test field
    */
   protected String m_strField2;
   
   /**
    * Third test field
    */
   protected String m_strField3;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Empty test data initialized to default parameters
	 * 
	 * @throws OSSException - an error has occurred
    */
   public TestModifiableDataObject(
   ) throws OSSException
   {
      this(DataObject.NEW_ID, DataObject.NEW_ID, "", "", "", null, null);
   }
   
   /**
    * Empty test data for a specified domain initialized to default parameters
    *
    * @param lDomainId - Id of the domain this test data belongs to 
	 * @throws OSSException - an error has occurred
    */
   public TestModifiableDataObject(
      long lDomainId
   ) throws OSSException
   {
      this(DataObject.NEW_ID, lDomainId, "", "", "", null, null);
   }
   
   /**
    * Create test data from a given parameters.
    *
    * @param lId - Unique ID identifying this test data
    * @param lDomainId - Id of the domain this test data belongs to 
    * @param strField1 - first field of the test data
    * @param strField2 - second field of the test data
    * @param strField3 - third field of the test data
    * @param creationTimestamp - Timestamp when the test data was created
    * @param modificationTimestamp - Timestamp when the test data was last time modified
	 * @throws OSSException - an error has occurred
    */ 
   public TestModifiableDataObject(
      long      lId,
      long      lDomainId,
      String    strField1,
      String    strField2,
      String    strField3,
      Timestamp creationTimestamp,
      Timestamp modificationTimestamp
   ) throws OSSException
   {
      super(lId, TestModifiableDataObjectDataDescriptor.class, lDomainId, creationTimestamp, 
			   modificationTimestamp);
      
      m_strField1   = strField1;
      m_strField2  = strField2;
      m_strField3 = strField3;
   }
   
   // Public methods ///////////////////////////////////////////////////////////
   
   /**
    * First test data field.
    *
    * @return String
    */
   public String getField1(
   )
   {
      return m_strField1;
   }
   
   /**
    * Second test data field.
    *
    * @return String
    */
   public String getField2(
   )
   {
      return m_strField2;
   }
   
   /**
    * Third test data field.
    *
    * @return String
    */
   public String getField3(
   )
   {
      return m_strField3;
   }
   
   /**
    * {@inheritDoc}
	 * 
	 * @param oObject {@inheritDoc}
	 * @return {@inheritDoc}
    */
	@Override
   public boolean isSame(
      Object oObject
   )
   {
      boolean bReturn = false;
      TestModifiableDataObject    data;

      if (oObject == this)
      {
         bReturn = true;
      }
      else
      {
         if (oObject != null && oObject instanceof TestModifiableDataObject)
         {
            data = (TestModifiableDataObject) oObject;
            bReturn = ((data.getField1() == null && m_strField1 == null)
                           || data.getField1().equals(m_strField1))
                     && ((data.getField2() == null && m_strField2 == null)
                           || data.getField2().equals(m_strField2))
                     && ((data.getField3() == null && m_strField3 == null)
                           || data.getField3().equals(m_strField3));
         }
      }
      return bReturn;
   }
}
