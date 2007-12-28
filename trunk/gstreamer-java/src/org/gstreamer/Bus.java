/* 
 * Copyright (C) 2007 Wayne Meissner
 * Copyright (C) 2004 Wim Taymans <wim@fluendo.com>
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
import java.util.Collections;
import java.util.Map;
import com.sun.jna.ptr.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gstreamer.event.BusListener;
import org.gstreamer.event.BusSyncHandler;
import org.gstreamer.event.ErrorEvent;
import org.gstreamer.event.StateEvent;
import org.gstreamer.lowlevel.GstAPI.GstCallback;
import org.gstreamer.lowlevel.GstAPI.GErrorStruct;
import static org.gstreamer.lowlevel.GstAPI.gst;
import static org.gstreamer.lowlevel.GlibAPI.glib;
import org.gstreamer.lowlevel.GstAPI.MessageStruct;


/**
 * The {@link Bus} is an object responsible for delivering {@link Message}s in
 * a first-in first-out way from the streaming threads to the application.
 * <p>
 * Since the application typically only wants to deal with delivery of these
 * messages from one thread, the Bus will marshall the messages between
 * different threads. This is important since the actual streaming of media
 * is done in another thread than the application.
 * <p>
 * The Bus provides support for GSource based notifications. This makes it
 * possible to handle the delivery in the glib mainloop.
 * <p>
 * A message is posted on the bus with the gst_bus_post() method. With the
 * gst_bus_peek() and gst_bus_pop() methods one can look at or retrieve a
 * previously posted message.
 * <p>
 * The bus can be polled with the gst_bus_poll() method. This methods blocks
 * up to the specified timeout value until one of the specified messages types
 * is posted on the bus. The application can then _pop() the messages from the
 * bus to handle them.
 * <p>
 * Alternatively the application can register an asynchronous bus function
 * using gst_bus_add_watch_full() or gst_bus_add_watch(). This function will
 * install a #GSource in the default glib main loop and will deliver messages 
 * a short while after they have been posted. Note that the main loop should 
 * be running for the asynchronous callbacks.
 * <p>
 * It is also possible to get messages from the bus without any thread
 * marshalling with the {@link #setSyncHandler} method. This makes it
 * possible to react to a message in the same thread that posted the
 * message on the bus. This should only be used if the application is able
 * to deal with messages from different threads.
 * <p>
 * Every {@link Pipeline} has one bus.
 * <p>
 * Note that a Pipeline will set its bus into flushing state when changing
 * from READY to NULL state.
 */
public class Bus extends GstObject {
    static final Logger log = Logger.getLogger(Bus.class.getName());
    static final Level LOG_DEBUG = Level.FINE;
    
    /**
     * Creates a new instance of Bus
     */
    Bus(Initializer init) { 
        super(init); 
        gst.gst_bus_enable_sync_message_emission(this);
        gst.gst_bus_set_sync_handler(this, Pointer.NULL, null);
        gst.gst_bus_set_sync_handler(this, syncCallback, null);
    }
    
    /**
     * Add a listener for all message types transmitted on the Bus.
     * 
     * @param listener
     */
    public void addBusListener(BusListener listener) {
        listeners.put(listener, new BusListenerProxy(this, listener));
    }
    public void removeBusListener(BusListener listener) {
        BusListenerProxy proxy = listeners.remove(listener);
        if (proxy != null) {
            proxy.disconnect();
        }
    }
    
    /**
     * Instructs the bus to flush out any queued messages.
     * 
     * If flushing, flush out any messages queued in the bus. Will flush future 
     * messages until {@link #setFlushing} is called with false.
     * 
     * @param flushing true if flushing is desired.
     */
    public void setFlushing(boolean flushing) {
        gst.gst_bus_set_flushing(this, flushing ? 1 : 0);
    }
    
    /**
     * Signal emitted when end-of-stream is reached in a pipeline.
     * 
     * The application will only receive this message in the PLAYING state and 
     * every time it sets a pipeline to PLAYING that is in the EOS state. 
     * The application can perform a flushing seek in the pipeline, which will 
     * undo the EOS state again. 
     */
    public static interface EOS {
        public void eosMessage(GstObject source);
    }
    
