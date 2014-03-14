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
package com.elphel.vdt.core.tools.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import com.elphel.vdt.core.tools.Inheritable;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.EntityUtils;
import com.elphel.vdt.core.tools.Updateable;
import com.elphel.vdt.core.tools.Checks;


public class DesignMenu extends DesignMenuItem implements Inheritable {
    private final static String FIRST_MARK = "first";
    
    private String description;
    private String inheritedMenuName;
    private DesignMenu inheritedMenu;
    private List<DesignMenuItem> items;
    private boolean initialized = false;
    private boolean updated = false;

    private static int nestingInit = 0;

    public DesignMenu(Config config,
                      DesignMenu parentMenu,
                      String inheritedMenuName,
                      String afterItem,
                      String name,
                      String label,
                      String icon,
                      String description,
                      Boolean visible,
                      List<DesignMenuItem> items)
    {
        super(config,
              parentMenu,
              afterItem,
              name,
              label,
              icon,
              visible);
        
        this.description = description;
        this.inheritedMenuName = inheritedMenuName;
        this.items = items;
    }
    
    public void init() throws ConfigException {
        if(initialized)
            return;

//        String spaces = "";
//        for(int i = 0; i < nestingInit; i++)
//            spaces += "  ";
//        System.out.println(spaces + name);
        
        nestingInit++;
        
        super.init();
        
        if(inheritedMenuName != null) {
            if(inheritedMenu == null) {
                inheritedMenu = config.getDesignMenuManager().findDesignMenu(inheritedMenuName);    
    
                if(inheritedMenu == null)
                    throw new ConfigException("Base design menu '" + inheritedMenuName + 
                                              "' of menu '" + name + 
                                              "' is not found");
            
                checkParentInheritance();
                
                Checks.checkCyclicInheritance(this, "design menu");
            
                if(inheritedMenu != null)
                    inheritedMenu.init();
        
                inheritMenu();
            } else {
                if(!updated)
                    throw new ConfigException(
                            "Internal error: menu element '" + name + 
                            (parentMenu != null? "' of design menu '" + parentMenu.getName() : "") +
                            "' is neigher initialized nor updated, but has already inherited menu '" + inheritedMenuName + 
                            "'");
            }
        }

        String commonMessagePart = " of menu '" + name +  
                                   (parentMenu != null? "' in menu '" + parentMenu.getName() : "'") + 
                                   " is absent";
        
        if(label == null)
            throw new ConfigException("Label" + commonMessagePart);
        else if(parentMenu == null && description == null)
            throw new ConfigException("Description" + commonMessagePart);

        initItemsList();
        
        nestingInit--;

        initialized = true;
    }

