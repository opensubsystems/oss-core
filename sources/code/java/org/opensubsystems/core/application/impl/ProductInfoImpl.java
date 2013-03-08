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

package org.opensubsystems.core.application.impl;

import org.opensubsystems.core.application.ProductInfo;
import org.opensubsystems.core.util.OSSObject;

/**
 * Simple implementation of ProductInfo interface
 * 
 * @author bastafidli
 */
public class ProductInfoImpl extends OSSObject
                             implements ProductInfo
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Full copyright information for this product.
    */
   protected String m_strCopyright;
   
   /**
    * Full name of the entity which created this product.
    */
   protected String m_strCreator;
   
   /**
    * Full name of the product to display to the user.
    */
   protected String m_strName;
   
   /**
    * Full version of the product to display to the user, this should include 
    * major version, minor version as well as other information.
    */
   protected String m_strVersion;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Full constructor.
    * 
    * @param strName - full name of the product to display to the user
    * @param strVersion - full version of the product to display to the user, 
    *                     this should include major version, minor version as 
    *                     well as other information
    * @param strCreator - full name of the entity which created this product
    * @param strCopyright - full copyright information for this product
    */
   public ProductInfoImpl(
      String strName, 
      String strVersion,
      String strCreator, 
      String strCopyright 
   )
   {
      super();

      m_strName = strName;
      m_strVersion = strVersion;
      m_strCreator = strCreator;
      m_strCopyright = strCopyright;
   }

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   @Override
   public String getCopyright()
   {
      return m_strCopyright;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getCreator()
   {
      return m_strCreator;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName()
   {
      return m_strName;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getVersion()
   {
      return m_strVersion;
   }
}
