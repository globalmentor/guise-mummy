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

import static com.globalmentor.java.Objects.*;

import java.util.ArrayList;

import com.guiseframework.component.*;
import com.guiseframework.event.*;

/**
 * Abstract strategy for keeping track of prototype providers and working with provisioned prototypes. When the prototype providers change provided prototypes,
 * those provided prototypes are processed. This version monitors the parent composite component children and automatically uses top-level prototype providers
 * added to or removed from the hierarchy. Prototype provisions are not processed initially; this strategy should be initialized after construction by calling
 * {@link #processPrototypeProvisions()}. This class is thread safe based upon its exposed read and write locks.
 * @author Garret Wilson
 */
public abstract class AbstractCompositeComponentPrototypeProvisionStrategy extends AbstractPrototypeProvisionStrategy {

	/** The composite component the top-level prototype provider children of which will be monitored. */
	private final CompositeComponent parentComponent;

	/** @return The composite component the top-level prototype provider children of which will be monitored. */
	protected CompositeComponent getParentComponent() {
		return parentComponent;
	}

	/**
	 * The listener that listens for prototype producers being added and removed and manages those that are top-level prototype producers under the parent
	 * component. If one or more prototypes provider is added or removed, prototype provisions are processed.
	 * @see #processPrototypeProvisions()
	 */
	private final CompositeComponentListener compositeComponentListener = new CompositeComponentListener() {

		/**
		 * Called when a child component is added to a composite component.
		 * @param childComponentEvent The event indicating the added child component and the target parent composite component.
		 */
		public void childComponentAdded(final ComponentEvent childComponentEvent) {
			boolean modifiedPrototypeProviders = false; //keep track of whether we modified prototype providers; if so, we'll need to reprocess the prototype provisions
			Component component = childComponentEvent.getComponent(); //get the added component
			if(component instanceof PrototypeProvider) { //if the added child is a prototype provider
				final PrototypeProvider prototypeProvider = (PrototypeProvider)component; //get the component as a prototype provider
				final CompositeComponent parentComponent = getParentComponent(); //get the parent component
				component = component.getParent(); //start with the component's parent
				while(component != parentComponent) { //while we haven't reached the parent component
					assert component != null : "Added child components should have the parent component somewhere along the hierarchy.";
					if(component instanceof PrototypeProvider) { //if we found another prototype provider between us and the added prototype provider
						return; //this is not a top-level prototype provider; ignore it
					}
					component = component.getParent(); //go up another level
				}
				modifiedPrototypeProviders = addPrototypeProvider(prototypeProvider); //the added component is a top-level prototype provider, so add it to the set of managed prototype providers; see if we actually changed the known prototype providers
			} else if(component instanceof CompositeComponent) { //if the added child is not a prototype provider, if it is a composite component it may have child prototype providers already added; make sure those are managed
				for(final PrototypeProvider prototypeProvider : Components.getChildComponents((CompositeComponent)component, PrototypeProvider.class,
						new ArrayList<PrototypeProvider>(), true, false)) { //get all the top-level descendant prototype provider components of the component
					if(addPrototypeProvider(prototypeProvider)) { //manage this prototype provider; if this is a new prototype provider
						modifiedPrototypeProviders = true; //show that we now need to process prototype provisions
					}
				}
			}
			if(modifiedPrototypeProviders) { //if we added one or more prototype providers
				processPrototypeProvisions(); //process the prototype provisions, now that we have different prototype providers
			}
		}

		/**
		 * Called when a child component is removed from a composite component.
		 * @param childComponentEvent The event indicating the removed child component and the target parent composite component.
		 */
		public void childComponentRemoved(final ComponentEvent childComponentEvent) {
			boolean modifiedPrototypeProviders = false; //keep track of whether we modified prototype providers; if so, we'll need to reprocess the prototype provisions
			final Object component = childComponentEvent.getComponent(); //get the removed component
			if(component instanceof PrototypeProvider) { //if the removed child is a prototype provider
				if(removePrototypeProvider((PrototypeProvider)component)) { //remove the prototype provider; this will have no effect if we weren't managing it to begin with, so it's not worth it to check ahead of time; if this was one of our prototype providers
					modifiedPrototypeProviders = true; //show that we now need to process prototype provisions
				}
			} else if(component instanceof CompositeComponent) { //if the removed child is not a prototype provider, if it is a composite component it may have child prototype providers; make sure those are removed
				for(final PrototypeProvider prototypeProvider : Components.getChildComponents((CompositeComponent)component, PrototypeProvider.class,
						new ArrayList<PrototypeProvider>(), true, false)) { //get all the top-level descendant prototype provider components of the component
					if(removePrototypeProvider(prototypeProvider)) { //remove this prototype provider; if this is a new prototype provider
						modifiedPrototypeProviders = true; //show that we now need to process prototype provisions
					}
				}
			}
			if(modifiedPrototypeProviders) { //if we removed one or more prototype providers
				processPrototypeProvisions(); //process the prototype provisions, now that we have different prototype providers
			}
		}
	};

	/**
	 * Parent component and prototype providers constructor.
	 * @param parentComponent The composite component the top-level prototype provider children of which will be monitored.
	 * @param basePrototypeProviders The base prototype providers that will provide prototypes for processing, outside the children of the composite component
	 *          parent.
	 * @throws NullPointerException if the given parent component, prototype providers, and/or one or more prototype provider is <code>null</code>.
	 */
	public AbstractCompositeComponentPrototypeProvisionStrategy(final CompositeComponent parentComponent, final PrototypeProvider... basePrototypeProviders) {
		super(basePrototypeProviders); //construct the parent class
		this.parentComponent = checkInstance(parentComponent, "Parent component cannot be null.");
		for(final PrototypeProvider prototypeProvider : Components.getChildComponents(parentComponent, PrototypeProvider.class, new ArrayList<PrototypeProvider>(),
				true, false)) { //get all the top-level descendant prototype provider components of the parent component
			addPrototypeProvider(prototypeProvider); //manage this prototype provider
		}
		parentComponent.addCompositeComponentListener(compositeComponentListener); //listen for children being added and removed
	}

}
