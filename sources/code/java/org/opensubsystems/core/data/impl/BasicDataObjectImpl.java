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

import java.io.Serializable;
import java.sql.Timestamp;

import org.opensubsystems.core.data.BasicDataObject;
import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.data.DataDescriptorManager;
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
public abstract class BasicDataObjectImpl implements BasicDataObject,
                                                     Serializable
{
   // Attributes ///////////////////////////////////////////////////////////////

   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 6389524056661895594L;

   /**
    * Id of this instance of data. This is private so that we can maintain the 
    * cached object value without fear that these two won't be in sync.
    */
   private long m_lId;
   
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

   /**
    * Class identifying data descriptor for the object. This is private so that 
    * we can maintain the cached object value without fear that these two won't 
    * be in sync.
    */
   private Class<DataDescriptor> m_clsDataDescriptor;
   
   // Cached values ////////////////////////////////////////////////////////////
   
   /**
    * Id of this instance of data represented as object.
    * This is constructed only when somebody asks for it. It is private so that
    * we can keep it in sync with the real id. 
    */
   private Long m_lIdObject;
   
   /**
    * Data descriptor describing the current data object. This is a cached copy
    * and in order to ensure it is in sync with the source, it is private.
    */
   private DataDescriptor m_dataDescriptor;
   
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
      restore(lId, clsDataDescriptor, lDomainId, creationTimestamp);
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public boolean isFromPersistenceStore(
   )
   {
      return m_bFromPersistanceStore;
   }

   /**
    * {@inheritDoc}
    */
   public void setFromPersistenceStore(
   )
   {
      m_bFromPersistanceStore = true;
   }
   
   /**
    * {@inheritDoc}
    */
   public long getDomainId(
   ) 
   {
      return m_lDomainId;
   }
   
   /**
    * {@inheritDoc}
    */
   public long getId() 
   {
      return m_lId;
   }

   /**
    * {@inheritDoc}
    */
   public Long getIdAsObject() 
   {
      // This doesn't have to be synchronized since it really doesn't matter
      // if we create two objects in case of concurrent access
      if (m_lIdObject == null)
      {
         if (m_lId == DataObject.NEW_ID)
         {
            m_lIdObject = DataObject.NEW_ID_OBJ;
         }
         else
         {
            m_lIdObject = new Long(m_lId);            
         }
      }
      
      return m_lIdObject;
   }

   /**
    * {@inheritDoc}
    */
   public void setId(
      long lNewId
   ) 
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         // Prevent changing id by accident
         assert ((m_lId == DataObject.NEW_ID) || (lNewId == DataObject.NEW_ID))
                : "Cannot set id for object which already has id " + m_lId;
      }

      if (m_lId != lNewId)
      {
         m_lId = lNewId;
         m_lIdObject = null;         
      }
   }

   /**
    * {@inheritDoc}
    */
   public Class<DataDescriptor> getDataDescriptorClass(
   )
   {
      return m_clsDataDescriptor;
   }
   
   /**
    * {@inheritDoc}
    */
   public DataDescriptor getDataDescriptor(
   )
   {
      return m_dataDescriptor;
   }
   
   /**
    * {@inheritDoc}
    */
   public int getDataType(
   )
   {
      return m_dataDescriptor.getDataType();
   }

   /**
    * {@inheritDoc}
    */
   public Integer getDataTypeAsObject(
   ) 
   {
      return m_dataDescriptor.getDataTypeAsObject();
   }

   /**
    * {@inheritDoc}
    */
   public Timestamp getCreationTimestamp()
   {
      return m_creationTimestamp;
   }
   
   /**
    * {@inheritDoc}
    */
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
         bReturn = (getId() == helper.getId())
                   && ObjectUtils.equals(getDataDescriptorClass(), 
                                         helper.getDataDescriptorClass())
                   && (getDomainId() == helper.getDomainId())
                   && ObjectUtils.equals(getCreationTimestamp(), 
                                         helper.getCreationTimestamp())
                  // And now compare all other "business" related attributes
                  && (isSame(oObject));
      }
      return bReturn;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      int iResult = HashCodeUtils.SEED;
      iResult = HashCodeUtils.hash(iResult, m_lId);
      if (m_clsDataDescriptor == null)
      {
         iResult = HashCodeUtils.hash(iResult, "null");
      }
      else
      {
         iResult = HashCodeUtils.hash(iResult, m_clsDataDescriptor.getName());
      }
      iResult = HashCodeUtils.hash(iResult, m_lDomainId);
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
      m_lId = lId;
      // Construct the descriptor immediately so if there is an exception if
      // happens here so that we can call all the other methods without worrying
      // about exception handling
      m_clsDataDescriptor = clsDataDescriptor;
      m_dataDescriptor = DataDescriptorManager.getInstance(m_clsDataDescriptor);
      m_lDomainId = lDomainId;
      m_creationTimestamp = creationTimestamp;
      m_lIdObject = null;
   }
}
