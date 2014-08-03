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

package com.guiseframework.event;

/**
 * An object that listens for components being added to or removed from a composite component.
 * @author Garret Wilson
 */
public interface CompositeComponentListener extends GuiseEventListener {

	/**
	 * Called when a child component is added to a composite component.
	 * @param childComponentEvent The event indicating the added child component and the target parent composite component.
	 */
	public void childComponentAdded(final ComponentEvent childComponentEvent);

	/**
	 * Called when a child component is removed from a composite component.
	 * @param childComponentEvent The event indicating the removed child component and the target parent composite component.
	 */
	public void childComponentRemoved(final ComponentEvent childComponentEvent);

}
