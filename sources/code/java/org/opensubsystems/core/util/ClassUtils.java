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

package org.opensubsystems.core.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSDynamicClassException;
import org.opensubsystems.core.error.OSSException;

/**
 * Set of utilities to work with classes.  
 *
 * @author bastafidli
 */
public final class ClassUtils extends OSSObject
{
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(ClassUtils.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private ClassUtils(
   )
   {
      // Do nothing
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /** 
    * Create new instance of the class
    * 
    * @param strClassName - identifier of the class for which new instance   
    *                       should be created
    * @return Object - new instance of the object
    * @throws OSSException - an error has occurred
    */
   public static Object createNewInstance(
      String strClassName
   ) throws OSSException
   {
      Object objInstance;

      try
      {
         objInstance = createNewInstance(Class.forName(strClassName));
      }
      catch (ClassNotFoundException eNoClass)
      {
         throw new OSSDynamicClassException("Unexpected exception.", eNoClass);         
      }
      
      return objInstance;      
   }

   /** 
    * Create new instance of the class
    * 
    * @param templateClass - identifier of the class for which new instance   
    *                        should be created
    * @return Object - new instance of the object
    * @throws OSSException - an error has occurred
    */
   public static Object createNewInstance(
      Class templateClass
   ) throws OSSException
   {
      Object objInstance;

      try
      {
         objInstance = templateClass.newInstance();
         s_logger.finest("Instantiated class " + templateClass.getName());        
      }
      catch (IllegalAccessException eIllAcc)
      {
         throw new OSSDynamicClassException("Unexpected exception.", eIllAcc);         
      }
      catch (InstantiationException ineExc)
      {
         throw new OSSDynamicClassException("Unexpected exception.", ineExc);
      }
      
      return objInstance;      
   }
   
   /**
    * Get the instances of the same class as the specified template from the 
    * source map and put them into the buffer under the same key. This method
    * uses "instance of" to identify the instances.
    * 
    * @param mpSource - source map, from which the instances, specified as values 
    *                   in the map, matching the template will be retrieved
    * @param clsTemplate - template that will be used to search instances
    * @param mpBuffer - buffer, to which will be the instances matching the 
    *                   template placed under the same key
    */
   public static void getInstances(
      Map   mpSource,
      Class clsTemplate,
      Map   mpBuffer
   )
   {
      Map.Entry entry;
      Iterator  objects;
      
      for (objects = mpSource.entrySet().iterator(); objects.hasNext();)
      {
         entry = (Map.Entry)objects.next();
         if (clsTemplate.isInstance(entry.getValue()))
         {
            mpBuffer.put(entry.getKey(), entry.getValue());
         }
      }
   }
   
   /**
    * Get the instances of the same class as the specified template from the 
    * source collection and put them into the buffer. This method uses 
    * "instance of" to identify the instances.
    * 
    * @param colSource - source collection, from which the instances matching  
    *                    the template will be retrieved
    * @param clsTemplate - template that will be used to search instances
    * @param colBuffer - buffer, to which will be the instances matching the 
    *                    template placed
    */
   public static void getInstances(
      Collection colSource,
      Class      clsTemplate,
      Collection colBuffer
   )
   {
      Object   entry;
      Iterator objects;
      
      for (objects = colSource.iterator(); objects.hasNext();)
      {
         entry = objects.next();
         if (clsTemplate.isInstance(entry))
         {
            colBuffer.add(entry);
         }
      }
   }
}
