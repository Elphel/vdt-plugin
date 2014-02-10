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


import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;

import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

import org.eclipse.ui.console.IConsoleListener;

public class RunningBuilds {
	public class MonListener{
		private IStreamMonitor monitor;
		private IStreamListener listener;
		public IStreamMonitor getMonitor() {
			return monitor;
		}
		public IStreamListener getListener() {
			return listener;
		}
		public MonListener(IStreamMonitor monitor, IStreamListener listener) {
			super();
			this.monitor = monitor;
			this.listener = listener;
		}
		public void finalize() throws Throwable{
			if ((monitor!=null) && (listener!=null)){
				monitor.removeListener(listener);
			}
			monitor=null;
			listener=null;
			super.finalize();
		}
	}
    private Map<IConsole,MonListener> parserListeners=null; // consoles mapped to pairs of monitors and listeners
                                                            // that should be disconnected when parser is terminated

//	int nextBuildStep=0;
	private final Map<String, VDTRunnerConfiguration> unfinishedBuilds;
	public RunningBuilds(){
		parserListeners= new ConcurrentHashMap<IConsole,MonListener>();
		unfinishedBuilds = new ConcurrentHashMap<String, VDTRunnerConfiguration>();
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
// This is not used, just for testing
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
			System.out.println("***Addded console listeners");
		}
		manager.addConsoleListener(new IConsoleListener(){
			public void consolesAdded(IConsole[] consoles){
				for (int i=0;i<consoles.length;i++){
					if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
						System.out.println("+++ Added: "+consoles[i].getName());
					}
					// Only shows added consoles
				}
			}
			public void consolesRemoved(IConsole[] consoles){
				for (int i=0;i<consoles.length;i++){
					if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
						System.out.println("--- Removed: "+consoles[i].getName());
					}
					//					unfinishedBuilds.remove(consoles[i]);
					removeMonListener(consoles[i]); // remove listeners that provided input data for parsers
					removeConsole(consoles[i]);
				}
			}
		});
		
	}
	
	public void addMonListener(IConsole parserConsole, IStreamMonitor monitor, IStreamListener listener){
		parserListeners.put(parserConsole, new MonListener(monitor, listener));
	}
	private void removeMonListener(IConsole parserConsole){
		MonListener monListener=parserListeners.remove(parserConsole);
		if (monListener!=null){
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
				System.out.println("--- Removing listener from the terminated parser console "+parserConsole.getName());
			}
			try {
				monListener.finalize();
			} catch (Throwable e) {
				System.out.println("Failed to finalize monListener for console "+parserConsole.getName());
				e.printStackTrace();
			}
		}
	}
	
	public String findConsoleParent(IConsole console){
		Iterator<String> iter=unfinishedBuilds.keySet().iterator();
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
			System.out.println("findConsoleParent("+console.getName()+")");
		}
		while (iter.hasNext()) {
			String consoleName=iter.next();
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
				System.out.print("Got console name:"+consoleName);
			}
			VDTRunnerConfiguration runConfig=unfinishedBuilds.get(consoleName);
			if (runConfig.hasConsole(console)){
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
					System.out.println(consoleName+" -> GOT IT");
				}
				return consoleName;
			}
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
				System.out.println(consoleName+" -> no luck");
			}
		}
		return null;		
	}
	public void removeConsole(IConsole console){
		String consoleName=findConsoleParent(console);
		if (consoleName!=null){
			VDTRunnerConfiguration runConfig=unfinishedBuilds.get(consoleName);
			runConfig.removeConsole(console);
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
				System.out.println("Removing console "+console.getName()+" from runConfig for "+consoleName);
			}
			if (runConfig.noConsoles()){
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
					System.out.println("No consoles left in unfinished "+consoleName+" - removing it too");
				}
				unfinishedBuilds.remove(consoleName);
			}
		} else {
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
				System.out.println("Console "+console.getName()+" did not belong here");
			}
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
