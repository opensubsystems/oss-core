/*
 * Copyright (C) 2005 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.logic.impl;

import org.opensubsystems.core.data.ModifiableDataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.ModifiableDataController;
import org.opensubsystems.core.persist.ModifiableDataFactory;

/**
 * Implementation of ModifiableDataController interface to manage data. It can 
 * be used by any component that returns from getDataFactory() method an 
 * instance of ModifiableDataFactory to access and modify the data. 
 *
 * @author bastafidli
 */
public abstract class ModifiableDataControllerImpl extends    BasicDataControllerImpl
                                                   implements ModifiableDataController
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public ModifiableDataObject save(
      ModifiableDataObject data
   ) throws OSSException
   {
      return ((ModifiableDataFactory)getDataFactory(data)).save(data);
   }
}
