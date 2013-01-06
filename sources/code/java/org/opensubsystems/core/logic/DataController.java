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

package org.opensubsystems.core.logic;

import java.rmi.RemoteException;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;

/**
 * Base interface for all controllers managing data objects. The primary purpose 
 * of data controller is to implement additional functionality on top of the 
 * persistence layer such as security checks, logging, etc. Every data object 
 * has to be accessible somehow to be available to the presentation tier and 
 * controller, which makes data object accessible should implement this interface.
 *
 * @author bastafidli
 */
public interface DataController extends StatelessController
{
   /**
    * Get data object knowing just the unique id. This method will succeed only 
    * if the data object identified by specified id exists in the current 
    * domain, which is a domain identified by 
    * CallContext.getInstance().getCurrentDomainId(). 
    * If the object doesn't exist in the current domain, this method should 
    * throw an exception and shouldn't retrieve anything. If the client needs 
    * to retrieve data object in a different domain than the current one, it 
    * needs to provide for it its' own specific interface. 
    *
    * @param lId - ID of the data object to retrieve
    * @return DataObject - retrieved data object, null if the data object 
    *                       doesn't exists or if user doesn't have access to that 
    *                       data object granted
    * @throws OSSException - an error has occurred 
    * @throws RemoteException - required since this method can be called remotely
    */
   DataObject get(
      long lId
   ) throws OSSException,
            RemoteException;
}
