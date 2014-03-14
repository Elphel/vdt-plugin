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
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.elphel.vdt.ui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;


public class ScrolledPageContent extends SharedScrolledComposite {

        private FormToolkit fToolkit;
        
        public ScrolledPageContent(Composite parent) {
                this(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        }
        
        public ScrolledPageContent(Composite parent, int style) {
                super(parent, style);
                
                setFont(parent.getFont());
                
                FormColors colors= new FormColors(parent.getDisplay());
                colors.setBackground(null);
                colors.setForeground(null);
                
                fToolkit= new FormToolkit(colors);
                
                setExpandHorizontal(true);
                setExpandVertical(true);
                
                Composite body= new Composite(this, SWT.NONE);
                body.setFont(parent.getFont());
                setContent(body);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.Widget#dispose()
         */
        public void dispose() {
                fToolkit.dispose();
                super.dispose();
        }
        
        public void adaptChild(Control childControl) {
                fToolkit.adapt(childControl, true, true);
        }
        
        public Composite getBody() {
                return (Composite) getContent();
        }

}
