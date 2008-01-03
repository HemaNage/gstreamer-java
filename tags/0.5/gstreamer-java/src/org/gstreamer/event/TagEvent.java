/* 
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

package org.gstreamer.event;

import org.gstreamer.GstObject;
import org.gstreamer.TagList;

/**
 *
 * @author wayne
 */
public class TagEvent extends java.util.EventObject {
    public TagEvent(GstObject source, TagList tagList) {
        super(source);
        this.tagList = tagList;
    }
    public TagList getTagList() {
        return tagList;
    }
    private final TagList tagList;
}
