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
import com.elphel.vdt.core.tools.BooleanUtils;


public class ParamTypeBool extends ParamType {
    public static final String NAME = "bool";

    public static final String FORMAT_TRUE_ID = "formatTrue";
    public static final String FORMAT_FALSE_ID = "formatFalse";

    private String formatTrue;
    private String formatFalse;

    public ParamTypeBool(String formatTrue, String formatFalse) {
        this.formatTrue = formatTrue;
        this.formatFalse = formatFalse;
    }
    
    public void init(ControlInterface controlInterface, String typedefName)
        throws ConfigException 
    {
    }    
    
    public String getName() {
        return NAME;
    }

    public boolean isList() {
        return false;
    }
    
    public String getFormatFalse() {
        return formatFalse;
    }
    
    public String getFormatTrue() {
        return formatTrue;
    }
    
    public boolean toBoolean(String value) {
        if(value.equals(BooleanUtils.VALUE_TRUE))
            return true;
        else
            return false;
    }

    public String toExternalForm(String paramValue) {
        if(BooleanUtils.isTrue(paramValue))
            return formatTrue;
        else if(BooleanUtils.isFalse(paramValue))
            return formatFalse;
        else 
            return null;
    }
    
    public void checkValue(String value) throws ConfigException {
        assert !FORMAT_TRUE_ID.equals(FORMAT_FALSE_ID);
        
        if(!BooleanUtils.isBoolean(value))
            throw new ConfigException("Bad bool value '" + value + 
                                      "': must be either '" + BooleanUtils.VALUE_TRUE + 
                                      "' or '" + BooleanUtils.VALUE_FALSE + 
                                      "'"); 
    }
}
