package com.guiseframework.platform;

import java.util.Locale;

import javax.mail.internet.ContentType;

/**The identification of the client software accessing Guise on the platform.
@author Garret Wilson
*/
public interface ClientProduct extends Product
{

	/**@return The content types accepted by the client.*/
	public Iterable<ContentType> getAcceptedContentTypes();

	/**Determines if the client accepts the given content type.
	Wildcard content types are correctly matched.
	@param contentType The content type to check.
	@return <code>true</code> if the client accepts the given content type.
	*/
	public boolean isAcceptedContentType(final ContentType contentType);

	/**Determines if the client accepts the given content type.
	@param contentType The content type to check.
	@param matchWildcards <code>true</code> if the content type should be matched against wildcard sequences, as is normal.
	@return <code>true</code> if the client accepts the given content type.
	*/
	public boolean isAcceptedContentType(final ContentType contentType, final boolean matchWildcards);

	/**@return The languages accepted by the client.*/
	public Iterable<Locale> getClientAcceptedLanguages();

}
