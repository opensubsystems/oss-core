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
import java.util.Date;
import org.opensubsystems.core.error.OSSException;

/**
 * Tests for BasicDataObjectTest and BasicDataObjectTestImpl classes.
 * 
 * @author bastafidli
 */
public class BasicDataObjectTest extends DataObjectTest
{
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Constructor for DataObjectTest.
	 * 
    * @param strName - name of the test
    * @throws OSSException - an error has occurred
    */
   public BasicDataObjectTest(
      String strName
   ) throws OSSException
   {
      super(strName, TestBasicDataObject.TestBasicDataObjectDataDescriptor.class,
           TestBasicDataObject.TestBasicDataObjectDataDescriptor.TEST_BASIC_DATA_TYPE_DESIRED_VALUE);
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Create test data from a given parameters.
    *
    * @param lId - Unique ID identifying this test data
    * @param lDomainId - Unique ID identifying domain for this test data
    * @param creationTimestamp - creation timestamp for this test data
    * @param strField1 - first field of the test data
    * @param strField2 - second field of the test data
    * @param strField3 - third field of the test data
    * @return TestBasicDataObject - data to use for testing
	 * @throws OSSException - an error has occurred
    */ 
   protected TestBasicDataObject createTestDataObject(
      long      lId,
      long      lDomainId,
      Timestamp creationTimestamp,
      String    strField1,
      String    strField2,
      String    strField3
   ) throws OSSException
   {
      return new TestBasicDataObject(lId, lDomainId, creationTimestamp, strField1, 
                                     strField2, strField3);
   }

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
   @Override
   protected DataObject createTestDataObject(
      long   lId,
      String strField1,
      String strField2,
      String strField3
   ) throws OSSException
   {
      return createTestDataObject(lId, DataObject.NEW_ID, null, strField1, 
                                  strField2, strField3);
   }

   // Tests ////////////////////////////////////////////////////////////////////

   /**
    * Test isFromPersistenceStore method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testIsFromPersistenceStore(
   ) throws Exception
   {
      Timestamp now = new Timestamp((new Date()).getTime());
		TestBasicDataObject data1 = createTestDataObject(1, 11, now, "1value1", 
                                                       "1value2", "1value3");
		TestBasicDataObject data2 = createTestDataObject(2, 22, now, "2value1", 
                                                       "2value2", "2value3");
		
		assertFalse("Persistance store flag doesn't match", data1.isFromPersistenceStore());
		assertFalse("Persistance store flag doesn't match", data2.isFromPersistenceStore());
   }


   /**
    * Test setFromPersistenceStore method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testSetFromPersistenceStore(
   ) throws Exception
   {
      Timestamp now = new Timestamp((new Date()).getTime());
		TestBasicDataObject data1 = createTestDataObject(1, 11, now, "1value1", 
                                                       "1value2", "1value3");
		TestBasicDataObject data2 = createTestDataObject(2, 22, now, "2value1", 
                                                       "2value2", "2value3");
		
		assertFalse("Persistance store flag doesn't match", data1.isFromPersistenceStore());
		assertFalse("Persistance store flag doesn't match", data2.isFromPersistenceStore());
      
      data1.setFromPersistenceStore();

		assertTrue("Persistance store flag doesn't match", data1.isFromPersistenceStore());
		assertFalse("Persistance store flag doesn't match", data2.isFromPersistenceStore());
   }

   /**
    * Test getDomainId method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetDomainId(
   ) throws Exception
   {
      Timestamp now = new Timestamp((new Date()).getTime());
		TestBasicDataObject data1 = createTestDataObject(1, 11, now, "1value1", 
                                                       "1value2", "1value3");
		TestBasicDataObject data2 = createTestDataObject(2, 22, now, "2value1", 
                                                       "2value2", "2value3");
		
		assertEquals("Domain id doesn't match", 11, data1.getDomainId());
		assertEquals("Domain id doesn't match", 22, data2.getDomainId());
   }

   /**
    * Test getCreationTimestamp method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetCreationTimestamp(
   ) throws Exception
   {
      Timestamp now = new Timestamp((new Date()).getTime());
      Timestamp later = new Timestamp((new Date()).getTime() + 1000);
		TestBasicDataObject data1 = createTestDataObject(1, 11, now, "1value1", 
                                                       "1value2", "1value3");
		TestBasicDataObject data2 = createTestDataObject(2, 22, later, "2value1", 
                                                       "2value2", "2value3");
		
      assertFalse("Two test timestamps cannot be the same", now.equals(later));
		assertEquals("Creation timestamp doesn't match", now, data1.getCreationTimestamp());
		assertEquals("Creation timestamp doesn't match", later, data2.getCreationTimestamp());
   }

   /**
    * Test setCreationTimestamp method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testSetCreationTimestamp(
   ) throws Exception
   {
      Timestamp now = new Timestamp((new Date()).getTime());
      Timestamp later = new Timestamp((new Date()).getTime() + 1000);
		TestBasicDataObject data1 = createTestDataObject(1, 11, null, "1value1", 
                                                       "1value2", "1value3");
		TestBasicDataObject data2 = createTestDataObject(2, 22, now, "2value1", 
                                                       "2value2", "2value3");
		
      assertFalse("Two test timestamps cannot be the same", now.equals(later));
		assertNull("Creation timestamp doesn't match", data1.getCreationTimestamp());
		assertEquals("Creation timestamp doesn't match", now, data2.getCreationTimestamp());

      data1.setCreationTimestamp(later);
		assertEquals("Creation timestamp doesn't match", later, data1.getCreationTimestamp());
      try
		{
         data1.setCreationTimestamp(now);
			fail("It should not be possible to change creation timestampe once it was set.");
		}
		catch (Throwable thr)
		{
			// Do nothing since this is expected java.lag.AssertionError
		}

      try
		{
         data2.setCreationTimestamp(later);
			fail("It should not be possible to change creation timestampe once it was set.");
		}
		catch (Throwable thr)
		{
			// Do nothing since this is expected java.lag.AssertionError
		}
   }
}
