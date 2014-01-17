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
package com.elphel.vdt.core;

import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import com.elphel.vdt.ui.MessageUI;

/**
 * Utilities for VDT Plud-in.
 *
 * Created: 22.12.2005
 * @author  Lvov Konstantin
 */

public class Utils {

    public static final int OS_WINDOWS = 1;
    public static final int OS_LINUX = 2;
    private static int os;

    public static boolean isWindows() {
        return os == OS_WINDOWS;
    }

    public static boolean isLinux() {
        return os == OS_LINUX;
    }
    
	/** Returns the pure file name without path and extensions. */
	public static String getPureFileName(String fileName) {
        if ((fileName == null) || (fileName.length() == 0))
        	return "";
        int dot_pos = fileName.lastIndexOf(".");
        if (dot_pos == -1)
        	return fileName;
        else
        	return fileName.substring(0, dot_pos); 
	}
	
    public static boolean stringContainsSpace(String s) {
        return s.contains(" ") || s.contains("\n");
    }

    public static boolean stringEndsWithSpace(String s) {
        return s.endsWith(" ") || s.endsWith("\n");
    }

    public static boolean stringStartsWithSpace(String s) {
        return s.startsWith(" ") || s.startsWith("\n");
    }

    public static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    public static boolean isSpace(char ch) {
        return ch == ' ' || ch == '\t';
    }
    
    public static boolean isNewLine(char ch) {
        return ch == '\n';
    }

    public static int findBoundary(String s) {
        return findBoundary(s, 0);
    }
    
    public static int findBoundary(String s, int from) {
        for(int i = from; i < s.length(); i++) {
            char ch = s.charAt(i);

            if(!Utils.isAlpha(ch) && !Utils.isDigit(ch) && !isAuxStrChar(ch))
                return i;
        }
        
        return s.length(); 
    }
    
    public static boolean containsStr(List<String> list, String str) {
        for(Iterator<String> i = list.iterator(); i.hasNext();)
            if(((String)i.next()).equals(str))
                return true;
        
        return false;
    }
    
    public static String listToString(List<String> list) {
        String str = "";
        
        for(Iterator<String> i = list.iterator(); i.hasNext();)
            str += (String)i.next();
        
        return str;
    }
    
    private static boolean isAuxStrChar(char ch) {
        return ch == '_'; 
    }
    
    static {
        String osName = System.getProperty("os.name");
        if(osName.indexOf("Windows") >= 0)
            os = OS_WINDOWS;
        else if (osName.indexOf("Linux") >= 0)
            os = OS_LINUX;
        else
            MessageUI.fatalError("Unknown os.name");
    }

    /**
     * Add this nature to the project
     */
    public static void addNature( String natureID
                                , IProject project
                                , IProgressMonitor monitor ) throws CoreException
    {                               
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        } else if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        
        if (!project.hasNature(natureID)) {
            IProjectDescription description = project.getDescription();
            String[] prevNatures= description.getNatureIds();
            String[] newNatures= new String[prevNatures.length + 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
            newNatures[prevNatures.length]= natureID;
            description.setNatureIds(newNatures);
            project.setDescription(description, monitor);
        } else 
            monitor.worked(1);
    }
    
    /**
     * Add this nature to the project
     */
    public static void removeNature( String natureID
                                   , IProject project
                                   , IProgressMonitor monitor ) throws CoreException
    {                               
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        } else if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        
        if (project.hasNature(natureID)) {
            IProjectDescription description = project.getDescription();
            String[] prevNatures = description.getNatureIds();
            String[] newNatures  = new String[prevNatures.length - 1];
            int i = 0;
            for (String id :  prevNatures) {
                if (! natureID.equals(id)) 
                    newNatures[i] = id;
                i++;    
            }
            description.setNatureIds(newNatures);
            project.setDescription(description, monitor);
        } else 
            monitor.worked(1);
    }
    
} // class Utils
