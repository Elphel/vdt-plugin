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
package com.elphel.vdt.core.tools.params;

import java.util.*;

import com.elphel.vdt.core.tools.params.recognizers.*;
import com.elphel.vdt.core.tools.params.types.ParamType;
import com.elphel.vdt.core.tools.params.conditions.*;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.config.ConfigException;
//import com.elphel.vdt.core.tools.generators.FileListGenerator;
//import com.elphel.vdt.core.tools.generators.SourceListGenerator;
import com.elphel.vdt.core.tools.*;
import com.elphel.vdt.ui.MessageUI;

public class Parameter implements Cloneable, Updateable {
    private String id;
    private String outid;
    private String typeName;
    private String syntaxName;
    private String defaultValue;
    private String label;
    private String omitValue;
    private String readonly;
    private String visible;

    private List<String> currentValue = new ArrayList<String>();

    private Condition relevant;
    
    private Context context;
    private ParamType type;
    private Syntax syntax;

    private boolean hasDependentParameters;
    
    private boolean isChild; // Andrey: trying to resolve double inheritance  - at configuration time and when generating output
    private String sourceXML; // Andrey: For error reporting - individual to parameter
/*    
    public Parameter(String id,
    		String outid, 
    		String typeName, 
    		String syntaxName, 
    		String defaultValue,
    		String label,
    		String omitValue,
    		String readonly, 
    		String visible,
    		Condition relevant){
    	this(id,
    			outid,
    			typeName,
    			syntaxName,
    			defaultValue,
    			label,
    			omitValue,
    			readonly,
    			visible,
    			relevant,
    			null);
    }
    public Parameter(String id,
                     String outid, 
                     String typeName, 
                     String syntaxName, 
                     String defaultValue,
                     String label,
                     String omitValue,
                     String readonly, 
                     String visible) 
    {
        this(id,
             outid,
             typeName,
             syntaxName,
             defaultValue,
             label,
             omitValue,
             readonly,
             visible,
             null);
    }
  */  
    public Parameter(String id,
                     String outid, 
                     String typeName, 
                     String syntaxName, 
                     String defaultValue,
                     String label,
                     String omitValue,
                     String readonly, 
                     String visible,
                     Condition relevant,
                     String sourceXML) 
    {
        this.id = id;
        this.isChild=false;
        this.outid = outid != null? outid : id;
        this.typeName = typeName;
        this.syntaxName = syntaxName;
        this.defaultValue = defaultValue;
        this.label = label;
        this.omitValue = omitValue; 
        this.readonly = readonly;
        this.visible = visible;
        this.relevant = relevant;
        this.hasDependentParameters = false;
        this.sourceXML=sourceXML;
/*        
        if (id.equals("SimulationTopFile")){ // Andrey
        	System.out.println("Creating parameter SimulationTopFile, defaultValue="+defaultValue);
        }
*/        
    }   
    protected Parameter(Parameter param) {
        this(param.id, 
             param.outid,      
             param.typeName,   
             param.syntaxName, 
             param.defaultValue,
             param.label,      
             param.omitValue,
             param.readonly,  
             param.visible,
             param.relevant,
             param.sourceXML);
        this.type = param.type;
        this.syntax = param.syntax;
        this.context = param.context; // Added by Andrey - may break something else? Supposed not to clone, otherwise fails in Tools.initParams()
    }

    public Object clone() { // did not clone context (intentionally)
        return new Parameter(this);
    }
    
    public boolean getIsChild(){
    	return isChild;
    }
    public void setIsChild(boolean isChild){
    	this.isChild=isChild;
    }
    
    public boolean matches(Updateable other) {
        Parameter otherParam = (Parameter)other;
        
        return ConditionUtils.conditionsEqual(relevant, otherParam.relevant) &&
               id.equals(otherParam.id);
    }
    
