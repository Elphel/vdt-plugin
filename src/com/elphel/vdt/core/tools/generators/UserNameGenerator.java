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
package com.elphel.vdt.core.tools.generators;

import com.elphel.vdt.VDT;

public class UserNameGenerator extends AbstractGenerator {
	public static final String NAME = VDT.GENERATOR_ID_USERNAME;
	public UserNameGenerator()
	{
		super(null); // null for topFormatProcessor - this generator can not reference other parameters
	}

	public String getName() {
		return NAME;
	}

	protected String[] getStringValues() {
		return new String[] {System.getProperty("user.name")};
	}
} //UserNameGenerator
