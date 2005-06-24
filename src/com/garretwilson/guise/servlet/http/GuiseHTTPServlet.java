package com.garretwilson.guise.servlet.http;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import static java.util.Collections.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.garretwilson.guise.*;
import com.garretwilson.guise.component.*;
import com.garretwilson.guise.context.*;
import com.garretwilson.guise.context.text.*;
import com.garretwilson.guise.controller.Controller;
import com.garretwilson.guise.controller.text.xml.xhtml.*;
import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.guise.test.HomeFrame;
import com.garretwilson.io.InputStreamUtilities;
import com.garretwilson.io.OutputStreamUtilities;
import com.garretwilson.net.http.*;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.getRawPathInfo;

/**The servlet that controls a Guise web applications. 
@author Garret Wilson
*/
public class GuiseHTTPServlet extends BasicHTTPServlet
{

	/**@return The servlet's Guise context.*/
	private final HTTPServletGuise guise;

		/**@return The servlet's Guise context.*/
		protected HTTPServletGuise getGuise() {return guise;}

	/**The synchronized map of Guise sessions weakly keyed to HTTP sessions.*/
	private final Map<HttpSession, HTTPGuiseSession> guiseSessionMap=synchronizedMap(new WeakHashMap<HttpSession, HTTPGuiseSession>());

	/**Retrieves a session for the given HTTP session.
	If there is currently no Guise session for this HTTP session, one will be created.
	@param httpSession The HTTP session for which a Guise session should be retrieved. 
	@return The Guise session associated with the provided HTTP session.
	*/
	protected HTTPGuiseSession getGuiseSession(final HttpSession httpSession)
	{
		synchronized(guiseSessionMap)	//don't allow others to access the session map while we access it
		{
			HTTPGuiseSession guiseSession=guiseSessionMap.get(httpSession);	//get the Guise session associated with the HTTP session
			if(guiseSession==null)	//if there is no Guise session associated with this HTTP session
			{
				guiseSession=new HTTPGuiseSession(getGuise(), httpSession);	//create a new Guise session
				guiseSessionMap.put(httpSession, guiseSession);	//store the Guise session in the map, keyed to the HTTP session
			}
			return guiseSession;	//return the Guise session we found or created
		}
	}

	/**Default constructor.*/
	public GuiseHTTPServlet()
	{
		guise=new HTTPServletGuise();	//create a new Guise instance
	}
		
	/**Initializes the servlet.
	@param servletConfig The servlet configuration. 
	@exception ServletException if there is a problem initializing.
	*/
	public void init(final ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);	//do the default initialization
		getGuise().registerRenderStrategy(ActionControl.class, XHTMLButtonController.class);
		getGuise().registerRenderStrategy(Label.class, XHTMLLabelController.class);
		getGuise().registerRenderStrategy(Frame.class, XHTMLFrameController.class);
		getGuise().registerRenderStrategy(Panel.class, XHTMLPanelController.class);
		getGuise().registerRenderStrategy(ValueControl.class, XHTMLInputController.class);
	}

	/**Services the POST method.
	This version delegates to <code>doGet()</code>.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  @see #doGet(HttpServletRequest, HttpServletResponse)
  */
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);	//delegate to the GET method servicing
	}

	/**Services the GET method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final HTTPServletGuise guise=getGuise();	//get the Guise instance
		final HTTPGuiseSession guiseSession=getGuiseSession(request.getSession());	//gets the current HTTP session and retrieves the corresponding Guise session
		final HTTPServletGuiseContext guiseContext=new HTTPServletGuiseContext(guiseSession, request, response);	//create a new Guise context
		final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
		if(rawPathInfo.endsWith(".css"))	//TODO fix
		{
			response.setContentType("text/css");
			final File file=new File(getServletContext().getRealPath(rawPathInfo));	//TODO fix all this; temporary hack
			final InputStream inputStream=new BufferedInputStream(new FileInputStream(file));
			try
			{
				OutputStreamUtilities.write(inputStream, response.getOutputStream());	//TODO fix
			}
			finally
			{
				inputStream.close();
			}
			return;
		}
		Debug.trace("raw path info", rawPathInfo);
		try
		{
			final Frame frame=guiseSession.getBoundFrame(rawPathInfo);	//get the frame bound to the requested path
			if(frame!=null)	//if we found a frame class for this address
			{
				frame.updateModel(guiseContext);	//tell the frame to update its model
				frame.updateView(guiseContext);		//tell the frame to update its view
			}
			else	//if we have no frame type for this address
			{
				throw new HTTPNotFoundException("Not found: "+request.getRequestURL());
			}
		}
		catch(final NoSuchMethodException noSuchMethodException)	//catch and rethrow instantiation errors from retrieving/creating the frame
		{
			throw new ServletException(noSuchMethodException);
		}
		catch(final InvocationTargetException invocationTargetException)
		{
			throw new ServletException(invocationTargetException);
		}
		catch(final InstantiationException instantiationException)
		{
			throw new ServletException(instantiationException);
		}
		catch(final IllegalAccessException illegalAccessException)
		{
			throw new ServletException(illegalAccessException);
		}
	}

}
