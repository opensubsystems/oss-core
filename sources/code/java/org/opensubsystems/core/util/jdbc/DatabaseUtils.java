/*
 * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.TwoIntStruct;

/**
 * Set of common utility methods related to database access.
 * 
 * @author bastafidli
 */
public final class DatabaseUtils
{
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(DatabaseUtils.class);
   
   /**
    * Hashed tables-columns existence information for dependency checking. 
    */
   private static Map s_mpDependencyCache = new HashMap();

   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private DatabaseUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Execute given statement and close the statement after it is done.
    * 
    * @param jdbcstatement - statement to execute
    * @return int - number of affected rows
    * @throws SQLException - only if the update fails, not in any other case
    */
   public static int executeUpdateAndClose(
      PreparedStatement jdbcstatement
   ) throws SQLException
   {
      int iUpdateCount = 0;
      
      try
      {
         iUpdateCount = jdbcstatement.executeUpdate();
      }
      finally
      {
         closeStatement(jdbcstatement);
      }
      
      return iUpdateCount;
   }

   /**
    * Gracefully close result set so that no error is generated. 
    * This method NEVER throws any exception therefore it is safe to call it
    * in finally before returning connection.  
    * 
    * @param results - result set to close, if null it is ignored
    */
   public static void closeResultSet(
      ResultSet results
   )
   {
      try
      {
         if (results != null)
         {
            results.close();
         }
      }
      catch (SQLException sqleExc)
      {
         // Ignore this
         s_logger.log(Level.WARNING, 
                      "Failed to close the database result set",
                      sqleExc);
      }
   }

   /**
    * Gracefully close result set and statement so that no error is generated. 
    * This method NEVER throws any exception therefore it is safe to call it
    * in finally before returning connection.  
    * 
    * @param results - result set to close, if null it is ignored
    * @param jdbcstatement - jdbc statement to close, if null it is ignored
    */
   public static void closeResultSetAndStatement(
      ResultSet results,
      Statement jdbcstatement
   )
   {
      try
      {
         closeResultSet(results);
      }
      finally
      {
         closeStatement(jdbcstatement);
      }
   }

   /**
    * Gracefully close statement so that no error is generated.
    * This method NEVER throws any exception therefore it is safe to call it
    * in finally before returning connection.  
    * 
    * @param jdbcstatement - jdbc statement to close, if null it is ignored
    */
   public static void closeStatement(
      Statement jdbcstatement
   )
   {
      if (jdbcstatement != null)
      {
         try
         {
            jdbcstatement.close();
         }
         catch (SQLException sqleExc)
         {
            s_logger.log(Level.WARNING, 
                         "Failed to close the database statement", 
                         sqleExc);
         }
      }
   }
   
   /**
    * Execute statement and load at most one data object from the result set and 
    * if the result set contains more than one item announce error.
    * 
    * Note: Since the caller constructed the prepared statement, it is responsible
    * for closing it.
    * 
    * @param pstmQuery - query to execute
    * @param iDefault - default value to return if the result set doesn't contain
    *                   any value
    * @param strErrorMessage - error message to announce if there is more than 
    *                          one item
    * @return int - loaded int or null if the result set was empty
    * @throws SQLException - an error has occurred
    * @throws OSSException - an error has occurred
    */
   public static int loadAtMostOneInt(
      PreparedStatement pstmQuery,
      int               iDefault,
      String            strErrorMessage
   ) throws SQLException,
            OSSException
   {
      int       iData = iDefault;
      ResultSet rsQueryResults = null;
      
      try
      {
         rsQueryResults = pstmQuery.executeQuery();
         if (rsQueryResults.next())
         {
            iData = rsQueryResults.getInt(1);
            if (GlobalConstants.ERROR_CHECKING)
            {
               assert (!rsQueryResults.next()) : strErrorMessage;
            }
         }
      }
      finally
      {
         closeResultSet(rsQueryResults);
      }
      
      return iData;
   }

