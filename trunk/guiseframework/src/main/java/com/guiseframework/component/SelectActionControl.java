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

import static com.globalmentor.java.Classes.*;

import java.net.URI;

import com.guiseframework.model.Selectable;

/**
 * An action control that keeps track of its selected state. If the control is set to be toggled, when the action is initiated the selected state alternates
 * between <code>true</code> and <code>false</code>. Otherwise, an action sets the selected state to <code>true</code>. The control defaults to auto-select
 * mode. If this mode is turned off, no selection or toggling occurs automatically when the action occurs.
 * @author Garret Wilson
 */
public interface SelectActionControl extends ActionControl, Selectable {

	/** The auto-select bound property. */
	public static final String AUTO_SELECT_PROPERTY = getPropertyName(SelectActionControl.class, "autoSelect");
	/** The selected icon bound property. */
	public static final String SELECTED_GLYPH_URI_PROPERTY = getPropertyName(SelectActionControl.class, "selectedGlyphURI");
	/** The toggle bound property. */
	public static final String TOGGLE_PROPERTY = getPropertyName(SelectActionControl.class, "toggle");
	/** The unselected icon bound property. */
	public static final String UNSELECTED_GLYPH_URI_PROPERTY = getPropertyName(SelectActionControl.class, "unselectedGlyphURI");

	/** @return Whether this control automatically sets or toggles the selection state when the action occurs. */
	public boolean isAutoSelect();

	/**
	 * Sets whether this control automatically sets or toggles the selection state when the action occurs. This is a bound property of type <code>Boolean</code>.
	 * @param newAutoSelect <code>true</code> if the control should automatically set or toggle the selection state when an action occurs.
	 * @see #AUTO_SELECT_PROPERTY
	 */
	public void setAutoSelect(final boolean newAutoSelect);

	/** @return The selected icon URI, which may be a resource URI, or <code>null</code> if there is no selected icon URI. */
	public URI getSelectedGlyphURI();

	/**
	 * Sets the URI of the selected icon. This is a bound property of type <code>URI</code>.
	 * @param newSelectedIcon The new URI of the selected icon, which may be a resource URI.
	 * @see #SELECTED_GLYPH_URI_PROPERTY
	 */
	public void setSelectedGlyphURI(final URI newSelectedIcon);

	/**
	 * @return Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the
	 *         value to <code>true</code>.
	 */
	public boolean isToggle();

	/**
	 * Sets whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the
	 * value to <code>true</code>. This is a bound property of type <code>Boolean</code>.
	 * @param newToggle <code>true</code> if the component should act as a toggle, else <code>false</code> if the action should unconditionally set the value to
	 *          <code>true</code>.
	 * @see #TOGGLE_PROPERTY
	 */
	public void setToggle(final boolean newToggle);

	/** @return The unselected icon URI, which may be a resource URI, or <code>null</code> if there is no unselected icon URI. */
	public URI getUnselectedGlyphURI();

	/**
	 * Sets the URI of the unselected icon. This is a bound property of type <code>URI</code>.
	 * @param newUnselectedIcon The new URI of the unselected icon, which may be a resource URI.
	 * @see #UNSELECTED_GLYPH_URI_PROPERTY
	 */
	public void setUnselectedGlyphURI(final URI newUnselectedIcon);

}
