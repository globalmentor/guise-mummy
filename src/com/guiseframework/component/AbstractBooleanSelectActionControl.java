package com.guiseframework.component;

import java.net.URI;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**Selectable action control that stores a Boolean value in its model representing the selected state.
The selected property and the Boolean value will be kept synchronized.
When the value and/or changes, separate property change events for both {@link ValueModel#VALUE_PROPERTY} and for {@link Selectable#SELECTED_PROPERTY} will be fired.
A validator requiring a non-<code>null</code> value is automatically installed.
@author Garret Wilson
*/
public abstract class AbstractBooleanSelectActionControl<C extends SelectActionControl<C> & ActionValueControl<Boolean, C>> extends AbstractActionValueControl<Boolean, C> implements SelectActionControl<C>
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

	/**Returns whether the component is selected.
	This implementation returns the value of the value model.
	@return Whether the component is selected.
	*/
	public boolean isSelected() {return Boolean.TRUE.equals(getValue());}

	/**Sets whether the component is selected.
	This is a bound property of type <code>Boolean</code>.
	This implementation delegates to he value model.
	@param newSelected <code>true</code> if the component should be selected, else <code>false</code>.
	@see #SELECTED_PROPERTY
	*/
	public void setSelected(final boolean newSelected)
	{
		try
		{
			setValue(Boolean.valueOf(newSelected));	//update the value model
		}
		catch(final ValidationException validationException)	//if there is a validation error
		{
			throw new AssertionError(validationException);	//TODO improve
		}
	}

	/**The selected icon URI, or <code>null</code> if there is no selected icon URI.*/
	private URI selectedIcon=null;

		/**@return The selected icon URI, or <code>null</code> if there is no selected icon URI.*/
		public URI getSelectedIcon() {return selectedIcon;}

		/**Sets the URI of the selected icon.
		This is a bound property of type <code>URI</code>.
		@param newSelectedIcon The new URI of the selected icon.
		@see #SELECTED_ICON_PROPERTY
		*/
		public void setSelectedIcon(final URI newSelectedIcon)
		{
			if(!ObjectUtilities.equals(selectedIcon, newSelectedIcon))	//if the value is really changing
			{
				final URI oldSelectedIcon=selectedIcon;	//get the old value
				selectedIcon=newSelectedIcon;	//actually change the value
				firePropertyChange(SELECTED_ICON_PROPERTY, oldSelectedIcon, newSelectedIcon);	//indicate that the value changed
			}			
		}

	/**The selected icon URI resource key, or <code>null</code> if there is no selected icon URI resource specified.*/
	private String selectedIconResourceKey=null;

		/**@return The selected icon URI resource key, or <code>null</code> if there is no selected icon URI resource specified.*/
		public String getSelectedIconResourceKey() {return selectedIconResourceKey;}

		/**Sets the key identifying the URI of the selected icon in the resources.
		This is a bound property.
		@param newSelectedIconResourceKey The new selected icon URI resource key.
		@see #SELECTED_ICON_RESOURCE_KEY_PROPERTY
		*/
		public void setSelectedIconResourceKey(final String newSelectedIconResourceKey)
		{
			if(!ObjectUtilities.equals(selectedIconResourceKey, newSelectedIconResourceKey))	//if the value is really changing
			{
				final String oldSelectedIconResourceKey=selectedIconResourceKey;	//get the old value
				selectedIconResourceKey=newSelectedIconResourceKey;	//actually change the value
				firePropertyChange(SELECTED_ICON_RESOURCE_KEY_PROPERTY, oldSelectedIconResourceKey, newSelectedIconResourceKey);	//indicate that the value changed
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

	/**The unselected icon URI, or <code>null</code> if there is no unselected icon URI.*/
	private URI unselectedIcon=null;

		/**@return The unselected icon URI, or <code>null</code> if there is no unselected icon URI.*/
		public URI getUnselectedIcon() {return unselectedIcon;}

		/**Sets the URI of the unselected icon.
		This is a bound property of type <code>URI</code>.
		@param newUnselectedIcon The new URI of the unselected icon.
		@see #UNSELECTED_ICON_PROPERTY
		*/
		public void setUnselectedIcon(final URI newUnselectedIcon)
		{
			if(!ObjectUtilities.equals(unselectedIcon, newUnselectedIcon))	//if the value is really changing
			{
				final URI oldUnselectedIcon=unselectedIcon;	//get the old value
				unselectedIcon=newUnselectedIcon;	//actually change the value
				firePropertyChange(UNSELECTED_ICON_PROPERTY, oldUnselectedIcon, newUnselectedIcon);	//indicate that the value changed
			}			
		}

	/**The unselected icon URI resource key, or <code>null</code> if there is no unselected icon URI resource specified.*/
	private String unselectedIconResourceKey=null;

		/**@return The unselected icon URI resource key, or <code>null</code> if there is no unselected icon URI resource specified.*/
		public String getUnselectedIconResourceKey() {return unselectedIconResourceKey;}

		/**Sets the key identifying the URI of the unselected icon in the resources.
		This is a bound property.
		@param newUnselectedIconResourceKey The new unselected icon URI resource key.
		@see #UNSELECTED_ICON_RESOURCE_KEY_PROPERTY
		*/
		public void setUnselectedIconResourceKey(final String newUnselectedIconResourceKey)
		{
			if(!ObjectUtilities.equals(unselectedIconResourceKey, newUnselectedIconResourceKey))	//if the value is really changing
			{
				final String oldUnselectedIconResourceKey=unselectedIconResourceKey;	//get the old value
				unselectedIconResourceKey=newUnselectedIconResourceKey;	//actually change the value
				firePropertyChange(UNSELECTED_ICON_RESOURCE_KEY_PROPERTY, oldUnselectedIconResourceKey, newUnselectedIconResourceKey);	//indicate that the value changed
			}
		}

	/**Value model constructor.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public AbstractBooleanSelectActionControl(final ValueModel<Boolean> valueModel)
	{
		super(valueModel);	//construct the parent class
		setValidator(new ValueRequiredValidator<Boolean>());	//install a value-required validator
		addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Boolean>()	//listen for the value changing
				{
					public void propertyChange(final GuisePropertyChangeEvent<Boolean> propertyChangeEvent)	//if the value changes
					{
						final Boolean oldValue=propertyChangeEvent.getOldValue();	//get the old value
						final Boolean newValue=propertyChangeEvent.getNewValue();	//get the new value
						firePropertyChange(SELECTED_PROPERTY, oldValue!=null ? oldValue : Boolean.FALSE, newValue!=null ? newValue : Boolean.FALSE);	//fire an identical property change event for the "selected" property, except that the selected property doesn't allow null
					}			
				});
		addActionListener(new AbstractSelectActionControl.SelectActionListener(this));	//listen for an action and set the selected state accordingly
	}

}
