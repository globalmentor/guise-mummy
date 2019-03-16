/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy;

import java.util.Collection;

import com.globalmentor.net.URIPath;

import io.urf.model.UrfResourceDescription;

/**
 * Provides information about a page to be mummified.
 * @author Garret Wilson
 */
public interface Page extends Artifact {

	/** @return A description of the source resource for this page. */
	//TODO implement public UrfResourceDescription getResourceDescription();

	/** @return Whether this page is allowed to have child pages. */
	//TODO fix public boolean isCollection();

	/**
	 * Returns the relative paths to source resources, relative to the current resource.
	 * <p>
	 * For example if the site context root is <code>file:/home/user/site/</code> and this resource has a context path of <code>/foo/</code>, a child resource
	 * with a path <code>file:/home/user/site/foo/bar/example.xhtml</code> would return its relative path as <code>bar/example.xhtml</code>.
	 * </p>
	 * @return
	 */
	//TODO implement public Collection<URIPath> getChildResourcePaths();

}
