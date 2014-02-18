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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.variables.SelectedResourceManager;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

import java.util.regex.Matcher;

public class ToolLogFile {
	public static final String DEFAULT_LOG_FOLDER=    "tool_logs";
	public static final String ERROR_LOG_SUFFIX=      "-err";
	public static final String OUTPUT_LOG_SUFFIX=     "-out";
	public static final String LOG_EXTENSION=         "log";
	public static final String TOOL_TO_LINE_SEPARATOR="_";
	public static final String BUILD_STAMP_SEPARATOR="-";
	public static final String REGEX_SEP_TO_END="[_\\-].*";
//	private String resolvedLogPath;
	private FileWriter logOutWriter;
	private FileWriter logErrWriter;
	private FileReader logOutReader;
	private FileReader logErrReader;
	private IFile targetOutIFile;
	private IFile targetErrIFile;
	private boolean hasOut;
	private boolean hasErr;
	private boolean singleFile;
	private int errBytes;
	private int outBytes;
	
	
	private boolean debugPrint;
	public static IFolder getDir(String logDir){
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		return project.getFolder((logDir==null)?DEFAULT_LOG_FOLDER:logDir);
	}

	public static String getBaseLogName(String logTool, String logName){
		String baseName=logTool;
		if ((logName!=null) && (logName.length()>0))baseName+=TOOL_TO_LINE_SEPARATOR+logName;
		return baseName;
	}
	public static String getBaseLogName(String logTool){
		return logTool+TOOL_TO_LINE_SEPARATOR;
	}
	public static String getBaseRegex(String logTool){
		return logTool+REGEX_SEP_TO_END;
	}

	public static String getTimeStamp(String logTool, String name){
		if (name==null) return null;
		// remove prefix
		int index=name.lastIndexOf(logTool);
		if (index<0) return null;
		name=name.substring(index+logTool.length());
		index=name.lastIndexOf(".");
		if (index>0) name=name.substring(0,index);
		index=name.lastIndexOf(BUILD_STAMP_SEPARATOR);
		if (index<0) return null;
		name=name.substring(index+BUILD_STAMP_SEPARATOR.length());
// (\d+)\D*\z - last group of digits		
		Matcher m=Pattern.compile("(\\d+)\\D*\\z").matcher(name);
		if (m.find()){
			return m.group(1);
		}
		return null;
	}
	
	public static String insertTimeStamp(String filename, String timestamp){
		int index=filename.lastIndexOf(".");
		if (index>=0){
			return filename.substring(0, index)+BUILD_STAMP_SEPARATOR+timestamp+filename.substring(index);
		} else {
			return filename+BUILD_STAMP_SEPARATOR+timestamp;
		}
	}
	
	/**
	 * @param logDir  Directory path of the log files root directory (or null)
	 * @param logTool     Tool name for which log is created
	 * @param logName     Name specified in the individual output line
	 * @param extension  File extension (like "log") If null, will use default
	 * @param hasOut Has output log (if no separate error log - no suffix
	 * @param hasErr Has error  log (if no separate output log - no suffix
	 * @param buildStamp if null - write log mode, "" - read link (latest) file, else - read that build stamp file 
	 */
	public ToolLogFile (
			String logDir,
			String logTool,
			String logName,
			String extension,  // extension does not include "."
			boolean useOut,
			boolean useErr,
			String buildStamp){
		hasOut=useOut;
		hasErr=useErr;
		logOutWriter= null;
		logOutReader= null;
		logErrWriter= null;
		logErrReader= null;
		targetOutIFile=null;
		targetErrIFile=null;
        debugPrint=VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING);
		boolean writeMode=(buildStamp==null);
		if (!hasOut && !hasErr){
			return; // nothing to do
		}
		singleFile= !(hasOut && hasErr);
		String ext=(extension==null)?LOG_EXTENSION:extension;
		if ((ext.length()>0) && !ext.substring(0,1).equals("."))ext="."+ext;
//		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		if (buildStamp==null) buildStamp=SelectedResourceManager.getDefault().getBuildStamp();
//		IFolder iLogFolder = project.getFolder((logDir==null)?DEFAULT_LOG_FOLDER:logDir);
		IFolder iLogFolder =getDir(logDir);
		if (!iLogFolder.exists()){
			if (writeMode){
				try {
					iLogFolder.create(IResource.NONE, true, null); // should it be IResource.DERIVED ?
				} catch (CoreException e){
					MessageUI.error("Failed to create directory "+iLogFolder.toString()+" for writing logs");
					return;
				}
			} else {
				MessageUI.error(iLogFolder.toString()+" log directory does not exist");
				return;
			}
		}
//		String baseName=logTool;
//		if ((logName!=null) && (logName.length()>0))baseName+=TOOL_TO_LINE_SEPARATOR+logName;
		String baseName=getBaseLogName(logTool, logName);
		
		String baseNameOut=baseName;
		String baseNameErr=baseName;
		String buildStampWithSep=(buildStamp.length()>0)?(BUILD_STAMP_SEPARATOR+buildStamp):"";
		if (!singleFile){
			baseNameOut+=OUTPUT_LOG_SUFFIX;
			baseNameErr+=ERROR_LOG_SUFFIX;
		}
		
		targetOutIFile = iLogFolder.getFile(baseNameOut+buildStampWithSep+ext);
		targetErrIFile = singleFile? targetOutIFile : iLogFolder.getFile(baseNameErr+buildStampWithSep+ext);
		
