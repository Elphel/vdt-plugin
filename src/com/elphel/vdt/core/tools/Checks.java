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
package com.elphel.vdt.core.tools;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.elphel.vdt.core.tools.config.ConfigException;
//TODO: Andrey: Now Parameter has file name
public class Checks {
    public static void checkCyclicInheritance(Inheritable inheritanceChainMember, String entityName)
        throws ConfigException
    {
        List<Inheritable> chain = new ArrayList<Inheritable>();
        Inheritable chainItem = inheritanceChainMember;
        
        while(chainItem != null) {
            if(chain.contains(chainItem)) {
                chain.add(chainItem);            

                String message = "Cyclic " + entityName + " inheritance detected: ";

                for(Iterator<Inheritable> i = chain.iterator(); i.hasNext();) {
                    message += "'" + ((Inheritable)i.next()).getName() + "'";
                    
                    if(i.hasNext())
                        message += " extends ";
                }
                
                throw new ConfigException(message);
            }
            
            chain.add(chainItem);            
            
            chainItem = chainItem.getBase();
        }
    }
    
    public static void checkDuplicatedNames(List<? extends NamedEntity> objlist, 
                                            String entityName)
        throws ConfigException 
    {
        for(int i = 0; i < objlist.size() - 1; i++) {
            NamedEntity obj1 = objlist.get(i);

            for(int j = i+1; j < objlist.size(); j++) {
                NamedEntity obj2 = (NamedEntity)objlist.get(j);

                if(obj1.getName().equals(obj2.getName()))
                    throw new ConfigException("Element '" + obj1.getName() + 
                                              "' is duplicated in " + entityName);
            }            
        }
    }
}
