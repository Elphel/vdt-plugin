/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
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
package com.elphel.vdt.core.tools.generators;

import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import com.elphel.vdt.VDT;
import com.elphel.vdt.veditor.VerilogPlugin;


public class ParsersPathGenerator extends AbstractGenerator {
    public static final String NAME = VDT.GENERATOR_PARSERS_PATH;
    
    public String getName() {
        return NAME;
    }
    public ParsersPathGenerator()
    {
    	super(null); // null for topFormatProcessor - this generator can not reference other parameters
    }

    protected String[] getStringValues() {
        String path = "$nl$/" + VDT.PATH_TO_PARSERS; //$NON-NLS-1$
        try {
        	URL url=FileLocator.find(VerilogPlugin.getDefault().getBundle(), new Path(path), null);
        	if(url != null) {
        		URI uri=FileLocator.resolve(url).toURI();
//        		System.out.println("ParsersPathGenerator()->"+uri.getRawPath()+" : "+uri.toString());
        		return new String[]{uri.getRawPath()};
        	}
        	return null;
        } catch (Exception e){
        	return null;
        }
    }
}
