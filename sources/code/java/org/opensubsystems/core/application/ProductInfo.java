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
 * Every product has set of attributes such as name, version, creator. These
 * are published through this interface.
 * 
 * Every product must provide class implementing this interface to provide 
 * information about itself.
 *  
 * @author bastafidli
 */
public interface ProductInfo
{
   /**
    * @return String - full name of the product to display to the user
    */
   String getName(
   );
   
   /**
    * @return String - full version of the product to display to the user, this 
    *                  should include major version, minor version as well as 
    *                  other information
    */
   String getVersion(
   );
   
   /**
    * @return String - full name of the entity which created this product.
    */   
   String getCreator(
   );

   /**
    * @return String - full copyright information for this product.
    */   
   String getCopyright(
   );
}
