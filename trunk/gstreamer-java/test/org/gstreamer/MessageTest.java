/* 
 * Copyright (c) 2009 Levente Farkas
 * Copyright (c) 2008 Wayne Meissner
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.gstreamer.lowlevel.GstMessageAPI;
import org.gstreamer.lowlevel.GstElementAPI;
import org.gstreamer.lowlevel.GstNative;
import org.gstreamer.lowlevel.GstTagListAPI;
import org.gstreamer.message.BufferingMessage;
import org.gstreamer.message.DurationMessage;
import org.gstreamer.message.EOSMessage;
import org.gstreamer.message.LatencyMessage;
import org.gstreamer.message.SegmentDoneMessage;
import org.gstreamer.message.StateChangedMessage;
import org.gstreamer.message.TagMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author wayne
 */
public class MessageTest {
    private GstMessageAPI messageAPI() { return GstNative.load(GstMessageAPI.class); }
    private GstElementAPI elementAPI() { return GstNative.load(GstElementAPI.class); }
    private GstTagListAPI tagListAPI() { return GstNative.load(GstTagListAPI.class); }

    public MessageTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Gst.init("BusTest", new String[] {});
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
      
    @Test public void gst_message_new_eos() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        Message msg = messageAPI().gst_message_new_eos(fakesink);
        assertTrue("gst_message_new_eos did not return an instance of EOSMessage", msg instanceof EOSMessage);
    }
    @Test public void EOSMessage_getSource() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        Message msg = messageAPI().gst_message_new_eos(fakesink);
        assertEquals("Wrong source in message", fakesink, msg.getSource());
    }
    @Test public void postEOS() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<Message>(null);
        pipe.getBus().connect("message::eos", new Bus.MESSAGE() {

            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        elementAPI().gst_element_post_message(pipe.sink, new EOSMessage(pipe.sink));
        pipe.run();

        Message msg = signalMessage.get();
        assertNotNull("No message available on bus", msg);
        assertEquals("Wrong message type", MessageType.EOS, msg.getType());
        assertTrue("Message not intance of EOSMessage", msg instanceof EOSMessage);
        assertEquals("Wrong source in message", pipe.pipe, msg.getSource());
        pipe.dispose();
    }
    @Test public void gst_message_new_percent() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        Message msg = messageAPI().gst_message_new_buffering(fakesink, 55);
        assertTrue("gst_message_new_eos did not return an instance of BufferingMessage", msg instanceof BufferingMessage);
    }
    @Test public void BufferingMessage_getPercent() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        BufferingMessage msg = (BufferingMessage) messageAPI().gst_message_new_buffering(fakesink, 55);
        assertEquals("Wrong source in message", 55, msg.getPercent());
    }
    @Test public void postBufferingMessage() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<Message>(null);
        pipe.getBus().connect("message::buffering", new Bus.MESSAGE() {

            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        final int PERCENT = 55;
        elementAPI().gst_element_post_message(pipe.sink, new BufferingMessage(pipe.src, PERCENT));
        pipe.run();
        Message msg = signalMessage.get();
        assertNotNull("No message available on bus", msg);
        assertEquals("Wrong message type", MessageType.BUFFERING, msg.getType());
        assertTrue("Message not instance of BufferingMessage", msg instanceof BufferingMessage);
        assertEquals("Wrong source in message", pipe.src, msg.getSource());
        assertEquals("Wrong percent value in message", PERCENT, ((BufferingMessage) msg).getPercent());
        pipe.dispose();
    }
    private static final long DURATION = 1234000000;
    @Test public void gst_message_new_duration() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        Message msg = messageAPI().gst_message_new_duration(fakesink, Format.TIME, DURATION);
        assertTrue("gst_message_new_duration did not return an instance of DurationMessage", msg instanceof DurationMessage);
    }
    @Test public void DurationMessage_getDuration() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        DurationMessage msg = (DurationMessage)messageAPI().gst_message_new_duration(fakesink, Format.TIME, DURATION);
        assertEquals("Wrong duration in message", DURATION, msg.getDuration());
    }
    @Test public void postDurationMessage() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<Message>(null);
        pipe.getBus().connect("message::duration", new Bus.MESSAGE() {

            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        elementAPI().gst_element_post_message(pipe.src, new DurationMessage(pipe.src, Format.TIME, DURATION));
        pipe.play().run();
        Message msg = signalMessage.get();
        assertNotNull("No message available on bus", msg);
        assertEquals("Wrong message type", MessageType.DURATION, msg.getType());
        assertTrue("Message not instance of EOSMessage", msg instanceof DurationMessage);
        assertEquals("Wrong source in message", pipe.src, msg.getSource());
        assertEquals("Wrong duration value in message", DURATION, ((DurationMessage) msg).getDuration());
        assertEquals("Wrong duration format in message", Format.TIME, ((DurationMessage) msg).getFormat());
        pipe.dispose();
    }
    @Test public void gst_message_new_tag() {
        Element src = ElementFactory.make("fakesrc", "src");
        Message msg = messageAPI().gst_message_new_tag(src, new TagList());
        assertTrue("gst_message_new_tag did not return an instance of TagMessage", msg instanceof TagMessage);
    }
    @Test public void TagMessage_getTagList() {
        Element src = ElementFactory.make("fakesrc", "src");
        TagList tl = new TagList();
        final String MAGIC = "fubar";
        tagListAPI().gst_tag_list_add(tl, TagMergeMode.APPEND, "artist", MAGIC);
        TagMessage msg = (TagMessage) messageAPI().gst_message_new_tag(src, tl);
        tl = msg.getTagList();
        assertEquals("Wrong artist in tag list", MAGIC, tl.getString("artist", 0));
    }
    @Test public void gst_message_new_state_changed() {
        Element src = ElementFactory.make("fakesrc", "src");
        Message msg = messageAPI().gst_message_new_state_changed(src, State.READY, State.PLAYING, State.VOID_PENDING);
        assertTrue("gst_message_new_state_changed did not return an instance of StateChangedMessage", msg instanceof StateChangedMessage);
    }
    @Test public void constructStateChanged() {
        Element src = ElementFactory.make("fakesrc", "src");
        new StateChangedMessage(src, State.READY, State.PLAYING, State.VOID_PENDING);
    }
    @Test public void StateChanged_get() {
        Element src = ElementFactory.make("fakesrc", "src");
        StateChangedMessage msg = (StateChangedMessage) messageAPI().gst_message_new_state_changed(src, State.READY, State.PLAYING, State.VOID_PENDING);
        assertEquals("Wrong old state", State.READY, msg.getOldState());
        assertEquals("Wrong new state", State.PLAYING, msg.getNewState());
        assertEquals("Wrong pending state", State.VOID_PENDING, msg.getPendingState());
    }
    @Test public void postStateChangedMessage() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<Message>(null);
        
        pipe.getBus().connect("message::state-changed", new Bus.MESSAGE() {

            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        elementAPI().gst_element_post_message(pipe.src, 
                new StateChangedMessage(pipe.src, State.READY, State.PLAYING, State.VOID_PENDING));
        pipe.run();
        Message msg = signalMessage.get();
        assertNotNull("No message available on bus", msg);
        assertEquals("Wrong message type", MessageType.STATE_CHANGED, msg.getType());
        StateChangedMessage smsg = (StateChangedMessage) msg;
        assertEquals("Wrong old state", State.READY, smsg.getOldState());
        assertEquals("Wrong new state", State.PLAYING, smsg.getNewState());
        assertEquals("Wrong pending state", State.VOID_PENDING, smsg.getPendingState());
        pipe.dispose();
    }
    @Test public void gst_message_new_segment_done() {
        Element src = ElementFactory.make("fakesrc", "src");
        Message msg = messageAPI().gst_message_new_segment_done(src, Format.TIME, 0xdeadbeef);
        assertTrue("gst_message_new_segment_done did not return an instance of SegmentDoneMessage", 
                msg instanceof SegmentDoneMessage);
    }
    @Test public void constructSegmentDone() {
        Element src = ElementFactory.make("fakesrc", "src");
        new SegmentDoneMessage(src, Format.TIME, 0xdeadbeef);
    }
    @Test public void parseSegmentDone() {
        Element src = ElementFactory.make("fakesrc", "src");
        SegmentDoneMessage msg = (SegmentDoneMessage) messageAPI().gst_message_new_segment_done(src, Format.TIME, 0xdeadbeef);
        assertEquals("Wrong format", Format.TIME, msg.getFormat());
        assertEquals("Wrong position", 0xdeadbeef, msg.getPosition());
    }
    @Test public void postSegmentDoneMessage() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<Message>(null);
        
        pipe.getBus().connect("message::segment-done", new Bus.MESSAGE() {

            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        final int POSITION = 0xdeadbeef;
        elementAPI().gst_element_post_message(pipe.src, 
                new SegmentDoneMessage(pipe.src, Format.TIME, POSITION));
        pipe.run();
        Message msg = signalMessage.get();
        assertNotNull("No message available on bus", msg);
        assertEquals("Wrong message type", MessageType.SEGMENT_DONE, msg.getType());
        SegmentDoneMessage smsg = (SegmentDoneMessage) msg;
        assertEquals("Wrong format", Format.TIME, smsg.getFormat());
        assertEquals("Wrong position", POSITION, smsg.getPosition());
        pipe.dispose();
    }
    @Test public void postLatencyMessage() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<Message>(null);
        
        pipe.getBus().connect("message::latency", new Bus.MESSAGE() {

            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        elementAPI().gst_element_post_message(pipe.src, 
                new LatencyMessage(pipe.src));
        pipe.run();
        Message msg = signalMessage.get();
        assertNotNull("No message available on bus", msg);
        assertEquals("Wrong message type", MessageType.LATENCY, msg.getType());
        @SuppressWarnings("unused")
        LatencyMessage smsg = (LatencyMessage) msg;
        pipe.dispose();
    }
}