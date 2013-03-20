/*
 * Copyright (C) 2008 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.persist.jdbc.impl;

import java.util.HashMap;
import java.util.Map;

import org.opensubsystems.core.util.SetupReader;
import org.opensubsystems.core.util.ThreeElementStruct;

/**
 * Class that reads setup for database from properties. Each database has unique 
 * name, which can represent different DBMS types (e.g. Oracle, DB2, etc.) or 
 * different groups of settings for the same database (e.g. Oracle Production, 
 * Oracle Development, etc.). This class will find all properties for that name 
 * or will use the default values if property with such name is not present.
 * 
 * Each property name consist from three parts. 
 * 1. base path, for example oss.database.
 * 2. reader name, for example oracle
 * 3. parameter name, for example username
 *  
 * Property name looks like <basepath>.<readername>.<parametername> for example
 * oss.datasource.mydatabase.username
 * 
 * @author bastafidli
 */
public class DatabaseSetupReader extends SetupReader
{
   // Configuration settings ///////////////////////////////////////////////////
   
   /**
    * Base path for all properties
    */
   public static final String DATABASE_BASE_PATH = "oss.database";

   // Configuration parameters names 
   
   /** 
    * SQL statement that can be used to test connections
    */   
   public static final String CONNECTION_TEST_STATEMENT 
                                 = "connectionteststatement";

   /** 
    * Result set type that should be used to load lists of items from the result 
    * set efficiently. Some databases support efficient absolute cursors that 
    * allow us to efficiently find out the size of the result set and efficiently 
    * allocate memory for it and they may require special result set type to do 
    * so. See ResultSet.TYPE_XXX constants.
    */   
   public static final String SELECT_LIST_RESULT_SET_TYPE 
                                 = "selectlist.resultsettype";

   /** 
    * Result set concurrency that should be used to load lists of items from the 
    * result set efficiently. Some databases support efficient absolute cursors 
    * that allow us to efficiently find out the size of result set and efficiently 
    * allocate memory for it and they may require special result set concurrency 
    * to do so.
    */   
   public static final String SELECT_LIST_RESULT_SET_CONCURRENCY 
                                 = "selectlist.resultsetconcurrency";

   /** 
    * Flag specifying if database support limiting the range of rows retrieved 
    * by a query. This means that database has to provide a way how to construct 
    * EFFICIENT SQL that allows us to retrieve items starting from row X and 
    * ending at row Y from the result set containing further criteria.
    */   
   public static final String RANGE_SUPPORT = "rangesupport";

   /** 
    * Flag specifying if database (driver) allows us to call methods such as 
    * absolute() or last() for the retrieved result sets.
    */   
   public static final String ABSOLUTE_POSITIONING_SUPPORT 
                                 = "absolutepositioningsupport";

   /** 
    * Find out if when trying to find out size of the result set we should use
    * count(*)/count(1)/count(id) instead of using last() if the
    * ABSOLUTE_POSITIONING_SUPPORT indicates the database has support for it.
    */   
   public static final String PREFER_COUNT_TO_LAST = "prefercounttolast";

   /** 
    * Setting specifying how many database operations should be batched together 
    * before they are sent to the database to execute.
    */   
   public static final String BATCH_SIZE = "batchsize";

   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Default value specifying how many database operations should be batched 
    * together before they are sent to the database to execute.
    */
   public static final int BATCH_SIZE_DEFAULT = 500;
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   // Configuration default values
   
   /**
    * Default value for connection test statement.
    */
   protected String m_strDefaultConnectionTestStatement;
   
   /**
    * Default value for result set type when selecting lists.
    */
   protected int m_iDefaultSelectListResultSetType;
   
   /**
    * Default value for result set concurrency when selecting lists.
    */
   protected int m_iDefaultSelectListResultSetConcurrency;
   
   /**
    * Default value for flag specifying range support when selecting lists.
    */
   protected boolean m_bDefaultRangeSupport;
   
   /**
    * Default value for flag specifying absolute positioning support.
    */
   protected boolean m_bDefaultAbsolutePositioningSupport;
   
   /**
    * Default value for flag specifying if to prefer count() to last().
    */
   protected boolean m_bDefaultPreferCountToLast;
   
   /**
    * Flag specifying if default values were already initalized
    */
   protected boolean m_bDefaultValuesInitialized = false;
   
   // Constants ////////////////////////////////////////////////////////////////
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Map with all registered parameters. The key is String parameter name and 
    * the value is an a ThreeObjectStruct where the element 
    * 1. is Integer constant (one of the PARAMETER_TYPE_XXX constants) 
    *    representing the type. If this map is null of empty, the constructor 
    *    will invoke the registerParameters method to register the parameters 
    *    that will inserted to this map. This allows you to pass in static 
    *    variable that can be shared between all instances.
    * 2. is Object representing the default value. This can be static variable 
    *    that can be shared between all instances.
    * 3. is String representing user friendly name of the property
    */
   protected  static Map<String, ThreeElementStruct<Integer, Object, String>> s_registeredParameters 
      = new HashMap<>();

