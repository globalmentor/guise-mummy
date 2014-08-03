/*
 * Copyright © 2005-2013 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework;

import java.io.*;
import java.lang.ref.*;
import java.net.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.globalmentor.io.InputStreams.*;
import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Conditions.*;

import com.globalmentor.config.ConfigurationException;
import com.globalmentor.iso.datetime.ISODate;
import com.globalmentor.java.Threads;

import static com.globalmentor.net.URIs.*;
import static com.globalmentor.util.PropertiesUtilities.*;
import static org.urframework.URF.*;

/**
 * The singleton Guise class. There will only be one instance of Guise per JVM.
 * <p>
 * This class depends on a properties file resource.
 * </p>
 * @author Garret Wilson
 */
public final class Guise {

	//properties keys for the properties resources
	private final static String VERSION_PROPERTIES_KEY = "version"; //String
	private final static String BUILD_DATE_PROPERTIES_KEY = "build.date"; //ISODate

	static {
		try {
			final Properties properties = loadPropertiesResource(Guise.class);
			version = checkConfigurationNotNull(properties.getProperty(VERSION_PROPERTIES_KEY));
			buildDate = ISODate.valueOf(checkConfigurationNotNull(properties.getProperty(BUILD_DATE_PROPERTIES_KEY)));
		} catch(final IOException ioException) {
			throw new ConfigurationException(ioException);
		}
	}

	private final static String version;

	/** @return The version of Guise. */
	public static String getVersion() {
		return version;
	}

	private final static ISODate buildDate;

	/** @return The build date of Guise. */
	public static ISODate getBuildDate() {
		return buildDate;
	}

	/** The name of Guise. */
	public final static String GUISE_NAME = "Guise" + TRADE_MARK_SIGN_CHAR;

	/** The web address of Guise. */
	public final static URI GUISE_WEB_URI = URI.create("http://www.guiseframework.com/");

	/** The base key to Guise assets bundled in the Guise distributable. */
	public final static String GUISE_ASSETS_BASE_KEY = "assets/";

	//Guise ontology

	/** The recommended prefix to the Guise ontology namespace. */
	public final static String NAMESPACE_PREFIX = "guise";
	/** The URI to the Guise ontology namespace. */
	public final static URI NAMESPACE_URI = URI.create("http://guiseframework.com/namespaces/guise/");

	//properties
	/** The property indicating the URI of the theme of a resource. */
	public final static URI THEME_URI_PROPERTY_URI = createResourceURI(NAMESPACE_URI, "themeURI");

	/** The singleton instance of Guise. */
	private static Guise instance = null;

	/** @return The singleton instance of Guise. */
	public static Guise getInstance() {
		if(instance == null) { //if Guise has not yet been created TODO make this concurrent-aware
			instance = new Guise(); //create a new Guise
		}
		return instance;
	}

	/** Whether this deployment of Guise is licensed. */
	private final boolean licensed;

	/** @return Whether this deployment of Guise is licensed. */
	public boolean isLicensed() {
		return licensed;
	}

	/** The cache of asset references keyed to asset strings. */
	private Map<String, Reference<byte[]>> assetMap = new ConcurrentHashMap<String, Reference<byte[]>>();

	/**
	 * Retrieves a Guise asset keyed to its location. Assets are cached for quick future retrieval. Due to race conditions, an asset may initially be loaded more
	 * than once in this implementation before its final value is placed in the cache.
	 * @param guiseAssetKey The location of the asset.
	 * @return The asset, or <code>null</code> if there is no such asset.
	 * @throws IllegalArgumentException if the asset key does not begin with {@value #GUISE_ASSETS_BASE_KEY}.
	 * @throws IOException if there is an error loading the asset.
	 * @see #GUISE_ASSETS_BASE_KEY
	 */
	public byte[] getGuiseAsset(final String guiseAssetKey) throws IOException {
		final String key = normalizePath(guiseAssetKey); //normalize the asset key
		if(!key.startsWith(GUISE_ASSETS_BASE_KEY)) { //if this isn't an asset key
			throw new IllegalArgumentException("String " + guiseAssetKey + " is not a Guise asset key.");
		}
		final Reference<byte[]> reference = assetMap.get(key); //get a reference to the asset
		byte[] asset = reference != null ? reference.get() : null; //dereference the reference, if there is a reference
		if(asset == null) { //if we haven't yet loaded the asset, or it has been dereferenced
			final InputStream assetInputStream = getClass().getResourceAsStream(key); //get an input stream to the asset
			if(assetInputStream != null) { //if we got an input stream to the asset
				asset = getBytes(assetInputStream); //load the asset
				assetMap.put(key, new SoftReference<byte[]>(asset)); //cache the asset
			}
		}
		return asset; //return whatever asset we found
	}

