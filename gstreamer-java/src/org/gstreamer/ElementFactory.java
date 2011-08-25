/* 
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gstreamer.lowlevel.GlibAPI;
import org.gstreamer.lowlevel.GlibAPI.GList;
import org.gstreamer.lowlevel.GstCapsAPI;
import org.gstreamer.lowlevel.GstElementFactoryAPI;
import org.gstreamer.lowlevel.GstNative;
import org.gstreamer.lowlevel.GstPadTemplateAPI;
import org.gstreamer.lowlevel.GstPadTemplateAPI.GstStaticPadTemplate;
import org.gstreamer.lowlevel.GstPluginAPI;
import org.gstreamer.lowlevel.GstTypes;
import org.gstreamer.lowlevel.NativeObject;

import com.sun.jna.Pointer;

/**
 * ElementFactory is used to create instances of elements.
 * 
 * Use the {@link #find} and {@link #create} methods to create element instances 
 * or use {@link #make} as a convenient shortcut.
 *
 */
public class ElementFactory extends PluginFeature {
    private static Logger logger = Logger.getLogger(ElementFactory.class.getName());
    private static Level DEBUG = Level.FINE;

    private static final Map<String, Class<? extends Element>> typeMap
        = new HashMap<String, Class<? extends Element>>();

    public static final String GTYPE_NAME = "GstElementFactory";
    
    private static interface API extends GstElementFactoryAPI, GstCapsAPI, GstPadTemplateAPI, GstPluginAPI, GlibAPI {}
    private static final API gst = GstNative.load(API.class);
    
    /**
     * Register a new class into the typeMap.
     */    
    public static void registerElement(Class<? extends Element> klass, String name) {
    	if (!typeMap.containsKey(name))
    		typeMap.put(name, klass);
    }
    
    /**
     * Retrieve an instance of a factory that can produce {@link Element}s
     * 
     * @param name The type of {@link Element} to produce.
     * @return An ElementFactory that will produce {@link Element}s of the 
     * desired type.
     */
    public static ElementFactory find(String name) {
        logger.entering("ElementFactory", "find", name);
        ElementFactory factory = gst.gst_element_factory_find(name);
        if (factory == null) {
            throw new IllegalArgumentException("No such Gstreamer factory: " + name);
        }        
        return factory;
    }
    
    /**
     * Creates a new Element from the specified factory.
     *
     * @param factoryName The name of the factory to use to produce the Element
     * @param name The name to assign to the created Element
     * @return A new GstElemElement
     */
    public static Element make(String factoryName, String name) {        
        logger.entering("ElementFactory", "make", new Object[] { factoryName, name});
        return elementFor(makeRawElement(factoryName, name), factoryName);
    }

    /**
     * Get a list of factories that match the given type. Only elements with a
     * rank greater or equal to minrank will be returned. The list of factories
     * is returned by decreasing rank.
     * 
     * @param type
     *            a {@link ElementFactoryListType}
     * @param minrank
     *            Minimum rank
     * @return a List of ElementFactory elements.
     */
    static List<ElementFactory> listGetElement(ElementFactoryListType type, Rank minrank) {
        GList glist = gst.gst_element_factory_list_get_elements(type.getValue(), minrank.getValue());
        List<ElementFactory> list = new ArrayList<ElementFactory>();

        GList next = glist;
        while (next != null) {
            if (next.data != null) {
                ElementFactory fact = new ElementFactory(initializer(next.data, true, true));
                list.add(fact);
            }
            next = next.next();
        }

        gst.gst_plugin_list_free(glist);

        return list;
    }

    /**
     * Filter out all the elementfactories in list that can handle caps in the
     * given direction.
     * 
     * If subsetonly is true, then only the elements whose pads templates are a
     * complete superset of caps will be returned. Else any element whose pad
     * templates caps can intersect with caps will be returned.
     * 
     * @param list
     *            a {@link List} of {@link ElementFactory} to filter
     * @param caps
     *            a {@link Caps}
     * @param direction
     *            a {@link PadDirection} to filter on
     * @param subsetonly
     *            whether to filter on caps subsets or not.
     * @return a {@link List} of {@link ElementFactory} elements that match the
     *         given requisits.
     */
    static List<ElementFactory> listFilter(List<ElementFactory> list, Caps caps,
            PadDirection direction, boolean subsetonly) {
        GList glist = null;
        List<ElementFactory> filterList = new ArrayList<ElementFactory>();

        for (ElementFactory fact : list) {
            fact.ref();
            glist = gst.g_list_append(glist, fact.handle());
        }

        GList gFilterList = gst.gst_element_factory_list_filter(glist, caps, direction, subsetonly);

        GList next = gFilterList;
        while (next != null) {
            if (next.data != null) {
                ElementFactory fact = new ElementFactory(initializer(next.data, true, true));
                filterList.add(fact);
            }
            next = next.next();
        }

        gst.gst_plugin_list_free(glist);
        gst.gst_plugin_list_free(gFilterList);

        return filterList;
    }

    /**
     * Get a list of factories that match the given parameter.
     *
     * It is a combination of {@link listGetElement} and {@link listFilter}
     * passing all the results of the first call to the second.
     *
     * This method improves performance because there is no need to map to java
     * list the elements returned by the first call.
     *
     * @param type
     *            a {@link ElementFactoryListType}
     * @param minrank
     *            Minimum rank
     * @param caps
     *            a {@link Caps}
     * @param direction
     *            a {@link PadDirection} to filter on
     * @param subsetonly
     *            whether to filter on caps subsets or not.
     * @return a {@link List} of {@link ElementFactory} elements that match the
     *         given requisits.
     */
    static List<ElementFactory> listGetElementFilter(ElementFactoryListType type, Rank minrank,
            Caps caps, PadDirection direction, boolean subsetonly) {
        List<ElementFactory> filterList = new ArrayList<ElementFactory>();

        GList glist = gst.gst_element_factory_list_get_elements(type.getValue(), minrank.getValue());

        GList gFilterList = gst.gst_element_factory_list_filter(glist, caps, direction, subsetonly);

        GList next = gFilterList;
        while (next != null) {
            if (next.data != null) {
                ElementFactory fact = new ElementFactory(initializer(next.data, true, true));
                filterList.add(fact);
            }
            next = next.next();
        }

        gst.gst_plugin_list_free(glist);
        gst.gst_plugin_list_free(gFilterList);

        return filterList;
    }

