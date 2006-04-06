package com.guiseframework;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.garretwilson.util.DecoratorReverseMap;
import com.garretwilson.util.ReverseMap;

import static com.garretwilson.io.InputStreamUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

import static com.garretwilson.text.CharacterConstants.*;

/**The singleton Guise class.
There will only be one instance of Guise per JVM.
@author Garret Wilson
*/
public class Guise
{

	/**The name of Guise.*/
	public final static String GUISE_NAME="Guise"+TRADE_MARK_SIGN_CHAR;

	/**The web address of Guise.*/
	public final static URI GUISE_WEB_URI=URI.create("http://www.guiseframework.com/");

	/**The base path to Guise public resources.*/
	public final static String PUBLIC_RESOURCE_BASE_PATH="pub/";

	/**The identifier of this build.*/
	public final static String BUILD_ID="2006-04-05";
	
	/**The singleton instance of Guise.*/
	private static Guise instance=null;

	/**@return The singleton instance of Guise.*/
	public static Guise getInstance()
	{
		if(instance==null)	//if Guise has not yet been created
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

	/**The thread-safe reverse map of thread groups for Guise sessions.*/
	private final ReverseMap<GuiseSession, ThreadGroup> sessionThreadGroupReverseMap=new DecoratorReverseMap<GuiseSession, ThreadGroup>(new ConcurrentHashMap<GuiseSession, ThreadGroup>(), new ConcurrentHashMap<ThreadGroup, GuiseSession>());

	/**Determines the thread group to use for the given session.
	This method must not be called for a session that has not yet been added.
	@param guiseSession The session for which a thread group is requested.
	@return The thread group to use for the given session.
	@exception IllegalStateException if the given session has not yet been associated with a thread group because it has not yet been added.
	*/
	ThreadGroup getThreadGroup(final GuiseSession guiseSession)
	{
		final ThreadGroup threadGroup=sessionThreadGroupReverseMap.get(guiseSession);	//retrieve the thread group associated with the given session
		if(threadGroup==null)	//if there is no thread group for this session
		{
			throw new IllegalStateException("Guise session "+guiseSession+" not yet associated with a thread group.");
		}
		return threadGroup;	//return the thread group
	}

	/**Adds a Guise session and creates an associated thread group.
	This method creates a thread group for the session.
	@param guiseSession The Guise session to add.
	*/
	void addGuiseSession(final GuiseSession guiseSession)
	{
		final ThreadGroup threadGroup=new ThreadGroup("Guise Session Thread Group "+guiseSession.toString());	//create a new thread group for the session TODO improve name
		sessionThreadGroupReverseMap.put(guiseSession, threadGroup);	//associate the thread group with the Guise session and vice versa
	}

	/**Removes a Guise session and associated thread group.
	@param guiseSession The Guise session to remove.
	*/
	void removeGuiseSession(final GuiseSession guiseSession)
	{
		sessionThreadGroupReverseMap.remove(guiseSession);	//remove the association between the session and the thread group
	}
	
	/**Private default constructor.
	@see #getInstance()
	*/
	private Guise()
	{
		licensed=getClass().getResource("license.properties")!=null;	//determine if Guise is licensed; for now we simply see if the license.properties file exists
	}
}
