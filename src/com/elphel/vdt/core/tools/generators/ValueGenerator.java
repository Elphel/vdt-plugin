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
package com.elphel.vdt.core.tools.generators;

import java.util.*;

import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.ui.MessageUI;

public class ValueGenerator extends AbstractGenerator {
    private Parameter param;
     
    public ValueGenerator(Parameter param, 
                          String prefix, 
                          String suffix, 
                          String separator,
                          FormatProcessor topProcessor) 
    {
        super(prefix, suffix, separator, topProcessor);
         
        this.param = param;
    }
    
    public String getName() {
        return "value of parameter '" + param.getID() + 
               "' of context '" + param.getContext().getName() + 
               "'";
    }
     
    protected String[] getStringValues() {
        List<String> values = param.getValue(topProcessor);
        
        return values.toArray(new String[values.size()]);
    }

    public String[] generate() {
        if (!param.getType().isList()) {
//        	List<String> rslt=param.getValue(topProcessor);
        	List<String> rslt=new ArrayList<String>(param.getValue(topProcessor));
        	if (rslt.isEmpty()){
        		System.out.println("BUG in ValueGenerator.java#generate: param.getValue() isEmpty for "+param.getID());
        		return new String[]{prefix + "" + suffix};
        	} else {
        		try{
        			return new String[]{prefix + rslt.get(0) + suffix};
        		} catch (Exception e){
        			System.out.println("**** Error in ValueGenerator while processing parameter "+param.getID());
        			System.out.println("rslt="+rslt.toString());
        			MessageUI.error("**** Error in ValueGenerator while processing parameter "+param.getID());
            		return new String[]{prefix + "" + suffix};
        		}
        	}
//            return new String[]{prefix + param.getValue(topProcessor).get(0) + suffix};
        }
        else
            return super.generate();
    }
}
/*
java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
	at java.util.ArrayList.rangeCheck(ArrayList.java:604)
	at java.util.ArrayList.get(ArrayList.java:382)
	at com.elphel.vdt.core.tools.generators.ValueGenerator.generate(ValueGenerator.java:58)
	at com.elphel.vdt.core.tools.params.FormatProcessor.generateAndAdd(FormatProcessor.java:155)
	at com.elphel.vdt.core.tools.params.FormatProcessor.processTemplate(FormatProcessor.java:106)
	at com.elphel.vdt.core.tools.params.FormatProcessor.process(FormatProcessor.java:73)
	at com.elphel.vdt.core.tools.params.conditions.StringComparison.isTrue(StringComparison.java:77)
	at com.elphel.vdt.core.tools.params.conditions.ConditionUtils.resolveConditionStrings(ConditionUtils.java:37)
	at com.elphel.vdt.core.tools.params.CommandLinesBlock.getLines(CommandLinesBlock.java:208)
	at com.elphel.vdt.core.tools.contexts.Context.buildParams(Context.java:304)
	at com.elphel.vdt.core.tools.params.Tool.buildParams(Tool.java:1301)
	at com.elphel.vdt.core.tools.params.Tool.buildParams(Tool.java:1284)
	at com.elphel.vdt.core.launching.VDTLaunchUtil.getArguments(VDTLaunchUtil.java:163)
	at com.elphel.vdt.core.launching.VDTLaunchConfigurationDelegate.doLaunch(VDTLaunchConfigurationDelegate.java:101)
	at com.elphel.vdt.core.launching.VDTLaunchConfigurationDelegate.launch(VDTLaunchConfigurationDelegate.java:149)
	at org.eclipse.debug.internal.core.LaunchConfiguration.launch(LaunchConfiguration.java:858)
	at org.eclipse.debug.internal.core.LaunchConfiguration.launch(LaunchConfiguration.java:707)
	at org.eclipse.debug.internal.ui.DebugUIPlugin.buildAndLaunch(DebugUIPlugin.java:1018)
	at org.eclipse.debug.internal.ui.DebugUIPlugin$8.run(DebugUIPlugin.java:1222)
	at org.eclipse.core.internal.jobs.Worker.run(Worker.java:53)

 */
