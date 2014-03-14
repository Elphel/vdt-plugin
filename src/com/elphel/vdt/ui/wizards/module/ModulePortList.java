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
package com.elphel.vdt.ui.wizards.module;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of list of Verilog module ports for TableViever. 
 * 
 * Created: 08.01.2006
 * @author  Lvov Konstantin
 */

public class ModulePortList {

    private List<ModulePort> ports = new ArrayList<ModulePort>();
    private List<Listener> listeners = new ArrayList<Listener>();
        
    public List<ModulePort> getPorts() {
//              return (ModulePort[])ports.toArray(new ModulePort[ports.size()]);
        return ports;
    }

    /**
     * Add a new Verilog module port to the collection of ports
     */
    public void addPort() {
        ModulePort port = new ModulePort();
        ports.add(port);
        for (int i = 0; i < listeners.size(); i++)
            ((Listener)listeners.get(i)).portAdded(port);
    }
    
    /**
     * Remove specified Verilog module port from the collection of ports
     */
    public void removePort(ModulePort port) {
        ports.remove(port);
        for (int i = 0; i < listeners.size(); i++)
            ((Listener)listeners.get(i)).portRemoved(port);
    }
    
    /**
     * Notify listener of changes of specified Verilog module port
     */
    public void updatePort(ModulePort port) {
        for (int i = 0; i < listeners.size(); i++)
            ((Listener)listeners.get(i)).portChanged(port);
    }
        
    public void moveUpPort(ModulePort port) {
        int position = ports.indexOf(port);
        if (position > 0) {
            removePort(port);
            position--;
            ports.add(position, port);
            for (int i = 0; i < listeners.size(); i++)
                ((Listener)listeners.get(i)).portInserted(port, position);
        }
    } // moveUpPort()
    
    public void moveDownPort(ModulePort port) {
        int position = ports.indexOf(port);
        if ((position >= 0) && (position != ports.size()-1)) {
            removePort(port);
            position++;
            ports.add(position, port);
            for (int i = 0; i < listeners.size(); i++)
                ((Listener)listeners.get(i)).portInserted(port, position);
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
        public void portAdded(ModulePort port);
        public void portRemoved(ModulePort port);
        public void portChanged(ModulePort port);
        public void portInserted(ModulePort port, int position);
    } // interface Listener
    
} // class ModulePortList
