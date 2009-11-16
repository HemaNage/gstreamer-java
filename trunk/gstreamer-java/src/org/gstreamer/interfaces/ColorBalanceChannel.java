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

package org.gstreamer.interfaces;

import org.gstreamer.GObject;
import org.gstreamer.lowlevel.GstColorBalanceAPI;
import org.gstreamer.lowlevel.GstNative;

import com.sun.jna.Pointer;

public class ColorBalanceChannel extends GObject {
	private static final GstColorBalanceAPI gst = GstNative.load("gstinterfaces", GstColorBalanceAPI.class);
	private final GstColorBalanceAPI.ColorBalanceChannelStruct struct;
	private final ColorBalance colorBalance;

	/**
	 * For internal gstreamer-java use only
	 * 
	 * @param init
	 */
	public ColorBalanceChannel(Initializer init) {
		super(init);
		throw new IllegalArgumentException("Cannot instantiate");
	}

	ColorBalanceChannel(ColorBalance colorBalance, Pointer ptr,
			boolean needRef, boolean ownsHandle) {
		super(initializer(ptr, needRef, ownsHandle));
		struct = new GstColorBalanceAPI.ColorBalanceChannelStruct(ptr);
		this.colorBalance = colorBalance;
	}

	public String getName() {
		return struct.getLabel();
	}

	public int getMinValue() {
		return struct.getMinValue();
	}

	public int getMaxValue() {
		return struct.getMaxValue();
	}

	public void setValue(int value) {
		gst.gst_color_balance_set_value(colorBalance, this, value);
	}

	public int getValue(int value) {
		return gst.gst_color_balance_get_value(colorBalance, this);
	}
}