	/**
	 * Determines if a specified Guise asset exists. This version delegates to {@link #getAssetURL(String)}.
	 * @param guiseAssetKey The location of the asset.
	 * @return <code>true</code> if the URL references an existing Guise asset, else <code>false</code>.
	 * @throws IllegalArgumentException if the asset key does not begin with {@value #GUISE_ASSETS_BASE_KEY}.
	 * @throws IOException if there is an error accessing the asset.
	 * @see #GUISE_ASSETS_BASE_KEY
	 */
	public boolean hasAsset(final String guiseAssetKey) throws IOException {
		return getAssetURL(guiseAssetKey) != null; //see if there is actually an asset at the given location
	}

	/**
	 * Retrieves a URL to a Guise asset keyed to its location. The URL allows connections to the asset. The returned URL represents internal access to the asset
	 * and should normally not be presented to users.
	 * @param guiseAssetKey The location of the asset.
	 * @return A URL to the asset, or <code>null</code> if there is no such asset.
	 * @throws IllegalArgumentException if the asset key does not begin with {@value #GUISE_ASSETS_BASE_KEY}.
	 * @throws IOException if there is an error loading the asset.
	 * @see #GUISE_ASSETS_BASE_KEY
	 */
	public URL getAssetURL(final String guiseAssetKey) throws IOException {
		final String key = normalizePath(guiseAssetKey); //normalize the asset key
		if(!key.startsWith(GUISE_ASSETS_BASE_KEY)) { //if this isn't an asset key
			throw new IllegalArgumentException("String " + guiseAssetKey + " is not a Guise asset key.");
		}
		return getClass().getResource(key); //get a URL to the asset
	}

	/**
	 * Retrieves an input stream to a Guise asset keyed to its location. This method will use cached assets if possible, but will not cache new assets.
	 * @param guiseAssetKey The location of the asset.
	 * @return An input stream to an asset, or <code>null</code> if there is no such asset.
	 * @throws IllegalArgumentException if the asset key does not begin with {@value #GUISE_ASSETS_BASE_KEY}.
	 * @throws IOException if there is an error loading the asset.
	 * @see #GUISE_ASSETS_BASE_KEY
	 */
	public InputStream getAssetInputStream(final String guiseAssetKey) throws IOException {
		final String key = normalizePath(guiseAssetKey); //normalize the asset key
		if(!key.startsWith(GUISE_ASSETS_BASE_KEY)) { //if this isn't an asset key
			throw new IllegalArgumentException("String " + guiseAssetKey + " is not a Guise asset key.");
		}
		final Reference<byte[]> reference = assetMap.get(key); //get a reference to the asset
		byte[] asset = reference != null ? reference.get() : null; //dereference the reference, if there is a reference
		if(asset != null) { //if the asset is already loaded
			return new ByteArrayInputStream(asset); //return an input stream to the cached asset
		} else { //if we haven't yet loaded the asset, or it has been dereferenced
			return getClass().getResourceAsStream(key); //get an input stream to the asset
		}
	}

	/** The thread-safe map of Guise session thread groups for Guise sessions. */
	private final Map<GuiseSession, GuiseSessionThreadGroup> sessionThreadGroupMap = new ConcurrentHashMap<GuiseSession, GuiseSessionThreadGroup>();

	/**
	 * Adds a Guise session and creates an associated thread group. This method creates a thread group for the session.
	 * @param guiseSession The Guise session to add.
	 */
	void addGuiseSession(final GuiseSession guiseSession) {
		final GuiseSessionThreadGroup threadGroup = new GuiseSessionThreadGroup(guiseSession); //create a new thread group for the session
		sessionThreadGroupMap.put(guiseSession, threadGroup); //associate the thread group with the Guise session and vice versa
	}

