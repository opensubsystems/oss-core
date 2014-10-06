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

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Various utility methods useful when working with limited resources.
 * 
 * @author bastafidli
 */
public class ResourceUtils 
{
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Commons logger variable used to log runtime information.
    */
   private static Logger s_logger = Log.getInstance(ResourceUtils.class);

   // Constructors /////////////////////////////////////////////////////////////
    
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private ResourceUtils(
   )
   {
      // Do nothing
   }
   
   // Public methods ///////////////////////////////////////////////////////////

   /**
    * Close the given resource.
    * 
    * @param resource The resource to be closed.
    */
   public static void close(
      Closeable resource
   ) 
   {
      if (resource != null) 
      {
         try 
         {
            resource.close();
         } 
         catch (IOException ioeExc) 
         {
            // Ignore IOException. If you want to handle this anyway, it might 
            // be useful to know that this will generally only be thrown when 
           // the client aborted the request.
            s_logger.log(Level.WARNING, "Exception while closing resource.", ioeExc);
         }
      }
   }
}
