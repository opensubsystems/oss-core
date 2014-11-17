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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensubsystems.core.error.OSSConfigException;
import org.opensubsystems.core.error.OSSException;

/**
 * Class responsible for encrypting of data.
 *
 * @author OpenSubsystems
 */
// TODO: BouncyCastle: Investigate if we can replace BC with default provider from JRE/JDK
public final class CryptoUtils extends OSSObject
{
   // Configuration constants //////////////////////////////////////////////////
   
   /**
    * Configuration setting allowing to specify algorithm to generate message 
    * digests for verifying passwords. 
    * The standard names of the algorithms are defined by 
    * Java Cryptography Architecture API Specification Reference
    * http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppA 
    */
   public static final String PASSWORD_DIGEST_ALGORITHM 
                                 = "oss.messagedigest.algorithm";
   
   /**
    * Configuration setting allowing to specify algorithm implementation 
    * provider to use for an algorithm defined by property 
    * oss.messagedigest.algorithm. 
    */
   public static final String PASSWORD_ALGORITHM_PROVIDER 
                                 = "oss.messagedigest.provider";
   
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Default algorithm to use to encrypt passwords. Get a message digest object 
    * using the SHA-512 algorithm (since SHA1 and MD5 are not secure anymore).
    * The standard names of the algorithms are defined by 
    * Java Cryptography Architecture API Specification Reference
    * http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppA
    */
   public static final String PASSWORD_DIGEST_ALGORITHM_DEFAULT = "SHA-512";
   
   /**
    * Default provider algorithm of which to use. By default we use Bouncy 
    * Castle algorithm implementations.
    * TODO: Configuration: Bouncy Castle: JRE/JDK now include default provider
    * so we may not need this one
    */
   public static final String PASSWORD_ALGORITHM_PROVIDER_DEFAULT = "BC"; 
   
