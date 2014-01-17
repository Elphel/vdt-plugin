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
package com.elphel.vdt.core.tools.params.types;

import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.params.ControlInterface;


public class ParamTypeNumber extends ParamType {
    public static final String NAME = "number";

    public static final String LO_ID = "lo";
    public static final String HI_ID = "hi";
    public static final String FORMAT_ID = "format";
    
    private int lo;
    private int hi;
    private String format;

    public ParamTypeNumber(int lo, int hi, String format) {
        this.lo = lo;
        this.hi = hi;
        this.format = format;
    }

    public void init(ControlInterface controlInterface, String typedefName)
        throws ConfigException 
    {
        if(lo > hi)
            throw new ConfigException("Low bound (" + lo +
                                      ") of '" + NAME + 
                                      "' type '" + NAME +
                                      "' is greater that the high bound (" + hi + ")");
    }    
    
    public String getName() {
        return NAME;
    }

    public boolean isList() {
        return false;
    }
    
    public String getFormat() {
        return format;
    }
    
    public int getLo() {
        return lo;
    }

    public int getHi() {
        return hi;
    }

    public String toExternalForm(String paramValue) {
        return paramValue;
    }

    public void checkValue(String value) throws ConfigException {
    }
}
