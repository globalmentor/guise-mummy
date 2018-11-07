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

package io.guise.framework.component.transfer;

import io.guise.framework.component.Component;

/**
 * A strategy for importing data into a component.
 * @param <C> The type of component supported by this export strategy.
 * @author Garret Wilson
 */
public interface ImportStrategy<C extends Component> {

	/**
	 * Determines whether this strategy can import the given transferable object.
	 * @param component The component into which the object will be transferred.
	 * @param transferable The object to be transferred.
	 * @return <code>true</code> if the given object can be imported.
	 */
	public boolean canImportTransfer(final C component, final Transferable<?> transferable);

	/**
	 * Imports the given data into the given component.
	 * @param component The component into which the object will be transferred.
	 * @param transferable The object to be transferred.
	 * @return <code>true</code> if the given object was imported.
	 */
	public boolean importTransfer(final C component, final Transferable<?> transferable);
}
