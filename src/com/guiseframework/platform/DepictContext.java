package com.guiseframework.platform;

import java.net.URI;

import com.garretwilson.beans.PropertyBindable;
import com.guiseframework.Destination;
import com.guiseframework.GuiseSession;

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

	/**@return The current absolute URI for this depiction.*/
	public URI getDepictURI();

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
