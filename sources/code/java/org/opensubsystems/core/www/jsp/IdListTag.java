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
 
package org.opensubsystems.core.www.jsp;

import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.opensubsystems.core.util.DataObjectUtils;
import org.opensubsystems.core.util.jsp.TagUtils;

/**
 * Custom tag to construct string of ids of collection of data objects presented 
 * in a request separated by ','.
 *  
 * @author OpenSubsystems
 */
public class IdListTag extends TagSupport
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 6752629609523601805L;

   /**
    * Name of the request attribute containing collection of data objects whose 
    * list of ids should be constructed. Required.
    */
   protected String m_strName; 
   
   // Constructor //////////////////////////////////////////////////////////////
   
   /**
    * Constructor for custom tag.
    */
   public IdListTag() 
   {
      super();

      m_strName = ""; 
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int doStartTag(
   ) throws JspException 
   {
      Collection colItems;
      
      colItems = (Collection)pageContext.getRequest().getAttribute(m_strName);
      if ((colItems != null) && (!colItems.isEmpty()))
      {                           
         TagUtils.write(pageContext, 
                        DataObjectUtils.parseCollectionIdsToString(colItems, ","));
      }
      return (SKIP_BODY);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int doEndTag(
   ) throws JspException 
   {
      return (EVAL_PAGE);
   }

   /**
    * Returns the name
    * 
    * @return name
    */
   public String getName()
   {
      return m_strName;
   }

   /**
    * Sets the name
    * 
    * @param strName - name that will be set 
    */
   public void setName(
      String strName
   )
   {
      m_strName = strName;
   }
}
