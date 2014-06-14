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

import java.util.Iterator;
import java.util.List;

import com.elphel.vdt.core.tools.Updateable;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.params.FormatProcessor;

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

    public void update(Updateable from, FormatProcessor topProcessor) throws ConfigException {
        UpdateableStringsContainer container = (UpdateableStringsContainer)from;
        
        ConditionalStringsList oldStrings = 
            (ConditionalStringsList)container.strings.clone();
        
        if(strings != null)
            oldStrings.add(strings);

        strings = oldStrings; 
        
        insertSpecifiedStrings(topProcessor);
        deleteSpecifiedStrings();        
    }
    
    private void insertSpecifiedStrings(FormatProcessor topProcessor) throws ConfigException {
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
                    
                    if(cond != null && !cond.isTrue(topProcessor))
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
