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


import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.test.OSSTestCase;

/**
 * Tests for DataObject and DataObjectImpl classes.
 * 
 * @author bastafidli
 */
public class DataObjectTest extends OSSTestCase
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Data descriptor class for data object tested by this test case.
    */
   protected Class m_clsDataDescriptor;
   
   /**
    * Desired data type for data object tested by this test case.
    */
   protected int m_iDesiredDataType;
   
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Constructor for DataObjectTest.
	 * 
    * @param strName - name of the test
    * @throws OSSException - an error has occurred
    */
   public DataObjectTest(
      String strName
   ) throws OSSException
   {
      this(strName, TestDataObject.TestDataObjectDataDescriptor.class,
           TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE);
   }

   /**
    * Constructor for DataObjectTest.
	 * 
    * @param strName - name of the test
    * @param clsDataDescriptor - data descriptor for class created in createTestDataObject
    * @param iDesiredDataType - desired data type for class created in createTestDataObject
    * @throws OSSException - an error has occurred
    */
   public DataObjectTest(
      String strName,
      Class  clsDataDescriptor,
      int    iDesiredDataType
   ) throws OSSException
   {
      super(strName);
      
      m_clsDataDescriptor = clsDataDescriptor;
      // Don't try to derive this value using code we are trying to test, just
      // let the caller specify it for us
      m_iDesiredDataType = iDesiredDataType;
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Create test data from a given parameters.
    *
    * @param lId - Unique ID identifying this test data
    * @param strField1 - first field of the test data
    * @param strField2 - second field of the test data
    * @param strField3 - third field of the test data
    * @return DataObject - data to use for testing
	 * @throws OSSException - an error has occurred
    */ 
   protected DataObject createTestDataObject(
      long   lId,
      String strField1,
      String strField2,
      String strField3
   ) throws OSSException
   {
      return new TestDataObject(lId, strField1, strField2, strField3);
   }

   // Tests ////////////////////////////////////////////////////////////////////

   /**
    * Test isSame method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testIsSame(
   ) throws Exception
   {
		DataObject data1 = createTestDataObject(1, "1value1", "1value2", "1value3");
		DataObject data1b = createTestDataObject(1, "1bvalue1", "1bvalue2", "1bvalue3");
		DataObject data2 = createTestDataObject(2, "2value1", "2value2", "2value3");
		DataObject data2b = createTestDataObject(3, "2value1", "2value2", "2value3");
		
		assertFalse("Same ID, different values are NOT the same",
					   data1.isSame(data1b));
		assertFalse("Different ID, different values are NOT the same",
					   data1.isSame(data2));
		assertTrue("Different ID, same values are the same",
					  data2.isSame(data2b));
   }

   /**
    * Test getDataDescriptorClass method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDataDescriptorClass(
   ) throws Exception
   {
		DataObject data1 = createTestDataObject(1, "1value1", "1value2", "1value3");
		DataObject data2 = createTestDataObject(2, "2value1", "2value2", "2value3");
		
		// Multiple instances of the same class have to have the same data descriptor
		assertEquals("Data descriptor doesnt match the expected class",
					    m_clsDataDescriptor, data1.getDataDescriptorClass());
		assertEquals("Data descriptor doesnt match the expected class",
					    m_clsDataDescriptor, data2.getDataDescriptorClass());
   }

   /**
    * Test getDataDescriptor method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDataDescriptor(
   ) throws Exception
   {
		DataObject data1 = createTestDataObject(1, "1value1", "1value2", "1value3");
		DataObject data2 = createTestDataObject(2, "2value1", "2value2", "2value3");
		DataDescriptor descriptor1;
		DataDescriptor descriptor2;
		
		// Multiple instances of the same class have to have the same descriptor
		descriptor1 = data1.getDataDescriptor();
		assertEquals("Data descriptor doesnt match the expected class",
					    m_clsDataDescriptor, descriptor1.getClass());
		descriptor2 = data2.getDataDescriptor();
		assertEquals("Data descriptor doesnt match the expected class",
					    m_clsDataDescriptor, descriptor2.getClass());
		assertTrue("Data descriptor instance is shared between all instances of class",
					  descriptor1 == descriptor2);
   }

   /**
    * Test getDataType method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDataType(
   ) throws Exception
   {
		DataObject data1 = createTestDataObject(1, "1value1", "1value2", "1value3");
		DataObject data2 = createTestDataObject(2, "2value1", "2value2", "2value3");
		
		// Multiple instances of the same class have to have the same data type
		assertEquals("Data type doesn't match", m_iDesiredDataType, data1.getDataType());
		assertEquals("Data type doesn't match", m_iDesiredDataType, data2.getDataType());
   }

   /**
    * Test getDataType method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDataTypeWithRemappedValues(
   ) throws Exception
   {
		// Remap the desired data type to a new value
		Map<Integer, Integer> mpRemmapedDataTypes = new HashMap<>(1);
		DataDescriptorManager original;

		original = DataDescriptorManager.getManagerInstance();
		try
		{
			// We have to setup new manager instance since if some tests were already
			// run and data objects were created the manager doesn't allow remapping
			// With new instance we will circumvent the restriction
			DataDescriptorManager.setManagerInstance(new DataDescriptorManager());
			mpRemmapedDataTypes.put(m_iDesiredDataType, m_iDesiredDataType + 1);
			DataDescriptorManager.getManagerInstance().setDesiredDataTypeMap(mpRemmapedDataTypes);

			DataObject data1 = createTestDataObject(1, "1value1", "1value2", "1value3");
			DataObject data2 = createTestDataObject(2, "2value1", "2value2", "2value3");

			// Multiple instances of the same class have to have the same data type
			assertEquals("Data type doesn't match",m_iDesiredDataType + 1,
							 data1.getDataType());
			assertEquals("Data type doesn't match", m_iDesiredDataType + 1,
							 data2.getDataType());
		}
		finally
		{
			DataDescriptorManager.setManagerInstance(original);
		}
				  
   }

   /**
    * Test getDataTypeAsObject method 
    * 
    * @throws Exception - and error has occurred  
    */
   @SuppressWarnings({"NumberEquality"})
   public void testGetDataTypeAsObject(
   ) throws Exception
   {
		DataObject data1 = createTestDataObject(1, "1value1", "1value2", "1value3");
		DataObject data2 = createTestDataObject(2, "2value1", "2value2", "2value3");
		
		// Multiple instances of the same class have to have the same data type
		assertEquals("Data type doesn't match", (Object)m_iDesiredDataType,
						 (Object)data1.getDataTypeAsObject());
		assertEquals("Data type doesn't match", (Object)m_iDesiredDataType,
						 (Object)data1.getDataTypeAsObject());

		Integer iFirst = data1.getDataTypeAsObject();
		Integer iSecond = data1.getDataTypeAsObject();
	
		// We actually want to test using == so supress the warning
		assertTrue("Data type object representation is shared between invocations",
					  iFirst == iSecond);
	}

   /**
    * Test getId method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetId(
   ) throws Exception
   {
		DataObject data1 = createTestDataObject(1, "1value1", "1value2", "1value3");
		DataObject data2 = createTestDataObject(2, "2value1", "2value2", "2value3");
		
		assertEquals("Data id doesn't match", 1, data1.getId());
		assertEquals("Data Id doesn't match", 2, data2.getId());
   }

   /**
    * Test getIdAsObject method 
    * 
    * @throws Exception - and error has occurred  
    */
   @SuppressWarnings({"NumberEquality"})
   public void testIdAsObject(
   ) throws Exception
   {
		DataObject data1 = createTestDataObject(1, "1value1", "1value2", "1value3");
		DataObject data2 = createTestDataObject(2, "2value1", "2value2", "2value3");
		
		// Multiple instances of the same class have to have the same data type
		// Have to use new Long otherwise the comparison fails
		assertEquals("Data id doesn't match", (Long)1l, (Object)data1.getIdAsObject());
		assertEquals("Data id doesn't match", (Long)2l, (Object)data2.getIdAsObject());

		Long lFirst = data1.getIdAsObject();
		Long lSecond = data1.getIdAsObject();
	
		// We actually want to test using == so supress the warning
		assertTrue("Data id object representation is shared between invocations",
					  lFirst == lSecond);
	}


   /**
    * Test setId method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testSetId(
   ) throws Exception
   {
		DataObject data1 = createTestDataObject(DataObject.NEW_ID, "1value1", "1value2", 
                                          "1value3");
		DataObject data2 = createTestDataObject(1, "1value1", "1value2", "1value3");
		
      data1.setId(11);
		assertEquals("Data id doesn't match", 11, data1.getId());
		
		try
		{
			data2.setId(11);
			fail("It should not be possible to change data object id once it was set.");
		}
		catch (Throwable thr)
		{
			// Do nothing since this is expected java.lag.AssertionError
		}
   }
}
