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

package io.guise.framework.component;

import io.guise.framework.event.*;

/**
 * A component that can contain other components. A composite component may contain other components, but only a {@link Container} allows for application
 * addition and removal of child components.
 * @author Garret Wilson
 */
public interface CompositeComponent extends Component {

	/** @return Whether this component has childh components. */
	public boolean hasChildComponents();

	/** @return An iterable to child components. */
	public Iterable<Component> getChildComponents();

	/** @return The number of child components. */
	//TODO add if needed	public int getChildCount();

	/**
	 * Called when a descendant component is added to a descendant composite component. The target of the event indicates the descendant composite component to
	 * which the descendant component was added. The event is propogated to this component's parent, if any. This method is called by child components and should
	 * not be directly invoked by an application.
	 * @param childComponentEvent The event indicating the added child component and the target parent composite component.
	 */
	//TODO del	public void descendantComponentAdded(final ComponentEvent childComponentEvent);

	/**
	 * Called when a descendant component is removed from a descendant composite component. The target of the event indicates the descendant composite component
	 * from which the descendant component was removed. The event is propogated to this component's parent, if any. This method is called by child components and
	 * should not be directly invoked by an application.
	 * @param childComponentEvent The event indicating the removed child component and the target parent composite component.
	 */
	//TODO del	public void descendantComponentRemoved(final ComponentEvent childComponentEvent);

	/**
	 * Adds a composite component listener. An event will be fired for each descendant component added or removed, with the event target indicating the parent
	 * composite component of the change.
	 * @param compositeComponentListener The composite component listener to add.
	 */
	public void addCompositeComponentListener(final CompositeComponentListener compositeComponentListener);

	/**
	 * Removes a composite component listener. An event will be fired for each descendant component added or removed, with the event target indicating the parent
	 * composite component of the change.
	 * @param compositeComponentListener The composite component listener to remove.
	 */
	public void removeCompositeComponentListener(final CompositeComponentListener compositeComponentListener);

}