    public void init(Context context) throws ConfigException {
/*        if(this.context != null)
            throw new ConfigException("Parameter ('" + id + "') cannot be re-initialized");
          this.context = context;
    
             */
/*    	
        if (id.equals("SimulationTopFile")){ // Andrey
        	System.out.println("Initializing parameter SimulationTopFile, defaultValue="+defaultValue);
        }
*/
    	
// Andrey: replacing with
        if(this.context == context) {
// That kind of check never happens, same id parameter fro the same tool (not inherited) is silently ignored        	
            throw new ConfigException("Parameter ('" + id + "') cannot be re-initialized on the same context level in "+sourceXML); // wrong file name, should be per-parameter
        }
        if(this.context != null) {
/*        	
        	 System.out.println ("Andrey: Trying to use already defined context for parameter '"+id+"', context='"+this.context.getName()+
        			 "' instead of the currently processed '"+context.getName()+"' for parameter '"+this.getID()+"'"+
        			 " isChild="+getIsChild());
*/        			 
         	setIsChild(true); // Still does not work with EntityUtils.update(), only with EntityUtilsMarkChildren.update()
        	return; // this parameter is inherited, already processed 
        }
       	this.context = context;
        if (getIsChild()){
 /*
         	 System.out.println ("Andrey: isChild is set for parameter '"+id+"' context '"+this.context.getName()+
  
    			 "' instead of the currently processed '"+context.getName()+"' for parameter '"+this.getID()+"'"+
    			 " isChild="+getIsChild());
*/    			 
       	
        }
       	this.context = context;
        String contextInfo = "Context '" + context.getName() + "'";
        if(typeName == null)
            throw new ConfigException(contextInfo + ": Type name of parameter '" + id + "' is absent in "+sourceXML);
        else if(syntaxName == null)
            throw new ConfigException(contextInfo + ": Syntax name of parameter '" + id + "' is absent in "+sourceXML);
        else if(defaultValue == null)
            throw new ConfigException(contextInfo + ": Default value of parameter '" + id + "' is absent in "+sourceXML);
        
        if(readonly == null)
            readonly = new String(BooleanUtils.VALUE_FALSE);
        else
            checkBoolInitialValue(readonly, "readonly");
        
        if(visible == null)
            visible = new String(BooleanUtils.VALUE_TRUE);
        else
            checkBoolInitialValue(visible, "visible");
        
        if(!BooleanUtils.isFalse(visible)) {
            if(label == null)
                throw new ConfigException(contextInfo + ": Label of the parameter '" + id +
                                          "' is absent, while visible attribute is not " + 
                                          BooleanUtils.VALUE_FALSE+" in "+sourceXML);
        }
        
//        this.type = context.getControlInterface().findParamType(typeName);
        this.type = this.context.getControlInterface().findParamType(typeName);
        
        if(this.type == null)
            throw new ConfigException(contextInfo + ": Parameter type '" + typeName + 
//                    "' doesn't exist in control interface '" + context.getControlInterface().getName() +
                                      "' doesn't exist in control interface '" + this.context.getControlInterface().getName() +
                                      "' in "+sourceXML);
        
//        this.syntax = context.getControlInterface().findSyntax(syntaxName);
        this.syntax = this.context.getControlInterface().findSyntax(syntaxName);

        if(this.syntax == null)
            throw new ConfigException(contextInfo + ": Syntax '" + syntaxName + 
//                                      "' doesn't exist in control interface '" + context.getControlInterface().getName() +
                                      "' doesn't exist in control interface '" + this.context.getControlInterface().getName() +
                                      "' in "+sourceXML);
    }
    
    //
    // getters section
    //
    
    public Context getContext() {
        return context;        
    }
    
    public boolean isVisible() {
        return resolveBooleanFieldValue(visible, "visible");
    }

    public boolean isReadOnly() {
        return resolveBooleanFieldValue(readonly, "readonly");
    }
    
    // checks whether the passed id belongs to this param's id 
    // it is only proper way to resolve parameter relevantness!  
    public boolean isSame(String paramID) {
        if(paramID.equals(id)) {
            if(relevant == null || relevant.isTrue())
                return true;
        }
            
        return false;
    }
    
    public String getID() {
        return id;
    }

    public String getOutID() {
        return outid;
    }

    public String getLabel() {
        return label;
    }
    
    public ParamType getType() {
        return type;
    }

    public List<String> getDependencies() {
        List<String> deps = new ArrayList<String>();
        
        MultiConditionValue defaultValueCondition = 
            ConditionUtils.getContextCondition(context, defaultValue);
        
        if(defaultValueCondition != null)
            deps.addAll(defaultValueCondition.getDependencies());
        
        if(relevant != null)
            deps.addAll(relevant.getDependencies());
        
        if(deps.isEmpty())
            return null;
        
        for(Iterator<String> depIter = deps.iterator(); depIter.hasNext();) {
            if(!ParamUtils.isParamString(depIter.next()))
                depIter.remove();          
        }
        
        return deps;
    }
    
    // 
    // current and default values manipulations 
    //
    
