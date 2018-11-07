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

import static io.guise.framework.theme.Theme.*;

import java.beans.PropertyVetoException;
import java.net.URI;

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;
import com.globalmentor.java.Objects;

import io.guise.framework.model.*;
import io.guise.framework.theme.Theme;
import io.guise.framework.validator.*;

/**
 * Selectable action control that stores a Boolean value in its model representing the selected state. The selected property and the Boolean value will be kept
 * synchronized. When the value and/or changes, separate property change events for both {@link ValueModel#VALUE_PROPERTY} and for
 * {@link Selectable#SELECTED_PROPERTY} will be fired. A validator requiring a non-<code>null</code> value is automatically installed.
 * <p>
 * The selected and unselected icons are set by default to {@link Theme#GLYPH_SELECTED} and {@link Theme#GLYPH_UNSELECTED}, respectively.
 * </p>
 * @author Garret Wilson
 */
public abstract class AbstractBooleanSelectActionControl extends AbstractActionValueControl<Boolean> implements SelectActionControl {

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

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns the value of the value model.
	 * </p>
	 */
	@Override
	public boolean isSelected() {
		return Boolean.TRUE.equals(getValue());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to he value model.
	 * </p>
	 */
	@Override
	public void setSelected(final boolean newSelected) {
		try {
			setValue(Boolean.valueOf(newSelected)); //update the value model
		} catch(final PropertyVetoException propertyVetoException) { //if there is a validation error
			//TODO decide what to do here; throwing an assertion error is not a good idea, because a validator could be installed and a property veto exception would be a valid result			throw new AssertionError(validationException);	//TODO improve
		}
	}

	/** The selected icon URI, which may be a resource URI, or <code>null</code> if there is no selected icon URI. */
	private URI selectedGlyphURI = GLYPH_SELECTED;

	@Override
	public URI getSelectedGlyphURI() {
		return selectedGlyphURI;
	}

	@Override
	public void setSelectedGlyphURI(final URI newSelectedIcon) {
		if(!Objects.equals(selectedGlyphURI, newSelectedIcon)) { //if the value is really changing
			final URI oldSelectedGlyphURI = selectedGlyphURI; //get the old value
			selectedGlyphURI = newSelectedIcon; //actually change the value
			firePropertyChange(SELECTED_GLYPH_URI_PROPERTY, oldSelectedGlyphURI, newSelectedIcon); //indicate that the value changed
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
	private URI unselectedGlyphURI = GLYPH_UNSELECTED;

	@Override
	public URI getUnselectedGlyphURI() {
		return unselectedGlyphURI;
	}

	@Override
	public void setUnselectedGlyphURI(final URI newUnselectedIcon) {
		if(!Objects.equals(unselectedGlyphURI, newUnselectedIcon)) { //if the value is really changing
			final URI oldUnselectedGlyphURI = unselectedGlyphURI; //get the old value
			unselectedGlyphURI = newUnselectedIcon; //actually change the value
			firePropertyChange(UNSELECTED_GLYPH_URI_PROPERTY, oldUnselectedGlyphURI, newUnselectedIcon); //indicate that the value changed
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
	public AbstractBooleanSelectActionControl(final InfoModel infoModel, final ActionModel actionModel, final ValueModel<Boolean> valueModel,
			final Enableable enableable) {
		super(infoModel, actionModel, valueModel, enableable); //construct the parent class
		setValidator(new ValueRequiredValidator<Boolean>()); //install a value-required validator
		addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>() { //listen for the value changing

			@Override
			public void propertyChange(final GenericPropertyChangeEvent<Boolean> propertyChangeEvent) { //if the value changes
				final Boolean oldValue = propertyChangeEvent.getOldValue(); //get the old value
				final Boolean newValue = propertyChangeEvent.getNewValue(); //get the new value
				firePropertyChange(SELECTED_PROPERTY, oldValue != null ? oldValue : Boolean.FALSE, newValue != null ? newValue : Boolean.FALSE); //fire an identical property change event for the "selected" property, except that the selected property doesn't allow null
			}
		});
		addActionListener(new AbstractSelectActionControl.SelectActionListener(this)); //listen for an action and set the selected state accordingly
	}

}
