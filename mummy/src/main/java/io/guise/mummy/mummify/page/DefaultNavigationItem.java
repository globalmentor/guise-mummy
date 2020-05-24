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

import static java.util.Collections.*;
import static java.util.Objects.*;

import java.util.*;

import javax.annotation.*;

import io.guise.mummy.Artifact;
import io.urf.model.UrfResourceDescription;

/**
 * A default implementation of a point of navigation.
 * @author Garret Wilson
 */
public class DefaultNavigationItem implements NavigationItem {

	private final String label;

	@Override
	public String getLabel() {
		return label;
	}

	@Nullable
	private final String iconId;

	@Override
	public Optional<String> findIconId() {
		return Optional.ofNullable(iconId);
	}

	@Nullable
	private final String href;

	@Override
	public Optional<String> findHref() {
		return Optional.ofNullable(href);
	}

	@Nullable
	private final List<NavigationItem> navigation;

	@Override
	public List<NavigationItem> getNavigation() {
		return navigation;
	}

	/**
	 * Constructor.
	 * @implSpec A defensive copy will be made of the navigation items.
	 * @param label The label for navigation.
	 * @param iconId The icon identifier string, or <code>null</code> if there is no icon identified.
	 * @param href The URI reference for navigation, or <code>null</code> if this navigation item has no target.
	 * @param navigation The navigation subordinate to this navigation item; may be empty.
	 * @return A navigation item.
	 */
	private DefaultNavigationItem(@Nonnull final String label, @Nullable final String iconId, @Nullable final String href,
			@Nonnull final Collection<NavigationItem> navigation) {
		this.label = requireNonNull(label);
		this.iconId = iconId;
		this.href = href;
		this.navigation = List.copyOf(navigation);
	}

	/**
	 * Constructs a navigation item for a link, targeting an artifact, with no sub-navigation.
	 * @implSpec This method delegates to {@link #forArtifactReference(String, Artifact, List)}.
	 * @param href The URI reference for navigation; normally a relative reference.
	 * @param navigationArtifact The artifact target of the navigation.
	 * @return A navigation item from the href and artifact.
	 * @see Artifact#determineLabel()
	 */
	public static DefaultNavigationItem forArtifactReference(@Nonnull final String href, @Nonnull final Artifact navigationArtifact) {
		return forArtifactReference(href, navigationArtifact, emptyList());
	}

	/**
	 * Constructs a navigation item for a link, targeting an artifact, with specified sub-navigation.
	 * @param href The URI reference for navigation; normally a relative reference.
	 * @param navigationArtifact The artifact target of the navigation.
	 * @param navigation The navigation subordinate to this navigation item; may be empty.
	 * @return A navigation item from the href and artifact.
	 * @see Artifact#determineLabel()
	 */
	public static DefaultNavigationItem forArtifactReference(@Nonnull final String href, @Nonnull final Artifact navigationArtifact,
			@Nonnull final List<NavigationItem> navigation) {
		return new DefaultNavigationItem(navigationArtifact.determineLabel(),
				navigationArtifact.getResourceDescription().findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_ICON).map(Object::toString).orElse(null),
				requireNonNull(href), navigation);
	}

	/**
	 * Constructs a navigation item for a link, targeting an artifact. If appropriate information is not available from the given description, it is retrieved
	 * from the artifact's description.
	 * @param href The URI reference for navigation; normally a relative reference.
	 * @param description The description of the navigation item. Any {@value NavigationItem#PROPERTY_HANDLE_HREF} and
	 *          {@value NavigationItem#PROPERTY_HANDLE_NAVIGATION} properties will be ignored.
	 * @param navigationArtifact The artifact target of the navigation.
	 * @param navigation The navigation subordinate to this navigation item; may be empty.
	 * @return A navigation item from the href and artifact.
	 * @see Artifact#PROPERTY_HANDLE_LABEL
	 * @see Artifact#PROPERTY_HANDLE_ICON
	 * @see NavigationItem#PROPERTY_HANDLE_HREF
	 * @see Artifact#getResourceDescription()
	 * @see Artifact#determineLabel()
	 */
	public static DefaultNavigationItem forArtifactReference(@Nonnull final String href, @Nullable final UrfResourceDescription description,
			@Nonnull final Artifact navigationArtifact, @Nonnull final List<NavigationItem> navigation) {
		requireNonNull(navigationArtifact);
		return new DefaultNavigationItem(
				description.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_LABEL).map(Object::toString).orElseGet(navigationArtifact::determineLabel),
				description.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_ICON).map(Object::toString)
						.or(() -> navigationArtifact.getResourceDescription().findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_ICON).map(Object::toString)).orElse(null),
				requireNonNull(href), navigation);
	}

	/**
	 * Constructs a navigation item for a link that is not targeting an artifact.
	 * @param href The URI reference for navigation; normally an absolute URI.
	 * @param description The description of the navigation item. Any {@value NavigationItem#PROPERTY_HANDLE_HREF} and
	 *          {@value NavigationItem#PROPERTY_HANDLE_NAVIGATION} properties will be ignored.
	 * @param navigation The navigation subordinate to this navigation item; may be empty.
	 * @return A navigation item from the href.
	 * @see Artifact#PROPERTY_HANDLE_LABEL
	 * @see Artifact#PROPERTY_HANDLE_ICON
	 * @throws IllegalArgumentException if the description does not indicate a label.
	 */
	public static DefaultNavigationItem forReference(@Nonnull final String href, @Nullable final UrfResourceDescription description,
			@Nonnull final List<NavigationItem> navigation) {
		return new DefaultNavigationItem(
				description.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_LABEL).map(Object::toString)
						.orElseThrow(() -> new IllegalArgumentException("Nav item description missing label.")),
				description.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_ICON).map(Object::toString).orElse(null), requireNonNull(href), navigation);
	}

	/**
	 * Constructs a navigation item from a description, with no reference.
	 * @apiNote If a reference is desired, it should be preprocessed to create the appropriate relative reference, and passed to
	 *          {@link #forArtifactReference(String, UrfResourceDescription, Artifact, List)}.
	 * @param description The description of the navigation item. Any {@value NavigationItem#PROPERTY_HANDLE_HREF} and
	 *          {@value NavigationItem#PROPERTY_HANDLE_NAVIGATION} properties will be ignored.
	 * @param navigation The navigation subordinate to this navigation item; may be empty.
	 * @return A navigation item from the description.
	 * @see Artifact#PROPERTY_HANDLE_LABEL
	 * @see Artifact#PROPERTY_HANDLE_ICON
	 * @throws IllegalArgumentException if the description does not indicate a label.
	 */
	public static DefaultNavigationItem fromDescription(@Nullable final UrfResourceDescription description, @Nonnull final List<NavigationItem> navigation) {
		return new DefaultNavigationItem(
				description.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_LABEL).map(Object::toString)
						.orElseThrow(() -> new IllegalArgumentException("Nav item description missing label.")),
				description.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_ICON).map(Object::toString).orElse(null), null, navigation);
	}

}
