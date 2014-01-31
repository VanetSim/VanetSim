/*
 * VANETsim open source project - http://www.vanet-simulator.org
 * Copyright (C) 2008 - 2013  Andreas Tomandl, Florian Scheuer, Bernhard Gruber
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vanetsim.localization;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Helper class for internationalization support.
 */
public final class Messages {
	
	/** The <code>ResourceBundle</code> where to get the messages from. */
	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("vanetsim.localization.messages");	//$NON-NLS-1$

	/**
	 * Instantiates a new instance.
	 */
	private Messages() {
	}

	/**
	 * Returns a localized string.
	 * 
	 * @param key	key to find the string in the language file
	 * 
	 * @return the localized string
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * Returns a localized string.
	 * 
	 * @param key		key to find the string in the language file
	 * @param locale	locale which sets the language only for 1 time
	 * 
	 * @return the localized string
	 */
	public static String getString(String key, String locale) {
		try {
			Locale savedLocale = RESOURCE_BUNDLE.getLocale();
			RESOURCE_BUNDLE = ResourceBundle.getBundle("vanetsim.localization.messages", new Locale(locale));
			String returnMessage =  RESOURCE_BUNDLE.getString(key);
			RESOURCE_BUNDLE = ResourceBundle.getBundle("vanetsim.localization.messages", savedLocale);
			return returnMessage;
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * changes the bundle. If this method is not called the standard language of the computer OS is chosen
	 */
	public static void setLanguage(String locale){
		RESOURCE_BUNDLE = ResourceBundle.getBundle("vanetsim.localization.messages", new Locale(locale));
	}
}
