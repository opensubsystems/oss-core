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

package org.opensubsystems.core.persist;

import org.opensubsystems.core.data.ModifiableDataObject;
import org.opensubsystems.core.error.OSSException;

/**
 * Basic operations to perform with the modifiable data object in the persistence 
 * store. 
 *
 * @author bastafidli
 */
public interface ModifiableDataFactory extends BasicDataFactory
{
   /**
    * Save data in the persistence store.
    *
    * @param  data - data to save
    * @return ModifiableDataObject - modified data with updated modification time
    * @throws OSSException - an error while saving data
    */
   ModifiableDataObject save(
      ModifiableDataObject data
   ) throws OSSException;
}
