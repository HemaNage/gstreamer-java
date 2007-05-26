/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package org.gstreamer;

import com.sun.jna.Pointer;

/**
 *
 */
public class Clock extends GstObject {

    public Clock(Pointer ptr, boolean needRef, boolean ownsHandle) {
        super(ptr, needRef, ownsHandle);
    }
    static Clock objectFor(Pointer ptr) {
        return objectFor(ptr, true);
    }
    static Clock objectFor(Pointer ptr, boolean needRef) {
        return (Clock) GstObject.objectFor(ptr, Clock.class, needRef);
    }
}
