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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for reading and saving of configuration files. 
 * 
 * @author bastafidli
 */
public class Config extends OSSObject
{
   // Configuration settings ///////////////////////////////////////////////////

   /** 
    * Name of the property which can specify the configuration file to use.
    */   
   public static final String CONFIG_FILE_NAME = "oss.config.file";

   /** 
    * Name of the property which can specify the configuration file that a given 
    * configuration file depends on. If this property is found in the file being 
    * loaded, the dependent file is loaded first. Then the properties loaded 
    * from the dependent file are replaced with the properties which were 
    * redefined in the current file.
    */   
   public static final String DEPENDENT_CONFIG_FILE_NAME 
                                 = "oss.config.dependent.file";
   
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Default file name of configuration file.
    */
   public static final String DEFAULT_CONFIG_FILE_NAME = "oss.properties";

   /**
    * Lock used in synchronized sections.
    */
   private static final String IMPL_LOCK = "IMPL_LOCK";

   // Attributes ///////////////////////////////////////////////////////////////

   /**
    * Properties representing settings for this application. This property is 
    * not transient since it represents values which needs to be stored on a 
    * disk. The properties are cached the first time any of them is accessed.
    */
   protected Properties m_prpProperties;
   
   /**
    * Name of the property file containing application properties. It can be 
    * null if properties were specified explicitly in constructor.
    */
   protected String m_strRequestedConfigFile;

   /**
    * If the property file name is not specified by m_strRequestedConfigFile, it 
    * can be specified by this URL as found on the system class path.
    */
   protected URL m_urlDefaultConfigFile;
   
   /**
    * Name of the configuration file actually used. It is one of 
    * m_strPropertyFileName or m_urlDefaultPropertyFile. 
    */
   protected String m_strActualConfigFile;
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(Config.class);
  
   /**
    * Reference to the Config actually in use.
    */
   private static Config s_defaultInstance;
   
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Constructor to allow access to default property  file.
    */
   public Config(
   )
   {
      this(null, null);
   }
   
   /**
    * Public constructor to access specified property file.
    * 
    * @param strRequestedConfigFile - name of the property file to use.
    */
   public Config(
      String strRequestedConfigFile
   )
   {
      this(null, strRequestedConfigFile);
   }

   /**
    * Public constructor to use specified properties
    * 
    * @param predefinedProperties - predefined properties which should be used
    */
   public Config(
      Properties predefinedProperties
   )
   {
      this(predefinedProperties, null);
   }

