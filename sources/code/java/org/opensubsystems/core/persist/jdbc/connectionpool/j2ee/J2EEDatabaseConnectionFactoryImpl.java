/*
 * Copyright (C) 2005 - 2015 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.persist.jdbc.connectionpool.j2ee;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.Database;
import org.opensubsystems.core.persist.jdbc.connectionpool.impl.PooledDatabaseConnectionFactoryImpl;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.j2ee.J2EEUtils;
import org.opensubsystems.core.util.j2ee.J2EEUtils.J2EEServers;

/**
 * Base class for implementation of j2ee factories for retrieving and returning 
 * of database connections, which are maintained in a pool of always ready 
 * connections. 
 *
 * @author OpenSubsystems
 */
public class J2EEDatabaseConnectionFactoryImpl extends PooledDatabaseConnectionFactoryImpl
{
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(J2EEDatabaseConnectionFactoryImpl.class);
   
   /**
    * Prefix of the data source that will be used for all data sources 
    * specified in JBoss server.
    */
   public static final String DATASOURCE_NAME_PREFIX_JBOSS = "java:/";

   /**
    * Prefix of the data source that will be used for all data sources 
    * specified in IBM WebSphere server.
    * Originally WebSphere allowed to access datasources using JNDI name
    * jdbc/datasourcename. Since version 6 WebSPphere prints warning that
    * this naming convention and a full JNDI reference should be used, see
    * http://publib.boulder.ibm.com/infocenter/wasinfo/v6r0/topic/com.ibm.websphere.express.doc/info/exp/ae/rdat_jnditips.html
    * 
    * I have tried to change this to java:comp/env/jdbc/ as described in
    * http://publib.boulder.ibm.com/infocenter/wasinfo/v6r0/topic/com.ibm.websphere.express.doc/info/exp/ae/tdat_accdfac.html
    * but when the war file was deployed in WebSphere, the application couldn't
    * find the datasource.   
    */
   public static final String DATASOURCE_NAME_PREFIX_WEBSPHERE = "jdbc/";
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void initializeConnection(
      Connection cntDBConnection,
      boolean    bAutoCommit 
   ) throws SQLException
   {
      J2EEServers activeServerType = J2EEUtils.getJ2EEServerType();

      if (activeServerType == J2EEServers.J2EE_SERVER_JBOSS) 
      {
         try
         {
            // Do not call initializeConnection() method we are in transaction.
            // This is important to do only for JBoss server because there is 
            // problem to call setAutoCommit() method if transaction is in progress.
            if (!m_transactionFactory.isTransactionInProgress())
            {
               super.initializeConnection(cntDBConnection, bAutoCommit);
            }
            else
            {
               s_logger.log(Level.FINEST, "Ignoring request to set autocommit to {0}" 
                            + " since we are running inside of"
                            + " JBoss and transaction is in progress.", 
                            bAutoCommit);
            }
         }
         catch (OSSException ossExc)
         {
            throw new SQLException("Error while getting transaction status.");
         }
      }
      else
      {
         super.initializeConnection(cntDBConnection, bAutoCommit);
      }
   }

   // Helper bethods ///////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected Connection getPooledConnection(
      ConnectionPoolDefinition connectionpool
   ) throws OSSException
   {
      Connection conReturn;

      try
      {
         conReturn = ((DataSource)connectionpool.getConnectionPool()).getConnection();
      }
      catch (SQLException sqlExc)
      {
         throw new OSSDatabaseAccessException(
                      "Cannot get database connection from pool.", sqlExc);
      }
      
      return conReturn;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Connection getPooledConnection(
      ConnectionPoolDefinition connectionpool,
      String                   strUser,
      String                   strPassword
   ) throws OSSException
   {
      Connection conReturn;

      try
      {
         DataSource source;
         
         if (connectionpool != null)
         {
            source = (DataSource)connectionpool.getConnectionPool();
            if (source != null)
            {
               conReturn = source.getConnection(strUser, strPassword);
            }
            else
            {
               // This is a normal situation e.g. in Jonas when if the user 
               // doesn't exists Jonas doesn't initialize the datasource
               throw new OSSDatabaseAccessException(
                            "Connection pool " 
                            + connectionpool.getName()
                            + " doesn't exist. Maybe it wasn't initialized yet.");
            }
         }
         else
         {
            // This is a normal situation e.g. in Jonas when if the user doesn't
            // exists Jonas doesn't initialize the datasource
            throw new OSSDatabaseAccessException("Connection pool doesn't exist."
                                                 + " Maybe it wasn't initialized yet.");
         }
      }
      catch (SQLException sqlExc)
      {
         throw new OSSDatabaseAccessException(
                      "Cannot get database connection from pool for specified" 
                      + " user/password.", sqlExc);
      }
      
      return conReturn;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Object createConnectionPool(
      String   strConnectionPoolName,
      Database database,
      String   strDriverName,
      String   strUrl,
      String   strUser,
      String   strPassword,
      int      iTransactionIsolation
   ) throws OSSException
   {
      // Try to check if particular pool exists (connect to the data source)
      // and try to retrieve connection from it (and return immediately this 
      // connection). If it will pass it means that connection pool exists.
      InitialContext context      = null;
      DataSource     dsDataSource = null; 
      StringBuffer   dataSourceName = new StringBuffer();
      
      try
      {
         
         J2EEServers actualServerType = J2EEUtils.getJ2EEServerType();
         
         switch (actualServerType)
         {
            case J2EE_SERVER_JBOSS:
            {
               // Add prefix for JBoss server.
               dataSourceName.append(DATASOURCE_NAME_PREFIX_JBOSS);
               break;
            }
            case J2EE_SERVER_WEBSPHERE:
            {
               // Add prefix for IBM WebSphere server.
               dataSourceName.append(DATASOURCE_NAME_PREFIX_WEBSPHERE);
               break;
            }
            default:
            {
               // Default don't add prefix. For JOnAS and BEA WebLogic servers 
               // will be used pure datasource name.
               break;
            }
         }
         
         dataSourceName.append(strConnectionPoolName);
         
         s_logger.log(Level.FINEST, "Looking up datasource {0}", 
                      dataSourceName.toString());

         // Obtain the DataSource object associated with the logical name.
         context      = new InitialContext(); 
         dsDataSource = (DataSource) context.lookup(dataSourceName.toString());

         s_logger.log(Level.FINE, "Found datasource {0}", dataSourceName.toString());
         // Given the logical name for the resource, the lookup method returns 
         // the DataSource object that is bound to the JNDI name in the directory. 
     
         // Get the Connection object from the DataSource object.
      }
      catch (NamingException neExc)
      {
         s_logger.log(Level.FINEST, "Datasource {0} not found.", dataSourceName);
         // This has to be OSSDatabaseAccessException since we can detect it
         // if user we are using for connection doesn't exist. This exception
         // is encountered for example in Jonas if the user for the data source
         // is not created yet
         throw new OSSDatabaseAccessException(
                      "Error occurred while looking up data source.", 
                      neExc);
      }
      finally
      {
         if (context != null)
         {
             try
             {
                context.close();
             }
             catch (NamingException nExc)
             {
                s_logger.log(Level.WARNING, "Unable to close context", nExc);
             }
         }
      }

      return dsDataSource;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void destroyConnectionPool(
      ConnectionPoolDefinition connectionpool
   ) throws OSSException
   {
      // Here we cannot close connection pool because we have not created it in 
      // this class.
      // This method will do nothing.
   }
}
