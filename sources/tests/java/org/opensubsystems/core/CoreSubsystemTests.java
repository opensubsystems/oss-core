/*
 * Copyright (C) 2003 - 2016 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.opensubsystems.core.data.BasicDataObjectTest;
import org.opensubsystems.core.data.DataDescriptorTest;
import org.opensubsystems.core.data.DataObjectTest;
import org.opensubsystems.core.data.ModifiableDataObjectTest;

import org.opensubsystems.core.persist.DataFactoryManagerTest;
import org.opensubsystems.core.persist.jdbc.DatabaseFactoryClassFactoryTest;
import org.opensubsystems.core.persist.jdbc.DatabaseSchemaClassFactoryTest;
import org.opensubsystems.core.persist.jdbc.DatabaseSchemaManagerTest;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.connectionpool.ConnectionPoolTests;
import org.opensubsystems.core.persist.jdbc.database.DatabaseTests;
import org.opensubsystems.core.util.ClassFactoryTest;
import org.opensubsystems.core.util.ClassUtilsTest;
import org.opensubsystems.core.util.ConfigTest;
import org.opensubsystems.core.util.CryptoUtilsTest;
import org.opensubsystems.core.util.DateUtilsTest;
import org.opensubsystems.core.util.FileCommitUtilsTest;
import org.opensubsystems.core.util.FileUtilsTest;
import org.opensubsystems.core.util.MultiConfigTest;
import org.opensubsystems.core.util.StringUtilsTest;
import org.opensubsystems.core.util.test.Tests;

/**
 * Test for classes included in Core package.
 *
 * @author bastafidli
 */
public final class CoreSubsystemTests extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Constructor
    */
   public CoreSubsystemTests(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Create suite of all core tests.
    * 
    * @return Test - suite of tests to run
    */
   public static Test suite(
   )
   {
      TestSuite suite = new DatabaseTestSuite("Test for core");
      try
      {
         addGenericTests(suite);
      }
      catch (Throwable thr)
      {
         System.out.println(thr);
         System.out.println(thr.getCause());
         thr.getCause().printStackTrace(System.out);
      }

      // Here we are using DatabaseTestSetup instead of ApplicationTestSetup
      // since we are just directly testing  database functionality without
      // accessing any business logic functionality packaged into application 
      // modules
      TestSetup wrapper = new DatabaseTestSetup(suite);

      return wrapper;
   }

   /**
    * Add all generic database tests to given suite.
    * 
    * @param suite - suite of tests to run
    */
   public static void addGenericTests(
      TestSuite suite
   ) 
   {
      suite.addTestSuite(DataObjectTest.class);
      suite.addTestSuite(BasicDataObjectTest.class);
      suite.addTestSuite(ModifiableDataObjectTest.class);
      suite.addTestSuite(DataDescriptorTest.class);

      suite.addTestSuite(ClassFactoryTest.class);
      suite.addTestSuite(ClassUtilsTest.class);
      suite.addTestSuite(ConfigTest.class);
      suite.addTestSuite(MultiConfigTest.class);
      suite.addTestSuite(DateUtilsTest.class);
      suite.addTestSuite(FileCommitUtilsTest.class);
      suite.addTestSuite(FileUtilsTest.class);
      suite.addTestSuite(CryptoUtilsTest.class);
      suite.addTestSuite(StringUtilsTest.class);
      suite.addTestSuite(DataFactoryManagerTest.class);
      suite.addTestSuite(DatabaseFactoryClassFactoryTest.class);
      suite.addTestSuite(DatabaseSchemaClassFactoryTest.class);
      suite.addTestSuite(DatabaseSchemaManagerTest.class);
      ConnectionPoolTests.addGenericTests(suite);
      DatabaseTests.addGenericTests(suite); 
   }   
}
