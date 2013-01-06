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

import org.opensubsystems.core.data.ModifiableDataObject;
import org.opensubsystems.core.error.OSSException;

/**
 * This interface adds support for modifying data objects by the application.
 *
 * @author bastafidli
 */
public interface ModifiableDataController extends BasicDataController
{
   /**
    * Save data object.
    *
    * @param  data - data object to save
    * @return ModifiableDataObject - saved data object, null if user doesn't 
    *                                 have access to that data object granted
    * @throws OSSException - an error has occurred 
    * @throws RemoteException - required since this method can be called remotely
    */
   ModifiableDataObject save(
      ModifiableDataObject data
   ) throws OSSException,
            RemoteException;
}
