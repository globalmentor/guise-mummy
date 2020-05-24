/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy.mummify.page;

import java.util.*;

/**
 * Encapsulates information about a point of navigation.
 * @author Garret Wilson
 */
public interface NavigationItem {

	/** The property handle of the href indicating the target of navigation. */
	public static final String PROPERTY_HANDLE_HREF = "href";
	/** The property handle of the list of child navigation items. */
	public static final String PROPERTY_HANDLE_NAVIGATION = "navigation";

	/** @return A label for navigation. */
	public String getLabel();

	/** @return The icon identifier string, which will not be present if no icon is identified. */
	public Optional<String> findIconId();

	/**
	 * Returns the link reference, if any.
	 * @apiNote This is a URL reference, which may be a relative reference or the string form of a URL.
	 * @return The hypertext reference to use for the link, which will not be present if there should be no link.
	 */
	public Optional<String> findHref();

	/** @return The navigation subordinate to this navigation item; may be empty. */
	public List<NavigationItem> getNavigation();

}
