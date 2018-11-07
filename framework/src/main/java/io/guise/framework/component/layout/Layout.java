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

package io.guise.framework.component.layout;

import com.globalmentor.beans.PropertyBindable;

import io.guise.framework.GuiseSession;
import io.guise.framework.component.Component;
import io.guise.framework.component.LayoutComponent;

/**
 * Contains layout information for a layout component.
 * @param <T> The type of layout constraints associated with each component. This interface and subclasses represent layout definitions, not layout
 *          implementations. If the property of a component's constraints changes, a subclass of {@link LayoutConstraintsPropertyChangeEvent} will be fired
 *          indicating the associated component and constraints for which the value changed.
 * @author Garret Wilson
 */
public interface Layout<T extends Constraints> extends PropertyBindable {

	/** @return The Guise session that owns this layout. */
	public GuiseSession getSession();

	/** @return The layout component that owns this layout, or <code>null</code> if this layout has not been installed into a layout component. */
	public LayoutComponent getOwner();

	/** @return The class representing the type of constraints appropriate for this layout. */
	public Class<? extends T> getConstraintsClass();

	/**
	 * Sets the layout component that owns this layout This method is managed by layout components, and normally should not be called by applications. A layout
	 * cannot be given a layout component if it is already installed in another layout component. Once a layout is installed in a layout component, it cannot be
	 * uninstalled. A layout cannot be given a layout component unless that layout component already recognizes this layout as its layout. If a layout is given the
	 * same layout component it already has, no action occurs.
	 * @param newLayoutComponent The new layout component for this layout.
	 * @throws NullPointerException if the given layout component is <code>null</code>.
	 * @throws IllegalStateException if a different layout component is provided and this layout already has a layout component.
	 * @throws IllegalArgumentException if a different layout component is provided and the given layout component does not already recognize this layout as its
	 *           layout.
	 */
	public void setOwner(final LayoutComponent newLayoutComponent);

	/**
	 * Adds a component to the layout. Called immediately after a component is added to the associated layout component. This method is called by the associated
	 * layout component, and should not be called directly by application code.
	 * @param component The component to add to the layout.
	 * @throws IllegalStateException if this layout has not yet been installed into a layout component.
	 */
	public void addComponent(final Component component);

	/**
	 * Removes a component from the layout. Called immediately before a component is removed from the associated layout component. This method is called by the
	 * associated layout component, and should not be called directly by application code.
	 * @param component The component to remove from the layout.
	 */
	public void removeComponent(final Component component);

	/**
	 * Retrieves layout constraints associated with a component. If the constraints currently associated with the component are not compatible with this layout,
	 * or if no constraints are associated with the given component, default constraints are created and associated with the component.
	 * @param component The component for which layout metadata is being requested.
	 * @return The constraints associated with the component.
	 * @throws IllegalStateException if this layout has not yet been installed into a layout component.
	 * @throws IllegalStateException if no constraints are associated with the given component and this layout does not support default constraints.
	 * @see #getConstraintsClass()
	 * @see Component#getConstraints()
	 * @see Component#setConstraints(Constraints)
	 */
	public T getConstraints(final Component component);

	/**
	 * Creates default constraints for the layout component.
	 * @return New default constraints for the layout component.
	 * @throws IllegalStateException if this layout does not support default constraints.
	 */
	public T createDefaultConstraints();

}
