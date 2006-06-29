package com.guiseframework;

import java.io.*;
import java.lang.ref.*;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.garretwilson.io.InputStreamUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

import static com.garretwilson.text.CharacterConstants.*;

/**The singleton Guise class.
There will only be one instance of Guise per JVM.
@author Garret Wilson
*/
public final class Guise
{

	/**The name of Guise.*/
	public final static String GUISE_NAME="Guise"+TRADE_MARK_SIGN_CHAR;

	/**The web address of Guise.*/
	public final static URI GUISE_WEB_URI=URI.create("http://www.guiseframework.com/");

	/**The base path to Guise public resources.*/
	public final static String PUBLIC_RESOURCE_BASE_PATH="pub/";

	/**The identifier of this build.*/
	public final static String BUILD_ID="2006-06-28";
	
		//Guise ontology
	
	/**The recommended prefix to the Guise ontology namespace.*/
	public final static String GUISE_NAMESPACE_PREFIX="guise";
	/**The URI to the Guise ontology namespace.*/
	public final static URI GUISE_NAMESPACE_URI=URI.create("http://guiseframework.com/namespaces/guise#");
			//class names
	/**The local name of guise:Theme.*/
//TODO del if not needed	public final static String THEME_CLASS_NAME="Theme";
			//property names
	/**The spine of a book. The local name of xeb:spine.*/
//TODO fix	public final static String SPINE_PROPERTY_NAME="spine";
	
	/**The singleton instance of Guise.*/
	private static Guise instance=null;

	/**@return The singleton instance of Guise.*/
	public static Guise getInstance()
	{
		if(instance==null)	//if Guise has not yet been created TODO make this concurrent-aware
		{
			instance=new Guise();	//create a new Guise
		}
		return instance;
	}

	/**Whether this deployment of Guise is licensed.*/
	private final boolean licensed;

		/**@return Whether this deployment of Guise is licensed.*/
		public boolean isLicensed() {return licensed;}

	/**The cache of public resource references keyed to resource strings.*/
	private Map<String, Reference<byte[]>> publicResourceMap=new ConcurrentHashMap<String, Reference<byte[]>>();

	/**Retrieves a public resource keyed to its location.
	Resources are cached for quick future retrieval.
	Due to race conditions, a resource may initially be loaded more than once in this implementation before its final value is placed in the cache.
	@param publicResourceKey The location of the resource.
	@return The resource, or <code>null</code> if there is no such resource.
	@exception IllegalArgumentException if the resource key does not begin with the public resource path.
	@exception IOException if there is an error loading the resource.
	@see #PUBLIC_RESOURCE_BASE_PATH
	*/
	public byte[] getPublicResource(final String publicResourceKey) throws IOException
	{
		final String key=normalizePath(publicResourceKey);	//normalize the resource key
		if(!key.startsWith(PUBLIC_RESOURCE_BASE_PATH))	//if this isn't a public resource key
		{
			throw new IllegalArgumentException(publicResourceKey);
		}
		final Reference<byte[]> reference=publicResourceMap.get(key);	//get a reference to the resource
		byte[] resource=reference!=null ? reference.get() : null;	//dereference the reference, if there is a reference
		if(resource==null)	//if we haven't yet loaded the resource, or it has been dereferenced
		{
			final InputStream resourceInputStream=getClass().getResourceAsStream(key);	//get an input stream to the resource
			if(resourceInputStream!=null)	//if we got an input stream to the resource
			{
				resource=getBytes(resourceInputStream);	//load the resource
				publicResourceMap.put(key, new SoftReference<byte[]>(resource));	//cache the resource
			}
		}
		return resource;	//return whatever resource we found
	}

	/**Retrieves a URL to a public resource keyed to its location.
	The URL allows connections to the resource.
	@param publicResourceKey The location of the resource.
	@return A URL to the resource, or <code>null</code> if there is no such resource.
	@exception IllegalArgumentException if the resource key does not begin with the public resource path.
	@exception IOException if there is an error loading the resource.
	@see #PUBLIC_RESOURCE_BASE_PATH
	*/
	public URL getPublicResourceURL(final String publicResourceKey) throws IOException
	{
		final String key=normalizePath(publicResourceKey);	//normalize the resource key
		if(!key.startsWith(PUBLIC_RESOURCE_BASE_PATH))	//if this isn't a public resource key
		{
			throw new IllegalArgumentException(publicResourceKey);
		}
		return getClass().getResource(key);	//get a URL to the resource
	}

