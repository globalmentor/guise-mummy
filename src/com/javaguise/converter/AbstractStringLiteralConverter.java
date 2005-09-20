package com.javaguise.converter;

import com.javaguise.session.GuiseSession;

/**An abstract implementation an object that can convert a value from and to a string.
@param <V> The value type this converter supports.
@author Garret Wilson
*/
public abstract class AbstractStringLiteralConverter<V> extends AbstractConverter<V, String>
{

	/**Session constructor with no value required.
	@param session The Guise session that owns this converter.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractStringLiteralConverter(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Converts a value from the value space to a literal value in the lexical space.
	This implementation returns the {@link Object#toString()} version of the value, if a value is given.
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>..
	@exception ConversionException if the value cannot be converted.
	*/ 
	public String convertValue(final V value) throws ConversionException
	{
		return value!=null ? value.toString() : null;	//convert the value to a string if there is a value
	}
}
