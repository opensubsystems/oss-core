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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInternalErrorException;

/**
 * Class used for safe file manipulation. It simulates database transaction for 
 * file operations such as copy, move, replace or delete. The client should 
 * prepare any new or modified file to temporary location. Then the client should 
 * call prepareToCommit() method which will prepare the file for the safe 
 * manipulation. After everything is ready, commit() method should be called, 
 * otherwise rollback() method should by called.
 * 
 * @author OpenSubsystems
 */
public class FileCommitUtils extends OSSObject
{
   // Configuration settings ///////////////////////////////////////////////////
   
   /**
    * Configuration setting for how many times to retry file commit or rollback 
    * operations in case of error before the commit or rollback of file 
    * manipulation operation is aborted.
    */
   public static final String FILECOMMIT_RETRY_COUNT = "oss.filecommit.retrycount";
   
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Default value for configuration setting how many times to retry file commit 
    * operations in case or error before we give up.
    */
   public static final int FILECOMMIT_RETRY_COUNT_DEFAULT = 3;

   /**
    * Constant for sleeping time duration
    */
   public static final int SLEEP_TIME = 3000;

   /**
    * Constant for ready to copy directory name
    */
   public static final String FILE_PATH_READY_TO_COPY = "ready_to_copy";

   /**
    * Constant for ready to move directory name
    */
   public static final String FILE_PATH_READY_TO_MOVE = "ready_to_move";

   /**
    * Constant for ready to replace directory name
    */
   public static final String FILE_PATH_READY_TO_REPLACE = "ready_to_replace";

   /**
    * Constant for ready to replace directory name
    */
   public static final String FILE_PATH_READY_TO_BE_REPLACED = "ready_to_be_replaced";

   /**
    * Constant for ready to delete directory name
    */
   public static final String FILE_PATH_READY_TO_DELETE = "ready_to_delete";

   /**
    * Return code for successful status
    */
   public static final int RC_OK = 0;

   /**
    * Object code for for successful status
    */
   public static final Integer RC_OK_OBJ = new Integer(RC_OK);

   /**
    * Return code for DB exception status
    */
   protected static final int RC_DB_ERR = 1;

   /**
    * Object code for for DB exception status
    */
   public static final Integer RC_DB_ERR_OBJ = new Integer(RC_DB_ERR);

   /**
    * Return code for file exception status
    */
   protected static final int RC_FILE_ERR = 2;

   /**
    * Object code for for file exception status
    */
   public static final Integer RC_FILE_ERR_OBJ = new Integer(RC_FILE_ERR);

   /**
    * Constant for initial status: no action yet
    */
   public static final int STATUS_INITIAL = 0;

   /**
    * Constant for status: prepare to commit
    */
   public static final int STATUS_PREPARE_TO_COMMIT = 1;

   /**
    * Constant for status: commit
    */
   public static final int STATUS_COMMIT = 2;

   /**
    * Constant for status: rollback
    */
   public static final int STATUS_ROLLBACK = 3;

   /**
    * Constant for sender list identifier: copy
    */
   public static final int LIST_IDENTIFIER_COPY = 1;

   /**
    * Constant for sender list identifier: replace
    */
   public static final int LIST_IDENTIFIER_REPLACE = 2;

   /**
    * Constant for sender list identifier: to be replaced
    */
   public static final int LIST_IDENTIFIER_TO_BE_REPLACED = 3;

   /**
    * Constant for sender list identifier: replace
    */
   public static final int LIST_IDENTIFIER_MOVE = 4;

   /**
    * Constant for sender list identifier: replace
    */
   public static final int LIST_IDENTIFIER_DELETE = 5;

   /**
    * Constant for sender identifier: commit
    */
   public static final int SENDER_COMMIT = 1;

   /**
    * Constant for sender identifier: rollback
    */
   public static final int SENDER_ROLLBACK = 2;
   
   // Attributes ///////////////////////////////////////////////////////////////   
   
   /**
    * Error returning code
    */
   protected Integer m_iReturnCode;

   /**
    * List of file names and path names that will be copied FROM (sources)
    */
   protected List<TwoElementStruct<String, String>> m_lstCopyFromFiles;

   /**
    * List of file names and path names that will be copied TO (destinations)
    */
   protected List<TwoElementStruct<String, String>> m_lstCopyToFiles;

   /**
    * List of file names and path names that will be moved FROM (sources)
    */
   protected List<TwoElementStruct<String, String>> m_lstMoveFromFiles;

   /**
    * List of file names and path names that will be moved TO (destinations)
    */
   protected List<TwoElementStruct<String, String>> m_lstMoveToFiles;

   /**
    * List of file names and path names that will be replaced FROM (sources)
    */
   protected List<TwoElementStruct<String, String>> m_lstReplaceFromFiles;

   /**
    * List of file names and path names that will be replaced TO (destinations)
    */
   protected List<TwoElementStruct<String, String>> m_lstReplaceToFiles;

   /**
    * List of file names and path names that will be deleted
    */
   protected List<TwoElementStruct<String, String>> m_lstDeleteFiles;

   /**
    * Current status of the file commit (prepare commit, commit or rollback). 
    * Status will be used for checking if there will be not called prepare
    * to commit, commit or rollback more than once one after another. It will
    * contain one of the STATUS_XXX values.
    */
   protected int m_iStatus;

   /**
    * Flag signaling if moved files will be deleted from ready_to_move directory 
    * during rollback process. This flag will be TRUE if there will be moved 
    * files from temporary directory to the repository. If error will occurred, 
    * there is not important move files back from ready_to_move directory to the 
    * temporary one.
    */
   protected boolean m_bMovedFilesDeleteForRollback;

   // Class variables ///////////////////////////////////////////////////////////////

   // === Schema for ready to copy process ==========================================
   //            LIST                 DIRECTORY              COMMIT         ROLLBACK
   //--------------------------------------------------------------------------------
   // m_lstReadyToCopyFrom      = /ready_to_copy/         ---->>>|          delete
   //                                                            |
   // m_lstReadyToCopyTo        = /<final_destination>/   <<<----|
   // ===============================================================================
   /**
    * List of files that are ready to copy FROM (sources).
    * a.) This list will be used for commit if everything will be ok. Files will be renamed
    * from current 'ready_to_copy' subdirectory into their final destination.
    * b.) This list will be used for rollback if something fails. Files will be deleted
    * from current 'ready_to_copy' subdirectory.
    */
   protected List<File> m_lstReadyToCopyFrom;

   /**
    * List of files that are ready to copy TO (destinations).
    * This list will be used for commit if everything will be ok. Files will be
    * renamed from current 'ready_to_copy' subdirectory into final destination. This
    * list will stored these final destinations.
    */
   protected List m_lstReadyToCopyTo;


   // === Schema for ready to move process ==========================================
   //           LIST                    DIRECTORY            COMMIT         ROLLBACK
   //--------------------------------------------------------------------------------
   // m_lstReadyToMoveFrom       =    /ready_to_move/      ---->>>|         ---->>>|
   //                                                             |                |
   // m_lstReadyToMoveTo         = /<final_destination>/   <<<----|                |
   //                            /<original_destination>/                   <<<----|
   // ===============================================================================
   /**
    * List of files that are ready to move FROM (sources).
    * a.) This list will be used for commit if everything will be ok. Files will be moved
    * from current 'ready_to_move' subdirectory into their final destination.
    * b.) This list will be used for rollback if something fails. Files will be moved
    * back from current 'ready_to_move' subdirectory to the original directory
    * where they were stored before.
    */
   protected List<File> m_lstReadyToMoveFrom;

   /**
    * List of files that are ready to move TO (destinations).
    * a.) This list will be used for commit if everything will be ok. Files will be moved
    * from current 'ready_to_move' subdirectory into their final destination. This
    * list will stored these final destinations.
    * b.) This list will be used for rollback if something fails. Files will be moved
    * back from current 'ready_to_move' subdirectory to the original directory
    * where they were stored before. List will stored original destinations.
    */
   protected List<File> m_lstReadyToMoveTo;


   // === Schema for ready to be replaced process ===================================
   //           LIST                    DIRECTORY            COMMIT         ROLLBACK
   //--------------------------------------------------------------------------------
   // m_lstReadyToBeReplacedFrom = /ready_to_be_replaced/    delete         ---->>>|
   //                                                                              |
   // m_lstReadyToBeReplacedTo   = /<final_destination>/                    <<<----|
   // ===============================================================================
   /**
    * List of files that are ready to be replaced FROM (sources).
    * a.) This list will be used for commit if everything will be ok. Files will be deleted
    * from current 'ready_to_be_replaced' subdirectory.
    * b.) This list will be used for rollback if something fails. Files will be renamed
    * back from current 'ready_to_be_replaced' subdirectory to their original directory
    * where they were stored before (their destinations are stored in the list
    * m_lstReadyToBeReplacedTo).
    */
   protected List<File> m_lstReadyToBeReplacedFrom;
   
