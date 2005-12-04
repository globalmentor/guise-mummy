package com.javaguise.converter;

import com.javaguise.GuiseSession;

/**Indicates an object that can convert a value from and to its lexical form.
@param <V> The value type this converter supports.
@param <L> The literal type of the lexical form of the value.
@author Garret Wilson
*/
public interface Converter<V, L>
{

	/**@return The Guise session that owns this converter.*/
	public GuiseSession getSession();

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/ 
	public V convertLiteral(final L literal) throws ConversionException;

	/**Converts a value from the value space to a literal value in the lexical space.
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>..
	@exception ConversionException if the value cannot be converted.
	*/ 
	public L convertValue(final V value) throws ConversionException;

	/**Determines whether a given literal value in the lexical space can be converted to a value in the value space.
	@param literal The literal value to validate.
	@return <code>true</code> if the literal is valid, else <code>false</code>.
	*/
	public boolean isValidLiteral(final L literal);

	/**Determines if the given literal in the lexical space is a valid representation of the given value in the value space.
	@param value The value to compare.
	@param literal The literal value in the lexical space to compare with the value after conversion.
	@return <code>true</code> if the given literal in the lexical space is a valid representation of the given value in the value space.
	*/
	public boolean isEquivalent(final V value, final L literal);
}
