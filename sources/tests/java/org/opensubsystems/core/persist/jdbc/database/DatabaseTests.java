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

package org.opensubsystems.core.persist.jdbc.database;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.database.AddBatchTest.AddBatchTestInternal;
import org.opensubsystems.core.persist.jdbc.database.DBProcReturnInsertedRowsCountTest.DBProcReturnInsertedRowsCountTestInternal;
import org.opensubsystems.core.persist.jdbc.database.DateTest.DateTestInternal;
import org.opensubsystems.core.persist.jdbc.database.DeleteWithSubqueryTest.DeleteWithSubqueryTestInternal;
import org.opensubsystems.core.persist.jdbc.database.GeneratedKeyTest.GeneratedKeyTestInternal;
import org.opensubsystems.core.persist.jdbc.database.PreparedStatementTest.PreparedStatementTestInternal;
import org.opensubsystems.core.persist.jdbc.database.QueryTest.QueryTestInternal;
import org.opensubsystems.core.persist.jdbc.database.SameColumnTest.SameColumnTestInternal;
import org.opensubsystems.core.persist.jdbc.database.SelectAfterInsertIntoUniqueColumnTest.SelectAfterInsertIntoUniqueColumnTestInternal;
import org.opensubsystems.core.persist.jdbc.database.SetNullColumnTest.SetNullColumnTestInternal;
import org.opensubsystems.core.persist.jdbc.database.TimeTest.TimeTestInternal;
import org.opensubsystems.core.persist.jdbc.database.TimestampTest.TimestampTestInternal;
import org.opensubsystems.core.persist.jdbc.database.TransactionTest.TransactionTestInternal;
import org.opensubsystems.core.persist.jdbc.database.UpdatableResultSetTest.UpdatableResultSetTestInternal;
import org.opensubsystems.core.persist.jdbc.database.UpdateDBProcWithoutOutputTest.UpdateDBProcWithoutOutputTestInternal;
import org.opensubsystems.core.util.test.Tests;

/**
 * Test suite containing all tests verifying behavior of different database 
 * drivers.
 * 
 * @author bastafidli
 */
public final class DatabaseTests extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private DatabaseTests(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Create suite of all database driver tests.
    * 
    * @return Test - suite of tests to run
    */
   public static Test suite(
   )
   {
      TestSuite suite = new DatabaseTestSuite("Test for database driver");
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
    * @param suite - suite to add tests to
    */
   public static void addGenericTests(
      TestSuite suite
   ) 
   {      
      suite.addTestSuite(AddBatchTestInternal.class);
      suite.addTestSuite(DateTestInternal.class);
      suite.addTestSuite(DBProcReturnInsertedRowsCountTestInternal.class);
      suite.addTestSuite(DeleteWithSubqueryTestInternal.class);
      suite.addTestSuite(GeneratedKeyTestInternal.class);
      suite.addTestSuite(PreparedStatementTestInternal.class);
      suite.addTestSuite(QueryTestInternal.class);
      suite.addTestSuite(SameColumnTestInternal.class);
      suite.addTestSuite(SelectAfterInsertIntoUniqueColumnTestInternal.class);
      suite.addTestSuite(SetNullColumnTestInternal.class);
      suite.addTestSuite(TimestampTestInternal.class);
      suite.addTestSuite(TimeTestInternal.class);
      suite.addTestSuite(TransactionTestInternal.class);
      suite.addTestSuite(UpdatableResultSetTestInternal.class);
      suite.addTestSuite(UpdateDBProcWithoutOutputTestInternal.class);
   }   
}
