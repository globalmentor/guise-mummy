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

import java.beans.*;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.globalmentor.beans.PropertyBindable;
import com.guiseframework.*;
import com.guiseframework.event.*;

/**
 * An abstract strategy for depicting objects on some platform.
 * <p>
 * The {@link Depictor#GENERAL_PROPERTY} is used to indicate that some general property has changed.
 * </p>
 * @param <O> The type of object being depicted.
 * @author Garret Wilson
 */
public abstract class AbstractDepictor<O extends DepictedObject> implements Depictor<O> {

	/** The Guise session that owns this object. */
	private final GuiseSession session;

	/** @return The Guise session that owns this object. */
	public GuiseSession getSession() {
		return session;
	}

	private final Platform platform;

	@Override
	public Platform getPlatform() {
		return platform;
	}

	/** {@inheritDoc} This method delegates to {@link Platform#getDepictContext()}. */
	@Override
	public DepictContext getDepictContext() {
		return getPlatform().getDepictContext();
	}

	/** The thread-safe list of properties that are to be ignored. */
	private final Set<String> ignoredProperties = new CopyOnWriteArraySet<String>();

	/** @return The depicted object properties that are to be ignored. */
	protected Set<String> getIgnoredProperties() {
		return ignoredProperties;
	}

	/** The thread-safe list of modified properties. */
	private final Set<String> modifiedProperties = new CopyOnWriteArraySet<String>();

	/** @return The depicted object properties that have been modified. */
	protected Set<String> getModifiedProperties() {
		return modifiedProperties;
	}

	/**
	 * Calls when a property has been modified to sets whether a property has been modified. If the property's modified status is set to <code>true</code>, the
	 * depictor's {@link #isDepicted()} status is changed to <code>false</code>. If the property's modified status is set to <code>false</code> and there are no
	 * other modified properties, the depictor's {@link #isDepicted()} status is set to <code>true</code>.
	 * @param property The property that has been modified.
	 * @param modified Whether the property has been modified.
	 * @see #setDepicted(boolean)
	 */
	protected void setPropertyModified(final String property, final boolean modified) {
		if(modified) { //if the property is modified
			modifiedProperties.add(property); //add this property to the list of modified properties
			depicted = false; //note that the depiction is not updated
		} else { //if the property is not modified
			if(modifiedProperties.remove(property)) { //remove the property from the set of modified properties; if the property was in the set
				if(modifiedProperties.isEmpty()) { //if there are no modified properties
					depicted = true; //count the depiction as updated
				}
			}
		}
	}

	/** The listener that marks this depiction as dirty if a change occurs. */
	private final DepictedPropertyChangeListener depictedPropertyChangeListener = new DepictedPropertyChangeListener();

	/** @return The listener that marks this depiction as dirty if a change occurs. */
	protected DepictedPropertyChangeListener getDepictedPropertyChangeListener() {
		return depictedPropertyChangeListener;
	}

	/** The object being depicted. */
	private O depictedObject = null;

	@Override
	public O getDepictedObject() {
		return depictedObject;
	}

	/** Whether this depictor's representation of the depicted object is up to date. */
	private boolean depicted = false;

	@Override
	public boolean isDepicted() {
		return depicted;
	}

	@Override
	public void setDepicted(final boolean newDepicted) {
		if(newDepicted) { //if the depiction is being marked as updated 
			modifiedProperties.clear(); //remove all modified properties
		} else { //if the depiction is being marked as not updated
			modifiedProperties.add(GENERAL_PROPERTY); //add the general property to the list of modified properties				
		}
		depicted = newDepicted; //update the depicted status
	}

	/** Default constructor. */
	public AbstractDepictor() {
		this.session = Guise.getInstance().getGuiseSession(); //store a reference to the current Guise session
		this.platform = this.session.getPlatform(); //store a reference to the platform
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version listens for property changes of a {@link PropertyBindable} object.
	 * </p>
	 * <p>
	 * This version listens for list changes of a {@link ListListenable} object.
	 * </p>
	 * @see #depictedPropertyChangeListener
	 */
	@Override
	public void installed(final O depictedObject) {
		if(this.depictedObject != null) { //if this depictor is already installed
			throw new IllegalStateException("Depictor is already installed in a depicted object.");
		}
		this.depictedObject = depictedObject; //change depicted objects
		if(depictedObject instanceof PropertyBindable) { //if the depicted object allows bound properties
			((PropertyBindable)depictedObject).addPropertyChangeListener(getDepictedPropertyChangeListener()); //listen for property changes
		}
		if(depictedObject instanceof ListListenable) { //if the depicted object notifies of list changes
			((ListListenable<Object>)depictedObject).addListListener(getDepictedPropertyChangeListener()); //listen for list changes
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version stop listening for property changes of a {@link PropertyBindable} object.
	 * </p>
	 * <p>
	 * This version stops listening for list changes of a {@link ListListenable} object.
	 * </p>
	 * @see #depictedPropertyChangeListener
	 */
	@Override
	public void uninstalled(final O depictedObject) {
		if(this.depictedObject == null) { //if this depictor is not installed
			throw new IllegalStateException("Depictor is not installed in a depicted object.");
		}
		this.depictedObject = null; //remove the depicted object
		if(depictedObject instanceof PropertyBindable) { //if the depicted object allows bound properties
			((PropertyBindable)depictedObject).removePropertyChangeListener(depictedPropertyChangeListener); //stop listening for property changes
		}
		if(depictedObject instanceof ListListenable) { //if the depicted object notifies of list changes
			((ListListenable<Object>)depictedObject).removeListListener(depictedPropertyChangeListener); //stop listening for list changes
		}
	}

	@Override
	public void processEvent(final PlatformEvent event) {
	}

	/** {@inheritDoc} This implementation marks the depiction as depicted. */
	@Override
	public void depict() throws IOException {
		setDepicted(true); //show that the depiction has been updated
	}

	/**
	 * Called when a depicted object bound property is changed.
	 * <p>
	 * This method may also be called for objects related to the depicted object, so if specific properties are checked the event source should be verified to be
	 * the depicted object.
	 * </p>
	 * <p>
	 * This implementation marks the property as being modified if the property is not an ignored property.
	 * </p>
	 * @param propertyChangeEvent An event object describing the event source and the property that has changed.
	 * @see #getIgnoredProperties()
	 * @see #setPropertyModified(String, boolean)
	 */
	protected void depictedObjectPropertyChange(final PropertyChangeEvent propertyChangeEvent) {
		final String propertyName = propertyChangeEvent.getPropertyName(); //get the name of the changing property
		if(getIgnoredProperties().contains(propertyName)) { //if this is an ignored property
			return; //ignore this property change
		}
		setPropertyModified(propertyName, true); //show that a property has been modified
	}

	/**
	 * A listener that marks this depiction as dirty if changes occur.
	 * <p>
	 * Property changes are delegated to {@link AbstractDepictor#depictedObjectPropertyChange(PropertyChangeEvent)}.
	 * </p>
	 * @author Garret Wilson
	 */
	protected class DepictedPropertyChangeListener implements PropertyChangeListener, ListListener<Object> {

		@Override
		public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
			depictedObjectPropertyChange(propertyChangeEvent); //delegate to the outer class method
		}

		@Override
		public void listModified(final ListEvent<Object> listEvent) {
			setDepicted(false); //show that we need general updates			
		}
	};

}
