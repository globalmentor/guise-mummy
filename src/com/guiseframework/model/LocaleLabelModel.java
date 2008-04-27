/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.model;

import java.util.Locale;

import com.guiseframework.Guise;
import com.guiseframework.GuiseSession;

import static com.globalmentor.java.Objects.*;

/**A label model that provides a localized version of a locale name.
@author Garret Wilson
*/
public class LocaleLabelModel extends DefaultLabelModel
{

	/**The Guise session that owns this object.*/
	private final GuiseSession session;

		/**@return The Guise session that owns this object.*/
		public GuiseSession getSession() {return session;}

	/**The locale the label should represent.*/
	private final Locale locale;

		/**@return The locale the label should represent.*/
		public Locale getLocale() {return locale;}

	/**Constructs a label model indicating the locale to represent.
	@exception NullPointerException if the given locale is <code>null</code>.
	*/
	public LocaleLabelModel(final Locale locale)
	{
		this.session=Guise.getInstance().getGuiseSession();	//store a reference to the current Guise session
		this.locale=checkInstance(locale, "Locale cannot be null");	//save the locale
	}

	/**Determines the text of the label.
	This version returns the localized version of the locale if the value has not been explicitly set.
	@return The label text, or <code>null</code> if there is no label text.
	*/
	public String getLabel()
	{
		final String label=super.getLabel();	//get the default label
		return label!=null ? label : getLocale().getDisplayName(getSession().getLocale());	//return the localized name of the locale based upon the current session locale, if no locale was explicitly set 
	}

}
