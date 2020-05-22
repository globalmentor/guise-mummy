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

import static java.util.Objects.*;

import java.util.Optional;

import javax.annotation.*;

import io.guise.mummy.Artifact;
import io.urf.model.UrfResourceDescription;

/**
 * A default implementation of a point of navigation.
 * @author Garret Wilson
 */
public class DefaultNavigationItem implements NavigationItem {
	@Nonnull
	private final String label;
	@Nullable
	private final String iconId;
	@Nullable
	private final String href;

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Optional<String> findIconId() {
		return Optional.ofNullable(iconId);
	}

	@Override
	public Optional<String> findHref() {
		return Optional.ofNullable(href);
	}

	/**
	 * Constructor.
	 * @param label The label for navigation.
	 * @param iconId The icon identifier string, or <code>null</code> if there is no icon identified.
	 * @param href The URI reference for navigation, or <code>null</code> if this navigation item has no target.
	 * @return A navigation item.
	 */
	private DefaultNavigationItem(@Nonnull final String label, @Nullable final String iconId, @Nullable final String href) {
		this.label = requireNonNull(label);
		this.iconId = iconId;
		this.href = href;
	}

	/**
	 * Constructs a navigation item for a link, targeting an artifact. Label and other information is retrieved from the artifact's description.
	 * @param href The URI reference for navigation.
	 * @param navigationArtifact The artifact target of the navigation.
	 * @return A navigation item from the href and artifact.
	 * @see Artifact#PROPERTY_HANDLE_LABEL
	 * @see Artifact#PROPERTY_HANDLE_ICON
	 * @see NavigationItem#PROPERTY_HANDLE_HREF
	 * @see Artifact#getResourceDescription()
	 * @see Artifact#determineLabel()
	 */
	public static DefaultNavigationItem forHrefTargetingArtifact(@Nullable final String href, @Nonnull final Artifact navigationArtifact) {
		requireNonNull(navigationArtifact);
		return new DefaultNavigationItem(navigationArtifact.determineLabel(), null, href);
	}

	/**
	 * Constructs a navigation item from a description.
	 * @param description The description of the navigation item.
	 * @return A navigation item from the description.
	 * @see Artifact#PROPERTY_HANDLE_LABEL
	 * @see Artifact#PROPERTY_HANDLE_ICON
	 * @see NavigationItem#PROPERTY_HANDLE_HREF
	 * @throws IllegalArgumentException if the description does not indicate a label.
	 */
	public static DefaultNavigationItem fromDescription(@Nullable final UrfResourceDescription description) {
		return new DefaultNavigationItem(
				description.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_LABEL).map(Object::toString)
						.orElseThrow(() -> new IllegalArgumentException("Nav item description missing label.")),
				description.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_ICON).map(Object::toString).orElse(null),
				description.findPropertyValueByHandle(NavigationItem.PROPERTY_HANDLE_HREF).map(Object::toString).orElse(null));
	}

	/**
	 * Constructs a navigation item from a description, targeting an artifact. If appropriate information is not available from the description, it is retrieved
	 * from the artifact's description.
	 * @param description The description of the navigation item.
	 * @param navigationArtifact The artifact target of the navigation.
	 * @return A navigation item from the description.
	 * @see Artifact#PROPERTY_HANDLE_LABEL
	 * @see Artifact#PROPERTY_HANDLE_ICON
	 * @see NavigationItem#PROPERTY_HANDLE_HREF
	 * @see Artifact#getResourceDescription()
	 * @see Artifact#determineLabel()
	 */
	public static DefaultNavigationItem fromDescriptionTargetingArtifact(@Nullable final UrfResourceDescription description,
			@Nonnull final Artifact navigationArtifact) {
		requireNonNull(navigationArtifact);
		return new DefaultNavigationItem(
				description.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_LABEL).map(Object::toString).orElseGet(navigationArtifact::determineLabel),
				description.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_ICON).map(Object::toString)
						.or(() -> navigationArtifact.getResourceDescription().findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_ICON).map(Object::toString)).orElse(null),
				description.findPropertyValueByHandle(NavigationItem.PROPERTY_HANDLE_HREF).map(Object::toString).orElse(null));
	}

}
