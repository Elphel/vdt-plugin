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
package com.elphel.vdt.core.tools.contexts;

import java.util.List;

import com.elphel.vdt.core.tools.menu.DesignMenu;
import com.elphel.vdt.core.tools.params.CommandLinesBlock;
import com.elphel.vdt.core.tools.params.ParamGroup;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;


public class InstallationContext extends ContextWithoutCommandLine {
    private DesignMenu designMenu; 
    
    public InstallationContext(String name,
                               String controlInterfaceName,
                               String label,
                               String iconName,            
                               String inputDialogLabel,
                               List<Parameter> params,
                               List<ParamGroup> paramGroups,
                               List<CommandLinesBlock> commandLinesBlocks) 
        throws ConfigException
    {
        super(name, 
              controlInterfaceName,
              label,
              iconName,            
              inputDialogLabel,
              params, 
              paramGroups, 
              commandLinesBlocks);
    }
    
    public void init(Config config) throws ConfigException {
        super.init(config);
    }

    public DesignMenu getDesignMenu() {
        return designMenu;
    }
}
