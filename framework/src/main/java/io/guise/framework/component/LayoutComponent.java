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

package io.guise.framework.component;

import static com.globalmentor.java.Classes.*;

import io.guise.framework.component.layout.*;

/**
 * Composite component that allows for layout of its children.
 * @author Garret Wilson
 */
public interface LayoutComponent extends CompositeComponent {

	/** The bound property of the layout. */
	public static final String LAYOUT_PROPERTY = getPropertyName(LayoutComponent.class, "layout");
	/** The bound property of whether a theme has been applied to this component's layout. */
	public static final String LAYOUT_THEME_APPLIED_PROPERTY = getPropertyName(LayoutComponent.class, "layoutThemeApplied");

	/** @return The layout definition for the container. */
	public Layout<? extends Constraints> getLayout();

	/** @return Whether a theme has been applied to this component's layout. */
	public boolean isLayoutThemeApplied();

	/**
	 * Sets whether a theme has been applied to this component's layout. This is a bound property of type {@link Boolean}.
	 * @param newLayoutThemeApplied <code>true</code> if a theme has been applied to this component's layout, else <code>false</code>.
	 * @see #LAYOUT_THEME_APPLIED_PROPERTY
	 * @see #setThemeApplied(boolean)
	 */
	public void setLayoutThemeApplied(final boolean newLayoutThemeApplied);

}
