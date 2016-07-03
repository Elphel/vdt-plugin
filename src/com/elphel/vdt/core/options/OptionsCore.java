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
package com.elphel.vdt.core.options;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.elphel.vdt.VDT;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.Tool;

/**
 * Support for load/store of context options programmatically.
 * 
 * Created: 13.04.2006
 * @author  Lvov Konstantin
 */

public class OptionsCore {

    public static String getLocationPreferenceName(Tool tool) {
        return VDT.ID_VDT+ ".Tool.Location." + tool.getName();
    }
/*
    public static String getShellPreferenceName(Tool tool) {
        return VDT.ID_VDT+ ".Tool.Shell." + tool.getName();
    }
*/    
    public static String getLocationPreferenceName(PackageContext context) {
        return VDT.ID_VDT+ ".Package.Location" + context.getName();
    }

    public static boolean isLocationRelative(Tool tool) {
        String location = tool.getExeName();
        return isLocationRelative(location);
    }

    public static boolean isLocationRelative(String location) {
        return location.startsWith("~");
    }
    
    public static void doStorePackageLocation(PackageContext context, String location) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        Option option = new ValueBasedOption( getLocationPreferenceName(context)
                                            , location );
        option.setPreferenceStore(store);
        option.doStore();
        finalizeDoStore(store);
    }
    
    public static String getPackageLocation(PackageContext context) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        return store.getString(getLocationPreferenceName(context));
    }
    
    public static String getAbsoluteLocation(PackageContext context, String location) {
        String packageLocation = getPackageLocation(context);
        return getAbsoluteLocation(packageLocation, location);
    }

    public static String getAbsoluteLocation(String packageLocation, String location) {
        if (!isLocationRelative(location))
            return location;
        String toolLocation = location.substring("~".length());
        toolLocation = packageLocation + toolLocation;
        File file = new File(toolLocation);
        return file.getAbsolutePath();
    }
    
    
    public static String getRelativeLocation(Tool tool) {
        return getRelativeLocation( tool.getParentPackage()
                                  , tool.getExeName() );
    }

    public static String getRelativeLocation(PackageContext context, String location) {
        String packageLocation = getPackageLocation(context);
        return getRelativeLocation(packageLocation, location);
    }
    
    public static String getRelativeLocation(String packageLocation, String location) {
        if (isLocationRelative(location))
            return location;

        File file = new File(location);
        if (!file.isAbsolute()) {
            if (location.startsWith(File.separator))
                return "~" + location;
            else    
                return "~" + File.separator + location;
        }
            
        if (location.length() < packageLocation.length())
            return location;
        location = location.substring(packageLocation.length());
        if (location.length() == 0)
            return null;
        if (!location.startsWith(File.separator))
            location = File.separator + location;
        location = "~" + location;
        return location;
    }
    
//    public static void doStoreLocation(Tool tool) {
//        IPreferenceStore store = VDTPlugin.getDefault().getPreferenceStore();
//        store.setValue(getLocationPreferenceName(tool), tool.getExeName());
//    }

    public static void doLoadLocation(Tool tool) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        String location = store.getString(getLocationPreferenceName(tool));
        if ((location != null) || (location.length() == 0)) {
            if (isLocationRelative(location)) {
                location = getAbsoluteLocation(tool.getParentPackage(), location);
            }
            tool.setLocation(location);
        }
