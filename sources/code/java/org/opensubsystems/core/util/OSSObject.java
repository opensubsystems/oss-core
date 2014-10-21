/*
 * Copyright (C) 2012 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
package org.opensubsystems.core.util;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Base class for all classes in this project. It is a place to define 
 * alternative or enhanced behavior of standard Java methods 
 * 
 * @author bastafidli
 */
public class OSSObject 
{
   // Configuration settings ///////////////////////////////////////////////////
   
   /**
    * Specifies if the toString output should be collapsed or not.
    */
   public static final String TOSTRING_COLLAPSE = "oss.tostring.collapse";

   /**
    * Specifies if the toString output should omit empty values.
    */
   public static final String TOSTRING_OMIT_EMPTY_VALUES = "oss.tostring.omit.empty.values";
   
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Limit how many levels of indentation are supported.
    */
   public static final int INDENTATION_LIMIT = 100;
   
   /**
    * Pregenerated indentation strings used to indent output of toString. Each
    * one starts with new line.
    */
   public static final String[] INDENTATION = new String[INDENTATION_LIMIT];

   /**
    * Default toString collapse setting. Keep it false so that it doesn't hide 
    * any values.
    */
   public static final Boolean TOSTRING_COLLAPSE_DEFAULT = Boolean.FALSE;
   
   /**
    * Default toString omit empty values setting. Keep it false so that it 
    * doesn't hide any values.
    */
   public static final Boolean TOSTRING_OMIT_EMPTY_VALUES_DEFAULT = Boolean.FALSE;
   
   // Cached values ////////////////////////////////////////////////////////////
   
   /**
    * Flag specifying if the toString output should be collapsed.
    */
   protected static Boolean s_bCollapseToString = null;
   
