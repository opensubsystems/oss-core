/*
 * Copyright (C) 2016 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 * Tests for ModifiableDataObject and ModifiableDataObjectImpl classes.
 * 
 * @author bastafidli
 */
public class ModifiableDataObjectTest extends BasicDataObjectTest
{
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Constructor for ModifiableDataObjectTest.
	 * 
    * @param strName - name of the test
    * @throws OSSException - an error has occurred
    */
   public ModifiableDataObjectTest(
      String strName
   ) throws OSSException
   {
      super(strName, TestModifiableDataObject.TestModifiableDataObjectDataDescriptor.class,
           TestModifiableDataObject.TestModifiableDataObjectDataDescriptor.TEST_MODIFIABLE_DATA_TYPE_DESIRED_VALUE);
   }


   /**
    * Constructor for ModifiableDataObjectTest.
	 * 
    * @param strName - name of the test
    * @param clsDataDescriptor - data descriptor for class created in createTestDataObject
    * @param iDesiredDataType - desired data type for class created in createTestDataObject
    * @throws OSSException - an error has occurred
    */
   public ModifiableDataObjectTest(
      String strName,
      Class  clsDataDescriptor,
      int    iDesiredDataType
   ) throws OSSException
   {
      super(strName, clsDataDescriptor, iDesiredDataType);
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Create test data from a given parameters.
    *
    * @param lId - Unique ID identifying this test data
    * @param lDomainId - Unique ID identifying domain for this test data
    * @param creationTimestamp - creation timestamp for this test data
    * @param modificationTimestamp - modification timestamp for this test data
    * @param strField1 - first field of the test data
    * @param strField2 - second field of the test data
    * @param strField3 - third field of the test data
    * @return ModifiableDataObject - data to use for testing
	 * @throws OSSException - an error has occurred
    */ 
   protected ModifiableDataObject createTestDataObject(
      long      lId,
      long      lDomainId,
      Timestamp creationTimestamp,
      Timestamp modificationTimestamp,
      String    strField1,
      String    strField2,
      String    strField3
   ) throws OSSException
   {
      return new TestModifiableDataObject(lId, lDomainId, creationTimestamp, 
                                          modificationTimestamp, strField1, 
                                          strField2, strField3);
   }

   /**
    * Create test data from a given parameters.
    *
    * @param lId - Unique ID identifying this test data
    * @param lDomainId - Unique ID identifying domain for this test data
    * @param creationTimestamp - creation timestamp for this test data
    * @param strField1 - first field of the test data
    * @param strField2 - second field of the test data
    * @param strField3 - third field of the test data
    * @return BasicDataObject - data to use for testing
	 * @throws OSSException - an error has occurred
    */ 
   @Override
   protected BasicDataObject createTestDataObject(
      long      lId,
      long      lDomainId,
      Timestamp creationTimestamp,
      String    strField1,
      String    strField2,
      String    strField3
   ) throws OSSException
   {
      return createTestDataObject(lId, lDomainId, creationTimestamp, null, 
                                  strField1, strField2, strField3);
   }

   // Tests ////////////////////////////////////////////////////////////////////


   /**
    * Test getModificationTimestamp method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testGetModificationTimestamp(
   ) throws Exception
   {
      Timestamp now = new Timestamp((new Date()).getTime());
      Timestamp later = new Timestamp((new Date()).getTime() + 1000);
      Timestamp evenlater = new Timestamp((new Date()).getTime() + 2000);
		ModifiableDataObject data1 = createTestDataObject(1, 11, now, now, "1value1", 
                                                       "1value2", "1value3");
		ModifiableDataObject data2 = createTestDataObject(2, 22, later, evenlater, "2value1", 
                                                       "2value2", "2value3");
		
      assertFalse("Two test timestamps cannot be the same", now.equals(later));
      assertFalse("Two test timestamps cannot be the same", later.equals(evenlater));
      assertFalse("Two test timestamps cannot be the same", now.equals(evenlater));
		assertEquals("Modification timestamp doesn't match", now, data1.getModificationTimestamp());
		assertEquals("Modification timestamp doesn't match", evenlater, data2.getModificationTimestamp());
   }

   /**
    * Test setModificationTimestamp method 
    * 
    * @throws Exception - and error has occurred  
    */
   public void testSetModificationTimestamp(
   ) throws Exception
   {
      Timestamp now = new Timestamp((new Date()).getTime());
      Timestamp later = new Timestamp((new Date()).getTime() + 1000);
      Timestamp evenlater = new Timestamp((new Date()).getTime() + 2000);
		ModifiableDataObject data1 = createTestDataObject(1, 11, null, null, "1value1", 
                                                       "1value2", "1value3");
		ModifiableDataObject data2 = createTestDataObject(2, 22, now, later, "2value1", 
                                                       "2value2", "2value3");
		
      assertFalse("Two test timestamps cannot be the same", now.equals(later));
		assertNull("Modification timestamp doesn't match", data1.getModificationTimestamp());
		assertEquals("Modification timestamp doesn't match", later, data2.getModificationTimestamp());

      data1.setModificationTimestamp(later);
		assertEquals("Modification timestamp doesn't match", later, data1.getModificationTimestamp());
      try
		{
         data1.setModificationTimestamp(evenlater);
			fail("It should not be possible to change modification timestamp once it was set.");
		}
		catch (Throwable thr)
		{
			// Do nothing since this is expected java.lag.AssertionError
		}

      try
		{
         data2.setModificationTimestamp(evenlater);
			fail("It should not be possible to change creation timestamp once it was set.");
		}
		catch (Throwable thr)
		{
			// Do nothing since this is expected java.lag.AssertionError
		}
   }
}
