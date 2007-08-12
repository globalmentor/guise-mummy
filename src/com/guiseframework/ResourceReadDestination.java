package com.guiseframework;

import java.io.InputStream;
import java.net.URI;

import com.garretwilson.net.ResourceIOException;
import com.garretwilson.net.URIPath;
import com.garretwilson.rdf.RDFResource;

/**A navigation point that retrieves a resource description and/or contents.
@author Garret Wilson
*/
public interface ResourceReadDestination extends Destination
{

	/**Returns a description of the resource.
	The resource should include valid values for the following properties:
	<ul>
		<li><code>mime:contentType</code></li>
		<li><code>file:size</code></li>
		<li><code>file:modifiedTime</code></li>
	</ul>
	@param session The current Guise Session. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring component or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return A description of the resource.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception ResourceIOException if there is an error retrieving the resource description.
	*/
	public RDFResource getResourceDescription(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

	/**Retrieves an input stream to the resource.
	@param session The current Guise Session. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring component or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return An input stream to the given resource.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception ResourceIOException Thrown if there is an error accessing the resource, such as a missing file.
	*/
	public InputStream getInputStream(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

}
