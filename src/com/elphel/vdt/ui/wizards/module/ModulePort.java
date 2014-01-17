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
package com.elphel.vdt.ui.wizards.module;

/**
 * Port of Verilog module has the following properties: 
 * name, direction, MSB, LSB. 
 * 
 * Created: 08.01.2006
 * @author  Lvov Konstantin
 */

public class ModulePort {

    private String name;
    private Direction direction;
    private int msb;
    private int lsb;
    
    private static int portCounter = 0;
    
    public ModulePort() {
        portCounter++;
        name = "port" + portCounter;
        direction = Direction.input;
        msb = 0;
        lsb = 0;
    }
    
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    
    public void setDirection(Direction direction) { this.direction = direction; }
    public Direction getDirection() { return direction; }

    public void setMSB(int msb) { this.msb = msb; }
    public int getMSB() { return msb; }
    
    public void setLSB(int lsb) { this.lsb = lsb; }
    public int getLSB() { return lsb; }
    
    public static final class Direction {
        public static final Direction input  = new Direction();
        public static final Direction output = new Direction();
        public static final Direction inout  = new Direction();

        private Direction() {};     
    } // class Dorection
    
    public static void resetPortCounter() {
        portCounter = 0;
    }
        
} // class ModulePort
