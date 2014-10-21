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
 
package org.opensubsystems.core.data.impl;

import java.sql.Timestamp;

import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.data.IdentifiableDataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.HashCodeUtils;
import org.opensubsystems.core.util.ObjectUtils;
import org.opensubsystems.core.util.StringUtils;

/**
 * Base class for all data objects that can be identified by their name and the 
 * description. Since for most objects with name and description, these attributes
 * can change, the parent is modifiable data object.
 * 
 * This class is not abstract since name and description are sufficient 
 * attributes for some objects.
 * 
 * @author bastafidli
 */
public class IdentifiableDataObjectImpl extends    ModifiableDataObjectImpl
                                        implements IdentifiableDataObject
{
   // Attributes ///////////////////////////////////////////////////////////////

   /**
    * Name of the data object.
    */
   protected String m_strName;

   /**
    * Description of the data object.
    */
   protected String m_strDescription;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Simple constructor creating new data object in particular domain.
    * 
    * @param clsDataDescriptor - class identifying data descriptor for the object
    * @param lDomainId - domain this data object belongs to
    * @param strName - name of the data object
    * @param strDescription - description of the data object
    * @throws OSSException - an error has occurred
    */
   public IdentifiableDataObjectImpl(
      Class<? extends DataDescriptor> clsDataDescriptor,
      long                            lDomainId,
      String                          strName,
      String                          strDescription
   ) throws OSSException
   {
      this(DataObject.NEW_ID, clsDataDescriptor, lDomainId, null, null, strName, 
           strDescription);
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
    * @param strName - name of the data object
    * @param strDescription - description of the data object
    * @throws OSSException - an error has occurred
    */
   public IdentifiableDataObjectImpl(
      long                            lId,
      Class<? extends DataDescriptor> clsDataDescriptor,
      long                            lDomainId,
      Timestamp                       creationTimestamp, 
      Timestamp                       modificationTimestamp,
      String                          strName,
      String                          strDescription
   ) throws OSSException
   {
      super(lId, clsDataDescriptor, lDomainId, creationTimestamp, modificationTimestamp);
      
      m_strName = strName;
      m_strDescription = strDescription;
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName()
   {
      return m_strName;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setName(
      String strName
   )
   {
      m_strName = strName;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public String getDescription()
   {
      return m_strDescription;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setDescription(
      String strDescription
   )
   {
      m_strDescription = strDescription;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void toString(
      StringBuilder sb,
      int           ind
   )
   {
      append(sb, ind + 0, "IdentifiableDataObjectImpl[", true);
      append(sb, ind + 1, "m_strName = ", m_strName);
      append(sb, ind + 1, "m_strDescription = ", m_strDescription);
      super.toString(sb, ind + 1);
      append(sb, ind + 0, "]", true);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isSame(
      Object oObject
   )
   {
      boolean bReturn = false;
      IdentifiableDataObject helper;

      if (oObject == this)
      {
         bReturn = true;
      }
      else if ((oObject != null) && (oObject instanceof IdentifiableDataObject))
      {
         helper = (IdentifiableDataObject) oObject;
         bReturn = ObjectUtils.equals(getName(), helper.getName()) 
                   && ObjectUtils.equals(getDescription(), helper.getDescription());
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
 
      iResult = HashCodeUtils.hash(iResult, m_strName);
      iResult = HashCodeUtils.hash(iResult, m_strDescription);
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
    * @param strName - name of the data object
    * @param strDescription - description of the data object
    * @throws OSSException - an error has occurred
    */
   protected void restore(
      long                  lId,
      Class<DataDescriptor> clsDataDescriptor,
      long                  lDomainId,
      Timestamp             creationTimestamp, 
      Timestamp             modificationTimestamp,
      String                strName,
      String                strDescription
   ) throws OSSException
   {
      super.restore(lId, clsDataDescriptor, lDomainId, creationTimestamp, modificationTimestamp);
      
      m_strName = strName;
      m_strDescription = strDescription;
   }
}