    public Object clone() {
        ArrayList<DesignMenuItem> newItems = new ArrayList<DesignMenuItem>(items);        
        
        DesignMenu newMenu = new DesignMenu(config,
                                            parentMenu,
                                            afterItem,
                                            inheritedMenuName,
                                            name,
                                            label,
                                            icon,
                                            description,
                                            visible,
                                            newItems);
        
        // we must avoid repeated initialization        
        newMenu.initialized = initialized;
        newMenu.updated = updated;
        
        newMenu.inheritedMenu = inheritedMenu;
        
        return newMenu;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<DesignMenuItem> getItems() {
        return items;
    }

    public String getInheritedMenuName() {
        return inheritedMenuName;
    }
    
    public Inheritable getBase() {
        return inheritedMenu;
    }
    
    public List<String> getReferencedToolNames() {
        List<String> toolList = new ArrayList<String>();
        
        for(Iterator<DesignMenuItem> i = items.iterator(); i.hasNext();) {
            DesignMenuItem it = (DesignMenuItem)i.next();
            
            if(it instanceof DesignMenuToolItem)
                toolList.add(((DesignMenuToolItem)it).getToolName());
            else if(it instanceof DesignMenu)
                toolList.addAll(((DesignMenu)it).getReferencedToolNames());
            else
                assert false;
        }
        
        return toolList;
    }

    private void inheritMenu() throws ConfigException {
        ArrayList<DesignMenuItem> itemsBeforeUpdate = 
            new ArrayList<DesignMenuItem>(items);
        
        inheritItems();
        
        reorderItems(itemsBeforeUpdate);
    }   

    public void update(Updateable other) throws ConfigException {
        DesignMenuItem item = (DesignMenuItem)other;
        
        super.update(item);
        
        if(!(item instanceof DesignMenu))
            throw new ClassCastException("Trying to update design menu from foreign object '" + 
                                         item.getName() + "'");
        
        DesignMenu menuItem = (DesignMenu)item;

        if(inheritedMenuName == null) {
            inheritedMenuName = item.getName();
            inheritedMenu = menuItem;
            
            if(!inheritedMenu.initialized)
                throw new ConfigException(
                        "Internal error: menu element '" + name + 
                        (parentMenu != null? "' of design menu '" + parentMenu.getName() : "") +
                        "' updates menu element '" + item.getName() + 
                        (item.parentMenu != null? "' of design menu '" + item.parentMenu.getName() : "") +
                        "' that is not initialized yet");
            
            inheritMenu();
        } else {
            // we don't need anything to update
        }

        assert menuItem.initialized;
                
        updated = true;
    }

    private void checkParentInheritance() throws ConfigException {
        if(parentMenu == null)
            return;

        DesignMenu forerunner = null, parent = parentMenu;
        
        while(parent != null) {
            assert parent != this;
            
            forerunner = parent;
            
            parent = parent.getParentMenu();
        }
        
        assert forerunner != null;
        
        if(forerunner == inheritedMenu)
            throw new ConfigException("Menu '" + name + 
                                      "' inherits its forerunner menu '" + forerunner.getName() +
                                      "'");
    }
    
    private void initItemsList() throws ConfigException {
        if(items.isEmpty())
            throw new ConfigException("Menu '" + name + "'" + 
                                      (parentMenu != null? " in menu '" + parentMenu.getName() : "'") + 
                                      " doesn't contain any items");
        
        Checks.checkDuplicatedNames(items, "menu '" + name + "'");
        
        for(Iterator<DesignMenuItem> i = items.iterator(); i.hasNext();)
            ((DesignMenuItem)i.next()).init();
    }

    private void inheritItems() throws ConfigException {
        EntityUtils.update(inheritedMenu.items, items);
    }

    private void reorderItems(List<DesignMenuItem> itemsBeforeUpdate) throws ConfigException {
        for(Iterator<DesignMenuItem> i = itemsBeforeUpdate.iterator(); i.hasNext();) {
            DesignMenuItem item = (DesignMenuItem)i.next();
            String after = item.afterItem;
            
            if(after != null) {
                int itemIndex = items.indexOf(item);
               
                assert itemIndex >= 0;
    
                int afterItemIndex = -1;
                
                if(!after.equals(FIRST_MARK)) {
                    for(int j = 0; j < items.size(); j++) {
                        DesignMenuItem candidate = items.get(j);
                        
                        if(candidate.getName().equals(after)) {
                            afterItemIndex = j;
                            break;
                        }    
                    }
                    
                    if(afterItemIndex < 0)
                        throw new ConfigException(
                                "Element '" + item.getName() + 
                                "' of menu '" + name + 
                                "' has attribute 'after' pointing to element '" + after +
                                "' that is unknown");
                }
                
                // we just need to put the 'item' at position j+1 in
                // the list 'items' and remove it from the old pos                        
                items.add(afterItemIndex + 1, item);
                
                // in case if the item index > j, we need to increase it by 1
                // before removing, because previous insertion increases
                // the item position in the 'items' list
                if(itemIndex > afterItemIndex)
                    itemIndex++;
                
                items.remove(itemIndex);
            }
        }
    }   
}
