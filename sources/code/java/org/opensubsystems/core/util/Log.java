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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Class responsible for instantiating of the system logger. This way anybody
 * can use this default logger to output their log messages without worrying
 * what logger to use. 
 *
 * @author bastafidli
 */
public class Log extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Default file name of configuration file.
    */
   public static final String DEFAULT_LOGCONFIG_FILE_NAME = "osslog.properties";

   // Attributes ///////////////////////////////////////////////////////////////

   /**
    * Default Java logger to log messages to the application.
    */
   protected static transient Logger s_lgLogger = null;
   
   /**
    * Helper mutex to synchronize some methods.
    */
   protected static transient String s_strMutex = "log.mutex";

   /**
    * Name of the configuration file actually used to configure the logger.
    */
   protected static String s_strConfigFile;
   
   /**
    * Logger for this class. It will be initialized only after the log 
    * configuration is initialized.
    */
   protected static Logger s_logger;
   
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Static initializer
    */
   static 
   {
      InputStream isConfigFile = null;
      
      try
      {
         // At first, try to use config file from command line definition
         String strLogfilename;
         
         strLogfilename = System.getProperty("java.util.logging.config.file");
         try
         {
            // In case you are wondering while property name below was chosen
            // look at JavaDoc for java.util.logging.LogManager class which
            // defines this property as a standard property to specify logging
            // configuration
            if ((strLogfilename != null) && (strLogfilename.length() > 0))
            {
               isConfigFile = new FileInputStream(strLogfilename);
               s_strConfigFile = strLogfilename;
            }
         }
         catch (FileNotFoundException fnfExc1)
         {
            // Try to search for it, this will also setup s_strConfigFile
            isConfigFile = findConfigFile(strLogfilename);
            
            if (isConfigFile == null)
            {
               // We have to use system.out since the logger is not initialized yet
               System.out.println("File " + strLogfilename 
                  + " set in java.util.logging.config.file doesn't exist");
            }
         }
         if (isConfigFile == null)
         {
            try
            {
               // Open the default file
               isConfigFile = new FileInputStream(DEFAULT_LOGCONFIG_FILE_NAME);
               s_strConfigFile = DEFAULT_LOGCONFIG_FILE_NAME;
            }
            catch (FileNotFoundException fnfExc2)
            {
               // Try to search for it, this will also setup s_strConfigFile
               isConfigFile = findConfigFile(DEFAULT_LOGCONFIG_FILE_NAME);

               if (isConfigFile == null)
               {
                  // We have to use system.out since the logger is not initialized yet
                  System.out.println("Default log configuration file " 
                                     + DEFAULT_LOGCONFIG_FILE_NAME 
                                     + " doesn't exist");
               }
            }
         }
         if (isConfigFile != null)
         {
// TODO: Fix bug: For now disable this since when running application under Tomcat it breaks the logging            
//            LogManager.getLogManager().readConfiguration(isConfigFile);
            System.out.println("Logging subsystem initialized.");
         }
      }
      catch (IOException ioeExc)
      {
         // Logger is not initialized yet, use System.out.
         System.out.println("Cannot initialize logging subsystem.");
         ioeExc.printStackTrace();
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
         }
         finally
         {
            s_logger = Log.getInstance(Log.class);
            // Print this as info because otherwise we wouldn't know where to 
            // change it
            System.out.println("Using log configuration file " + s_strConfigFile);
            s_logger.log(Level.CONFIG, "Using log configuration file {0}", 
                        s_strConfigFile);
         }
      }
   }
   
   /**
    * Hidden constructor to disable creation of instances of this class.
    */
   protected Log(
   )
   {
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Get instance of configured logger which can be used to log messages from 
    * the application.
    *
    * @param classIdentifier - identifier of class to get the logger for
    * @return Logger - instance of logger ready to use
    */
   public static Logger getInstance(
      Class classIdentifier
   )
   {
      return getInstance(classIdentifier.getName());
   }

   /**
    * Get instance of configured logger which can be used to log messages from 
    * the application.
    *
    * @param logIdentifier - identifier of log to get 
    * @return Logger - instance of logger ready to use
    */
   public static Logger getInstance(
      String logIdentifier
   )
   {
      // We may do any additional configuration here but for now we are fine
      // with the default configuration read from file specified by property
      // java.util.logging.config.file as specified in documentation for 
      // LogManager
      return Logger.getLogger(logIdentifier);
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Find configuration file.
    * 
    * @param  strConfigFileName - name of the configuration file
    * @return InputStream - opened configuration file
    * @throws IOException - an error has occurred opening configuration file
    * @throws FileNotFoundException - configuration file cannot be found
    */
   protected static InputStream findConfigFile(
      String strConfigFileName
   ) throws IOException,
            FileNotFoundException
   {
      InputStream isConfigFile = null;
      URL         urlDefaultPropertyFile;

      urlDefaultPropertyFile = FileUtils.findFileOnClassPath(strConfigFileName);
      isConfigFile = urlDefaultPropertyFile.openStream();
      s_strConfigFile = urlDefaultPropertyFile.toString();
      
      return isConfigFile;
   }
}
