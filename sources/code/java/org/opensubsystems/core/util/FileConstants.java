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

/**
 * This interface contains common constants used with file operations.
 *  
 * @author bastafidli
 */
public interface FileConstants
{
   /**
    * Constant for file extension type. Includes the '.'.
    */
   String FILE_EXTENTION_PDF = ".pdf";

   /**
    * Constant for file extension type. Includes the '.'.
    */
   String FILE_EXTENTION_TIF = ".tif";

   /**
    * Constant for file extension type. Includes the '.'.
    */
   String FILE_EXTENTION_JPG = ".jpg";

   /**
    * Extension in thumbnail filename placed after the name fore the real file
    * extension.
    */
   String FILE_THUMBNAIL_ID_EXTENSION = "_thm"; 
   
   /**
    * Constant for thumbnail file extension type. Includes the '.'.
    */
   String FILE_EXTENTION_THUMBNAIL_JPG 
             = FILE_THUMBNAIL_ID_EXTENSION + FILE_EXTENTION_JPG;
}
