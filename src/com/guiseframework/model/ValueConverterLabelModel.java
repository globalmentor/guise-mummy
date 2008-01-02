package com.guiseframework.model;

import com.guiseframework.converter.ConversionException;
import com.guiseframework.converter.Converter;

import static com.globalmentor.java.Objects.*;

/**A label model that converts a value to a string for the label.
If no label is explicitly set, the label will represent the given value converted to a string using the given converter.
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

	/**Value and converter constructor.
	@param value The value to represent as a label.
	@param converter The converter to use for displaying the value as a string.
	@exception NullPointerException if the given converter is <code>null</code>.
	*/
	public ValueConverterLabelModel(final V value, final Converter<V, String> converter)
	{
		this.value=value;	//save the value
		this.converter=checkInstance(converter, "Converter cannot be null.");	//save the converter		
	}

	/**Determines the text of the label.
	This implementation converts the value if no label is explicitly specified.
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
