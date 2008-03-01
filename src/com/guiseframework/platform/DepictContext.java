package com.guiseframework.platform;

import java.net.URI;

import com.globalmentor.beans.PropertyBindable;
import com.guiseframework.*;

/**Encapsulation of information related to the current depiction.
@author Garret Wilson
*/
public interface DepictContext extends PropertyBindable
{

	/**@return The Guise user session of which this context is a part.*/
	public GuiseSession getSession();

	/**@return The platform on which Guise objects are depicted.*/
	public Platform getPlatform();

	/**@return The destination with which this context is associated.*/
	public Destination getDestination();

	/**@return The current full absolute URI for this depiction, including any query.*/
	public URI getDepictionURI();

	/**Determines the URI to use for depiction based upon a navigation URI.
	The URI will first be dereferenced for the current session and then resolved to the application.
	This method delegates to {@link GuiseSession#getDepictionURI(URI, String...)}.
	@param navigationURI The navigation URI, which may be absolute or relative to the application.
	@param suffixes The suffixes, if any, to append to a resource key in a URI reference.
	@return A URI suitable for depiction, deferenced and resolved to the application.
	@see GuiseSession#getDepictionURI(URI, String...)
	*/
	public URI getDepictionURI(final URI navigationURI, final String... suffixes);

	/**Retrieves styles for this context.
	Styles appear in the following order:
	<ol>
		<li>theme styles (from most distant parent to current theme)</li>
		<li>application style</li>
		<li>destination style</li>
	</ol>
	@return The URIs of the styles for this context, in order.
	*/
	public Iterable<URI> getStyles();

}
