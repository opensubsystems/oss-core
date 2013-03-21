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

import java.util.Stack;

import javax.servlet.jsp.JspException;

import org.opensubsystems.core.util.jsp.TagUtils;


/**
 * Custom tag to generate all HTML code necessary to display block element (DIV
 * or SPAN), for which we can set id and style and the ID is remembered so that
 * child containers can access it and use it to generate compound IDs.
 *  
 * @author bastafidli
 */
public class BlockElementTag extends PageElementCacheTag
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Name of the attribute representing stack storing ID of the current element.
    */
   public static final String CURRENT_ELEMENT_ID = "currentelementid";
   
   /**
    * DIV block element.
    */
   public static final String DIV_BLOCK_ELEMENT = "div";
   
   /**
    * SPAN block element.
    */
   public static final String SPAN_BLOCK_ELEMENT = "span";
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -5162089287834734148L;

   /**
    * Class for the the whole element.
    */
   protected String m_strCssclass;
   
   /**
    * Additional CSS style for the the whole element.
    */
   protected String m_strStyle;
   
   /**
    * Type of the block element.
    */
   protected String m_strType;
   
   /**
    * Suffix to append to id when generating HTML.
    */
   protected String m_strIdSuffix;

   /**
    * Flag signaling if the id of this element is related to the id of the 
    * parent element. If true then the current id is concatenated with the parent
    * id when any code is being generated. If false then only the current id as 
    * setup for this element is being used. Null means false to speed up the code.
    */
   protected String m_strRelatedIds;
   
   /**
    * The original value of m_strId if it was changed due to m_bRelatedIds.
    */
   private String m_strOriginalId;
   
   /**
    * Flag signaling if ID was stored in the stack or not.
    */
   private boolean m_bIdStored;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for custom tag.
    * 
    * @param strCssclass - initial class of the element
    * @param strType - type of tag - DIV or SPAN
    */
   public BlockElementTag(
      String strCssclass,
      String strType
   ) 
   {
      super();
      
      // Do not use null, since if id is not set, we don't want to get NPE 
      m_strCssclass = strCssclass;
      m_strStyle = "";
      m_strType = strType;
      m_strIdSuffix = "";
      m_bIdStored = false;
      // By default we do not want related ids since this is new feature and 
      // most code is not aware of it so we do not want to break code which 
      // expect ids to have some specific value
      m_strRelatedIds = null; 
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int doStartTag(
   ) throws JspException 
   {
      StringBuilder sbHtml = new StringBuilder();

      adjustId();
      
      if ((m_strType == null) || (m_strType.length() == 0))
      {
         m_strType = DIV_BLOCK_ELEMENT;
      }
      
      // Generate the start of the element 
      sbHtml.append("<");
      sbHtml.append(m_strType);
      sbHtml.append(" ");
      if ((m_strId != null) && (m_strId.length() > 0))
      {
         sbHtml.append(" id=\"");
         sbHtml.append(m_strId);
         if ((m_strIdSuffix != null) && (m_strIdSuffix.length() > 0))
         {
            sbHtml.append(m_strIdSuffix);
         }
         sbHtml.append("\"");
      }
      if ((m_strCssclass != null) && (m_strCssclass.length() > 0))
      {
         sbHtml.append(" class=\"");
         sbHtml.append(m_strCssclass);
         sbHtml.append("\"");
      }
      if ((m_strStyle != null) && (m_strStyle.length() > 0))
      {
         sbHtml.append(" style=\"");
         sbHtml.append(m_strStyle);
         sbHtml.append("\"");
      }
      sbHtml.append(">");
      
      pushCurrentId();
      TagUtils.write(pageContext, sbHtml.toString());
      
      return (EVAL_BODY_INCLUDE);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int doEndTag(
   ) throws JspException 
   {         
      // Finish the element
      StringBuilder sbHtml =  new StringBuilder();
      
      sbHtml.append("</");
      sbHtml.append(m_strType);
      sbHtml.append(">");
      
      popCurrentId();
      TagUtils.write(pageContext, sbHtml.toString());
      
      restoreId();
      
      return (EVAL_PAGE);
   }
   
   /**
    * @return String - Class for the the whole element
    */
   public String getCssclass(
   )
   {
      return m_strCssclass;
   }

   /**
    * @return String - type of the block element
    */
   public String getType(
   )
   {
      return m_strType;
   }

   /**
    * @return String - Additional CSS style for the the whole element.
    */
   public String getStyle()
   {
      return m_strStyle;
   }
   
   /**
    * @param strCssclass - Class for the the whole element
    */
   public void setCssclass(
      String strCssclass
   )
   {
      m_strCssclass = strCssclass;
   }

   /**
    * @param strType - type of the block element
    */
   public void setType(
      String strType
   )
   {
      m_strType = strType;
   }
   
   /**
    * @param style - Additional CSS style for the the whole element.
    */
   public void setStyle(
      String style
   )
   {
      m_strStyle = style;
   }

   /**
    * @return String - If id of this and parent tag should be related then this 
    *                  attribute should say true or 1.
    */
   public String getRelatedids(
   )
   {
      return m_strRelatedIds;
   }

   /**
    * @param strRelatedIds - If id of this and parent tag should be related then 
    *                        this attribute should say true or 1.
    */
   public void setRelatedids(
      String strRelatedIds
   )
   {
      m_strRelatedIds = strRelatedIds;
   }
   
   /**
    * @param bRelatedIds - If id of this and parent tag should be related then 
    *                      this attribute should say true or 1.
    */
   public void setRelatedids(
      boolean bRelatedIds
   )
   {
      m_strRelatedIds = Boolean.toString(bRelatedIds);
   }

   /**
    * @return boolean - true if the id of this and parent tag are related.
    */
   public boolean isRelatedIdsTag(
   )
   {
      return ((Boolean.TRUE.toString().equalsIgnoreCase(m_strRelatedIds))
             || ("1".equals(m_strRelatedIds))); 
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Store to the current id to be used by nested elements. This method
    * is usually called from the doStartTag method.
    */
   protected void pushCurrentId(
   )
   {
      Object objTemp;
      Stack  ids;

      if ((m_strId != null) && (m_strId.length() > 0) && (!m_bIdStored))
      {
         // Store the ID only if it makes sense to store it
         objTemp = pageContext.getAttribute(CURRENT_ELEMENT_ID);
         if (objTemp == null)
         {
            ids = new Stack();
            pageContext.setAttribute(CURRENT_ELEMENT_ID, ids);
         }
         else
         {
            ids = (Stack)objTemp;
         }
         ids.push(m_strId);
         m_bIdStored = true;
      }      
   }
   
   /**
    * Restore to the current id to be used by nested elements. This method
    * is usually called from the doEndTag method.
    * 
    * @throws JspException - id was stored but cannot be found anymore
    */
   protected void popCurrentId(
   ) throws JspException
   {
      Object objTemp;
      Stack  ids;

      if (m_bIdStored)
      {
         // Store the ID only if it makes sense to store it
         objTemp = pageContext.getAttribute(CURRENT_ELEMENT_ID);
         if (objTemp == null)
         {
            throw new JspException("Cannot find element " + CURRENT_ELEMENT_ID 
                                   + " in page context even though it should be there.");
         }
         else
         {
            ids = (Stack)objTemp;
         }
         ids.pop();
         m_bIdStored = false;
      }      
   }

   /**
    * Restore to the current id to be used by nested elements. This method
    * is usually called from the doEndTag method.
    * 
    * @return String - current id pushed by parent element or empty string if
    *                  none, never null 
    */
   protected String getCurrentId(
   )
   {
      Object objTemp;
      Stack  ids;
      String strCurrentId = "";

      // Store the ID only if it makes sense to store it
      objTemp = pageContext.getAttribute(CURRENT_ELEMENT_ID);
      if (objTemp != null)
      {
         ids = (Stack)objTemp;
         if (!ids.empty())
         {
            strCurrentId = (String)ids.peek();            
         }
      }
      
      return strCurrentId;
   }
   
   /**
    * Set up flag for stored ID
    * @param bIdStored - flag signaling if id is stored or not 
    */
   protected void setStoredId(
      boolean bIdStored
   )
   {
      m_bIdStored = bIdStored;
   }
   
   /**
    * Checks if the ids of this and parent tags are related and if they are, 
    * change the id of this tag to be concatenation with the parent id. If they 
    * are not, this method will not do anything. The restoreId method will 
    * reverse this change. 
    */
   protected void adjustId(
   )
   {
      if ((m_strRelatedIds != null) && (isRelatedIdsTag()))
      {
         StringBuilder buffer = new StringBuilder(getCurrentId());
         
         buffer.append(m_strId);
         
         m_strOriginalId = m_strId;
         m_strId = buffer.toString();
      }
   }
   
   /**
    * Restore the changed (if any) made by adjustId method. 
    */
   protected void restoreId(
   )
   {
      if (m_strOriginalId != null)
      {
         m_strId = m_strOriginalId;
      }
   }
}
