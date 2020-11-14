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

package io.guise.framework.platform.web;

import java.io.IOException;
import java.net.URI;

import static java.util.Objects.*;

import javax.servlet.http.*;

import com.globalmentor.log.Log;
import com.globalmentor.net.ContentType;
import com.globalmentor.net.URIPath;

import io.guise.framework.*;

import static com.globalmentor.net.URIs.*;
import static com.globalmentor.servlet.http.HTTPServlets.*;
import static io.guise.framework.platform.web.WebPlatform.*;

/**
 * Information about the Guise request of an HTTP servlet.
 * @author Garret Wilson
 */
public class HTTPServletGuiseRequest {

	/** The HTTP servlet request. */
	private final HttpServletRequest httpServletRequest;

	/** @return The HTTP servlet request. */
	public HttpServletRequest getHTTPServletRequest() {
		return httpServletRequest;
	}

	/** The HTTP servlet response. */
	//TODO del	private final HttpServletResponse httpServletResponse;

	/** @return The HTTP servlet response. */
	//TODO del		public HttpServletResponse getHTTPServletResponse() {return httpServletResponse;}

	/** The requested plain depict URI. */
	private final URI depictURI;

	/** @return The requested plain depict URI. */
	public URI getDepictURI() {
		return depictURI;
	}

	/** The requested path in its logical form (which may be different than that which appears in the request URI). */
	private final URIPath navigationPath;

	/** @return The requested path in its logical form (which may be different than that which appears in the request URI). */
	public URIPath getNavigationPath() {
		return navigationPath;
	}

	/** Whether the request is for a Guise reserved path. */
	private final boolean requestPathReserved;

	/** @return Whether the request is for a Guise reserved path. */
	public boolean isRequestPathReserved() {
		return requestPathReserved;
	}

	/** The content type of the request, or <code>null</code> if not known. */
	private final ContentType requestContentType;

	/** @return The content type of the request, or <code>null</code> if not known. */
	public ContentType getRequestContentType() {
		return requestContentType;
	}

	/** Whether this is an AJAX request. */
	private final boolean ajax;

	/** @return Whether this is an AJAX request. */
	public boolean isAJAX() {
		return ajax;
	}

	/** The bookmark represented by the request, or <code>null</code> if no bookmark is contained in the request. */
	private final Bookmark bookmark;

	/** @return The bookmark represented by the request, or <code>null</code> if no bookmark is contained in the request. */
	public Bookmark getBookmark() {
		return bookmark;
	}

	/** The plain URI of the referring location, or <code>null</code> if there is no referrer. */
	private final URI referrerURI;

	/** @return The plain URI of the referring location, or <code>null</code> if there is no referrer. */
	public URI getReferrerURI() {
		return referrerURI;
	}

	/**
	 * Extracts the bookmark contained in the given request URL.
	 * @param request The HTTP request object.
	 * @return The bookmark represented by the request, or <code>null</code> if no bookmark is contained in the request.
	 */
	/*TODO del if not needed
			protected static Bookmark getBookmark(final HttpServletRequest request)
			{
				final String queryString=request.getQueryString();	//get the query string from the request
				if(queryString!=null && queryString.length()>0) {	//if there is a query string (Tomcat 5.5.16 returns an empty string for no query, even though the Java Servlet specification 2.4 says that it should return null; this is fixed in Tomcat 6)
		//TODO del Log.trace("just got query string from request, length", queryString.length(), "content", queryString);
					return new Bookmark(String.valueOf(QUERY_SEPARATOR)+queryString);	//construct a new bookmark, preceding the string with a query indicator
				}
				else {	//if there is no query string, there is no bookmark
		//TODO del Log.trace("just got null query string from request");
					return null;	//indicate that there is no bookmark information
				}
			}
	*/

	/**
	 * Creates an HTTP request.
	 * @param request The HTTP request.
	 * @param guiseContainer The Guise container.
	 * @param guiseApplication The Guise application.
	 * @throws IOException if there is an error reading or writing data.
	 */
	public HTTPServletGuiseRequest(final HttpServletRequest request, /*TODO del final HttpServletResponse response, */final GuiseContainer guiseContainer,
			final GuiseApplication guiseApplication) throws IOException {
		this.httpServletRequest = requireNonNull(request, "HTTP servlet request cannot be null.");
		//TODO del		this.httpServletResponse=requireNonNull(response, "HTTP servlet request cannot be null.");
		depictURI = URI.create(request.getRequestURL().toString()); //get the URI of the current request
		final String queryString = request.getQueryString(); //get the query string from the request
		bookmark = queryString != null && queryString.length() > 0 ? new Bookmark(String.valueOf(QUERY_SEPARATOR) + queryString) : null; //create a bookmark if there is a query string (Tomcat 5.5.16 returns an empty string for no query, even though the Java Servlet specification 2.4 says that it should return null; this is fixed in Tomcat 6)
		Log.debug("servicing Guise request with request URI:", depictURI, "bookmark:", bookmark);
		final String rawPathInfo = getRawPathInfo(request); //get the raw path info
		assert isPathAbsolute(rawPathInfo) : "Expected absolute path info, received " + rawPathInfo; //the Java servlet specification says that the path info will start with a '/'
		URI referrerURI = getRefererURI(request); //get the referring URI, if any
		if(referrerURI != null) { //if there is a referring URI
			referrerURI = getPlainURI(referrerURI); //make sure the referrer is plain
		}
		this.referrerURI = referrerURI; //save the referring URI
		URIPath requestPath = URIPath.of(rawPathInfo.substring(1)); //remove the beginning slash to get the request path from the path info
		requestPathReserved = requestPath.toString().startsWith(GuiseApplication.GUISE_RESERVED_BASE_PATH.toString()); //see if this is a request for a Guise reserved path (e.g. a public resource or a temporary resource)
		navigationPath = guiseApplication.getNavigationPath(depictURI); //get the logical version of the the path
		final String contentTypeString = request.getContentType(); //get the request content type
		requestContentType = contentTypeString != null ? ContentType.parse(contentTypeString) : null; //create a content type object from the request content type, if there is one
		ajax = requestContentType != null && GUISE_AJAX_REQUEST_MEDIA_TYPE.hasBaseType(requestContentType); //see if this is a Guise AJAX request
	}

	@Override
	public String toString() {
		return getDepictURI().toString() + " (" + getNavigationPath() + ") " + getBookmark();
	}
}
