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
 
package org.opensubsystems.core.data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Base interface for all data objects that can be uniquely identified, tracked 
 * when they were created and which belongs to some partition called domain.
 * 
 * @author bastafidli
 */
public interface BasicDataObject extends DataObject,
                                         Serializable
{
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Check if this record was loaded from some persistence store  and therefore 
    * it is existing data object which was already created in the persistence 
    * store or it came from somewhere else (was created in memory).
    *
    * @return boolean
    */
   boolean isFromPersistenceStore(
   );

   /**
    * Set flag that this record was loaded from some persistence store.
    */
   void setFromPersistenceStore(
   );
   
   /**
    * Get unique identification of partition where the instance of this data
    * belongs to.
    * 
    * @return long
    */
   long getDomainId(
   ); 
   
   /**
    * Set the unique id for this instance of the data if it wasn't set yet.
    * 
    * @param lNewId - new id of the data object
    */
   void setId(
      long lNewId
   ); 

   /**
    * Get the creation timestamp, which is timestamp when the data object was 
    * created.
    *
    * @return Timestamp
    */
   Timestamp getCreationTimestamp(
   );
   
   /**
    * Set the creation timestamp, which is timestamp when the data object was 
    * created.
    *
    * @param creationTimestamp - new creation timestamp
    */
   void setCreationTimestamp(
      Timestamp creationTimestamp
   );
}
