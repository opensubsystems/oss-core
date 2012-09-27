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

package org.opensubsystems.core.persist;

import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;

/**
 * Base interface for all data factories responsible for loading and persisting 
 * data. Data factory is here to implement Data Access Object pattern as 
 * described in 
 * http://java.sun.com/blueprints/corej2eepatterns/Patterns/DataAccessObject.html
 * or more generically the Abstract Factory pattern as described by GoF95.
 * It's main purpose is to persist, retrieve and change the persisted data 
 * objects in the underlying persistence store without exposing any persistence 
 * store dependent information to the rest of the application.
 * 
 * This interface doesn't dictate implementation therefore it is possible
 * to have FileDataFactory, DatabaseDataFactory, etc.
 *
 * @author bastafidli
 */
public interface DataFactory
{   
   /**
    * Get class identifying the data descriptor describing the type of data 
    * objects managed by the data factory.
    * 
    * @return Class
    */
   Class<DataDescriptor> getDataDescriptorClass(
   );
   
   /**
    * Get instance of the data descriptor describing the type of data objects
    * managed by the data factory.
    * 
    * @return DataDescriptor
    */
   DataDescriptor getDataDescriptor(
   );

   /**
    * Get specific data object identified by its id from the persistence store.
    * 
    * Every data factory has to support this method otherwise we wouldn't have 
    * any way how to load and test existence of the data.
    *
    * @param  iId - id of the data object to get, if the id is NEW_ID a new data
    *               object initialized to default values
    * @param  iDomainId - if the data object exists in a domain then by specifying 
    *                     the domain id it allows the persistence store to limit 
    *                     the data that will be searched and also possibly 
    *                     enforce in what domain the id can possibly exist. This
    *                     allows to enforce security on the persistence layer 
    *                     that by ensuring that if the data object doesn't exist 
    *                     in the domain where it is expected to exist, it will 
    *                     not be even retrieved. If the data object doesn't exist
    *                     in the domain, you can pass DataObject.NEW_ID here
    *                     since it won't be used.
    * @return DataObject - specified data object or null if it couldn't be 
    *                      retrieved
    * @throws OSSException - an error while getting data
    */
   DataObject get(
      int iId,
      int iDomainId
   ) throws OSSException;
}
