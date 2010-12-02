package org.gstreamer.elements.bad;

import org.gstreamer.Element;
import org.gstreamer.Pad;
import org.gstreamer.lowlevel.GstAPI.GstCallback;

import com.sun.jna.Pointer;

public class InputSelector extends Element {
    public InputSelector(Initializer init) {
        super(init);
    }

    /**
     * Block all sink pads in preparation for a switch. 
     * 
     * @return the stop time of the current switch segment, as a running time, or 0 
     * if there is no current active pad or the current active pad never received data.
     */
    public static interface BLOCK {
    	public long block(InputSelector inputselector, Pointer user_data); 
    }
    /**
     * Adds a listener for the <code>block</code> signal
     *
     * @param listener Listener to be called
     */
    public void connect(final BLOCK listener) {
        connect(BLOCK.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public long callback(InputSelector inputselector, Pointer user_data) {
                return listener.block(inputselector, user_data);
            }
        });
    }

    /**
     * If you have a stream with only one switch element, such as an audio-only stream, 
     * a stream switch should be performed by first emitting the block signal, and then 
     * emitting the switch signal with -1 for the stop and start time values.
     * 
     * The intention of the stop_time and start_time arguments is to allow multiple switch 
     * elements to switch and maintain stream synchronization. When switching a stream with 
     * multiple feeds, you will need as many switch elements as you have feeds. For example, 
     * a feed with audio and video will have one switch element between the audio feeds and 
     * one for video.
     * 
     * A switch over multiple switch elements should be performed as follows: First, emit the 
     * block signal, collecting the returned values. The maximum running time returned by block 
     * should then be used as the time at which to close the previous segment.
     * 
     * Then, query the running times of the new audio and video pads that you will switch to. 
     * Naturally, these pads are on separate switch elements. Take the minimum running 
     * time for those streams and use it for the time at which to open the new segment.
     * 
     * If pad is the same as the current active pad, the element will cancel any previous 
     * block without adjusting segments.
     */
    public static interface SWITCH {
    	/**
    	 * The reason to call this function Switch in stead of switch is that "switch" is a keyword 
    	 * in Java and a Java function can't be called switch (so it's not a typo).   
    	 * 
    	 * @param inputselector 
    	 * @param pad the pad to switch to
    	 * @param stop_time running time at which to close the previous segment, or -1 to use 
    	 * 			the running time of the previously active sink pad
    	 * @param start_time running time at which to start the new segment, or -1 to use the 
    	 * 			running time of the newly active sink pad
    	 * @param user_data user data set when the signal handler was connected.
    	 * @return
    	 */
    	public void Switch(InputSelector inputselector, Pad pad, int stop_time, int start_time, Pointer user_data); 
    }
    /**
     * Adds a listener for the <code>switch</code> signal
     *
     * @param listener Listener to be called
     */
    public void connect(final SWITCH listener) {
        connect(SWITCH.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(InputSelector inputselector, Pad pad, int stop_time, int start_time, Pointer user_data) {
                listener.Switch(inputselector, pad, stop_time, start_time, user_data);
            }
        });
    }

}
