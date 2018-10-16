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

import java.io.IOException;

import com.globalmentor.event.EventListenerManager;
import com.globalmentor.java.Longs;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.transfer.Transferable;
import com.guiseframework.event.GuiseBoundPropertyObject;

/**
 * Abstract implementation of an object that can be depicted on some platform.
 * @author Garret Wilson
 */
public abstract class AbstractDepictedObject extends GuiseBoundPropertyObject implements DepictedObject {

	/** The object managing event listeners. */
	private final EventListenerManager eventListenerManager = new EventListenerManager();

	/** @return The object managing event listeners. */
	protected EventListenerManager getEventListenerManager() {
		return eventListenerManager;
	}

	/** The object depiction identifier */
	private final long depictID;

	@Override
	public long getDepictID() {
		return depictID;
	}

	/** The depictor for this object. */
	private final Depictor<? extends DepictedObject> depictor;

	@Override
	public Depictor<? extends DepictedObject> getDepictor() {
		return depictor;
	}

	@Override
	public void processEvent(final PlatformEvent event) {
		getDepictor().processEvent(event); //ask the depictor to process the event
	}

	@Override
	public void depict() throws IOException {
		getDepictor().depict(); //ask the depictor to depict the object
	}

	/**
	 * Default constructor.
	 * @throws IllegalStateException if no depictor is registered for this object type.
	 */
	public AbstractDepictedObject() {
		final GuiseSession session = getSession(); //get the Guise session
		final Platform platform = session.getPlatform(); //get the Guise platform
		this.depictID = platform.generateDepictID(); //ask the platform to generate a new depict ID
		this.depictor = platform.getDepictor(this); //ask the platform for a depictor for the object
		if(this.depictor == null) { //if no depictor is registered for this object
			throw new IllegalStateException("No depictor registered for class " + getClass());
		}
		notifyDepictorInstalled(depictor); //tell the the depictor it has been installed
		platform.registerDepictedObject(this); //register this depicted object with the platform
	}

	/**
	 * Notifies a depictor that it has been installed in this object.
	 * @param <O> The type of depicted object expected by the depictor.
	 * @param depictor The depictor that has been installed.
	 */
	@SuppressWarnings("unchecked")
	//at this point we have to assume that the correct type of depictor has been registered for this object
	private <O extends DepictedObject> void notifyDepictorInstalled(final Depictor<O> depictor) {
		depictor.installed((O)this); //tell the depictor it has been installed		
	}

	@Override
	public Transferable<?> exportTransfer() {
		return null; //indicate that no data could be exported
	}

	@Override
	public int hashCode() {
		return Longs.hashCode(getDepictID()); //return the hash code of the ID
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns whether the object is a depicted object with the same ID.
	 * </p>
	 */
	@Override
	public boolean equals(final Object object) {
		return object instanceof DepictedObject && getDepictID() == ((DepictedObject)object).getDepictID(); //see if the other object is a depicted object with the same ID
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder(super.toString()); //create a string builder for constructing the string
		stringBuilder.append(' ').append('[').append(getDepictID()).append(']'); //append the ID
		return stringBuilder.toString(); //return the string builder
	}
}
