/*
 * Copyright (C) 2006 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;


/**
 * Custom tag to initialize the web module rendering. This needs to be the first 
 * tag of all web module tags and should be placed in the page before the html 
 * tag.
 *  
 * @author OpenSubsystems
 */
public class WebModuleInitTag extends PageElementCacheTag
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 5887888143382459204L;
   
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Constructor for custom tag.
    */
   public WebModuleInitTag() 
   {
      super();
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int doStartTag(
   ) throws JspException 
   {
      String        contextpath;
      StringBuilder sbBuffer = new StringBuilder();

      contextpath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();

      /*
      <link rel="StyleSheet" type="text/css" media="screen"
            href="<%=contextpath%>/core/css/tabtoolbar.css" >
      */
      sbBuffer.append("<link href=\"");
      sbBuffer.append(contextpath);
      sbBuffer.append("/core/css/tabtoolbar.css\" rel=\"StyleSheet\"" +
                      " type=\"text/css\" media=\"screen\">");            
      cache(PageElementCacheTag.CSS_ELEMENT, sbBuffer.toString());
               
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
}
