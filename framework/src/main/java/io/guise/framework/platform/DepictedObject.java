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

package io.guise.framework.platform;

import java.io.IOException;

import io.guise.framework.GuiseSession;
import io.guise.framework.component.transfer.Transferable;

/**
 * An object that can be depicted on some platform.
 * @author Garret Wilson
 */
public interface DepictedObject {

	/** @return The Guise session that owns this object. */
	public GuiseSession getSession();

	/** @return The object depiction identifier. */
	public long getDepictID();

	/** @return The depictor for this object. */
	public Depictor<? extends DepictedObject> getDepictor();

	/**
	 * Exports data from the depicted object. Each export strategy, from last to first added, will be asked to export data, until one is successful.
	 * @return The object to be transferred, or <code>null</code> if no data can be transferred.
	 */
	public Transferable<?> exportTransfer();

	/**
	 * Processes an event from the platform. This method delegates to the currently installed depictor.
	 * @param event The event to be processed.
	 * @throws IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	 * @see #getDepictor()
	 * @see Depictor#processEvent(PlatformEvent)
	 */
	public void processEvent(final PlatformEvent event);

	/**
	 * Updates the depiction of the object. The depiction will be marked as updated. This method delegates to the currently installed depictor.
	 * @throws IOException if there is an error updating the depiction.
	 * @see #getDepictor()
	 * @see Depictor#depict()
	 */
	public void depict() throws IOException;

}
