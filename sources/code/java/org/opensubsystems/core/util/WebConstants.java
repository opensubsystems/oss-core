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

/**
 * Definition of common constants for WWW used throughout the application.
 *
 * @author bastafidli
 */
public interface WebConstants
{
   /** 
    * Default HTTP port.
    */
   int HTTP_PORT_DEFAULT = 80;
   
   /** 
    * Default HTTPS port.
    */
   int HTTP_SECURE_PORT_DEFAULT = 443;

   /** 
    * Standard backup (2nd instance) HTTP port.
    */
   int HTTP_PORT_BACKUP = 8080;

   /** 
    * Backup (2nd instance) HTTPS port.
    */
   int HTTP_SECURE_PORT_BACKUP = 8443;

   /** 
    * Minimal HTTP port. 
    * Zero actually means that random port will be chosen.
    * @see java.net.InetSocketAddress
    */
   int HTTP_PORT_MIN = 0;
   
   /** 
    * Minimal HTTPS port. 
    * Zero actually means that random port will be chosen.
    * @see java.net.InetSocketAddress
    */
   int HTTP_SECURE_PORT_MIN = 0;

   /** 
    * Maximal HTTP port.
    * @see java.net.InetSocketAddress
    */
   int HTTP_PORT_MAX =  65535;

   /** 
    * Maximal HTTPS port.
    * @see java.net.InetSocketAddress
    */
   int HTTP_SECURE_PORT_MAX =  65535;

   /**
    * String defining a http protocol
    */
   String PROTOCOL_HTTP = "http";
   
   /**
    * String defining a https protocol
    */
   String PROTOCOL_HTTPS = "https";

   /**
    * String used as a root URL
    */
   String URL_ROOT = "/";

   /**
    * String used to separate different portions of URLs
    *
    * @see #URL_SEPARATOR_CHAR
    */
   String URL_SEPARATOR = "/";

   /**
    * Character used to separate different portions of URLs
    *
    * @see #URL_SEPARATOR
    */
   char URL_SEPARATOR_CHAR = '/';

   /**
    * String used to separate extension in URLs
    *
    * @see #EXTENSION_SEPARATOR_CHAR
    */
   String EXTENSION_SEPARATOR = ".";

   /**
    * Character used to separate extension in URLs
    *
    * @see #EXTENSION_SEPARATOR
    */
   char EXTENSION_SEPARATOR_CHAR = '.';

   /**
    * String used to separate parameters from the rest of the url
    *
    * @see #URL_PARAMETER_SEPARATOR_CHAR
    */
   String URL_PARAMETER_SEPARATOR = "?";

   /**
    * Character used to separate parameters from the rest of the url
    *
    * @see #URL_PARAMETER_SEPARATOR
    */
   char URL_PARAMETER_SEPARATOR_CHAR = '?';

   /**
    * String used to separate parameter name from value.
    *
    * @see #URL_PARAMETER_VALUE_SEPARATOR_CHAR
    */
   String URL_PARAMETER_VALUE_SEPARATOR = "=";

   /**
    * Character used to separate parameter name from value.
    *
    * @see #URL_PARAMETER_VALUE_SEPARATOR
    */
   char URL_PARAMETER_VALUE_SEPARATOR_CHAR = '=';

   /**
    * String used to separate parameters from each other.
    *
    * @see #URL_PARAMETER_PARAMETER_SEPARATOR_CHAR
    */
   String URL_PARAMETER_PARAMETER_SEPARATOR = "&";

   /**
    * Character used to separate parameters from each other.
    *
    * @see #URL_PARAMETER_PARAMETER_SEPARATOR
    */
   char URL_PARAMETER_PARAMETER_SEPARATOR_CHAR = '&';

   /**
    * Valid extension for web images. ALways should be compared with lower case strings.
    *
    * @see #WEB_IMAGE_EXTENSION_LENGTH
    */
   String JPEG_IMAGE_EXTENSION = ".jpg";

   /**
    * Length of JPEG_IMAGE_EXTENSION.
    *
    * @see #JPEG_IMAGE_EXTENSION
    */
   int WEB_IMAGE_EXTENSION_LENGTH = JPEG_IMAGE_EXTENSION.length();

   /**
    * Valid extension for web pages. ALways should be compared with lower case strings.
    *
    * @see #WEB_PAGE_EXTENSION_LENGTH
    */
   String WEB_PAGE_EXTENSION = ".html";

   /**
    * Length of WEB_PAGE_EXTENSION.
    *
    * @see #WEB_PAGE_EXTENSION
    */
   int WEB_PAGE_EXTENSION_LENGTH = WEB_PAGE_EXTENSION.length();

   /**
    * Default page to display for directory.
    */
   String DEFAULT_DIRECTORY_WEB_PAGE = "index.html";
   
   /**
    * Item separator 
    */
   String ITEM_SEPARATOR = ":";

   /**
    * Object separator
    */
   String OBJECT_SEPARATOR = ";";

   /**
    * Object separator 2 
    */
   String OBJECT_SEPARATOR2 = "#";

}
