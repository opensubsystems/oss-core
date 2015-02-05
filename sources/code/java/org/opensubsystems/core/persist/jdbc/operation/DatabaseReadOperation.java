/*
 * Copyright (C) 2003 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.persist.jdbc.operation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.DatabaseFactory;
import org.opensubsystems.core.persist.jdbc.DatabaseSchema;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Adapter to simplify writing of database reads, which takes care of
 * requesting and returning connections, transaction management and 
 * exception handling. To use this adapter you just need to define anonymous 
 * class and override method performOperation to provide the actual database 
 * read. Optionally you may want to override one of the handleXXX methods to 
 * provide custom error handling. 
 *
 * Example of method in factory which reads data using query produced by its schema 
 *
 * public DataObject get(
 *    final int iDomainId,
 *    final int iId
 * ) throws OSSException
 * {
 *    DatabaseReadOperation dbop = new DatabaseReadOperation(
 *       this, m_schema.getSelectMyDataById(MyDatabaseSchema.MYDATA_COLUMNS), 
 *       m_schema, dataType)
 *    {
 *       protected Object performOperation(
 *          DatabaseFactoryImpl dbfactory,
 *          Connection          cntConnection,
 *          PreparedStatement   pstmQuery
 *       ) throws OSSException,
 *                SQLException
 *       {
 *          pstmQuery.setInt(1, iId);
 *          pstmQuery.setInt(2, iDomainId);
 *          return DatabaseUtils.loadAtMostOneData(dbfactory, pstmQuery,
 *                    "Multiple records loaded from database for domain ID "
 *                    + iDomainId + " and ID " + iId);
 *       }         
 *    };
 *    return (DataObject)dbop.executeRead();
 * }
 *
 * @author bastafidli
 */
