/* 
 * Copyright (c) 2009 Levente Farkas
 * Copyright (c) 2007, 2008 Wayne Meissner
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

import org.gstreamer.Bus;
import org.gstreamer.ClockTime;
import org.gstreamer.Message;
import org.gstreamer.MessageType;
import org.gstreamer.lowlevel.GstAPI.GstCallback;
import org.gstreamer.lowlevel.annotations.CallerOwnsReturn;
import org.gstreamer.lowlevel.annotations.IncRef;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * GstBus functions
 */
public interface GstBusAPI extends com.sun.jna.Library {
    GstBusAPI GSTBUS_API = GstNative.load(GstBusAPI.class);

    GType gst_bus_get_type();
    Bus gst_bus_new();
    boolean gst_bus_post(Bus bus, @IncRef Message message);

    boolean gst_bus_have_pending(Bus bus);
    @CallerOwnsReturn Message gst_bus_peek(Bus bus);
    @CallerOwnsReturn Message gst_bus_pop(Bus bus);
    @CallerOwnsReturn Message gst_bus_pop_filtered(Bus bus, MessageType types);
    @CallerOwnsReturn Message gst_bus_timed_pop(Bus bus, ClockTime timeout);
    @CallerOwnsReturn Message gst_bus_timed_pop_filtered(Bus bus, ClockTime timeout, MessageType types);
    /* polling the bus */
    @CallerOwnsReturn Message gst_bus_poll(Bus bus, MessageType events, /* GstClockTimeDiff */ long timeout);
    @CallerOwnsReturn Message gst_bus_poll(Bus bus, MessageType events, ClockTime timeout);

    void gst_bus_set_flushing(Bus ptr, int flushing);
    interface BusCallback extends GstCallback {
        boolean callback(Bus bus, Message msg, Pointer data);
    }
    NativeLong gst_bus_add_watch(Bus bus, BusCallback function, Pointer data);
    void gst_bus_set_sync_handler(Bus bus, GstCallback function, Pointer data);
    void gst_bus_enable_sync_message_emission(Bus bus);
    void gst_bus_disable_sync_message_emission(Bus bus);
    
}
