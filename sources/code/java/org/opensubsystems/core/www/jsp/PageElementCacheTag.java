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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.jsp.TagUtils;

/**
 * Custom tag providing ability to cache elements of the page usually from 
 * custom tags and then display them at later point when requested. The main 
 * purpose is to allow other tags to specify elements, such as css includes or 
 * javascript includes at one spot, e.g. using initialization tag. These elements 
 * will then be place on a specific places at the page using this tag. It also 
 * allows derived tags to cache their content and then other tags to fetch them 
 * and display at other places.  
 *  
 * @author bastafidli
 */
public class PageElementCacheTag extends BodyTagSupport
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Messages collected during processing of current request.
    */
   public static final String PAGE_ELEMENTS_CACHE = "pageelements";
   
   /**
    * CSS style sheets cache.
    */
   public static final String CSS_ELEMENT = "css";

   /**
    * JavaScript js import cache.
    */
   public static final String JS_ELEMENT = "js";

   /**
    * JavaScript script cache.
    */
   public static final String SCRIPT_ELEMENT = "script";

   /**
    * Indicator to use in attributes to signal to retrieve content from the cache.
    */
   public static final String CACHE_INDICATOR = "cache:";

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -2506816420084812132L;

   /**
    * Id of the cached element to retrieve.
    */
   protected String m_strId; 
   
   /**
    * If this id is specified then the content will not be printed into output
    * but instead will be cached under id specified in this attribute.
    */
   protected String m_strCacheas;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for custom tag.
    */
   public PageElementCacheTag() 
   {
      super();
      
      m_strId = ""; 
      m_strCacheas = "";
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int doStartTag(
   ) throws JspException 
   {
      String strOutput;
      
      strOutput = getCachedContent(getId());
      
      if ((m_strCacheas != null) && (m_strCacheas.length() > 0))
      {
         if (strOutput != null)
         {
            cache(m_strCacheas, strOutput);
         }
      }
      else
      {
         TagUtils.write(pageContext, strOutput);
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
    * @return String - Id of the cached element to retrieve.
    */
   @Override
   public String getId(
   )
   {
      return m_strId;
   }

   /**
    * @return String - New Id under which the output will be cached   
    */
   public String getCacheas(
   )
   {
      return m_strCacheas;
   }

   /**
    * @param strId - Id of the cached element to retrieve. 
    */
   @Override
   public void setId(
      String strId
   )
   {
      m_strId = strId;
   }
   
   /**
    * @param strCacheas - New Id under which the output will be cached
    */
   public void setCacheas(
      String strCacheas
   )
   {
      m_strCacheas = strCacheas;
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Add item for specific page element to the cache. 
    * 
    * @param strPageElementId - constant representing HTML page element for which 
    *                           the item will be cached
    * @param strItemToCache - element to cache
    */
   protected void cache(
      String strPageElementId,
      String strItemToCache
   )
   {
      // We have to use thread based cache since if page is constracted using
      // Tiles then each include has a separate page context
      Map cache = (Map)CallContext.getInstance().getCache(PAGE_ELEMENTS_CACHE);
      
      List items;
      
      items = (List)cache.get(strPageElementId.toLowerCase());
      if (items == null)
      {
         items = new ArrayList();
         cache.put(strPageElementId.toLowerCase(), items);
      }
      items.add(strItemToCache);
   }

   /**
    * Get the cached content.
    * 
    * @param strPageElementId - id of the cached element to get
    * @return String - cached content or empty string if nothing is cached,
    *                  but never null
    */
   protected String getCachedContent(
      String strPageElementId
   )
   {
      return getCachedContent(strPageElementId, true);
   }


   /**
    * Get the cached content.
    * 
    * @param strPageElementId - id of the cached element to get
    * @param bRemove - remove the element from the cache after it is retrieved
    * @param strSeparator - separator for particular cached items
    * @return String - cached content or empty string if nothing is cached,
    *                  but never null
    */
   protected String getCachedContent(
      String  strPageElementId,
      boolean bRemove,
      String  strSeparator
   ) 
   {
      // We have to use thread based cache since if page is constracted using
      // Tiles then each include has a separate page context
      Map           cache = (Map)CallContext.getInstance().getCache(PAGE_ELEMENTS_CACHE);
      StringBuilder sbHtml = new StringBuilder();
      
      if (cache != null)
      {
         List items;
         
         if (bRemove)
         {
            items = (List)cache.remove(strPageElementId.toLowerCase());            
         }
         else
         {
            items = (List)cache.get(strPageElementId.toLowerCase());
         }
         if ((items != null) && (!items.isEmpty()))
         {
            Iterator     iter;
            
            for (iter = items.iterator(); iter.hasNext();)
            {
               sbHtml.append(iter.next());
               sbHtml.append(strSeparator);
            }            
         }
      }
      
      return sbHtml.toString();
   }

   /**
    * Get the cached content.
    * 
    * @param strPageElementId - id of the cached element to get
    * @param bRemove - remove the element from the cache after it is retrieved
    * @return String - cached content or empty string if nothing is cached,
    *                  but never null
    */
   protected String getCachedContent(
      String  strPageElementId,
      boolean bRemove
   ) 
   {
      return getCachedContent(strPageElementId, bRemove, "\n");
   }

   /**
    * Get the elements of cached content elements.
    * 
    * @param strPageElementId - id of the cached element to get
    * @param bRemove - remove the element from the cache after it is retrieved
    * @return Lists - cached content or empty List if nothing is cached,
    *                       but never null
    */
   protected List getCachedContentElements(
      String  strPageElementId,
      boolean bRemove
   ) 
   {
      // We have to use thread based cache since if page is constracted using
      // Tiles then each include has a separate page context
      Map  cache = (Map)CallContext.getInstance().getCache(PAGE_ELEMENTS_CACHE);
      List items = null;
      
      if (cache != null)
      {
         if (bRemove)
         {
            items = (List)cache.remove(strPageElementId.toLowerCase());            
         }
         else
         {
            items = (List)cache.get(strPageElementId.toLowerCase());
         }
      }
      if (items == null)
      {
         items = Collections.EMPTY_LIST;            
      }
      
      return items;
   }
}