public abstract class DatabaseReadOperation extends    DatabaseOperation 
                                            implements DatabaseOperations
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor to use when the database read doesn't require any 
    * prepared statement.
    * 
    * @param factory - factory which is executing this operation
    */
   public DatabaseReadOperation(
      DatabaseFactory factory
   )
   {
      this(factory, null, null);
   }

   /**
    * Constructor to use when database read doesn't require any prepared 
    * statement. There are contained attributes related to handling SQL errors.
    * 
    * @param factory - factory which is executing this operation
    * @param strQueryToPrepare - query which should be used to construct prepared
    *                            statement which will be passed in to executeUpdate
    * @param schema - database schema used with this operation
    */
   public DatabaseReadOperation(
      DatabaseFactory factory,
      String          strQueryToPrepare,
      DatabaseSchema  schema
   )
   {
      super(factory, strQueryToPrepare, schema, null);
   }   


   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Method to execute database read invoking the user defined code
    * in performOperation.
    * 
    * @return Object - data read from the database
    * @throws OSSException - an error has occurred
    */
   public Object executeRead(
   ) throws OSSException
   {
      Connection        cntConnection = null;
      PreparedStatement pstmQuery = null;
      
      try
      {
         // Request autocommit true since we are just reading data from the 
         // database
         cntConnection = DatabaseConnectionFactoryImpl.getInstance()
                            .requestConnection(true);
         // Prepare the query if any query was specified
         pstmQuery = prepareQuery(m_factory, cntConnection, m_strQuery);
         // Execute the read hopefully defined in the derived class
         m_returnData = performOperation(m_factory, cntConnection, pstmQuery);
      }
      catch (SQLException sqleExc)
      {
         handleSQLException(sqleExc, cntConnection, DatabaseOperations.DBOP_SELECT, 
                            m_factory.getDataDescriptor().getDataType(),
                            m_factory.getDataDescriptor().getDisplayableViewName(),
                            m_data);
      }
      // We just want to propagate OSSException since we do not want
      // to loose any error message produced by underlying layer and there
      // is no need to add any more messaging.
      catch (OSSException ossExc)
      {
         handleKnownError(ossExc, cntConnection, DatabaseOperations.DBOP_SELECT, 
                          m_factory.getDataDescriptor().getDataType(),
                          m_factory.getDataDescriptor().getDisplayableViewName(),
                          m_data);
      }
      // We must catch Throwable to rollback since assert throw Error and not 
      // Exception
      catch (Throwable thr)
      {
         handleUnknownError(thr, cntConnection, DatabaseOperations.DBOP_SELECT, 
                            m_factory.getDataDescriptor().getDataType(),
                            m_factory.getDataDescriptor().getDisplayableViewName(),
                            m_data);
      }      
      finally
      {
         DatabaseUtils.close(pstmQuery);
         DatabaseConnectionFactoryImpl.getInstance().returnConnection(
                                                        cntConnection);
      }  
      
      return m_returnData;
   }

   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Prepare the query if it was specified using the provided connection. 
    * 
    * @param dbfactory - database factory executing this operation
    * @param cntConnection - ready to use connection to perform the database
    *                        operation. No need to return this connection.
    * @param strQuery - query to prepare, might be null or empty if there is
    *                   nothing to prepare
    * @return PreparedStatement - prepared statement for query passed in as a 
    *                             parameter to the constructor. If no query was 
    *                             passed into constructor, this will be null.
    * @throws OSSException - an error has occurred
    * @throws SQLException - an error has occurred
    */
   protected PreparedStatement prepareQuery(
      DatabaseFactory dbfactory,
      Connection      cntConnection,
      String          strQuery
   ) throws OSSException,
            SQLException
   {
      PreparedStatement pstmQuery = null;
      
      if ((strQuery != null) && (strQuery.length() > 0))
      {
         pstmQuery = cntConnection.prepareStatement(strQuery);
      }
      
      return pstmQuery;
   }
   
   /**
    * Define content of this method to perform the database operation using the 
    * provided connection and optional prepared statement.
    * 
    * @param dbfactory - database factory executing this operation
    * @param cntConnection - ready to use connection to perform the database
    *                        operation. No need to return this connection.
    * @param pstmStatement - prepared statement for query passed in as a 
    *                        parameter to the constructor. No need to close 
    *                        this statement. If no query was passed into 
    *                        constructor, this will be null.
    * @return Object - data read from the database accessible through getReturnData
    * @throws OSSException - an error has occurred
    * @throws SQLException - an error has occurred
    */
   protected Object performOperation(
      DatabaseFactory   dbfactory,
      Connection        cntConnection,
      PreparedStatement pstmStatement
   ) throws OSSException,
            SQLException
   {
      // Override this method to provide actual commands to update the database
      return null;
   }   
   

   /**
    * Provide custom handling of SQL Exceptions to usually detect constraint
    * violation. By default just handle it as unknown error.
    * 
    * @param sqleExc - SQLException to handle
    * @param cntConnection - ready to use connection to perform the database
    *                        operation. No need to return this connection.
    * @param iOperationType - type of the operation that caused the exception, 
    *                         see DatabaseOperations for possible values
    * @param iDataType - data type the data object represents (e.g if this is
    *                    type user and data is Integer, that means it is id
    *                    of user object). This is one of the DataConstant 
    *                    constants.
    * @param strDisplayableViewName - displayable name for the view of the data 
    *                                 object instance. This name allows different 
    *                                 views of the same data objects to be called 
    *                                 differently to make them more user friendly.  
    * @param data - data object the exception is handled for 
    * @throws OSSException - properly handled exception
    */
   protected void handleSQLException(
      SQLException sqleExc,
      Connection   cntConnection,
      int          iOperationType,
      int          iDataType,
      String       strDisplayableViewName,
      Object       data
   ) throws OSSException
   {
      if (m_dbschema != null)
      {
         m_dbschema.handleSQLException(
            sqleExc, cntConnection, iOperationType, iDataType, 
            strDisplayableViewName, data);
      }
      else
      {
         // By default just handle it as unknown error
         handleUnknownError(sqleExc, cntConnection, iOperationType, iDataType, 
                            strDisplayableViewName, data);
      }
   }
   
   /**
    * Override this method to provide any custom error handling for expected 
    * error, which were most likely produced by lower layer. The default
    * implementation just propagates this error.

    * @param exc - known error which must be handled.
    * @param cntConnection - ready to use connection to perform the database
    *                        operation. No need to return this connection.
    * @param iOperationType - type of the operation that caused the exception, 
    *                         see DatabaseOperations for possible values
    * @param iDataType - data type the data object represents (e.g if this is
    *                    type user and data is Integer, that means it is id
    *                    of user object). This is one of the DataConstant 
    *                    constants.
    * @param strDisplayableViewName - displayable name for the view of the data 
    *                                 object instance. This name allows different 
    *                                 views of the same data objects to be called 
    *                                 differently to make them more user friendly.  
    * @param data - data object the exception is handled for 
    * @throws OSSException - properly handled exception
    */
   protected void handleKnownError(
      OSSException exc,
      Connection   cntConnection,
      int          iOperationType,
      int          iDataType,
      String       strDisplayableViewName,
      Object       data
   ) throws OSSException
   {
      throw exc;
   }

   /**
    * Override this method to provide any custom error handling for unexpected 
    * error, which weren't handled by lower layer. The default implementation
    * wraps it in proper error.
    * 
    * @param thr - throwable causing this error. This is not OSSException
    *              or a derived class.
    * @param cntConnection - ready to use connection to perform the database
    *                        operation. No need to return this connection.
    * @param iOperationType - type of the operation that caused the exception, 
    *                         see DatabaseOperations for possible values
    * @param iDataType - data type the data object represents (e.g if this is
    *                    type user and data is Integer, that means it is id
    *                    of user object). This is one of the DataConstant 
    *                    constants.
    * @param strDisplayableViewName - displayable name for the view of the data 
    *                                 object instance. This name allows different 
    *                                 views of the same data objects to be called 
    *                                 differently to make them more user friendly.  
    * @param data - data object the exception is handled for 
    * @throws OSSException - properly handled exception
    */
   protected void handleUnknownError(
      Throwable    thr,
      Connection   cntConnection,
      int          iOperationType,
      int          iDataType,
      String       strDisplayableViewName,
      Object       data
   ) throws OSSException
   {
      switch (iOperationType)
      {
         case (DBOP_SELECT) :
         {

            throw new OSSDatabaseAccessException(
                         "Failed to load " + strDisplayableViewName 
                         + " data from the database.",
                     thr);
         }
         default:
         {
            if (GlobalConstants.ERROR_CHECKING)
            {
               assert false : "Unknown database operation type " + iOperationType;
            }               
         }
      }
   }
}
