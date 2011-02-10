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

package com.guiseframework.platform.web;

import java.io.*;
import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import com.globalmentor.model.NameValuePair;
import com.globalmentor.net.ContentType;
import com.globalmentor.net.http.HTTPServlets;
import com.globalmentor.text.CharacterEncoding;
import com.globalmentor.text.Text;
import com.guiseframework.Destination;
import com.guiseframework.GuiseSession;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.text.CharacterEncoding.*;
import static com.guiseframework.platform.web.WebPlatform.*;

/**A web depict context of an HTTP servlet.
The output stream defaults to <code>text/plain</code> encoded in <code>UTF-8</code>.
@author Garret Wilson
*/
public class HTTPServletWebDepictContext extends AbstractWebDepictContext
{

	/**The Guise HTTP request.*/
	private final HTTPServletGuiseRequest guiseRequest;

		/**@return The Guise HTTP request.*/
		protected HTTPServletGuiseRequest getGuiseRequest() {return guiseRequest;}

	/**The HTTP servlet response.*/
	private final HttpServletResponse response;

		/**@return The HTTP servlet response.*/
		protected HttpServletResponse getResponse() {return response;}

		/**The current full absolute URI for this depiction, including any query.*/
		private final URI depictURI;

		/**@return The current full absolute URI for this depiction, including any query.*/
		public URI getDepictionURI() {return depictURI;}

	/**The current content type of the output.*/
	private ContentType outputContentType=ContentType.getInstance(ContentType.TEXT_PRIMARY_TYPE, Text.PLAIN_SUBTYPE);	//default to text/plain

	/**The qualified name to use for the attribute hash attribute.*/
	private final String attributeHashAttributeQualifiedName=getQualifiedName(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_ATTRIBUTE_HASH);

		/**@return The qualified name to use for the attribute hash attribute.*/
		protected String getAttributeHashAttributeQualifiedName() {return attributeHashAttributeQualifiedName;}

	/**The qualified name to use for the content hash attribute.*/
	private final String contentHashAttributeQualifiedName=getQualifiedName(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_CONTENT_HASH);

		/**@return The qualified name to use for the content hash attribute.*/
		protected String getContentHashAttributeQualifiedName() {return contentHashAttributeQualifiedName;}

	/**Constructor.
	@param guiseRequest Guise request information.
	@param response The HTTP servlet response. 
	@param session The Guise user session of which this context is a part.
	@param destination The destination with which this context is associated.
	@exception NullPointerException if the given Guise request, session, and/or destination is <code>null</code>.
	@exception IOException If there was an I/O error loading a needed resource.
	*/
	public HTTPServletWebDepictContext(final HTTPServletGuiseRequest guiseRequest, final HttpServletResponse response, final GuiseSession session, final Destination destination) throws IOException
	{
		super(session, destination);	//construct the parent class
		this.guiseRequest=checkInstance(guiseRequest, "Guise request cannot be null.");
		this.response=checkInstance(response, "Response cannot be null.");
//TODO decide if we want this to include parameters or not		this.navigationURI=URI.create(request.getRequestURL().toString());	//create the absolute navigation URI from the HTTP requested URL
		this.depictURI=HTTPServlets.getRequestURI(guiseRequest.getHTTPServletRequest());	//get the depiction URI, containing any query
/*TODO del if not needed
		final String referrer=getReferer(request);	//get the request referrer, if any
		referrerURI=referrer!=null ? getPlainURI(URI.create(referrer)) : null;	//get a plain URI version of the referrer, if there is a referrer
*/
/*TODO del
		final String contentTypeString=request.getContentType();	//get the request content type
		final ContentType contentType=contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object from the request content type, if there is one
*/
		setHashAttributesGenerated(true);	//always generate hash attributes
		final ContentType defaultContentType=ContentType.getInstance(outputContentType.getPrimaryType(), outputContentType.getSubType(), new NameValuePair<String, String>(ContentType.CHARSET_PARAMETER, UTF_8));	//default to text/plain encoded in UTF-8
		response.setContentType(defaultContentType.toString());	//initialize the default content type and encoding
		HTTPServlets.setContentLanguage(response, session.getLocale());	//set the response content language
	}

	/**@return The character encoding currently used for the text output.*/
	public CharacterEncoding getOutputCharacterEncoding()
	{
		return new CharacterEncoding(getResponse().getCharacterEncoding(), NO_BOM);	//return the current output character encoding
	}

	/**@return The current content type of the text output.*/
	public ContentType getOutputContentType() {return outputContentType;}

	/**Sets the content type of the text output.
	This implementation removes all parameters and adds a character set parameter of the current encoding.
	@param contentType The content type of the text output.
	*/
	public void setOutputContentType(final ContentType contentType)
	{
			//default to text/plain encoded in UTF-8 replace the charset parameter with the currently set character set TODO change to really just replace one parameter, instead of removing all others
		this.outputContentType=ContentType.getInstance(contentType.getPrimaryType(), contentType.getSubType(), new NameValuePair<String, String>(ContentType.CHARSET_PARAMETER, getOutputCharacterEncoding().toString()));
		getResponse().setContentType(this.outputContentType.toString());	//set the content type of the response, including the current character set
	}

}