   // Constructor //////////////////////////////////////////////////////////////
   
   /**
    * @param strReaderName - name of reader usually representing the dbms for 
    *                        which to read the settings. This can represent 
    *                        different DBMS types (e.g. Oracle, DB2, etc.) or 
    *                        different groups of settings for the same database 
    *                        (e.g. Oracle Production, Oracle Development, etc.).
    * @param strDefaultConnectionTestStatement - default value for connection 
    *                                            test statement
    * @param iDefaultSelectListResultSetType - default value for result set type 
    *                                          when selecting lists
    * @param iDefaultSelectListResultSetConcurrency - default value for result 
    *                                                 set concurrency when 
    *                                                 selecting lists
    * @param bDefaultRangeSupport - default value for flag specifying 
    *                                         range support when selecting lists
    * @param bDefaultAbsolutePositioningSupport - default value for flag 
    *                                             specifying absolute positioning 
    *                                             support
    * @param bDefaultPreferCountToLast - default value for flag specifying if to 
    *                                    prefer count() to last().
    */
   public DatabaseSetupReader(
      String  strReaderName,
      String  strDefaultConnectionTestStatement,
      int     iDefaultSelectListResultSetType,
      int     iDefaultSelectListResultSetConcurrency,
      boolean bDefaultRangeSupport,
      boolean bDefaultAbsolutePositioningSupport,
      boolean bDefaultPreferCountToLast
   )
   {
      // We can pass the static variables here since once the types are 
      // registered they are the same for all instances so it is enough to 
      // register them only once
      super(DATABASE_BASE_PATH, strReaderName, s_registeredParameters);
      
      m_strDefaultConnectionTestStatement = strDefaultConnectionTestStatement;
      m_iDefaultSelectListResultSetType = iDefaultSelectListResultSetType;
      m_iDefaultSelectListResultSetConcurrency = iDefaultSelectListResultSetConcurrency;
      m_bDefaultRangeSupport = bDefaultRangeSupport;
      m_bDefaultAbsolutePositioningSupport = bDefaultAbsolutePositioningSupport;
      m_bDefaultPreferCountToLast = bDefaultPreferCountToLast;
      
      m_bDefaultValuesInitialized = true;
      
      // We need to call this method again even though it was called already by
      // the base class since only now we have all the default values available
      registerParameters();
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void registerParameters(
   )
   {
      
      if (m_bDefaultValuesInitialized)
      {
         registerParameter(CONNECTION_TEST_STATEMENT, 
                           SetupReader.PARAMETER_TYPE_STRING_OBJ,
                           m_strDefaultConnectionTestStatement,
                           "SQL statement that can be used to test connections");
         
         registerParameter(SELECT_LIST_RESULT_SET_TYPE, 
                           SetupReader.PARAMETER_TYPE_INTEGER_OBJ,
                           Integer.toString(m_iDefaultSelectListResultSetType),
                           "Result set type that should be used to load lists of items");
   
         registerParameter(SELECT_LIST_RESULT_SET_CONCURRENCY, 
                           SetupReader.PARAMETER_TYPE_INTEGER_OBJ,
                           Integer.toString(m_iDefaultSelectListResultSetConcurrency),
                           "Result set concurrency that should be used to load lists of items");
   
         registerParameter(RANGE_SUPPORT, 
                           SetupReader.PARAMETER_TYPE_BOOLEAN_OBJ,
                           Boolean.toString(m_bDefaultRangeSupport),
                           "Flag specifying if database support limiting the range"
                           + " of rows retrieved by a query");
   
         registerParameter(ABSOLUTE_POSITIONING_SUPPORT, 
                           SetupReader.PARAMETER_TYPE_BOOLEAN_OBJ,
                           Boolean.toString(m_bDefaultAbsolutePositioningSupport),
                           "Flag specifying if database (driver) allows us to call"
                           + " methods such as absolute() or last() for the retrieved"
                           + " result sets");
   
         registerParameter(PREFER_COUNT_TO_LAST, 
                           SetupReader.PARAMETER_TYPE_BOOLEAN_OBJ,
                           Boolean.toString(m_bDefaultPreferCountToLast),
                           "Find out if when trying to find out size of the result"
                           + " set we should use count(*)/count(1)/count(id) instead"
                           + " of using last()");

         registerParameter(BATCH_SIZE, 
                           SetupReader.PARAMETER_TYPE_INTEGER_OBJ,
                           Integer.toString(BATCH_SIZE_DEFAULT),
                           "How many database operations to batch together");

      }
   }
}