/*        
        boolean isShell = store.getBoolean(getShellPreferenceName(tool));
        tool.setIsShell(isShell);
*/        
    }
    
    private static IPreferenceStore getPreferenceStore(IProject project) {
        return new ScopedPreferenceStore( new ProjectScope(project)
                                        , VDT.ID_VDT 
                                        );
    }

    public static IPreferenceStore getPreferenceStore(Context context, IProject project) {
        return new ScopedPreferenceStore( new ProjectScope(project)
                , VDT.ID_VDT+"."+context.getName() 
                );
    }
    
    public static String doLoadOption(Option option, IProject project) {
        IPreferenceStore store = getPreferenceStore(project);
        option.setPreferenceStore(store);
        return option.doLoad();
    }
    
    public static void doLoadContextOptions(Context context, IProject project) {
        IPreferenceStore store = getPreferenceStore(context, project);
        doLoadContextOptions(context, store);
    }

    public static void doLoadContextOptions(Context context) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        doLoadContextOptions(context, store);
    }

    public static void doLoadContextOptions(Context context, IPreferenceStore store) {
//    	System.out.println("doLoadContextOptions("+context.getName()+")");
        List<Parameter> list = context.getParams();
        if (list.isEmpty())
            return;
        
        checkVersionCompatibility(context, store);
        
        for (Parameter param : list) {
            Option option;
        	if (param.getType().isList()) /* Null pointer here on error */
                option = new ParamBasedListOption(param);
            else 
                option = new ParamBasedOption(param);
            
            option.setPreferenceStore(store);
            option.doLoad();
        }
    }
    
    public static void doStoreOption(String name, String value, IProject project) {
        ValueBasedOption option = new ValueBasedOption(name);
        option.setValue(value);
        doStoreOption(option, project);
    }

    public static void doStoreOption(Option option, IProject project) {
        IPreferenceStore store = getPreferenceStore(project);
        option.setPreferenceStore(store);
        option.doStore();
        finalizeDoStore(store);
    }

    public static void doStoreContextOptions(Context context, IProject project) {
    	if ("cocotb".equals(context.getName())){
    		System.out.println("doStoreContextOptions('cocotb'), project="+project);
    	}
        IPreferenceStore store = getPreferenceStore(context, project);
        doStoreContextOptions(context, store);
    }

    public static void doStoreContextOptions(Context context) {
    	if ("cocotb".equals(context.getName())){
    		System.out.println("doStoreContextOptions('cocotb')");
    	}
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        doStoreContextOptions(context, store);
    }

    public static void doStoreContextOptions(Context context, IPreferenceStore store) {
    	if ("cocotb".equals(context.getName())){
    		System.out.println("static doStoreContextOptions('cocotb')");
    	}
    	
        List<Parameter> list = context.getParams();
        if (list.isEmpty())
            return;
        
        for (Parameter param : list ) {
            Option option;
            if (param.getType().isList())
                option = new ParamBasedListOption(param);
            else 
                option = new ParamBasedOption(param);
            option.setPreferenceStore(store);
            option.doStore();
        }
        OptionsUtils.setStoreVersion(context.getContextVersion(), context.getName(), store);
        finalizeDoStore(store);
    }

    private static void finalizeDoStore(IPreferenceStore store) {
        if (store instanceof ScopedPreferenceStore) {
            try {
                if (store.needsSaving())
                    ((ScopedPreferenceStore)store).save();
            } catch (IOException e) {
            	System.out.println("finalizeDoStore() - out of sync (edited manually settings file?");
                  // Nothing do do, we don't need to bother the user
            }
        }    
    }

    public static void doClearContextOptions(Context context) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        doClearContextOptions(context, store);
    }

    public static void doClearContextOptions(Context context, IProject project) {
        IPreferenceStore store = getPreferenceStore(context, project);
        doClearContextOptions(context, store);
    }
    
    public static void doClearContextOptions(Context context, IPreferenceStore store) {
        List<Parameter> list = context.getParams();
        if (list.isEmpty())
            return;
        
        for (Parameter param : list) {
            Option option;
            if (param.getType().isList())
                option = new ParamBasedListOption(param);
            else 
                option = new ParamBasedOption(param);
            option.setPreferenceStore(store);
            option.doClear();
        }
        OptionsUtils.clearStore(context.getName(), store);
        finalizeDoStore(store);
    }

    private static void checkVersionCompatibility(Context context, IPreferenceStore store) {
    	// Context Version is a version of the file, like <vdt-project version = "0.8">
        if (OptionsUtils.isVersionCompatible(context.getContextVersion(), context.getName(), store))
            return;

        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        String message = "Version of TSL description for context '"+ context.getName()+"' has been changed.\n"+
                         "What do you wish to do with the stored values?\n"+
        		         "'Delete' resets all context values to defaults\n"+
                         "'Update' tries to convert settings to newer format\n"+
        		         "'Cancel' does nothing, keeping existing files (until saved).";
        MessageDialog messageBox = new MessageDialog( shell, "Warning", null
                                                    , message
                                                    , MessageDialog.WARNING
                                                    , new String[]{"Delete", "Update", "Cancel"}
                                                    , 1);
        messageBox.open();
        int returnCode = messageBox.getReturnCode();
        if (returnCode == 0) {
            OptionsUtils.clearStore(context.getName(), store);
            finalizeDoStore(store);
        } else if (returnCode == 1) {
        	OptionsUtils.updateStore(context.getName(), store);
            finalizeDoStore(store);
        }
    }
    
} // class OptionsCore
