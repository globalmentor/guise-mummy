package com.javaguise.model;

import java.util.Locale;
import java.util.MissingResourceException;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.GuiseSession;

/**A value model with a label that provides a localized version of a locale name.
@param <V> The type of value contained in the model.
@author Garret Wilson
*/
public class LocaleLabelValueModel<V> extends DefaultValueModel<V>
{

	/**The locale the label should represent.*/
	private final Locale locale;

		/**@return The locale the label should represent.*/
		public Locale getLocale() {return locale;}

	/**Constructs a boolean value model indicating the locale to represent.
	@param session The Guise session that owns this model.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session, class object, and/or locale is <code>null</code>.
	*/
	public LocaleLabelValueModel(final GuiseSession session, final Class<V> valueClass, final Locale locale)
	{
		super(session, valueClass);	//construct the parent class
		this.locale=checkNull(locale, "Locale cannot be null");	//save the locale
	}

	/**Determines the text of the label.
	This version returns the localized version of the locale.
	@return The label text, or <code>null</code> if there is no label text.
	*/
	public String getLabel() throws MissingResourceException
	{
		return getLocale().getDisplayName(getSession().getLocale());	//return the localized name of the locale based upon the current session locale 
	}

	/**Sets the text of the label.
	This version throws an {@link UnsupportedOperationException}.
	@param newLabel The new text of the label.
	@see LabelModel#LABEL_PROPERTY
	*/
	public void setLabel(final String newLabel)
	{
		throw new UnsupportedOperationException("A date label cannot be set.");
	}
	
	/**@return <code>true</code> if this model has label information, such as an icon or a label string.*/
	public boolean hasLabel()
	{
		return true;	//a locale label value model always has label information
	}

}