	/**Retrieves an input stream to a public resource keyed to its location.
	This method will use cached resources if possible, but will not cache new resources.
	@param publicResourceKey The location of the resource.
	@return An input stream to a resource, or <code>null</code> if there is no such resource.
	@exception IllegalArgumentException if the resource key does not begin with the public resource path.
	@exception IOException if there is an error loading the resource.
	@see #PUBLIC_RESOURCE_BASE_PATH
	*/
	public InputStream getPublicResourceInputStream(final String publicResourceKey) throws IOException
	{
		final String key=normalizePath(publicResourceKey);	//normalize the resource key
		if(!key.startsWith(PUBLIC_RESOURCE_BASE_PATH))	//if this isn't a public resource key
		{
			throw new IllegalArgumentException(publicResourceKey);
		}
		final Reference<byte[]> reference=publicResourceMap.get(key);	//get a reference to the resource
		byte[] resource=reference!=null ? reference.get() : null;	//dereference the reference, if there is a reference
		if(resource!=null)	//if the resource is already loaded
		{
			return new ByteArrayInputStream(resource);	//return an input stream to the cached resource
		}
		else	//if we haven't yet loaded the resource, or it has been dereferenced
		{
			return getClass().getResourceAsStream(key);	//get an input stream to the resource
		}
	}

	/**The thread-safe map of Guise session thread groups for Guise sessions.*/
	private final Map<GuiseSession, GuiseSessionThreadGroup> sessionThreadGroupMap=new ConcurrentHashMap<GuiseSession, GuiseSessionThreadGroup>();

	/**Adds a Guise session and creates an associated thread group.
	This method creates a thread group for the session.
	@param guiseSession The Guise session to add.
	*/
	void addGuiseSession(final GuiseSession guiseSession)
	{
		final GuiseSessionThreadGroup threadGroup=new GuiseSessionThreadGroup(guiseSession);	//create a new thread group for the session
		sessionThreadGroupMap.put(guiseSession, threadGroup);	//associate the thread group with the Guise session and vice versa
	}

	/**Removes a Guise session and associated thread group.
	@param guiseSession The Guise session to remove.
	*/
	void removeGuiseSession(final GuiseSession guiseSession)
	{
		sessionThreadGroupMap.remove(guiseSession);	//remove the association between the session and the thread group
	}

	/**Determines the thread group to use for the given session.
	This method must not be called for a session that has not yet been added.
	@param guiseSession The session for which a thread group is requested.
	@return The thread group to use for the given session.
	@exception IllegalStateException if the given session has not yet been associated with a thread group because it has not yet been added.
	*/
	public final GuiseSessionThreadGroup getThreadGroup(final GuiseSession guiseSession)
	{
		final GuiseSessionThreadGroup threadGroup=sessionThreadGroupMap.get(guiseSession);	//retrieve the thread group associated with the given session
		if(threadGroup==null)	//if there is no thread group for this session
		{
			throw new IllegalStateException("Guise session "+guiseSession+" not yet associated with a Guise thread group.");
		}
		return threadGroup;	//return the thread group
	}

	/**Retrieves the Guise session information for the current thread.
	This method calls {@link #getGuiseSession(Thread)} with the current thread.
	@return The Guise session for the current thread.
	@exception IllegalStateException if the current thread is not associated with any Guise session.
	*/
	public final GuiseSession getGuiseSession()
	{
		final Thread currentThread=Thread.currentThread();	//get the current thread
		final GuiseSession guiseSession=getGuiseSession(currentThread);	//get the session for the current thread
		if(guiseSession==null)	//if there is no Guise session for the current thread
		{
			throw new IllegalStateException("Current thread "+currentThread+" is not associated associated with any Guise session.");
		}
		return guiseSession;	//return the session for the current thread
	}

	/**Retrieves the Guise session information for the given thread.
	All thread groups up the hierarchy are searched for an instance of {@link GuiseSessionThreadGroup}.
	@return The Guise session for the given thread, or <code>null</code> if the given thread is not.
	*/
	private final GuiseSession getGuiseSession(final Thread thread)
	{
		ThreadGroup threadGroup=thread.getThreadGroup();	//get the thread's thread group
		while(threadGroup!=null)	//stop looking if we run out of thread groups
		{
			if(threadGroup instanceof GuiseSessionThreadGroup)	//if this is a Guise session thread group
			{
				return ((GuiseSessionThreadGroup)threadGroup).getGuiseSession();	//return the session from the thread group
			}
			threadGroup=threadGroup.getParent();	//check this thread group's parent thread group
		}
		return null;	//we were unable to find a Guise session thread group
	}
	
	/**Private default constructor.
	@see #getInstance()
	*/
	private Guise()
	{
		licensed=getClass().getResource("license.properties")!=null;	//determine if Guise is licensed; for now we simply see if the license.properties file exists
	}
}
