package com.guiseframework;

import java.io.OutputStream;
import java.net.URI;

import com.garretwilson.net.ResourceIOException;
import com.garretwilson.net.URIPath;
import com.globalmentor.urf.*;
import com.globalmentor.urf.content.Content;

/**A navigation point that sets a resource description and/or contents.
@author Garret Wilson
*/
public interface ResourceWriteDestination extends Destination
{

	/**Retrieves an output stream to the resource.
	The resource may include any of the following properties:
	<ul>
		<li>{@value URF#NAME_PROPERTY_URI}</li>
		<li>{@value Content#TYPE_PROPERTY_URI}</li>
		<li>{@value Content#LENGTH_PROPERTY_URI}</li>
		<li>{@value Content#MODIFIED_PROPERTY_URI}</li>
	</ul>
	@param session The current Guise Session. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring component or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return An output stream to the given resource.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception ResourceIOException Thrown if there is an error writing to the resource.
	*/
	public OutputStream getOutputStream(final URFResource resourceDescription, final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

}
