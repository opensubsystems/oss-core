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

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.opensubsystems.core.util.Messages;
import org.opensubsystems.core.util.jsp.TagUtils;

/**
 * Custom tag to generate JavaScript array with list of messages, which should 
 * be displayed on the UI. The result will be two JavaScript arrays. The 
 * arrMessages will contain the messages and the arrMessageTypes will contain
 * the types of messages (error, info).
 *  
 * @author bastafidli
 */
public class MessageListTag extends TagSupport
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 2757149284634427196L;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for custom tag.
    */
   public MessageListTag() 
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
      TagUtils.write(pageContext, generateMessageList(pageContext));
      
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
    * Generate JavaScript representing list of messages.
    * 
    * @param pageContext - context of the current page
    * @return String - JavaScript representing list of messages
    */
   public static String generateMessageList(
      PageContext pageContext
   )
   {
      StringBuilder sbHtml = new StringBuilder();
      Messages msgs = (Messages)pageContext.getRequest().getAttribute(
                          MessageTag.MESSAGES_REQUEST_PARAM);
      
      sbHtml.append("var arrMessages     = new Array();\n" + 
                    "var arrMessageTypes = new Array();\n");
      
      if (msgs != null)
      {                           
         List lstErrors = msgs.getAllErrorMessages();
         List lstInfos = msgs.getInfoMessages();
         int  iIndex = 0;
         Iterator msgIter;

         if ((lstErrors != null) && (!lstErrors.isEmpty()))
         {
            for (msgIter = lstErrors.iterator(); msgIter.hasNext(); iIndex++)
            {
               sbHtml.append("arrMessages[");
               sbHtml.append(iIndex);
               sbHtml.append("] = \"");
               sbHtml.append(msgIter.next().toString());
               sbHtml.append("\"\n");
               sbHtml.append("arrMessageTypes[");
               sbHtml.append(iIndex);
               sbHtml.append("] = \"error\";\n");
            }
         }
         if ((lstInfos != null) && (!lstInfos.isEmpty()))
         {
            for (msgIter = lstInfos.iterator(); msgIter.hasNext(); iIndex++)
            {
               sbHtml.append("arrMessages[");
               sbHtml.append(iIndex);
               sbHtml.append("] = \"");
               sbHtml.append(msgIter.next().toString());
               sbHtml.append("\"\n");
               sbHtml.append("arrMessageTypes[");
               sbHtml.append(iIndex);
               sbHtml.append("] = \"info\";\n");
            }
         }
      }
      
      return sbHtml.toString();
   }
}
