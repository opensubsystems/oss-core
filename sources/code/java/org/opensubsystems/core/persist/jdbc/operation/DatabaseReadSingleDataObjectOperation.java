/*
 * Copyright (C) 2006 - 2016 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.DatabaseFactory;
import org.opensubsystems.core.persist.jdbc.DatabaseSchema;

/**
 * Adapter to simplify writing of database reads which reads single data object, 
 * which takes care of requesting and returning connections, transaction 
 * management, query preparation and exception handling.
 *
 * Example of method in factory which reads single data object: 
 *
 *  public DataObject get(
 *     final long lId,
 *     final long lDomainId
 *  ) throws OSSException
 *  {
 *     DataObject data = null;
 *
 *     // If the ID is supplied try to read the data from the database, 
 *     // if it is not, it is new data which doesn't have ID yet
 *     if (lId == DataObject.NEW_ID)
 *     {
 *        // These values are used as default values for new data object
 *        data = new MyData(lDomainId);
 *     }
 *     else
 *     {
 *        int[] arrColumnCodes = getListDataDescriptor().getAllColumnCodes();
 *        
 *        DatabaseReadOperation dbop = new DatabaseReadSingleDataObjectOperation(
 *                this, m_schema.getSelectMyDataById(arrColumnCodes, true),
 *                 m_schema, iId, iDomainId);
 *        data = (DataObject)dbop.executeRead();
 *     }
 *
 *     return data;
 *  }
 *
 * @author OpenSubsystems
 */
public class DatabaseReadSingleDataObjectOperation extends DatabaseReadOperation
{
   // Attributes ///////////////////////////////////////////////////////////////

   /**
    * ID of data object.
    */
   private final long m_lId;

   /**
    * ID of domain the data object belongs to.
    */
   private final long m_lDomainId;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor to use when database update doesn't require any prepared
    * statement.
    * 
    * @param factory - factory which is executing this operation
    * @param strQuery - sql query that has to be processed
    * @param schema - database schema used with this operation
    * @param lId - ID of data object to read
    * @param lDomainId - ID of domain the data object belongs to
    */
   public DatabaseReadSingleDataObjectOperation(
      DatabaseFactory factory,
      String          strQuery,
      DatabaseSchema  schema,
      long            lId,
      long            lDomainId
   )
   {
      super(factory, strQuery, schema);
      
      m_lId = lId;
      m_lDomainId = lDomainId;
   }
   
   // Overwritten method ///////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected Object performOperation(
      DatabaseFactory     dbfactory, 
      Connection          cntConnection, 
      PreparedStatement   pstmQuery
   ) throws OSSException, SQLException
   {
      StringBuilder buffer = new StringBuilder();
      // construct error message
      buffer.append("Multiple records loaded from database for ID ");
      buffer.append(m_lId);
      if (m_dbschema.isInDomain())
      {
         buffer.append(" and domain ID ");
         buffer.append(m_lDomainId);
      }

      pstmQuery.setLong(1, m_lId);
      if (m_dbschema.isInDomain())
      {
         // set up domain ID parameter if data object is in domain
         pstmQuery.setLong(2, m_lDomainId);
      }

      return loadAtMostOneData(dbfactory, pstmQuery, buffer.toString());
   }
}
