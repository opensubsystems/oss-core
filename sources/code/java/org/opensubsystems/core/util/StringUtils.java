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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.opensubsystems.core.error.OSSInvalidDataException;
import org.opensubsystems.core.error.OSSException;

/**
 * Utility methods for String manipulation.
 * 
 * @author OpenSubsystems
 */
public final class StringUtils extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Constant for assigning 
    */
   public static final String NULL_STRING = "null";
   
   /**
    * Constant for assigning 
    */
   public static final String EMPTY_STRING = "";
   
   /**
    * Constant for assigning
    */
   public static final String COMMA_STRING = ",";
   
   /**
    * Keep the original case of the string;
    */
   public static final int CASE_ORIGINAL = 0;
   
   /**
    * Convert the string to upper case.
    */
   public static final int CASE_TOUPPER = 1;
   
   /**
    * Convert the string to lower case.
    */
   public static final int CASE_TOLOWER = 2;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private StringUtils(
   )
   {
      // Do nothing
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   public static String valueIfNotNull(
      Object input
   )
   {
      if (input == null)
      {
         return NULL_STRING;
      }
      else
      {
         return input.toString();
      }
   }
   
   /**
    * Count number of occurrence of lookup in text.
    * 
    * @param strText - text to search in for occurrences of lookup
    * @param cLookup - character to count
    * @return int - number of occurrences of lookup in text.
    */
   public static int count(
      String strText,
      char   cLookup
   )
   {
      int iCount = 0;
      
      if (strText != null)
      {
         int iIndex = strText.indexOf(cLookup);
   
         while (iIndex != -1)
         {
            iCount++;
            iIndex = strText.indexOf(cLookup, iIndex + 1);
         }
      }
      
      return iCount;
   }
   
   /**
    * Parse textual representation of fraction to a floating point number
    * 
    * @param strTextToParse - text in the form 
    *                         "any_text whole_part quotient/divisor any_text"
    * @param dDefaultValue - if the test is unparsable, what default value to 
    *                        return
    * @param bIgnoreRest - if true, this will ignore the rest of the string 
    *                      (any_other_text) after the fraction, if false then  
    *                      the whole string is considered 
    * @return double - number corresponding to the fraction
    */
   public static double parseFraction(
      String  strTextToParse,
      double  dDefaultValue,
      boolean bIgnoreRest
   )
   {
      double parsed = dDefaultValue;
      int    iLength;
      int    iIndex;
      int    iIndexStart;
      int    iIndexEnd;
      int    iNumber;
            
      // lets use "xxxxxxx 123 456 / 789 yyyyy" as example or 
      // lets use "xxxxxxx 123 / 789 yyyyy" as example 
      
      iIndexStart = 0;
      iLength = strTextToParse.length();
      if (bIgnoreRest)
      {
         // Skip while not number
         while ((iIndexStart < iLength) 
               && (!Character.isDigit(strTextToParse.charAt(iIndexStart))))
         {
            iIndexStart++;
         }
         // We skiped "xxxxxxx", iIndexStart is at "123 456 / 789 yyyyy"
      }
      
      // We should be at first digit
      if (iIndexStart < iLength)
      {
         // Find end of the number
         iIndex = iIndexStart;
         while ((iIndex < iLength) 
               && (Character.isDigit(strTextToParse.charAt(iIndex))))
         {
            iIndex++;
         }
         iIndexEnd = iIndex;
         // We skipped "123", iIndexStart is at "123 456 / 789 yyyyy" 
         // iIndexEnd is at " 456 / 789 yyyyy"
       
         if (iIndexStart != iIndexEnd)
         {
            // There was at least some digits
            iNumber = Integer.parseInt(strTextToParse.substring(iIndexStart, 
                                                             iIndexEnd));
            // iNumber is 123

            // There was at least one digit, now is it whole part or quotient?
            // Skip spaces
            while ((iIndex < iLength) 
                  && ((strTextToParse.charAt(iIndex) == ' ')
                     || (strTextToParse.charAt(iIndex) == '-')))
            {
               iIndex++;
            }
            // We skipped "123", iIndex is at "456 / 789 yyyyy" 
            
            // Now we have stopped because of 2 things, we either reached end of
            // string or we have found something other than space, if it is /
            // then it was quotient, if it is digit, then it was whole part
            if (iIndex == iLength)
            {
               // it was a whole part and we are done
               parsed = iNumber;
            }
            else
            {
               int iQuotient;
               int iDivisor;
               
               if (Character.isDigit(strTextToParse.charAt(iIndex)))
               {
                  int iWholePart;
                  
                  // it was a whole part and we continue to look for the quotient
                  iWholePart = iNumber;
                  
                  // Find end of the number
                  iIndexStart = iIndex; // Remember start
                  while ((iIndex < iLength) 
                        && (Character.isDigit(strTextToParse.charAt(iIndex))))
                  {
                     iIndex++;
                  }
                  iIndexEnd = iIndex;
                  // We skipped "456", iStartIndex is at "456 / 789 yyyyy"
                  // And iIndexEnd is at " / 789 yyyyy"
                  
                  iQuotient = Integer.parseInt(strTextToParse.substring(
                                                                 iIndexStart, 
                                                                 iIndexEnd));
                  // iQuotient is 456

                  // Skip spaces
                  while ((iIndex < iLength) 
                        && (strTextToParse.charAt(iIndex) == ' '))
                  {
                     iIndex++;
                  }
                  // And iIndex is at "/ 789 yyyyy"

                  if (strTextToParse.charAt(iIndex) == '/')
                  {   
                     // It was a quotient and we continue to look for divisor
                     
                     iIndexStart = iIndex + 1;
                     while ((iIndexStart < iLength) 
                           && (strTextToParse.charAt(iIndexStart) == ' '))
                     {
                        iIndexStart++;
                     }
                     // And iIndexStart is at "789 yyyyy"
                     
                     // We should be at next digit
                     if (iIndexStart < iLength)
                     {
                        // Find end of the number
                        iIndex = iIndexStart;
                        while ((iIndex < iLength) 
                              && (Character.isDigit(strTextToParse.charAt(
                                                                      iIndex))))
                        {
                           iIndex++;
                        }
                        iIndexEnd = iIndex;
                        // We skiped "789", iStartIndex is at "789 yyyyy"
                        // And iIndexEnd is at " yyyyy"

                        if (iIndexStart != iIndexEnd)
                        {
                           iDivisor = Integer.parseInt(strTextToParse.substring(
                                                          iIndexStart,
                                                          iIndexEnd));
                           // iDivisor is 789
                           if (iDivisor != 0)
                           {   
                              if (iIndexEnd == iLength)
                              {
                                 // And we are at the end of the string
                                 parsed = ((double)(iWholePart)) 
                                          + (((double)iQuotient) 
                                             / ((double)iDivisor));
                              }
                              else
                              {
                                 if (bIgnoreRest)
                                 {
                                    // And we can ignore what is after
                                    parsed = ((double)(iWholePart)) 
                                             + (((double)iQuotient) 
                                                / ((double)iDivisor));
                                 }                              
                                 else
                                 {
                                    // there was something else we don't know 
                                    // what so return the default value                                 
                                 }                                    
                              }
                           }
                        }
                        else
                        {
                           // The divisor is missing, return default value
                        }
                     }
                     else
                     {
                        // The divisor is missing, return default value 
                     }
                  }
                  else
                  {
                     // The divisor is missing, return default value                      
                  }
               }
               else
               {
                  if (strTextToParse.charAt(iIndex) == '/')
                  {   
                     // And iIndex is at "/ 456 yyyyy"

                     // It was a quotient and we continue to look for divisor
                     iQuotient = iNumber;
                     // iQuotient is 123
                     
                     iIndexStart = iIndex + 1;
                     while ((iIndexStart < iLength) 
                           && (strTextToParse.charAt(iIndexStart) == ' '))
                     {
                        iIndexStart++;
                     }
                     // And iIndexStart is at "456 yyyyy"
                     
                     // We should be at next digit
                     if (iIndexStart < iLength)
                     {
                        // Find end of the number
                        iIndex = iIndexStart;
                        while ((iIndex < iLength) 
                              && (Character.isDigit(strTextToParse.charAt(
                                                                      iIndex))))
                        {
                           iIndex++;
                        }
                        iIndexEnd = iIndex;
                        // We skipped "456", iIndexStart is at "456 yyyyy"
                        // iIndexEnd is at " yyyyy"

                        if (iIndexStart != iIndexEnd)
                        {
                           iDivisor = Integer.parseInt(strTextToParse.substring(
                                                 iIndexStart, iIndexEnd));
                           // iDivisor is 456

                           if (iDivisor != 0)
                           {   
                              if (iIndexEnd == iLength)
                              {
                                 // And we are at the end of the string
                                 parsed = ((double)iQuotient) 
                                           / ((double)iDivisor);
                              }
                              else
                              {
                                 if (bIgnoreRest)
                                 {
                                    // And we can ignore what is after
                                    parsed = ((double)iQuotient) 
                                             / ((double)iDivisor);
                                 }                              
                                 else
                                 {
                                    // there was something else we don't know 
                                    // what so return the default value                                 
                                 }                                    
                              }
                           }
                        }
                        else
                        {
                           // The divisor is missing, return default value
                        }
                     }
                     else
                     {
                        // The divisor is missing, return default value 
                     }
                  }
                  else
                  {
                     // It was a whole part and there is something else   
                     if (bIgnoreRest)
                     {
                         // and we are done
                         parsed = iNumber;                        
                     }
                     else
                     {
                        // there was something else we don't know what so
                        // return the default value
                     }
                  }                  
               }
            }
         }         
      }
      
      return parsed;
   }
   
   /**
    * Parse String to array of Strings while treating quoted values as single
    * element.
    * 
    * @param strParse - String to parse
    * @param strDel - String delimiter
    * @param bAllowSingleQuote - single quotes such as ' can be used to group value
    * @param bAllowDoubleQuote - double quote such as " can be used to group value
    * @return String[] - parsed list
    * @throws OSSInvalidDataException - error during parsing
    */
   public static String[] parseQuotedStringToStringArray(
      String  strParse,
      String  strDel,
      boolean bAllowSingleQuote,
      boolean bAllowDoubleQuote
   ) throws OSSInvalidDataException
   {
      String[] arrayStrings;
      
      if (strParse == null)
      {
         arrayStrings = null;
      }
      else
      {
         List<String> lstElements = new ArrayList<>();
         int          iCurrentIndex = 0;
         int          iNextIndex;
         int          iDelLength = strDel.length();
         int          iParseLength = strParse.length();
         
         while (iCurrentIndex < iParseLength)
         {
            if ((bAllowSingleQuote) && (strParse.charAt(iCurrentIndex) == '\''))
            {
               // Find next single quote and treat the things in the middle as
               // single element
               iNextIndex = strParse.indexOf('\'', iCurrentIndex + 1);
               if (iNextIndex == -1)
               {
                  throw new OSSInvalidDataException(
                               "Incorrect input. " + strParse 
                               + " No single quote following the one"
                               + " at location " + iCurrentIndex);
               }
               lstElements.add(strParse.substring(iCurrentIndex + 1, iNextIndex));
               iCurrentIndex = iNextIndex + 1;
               if (strParse.substring(iCurrentIndex).startsWith(strDel))
               {
                  iCurrentIndex += iDelLength;
               }
            }
            else if ((bAllowDoubleQuote) && (strParse.charAt(iCurrentIndex) == '"'))
            {
               // Find next double quote and treat the things in the middle as
               // single element
               iNextIndex = strParse.indexOf('"', iCurrentIndex + 1);
               if (iNextIndex == -1)
               {
                  throw new OSSInvalidDataException(
                               "Incorrect input. " + strParse 
                               + " No double quote following the one"
                               + " at location " + iCurrentIndex);
               }
               lstElements.add(strParse.substring(iCurrentIndex + 1, iNextIndex));
               iCurrentIndex = iNextIndex + 1;            
               if (strParse.substring(iCurrentIndex).startsWith(strDel))
               {
                  iCurrentIndex += iDelLength;
               }
            }
            else
            {
               // Find next separator and treat the things in the middle as
               // single element
               iNextIndex = strParse.indexOf(strDel, iCurrentIndex);
               if (iNextIndex == -1)
               {
                  // No other delimiter found so take the rest of the string
                  lstElements.add(strParse.substring(iCurrentIndex));
                  iCurrentIndex = iParseLength;
               }
               else
               {
                  lstElements.add(strParse.substring(iCurrentIndex, iNextIndex));               
                  iCurrentIndex = iNextIndex + iDelLength;
               }
            }
         }
         arrayStrings = lstElements.toArray(new String[lstElements.size()]);
      }
      
      return arrayStrings;  
   }

   /**
    * Parse String to array of integers.
    * 
    * @param strParse - String to parse
    * @param strDel - String delimiter
    * @return int[] - parsed list
    * @throws OSSException - error during parsing
    */
   public static int[] parseStringToIntArray(
      String strParse,
      String strDel
   ) throws OSSException
   {
      int[] arrayInts;
      try
      {
         if (strParse == null)
         {
             arrayInts = null;
         }
         else
         {
            // TODO: Performance: Memory vs speed, here we allocate list and then
            // another array, how about just counting the number of elements
            // and then allocating array and parsing directly to array without
            // the extra list and copying from list to array?
            List<String> lstInts = parseStringToList(strParse, strDel);
         
            if (lstInts == null || lstInts.size() < 1)
            {
               arrayInts = null;
            }
            else
            {
               Iterator<String> items;
               int              iCount;
               
               arrayInts = new int[lstInts.size()];
               for (iCount = 0, items = lstInts.iterator(); items.hasNext();)
               {
                  arrayInts[iCount++] = Integer.parseInt((items.next()).trim());
               }
            }   
         }
      }
      catch (NumberFormatException eExc)
      {
         throw new OSSInvalidDataException(
                      "Problems with parsing String to array of int.", 
                      eExc);
      }
      return arrayInts;  
   }

   /**
    * Parse String to array of Integers.
    * 
    * @param strParse - String to parse
    * @param strDel - String delimiter
    * @return Integer[] - parsed list
    * @throws OSSException - error during parsing
    */
   public static Integer[] parseStringToIntegerArray(
      String strParse,
      String strDel
   ) throws OSSException
   {
      Integer[] arrayInts;
      try
      {
         if (strParse == null)
         {
             arrayInts = null;
         }
         else
         {
            // TODO: Performance: Memory vs speed, here we allocate list and then
            // another array, how about just counting the number of elements
            // and then allocating array and parsing directly to array without
            // the extra list and copying from list to array?
            List<String> strInts = parseStringToList(strParse, strDel);
         
            if (strInts == null || strInts.size() < 1)
            {
               arrayInts = null;
            }
            else
            {
               arrayInts = new Integer[strInts.size()];
               for (int iCount = 0; iCount < strInts.size(); iCount++)
               {
                  arrayInts[iCount] = Integer.valueOf(
                                         (strInts.get(iCount)).trim());
               }
            }   
         }
      }
      catch (NumberFormatException eExc)
      {
         throw new OSSInvalidDataException(
                      "Problems with parsing String to array of int.", 
                      eExc);
      }
      return arrayInts;  
   }

   /**
    * Parse String to array of Integers.
    * 
    * @param strParse - String to parse
    * @param strDel - String delimiter
    * @param container - if specified then it will be filled with items (it WILL 
    *                    NOT be emptied first). If this is null, the default 
    *                    collection will be allocated. This allows you here
    *                    to pass list or set so this method is more flexible.
    * @return Collection - parsed collection, if container was specified, the 
    *                      same object will be returned. If it was not specified
    *                      default object will be returned. If strParse was not
    *                      null, then this will be not null.
    * @throws OSSException - error during parsing
    */
   public static Collection<Integer> parseStringToIntegerCollection(
      String     strParse,
      String     strDel,
      Collection<Integer> container
   ) throws OSSException
   {
      Collection<Integer> colReturn;
      
      if (strParse == null || strParse.length() < 1)
      {
         if (container != null)
         {
            colReturn = container;
         }
         else
         {
            colReturn = null;
         }
      }
      else
      {
         if (container == null)
         {
            colReturn = new ArrayList<>();
         }
         else
         {
            colReturn = container;
         }
         
         try
         {
            // TODO: Performance: StringTokenizer is considered to be slow
            // because it creates lots of objects internally, consider replacing
            // this with String.indexOf(delimiter)
            StringTokenizer strTokParse = new StringTokenizer(strParse, strDel);
            String          strTemp;
            
            if (container == null)
            {
               // This has to be List since parseStringToList requires it
               colReturn = new ArrayList<>();
            }
            else
            {
               // TODO: Bug: This might have been a possible bug since the 
               // description states that the method will not empty the container. 
               // I have therefore commented this out and it should be removed 
               // later once the rest of the code was verified to work
               // container.clear();
               colReturn = container;
            }
            
            while (strTokParse.hasMoreTokens())
            {
               strTemp = strTokParse.nextToken();
               strTemp = strTemp.trim();
               colReturn.add(Integer.valueOf(strTemp));
            }
         }
         catch (NumberFormatException eExc)
         {
            throw new OSSInvalidDataException(
                         "Problems with parsing String to array of int.", 
                         eExc);
         }
      }
      
      return colReturn;  
   }

   /**
    * Parse String to array of Strings.
    * 
    * @param strParse - String to parse
    * @param strDel - String delimiter
    * @return String[] - parsed list
    */
   public static String[] parseStringToStringArray(
      String strParse,
      String strDel
   )
   {
      String[] arrayStrings;
      
      if (strParse == null)
      {
          arrayStrings = null;
      }
      else
      {
         // TODO: Performance: Memory vs speed, here we allocate list and then
         // another array, how about just counting the number of elements
         // and then allocating array and parsing directly to array without
         // the extra list and copying from list to array?
         List<String> lstStrings = parseStringToList(strParse, strDel);
      
         if ((lstStrings == null) || (lstStrings.isEmpty()))
         {
            arrayStrings = null;
         }
         else
         {
            arrayStrings = lstStrings.toArray(new String[lstStrings.size()]);
         }   
      }
      
      return arrayStrings;  
   }

   /**
    * Parse array of integers to String.
    * 
    * @param arrParse - int array to parse
    * @param strDel - String delimiter
    * @return String - parsed array
    */
   public static String parseIntArrayToString(
      int[]  arrParse,
      String strDel
   ) 
   {
      StringBuilder strbInts = new StringBuilder();
      if ((arrParse != null) && (arrParse.length > 0))
      {
         for (int iCount = 0; iCount < arrParse.length; iCount++)
         {
            if (iCount > 0)
            {
               strbInts.append(strDel);
            }
            strbInts.append(arrParse[iCount]);
         }
      }
      return strbInts.toString();  
   }
   
   /**
    * Parse collection of objects to String by calling toString on each element.
    * 
    * @param colObjects - collection of data objects to parse
    * @param strDel - String delimiter
    * @return String - parsed array
    */
   public static String parseCollectionToString(
      Collection<?> colObjects,
      String        strDel
   ) 
   {
      StringBuilder strbInts = new StringBuilder();
      if ((colObjects != null) && (!colObjects.isEmpty()))
      {
         for (Iterator<?> items = colObjects.iterator(); items.hasNext();)
         {
            if (strbInts.length() > 0)
            {
               strbInts.append(strDel);
            }
            strbInts.append(items.next().toString());
         }
      }
      return strbInts.toString();  
   }

   /**
    * Parse String to List.
    * 
    * @param strParse - String to parse
    * @param strDel - String delimiter
    * @return List - parsed list of items in the string delimited by delimiter
    */
   public static List<String> parseStringToList(
      String strParse,
      String strDel
   )
   {
      return (List<String>)parseStringToCollection(strParse, strDel, false, 
                                              CASE_ORIGINAL, null);  
   }

   /**
    * Parse String to ANY collection you specify and trim each item.
    * 
    * @param strParse - String to parse
    * @param strDel - String delimiter
    * @param bTrim - should it be trimmed or not
    * @param iConvertCase - how to convert the case of the string - one of the
    *                       CASE_XXX constants
    * @param container - if specified then it will be filled with items (it WILL 
    *                    NOT be emptied first). If this is null, the default 
    *                    collection will be allocated. This allows you here
    *                    to pass list or set so this method is more flexible.
    * @return Collection - parsed collection, if container was specified, the 
    *                      same object will be returned. If it was not specified
    *                      default object will be returned. If strParse was not
    *                      null, then this will be not null.
    */
   public static Collection<String> parseStringToCollection(
      String     strParse,
      String     strDel,
      boolean    bTrim,
      int        iConvertCase,
      Collection<String> container
   )
   {
      Collection<String> colReturn;
      
      if ((strParse == null) || (strParse.length() < 1))
      {
         if (container != null)
         {
            colReturn = container;
         }
         else
         {
            colReturn = null;
         }
      }
      else
      {
         // TODO: Performance: StringTokenizer is considered to be slow
         // because it creates lots of objects internally, consider replacing
         // this with String.indexOf(delimiter)
         StringTokenizer strTokParse = new StringTokenizer(strParse, strDel);
         String          strTemp;
         
         if (container == null)
         {
            // This has to be List since parseStringToList requires it
            colReturn = new ArrayList<>();
         }
         else
         {
            // TODO: Bug: This might have been a possible bug since the 
            // description states that the method will not empty the container. 
            // I have therefore commented this out and it should be removed 
            // later once the rest of the code was verified to work
            // container.clear();
            colReturn = container;
         }
         
         if (strParse.startsWith(strDel))
         {
            // If the string starts with the delimiter, tokenizer would skip it
            // but we have to have empty element in front
            colReturn.add("");            
         }
         
         while (strTokParse.hasMoreTokens())
         {
            strTemp = strTokParse.nextToken();
            if (bTrim)
            {
               strTemp = strTemp.trim();
            }
            switch (iConvertCase)
            {
               case (CASE_ORIGINAL) :
               {
                  // do nothing
                  break;
               }
               case (CASE_TOUPPER) :
               {
                  strTemp = strTemp.toUpperCase();
                  break;
               }
               case (CASE_TOLOWER) :
               {
                  strTemp = strTemp.toLowerCase();
                  break;
               }
               default :
               {
                  assert false : "Incorrect case specification.";
               }
            }
            colReturn.add(strTemp);
         }
      }
      return colReturn;  
   }

   /**
    * Count how many elements are in the specified string separated by the 
    * specified delimiter.
    * 
    * @param strParse - String to parse
    * @param strDel - String delimiter
    * @return int - how many elements are in the specified string separated by
    *               the specified delimiter
    */
   public static int countElementsInString(
      String strParse,
      String strDel
   )
   {
      int iCount = 0;
      
      if ((strParse != null) && (strParse.length() > 0))
      {
         // TODO: Performance: StringTokenizer is considered to be slow
         // because it creates lots of objects internally, consider replacing
         // this with String.indexOf(delimiter)
         int iDelimiterLength = strDel.length();
         int iIndex;
         int iPreviousIndex = -1;

         iIndex = strParse.indexOf(strDel);
         while (iIndex != -1)
         {
            // This will count element even if the string starts with the delimiter
            iCount++;
            iPreviousIndex = iIndex;
            iIndex = strParse.indexOf(strDel, iPreviousIndex + iDelimiterLength);
         }
         
         if (iPreviousIndex < strParse.length())
         {
            // And this will count if there is no delimiter at the end
            iCount++;
         }
      }
      
      return iCount;  
   }

   /**
    * Method to limit String length for display and add '...' to the end 
    * 
    * @param limitLength - limit of length
    * @param strValue - String to limit
    * @return String - limited String
    */
   public static String limitStringLength(
      int limitLength,
      String strValue
   )
   {
      StringBuilder sbReturn = new StringBuilder();
      
      if ((limitLength > 0) && (strValue.length() > limitLength))
      {
         // If limit length is lower then 5 we will do just exact substring
         if (limitLength < 5)
         {
            sbReturn.append(strValue.substring(0, limitLength));
         }
         // If limit length is lower then 15 and higher then 4 we will 
         // return substring of (limit - 3) and '...'
         else if (limitLength < 15)
         {
            sbReturn.append(strValue.substring(0, limitLength - 3));
            sbReturn.append("...");
         }
         // If limit length is higher then 15 we will try to find 
         // some space ' ' near before limit and cut string there 
         else
         {
            // if we will not find ' ' near before limit 
            // we will return substring of (limit - 3) and '...'
            if ((strValue.indexOf(" ", limitLength - 12) > (limitLength - 4))
               || (strValue.indexOf(" ", limitLength - 12) < 0))
            {
               sbReturn.append(strValue.substring(0, limitLength - 3));
               sbReturn.append("...");
            }
            // if we will find ' ' near before limit 
            // we will return substring until ' ' and ' ...'
            else
            {
               sbReturn.append(strValue.substring(0, strValue.indexOf(
                                 " ", limitLength - 12)));
               sbReturn.append(" ...");
            }            
         }
      }
      else
      {
         sbReturn.append(strValue);         
      }
      
      return sbReturn.toString();
   }

   /**
    * Method to remove comma from start and from end of the string for examples
    * to use it as parameter to SQL IN operator
    * 
    * @param strToRemoveFrom - String to remove comma from
    * @return String - string with removed commas from the start and end of the 
    *                  string 
    */
   public static String removeComma(
      String strToRemoveFrom
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert strToRemoveFrom.charAt(0) == ','
                 : "String should to start with character ','";
         assert strToRemoveFrom.charAt(strToRemoveFrom.length() - 1) == ','
                 : "String should to end with character ','";
      }
      // we have to remove comma from start and from end of the string
      // because then it can be used for SQL IN operator
      if (strToRemoveFrom.length() > 2)
      {
         strToRemoveFrom = strToRemoveFrom.substring(1, strToRemoveFrom.length() - 1);
      }
      else
      {
         strToRemoveFrom = "";
      }

      return strToRemoveFrom;
   }
   
   /**
    * Concat all the specified strings to a single one
    * 
    * @param strings - strings to connect, all null and empty ones will be ignored
    * @param separator - separator to put in between the string elements
    * @param quote - quote string to put around string elements, if null nothing
    *                will be put around them
    * @return String with concatenated inputs
    */
   public static String concat(
      String[] strings,
      String   separator,
      String   quote
   )
   {
      StringBuilder output = new StringBuilder();
      
      if (strings != null)
      {
         int          iIndex;
         boolean      bSeparator;
         boolean      bQuote;
         
         bSeparator = (separator != null) && (separator.length() > 0);
         bQuote = (quote != null) && (quote.length() > 0);
         for (iIndex = 0; iIndex < strings.length; iIndex++)
         {
            if ((strings[iIndex] != null) && (strings[iIndex].length() > 0))
            {
               if ((output.length() > 0) && (bSeparator))
               {
                  output.append(separator);
               }
               if (bQuote)
               {
                  output.append(quote);
               }
               output.append(strings[iIndex]);            
               if (bQuote)
               {
                  output.append(quote);
               }
            }
         }
      }
      
      return output.toString();
   }
   
   /**
    * Test if any element in the container contains given string.
    * 
    * @param container - container of which elements will be searched to see if
    *                    they contains given text 
    * @param strSearch - text to search in the elements of specified container
    * @return boolean - true if any of the elements in container contains given 
    *                   text
    */
   public static boolean isContained(
      Collection<String> container,
      String             strSearch
   )
   {
      boolean bReturn = false;
      
      if ((container != null) && (!container.isEmpty()))
      {
         for (Iterator<String> itrElements = container.iterator(); 
             (itrElements.hasNext() && (!bReturn));)
         {
            if ((itrElements.next()).indexOf(strSearch) != -1)
            {
               bReturn = true;
            }
         }
      }
      
      return bReturn;
   }

   /**
    * Test if given string contains any element in the container.
    * 
    * @param container - container of which elements will be searched to see if
    *                    they are contained within given text 
    * @param strSearch - text to search in for the elements of specified container
    * @return boolean - true if the search text contains any of the elements in 
    *                   container
    */
   public static boolean contains(
      Collection<String> container,
      String             strSearch
   )
   {
      boolean bReturn = false;
      
      if ((container != null) && (!container.isEmpty()))
      {
         for (Iterator<String> itrElements = container.iterator(); 
             (itrElements.hasNext() && (!bReturn));)
         {
            if (strSearch.indexOf(itrElements.next()) != -1)
            {
               bReturn = true;
            }
         }
      }
      
      return bReturn;
   }
   
   /**
    * Method return boolean result if particular substring is contained 
    * within the list of substrings separated by a separator.
    * 
    * @param strSearchIn - string of all substrings separated by separator to 
    *                      search in 
    * @param strSearchFor - string that will be search for
    * @param strSeparator - item separator
    * @return boolean - true if it contains the ID, false otherwise
    */
   public static boolean containsInSeparatedString(
      String strSearchIn,
      String strSearchFor,
      String strSeparator
   )
   {
      boolean bReturn = false;
      
      StringBuilder sbInputString = new StringBuilder();
      StringBuilder sbSearchString = new StringBuilder();
      
      if (strSearchIn.length() > 0)
      {
         // add separator at the beginning and end of the input string
         sbInputString.append(strSeparator);
         sbInputString.append(strSearchIn);
         sbInputString.append(strSeparator);
         
         // add separator at the beginning and end of the search string
         sbSearchString.append(strSeparator);
         sbSearchString.append(strSearchFor);
         sbSearchString.append(strSeparator);
         
         // search for particular ID
         if (sbInputString.indexOf(sbSearchString.toString()) != -1)
         {
            bReturn = true;            
         }
      }
      
      return bReturn;
   }

   /**
    * Encode URL string by method URLEncoder.encode() - after this encoding 
    * all spaces will be encoded as + character. But there is also problem 
    * with decoding this + character - it is decoded again as character + . 
    * It means after 1st encode process we will replace all occurrences of + by 
    * code for space.
    * E.g.  input string  = "abc def"
    *       after encoded = "abc+def"
    *       after replace = "abc%20def"
    * @param strInput - input string that has to be encoded
    * @return - encoded string
    * @throws UnsupportedEncodingException - error while encode process
    */
   public static String encode(
      String strInput
   ) throws UnsupportedEncodingException
   {
      String strEncoded = URLEncoder.encode(strInput, "UTF-8");
      return strEncoded.replaceAll("\\+", "%20");
   }
}
