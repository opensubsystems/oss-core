/*
 * Copyright (C) 2003 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.opensubsystems.core.error.OSSDynamicClassException;
import org.opensubsystems.core.error.OSSException;

/**
 * Purpose of this class is to make creation of configurable factories easier.
 * It allows caller to specify class or class name of the class for which some 
 * new instance is desired. 
 * 
 * The derived classes can perform name transformation of the class that should 
 * be created from the name that is specified based on some rules encapsulated 
 * in the overwritten methods createDefaultClassNames(). 
 *
 * @author bastafidli
 */
public class ClassFactory<T> extends OSSObject
{   
   // Constants ////////////////////////////////////////////////////////////////
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * See 
    * http://www.angelikalanger.com/GenericsFAQ/FAQSections/ProgrammingIdioms.html#FAQ604
    * why this is needed
    */
   private Class<? extends T> m_type;
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(ClassFactory.class);
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Creates a new instance of ClassFactory
    * 
    * @param type - type for objects instantiated by this factory 
    */
   public ClassFactory(
      Class<? extends T> type
   )
   {
      m_type = type;
   }

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Create instance of the class whose type was specified in the constructor
    * by calling parameterless constructor. The created instance doesn't have to 
    * be of the same type as the passed in class identifier. The identifier can 
    * be used to create name of different class by some transformation, e.g. in 
    * case an interface name is passed in and default implementation class is 
    * desired.
    * 
    * @return T - new instance of the object, never null since exception 
    *                  will be thrown if not found
    * @throws OSSException - an error has occurred
    */
   public T createInstance(
   ) throws OSSException
   {
      return createInstance(m_type.getName());
   }

   /**
    * Create instance of the class by calling parameterless constructor. 
    * The created instance doesn't have to be of the same type as the passed in
    * class identifier. The identifier can be used to create name of different 
    * class by some transformation, e.g. in case an interface name is passed in
    * and default implementation class is desired.
    * 
    * @param clsClassIdentifier - identifier of the class for which new instance   
    *                             should be created
    * @return T - new instance of the object, never null since exception 
    *                  will be thrown if not found
    * @throws OSSException - an error has occurred
    */
   public T createInstance(
      Class<? extends T> clsClassIdentifier
   ) throws OSSException
   {
      return createInstance(clsClassIdentifier.getName());
   }

   /**
    * Create instance of the class by calling parameterless constructor. 
    * The created instance doesn't have to be of the same type as the passed in
    * class identifier. The identifier can be used to create name of different 
    * class by some transformation, e.g. in case an interface name is passed in
    * and default implementation class is desired.
    * 
    * @param clsClassIdentifier - identifier of the class for which new instance   
    *                             should be created
    * @param clsDefault - default class to instantiate if no other class based
    *                     on clsClassIdentifier cannot be instantiated
    * @return T - new instance of the object, never null since exception 
    *                  will be thrown if not found
    * @throws OSSException - an error has occurred
    */
   public T createInstance(
      Class<? extends T> clsClassIdentifier,
      Class<? extends T> clsDefault
   ) throws OSSException
   {
      return createInstance(clsClassIdentifier.getName(), clsDefault);
   }
   
   /**
    * Create instance of the class by calling parameterless constructor. 
    * The created instance doesn't have to be of the same type as the passed in
    * class identifier. The identifier can be used to create name of different 
    * class by some transformation, e.g. in case an interface name is passed in
    * and default implementation class is desired.
    * 
    * @param strClassIdentifier - identifier of the class for which new instance  
    *                              should be created
    * @param clsDefault - default class to instantiate if no other class based
    *                     on clsClassIdentifier cannot be instantiated
    * @return T - new instance of the object, never null since exception 
    *             will be thrown if not found
    * @throws OSSException - an error has occurred
    */
   public T createInstance(
      String strClassIdentifier,
      Class<? extends T>  clsDefault
   ) throws OSSException
   {
      T      instance;
      Object objInstance;
      
      try
      {
         instance = createInstance(strClassIdentifier);
      }
      catch (OSSDynamicClassException dceExc)
      {
         objInstance = ClassUtils.createNewInstance(clsDefault);
         instance = verifyInstance(objInstance);
      }
      
      return instance;      
   }
   