   /**
    * Execute statement and load at most one data object from the result set and 
    * if the result set contains more than one item announce error.
    * 
    * Note: Since the caller constructed the prepared statement, it is responsible
    * for closing it.
    * 
    * @param pstmQuery - query to execute
    * @param strDefault - default value to return if the result set doesn't contain
    *                     any value
    * @param strErrorMessage - error message to announce if there is more than 
    *                          one item
    * @return String - loaded string or null if the result set was empty
    * @throws SQLException - an error has occurred
    * @throws OSSException - an error has occurred
    */
   public static String loadAtMostOneString(
      PreparedStatement pstmQuery,
      String            strDefault,
      String            strErrorMessage
   ) throws SQLException,
            OSSException
   {
      String    strData        = strDefault;
      ResultSet rsQueryResults = null;
      
      try
      {
         rsQueryResults = pstmQuery.executeQuery();
         if (rsQueryResults.next())
         {
            strData = rsQueryResults.getString(1);
            if (GlobalConstants.ERROR_CHECKING)
            {
               assert (!rsQueryResults.next()) : strErrorMessage;
            }
         }
      }
      finally
      {
         closeResultSet(rsQueryResults);
      }
      
      return strData;
   }
   
   /**
    * Execute statement and load multiple strings from the result set. If there 
    * are no results null will be returned. 
    * 
    * Note: Since the caller constructed the prepared statement, it is responsible
    * for closing it.
    * 
    * @param pstmQuery - query to execute
    * @return List - list of loaded strings or null if there were no results
    * @throws SQLException - an error has occurred
    * @throws OSSException - an error has occurred
    */
   public static List loadMultipleStrings(
      PreparedStatement pstmQuery
   ) throws SQLException,
            OSSException
   {
      ResultSet rsQueryResults = null;
      List      lstData = null;
      
      try
      {
         rsQueryResults = pstmQuery.executeQuery();
         // We cannot user first() and beforeFirst() since we don know
         // id absolute positioning is supported so lets
         // call next and add call to next above and convert the loop
         // below to do while
         if (rsQueryResults.next())
         {
            // Here we do not know the count so we do not know how many 
            // items to load, for now use ArrayList even though it may be 
            // slow if it has to expand a lot
            lstData = new ArrayList();
         }
         if (lstData != null)
         {
            do
            {
               lstData.add(rsQueryResults.getString(1));
            }
            while (rsQueryResults.next());
         }
      }
      finally
      {
         DatabaseUtils.closeResultSet(rsQueryResults);
      }
       
      return lstData;
   }

   /**
    * Execute statement and load multiple int's from the result set. If there 
    * are no results null will be returned.
    * 
    * Note: Since the caller constructed the prepared statement, it is 
    * responsible for closing it.
    * 
    * @param pstmQuery - query to execute
    * @return int[] - list of loaded int's or null if there were no results
    * @throws SQLException - an error has occurred
    * @throws OSSException - an error has occurred
    */
   public static int[] loadMultipleIntsAsArray(
      PreparedStatement pstmQuery
   ) throws SQLException,
            OSSException
   {
      ResultSet rsQueryResults = null;
      int[]     arrData = null;
      
      try
      {
         rsQueryResults = pstmQuery.executeQuery();
         // Here we do not know the count so we do not know how many items
         // to load, for now use ArrayList even though it may be slow
         // if it has to expand a lot
         if (rsQueryResults.next())
         {
            int iActualCount = 0;
            
            List lstData = new ArrayList();
            do
            {  
               lstData.add(new Integer(rsQueryResults.getInt(1)));
            }
            while (rsQueryResults.next());
            arrData = new int[lstData.size()];
            for (Iterator items = lstData.iterator(); items.hasNext();)
            {
               arrData[iActualCount++] = ((Integer)items.next()).intValue();
            }
         }
      }
      finally
      {
         DatabaseUtils.closeResultSet(rsQueryResults);
      }
       
      return arrData;
   }

   /**
    * Execute statement and load multiple TwoIntStruct objects from the result set. 
    * If there are no results null will be returned. 
    * 
    * Note: Since the caller constructed the prepared statement, it is responsible
    * for closing it.
    * 
    * @param pstmQuery - query to execute
    * @return TwoIntStruct[] - list of loaded int's or null if there were no results
    * @throws SQLException - an error has occurred
    * @throws OSSException - an error has occurred
    */
   public static TwoIntStruct[] loadMultipleTwoIntStruct(
      PreparedStatement pstmQuery
   ) throws SQLException,
            OSSException
   {
      ResultSet      rsQueryResults = null;
      TwoIntStruct[] arrData = null;
      
      try
      {
         int iActualCount = 0;

         rsQueryResults = pstmQuery.executeQuery();
         // Here we do not know the count so we do not know how many items
         // to load, for now use ArrayList even though it may be slow
         // if it has to expand a lot
         if (rsQueryResults.next())
         {
            List lstData = new ArrayList();
            do
            {  
               lstData.add(new TwoIntStruct(rsQueryResults.getInt(1),
                                            rsQueryResults.getInt(2)));
            }
            while (rsQueryResults.next());
            arrData = new TwoIntStruct[lstData.size()];
            for (Iterator items = lstData.iterator(); items.hasNext();)
            {
               arrData[iActualCount++] = (TwoIntStruct)items.next();
            }
         }
      }
      finally
      {
         DatabaseUtils.closeResultSet(rsQueryResults);
      }
       
      return arrData;
   }
   
