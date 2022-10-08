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

/**
 * An abstract implementation of a strategy for processing input.
 * @author Garret Wilson
 */
public class AbstractInputStrategy implements InputStrategy {

	/** The parent input strategy, or <code>null</code> if there is no parent input strategy. */
	private final InputStrategy parent;

	@Override
	public InputStrategy getParent() {
		return parent;
	}

	/**
	 * Parent constructor.
	 * @param parent The parent input strategy, or <code>null</code> if there is no parent input strategy.
	 */
	public AbstractInputStrategy(final InputStrategy parent) {
		this.parent = parent; //save the parent
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version does nothing besides delegate to the parent input strategy.
	 * </p>
	 */
	@Override
	public boolean input(final Input input) {
		final InputStrategy parent = getParent(); //get the parent
		if(parent != null) { //if there is a parent input strategy
			return parent.input(input); //pass the input to the parent
		} else { //if there is no parent
			return false; //indicate that the input was not consumed
		}
	}
}
