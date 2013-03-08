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

package org.opensubsystems.core.util.jsp;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import org.opensubsystems.core.util.OSSObject;

/**
 * This class is a collection of useful methods when working with custom JSP 
 * tags.
 *
 * Many methods in this class are inspired by Struts classes
 * org.apache.struts.util.ResponseUtils
 * org.apache.struts.taglib.TagUtils
 *
 * @author bastafidli
 */
public final class TagUtils extends OSSObject
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private TagUtils(
   )
   {
      // Do nothing
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Filter the specified string for characters that are senstive to
    * HTML interpreters, returning the string with these characters replaced
    * by the corresponding character entities.
    *
    * @param value - the string to be filtered and returned
    * @return String - filtered value 
    */
   public static String filter(
       String value
    ) 
    {
       if (value == null) 
       {
           return (null);
       }

       char[] content = new char[value.length()];
       
       value.getChars(0, value.length(), content, 0);
       StringBuilder result = new StringBuilder(content.length + 50);
       
       for (int i = 0; i < content.length; i++) 
       {
           switch (content[i]) 
           {
              case ('<'):
              {
                 result.append("&lt;");
                 break;
              }
              case ('>'):
              {
                 result.append("&gt;");
                 break;
              }
              case ('&'):
              {
                 result.append("&amp;");
                 break;
              }
              case ('"'):
              {
                 result.append("&quot;");
                 break;
              }
              case ('\''):
              {
                 result.append("&#39;");
                 break;
              }
              default:
              {
                 result.append(content[i]);
                 break;
              }
           }
       }

       return result.toString();
   }


   /**
    * Write the specified text as the response to the writer associated with
    * this page.  
    * 
    * Note: If you are writing body content from the <code>doAfterBody()</code> 
    * method of a custom tag class that implements <code>BodyTag</code>, you 
    * should be calling <code>writePrevious()</code> instead.
    *
    * @param pageContext - the PageContext object for this page
    * @param text - the text to be written
    * @exception JspException - an error has occurred
    */
   public static void write(
      PageContext pageContext, 
      String      text
   )throws JspException 
   {
      JspWriter writer = pageContext.getOut();

      try 
      {
         writer.print(text);
      } 
      catch (IOException ioExc) 
      {
         throw new JspException(ioExc);
      }
   }


   /**
    * Write the specified text as the response to the writer associated with
    * the body content for the tag within which we are currently nested.
    *
    * @param pageContext - the PageContext object for this page
    * @param text - the text to be written
    * @exception JspException - an error has occurred
    */
   public static void writePrevious(
      PageContext pageContext, 
      String      text
   ) throws JspException 
   {
       JspWriter writer = pageContext.getOut();
       if (writer instanceof BodyContent) 
       {
           writer = ((BodyContent)writer).getEnclosingWriter();
       }
       try 
       {
           writer.print(text);
       } 
       catch (IOException ioExc) 
       {
           throw new JspException(ioExc);
       }
   }
}
