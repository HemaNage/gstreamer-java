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

import java.util.logging.Logger;
import static org.gstreamer.lowlevel.GstAPI.gst;

/**
 * Base class for contents of a {@link Plugin}
 *
 * This is a base class for anything that can be added to a Plugin.
 * @see Plugin
 */
public class PluginFeature extends GstObject {
    private static Logger logger = Logger.getLogger(PluginFeature.class.getName());
    
    /** Creates a new instance of PluginFeature */
    public PluginFeature(Initializer init) { 
        super(init); 
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    /**
     *  Gets the name of a plugin feature.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return gst.gst_plugin_feature_get_name(this);
    }
    
    /**
     * Sets the name of a plugin feature. The name uniquely identifies a feature
     * within all features of the same type. Renaming a plugin feature is not
     * allowed.
     * @param name The name to set.
     */
    @Override
    public void setName(String name) {
        gst.gst_plugin_feature_set_name(this, name);
    }
    
    /**
     * Set the rank for the plugin feature.
     * Specifies a rank for a plugin feature, so that autoplugging uses
     * the most appropriate feature.
     * @param rank The rank value - higher number means more priority rank
     */
    public void setRank(int rank) {
        gst.gst_plugin_feature_set_rank(this, rank);
    }
    
    /**
     * Gets the rank of a plugin feature.
     *
     * @return The rank of the feature.
     */
    public int getRank() {
        return gst.gst_plugin_feature_get_rank(this);
    }
    
    /**
     * Checks whether the given plugin feature is at least the required version.
     *
     * @param major Minimum required major version
     * @param minor Minimum required minor version
     * @param micro Minimum required micro version
     * @return true if the plugin feature has at least the required version, otherwise false.
     */
    public boolean checkVersion(int major, int minor, int micro) {
        return gst.gst_plugin_feature_check_version(this, minor, minor, micro);
    }
}
