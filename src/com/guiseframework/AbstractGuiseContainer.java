package com.guiseframework;

import java.io.*;
import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.garretwilson.net.URIPath;
import com.garretwilson.net.http.HTTPNotFoundException;
import com.garretwilson.net.http.HTTPResource;

import static com.garretwilson.io.FileUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.lang.ThreadUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

/**An abstract base class for a Guise instance.
This implementation only works with Guise applications that descend from {@link AbstractGuiseApplication}.
@author Garret Wilson
*/
public abstract class AbstractGuiseContainer implements GuiseContainer
{

	/**The base URI of the container.*/
	private URI baseURI=null;

		/**Reports the base URI of the container.
		The base URI is an absolute URI that ends with the base path, which ends with a slash ('/').
		@return The base URI representing the Guise container.
		*/
		public URI getBaseURI() {return baseURI;}

	/**The base path of the container.*/
	private URIPath basePath=null;

		/**Reports the base path of the container.
		The base path is an absolute path that ends with a slash ('/'), indicating the base path of the application base paths.
		@return The base path representing the Guise container.
		*/
		public URIPath getBasePath() {return basePath;}

	/**The thread-safe map of Guise applications keyed to application base paths.*/
	private final Map<URIPath, AbstractGuiseApplication> applicationMap=new ConcurrentHashMap<URIPath, AbstractGuiseApplication>();

	/**The thread-safe reverse map of thread groups for Guise sessions.*/
//TODO del	private final ReverseMap<GuiseSession, ThreadGroup> sessionThreadGroupReverseMap=new DecoratorReverseMap<GuiseSession, ThreadGroup>(new ConcurrentHashMap<GuiseSession, ThreadGroup>(), new ConcurrentHashMap<ThreadGroup, GuiseSession>());

	/**Determines the thread group to use for the given session.
	This method must not be called for a session that has not yet been added to the container.
	@param guiseSession The session for which a thread group is requested.
	@return The thread group to use for the given session.
	@exception IllegalStateException if the given session has not yet been associated with a thread group because it has not yet been added to the container.
	*/
/*TODO del
	protected ThreadGroup getThreadGroup(final GuiseSession guiseSession)
	{
		final ThreadGroup threadGroup=sessionThreadGroupReverseMap.get(guiseSession);	//retrieve the thread group associated with the given session
		if(threadGroup==null)	//if there is no thread group for this session
		{
			throw new IllegalStateException("Guise session "+guiseSession+" not yet associated with a thread group.");
		}
		return threadGroup;	//return the thread group
	}
*/
	
	/**Adds and initializes a Guise session.
	This version creates a thread group for the session.
	The Guise session will be registered with the Guise application before it is initialized.
	Initialization will occur inside the appropriate session thread group.
	@param guiseSession The Guise session to add.
	@see GuiseApplication#registerSession(GuiseSession)
	@see GuiseSession#initialize()
	*/
	protected void addGuiseSession(final GuiseSession guiseSession)
	{
		final Guise guise=Guise.getInstance();	//get the Guise instance
		guise.addGuiseSession(guiseSession);	//add the Guise session to Guise
		guiseSession.getApplication().registerSession(guiseSession);	//register the session from the application
		final GuiseSessionThreadGroup guiseSessionThreadGroup=guise.getThreadGroup(guiseSession);	//get the thread group for this session
		call(guiseSessionThreadGroup, new Runnable()	//initialize the Guise session in its own thread group
				{
					public void run()
					{
						guiseSession.initialize();	//let the Guise session know it's being initialized so that it can listen to the application
					}
				});
	}

	/**Removes and destroys a Guise session.
	The Guise session will be unregistered from the Guise application after it is uninitialized.
	Destruction will occur inside the appropriate session thread group.
	@param guiseSession The Guise session to remove.
	@see GuiseSession#destroy()
	@see GuiseApplication#unregisterSession(GuiseSession) 
	*/
	protected void removeGuiseSession(final GuiseSession guiseSession)
	{
		final Guise guise=Guise.getInstance();	//get the Guise instance
		final GuiseSessionThreadGroup guiseSessionThreadGroup=guise.getThreadGroup(guiseSession);	//get the thread group for this session
		call(guiseSessionThreadGroup, new Runnable()	//destroy the Guise session in its own thread group
				{
					public void run()
					{
						guiseSession.destroy();	//let the Guise session know it's being destroyed so that it can clean up and release references to the application
					}
				});
		guiseSession.getApplication().unregisterSession(guiseSession);	//unregister the session from the application
		guise.removeGuiseSession(guiseSession);	//remove the Guise session from Guise
	}

