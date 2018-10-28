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

package io.guise.framework.platform;

import static java.util.Objects.*;

import com.globalmentor.event.EventListenerManager;
import com.globalmentor.model.TaskState;

import io.guise.framework.event.*;

/**
 * An abstract implementation of a local file on a platform.
 * @author Garret Wilson
 */
public abstract class AbstractPlatformFile implements PlatformFile {

	/** The object managing event listeners. */
	private final EventListenerManager eventListenerManager = new EventListenerManager();

	/** @return The object managing event listeners. */
	protected EventListenerManager getEventListenerManager() {
		return eventListenerManager;
	}

	/** The name of the file. */
	private final String name;

	@Override
	public String getName() {
		return name;
	}

	/** The size of the file, or -1 if the size is unknown. */
	private final long size;

	@Override
	public long getSize() {
		return size;
	}

	/**
	 * Name and size constructor.
	 * @param name The name of the file.
	 * @param size The size of the file, or -1 if the size is unknown.
	 * @throws NullPointerException if the given name is <code>null</code>.
	 */
	public AbstractPlatformFile(final String name, final long size) {
		this.name = requireNonNull(name, "Name cannot be null.");
		this.size = size;
	}

	@Override
	public void addProgressListener(final ProgressListener<Long> progressListener) {
		getEventListenerManager().add(ProgressListener.class, progressListener); //add the listener
	}

	@Override
	public void removeProgressListener(final ProgressListener<Long> progressListener) {
		getEventListenerManager().remove(ProgressListener.class, progressListener); //remove the listener
	}

	/**
	 * Fires a progress event to all registered progress listeners. This method delegates to {@link #fireProgressed(ProgressEvent)}.
	 * @param state The state of the progress.
	 * @param transferred The current number of bytes transferred, or <code>-1</code> if not known.
	 * @param total The total or estimated total bytes to transfer, or <code>-1</code> if not known.
	 * @throws NullPointerException if the given state is <code>null</code>.
	 * @see ProgressListener
	 * @see ProgressEvent
	 */
	protected void fireProgressed(final TaskState state, final long transferred, final long total) {
		final EventListenerManager eventListenerManager = getEventListenerManager(); //get event listener support
		if(eventListenerManager.hasListeners(ProgressListener.class)) { //if there are progress listeners registered
			fireProgressed(new ProgressEvent<Long>(this, null, state, transferred >= 0 ? Long.valueOf(transferred) : null, total >= 0 ? Long.valueOf(total) : null)); //create and fire a new progress event
		}
	}

	/**
	 * Fires a given progress event to all registered progress listeners.
	 * @param progressEvent The progress event to fire.
	 */
	protected void fireProgressed(final ProgressEvent<Long> progressEvent) {
		for(final ProgressListener<Long> progressListener : getEventListenerManager().getListeners(ProgressListener.class)) { //for each progress listener
			progressListener.progressed(progressEvent); //dispatch the progress event to the listener
		}
	}

	@Override
	public String toString() {
		return getName(); //return the name of the file as a representation
	}
}
