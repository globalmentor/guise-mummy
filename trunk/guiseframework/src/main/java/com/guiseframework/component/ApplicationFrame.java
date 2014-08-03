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

/**
 * The root frame of an application.
 * @author Garret Wilson
 */
public interface ApplicationFrame extends Frame {

	/** @return An iterable to all child frames. */
	public Iterable<Frame> getChildFrames();

	/**
	 * Adds a frame to the list of child frames. This method should usually only be called by the frames themselves.
	 * @param frame The frame to add.
	 * @throws NullPointerException if the given frame is <code>null</code>.
	 * @throws IllegalArgumentException if the given frame is this frame.
	 */
	public void addChildFrame(final Frame frame);

	/**
	 * Removes a frame from the list of child frames. This method should usually only be called by the frames themselves.
	 * @param frame The frame to remove.
	 * @throws NullPointerException if the given frame is <code>null</code>.
	 * @throws IllegalArgumentException if the given frame is this frame.
	 */
	public void removeChildFrame(final Frame frame);

}
