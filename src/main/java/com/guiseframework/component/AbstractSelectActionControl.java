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

import static java.util.Objects.*;

import java.net.URI;

import com.globalmentor.java.Objects;
import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**
 * Abstract selectable action control.
 * @author Garret Wilson
 */
public abstract class AbstractSelectActionControl extends AbstractActionControl implements SelectActionControl {

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
	 * Info model, action model, and enableable object constructor.
	 * @param infoModel The component info model.
	 * @param actionModel The component action model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @throws NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	 */
	public AbstractSelectActionControl(final InfoModel infoModel, final ActionModel actionModel, final Enableable enableable) {
		super(infoModel, actionModel, enableable); //construct the parent class
		addActionListener(new SelectActionListener(this)); //listen for an action and set the selected state accordingly
	}

	/**
	 * An action listener that selects a select action listener if auto-select is turned on, toggling the select status if necessary.
	 * @author Garret Wilson
	 */
	public static class SelectActionListener implements ActionListener {

		/** The control to select. */
		protected final SelectActionControl selectActionControl;

		/**
		 * Select action control constructor.
		 * @param selectActionControl The control to select when the action occurs.
		 * @throws NullPointerException if the given select action control is <code>null</code>.
		 */
		public SelectActionListener(final SelectActionControl selectActionControl) {
			this.selectActionControl = requireNonNull(selectActionControl, "Select action control cannot be null.");
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation auto-selects the select action control if auto-select is turned on, toggling if appropriate.
		 * </p>
		 */
		@Override
		public void actionPerformed(final ActionEvent actionEvent) { //if an action occurs
			if(selectActionControl.isAutoSelect()) { //if we should automatically select the control
				selectActionControl.setSelected(selectActionControl.isToggle() ? !selectActionControl.isSelected() : true); //if we should toggle, switch the selected state; otherwise, just switch to the selected state
			}
		}
	}

}
