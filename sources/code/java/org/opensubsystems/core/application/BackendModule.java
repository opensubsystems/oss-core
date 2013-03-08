/*
 * Copyright (C) 2007 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.application;

import org.opensubsystems.core.logic.StatelessController;
import org.opensubsystems.core.persist.DataFactory;

/**
 * Module interface suitable for representing backend module exposing their 
 * business logic and data access layer but not their persistence mechanism or 
 * user interface.   
 * 
 * @author bastafidli
 */
public interface BackendModule extends Module
{
   /**
    * @return StatelessController[] - Controllers provided by this subsystem
    */
   StatelessController[] getControllers();

   /**
    * @return DataFactory[] - data factories provided by this subsystem
    */
   DataFactory[] getFactories();
}
