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

package com.guiseframework.component;

import java.net.URI;

import com.globalmentor.net.URIPath;
import com.guiseframework.event.NavigateActionListener;
import com.guiseframework.model.*;
import com.guiseframework.prototype.ActionPrototype;

/**
 * Control with an action model rendered as a link.
 * @author Garret Wilson
 */
public class Link extends AbstractLinkControl {

	/** Default constructor. */
	public Link() {
		this((String)null); //construct the class with no label
	}

	/**
	 * Label convenience constructor.
	 * @param label The label to use in the link, or <code>null</code> if there should be no label.
	 */
	public Link(final String label) {
		this(new DefaultInfoModel(label)); //construct the class with a default info model
	}

	/**
	 * Info model constructor.
	 * @param infoModel The component info model.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public Link(final InfoModel infoModel) {
		super(infoModel, new DefaultActionModel(), new DefaultEnableable()); //construct the parent class with the given info model and default other models
	}

	/**
	 * Info model, action model, and enableable object constructor.
	 * @param infoModel The component info model.
	 * @param actionModel The component action model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @throws NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	 */
	public Link(final InfoModel infoModel, final ActionModel actionModel, final Enableable enableable) {
		super(infoModel, actionModel, enableable); //construct the parent class
	}

	/**
	 * Label and navigation path convenience constructor. A {@link NavigateActionListener} will be installed to navigate to the provided navigation path.
	 * @param label The label to use in the link, or <code>null</code> if there should be no label.
	 * @param navigationPath The destination path that will be used for navigation when the link is selected.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 */
	public Link(final String label, final URIPath navigationPath) {
		this(label); //construct the class with default models and the label
		addActionListener(new NavigateActionListener(navigationPath)); //add an action listener to navigate to the indicated location
	}

	/**
	 * Label and navigation URI convenience constructor. A {@link NavigateActionListener} will be installed to navigate to the provided navigation URI.
	 * @param label The label to use in the link, or <code>null</code> if there should be no label.
	 * @param navigationURI The destination URI that will be used for navigation when the link is selected.
	 * @throws NullPointerException if the given navigation URI is <code>null</code>.
	 */
	public Link(final String label, final URI navigationURI) {
		this(label); //construct the class with default models and the label
		addActionListener(new NavigateActionListener(navigationURI)); //add an action listener to navigate to the indicated location
	}

	/**
	 * Prototype constructor.
	 * @param actionPrototype The prototype on which this component should be based.
	 * @throws NullPointerException if the given prototype is <code>null</code>.
	 */
	public Link(final ActionPrototype actionPrototype) {
		this(actionPrototype, actionPrototype, actionPrototype); //use the action prototype as every needed model
	}

}
