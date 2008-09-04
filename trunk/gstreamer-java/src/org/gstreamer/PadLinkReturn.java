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

package org.gstreamer;

import org.gstreamer.lowlevel.IntegerEnum;
import org.gstreamer.lowlevel.annotations.DefaultEnumValue;

/**
 * Result values from {@link Pad#link(Pad)} and friends.
 */
public enum PadLinkReturn implements IntegerEnum {
    /** Link succeeded. */
    OK(0),
    /** Pads have no common grandparent. */
    WRONG_HIERARCHY(-1),
    /** Pad was already linked. */
    WAS_LINKED(-2),
    /** Pads have wrong direction. */
    WRONG_DIRECTION(-3),
    /** Pads do not have common format. */
    NOFORMAT(-4),
    /** Pads cannot cooperate in scheduling. */
    NOSCHED(-5),
    /** Refused for some reason. */
    REFUSED(-6),
    
    /** The default enum value used when no other value matches the native value */
    @DefaultEnumValue __UNKNOWN_NATIVE_VALUE(~0);
    PadLinkReturn(int value) {
        this.value = value;
    }
    /**
     * Gets the integer value of the enum.
     * @return The integer value for this enum.
     */
    public int intValue() {
        return value;
    }
    
    private final int value;
}
