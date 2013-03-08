/*
 * Copyright (C) 2003 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSException;

/**
 * Utility class for correct executing external processes. This class was 
 * inspired by article 
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 *  
 * @author bastafidli
 */
public final class ProcessExec extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Return value used when waiting for process to finish was interrupted.
    */
   public static final int INTERRUPTED_PROCESS_RETURN_VALUE = -1;
   
   /**
    * Return value used when waiting for process to finish was interrupted.
    */
   public static final Integer INTERRUPTED_PROCESS_RETURN_VALUE_OBJ 
                                  = new Integer(INTERRUPTED_PROCESS_RETURN_VALUE);

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(ProcessExec.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private ProcessExec(
   )
   {
      // Do nothing
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Execute specified command or process and returns only after the command 
    * finished.
    * 
    * @param commandline - array containing the command to call and its arguments,
    *                      see Runtime.exec for more description 
    * @param bCapture - if true then the output and error will be captured 
    *                   (careful about memory) and can be read
    * @return Object[] - index 0 - Integer value of the exit code
    *                  - index 1 - StreamGobbler for the output stream
    *                  - index 2 - StreamGobbler for the error stream
    * @throws IOException - error in function
    * @throws OSSException - error in function
    */
   public static Object[] executeAndWait(
      String   commandline,
      boolean  bCapture
   ) throws IOException,
            OSSException
   {
      Object[] returnValue;
      
      s_logger.log(Level.FINEST, "Executing command {0}", commandline);
      returnValue = execute(commandline, bCapture);
      // any error???
      int exitVal;
      
      try
      {
         exitVal = ((Process)returnValue[0]).waitFor();
         returnValue[0] = new Integer(exitVal);
      }
      catch (InterruptedException ieExc)
      {
         // We don't know what has happened so return default value
         returnValue[0] = INTERRUPTED_PROCESS_RETURN_VALUE_OBJ;         
      }
      
      return returnValue;
   }
   
   /**
    * Execute specified command or process and returns only after the command 
    * finished.
    * 
    * @param commandline - array containing the command to call and its arguments,
    *                      see Runtime.exec for more description 
    * @param environment - array containing the environment for the process
    *                      or just null to inherit environment,
    *                      see Runtime.exec for more description 
    * @param workingDir - current working directory for the process or just null
    *                     to inherit from parent
    *                     see Runtime.exec for more description 
    * @param bCapture - if true then the output and error will be captured 
    *                   (careful about memory) and can be read
    * @param lMaxCaptureSize - maximal size of EACH output to be held in memory.
    * @return Object[] - index 0 - Integer value of the exit code
    *                  - index 1 - StreamGobbler for the output stream
    *                  - index 2 - StreamGobbler for the error stream
    * @throws IOException - error in function
    */
   public static Object[] executeAndWait(
      String[] commandline,
      String[] environment,
      File     workingDir,
      boolean  bCapture,
      long     lMaxCaptureSize
   ) throws IOException 
   {
      Object[] returnValue;
      
      returnValue = execute(commandline, environment, workingDir, bCapture, 
                            lMaxCaptureSize);
   
      // any error???
      int exitVal;
      
      try
      {
         exitVal = ((Process)returnValue[0]).waitFor();
         returnValue[0] = new Integer(exitVal);
      }
      catch (InterruptedException ieExc)
      {
         // We don't know what has happened so return default value
         returnValue[0] = INTERRUPTED_PROCESS_RETURN_VALUE_OBJ;
      }
      
      return returnValue;
   }

   /**
    * Execute specified command or process and returns immediately even before 
    * the command finished.
    * 
    * @param commandline - String containing the command to call and its arguments
    *                      it will be parsed to follow requirements of 
    *                      Runtime.exec
    * @param bCapture - if true then the output and error will be captured 
    *                   (careful about memory) and can be read
    * @return Object[] - index 0 - Process object for the executed process
    *                  - index 1 - StreamGobbler for the output stream
    *                  - index 2 - StreamGobbler for the error stream
    * @throws IOException - error in function
    * @throws OSSException - error in function
    */
   public static Object[] execute(
      String   commandline,
      boolean  bCapture
   ) throws IOException,
            OSSException
   {
      Object[] returnValue;
      
      s_logger.log(Level.FINEST, "Executing command {0}", commandline);
      returnValue = execute(StringUtils.parseQuotedStringToStringArray(
                               commandline, " ", true, true),
                            null, null, bCapture, Long.MAX_VALUE);
      
      return returnValue;
   }
   
   
   /**
    * Execute specified command or process and returns immediately even before 
    * the command finished.
    * 
    * @param commandline - array containing the command to call and its arguments,
    *                      see Runtime.exec for more description 
    * @param environment - array containing the environment for the process
    *                      or just null to inherit environment,
    *                      see Runtime.exec for more description 
    * @param workingDir - current working directory for the process or just null
    *                     to inherit from parent
    *                     see Runtime.exec for more description 
    * @param bCapture - if true then the output and error will be captured 
    *                   (careful about memory) and can be read
    * @param lMaxCaptureSize - maximal size of EACH output to be held in memory.
    * @return Object[] - index 0 - Process object for the executed process
    *                  - index 1 - StreamGobbler for the output stream
    *                  - index 2 - StreamGobbler for the error stream
    * @throws IOException - error in function
    */
   public static Object[] execute(
      String[] commandline,
      String[] environment,
      File     workingDir,
      boolean  bCapture,
      long     lMaxCaptureSize
   ) throws IOException
   {      
      String[] cmd;
      Object[] returnValue = new Object[3];

      if (GlobalConstants.isWindows())
      {
         cmd = new String[3];
         cmd[0] = "cmd.exe";
         cmd[1] = "/C";
         cmd[2] = StringUtils.concat(commandline, " ", null);
      }
      else
      {
         cmd = commandline;
      }
      
      // TODO: JDK 1.5: Consider using ProcessBuilder
      Runtime rt = Runtime.getRuntime();
      s_logger.log(Level.FINEST, "Executing command {0}", 
                   StringUtils.concat(cmd, " ", null));
      Process proc = rt.exec(cmd);
      
      // any error message?
      StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), 
                                                     "ERROR", bCapture, 
                                                     lMaxCaptureSize);            
      
      // any output?
      StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), 
                                                      "OUTPUT", bCapture, 
                                                      lMaxCaptureSize);
      
      // kick them off
      errorGobbler.start();
      outputGobbler.start();
      
      returnValue[0] = proc;
      returnValue[1] = outputGobbler;
      returnValue[2] = errorGobbler;  
      
      return returnValue;
   }
}
