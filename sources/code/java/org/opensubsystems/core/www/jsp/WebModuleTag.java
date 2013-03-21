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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.opensubsystems.core.application.Module;
import org.opensubsystems.core.application.impl.ApplicationImpl;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.WebConstants;
import org.opensubsystems.core.util.jsp.TagUtils;
import org.opensubsystems.core.www.WebModule;

/**
 * Custom tag to render visual representation of a web module in the page. The 
 * default visual representation of a web module is a tab on a toolbar. 
 *  
 * @author OpenSubsystems
 */
public class WebModuleTag extends BlockElementTag
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Name of the active module. If the currently rendered module matches this 
    * name it will be rendered as an active module.
    */ 
   protected String m_strActiveModule; 

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for custom tag.
    */
   public WebModuleTag() 
   {
      super("", BlockElementTag.DIV_BLOCK_ELEMENT);
      
      m_strActiveModule = null;
   }

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   @Override
   public int doStartTag(
   ) throws JspException
   {
      // Figure out which web modules are present and will be displayed as 
      // module toolbar tabs
      String        contextpath;
      StringBuilder sbHtml = new StringBuilder();
      Map           mpModules = null;
      
      contextpath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();

      try
      {
         mpModules = ApplicationImpl.getInstance().getModules();         
      }
      catch (OSSException osseExc)
      {
         throw new JspException("An unexpected exception has occurred.", osseExc);
      }

      if ((mpModules != null) && (!mpModules.isEmpty()))
      {
         Iterator  itDefinitions;
         Module    module;
         WebModule webModule;
         int       iIndex = 1;
         
         StringBuffer sbIconFilePath = new StringBuffer(); 
         URL          fileURL;

         /*
            <logic:equal name="activemodule" value="security">
               <div id="securitytab" 
                    class="clsToolbarButtonActive"><a href="<%=contextpath%>/osssecurity" 
                    title="Security"><img src="/security/images/securityicon.gif" 
                    align="middle" border="0"> Security</a></div>
            </logic:equal>
            <logic:notEqual name="activemodule" value="security">
               <div id="securitytab" 
                    class="clsToolbarButton"><a href="<%=contextpath%>/ossportal" 
                    title="Security"><img src="/security/images/securityicon.gif" 
                    align="middle" border="0"> Security</a></div>
            </logic:notEqual>      
          */

         // Go through all definitions and generate particular tabs
         for (itDefinitions = mpModules.values().iterator();
             itDefinitions.hasNext();)
         {
            module = (WebModule)itDefinitions.next();
            // There can be all kinds of modules present in the application so 
            // take into account only web modules 
            if (module instanceof WebModule)
            {
               webModule = (WebModule)module;
               sbHtml.append("<");
               sbHtml.append(m_strType);
               sbHtml.append(" id=\"module");
               sbHtml.append(iIndex++);
               sbHtml.append("\" class=\"clsToolbarButton");
               if ((m_strActiveModule != null) 
                    && (m_strActiveModule.equals(webModule.getIdentifier())))
               {
                  sbHtml.append("Active");
               }
               sbHtml.append("\"><a href=\"");
               sbHtml.append(contextpath);
               sbHtml.append(WebConstants.URL_SEPARATOR_CHAR);
               sbHtml.append(webModule.getURL());
               sbHtml.append("\" title=\"");
               sbHtml.append(webModule.getTooltip());
               sbHtml.append("\">");
               
               // construct full path for icon file that will be shown 
               // within the module tab before tab label
               // if icon file does not exist, no icon will be shown
               sbIconFilePath.append(WebConstants.URL_SEPARATOR_CHAR);
               sbIconFilePath.append(webModule.getIdentifier());
               sbIconFilePath.append(WebConstants.URL_SEPARATOR_CHAR);
               sbIconFilePath.append("images");
               sbIconFilePath.append(WebConstants.URL_SEPARATOR_CHAR);
               sbIconFilePath.append(webModule.getIdentifier());
               sbIconFilePath.append("icon.gif");
   
               try
               {
                  fileURL = pageContext.getServletContext().getResource(
                               sbIconFilePath.toString());
               }
               catch (MalformedURLException eURLExc)
               {
                  // do not throw exception, just consider that file doesn't exist 
                  fileURL = null;
               }
               
               if (fileURL != null)
               {
                  // generate code for showing image only if particular image file 
                  // exists on specified path
                  sbHtml.append("<img src=\"");
                  sbHtml.append(sbIconFilePath);
                  sbHtml.append("\" align=\"middle\" border=\"0\"> ");
               }
               // clear string buffer for next usage
               sbIconFilePath.delete(0, sbIconFilePath.length());
               
               sbHtml.append(webModule.getName());
               sbHtml.append("</a></");
               sbHtml.append(m_strType);
               sbHtml.append(">");
            }
         }
         TagUtils.write(pageContext, sbHtml.toString());
      }

      return super.doStartTag();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int doEndTag(
   ) throws JspException
   {
      // And now generate the default end of the element
      return super.doEndTag();
   }
   
   /**
    * @return String - name of the active module. If the currently rendered 
    *                  module matches this name it will be rendered as an 
    *                  active module.
    */
   public String getActiveModule(
   )
   {
      return m_strActiveModule;
   }

   /**
    * @param strActiveModule - name of the active module. If the currently 
    *                          rendered module matches this name it will be 
    *                          rendered as an active module.
    */
   public void setActivemodule(
      String strActiveModule
   )
   {
      m_strActiveModule = strActiveModule;
   }
}
