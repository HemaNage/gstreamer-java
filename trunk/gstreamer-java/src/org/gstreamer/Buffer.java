/* 
 * Copyright (C) 2007 Wayne Meissner
 * Copyright (C) 1999,2000 Erik Walthinsen <omega@cse.ogi.edu>
 *                    2000 Wim Taymans <wtay@chello.be>
 * 
 * This file is part of gstreamer-java.
 *
 * gstreamer-java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gstreamer-java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gstreamer-java.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gstreamer;

import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
import org.gstreamer.lowlevel.GstAPI;
import org.gstreamer.lowlevel.GstAPI.BufferStruct;
import static org.gstreamer.lowlevel.GstAPI.gst;

/**
 * Data-passing buffer type, supporting sub-buffers.
 * Ssee {@link Pad}, {@link MiniObject}
 * <p>
 * Buffers are the basic unit of data transfer in GStreamer.  The Buffer
 * type provides all the state necessary to define a region of memory as part
 * of a stream.  Sub-buffers are also supported, allowing a smaller region of a
 * buffer to become its own buffer, with mechanisms in place to ensure that
 * neither memory space goes away prematurely.
 * <p>
 * Non-plugins will usually not need to allocate buffers, but they can be allocated
 * using new {@link #Buffer(int)} to create a buffer with preallocated data of a given size.
 * <p>
 * The data pointed to by the buffer can be accessed with the {@link #getByteBuffer}
 * method.  For buffers of size 0, the data pointer is undefined (usually NULL) 
 * and should never be used.
 * <p>
 * If an element knows what pad you will push the buffer out on, it should use
 * gst_pad_alloc_buffer() instead to create a buffer.  This allows downstream
 * elements to provide special buffers to write in, like hardware buffers.
 * <p>
 * A buffer has a pointer to a {@link Caps} describing the media type of the data
 * in the buffer. Attach caps to the buffer with {@link #setCaps}; this
 * is typically done before pushing out a buffer using gst_pad_push() so that
 * the downstream element knows the type of the buffer.
 * <p>
 * A buffer will usually have a timestamp, and a duration, but neither of these
 * are guaranteed (they may be set to -1). Whenever a
 * meaningful value can be given for these, they should be set. The timestamp
 * and duration are measured in nanoseconds (they are long values).
 * <p>
 * A buffer can also have one or both of a start and an end offset. These are
 * media-type specific. For video buffers, the start offset will generally be
 * the frame number. For audio buffers, it will be the number of samples
 * produced so far. For compressed data, it could be the byte offset in a
 * source or destination file. Likewise, the end offset will be the offset of
 * the end of the buffer. These can only be meaningfully interpreted if you
 * know the media type of the buffer (the #GstCaps set on it). Either or both
 * can be set to -1.
 * <p>
 * To efficiently create a smaller buffer out of an existing one, you can
 * use {@link #createSubBuffer}.
 * <p>
 * If a plug-in wants to modify the buffer data in-place, it should first obtain
 * a buffer that is safe to modify by using {@link #makeWritable}.  This
 * function is optimized so that a copy will only be made when it is necessary.
 * <p>
 * A plugin that only wishes to modify the metadata of a buffer, such as the
 * offset, timestamp or caps, should use gst_buffer_make_metadata_writable(),
 * which will create a subbuffer of the original buffer to ensure the caller
 * has sole ownership, and not copy the buffer data.
 * <p>
 * Buffers can be efficiently merged into a larger buffer with
 * gst_buffer_merge() and gst_buffer_span() if the gst_buffer_is_span_fast()
 * function returns TRUE.
 * <p>
 */
public class Buffer extends MiniObject {
    
    public Buffer(Initializer init) {
        super(init);
        struct = new BufferStruct(handle());
    }
    
    /**
     * Creates a newly allocated buffer without any data.
     */
    public Buffer() {
        this(gst.gst_buffer_new(), false);
    }
    
    /**
     * Creates a newly allocated buffer with data of the given size.
     * The buffer memory is not cleared. If the requested amount of
     * memory cannot be allocated, an exception will be thrown.
     *
     * Note that when size == 0, the buffer data pointer will be NULL.
     *
     * @param size
     */
    public Buffer(int size) {
        this(initializer(allocBuffer(size), false, true));
    }
    
