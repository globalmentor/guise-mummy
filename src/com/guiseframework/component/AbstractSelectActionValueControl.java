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

import java.net.URI;

import com.globalmentor.java.Objects;
import com.guiseframework.model.*;

/**Selectable action value control for which the selected state is distinct from the contained value.
@param <V> The type of value the control represents.
@author Garret Wilson
*/
public abstract class AbstractSelectActionValueControl<V> extends AbstractActionValueControl<V> implements SelectActionControl
{

	/**Whether this control automatically sets or toggles the selection state when the action occurs.*/
	private boolean autoSelect=true;

		/**@return Whether this control automatically sets or toggles the selection state when the action occurs.*/
		public boolean isAutoSelect() {return autoSelect;}

		/**Sets whether this control automatically sets or toggles the selection state when the action occurs.
		This is a bound property of type <code>Boolean</code>.
		@param newAutoSelect <code>true</code> if the control should automatically set or toggle the selection state when an action occurs, or <code>false</code> if no selection occurs automatically.
		@see #AUTO_SELECT_PROPERTY
		*/
		public void setAutoSelect(final boolean newAutoSelect)
		{
			if(autoSelect!=newAutoSelect)	//if the value is really changing
			{
				final boolean oldAutoSelect=autoSelect;	//get the current value
				autoSelect=newAutoSelect;	//update the value
				firePropertyChange(AUTO_SELECT_PROPERTY, Boolean.valueOf(oldAutoSelect), Boolean.valueOf(newAutoSelect));
			}
		}

	/**Whether the component is selected.*/
	private boolean selected=false;

		/**@return Whether the component is selected.*/
		public boolean isSelected() {return selected;}

		/**Sets whether the component is selected.
		This is a bound property of type <code>Boolean</code>.
		@param newSelected <code>true</code> if the component should be selected, else <code>false</code>.
		@see #SELECTED_PROPERTY
		*/
		public void setSelected(final boolean newSelected)
		{
			if(selected!=newSelected)	//if the value is really changing
			{
				final boolean oldSelected=selected;	//get the current value
				selected=newSelected;	//update the value
				firePropertyChange(SELECTED_PROPERTY, Boolean.valueOf(oldSelected), Boolean.valueOf(newSelected));
			}
		}
		
	/**The selected icon URI, which may be a resource URI, or <code>null</code> if there is no selected icon URI.*/
	private URI selectedIcon=null;

		/**@return The selected icon URI, which may be a resource URI, or <code>null</code> if there is no selected icon URI.*/
		public URI getSelectedGlyphURI() {return selectedIcon;}

		/**Sets the URI of the selected icon.
		This is a bound property of type <code>URI</code>.
		@param newSelectedIcon The new URI of the selected icon, which may be a resource URI.
		@see #SELECTED_GLYPH_URI_PROPERTY
		*/
		public void setSelectedGlyphURI(final URI newSelectedIcon)
		{
			if(!Objects.equals(selectedIcon, newSelectedIcon))	//if the value is really changing
			{
				final URI oldSelectedIcon=selectedIcon;	//get the old value
				selectedIcon=newSelectedIcon;	//actually change the value
				firePropertyChange(SELECTED_GLYPH_URI_PROPERTY, oldSelectedIcon, newSelectedIcon);	//indicate that the value changed
			}			
		}

	/**Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.*/
	private boolean toggle=false;

		/**@return Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.*/
		public boolean isToggle() {return toggle;}

		/**Sets whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.
		This is a bound property of type <code>Boolean</code>.
		@param newToggle <code>true</code> if the component should act as a toggle, else <code>false</code> if the action should unconditionally set the value to <code>true</code>.
		@see #TOGGLE_PROPERTY
		*/
		public void setToggle(final boolean newToggle)
		{
			if(toggle!=newToggle)	//if the value is really changing
			{
				final boolean oldToggle=toggle;	//get the current value
				toggle=newToggle;	//update the value
				firePropertyChange(TOGGLE_PROPERTY, Boolean.valueOf(oldToggle), Boolean.valueOf(newToggle));
			}
		}

	/**The unselected icon URI, which may be a resource URI, or <code>null</code> if there is no unselected icon URI.*/
	private URI unselectedIcon=null;

		/**@return The unselected icon URI, which may be a resource URI, or <code>null</code> if there is no unselected icon URI.*/
		public URI getUnselectedGlyphURI() {return unselectedIcon;}

		/**Sets the URI of the unselected icon.
		This is a bound property of type <code>URI</code>.
		@param newUnselectedIcon The new URI of the unselected icon, which may be a resource URI.
		@see #UNSELECTED_GLYPH_URI_PROPERTY
		*/
		public void setUnselectedGlyphURI(final URI newUnselectedIcon)
		{
			if(!Objects.equals(unselectedIcon, newUnselectedIcon))	//if the value is really changing
			{
				final URI oldUnselectedIcon=unselectedIcon;	//get the old value
				unselectedIcon=newUnselectedIcon;	//actually change the value
				firePropertyChange(UNSELECTED_GLYPH_URI_PROPERTY, oldUnselectedIcon, newUnselectedIcon);	//indicate that the value changed
			}			
		}

	/**Label model, action model, value model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public AbstractSelectActionValueControl(final LabelModel labelModel, final ActionModel actionModel, final ValueModel<V> valueModel, final Enableable enableable)
	{
		super(labelModel, actionModel, valueModel, enableable);	//construct the parent class
		addActionListener(new AbstractSelectActionControl.SelectActionListener(this));	//listen for an action and set the selected state accordingly
	}
	
}
