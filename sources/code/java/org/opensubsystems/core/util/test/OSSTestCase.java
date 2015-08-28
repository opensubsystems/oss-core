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
 
package org.opensubsystems.core.util.test;

import java.util.logging.Logger;
import junit.framework.TestCase;

import org.opensubsystems.core.util.Config;

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
public class OSSTestCase extends TestCase
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
      // No need to do this since by setting the property above the Config will
      // use the correct property file
      // Config.getInstance().setPropertyFileName("osstest.properties");
   }
   
   /**
    * Constructor for OSSTestCase.
	 * 
    * @param strName - name of the test
    */
   public OSSTestCase(
      String strName
   )
   {
      super(strName);
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   // Helper methods ///////////////////////////////////////////////////////////
}