   /**
    * Merge new columns to the list of existing columns safely so if they already
    * exist in the original list, they won't be added. 
    * 
    * @param arrOriginalColumns - list of original columns
    * @param arrExtraColumns - new columns to add to the list
    * @return int[] - if the list already contained the columns, it will return
    *                  the same list otherwise it will return new list
    *                  with extra columns
    */
   public static int[] mergeColumnsSafely(
      int[] arrOriginalColumns,
      int[] arrExtraColumns
   )
   {
      int[]   arrReturn = arrOriginalColumns;
      List    lstAddedColumns = new ArrayList();
      int     iIndex;
      int     iIndex1; 
      Integer iColumn;
      boolean bFoundFlag;
      
      // Try to find if the columns already exist in the list
      for (iIndex = 0; iIndex < arrExtraColumns.length; iIndex++)
      {
         bFoundFlag = false;
         for (iIndex1 = 0; iIndex1 < arrOriginalColumns.length; iIndex1++)
         {
            if (arrExtraColumns[iIndex] == arrOriginalColumns[iIndex1])
            {
               // Column is already there
               bFoundFlag = true;
               break;
            }
         }
         if (!bFoundFlag)
         {
            // new column value not found
            lstAddedColumns.add(new Integer(arrExtraColumns[iIndex]));    
         }
      }

      if (lstAddedColumns.size() > 0)
      {
         // There are not some columns yet, copy the original elements
         arrReturn = new int[arrOriginalColumns.length + lstAddedColumns.size()];
         System.arraycopy(arrOriginalColumns, 0, arrReturn, 0, 
                          arrOriginalColumns.length);

         // Add new columns to the original 
         for (iIndex = 0; iIndex < lstAddedColumns.size(); iIndex++)
         {
            iColumn = (Integer) lstAddedColumns.get(iIndex);
            arrReturn[arrOriginalColumns.length + iIndex] = iColumn.intValue(); 
         }
      }

      return arrReturn;
   }

   /**
    * Generated question mark placeholders into a buffer that will be used as a 
    * query to construct prepared statement. 
    * 
    * @param sbBuffer - buffer into which this method will be append one "?" for 
    *                   each specified argument 
    * @param objValue - if this is not an array or a collection, only one "?" 
    *                   will be appended otherwise an "(?, ?, ....)" will be 
    *                   appended
    * @param lstPrepStmtArgumentBuffer - buffer which will collect arguments 
    *                                    that should be used to populate 
    *                                    prepared statement constructed from 
    *                                    this query. This method will add the
    *                                    value(s) to this buffer. If the objValue
    *                                    is an array, each element of the array 
    *                                    will be added individually
    */
   public static void generatePreparedStatementPlaceholders(
      StringBuffer sbBuffer, 
      Object       objValue,
      List         lstPrepStmtArgumentBuffer
   )
   {
      if (objValue instanceof int[])
      {
         int[] values = (int[])objValue;
         int   iLimit = values.length;
         
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert iLimit > 0 
                   : "Array of arguments has to have at least 1 element.";
         }
         
         // TODO: Performance: Consider just having a constant with one large
         // string and just adding a substring of it based on how many parameters
         // we have
         sbBuffer.append("(?");
         lstPrepStmtArgumentBuffer.add(new Integer(values[0]));
         // Start from 1 below since we already added 1 above
         for (int iIndex = 1; iIndex < iLimit; iIndex++)
         {
           sbBuffer.append(",?");
           lstPrepStmtArgumentBuffer.add(new Integer(values[iIndex]));
         }
         sbBuffer.append(')');
      }
      else if (objValue instanceof Object[])
      {
         Object[] values = (Object[])objValue;
         int      iLimit = values.length;
         
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert iLimit > 0 
                   : "Array of arguments has to have at least 1 element.";
         }
         
