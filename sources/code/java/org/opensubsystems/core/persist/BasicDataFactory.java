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

package org.opensubsystems.core.persist;

import java.util.Collection;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;

/**
 * Basic operations to perform with the data object in the persistence store.
 * It just defines retrieve, create and delete operations since these are the
 * basic actions we need to perform with any kind of data to at least implement 
 * tests.
 *
 * @author bastafidli
 */
public interface BasicDataFactory extends DataFactory
{
   /**
    * Create data object in the persistence store.
    *
    * @param  data - data to create
    * @return DataObject - newly created data
    * @throws OSSException - an error while creating data
    */
   DataObject create(
      DataObject data
   ) throws OSSException;

   /**
    * Create collection of data objects. This method is explicitly defined to 
    * allow optimized handling of bulk inserts. 
    *
    * @param colDataObject - collection of data objects that will be created
    * @return int - number of inserted data items
    * @throws OSSException - error during create
    */
   int create(
      Collection<DataObject> colDataObject
   ) throws OSSException;

   /**
    * Delete specified data.
    *
    * @param  lId - id of the data object to be deleted
    * @param  lDomainId - if the data object exists in a domain then by specifying 
    *                     the domain id it allows the persistence store to limit 
    *                     the data that will be searched and also possibly 
    *                     enforce in what domain the id can possibly exist. This
    *                     allows to enforce security on the persistence layer 
    *                     that by ensuring that if the data object doesn't exist 
    *                     in the domain where it is expected to exist, it will 
    *                     not be even deleted. If the data object doesn't exist
    *                     in the domain, you can pass DataObject.NEW_ID here
    *                     since it won't be used.
    * @throws OSSException - an error has occurred deleting data
    */
   void delete(
      long lId,
      long lDomainId
   ) throws OSSException;
}
