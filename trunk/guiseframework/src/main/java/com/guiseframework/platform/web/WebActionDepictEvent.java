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

package com.guiseframework.platform.web;

import com.guiseframework.platform.DepictedObject;

/**
 * Indicates that an action has been initiated on the web platform.
 * @author Garret Wilson
 */
public class WebActionDepictEvent extends AbstractWebDepictEvent {

	/** The ID of the component target site, or <code>null</code> if there was no component target. */
	private final String targetID;

	/** @return The ID of the component target site, or <code>null</code> if there was no component target. */
	public String getTargetID() {
		return targetID;
	}

	/** The action identifier, or <code>null</code> if this event represents a general action. */
	private final String actionID; //TODO maybe eventually delete this and replace related code reference with WebChangeEvent

	/** @return The action identifier, or <code>null</code> if this event represents a general action. */
	public String getActionID() {
		return actionID;
	}

	/** The zero-based option, such as 0 for a left click or 1 for a right click. */
	private final int option;

	/** @return The zero-based option, such as 0 for a left click or 1 for a right click. */
	public int getOption() {
		return option;
	}

	/**
	 * Depicted object, depicted object, target ID, action ID, and option constructor.
	 * @param depictedObject The depicted object on which the event initially occurred.
	 * @param targetID The ID of the component target site, or <code>null</code> if there was no component target.
	 * @param actionID The action identifier, or <code>null</code> if this event represents a general action.
	 * @param option The zero-based option, such as 0 for a left click or 1 for a right click.
	 * @throws NullPointerException if the given context and/or component ID is <code>null</code>.
	 */
	public WebActionDepictEvent(final DepictedObject depictedObject, final String targetID, final String actionID, final int option) {
		super(depictedObject); //construct the parent class
		this.targetID = targetID;
		this.actionID = actionID;
		this.option = option;
	}
}
