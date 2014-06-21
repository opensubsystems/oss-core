/*
 * Copyright (C) 2003 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.persist.jdbc.test;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.util.Config;

/**
 * This test suite which should be used to group tests accessing database. It 
 * provides functionality necessary to run database tests, mainly it points all 
 * tests in the suite to the correct database.
 *
 * @author bastafidli
 */
public class DatabaseTestSuite extends TestSuite
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Data source which will be used by all tests in this suite
    */
   protected String m_strDataSourceName = null;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Static initializer 
    */
   static
   {
      if (Config.getInstance().getRequestedConfigFile() == null)
      {
         Config.getInstance().setPropertyFileName(
                                 DatabaseTest.DEFAULT_PROPERTY_FILE);
      }
   }
   
   /**
    * Run all tests from 
    * 
    * @param strName - name of the test suite
    */
   public DatabaseTestSuite(
      String strName
   ) 
   {
      super(strName);
   }

   // Logic //////////////////////////////////////////////////////////////////// 
   
   /**
    * {@inheritDoc}
    */
   public void runTest(
      Test       testToRun, 
      TestResult result
   ) 
   {
      try
      {
         setTestDataSource(testToRun);
      }
      catch (OSSException bfeExc)
      {
         // This method cannot throw exception so we need to convert it to
         // uncheck exception
         throw new RuntimeException(bfeExc);
      }
      
      super.runTest(testToRun, result);
   }

   /**
    * Set the data source to be used by this database test. THis method must 
    * be called before any test can be run.
    * 
    * @param strDataSourceName - data source which will be used by this test
    * @throws OSSException - an error has occurred
    */
   public void setDataSourceName(
      String strDataSourceName
   ) throws OSSException 
   {
      DatabaseConnectionFactoryImpl.getInstance().setDefaultDataSource(
         strDataSourceName);
   }

   /**
    * Get the name of the data source used by this test.
    * 
    * @return String - data source name
    * @throws OSSException - an error has occurred
    */
   public String getDataSourceName(
   ) throws OSSException
   {
      return DatabaseConnectionFactoryImpl.getInstance().getDefaultDataSourceName();
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Set datasource for all database tests in specified test or test suite
    * 
    * @param testToSet - test or test suite to set the data source for
    * @throws OSSException - an error has occurred
    */
   protected void setTestDataSource(
      Test   testToSet
   ) throws OSSException
   {
      if (m_strDataSourceName != null)
      {
         if (testToSet instanceof DatabaseTest)
         {
            setDataSourceName(m_strDataSourceName);
         }
         else if (testToSet instanceof TestSuite)
         {
            // We need to set the datasource for every applicable test in the suite
            Enumeration enumTests;
            Test        testTemp;
            
            for (enumTests = ((TestSuite)testToSet).tests(); enumTests.hasMoreElements();)
            {
               testTemp = (Test)enumTests.nextElement();
               // Do this to limit recursion
               if (testTemp instanceof DatabaseTest)
               {
                  setDataSourceName(m_strDataSourceName);
               }
               else
               {
                  setTestDataSource(testTemp);               
               }
            }
         }
      }
   }
}
