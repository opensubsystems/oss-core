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

import junit.extensions.TestSetup;
import junit.framework.Test;
import org.opensubsystems.core.persist.jdbc.Database;

import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.Config;

/**
 * This class makes sure that the database is property initialized before the 
 * tests are executed and that the database is properly shutdown when all
 * tests are finished. ALL TESTCASES DERIVED FROM  DatabaseTest CLASS AND ALL
 * TEST SUITES INCLUDING SUCH TEST CASES SHOULD FOLLOW THE DIRECTION BELLOW. 
 *
 * To use this setup in your class define your tests as
 *  
 * class MyTest
 * { 
 *    public static Test suite(
 *    )
 *    {
 *       TestSuite suite = new DatabaseTestSuite("MyTest");
 *       suite.addTestSuite(MyTestInternal.class);
 *       TestSetup wrapper = new DatabaseTestSetup(suite);
 * 
 *       return wrapper;
 *    }
 *    public static class MyTestInternal extends TestCase
 *    {
 *       // add tests here
 *    }
 * }
 *
 * This way if the top level test gets executed, it will be executed as a suite
 * and this setup will be invoked for all tests in the suite and therefore the
 * database will be properly initialized and shutdown.
 * If you want to include these tests in another suite, DO NOT ADD MyTest
 * but instead add MyTest.MyTestInternal. This way the suite which includes
 * this test can invoke the setup once for all included tests.
 *
 * To create a suite of suites, define your suite as
 * 
 * public final class MyTests
 * {
 *    public static Test suite(
 *    )
 *    {
 *       TestSuite suite = new DatabaseTestSuite("My tests");
 *       try
 *       {
 *          addGenericTests(suite);
 *       }
 *       catch (Throwable thr)
 *       {
 *          System.out.println(thr);
 *          System.out.println(thr.getCause());
 *          thr.getCause().printStackTrace(System.out);
 *       }
 *       TestSetup wrapper = new DatabaseTestSetup(suite);
 * 
 *       return wrapper;
 *    }
 * 
 *    public static void addGenericTests(
 *       TestSuite suite
 *    ) 
 *    {      
 *       // Here we are adding single test case class using the inner internal class
 *       suite.addTestSuite(MyTestInternal.class);
 *       // Here we are including tests from another suite without including the 
 *       // DatabaseTestSetup again
 *       MyOtherTests.addGenericTests(suite);
 *    }
 * }
 *
 * @author bastafidli
 */
public class DatabaseTestSetup extends TestSetup
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Default property file used to run tests.
    */
   protected static final String DEFAULT_PROPERTY_FILE = "osstest.properties";

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Static initializer 
    */
   static
   {
      if (Config.getInstance().getRequestedConfigFile() == null)
      {
         Config.getInstance().setPropertyFileName(DEFAULT_PROPERTY_FILE);
      }
   }

   /**
    * Create new DatabaseTestSetup.
    * 
    * @param test - test to run
    */
   public DatabaseTestSetup(
      Test test
   )
   {
      super(test);
   }   

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void setUp(
   ) throws Exception
   {
      super.setUp();
      
      // This will start database if it is not started
      // Try to initialize the default database if any
      Database dbDefaultDB;
      
      dbDefaultDB = DatabaseImpl.getInstance();
      if (dbDefaultDB != null)
      {
         dbDefaultDB.start();         
      }      
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void tearDown(
   ) throws Exception
   {
      // Some databases for example HSQLDB require to explicitly stop the
      // database so this gives us opportunity to do it
      Database dbDefaultDB;
      
      dbDefaultDB = DatabaseImpl.getInstance();
      if (dbDefaultDB != null)
      {
         dbDefaultDB.stop();         
      }      

      super.tearDown();
   }
}