   /**
    * Create instance of the class by calling parameterless constructor. 
    * The created instance doesn't have to be of the same type as the passed in
    * class identifier. The identifier can be used to create name of different 
    * class by some transformation, e.g. in case an interface name is passed in
    * and default implementation class is desired.
    * 
    * @param strClassIdentifier - identifier of the class for which new instance  
    *                              should be created
    * @return T - new instance of the object, never null since exception 
    *             will be thrown if not found
    * @throws OSSException - an error has occurred
    */
   public T createInstance(
      String strClassIdentifier
   ) throws OSSException
   {
      String       strModifier;
      T            objInstance = null;
      List<String> lstClassNames = new ArrayList<>();
      
      strModifier = getModifier();
      createConfiguredClassNames(strClassIdentifier, strModifier, lstClassNames);
      if (!lstClassNames.isEmpty())
      {
         // There were some class configured for this property so try to 
         // instantiate it and if it cannot be instantiated, throw an exception
         // since default class might be instantiated instead
         objInstance = instantiateClass(lstClassNames, strClassIdentifier);
         if (objInstance != null)
         {
            s_logger.log(Level.FINEST, "Class instantiation for {0} based on "
                         + "configured setting instantiated {1}", 
                         new Object[]{strClassIdentifier, 
                                      objInstance.getClass().getName()});
         }
      }
      else
      {
         // Since no other class was configured for specified identifier, try 
         // to get default class for given identifier and if there is any
         // try to instantiate it
         createDefaultClassNames(strClassIdentifier, strModifier, lstClassNames);
         if (!lstClassNames.isEmpty())
         {
            objInstance = instantiateClass(lstClassNames, strClassIdentifier);
            if (objInstance != null)
            {
               s_logger.log(Level.FINEST, "Class instantiation for {0} based on "
                            + "default setting instantiated {1}", 
                            new Object[]{strClassIdentifier, 
                                         objInstance.getClass().getName()});
            }
         }
         else
         {
            throw new OSSDynamicClassException("No class names available for " 
                                               + strClassIdentifier);         
         }
      }
      
      if (objInstance == null)
      {
         throw new OSSDynamicClassException("Failed to create instance for " 
                                            + strClassIdentifier);
      }
      
      return objInstance;
   }
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Instantiate new instance using ordered list of class names to consider.
    * 
    * @param lstClassNames - ordered list of classes to consider as a template
    *                        for new instance
    * @param strClassIdentifier - identifier of the class for which new instance  
    *                              should be created, used for reporting purposes
    * @return T - new instance or null if it cannot be instantiated
    * @throws OSSException - an error has occurred
    */
   protected T instantiateClass(
      List<String> lstClassNames,
      String       strClassIdentifier
   ) throws OSSException
   {
      T                instance = null;
      Object           objInstance = null;
      String           strClassName;
      Iterator<String> classNames; 
      
      for (classNames = lstClassNames.iterator(); 
          (classNames.hasNext()) && (objInstance == null);)
      {
         strClassName = classNames.next();
         try
         {
            objInstance = ClassUtils.createNewInstance(strClassName);
            instance = verifyInstance(objInstance); 
            // Print success message so that we can see what class are we using
            Log.s_logger.log(Level.FINER, "Speculative class instantiation for"
                             + " {0} succeeded for class {1}", 
                             new Object[]{strClassIdentifier, strClassName});
         }
         catch (OSSDynamicClassException dceExc)
         {
            // Do not print exception here because it is looking like an error 
            // in the log while what we tried to do was speculatively find a 
            // class
            Throwable cause = dceExc.getCause();
            
            // Instead of stack trace print just the original message of the 
            // cause since the class is just not present
            s_logger.log(Level.FINEST, "Speculative class instantiation for {0}"
                         + " failed for class {1} with error {2}", 
                         new Object[]{strClassIdentifier, strClassName, cause});
         }
      }
      
      return instance;
   }
   
   /**
    * This method allows derived class to implement verifications if desired
    * that the instantiated class must satisfy. 
    * 
    * @param objInstance - instance to verify, can be null
    * @param T - return the instance casted to the correct value 
    * @throws OSSException - an error has occurred (e.g. verification error)
    */
   protected T verifyInstance(
      Object objInstance
   ) throws OSSException
   {
      // Make sure that the instantiated class is of type DataFactory 
      // and DatabaseFactory
      if ((objInstance != null) && (!(m_type.isInstance(objInstance))))
      {
         throw new OSSDynamicClassException("Instantiated class is not of type"
                                            + m_type.getName()
                                            + " and is instead of type "
                                            + objInstance.getClass().getName());
                         
      }
      return m_type.cast(objInstance);
   }
   
   /**
    * Create new class name based on the specified identifier and modifier.
    * 
    * @param strClassIdentifier - identifier of the class for which new instance  
    *                              should be created
    * @param strModifier - modifier used together with identifier to query 
    *                      configured class name or null if none is available
    * @param lstClassNames - container to add the possible class names to. 
    *                        The classes will be tried in order as they are added
    *                        to this container. 
    * @throws OSSException - an error has occurred
    */
   protected void createDefaultClassNames(
      String       strClassIdentifier,
      String       strModifier,
      List<String> lstClassNames
   ) throws OSSException
   {
      // The simplest implementation, just use the identifier as class name
      lstClassNames.add(strClassIdentifier);
   }

   /**
    * Create name for the class identified by given identifier based on 
    * configuration information. This name is first queried using modifier
    * appended to the identifier and then without modifier for default configuration.  
    * 
    * @param strClassIdentifier - identifier of the class for which new instance  
    *                              should be created
    * @param strModifier - modifier used together with identifier to query 
    *                      configured class name or null if none is available
    * @param lstClassNames - container to add the possible class names to. 
    *                        The classes will be tried in order as they are added
    *                        to this container. 
    * @throws OSSException - an error has occurred
    */                  
   protected void createConfiguredClassNames(
      String       strClassIdentifier,
      String       strModifier,
      List<String> lstClassNames
   ) throws OSSException
   {
      Properties   prpSettings;
      String       strClassName;
      StringBuilder sbProperty = new StringBuilder(strClassIdentifier);
      
      prpSettings = Config.getInstance().getProperties();
      
      // First specific property in form classidentifier.modifier
      if (strModifier != null)
      {
         sbProperty.append(".");
         sbProperty.append(strModifier.toLowerCase());
         strClassName = PropertyUtils.getStringProperty(
                                         prpSettings, sbProperty.toString(), 
                                         null, "Classname", false);
         
         if (strClassName != null)
         {
            lstClassNames.add(strClassName);
         }
      }
      strClassName = PropertyUtils.getStringProperty(
                                      prpSettings, strClassIdentifier, 
                                      null, "Classname", false);
      if (strClassName != null)
      {
         lstClassNames.add(strClassName);
      }
   }
   
   /**
    * Get modifier which should be used together with the class identifier to
    * construct class names.
    * 
    * @return String - modifier or null if no modifier should be used
    * @throws OSSException - an error has occurred
    */
   protected String getModifier(
   ) throws OSSException
   {
      return null;
   }   
}
