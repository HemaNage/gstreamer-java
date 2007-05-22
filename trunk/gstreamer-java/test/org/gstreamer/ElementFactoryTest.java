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
public class ElementFactoryTest {
    
    public ElementFactoryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
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
    public void testMakeFakesrc() {
        Element e = ElementFactory.make("fakesrc", "source");
        assertNotNull("Failed to create fakesrc", e);
    }
    @Test
    public void testMakeBin() {
        Element e = ElementFactory.make("bin", "bin");
        assertNotNull("Failed to create bin", e);
        assertTrue("Element not a subclass of Bin", e instanceof Bin);
    }
    @Test
    public void testMakePipeline() {
        Element e = ElementFactory.make("pipeline", "bin");
        assertNotNull("Failed to create pipeline", e);
        assertTrue("Element not a subclass of Bin", e instanceof Bin);
        assertTrue("Element not a subclass of Pipeline", e instanceof Pipeline);
    }
    //@Test
    // Doesn't work yet
    public void testMakePlaybin() {
        Element e = ElementFactory.make("playbin", "bin");
        assertNotNull("Failed to create playbin", e);
        assertTrue("Element not a subclass of Bin", e instanceof Bin);
        assertTrue("Element not a subclass of Pipeline", e instanceof Pipeline);
        assertTrue("Element not a subclass of PlayBin", e instanceof PlayBin);
    }
    @Test
    public void testCreateFakesrc() {
        ElementFactory factory = ElementFactory.find("fakesrc");
        assertNotNull("Could not locate fakesrc factory", factory);
        Element e = factory.create("source");
        assertNotNull("Failed to create fakesrc", e);
    }
    @Test
    public void testCreateBin() {
        ElementFactory factory = ElementFactory.find("bin");
        assertNotNull("Could not locate bin factory", factory);
        Element e = factory.create("bin");
        assertNotNull("Failed to create bin", e);
        assertTrue("Element not a subclass of Bin", e instanceof Bin);
    }
    @Test
    public void testCreatePipeline() {
        ElementFactory factory = ElementFactory.find("pipeline");
        assertNotNull("Could not locate pipeline factory", factory);
        Element e = factory.create("bin");
        assertNotNull("Failed to create pipeline", e);
        assertTrue("Element not a subclass of Bin", e instanceof Bin);
        assertTrue("Element not a subclass of Pipeline", e instanceof Pipeline);
    }
    //@Test
    // Does not work yet
    public void testCreatePlaybin() {
        ElementFactory factory = ElementFactory.find("playbin");
        assertNotNull("Could not locate pipeline factory", factory);
        Element e = factory.create("bin");
        assertNotNull("Failed to create playbin", e);
        assertTrue("Element not a subclass of Bin", e instanceof Bin);
        assertTrue("Element not a subclass of Pipeline", e instanceof Pipeline);
        assertTrue("Element not a subclass of PlayBin", e instanceof PlayBin);
    }
}
