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

package com.guiseframework.prototype;

import java.beans.PropertyChangeListener;
import java.util.*;
import static java.util.Collections.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.globalmentor.beans.*;
import com.globalmentor.collections.DecoratorReadWriteLockSet;
import com.globalmentor.util.*;

/**
 * Abstract strategy for keeping track of prototype providers and working with provisioned prototypes. When the prototype providers change provided prototypes,
 * those provided prototypes are processed. Prototype provisions are not processed initially; this strategy should be initialized after construction by calling
 * {@link #processPrototypeProvisions()}. This class is thread safe based upon its exposed read and write locks.
 * @author Garret Wilson
 */
public abstract class AbstractPrototypeProvisionStrategy extends ReentrantReadWriteLock {

	/** The prototype providers known to this provision strategy. */
	private final Set<PrototypeProvider> prototypeProviders = new DecoratorReadWriteLockSet<PrototypeProvider>(new HashSet<PrototypeProvider>(), this);

	/**
	 * Returns an iterable to the prototype providers known to this provision strategy. This iterable should be accessed under a read lock.
	 * @return The prototype providers known to this provision strategy.
	 */
	protected Iterable<PrototypeProvider> getPrototypeProviders() {
		return unmodifiableSet(prototypeProviders);
	}

	/**
	 * Add a prototoype provider to be managed. If the prototype provider is already being managed, no action occurs.
	 * @param prototypeProvider The prototype provider to add.
	 * @return <code>true</code> if the prototype provider was not already being managed.
	 */
	protected boolean addPrototypeProvider(final PrototypeProvider prototypeProvider) {
		writeLock().lock(); //get a write lock
		try {
			final boolean result = prototypeProviders.add(prototypeProvider); //add this prototype provider to our set
			if(result) { //if we weren't already managing it
				prototypeProvider.addPropertyChangeListener(PrototypeProvider.PROTOTYPE_PROVISIONS_PROPERTY, prototypeProvisionsChangeListener); //listen for changes to this prototype producer's prototype provisions
			}
			return result; //return whether we actually added a prototype provider
		} finally {
			writeLock().unlock(); //always release the write lock
		}
	}

	/**
	 * Removes a prototoype provider being managed. If the prototype provider is not being managed, no action occurs.
	 * @param prototypeProvider The prototype provider to remove.
	 * @return <code>true</code> if the prototype provider was not already being managed.
	 */
	protected boolean removePrototypeProvider(final PrototypeProvider prototypeProvider) {
		writeLock().lock(); //get a write lock
		try {
			final boolean result = prototypeProviders.remove(prototypeProvider); //remove this prototype provider from our set
			if(result) { //if we were managing it
				prototypeProvider.removePropertyChangeListener(PrototypeProvider.PROTOTYPE_PROVISIONS_PROPERTY, prototypeProvisionsChangeListener); //stop listening for changes to this prototype producer's prototype provisions
			}
			return result; //return whether we actually removed a prototype provider
		} finally {
			writeLock().unlock(); //always release the write lock
		}
	}

	/**
	 * The change listener that listens for the provided prototypes of a prototype provider to change, and in response gathers prototype provisions from all the
	 * prototype providers and processes them.
	 * @see #processPrototypeProvisions()
	 */
	private final PropertyChangeListener prototypeProvisionsChangeListener = new AbstractGenericPropertyChangeListener<Set<PrototypeProvision<?>>>() {

		public void propertyChange(final GenericPropertyChangeEvent<Set<PrototypeProvision<?>>> genericPropertyChangeEvent) { //if different prototypes are provided
			processPrototypeProvisions(); //process the prototype provisions
		}
	};

	/**
	 * Prototype providers constructor.
	 * @param prototypeProviders The prototype providers that will provide prototypes for processing.
	 * @throws NullPointerException if the given prototype providers and/or one or more prototype provider is <code>null</code>.
	 */
	public AbstractPrototypeProvisionStrategy(final PrototypeProvider... prototypeProviders) {
		for(final PrototypeProvider prototypeProvider : prototypeProviders) { //look at each of the prototype providers
			addPrototypeProvider(prototypeProvider); //manage this prototype provider
		}
	}

	/**
	 * Gather prototype provisions from the known prototype providers. The returned set is mutable.
	 * @return The prototype provisions gathered for later processing from the known prototype providers.
	 */
	protected Set<PrototypeProvision<?>> gatherPrototypeProvisions() {
		final Set<PrototypeProvision<?>> prototypeProvisions = new TreeSet<PrototypeProvision<?>>(); //create a sorted set to assist in using the prototype provisions
		for(final PrototypeProvider prototypeProvider : getPrototypeProviders()) { //look at each of the prototype providers
			prototypeProvisions.addAll(prototypeProvider.getPrototypeProvisions()); //add these prototype provisions from this prototype provider
		}
		return prototypeProvisions; //return the prototype provisions we gathered
	}

	/**
	 * Processes prototype provisions.
	 * @see #gatherPrototypeProvisions()
	 * @see #processPrototypeProvisions(Set)
	 */
	public void processPrototypeProvisions() {
		processPrototypeProvisions(gatherPrototypeProvisions()); //gather prototype provisions and process them		
	}

	/**
	 * Processes prototype provisions.
	 * @param prototypeProvisions The mutable set of prototype provisions to be used.
	 */
	protected abstract void processPrototypeProvisions(final Set<PrototypeProvision<?>> prototypeProvisions);

}
