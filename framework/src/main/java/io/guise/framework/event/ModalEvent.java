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

package io.guise.framework.event;

import io.guise.framework.component.ModalNavigationPanel;

/**
 * An event indicating that a component changed modes. author Garret Wilson
 */
public class ModalEvent extends AbstractGuiseEvent {

	private static final long serialVersionUID = -2934728082421212219L;

	@Override
	public ModalNavigationPanel<?> getSource() {
		return (ModalNavigationPanel<?>)super.getSource(); //cast the event to the appropriate type
	}

	/**
	 * Source constructor.
	 * @param source The object on which the event initially occurred.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public ModalEvent(final ModalNavigationPanel<?> source) {
		super(source); //construct the parent class
	}

}
