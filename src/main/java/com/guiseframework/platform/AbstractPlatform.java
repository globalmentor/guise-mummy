/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.platform;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.*;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

import com.globalmentor.collections.DecoratorReadWriteLockMap;
import com.globalmentor.collections.PurgeOnWriteWeakValueHashMap;
import com.globalmentor.collections.ReadWriteLockMap;
import com.globalmentor.util.*;
import com.guiseframework.GuiseApplication;

/**
 * The platform on which Guise objects are being depicted.
 * @author Garret Wilson
 */
public abstract class AbstractPlatform implements Platform {

	/** The Guise application running on this platform. */
	private final GuiseApplication application;

	/** @return The Guise application running on this platform. */
	public GuiseApplication getApplication() {
		return application;
	}

	/** The lock used for exclusive depiction on the platform. */
	private Lock depictLock = new ReentrantLock();

	/** @return The lock used for exclusive depiction on the platform. */
	public Lock getDepictLock() {
		return depictLock;
	}

	/** The map of depictors for depicted object types. */
	private final Map<Class<? extends DepictedObject>, Class<? extends Depictor<?>>> depictorMap = new ConcurrentHashMap<Class<? extends DepictedObject>, Class<? extends Depictor<?>>>();

	/**
	 * Registers the class of a depictor to depict an object of the given class (and by default subclasses).
	 * @param <O> The type of registered depicted object class.
	 * @param depictedObjectClass The class of the depicted object for which the depictor should be registered.
	 * @param depictorClass The class of depictor to use for depicting the objects.
	 * @return The depictor class previously registered with the given depicted object class, or <code>null</code> if there was no previous registration.
	 */
	@SuppressWarnings("unchecked")
	//it would be nice to guarantee Class<? extends Depictor<? super O>> access here, but Java classes do not support more than one level of generics
	protected <O extends DepictedObject> Class<? extends Depictor<? super O>> registerDepictorClass(final Class<O> depictedObjectClass,
			final Class<?> depictorClass) {
		return (Class<? extends Depictor<? super O>>)depictorMap.put(depictedObjectClass, (Class<? extends Depictor<?>>)depictorClass); //register the depictor and return the old registration, if any
	}

	/**
	 * Determines the depictor class registered for the given depicted object class.
	 * @param <O> The type of registered depicted object class.
	 * @param depictedObjectClass The class of depicted object that may be registered.
	 * @return The class of depictor registered to depict object of the specific class, or <code>null</code> if no depictor is registered.
	 */
	@SuppressWarnings("unchecked")
	//all access classes to the map guarantee the type
	protected <O extends DepictedObject> Class<? extends Depictor<? super O>> getRegisteredDepictorClass(final Class<? extends DepictedObject> depictedObjectClass) {
		return (Class<? extends Depictor<? super O>>)depictorMap.get(depictedObjectClass); //return any registration
	}

	/**
	 * Determines the depictor class appropriate for the given depicted object class. A depicted class is located by individually looking up the depicted object
	 * class hierarchy for registered depictors.
	 * @param <O> The type of registered depicted object class.
	 * @param depictedObjectClass The class of depicted object for which a depictor should be returned.
	 * @return A class of depictor for the given depicted object class, or <code>null</code> if no depictor is registered.
	 */
	protected <O extends DepictedObject> Class<? extends Depictor<? super O>> getDepictorClass(final Class<O> depictedObjectClass) {
		final List<Class<? extends DepictedObject>> depictedObjectAncestorClasses = getAncestorClasses(depictedObjectClass, DepictedObject.class); //get all the classes up to and including the depicted object class, in increasing order of distance and abstractness
		for(final Class<? extends DepictedObject> depictedObjectAncestorClass : depictedObjectAncestorClasses) { //for each ancestor class
			final Class<? extends Depictor<? super O>> depictorClass = getRegisteredDepictorClass(depictedObjectAncestorClass); //see if there is a depictor class registered for this depicted object type
			if(depictorClass != null) { //if we found a depictor class
				return depictorClass; //return the depictor class
			}
		}
		return null;
	}

