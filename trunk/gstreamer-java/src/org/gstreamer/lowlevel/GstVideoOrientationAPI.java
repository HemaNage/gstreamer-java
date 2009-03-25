/* 
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

import org.gstreamer.interfaces.VideoOrientation;

import com.sun.jna.Library;

public interface GstVideoOrientationAPI extends Library {
	GstVideoOrientationAPI INSTANCE = GstNative.load("gstinterfaces",
			GstVideoOrientationAPI.class);

	GType gst_video_orientation_get_type();

	/* vitrual class functions */
	boolean gst_video_orientation_get_hflip(VideoOrientation video_orientation,
			boolean flip);

	boolean gst_video_orientation_get_vflip(VideoOrientation video_orientation,
			boolean flip);

	boolean gst_video_orientation_get_hcenter(
			VideoOrientation video_orientation, int center);

	boolean gst_video_orientation_get_vcenter(
			VideoOrientation video_orientation, int center);

	boolean gst_video_orientation_set_hflip(VideoOrientation video_orientation,
			boolean flip);

	boolean gst_video_orientation_set_vflip(VideoOrientation video_orientation,
			boolean flip);

	boolean gst_video_orientation_set_hcenter(
			VideoOrientation video_orientation, int center);

	boolean gst_video_orientation_set_vcenter(
			VideoOrientation video_orientation, int center);
}
