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
import org.opensubsystems.core.util.test.OSSTestCase;

/**
 * Tests for DataObject class.
 * 
 * @author bastafidli
 */
public class DataObjectTest extends OSSTestCase
{
   // Tests ////////////////////////////////////////////////////////////////////

   /**
    * Constructor for DataObjectTest.
	 * 
    * @param strName - name of the test
    */
   public DataObjectTest(
      String strName
   )
   {
      super(strName);
   }

   /**
    * Set up environment for the test case.
    * 
    * @throws Exception - an error has occurred during setting up test
    */
	@Override
   protected void setUp(
   ) throws Exception
   {
      super.setUp();
   }

   /**
    * Restore original environment after the test case.
    * 
    * @throws Exception - an error has occurred during tearing down up test
    */
	@Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
   }

   /**
    * Test isSame method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testIsSame(
   ) throws Exception
   {
		TestDataObject data1 = new TestDataObject(1, "1value1", "1value2", "1value3");
		TestDataObject data1b = new TestDataObject(1, "1bvalue1", "1bvalue2", "1bvalue3");
		TestDataObject data2 = new TestDataObject(2, "2value1", "2value2", "2value3");
		TestDataObject data2b = new TestDataObject(3, "2value1", "2value2", "2value3");
		
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
		TestDataObject data1 = new TestDataObject(1, "1value1", "1value2", "1value3");
		TestDataObject data2 = new TestDataObject(2, "2value1", "2value2", "2value3");
		
		// Multiple instances of the same class have to have the same data descriptor
		assertEquals("Data descriptor doesnt match the expected class",
					    TestDataObject.TestDataObjectDataDescriptor.class,
						 data1.getDataDescriptorClass());
		assertEquals("Data descriptor doesnt match the expected class",
					    TestDataObject.TestDataObjectDataDescriptor.class,
						 data2.getDataDescriptorClass());
   }

   /**
    * Test getDataDescriptor method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDataDescriptor(
   ) throws Exception
   {
		TestDataObject data1 = new TestDataObject(1, "1value1", "1value2", "1value3");
		TestDataObject data2 = new TestDataObject(2, "2value1", "2value2", "2value3");
		DataDescriptor descriptor1;
		DataDescriptor descriptor2;
		
		// Multiple instances of the same class have to have the same descriptor
		descriptor1 = data1.getDataDescriptor();
		assertEquals("Data descriptor doesnt match the expected class",
					    TestDataObject.TestDataObjectDataDescriptor.class,
						 descriptor1.getClass());
		descriptor2 = data2.getDataDescriptor();
		assertEquals("Data descriptor doesnt match the expected class",
					    TestDataObject.TestDataObjectDataDescriptor.class,
						 descriptor2.getClass());
		assertTrue("Data descriptor instance is shared between all instances of class",
					  descriptor1.getClass() == descriptor2.getClass());
   }

   /**
    * Test getDataType method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDataType(
   ) throws Exception
   {
		TestDataObject data1 = new TestDataObject(1, "1value1", "1value2", "1value3");
		TestDataObject data2 = new TestDataObject(2, "2value1", "2value2", "2value3");
		
		// Multiple instances of the same class have to have the same data type
		assertEquals("Data type doesn't match",
					    TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE,
						 data1.getDataType());
		assertEquals("Data type doesn't match",
					    TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE,
						 data2.getDataType());
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
			mpRemmapedDataTypes.put(
				TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE,
				TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE + 1);
			DataDescriptorManager.getManagerInstance().setDesiredDataTypeMap(mpRemmapedDataTypes);

			TestDataObject data1 = new TestDataObject(1, "1value1", "1value2", "1value3");
			TestDataObject data2 = new TestDataObject(2, "2value1", "2value2", "2value3");

			// Multiple instances of the same class have to have the same data type
			assertEquals("Data type doesn't match",
							 TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE + 1,
							 data1.getDataType());
			assertEquals("Data type doesn't match",
							 TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE + 1,
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
   public void testGetDataTypeAsObject(
   ) throws Exception
   {
		TestDataObject data1 = new TestDataObject(1, "1value1", "1value2", "1value3");
		TestDataObject data2 = new TestDataObject(2, "2value1", "2value2", "2value3");
		
		// Multiple instances of the same class have to have the same data type
		assertEquals("Data type doesn't match",
					    (Object)TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE,
						 (Object)data1.getDataTypeAsObject());
		assertEquals("Data type doesn't match",
					    (Object)TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE,
						 (Object)data1.getDataTypeAsObject());

		Integer iFirst = data1.getDataTypeAsObject();
		Integer iSecond = data1.getDataTypeAsObject();
	
		// We actually want to test using ==
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
		TestDataObject data1 = new TestDataObject(1, "1value1", "1value2", "1value3");
		TestDataObject data2 = new TestDataObject(2, "2value1", "2value2", "2value3");
		
		assertEquals("Data id doesn't match", 1, data1.getId());
		assertEquals("Data Id doesn't match", 2, data2.getId());
   }

   /**
    * Test getIdAsObject method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testIdAsObject(
   ) throws Exception
   {
		TestDataObject data1 = new TestDataObject(1, "1value1", "1value2", "1value3");
		TestDataObject data2 = new TestDataObject(2, "2value1", "2value2", "2value3");
		
		// Multiple instances of the same class have to have the same data type
		// Have to use new Long otherwise the comparison fails
		assertEquals("Data id doesn't match", new Long(1), (Object)data1.getIdAsObject());
		assertEquals("Data id doesn't match", new Long(2), (Object)data2.getIdAsObject());

		Long lFirst = data1.getIdAsObject();
		Long lSecond = data1.getIdAsObject();
	
		// We actually want to test using ==
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
		TestDataObject data1 = new TestDataObject();
		TestDataObject data2 = new TestDataObject(1, "1value1", "1value2", "1value3");
		
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
