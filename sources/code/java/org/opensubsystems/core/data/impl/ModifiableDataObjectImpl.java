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
 
package org.opensubsystems.core.data.impl;

import java.sql.Timestamp;

import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.data.ModifiableDataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.HashCodeUtils;
import org.opensubsystems.core.util.ObjectUtils;

/**
 * Base class for all data objects that can be modified and therefore want to 
 * track when they were last modified.
 * 
 * @author bastafidli
 */
public abstract class ModifiableDataObjectImpl extends    BasicDataObjectImpl
                                               implements ModifiableDataObject
{
   // Attributes ///////////////////////////////////////////////////////////////

   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -4054185247427458814L;

   /**
    * Modification timestamp when the data object was last time modified.
    */
   protected Timestamp m_modificationTimestamp;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Simple constructor creating new data object in particular domain.
    * 
    * @param clsDataDescriptor - class identifying data descriptor for the object
    * @param lDomainId - domain this data object belongs to
    * @throws OSSException - an error has occurred
    */
   public ModifiableDataObjectImpl(
      Class<DataDescriptor> clsDataDescriptor,
      long                  lDomainId
   ) throws OSSException
   {
      this(DataObject.NEW_ID, clsDataDescriptor, lDomainId, null, null);
   }

   /**
    * Full constructor.
    * 
    * @param lId - id of this data object
    * @param clsDataDescriptor - class identifying data descriptor for the object
    * @param lDomainId - domain this data object belongs to
    * @param creationTimestamp - timestamp when the data object was created.
    * @param modificationTimestamp - timestamp when the data object was last 
    *                                time modified.
    * @throws OSSException - an error has occurred
    */
   public ModifiableDataObjectImpl(
      long                  lId,
      Class<DataDescriptor> clsDataDescriptor,
      long                  lDomainId,
      Timestamp             creationTimestamp, 
      Timestamp             modificationTimestamp
   ) throws OSSException
   {
      super(lId, clsDataDescriptor, lDomainId, creationTimestamp);
      
      m_modificationTimestamp = modificationTimestamp;
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public Timestamp getModificationTimestamp()
   {
      return m_modificationTimestamp;
   }
   
   /**
    * {@inheritDoc}
    */
   public void setModificationTimestamp(
      Timestamp modificationTimestamp
   )
   {
      m_modificationTimestamp = modificationTimestamp;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(
      Object oObject
   )
   {
      boolean bReturn = false;
      ModifiableDataObject helper;

      if (oObject == this)
      {
         bReturn = true;
      }
      else if ((oObject != null) && (oObject instanceof ModifiableDataObject))
      {
         helper = (ModifiableDataObject) oObject;
         bReturn = ObjectUtils.equals(getModificationTimestamp(), 
                                      helper.getModificationTimestamp()) 
                   && (super.equals(oObject));
      }

      return bReturn;
   }   

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      int iResult = HashCodeUtils.SEED;
      if (m_modificationTimestamp != null)
      {
         iResult = HashCodeUtils.hash(iResult, m_modificationTimestamp.getTime());
      }
      else
      {
         iResult = HashCodeUtils.hash(iResult, "null");         
      }
      iResult = HashCodeUtils.hash(iResult, super.hashCode());
      return iResult;
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Restore all values from specified values. This is here to reinitialize
    * object in case it needs to be reused or reconstructed (e.g. when rollback
    * is issued).
    * 
    * @param lId - id of this data object
    * @param clsDataDescriptor - class identifying data descriptor for the object
    * @param lDomainId - domain this data object belongs to
    * @param creationTimestamp - timestamp when the data object was created.
    * @param modificationTimestamp - timestamp when the data object was last 
    *                                time modified.
    * @throws OSSException - an error has occurred
    */
   protected void restore(
      long                  lId,
      Class<DataDescriptor> clsDataDescriptor,
      long                  lDomainId,
      Timestamp             creationTimestamp, 
      Timestamp             modificationTimestamp
   ) throws OSSException
   {
      super.restore(lId, clsDataDescriptor, lDomainId, creationTimestamp);
      
      m_modificationTimestamp = modificationTimestamp;
   }
}
