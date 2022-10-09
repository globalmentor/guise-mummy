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

import io.guise.framework.model.*;
import io.guise.framework.style.Color;
import io.guise.framework.theme.Theme;

import static com.globalmentor.java.Classes.*;
import static io.guise.framework.theme.Theme.*;

import java.util.Objects;

/**
 * A label component that is able to indicate a selected state. This component has no facility for interacting with the user.
 * @author Garret Wilson
 */
public class SelectableLabel extends Label implements Selectable {

	/** The bound property of the selected background color. */
	public static final String SELECTED_BACKGROUND_COLOR_PROPERTY = getPropertyName(SelectableLabel.class, "selectedBackgroundColor");

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

	/** The selected background color of the component, or <code>null</code> if no selected background color is specified for this component. */
	private Color selectedBackgroundColor = COLOR_SELECTED_BACKGROUND;

	/**
	 * Returns the selected background color of the component. The default value is {@link Theme#COLOR_SELECTED_BACKGROUND}.
	 * @return The selected background color of the component, or <code>null</code> if no selected background color is specified for this component.
	 */
	public Color getSelectedBackgroundColor() {
		return selectedBackgroundColor;
	}

	/**
	 * Sets the selected background color of the component. This is a bound property.
	 * @param newSelectedBackgroundColor The selected background color of the component, or <code>null</code> if the default selected background color should be
	 *          used.
	 * @see #SELECTED_BACKGROUND_COLOR_PROPERTY
	 */
	public void setSelectedBackgroundColor(final Color newSelectedBackgroundColor) {
		if(!Objects.equals(selectedBackgroundColor, newSelectedBackgroundColor)) { //if the value is really changing
			final Color oldSelectedBackgroundColor = selectedBackgroundColor; //get the old value
			selectedBackgroundColor = newSelectedBackgroundColor; //actually change the value
			firePropertyChange(SELECTED_BACKGROUND_COLOR_PROPERTY, oldSelectedBackgroundColor, newSelectedBackgroundColor); //indicate that the value changed
		}
	}

	/** Default constructor with a default info model. */
	public SelectableLabel() {
		this(new DefaultInfoModel()); //construct the class with a default info model
	}

	/**
	 * Info model constructor.
	 * @param infoModel The component info model.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public SelectableLabel(final InfoModel infoModel) {
		super(infoModel); //construct the parent class
	}
}
