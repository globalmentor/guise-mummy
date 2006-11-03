package com.guiseframework.platform.web;

import java.util.*;

import static java.util.Collections.*;

import javax.servlet.http.*;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;

import com.garretwilson.util.Debug;
import com.guiseframework.GuiseApplication;
import com.guiseframework.GuiseSession;

/**Manages Guise sessions for an HTTP web application.
Guise sessions are created and released in conjunction with associated HTTP servlet sessions.
There may be multiple Guise applications within one web application.
@author Garret Wilson
@see HTTPServletGuiseContainer
*/
public class HTTPGuiseSessionManager implements HttpSessionListener
{

	/**The synchronized map of Guise containers keyed to HTTP sessions.*/
	private static final Map<HttpSession, HTTPServletGuiseContainer> guiseContainerMap=synchronizedMap(new HashMap<HttpSession, HTTPServletGuiseContainer>());

	private static HttpSession spiderSession=null;	//TODO fix to be separate for each application; testing
	
	/**Retrieves a session for the given HTTP session.
	This method can only be accessed by classes in the same package.
	@param guiseContainer The Guise container that owns the application. 
	@param guiseApplication The application to install to own the created session..
	@param httpRequest The HTTP request with which the Guise session is to be associated. 
	@return The Guise session associated with the provided HTTP session.
	@exception IllegalArgumentException if the provided HTTP session is not a session from this web application or the HTTP session has been invalidated, and there is therefore no corresponding Guise session.
	*/
	protected static GuiseSession getGuiseSession(final HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication, final HttpServletRequest httpRequest)
	{
//TODO del Debug.trace("requested session ID: ", httpRequest.getRequestedSessionId());
		HttpSession httpSession=httpRequest.getSession(false);	//get the current HTTP session from the HTTP request, if there is a session
		if(httpSession==null)	//if there is no session yet for this request, we'll create one
		{
			final Map<String, Object> userAgentProperties=getUserAgentProperties(httpRequest);	//get user agent-related properties TODO have the method cache these in the request
			final String userAgentName=asInstance(userAgentProperties.get(USER_AGENT_NAME_PROPERTY), String.class);	//get the user agent name
			final boolean isSpider=UNSESSIONED_SPIDER_USER_AGENT_NAMES.contains(userAgentName);	//see if the user agent is a spider that does not support sessions
				//if this isn't a spider, we have no existing spider session, or the existing spider session is almost expired, create a session
			if(!isSpider || spiderSession==null || (spiderSession.getLastAccessedTime()-System.currentTimeMillis())/1000>spiderSession.getMaxInactiveInterval()-2)
			{
//TODO del Debug.info("creating session for user agent name", userAgentName);
				httpSession=httpRequest.getSession(true);	//create a new HTTP session for the HTTP request
					//TODO is there a race condition here? could two requests requesting the same session happen concurrently?
				guiseContainerMap.put(httpSession, guiseContainer);	//store our Guise container so we'll know with which container this session is associated (this servlet may serve many Guise applications in many Guise containers in the web application)
				if(isSpider)	//if we just created a session for a spider
				{
//TODO del Debug.info("storing this session as a spider session");
					spiderSession=httpSession;	//store the spider session for future sharing
				}
			}
			else	//if this is a spider and we have a spider session
			{
//TODO del Debug.info("using spider session for user agent name", userAgentName);
				httpSession=spiderSession;	//use the spider session
			}
		}
		return guiseContainer.getGuiseSession(guiseApplication, httpRequest, httpSession);	//ask the Guise application for a Guise session corresponding to the HTTP session
	}
	
	/**Called when an HTTP session is created.
	This implementation does nothing, as there is no way to tell with which Guise container and Guise application application a new Guise application should be associated.
	@param httpSessionEvent Information regarding the created HTTP session.
	@see #getGuiseSession(HTTPServletGuiseContainer, GuiseApplication, HttpServletRequest)
	*/
	public void sessionCreated(final HttpSessionEvent httpSessionEvent)
	{
	}

	/**Called when an HTTP session is invalidated.
	This implementation removes the corresponding Guise session.
	@param httpSessionEvent Information regarding the invalidated HTTP session.
	*/
	public void sessionDestroyed(final HttpSessionEvent httpSessionEvent)
	{
		synchronized(guiseContainerMap)	//don't allow anyone to modify our map of applications while we access it
		{
			final HttpSession httpSession=httpSessionEvent.getSession();	//get the HTTP session just invalidated
			final HTTPServletGuiseContainer guiseContainer=guiseContainerMap.get(httpSession);	//see if we have a Guise container associated with this HTTP request
			if(guiseContainer!=null)	//if we know the Guise container associated with this HTTP request
			{
				guiseContainerMap.remove(httpSession);	//remove the association between this HTTP session and the container
				final Set<GuiseSession> guiseSessions=guiseContainer.removeGuiseSessions(httpSession);	//remove the Guise sessions associated with the HTTP session in the application
				assert !guiseSessions.isEmpty() : "Guise container associated with HTTP session unexpectedly did not have any associated Guise sessions.";
			}
		}
	}
}
