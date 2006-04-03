package com.guiseframework;

import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.createPathURI;
import static com.garretwilson.net.URIUtilities.isAbsolutePath;
import static com.garretwilson.net.URIUtilities.isContainerPath;

/**An abstract base class for a Guise instance.
This implementation only works with Guise applications that descend from {@link AbstractGuiseApplication}.
@author Garret Wilson
*/
public abstract class AbstractGuiseContainer	implements GuiseContainer
{

	/**The base path of the container.*/
	private String basePath=null;

		/**Reports the base path of the container.
		The base path is an absolute path that ends with a slash ('/'), indicating the base path of the application base paths.
		@return The base path representing Guise container.
		*/
		public String getBasePath() {return basePath;}

	/**The thread-safe map of Guise applications keyed to application base paths.*/
	private final Map<String, AbstractGuiseApplication> applicationMap=new ConcurrentHashMap<String, AbstractGuiseApplication>();

	/**Installs the given application at the given base path.
	@param application The application to install.
	@param basePath The base path at which the application is being installed.
	@exception NullPointerException if either the application or base path is <code>null</code>.
	@exception IllegalArgumentException if the base path is not absolute and does not end with a slash ('/') character.
	@exception IllegalStateException if the application is already installed in some container.
	@exception IllegalStateException if there is already an application installed in this container at the given base path.
	*/
	protected void installApplication(final AbstractGuiseApplication application, final String basePath)
	{
		checkInstance(application, "Application cannot be null");
		checkInstance(basePath, "Application base path cannot be null");
		synchronized(applicationMap)	//synchronize installations so that we can check the existence of the base path in the container
		{
			if(applicationMap.get(basePath)!=null)	//if there is already an application installed at the given base path
			{
				throw new IllegalStateException("Application already installed at base path "+basePath);
			}
			application.install(this, basePath);	//tell the application it's being installed
			applicationMap.put(basePath, application);	//install the application in the map
		}
	}

	/**Uninstalls the given application.
	@param application The application to uninstall.
	@exception NullPointerException if the application is <code>null</code>.
	@exception IllegalStateException if the application is not installed in this container.
	*/
	protected void uninstallApplication(final AbstractGuiseApplication application)
	{
		checkInstance(application, "Application cannot be null");
		final String basePath=application.getBasePath();	//get the application's base path
		if(basePath==null || application.getContainer()!=this)	//if the application has no bsae path or has a different container than this class
		{
			throw new IllegalStateException("Application installed in a different container.");
		}
		synchronized(applicationMap)	//synchronize uninstallations so that we can check the existence of the base path in the container
		{
			if(applicationMap.get(basePath)!=application)	//if something (or nothing) other than the given application is installed at this base path
			{
				throw new IllegalStateException("Application not installed at base path "+basePath);
			}
			applicationMap.remove(basePath);	//remove the application in the map
			application.uninstall(this);	//tell the application it's being uninstalled
		}
	}

	/**Container base path constructor.
	@param basePath The base path of the container.
	@exception NullPointerException if the base path is <code>null</code>.
	@exception IllegalArgumentException if the base path is not absolute and does not end with a slash ('/') character.
	*/
	public AbstractGuiseContainer(final String basePath)
	{
		checkInstance(basePath, "Application base path cannot be null");
		if(!isAbsolutePath(basePath) || !isContainerPath(basePath))	//if the path doesn't begin and end with a slash
		{
			throw new IllegalArgumentException("Container base path "+basePath+" does not begin and ends with a path separator.");
		}
		this.basePath=basePath;	//store the base path
	}
	
	/**Resolves a relative or absolute path against the container base path.
	Relative paths will be resolved relative to the container base path. Absolute paths will be be considered already resolved.
	For a container base path "/path/to/container/", resolving "relative/path" will yield "/path/to/container/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path".
	@param path The path to be resolved.
	@return The path resolved against the container base path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #resolveURI(URI)} should be used instead).
	@see #resolveURI(URI)
	*/
	public String resolvePath(final String path)
	{
		return resolveURI(createPathURI(path)).toString();	//create a URI for the given path, ensuring that the string only specifies a path, and resolve that URI
	}

	/**Resolves URI against the container base path.
	Relative paths will be resolved relative to the container base path. Absolute paths will be considered already resolved, as will absolute URIs.
	For a container base path "/path/to/container/", resolving "relative/path" will yield "/path/to/container/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	@param uri The URI to be resolved.
	@return The uri resolved against the container base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see GuiseContainer#getBasePath()
	*/
	public URI resolveURI(final URI uri)
	{
		return URI.create(getBasePath()).resolve(checkInstance(uri, "URI cannot be null."));	//create a URI from the container base path and resolve the given path against it
	}

	/**Determines if the application has a resource available stored at the given resource path.
	The provided path is first normalized.
	@param resourcePath A container-relative path to a resource in the resource storage area.
	@return <code>true</code> if a resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	protected abstract boolean hasResource(final String resourcePath);

	/**Retrieves and input stream to the resource at the given path.
	The provided path is first normalized.
	@param resourcePath A container-relative path to a resource in the resource storage area.
	@return An input stream to the resource at the given resource path, or <code>null</code> if no resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	protected abstract InputStream getResourceAsStream(final String resourcePath);

	/**Looks up an application principal from the given ID.
	This version delegates to the given Guise application.
	@param application The application for which a principal should be returned for the given ID.
	@param id The ID of the principal.
	@return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	*/
	protected Principal getPrincipal(final AbstractGuiseApplication application, final String id)
	{
		return application.getPrincipal(id);	//delegate to the application
	}

	/**Looks up the corresponding password for the given principal.
	This version delegates to the given Guise application. 
	@param application The application for which a password should e retrieved for the given principal.
	@param principal The principal for which a password should be returned.
	@return The password associated with the given principal, or <code>null</code> if no password is associated with the given principal.
	*/
	protected char[] getPassword(final AbstractGuiseApplication application, final Principal principal)
	{
		return application.getPassword(principal);	//delegate to the application 
	}

	/**Determines the realm applicable for the resource indicated by the given URI.
	This version delegates to the given Guise application.
	@param application The application for which a realm should be returned for the given resouce URI.
	@param resourceURI The URI of the resource requested.
	@return The realm appropriate for the resource, or <code>null</code> if the given resource is not in a known realm.
	@see GuiseApplication#relativizeURI(URI)
	*/
	protected String getRealm(final AbstractGuiseApplication application, final URI resourceURI)
	{
		return application.getRealm(application.relativizeURI(resourceURI));	//delegate to the application
	}

	/**Checks whether the given principal is authorized to access the resouce at the given application path.
	This version delegates to the given Guise application.
	@param application The application for which a principal should be authorized for a given resouce URI.
	@param resourceURI The URI of the resource requested.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	@return <code>true</code> if the given principal is authorized to access the resource represented by the given resource URI.
	*/
	protected boolean isAuthorized(final AbstractGuiseApplication application, final URI resourceURI, final Principal principal, final String realm)
	{
		return application.isAuthorized(application.relativizeURI(resourceURI), principal, realm);	//delegate to the application
	}
}
