/* 
 * Copyright (c) 2009 Levente Farkas
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

import org.gstreamer.lowlevel.NativeObject;
import org.gstreamer.lowlevel.RefCountedObject;

import com.sun.jna.Pointer;

import static org.gstreamer.lowlevel.GstMiniObjectAPI.GSTMINIOBJECT_API;

/**
 * Lightweight base class for the GStreamer object hierarchy
 *
 * MiniObject is a baseclass like {@link GObject}, but has been stripped down of 
 * features to be fast and small.
 * It offers sub-classing and ref-counting in the same way as GObject does.
 * It has no properties and no signal-support though.
 */
public class MiniObject extends RefCountedObject {
    /**
     * Creates a new instance of MiniObject
     */
    public MiniObject(Initializer init) {
        super(init);
    }
    
    /**
     * Checks if a mini-object is writable.  A mini-object is writable
     * if the reference count is one and the {@link MiniObjectFlags#READONLY}
     * flag is not set.  Modification of a mini-object should only be
     * done after verifying that it is writable.
     *
     * @return true if the object is writable.
     */
    public boolean isWritable() {
        return GSTMINIOBJECT_API.gst_mini_object_is_writable(this);
    }
    
    /**
     * Makes a writable instance of this MiniObject.
     * <p> The result is cast to <tt>subclass</tt>.
     * 
     * @param subclass the subclass to cast the result to.
     * @return a writable version of this MiniObject.
     */
    protected <T extends MiniObject> T makeWritable(Class<T> subclass) {
        MiniObject result = GSTMINIOBJECT_API.gst_mini_object_make_writable(this);
        if (result == null) {
            throw new NullPointerException("Could not make " + subclass.getSimpleName() 
                    + " writable");
        }
        return subclass.cast(result);
    }
    /*
     * FIXME: this one returns a new MiniObject, so we need to replace the Pointer
     * with the new one.  Messy.
    public void makeWritable() {
        GSTMINIOBJECT_API.gst_mini_object_make_writable(this);
    }
    */
    protected void ref() {
        GSTMINIOBJECT_API.gst_mini_object_ref(this);
    }
    protected void unref() {
        GSTMINIOBJECT_API.gst_mini_object_unref(this);
    }
    
    protected void disposeNativeHandle(Pointer ptr) {
        GSTMINIOBJECT_API.gst_mini_object_unref(ptr);
    }
    
    public static <T extends MiniObject> T objectFor(Pointer ptr, Class<T> defaultClass, boolean needRef) {        
        return NativeObject.objectFor(ptr, defaultClass, needRef);        
    }
}
