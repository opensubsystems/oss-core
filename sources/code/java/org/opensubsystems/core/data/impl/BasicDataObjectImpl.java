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
  
package org.opensubsystems.core.data.impl;

import java.io.Serializable;
import java.sql.Timestamp;

import org.opensubsystems.core.data.BasicDataObject;
import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.HashCodeUtils;
import org.opensubsystems.core.util.ObjectUtils;

/**
 * Base class for all data objects that can be uniquely identified, track 
 * when they were created and which belongs to some partition called domain.
 * 
 * @author bastafidli
 */
public abstract class BasicDataObjectImpl extends DataObjectImpl
                                          implements BasicDataObject,
                                                     Serializable
{
   // Attributes ///////////////////////////////////////////////////////////////

   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 6389524056661895594L;

   /**
    * Id of the domain this data object belongs to. Domain represents default 
    * partition where the data object belongs to (since it is presumed that
    * each data object belongs to some partition). 
    */
   protected long m_lDomainId; 

   /**
    * Flag which is set to true if this data was loaded from some persistence 
    * store, e.g. database and false if it was constructed in memory and it is
    * not persisted.
    */
   protected boolean m_bFromPersistanceStore = false;

   /**
    * Creation timestamp when the data object was created.
    */
   protected Timestamp m_creationTimestamp;

   // Cached values ////////////////////////////////////////////////////////////
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Simple constructor creating new data object in particular domain.
    * 
    * @param clsDataDescriptor - class identifying data descriptor for the object
    * @param lDomainId - domain this data object belongs to
    * @throws OSSException - an error has occurred
    */
   public BasicDataObjectImpl(
      Class<DataDescriptor> clsDataDescriptor,
      long                  lDomainId
   ) throws OSSException
   {
      this(DataObject.NEW_ID, clsDataDescriptor, lDomainId, null);
   }

   /**
    * Full constructor.
    * 
    * @param lId - id of this data object
    * @param clsDataDescriptor - class identifying data descriptor for the object
    * @param lDomainId - domain this data object belongs to
    * @param creationTimestamp - timestamp when the data object was created.
    * @throws OSSException - an error has occurred
    */
   public BasicDataObjectImpl(
      long                  lId,
      Class<DataDescriptor> clsDataDescriptor,
      long                  lDomainId,
      Timestamp             creationTimestamp 
   ) throws OSSException
   {
      super(lId, clsDataDescriptor);

      m_lDomainId = lDomainId;
      m_creationTimestamp = creationTimestamp;
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isFromPersistenceStore(
   )
   {
      return m_bFromPersistanceStore;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setFromPersistenceStore(
   )
   {
      m_bFromPersistanceStore = true;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public long getDomainId(
   ) 
   {
      return m_lDomainId;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Timestamp getCreationTimestamp()
   {
      return m_creationTimestamp;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setCreationTimestamp(
      Timestamp creationTimestamp
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert ((m_creationTimestamp == null) || (creationTimestamp == null))
                 : "Creation timestamp can be set only if it is null.";
      }

      m_creationTimestamp = creationTimestamp;
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
      BasicDataObject helper;

      if (oObject == this)
      {
         bReturn = true;
      }
      else if ((oObject != null) && (oObject instanceof BasicDataObject))
      {
         helper = (BasicDataObject) oObject;
         // No need to compare the cached values since they are derived from these
         // We need to compare the getXyz methods here because if we compare
         // variable with getXyz method the comparison would fail since the 
         // getXyz method may implement special behavior
         bReturn = (getDomainId() == helper.getDomainId())
                   && ObjectUtils.equals(getCreationTimestamp(), 
                                         helper.getCreationTimestamp())
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
      iResult = HashCodeUtils.hash(iResult, m_lDomainId);
      if (m_creationTimestamp != null)
      {
         iResult = HashCodeUtils.hash(iResult, m_creationTimestamp.getTime());
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
    * @throws OSSException - an error has occurred
    */
   protected void restore(
      long                  lId,
      Class<DataDescriptor> clsDataDescriptor,
      long                  lDomainId,
      Timestamp             creationTimestamp 
   ) throws OSSException
   {
      super.restore(lId, clsDataDescriptor);

      m_lDomainId = lDomainId;
      m_creationTimestamp = creationTimestamp;
   }
}
