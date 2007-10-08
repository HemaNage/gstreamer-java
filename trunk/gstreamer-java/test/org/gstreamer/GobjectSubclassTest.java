/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.gstreamer;

import com.sun.jna.Pointer;
import org.gstreamer.lowlevel.BaseAPI;
import org.gstreamer.lowlevel.GObjectAPI;
import org.gstreamer.lowlevel.GObjectAPI.GClassInitFunc;
import org.gstreamer.lowlevel.GObjectAPI.GInstanceInitFunc;
import org.gstreamer.lowlevel.GObjectAPI.GTypeInstance;
import org.gstreamer.lowlevel.GType;
import org.gstreamer.lowlevel.GstAPI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wayne
 */
public class GobjectSubclassTest {
    
    public GobjectSubclassTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
//        GObjectAPI.gobj.g_type_init_with_debug_flags(1 << 0);
        Gst.init("test", new String[] {});
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    @Test 
    public void registerNewGObjectClass() throws Exception {
        final GObjectAPI gobj = GObjectAPI.gobj;
        final GstAPI gst = GstAPI.gst;        
        
        final PadTemplate template = new PadTemplate("src", PadDirection.SRC, 
                Caps.anyCaps());
        final boolean[] classInitCalled  = { false };
        final GClassInitFunc classInit = new GClassInitFunc() {

            public void callback(Pointer g_class, Pointer class_data) {
                classInitCalled[0] = true;
            }
        };
        final GObjectAPI.GBaseInitFunc baseInit = new GObjectAPI.GBaseInitFunc() {

            public void callback(Pointer g_class) {
                gst.gst_element_class_add_pad_template(g_class, template);                    
            }
        };
        final boolean[] instanceInitCalled  = { false };
        final GInstanceInitFunc instanceInit = new GInstanceInitFunc() {

            public void callback(GTypeInstance instance, Pointer g_class) {
                instanceInitCalled[0] = true;                
            }
        };
        final String name = "NewTestClass";

        GObjectAPI.GTypeInfo info = new GObjectAPI.GTypeInfo();
        info.clear();
        info.class_init = classInit;
        info.instance_init = instanceInit;
        info.class_size = (short)new BaseAPI.GstBaseSrcClass().size();
        info.instance_size = (short)new BaseAPI.GstBaseSrcStruct().size();
        info.class_size = 1024;
        info.base_init = baseInit;
        info.instance_size = 1024;        
        
        GType type = gobj.g_type_register_static(BaseAPI.INSTANCE.gst_base_src_get_type(), 
                name, info, 0);
        System.out.println("New type=" + type);
        assertEquals("Name incorrect", name, gobj.g_type_name(type));
        assertEquals("Cannot locate type by name", type, gobj.g_type_from_name(name));
        
        //Pointer instance = gobj.g_type_create_instance(type);
        gobj.g_object_new(type, new Object[0]);
        
    }
}
