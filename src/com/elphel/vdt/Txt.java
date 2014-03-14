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
package com.elphel.vdt;


/** Txt - access to resource strings
 *
 * Comments:  cut-and-paste from com.excelsior.wizard
 */

import java.text.*;
import java.util.*;

import com.elphel.vdt.ui.MessageUI;


public class Txt {

//    private static ResourceBundle messageRB;
//    static {
//        messageRB = (System.getProperty("os.name").indexOf("Windows") >= 0)
//                  ?  ResourceBundle.getBundle("com.excelsior.wizard.texts.Strings_win")
//                  :  ResourceBundle.getBundle("com.excelsior.wizard.texts.Strings_linux") ;
//        if (messageRB.getLocale().getLanguage().equals("")
//        && ! Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage()))
//        {
//            Locale.setDefault(Locale.ENGLISH);
//        }
//    }

    private static ResourceBundle messageRB =
        ResourceBundle.getBundle("com.elphel.vdt.ui.texts.Strings");

    private static boolean wasErrors = false;

    public static String s(String id) {
        try {
            String s = messageRB.getString(id).trim();
            if( s.length() >= 2
                && s.charAt(0) == '\''
                && s.charAt(s.length()-1) == '\''
              )
                s = s.substring(1, s.length()-1);
            return s;
        } catch(MissingResourceException e) {
            if (!wasErrors) {
                wasErrors = true;
                MessageUI.error("VDT message file broken: key = "+id, e);
            }
            return id;
        }
    }

    public static String s(String id, String param) {
        String template = s(id);
        Object[] foo = new Object[]{param};
        return MessageFormat.format(template, foo);
    }

    public static String s(String id, int param) {
        String template = s(id);
        Object[] foo = new Object[]{new Integer(param)};
        return MessageFormat.format(template, foo);
    }

    public static String s(String id, Object[] params) {
        String template = s(id);
        return MessageFormat.format(template, params);
    }

    /**
     * Silent version of s(). If key string does not exists, simply returns null.
     */
    public static String getStringIfExists(String id) {
        try {
            String s = messageRB.getString(id).trim();
            if( s.length() >= 2
                && s.charAt(0) == '\''
                && s.charAt(s.length()-1) == '\''
              )
                s = s.substring(1, s.length()-1);
            return s;
        } catch(MissingResourceException e) {
            return null;
        }
    }

} // class Txt