   /**
    * Flag specifying if the toString output should omit empty values.
    */
   protected static Boolean s_bOmitEmptyValuesInToString = null;
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Class constructor
    */
   static 
   {
      StringBuilder sb = new StringBuilder("\n");
      
      for (int iIndex = 0; iIndex < INDENTATION_LIMIT; iIndex++)
      {
         INDENTATION[iIndex] = sb.toString();
         sb.append("   ");
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public final String toString()
   {
      StringBuilder sb = new StringBuilder();
      
      toString(sb, 0);
      return sb.toString();
   }

   /**
    * Produce string representation of the object properly indented to display
    * the object hierarchy
    * 
    * @param sb - buffer used to create the string representation of object
    * @param iIndentLevel - indentation level with 0 being just new line
    */
   public void toString(
      StringBuilder sb,
      int           iIndentLevel
   )
   {
      // Default implementation does nothing
   }
   
   // Helper methods //////////////////////////////////////////////////////////
   
   /**
    * Indent the future content of the buffer by a specified level.
    *  
    * @param sb - buffer to which the indentation will be appended
    * @param iIndentLevel - level to which to indent future content
    */
   protected final void indent(
      StringBuilder sb,
      int           iIndentLevel
   )
   {
      append(sb, INDENTATION[iIndentLevel]);
   }
   
   /**
    * Safely append value to the buffer even if it is null.
    * 
    * @param sb - buffer to use to append values
    * @param value - value to append, can be null
    */
   protected final void append(
      StringBuilder sb,
      Object        value
   )
   {
      append(sb, 0, value);
   }

   /**
    * Safely append value to the buffer even if it is null.
    * 
    * @param sb - buffer to use to append values
    * @param iIndentLevel - level at which to append the value
    * @param value - value to append, can be null
    */
   protected final void append(
      StringBuilder sb,
      int           iIndentLevel,
      Object        value
   )
   {
      append(sb, iIndentLevel, value, false);
   }
   
   /**
    * Safely append value to the buffer even if it is null.
    * 
    * @param sb - buffer to use to append values
    * @param iIndentLevel - level at which to append the value
    * @param value - value to append, can be null
    * @param bCollapsable - if true then this call can be collapsed (omitted)
    *                       if the configuration setting is set appropriately
    */
   protected final void append(
      StringBuilder sb,
      int           iIndentLevel,
      Object        value,
      boolean       bCollapsable
   )
   {
      if ((!bCollapsable) || (!isToStringCollapsed()))
      {
         if (iIndentLevel > 0)
         {
            sb.append(INDENTATION[iIndentLevel]);
         }
         if (value == null)
         {
            sb.append(StringUtils.NULL_STRING);
         }
         else
         {
            if (value instanceof Map)
            {
               StringUtils.toStringMap(sb, iIndentLevel, (Map)value);
            }
            else if (value instanceof Collection)
            {
               StringUtils.toStringCollection(sb, iIndentLevel, (Collection)value);
            }
            else if (value instanceof OSSObject)
            {
               ((OSSObject)value).toString(sb, iIndentLevel);
            }
            else
            {
               sb.append(value);
            }
         }
      }
   }

   /**
    * Safely append value to the buffer even if it is null.
    * 
    * @param sb - buffer to use to append values
    * @param iIndentLevel - level at which to append the value
    * @param label - label to use for the value
    * @param value - value to append, can be null
    */
   protected final void append(
      StringBuilder sb,
      int           iIndentLevel,
      String        label,
      Object        value
   )
   {
      if ((isNonEmptyValue(value)) || (!isToStringToOmitEmptyValues()))
      {
         sb.append(INDENTATION[iIndentLevel]);
         sb.append(label);
         if (value instanceof Map)
         {
            StringUtils.toStringMap(sb, iIndentLevel, (Map)value);
         }
         else if (value instanceof Collection)
         {
            StringUtils.toStringCollection(sb, iIndentLevel, (Collection)value);
         }
         else if (value instanceof OSSObject)
         {
            ((OSSObject)value).toString(sb, iIndentLevel + 1);
         }
         else
         {
            append(sb, value);
         }
      }
   }
   
   /**
    * Get flag, which is telling us if we should collapse output of the toString.
    * 
    * 
    * @return boolean - if true then the output of the toString will be collapsed
    *                   (some boilerplate values will be omitted) otherwise 
    *                   everything will be included.
    */
   public boolean isToStringCollapsed()
   {
      if (s_bCollapseToString == null)
      {
         // Read it here instead of in static block or constructor since if this 
         // code is executed in different execution context, it might have 
         // different configuration settings.
         // No synchronization is needed since if the value is overwritten it
         // doesn't change the logic
         Properties prpSettings;

         prpSettings = Config.getInstance().getProperties();
         s_bCollapseToString = PropertyUtils.getBooleanProperty(
                                  prpSettings, TOSTRING_COLLAPSE,
                                  TOSTRING_COLLAPSE_DEFAULT,
                                  "Collapse toString messages"
                               );

      }
      
      return s_bCollapseToString;
   }
   
   /**
    * Get flag, which is telling us if we should omit empty values in the toString.
    * 
    * 
    * @return boolean - if true then the output of the toString will omit empty 
    *                   values otherwise everything will be included.
    */
   public boolean isToStringToOmitEmptyValues()
   {
      if (s_bOmitEmptyValuesInToString == null)
      {
         // Read it here instead of in static block or constructor since if this 
         // code is executed in different execution context, it might have 
         // different configuration settings.
         // No synchronization is needed since if the value is overwritten it
         // doesn't change the logic
         Properties prpSettings;

         prpSettings = Config.getInstance().getProperties();
         s_bOmitEmptyValuesInToString = PropertyUtils.getBooleanProperty(
                                  prpSettings, TOSTRING_OMIT_EMPTY_VALUES,
                                  TOSTRING_OMIT_EMPTY_VALUES_DEFAULT,
                                  "Omit empty values in toString messages"
                               );

      }
      
      return s_bOmitEmptyValuesInToString;
   }
   
   /**
    * Test if the value would result in empty output.
    * 
    * @param value - value to test
    * @return boolean - if true then the value would not result in empty output,
    *                   false otherwise
    */
   protected final boolean isNonEmptyValue(
      Object value
   )
   {
      boolean bReturn = true;
      
      if (value == null)
      {
         bReturn = false;
      }
      else if (value instanceof String)
      {
         bReturn = !((String)value).isEmpty();
      }
    
      return bReturn;
   }
}
