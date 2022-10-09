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

import com.globalmentor.event.AbstractEvent;

import io.guise.framework.Guise;
import io.guise.framework.GuiseSession;

/**
 * The base class for custom Guise events.
 * @author Garret Wilson
 */
public abstract class AbstractGuiseEvent extends AbstractEvent implements GuiseEvent {

	private static final long serialVersionUID = -2091559666781881024L;

	/** The Guise session in which this event was generated. */
	private final GuiseSession session;

	@Override
	public GuiseSession getSession() {
		return session;
	}

	/**
	 * Source constructor.
	 * @param source The object on which the event initially occurred.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public AbstractGuiseEvent(final Object source) {
		super(source); //construct the parent class
		this.session = Guise.getInstance().getGuiseSession(); //store a reference to the current Guise session
	}

}
