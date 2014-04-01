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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.params.Parameter;

/**
 * Utilities for ExDT options.
 * 
 * Created: 16.05.2006
 * @author  Lvov Konstantin
 */

class OptionsUtils {

    public static final String KEY_CONTENT = "com.elphel.store.context.";
    public static final String KEY_VERSION = "com.elphel.store.version.";
    
    private static final String SEPARATOR = "<-@##@->";
    
    public static String convertListToString(List<String> list) {
        String value = "";
        Iterator i = list.iterator();
        while(i.hasNext()) {
            String str = (String)i.next();
            value += str + SEPARATOR;
        }
        if (value.contains("ConstraintsFiles")){
        	System.out.println("convertListToString() contains ConstraintsFiles");
        	System.out.println("convertListToString() contains ConstraintsFiles");
        	
        }
        return value;
    }
    
    public static String convertListToString(String[] list) {
        return convertListToString(Arrays.asList(list));
    }

        
    public static List<String> convertStringToList(String value) {
        List<String> list;
        if ((value == null) || (value.length() == 0 )) {
            list = new ArrayList<String>();
        } else {
            if (value.contains("ConstraintsFiles")){
            	System.out.println("convertStringToList() contains ConstraintsFiles");
            	System.out.println("convertStringToList() contains ConstraintsFiles");
            }
        	
            String items[] = value.split(SEPARATOR);
            list = new ArrayList<String>(items.length);
            for (String item : items) {
                list.add(item);
            }
        }
        return list;    
    }
    
    public static String toKey(Parameter param) {
        Context context = param.getContext();
        int index = context.getParams().indexOf(param);
        return "" + param.getContext().getName() + "_" + index + "_" + param.getID();
    }
    
    
    public static void addOption( final String key
                                , final String contextID 
                                , IPreferenceStore store) 
    {
        final String contentKey = getContentKey(contextID);
        if (contentKey.equals(key))
            return;
        
        List<String> context = new ArrayList<String>();
        ValueBasedListOption option = new ValueBasedListOption(contentKey, contextID, context);
        option.setPreferenceStore(store);
        context = option.doLoadList();
        if (! hasKey(key, context)) {
            context.add(key);
            option.doStore(context);
        }
    }

    public static void removeOption( final String key
                                   , final String contextID 
                                   , IPreferenceStore store) 
    {
        final String contentKey = getContentKey(contextID);
        if (contentKey.equals(key) || ! store.contains(contentKey)) { 
            return;
        }
        List<String> context = getStoreContext(contextID, store);
        ValueBasedListOption option = new ValueBasedListOption(contentKey, contextID, context);
        option.setPreferenceStore(store);
        context = option.doLoadList();
        if (hasKey(key, context)) {
            context.remove(key);
            if (context.isEmpty()) {
                option.doClear();
                store.setToDefault(contentKey);
            } else {
                option.doStore(context);
            }    
        }
    }
    
    private static boolean hasKey(final String key, List<String> context) {
        for (String item : context) {
            if (key.equals(item))
                return true;
        }
        return false;
    }
    
    private static final String getContentKey(String contextID) {
        if ((contextID != null) && (contextID.length() > 0))
            return KEY_CONTENT + contextID;
        else
            return KEY_CONTENT;
    }

    private static final String getVersionKey(String contextID) {
        if ((contextID != null) && (contextID.length() > 0))
            return KEY_VERSION + contextID;
        else
            return KEY_VERSION;
    }
    
    public static void clearStore(final String contextID, IPreferenceStore store) {
        List<String> context = getStoreContext(contextID, store);
        for (String key : context) {
            store.setToDefault(key);
        }
        store.setToDefault(getContentKey(contextID));
        store.setToDefault(getVersionKey(contextID));
    }

    public static void setStoreVersion( final String version
                                      , final String contextID
                                      , IPreferenceStore store)
    {
        final String contentKey = getContentKey(contextID);
        final String versionKey = getVersionKey(contextID);
        if ((version != null) && store.contains(contentKey))
            store.setValue(versionKey, version);
        else
            store.setToDefault(versionKey);
    }
    
    public static boolean isVersionCompatible( final String version
                                             , final String contextID
                                             , IPreferenceStore store ) 
    {
        boolean compatible;
        final String versionKey = getVersionKey(contextID);
        if (version == null) {
            compatible = ! store.contains(versionKey);
        } else if (store.contains(versionKey)) {
            compatible = version.equals(store.getString(versionKey));
        } else {
            compatible = ! store.contains(versionKey);
        }
        return compatible;
    }
    
    private static List<String> getStoreContext( final String contextID
                                               , IPreferenceStore store ) {
        List<String> context = new ArrayList<String>();
        ValueBasedListOption option = new ValueBasedListOption(getContentKey(contextID), contextID, context);
        option.setPreferenceStore(store);
        context = option.doLoadList();
        return context;
    }
    
} // class OptionsUtils
