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

package com.guiseframework.platform.web;

import com.guiseframework.component.SelectableLabel;
import com.guiseframework.style.Color;

/**
 * Strategy for rendering a label component that is selectable.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebSelectableLabelDepictor<C extends SelectableLabel> extends WebLabelDepictor<C> {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version uses the selected color if available and the component is selected.
	 * </p>
	 * @see SelectableLabel#getSelectedBackgroundColor()
	 */
	@Override
	protected Color getColor() {
		return super.getColor(); //TODO fix
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version uses the selected background color if available and the component is selected.
	 * </p>
	 * @see SelectableLabel#getSelectedBackgroundColor()
	 */
	@Override
	protected Color getBackgroundColor() {
		final C component = getDepictedObject(); //get the component
		if(component.isSelected()) { //if the component is selected
			final Color selectedBackgroundColor = component.getSelectedBackgroundColor(); //get the selected background color
			if(selectedBackgroundColor != null) { //if there is a backgrond color for the selected state
				return selectedBackgroundColor; //return the defined selected background color
			}
		}
		return super.getBackgroundColor(); //return the default background color if the component isn't selected or we couldn't find a selected background color
	}

}
