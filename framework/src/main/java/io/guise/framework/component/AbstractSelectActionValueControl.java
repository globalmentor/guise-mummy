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

package io.guise.framework.component;

import java.net.URI;

import com.globalmentor.java.Objects;

import io.guise.framework.model.*;

/**
 * Selectable action value control for which the selected state is distinct from the contained value.
 * @param <V> The type of value the control represents.
 * @author Garret Wilson
 */
public abstract class AbstractSelectActionValueControl<V> extends AbstractActionValueControl<V> implements SelectActionControl {

	/** Whether this control automatically sets or toggles the selection state when the action occurs. */
	private boolean autoSelect = true;

	@Override
	public boolean isAutoSelect() {
		return autoSelect;
	}

	@Override
	public void setAutoSelect(final boolean newAutoSelect) {
		if(autoSelect != newAutoSelect) { //if the value is really changing
			final boolean oldAutoSelect = autoSelect; //get the current value
			autoSelect = newAutoSelect; //update the value
			firePropertyChange(AUTO_SELECT_PROPERTY, Boolean.valueOf(oldAutoSelect), Boolean.valueOf(newAutoSelect));
		}
	}

	/** Whether the component is selected. */
	private boolean selected = false;

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(final boolean newSelected) {
		if(selected != newSelected) { //if the value is really changing
			final boolean oldSelected = selected; //get the current value
			selected = newSelected; //update the value
			firePropertyChange(SELECTED_PROPERTY, Boolean.valueOf(oldSelected), Boolean.valueOf(newSelected));
		}
	}

	/** The selected icon URI, which may be a resource URI, or <code>null</code> if there is no selected icon URI. */
	private URI selectedIcon = null;

	@Override
	public URI getSelectedGlyphURI() {
		return selectedIcon;
	}

	@Override
	public void setSelectedGlyphURI(final URI newSelectedIcon) {
		if(!Objects.equals(selectedIcon, newSelectedIcon)) { //if the value is really changing
			final URI oldSelectedIcon = selectedIcon; //get the old value
			selectedIcon = newSelectedIcon; //actually change the value
			firePropertyChange(SELECTED_GLYPH_URI_PROPERTY, oldSelectedIcon, newSelectedIcon); //indicate that the value changed
		}
	}

	/**
	 * Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to
	 * <code>true</code>.
	 */
	private boolean toggle = false;

	@Override
	public boolean isToggle() {
		return toggle;
	}

	@Override
	public void setToggle(final boolean newToggle) {
		if(toggle != newToggle) { //if the value is really changing
			final boolean oldToggle = toggle; //get the current value
			toggle = newToggle; //update the value
			firePropertyChange(TOGGLE_PROPERTY, Boolean.valueOf(oldToggle), Boolean.valueOf(newToggle));
		}
	}

	/** The unselected icon URI, which may be a resource URI, or <code>null</code> if there is no unselected icon URI. */
	private URI unselectedIcon = null;

	@Override
	public URI getUnselectedGlyphURI() {
		return unselectedIcon;
	}

	@Override
	public void setUnselectedGlyphURI(final URI newUnselectedIcon) {
		if(!Objects.equals(unselectedIcon, newUnselectedIcon)) { //if the value is really changing
			final URI oldUnselectedIcon = unselectedIcon; //get the old value
			unselectedIcon = newUnselectedIcon; //actually change the value
			firePropertyChange(UNSELECTED_GLYPH_URI_PROPERTY, oldUnselectedIcon, newUnselectedIcon); //indicate that the value changed
		}
	}

	/**
	 * Info model, action model, value model, and enableable object constructor.
	 * @param infoModel The component info model.
	 * @param actionModel The component action model.
	 * @param valueModel The component value model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @throws NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	 */
	public AbstractSelectActionValueControl(final InfoModel infoModel, final ActionModel actionModel, final ValueModel<V> valueModel, final Enableable enableable) {
		super(infoModel, actionModel, valueModel, enableable); //construct the parent class
		addActionListener(new AbstractSelectActionControl.SelectActionListener(this)); //listen for an action and set the selected state accordingly
	}

}
