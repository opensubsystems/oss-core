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

import org.opensubsystems.core.error.OSSException;

/**
 * Tests for DataObject and DataObjectImpl classes.
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
      return new TestBasicDataObject(lId, DataObject.NEW_ID, null, strField1, 
                                     strField2, strField3);
   }

   // Tests ////////////////////////////////////////////////////////////////////

}
