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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;

import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

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
    private static final String CONTEXT_SEPARATOR = "_@_";
    
    public static String convertListToString(List<String> list) {
        String value = "";
        Iterator<String> i = list.iterator();
        while(i.hasNext()) {
            String str = (String)i.next();
            value += str + SEPARATOR;
        }
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
    		if (value.contains("ConstraintsFiles")){
    			System.out.println("convertStringToList() contains ConstraintsFiles");
    			System.out.println("convertStringToList() contains ConstraintsFiles");
    		}
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
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
        		if (value.contains("ConstraintsFiles")){
        			System.out.println("convertStringToList() contains ConstraintsFiles");
        			System.out.println("convertStringToList() contains ConstraintsFiles");
        		}
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
//        Context context = param.getContext();
//        int index = context.getParams().indexOf(param);
//        return "" + param.getContext().getName() + "_" + index + "_" + param.getID();
        return "" + param.getContext().getName() + CONTEXT_SEPARATOR + param.getID();
        
  /* TODO: 07/02/2016: Disabled using parameter index in the key - that was causing VDT to forget all settings
   * when number of tool parameters was modified.
   *  ContextOptionsDialog */       
        
        
//TODO: 07/01/2016: Understand why index is needed? It changes when xml files are edited (added/deleted parameters
//      But each parameter ID + context should already be unique
//      So meanwhile I'll add searching for same context/parameter with other indices if the exact match is not found         
        
        
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

    public static void updateStore(final String contextID, IPreferenceStore store) {
        List<String> contextList = getStoreContext(contextID, store);
        Map<String,String> newKeyValues= new HashMap<String,String>();
        Pattern patt_new=Pattern.compile("[a-zA-Z0-9_]*"+CONTEXT_SEPARATOR);
        Pattern patt_indx=Pattern.compile("([a-zA-Z0-9_]*)_[0-9]+_(.*)");
        for (String key : contextList) {
        	if (patt_new.matcher(key).lookingAt()){
        		newKeyValues.put(key, store.getString(key)); // Found new <context>_@_<parameter>
        	} else {
        		Matcher matcher = patt_indx.matcher(key);
        		if (matcher.lookingAt()) {
        			String newKey = matcher.group(1)+CONTEXT_SEPARATOR+matcher.group(2);
        			
        			if (!newKeyValues.containsKey(newKey)){ // Only if it is not yet set - not to overwrite the new value
                		newKeyValues.put(newKey, store.getString(key)); // Found new <context>_@_<parameter>
        			}
        					
        		}
        	}
            store.setToDefault(key);
        }
        // Save converted/filtered values, if they are not empty (will not be able to overwrite non-empty default)
        for (String key :newKeyValues.keySet()){
        	store.putValue(key,newKeyValues.get(key));
        }
        // now regenerate context string
        //Rebuild the list, getting rid of indexed entries 
        contextList = new ArrayList<String>();
        for (String key :newKeyValues.keySet()){
        	contextList.add(key);
        }
        
        
        final String contentKey = getContentKey(contextID);
        ValueBasedListOption option = new ValueBasedListOption(contentKey, contextID, contextList);
        option.setPreferenceStore(store);
        option.doStore(contextList);
//        store.setToDefault(getContentKey(contextID));
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
    
    public static List<String> getStoreContext( final String contextID
//    private static List<String> getStoreContext( final String contextID
                                               , IPreferenceStore store ) {
        List<String> context = new ArrayList<String>();
        ValueBasedListOption option = new ValueBasedListOption(getContentKey(contextID), contextID, context);
        option.setPreferenceStore(store);
        context = option.doLoadList();
        return context;
    }
    
} // class OptionsUtils