    /**
     * Signal emitted when an error occurs.
     * 
     * When the application receives an error message it should stop playback
     * of the pipeline and not assume that more data will be played.
     */
    public static interface ERROR {
        public void errorMessage(GstObject source, int code, String message);
    }
    
    /**
     * Signal emitted when a warning message is delivered.
     */
    public static interface WARNING {
        public void warningMessage(GstObject source, int code, String message);
    }
    
    /**
     * Signal emitted when an informational message is delivered.
     */
    public static interface INFO {
        public void infoMessage(GstObject source, int code, String message);
    }
    
    /**
     * Signal emitted when a new tag is identified on the stream.
     */
    public static interface TAG {
        public void tagMessage(GstObject source, TagList tagList);
    }
    
    /**
     * Signal emitted when a state change happens.
     */
    public static interface STATE_CHANGED {
        public void stateMessage(GstObject source, State old, State current, State pending);
    }
    
    /**
     * Signal emitted when the pipeline is buffering data. 
     * When the application receives a buffering message in the PLAYING state 
     * for a non-live pipeline it must PAUSE the pipeline until the buffering 
     * completes, when the percentage field in the message is 100%. For live 
     * pipelines, no action must be performed and the buffering percentage can
     * be used to inform the user about the progress.
     */
    public static interface BUFFERING {
        public void bufferingMessage(GstObject source, int percent);
    }
    /**
     * Signal emitted when the duration of a pipeline changes. 
     * 
     * The application can get the new duration with a duration query.
     */
    public static interface DURATION {
        public void durationMessage(GstObject source, Format format, long duration);
    }
    public static interface SEGMENT_START {
        public void segmentStart(GstObject source, Format format, long position);
    }
    /**
     * Signal emitted when the pipeline has completed playback of a segment.
     */
    public static interface SEGMENT_DONE {
        public void segmentDone(GstObject source, Format format, long position);
    }
    
