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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.StringUtils;
import org.opensubsystems.core.util.jsp.TagUtils;

/**
 * Custom tag to intelligently limit the length of a text to certain length.
 * If the length of the string needs to be limited, it will be cut if possible
 * on a word boundary and a ... will be appended to the end.
 *  
 * @author bastafidli
 */
public class StringLimitTag extends BodyTagSupport
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -6436264560155159257L;

   /**
    * Number representing the length to limit the text present in the body of 
    * the tag to. 
    */
   protected String m_strLimit;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for custom tag.
    */
   public StringLimitTag(
   ) 
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
      // Buffer the body since we need to have the whole body before we can
      // limit it's length
      return (EVAL_BODY_BUFFERED);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int doEndTag(
   ) throws JspException 
   {
      // Get the buffered body
      BodyContent content = getBodyContent();
      
      if (content != null)
      {
         int iLimit = Integer.parseInt(m_strLimit);
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert iLimit > 0 : "Limit has to be positive number.";
         }
         
         TagUtils.write(pageContext, 
                        StringUtils.limitStringLength(iLimit, content.getString()));
      }
      
      return (EVAL_PAGE);
   }
   
   /**
    * @return String - numeric limit representing length to limit the body text
    */
   public String getLimit(
   )
   {
      return m_strLimit;
   }
   
   /**
    * @param strLimit - numeric limit representing length to limit the body text to
    */
   public void setLimit(
      String strLimit
   )
   {
      m_strLimit = strLimit;
   }
}
