package com.javaguise.converter;

import com.garretwilson.lang.ObjectUtilities;
import com.javaguise.session.GuiseSession;

import static com.garretwilson.lang.ObjectUtilities.*;

/**An abstract implementation an object that can convert a value from and to its lexical form.
@param <V> The value type this converter supports.
@param <L> The literal type of the lexical form of the value.
@author Garret Wilson
*/
public abstract class AbstractConverter<V, L> implements Converter<V, L>
{

	/**The Guise session that owns this converter.*/
	private final GuiseSession session;

		/**@return The Guise session that owns this converter.*/
		public GuiseSession getSession() {return session;}

	/**Session constructor.
	@param session The Guise session that owns this converter.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractConverter(final GuiseSession session)
	{
		this.session=checkNull(session, "Session cannot be null");	//save the session
	}

	/**Determines whether a given literal value in the lexical space can be converted to a value in the value space.
	This implementation attempts to convert the literal value and returns <code>false</code> if conversion is unsuccessful.
	@param literal The literal value to validate.
	@return <code>true</code> if the literal is valid, else <code>false</code>.
	*/
	public boolean isValidLiteral(final L literal)
	{
		try
		{
			convertLiteral(literal);	//try to convert the literal
			return true;	//indicate that the literal can be converted
		}
		catch(final ConversionException conversionException)
		{
			return false;	//indicate that the literal cannot be converted 
		}
	}

	/**Determines if the given literal in the lexical space is a valid representation of the given value in the value space.
	This implementation assumes that non-<code>null</code> values can correctly be compared using {@link Object#equals(Object)}.
	@param value The value to compare.
	@param literal The literal value in the lexical space to compare with the value after conversion.
	@return <code>true</code> if the given literal in the lexical space is a valid representation of the given value in the value space.
	*/
	public boolean isEquivalent(final V value, final L literal)
	{
		try
		{
			return ObjectUtilities.equals(value, convertLiteral(literal));	//see if the literal value converted to the value space matches the given value
		}
		catch(final ConversionException conversionException)	//if the literal value couldn't be converted
		{
			return false;	//the values aren't equivalent
		}		
	}

}
