/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */
package yajhfc.phonebook.convrules;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yajhfc.Utils;
import yajhfc.phonebook.PBEntryField;

/**
 * @author jonas
 *
 */
public class RuleParser {
	private static Map<String,PBEntryField> descToFieldMap;
	public static Map<String,PBEntryField> getDescriptionToFieldMap() {
		if (descToFieldMap == null) {
			descToFieldMap = new HashMap<String, PBEntryField>(PBEntryField.FIELD_COUNT);
			for (PBEntryField field : PBEntryField.values()) {
				descToFieldMap.put(field.getDescription().toLowerCase(), field);
			}
		}
		return descToFieldMap;
	}
	
	
	private static final Pattern stringPattern = Pattern.compile("\\G((?:\\\\\\[|[^\\[])+)");
	private static final Pattern fieldPattern = Pattern.compile("\\G\\[(.+?)\\]");
	
	/**
	 * Parses the given String into a ConcatRule
	 * @param text
	 * @param localized
	 * @return
	 * @throws ParseException
	 */
	public static ConcatRule parseRule(String text, boolean localized) throws RuleParseException {
		Matcher stringMatcher = stringPattern.matcher(text);
		Matcher fieldMatcher = fieldPattern.matcher(text);
		int parsePos = 0;
		List<Object> ruleChilds = new ArrayList<Object>();
		while (parsePos < text.length()) {
			if (stringMatcher.find(parsePos)) {
				ruleChilds.add(parseString(stringMatcher));
				parsePos = stringMatcher.end();
			} else if (fieldMatcher.find(parsePos)) {
				ruleChilds.add(parseField(fieldMatcher, localized));
				parsePos = fieldMatcher.end();
			} else {
				throw new RuleParseException(MessageFormat.format("Error: Could not parse input beginning with \"{0}\".", text.substring(parsePos)),
				        MessageFormat.format(Utils._("Error: Could not parse input beginning with \"{0}\"."), text.substring(parsePos)), parsePos);
			}
		}
		return new ConcatRule(ruleChilds.toArray());
	}
	
	@SuppressWarnings("fallthrough")
	private static Object parseString(Matcher stringMatcher) {
	    String quoted = stringMatcher.group(1);
	    if (quoted.indexOf('\\') >= 0) {
	        StringBuilder res = new StringBuilder(quoted.length());
	        for (int i=0; i<quoted.length(); i++) {
	            char c = quoted.charAt(i);
	            switch (c) {
	            case '\\':
	                i++;
	                if (i<quoted.length())
	                    c = quoted.charAt(i);
	                // Fallthrough intended
	            default: 
	                res.append(c);
	            }
	        }
	        return res.toString();
	    } else {
	        // Nothing quoted in String
	        return quoted;
	    }
	}
	
	private static Object parseField(Matcher fieldMatcher, boolean localized) throws RuleParseException {
		String name = fieldMatcher.group(1).toLowerCase();
		PBEntryField field;
		if (localized) {
			field = getDescriptionToFieldMap().get(name);
		} else {
			field = PBEntryField.getKeyToFieldMap().get(name);
		}
		if (field == null) {
			throw new RuleParseException(MessageFormat.format("Unknown field \"{0}\".", name), 
			        MessageFormat.format(Utils._("Unknown field \"{0}\"."), name),
			        fieldMatcher.start(), fieldMatcher.end()-1);
		}
		return field;
	}

	/**
	 * Converts the given ConcatRule to a String that will parse to that rule again using parseRule
	 * @param rule
	 * @param localized
	 * @return
	 */
	@SuppressWarnings("fallthrough")
    public static String ruleToString(EntryToStringRule rule, boolean localized) {
	    if (rule instanceof EntryToStringRuleEnum) {
	        rule = ((EntryToStringRuleEnum) rule).getWrappedRule();
	    }
	    if (!(rule instanceof ConcatRule)) {
	        throw new UnsupportedOperationException("Only ConcatRules supported!");
	    }
	    
		ConcatRule cr = (ConcatRule)rule;
		StringBuilder res = new StringBuilder();
		
		for (Object child : cr.getChildren()) {
			if (child instanceof EntryToStringRule) {
			    if (child instanceof ConcatRule) {
			        // Flatten the rule
			        res.append(ruleToString((ConcatRule)child, localized));
			    } else {
			        throw new UnsupportedOperationException("Nested non-concat rules not supported");
			    }
			} else if (child instanceof PBEntryField) {
				String s;
				if (localized) {
					s = ((PBEntryField) child).getDescription();
				} else {
					s = ((PBEntryField) child).getKey();
				}
				res.append('[').append(s).append(']');
			} else {
				String s = child.toString();
				for (int i=0; i<s.length(); i++) {
					char c = s.charAt(i);
					switch (c) {
					case '[':
					case ']':
					case '\\':
						res.append('\\'); //Escape [, ] and \
						// Fallthrough intended
					default: 
						res.append(c);
					}
				}
			}
		}
		return res.toString();	
	}
	
//	public static void main(String[] args) throws ParseException {
//		System.out.println(stringPattern.matcher("hbhbh \\[ ] hhbhbh").matches());
//		
//		String input = "Hallo \\[ff] [givenname] ] [surname] ! at [COMPANY]";
//		ConcatRule rule = parseRule(input, false);
//		for (int i=0; i<rule.getChildren().length; i++) {
//			Object child = rule.getChildren()[i];
//			System.out.println("child " + i + " " + child.getClass() + ": " + child);
//		}
//		System.out.println(ruleToString(rule, false));
//		
//		rule = RuleParserDialog.showForRule(new Frame(), "Test", NameRule.values(), rule);
//		System.out.println(rule);
//		
//		System.exit(0);
//	}
	
	public static class RuleParseException extends ParseException {
	    private final int errorEnd;
	    private final String localizedMsg;
	    
        public RuleParseException(String msg, String localizedMsg, int errorOffset) {
            this(msg, localizedMsg, errorOffset, -1);
        }
	    
        public RuleParseException(String msg, String localizedMsg, int errorOffset, int errorEnd) {
            super(msg, errorOffset);
            this.errorEnd = errorEnd;
            this.localizedMsg = localizedMsg;
        }
        
        @Override
        public String getLocalizedMessage() {
            return localizedMsg;
        }
	    
        /**
         * Returns the index of the end of the offending substring or -1 if no such end can be given
         * @return
         */
        public int getErrorEnd() {
            return errorEnd;
        }
	}
}
