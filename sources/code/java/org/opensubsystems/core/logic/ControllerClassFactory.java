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

package org.opensubsystems.core.logic;

import org.opensubsystems.core.util.ImplementationClassFactory;

/**
 * Class factory responsible for instantiation of controllers. The correct 
 * controller is instantiated based on the controller interface and currently 
 * active component model.
 * 
 * @author bastafidli
 */
public class ControllerClassFactory extends ImplementationClassFactory<StatelessController>
{
   /**
    * Constructor
    */
   public ControllerClassFactory()
   {
      super(StatelessController.class);
   }
}