   /**
    * List of files that are ready to be replaced TO (destinations).
    * This list will be used for rollback if something fails. Files will be renamed
    * back from current 'ready_to_be_replaced' subdirectory to the original directory
    * stored in this list.
    */
   protected List<File> m_lstReadyToBeReplacedTo;


   // === Schema for ready to replace process =======================================
   //           LIST                    DIRECTORY            COMMIT         ROLLBACK
   //--------------------------------------------------------------------------------
   // m_lstReadyToReplaceFrom    = /ready_to_replace/      ---->>>|         delete
   //                                                             |
   // m_lstReadyToReplaceTo      = /<final_destination>/   <<<----|
   // ===============================================================================
   /**
    * List of files that are ready to replace FROM (sources).
    * a.) This list will be used for commit if everything will be ok. Files will be renamed
    * from current 'ready_to_replace' subdirectory into their final destination.
    * b.) This list will be used for rollback if something fails. Files will be deleted
    * from current 'ready_to_replace' subdirectory.
    */
   protected List<File> m_lstReadyToReplaceFrom;

   /**
    * List of files that are ready to replace TO (destinations).
    * This list will be used for commit if everything will be ok. Files will be renamed
    * from current 'ready_to_replace' subdirectory into their final destination. This
    * list will stored these final destinations.
    */
   protected List<File> m_lstReadyToReplaceTo;


   // === Schema for ready to delete process ========================================
   //           LIST                    DIRECTORY            COMMIT         ROLLBACK
   //--------------------------------------------------------------------------------
   // m_lstReadyToDeleteFrom = /ready_to_delete/             delete         ---->>>|
   //                                                                              |
   // m_lstReadyToDeleteTo   = /<final_destination>/                        <<<----|
   // ===============================================================================
   /**
    * List of files that are ready to delete FROM (sources).
    * a.) This list will be used for commit if everything will be ok. Files will be deleted
    * from current 'ready_to_delete' subdirectory.
    * b.) This list will be used for rollback if something fails. Files will be renamed
    * back from current 'ready_to_delete' subdirectory to the original directory
    * where they were stored before.
    */
   protected List<File> m_lstReadyToDeleteFrom;

