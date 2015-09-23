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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.opensubsystems.core.data.TestDataObject.TestDataObjectDataDescriptor;
import org.opensubsystems.core.util.test.OSSTestCase;

/**
 * Tests for DataDescriptor and DataDescriptorImpl classes.
 * 
 * @author bastafidli
 */
public class DataDescriptorTest extends OSSTestCase
{
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Constructor for DataDescriptorTest.
	 * 
    * @param strName - name of the test
    */
   public DataDescriptorTest(
      String strName
   )
   {
      super(strName);
   }

   // Tests ////////////////////////////////////////////////////////////////////
   
   /**
    * Test getDataType method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDataType(
   ) throws Exception
   {
		DataDescriptor descriptor1;
		
      // First create simple instance of the data descriptor
      descriptor1 = new TestDataObjectDataDescriptor();
      
      try
      {
         descriptor1.getDataType();
         fail("It should not be possible to get the data type since data descriptor"
              + " has to be first registered with DataDescriptorManager to get"
              + " assigned real data type value based on the desired data type value.");
      }
      catch (Throwable thr)
      {
			// Do nothing since this is expected java.lag.AssertionError
      }

      // Data descriptors should be really accessed using DataDescriptorManager
      // which properly initializes them
      descriptor1 = DataDescriptorManager.getInstance(TestDataObjectDataDescriptor.class);
      
      assertNotNull("DataDescriptorManager must always be able to create instance"
                    + " of data descriptor", descriptor1);
      
      // When properly initialized by DataDescriptorManager we should be able 
      // to get the data type
		assertEquals("Data type doesn't match",
					    TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE,
						 descriptor1.getDataType());
      
      // Another way how to register the descriptor with DataDescriptorManager
      // is to create data type that uses this descriptor
		TestDataObject data = new TestDataObject();
		DataDescriptor descriptor2;

		assertEquals("Data descriptor doesnt match the expected class",
					    TestDataObject.TestDataObjectDataDescriptor.class,
						 data.getDataDescriptorClass());
      
		descriptor2 = data.getDataDescriptor();
      
      // This also implies that the data type is of course the same
		assertTrue("Data descriptor instance is shared between all instances of class",
					  descriptor1 == descriptor2);      
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

         DataDescriptor descriptor1;
      
         // Data descriptors should be really accessed using DataDescriptorManager
         // which properly initializes them
         descriptor1 = DataDescriptorManager.getInstance(TestDataObjectDataDescriptor.class);

         assertNotNull("DataDescriptorManager must always be able to create instance"
                       + " of data descriptor", descriptor1);

         // When properly initialized by DataDescriptorManager we should be able 
         // to get the data type
         assertEquals("Data type doesn't match",
                      TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE + 1,
                      descriptor1.getDataType());

         // Another way how to register the descriptor with DataDescriptorManager
         // is to create data type that uses this descriptor
         TestDataObject data = new TestDataObject();
         DataDescriptor descriptor2;

         assertEquals("Data descriptor doesnt match the expected class",
                      TestDataObject.TestDataObjectDataDescriptor.class,
                      data.getDataDescriptorClass());

         descriptor2 = data.getDataDescriptor();

         // This also implies that the data type is of course the same
         assertTrue("Data descriptor instance is shared between all instances of class",
                    descriptor1 == descriptor2);      
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
		DataDescriptor descriptor1 = DataDescriptorManager.getInstance(
                                      TestDataObjectDataDescriptor.class);
		
		// Multiple instances of the same class have to have the same data type
		assertEquals("Data type doesn't match",
					    (Object)TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE,
						 (Object)descriptor1.getDataTypeAsObject());

		Integer iFirst = descriptor1.getDataTypeAsObject();
		Integer iSecond = descriptor1.getDataTypeAsObject();
	
		// We actually want to test using ==
		assertTrue("Data type object representation is shared between invocations",
					  iFirst == iSecond);
	}

   /**
    * Test getDesiredDataType method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDesiredDataType(
   ) throws Exception
   {
		DataDescriptor descriptor1;
		
      // First create simple instance of the data descriptor, since we should
      // be able to get desired data type this way just fine
      descriptor1 = new TestDataObjectDataDescriptor();
      
		assertEquals("Desired data type doesn't match",
					    TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE,
						 descriptor1.getDesiredDataType());

      // Data descriptors should be really accessed using DataDescriptorManager
      // which properly initializes them, we shouldbe able to still get desired 
      // data type this way just fine
      descriptor1 = DataDescriptorManager.getInstance(TestDataObjectDataDescriptor.class);
      
      assertNotNull("DataDescriptorManager must always be able to create instance"
                    + " of data descriptor", descriptor1);
      
      // When properly initialized by DataDescriptorManager we should be able 
      // to get the data type
		assertEquals("Desired data type doesn't match",
					    TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE,
						 descriptor1.getDesiredDataType());
      
      // Another way how to register the descriptor with DataDescriptorManager
      // is to create data type that uses this descriptor,  we should be able 
      // to get the data type
		TestDataObject data = new TestDataObject();
		DataDescriptor descriptor2;

		assertEquals("Data descriptor doesnt match the expected class",
					    TestDataObject.TestDataObjectDataDescriptor.class,
						 data.getDataDescriptorClass());
      
		descriptor2 = data.getDataDescriptor();
      
      // This also implies that the data type is of course the same
		assertTrue("Data descriptor instance is shared between all instances of class",
					  descriptor1 == descriptor2);      
   }

   /**
    * Test getDesiredDataType method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDesiredDataTypeWithRemappedValues(
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

         DataDescriptor descriptor1;
      
         // Data descriptors should be really accessed using DataDescriptorManager
         // which properly initializes them
         descriptor1 = DataDescriptorManager.getInstance(TestDataObjectDataDescriptor.class);

         assertNotNull("DataDescriptorManager must always be able to create instance"
                       + " of data descriptor", descriptor1);

         // When properly initialized by DataDescriptorManager we should be able 
         // to get the desirec data type as the original value
         assertEquals("Desired data type doesn't match",
                      TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_DESIRED_VALUE,
                      descriptor1.getDesiredDataType());

         // Another way how to register the descriptor with DataDescriptorManager
         // is to create data type that uses this descriptor
         TestDataObject data = new TestDataObject();
         DataDescriptor descriptor2;

         assertEquals("Data descriptor doesnt match the expected class",
                      TestDataObject.TestDataObjectDataDescriptor.class,
                      data.getDataDescriptorClass());

         descriptor2 = data.getDataDescriptor();

         // This also implies that the data type is of course the same
         assertTrue("Data descriptor instance is shared between all instances of class",
                    descriptor1 == descriptor2);      
		}
		finally
		{
			DataDescriptorManager.setManagerInstance(original);
		}
   }

   /**
    * Test getDisplayableViewName method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDisplayableViewName(
   ) throws Exception
   {
		DataDescriptor descriptor1;
		
      // First create simple instance of the data descriptor, since we should
      // be able to get displayable view name this way just fine
      descriptor1 = new TestDataObjectDataDescriptor();
      
		assertEquals("Data displayable view name doesn't match",
					    TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_NAME,
						 descriptor1.getDisplayableViewName());

      // Data descriptors should be really accessed using DataDescriptorManager
      // which properly initializes them, we shouldbe able to still get desired 
      // data type this way just fine
      descriptor1 = DataDescriptorManager.getInstance(TestDataObjectDataDescriptor.class);
      
      assertNotNull("DataDescriptorManager must always be able to create instance"
                    + " of data descriptor", descriptor1);
      
      // When properly initialized by DataDescriptorManager we should be able 
      // to get the data type
		assertEquals("Data displayable view name doesn't match",
					    TestDataObject.TestDataObjectDataDescriptor.TEST_DATA_TYPE_NAME,
						 descriptor1.getDisplayableViewName());
      
      // Another way how to register the descriptor with DataDescriptorManager
      // is to create data type that uses this descriptor,  we should be able 
      // to get the data type
		TestDataObject data = new TestDataObject();
		DataDescriptor descriptor2;

		assertEquals("Data descriptor doesnt match the expected class",
					    TestDataObject.TestDataObjectDataDescriptor.class,
						 data.getDataDescriptorClass());
      
		descriptor2 = data.getDataDescriptor();
      
      // This also implies that the data type is of course the same
		assertTrue("Data descriptor instance is shared between all instances of class",
					  descriptor1 == descriptor2);      
   }

   /**
    * Test getViewName method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetViewName(
   ) throws Exception
   {
		DataDescriptor descriptor1;
		
      // First create simple instance of the data descriptor, since we should
      // be able to get displayable view name this way just fine
      descriptor1 = new TestDataObjectDataDescriptor();
      
		assertEquals("Data view name doesn't match",
					    TestDataObject.TestDataObjectDataDescriptor.TEST_TYPE_VIEW,
						 descriptor1.getViewName());

      // Data descriptors should be really accessed using DataDescriptorManager
      // which properly initializes them, we shouldbe able to still get desired 
      // data type this way just fine
      descriptor1 = DataDescriptorManager.getInstance(TestDataObjectDataDescriptor.class);
      
      assertNotNull("DataDescriptorManager must always be able to create instance"
                    + " of data descriptor", descriptor1);
      
      // When properly initialized by DataDescriptorManager we should be able 
      // to get the data type
		assertEquals("Data view name doesn't match",
					    TestDataObject.TestDataObjectDataDescriptor.TEST_TYPE_VIEW,
						 descriptor1.getViewName());
      
      // Another way how to register the descriptor with DataDescriptorManager
      // is to create data type that uses this descriptor,  we should be able 
      // to get the data type
		TestDataObject data = new TestDataObject();
		DataDescriptor descriptor2;

		assertEquals("Data descriptor doesnt match the expected class",
					    TestDataObject.TestDataObjectDataDescriptor.class,
						 data.getDataDescriptorClass());
      
		descriptor2 = data.getDataDescriptor();
      
      // This also implies that the data type is of course the same
		assertTrue("Data descriptor instance is shared between all instances of class",
					  descriptor1 == descriptor2);      
   }

   /**
    * Test getFields method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetFields(
   ) throws Exception
   {
		DataDescriptor descriptor1;
      EnumSet        fields;

      // First create simple instance of the data descriptor, since we should
      // be able to get displayable view name this way just fine
      descriptor1 = new TestDataObjectDataDescriptor();

      fields = descriptor1.getFields();
      
      assertNotNull("Data Descriptor has to have come fields defined", fields);
      assertFalse("Data Descriptor has to have come fields defined", fields.isEmpty());
      assertEquals("Test data object has to have expected set of fields", 4,
                   fields.size());
      assertTrue("Data Descriptor has to have the specified set of fiels", 
                 fields.equals(EnumSet.allOf(
                    TestDataObjectDataDescriptor.TestDataFields.class)));
      
      // Data descriptors should be really accessed using DataDescriptorManager
      // which properly initializes them, it should not change though the 
      // fields the data descriptor exposes
      descriptor1 = DataDescriptorManager.getInstance(TestDataObjectDataDescriptor.class);
      
      assertNotNull("DataDescriptorManager must always be able to create instance"
                    + " of data descriptor", descriptor1);
      
      assertNotNull("Data Descriptor has to have come fields defined", fields);
      assertFalse("Data Descriptor has to have come fields defined", fields.isEmpty());
      assertEquals("Test data object has to have expected set of fields", 4,
                   fields.size());
      assertTrue("Data Descriptor has to have the specified set of fiels", 
                 fields.equals(EnumSet.allOf(
                    TestDataObjectDataDescriptor.TestDataFields.class)));
      
      // Another way how to register the descriptor with DataDescriptorManager
      // is to create data type that uses this descriptor,  we should be able 
      // to get the data type
		TestDataObject data = new TestDataObject();
		DataDescriptor descriptor2;

		assertEquals("Data descriptor doesnt match the expected class",
					    TestDataObject.TestDataObjectDataDescriptor.class,
						 data.getDataDescriptorClass());
      
		descriptor2 = data.getDataDescriptor();
      
      // This also implies that the data type is of course the same
		assertTrue("Data descriptor instance is shared between all instances of class",
					  descriptor1 == descriptor2);      
   }

   /**
    * Test getFieldMaxLength method by testing the default (0) value returned.
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetFieldMaxLengthWithDefaultValue(
   ) throws Exception
   {
		DataDescriptor descriptor1;
      EnumSet        fields;
		
      // First create simple instance of the data descriptor, since we should
      // be able to get displayable view name this way just fine
      descriptor1 = new TestDataObjectDataDescriptor();

      fields = descriptor1.getFields();
      
      assertNotNull("Data Descriptor has to have come fields defined", fields);
      assertFalse("Data Descriptor has to have come fields defined", fields.isEmpty());
      assertEquals("Test data object has to have expected set of fields", 4,
                   fields.size());
      assertTrue("Data Descriptor has to have the specified set of fiels", 
                 fields.equals(EnumSet.allOf(
                    TestDataObjectDataDescriptor.TestDataFields.class)));
      
      for (Enum field : TestDataObjectDataDescriptor.TestDataFields.values())
      {
         assertEquals("When created directly all fields have 0 max length",
                      (Integer)0, descriptor1.getFieldMaxLength(field));
      }
      
      // Data descriptors should be really accessed using DataDescriptorManager
      // which properly initializes them, but that should not influence the data 
      // field lengths
      descriptor1 = DataDescriptorManager.getInstance(TestDataObjectDataDescriptor.class);
      
      assertNotNull("DataDescriptorManager must always be able to create instance"
                    + " of data descriptor", descriptor1);
      assertNotNull("Data Descriptor has to have come fields defined", fields);
      assertFalse("Data Descriptor has to have come fields defined", fields.isEmpty());
      assertEquals("Test data object has to have expected set of fields", 4,
                   fields.size());
      assertTrue("Data Descriptor has to have the specified set of fiels", 
                 fields.equals(EnumSet.allOf(
                    TestDataObjectDataDescriptor.TestDataFields.class)));
      
      for (Enum field : TestDataObjectDataDescriptor.TestDataFields.values())
      {
         assertEquals("When created directly all fields have 0 max length",
                      (Integer)0, descriptor1.getFieldMaxLength(field));
      }
      
      // Another way how to register the descriptor with DataDescriptorManager
      // is to create data type that uses this descriptor,  we should be able 
      // to get the data type
		TestDataObject data = new TestDataObject();
		DataDescriptor descriptor2;

		assertEquals("Data descriptor doesnt match the expected class",
					    TestDataObject.TestDataObjectDataDescriptor.class,
						 data.getDataDescriptorClass());
      
		descriptor2 = data.getDataDescriptor();
      
      // This also implies that the data type is of course the same
		assertTrue("Data descriptor instance is shared between all instances of class",
					  descriptor1 == descriptor2);      
   }
}