    static Pointer makeRawElement(String factoryName, String name) {
        logger.entering("ElementFactory", "makeRawElement", new Object[] { factoryName, name});
        Pointer elem = gst.ptr_gst_element_factory_make(factoryName, name);
        logger.log(DEBUG, "Return from gst_element_factory_make=" + elem);
        if (elem == null) {
            throw new IllegalArgumentException("No such Gstreamer factory: "
                    + factoryName);
        }
        return elem;
    }
    
    @SuppressWarnings("unchecked")
    private static Element elementFor(Pointer ptr, String factoryName) {
        Class<? extends Element> cls = typeMap.get(factoryName);
        cls = (cls == null) ? (Class<Element>)GstTypes.classFor(ptr) : cls;
        cls = (cls == null || !Element.class.isAssignableFrom(cls)) ? Element.class : cls;
        return NativeObject.objectFor(ptr, cls);
    }

	/**
     * Creates a new instance of ElementFactory
     * @param init internal initialization data.
     */
    public ElementFactory(Initializer init) {
        super(init); 
        logger.entering("ElementFactory", "<init>", new Object[] { init });
    }
    
    /**
     * Creates a new element from the factory.
     *
     * @param name the name to assign to the created Element
     * @return A new {@link Element}
     */
    public Element create(String name) {
        logger.entering("ElementFactory", "create", name);
        Pointer elem = gst.ptr_gst_element_factory_create(this, name);
        logger.log(DEBUG, "gst_element_factory_create returned: " + elem);
        if (elem == null) {
            throw new IllegalArgumentException("Cannot create GstElement");
        }
        return elementFor(elem, getName());
    }
    /**
     * Returns the name of the person who wrote the factory.
     * 
     * @return The name of the author
     */
    public String getAuthor() {
        logger.entering("ElementFactory", "getAuthor");
        return gst.gst_element_factory_get_author(this);
    }
    /**
     * Returns a description of the factory.
     * 
     * @return A brief description of the factory.
     */
    public String getDescription() {
        logger.entering("ElementFactory", "getDescription");
        return gst.gst_element_factory_get_description(this);
    }
    /**
     * Returns the long, English name for the factory.
     * 
     * @return The long, English name for the factory.
     */
    public String getLongName() {
        logger.entering("ElementFactory", "getLongName");
        return gst.gst_element_factory_get_longname(this);
    }
    
    /**
     * Returns a string describing the type of factory.
     * This is an unordered list separated with slashes ('/').
     * 
     * @return The description of the type of factory.
     */
    public String getKlass() {
        logger.entering("ElementFactory", "getKlass");
        return gst.gst_element_factory_get_klass(this);
    }
    
    /**
     * Gets the list of {@link StaticPadTemplate} for this factory.
     *
     * @return The list of {@link StaticPadTemplate}
     */
    public List<StaticPadTemplate> getStaticPadTemplates() {
        logger.entering("ElementFactory", "getStaticPadTemplates");
        GList glist = gst.gst_element_factory_get_static_pad_templates(this);
        logger.log(DEBUG, "gst.gst_element_factory_get_static_pad_templates returned: " + glist);
        List<StaticPadTemplate> templates = new ArrayList<StaticPadTemplate>();
        GList next = glist;
        while (next != null) {
            if (next.data != null) {
                GstStaticPadTemplate temp = new GstStaticPadTemplate(next.data);
                templates.add(new StaticPadTemplate(temp.name_template, temp.direction,
                        temp.presence, gst.gst_static_caps_get(temp.static_caps)));
            }
            next = next.next();
        }
        return templates;
    }

    public enum ElementFactoryListType {
        DECODER((long) 1 << 0),
        ENCODER((long) 1 << 1),
        SINK((long)1 << 2),
        SRC((long)1 << 3),
        MUXER((long)1 << 4),
        DEMUXER((long)1 << 5),
        PARSER((long)1 << 6),
        PAYLOADER((long)1 << 7),
        DEPAYLOADER((long)1 << 8),
        FORMATTER((long)1 << 9),
        MAX_ELEMENTS((long)1 << 48),
        ANY((((long)1) << 49) - 1),

        MEDIA_ANY(~((long) 0) << 48),
        MEDIA_VIDEO((long)1 << 49),
        MEDIA_AUDIO((long)1 << 50),
        MEDIA_IMAGE((long)1 << 51),
        MEDIA_SUBTITLE((long)1 << 52),
        MEDIA_METADATA((long)1 << 53),
        VIDEO_ENCODER(ENCODER.getValue() | MEDIA_VIDEO.getValue() | MEDIA_IMAGE.getValue()),
        AUDIO_ENCODER(ENCODER.getValue() | MEDIA_AUDIO.getValue()),
        AUDIOVIDEO_SINKS(SINK.getValue() | MEDIA_AUDIO.getValue() | MEDIA_VIDEO.getValue() | MEDIA_IMAGE.getValue()),
        DECODABLE(ENCODER.getValue() | DEMUXER.getValue() | DEPAYLOADER.getValue() | PARSER.getValue());

        private long value;

        private ElementFactoryListType(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }
}