    public void setCurrentValue(String value) throws ToolException {
//        if (id.equals("SimulationTopFile")){ // Andrey
//       	System.out.println("setCurrentValue() SimulationTopFile, value="+value);
//      }

        if(type.isList())
            throw new ToolException("Assigning a non-list value to list parameter");
        
        checkValue(value);
        
        currentValue.clear();
        currentValue.add(value);
        
        canonicalizeValue(currentValue);
    }
    
    public void setCurrentValue(List<String> value) throws ToolException {
//        if (id.equals("SimulationTopFile")){ // Andrey
//        	System.out.println("setCurrentValue() SimulationTopFile a list value");
//        }

        if(!type.isList())
            throw new ToolException("Assigning a list value to non-list parameter");

        checkValue(value);
        
        currentValue = new ArrayList<String>(value);
        
        canonicalizeValue(currentValue);
    }
    
    public List<String> getCurrentValue() {
 //       if (id.equals("SimulationTopFile")){ // Andrey
 //       	System.out.println("getCurrentValue() SimulationTopFile, value="+currentValue);
 //       }

        if(currentValue.isEmpty())
            return null;
        
        return currentValue;
    }
    public List<String> getDefaultValue() {
    	return getDefaultValue(false);
    }
    
    public List<String> getDefaultValue(boolean menuMode) {
        String resolvedDefaultValue = ConditionUtils.resolveContextCondition(context, defaultValue);
        String errmsg = "Parameter '" + id + 
                        "' of context '" + context.getName() + 
                        "' - error processing default value: ";

        List<String> processedDefaultValue = null;        
        
        FormatProcessor processor = new FormatProcessor(new Recognizer[] {
                                                            //new RepeaterRecognizer(),
                                                            new SimpleGeneratorRecognizer(menuMode),
                                                            new ContextParamRecognizer(context)
                                                        });

        try {
            processedDefaultValue = processor.process(resolvedDefaultValue);
            
            for(Iterator<String> i = processedDefaultValue.iterator(); i.hasNext();)
                type.checkValue(i.next());
        } catch(ToolException e) {
            MessageUI.error(errmsg + e.getMessage(), e);
        } catch(ConfigException e) {
            MessageUI.error(errmsg + e.getMessage(), e);
        }

        canonicalizeValue(processedDefaultValue);
        
        return processedDefaultValue;
    }
    
    // returns current value if it is set 
    // otherwise returns default value 
    public List<String> getValue() {
/*    	
        if (id.equals("SimulationTopFile")){ // Andrey
        	System.out.println("getValue() SimulationTopFile");
        }
 */   	
        if(!currentValue.isEmpty())
            return currentValue;
        
        return getDefaultValue();
    }

    // returns external form of the current value unless it equals null; 
    // otherwise returns external form of the default value 
    public List<String> getExternalValueForm() {
/*    	
        if (id.equals("SimulationTopFile")){ // Andrey
        	System.out.println("getExternalValueForm() SimulationTopFile");
        }
*/
        List<String> externalFormValue = new ArrayList<String>();
        
        for(Iterator<String> i = getValue().iterator(); i.hasNext();) {
            String elem = type.toExternalForm((String)i.next());
            
            if(elem == null)
                throw new NullPointerException("Parameter '" + getID() + "' has bad external form");
            
            externalFormValue.add(elem);
        }
        
        return externalFormValue;
    }

    public boolean isDefault() {
        return getCurrentValue() == null;
    }
    
    public void setToDefault() {
        currentValue.clear();
    }
    
    public String[] getCommandLine() throws ToolException {
        String omit = getOmitValue();
        List<String> value = getValue();
                
        if(value.size() == 1) {
            if(omit != null && type.equal(omit, value.get(0)))
                return new String[]{""};
        }
        
        String format = syntax.getFormat();
        
        FormatProcessor processor = new FormatProcessor(new Recognizer[] {
                                                            new ParamFormatRecognizer(this),
                                                            new ParamRepeaterRecognizer(this),
                                                            new SimpleGeneratorRecognizer(),
                                                            new RepeaterRecognizer(),
                                                        });
        
        List<String> commandLine = processor.process(format);
        
        return commandLine.toArray(new String[commandLine.size()]);
    }

    // initializes null fields (except id) from the corresponding
    // fields of the given param
    public void update(Updateable other) {
        Parameter param = (Parameter)other;
        
        if(id == null) 
            throw new NullPointerException("id == null");
        
        if(outid == null)
            outid = param.outid;
        
        if(typeName == null)
            typeName = param.typeName;
        
        if(syntaxName == null)
            syntaxName = param.syntaxName;
        
        if(defaultValue == null) {
            defaultValue = param.defaultValue;
            if (id.equals("SimulationTopFile")){ // Andrey
            	System.out.println("Updating parameter SimulationTopFile, defaultValue="+defaultValue);
            }
        }            
        
        if(label == null)
            label = param.label;
           
        if(omitValue == null)
           omitValue = param.omitValue; 
           
        if(readonly == null)
           readonly = param.readonly;
        
        if(visible == null)
           visible = param.visible;
    }
    