	/**Installs the given application at the given base path.
	This version ensures the home, log, and temp directories exist.
	If no theme is specified, the default theme will be loaded.
	@param application The application to install.
	@param basePath The base path at which the application is being installed.
	@param homeDirectory The home directory of the application.
	@param logDirectory The log directory of the application.
	@param tempDirectory The temporary directory of the application.
	@exception NullPointerException if the application, base path, home directory, log directory, and/or temporary directory is <code>null</code>.
	@exception IllegalArgumentException if the base path is not absolute and does not end with a slash ('/') character.
	@exception IllegalStateException if the application is already installed in some container.
	@exception IllegalStateException if there is already an application installed in this container at the given base path.
	@exception IOException if there is an I/O error when installing the application.
	*/
	protected void installApplication(final AbstractGuiseApplication application, final URIPath basePath, final File homeDirectory, final File logDirectory, final File tempDirectory) throws IOException
	{
		checkInstance(application, "Application cannot be null");
		checkInstance(basePath, "Application base path cannot be null");
		synchronized(applicationMap)	//synchronize installations so that we can check the existence of the base path in the container
		{
			if(applicationMap.get(basePath)!=null)	//if there is already an application installed at the given base path
			{
				throw new IllegalStateException("Application already installed at base path "+basePath);
			}
			ensureDirectoryExists(homeDirectory);	//make sure the application home directory exists
			ensureDirectoryExists(logDirectory);	//make sure the application log directory exists
			ensureDirectoryExists(tempDirectory);	//make sure the application temporary directory exists
			application.install(this, basePath, homeDirectory, logDirectory, tempDirectory);	//tell the application it's being installed
			applicationMap.put(basePath, application);	//install the application in the map
		}
	}

	/**Uninstalls the given application.
	@param application The application to uninstall.
	@exception NullPointerException if the application is <code>null</code>.
	@exception IllegalStateException if the application is not installed in this container.
	*/
	protected void uninstallApplication(final AbstractGuiseApplication application)	//TODO add a facility to unregister and remove all sessions associated with the application
	{
		checkInstance(application, "Application cannot be null");
		final URIPath basePath=application.getBasePath();	//get the application's base path
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

	/**Container base URI constructor.
	@param baseURI The base URI of the container, an absolute URI that ends with the base path, which ends with a slash ('/'), indicating the base path of the application base paths.
	@exception NullPointerException if the base URI is <code>null</code>.
	@exception IllegalArgumentException if the base URI is not absolute or does not end with a slash ('/') character.
	*/
	public AbstractGuiseContainer(final URI baseURI)
	{
		checkInstance(baseURI, "Application base URI cannot be null");
		if(!isAbsolutePath(baseURI) || !isCollectionPath(baseURI.getPath()))	//if the base URI isn't absolute and doesn't end with a slash
		{
			throw new IllegalArgumentException("Container base URI "+baseURI+" is not absolute and does not end with a path separator.");
		}
		this.baseURI=baseURI;	//store the base URI		
		basePath=new URIPath(baseURI.getRawPath());	//store the base path
		checkInstance(basePath, "Application base path cannot be null");
		if(!basePath.isAbsolute() || !basePath.isCollection())	//if the path doesn't begin and end with a slash
		{
			throw new IllegalArgumentException("Container base path "+basePath+" does not begin and end with a path separator.");
		}		
	}
	
	/**Resolves a relative or absolute path against the container base path.
	Relative paths will be resolved relative to the container base path. Absolute paths will be be considered already resolved.
	For a container base path "/path/to/container/", resolving "relative/path" will yield "/path/to/container/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path".
	@param path The path to be resolved.
	@return The path resolved against the container base path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #resolveURI(URI)} should be used instead).
	@see #getBasePath()
	*/
	public URIPath resolvePath(final URIPath path)
	{
		return getBasePath().resolve(path);	//resolve the path against the base path
	}

	/**Resolves URI against the container base path.
	Relative paths will be resolved relative to the container base path. Absolute paths will be considered already resolved, as will absolute URIs.
	For a container base path "/path/to/container/", resolving "relative/path" will yield "/path/to/container/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	@param uri The URI to be resolved.
	@return The uri resolved against the container base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see #getBasePath()
	*/
	public URI resolveURI(final URI uri)
	{
		return getBasePath().resolve(checkInstance(uri, "URI cannot be null."));	//create a URI from the container base path and resolve the given path against it
	}

	/**Determines if the application has a resource available stored at the given resource path.
	The provided path is first normalized.
	@param resourcePath A container-relative path to a resource in the resource storage area.
	@return <code>true</code> if a resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	protected abstract boolean hasResource(final String resourcePath);

	/**Retrieves an input stream to the resource at the given path.
	The provided path is first normalized.
	@param resourcePath A container-relative path to a resource in the resource storage area.
	@return An input stream to the resource at the given resource path, or <code>null</code> if no resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	protected abstract InputStream getResourceInputStream(final String resourcePath);

	/**Retrieves an input stream to the entity at the given URI.
	The URI is first resolved to the container base URI.
	@param uri A URI to the entity; either absolute or relative to the container.
	@return An input stream to the entity at the given resource URI, or <code>null</code> if no entity exists at the given resource path..
	@exception NullPointerException if the given URI is <code>null</code>.
	@exception IOException if there was an error connecting to the entity at the given URI.
	@see #getBaseURI()
	*/
	public InputStream getInputStream(final URI uri) throws IOException	//TODO fix to work with resource URIs by delegating to getResourceInputStream()
	{
			//TODO make sure this is an HTTP URI; update to work with resource URIs
		final URI resolvedURI=getBaseURI().resolve(uri);	//resolve the URI against the container base URI
		try
		{
			return new HTTPResource(resolvedURI).getInputStream();	//get an input stream to the URI
		}
		catch(final HTTPNotFoundException httpNotFoundException)	//if the file was not found
		{
			return null;	//indicate that there is no file at this URI
		}
	}

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