		if (writeMode) {
			outBytes=0; // jsut for debugging
			errBytes=0;
			byte [] emptyBA={};

			IFile linkOutIFile=   iLogFolder.getFile(baseNameOut+ext);
			IFile linkErrIFile=   singleFile ? linkOutIFile : iLogFolder.getFile(baseNameErr+ext);
			if (!targetOutIFile.exists()){
				try {
					targetOutIFile.create(new ByteArrayInputStream(emptyBA), IResource.NONE, null);
				} catch (CoreException e) {
					MessageUI.error("Failed to create log file "+targetOutIFile.toString());
					return;
				}
			}
			if (!singleFile && !targetErrIFile.exists()){
				try {
					targetErrIFile.create(new ByteArrayInputStream(emptyBA), IResource.NONE, null);
				} catch (CoreException e) {
					MessageUI.error("Failed to create error log file "+targetErrIFile.toString());
					return;
				}
			}
			// Create logWriter(s)
	        try {
	        	logOutWriter=new FileWriter(targetOutIFile.getLocation().toFile(),true); // append
	        } catch (IOException e) {
	        	MessageUI.error("Failed to open "+targetOutIFile.toString()+" for writing log");
	        }
			if (!singleFile){
		        try {
		        	logErrWriter=new FileWriter(targetErrIFile.getLocation().toFile(),true); // append
		        } catch (IOException e) {
		        	MessageUI.error("Failed to open "+targetErrIFile.toString()+" for writing log");
		        }
			}
			// Create links to the latest logs
			try {
				linkOutIFile.createLink(
						targetOutIFile.getLocation(),
						IResource.ALLOW_MISSING_LOCAL |  IResource.REPLACE,
						null);
			} catch (CoreException e) {
	        	MessageUI.error("Failed to create link "+linkOutIFile.toString()+" to the target log "+
	        			targetOutIFile.toString());
			}
			if (!singleFile){
				try {
					linkErrIFile.createLink(
							targetErrIFile.getLocation(),
							IResource.ALLOW_MISSING_LOCAL |  IResource.REPLACE,
							null);
				} catch (CoreException e) {
		        	MessageUI.error("Failed to create link "+linkErrIFile.toString()+" to the target error log "+
		        			targetErrIFile.toString());
				}
			}			
		} else { // read logs mode
			if (!targetOutIFile.exists()){
				if(debugPrint) System.out.println("Skipping non-existent log file:"+targetOutIFile.toString());
			} else {
				try {
					logOutReader=new FileReader(targetOutIFile.getLocation().toFile());
				} catch (FileNotFoundException e) {
					if(debugPrint) System.out.println("- Skipping non-existent log file:"+targetOutIFile.toString());
				}
			}
			if (!singleFile) {
				if (!targetErrIFile.exists()){
					if(debugPrint) System.out.println("Skipping non-existent error log file:"+targetErrIFile.toString());
				} else {
					try {
						logErrReader=new FileReader(targetErrIFile.getLocation().toFile());
					} catch (FileNotFoundException e) {
						if(debugPrint) System.out.println("- Skipping non-existent error log file:"+targetErrIFile.toString());
					}
				}
			} else if (hasErr){ // single error log, no out log
				// reconnect out to err (for name and reader)
				logErrReader=logOutReader;
				logOutReader=null;
				targetErrIFile=targetOutIFile;
				targetOutIFile=null;
			}
		}
	}
	
	public void appendOut(String string) { // no \n added, should be provided
		if (!hasOut ||(logOutWriter==null)) return; // do nothing
		try {
			logOutWriter.append(string);
			errBytes+=string.length();
//			if(debugPrint) System.out.println("out->out: "+string);

		} catch (IOException e) {
			System.out.println("Failed to append log file "+targetOutIFile.toString());
			closeOut();
		}
	}

	public void appendErr(String string) { // no \n added, should be provided
//		if (!hasErr) return;
		if (singleFile?(logOutWriter==null):(logErrWriter==null)) return; 
		try {
			if (singleFile) {
				logOutWriter.append(string);
				outBytes+=string.length();
//				if(debugPrint) System.out.println("err->out: "+string);
			} else {
				logErrWriter.append(string);
//				if(debugPrint) System.out.println("err->err: "+string);
			}
		} catch (IOException e) {
			System.out.println("Failed to append error log file "+
		   (singleFile?targetOutIFile:targetErrIFile).toString()+" string was:"+string);
			if (singleFile) closeOut();
			else  closeErr();
//			close();
		}
	}

	public void closeOut(){ // should be called first
		if ((logOutWriter!=null) && ((logErrWriter==null) || (logErrWriter==logOutWriter))) {
			try {
				logOutWriter.close();
				if(debugPrint) System.out.println("closeOut(), wrote "+outBytes+" bytes");
			} catch (IOException e) {
				System.out.println("Failed to close log file "+targetOutIFile.toString());
			}
			logOutWriter=null;
		}
	}

	public void closeErr(){
		if (logErrWriter!=null) {
			try {
				logErrWriter.close();
				if(debugPrint) System.out.println("closeErr(), wrote "+errBytes+" bytes");
			} catch (IOException e) {
				System.out.println("Failed to close error log file "+targetErrIFile.toString());
			}
			logErrWriter=null;
		}
	}
	
	public FileReader getOutReader(){
		return logOutReader;
	}

	public FileReader getErrReader(){
		return logErrReader;
	}
	
	public String getOutLogName(){
		return (targetOutIFile!=null) ? targetOutIFile.getName():"";
	}
	public String getErrLogName(){
		return (targetErrIFile!=null) ? targetErrIFile.getName():"";
	}

	
	public void close(){
		closeOut();
		closeErr();
	}
	
	public void finalize() throws Throwable{
		close();
		super.finalize();
	}

}
