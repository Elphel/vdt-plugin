/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
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
package com.elphel.vdt.util;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.elphel.vdt.VerilogUtils;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.parser.HdlParserException;
import com.elphel.vdt.veditor.parser.OutlineContainer;
import com.elphel.vdt.veditor.parser.OutlineDatabase;
import com.elphel.vdt.veditor.parser.ParserFactory;
import com.elphel.vdt.veditor.parser.ParserReader;
import com.elphel.vdt.veditor.parser.verilog.VariableStore;
import com.elphel.vdt.veditor.parser.verilog.VerilogParser;
import com.elphel.vdt.veditor.parser.verilog.VerilogParserReader;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

public class VerilogParseMultipass {
	public VerilogParseMultipass(){
		
	}

	/**
	 * Parse specified verilog multiple times to resolve all parameters and ports, parametric instances
	 * @param file Verilog file to process
	 * @param processFile process file if true, otherwise just delete markers
	 * @return true on success, false on failure
	 */
	public boolean parseMultiPass (IFile file, boolean processFile){
		IProject project = file.getProject();
		VerilogParser parser=null;
		VerilogPlugin.deleteMarkers(file);
		if (!processFile) {
			return true;
		}
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_EDITOR)) {
			System.out.println ("parseMultiPass("+file+")");
		}
		String text= VerilogUtils.getEditorText(file);
		try {
			parser=null;
			// VerilogParserReader handles verilog compiler directive
			ParserReader reader = (text==null)?
					(new VerilogParserReader(file.getContents(), file)): // use file
					(new VerilogParserReader(text, file)); // use (possibly dirty) editor window
			parser = (VerilogParser) ParserFactory.createVerilogParser(reader, project, file); // Creates file on outline database
			//do we have parser
			if(parser!= null){
				parser.parse(1);				
			} else {
				return false;
			}
		} catch (CoreException e) {
			return false;
		} catch (HdlParserException e){
			return false;
		}
		OutlineDatabase database = OutlineDatabase.getProjectsDatabase(project);
		if (database==null){
			return false; // BUG?
		}
		OutlineContainer container=database.getOutlineContainer(file);
		database.scanTree(file); // not needed, probably? Or should it be done before that?
		// Second pass so module instances are known before processing assignments (current
		// order is opposite. First pass creates warning markers and later defines instances.
		// Second pass erases markers and uses hierarchical access to instances
		// Andrey
		int maxPass=2;
		for (int numPass=2;numPass<=maxPass;numPass++){
			// for Verilog only - save/restore variable store between parser passes (for parameters assigned after use)
			Map<String,VariableStore> variableStoreMap=parser.getVariableStoreMap();
			ParserReader reader;
			try {
//				reader = new VerilogParserReader(file.getContents(), file);
				reader = (text==null)?
						(new VerilogParserReader(file.getContents(), file)): // use file
						(new VerilogParserReader(text, file)); // use (possibly dirty) editor window
				
			} catch (CoreException e) {
				System.out.println("BUG: parseMultiPass() could not create VerilogParserReader() e="+e);
				return false;
			}
			parser = (VerilogParser) ParserFactory.createVerilogParser(reader, project, file);
			parser.setLastVariableStoreMap(variableStoreMap);
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_EDITOR))
				System.out.println("======== parseMultiPass, pass = "+numPass+" ============");
			VerilogPlugin.deleteMarkers(file);

			try{
				parser.parse(numPass);				
			}
			catch (HdlParserException e){
				System.out.println("BUG: parseMultiPass() parser("+numPass+") failed,  e="+e);
				return false;
			}
			if (numPass==2){
				int rank=parser.getParametersRank();
				if (rank>=0) maxPass= rank+2;
			}
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_EDITOR)) {
				System.out.println("parseMultiPass: getDepsResolved()=    "+parser.getDepsResolved()+" file:"+file);
				System.out.println("parseMultiPass: getParsResolved()=    "+parser.getParsResolved()+" file:"+file);
				System.out.println("parseMultiPass: getExpressionsValid()="+parser.getExpressionsValid()+" file:"+file);
				System.out.println("parseMultiPass: getPortsValid()=      "+parser.getPortsValid()+" file:"+file);
			}
			if (parser.getExpressionsValid() || (numPass == maxPass)) {
				parser.setParametricPorts();
				break; // all done
			}
		}
		return true;
	}
	

}