    private static Pointer allocBuffer(int size) {
        Pointer ptr = GstAPI.gst.gst_buffer_new_and_alloc(size);
        if (ptr == null) {
            throw new OutOfMemoryError("Could not allocate Buffer of size "+ size);
        }
        return ptr;
    }
    Buffer(Pointer ptr, boolean needRef) {
        this(initializer(ptr, needRef, true));
    }
    
    protected void ref() {
        super.ref();
        if (struct != null) {
            struct.read(); // sync up the refcnt
        }
    }
    protected void unref() {
        super.unref();
        if (struct != null) {
            struct.read(); // sync up the refcnt
        }
    }
    
    /**
     * Creates a sub-buffer from this buffer at offset and size.
     * <p>
     * This sub-buffer uses the actual memory space of the parent buffer.
     * This function will copy the offset and timestamp fields when the
     * offset is 0. If not, they will both be set to -1.
     * <p>
     * If offset equals 0 and size equals the total size of @buffer, the
     * duration and offset end fields are also copied. If not they will be set
     * to -1.
     *
     * @param offset The offset into parent Buffer at which the new sub-buffer begins.
     * @param size the size of the new Buffer sub-buffer, in bytes.
     * @return The new Buffer, or null if the arguments were invalid.
     */
    public Buffer createSubBuffer(int offset, int size) {
        return gst.gst_buffer_create_sub(this, offset, size);
    }
    /**
     * Tests if you can safely write data into a buffer's data array or validly
     * modify the caps and timestamp metadata. Metadata in a GstBuffer is always
     * writable, but it is only safe to change it when there is only one owner
     * of the buffer - ie, the refcount is 1. 
     * 
     * @return true if the Buffer is writable.
     */
    @Override
    public boolean isWritable() {
        return super.isWritable();
    }
    
    /**
     * Makes a writable buffer from this buffer. If the this buffer is
     * already writable, this will simply return the same buffer. A copy will 
     * otherwise be made and returned.
     * 
     * @return A writable Buffer referring to the same memory as this one.
     */
    public Buffer makeWritable() {
        Pointer ptr = gst.gst_mini_object_make_writable(this);
        if (ptr == null) {
            throw new NullPointerException("Could not make Buffer writable");
        }
        // If it returned the same pointer, can safely return this - else create a new Proxy
        return ptr.equals(handle()) ? this : new Buffer(ptr, false);
    }
    public boolean isMetadataWritable() {
        return false;
    }
    
    public int getSize() {
        return struct.size;
    }
    public long getDuration() {
        return struct.duration;
    }
    public long getTimestamp() {
        return struct.timestamp;
    }
    
    /**
     * Gets the media type of the buffer. This can be null if there
     * is no media type attached to this buffer.
     *
     * @return a {@link Caps} describing the media type, or null if there was no
     * media type associated with the buffer.
     */
    public Caps getCaps() {
        return gst.gst_buffer_get_caps(this);
    }
    /**
     * Sets the media type on the buffer. 
     * @param caps the {@link Caps} describing the media type.
     */
    public void setCaps(Caps caps) {
        gst.gst_buffer_set_caps(this, caps);
    }
    
    /**
     * Get a {@link java.nio.ByteBuffer} that can access the native memory
     * associated with this Buffer.
     * @return A {@link java.nio.ByteBuffer} that can access this Buffer's data.
     */
    public synchronized ByteBuffer getByteBuffer() {
        if (byteBuffer == null && struct.data != null && struct.size > 0) {
            byteBuffer = struct.data.getByteBuffer(0, struct.size);
        }
        return byteBuffer;
    }
    public long getOffset() {
        return struct.offset;
    }
    public void setOffset(long offset) {
        struct.offset = offset;
    }
    public long getLastOffset() {
        return struct.offset_end;
    }
    public void setLastOffset(long offset) {
        struct.offset_end = offset;
    }
    public static Buffer objectFor(Pointer ptr, boolean needRef) {
        return MiniObject.objectFor(ptr, Buffer.class, needRef);
    }
    public final BufferStruct struct;
    private ByteBuffer byteBuffer;
}
