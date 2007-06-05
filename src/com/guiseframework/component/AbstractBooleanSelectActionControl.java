package com.guiseframework.component;

import java.beans.PropertyVetoException;
import java.net.URI;

import com.garretwilson.beans.AbstractGenericPropertyChangeListener;
import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.model.*;
import static com.guiseframework.theme.Theme.*;

import com.guiseframework.theme.Theme;
import com.guiseframework.validator.*;

/**Selectable action control that stores a Boolean value in its model representing the selected state.
The selected property and the Boolean value will be kept synchronized.
When the value and/or changes, separate property change events for both {@link ValueModel#VALUE_PROPERTY} and for {@link Selectable#SELECTED_PROPERTY} will be fired.
A validator requiring a non-<code>null</code> value is automatically installed.
<p>The selected and unselected icons are set by default to {@link Theme#GLYPH_SELECTED} and {@link Theme#GLYPH_UNSELECTED}, respectively.</p>
@author Garret Wilson
*/
public abstract class AbstractBooleanSelectActionControl<C extends SelectActionControl<C> & ActionValueControl<Boolean, C> & LabelDisplayableComponent<C>> extends AbstractActionValueControl<Boolean, C> implements SelectActionControl<C>
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
		catch(final PropertyVetoException propertyVetoException)	//if there is a validation error
		{
//TODO decide what to do here; throwing an assertion error is not a good idea, because a validator could be installed and a property veto exception would be a valid result			throw new AssertionError(validationException);	//TODO improve
		}
	}

	/**The selected icon URI, which may be a resource URI, or <code>null</code> if there is no selected icon URI.*/
	private URI selectedIcon=GLYPH_SELECTED;

		/**@return The selected icon URI, which may be a resource URI, or <code>null</code> if there is no selected icon URI.*/
		public URI getSelectedIcon() {return selectedIcon;}

		/**Sets the URI of the selected icon.
		This is a bound property of type <code>URI</code>.
		@param newSelectedIcon The new URI of the selected icon, which may be a resource URI.
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
	private URI unselectedIcon=GLYPH_UNSELECTED;

		/**@return The unselected icon URI, which may be a resource URI, or <code>null</code> if there is no unselected icon URI.*/
		public URI getUnselectedIcon() {return unselectedIcon;}

		/**Sets the URI of the unselected icon.
		This is a bound property of type <code>URI</code>.
		@param newUnselectedIcon The new URI of the unselected icon, which may be a resource URI.
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

	/**Label model, action model, value model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public AbstractBooleanSelectActionControl(final LabelModel labelModel, final ActionModel actionModel, final ValueModel<Boolean> valueModel, final Enableable enableable)
	{
		super(labelModel, actionModel, valueModel, enableable);	//construct the parent class
		setValidator(new ValueRequiredValidator<Boolean>());	//install a value-required validator
		addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>()	//listen for the value changing
				{
					public void propertyChange(final GenericPropertyChangeEvent<Boolean> propertyChangeEvent)	//if the value changes
					{
						final Boolean oldValue=propertyChangeEvent.getOldValue();	//get the old value
						final Boolean newValue=propertyChangeEvent.getNewValue();	//get the new value
						firePropertyChange(SELECTED_PROPERTY, oldValue!=null ? oldValue : Boolean.FALSE, newValue!=null ? newValue : Boolean.FALSE);	//fire an identical property change event for the "selected" property, except that the selected property doesn't allow null
					}			
				});
		addActionListener(new AbstractSelectActionControl.SelectActionListener(this));	//listen for an action and set the selected state accordingly
	}

}
