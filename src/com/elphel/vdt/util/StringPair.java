/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of Eclipse/VDT plug-in.
 * Eclipse/VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Eclipse/VDT plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.elphel.vdt.util;

public class StringPair {
    private String first;
    private String second;

    public StringPair(String first, String second) {
        this.first = first;
        this.second = second;
    }

    public boolean equals(Object obj) {
        if(this == obj)
            return true;

        if(!(obj instanceof StringPair))
            return false;

        StringPair other = (StringPair)obj;

        return first.equals(other.first) && second.equals(other.second);
    }

    public int hashCode() {
        return first.hashCode() ^ second.hashCode();
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }
}