    public void checkConsistency(String typeName, String value)
        throws ToolException
    {
        List<String> valueList = new ArrayList<String>(1);
        
        valueList.add(value);
        
        checkConsistency(typeName, valueList);
    }
    
    private void checkValue(String value) throws ToolException {
        List<String> valueList = new ArrayList<String>(1);
        
        valueList.add(value);
        
        checkValue(valueList);
    }
    
    private void checkValue(List<String> value) throws ToolException {
        for(Iterator<String> i = value.iterator(); i.hasNext();) {
            String v = (String)i.next();
            
            try {
                type.checkValue(v);
            } catch(ConfigException e) {
                throw new ToolException(getParamInfo() + ": " + e.getMessage());
            }
        }
    }

    private void checkConsistency(String typeName, List<String> value)
        throws ToolException 
    {
        if(!typeName.equals(this.typeName))
            throw new ToolException(getParamInfo() + 
                                    " of type '" + this.typeName + 
                                    "' is not compatible with type '" + typeName);
        
        checkValue(value);
    }
    
    private String getParamInfo() {
        return "Parameter '" + id + 
               "' of context '" + context.getName() +
               "'";
    }

    //
    // private stuff below
    //
    
    private String getOmitValue() {        
        FormatProcessor processor = new FormatProcessor(new Recognizer[] {
                                                            new ContextParamRecognizer(context),
                                                            new SimpleGeneratorRecognizer()
                                                        },
                                                        false);

        String resolvedOmitValue = ConditionUtils.resolveContextCondition(context, omitValue);
        String result = null;
        
        if(resolvedOmitValue != null) {
            try {
                result = processor.process(resolvedOmitValue).get(0);
            } catch(ToolException e) {
                MessageUI.error(e);
            }
        }
        
        return result;
    }
    
    private boolean resolveBooleanFieldValue(String field, String fieldName) {
        String fieldValue = ConditionUtils.resolveContextCondition(context, field);
        
        if(!field.equals(fieldValue)) {
            try {
                checkBoolAfterResolving(fieldValue, fieldName);
            } catch(ConfigException e) {
                MessageUI.error(e);
            }
        }
        
        return BooleanUtils.isTrue(fieldValue);
    }
    
    private void canonicalizeValue(List<String> value) {
        for(int i = 0; i < value.size(); i++)
            value.set(i, type.canonicalizeValue(value.get(i)));
    }
    
    private void checkBoolInitialValue(String boolAttr, String attrName)
        throws ConfigException
    {
        if(!BooleanUtils.isBoolean(boolAttr) &&
           !StringConditionParser.isConditionString(boolAttr)) 
        {
             throw new ConfigException("Context '" + context.getName() + "': " +
                                       "Attribute '" + attrName + 
                                       "' of parameter '" + id + 
                                       "' has value that is neither " + BooleanUtils.VALUE_TRUE +
                                       ", nor " + BooleanUtils.VALUE_FALSE +
                                       ", nor a condition expression in "+sourceXML);
        }            
    }

    private void checkBoolAfterResolving(String boolValue, String attrName)
        throws ConfigException
    {
        if(!BooleanUtils.isBoolean(boolValue)) {
             throw new ConfigException("Context '" + context.getName() + "': " +
                                       "After condition resolving, attribute '" + attrName + 
                                       "' of parameter '" + id + 
                                       "' has value '" + boolValue +
                                       "' that is neither " + BooleanUtils.VALUE_TRUE +
                                       ", nor " + BooleanUtils.VALUE_FALSE+" in "+sourceXML);
        }            
    }
    
    public boolean hasDependentParameters() {
        return hasDependentParameters;
    }
    
    public void checkDependentParametersPresents(List<Parameter> parameters) {
        if (hasDependentParameters)
            return;
        
        for (Parameter param : parameters) {
            if (param == this)
                continue;
            List<String> paramDependencies = param.getDependencies();
            if (paramDependencies != null) {
                for (String paramString : paramDependencies) {
                    if (isSame(ParamUtils.getParamID(paramString))) {
                        hasDependentParameters = true;
                        return;
                    }
                }
            }
        }
    }
}
