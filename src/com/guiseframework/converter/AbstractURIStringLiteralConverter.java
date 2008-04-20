package com.guiseframework.converter;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.net.URISyntaxException;

/**An abstract converter that converts a {@link URI} from and to a string literal, correctly resolving relative URIs.
If a base URI is provided, any URI will be resolved to the given base URI.
Otherwise, relative URIs are accepted as-is.
For example, if relative URIs should be interpreted as path URIs, a base URI of <code>path:</code> should be used.
@author Garret Wilson
@see URI
*/
public abstract class AbstractURIStringLiteralConverter extends AbstractStringLiteralConverter<URI>	//TODO add options for allowing or disallowing queries, etc.
{

	/**Resolves a converted URI if needed.
	If the URI is already absolute, no action occurs.
	@param uri The URI to resolve.
	@return The URI resolved as needed and as appropriate.
	*/
	protected abstract URI resolveURI(final URI uri);

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	This version resolves any URI using {@link #resolveURI(URI)}.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	@see #resolveURI(URI)
	*/ 
	public URI convertLiteral(final String literal) throws ConversionException
	{
		try
		{
			return literal!=null && literal.length()>0 ? resolveURI(new URI(literal)) : null;	//if there is a literal, convert it to a URI and resolve it
		}
		catch(final URISyntaxException uriSyntaxException)	//if the string does not contain a valid URI
		{
			throw new ConversionException(format(getSession().dereferenceString(getInvalidValueMessage()), literal), literal);
		}
	}
}
