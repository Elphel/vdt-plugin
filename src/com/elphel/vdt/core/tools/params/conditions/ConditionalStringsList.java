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
package com.elphel.vdt.core.tools.params.conditions;

import java.util.*;

public class ConditionalStringsList implements Cloneable {
    public static class Entry {
        private Condition condition;
        private List<String> strings;
        
        Entry(Condition condition, List<String> strings) {
            this.condition = condition;
            this.strings = strings;
        }
        
        public Condition getCondition() {
            return condition;
        }

        public List<String> getStrings() {
            return strings;
        }
        
        public void setCondition(Condition condition) {
            this.condition = condition;
        }
    }
    
    private List<Entry> entries = new ArrayList<Entry>();
    
    public ConditionalStringsList() {        
    }
    
    protected ConditionalStringsList(List<Entry> entries) {
        this.entries = entries;
    }
    
    public List<Entry> getEntries() {
        return entries; 
    }
    
    public boolean isEmpty() {
        return entries.isEmpty();
    }
    
    public void add(Condition condition, List<String> strings) {
        entries.add(new Entry(condition, strings));
    }
    
    public void add(ConditionalStringsList list) {
        entries.addAll(list.entries);
    }
    
    public void add(int entryIndex, int listIndex, ConditionalStringsList list) {
        if(entryIndex > entries.size() || entryIndex < 0)
            throw new IndexOutOfBoundsException("Entry index: " + entryIndex + 
                                                ", Size: " + entries.size());

        Entry entry = entries.get(entryIndex);
        List<String> strings = entry.getStrings();
        
        if(listIndex > strings.size() || listIndex < 0)
            throw new IndexOutOfBoundsException("List entry index: " + listIndex + 
                                                ", List size: " + strings.size());
        
        List<String> stringsHead = new ArrayList<String>(strings.subList(0, listIndex));
        List<String> stringsTail = new ArrayList<String>(strings.subList(listIndex, strings.size()));

        int index = entryIndex;
        entries.set(index, new Entry(entry.getCondition(), stringsHead));
        
        index = entryIndex + 1;
        entries.addAll(index, list.getEntries());

        index = entryIndex + list.getEntries().size() + 1;
        entries.add(index, new Entry(entry.getCondition(), stringsTail));
    }
    
    public void strengthenConditions(Condition addCondition) {
        if(addCondition == null)
            return;
        
        for(Entry entry : entries) {
            Condition cond = entry.getCondition();
            
            if(cond == null)
                entry.setCondition(addCondition);
            else
                entry.setCondition(new Condition(Condition.BOOL_OP.AND,
                                                 addCondition,
                                                 cond));
        }
    }
    
    public Object clone() {
        return new ConditionalStringsList(new ArrayList<Entry>(entries));
    }
}