   /**
    * List of files that are ready to delete TO (destinations).
    * This list will be used for rollback if something fails. Files will be renamed
    * back from current 'ready_to_delete' subdirectory to the original directory
    * stored in this list.
    */
   protected List<File> m_lstReadyToDeleteTo;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(FileCommitUtils.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor
    */
   public FileCommitUtils()
   {
      this(new Integer(STATUS_INITIAL), null, null, null, null, null, null, 
           null, false);
   }

   /**
    * Full constructor
    * 
    * @param iReturnCode - error returning code
    * @param lstCopyFromFiles - list of file and path names that will be copied FROM (sources)
    * @param lstCopyToFiles - list of file and path names that will be copied TO (destination)
    * @param lstMoveFromFiles - list of file and path names that will be moved FROM (sources)
    * @param lstMoveToFiles - list of file and path names that will be moved TO (destination)
    * @param lstReplaceFromFiles - list of file and path names that will be replaced FROM (sources)
    * @param lstReplaceToFiles list of file and path names that will be replaced TO (destinations)
    * @param lstDeleteFiles - list of file and path names that will be deleted
    * @param bMovedFilesDeleteForRollback - flag signaling if moved files will be deleted from 
    *                                       ready_to_move directory during rollback process.
    *                                       This flag will be TRUE if there will be moved files 
    *                                       from temporary directory to the repository. If error 
    *                                       will occurred, there is not important move files back 
    *                                       from ready_to_move directory to the temporary one.
    */
   public FileCommitUtils(
      Integer                                iReturnCode,
      List<TwoElementStruct<String, String>> lstCopyFromFiles,
      List<TwoElementStruct<String, String>> lstCopyToFiles,
      List<TwoElementStruct<String, String>> lstMoveFromFiles,
      List<TwoElementStruct<String, String>> lstMoveToFiles,
      List<TwoElementStruct<String, String>> lstReplaceFromFiles,
      List<TwoElementStruct<String, String>> lstReplaceToFiles,
      List<TwoElementStruct<String, String>> lstDeleteFiles,
      boolean                                bMovedFilesDeleteForRollback
   )
   {
      super();
      initGlobalLists();

      m_iReturnCode = iReturnCode;
      if (lstCopyFromFiles != null)
      {
         m_lstCopyFromFiles = lstCopyFromFiles;
      }
      else
      {
         m_lstCopyFromFiles = Collections.emptyList();
      }
      if (lstCopyToFiles != null)
      {
         m_lstCopyToFiles = lstCopyToFiles;
      }
      else
      {
         m_lstCopyToFiles = Collections.emptyList();
      }
      if (lstMoveFromFiles != null)
      {
         m_lstMoveFromFiles = lstMoveFromFiles;
      }
      else
      {
         m_lstMoveFromFiles = Collections.emptyList();
      }
      if (lstMoveToFiles != null)
      {
         m_lstMoveToFiles = lstMoveToFiles;
      }
      else
      {
         m_lstMoveToFiles = Collections.emptyList();
      }
      if (lstReplaceFromFiles != null)
      {
         m_lstReplaceFromFiles = lstReplaceFromFiles;
      }
      else
      {
         m_lstReplaceFromFiles = Collections.emptyList();
      }
      if (lstReplaceToFiles != null)
      {
         m_lstReplaceToFiles = lstReplaceToFiles;
      }
      else
      {
         m_lstReplaceToFiles = Collections.emptyList();
      }
      if (lstDeleteFiles != null)
      {
         m_lstDeleteFiles = lstDeleteFiles;
      }
      else
      {
         m_lstDeleteFiles = Collections.emptyList();
      }
      m_bMovedFilesDeleteForRollback = bMovedFilesDeleteForRollback;
      m_iStatus = FileCommitUtils.STATUS_INITIAL;
   }

   // main methods ////////////////////////////////////////////////

   /**
    * Method will prepare files to commit. It will copy, move, replace and delete files that 
    * were send form the controller into the ready_to_XXX directory. If something fails
    * during this operation, there will be set up list for restoring changes back.
    * 
    * @throws IOException - error occurred during file moving
    */
   public void prepareToCommit(
   ) throws IOException
   {
      // don't process this method when ReturnCode is null 
      if (m_iReturnCode != null)
      {
         StringBuilder sbTemp  = new StringBuilder();
         StringBuilder sbTemp1 = new StringBuilder();
         StringBuilder sbTemp2 = new StringBuilder();
   
         TwoElementStruct<String, String> sourceElement = null;
         TwoElementStruct<String, String> destinElement = null;
         File fSourceFile               = null;
         File fSourceFileBackUp         = null;
         File fDestinationFile;
         File fFinalDestinationFile;
         File fDestinationFileBackUp    = null;
         File fPathTemp;
   
         // Initialize lists that will stored already prepared files to commit. These lists 
         // will be used for rollback when something will fail.
         List<File> lstReadyToCopyFilesFrom = new ArrayList<>(m_lstCopyToFiles.size());
         List<File> lstReadyToCopyFilesTo   = new ArrayList<>(m_lstCopyToFiles.size());
         List<File> lstReadyToMoveFilesFrom = new ArrayList<>(m_lstMoveToFiles.size());
         List<File> lstReadyToMoveFilesTo   = new ArrayList<>(m_lstMoveToFiles.size());
         List<File> lstReadyToMoveFilesToOriginal = new ArrayList<>(m_lstMoveToFiles.size());
         List<File> lstReadyToReplaceFilesFrom    = new ArrayList<>(m_lstReplaceToFiles.size());
         List<File> lstReadyToReplaceFilesTo      = new ArrayList<>(m_lstReplaceToFiles.size());
         List<File> lstReadyToBeReplacedFilesFrom = new ArrayList<>(m_lstReplaceToFiles.size());
         List<File> lstReadyToBeReplacedFilesTo   = new ArrayList<>(m_lstReplaceToFiles.size());
         List<File> lstReadyToDeleteFilesFrom     = new ArrayList<>(m_lstDeleteFiles.size());
         List<File> lstReadyToDeleteFilesTo       = new ArrayList<>(m_lstDeleteFiles.size());
         
         Iterator<TwoElementStruct<String, String>> itHelp1;
         Iterator<TwoElementStruct<String, String>> itHelp2;
         
         assert m_iStatus == FileCommitUtils.STATUS_INITIAL 
                : "Cannot prepare file transaction to commit while " +
                  "there is not initial status defined (file " +
                  "transaction has been already started).";
   
         if (m_iStatus == FileCommitUtils.STATUS_INITIAL)
         {
            m_iStatus = FileCommitUtils.STATUS_PREPARE_TO_COMMIT;
         }
   
         try
         {
            // copy processed files only if there was no error generated from the controller
            if (m_iReturnCode == FileCommitUtils.RC_OK_OBJ)
            {
               // initialize list of global variables
               initGlobalLists();
      
               // =========================================================================
               // 1. COPY FILES
               // -------------------------------------------------------------------------
               // If there are files to be copied (from old version to the new one), create
               // 'ready_to_copy' directory the files will be stored temporary in. If this 
               // will pass, we will known there is enough disk storage for new files
               // =========================================================================
               itHelp1 = m_lstCopyFromFiles.iterator();
               itHelp2 = m_lstCopyToFiles.iterator();
               while (itHelp1.hasNext() && itHelp2.hasNext())
               {
                  // get path name and file name of source and destination files
                  sourceElement = itHelp1.next();
                  destinElement = itHelp2.next();
                  
                  // construct source file
                  sbTemp.delete(0, sbTemp.length());
                  // check if there is path name present and if yes, append it
                  if (sourceElement.getFirst() != null)
                  {
                     sbTemp.append(sourceElement.getFirst());
                  }
                  sbTemp.append(sourceElement.getSecond());
                  fSourceFile = new File(sbTemp.toString());
      
                  // check if the source file already exists
                  if (fSourceFile.exists())
                  {
                     // construct destination ready_to_copy full path and than copy 
                     // source file to this place 
                     // also construct the final destination path
                     sbTemp.delete(0, sbTemp.length());
                     sbTemp1.delete(0, sbTemp1.length());
                     // check if there is path name presents and if yes, append it
                     if (destinElement.getFirst() != null)
                     {
                        sbTemp.append(destinElement.getFirst());
                        sbTemp1.append(destinElement.getFirst());
                     }
                     sbTemp.append(FILE_PATH_READY_TO_COPY);
                     sbTemp.append(File.separator);
                     
                     // create destination 'ready_to_copy' directory
                     fPathTemp = new File(sbTemp.toString());
                     
                     try
                     {
                        // check if exists destination 'ready_to_copy' directory
                        if (!fPathTemp.exists()) 
                        {
                           // directory does not exist, so create new one
                           if (!fPathTemp.mkdirs())
                           {
                              // set global ready to copy variable that will be used 
                              // for rollback method
                              m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;
   
                              // directory creation failed                           
                              throw new IOException("Error occurred during creating of '" 
                                       + FILE_PATH_READY_TO_COPY + "' directory.");
                           }
                        }
                     }
                     catch (Throwable thr)
                     {
                        // set global ready to copy variable that will be used for rollback method
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;
   
                        // directory creation failed
                        throw new IOException("Error occurred during creating of '"
                                  + FILE_PATH_READY_TO_COPY + "' directory. ");
                     }
      
                     sbTemp.append(destinElement.getSecond());
                     // create full 'ready_to_copy' destination path 
                     fDestinationFile = new File(sbTemp.toString());
      
                     sbTemp1.append(destinElement.getSecond());
                     // create full final destination path
                     fFinalDestinationFile = new File(sbTemp1.toString());
      
                     // Add 'ready_to_copy' source file to the list.
                     // a.) This list will be used for moving of already copied files to their
                     //     destination directory if everything will be ok.This list stores sources.
                     // b.) This list will be used for deleting of already copied 
                     //     files if something will fail.
                     lstReadyToCopyFilesFrom.add(fDestinationFile);
      
                     // Add 'ready_to_copy' destination file to the list.
                     // This list will be used for moving of already copied files to their
                     // destination directory. This list stores final destinations.
                     lstReadyToCopyFilesTo.add(fFinalDestinationFile);
      
                     // !!! if next operation (FileUtils.moveFile) fail, it is possible we will have
                     // stored corrupted file in the ready_to_copy directory !!!
      
                     try
                     {
                        // copy source file from the repository directory to 
                        // 'ready_to_copy' destination
                        FileUtils.copyFile(fSourceFile, fDestinationFile);
      
                        // set global ready to copy variable that will be used for commit method
                        // commit will copy source files from 'ready_to_copy' directory to their
                        // final destinations
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;
                        m_lstReadyToCopyTo = lstReadyToCopyFilesTo;
                     } 
                     catch (IOException 
                            | OSSException ioExc)
                     {
                        // set global ready to copy variable that will be used for rollback method
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;
   
                        throw new IOException("Error occurred during copying of the file. ");
                     }
                  }
                  else
                  {
                     // set global ready to copy variable that will be used for rollback method
                     m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;
   
                     // source file does not exist
                     throw new IOException("Source file that has to be copied does not exist.");
                  }
               }
   
               // =========================================================================
               // 2. MOVE FILES
               // -------------------------------------------------------------------------
               // If there are files to be moved, create 'ready_to_move' directory the files 
               // will be stored temporary in. If this will pass, we will known there is 
               // enough disk storage for these files.
               // =========================================================================
               itHelp1 = m_lstMoveFromFiles.iterator();
               itHelp2 = m_lstMoveToFiles.iterator();
               while (itHelp1.hasNext() && itHelp2.hasNext())
               {
                  // get path name and file name of source and destination files
                  sourceElement = itHelp1.next();
                  destinElement = itHelp2.next();
                  
                  // construct source file
                  sbTemp.delete(0, sbTemp.length());
                  // check if there is path name present and if yes, append it
                  if (sourceElement.getFirst() != null)
                  {
                     sbTemp.append(sourceElement.getFirst());
                  }
                  sbTemp.append(sourceElement.getSecond());
                  fSourceFile = new File(sbTemp.toString());
   
                  // check if the source file already exists
                  if (fSourceFile.exists())
                  {
                     // construct destination ready_to_move full path and than move 
                     // source file to this place 
                     // also construct the final destination path
                     sbTemp.delete(0, sbTemp.length());
                     sbTemp1.delete(0, sbTemp1.length());
                     // check if there is path name presents and if yes, append it
                     if (destinElement.getFirst() != null)
                     {
                        sbTemp.append(destinElement.getFirst());
                        sbTemp1.append(destinElement.getFirst());
                     }
                     sbTemp.append(FILE_PATH_READY_TO_MOVE);
                     sbTemp.append(File.separator);
                     
                     // create destination 'ready_to_move' directory
                     fPathTemp = new File(sbTemp.toString());
                     
                     try
                     {
                        // check if exists destination 'ready_to_move' directory
                        if (!fPathTemp.exists()) 
                        {
                           // directory does not exist, so create new one
                           if (!fPathTemp.mkdirs())
                           {
                              // set global ready to copy variable that will be used for rollback
                              m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                              // set global ready to copy variable that will be used 
                              // for rollback method
                              m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
   
                              // directory creation failed                           
                              throw new IOException("Error occurred during creating of '" 
                                       + FILE_PATH_READY_TO_MOVE + "' directory.");
                           }
                        }
                     }
                     catch (Throwable thr)
                     {
                        // set global ready to copy variable that will be used for rollback method
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                        // set global ready to move variable that will be used for rollback method
                        m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
   
                        // directory creation failed
                        throw new IOException("Error occurred during creating of '"
                                  + FILE_PATH_READY_TO_MOVE + "' directory. ");
                     }
      
                     sbTemp.append(destinElement.getSecond());
                     // create full 'ready_to_move' destination path 
                     fDestinationFile = new File(sbTemp.toString());
      
                     sbTemp1.append(destinElement.getSecond());
                     // create full final destination path
                     fFinalDestinationFile = new File(sbTemp1.toString());
   
                     // test if there exists file with the same name in the final destination dir.
                     if (fFinalDestinationFile.exists())
                     {
                        // set global ready to copy variable that will be used for rollback method
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                        // set global ready to move variable that will be used for rollback method
                        m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                        m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;
      
                        // destination file with the same name already exist
                        throw new IOException("File that has to be moved already exist in the "
                                                + "destination directory.");
                     }
   
                     // Add 'ready_to_move' source file to the list.
                     // a.) This list will be used for moving of already moved files to their
                     //     destination directory if everything will be ok.This list stores sources.
                     // b.) This list will be used for moving back of already moved
                     //     files if something will fail.
                     lstReadyToMoveFilesFrom.add(fDestinationFile);
      
                     // Add 'ready_to_move' destination file to the list.
                     // This list will be used for moving of already moved files to their
                     // destination directory if everything will be ok.
                     // This list stores final destinations.
                     lstReadyToMoveFilesTo.add(fFinalDestinationFile);
                     
                     // Add 'ready_to_move' destination file to the list.
                     // This list will be used for moving of already moved files to their
                     // original directory if something will fail.
                     // This list stores final destinations.
                     lstReadyToMoveFilesToOriginal.add(fSourceFile);
      
                     // !!! if next operation (FileUtils.moveFile) fail, it is possible we will have
                     // stored corrupted file in the ready_to_move directory !!!
      
                     try
                     {
                        // move source file from the original dir. to 'ready_to_move' destination
                        FileUtils.moveFile(fSourceFile, fDestinationFile);
      
                        // set global ready to copy variable that will be used for commit method
                        // commit will move source files from 'ready_to_move' directory to their
                        // final destinations
                        m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                        m_lstReadyToMoveTo = lstReadyToMoveFilesTo;
                     } 
                     catch (IOException 
                            | OSSException ioExc)
                     {
                        // set global ready to copy variable that will be used for rollback method
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                        // set global ready to move variable that will be used for rollback method
                        m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                        m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;
   
                        throw new IOException("Error occurred during moving of the file. ");
                     }
                  }
                  else
                  {
                     // set global ready to copy variable that will be used for rollback method
                     m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                     // set global ready to move variable that will be used for rollback method
                     m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                     m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;
   
                     // source file does not exist
                     throw new IOException("Source file that has to be moved does not exist.");
                  }
               }
   
               // =========================================================================
               // 3. REPLACE FILES
               // -------------------------------------------------------------------------
               // If there are files to be replaced (from source to destination), create
               // 'ready_to_replace' directory the files will be stored temporary in. If this 
               // will pass, we will known there is enough disk storage for new files
               // =========================================================================
               itHelp1 = m_lstReplaceFromFiles.iterator();
               itHelp2 = m_lstReplaceToFiles.iterator();
               while (itHelp1.hasNext() && itHelp2.hasNext())
               {
                  // get path name and file name of source and destination files
                  sourceElement = itHelp1.next();
                  destinElement = itHelp2.next();
                  
                  // construct source file
                  sbTemp.delete(0, sbTemp.length());
                  // check if there is path name present and if yes, append it
                  if (sourceElement.getFirst() != null)
                  {
                     sbTemp.append(sourceElement.getFirst());
                  }
                  sbTemp.append(sourceElement.getSecond());
                  fSourceFile = new File(sbTemp.toString());
      
                  // construct source file that will be backed up
                  sbTemp.delete(0, sbTemp.length());
                  // check if there is path name present and if yes, append it
                  if (destinElement.getFirst() != null)
                  {
                     sbTemp.append(destinElement.getFirst());
                  }
                  sbTemp.append(destinElement.getSecond());
                  fSourceFileBackUp = new File(sbTemp.toString());
                  
                  // check if the source files exist
                  if (fSourceFile.exists() && fSourceFileBackUp.exists())
                  {
                     // construct destination ready_to_replace full path and than copy 
                     // source file to this place 
                     // also construct destination ready_to_be_replaced full path and than rename 
                     // file that will be replaced to this place (this will be used as backup of
                     // this file if something will fail) 
      
                     sbTemp.delete(0, sbTemp.length());
                     sbTemp1.delete(0, sbTemp1.length());
                     sbTemp2.delete(0, sbTemp2.length());
                     // check if there is path name presents and if yes, append it
                     if (destinElement.getFirst() != null)
                     {
                        sbTemp.append(destinElement.getFirst());
                        sbTemp1.append(destinElement.getFirst());
                        sbTemp2.append(destinElement.getFirst());
                     }
                     sbTemp.append(FILE_PATH_READY_TO_REPLACE);
                     sbTemp.append(File.separator);
                     sbTemp1.append(FILE_PATH_READY_TO_BE_REPLACED);
                     sbTemp1.append(File.separator);
                     
                     // create destination 'ready_to_replace' directory
                     fPathTemp = new File(sbTemp.toString());
                     
                     try
                     {
                        // check if exists destination 'ready_to_replace' directory
                        if (!fPathTemp.exists()) 
                        {
                           // directory does not exist, so create new one
                           if (!fPathTemp.mkdirs())
                           {
                              // set global ready to copy variable that will be used for rollback
                              m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                              // set global ready to move variable that will be used for rollback
                              m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                              m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;

                              // set global ready to replace and ready to be replaced variables 
                              // that will be used for rollback method
                              m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                              m_lstReadyToReplaceTo = lstReadyToReplaceFilesTo;
                              m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                              m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;
   
                              // directory creation failed
                              throw new IOException(
                                    "Error occurred during creating of '" 
                                    + FILE_PATH_READY_TO_REPLACE + "' directory.");
                           }
                        }
                     }
                     catch (Throwable thr)
                     {
                        // set global ready to copy variable that will be used for rollback method
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                        // set global ready to move variable that will be used for rollback method
                        m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                        m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;

                        // set global ready to replace and ready to be replaced variables that 
                        // will be used for rollback method
                        m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                        m_lstReadyToReplaceTo = lstReadyToReplaceFilesTo;
                        m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                        m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;
                        throw new IOException("Error occurred during creating of '" 
                                              + FILE_PATH_READY_TO_REPLACE 
                                              + "' directory.", thr);
                     }
      
                     // create destination 'ready_to_be_replaced' directory
                     fPathTemp = new File(sbTemp1.toString());
                     
                     try
                     {
                        // check if exists destination 'ready_to_be_replaced' directory
                        if (!fPathTemp.exists()) 
                        {
                           // directory does not exist, so create new one
                           if (!fPathTemp.mkdirs())
                           {
                              // set global ready to copy variable that will be used for rollback
                              m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                              // set global ready to move variable that will be used for rollback
                              m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                              m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;

                              // set global ready to replace and ready to be replaced variables 
                              // that will be used for rollback method
                              m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                              m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                              m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;
   
                              // directory creation failed
                              throw new IOException(
                                    "Error occurred during creating of '" 
                                    + FILE_PATH_READY_TO_BE_REPLACED + "' directory.");
                           }
                        }
                     }
                     catch (Throwable thr)
                     {
                        // set global ready to copy variable that will be used for rollback method
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                        // set global ready to move variable that will be used for rollback method
                        m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                        m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;

                        // set global ready to replace and ready to be replaced variables that 
                        // will be used for rollback method
                        m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                        m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                        m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;
                        throw new IOException("Error occurred during creating of '" 
                                              + FILE_PATH_READY_TO_BE_REPLACED 
                                              + "' directory.", thr);
                     }
      
                     sbTemp.append(sourceElement.getSecond());
                     sbTemp1.append(destinElement.getSecond());
                     sbTemp2.append(sourceElement.getSecond());
      
                     // create full 'ready_to_replace' destination path 
                     fDestinationFile = new File(sbTemp.toString());
      
                     // create final destination path of the replace file 
                     fFinalDestinationFile = new File(sbTemp2.toString());
      
                     // create full 'ready_to_be_replaced' destination path 
                     fDestinationFileBackUp = new File(sbTemp1.toString());
      
                     // Add 'ready_to_replace' source file to the list.
                     // a.) This list will be used for renaming already renamed files to their
                     //     destination directory if everything will be ok.This list stores sources.
                     // b.) This list will be used for deleting of already renamed 
                     //     files if something will fail.
                     lstReadyToReplaceFilesFrom.add(fDestinationFile);
      
                     // Add 'ready_to_replace' destination file to the list.
                     // This list will be used for renaming already renamed files to their
                     // destination directory if everything will be ok. 
                     // This list stores destinations.
                     lstReadyToReplaceFilesTo.add(fFinalDestinationFile);
      
                     // !!! if next operation (FileUtils.moveFile) fail, it is possible we will have
                     // stored corrupted file in the ready_to_replace directory !!!
      
                     try
                     {
                        // move source file from the repository directory 
                        // to 'ready_to_replace' destination
                        FileUtils.moveFile(fSourceFile, fDestinationFile);
                     } 
                     catch (IOException 
                            | OSSException ioExc)
                     {
                        // set global ready to copy variable that will be used for rollback method
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                        // set global ready to move variable that will be used for rollback method
                        m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                        m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;

                        // set global ready to replace and ready to be replaced variables that 
                        // will be used for rollback method
                        m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                        m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                        m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;
                        throw new IOException("Error occurred during copying of"
                                              + " the file.", ioExc);
                     }
      
                     try
                     {
                        // rename file that will be replaced into the 'ready_to_be_replaced' dir.
                        // we know that we are at the same filesystem and we can use renameTo method
                        if (fSourceFileBackUp.renameTo(fDestinationFileBackUp))
                        {
                           // If renameTo will pass, there will be added just renamed file to the 
                           // list of TO BE RENAMED files that will be used for rollback method if
                           // it will fail later. If it will fail now, it is not necessary to add 
                           // this file to the particular list, bacause there will be not file 
                           // renamed and it will stay on the same place.
                           
                           // Add 'ready_to_be_replaced' file to the list.
                           // a.) This list will be used for deleting of files 
                           //     if everything will be ok.
                           // b.) This list will be used for undo mode 
                           //     (rollback method) - renaming back 
                           // files if something will fail. In this list will be stored sources 
                           // for undo mode.
                           lstReadyToBeReplacedFilesFrom.add(fDestinationFileBackUp);
            
                           // Add 'ready_to_be_replaced' file to the list. This list will be used 
                           // for undo mode (rollback method) - renaming back files if something 
                           // will fail. In this list will be stored destinations for undo mode.
                           lstReadyToBeReplacedFilesTo.add(fSourceFileBackUp);
      
                           // set global ready to replace and ready to be replaced variables that 
                           // will be used for commit method
                           m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                           m_lstReadyToReplaceTo = lstReadyToReplaceFilesTo;
                           m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                           m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;
                        }
                        else
                        {
                           // set global ready to copy variable that will be used for rollback
                           m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                           // set global ready to move variable that will be used for rollback
                           m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                           m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;

                           // set global ready to replace and ready to be replaced variables that 
                           // will be used for rollback method
                           m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                           m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                           m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;
   
                           throw new IOException("Error occurred during file renaming.");
                        }
                     }
                     catch (Throwable thr)
                     {
                        // set global ready to copy variable that will be used for rollback method
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                        // set global ready to move variable that will be used for rollback method
                        m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                        m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;

                        // set global ready to replace and ready to be replaced variables that 
                        // will be used for rollback method
                        m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                        m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                        m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;
                        throw new IOException("Error occurred during file renaming.", 
                                              thr);                        
                     }
                  }
                  else
                  {
                     // set global ready to copy variable that will be used for rollback method
                     m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                     // set global ready to move variable that will be used for rollback method
                     m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                     m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;

                     // set global ready to replace and ready to be replaced variables that 
                     // will be used for rollback method
                     m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                     m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                     m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;
   
                     // source file does not exist
                     throw new IOException("Source file that has to be replaced does not exist.");
                  }
               }
      
               // =========================================================================
               // 4. DELETE FILES
               // -------------------------------------------------------------------------
               // If there are files to be deleted , create 'ready_to_delete' directory 
               // the files will be stored temporary in. If this will pass, we can delete
               // these files from 'ready_to_delete' directory.
               // =========================================================================
               itHelp1 = m_lstDeleteFiles.iterator();
               while (itHelp1.hasNext())
               {
                  // get path name and file name of source and destination files
                  sourceElement = itHelp1.next();
                  
                  // construct source file
                  sbTemp.delete(0, sbTemp.length());
                  // check if there is path name present and if yes, append it
                  if (sourceElement.getFirst() != null)
                  {
                     sbTemp.append(sourceElement.getFirst());
                  }
                  sbTemp.append(sourceElement.getSecond());
                  fSourceFile = new File(sbTemp.toString());
      
                  // check if the source file already exists
                  if (fSourceFile.exists())
                  {
                     // construct destination ready_to_delete full path and than rename 
                     // source file to this place. We can use renameTo method because we 
                     // are at the same file system.
      
                     sbTemp.delete(0, sbTemp.length());
                     // check if there is path name presents and if yes, append it
                     if (sourceElement.getFirst() != null)
                     {
                        sbTemp.append(sourceElement.getFirst());
                     }
                     sbTemp.append(FILE_PATH_READY_TO_DELETE);
                     sbTemp.append(File.separator);
                     
                     // create destination 'ready_to_delete' directory
                     fPathTemp = new File(sbTemp.toString());
                     // check if exists destination 'ready_to_delete' directory
                     if (!fPathTemp.exists()) 
                     {
                        try
                        {
                           // directory does not exist, so create new one
                           if (!fPathTemp.mkdirs())
                           {
                              // set global ready to copy variable that will be used for rollback
                              m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                              // set global ready to move variable that will be used for rollback
                              m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                              m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;
         
                              // set global ready to replace and ready to be replaced variables that
                              // will be used for rollback method
                              m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                              m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                              m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;

                              // set global ready to delete variables that will be used 
                              // for rollback method
                              m_lstReadyToDeleteFrom = lstReadyToDeleteFilesFrom;
                              m_lstReadyToDeleteTo = lstReadyToDeleteFilesTo;
   
                              // directory creation failed
                              throw new IOException(
                                    "Error occurred during creating of '" 
                                    + FILE_PATH_READY_TO_DELETE + "' directory.");
                           }
                        }
                        catch (Throwable thr)
                        {
                           // set global ready to copy variable that will be used for rollback
                           m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                           // set global ready to move variable that will be used for rollback
                           m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                           m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;
      
                           // set global ready to replace and ready to be replaced variables that 
                           // will be used for rollback method
                           m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                           m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                           m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;

                           // set global ready to delete variables that will be used 
                           // for rollback method
                           m_lstReadyToDeleteFrom = lstReadyToDeleteFilesFrom;
                           m_lstReadyToDeleteTo = lstReadyToDeleteFilesTo;
                           throw new IOException("Error occurred during creating of '" 
                                                 + FILE_PATH_READY_TO_DELETE 
                                                 + "' directory.", thr);                           
                        }
                     }
   
                     sbTemp.append(sourceElement.getSecond());
      
                     // create full 'ready_to_delete' destination path 
                     fSourceFileBackUp = new File(sbTemp.toString());
      
                     try
                     {
                        // rename file that will be replaced into the 'ready_to_delete' directory
                        // we know that we are at the same filesystem and we can use renameTo method
                        if (fSourceFile.renameTo(fSourceFileBackUp))
                        {
                           // Add 'ready_to_delete' source file to the list.
                           // a.) This list of source files will be used for deleting of files if
                           //     everything will be ok.
                           // b.) This list of source files will be used to rollback method - undo 
                           //     delete mode if something will fail.
                           lstReadyToDeleteFilesFrom.add(fSourceFileBackUp);
            
                           // Add 'ready_to_delete' destination file to the list. 
                           // This list of destination files will be used to rollback method - undo 
                           // delete mode if something will fail.
                           lstReadyToDeleteFilesTo.add(fSourceFile);
      
                           // set global ready to delete variables that will be used for commit 
                           // method
                           m_lstReadyToDeleteFrom = lstReadyToDeleteFilesFrom;
                        }
                        else
                        {
                           // set global ready to copy variable that will be used for rollback
                           m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                           // set global ready to move variable that will be used for rollback
                           m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                           m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;
      
                           // set global ready to replace and ready to be replaced variables that 
                           // will be used for rollback method
                           m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                           m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                           m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;

                           // set global ready to delete variables that will be used 
                           // for rollback method
                           m_lstReadyToDeleteFrom = lstReadyToDeleteFilesFrom;
                           m_lstReadyToDeleteTo = lstReadyToDeleteFilesTo;
   
                           throw new IOException("Error occurred during file renaming.");
                        }
                     }
                     catch (Throwable thr)
                     {
                        // set global ready to copy variable that will be used for rollback method
                        m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                        // set global ready to move variable that will be used for rollback method
                        m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                        m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;
   
                        // set global ready to replace and ready to be replaced variables that 
                        // will be used for rollback method
                        m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                        m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                        m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;

                        // set global ready to delete variables that will be used for rollback 
                        // method
                        m_lstReadyToDeleteFrom = lstReadyToDeleteFilesFrom;
                        m_lstReadyToDeleteTo = lstReadyToDeleteFilesTo;
                        throw new IOException("Error occurred during file renaming.", 
                                              thr);                           
                     }
                  }
                  else
                  {
                     // if file does not exist nothing will happend and delete process
                     // will continue. There will be not thrown exception because delete
                     // process should be successful also in case if files don't exist.
/*
                     // set global ready to copy variable that will be used for rollback method
                     m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;

                     // set global ready to move variable that will be used for rollback method
                     m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
                     m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;

                     // set global ready to replace and ready to be replaced variables that 
                     // will be used for rollback method
                     m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
                     m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
                     m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;

                     // set global ready to delete variables that will be used for rollback method
                     m_lstReadyToDeleteFrom = lstReadyToDeleteFilesFrom;
                     m_lstReadyToDeleteTo = lstReadyToDeleteFilesTo;
   
                     // source file does not exist
                     throw new IOException("Source file that has to be deleted does not exist.");
*/
                  }
               }
            }
            else
            {
               // there was send error code here so get error message
               if (m_iReturnCode == FileCommitUtils.RC_FILE_ERR_OBJ)
               {
                  throw new IOException("There was an error occurred during image processing.");
               }
            }
         }
         catch (IOException ioExc)
         {
            // throw internal IOException (this was already occurred by our internal try/catch)
            throw ioExc;
         }
         catch (Throwable thrExc)
         {
            // There was occurred another exception as our internal IOException. We have to
            // set up lists of already processed files that will be send to the rollback method.
   
            // set global ready to copy variable that will be used for rollback method
            m_lstReadyToCopyFrom = lstReadyToCopyFilesFrom;
   
            // set global ready to copy variable that will be used for commit method
            // commit will move source files from 'ready_to_move' directory to their
            // final destinations
            m_lstReadyToMoveFrom = lstReadyToMoveFilesFrom;
            m_lstReadyToMoveTo = lstReadyToMoveFilesToOriginal;
   
            // set global ready to replace and ready to be replaced variables that 
            // will be used for rollback method
            m_lstReadyToReplaceFrom = lstReadyToReplaceFilesFrom;
            m_lstReadyToReplaceTo = lstReadyToReplaceFilesTo;
            m_lstReadyToBeReplacedFrom = lstReadyToBeReplacedFilesFrom;
            m_lstReadyToBeReplacedTo = lstReadyToBeReplacedFilesTo;
   
            // set global ready to delete variables that will be used for rollback method
            m_lstReadyToDeleteFrom = lstReadyToDeleteFilesFrom;
            m_lstReadyToDeleteTo = lstReadyToDeleteFilesTo;
            throw new IOException("Unspecified error has occurred while preparing"
                                  + " files to commit.", thrExc);                                       
         }
      }
   }

   /**
    * Method will commit files. It will renameTo files stored in the ready_to_XXX 
    * directory into the final destination. If something fails during this operation, 
    * it will repeat iRetryCounter times (after a sleep). 
    * 
    * @throws OSSException - error occurred during files commiting
    */
   public void commit(
   ) throws OSSException
   {
      int iRetryCount = getRetryCount();
      
      // don't process this method when ReturnCode is null 
      if (m_iReturnCode != null)
      {
         int iRetryCounter = 0;
   
         if (m_iStatus == FileCommitUtils.STATUS_PREPARE_TO_COMMIT)
         {
            m_iStatus = FileCommitUtils.STATUS_COMMIT;
         }
         else
         {
            throw new OSSInternalErrorException("Cannot commit file transaction while " +
                                               "there is not prepare to commit status defined.");
         }
   
         // Process commit method while there are any not processed requested 
         // files but repeat this process max. (iRetryCounter + 1 (for the first 
         // time)) times, thats why we use <=
         while (((m_lstReadyToCopyFrom.size() > 0) 
                 || (m_lstReadyToMoveFrom.size() > 0)
                 || (m_lstReadyToReplaceFrom.size() > 0)
                 || (m_lstReadyToBeReplacedFrom.size() > 0) 
                 || (m_lstReadyToDeleteFrom.size() > 0)) 
                && (iRetryCounter <= iRetryCount))
         {
            if (iRetryCounter > 0)
            {
               // There was an error occurred. Wait for a few seconds and try again
               sleep();
            }
            
            try
            {
               if (GlobalConstants.ERROR_CHECKING)
               {
                  assert m_lstReadyToCopyFrom.size() == m_lstReadyToCopyTo.size() 
                           : "Size of the source and destination list of files that " + 
                             "will be copied has to be the same.";
                  assert m_lstReadyToMoveFrom.size() == m_lstReadyToMoveTo.size() 
                           : "Size of the source and destination list of files that " + 
                             "will be moved has to be the same.";
                  assert m_lstReadyToReplaceFrom.size() == m_lstReadyToReplaceTo.size() 
                           : "Size of the source and destination list of files that " +
                             "will be replaced has to be the same.";
                  assert m_lstReadyToBeReplacedFrom.size() 
                              == m_lstReadyToBeReplacedTo.size()
                           : "Size of the source and destination list of files that " + 
                             "will be replaced has to be the same.";
               }
         
               // At this point we known that we heave prepared files for final commiting. We are
               // in the same file system so we can use renameTo method for all files.
         
               // =========================================================================
               // 1. COPY FILES (rename files that have to be copied)
               // -------------------------------------------------------------------------
               // If there are prepared files to be copied (from old version to the new one), 
               // rename them from the 'ready_to_copy' directory to their final destination. If 
               // this will pass, list m_lstReadyToCopyFrom will be empty. If this will fail, 
               // list m_lstReadyToCopyFrom will be not empty and whole process will repeat
               // later (after sleeping for a time).
               // =========================================================================
               doRenameFiles(m_lstReadyToCopyFrom,
                             m_lstReadyToCopyTo,
                             SENDER_COMMIT,
                             LIST_IDENTIFIER_COPY);
   
               // =========================================================================
               // 2. MOVE FILES (rename files that have to be moved)
               // -------------------------------------------------------------------------
               // If there are prepared files to be moved, rename them from the 'ready_to_move'
               // directory to their final destination. If this will pass, list 
               // m_lstReadyToMoveFrom will be empty. If this will fail, list 
               // m_lstReadyToMoveFrom will be not empty and whole process will repeat
               // later (after sleeping for a time).
               // =========================================================================
               doRenameFiles(m_lstReadyToMoveFrom,
                             m_lstReadyToMoveTo,
                             SENDER_COMMIT,
                             LIST_IDENTIFIER_MOVE);
         
               // =========================================================================
               // 3. REPLACE FILES (replace files that have to be replaced)
               // -------------------------------------------------------------------------
               // If there are prepared files to be replaced, rename them from 'ready_to_replace' 
               // directory to their final destination. If this will pass, list 
               // m_lstReadyToReplaceFrom will be empty. If this will fail, list 
               // m_lstReadyToReplaceFrom will be not empty and whole process will repeat
               // later (after sleeping for a time).
               // =========================================================================
               doRenameFiles(m_lstReadyToReplaceFrom,
                             m_lstReadyToReplaceTo,
                             SENDER_COMMIT,
                             LIST_IDENTIFIER_REPLACE);
   
               // =========================================================================
               // 4. DELETE FILES
               // -------------------------------------------------------------------------
               // Delete files from 'ready_to_delete' and from 'ready_to_be_replaced' 
               // directories. If this will pass, lists m_lstReadyToDeleteFrom and 
               // m_lstReadyToBeReplacedFrom will be empty. If this will fail, one or both 
               // specified list will be not empty and whole process will repeat later 
               // (after sleeping for a time).
               // =========================================================================
               doDeleteFiles(m_lstReadyToBeReplacedFrom,
                             SENDER_COMMIT,
                             LIST_IDENTIFIER_TO_BE_REPLACED);
   
               doDeleteFiles(m_lstReadyToDeleteFrom,
                             SENDER_COMMIT,
                             LIST_IDENTIFIER_DELETE);

            }
            catch (Throwable thrExc)
            {
               s_logger.log(Level.WARNING, 
                            "An error has occurred during file commit", thrExc);
            }
            finally
            {
               iRetryCounter++;
            }
         }
      }
   }

   /**
    * Method to rollback files. It will rename back already collocated files.
    * (renameTo) files stored in the ready_to_XXX directory into the final 
    * destination. If something fails during this operation, it will be 
    * continue and there will be memoried files that failed. RenameTo process 
    * for failed files will repeat later (after a sleep). 
    * 
    * @throws OSSException - error occurred during files commiting
    */
   public void rollback(
   ) throws OSSException
   {
      int iRetryCount = getRetryCount();
      
      // don't process this method when ReturnCode is null 
      if (m_iReturnCode != null)
      {
         int iRetryCounter = 0;
   
         if (m_iStatus == FileCommitUtils.STATUS_PREPARE_TO_COMMIT)
         {
            m_iStatus = FileCommitUtils.STATUS_ROLLBACK;
         }
         else
         {
            throw new OSSInternalErrorException(
                         "Cannot prepare file transaction to rollback while " 
                         + "there is not prepare to commit status defined.");
         }
   
         // Process rollback method while there are any not processed requested 
         // files but repeat this process max. (iRetryCounter + 1 (for the first 
         // time)) times, thats why we use <=
         while (((m_lstReadyToBeReplacedFrom.size() > 0)
                 || (m_lstReadyToMoveFrom.size() > 0)
                 || (m_lstReadyToDeleteFrom.size() > 0)
                 || (m_lstReadyToCopyFrom.size() > 0) 
                 || (m_lstReadyToReplaceFrom.size() > 0))
                && (iRetryCounter <= iRetryCount))
         {
            if (iRetryCounter > 0)
            {
               // There was an error occurred. Wait for a few seconds and try again
               sleep();
            }
            
            try
            {
               // At this point we known that preparing of files generated exception. 
               // Files already copied or renamed should be deleted. There was no file 
               // renamed to the final destination directory, so we have to delete just 
               // files already renamed to the 'ready_to_XXX' subdirectory. We don't 
               // care about files that are still in the repository (temp) directory.
         
               // =========================================================================
               // 1. ROLLBACK READY TO BE REPLACED FILES 
               // =========================================================================
               // rename files back from the 'ready_to_be_replaced' to the original destination
               doRenameFiles(m_lstReadyToBeReplacedFrom,
                             m_lstReadyToBeReplacedTo,
                             SENDER_ROLLBACK,
                             LIST_IDENTIFIER_TO_BE_REPLACED);
         
               // =========================================================================
               // 2. ROLLBACK READY TO DELETE FILES 
               // =========================================================================
               // rename files back from the 'ready_to_delete' to the original destination
               doRenameFiles(m_lstReadyToDeleteFrom,
                             m_lstReadyToDeleteTo,
                             SENDER_ROLLBACK,
                             LIST_IDENTIFIER_DELETE);
         
               // =========================================================================
               // 3. ROLLBACK READY TO MOVE FILES 
               // =========================================================================
               if (m_bMovedFilesDeleteForRollback)
               {
                  // if there is not important to move files from 'ready_to_move' directory
                  // to the destination one, just delete moved files (for example there is not
                  // important to move back files from 'ready_to_move' directory to the temporary
                  // directory).
                  doDeleteFiles(m_lstReadyToMoveFrom,
                                SENDER_ROLLBACK,
                                LIST_IDENTIFIER_MOVE);
               }
               else
               {
                  // move files back from the 'ready_to_move' to the original destination
                  // We have to move these since they might be at different file system
                  doMoveFiles(m_lstReadyToMoveFrom,
                              m_lstReadyToMoveTo,
                              SENDER_ROLLBACK,
                              LIST_IDENTIFIER_MOVE);
               }
   
               // =========================================================================
               // 4. ROLLBACK READY TO COPY FILES 
               // =========================================================================
               // delete files from the 'ready_to_copy' directory
               doDeleteFiles(m_lstReadyToCopyFrom,
                             SENDER_ROLLBACK,
                             LIST_IDENTIFIER_COPY);
   
               // =========================================================================
               // 5. ROLLBACK READY TO REPLACE FILES 
               // =========================================================================
               // delete files from the 'ready_to_replace' directory
               doDeleteFiles(m_lstReadyToReplaceFrom,
                             SENDER_ROLLBACK,
                             LIST_IDENTIFIER_REPLACE);
            }
            catch (Throwable thrExc)
            {
               s_logger.log(Level.WARNING, 
                                   "An error has occurred during file rollback", thrExc);
            }
            iRetryCounter++;
         }
      }
   }

   /**
    * Current status of the file commit (prepare commit, commit or rollback). 
    * Status will be used for checking if there will be not called prepare
    * to commit, commit or rollback more than once one after another.
    * 
    * @return int - one of the STATUS_XXX values
    */
   public int getStatus()
   {
      return m_iStatus;
   }

   /**
    * Get setting telling us how many times to retry file commit or rollback 
    * operations in case or error before we give up.
    * 
    * @return int - non negative number, since 0 means we try once and then   
    *               to not retry again
    */
   public int getRetryCount()
   {
      // Read configuration parameters
      Properties prpSettings;
      int        iRetryCount;

      prpSettings = Config.getInstance().getProperties();
      iRetryCount = PropertyUtils.getIntPropertyInRange(
                                     prpSettings, FILECOMMIT_RETRY_COUNT, 
                                     FILECOMMIT_RETRY_COUNT_DEFAULT, 
                                     "File commit retry count value", 
                                     0, // 0 is allowed since we try once 
                                        // regardless
                                     Integer.MAX_VALUE);
      
      return iRetryCount;
   }
   
   // Helper methods ////////////////////////////////////////////////

   /**
    * Method that will rename source files to destination ones. This uses 
    * renameTo() method - we are sure that the source and destination files
    * will be on the same file system.
    * 
    * @param lstSources - list of source files
    * @param lstDestinations - list of destination files
    * @param iSenderIdentificator - identifier of the sender - commit/rollback.
    * @param iListIdentificator - identifier of the list. By this identifier we can decide 
    *                             deleting processed item from the particular global list.
    */
   private void doRenameFiles(
      List lstSources,
      List lstDestinations,
      int iSenderIdentificator,
      int iListIdentificator
   )
   {
      File fSourceFile;
      File fDestinationFile;
      
      // create copy of the source and destination list
      List lstSourcesCopy = new ArrayList(lstSources);
      List lstDestinationsCopy = new ArrayList(lstDestinations);

      Iterator itHelp1;
      Iterator itHelp2;
      
      int iErrorIndexPosition = 0;

      if (GlobalConstants.ERROR_CHECKING)
      {
         assert lstSourcesCopy.size() == lstDestinationsCopy.size() 
                  : "Size of the source and destination list of files that " + 
                    "will be renamed has to be the same.";
      }

      if (lstSourcesCopy.size() > 0)
      {
         
         itHelp1 = lstSourcesCopy.iterator();
         itHelp2 = lstDestinationsCopy.iterator();
         while (itHelp1.hasNext() && itHelp2.hasNext())
         {
            // get source and destination file to be renamed back
            fSourceFile = (File) itHelp1.next();
            fDestinationFile = (File) itHelp2.next();
   
            if (fSourceFile.exists())
            {
               if (fDestinationFile.exists())
               {
                  // Since the destination file already exists (e.g. it couldn't
                  // be removed because of permissions), remove the source
                  // This should never happen during commit (maybe we should check
                  // that this is really rollback) since for commit the flag
                  // should be replace and then the file to be replaced is moved
                  // away. If this is rollback then the destination (which is the
                  // real original) may be there and in that case we want to remove
                  // the copy                  
                  s_logger.log(Level.FINEST, "Destination file {0} already exists,"
                               + " removing the source file {1}", 
                               new Object[]{fDestinationFile.getAbsolutePath(), 
                                            fSourceFile.getAbsolutePath()});
                  if (!fSourceFile.delete())
                  {
                     // set position of the file in the list the error was occurred on
                     iErrorIndexPosition++;
                     // log this failure
                     s_logger.log(Level.WARNING, "Cannot delete file {0} since"
                                  + " the destination file already exists.", 
                                  fSourceFile.getAbsolutePath());
                  }
                  else
                  {
                     // file was successfully deleted, so remove it from the global lists
                     removeDeletedFileFromGlobalLists(iSenderIdentificator,
                                                      iListIdentificator,
                                                      iErrorIndexPosition);
                  }
               }
               else
               {
                  try
                  {
                     if (!fSourceFile.renameTo(fDestinationFile))
                     {
                        // set position of the file in the list the error was occurred on
                        iErrorIndexPosition++;
                        // log this failure
                        s_logger.log(Level.WARNING, 
                                     "Cannot rename file from {0} to {1}", 
                                     new Object[]{fSourceFile.getAbsolutePath(), 
                                                  fDestinationFile.getAbsolutePath()});
                     }
                     else
                     {
                        // file was successfully renamed, so remove it from the global lists
                        removeRenamedFileFromGlobalLists(iSenderIdentificator, 
                                                         iListIdentificator,
                                                         iErrorIndexPosition);
                     }
                  }
                  catch (Throwable thr)
                  {
                     // set position of the file in the list the error was occurred on
                     iErrorIndexPosition++;
                     // log this failure
                     s_logger.log(Level.WARNING, 
                                  "Cannot rename file from " + fSourceFile.getAbsolutePath() 
                                  + " to " + fDestinationFile.getAbsolutePath(), thr);
                  }
               }
            }
            else
            {
               // Source file does not exist. This case should never became, because we have 
               // already prepared files in private ready_to_XXX directory. Now we cannot 
               // do anything because we are already doing commit or rollback. We shold just
               // remove this file from the global lists.
               removeRenamedFileFromGlobalLists(iSenderIdentificator, 
                                                iListIdentificator,
                                                iErrorIndexPosition);
            }
         }
      }
   }

   /**
    * Method that will move source files to destination ones. This uses 
    * move() method since we can be going across file systems. For example during
    * rollback we are returning files back to their origin which can be at 
    * different file system.
    * 
    * @param lstSources - list of source files
    * @param lstDestinations - list of destination files
    * @param iSenderIdentificator - identifier of the sender - commit/rollback.
    * @param iListIdentificator - identifier of the list. By this identifier we can decide 
    *                             deleting processed item from the particular global list.
    */
   private void doMoveFiles(
      List lstSources,
      List lstDestinations,
      int iSenderIdentificator,
      int iListIdentificator
   )
   {
      File fSourceFile;
      File fDestinationFile;
      
      // create copy of the source and destination list
      List lstSourcesCopy = new ArrayList(lstSources);
      List lstDestinationsCopy = new ArrayList(lstDestinations);

      Iterator itHelp1;
      Iterator itHelp2;
      
      int iErrorIndexPosition = 0;

      if (GlobalConstants.ERROR_CHECKING)
      {
         assert lstSourcesCopy.size() == lstDestinationsCopy.size() 
                  : "Size of the source and destination list of files that " + 
                    "will be moved has to be the same.";
      }

      if (lstSourcesCopy.size() > 0)
      {
         
         itHelp1 = lstSourcesCopy.iterator();
         itHelp2 = lstDestinationsCopy.iterator();
         while (itHelp1.hasNext() && itHelp2.hasNext())
         {
            // get source and destination file to be move back
            fSourceFile = (File) itHelp1.next();
            fDestinationFile = (File) itHelp2.next();
   
            if (fSourceFile.exists())
            {
               if (fDestinationFile.exists())
               {
                  // Since the destination file already exists (e.g. it couldn't
                  // be removed because of permissions), remove the source
                  // This should never happen during commit (maybe we should check
                  // that this is really rollback) since for commit the flag
                  // should be replace and then the file to be replaced is moved
                  // away. If this is rollback then the destination (which is the
                  // real original) may be there and in that case we want to remove
                  // the copy                  
                  s_logger.log(Level.FINEST, "Destination file {0} already exists,"
                               + " removing the source file {1}", 
                               new Object[]{fDestinationFile.getAbsolutePath(), 
                                            fSourceFile.getAbsolutePath()});
                  if (!fSourceFile.delete())
                  {
                     // set position of the file in the list the error was occurred on
                     iErrorIndexPosition++;
                     // log this failure
                     s_logger.log(Level.WARNING, "Cannot delete file {0} since"
                                  + " the destination file already exists.", 
                                  fSourceFile.getAbsolutePath());
                  }
                  else
                  {
                     // file was successfully deleted, so remove it from the global lists
                     removeDeletedFileFromGlobalLists(iSenderIdentificator,
                                                      iListIdentificator,
                                                      iErrorIndexPosition);
                  }
               }
               else
               {
                  try
                  {
                     FileUtils.moveFile(fSourceFile, fDestinationFile);
                     // file was successfully moved, so remove it from the global lists
                     removeRenamedFileFromGlobalLists(iSenderIdentificator, 
                                                      iListIdentificator,
                                                      iErrorIndexPosition);
                  }
                  catch (Throwable thr)
                  {
                     // set position of the file in the list the error was occurred on
                     iErrorIndexPosition++;
                     // log this failure
                     s_logger.log(Level.WARNING, 
                                  "Cannot move file from " + fSourceFile.getAbsolutePath() 
                                  + " to " + fDestinationFile.getAbsolutePath(), thr);
                  }
               }
            }
            else
            {
               // Source file does not exist. This case should never became, because we have 
               // already prepared files in private ready_to_XXX directory. Now we cannot 
               // do anything because we are already doing commit or rollback. We shold just
               // remove this file from the global lists.
               removeRenamedFileFromGlobalLists(iSenderIdentificator, 
                                                iListIdentificator,
                                                iErrorIndexPosition);
            }
         }
      }
   }

   /**
    * Method for removing just renamed file from the global lstReadyToXXX list.
    * 
    * @param iSenderIdentificator - identifier of the sender - commit/rollback.
    * @param iListIdentificator - identifier of the list. By this identifier we can decide 
    *                             deleting processed item from the particular global list.
    * @param iIndexPosition - actual position in the particular global list. Items from the list
    *                         are removed from the start of this list. If error has occurred, index
    *                         position is increased and this item will be not deleted from the list
    *                         but whole process will repeat.
    */
   private void removeRenamedFileFromGlobalLists(
      int iSenderIdentificator,
      int iListIdentificator,
      int iIndexPosition
   )
   {
      switch (iSenderIdentificator)
      {
         // if the sender is commit() method
         case SENDER_COMMIT:
         {
            switch (iListIdentificator)
            {
               case LIST_IDENTIFIER_COPY:
               {
                  // remove processed item from ready to copy list
                  m_lstReadyToCopyFrom.remove(iIndexPosition);
                  m_lstReadyToCopyTo.remove(iIndexPosition);
                  break;
               }
               case LIST_IDENTIFIER_MOVE:
               {
                  m_lstReadyToMoveFrom.remove(iIndexPosition);
                  m_lstReadyToMoveTo.remove(iIndexPosition);
                  break;
               }
               case LIST_IDENTIFIER_REPLACE:
               {
                  // remove processed item from ready to replace list
                  m_lstReadyToReplaceFrom.remove(iIndexPosition);
                  m_lstReadyToReplaceTo.remove(iIndexPosition);
                  break;
               }
               default:
               {
                  assert false : "List identifier is incorrectly specified.";
               }
            }
            break;
         }
         // if the sender is rollback() method
         case SENDER_ROLLBACK:
         {
            switch (iListIdentificator)
            {
               case LIST_IDENTIFIER_TO_BE_REPLACED:
               {
                  // remove processed item from ready to be replaced list
                  m_lstReadyToBeReplacedFrom.remove(iIndexPosition);
                  m_lstReadyToBeReplacedTo.remove(iIndexPosition);
                  break;
               }
               case LIST_IDENTIFIER_MOVE:
               {
                  m_lstReadyToMoveFrom.remove(iIndexPosition);
                  m_lstReadyToMoveTo.remove(iIndexPosition);
                  break;
               }
               case LIST_IDENTIFIER_DELETE:
               {
                  // remove processed item from ready to delete list
                  m_lstReadyToDeleteFrom.remove(iIndexPosition);
                  m_lstReadyToDeleteTo.remove(iIndexPosition);
                  break;
               }
               default:
               {
                  assert false : "List identifier is incorrectly specified.";
               }
            }
            break;
         }
         default:
         {
            assert false : "Sender is incorrectly specified.";
         }
      }
   }

   /**
    * Method that will delete source files in the list.
    * 
    * @param lstSources - list of source files
    * @param iSenderIdentificator - identifier of the sender - commit/rollback.
    * @param iListIdentificator - identifier of the list. By this identifier we can decide 
    *                             deleting processed item from the particular global list.
    */
   private void doDeleteFiles(
      List lstSources,
      int iSenderIdentificator,
      int iListIdentificator
   )
   {
      File fSourceFile;

      // create copy of the source and destination list
      List lstSourcesCopy = new ArrayList(lstSources);

      Iterator itHelp1;
      int iErrorIndexPosition = 0;

      if (lstSourcesCopy.size() > 0)
      {
         itHelp1 = lstSourcesCopy.iterator();
         while (itHelp1.hasNext())
         {
            // get source file to be deleted
            fSourceFile = (File) itHelp1.next();
   
            if (fSourceFile.exists())
            {
               try
               {
                  if (!fSourceFile.delete())
                  {
                     // set position of the file in the list the error was occurred on
                     iErrorIndexPosition++;
                     // log this failure
                     s_logger.log(Level.WARNING, "Cannot delete file {0}", 
                                  fSourceFile.getAbsolutePath());
                  }
                  else
                  {
                     // file was successfully deleted, so remove it from the global lists
                     removeDeletedFileFromGlobalLists(iSenderIdentificator,
                                                      iListIdentificator,
                                                      iErrorIndexPosition);
                  }
               }
               catch (Throwable thr)
               {
                  // set position of the file in the list the error was occurred on
                  iErrorIndexPosition++;
                  // log this failure
                  s_logger.log(Level.WARNING, 
                                      "Cannot delete file " + fSourceFile.getAbsolutePath(), thr);
               }
            }
            else
            {
               // Source file does not exist. This case should never became, because we have 
               // already prepared files in private ready_to_XXX directory. Now we cannot 
               // do anything because we are already doing commit or rollback. We shold just
               // remove this file from the global lists.
               removeDeletedFileFromGlobalLists(iSenderIdentificator, 
                                                iListIdentificator,
                                                iErrorIndexPosition);
            }
         }
      }
   }

   /**
    * Method for removing just deleted file from the global lstReadyToXXX list.
    * 
    * @param iSenderIdentificator - identifier of the sender - commit/rollback.
    * @param iListIdentificator - identifier of the list. By this identifier we can decide 
    *                             deleting processed item from the particular global list.
    * @param iIndexPosition - actual position in the particular global list. Items from the list
    *                         are removed from the start of this list. If error has occurred, index
    *                         position is increased and this item will be not deleted from the list
    *                         but whole process will repeat.
    */
   private void removeDeletedFileFromGlobalLists(
      int iSenderIdentificator,
      int iListIdentificator,
      int iIndexPosition
   )
   {
      switch (iSenderIdentificator)
      {
         // if the sender is commit() method
         case SENDER_COMMIT:
         {
            switch (iListIdentificator)
            {
               case LIST_IDENTIFIER_TO_BE_REPLACED:
               {
                  // remove processed item from ready to be replaced list
                  m_lstReadyToBeReplacedFrom.remove(iIndexPosition);
                  break;
               }
               case LIST_IDENTIFIER_DELETE:
               {
                  // remove processed item from ready to delete list
                  m_lstReadyToDeleteFrom.remove(iIndexPosition);
                  break;
               }
               default:
               {
                  assert false : "List identifier is incorrectly specified.";
               }
            }
            break;
         }
         // if the sender is rollback() method
         case SENDER_ROLLBACK:
         {
            switch (iListIdentificator)
            {
               case LIST_IDENTIFIER_COPY:
               {
                  // remove processed item from ready to copy list
                  m_lstReadyToCopyFrom.remove(iIndexPosition);
                  break;
               }
               case LIST_IDENTIFIER_MOVE:
               {
                  m_lstReadyToMoveFrom.remove(iIndexPosition);
                  break;
               }
               case LIST_IDENTIFIER_REPLACE:
               {
                  // remove processed item from ready to replace list
                  m_lstReadyToReplaceFrom.remove(iIndexPosition);
                  break;
               }
               default:
               {
                  assert false : "List identifier is incorrectly specified.";
               }
            }
            break;
         }
         default:
         {
            assert false : "Sender is incorrectly specified.";
         }
      }
   }

   /**
    * Method for initializing of global lists.
    */
   private void initGlobalLists()
   {
      m_lstReadyToCopyFrom = Collections.EMPTY_LIST;
      m_lstReadyToCopyTo = Collections.EMPTY_LIST;
      m_lstReadyToMoveFrom = Collections.EMPTY_LIST;
      m_lstReadyToMoveTo = Collections.EMPTY_LIST;
      m_lstReadyToBeReplacedFrom = Collections.EMPTY_LIST;
      m_lstReadyToBeReplacedTo = Collections.EMPTY_LIST;
      m_lstReadyToReplaceFrom = Collections.EMPTY_LIST;
      m_lstReadyToReplaceTo = Collections.EMPTY_LIST;
      m_lstReadyToDeleteFrom = Collections.EMPTY_LIST;
      m_lstReadyToDeleteTo = Collections.EMPTY_LIST;
   }

   /**
    * Method to sleep process.
    */
   private void sleep()
   {
      try
      {
         Thread.sleep(SLEEP_TIME);
      }
      catch (InterruptedException iExc)
      {
         s_logger.log(Level.WARNING, "Sleep was interrupted.");
      }
   }
}
