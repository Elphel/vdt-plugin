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
package com.elphel.vdt.core.tools.params.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import com.elphel.vdt.util.StringPair;
import com.elphel.vdt.core.Utils;
import com.elphel.vdt.core.tools.contexts.Context;


public class StringConditionParser {
    private static final char TOK_EQ = '='; 
    private static final char TOK_NEQ = '#'; 
    private static final char TOK_AND = '^';
    private static final char TOK_OR = '|'; 
    private static final char TOK_COLON = ':';
    private static final char TOK_COMMA = ',';    
    private static final char TOK_BRAKET_OPEN = '(';
    private static final char TOK_BRAKET_CLOSE = ')';
    private static final char TOK_PERCENT = '%';
     
    private MultiConditionValue result;

    private List<StringPair> selValuePairs = new ArrayList<StringPair>();
    private String defaultValue;
    
    private String trueValue, falseValue;
    
    private Context context;
    private String expression;
    private String token;
    private int curpos;
    
    private enum ConditionType {
        BOOLEAN, // "<CONDITION_MARK>condition: val1,val2" 
        SWITCH   // "<CONDITION_MARK>%param: sel1=val1,...,selN=valN,valDef"
    }
    
    //
    // interface
    //
    
    public static final String CONDITION_MARK = "?"; 
    
    public StringConditionParser(Context context) {
        this.context = context;
    }
    
    public static boolean isConditionString(String s) {
        return s != null && s.startsWith(CONDITION_MARK);
    }
    
    public MultiConditionValue parse(String expression) throws ParseError {
        if(!isConditionString(expression))
            return null;
        
        this.expression = expression;
        this.result = null;
        
        switch(detectConditionType()) {
            case BOOLEAN: parseBooleanCondition(); break;
            case SWITCH:  parseSwitchCondition(); break;
        }
        
        return result;
    }
    
    //
    // below comes all the internal machinery
    //
    
    private ConditionType detectConditionType() throws ParseError {
        resetPos();

        if(!nextToken())
            parseError("no expression");

        ConditionType type = ConditionType.BOOLEAN;
        
        if(tokenIsChar(TOK_PERCENT)) {
            if(!nextToken() || (tokenIsSpec() && !tokenIsChar(TOK_PERCENT)))
                parseError("expected param or generator");
            
            if(tokenIsChar(TOK_PERCENT)) // assume generator %%GenName
                nextToken();
                
            nextToken();
            expected(new char[]{TOK_EQ, TOK_NEQ, TOK_COLON});
            
            if(tokenIsChar(TOK_COLON))
                type = ConditionType.SWITCH;
        }
        
        return type;
    }
    
    //
    // condition creators
    //
    
    private void parseBooleanCondition() throws ParseError {
        resetPos();
        nextToken();

        Condition condition = parseExpression();
        
        parseBooleanSelectorList();
        
        List<ConditionValue> conditionValues = new ArrayList<ConditionValue>();

        assert trueValue != null;
        
        conditionValues.add(new ConditionString(condition, trueValue));
        
        assert falseValue != null;

        // just use falseValue as default
        result = new MultiConditionValue(conditionValues, falseValue);
    }
    
    private void parseSwitchCondition() throws ParseError {
        resetPos();
        nextToken();

        expected(TOK_PERCENT);
        
        nextToken();
        
        String str;

        if(tokenIsChar(TOK_PERCENT)) {
            // generator            
            nextToken();
            str = "%%";
        } else {
            str = "%";        
        }
        
        str += token;

        nextToken();
        
        parseSwitchSelectorList();
        
        List<ConditionValue> conditionValues = new ArrayList<ConditionValue>();
        
        for(Iterator<StringPair> i = selValuePairs.iterator(); i.hasNext();) {
            StringPair selVal = (StringPair)i.next();
            Comparison comparison = new StringComparison(Comparison.COMPARE_OP.EQ, context, str, selVal.getFirst()); 
            ConditionValue condVal = new ConditionString(comparison, selVal.getSecond());
            
            conditionValues.add(condVal);
        }

        if(defaultValue != null)
            result = new MultiConditionValue(conditionValues, defaultValue);
        else
            result = new MultiConditionValue(conditionValues);
    }
    
    //
    // selector list parsers
    //
    
    private void parseBooleanSelectorList() throws ParseError {
        trueValue = null;
        falseValue = null;
        
        expected(TOK_COLON);
                
        // process two separated values
        for(int count = 0; count <= 1; count++) {
            nextTokenTo(TOK_COMMA);
            
            String value = token; 
            
            nextToken();
            
            if(count == 0) {
                expected(TOK_COMMA);

                trueValue = value;                
            } else {
                falseValue = value;
            }
        }
                
        if(token != null)
            parseError("expected end of expression");
    }
    
    private void parseSwitchSelectorList() throws ParseError {
        selValuePairs.clear();
        defaultValue = null;
        
        expected(TOK_COLON);
                
        for(;;) {
            nextTokenTo(TOK_EQ);
            
            String sel = token;
            
            if(!nextToken()) {
                // expression ended, so assume previous sel 
                // to be actually a default value
                defaultValue = sel;                
                break;
            }             
            
            expected(TOK_EQ);
            
            nextTokenTo(TOK_COMMA);
            
            selValuePairs.add(new StringPair(sel, token));
            
            if(!nextToken()) {
                defaultValue = "";
                break;
            }
            
            expected(TOK_COMMA);
        }   
    }

    //
    // boolean expression parser stuff
    //
    
