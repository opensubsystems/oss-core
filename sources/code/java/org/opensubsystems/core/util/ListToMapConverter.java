/*
 * Copyright (C) 2012 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.util.List;
import java.util.Map;

/**
 * Convert list of values to map keyed by Longs.
 * 
 * @author bastafidli
 * @param <K> - type of keys used for resulting Map
 * @param <V> - type of values used for resulting Map
 * @param <L> - type of values used in the original list
 */
public interface ListToMapConverter<K, V, L>
{
   /**
    * Convert list of values to a map keyed by a some key. The objects in the 
    * resulting maps may or may not be the same objects as the ones passed in.
    * 
    * 
    * @param lstValues - list of values to convert
    * @return Map - resulting Map 
    */
   public Map<K, V> convert(List<L> lstValues);
}
