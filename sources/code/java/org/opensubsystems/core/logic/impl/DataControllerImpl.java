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

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.DataController;
import org.opensubsystems.core.persist.DataFactory;
import org.opensubsystems.core.util.CallContext;

/**
 * Implementation of DataController interface to access data. It can be used
 * by any component which provides DataFactory to access the data. 
 *
 * @author bastafidli
 */
public abstract class DataControllerImpl extends    StatelessControllerImpl
                                           implements DataController
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * This implementation will get only the default data object supported 
    * by this controller in case the controller supports multiple data types.
    * 
    * @param lId {@inheritDoc}
    * @return DataObject {@inheritDoc}
    * @throws OSSException {@inheritDoc}
    */
   public DataObject get(
      long lId
   ) throws OSSException
   {
      return getDataFactory().get(lId, 
                CallContext.getInstance().getCurrentDomainId());
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Get data factory which can be used to access persistence layer for a data
    * object managed by this controller. In case the controller supports 
    * multiple data types this method should return the factory for the default
    * data type.
    * 
    * @return DataFactory
    */
   protected abstract DataFactory getDataFactory(
   );
}
