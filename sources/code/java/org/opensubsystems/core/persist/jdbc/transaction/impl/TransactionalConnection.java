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

package org.opensubsystems.core.persist.jdbc.transaction.impl;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.opensubsystems.core.persist.jdbc.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.util.GlobalConstants;

/**
 * Transactional connection is wrapper around real database connection to ensure
 * that the connection can be made part of global transaction spanning several
 * code components which do not know about each other.
 * 
 * @author bastafidli
 */
public class TransactionalConnection implements Connection
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Underlying database connection to which all method calls are delegated.
    */
   private Connection m_privateConnection;

   /**
    * Name of the data source this connection belongs to.
    */
   private String m_strDataSourceName;
   
   /**
    * User name under which this connection was created.
    */
   private String m_strUser;

   /**
    * Password under which this connection was created.
    */
   private String m_strPassword;
   
   /**
    * This flag is used by transaction to see if this connection was used 
    * while it was associated with the transaction to see if it should be 
    * committed or not. 
    */
   private boolean m_bUsed;
   
   /**
    * This counter is incremented when the connection is requested from the pool
    * and decremented when it is returned. This is used in case the connection
    * is returned in the transaction to mark it as returned since it cannot be
    * really returned while it is associated with transaction.
    */
   private int m_iActiveCount;
   
   /**
    * This flag will be set to true if the connection is associated with 
    * transaction so that the database connection factory knows it cannot
    * be returned to the pool.
    */
   private boolean m_bInTransaction;
   
   /**
    * Connection factory from which the private connection was acquired.
    */
   private DatabaseConnectionFactoryImpl m_connectionFactory;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Protected constructor so that only classes from this package can create it.
    * 
    * @param privateConnection - real connection to the database
    * @param strDataSourceName - name of the data source this connection belongs t
    *                            o
    * @param strUser - user name under which this connection was created
    * @param strPassword - password under which this connection was created
    * @param inTransaction - is the connection already in the transaction
    * @param connectionFactory - connection factory from which the private 
    *                            connection was acquired
    */
   protected TransactionalConnection(
      Connection                    privateConnection, 
      String                        strDataSourceName,
      String                        strUser,
      String                        strPassword,
      boolean                       inTransaction,
      DatabaseConnectionFactoryImpl connectionFactory
   ) 
   {
      super();
      
      m_privateConnection = privateConnection;
      m_strDataSourceName = strDataSourceName;
      m_strUser = strUser;
      m_strPassword = strPassword;
      m_bInTransaction = inTransaction;
      m_connectionFactory = connectionFactory;
      m_bUsed = false; // the connection wasn't used yet
      m_iActiveCount = 1; // the connection was just constructed so it has to be 
                          // active
   }   
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * This method is not public so that it can be used only in this package.
    * 
    * @return boolean - if true then the connection is active, it wasn't returned 
    *                   to the pool yet.
    */
   boolean isActive()
   {
      return (m_iActiveCount > 0);
   }
   
   /**
    * This method is not public so that it can be used only in this package.
    * 
    * @param active - if true then the connection was requested from the pool
    *                 if false it was already returned to the pool
    */
   void setActive(
      boolean active
   )
   {
      if (active)
      {
         m_iActiveCount++;
      }
      else
      {
         m_iActiveCount--;
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert m_iActiveCount >= 0 
                   : "Connection was returned too many times.";
         }
      }
   }
   
   /**
    * This method is not public so that it can be used only in this package.
    * 
    * @return boolean - if true then the connection any use of connection at
    *                   this time will be inside of transaction.
    */
   boolean isInTransaction()
   {
      return m_bInTransaction;
   }
   
   /**
    * This method is not public so that it can be used only in this package.
    * 
    * @param inTransaction - if in true then this connection is now associated
    *                        with the transaction
    * @throws SQLException - an error has occurred while associating connection
    *                        with transaction
    */
   void setInTransaction(
      boolean inTransaction
   ) throws SQLException
   {
      if ((inTransaction) && (m_privateConnection.getAutoCommit())) 
      {
         // Since the connection is now in transaction we need to disable
         // the autocommit
         m_privateConnection.setAutoCommit(false);
      }
      m_bInTransaction = inTransaction;
   }
   
   /**
    * This method is not public so that it can be used only in this package.
    * 
    * @return boolean - if true then some method was called on connection since
    *                   the last time this flag was reset 
    */
   public boolean isUsed()
   {
      return m_bUsed;
   }
   
   /**
    * @param used - if true then some method was called on connection since
    *               the last time this flag was reset
    * @throws SQLException - an error probably because the connection should
    *                        no longer be used 
    */
   void setUsed(
      boolean used
   ) throws SQLException
   {
      if ((m_iActiveCount == 0) && (used))
      {
         throw new SQLException("This connection was already returned to the pool" 
                                + " and shouldn't be used anymore."); 
      }
      m_bUsed = used;
   }
   
   /**
    * @return String
    */
   public String getDataSourceName()
   {
      return m_strDataSourceName;
   }

   /**
    * @return String
    */
   public String getPassword()
   {
      return m_strPassword;
   }
   
   /**
    * @return String
    */
   public String getUser()
   {
      return m_strUser;
   }
   
   /**
    * Verify if the specified connection is the one which is used by this wrapper.
    * 
    * @param verifiableConnection - connection to verify
    * @return boolean - true if the connections are the same
    */
   public boolean verifyConnection(
      Connection verifiableConnection
   )
   {
      // Verify if they are the same instance using ==
      return verifiableConnection == m_privateConnection;
   }   

   /**
    * @return DatabaseConnectionFactoryImpl
    */
   public DatabaseConnectionFactoryImpl getConnectionFactory()
   {
      return m_connectionFactory;
   }
   
   // java.sql.Connection delegating methods ///////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void clearWarnings(
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.clearWarnings();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void close(
   ) throws SQLException
   {
      throw new SQLException("Connection is not supposed to be closed. It should" 
                             + " be returned to database connection factory.");
      // setUsed(true);
      // m_privateConnection.close();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void commit(
   ) throws SQLException
   {
      if (m_bInTransaction)
      {
         throw new SQLException("Connection in transaction cannot be commited.");
      }
      // We are commiting so that means that this connection is clean and not 
      // used anymore
      setUsed(false);
      m_privateConnection.commit();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Statement createStatement(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createStatement();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Statement createStatement(
      int resultSetType, 
      int resultSetConcurrency
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createStatement(resultSetType, 
                                                 resultSetConcurrency);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Statement createStatement(
      int resultSetType,
      int resultSetConcurrency,
      int resultSetHoldability
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createStatement(resultSetType, 
                                                 resultSetConcurrency,
                                                 resultSetHoldability);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
   public boolean equals(
      Object obj
   )
   {
      // This is not really a use of connection
      // setUsed(true);
      return m_privateConnection.equals(obj);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean getAutoCommit(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getAutoCommit();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getCatalog(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getCatalog();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getHoldability(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getHoldability();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DatabaseMetaData getMetaData(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getMetaData();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getTransactionIsolation(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getTransactionIsolation();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Map<String,Class<?>> getTypeMap(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getTypeMap();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SQLWarning getWarnings(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getWarnings();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      // This is not really a use of connection
      // setUsed(true);
      return m_privateConnection.hashCode();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isClosed(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.isClosed();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isReadOnly(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.isReadOnly();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String nativeSQL(
      String sql
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.nativeSQL(sql);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public CallableStatement prepareCall(
      String sql
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareCall(sql);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public CallableStatement prepareCall(
      String sql, 
      int resultSetType, 
      int resultSetConcurrency
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareCall(sql, resultSetType, 
                                             resultSetConcurrency);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public CallableStatement prepareCall(
      String sql,
      int resultSetType,
      int resultSetConcurrency,
      int resultSetHoldability
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareCall(sql, resultSetType, 
                                             resultSetConcurrency,
                                             resultSetHoldability);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PreparedStatement prepareStatement(
      String sql
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PreparedStatement prepareStatement(
      String sql, 
      int autoGeneratedKeys
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql, autoGeneratedKeys);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PreparedStatement prepareStatement(
      String sql, 
      int resultSetType, 
      int resultSetConcurrency
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql, resultSetType, 
                                                  resultSetConcurrency);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PreparedStatement prepareStatement(
      String sql,
      int resultSetType,
      int resultSetConcurrency,
      int resultSetHoldability
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql, resultSetType, 
                                                  resultSetConcurrency,
                                                  resultSetHoldability);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PreparedStatement prepareStatement(
      String sql, 
      int[] columnIndexes
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql, columnIndexes);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PreparedStatement prepareStatement(
      String sql, String[] columnNames
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql, columnNames);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void releaseSavepoint(
      Savepoint savepoint
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.releaseSavepoint(savepoint);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void rollback(
   ) throws SQLException
   {
      if (m_bInTransaction)
      {
         throw new SQLException("Connection in transaction cannot be rollbacked.");
      }
      // We are rolling back so that means that this connection is clean and not 
      // used anymore
      setUsed(false);
      m_privateConnection.rollback();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void rollback(
      Savepoint savepoint
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.rollback(savepoint);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setAutoCommit(
      boolean autoCommit
   ) throws SQLException
   {
      if ((m_bInTransaction) && (autoCommit))
      {
         throw new SQLException(
                      "Connection in transaction cannot be set to autocommit.");
      }
      setUsed(true);
      m_privateConnection.setAutoCommit(autoCommit);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setCatalog(
      String catalog
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.setCatalog(catalog);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setHoldability(
      int holdability
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.setHoldability(holdability);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setReadOnly(
      boolean readOnly
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.setReadOnly(readOnly);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Savepoint setSavepoint(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.setSavepoint();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Savepoint setSavepoint(
      String name
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.setSavepoint(name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setTransactionIsolation(
      int level
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.setTransactionIsolation(level);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setTypeMap(
      Map<String, Class<?>> map
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.setTypeMap(map);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      // This is not really a use of connection
      // setUsed(true);
      return m_privateConnection.toString();
   }

   // These methods were added in Java 1.6 /////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   @Override
   public Array createArrayOf(
      String   typeName, 
      Object[] elements
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createArrayOf(typeName, elements);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Blob createBlob(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createBlob();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Clob createClob(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createClob();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public NClob createNClob(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createNClob();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SQLXML createSQLXML(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createSQLXML();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Struct createStruct(
      String   typeName, 
      Object[] attributes
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createStruct(typeName, attributes);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Properties getClientInfo(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getClientInfo();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getClientInfo(
      String name
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getClientInfo(name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isValid(
      int timeout
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.isValid(timeout);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setClientInfo(
      Properties properties
   ) throws SQLClientInfoException
   {
      try
      {
         setUsed(true);
      }
      catch (SQLException exc)
      {
         SQLClientInfoException exc2 = new SQLClientInfoException();
         exc2.setNextException(exc);
         throw exc2;
      }
      m_privateConnection.setClientInfo(properties);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setClientInfo(
      String name, 
      String value
   ) throws SQLClientInfoException
   {
      try
      {
         setUsed(true);
      }
      catch (SQLException exc)
      {
         SQLClientInfoException exc2 = new SQLClientInfoException();
         exc2.setNextException(exc);
         throw exc2;
      }
      m_privateConnection.setClientInfo(name, value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isWrapperFor(
      Class<?> iface
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.isWrapperFor(iface);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T unwrap(
      Class<T> iface
   ) throws SQLException 
   {
      setUsed(true);
      return m_privateConnection.unwrap(iface);
   }

   // These methods were added in Java 1.7 /////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   @Override
   public void setSchema(
      String schema
   ) throws SQLException 
   {
      setUsed(true);
      m_privateConnection.setSchema(schema);
   }
	
   /**
    * {@inheritDoc}
    */
   @Override
   public String getSchema(
   ) throws SQLException 
   {
      setUsed(true);
      return m_privateConnection.getSchema();
   }
	
   /**
    * {@inheritDoc}
    */
   @Override
   public void abort(
      Executor executor
   ) throws SQLException 
   {
      setUsed(true);
      m_privateConnection.abort(executor);
   }
	
   /**
    * {@inheritDoc}
    */
   @Override
   public void setNetworkTimeout(
      Executor executor, 
      int      milliseconds
   ) throws SQLException 
   {
      setUsed(true);
      m_privateConnection.setNetworkTimeout(executor, milliseconds);
   }
	
   /**
    * {@inheritDoc}
    */
   @Override
   public int getNetworkTimeout(
   ) throws SQLException 
   {
      setUsed(true);
      return m_privateConnection.getNetworkTimeout();
   }
}
