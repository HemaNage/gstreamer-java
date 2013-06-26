/* 
 * Copyright (c) 2007 Wayne Meissner
 * 
 * This file is part of gstreamer-java.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gstreamer.lowlevel;

import com.sun.jna.Pointer;

@SuppressWarnings("serial")
public class IntPtr extends Number {
    public final Number value;
    public IntPtr(int value) {
        this.value = Pointer.SIZE == 8 ? new Long(value) : new Integer(value);
    }
    
    @Override
	public String toString() {        
        return Integer.toHexString(intValue());
    }

    @Override
	public int intValue() {
        return value.intValue();
    }
    
    @Override
	public long longValue() {
        return value.longValue();
    }

    @Override
	public float floatValue() {
        return value.floatValue();        
    }

    @Override
	public double doubleValue() {
        return value.doubleValue();        
    }
}
