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

import org.gstreamer.lowlevel.GstClockAPI;
import org.gstreamer.lowlevel.GstNative;
import org.gstreamer.lowlevel.RefCountedObject;

import com.sun.jna.Pointer;

/**
 * A datatype to hold the handle to an outstanding sync or async clock callback.
 */
public class ClockID extends RefCountedObject implements Comparable<ClockID> {
    private static interface API extends GstClockAPI {}
    private static final API gst = GstNative.load(API.class);
    
    public ClockID(Initializer init) {
        super(init);
    }
    
    @Override
    protected void disposeNativeHandle(Pointer ptr) {
        gst.gst_clock_id_unref(ptr);
    }

    @Override
    protected void ref() {
        gst.gst_clock_id_ref(this);
    }

    @Override
    protected void unref() {
        gst.gst_clock_id_unref(this);
    }
    
    /**
     * Cancel an outstanding request. This can either
     * be an outstanding async notification or a pending sync notification.
     * After this call, @id cannot be used anymore to receive sync or
     * async notifications, you need to create a new #GstClockID.
     */
    public void unschedule() {
        gst.gst_clock_id_unschedule(this);
    }
    
    /**
     * Gets the time of the clock ID
     * <p>
     * Thread safe.
     * 
     * @return The time of this clock id.
     */
    public ClockTime getTime() {
        return gst.gst_clock_id_get_time(this);
    }
    
    /**
     * Compares this  ClockID to another. 
     *
     * @param other The other ClockID to compare to
     * @return negative value if a < b; zero if a = b; positive value if a > b
     */
    public int compareTo(ClockID other) {
        return gst.gst_clock_id_compare_func(this, other);
    }
}
