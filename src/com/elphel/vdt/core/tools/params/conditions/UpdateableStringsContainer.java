/*******************************************************************************
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of Eclipse/VDT plug-in.
 * Eclipse/VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * Eclipse/VDT plug-in is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Eclipse VDT plug-in; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/
package com.elphel.vdt.core.tools.params.conditions;

import java.util.Iterator;
import java.util.List;

import com.elphel.vdt.core.tools.Updateable;
import com.elphel.vdt.core.tools.config.ConfigException;

public abstract class UpdateableStringsContainer implements Updateable {
    private final static String FIRST_MARK = "first";
    
    protected ConditionalStringsList strings;
    protected ConditionalStringsList deleteStrings;
    protected List<NamedConditionalStringsList> insertStrings;
    
    public UpdateableStringsContainer(ConditionalStringsList strings,
                                      ConditionalStringsList deleteStrings,
                                      List<NamedConditionalStringsList> insertStrings) 
    {
        this.strings = strings;
        this.deleteStrings = deleteStrings;
        this.insertStrings = insertStrings;
    }
    
    public abstract boolean matches(Updateable other);
    public abstract Object clone();

    public void update(Updateable from) throws ConfigException {
        UpdateableStringsContainer container = (UpdateableStringsContainer)from;
        
        ConditionalStringsList oldStrings = 
            (ConditionalStringsList)container.strings.clone();
        
        if(strings != null)
            oldStrings.add(strings);

        strings = oldStrings; 
        
        insertSpecifiedStrings();
        deleteSpecifiedStrings();        
    }
    
    private void insertSpecifiedStrings() throws ConfigException {
        if(insertStrings == null)
            return;
        
        for(NamedConditionalStringsList insert : insertStrings) {
            String after = insert.getName();
            
            if(after.equals(FIRST_MARK)) {
                strings.add(0, 0, insert);
                continue;
            }
            
            FindAfter:
                
            // try to find line 'after' in the strings list, with condition that is true
            for(int entryIndex = 0; entryIndex < strings.getEntries().size(); entryIndex++) {
                ConditionalStringsList.Entry entry = strings.getEntries().get(entryIndex);
            
                for(int listIndex = 0; listIndex < entry.getStrings().size(); listIndex++) {
                    String s = entry.getStrings().get(listIndex);
                    
                    if(!after.equals(s))
                        continue;
                                                            
                    Condition cond = entry.getCondition();
                    
                    if(cond != null && !cond.isTrue())
                        continue;

                    // okay, entry found, and the condition is met
                    // now, apply the found condition and insert the
                    // strings at the found position
                    insert.strengthenConditions(cond);
                    strings.add(entryIndex, listIndex+1, insert);
                    
                    break FindAfter;
                }
            }
        }
    }
    
    private void deleteSpecifiedStrings() throws ConfigException {
        if(deleteStrings == null)
            return;
        
        for(ConditionalStringsList.Entry delStrings : deleteStrings.getEntries()) {
            for(ConditionalStringsList.Entry thisStrings : strings.getEntries()) {
                if(!ConditionUtils.conditionsEqual(thisStrings.getCondition(), 
                                                   delStrings.getCondition()))
                {
                    continue;
                }
                
                FindLineToDelete: 
                    
                for(String lineToDelete : delStrings.getStrings()) {
                    for(Iterator<String> foundLineToDelete = thisStrings.getStrings().iterator();
                        foundLineToDelete.hasNext();)
                    {
                        if((foundLineToDelete.next().equals(lineToDelete))) {
                            foundLineToDelete.remove();
                            break FindLineToDelete;
                        }
                    }
                        
                    //throw new ConfigException("Line '" + lineToDelete + 
                    //                          "' specied in the 'delete' list of context '" + contextName +
                    //                          "' doesn't exist");
                }
            }                
        }
    }
}
