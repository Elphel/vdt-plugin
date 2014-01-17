/*******************************************************************************
 * Copyright (c) 2004, 2006 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *******************************************************************************/

package com.elphel.vdt.core.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.elphel.vdt.veditor.VerilogPlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class VDTErrorParserControl
{
	private void reportMissingFile(String filename){
		String message = new String();
		message=String.format("\"%s\" is not found in the project. MS Windows users, check filename case!!!", filename);
		/*
		try{

 			IMarker marker=project.createMarker("com.elphel.vdt.veditor.builderproblemmarker");

			IMarker marker=new IMarker(); //project.createMarker("com.elphel.vdt.veditor.builderproblemmarker");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);			
			marker.setAttribute(IMarker.MESSAGE, message);

		}
		catch (CoreException e)
		{
		}
		 */		
	}
	private void setProblemMarker(String filename, int level, int lineNumber, String msg)
	{
		IResource file = getFile(filename);
		if (file != null && lineNumber > 0){
			VerilogPlugin.setExternalProblemMarker(file, level, lineNumber, msg);
		}
		else{
			reportMissingFile(filename);
		}
	}


	private IFile getFileRecursive(IContainer cont, IPath path) {
		try {
			for(IResource res: cont.members()) {
				if(res instanceof IContainer) {
					IFile result = getFileRecursive((IContainer)res,path);
					if(result!=null) return result;
				} else if(res instanceof IFile) {					
					IPath res_path = ((IFile)res).getLocation();					
					if(res_path.equals(path)) 
						return (IFile)res;
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}

	// Christian R. aka supachris from http://www.mikrocontroller.net/topic/264288
	// mg

	private IResource getFile(String filename) {
		File TestFile = new File(filename);
		IResource test=null;
		if(TestFile.isAbsolute())
		{
			IContainer project = ResourcesPlugin.getWorkspace().getRoot();
			IPath projectPath = Path.fromOSString(TestFile.getAbsolutePath());
			test = getFileRecursive(project, projectPath);
		}
		else
		{
			/*			
		IPath projectPath = project.getLocation().append(buildConfig.getWorkFolder());
		projectPath = projectPath.append(filename);	
			IContainer project = ResourcesPlugin.getWorkspace().getRoot();
			test = getFileRecursive(project,projectPath);
			 */
		}
			return test;
		}

		public static class ParseErrorString {
			private String regex;
			// results of parse(String string);
			public String filename;
			public int linenr;
			public String message;
			public int startinmatchedstring;
			public int endinmatchedstring;

			public ParseErrorString(String regexpr) {
				regex = regexpr;
			}

			/**
			 * Tries to parse string using regex
			 * @return boolean: parse succeeded
			 */
			public boolean parse(String string) {
				Pattern errPattern = Pattern.compile(regex);
				Matcher m = errPattern.matcher(string);
				if (!m.matches()) return false;

				int groupCount=m.groupCount();
				if(groupCount < 3) return false;

				int linenrindex = -1;

				for(int i=2;i<=groupCount;i++) {
					String group = m.group(i);
					try {
						linenr = Integer.parseInt(group);
						linenrindex = i;
					}
					catch (NumberFormatException e) {
					}
				}
				if(linenrindex==-1) return false;

				// filename is now at linenrindex-1
				filename = m.group(linenrindex-1);

				// now search for the longest string to capture the message:
				int length_win=-1;
				int messageindex=-1;
				for(int i=1;i<=groupCount;i++) {
					if(i==linenrindex-1) continue;
					if(i==linenrindex) continue;
					String group = m.group(i);
					if(group.length()>length_win) {
						length_win = group.length();
						messageindex = i;
					}
				}
				if(messageindex==-1) return false;

				message = m.group(messageindex); /* error message (w/o "error") */
				startinmatchedstring = m.start(linenrindex-1); /* start of link */
				endinmatchedstring = m.end(linenrindex); /* end of link */ 

				return true;
			}
		}

		public class ConsoleParser implements IPatternMatchListener {
			private String regex;
			private int problemlevel;

			ConsoleParser(String regexpr, int level) {
				regex = regexpr;
				problemlevel = level;
				System.out.println("New ConsoleParser(>"+regex+"<, "+level);
			}

			public int getCompilerFlags() {
				return 0;
			}

			public String getLineQualifier() {
				return null;
			}

			public String getPattern() {
				return regex;
			}

			public void connect(TextConsole console) {
				System.out.println("connect");
			}
			public void disconnect() {
				System.out.println("disconnect");
			}

			public void matchFound(PatternMatchEvent event) {
				int offset = event.getOffset();
				int length = event.getLength();

				Object object = event.getSource();
				if(! (object instanceof TextConsole)) return;
				TextConsole console = (TextConsole)object;

				String consolecontent = console.getDocument().get();
				String matchedstring = consolecontent.substring(offset, offset+length); /* matchstring - full matched string in console */

				ParseErrorString parser = new ParseErrorString(regex);
				boolean success = parser.parse(matchedstring);
				if (!success) return;

				setProblemMarker(parser.filename, problemlevel, parser.linenr, parser.message);		/* liner - line in file */	

				IResource resource = getFile(parser.filename);
				if(resource instanceof IFile) {
					IFile file = (IFile) resource;
					FileLink hyperlink = new FileLink(file,null,-1,-1,parser.linenr);
					try {
						console.addHyperlink(hyperlink, offset+parser.startinmatchedstring, /* here makes console blue (in debug mode it lags as is not redrawn immediately */
								parser.endinmatchedstring-parser.startinmatchedstring+1);
					} catch (BadLocationException e) {
					}
				} else {
					//VerilogPlugin.println("Not a filename!");
				}
			}
		}

	}