    private Condition parseExpression() throws ParseError {
        Condition condition = parseOr();

        return condition;        
    }
    
    private Condition parseOr() throws ParseError {
        Condition left = parseAnd();
        
        if(tokenIsChar(TOK_OR)) {
            if(!nextToken())
                parseError("no tokens after " + TOK_OR);
            
            Condition right = parseExpression();
            
            return new Condition(Condition.BOOL_OP.OR, left, right);
        } else {
            return left;
        }
    }
    
    private Condition parseAnd() throws ParseError {
        Condition left = null;

        if(tokenIsChar(TOK_BRAKET_OPEN)) {
            if(!nextToken())
                parseError("no tokens after open braket");
            
            left = parseBrakets();
        } else {        
            left = parseComparison();
        }
        
        if(tokenIsChar(TOK_AND)) {
            if(!nextToken())
                parseError("no tokens after " + TOK_OR);
            
            Condition right = parseExpression();
            
            return new Condition(Condition.BOOL_OP.AND, left, right);
        } else {
            return left;
        }
    }
    
    private Condition parseBrakets() throws ParseError {
        Condition condition = parseExpression();
    
        expected(TOK_BRAKET_CLOSE);
        
        nextToken();
        
        return condition;
    }
    
    private Comparison parseComparison() throws ParseError {
        checkEndOfExpression();

        String left = token;
        
        char[] comparisonSymbols = new char[]{ TOK_EQ, TOK_NEQ };
        
        if(!tokenIsChar(comparisonSymbols)) {
            nextTokenTo(comparisonSymbols);
            
            left += token;
        }

        nextToken();
        
        expected(comparisonSymbols);
        
        Comparison.COMPARE_OP op = null;
        
        if(tokenIsChar(TOK_EQ))
            op = Comparison.COMPARE_OP.EQ;
        else if(tokenIsChar(TOK_NEQ))
            op = Comparison.COMPARE_OP.NEQ;
        else
            assert false;
                
        nextTokenTo(new char[]{ TOK_EQ,
                                TOK_NEQ,
                                TOK_AND,
                                TOK_OR,
                                TOK_COLON,
                                TOK_BRAKET_OPEN,
                                TOK_BRAKET_CLOSE });

        checkEndOfExpression();
        
        String right = token;

        Comparison comparison = new StringComparison(op, context, left, right);
                
        nextToken();
        
        return comparison;
    }
    
    //
    // general purpose routines
    //
    
    private void resetPos() {
        curpos = CONDITION_MARK.length();        
    }
    
    private boolean nextToken() {
        while(curpos < expression.length() && Utils.isSpace(expression.charAt(curpos)))
            curpos++;
            
        if(curpos >= expression.length()) {
            token = null;
            return false;
        }
           
        char ch = expression.charAt(curpos++);
        
        token = "" + ch;
        
        if(isSpecChar(ch))
            return true;
            
        for(; curpos < expression.length(); curpos++) {
            ch = expression.charAt(curpos);
            
            if(isSpecChar(ch) || Utils.isSpace(ch))
                break;
               
            token += ch;            
        }
        
        return true;
    }
    
    // reads text to one of the given symbols
    // leading and trailing spaces are removed
    private void nextTokenTo(char[] endchars) {
        token = "";
        
        scanExpression:
            
        for(; curpos < expression.length(); curpos++) {
            char ch = expression.charAt(curpos);
            
            for(int i = 0; i < endchars.length; i++)
                if(ch == endchars[i])
                    break scanExpression;
            
            token += ch;            
        }
        
        token = token.trim();
    }

    private void nextTokenTo(char endchar) {
        nextTokenTo(new char[]{ endchar });
    }
    
    private boolean isSpecChar(char ch) {
        switch(ch) {
            case TOK_EQ:
            case TOK_NEQ:
            case TOK_AND:
            case TOK_OR:
            case TOK_COLON:
            case TOK_COMMA:
            case TOK_BRAKET_OPEN:
            case TOK_BRAKET_CLOSE:
            case TOK_PERCENT:
                return true;

            default:
                return false;
        }
    }
    
    private boolean tokenIsChar(char[] chars) {
        if(token != null) {
            for(int i = 0; i < chars.length; i++)
                if(token.equals("" + chars[i]))
                    return true; 
        }
        
        return false;
    }

    private boolean tokenIsChar(char ch) {
        return tokenIsChar(new char[]{ch});
    }
    
    private boolean tokenIsSpec() {
        return token != null && token.length() == 1 && isSpecChar(token.charAt(0));
    }
    
    private void parseError(String message) throws ParseError {
        int pos = curpos;
        
        if(token != null) {
            pos -= token.length();
            
            if(pos < 0)
                pos = curpos;
        }
        
        throw new ParseError(expression, pos, message);
    }
    
    private void expected(char symbol) throws ParseError {
        expected(new char[]{symbol});
    }
    
    private void expected(char[] symbols) throws ParseError {
        if(token != null) {
            for(int i = 0; i < symbols.length; i++)
                if(token.equals("" + symbols[i]))
                    return;
        }
        
        String symbolsString = "'";
        
        for(int i = 0; i < symbols.length-1; i++)
            symbolsString += symbols[i] + "' or '";
        
        symbolsString += symbols[symbols.length-1];
        
        parseError(symbolsString + "' expected, but '" + token + "' found");
    }
    
    private void checkEndOfExpression() throws ParseError {
        if(token == null)
            parseError("unexpected end of expression");
    }
}
