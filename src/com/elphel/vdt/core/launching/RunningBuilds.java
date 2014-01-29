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
package com.elphel.vdt.core.launching;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
//import org.eclipse.core.resources.IProject;
//import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
//import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
//import org.eclipse.debug.ui.DebugUITools;
//import org.eclipse.ui.console.ConsolePlugin;
//import org.eclipse.ui.console.IConsoleManager;
//import org.eclipse.ui.console.IPatternMatchListener;
//import org.eclipse.ui.console.MessageConsole;

import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.IConsole;
//import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.contexts.BuildParamsItem;
import com.elphel.vdt.ui.MessageUI;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.parser.OutlineContainer;
import com.elphel.vdt.veditor.preference.PreferenceStrings;








//import com.elphel.vdt.core.Utils;
import org.eclipse.ui.console.IConsoleListener;

public class RunningBuilds {
//	int nextBuildStep=0;
	private final Map<String, VDTRunnerConfiguration> unfinishedBuilds;
	public RunningBuilds(){
		unfinishedBuilds = new ConcurrentHashMap<String, VDTRunnerConfiguration>();
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
// This is not used, just for testing		
		System.out.println("***Addded console listeners");
		manager.addConsoleListener(new IConsoleListener(){
			public void consolesAdded(IConsole[] consoles){
				for (int i=0;i<consoles.length;i++){
					System.out.println("+++ Added: "+consoles[i].getName());
					// Only shows added consoles
				}
			}
			public void consolesRemoved(IConsole[] consoles){
				for (int i=0;i<consoles.length;i++){
					System.out.println("--- Removed: "+consoles[i].getName());
//					unfinishedBuilds.remove(consoles[i]);
					removeConsole(consoles[i]);
				}
			}
		});
	}
	
	public String findConsoleParent(IConsole console){
		Iterator<String> iter=unfinishedBuilds.keySet().iterator();
		System.out.println("findConsoleParent("+console.getName()+")");
		while (iter.hasNext()) {
			String consoleName=iter.next();
			System.out.print("Got console name:"+consoleName);
			VDTRunnerConfiguration runConfig=unfinishedBuilds.get(consoleName);
			if (runConfig.hasConsole(console)){
				System.out.println(consoleName+" -> GOT IT");
				return consoleName;
			}
			System.out.println(consoleName+" -> no luck");
		}
		return null;		
	}
	public void removeConsole(IConsole console){
		String consoleName=findConsoleParent(console);
		if (consoleName!=null){
			VDTRunnerConfiguration runConfig=unfinishedBuilds.get(consoleName);
			runConfig.removeConsole(console);
			System.out.println("Removing console "+console.getName()+" from runConfig for "+consoleName);
			if (runConfig.noConsoles()){
				System.out.println("No consoles left in unfinished "+consoleName+" - removing it too");
				unfinishedBuilds.remove(consoleName);
			}
		} else {
			System.out.println("Console "+console.getName()+" did not belong here");
		}
	}
	public boolean isUnfinished(IConsole console){
		return unfinishedBuilds.containsKey(console);
	}
	
	public VDTRunnerConfiguration resumeConfiguration(String consoleName){
		VDTRunnerConfiguration conf=unfinishedBuilds.get(consoleName);
		unfinishedBuilds.remove(consoleName);
		return conf;
	}

	public VDTRunnerConfiguration getConfiguration(String consoleName){
		VDTRunnerConfiguration conf=unfinishedBuilds.get(consoleName);
		return conf;
	}

	public void removeConfiguration(String consoleName){
		unfinishedBuilds.remove(consoleName);
	}

	public void saveUnfinished(String consoleName, VDTRunnerConfiguration configuration ){
		unfinishedBuilds.put(consoleName, configuration);
	}
    
} // class RunningBuilds
