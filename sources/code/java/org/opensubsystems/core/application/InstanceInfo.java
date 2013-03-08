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

package org.opensubsystems.core.application;

/**
 * This interface represents unique application instance on the computer or 
 * network. In general unique identification of application instance is an IP 
 * address of the machine where it is running. In some cases we may want to run 
 * multiple applications on the same machine (e.g. when allowing two users use 
 * the same machine in multi-terminal environment) and in that case the IP 
 * address is not unique and must be customized by each instance (e.g. using 
 * port, some sequence, etc.). Therefore this class is used as an abstraction of 
 * information that identify application instance.
 * 
 * @author bastafidli
 */
public interface InstanceInfo
{
   /**
    * Get the information uniquely identifying the instance of the application.
    * 
    * @return String
    */
   String getInfo(
   );

   /**
    * Set new application instance info.
    * 
    * @param strLocation - new application instance to set.
    */
   void setInfo(
      String strLocation
   );
}
