package com.guiseframework;

import java.io.OutputStream;
import java.net.URI;

import com.garretwilson.net.ResourceIOException;
import com.garretwilson.net.URIPath;
import com.garretwilson.rdf.RDFResource;

/**A navigation point that sets a resource description and/or contents.
@author Garret Wilson
*/
public interface ResourceWriteDestination extends Destination
{

	/**Retrieves an output stream to the resource.
	The resource may include any of the following properties:
	<ul>
		<li><code>mime:contentType</code></li>
		<li><code>file:name</code></li>
		<li><code>file:size</code></li>
		<li><code>file:modifiedTime</code></li>
	</ul>
	@param session The current Guise Session. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring component or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return An output stream to the given resource.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception ResourceIOException Thrown if there is an error writing to the resource.
	*/
	public OutputStream getOutputStream(final RDFResource resourceDescription, final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

}
