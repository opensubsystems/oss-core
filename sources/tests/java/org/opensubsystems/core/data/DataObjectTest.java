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
}