         // TODO: Performance: Consider just having a constant with one large
         // string and just adding a substring of it based on how many parameters
         // we have
         sbBuffer.append("(?");
         lstPrepStmtArgumentBuffer.add(values[0]);
         // Start from 1 below since we already added 1 above
         for (int iIndex = 1; iIndex < iLimit; iIndex++)
         {
           sbBuffer.append(",?");
           lstPrepStmtArgumentBuffer.add(values[iIndex]);
         }
         sbBuffer.append(')');
      }
      else if (objValue instanceof Collection)
      {
         Collection values = (Collection)objValue;
         int        iLimit = values.size();
         
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert iLimit > 0 
                   : "Collection of arguments has to have at least 1 element.";
         }
         
         // TODO: Performance: Consider just having a constant with one large
         // string and just adding a substring of it based on how many parameters
         // we have
         
         sbBuffer.append("(?");
         for (int iIndex = 1; iIndex < iLimit; iIndex++)
         {
           sbBuffer.append(",?");
            
         }
         sbBuffer.append(')');
         lstPrepStmtArgumentBuffer.addAll(values);
      }
      else
      {
         sbBuffer.append('?');
         lstPrepStmtArgumentBuffer.add(objValue);
      }
   }
   
   /**
    * Populate the question mark placeholders in a prepared statement with the 
    * supplied values. 
    * 
    * @param psQuery - prepared statement to populate with values
    * @param lstPrepStmtArguments - arguments that should be used to populate 
    *                               specified prepared statement
    * @throws SQLException - an error has occurred
    */
   public static void populatePreparedStatementPlaceholders(
      PreparedStatement psQuery, 
      List              lstPrepStmtArguments
   ) throws SQLException
   {
      Iterator values;
      Object   value;
      int      iIndex;
      
      for (values = lstPrepStmtArguments.iterator(), iIndex = 1; 
           values.hasNext(); iIndex++)
      {
         value = values.next();
         psQuery.setObject(iIndex, value);
      }
   }
   
   /**
    * Translate transaction isolation setting to a Connection.TRANSACTION_XXX 
    * constant.
    * 
    * @param strTransactionIsolation - one of the settings to set transaction 
    *                                  isolation
    * @return int
    */
   public static int convertTransactionIsolationToConstant(
      String strTransactionIsolation
   )
   {
      // Use the most conservative as default
      int iTransactionIsolation = Connection.TRANSACTION_SERIALIZABLE;
      
      if ("uncommited".equalsIgnoreCase(strTransactionIsolation))
      {
         iTransactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED;
      }
      else if ("commited".equalsIgnoreCase(strTransactionIsolation))
      {
         iTransactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
      }
      else if ("repeatable".equalsIgnoreCase(strTransactionIsolation))
      {
         iTransactionIsolation = Connection.TRANSACTION_REPEATABLE_READ;
      }
      else if ("serializable".equalsIgnoreCase(strTransactionIsolation))
      {
         iTransactionIsolation = Connection.TRANSACTION_SERIALIZABLE;
      }
      else if ("none".equalsIgnoreCase(strTransactionIsolation))
      {
         iTransactionIsolation = -1;
      }
      else
      {
         s_logger.log(Level.WARNING, "Incorrect transaction isolation setting {0}."
                      + " Using default serializable.", strTransactionIsolation); 
      }
      
      return iTransactionIsolation;
   }
   
   /**
    * Translate transaction isolation setting from a Connection.TRANSACTION_XXX 
    * constant.
    * 
    * @param iTransactionIsolation - one of the Connection.TRANSACTION_XXX constants
    * @return String
    */
   public static String convertTransactionIsolationFromConstant(
      int iTransactionIsolation
   )
   {
      // Use the most conservative as default
      String strTransactionIsolation = "unknown_" + iTransactionIsolation;
      
      if (iTransactionIsolation == Connection.TRANSACTION_READ_UNCOMMITTED)
      {
         strTransactionIsolation = "uncommited";
      }
      else if (iTransactionIsolation == Connection.TRANSACTION_READ_COMMITTED)
      {
         strTransactionIsolation = "commited";
      }
      else if (iTransactionIsolation == Connection.TRANSACTION_REPEATABLE_READ)
      {
         strTransactionIsolation = "repeatable";
      }
      else if (iTransactionIsolation == Connection.TRANSACTION_SERIALIZABLE)
      {
         strTransactionIsolation = "serializable";
      }
      else if (iTransactionIsolation == -1)
      {
         strTransactionIsolation = "none";
      }
      else
      {
         s_logger.log(Level.WARNING, "Incorrect transaction isolation constant {0}", 
                      iTransactionIsolation); 
      }
      
      return strTransactionIsolation;
   }

   /**
    * This method is for relation checking and for hashing table-column existence
    * for optimization. Basically what it can do for you is if you pass it a
    * set of tables and column names which can refer to your table it will check
    * if the specified id (your id) is in any of those tables (and therefore if 
    * there is anyone referring to you). If some of the tables doesn't exist, 
    * this method will remember it and it won't check the table anymore.
    * 
    * This can be used to break dependency on database level when you can 
    * configure in configuration file tables/columns which refer to some object 
    * and then the object can load this information from configuration file and 
    * check if there are any data referring to it. If those tables do not exist 
    * (e.g. because components containing those tables are not deployed than 
    * this method will correctly handle it)
    * 
    * @param cntConnection - database connection to use when analyzing 
    *                        dependencies. Caller is responsible for closing it
    * @param arTableColumn - array of {table name, column name} arrays to check 
    *                        relation to
    * @param iId - id of the object to check relations
    * @return boolean - true if relations exist
    * @throws OSSException - error during check
    */
   public static boolean hasRelations(
      Connection cntConnection,
      String[][] arTableColumn,
      int        iId
   ) throws OSSException
   {
      boolean           bReturn = false;
      PreparedStatement pstmQuery = null;
      ResultSet         rsResult = null;
      StringBuffer      sbQuery = null;
      boolean           bQueryInitialized = false;
      boolean           bExist;
      
      if ((arTableColumn != null) && (arTableColumn.length > 0))
      {
         for (int iCount = 0; iCount < arTableColumn.length; iCount++)
         {
            // TODO: Performnace: Optimize this with StringBuffers and preparedStatement
            if (s_mpDependencyCache.get(arTableColumn[iCount][0] 
                + "|" + arTableColumn[iCount][1]) == null)
            {
               try
               {
                  pstmQuery = cntConnection.prepareStatement(
                     "select " + 
                     arTableColumn[iCount][1] + 
                     " from " + 
                     arTableColumn[iCount][0]);
                  rsResult = pstmQuery.executeQuery();
                  
                  s_mpDependencyCache.put(
                     arTableColumn[iCount][0] + "|" + arTableColumn[iCount][1], 
                     "exist");
                  bExist = true;
                  
               }
               catch (SQLException sqleExc)
               {
                  s_mpDependencyCache.put(
                     arTableColumn[iCount][0] + "|" + arTableColumn[iCount][1], 
                     "not in DB");
                  bExist = false;
               }
               finally
               {
                  DatabaseUtils.closeResultSetAndStatement(rsResult, pstmQuery);
               }
            }
            else
            {
               bExist = s_mpDependencyCache.get(
                  arTableColumn[iCount][0] + "|" + arTableColumn[iCount][1]
               ).equals("exist");
            }
            
            if (bExist)
            {
               if (bQueryInitialized)
               {
                  sbQuery.append(" union ");
               }
               else
               {
                  sbQuery = new StringBuffer();
               }
               
               sbQuery.append("select ");
               sbQuery.append(arTableColumn[iCount][1]);
               sbQuery.append(" from ");
               sbQuery.append(arTableColumn[iCount][0]);
               sbQuery.append(" where ");
               sbQuery.append(arTableColumn[iCount][1]);
               sbQuery.append(" = ");
               sbQuery.append(iId);

               bQueryInitialized = true;
            }
         }
         
         if (sbQuery != null && sbQuery.length() > 0)
         {
            try
            {
               pstmQuery = cntConnection.prepareStatement(sbQuery.toString());
               rsResult = pstmQuery.executeQuery();
               
               if (rsResult.next())
               {
                  bReturn = true;
               }
            }
            catch (SQLException sqleExc)
            {
               throw new OSSDatabaseAccessException(
                     "Cannot get connection to the database.", 
                     sqleExc);
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(rsResult, pstmQuery);
            }               
         }
      }
      
      return bReturn;
   }
}
