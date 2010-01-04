/* 
 * Copyright (c) 2009 Levente Farkas
 * Copyright (c) 2009 Tamas Korodi <kotyo@zamba.fm>
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

import org.gstreamer.interfaces.XOverlay;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public interface GstXOverlayAPI extends Library {
	GstXOverlayAPI GSTXOVERLAY_API = GstNative.load("gstinterfaces", GstXOverlayAPI.class);

	GType gst_x_overlay_get_type();

	/* virtual class function wrappers */
	void gst_x_overlay_set_xwindow_id(XOverlay overlay, NativeLong xwindow_id);

	void gst_x_overlay_set_xwindow_id(XOverlay overlay, Pointer xwindow_id);

	void gst_x_overlay_expose(XOverlay overlay);

	void gst_x_overlay_handle_events(XOverlay overlay, boolean handle_events);
}
