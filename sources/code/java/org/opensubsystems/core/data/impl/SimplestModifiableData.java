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

import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.data.ModifiableDataObject;
import org.opensubsystems.core.error.OSSException;

/**
 * The simplest case of modifiable data object that is useful in situations when 
 * one needs transfer only the most basic identification information leaving out 
 * the business data.
 * 
 * This class is made final so that nobody cannot derive from it new classes
 * since that what the parent is for.
 * 
 * @author OpenSubsystems
 */
public final class SimplestModifiableData extends    ModifiableDataObjectImpl 
                                          implements Serializable
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 6595790483450634915L;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Simple constructor creating new data object in particular domain.
    * 
    * @param clsDataDescriptor - class identifying data descriptor for the object
    * @param lDomainId - domain this data object belongs to
    * @throws OSSException - an error has occurred
    */
   public SimplestModifiableData(
      Class<DataDescriptor> clsDataDescriptor,
      long                  lDomainId
   ) throws OSSException
   {
      this(DataObject.NEW_ID, clsDataDescriptor, lDomainId, null, null);
   }
   
   /**
    * Copy constructor.
    * 
    * @param data - source data object
    * @throws OSSException - an error has occurred
    */
   public SimplestModifiableData(
      ModifiableDataObject data
   ) throws OSSException
   {
      this(data.getId(), data.getDataDescriptorClass(), data.getDomainId(), 
           data.getCreationTimestamp(), data.getModificationTimestamp());
   }
   
   /**
    * Full constructor
    * 
    * @param lId - id of object
    * @param clsDataDescriptor - class identifying data descriptor for the object
    * @param lDomainId - domain id
    * @param creationTimestamp - creation timestamp
    * @param modificationTimestamp - modification timestamp
    * @throws OSSException - an error has occurred
    */
   public SimplestModifiableData(
      long                  lId,
      Class<DataDescriptor> clsDataDescriptor,
      long                  lDomainId,
      Timestamp             creationTimestamp,
      Timestamp             modificationTimestamp
   ) throws OSSException
   {
      super(lId, clsDataDescriptor, lDomainId, creationTimestamp, 
            modificationTimestamp);
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public boolean isSame(
      Object oObject
   )
   {
      // SimpleModifiableDataObject are always the same because they do not contain
      // any attribute other than the generated or environment dependent ones
      return true;
   }
}
