/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of VDT plug-in.
 * VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VDT plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Additional permission under GNU GPL version 3 section 7:
 * If you modify this Program, or any covered work, by linking or combining it
 * with Eclipse or Eclipse plugins (or a modified version of those libraries),
 * containing parts covered by the terms of EPL/CPL, the licensors of this
 * Program grant you additional permission to convey the resulting work.
 * {Corresponding Source for a non-source form of such a combination shall
 * include the source code for the parts of Eclipse or Eclipse plugins used
 * as well as that of the covered work.}
 *******************************************************************************/
package com.elphel.vdt.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Model of list of string entries. 
 * 
 * Created: 08.01.2006
 * @author  Lvov Konstantin
 */

public class ListPromptModel {

    private List<Entry> entries = new ArrayList<Entry>();
    private List<Listener> listeners = new ArrayList<Listener>();
    
    public ListPromptModel() {}

    public ListPromptModel(List<String> initialValues) {
    	if (initialValues != null) {
            for (Iterator i = initialValues.iterator(); i.hasNext(); ) {
            	String value = (String)i.next();
            	addEntry(value);
            }
    	}
    }
	
    public List<String> getList() {
    	List<String> list = new ArrayList<String>(entries.size());
        for (Iterator i = entries.iterator(); i.hasNext(); ) {
        	Entry entry = (Entry)i.next();
        	list.add(entry.getValue());
        }
        return list;
    }

    public List<Entry> getEntries() {
        return entries;
    }
    
    /**
     *  Add a new list entry 
     */
    public void addEntry(String value) {
        Entry entry = new Entry(value);
        entries.add(entry);
        for (int i = 0; i < listeners.size(); i++)
            ((Listener)listeners.get(i)).entryAdded(entry);
    }
    
    /**
     * Remove specified list entry
     */
    public void removeEntry(Entry entry) {
        entries.remove(entry);
        for (int i = 0; i < listeners.size(); i++)
            ((Listener)listeners.get(i)).entryRemoved(entry);
    }
    
    /**
     * Notify listener of changes of specified list entry
     */
    public void updateEntry(Entry entry) {
        for (int i = 0; i < listeners.size(); i++)
            ((Listener)listeners.get(i)).entryChanged(entry);
    }
        
    public void moveUpEntry(Entry entry) {
        int position = entries.indexOf(entry);
        if (position > 0) {
            removeEntry(entry);
            position--;
            entries.add(position, entry);
            for (int i = 0; i < listeners.size(); i++)
                ((Listener)listeners.get(i)).entryInserted(entry, position);
        }
    } // moveUpPort()
    
    public void moveDownEntry(Entry entry) {
        int position = entries.indexOf(entry);
        if ((position >= 0) && (position != entries.size()-1)) {
            removeEntry(entry);
            position++;
            entries.add(position, entry);
            for (int i = 0; i < listeners.size(); i++)
                ((Listener)listeners.get(i)).entryInserted(entry, position);
        }
    } // moveDownPort()
    
    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    public void removeAllListener() {
        listeners.clear();
    }
        
    public interface Listener {
        public void entryAdded(Entry entry);
        public void entryRemoved(Entry entry);
        public void entryChanged(Entry entry);
        public void entryInserted(Entry entry, int position);
    } // interface Listener
	
    //-------------------------------------------------------------------------
    //                         list promt entry
	//-------------------------------------------------------------------------
	class Entry {
	    private String value;
	    
	    public Entry(String value) {
	    	this.value = value;
	    }
	    
	    public void setValue(String value) {
	    	this.value = value;
	    }

	    public String getValue() {
	    	return value;
	    }
	    
	    public String toString() {
	    	return value;
	    }
	} // class Entry
	
} // class ListPromptModel
