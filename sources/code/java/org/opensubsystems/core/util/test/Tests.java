/*
 * Copyright (C) 2010 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
package org.opensubsystems.core.util.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestSuite;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.Log;

/**
 * This is base class for various test that ensures that correct configuration 
 * files are being used while the tests are run. It allows to use separate
 * configuration files 
 * - osstest.properties 
 * - osstestlog.properties
 * to configure the code executed during test rather than using the default 
 * property files 
 * - oss.properties 
 * - osslog.properties
 * 
 * @author bastafidli
 */
public class Tests
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Default log file used to run tests.
    */
   public static final String DEFAULT_LOG_FILE = "osstestlog.properties";

   /**
    * Default property file used to run tests.
    */
   public static final String DEFAULT_PROPERTY_FILE = "osstest.properties";

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger;

   // Constructors /////////////////////////////////////////////////////////////
   
   static
   {
      // Configure the tests to use separate logging configuration file and 
      // separate property file so that it is easy to test the code 
      System.setProperty("java.util.logging.config.file", DEFAULT_LOG_FILE);
      System.setProperty(Config.CONFIG_FILE_NAME, DEFAULT_PROPERTY_FILE);
      s_logger = Log.getInstance(Tests.class);
      // No need to do this since by setting the property above the Config will
      // use the correct property file
      // Config.getInstance().setPropertyFileName("osstest.properties");
   }
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   protected Tests(
   )
   {
      super();
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Add tests from a suite specified by a class name to a specified suite.
    * 
    * @param suiteToAddTo - suite to add tests to
    * @param strSuiteClassToAddFrom - class for a suite to add tests from
    */
   protected static void addGenericTestsFromTestSuite(
      TestSuite suiteToAddTo,
      String    strSuiteClassToAddFrom    
   )
   {
      try
      {
         Class  suiteToAddFrom;
         Method genericTestsMethod;
         
         suiteToAddFrom = Class.forName(strSuiteClassToAddFrom);
         genericTestsMethod = suiteToAddFrom.getMethod("addGenericTests", 
                                                       new Class[] {TestSuite.class});
         genericTestsMethod.invoke(null, new Object[] {suiteToAddTo});
         s_logger.log(Level.FINE, "Test from suite {0} were successfully added.", 
                      strSuiteClassToAddFrom);         
      }
      catch (ClassNotFoundException eNoClass)
      {
         // Log this just as fine with no exception since this is expected if
         // components are packed individually
         s_logger.log(Level.FINE, "Cannot find class {0} therefore no tests will"
                      + " be added from this suite.", strSuiteClassToAddFrom);         
      }
      catch (SecurityException | IllegalAccessException eSecurity)
      {
         s_logger.log(Level.SEVERE, 
                      "Cannot add tests from suite " + strSuiteClassToAddFrom
                      + " since method addGenericTests cannot be accessed.",
                      eSecurity);
      }
      catch (NoSuchMethodException eNoMethod)
      {
         s_logger.log(Level.SEVERE, 
                      "Cannot add tests from suite " + strSuiteClassToAddFrom
                      + " since method addGenericTests cannot be found.",
                      eNoMethod);
      }
      catch (IllegalArgumentException eBadArg)
      {
         s_logger.log(Level.SEVERE, 
                      "Cannot add tests from suite " + strSuiteClassToAddFrom
                      + " since method addGenericTests doesn't take TestSuite" 
                      + " as a parameter.", eBadArg);
      }
      catch (InvocationTargetException eCantInvoke)
      {
         s_logger.log(Level.SEVERE, 
                      "Cannot add tests from suite " + strSuiteClassToAddFrom
                      + " since an error has occurred invoking method.",
                      eCantInvoke);
      }
   }
}
