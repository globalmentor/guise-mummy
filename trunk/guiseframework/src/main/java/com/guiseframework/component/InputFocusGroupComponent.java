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

import static com.globalmentor.java.Classes.*;

import java.beans.PropertyVetoException;

import com.guiseframework.event.InputFocusStrategy;

/**
 * An input focusable Guise component that serves as a parent of other components that can receive input focus.
 * @author Garret Wilson
 */
public interface InputFocusGroupComponent extends InputFocusableComponent {

	/** The input focus strategy bound property. */
	public final static String INPUT_FOCUS_STRATEGY_PROPERTY = getPropertyName(InputFocusGroupComponent.class, "inputFocusStrategy");
	/** The input focused component bound property. */
	public final static String INPUT_FOCUSED_COMPONENT_PROPERTY = getPropertyName(InputFocusGroupComponent.class, "inputFocusedComponent");

	/** @return The input focus strategy for this input focus group. */
	public InputFocusStrategy getInputFocusStrategy();

	/**
	 * Sets the input focus strategy. This is a bound property
	 * @param newInputFocusStrategy The input focus strategy for this group.
	 * @throws NullPointerException if the given input focus strategy is <code>null</code>.
	 * @see #INPUT_FOCUS_STRATEGY_PROPERTY
	 */
	public void setInputFocusStrategy(final InputFocusStrategy newInputFocusStrategy);

	/**
	 * Indicates the component within this group that has the input focus. The focused component may be another {@link InputFocusGroupComponent}, which in turn
	 * will have its own focused component.
	 * @return The component within this group that has the input focus, or <code>null</code> if no component currently has the input focus.
	 */
	public InputFocusableComponent getInputFocusedComponent();

	/**
	 * Sets the focused component within this input focus group. This is a bound property.
	 * @param newInputFocusedComponent The component to receive the input focus.
	 * @throws PropertyVetoException if the given component is not a focusable component within this input focus group, the component cannot receive the input
	 *           focus, or the input focus change has otherwise been vetoed.
	 * @see #getInputFocusStrategy()
	 * @see #INPUT_FOCUSED_COMPONENT_PROPERTY
	 */
	public void setInputFocusedComponent(final InputFocusableComponent newInputFocusedComponent) throws PropertyVetoException;

	/**
	 * Indicates the leaf component within this group that has the focus. If this group's focused component is another {@link FocusGroupComponent}, the leaf focus
	 * component component is recursively retrieved from that component. This method will return a {@link FocusGroupComponent} if that focus group has no focused
	 * comonent. If there is no focused component within this focus group, this method returns <code>null</code>.
	 * @return The leaf component within this group that has the focus, or <code>null</code> if no leaf focusable component within this group currently has the
	 *         focus.
	 */
	//TODO move to ApplicationFrame	public FocusableComponent getLeafFocusedComponent();

}