   /**
    * Full internal constructor to facilitate initialization.
    * 
    * @param predefinedProperties - predefined properties which should be used
    * @param strRequestedConfigFile - name of the property file to use.
    */
   protected Config(
      Properties predefinedProperties,
      String     strRequestedConfigFile
   )
   {
      m_prpProperties = predefinedProperties;
      m_strRequestedConfigFile = strRequestedConfigFile;
      m_urlDefaultConfigFile = null;

      if (m_prpProperties == null)
      {
         // Initialize the properties if they were not specified
         try
         {
            loadPropertiesFromFile();
         }
         catch (FileNotFoundException fnfeExc)
         {
            s_logger.log(Level.WARNING, 
                         "Cannot find default configuration file " 
                         + m_strRequestedConfigFile,
                         fnfeExc);   
            initializePropertiesAfterFileAccessFailure();
         }
         catch (IOException ioeExc)
         {
            s_logger.log(
               Level.CONFIG, 
               "Error has occurred while accessing default configuration file.",
               ioeExc);   
            initializePropertiesAfterFileAccessFailure();
         }
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert m_prpProperties != null
                   : "Properties must be ";
         }
      }
   }
   
   // Static methods ///////////////////////////////////////////////////////////

   /**
    * Get the default config instance.
    *
    * @return Config - actual instance
    */
   public static Config getInstance(
   )
   {
      if (s_defaultInstance == null)
      {
         // Only if the default Config wasn't set by other means create a new one
         // Synchronize just for the creation
         synchronized (IMPL_LOCK)
         {
            if (s_defaultInstance == null)
            {
               // Specify no name so that default name will be searched.
               setInstance(new Config());
            }
         }   
      }
      
      return s_defaultInstance;
   }
   
   /**
    * Set default instance. This instance will be returned by getInstance 
    * method until it is changed.
    *
    * @param defaultInstance - new default Config instance
    * @see #getInstance
    */
   public static void setInstance(
      Config defaultInstance
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {         
         assert defaultInstance != null 
                : "Default config instance cannot be null";
      }   
      
      synchronized (IMPL_LOCK)
      {
         s_defaultInstance = defaultInstance;
      }   
   }

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Get the name of the property file that was specified in the constructor.
    * 
    * @return String - property file name or null if none specified
    */
   public String getRequestedConfigFile(
   )
   {
      return m_strRequestedConfigFile;
   }
   
   /**
    * Get full location and name of the actual property file that is being used. 
    * The file may be located in the file system or in jar file. This might be 
    * different than the requested config file.
    * 
    * @return String - full location and name of the property file that is 
    *                  actually in use.
    */
   public String getActualConfigFile(
   )
   {
      return m_strActualConfigFile;
   }

   /**
    * Set the name of the property file. This will force the already loaded 
    * properties to be reloaded.
    * 
    * @param strPropertyFileName - the strPropertyFileName to set
    */
   public void setPropertyFileName(
      String strPropertyFileName
   )
   {
      synchronized (IMPL_LOCK)
      {
         // Initialize the properties again by specifying new file name and
         // resetting the old properties and then reloading them
         m_strRequestedConfigFile = strPropertyFileName;
         m_prpProperties = null;
         
         Properties prpSettings;
         
         prpSettings = getProperties();
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert prpSettings != null
                   : "getPropertiesSafely must return not null properties.";
            assert s_defaultInstance.m_prpProperties != null
                   : "getPropertiesSafely must initialize m_prpProperties";
         }
      }
   }
   
   /**
    * Get all properties from default configuration file or file specified in 
    * the constructor or return the ones passed into the constructor. If no 
    * properties can be loaded, a new ones will be created and empty property 
    * object will be returned.
    *
    * @return Properties - set of properties for this application.
    */
   public Properties getProperties(
   )
   {
      if (m_prpProperties == null)
      {
         Properties prpSettings;
         
         synchronized (IMPL_LOCK)
         {
            try
            {
               loadPropertiesFromFile();
               prpSettings = m_prpProperties;
            }
            catch (FileNotFoundException fnfeExc)
            {
               s_logger.log(Level.WARNING, 
                            "Cannot find default configuration file " 
                            + m_strRequestedConfigFile,
                            fnfeExc);   
               prpSettings = initializePropertiesAfterFileAccessFailure();
            }
            catch (IOException ioeExc)
            {
               s_logger.log(
                  Level.CONFIG, 
                  "Error has occurred while accessing default configuration file.",
                  ioeExc);   
               prpSettings = initializePropertiesAfterFileAccessFailure();
            }
         }
         
         return prpSettings;
      }
      else
      {
         return m_prpProperties;
      }
   }

   /**
    * Save the configuration to a file.
    * 
    * @throws IOException - there was a problem saving configuration file.
    * @throws FileNotFoundException - file cannot be found
    */
   public void save(
   ) throws IOException,
            FileNotFoundException
   {
      // Allow only one save a time, it is save to synchronize on properties
      // since at this time they have to be initialized, if not, null pointer
      // exception will be thrown
      synchronized (IMPL_LOCK)
      {
         // Open the file
         OutputStream osConfigFile = null;

         try
         {
            if (m_strRequestedConfigFile != null)
            {
               // Open the file
               osConfigFile = new FileOutputStream(m_strRequestedConfigFile);
            }
            else if (m_urlDefaultConfigFile != null)
            {
               osConfigFile = new FileOutputStream(
                                     m_urlDefaultConfigFile.getFile());
            }
            else
            {
               throw new FileNotFoundException("No configuration file defined.");
            }
   
            BufferedOutputStream bosConfigFile = null;
                    
            // Store the properties
            try
            {
               bosConfigFile = new BufferedOutputStream(osConfigFile);
               
               // TODO: Improve: Once this is invoked, all the comments from 
               // the original file are lost and the properties are in random
               // order. Figure out how to save it so we don't mess up the 
               // comments and order/grouping of properties
               m_prpProperties.store(bosConfigFile, 
                                     "DO NOT MODIFY THIS FILE DIRECTLY.");
            }
            finally
            {
               // Close the file
               try
               {
                  if (bosConfigFile != null)
                  {
                     bosConfigFile.close();
                  }
               }
               catch (IOException ioeExc)
               {
                  // Ignore this
                  s_logger.log(Level.WARNING, 
                               "Failed to close buffer for configuration file " 
                               + m_strRequestedConfigFile, ioeExc);
               }
            }
         }
         finally
         {
            try
            {
               if (osConfigFile != null)
               {
                  osConfigFile.close();
               }
            }
            catch (IOException ioeExc)
            {
               // Ignore this
               s_logger.log(Level.WARNING, 
                            "Failed to close configuration file " 
                            + m_strRequestedConfigFile, ioeExc);
            }
         }         
      }
   }

   /**
    * Define specified properties if they are not defined yet. Each property 
    * from the specified set of properties will be tested if it is already 
    * defined and if it is not, then it will be defined to the value specified
    * in the passed in parameter.
    * 
    * @param propertiesToDefine - properties to define if they are not defined 
    *                             yet. Each property is tested independently 
    *                             from all the other properties. May be null
    *                             or empty.
    */
   public void defineUndefinedProperties(
      Properties propertiesToDefine
   )
   {
      if ((propertiesToDefine != null) && (!propertiesToDefine.isEmpty()))
      {
         Map.Entry currentProperty;

         // Get the currently defined properties
         m_prpProperties = getProperties();
         for (Iterator entries = propertiesToDefine.entrySet().iterator();
              entries.hasNext();)
         {
            currentProperty = (Map.Entry)entries.next();
            // Test which properties are already there and define those which
            // are not yet defined
            if (!m_prpProperties.contains(currentProperty.getKey()))
            {
               m_prpProperties.put(currentProperty.getKey(),
                                   currentProperty.getValue());
            }
         }
      }
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Find and configuration file. The calling class is responsible for
    * closing the input stream
    * 
    * @param  strConfigFileName - name of the configuration file
    * @return InputStream - opened config file
    * @throws IOException - an error has occurred opening configuration file
    * @throws FileNotFoundException - file cannot be found
    */
   protected InputStream findConfigFile(
      String strConfigFileName
   ) throws IOException,
            FileNotFoundException
   {
      InputStream isConfigFile = null;

      m_urlDefaultConfigFile = ClassLoader.getSystemResource(strConfigFileName);
      if (m_urlDefaultConfigFile == null)
      {
         // if there was not found configuration file by using 
         // ClassLoader.getSystemResource(), try to use 
         // getClass().getClassLoader().getResource()
         // This is here due to the fact that in J2EE server
         // ClassLoader.getSystemResource() search the configuration file
         // OUTSIDE of the packaged application (ear, war, jar) so that this
         // file can override the configuration file which is packaged INSIDE
         // the application which will be found belog with class loader 
         // for this class
         m_urlDefaultConfigFile = this.getClass().getClassLoader().getResource(
                                                       strConfigFileName);
      }      
      if (m_urlDefaultConfigFile == null)
      {
         throw new FileNotFoundException("Cannot find configuration file "
                                         + strConfigFileName);
      }
      else
      {
         isConfigFile = m_urlDefaultConfigFile.openStream();
         m_strActualConfigFile = m_urlDefaultConfigFile.toString();
      }
      
      return isConfigFile;
   }
   
   /**
    * Get all properties from default configuration file or file specified in 
    * the constructor or return the ones passed into the constructor.
    *
    * This method is private so that it cannot be overridden since it is called
    * from constructor.
    *  
    * @throws IOException - for example the file was not found
    */
   private void loadPropertiesFromFile(
   ) throws IOException
   {
      InputStream isConfigFile = null;

      try
      {
         if (m_strRequestedConfigFile == null)
         {
            // Load the name of configuration file
            m_strRequestedConfigFile = System.getProperty(CONFIG_FILE_NAME, 
                                                       "");
            if ((m_strRequestedConfigFile == null) 
               || (m_strRequestedConfigFile.length() == 0))
            {
               m_strRequestedConfigFile = null;
               s_logger.config("Name of configuration file is not"
                               + " specified using property " 
                               + CONFIG_FILE_NAME + ". Trying to load"
                               + " default configuration file from system"
                               + " classpath.");

               isConfigFile = findConfigFile(DEFAULT_CONFIG_FILE_NAME);
            }
            else
            {
               s_logger.log(Level.CONFIG,CONFIG_FILE_NAME 
                               + " = {0}", m_strRequestedConfigFile);
               
               try
               {
                  // Try to open the file first directly
                  isConfigFile = new FileInputStream(m_strRequestedConfigFile);
                  m_strActualConfigFile = m_strRequestedConfigFile;
               }
               catch (FileNotFoundException fnfExc)
               {
                  // Try to search for it
                  isConfigFile = findConfigFile(m_strRequestedConfigFile);
               }
            }
         }
         else
         {
            s_logger.log(Level.CONFIG,"Name of configuration file is specified" 
                            + " programatically = {0}", m_strRequestedConfigFile);
               
            try
            {
               // Open the file
               isConfigFile = new FileInputStream(m_strRequestedConfigFile);
               m_strActualConfigFile = m_strRequestedConfigFile;
            }
            catch (FileNotFoundException fnfExc)
            {
               // Try to search for it
               isConfigFile = findConfigFile(m_strRequestedConfigFile);
            }
         }

         BufferedInputStream bisConfigFile = null;
         
         // Load the properties
         s_logger.log(Level.CONFIG, "Using configuration file {0}", m_strActualConfigFile);
         m_prpProperties = new Properties();
         try
         {
            bisConfigFile = new BufferedInputStream(isConfigFile);
            m_prpProperties.load(bisConfigFile);
         }
         finally
         {
            // Close the file
            try
            {
               if (bisConfigFile != null)
               {
                  bisConfigFile.close();
               }
            }
            catch (IOException ioeExc)
            {
               // Ignore this
               s_logger.log(
                  Level.WARNING, 
                  "Failed to close buffer for configuration file.", 
                  ioeExc);
            }
         }
      }
      finally
      {
         try
         {
            if (isConfigFile != null)
            {
               isConfigFile.close();
            }
         }
         catch (IOException ioeExc)
         {
            // Ignore this
            s_logger.log(Level.WARNING, 
                         "Failed to close configuration file.", 
                         ioeExc);
         }
      }
      
      // If we read any properties see if there was a dependent file
      // specified and if so then read its settings and replace them
      // with the ones redefined in this file
      if (m_prpProperties != null)
      {
         String strDependentPropertyFile;
         
         strDependentPropertyFile = PropertyUtils.getStringProperty(
                                       m_prpProperties, 
                                       DEPENDENT_CONFIG_FILE_NAME,
                                       "",
                                       "Dependent property file", 
                                       true);
         if (strDependentPropertyFile.length() > 0)
         {
            // A dependent property file was specified so load it first
            // and then add on top of it the current properties
            Config dependentFile;
            Properties prpSettings;
            
            dependentFile = new Config(strDependentPropertyFile);
            prpSettings = dependentFile.getProperties();
            s_logger.log(Level.CONFIG, "Replacing properties specified in"
                         + " dependent file {0} with properties from {1}", 
                         new Object[]{strDependentPropertyFile, m_strActualConfigFile});
            // Now replace the settings which were redefined in this file
            prpSettings.putAll(m_prpProperties);
            // And the end result will be the current properties
            m_prpProperties = prpSettings;
         }
      }
   }
   
   /**
    * Initialize class variables after there was a problem accessing the 
    * property file.
    * 
    *  This method is private so that it cannot be overridden since it is called
    *  from constructor.
    *  
    *  @return Properties - properties that will be actually used by this class 
    */
   private Properties initializePropertiesAfterFileAccessFailure(
   )
   {
      Properties prpSettings;
      
      // This will initialize it to defaults 
      prpSettings = new Properties();
      m_prpProperties = prpSettings;                                             
      // This will cause it to create the default file further down
      if ((m_strRequestedConfigFile == null) 
         || (m_strRequestedConfigFile.length() == 0))
      {
         setPropertyFileName(Config.DEFAULT_CONFIG_FILE_NAME);
      }
      else
      {
         setPropertyFileName(m_strRequestedConfigFile);
      }
      
      return prpSettings;
   }
}
