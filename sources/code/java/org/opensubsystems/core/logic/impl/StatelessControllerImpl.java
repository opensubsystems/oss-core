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

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.StatelessController;
import org.opensubsystems.core.util.OSSObject;

/**
 * Implementation of StatelessController. This controller will be base class for 
 * all deriver controller implementation classes. 
 * 
 * @author OpenSubsystems
 */
public abstract class StatelessControllerImpl extends     OSSObject
                                                 implements StatelessController
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   // Logic ////////////////////////////////////////////////////////////////////   

   /**
    * {@inheritDoc}
    */
   public void constructor() throws OSSException
   {
   }
}
