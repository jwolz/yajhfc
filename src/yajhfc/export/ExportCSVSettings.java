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
 */
package yajhfc.export;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.phonebook.csv.CSVSettings;

/**
 * @author jonas
 *
 */
public class ExportCSVSettings extends CSVSettings {

	public ExportCSVSettings() {
		overwrite = true;
	}

	@Override
	protected void readAvailableFields(
			Map<String, SettingField> availableFieldsMap) {
		putAvailField(availableFieldsMap, "charset");
		putAvailField(availableFieldsMap, "separator");
		putAvailField(availableFieldsMap, "firstLineAreHeaders");
		putAvailField(availableFieldsMap, "quoteChar");
		//putAvailField(availableFieldsMap, "fileName");
	}


	private void putAvailField(Map<String, SettingField> availableFieldsMap, String name) {
		try {
			Field f = this.getClass().getField(name);
			availableFieldsMap.put(name, new ReflectionField(f));
		} catch (Exception e) {
			Logger.getLogger(ExportCSVSettings.class.getName()).log(Level.WARNING, "Error reading fields", e);
		} 
	}
}
