package com.garretwilson.guise.servlet.http;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import static java.util.Collections.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.garretwilson.guise.*;
import com.garretwilson.guise.application.AbstractGuiseApplication;
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
import static com.garretwilson.servlet.http.HttpServletUtilities.*;

/**The servlet that controls a Guise web applications. 
@author Garret Wilson
*/
public class GuiseHTTPServlet extends BasicHTTPServlet
{

	/**@return The global HTTP servlet Guise instance.
	This is a convenience method.
	@see HTTPServletGuise#getInstance()
	*/
/*TODO fix
	protected HTTPServletGuise getGuise()
	{
		return HTTPServletGuise.getInstance();	//return the global HTTP servlet Guise instance
	}
*/

	/**@return The Guise application controlled by this servlet.*/
	private final HTTPServletGuiseApplication guiseApplication;

		/**The Guise application controlled by this servlet.*/
		protected HTTPServletGuiseApplication getGuiseApplication() {return guiseApplication;}

	/**The factory method to create a Guise application.
	Subclasses can override this method to create a specialized application type. 
	@return A new Guise application object.
	*/
	protected HTTPServletGuiseApplication createGuiseApplication()
	{
		return new HTTPServletGuiseApplication();
	}

	/**Default constructor.
	Creates a single Guise application.
	@see #createGuiseApplication()
	*/
	public GuiseHTTPServlet()
	{
		guiseApplication=createGuiseApplication();	//create a store a Guise application
	}
		
	/**Initializes the servlet.
	@param servletConfig The servlet configuration. 
	@exception ServletException if there is a problem initializing.
	*/
	public void init(final ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);	//do the default initialization
		HTTPServletGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		guiseApplication.registerRenderStrategy(ActionControl.class, XHTMLButtonController.class);
		guiseApplication.registerRenderStrategy(Label.class, XHTMLLabelController.class);
		guiseApplication.registerRenderStrategy(Frame.class, XHTMLFrameController.class);
		guiseApplication.registerRenderStrategy(Panel.class, XHTMLPanelController.class);
		guiseApplication.registerRenderStrategy(ValueControl.class, XHTMLInputController.class);
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
		final HTTPServletGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		final HTTPGuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseApplication, request);	//retrieves the Guise session for this application and request
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

	/**A Guise application for Guise HTTP servlets.
	@author Garret Wilson
	*/
	protected class HTTPServletGuiseApplication extends AbstractGuiseApplication<HTTPServletGuiseContext>
	{
		/**The synchronized map of Guise sessions keyed to HTTP sessions.*/
		private final Map<HttpSession, HTTPGuiseSession> guiseSessionMap=synchronizedMap(new HashMap<HttpSession, HTTPGuiseSession>());

		/**Retrieves a Guise session for the given HTTP session.
		A Guise session will be created if none is currently associated with the given HTTP session.
		This method can only be accessed by classes in the same package.
		This method should only be called by HTTP Guise session manager.
		@param httpSession The HTTP session for which a Guise session should be retrieved. 
		@return The Guise session associated with the provided HTTP session.
		@see HTTPGuiseSessionManager
		*/
		HTTPGuiseSession getGuiseSession(final HttpSession httpSession)
		{
			synchronized(guiseSessionMap)	//don't allow anyone to modify the map of sessions while we access it
			{
				HTTPGuiseSession guiseSession=guiseSessionMap.get(httpSession);	//get the Guise session associated with the HTTP session
				if(guiseSession==null)	//if no Guise session is associated with the given HTTP session
				{
					guiseSession=createGuiseSession(httpSession);	//create a new Guise session
					guiseSessionMap.put(httpSession, guiseSession);	//associate the Guise session with the HTTP session
				}
				return guiseSession;	//return the Guise session
			}
		}

		/**Removes the Guise session for the given HTTP session.
		This method can only be accessed by classes in the same package.
		This method should only be called by HTTP Guise session manager.
		@param httpSession The HTTP session which should be removed along with its corresponding Guise session. 
		@return The Guise session previously associated with the provided HTTP session, or <code>null</code> if no Guise session was associated with the given HTTP session.
		@see HTTPGuiseSessionManager
		*/
		HTTPGuiseSession removeGuiseSession(final HttpSession httpSession)
		{
			return guiseSessionMap.remove(httpSession);	//remove the HTTP session and Guise session association
		}

		/**Factory method to create a Guise session from an HTTP session.
		@param httpSession The HTTP session for which a Guise session should be created.
		@return A new Guise session corresponding to the given HTTP session.
		*/ 
		protected HTTPGuiseSession createGuiseSession(final HttpSession httpSession)
		{
			return new HTTPGuiseSession(this, httpSession);	//create a default HTTP guise session
		}
	
	}
}
