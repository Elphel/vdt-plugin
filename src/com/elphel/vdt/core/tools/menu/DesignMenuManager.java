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
package com.elphel.vdt.core.tools.menu;

import java.util.*;

import com.elphel.vdt.core.tools.config.*;
import com.elphel.vdt.core.tools.params.*;
import com.elphel.vdt.core.tools.contexts.*;


public class DesignMenuManager {
    private List<DesignMenu> designMenuComponents = new ArrayList<DesignMenu>();
    private DesignMenu rootDesignMenu;

    private Config config;
    private boolean initialized = false;
    
    private enum REF_CONTEXT { 
        PACKAGE,
        PROJECT
    }
    
    public void init(Config config) throws ConfigException {
        if(initialized)
            throw new ConfigException("Context layer cannot be re-initialized");
    
        this.config = config;
        
        for(Iterator<DesignMenu> m = designMenuComponents.iterator(); m.hasNext();)
            ((DesignMenu)m.next()).init();
        
        initialized = true;
    }

    public void addDesignMenuComponents(List<DesignMenu> menuList) {
        designMenuComponents.addAll(menuList);
    }

    public List<DesignMenu> getDesignMenuList() {
        return designMenuComponents;
    }

    public DesignMenu findDesignMenu(String menuName) {
        for(Iterator<DesignMenu> i = designMenuComponents.iterator(); i.hasNext();) {
            DesignMenu menu = (DesignMenu)i.next();
            
            if(menu.getName().equals(menuName))
                return menu;
        }

        return null;
    }

    public List<Context> getProjectContexts(DesignMenu designMenu) {
        return getReferencedContexts(designMenu, REF_CONTEXT.PROJECT);
    }

    public List<Context> getPackageContexts(DesignMenu designMenu) {
        return getReferencedContexts(designMenu, REF_CONTEXT.PACKAGE);
    }

    public DesignMenu getRootDesignMenu() {
        return rootDesignMenu;
    }

    public List<Context> getReferencedContexts(DesignMenu designMenu, REF_CONTEXT refContexType) {
        List<Context> refContexts = new ArrayList<Context>();
        List<String> toolNames = designMenu.getReferencedToolNames();
        
        for(Iterator<String> i = toolNames.iterator(); i.hasNext();) {
            Tool tool = config.getContextManager().findTool((String)i.next());
            Context parentContext = null;
            
            switch(refContexType) {
                case PACKAGE:
                    parentContext = tool.getParentPackage();
                    break;
                    
                case PROJECT:
                    parentContext = tool.getParentProject();
                    break;
                    
                default:
                    assert false;
            }
            
            if(parentContext != null && !refContexts.contains(parentContext))
                refContexts.add(parentContext);
        }
        
        return refContexts;
    }
}