	/**
	 * Determines the depictor appropriate for the given depicted object. A depictor class is located by individually looking up the depicted object class
	 * hiearchy for registered depictor classes.
	 * @param <O> The type of depicted object.
	 * @param depictedObject The depicted object for which a depictor should be returned.
	 * @return A depictor to depict the given component, or <code>null</code> if no depictor is registered.
	 * @throws IllegalStateException if the registered depictor could not be instantiated for some reason.
	 */
	@SuppressWarnings("unchecked")
	//casting is needed because Object.getClass() doesn't return a class for THIS type
	public <O extends DepictedObject> Depictor<? super O> getDepictor(final O depictedObject) {
		final Class<O> depictedObjectClass = (Class<O>)depictedObject.getClass(); //get the depicted object class
		final Class<? extends Depictor<? super O>> depictorClass = getDepictorClass(depictedObjectClass); //walk the hierarchy to see if there is a depictor class registered for this component type
		if(depictorClass != null) { //if we found a depictor class
			try {
				return depictorClass.newInstance(); //return a new instance of the class
			} catch(final InstantiationException instantiationException) {
				throw new IllegalStateException(instantiationException);
			} catch(final IllegalAccessException illegalAccessException) {
				throw new IllegalStateException(illegalAccessException);
			}
		}
		return null; //show that we could not find a registered depictor
	}

	/** The weakly-referenced thread-safe map of depicted objects, keyed to their IDs. */
	private final ReadWriteLockMap<Long, DepictedObject> idDepictedObjectMap = new DecoratorReadWriteLockMap<Long, DepictedObject>(
			new PurgeOnWriteWeakValueHashMap<Long, DepictedObject>());

	/**
	 * Registers a depicted object so that it can interact with the platform.
	 * @param depictedObject The depicted object to register.
	 * @throws NullPointerException if the given depicted object is <code>null</code>.
	 */
	public void registerDepictedObject(final DepictedObject depictedObject) {
		idDepictedObjectMap.put(Long.valueOf(checkInstance(depictedObject, "Depicted object cannot be null.").getDepictID()), depictedObject);
	}

	/**
	 * Unregisters a depicted object so that no longer interacts with the platform.
	 * @param depictedObject The depicted object to unregister.
	 * @throws NullPointerException if the given depicted object is <code>null</code>.
	 */
	public void unregisterDepictedObject(final DepictedObject depictedObject) {
		idDepictedObjectMap.remove(Long.valueOf(checkInstance(depictedObject, "Depicted object cannot be null.").getDepictID()));
	}

	/**
	 * Retrieves a depicted object that has been registered with the platform by the ID of the depicted object.
	 * @param depictedObjectID The ID of the depicted object to retrieve.
	 * @return The registered depicted object with the given ID, or <code>null</code> if there is no depicted object registered with this platform with the given
	 *         ID.
	 */
	public DepictedObject getDepictedObject(final long depictedObjectID) {
		return idDepictedObjectMap.get(Long.valueOf(depictedObjectID)); //return the depicted object, if any, with the given ID
	}

	/** The variable used to generate unique depict IDs. */
	private final AtomicLong depictIDCounter = new AtomicLong(0);

	/**
	 * Generates a new depict ID unique to this session platform.
	 * @return A new depict ID unique to this session platform.
	 */
	public long generateDepictID() {
		return depictIDCounter.incrementAndGet(); //atomically get the next counter value
	}

	/** The thread-safe queue of messages to be delivered to the platform. */
	private final Queue<? extends PlatformMessage> sendMessageQueue = new ConcurrentLinkedQueue<PlatformMessage>();

	/** @return The thread-safe queue of messages to be delivered to the platform. */
	public Queue<? extends PlatformMessage> getSendMessageQueue() {
		return sendMessageQueue;
	}

	/**
	 * Application constructor.
	 * @param application The Guise application running on this platform.
	 * @throws NullPointerException if the given application and/or environment is <code>null</code>.
	 */
	public AbstractPlatform(final GuiseApplication application) {
		this.application = checkInstance(application, "Application cannot be null."); //save the application		
	}
}