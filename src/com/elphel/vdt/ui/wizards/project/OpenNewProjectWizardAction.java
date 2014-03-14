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
 *******************************************************************************/
package com.elphel.vdt.ui.wizards.project;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;

import com.elphel.vdt.ui.wizards.AbstractOpenWizardAction;


public class OpenNewProjectWizardAction extends AbstractOpenWizardAction {

    @Override
    protected Wizard createWizard() throws CoreException {
        return new NewProjectWizard();
    }

} // class OpenNewProjectWizardAction
