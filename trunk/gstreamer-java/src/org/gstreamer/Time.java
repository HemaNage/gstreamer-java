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

/**
 *
 */
public class Time {
    
    /**
     * Creates a new instance of Time
     * 
     * @param nanoseconds The length of time this object represents, in nanoseconds.
     */
    public Time(long nanoseconds) {
        this.nanoseconds = nanoseconds;
    }
    
    /**
     * Get the hours component of the total time.
     * 
     * @return The hours component of the total time.
     */
    public long getHours() {
        return (longValue() / NANOSECONDS / 3600) % 24;
    }
    
    /**
     * Get the minutes component of the total time.
     * 
     * @return The minutes component of the total time.
     */
    public long getMinutes() {
        return (longValue() / NANOSECONDS / 60) % 60;
    }
    
    /**
     * Get the seconds component of the total time.
     * 
     * @return The seconds component of the total time.
     */
    public long getSeconds() {
        return (longValue() / NANOSECONDS) % 60;
    }
    
    /**
     * Get the nanosecond component of the total time.
     * 
     * @return The nanoseconds component of the total time.
     */
    public long getNanoSeconds() {
        return nanoseconds % NANOSECONDS;
    }
    
    /**
     * Get the native GstTime represented by this object.
     * 
     * @return The length of time this object represents, in nanoseconds.
     */
    public long longValue() {
        return nanoseconds;
    }

    public String toString() {
        return String.format("%02d:%02d:%02d", getHours(), getMinutes(), getSeconds());
    }
    public static final long NANOSECONDS = 1000000000L;
    private long nanoseconds;
}