	/**
	 * Removes a Guise session and associated thread group.
	 * @param guiseSession The Guise session to remove.
	 * @throws IllegalStateException if the given Guise session is not recognized.
	 */
	void removeGuiseSession(final GuiseSession guiseSession) {
		final GuiseSessionThreadGroup guiseSessionThreadGroup = sessionThreadGroupMap.remove(guiseSession); //remove the association between the session and the thread group, retrieving a reference to the thread group
		if(guiseSessionThreadGroup == null) { //if there was no thread group associated with this session
			throw new IllegalStateException("Unrecognized Guise session; no thread group registered for session: " + guiseSession);
		}

		/*TODO fix
				//TODO maybe try to interrupt and join active threads in the thread group, preferably with a timeout; this is risky because we don't know the interruption policy of whatever threads have been started in the thread group, but if we let them run destroying the thread group will cause an exception because there are still active threads in the thread group, if the application session has started any new threads		
		Log.trace("ready to destroy thread group", guiseSessionThreadGroup.getName(), "with active threads", guiseSessionThreadGroup.activeCount());
		final Thread[] activeThreads=new Thread[guiseSessionThreadGroup.activeCount()];
		guiseSessionThreadGroup.enumerate(activeThreads);
				for(Thread activeThread:activeThreads)
				{
					Log.trace("active thread:", activeThread.getName());
				}
		*/
		//TODO maybe interrupt the thread group first as a safety precaution to make sure it can be destroyed, but when we reach here there shouldn't be any session-related threads running (see comment above) 
		guiseSessionThreadGroup.destroy(); //destroy the thread group (otherwise it would continue to maintain a reference to the Guise session, causing a memory leak)
	}

	/**
	 * Determines the thread group to use for the given session. This method must not be called for a session that has not yet been added.
	 * @param guiseSession The session for which a thread group is requested.
	 * @return The thread group to use for the given session.
	 * @throws IllegalStateException if the given session has not yet been associated with a thread group because it has not yet been added.
	 */
	public final GuiseSessionThreadGroup getThreadGroup(final GuiseSession guiseSession) {
		final GuiseSessionThreadGroup threadGroup = sessionThreadGroupMap.get(guiseSession); //retrieve the thread group associated with the given session
		if(threadGroup == null) { //if there is no thread group for this session
			throw new IllegalStateException("Guise session " + guiseSession + " not yet associated with a Guise thread group.");
		}
		return threadGroup; //return the thread group
	}

	/**
	 * Retrieves the Guise session information for the current thread. This method calls {@link #getGuiseSession(Thread)} with the current thread.
	 * @return The Guise session for the current thread.
	 * @throws IllegalStateException if the current thread is not associated with any Guise session.
	 */
	public final GuiseSession getGuiseSession() {
		final Thread currentThread = Thread.currentThread(); //get the current thread
		final GuiseSession guiseSession = getGuiseSession(currentThread); //get the session for the current thread
		if(guiseSession == null) { //if there is no Guise session for the current thread
			throw new IllegalStateException("Current thread " + currentThread + " is not associated associated with any Guise session.");
		}
		return guiseSession; //return the session for the current thread
	}

	/**
	 * Retrieves the Guise session information for the given thread. All thread groups up the hierarchy are searched for an instance of
	 * {@link GuiseSessionThreadGroup}.
	 * @return The Guise session for the given thread, or <code>null</code> if the given thread is not in a Guise session thread group.
	 * @see #getGuiseSessionThreadGroup(Thread)
	 */
	final GuiseSession getGuiseSession(final Thread thread) {
		final GuiseSessionThreadGroup guiseSessionThreadGroup = getGuiseSessionThreadGroup(thread); //get the Guise session thread group the given thread is in
		return guiseSessionThreadGroup != null ? guiseSessionThreadGroup.getGuiseSession() : null; //return the session from the thread group, if there is a Guise session thread group
	}

	/**
	 * Retrieves the Guise session thread group. All thread groups up the hierarchy are searched for an instance of {@link GuiseSessionThreadGroup}.
	 * @return The Guise session thread group for the given thread, or <code>null</code> if the given thread is not in a Guise session thread group.
	 */
	final GuiseSessionThreadGroup getGuiseSessionThreadGroup(final Thread thread) {
		return Threads.getThreadGroup(thread, GuiseSessionThreadGroup.class); //return the Guise session thread group for this class
	}

	/**
	 * Private default constructor.
	 * @see #getInstance()
	 */
	private Guise() {
		licensed = getClass().getResource("license.properties") != null; //determine if Guise is licensed; for now we simply see if the license.properties file exists
	}
}
