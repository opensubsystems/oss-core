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

package org.opensubsystems.core.persist.jdbc;

import org.opensubsystems.core.util.OSSObject;
import org.opensubsystems.core.util.StringUtils;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Simple structure collecting all information about the database data source.
 * 
 * @author bastafidli
 */
public class DatabaseSourceDefinition extends OSSObject
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Name of the data sources.
    */
   protected String m_strName;

   /**
    * Database for which this datasource was created.
    */
   protected Database m_database;
   
   /**
    * JDBC driver user by this data source.
    */
   protected String m_strDriver;

   /**
    * Real JDBC driver user by this data source in case the above driver is just 
    * proxy.
    */
   protected String m_strRealDriver;
   
   /**
    * URL to connect to the database.
    */
   protected String m_strUrl;
   
   /**
    * User name to use to connect to the database.
    */
   protected String m_strUser;
   
   /**
    * Password to use to connect to the database.
    */
   protected String m_strPassword;
   
   /**
    * Transaction isolation that should be used for connections.
    */
   protected int m_iTransactionIsolation;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor
    * 
    * @param strName - name of the data source
    * @param database - database for this this data source is being created 
    * @param strDriver - JDBC driver user by this data source
    * @param strUrl - URL to connect to the database
    * @param strUser - User name to use to connect to the database
    * @param strPassword - Password to use to connect to the database
    * @param iTransactionIsolation - transaction isolation that should be 
    *                                used for connections
    */
   public DatabaseSourceDefinition(
      String   strName,
      Database database,
      String   strDriver,
      String   strUrl,
      String   strUser,
      String   strPassword,
      int      iTransactionIsolation
   )
   {
      super();
      
      m_strName = strName;
      m_database = database;
      m_strDriver = strDriver;
      m_strRealDriver = strDriver;
      m_strUrl = strUrl;
      m_strUser = strUser;
      m_strPassword = strPassword;
      m_iTransactionIsolation = iTransactionIsolation;
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * @return String
    */
   public String getName()
   {
      return m_strName;
   }
   
   /**
    * @return Database
    */
   public Database getDatabase()
   {
      return m_database;
   }
   
   /**
    * @return String
    */
   public String getDriver()
   {
      return m_strDriver;
   }
   
   /**
    * @return String
    */
   public String getRealDriver()
   {
      return m_strRealDriver;
   }

   /**
    * @param strRealDriver - the real JDBC driver used by this data source
    */
   public void setRealDriver(
      String strRealDriver
   )
   {
      m_strRealDriver = strRealDriver;
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
   public String getUrl()
   {
      return m_strUrl;
   }
   
   /**
    * @return String
    */
   public String getUser()
   {
      return m_strUser;
   }
   
   /**
    * @return int
    */
   public int getTransactionIsolation()
   {
      return m_iTransactionIsolation;
   }
   
   /**
    * {@inheritDoc}
    */
   public void toString(
      StringBuilder sb,
      int           ind
   )
   {
      append(sb, ind + 0, "DataSourceDefinition[");
      append(sb, ind + 1, "m_strName = ", m_strName);
      append(sb, ind + 1, "m_database = ", 
             m_database != null ? m_database.getDatabaseTypeIdentifier()
                                : StringUtils.NULL_STRING);
      append(sb, ind + 1, "m_strDriver = ", m_strDriver);
      append(sb, ind + 1, "m_strRealDriver = ", m_strRealDriver);
      append(sb, ind + 1, "m_strUrl = ", m_strUrl);
      append(sb, ind + 1, "m_strUser = ", m_strUser);
      append(sb, ind + 1, "m_strPassword = ", m_strPassword);
      append(sb, ind + 1, "m_iTransactionIsolation = ", 
             DatabaseUtils.convertTransactionIsolationFromConstant(
                              m_iTransactionIsolation));
      append(sb, ind + 0, "]");
   }
}
