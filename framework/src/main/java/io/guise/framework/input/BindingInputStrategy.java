/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.input;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.guise.framework.Guise;
import io.guise.framework.GuiseSession;
import io.guise.framework.model.ActionModel;
import io.guise.framework.model.Enableable;

/**
 * An input strategy based upon input bindings between input and other input or actions. Typical uses include binding {@link CommandInput} to
 * {@link KeystrokeInput}, or binding an {@link ActionModel} to {@link CommandInput}. There must be a {@link GuiseSession} in effect when this
 * {@link #input(Input)} is called for this input strategy.
 * @author Garret Wilson
 */
public class BindingInputStrategy extends AbstractInputStrategy {

	/** The thread-safe map of bindings between input and other input or actions. The bound object is either of type {@link Input} or of type {@link ActionModel}. */
	private final Map<Input, Object> bindings = new ConcurrentHashMap<Input, Object>();

	/**
	 * Indicates whether the given input is bound.
	 * @param input The input that may be bound, such as {@link KeystrokeInput} or {@link CommandInput}.
	 * @return <code>true</code> if a binding exists for the given input, else <code>false</code>.
	 */
	public boolean isBound(final Input input) {
		return bindings.containsKey(input); //see if anything is bound to this input
	}

	/**
	 * Binds the given input to other input. If the given input is already bound, the old binding will be replaced.
	 * @param input The input to be bound, such as {@link KeystrokeInput}.
	 * @param targetInput The target input, such as {@link CommandInput}.
	 */
	public void bind(final Input input, final Input targetInput) {
		bindings.put(input, targetInput); //bind the target input to the input
	}

	/**
	 * Binds the given input to an action. If the given input is already bound, the old binding will be replaced.
	 * @param input The input to be bound, such as {@link CommandInput}.
	 * @param targetAction The target action that should be performed.
	 */
	public void bind(final Input input, final ActionModel targetAction) {
		bindings.put(input, targetAction); //bind the target action to the input
	}

	/**
	 * Unbinds the given input from any other input or action. If there is no binding with the given input, no action is taken.
	 * @param input The input to be unbound.
	 */
	public void unbind(final Input input) {
		bindings.remove(input); //remove any bindings to the input
	}

	/** Default constructor with no parent. */
	public BindingInputStrategy() {
		this(null); //construct the class with no parent
	}

	/**
	 * Parent constructor.
	 * @param parent The parent input strategy, or <code>null</code> if there is no parent input strategy.
	 */
	public BindingInputStrategy(final InputStrategy parent) {
		super(parent); //construct the parent class
	}

	@Override
	public boolean input(final Input input) {
		final Object boundObject = bindings.get(input); //get the object bound to this input
		if(boundObject != null) { //if there is a bound object
			if(boundObject instanceof Input) { //if the bound is more input
				Guise.getInstance().getGuiseSession().input((Input)boundObject); //send the input to the Guise session for further processing
			} else if(boundObject instanceof ActionModel) { //if the bound object is an action
				final ActionModel actionModel = ((ActionModel)boundObject); //get the action model
				if(actionModel instanceof Enableable && !((Enableable)actionModel).isEnabled()) { //if the action model is enableable but not enabled
					return false; //don't perform the action or consume the input
				}
				actionModel.performAction(); //perform the action
			} else { //if we don't recognize the bound object, something's wrong, because we control everything that's stored in the map
				throw new AssertionError("Unrecognized input binding: " + boundObject);
			}
			return true; //indicate that we consumed the input
		} else { //if there is nothing bound to the given input
			return super.input(input); //perform the default processing, which includes delegation to any parent input strategy
		}
	}
}
