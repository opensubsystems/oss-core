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

import java.io.File;
import java.io.FileFilter;

/**
 * Class to filter files based on specified extension.
 * 
 * @author bastafidli
 */
public class FileExtensionFilter implements FileFilter
{
   // Cached values ////////////////////////////////////////////////////////////
   
   /**
    * Shared file filter instance.
    */
   public static final FileFilter JPEG_IMAGE_FILTER 
                                     = new FileExtensionFilter(
                                        WebConstants.JPEG_IMAGE_EXTENSION);

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * File extension to filter on.
    */
   protected String m_strExtension;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Create new  FileExtensionFilter
    * 
    * @param strExtension - extension to filter on
    */
   public FileExtensionFilter(
      String strExtension
   )
   {
      assert strExtension != null : "Extension cannot be null";
      m_strExtension = strExtension;
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Test if the found file matches the filter.
    *
    * @param  flPathName - file to test
    * @return true - if file matches the filter
    */
   @Override
   public boolean accept(
      File flPathName
   )
   {
      return flPathName.isFile()
             &&  flPathName.getName().toLowerCase().endsWith(m_strExtension);
   }
}
