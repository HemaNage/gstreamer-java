/* 
 * Copyright (c) 2009 Levente Farkas
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

import java.util.Arrays;
import java.util.List;

import org.gstreamer.ClockTime;
import org.gstreamer.controller.ControlSource;
import org.gstreamer.controller.InterpolationControlSource;
import org.gstreamer.lowlevel.GValueAPI.GValue;
import org.gstreamer.lowlevel.GlibAPI.GList;
import org.gstreamer.lowlevel.GlibAPI.GSList;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface GstInterpolationControlSourceAPI extends Library {
	GstInterpolationControlSourceAPI GSTINTERPOLATIONCONTROLSOURCE_API 
		= GstNative.load("gstcontroller", GstInterpolationControlSourceAPI.class);
    int GST_PADDING = GstAPI.GST_PADDING;
    
    public enum InterpolateMode {
      NONE,
      TRIGGER,
      LINEAR,
      QUADRATIC,
      CUBIC,
      USER;
    }
	
	public static final class GstInterpolationControlSourceStruct extends com.sun.jna.Structure {
		public volatile ControlSource parent;

		/* <private> */
		public volatile Pointer /* GMutex */ lock;
		public volatile Pointer /* GstInterpolationControlSourcePrivate */ priv;
		public volatile Pointer[] _gst_reserved = new Pointer[GST_PADDING];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{
                "parent", "lock", "priv",
                "_gst_reserved"
            });
        }
	}
	
	public static final class GstInterpolationControlSourceClass extends com.sun.jna.Structure {
		public volatile ControlSource parent_class;
		  
		/*< private >*/
		public volatile Pointer[] _gst_reserved = new Pointer[GST_PADDING];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{
                "parent_class", "_gst_reserved"
            });
        }
	}
	
	GType gst_interpolation_control_source_get_type();

	/* Functions */
	InterpolationControlSource gst_interpolation_control_source_new();
	boolean gst_interpolation_control_source_set_interpolation_mode(InterpolationControlSource self, InterpolateMode mode);
	boolean gst_interpolation_control_source_set(InterpolationControlSource self, ClockTime timestamp, GValue value);
	boolean gst_interpolation_control_source_set_from_list(InterpolationControlSource self, GSList timedvalues);
	boolean gst_interpolation_control_source_unset(InterpolationControlSource self, ClockTime timestamp);
	void gst_interpolation_control_source_unset_all(InterpolationControlSource self);
	GList gst_interpolation_control_source_get_all(InterpolationControlSource self);
	int gst_interpolation_control_source_get_count(InterpolationControlSource self);
}
