package com.guiseframework;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.garretwilson.rdf.RDFResource;

/**A navigation point that retrieves a resource description and/or contents.
@author Garret Wilson
*/
public interface ResourceDestination extends Destination
{

	/**Determines if a resource does indeed exist at the given location.
	Guise session information must be available in calls to this method. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return Whether the requested resource exists.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception IOException if there is an error accessing the resource.
	*/
	public boolean exists(final String navigationPath, final Bookmark bookmark, final URI referrerURI) throws IOException;

	/**Returns a description of the resource.
	The resource should include valid values for the following properties:
	<ul>
		<li><code>mime:contentType</code></li>
		<li><code>file:size</code></li>
		<li><code>file:modifiedTime</code></li>
	</ul>
	Guise session information must be available in calls to this method. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return A description of the resource.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception IOException if there is an error retrieving the resource description.
	*/
	public RDFResource getResourceDescription(final String navigationPath, final Bookmark bookmark, final URI referrerURI) throws IOException;

	/**Retrieves an input stream to the resource.
	Guise session information must be available in calls to this method. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return An input stream to the given resource.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception IOException Thrown if there is an error accessing the resource, such as a missing file.
	*/
	public InputStream getInputStream(final String navigationPath, final Bookmark bookmark, final URI referrerURI) throws IOException;

}
