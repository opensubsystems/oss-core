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
import org.opensubsystems.core.logic.BasicDataController;
import org.opensubsystems.core.persist.BasicDataFactory;
import org.opensubsystems.core.util.CallContext;

/**
 * Implementation of BasicDataController interface to manage data. It can be 
 * used by any component that returns from getDataFactory() method an instance 
 * of BasicDataFactory to access the data. 
 *     
 * @author bastafidli
 */
public abstract class BasicDataControllerImpl extends    DataControllerImpl
                                                implements BasicDataController
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public DataObject create(
      DataObject data
   ) throws OSSException
   {
      return getDataFactory(data).create(data);
   }

   /**
    * This implementation will delete only the default data object supported 
    * by this controller in case the controller supports multiple data types.
    * 
    * @param iId {@inheritDoc}
    * @throws OSSException {@inheritDoc}
    */
   public void delete(
      int iId
   ) throws OSSException
   {
      ((BasicDataFactory)getDataFactory()).delete(iId, 
         CallContext.getInstance().getCurrentDomainId());      
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Get data factory which can be used to access persistence layer for a data
    * object managed by this controller. In case the controller supports 
    * multiple data types this method should look at the type of the data and 
    * return the factory supporting given data type.
    * 
    * @param data - data object which will be manipulated using the returned factory
    * @return DataFactory - data factory for given data object
    */
   protected abstract BasicDataFactory getDataFactory(
      DataObject data
   );
}
