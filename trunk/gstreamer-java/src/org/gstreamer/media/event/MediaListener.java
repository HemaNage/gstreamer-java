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

package org.gstreamer.media.event;

/**
 *
 * @author wayne
 */
public interface MediaListener {
    void pause(StopEvent evt);
    void start(StartEvent evt);
    void stop(StopEvent evt);
    void endOfMedia(EndOfMediaEvent evt);
    void positionChanged(PositionChangedEvent evt);
    void durationChanged(DurationChangedEvent evt);
}
