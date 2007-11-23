package com.guiseframework;

import java.io.*;
import java.lang.ref.*;
import java.net.*;
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

	/**The base key to Guise assets bundled in the Guise distributable.*/
	public final static String GUISE_ASSETS_BASE_KEY="assets/";

	/**The identifier of this build.*/
	public final static String BUILD_ID="2007-11-22";

		//Guise ontology
	
	/**The recommended prefix to the Guise ontology namespace.*/
//TODO del if not needed	public final static String GUISE_NAMESPACE_PREFIX="guise";
	/**The URI to the Guise ontology namespace.*/
//TODO del if not needed	public final static URI GUISE_NAMESPACE_URI=URI.create("http://guiseframework.com/namespaces/guise#");

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

	/**The cache of asset references keyed to asset strings.*/
	private Map<String, Reference<byte[]>> assetMap=new ConcurrentHashMap<String, Reference<byte[]>>();

	/**Retrieves a Guise asset keyed to its location.
	Assets are cached for quick future retrieval.
	Due to race conditions, an asset may initially be loaded more than once in this implementation before its final value is placed in the cache.
	@param guiseAssetKey The location of the asset.
	@return The asset, or <code>null</code> if there is no such asset.
	@exception IllegalArgumentException if the asset key does not begin with {@value #GUISE_ASSETS_BASE_KEY}.
	@exception IOException if there is an error loading the aset.
	@see #GUISE_ASSETS_BASE_KEY
	*/
	public byte[] getGuiseAsset(final String guiseAssetKey) throws IOException
	{
		final String key=normalizePath(guiseAssetKey);	//normalize the asset key
		if(!key.startsWith(GUISE_ASSETS_BASE_KEY))	//if this isn't an asset key
		{
			throw new IllegalArgumentException("String "+guiseAssetKey+" is not a Guise asset key.");
		}
		final Reference<byte[]> reference=assetMap.get(key);	//get a reference to the asset
		byte[] asset=reference!=null ? reference.get() : null;	//dereference the reference, if there is a reference
		if(asset==null)	//if we haven't yet loaded the asset, or it has been dereferenced
		{
			final InputStream assetInputStream=getClass().getResourceAsStream(key);	//get an input stream to the asset
			if(assetInputStream!=null)	//if we got an input stream to the asset
			{
				asset=getBytes(assetInputStream);	//load the asset
				assetMap.put(key, new SoftReference<byte[]>(asset));	//cache the asset
			}
		}
		return asset;	//return whatever asset we found
	}

	/**Determines if a URL specifies an existing Guise asset.
	This version delegates to {@link #getGuiseAssetURL(String)}.
	@param guiseAssetKey The location of the asset.
	@return <code>true</code> if the URL references an existing Guise asset, else <code>false</code>.
	@exception IllegalArgumentException if the asset key does not begin with {@value #GUISE_ASSETS_BASE_KEY}.
	@exception IOException if there is an error accessing the asset.
	@see #GUISE_ASSETS_BASE_KEY
	*/
	public boolean hasGuiseAssetURL(final String guiseAssetKey) throws IOException
	{
		return getGuiseAssetURL(guiseAssetKey)!=null;	//see if there is actually an asset at the given location
	}

	/**Retrieves a URL to a Guise asset keyed to its location.
	The URL allows connections to the asset.
	@param guiseAssetKey The location of the asset.
	@return A URL to the asset, or <code>null</code> if there is no such asset.
	@exception IllegalArgumentException if the asset key does not begin with {@value #GUISE_ASSETS_BASE_KEY}.
	@exception IOException if there is an error loading the asset.
	@see #GUISE_ASSETS_BASE_KEY
	*/
	public URL getGuiseAssetURL(final String guiseAssetKey) throws IOException
	{
		final String key=normalizePath(guiseAssetKey);	//normalize the asset key
		if(!key.startsWith(GUISE_ASSETS_BASE_KEY))	//if this isn't an asset key
		{
			throw new IllegalArgumentException("String "+guiseAssetKey+" is not a Guise asset key.");
		}
		return getClass().getResource(key);	//get a URL to the asset
	}

	/**Retrieves an input stream to a Guise asset keyed to its location.
	This method will use cached assets if possible, but will not cache new assets.
	@param guiseAssetKey The location of the asset.
	@return An input stream to an asset, or <code>null</code> if there is no such asset.
	@exception IllegalArgumentException if the asset key does not begin with {@value #GUISE_ASSETS_BASE_KEY}.
	@exception IOException if there is an error loading the asset.
	@see #GUISE_ASSETS_BASE_KEY
	*/
	public InputStream getGuiseAssetInputStream(final String guiseAssetKey) throws IOException
	{
		final String key=normalizePath(guiseAssetKey);	//normalize the asset key
		if(!key.startsWith(GUISE_ASSETS_BASE_KEY))	//if this isn't an asset key
		{
			throw new IllegalArgumentException("String "+guiseAssetKey+" is not a Guise asset key.");
		}
		final Reference<byte[]> reference=assetMap.get(key);	//get a reference to the asset
		byte[] asset=reference!=null ? reference.get() : null;	//dereference the reference, if there is a reference
		if(asset!=null)	//if the asset is already loaded
		{
			return new ByteArrayInputStream(asset);	//return an input stream to the cached asset
		}
		else	//if we haven't yet loaded the asset, or it has been dereferenced
		{
			return getClass().getResourceAsStream(key);	//get an input stream to the asset
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
	@exception IllegalStateException if the given Guise session is not recognized.
	*/
	void removeGuiseSession(final GuiseSession guiseSession)
	{
		final GuiseSessionThreadGroup guiseSessionThreadGroup=sessionThreadGroupMap.remove(guiseSession);	//remove the association between the session and the thread group, retrieving a reference to the thread group
		if(guiseSessionThreadGroup==null)	//if there was no thread group associated with this session
		{
			throw new IllegalStateException("Unrecognized Guise session; no thread group registered for session: "+guiseSession);
		}

/*TODO fix
		//TODO maybe try to interrupt and join active threads in the thread group, preferably with a timeout; this is risky because we don't know the interruption policy of whatever threads have been started in the thread group, but if we let them run destroying the thread group will cause an exception because there are still active threads in the thread group, if the application session has started any new threads		
Debug.trace("ready to destroy thread group", guiseSessionThreadGroup.getName(), "with active threads", guiseSessionThreadGroup.activeCount());
final Thread[] activeThreads=new Thread[guiseSessionThreadGroup.activeCount()];
guiseSessionThreadGroup.enumerate(activeThreads);
		for(Thread activeThread:activeThreads)
		{
			Debug.trace("active thread:", activeThread.getName());
		}
*/
			//TODO maybe interrupt the thread group first as a safety precaution to make sure it can be destroyed, but when we reach here there shouldn't be any session-related threads running (see comment above) 
		guiseSessionThreadGroup.destroy();	//destroy the thread group (otherwise it would continue to maintain a reference to the Guise session, causing a memory leak)
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
	@return The Guise session for the given thread, or <code>null</code> if the given thread is not in a Guise session thread group.
	@see #getGuiseSessionThreadGroup(Thread)
	*/
	final GuiseSession getGuiseSession(final Thread thread)
	{
		final GuiseSessionThreadGroup guiseSessionThreadGroup=getGuiseSessionThreadGroup(thread);	//get the Guise session thread group the given thread is in
		return guiseSessionThreadGroup!=null ? guiseSessionThreadGroup.getGuiseSession() : null;	//return the session from the thread group, if there is a Guise session thread group
	}

	/**Retrieves the Guise session thread group.
	All thread groups up the hierarchy are searched for an instance of {@link GuiseSessionThreadGroup}.
	@return The Guise session thread group for the given thread, or <code>null</code> if the given thread is not in a Guise session thread group.
	*/
	final GuiseSessionThreadGroup getGuiseSessionThreadGroup(final Thread thread)
	{
		ThreadGroup threadGroup=thread.getThreadGroup();	//get the thread's thread group
		while(threadGroup!=null)	//stop looking if we run out of thread groups
		{
			if(threadGroup instanceof GuiseSessionThreadGroup)	//if this is a Guise session thread group
			{
				return ((GuiseSessionThreadGroup)threadGroup);	//return the Guise session thread group
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
