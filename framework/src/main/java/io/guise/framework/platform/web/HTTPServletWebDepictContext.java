/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform.web;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;

import static java.util.Objects.*;

import javax.servlet.http.HttpServletResponse;

import com.globalmentor.net.MediaType;
import com.globalmentor.servlet.http.HTTPServlets;
import com.globalmentor.text.Text;
import com.globalmentor.xml.spec.NsQualifiedName;

import io.guise.framework.Destination;
import io.guise.framework.GuiseSession;

import static io.guise.framework.platform.web.WebPlatform.*;
import static java.nio.charset.StandardCharsets.*;

/**
 * A web depict context of an HTTP servlet.
 * <p>
 * The output stream defaults to <code>text/plain</code> encoded in <code>UTF-8</code>.
 * </p>
 * @author Garret Wilson
 */
public class HTTPServletWebDepictContext extends AbstractWebDepictContext {

	/** The Guise HTTP request. */
	private final HTTPServletGuiseRequest guiseRequest;

	/** @return The Guise HTTP request. */
	protected HTTPServletGuiseRequest getGuiseRequest() {
		return guiseRequest;
	}

	/** The HTTP servlet response. */
	private final HttpServletResponse response;

	/** @return The HTTP servlet response. */
	protected HttpServletResponse getResponse() {
		return response;
	}

	/** The current full absolute URI for this depiction, including any query. */
	private final URI depictURI;

	@Override
	public URI getDepictionURI() {
		return depictURI;
	}

	/** The current content type of the output. */
	private MediaType outputContentType = MediaType.of(MediaType.TEXT_PRIMARY_TYPE, Text.PLAIN_SUBTYPE); //default to text/plain

	/** The qualified name to use for the attribute hash attribute. */
	private final NsQualifiedName attributeHashAttributeQualifiedName;

	@Override
	protected NsQualifiedName getAttributeHashAttributeQualifiedName() {
		return attributeHashAttributeQualifiedName;
	}

	/** The qualified name to use for the content hash attribute. */
	private final NsQualifiedName contentHashAttributeQualifiedName;

	@Override
	protected NsQualifiedName getContentHashAttributeQualifiedName() {
		return contentHashAttributeQualifiedName;
	}

	/**
	 * Constructor.
	 * @param guiseRequest Guise request information.
	 * @param response The HTTP servlet response.
	 * @param session The Guise user session of which this context is a part.
	 * @param destination The destination with which this context is associated.
	 * @throws NullPointerException if the given Guise request, session, and/or destination is <code>null</code>.
	 * @throws IOException If there was an I/O error loading a needed resource.
	 */
	public HTTPServletWebDepictContext(final HTTPServletGuiseRequest guiseRequest, final HttpServletResponse response, final GuiseSession session,
			final Destination destination) throws IOException {
		super(session, destination); //construct the parent class
		this.guiseRequest = requireNonNull(guiseRequest, "Guise request cannot be null.");
		this.response = requireNonNull(response, "Response cannot be null.");
		//TODO decide if we want this to include parameters or not		this.navigationURI=URI.create(request.getRequestURL().toString());	//create the absolute navigation URI from the HTTP requested URL
		this.depictURI = HTTPServlets.getRequestURI(guiseRequest.getHTTPServletRequest()); //get the depiction URI, containing any query
		/*TODO del if not needed
				final String referrer=getReferer(request);	//get the request referrer, if any
				referrerURI=referrer!=null ? getPlainURI(URI.create(referrer)) : null;	//get a plain URI version of the referrer, if there is a referrer
		*/
		/*TODO del
				final String contentTypeString=request.getContentType();	//get the request content type
				final ContentType contentType=contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object from the request content type, if there is one
		*/
		attributeHashAttributeQualifiedName = new NsQualifiedName(GUISE_ML_NAMESPACE_URI, getQualifiedName(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_ATTRIBUTE_HASH));
		contentHashAttributeQualifiedName = new NsQualifiedName(GUISE_ML_NAMESPACE_URI, getQualifiedName(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_CONTENT_HASH));
		setHashAttributesGenerated(true); //always generate hash attributes
		final MediaType defaultContentType = MediaType.of(outputContentType.getPrimaryType(), outputContentType.getSubType(),
				MediaType.Parameter.of(MediaType.CHARSET_PARAMETER, UTF_8.name())); //default to text/plain encoded in UTF-8
		response.setContentType(defaultContentType.toString()); //initialize the default content type and encoding
		HTTPServlets.setContentLanguage(response, session.getLocale()); //set the response content language
	}

	@Override
	public Charset getOutputCharset() {
		return Charset.forName(getResponse().getCharacterEncoding()); //return the character encoding indicated by the HTTP servlet response
	}

	@Override
	public MediaType getOutputContentType() {
		return outputContentType;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation removes all parameters and adds a character set parameter of the current encoding.
	 * </p>
	 */
	@Override
	public void setOutputContentType(final MediaType contentType) {
		//TODO change to really just replace one parameter, instead of removing all others
		this.outputContentType = MediaType.of(contentType.getPrimaryType(), contentType.getSubType(),
				MediaType.Parameter.of(MediaType.CHARSET_PARAMETER, getOutputCharset().name()));
		getResponse().setContentType(this.outputContentType.toString()); //set the content type of the response, including the current character set
	}

}
