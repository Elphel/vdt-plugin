/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of VDT plug-in.
 * VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VDT plug-in is distributed in the hope that it will be useful,
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
package com.elphel.vdt.core.tools.params;

import java.util.*;

import com.elphel.vdt.core.tools.Inheritable;
import com.elphel.vdt.core.tools.Checks;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.params.types.ParamType;


public class ControlInterface implements Inheritable {
    public static final String BASIC_INTERFACE_NAME = "BasicInterface";
    
    private String name;
    private String baseInterfaceName;
    private ControlInterface baseInterface;
    private List<TypeDef> typedefList;
    private List<Syntax> syntaxList;
    private Config config;
    private boolean initialized = false; 

    public ControlInterface(Config config, String name) {
        this(config, name, null, null, null);
    }

    public ControlInterface(Config config,
                            String name, 
                            String baseInterfaceName,
                            List<TypeDef> typedefList, 
                            List<Syntax> syntaxList) 
    {
        this.config = config;
        this.name = name;
        this.baseInterfaceName = baseInterfaceName;
        this.typedefList = typedefList;
        this.syntaxList = syntaxList;
    }
    
    public void init() throws ConfigException {
        if(initialized)
            throw new ConfigException("Control interface cannot be re-initialized");
        
        if(!name.equals(BASIC_INTERFACE_NAME)) {
            if(baseInterfaceName == null)
                baseInterfaceName = BASIC_INTERFACE_NAME;        
            
            baseInterface = config.findControlInterface(baseInterfaceName);
                
            if(baseInterface == null) {
                if(!baseInterfaceName.equals(BASIC_INTERFACE_NAME))
                    throw new ConfigException("Base interface '" + baseInterfaceName +
                                              "' of control interface '" + name + 
                                              "' is absent");
                else
                    throw new ConfigException("The basic interface '" + BASIC_INTERFACE_NAME +
                                              "' is absent");
            }   
        } else {
            // we don't check base interface absence, because in
            // such a case a cyclic inheritance error would happen
        }
/*
        for(Iterator ti = typedefList.iterator(); ti.hasNext();)
            ((TypeDef)ti.next()).init(this);           
*/        
        
        initialized = true;
    }

    public void initTypes() throws ConfigException {
 /*       if(initialized)
            throw new ConfigException("Control interface cannot be re-initialized");
        
        if(!name.equals(BASIC_INTERFACE_NAME)) {
            if(baseInterfaceName == null)
                baseInterfaceName = BASIC_INTERFACE_NAME;        
            
            baseInterface = config.findControlInterface(baseInterfaceName);
                
            if(baseInterface == null) {
                if(!baseInterfaceName.equals(BASIC_INTERFACE_NAME))
                    throw new ConfigException("Base interface '" + baseInterfaceName +
                                              "' of control interface '" + name + 
                                              "' is absent");
                else
                    throw new ConfigException("The basic interface '" + BASIC_INTERFACE_NAME +
                                              "' is absent");
            }   
        } else {
            // we don't check base interface absence, because in
            // such a case a cyclic inheritance error would happen
        }
*/
        for(Iterator ti = typedefList.iterator(); ti.hasNext();)
            ((TypeDef)ti.next()).init(this);           
        
//        initialized = true;
    }

    public void check() throws ConfigException {
        Checks.checkCyclicInheritance(this, "control interface");
        
        for(int i = 0; i < syntaxList.size(); i++) {
            String syntaxName = syntaxList.get(i).getFormatName();

            for(int j = i+1; j < syntaxList.size(); j++) {
                if(syntaxName.equals(syntaxList.get(j).getFormatName()))
                    throw new ConfigException("Syntax '" + syntaxName + 
                                              "' is duplicated in control interface '" + name + "'");
            }

            if(baseInterface != null) {
                if(findSyntax(baseInterface, syntaxName) != null)
                    throw new ConfigException("Syntax '" + syntaxName + 
                                              "' duplicates syntax declared in its base control interface '" +
                                              baseInterfaceName + "'");
            }
        }

        for(int i = 0; i < typedefList.size(); i++) {
            String typedefName = typedefList.get(i).getName();

            for(int j = i+1; j < typedefList.size(); j++) {
                if(typedefName.equals(typedefList.get(j).getName()))
                    throw new ConfigException("Type definition '" + typedefName + 
                                              "' is duplicated in control interface '" + name + "'");
            }

            if(baseInterface != null) {
                if(findTypeDef(baseInterface, typedefName) != null)
                    throw new ConfigException("Type definition '" + typedefName + 
                                              "' in control interface '" + name + 
                                              "' duplicates one declared in its base control interface '" +
                                              baseInterfaceName + "'");
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public Config getConfig() {
        return config;
    }

    public ParamType findParamType(String typeName) {
        for(Iterator ti = typedefList.iterator(); ti.hasNext();) {
            TypeDef typeDef = (TypeDef)ti.next();
            
            if(typeName.equals(typeDef.getName()))
                return typeDef.getType();           
        }
       
        if(baseInterface != null)
            return baseInterface.findParamType(typeName);
        
        return null;
    }

    public Syntax findSyntax(String formatName) {
        return findSyntax(this, formatName);
    }
    
    boolean isInheritedFrom(ControlInterface baseControlInterface) {
        if(baseControlInterface == null)
            return true;
        
        if(name.equals(baseControlInterface.getName()))
            return true;
        
        if(baseInterface == null)
            return false;
        
        return baseInterface.isInheritedFrom(baseControlInterface);
    }
    
    public Inheritable getBase() {
        return baseInterface;
    }
    
    public static boolean isInheritedOrSame(ControlInterface ancestor, ControlInterface descendant) {
        return ancestor == descendant || descendant.isInheritedFrom(ancestor);
    }    
    
    private static Syntax findSyntax(ControlInterface interf, String formatName) {
        for(Iterator si = interf.syntaxList.iterator(); si.hasNext();) {
            Syntax syntax = (Syntax)si.next();
            
            if(formatName.equals(syntax.getFormatName()))
                return syntax;           
        }

        if(interf.baseInterface != null)
            return findSyntax(interf.baseInterface, formatName);
        
        return null;
    }

    private static TypeDef findTypeDef(ControlInterface interf, String typedefName) {
        for(Iterator ti = interf.typedefList.iterator(); ti.hasNext();) {
            TypeDef typedef = (TypeDef)ti.next();
            
            if(typedefName.equals(typedef.getName()))
                return typedef;           
        }

        if(interf.baseInterface != null)
            return findTypeDef(interf.baseInterface, typedefName);
        
        return null;
    }
}
