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

package org.opensubsystems.core.logic;

import java.rmi.RemoteException;

import org.opensubsystems.core.error.OSSException;

/**
 * Base interface for all stateless controllers. This interface will be base 
 * interface for all derived controller interfaces so that they can be easily
 * identified and in the future we can add methods required by all controllers 
 * here. 
 *
 * @author bastafidli
 */
public interface StatelessController
{
   /**
    * Method overridden in each controller and called by controller manager
    * when the controller is created. Controller should place any initialization 
    * call into the implementation of this method instead of into constructor 
    * since this method is called at the correct time when the controller is 
    * created.    
    *  
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   void constructor(
   ) throws OSSException,
            RemoteException;
}