   // Cached variables /////////////////////////////////////////////////////////
   
   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(CryptoUtils.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Static initializer
    */
   static
   {
      // Install the Bouncy Castle provider so that we have at least the default
      // provider always available
      Security.addProvider(new BouncyCastleProvider());
   }
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private CryptoUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Compute cryptographic has of the specified data.
    *
    * @param arData - data to compute cryptographic hash for
    * @return String - generated cryptographic hash of the data
    * @throws OSSException - an error has occurred
    */
   public static String computeHash(
      byte[] arData
   ) throws OSSException
   {
      MessageDigest  messageDigest;

      messageDigest = getMessageDigestAlgorithmInstance();
      
      // Calculate the digest
      messageDigest.update(arData);

      // Get the data hash
      byte[] arBytes = messageDigest.digest();
 
      // TODO: Improve: commons-codec: This can be replaced with
      // strFinal = new String(Hex.encodeHex(arrBytes));
      StringBuilder sbFinal = new StringBuilder();
      for (int iCount = 0; iCount < arBytes.length; iCount++)
      {
         // By converting it into the hexa-decimal number format  it is 
         // guaranteed, that the final string will have maximal length 
         // 2 * length (e.g. 32 chars (16 chars is the output from SHA1 and each
         // sign from the SHA1 string can be converted at most to the 
         // 2 chars - hexa-decimal number (from 0 to 255))) 
         sbFinal.append(Integer.toHexString(
            (int) arBytes[iCount] + (-1 * Byte.MIN_VALUE)));
      }
      return sbFinal.toString();
   }

   /**
    * Compute cryptographic has of the specified data.
    *
    * @param strData - data to compute cryptographic hash for
    * @return String - generated cryptographic hash of the data
    * @throws OSSException - an error has occurred
    */
   public static String computeHash(
      String strData
   ) throws OSSException
   {
      if (strData != null)
      {
         return computeHash(strData.getBytes());
      }
      else
      {
         return computeHash((byte[])null);
      }      
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Get instance of the message digest algorithm that should be used for 
    * current execution context.
    * 
    * @return MessageDigest - instance of message digest algorithm
    * @throws OSSException - an error has occurred
    */
   private static MessageDigest getMessageDigestAlgorithmInstance(
   ) throws OSSException
   {
      // Read configuration parameters
      Properties prpSettings;
      String     strPasswordAlgorithm;
      String     strPasswordAlgorithmProvider;
      
      MessageDigest messageDigest;
      
      prpSettings = Config.getInstance().getProperties();
      strPasswordAlgorithm = PropertyUtils.getStringProperty(
                                prpSettings, PASSWORD_DIGEST_ALGORITHM, 
                                PASSWORD_DIGEST_ALGORITHM_DEFAULT,
                                "Password digest algorithm", true);
      
      strPasswordAlgorithmProvider = PropertyUtils.getStringProperty(
                                        prpSettings, 
                                        PASSWORD_ALGORITHM_PROVIDER,
                                        PASSWORD_ALGORITHM_PROVIDER_DEFAULT,
                                        "Password digest algorithm provider",
                                        true);
      
      try
      {
         messageDigest = getMessageDigestAlgorithmInstance(
                            strPasswordAlgorithm, 
                            strPasswordAlgorithmProvider);
      }
      catch (OSSException ossExc)
      {
         s_logger.log(Level.CONFIG,"Algorithm {0}" 
                         + " set in" + " property "
                         + PASSWORD_DIGEST_ALGORITHM 
                         + " provided by provider {1}" 
                         + " set in property " + PASSWORD_ALGORITHM_PROVIDER 
                         + " doesn''t exist, using default values " 
                         + PASSWORD_DIGEST_ALGORITHM_DEFAULT + " and " 
                         + PASSWORD_ALGORITHM_PROVIDER_DEFAULT, 
                         new Object[]{strPasswordAlgorithm, 
                                      strPasswordAlgorithmProvider});
         strPasswordAlgorithm = PASSWORD_DIGEST_ALGORITHM_DEFAULT;
         strPasswordAlgorithmProvider = PASSWORD_ALGORITHM_PROVIDER_DEFAULT;
         
         s_logger.log(Level.CONFIG,"Final value of " + PASSWORD_DIGEST_ALGORITHM 
                         + " = {0}", strPasswordAlgorithm);
         s_logger.log(Level.CONFIG,"Final value of " + PASSWORD_ALGORITHM_PROVIDER 
                         + " = {0}", strPasswordAlgorithmProvider);
      
         messageDigest = getMessageDigestAlgorithmInstance(
                            strPasswordAlgorithm, 
                            strPasswordAlgorithmProvider);
      }
      
      return messageDigest;
   }

   /**
    * Get instance of the message digest algorithm that should be used for 
    * current execution context.
    * 
    * @return MessageDigest - instance of message digest algorithm
    * @throws OSSException - an error has occurred
    */
   private static MessageDigest getMessageDigestAlgorithmInstance(
      String strPasswordAlgorithm,
      String strPasswordAlgorithmProvider
   ) throws OSSException
   {
      MessageDigest messageDigest;

      try
      {
         if ((strPasswordAlgorithmProvider != null) 
            && (strPasswordAlgorithmProvider.length() > 0))
         {
            messageDigest = MessageDigest.getInstance(
                                             strPasswordAlgorithm, 
                                             strPasswordAlgorithmProvider);
         }
         else
         {
            messageDigest = MessageDigest.getInstance(strPasswordAlgorithm); 
         }
      }
      catch (NoSuchAlgorithmException | NoSuchProviderException exc)
      {
         assert (!PASSWORD_DIGEST_ALGORITHM_DEFAULT.equals(
                     strPasswordAlgorithm))
                || (!PASSWORD_ALGORITHM_PROVIDER_DEFAULT.equals(
                        strPasswordAlgorithmProvider))
                : "Default algorithm provided by default provider has to be"
                  + " always found.";
         throw new OSSConfigException("Cannot find message digest algorithm", 
                                      exc);
      }
      
      return messageDigest;
   }
}
