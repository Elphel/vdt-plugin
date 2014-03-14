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

import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.Updateable;
import com.elphel.vdt.core.tools.NamedEntity;


abstract public class DesignMenuItem implements Cloneable, 
                                                Updateable,
                                                NamedEntity
{
    protected Config config;
    protected String name;
    protected String label;
    protected String icon;
    protected Boolean visible;
    protected DesignMenu parentMenu;
    protected String afterItem;
    
    public DesignMenuItem(Config config,
                          DesignMenu parentMenu,
                          String afterItem,
                          String name,
                          String label,
                          String icon,
                          Boolean visible)
    {
        this.config = config;
        this.parentMenu = parentMenu;
        this.afterItem = afterItem;
        this.name = name;
        this.label = label;
        this.icon = icon;
        this.visible = visible;
    }

    public void init() throws ConfigException {
        if(afterItem != null) {
            if(parentMenu == null)
                throw new ConfigException("Menu element '" + name + 
                                          "' doesn't have parent menu, but its 'after' attribute is specified as '" +
                                          afterItem + "'");
            
            if(afterItem.equals(name))
                throw new ConfigException("Menu element '" + name + 
                                          "' of menu '" + parentMenu +
                                          "' has 'after' attribute that is the same as the element name");
        }
    }
    
    public boolean matches(Updateable other) {
        return name.equals(((DesignMenuItem)other).name);
    }
    
    public String getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }
    
    public DesignMenu getParentMenu() {
        return parentMenu;
    }

    public boolean isVisible() {
        if(visible == null)
            return true;
        
        return visible.booleanValue();
    }
    
    public void update(Updateable other) throws ConfigException {
        DesignMenuItem item = (DesignMenuItem)other;
        
        if(name == null) 
            throw new NullPointerException("name == null");

        if(label == null)
            label = item.label;

        if(visible == null)
            visible = item.visible;
    }

    public abstract Object clone();
}