    /**
     * Add a listener for end-of-stream messages.
     * 
     * @param listener The listener to be called when end-of-stream is encountered.
     */
    public void connect(final EOS listener) {
        connect("sync-message::eos", EOS.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Pointer busPtr, Pointer msgPtr, Pointer user_data) {
                listener.eosMessage(messageSource(msgPtr));
            }
        });
    }
    
    /**
     * Disconnect the listener for end-of-stream messages.
     * 
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(EOS listener) {
        super.disconnect(EOS.class, listener);
    }
    
    /**
     * Add a listener for error messages.
     * 
     * @param listener The listener to be called when an error in the stream is encountered.
     */
    public void connect(final ERROR listener) {
        connect("sync-message::error", ERROR.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Pointer busPtr, Pointer msgPtr, Pointer user_data) {
                PointerByReference err = new PointerByReference();
                gst.gst_message_parse_error(msgPtr, err, null);
                glib.g_error_free(err.getValue());
                GErrorStruct error = new GErrorStruct(err.getValue());
                listener.errorMessage(messageSource(msgPtr), error.code, error.message);
            }
        });
    }
    
    /**
     * Disconnect the listener for error messages.
     * 
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(ERROR listener) {
        super.disconnect(ERROR.class, listener);
    }
    
    /**
     * Add a listener for warning messages.
     * 
     * @param listener The listener to be called when an {@link Element} emits a warning.
     */
    public void connect(final WARNING listener) {
        connect("sync-message::warning", WARNING.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Pointer busPtr, Pointer msgPtr, Pointer user_data) {
                PointerByReference err = new PointerByReference();
                gst.gst_message_parse_warning(msgPtr, err, null);                
                GErrorStruct error = new GErrorStruct(err.getValue());
                listener.warningMessage(messageSource(msgPtr), error.code, error.message);
                glib.g_error_free(err.getValue());
            }
        });
    }
    
    /**
     * Disconnect the listener for warning messages.
     * 
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(WARNING listener) {
        super.disconnect(WARNING.class, listener);
    }
    
    /**
     * Add a listener for informational messages.
     * 
     * @param listener The listener to be called when an {@link Element} emits a an informational message.
     */
    public void connect(final INFO listener) {
        connect("sync-message::info", INFO.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Pointer busPtr, Pointer msgPtr, Pointer user_data) {
                PointerByReference err = new PointerByReference();
                gst.gst_message_parse_info(msgPtr, err, null);                
                GErrorStruct error = new GErrorStruct(err.getValue());
                listener.infoMessage(messageSource(msgPtr), error.code, error.message);
                glib.g_error_free(err.getValue());
            }
        });
    }
    
    /**
     * Disconnect the listener for informational messages.
     * 
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(INFO listener) {
        super.disconnect(INFO.class, listener);
    }
    
    /**
     * Add a listener for {@link State} changes in the Pipeline.
     * 
     * @param listener The listener to be called when the Pipeline changes state.
     */
    public void connect(final STATE_CHANGED listener) {
        connect("sync-message::state-changed", STATE_CHANGED.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Pointer busPtr, Pointer msgPtr, Pointer user_data) {
                IntByReference o = new IntByReference();
                IntByReference n = new IntByReference();
                IntByReference p = new IntByReference();
                gst.gst_message_parse_state_changed(msgPtr, o, n, p);
                listener.stateMessage(messageSource(msgPtr), State.valueOf(o.getValue()),
                        State.valueOf(n.getValue()), State.valueOf(p.getValue()));
            }
        });
    }
    /**
     * Disconnect the listener for {@link State} change messages.
     * 
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(STATE_CHANGED listener) {
        super.disconnect(STATE_CHANGED.class, listener);
    }
    /**
     * Add a listener for new media tags.
     * 
     * @param listener The listener to be called when new media tags are found.
     */
    public void connect(final TAG listener) {
        connect("sync-message::tag", TAG.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Pointer busPtr, Pointer msgPtr, Pointer user_data) {
                PointerByReference list = new PointerByReference();
                gst.gst_message_parse_tag(msgPtr, list);
                listener.tagMessage(messageSource(msgPtr), new TagList(TagList.initializer(list.getValue(), true, false)));
            }
        });
    }
    
    /**
     * Disconnect the listener for tag messages.
     * 
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(TAG listener) {
        super.disconnect(TAG.class, listener);
    }
    
    /**
     * Add a listener for {@link BUFFERING} messages in the Pipeline.
     * 
     * @param listener The listener to be called when the Pipeline buffers data.
     */
    public void connect(final BUFFERING listener) {
        connect("sync-message::buffering", BUFFERING.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Pointer busPtr, Pointer msgPtr, Pointer user_data) {
                IntByReference percent = new IntByReference(0);
                gst.gst_message_parse_buffering(msgPtr, percent);
                listener.bufferingMessage(messageSource(msgPtr), percent.getValue());
            }
        });
    }
    
    /**
     * Disconnect the listener for buffering messages.
     * 
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(BUFFERING listener) {
        super.disconnect(BUFFERING.class, listener);
    }
    
    /**
     * Add a listener for duration changes.
     * 
     * @param listener The listener to be called when the duration changes.
     */
    public void connect(final DURATION listener) {
        connect("sync-message::duration", DURATION.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Pointer busPtr, Pointer msgPtr, Pointer user_data) {
                System.out.println("duration update");
                IntByReference format = new IntByReference(0);
                LongByReference duration = new LongByReference(0);
                gst.gst_message_parse_duration(msgPtr, format, duration);
                listener.durationMessage(messageSource(msgPtr), 
                        Format.valueOf(format.getValue()), duration.getValue());
            }
        });
    }
    /**
     * Disconnect the listener for duration change messages.
     * 
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(DURATION listener) {
        super.disconnect(DURATION.class, listener);
    }
    public void connect(final SEGMENT_START listener) {
        connect("sync-message::segment-start", SEGMENT_START.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Pointer busPtr, Pointer msgPtr, Pointer user_data) {
                IntByReference format = new IntByReference(0);
                LongByReference position = new LongByReference(0);
                gst.gst_message_parse_segment_start(msgPtr, format, position);
                listener.segmentStart(messageSource(msgPtr), 
                        Format.valueOf(format.getValue()), position.getValue());
            }
        });
    }
    public void disconnect(SEGMENT_START listener) {
        super.disconnect(SEGMENT_START.class, listener);
    }
    public void connect(final SEGMENT_DONE listener) {
        connect("sync-message::segment-done", SEGMENT_DONE.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Pointer busPtr, Pointer msgPtr, Pointer user_data) {
                IntByReference format = new IntByReference(0);
                LongByReference position = new LongByReference(0);
                gst.gst_message_parse_segment_done(msgPtr, format, position);
                listener.segmentDone(messageSource(msgPtr), 
                        Format.valueOf(format.getValue()), position.getValue());
            }
        });
    }
    public void disconnect(SEGMENT_DONE listener) {
        super.disconnect(SEGMENT_DONE.class, listener);
    }
    public void setSyncHandler(BusSyncHandler handler) {
        syncHandler = handler;
    }

    private BusSyncHandler syncHandler = new BusSyncHandler() {
        public BusSyncReply syncMessage(Message msg) {
            return BusSyncReply.PASS;
        }
    };
    private static GstCallback syncCallback = new GstCallback() {
        @SuppressWarnings("unused")
        public int callback(Pointer busPtr, Pointer msgPtr, Pointer data) {
            Bus bus = (Bus) NativeObject.instanceFor(busPtr);
            //
            // If the Bus proxy has been disposed, just ignore
            //
            if (bus == null) {
                return BusSyncReply.PASS.intValue();
            }
            // Manually manage the refcount here
            Message msg = new Message(msgPtr, false, false);            
            BusSyncReply reply = bus.syncHandler.syncMessage(msg);
            
            //
            // If the message is to be dropped, unref it, otherwise it needs to 
            // keep its ref to be passed on
            //
            if (reply == BusSyncReply.DROP) {
                gst.gst_mini_object_unref(msg);
            }
            return reply.intValue();
        }
    };
    
    private final static GstObject messageSource(Pointer msgPtr) {
        return Element.objectFor(new MessageStruct(msgPtr).src, true);
    }
    
    private Map<BusListener, BusListenerProxy> listeners
            = Collections.synchronizedMap(new HashMap<BusListener, BusListenerProxy>());
}
class BusListenerProxy implements Bus.EOS, Bus.STATE_CHANGED, Bus.ERROR, Bus.WARNING, 
        Bus.INFO, Bus.TAG, Bus.BUFFERING, Bus.DURATION, Bus.SEGMENT_START, Bus.SEGMENT_DONE {
    public BusListenerProxy(Bus bus, final BusListener listener) {
        this.bus = bus;
        this.listener = listener;
        bus.connect((Bus.EOS) this);
        bus.connect((Bus.STATE_CHANGED) this);
        bus.connect((Bus.ERROR) this);
        bus.connect((Bus.WARNING) this);
        bus.connect((Bus.INFO) this);
        bus.connect((Bus.TAG) this);
        bus.connect((Bus.BUFFERING) this);
        bus.connect((Bus.DURATION) this);
        bus.connect((Bus.SEGMENT_START) this);
        bus.connect((Bus.SEGMENT_DONE) this);
    }
    public void eosMessage(GstObject source) {
        listener.eosEvent();
    }
    public void stateMessage(GstObject source, State old, State current, State pending) {
        listener.stateEvent(new StateEvent(source, old, current, pending));
    }
    public void errorMessage(GstObject source, int code, String message) {
        listener.errorEvent(new ErrorEvent(source, code, message));
    }
    public void warningMessage(GstObject source, int code, String message) {
        listener.warningEvent(new ErrorEvent(source, code, message));
    }
    public void infoMessage(GstObject source, int code, String message) {
        listener.infoEvent(new ErrorEvent(source, code, message));
    }
    public void tagMessage(GstObject source, TagList tagList)  {
        listener.tagEvent(tagList);
    }
    public void bufferingMessage(GstObject source, int percent) {
        listener.bufferingEvent(percent);
    }
    public void durationMessage(GstObject source, Format format, long duration) {
        listener.durationEvent(format, duration);
    }
    public void segmentStart(GstObject source, Format format, long position) {
        listener.segmentStart(format, position);
    }
    public void segmentDone(GstObject source, Format format, long position) {
        listener.segmentDone(format, position);
    }
    public void disconnect() {
        bus.disconnect((Bus.EOS) this);
        bus.disconnect((Bus.STATE_CHANGED) this);
        bus.disconnect((Bus.ERROR) this);
        bus.disconnect((Bus.WARNING) this);
        bus.disconnect((Bus.INFO) this);
        bus.disconnect((Bus.TAG) this);
        bus.disconnect((Bus.BUFFERING) this);
        bus.disconnect((Bus.SEGMENT_START) this);
        bus.disconnect((Bus.SEGMENT_DONE) this);
    }
    private Bus bus;
    private BusListener listener;

}
