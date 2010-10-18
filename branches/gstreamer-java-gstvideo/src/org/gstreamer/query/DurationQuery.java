/* 
 * Copyright (C) 2008 Wayne Meissner
 * Copyright (C) 1999,2000 Erik Walthinsen <omega@cse.ogi.edu>
 *                    2000 Wim Taymans <wim.taymans@chello.be>
 *                    2005 Wim Taymans <wim@fluendo.com>
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

package org.gstreamer.query;

import org.gstreamer.Format;
import org.gstreamer.Query;
import org.gstreamer.lowlevel.GstNative;

import com.sun.jna.Pointer;

/**
 * Used to query the total duration of a stream.
 */
public class DurationQuery extends Query {
    private static interface API extends com.sun.jna.Library { 
        Pointer ptr_gst_query_new_duration(Format format);
        void gst_query_set_duration(Query query, Format format, /* gint64 */ long duration);
        void gst_query_parse_duration(Query query, Format[] format, /* gint64 * */ long[] duration);
    }
    private static final API gst = GstNative.load(API.class);
    public DurationQuery(Initializer init) {
        super(init);
    }
    /**
     * Constructs a new stream duration query object to query in the given format. 
     * A duration query will give the total length of the stream.
     * 
     * @param format the {@link Format} for this duration query.
     */
    public DurationQuery(Format format) {
        super(initializer(gst.ptr_gst_query_new_duration(format)));
    }
    /**
     * Answers a duration query by setting the requested value in the given format.
     * @param format the {@link Format} for the duration
     * @param duration the duration of the stream
     */
    public void setDuration(Format format, long duration) {
        gst.gst_query_set_duration(this, format, duration);
    }
    /**
     * Gets the format of this duration query.
     * 
     * @return The {@link Format} of the duration value.
     */
    public Format getFormat() {
        Format[] fmt = new Format[1];
        gst.gst_query_parse_duration(this, fmt, null);
        return fmt[0];
    }

    /**
     * Gets the duration answer for this duration query.
     * 
     * @return The total duration.
     */
    public long getDuration() {
        long[] duration = new long[1];
        gst.gst_query_parse_duration(this, null, duration);
        return duration[0];
    }
    
    /**
     * Gets the duration as a user-readable string.
     * 
     * @return A string representing the duration.
     */
    @Override
    public String toString() {
        return String.format("duration: [format=%s, duration=%d]", getFormat(), getDuration());
    }
}
