/*
 * Copyright © 2005-2009 GlobalMentor, Inc. <https://www.globalmentor.com/>

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

import java.util.*;

import static java.util.Collections.*;

import javax.servlet.http.*;

import io.guise.framework.GuiseApplication;
import io.guise.framework.GuiseSession;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.servlet.http.HTTPServlets.*;

/**
 * Manages Guise sessions for an HTTP web application. Guise sessions are created and released in conjunction with associated HTTP servlet sessions. There may
 * be multiple Guise applications within one web application.
 * <p>
 * A HTTP request may override any HTTP session identified by the request by exlicitly identifying the Guise session UUID using the
 * {@link WebPlatform#GUISE_SESSION_UUID_URI_QUERY_PARAMETER} parameter.
 * </p>
 * @author Garret Wilson
 * @see HTTPServletGuiseContainer
 */
public class HTTPServletGuiseSessionManager implements HttpSessionListener {

	/** The synchronized map of Guise containers keyed to HTTP sessions. */
	private static final Map<HttpSession, HTTPServletGuiseContainer> guiseContainerMap = synchronizedMap(new HashMap<HttpSession, HTTPServletGuiseContainer>());

	private static HttpSession spiderSession = null; //TODO fix to be separate for each application; testing

	/**
	 * Retrieves a session for the given HTTP request, creating a session if necessary. If a {@link WebPlatform#GUISE_SESSION_UUID_URI_QUERY_PARAMETER} parameter
	 * is present in the HTTP request, it will be used to directly look up a Guise session, ignoring any identified HTTP session. If there is no Guise session
	 * matching a specified UUID, the Guise session will be retrieved normally.
	 * @param guiseContainer The Guise container that owns the application.
	 * @param guiseApplication The application to install to own the created session..
	 * @param httpRequest The HTTP request with which the Guise session is to be associated.
	 * @return The Guise session associated with the provided HTTP session.
	 * @throws IllegalArgumentException if the provided HTTP session is not a session from this web application or the HTTP session has been invalidated, and
	 *           there is therefore no corresponding Guise session.
	 */
	protected static GuiseSession getGuiseSession(final HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication,
			final HttpServletRequest httpRequest) {
		return getGuiseSession(guiseContainer, guiseApplication, httpRequest, true); //get a Guise session, creating one if necessary
	}

	/**
	 * Retrieves a session for the given HTTP request. If a {@link WebPlatform#GUISE_SESSION_UUID_URI_QUERY_PARAMETER} parameter is present in the HTTP request,
	 * it will be used to directly look up a Guise session, ignoring any identified HTTP session. If there is no Guise session matching a specified UUID, the
	 * Guise session will be retrieved normally.
	 * @param guiseContainer The Guise container that owns the application.
	 * @param guiseApplication The application to install to own the created session..
	 * @param httpRequest The HTTP request with which the Guise session is to be associated.
	 * @param createSession Whether a Guise session should be created if one does not already exist.
	 * @return The Guise session associated with the provided HTTP request, or <code>null</code> if there is no Guise session and session creation was not
	 *         requested.
	 * @throws IllegalArgumentException if the provided HTTP session is not a session from this web application or the HTTP session has been invalidated, and
	 *           there is therefore no corresponding Guise session.
	 */
	protected static GuiseSession getGuiseSession(final HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication,
			final HttpServletRequest httpRequest, final boolean createSession) {
		final String guiseSessionUUIDString = httpRequest.getParameter(WebPlatform.GUISE_SESSION_UUID_URI_QUERY_PARAMETER); //see if a Guise session UUID is specified
		if(guiseSessionUUIDString != null) { //if a Guise session UUID is specified
			final UUID guiseSessionUUID = UUID.fromString(guiseSessionUUIDString); //create a UUID from the string
			final GuiseSession guiseSession = guiseApplication.getSession(guiseSessionUUID); //see if the application knows of such a session
			if(guiseSession != null) { //if we found the session
				return guiseSession; //return the session we found
			}
		}
		//TODO del Log.trace("requested session ID: ", httpRequest.getRequestedSessionId());
		HttpSession httpSession = httpRequest.getSession(false); //get the current HTTP session from the HTTP request, if there is a session
		if(httpSession == null) { //if there is no session yet for this request, we'll create one
			final Map<String, Object> userAgentProperties = getUserAgentProperties(httpRequest); //get user agent-related properties TODO have the method cache these in the request
			final String userAgentName = asInstance(userAgentProperties.get(USER_AGENT_NAME_PROPERTY), String.class).orElse(null); //get the user agent name
			final boolean isSpider = UNSESSIONED_SPIDER_USER_AGENT_NAMES.contains(userAgentName); //see if the user agent is a spider that does not support sessions
			boolean useSpiderSession = isSpider && spiderSession != null; //we'll use the spider session if the user agent is a spider and we have a spider session
			if(useSpiderSession) { //even if the user agent is a spider, make sure the spider session is valid
				try {
					if((System.currentTimeMillis() - spiderSession.getLastAccessedTime()) / 1000 > spiderSession.getMaxInactiveInterval() - 2) //if the existing spider session is almost expired 
					{
						useSpiderSession = false; //don't use the spider session
					}
				} catch(final IllegalStateException illegalStateException) { //if the spider session is already invalidated (the servlet API seems to allow no way to check this explicitly for arbitrary sessions) 
					useSpiderSession = false;
				}
			}
			if(useSpiderSession) { //if this is a spider and we have a spider session
				//TODO del Debug.info("using spider session for user agent name", userAgentName);
				httpSession = spiderSession; //use the spider session
			} else { //if this isn't a spider, we have no existing spider session, or the existing spider session is almost expired, create a session
				//TODO del Debug.info("creating session for user agent name", userAgentName);
				httpSession = httpRequest.getSession(true); //create a new HTTP session for the HTTP request
				//TODO is there a race condition here? could two requests requesting the same session happen concurrently?
				guiseContainerMap.put(httpSession, guiseContainer); //store our Guise container so we'll know with which container this session is associated (this servlet may serve many Guise applications in many Guise containers in the web application)
				if(isSpider) { //if we just created a session for a spider
					//TODO del Debug.info("storing this session as a spider session");
					spiderSession = httpSession; //store the spider session for future sharing
				}
			}
		}
		return guiseContainer.getGuiseSession(guiseApplication, httpRequest, httpSession); //ask the Guise application for a Guise session corresponding to the HTTP session
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation does nothing, as there is no way to tell with which Guise container and Guise application application a new Guise application should be
	 * associated.
	 * </p>
	 */
	@Override
	public void sessionCreated(final HttpSessionEvent httpSessionEvent) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation removes the corresponding Guise session.
	 * </p>
	 */
	@Override
	public void sessionDestroyed(final HttpSessionEvent httpSessionEvent) {
		synchronized(guiseContainerMap) { //don't allow anyone to modify our map of applications while we access it
			final HttpSession httpSession = httpSessionEvent.getSession(); //get the HTTP session just invalidated
			final HTTPServletGuiseContainer guiseContainer = guiseContainerMap.get(httpSession); //see if we have a Guise container associated with this HTTP request
			if(guiseContainer != null) { //if we know the Guise container associated with this HTTP request
				guiseContainerMap.remove(httpSession); //remove the association between this HTTP session and the container
				final Set<GuiseSession> guiseSessions = guiseContainer.removeGuiseSessions(httpSession); //remove the Guise sessions associated with the HTTP session in the application
				assert !guiseSessions.isEmpty() : "Guise container associated with HTTP session unexpectedly did not have any associated Guise sessions.";
			}
		}
	}
}
