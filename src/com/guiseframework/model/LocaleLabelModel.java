package com.guiseframework.model;

import java.util.Locale;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A label model that provides a localized version of a locale name.
@author Garret Wilson
*/
public class LocaleLabelModel extends DefaultLabelModel
{

	/**The locale the label should represent.*/
	private final Locale locale;

		/**@return The locale the label should represent.*/
		public Locale getLocale() {return locale;}

	/**Constructs a label model indicating the locale to represent.
	@exception NullPointerException if the given locale is <code>null</code>.
	*/
	public LocaleLabelModel(final Locale locale)
	{
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
