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
}
