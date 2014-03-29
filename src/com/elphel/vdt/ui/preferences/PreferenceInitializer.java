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
package com.elphel.vdt.ui.preferences;


import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.ui.tools.ToolUI;
import com.elphel.vdt.ui.tools.ToolUIManager;

/**
 * Class used to initialize default preference values.
 * 
 * Created: 25.02.2006
 * @author  Lvov Konstantin
*/
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
//      IPreferenceStore store = VDTPlugin.getDefault().getPreferenceStore();
//        ToolUI[] tools = ToolUIManager.getToolUI();
//        for (int i=0; i < tools.length; i++) {
//          store.setDefault( PreferenceConstants.getPreferenceName(tools[i])
//                  , (new File(tools[i].getToolLocation())).getParent() );
//        }
    } // initializeDefaultPreferences()

} // class PreferenceInitializer
