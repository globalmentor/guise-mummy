package com.guiseframework.platform;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static java.util.Collections.*;

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.urf.URFResource;
import com.guiseframework.*;
import com.guiseframework.theme.Theme;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract encapsulation of information related to the current depiction.
@author Garret Wilson
*/
public abstract class AbstractDepictContext extends BoundPropertyObject implements DepictContext
{

	/**The Guise user session of which this context is a part.*/
	private final GuiseSession session;

		/**@return The Guise user session of which this context is a part.*/
		public GuiseSession getSession() {return session;}

	/**The platform on which Guise objects are depicted.*/
	private final Platform platform;

		/**@return The platform on which Guise objects are depicted.*/
		public Platform getPlatform() {return platform;}

	/**The destination with which this context is associated.*/
	private final Destination destination;

		/**@return The destination with which this context is associated.*/
		public Destination getDestination() {return destination;}

	/**The URIs of the styles for this context, in order.*/
	private final Iterable<URI> styles;

		/**Retrieves styles for this context.
		Styles appear in the following order:
		<ol>
			<li>theme styles (from most distant parent to current theme)</li>
			<li>application style</li>
			<li>destination style</li>
		</ol>
		@return The URIs of the styles for this context, in order.
		*/
		public Iterable<URI> getStyles() {return styles;}

	/**Guise session constructor.
	@param session The Guise user session of which this context is a part.
	@param destination The destination with which this context is associated.
	@exception NullPointerException if the given session and/or destination is null.
	@exception IOException If there was an I/O error loading a needed resource.
	*/
	public AbstractDepictContext(final GuiseSession session, final Destination destination) throws IOException
	{
		this.session=checkInstance(session, "Session cannot be null.");	//save the Guise session
		this.destination=checkInstance(destination, "Destination cannot be null.");	//save the destination
		this.platform=session.getPlatform();	//save the platform
		final GuiseApplication application=session.getApplication();	//get the application
		final List<URI> styleURIs=new ArrayList<URI>();	//create a new list to hold styles
			//theme styles
		Theme theme=session.getTheme();	//get the current session theme TODO this currently can't be null; finalize the API
		while(theme!=null)	//while there are themes
		{
			for(final URFResource style:theme.getStyles())	//get the styles
			{
				final URI styleURI=style.getURI();	//get this style's URI
				if(styleURI!=null)	//if this style has a URI
				{
					styleURIs.add(0, styleURI);	//add this style URI to the list at the beginning so that resolving parent styles get listed first
				}
			}
			theme=theme.getParent();	//get the theme's parent, if any
		}
				//application style
		final URI applicationStyleURI=application.getStyleURI();	//get the application style
		if(applicationStyleURI!=null)	//if there is a style TODO make sure the style is a CSS style
		{
			styleURIs.add(applicationStyleURI);	//add the application style
		}
		if(destination instanceof ComponentDestination)	//if this is a component destination TODO improve; should we require a component destination for all context instances?
		{
					//destination style
			final URI destinationStyleURI=((ComponentDestination)destination).getStyle();	//get the destination style, if any
			if(destinationStyleURI!=null)	//if there is a style TODO make sure the style is a CSS style
			{
				styleURIs.add(destinationStyleURI);	//add the destination style
			}
		}
		this.styles=unmodifiableList(styleURIs);	//save an unmodifiable version of the style URIs
	}

	/**Determines the URI to use for depiction based upon a logical URI.
	The URI will first be dereferenced for the current session and then resolved to the application.
	@param uri The logical URI, which may be absolute or relative to the application.
	@param suffixes The suffixes, if any, to append to a resource key in a URI reference.
	@return A URI suitable for depiction, deferenced and resolved to the application.
	@see GuiseSession#dereferenceURI(URI, String...)
	@see GuiseApplication#getDepictURI(URI, URI)
	*/
	public URI getDepictURI(final URI uri, final String... suffixes)
	{
		final GuiseSession guiseSession=getSession();	//ge the session
		final GuiseApplication guiseApplication=session.getApplication();	//get the application
		return guiseApplication.resolveURI(guiseApplication.getDepictURI(getDepictURI(), guiseSession.dereferenceURI(uri, suffixes)));	//determine the depict URI and resolve it to the application
	}

}
