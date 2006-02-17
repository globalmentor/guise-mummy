package com.guiseframework.model;

import com.guiseframework.GuiseSession;
import com.guiseframework.converter.ConversionException;
import com.guiseframework.converter.Converter;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A label model that converts a value to a string for the label.
If no label is explicitely set, the label will represent the given value converted to a string using the given converter.
@param <V> The type of value represented by the label.
@author Garret Wilson
*/
public class ValueConverterLabelModel<V> extends DefaultLabelModel
{

	/**The represented value.*/
	private final V value;

		/**@return The represented value.*/
		public final V getValue() {return value;}

	/**The converter to use for displaying the value as a string.*/
	private final Converter<V, String> converter;
		
		/**@return The converter to use for displaying the value as a string.*/
		public Converter<V, String> getConverter() {return converter;}

	/**Session, value, and converter constructor.
	@param session The Guise session that owns this model.
	@param value The value to represent as a label.
	@param converter The converter to use for displaying the value as a string.
	@exception NullPointerException if the given session and/or converter is <code>null</code>.
	*/
	public ValueConverterLabelModel(final GuiseSession session, final V value, final Converter<V, String> converter)
	{
		super(session);	//construct the parent
		this.value=value;	//save the value
		this.converter=checkNull(converter, "Converter cannot be null.");	//save the converter		
	}

	/**Determines the text of the label.
	This implementation converts the value if no label is explicitely specified.
	@return The label text, or <code>null</code> if there is no label text.
	@see #getLabel()
	@see #getValue()
	*/
	public String getLabel()
	{
		String label=super.getLabel();	//get the specified label
		if(label==null)	//if no label is specified
		{
			try
			{
				label=getConverter().convertValue(getValue());	//convert the value to a string
			}
			catch(final ConversionException conversionException)
			{
				throw new AssertionError(conversionException);	//TODO fix better
			}
		}
		return label;	//return the label
	}

}
