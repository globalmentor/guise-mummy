package com.garretwilson.guise.servlet.http;

import java.util.*;
import static java.util.Collections.*;

import javax.servlet.http.*;

/**Manages Guise sessions for an HTTP servlet.
Guise sessions are created and released in conjunction with associated HTTP servlet sessions.
@author Garret Wilson
@see ServletGuise
*/
public class HTTPGuiseSessionManager implements HttpSessionListener
{

	/**The synchronized map of Guise sessions keyed to HTTP sessions.*/
	private static final Map<HttpSession, HTTPGuiseSession> guiseSessionMap=synchronizedMap(new HashMap<HttpSession, HTTPGuiseSession>());

	/**Retrieves a session for the given HTTP session.
	This method can only be accessed by classes in the same package.
	@param httpSession The HTTP session for which a Guise session should be retrieved. 
	@return The Guise session associated with the provided HTTP session.
	@exception IllegalArgumentException if the provided HTTP session is not a session from this web application or the HTTP session has been invalidated, and there is therefore no corresponding Guise session.
	*/
	static HTTPGuiseSession getGuiseSession(final HttpSession httpSession)
	{
		final HTTPGuiseSession guiseSession=guiseSessionMap.get(httpSession);	//get the Guise session associated with the HTTP session
		if(guiseSession==null)	//if no Guise session is associated with the given HTTP session
		{
			throw new IllegalArgumentException("Unknown or invalid HTTP session "+httpSession);
		}
		return guiseSession;	//return the Guise session
	}

	/**Called when an HTTP session is created.
	This implementation creates and stores a corresponding Guise session.
	@param httpSessionEvent Information regarding the created HTTP session.
	*/
	public void sessionCreated(final HttpSessionEvent httpSessionEvent)
	{
		final HTTPServletGuise guise=HTTPServletGuise.getInstance();	//get the Guise instance
		final HttpSession httpSession=httpSessionEvent.getSession();	//get the HTTP session just created
		final HTTPGuiseSession guiseSession=new HTTPGuiseSession(guise, httpSession);	//create a new Guise session associated with the HTTP session
		assert !guiseSessionMap.containsKey(httpSession) : "Guise session map should not already contain a Guise session associated with this HTTP session.";
		guiseSessionMap.put(httpSession, guiseSession);	//store the Guise session in the map, keyed to the HTTP session		
	}

	/**Called when an HTTP session is invalidated.
	This implementation removes the corresponding Guise session.
	@param httpSessionEvent Information regarding the invalidated HTTP session.
	*/
	public void sessionDestroyed(final HttpSessionEvent httpSessionEvent)
	{
		final HTTPServletGuise guise=HTTPServletGuise.getInstance();	//get the Guise instance
		final HttpSession httpSession=httpSessionEvent.getSession();	//get the HTTP session just invalidated
		assert guiseSessionMap.containsKey(httpSession) : "Guise session map did not contain a Guise session associated with the HTTP session that was just invalidated.";
		guiseSessionMap.remove(httpSession);	//remove from the map the Guise session that was keyed to the HTTP session		
	}
	
}
