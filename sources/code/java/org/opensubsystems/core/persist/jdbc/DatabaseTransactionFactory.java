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

import java.sql.Connection;
import java.sql.SQLException;

import org.opensubsystems.core.util.jta.TransactionFactory;

/**
 * Interface to extending the standard transaction factory with database related 
 * transaction activities.
 * 
 * @author bastafidli
 */
public interface DatabaseTransactionFactory extends TransactionFactory
{
   /**
    * Commit implicit transaction for given connection. This operation succeeds
    * only if explicit UserTransaction is not in progress. Otherwise it has no 
    * effect. The purpose is that a given component doesn't know if a larger
    * transaction is in progress or not. Therefore it asks the database 
    * transaction factory to commit the transaction based on the connection and
    * the transaction factory since it is aware of the larger transaction may
    * decide to commit the transaction based on connection if no larger 
    * transaction is in progress or do not commit it and wait until the larger
    * transaction is committed. 
    * 
    * @param cntConnection - the connection to commit transaction for.
    * @throws SQLException - an error has occurred
    */
   void commitTransaction(
      Connection cntConnection
   ) throws SQLException;

   /**
    * Rollback implicit transaction for given connection. This operation succeeds
    * only if explicit UserTransaction is not in progress. Otherwise it has no 
    * effect. The purpose is that a given component doesn't know if a larger
    * transaction is in progress or not. Therefore it asks the database 
    * transaction factory to rollback the transaction based on the connection and
    * the transaction factory since it is aware of the larger transaction may
    * decide to rollback the transaction based on connection if no larger 
    * transaction is in progress or doo not rollback it and wait until the larger
    * transaction is rolled back.  
    * 
    * @param cntConnection - the connection to rollback transaction for.
    * @throws SQLException - an error has occurred
    */
   void rollbackTransaction(
      Connection cntConnection
   ) throws SQLException;
}
