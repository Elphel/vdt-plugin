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

package com.elphel.vdt.ui.options;

import java.awt.Component;
import java.io.File;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class FilteredFileSelector{
	private String approveText;
	private JFileChooser fileChooser;
	private Component parent;
	public FilteredFileSelector(
			File dir,
			String title,
			Component parent,
			String approveText,
			String approveToolTip,
			String filterRegex,
			String filterDescription,
			boolean allowDirs
			){
		this.parent=parent;
		fileChooser = new JFileChooser(dir);
		FileFilter filter1 = new RegexFileFilter(filterRegex, filterDescription,allowDirs);
		fileChooser.setFileFilter(filter1);
		if (title!=null) fileChooser.setDialogTitle(title);
		this.approveText=approveText;
		if (this.approveText==null) this.approveText="Select";
		if (approveToolTip!=null) fileChooser.setApproveButtonToolTipText(approveToolTip);
		fileChooser.setApproveButtonText(this.approveText);
	}
	public FilteredFileSelector(
			File dir,
			String title,
			Component parent,
			String approveText,
			String approveToolTip,
			String prefix,
			String suffix,
			boolean allowEmptyMiddle,
			String filterDescription,
			boolean allowDirs
			){
		this.parent=parent;
		fileChooser = new JFileChooser(dir);
		FileFilter filter1 = new PrefixSuffixFileFilter(prefix,suffix, allowEmptyMiddle,filterDescription,allowDirs);
		fileChooser.setFileFilter(filter1);
		if (title!=null) fileChooser.setDialogTitle(title);
		this.approveText=approveText;
		if (this.approveText==null) this.approveText="Select";
		if (approveToolTip!=null) fileChooser.setApproveButtonToolTipText(approveToolTip);
		fileChooser.setApproveButtonText(this.approveText);
	}
	
	public File openDialog() {
		if (fileChooser.showDialog(parent, approveText) ==  JFileChooser.APPROVE_OPTION) {
		return fileChooser.getSelectedFile();
		} else {
			return null;
		}
	}
	
	private class RegexFileFilter extends FileFilter {
		private String regex;
		private String description;
		private boolean allowDirs;
		private Pattern pattern;
		public RegexFileFilter(
				String filter,
				String description,
				boolean allowDirs){
			this.regex=filter;
			this.description=description;
			this.allowDirs=allowDirs;
			pattern=Pattern.compile(this.regex);
//			System.out.println("RegexFileFilter regex=\""+this.regex+"\"");
		}
		@Override
		public boolean accept(File file) {
		    if (file.isDirectory()) {
		        return allowDirs;
		      } else {
		        String name = file.getName();
//		        System.out.println("filename="+name+", matches()="+pattern.matcher(name).matches());
		        return pattern.matcher(name).matches();
		    }
		}
		@Override
		public String getDescription() {
			return description;
		}
	}
	private class PrefixSuffixFileFilter extends FileFilter {
		private String prefix;
		private String suffix;
		private boolean allowEmptyMiddle;
		private String description;
		private boolean allowDirs;
		public PrefixSuffixFileFilter(
				String prefix,
				String suffix,
				boolean allowEmptyMiddle,
				String description,
				boolean allowDirs){
			this.prefix=prefix;
			this.suffix=suffix;
			this.allowEmptyMiddle=allowEmptyMiddle;
			this.description=description;
			this.allowDirs=allowDirs;
			this.allowEmptyMiddle=allowEmptyMiddle;
		}
		@Override
		public boolean accept(File file) {
		    if (file.isDirectory()) {
		        return allowDirs;
		      } else {
		        String name = file.getName();
//		        System.out.println("filename="+name+", matches()="+pattern.matcher(name).matches());
		        if (!name.startsWith(prefix)) return false;
		        if (!name.endsWith(suffix)) return false;
		        if (allowEmptyMiddle || (name.length()> (prefix.length()+suffix.length()))) return true;
		        return false;
		    }
		}
		@Override
		public String getDescription() {
			return description;
		}
	}
}

