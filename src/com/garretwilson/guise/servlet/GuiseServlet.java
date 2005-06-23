package com.garretwilson.guise.servlet;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.garretwilson.guise.*;
import com.garretwilson.guise.component.*;
import com.garretwilson.guise.context.*;
import com.garretwilson.guise.context.text.*;
import com.garretwilson.guise.controller.Controller;
import com.garretwilson.guise.controller.text.xml.xhtml.*;
import com.garretwilson.guise.test.HomeFrame;
import com.garretwilson.net.http.*;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.getRawPathInfo;

/**The servlet that controls a Guise web applications. 
@author Garret Wilson
*/
public class GuiseServlet extends BasicHTTPServlet
{

	/**@return The servlet's Guise context.*/
	private final Guise<DefaultHTTPServletGuiseContext> guise;

		/**@return The servlet's Guise context.*/
		protected Guise<DefaultHTTPServletGuiseContext> getGuise() {return guise;}
	
	/**The map binding frame types to context-relative paths.*/
	private final Map<String, Class<? extends Frame>> pathFrameBindingMap=new HashMap<String, Class<? extends Frame>>();

		/**Binds a frame type to a particular context-relative path.
		Any existing binding for the given context-relative path is replaced.
		@param path The context-relative path to which the frame should be bound.
		@param frameClass The class of frame to render for this particular context-relative path.
		@return The frame previously bound to the given context-relative path, or <code>null</code> if no frame was previously bound to the path.
		@exception NullPointerException if the path and/or the frame is null.
		*/
		protected Class<? extends Frame> bindFrame(final String path, final Class<? extends Frame> frameClass)
		{
			return pathFrameBindingMap.put(checkNull(path, "Path cannot be null."), checkNull(frameClass, "Type cannot be null."));	//store the binding
		}

		/**Determines the class of frame bound to the given context-relative path.
		@param path The address for which a frame should be retrieved.
		@return The type of frame bound to the given address. 
		 */
		protected Class<? extends Frame> getBoundFrameClass(final String path)
		{
			return pathFrameBindingMap.get(path);	//return the bound frame type, if any
		}

	/**Default constructor.*/
	public GuiseServlet()
	{
		guise=new AbstractGuise<DefaultHTTPServletGuiseContext>(){};	//TODO fix
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
		final DefaultHTTPServletGuiseContext guiseContext=new DefaultHTTPServletGuiseContext(getGuise(), request, response);	//create a new Guise context
		final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
		Debug.trace("raw path info", rawPathInfo);
		final Frame frame;
		final Class<? extends Frame> frameClass=getBoundFrameClass(rawPathInfo);	//see which frame we should show for this path
		if(frameClass!=null)	//if we found a frame class for this address
		{
			try
			{
				frame = frameClass.getConstructor(String.class).newInstance("testFrame");
			}
			catch (NoSuchMethodException e)
			{
				Debug.error(e);
				throw new ServletException(e);
			}
			catch (InvocationTargetException e)
			{
				Debug.error(e);
				throw new ServletException(e);
			}
			catch (InstantiationException e)
			{
				Debug.error(e);
				throw new ServletException(e);
			}
			catch (IllegalAccessException e)
			{
				Debug.error(e);
				throw new ServletException(e);
			}
		}
		else	//if we have no frame type for this address
		{
			throw new HTTPNotFoundException("Not found: "+request.getRequestURL());
		}
		frame.updateModel(guiseContext);
		frame.updateView(guiseContext);
	}

}
