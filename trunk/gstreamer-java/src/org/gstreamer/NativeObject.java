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
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
abstract class NativeObject {
    static Logger logger = Logger.getLogger(NativeObject.class.getName());
    static Level LIFECYCLE = Level.FINE;
    /** Creates a new instance of NativeObject */
    NativeObject(Pointer ptr, boolean needRef) {
        this(ptr, needRef, true);
    }
    NativeObject(Pointer ptr, boolean needRef, boolean ownsHandle) {
        logger.entering("NativeObject", "<init>", new Object[] { ptr, needRef, ownsHandle });
        logger.log(LIFECYCLE, "Creating " + getClass().getSimpleName() + " (" + ptr + ")");
        this.handle = ptr;
        this.ownsHandle = new AtomicBoolean(ownsHandle);
        nativeRef = new NativeRef(this);
        instanceMap.put(ptr, nativeRef);
        if (ownsHandle && needRef) {
            ref();
        }
    }
    NativeObject(Pointer ptr) {
        this(ptr, true, false);
    }
    
    abstract void disposeNativeHandle(Pointer ptr);
    
    public void dispose() {
        logger.log(LIFECYCLE, "Disposing object " + this + " = " + handle());
        instanceMap.remove(handle(), nativeRef);
        if (!disposed.getAndSet(true) && ownsHandle.get()) {
            Gst.invokeLater(new Runnable() {
                public void run() {
                    disposeNativeHandle(handle());
                }
            });
        }
    }
    abstract void ref();
    abstract void unref();
    
    protected void finalize() throws Throwable {
        try {
            logger.log(LIFECYCLE, "Finalizing " + getClass().getSimpleName() + " (" + handle() + ")");
            dispose();
        } finally {
            super.finalize();
        }
    }
    
    Pointer handle() {
        return handle;
    }
    static NativeObject instanceFor(Pointer ptr) {
        WeakReference<NativeObject> ref = instanceMap.get(ptr);
        
        //
        // If the reference was there, but the object it pointed to had been collected, remove it from the map
        //
        if (ref != null && ref.get() == null) {
            instanceMap.remove(ptr);
        }
        return ref != null ? ref.get() : null;
    }
    public static NativeObject objectFor(Pointer ptr, Class<? extends NativeObject> cls, boolean needRef) {
        return objectFor(ptr, cls, needRef, true);
    }

    public static NativeObject objectFor(Pointer ptr, Class<? extends NativeObject> cls, boolean needRef, boolean ownsHandle) {
        logger.entering("NativeObject", "instanceFor", new Object[] { ptr, ownsHandle, needRef });
        // Ignore null pointers
        if (ptr == null || !ptr.isValid()) {
            return null;
        }
        NativeObject obj = NativeObject.instanceFor(ptr);
        if (obj == null || !(cls.isInstance(obj))) {
            try {
                Constructor constructor = cls.getDeclaredConstructor(Pointer.class, boolean.class, boolean.class);
                obj = (NativeObject) constructor.newInstance(ptr, needRef, ownsHandle);
            } catch (SecurityException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
        return obj;
    }
    static Pointer[] getObjectHandlesV(NativeObject... objects) {
        Pointer[] handles = new Pointer[objects.length + 1];
        for (int i = 0; i < objects.length; ++i) {
            handles[i] = objects[i].handle();
        }
        return handles;
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof NativeObject && ((NativeObject) o).handle().equals(handle());
    }
    
    @Override
    public int hashCode() {
        return handle.hashCode();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + handle() + ")";
    }
    
    //
    // No longer want to garbage collect this object
    //
    protected void disown() {
        logger.log(LIFECYCLE, "Disowning " + handle());
        ownsHandle.set(false);
    }
    static class NativeRef extends WeakReference<NativeObject> {
        public NativeRef(NativeObject obj) {
            super(obj);
            handle = obj.handle();
        }
        public Pointer handle;

    }
    private AtomicBoolean disposed = new AtomicBoolean(false);
    private Pointer handle;
    final AtomicBoolean ownsHandle;
    final NativeRef nativeRef;
    private static ConcurrentHashMap<Pointer, NativeRef> instanceMap = new ConcurrentHashMap<Pointer, NativeRef>();
}
