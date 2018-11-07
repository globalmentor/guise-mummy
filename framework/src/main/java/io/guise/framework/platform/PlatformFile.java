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

import java.net.URI;

import io.guise.framework.event.ProgressListenable;

/**
 * A local file on a platform.
 * @author Garret Wilson
 */
public interface PlatformFile extends ProgressListenable<Long> {

	/** @return The name of the file. */
	public String getName();

	/** @return The size of the file, or -1 if the size is unknown. */
	public long getSize();

	/** Cancels the current upload or download. */
	public void cancel();

	/**
	 * Uploads the file from the platform.
	 * @param destinationURI The URI representing the destination of the platform file, either absolute or relative to the application.
	 * @throws NullPointerException if the given destination URI is <code>null</code>.
	 * @throws IllegalStateException the platform file can no longer be uploaded because, for example, other platform files have since been selected.
	 */
	public void upload(final URI destinationURI);

}
