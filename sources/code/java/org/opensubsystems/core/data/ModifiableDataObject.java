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

package org.opensubsystems.core.data;

import java.sql.Timestamp;

/**
 * Base interface for all data objects that can be modified and therefore want 
 * to track when they were last modified.
 *
 * @author bastafidli
 */
public interface ModifiableDataObject extends BasicDataObject
{
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Get modification timestamp when the data object was last time modified.
    *
    * @return Timestamp
    */
   Timestamp getModificationTimestamp(
   );
   
   /**
    * Set modification timestamp when the data object was last time modified.
    *
    * @param modificationTimestamp - new modification timestamp
    */
   void setModificationTimestamp(
      Timestamp modificationTimestamp
   );
}
