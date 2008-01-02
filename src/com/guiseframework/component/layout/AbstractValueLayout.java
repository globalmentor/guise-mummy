package com.guiseframework.component.layout;

import static java.text.MessageFormat.*;

import java.beans.*;

import com.garretwilson.util.Debug;
import com.globalmentor.java.Objects;
import com.guiseframework.component.Component;
import com.guiseframework.component.Container;
import com.guiseframework.component.LayoutComponent;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

import static com.guiseframework.GuiseResourceConstants.*;
import static com.guiseframework.model.AbstractValueModel.createPropertyVetoException;

/**A layout that manages the selection of child components, only one of which can be selected at a time.
A value layout can only be used with a {@link Container} as its owner.
The layout maintains its own value model that maintains the current selected component.
If a child component implements {@link Activeable} the child component is set as active when selected and set as inactive when the child component is unselected.
@param <T> The type of layout constraints associated with each component.
@author Garret Wilson
*/
public abstract class AbstractValueLayout<T extends Constraints> extends AbstractLayout<T> implements ValueModel<Component>
{

	/**The value model used by this component.*/
	private final ValueModel<Component> valueModel;

		/**@return The value model used by this component.*/
		protected ValueModel<Component> getValueModel() {return valueModel;}

	/**The lazily-created listener of constraint property changes.*/
//TODO del if not needed	private CardConstraintsPropertyChangeListener cardConstraintsPropertyChangeListener=null;

		/**@return The lazily-created listener of card constraint property changes.*/
/*TODO del if not needed
		protected CardConstraintsPropertyChangeListener getConstraintsPropertyChangeListener()
		{
			if(cardConstraintsPropertyChangeListener==null)	//if we haven't yet created a property change listener for constraints
			{
				cardConstraintsPropertyChangeListener=new CardConstraintsPropertyChangeListener();	//create a new constraints property change listener
			}
			return cardConstraintsPropertyChangeListener;	//return the listener of constraints properties
		}
*/
		
	/**The index of the selected component, or -1 if the index is not known and should be recalculated.*/
	private int selectedIndex=-1;

		/**@return The index of the selected component, or -1 if no component is selected.*/
		public int getSelectedIndex()
		{
			if(selectedIndex<0)	//if there is no valid selected index, make sure the index is up-to-date
			{
				final Component selectedComponent=getValue();	//get the selected component
				if(selectedComponent!=null)	//if a component is selected, we'll need to update the selected index
				{
					final int newSelectedIndex=getOwner().indexOf(selectedComponent);	//update the selected index with the index in the container of the selected component
					assert newSelectedIndex>=0 : "Selected component "+selectedComponent+" is not in the container.";
					if(isSettingValue)	//we're setting the value, return the selected index without updating the variable, as a VetoablePropertyListener might be calling this method before the value is actually changed, so we want to leave the selected index uncached until after the value is actually changed
					{
						return newSelectedIndex;	//return the new selected index
					}
					else	//if we're not in the middle of setting the value
					{
						selectedIndex=newSelectedIndex;	//update the cached selected index; we'll return it later
					}
				}
			}
			return selectedIndex;	//return the selected index, which we've verified is up-to-date
		}

		/**Sets the index of the selected component.
		If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
		@param newIndex The index of the selected component, or -1 if no component is selected.
		@exception IllegalStateException if this layout has not yet been installed into a container.
		@exception IndexOutOfBoundsException if the index is out of range.
		@exception PropertyVetoException if the component at the given index is not a valid compoment to select or the change has otherwise been vetoed.
		*/
		public void setSelectedIndex(final int newIndex) throws PropertyVetoException
		{
			final Container container=getOwner();	//get the layout's container
			if(container==null)	//if we haven't been installed into a container
			{
				throw new IllegalStateException("Layout does not have container.");
			}
			final Component component=newIndex>=0 ? container.get(newIndex) : null;	//get the component at the given index, if a valid index was given
			if(newIndex!=getSelectedIndex() && component!=getValue())	//if we're really changing either the selected index or the component
			{
				selectedIndex=-1;	//uncache the selected index (don't actually change it yet---we want to make sure the value model allows the value to be changed)
				setValue(component);		//update the component value, throwing a validation exception if this index can't be selected
			}
		}

	/**Adds a component to the layout.
	Called immediately after a component is added to the associated container.
	This method is called by the associated container, and should not be called directly by application code.
	This version selects a component if none is selected.
	This version updates the new component's active status if the component implements {@link Activeable}.
	@param component The component to add to the layout.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public void addComponent(final Component component)
	{
		super.addComponent(component);	//add the component normally
		if(getValue()==null)	//if there is no component selected
		{
			try
			{
				setSelectedIndex(0);	//select the first component
			}
			catch(final PropertyVetoException propertyVetoException)	//if we can't select the first component, don't do anything
			{
			}
		}
		if(component instanceof Activeable)	//if the component is activable
		{
			((Activeable)component).setActive(getValue()==component);	//if the card is not the selected card, tell it that it is not active
		}
	}

	/**Removes a component from the layout.
	Called immediately before a component is removed from the associated container.
	This method is called by the associated container, and should not be called directly by application code.
	This implementation updates the selected component if necessary.
	@param component The component to remove from the layout.
	*/
	public void removeComponent(final Component component)
	{
		super.removeComponent(component);	//remove the component normally
		if(component==getValue())	//if the selected component was removed
		{
			final Container container=getOwner();	//get our container
			final int selectedIndex=container.indexOf(component);	//find the current index of the component that is being removed
			final int containerSize=container.size();	//find out how many components are in the container
			final int newSelectedComponentIndex;	//we'll determine the new selected index (that is, the index of the new selected component in this current state; it won't be the new selected index after removal)
			if(selectedIndex<containerSize-1)	//if this component wasn't the last component
			{
				newSelectedComponentIndex=selectedIndex+1;	//get the subsequent component
			}
			else	//if this was the last component tha twas removed
			{
				newSelectedComponentIndex=containerSize-2;	//get the second-to last component
			}
			try
			{
				setValue(container.get(newSelectedComponentIndex));		//update the component value, throwing a validation exception if this index can't be selected
			}				
			catch(final PropertyVetoException propertyVetoException)	//if we can't select the next component
			{
				getValueModel().resetValue();	//reset the selected component value TODO is there something better we can do here?
			}
		}
		this.selectedIndex=-1;	//always uncache the selected index, because the index of the selected component might have changed
	}

	/**@return The container that owns this layout, or <code>null</code> if this layout has not been installed into a container.*/
	public Container getOwner() {return (Container)super.getOwner();}

	/**Sets the container that owns this layout
	This method is managed by containers, and normally should not be called by applications.
//TODO del		A layout cannot be given a container if it is already installed in another container. Once a layout is installed in a container, it cannot be uninstalled.
	A layout cannot be given a container if it is already installed in another container.
	A layout cannot be given a container unless that container already recognizes this layout as its layout.
	If a layout is given the same container it already has, no action occurs.
	@param newOwner The new container for this layout.
//TODO del		@exception NullPointerException if the given container is <code>null</code>.
	@exception IllegalStateException if a different container is provided and this layout already has a container.
	@exception ClassCastException if the given layout component is not a {@link Container}.
	@exception IllegalArgumentException if a different container is provided and the given container does not already recognize this layout as its layout.
	*/
	public void setOwner(final LayoutComponent newOwner)
	{
		super.setOwner((Container)newOwner);	//make sure the new owner is a container
	}

	/**Default constructor.*/
	public AbstractValueLayout()
	{
		this.valueModel=new DefaultValueModel<Component>(Component.class);	//create a new value model
		this.valueModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the value model
		this.valueModel.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the value model
	}

	/**@return The default value.*/
	public Component getDefaultValue() {return getValueModel().getDefaultValue();}

	/**@return The input value, or <code>null</code> if there is no input value.*/
	public Component getValue() {return getValueModel().getValue();}

	/**The flat that indicates whether we are in the middle of setting the value.*/
	private boolean isSettingValue=false;
	
	/**Sets the input value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	If a validator is installed, the value will first be validated before the current value is changed.
	Validation always occurs if a validator is installed, even if the value is not changing.
	If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	This version makes sure that the given component is contained in the container, and resets the cached selected index so that it can be recalculated.
	This version updates the active status of the old and new components if the implement {@link Activeable}.
	@param newValue The input value of the model.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	@see #getValidator()
	@see #VALUE_PROPERTY
	*/
	public void setValue(final Component newValue) throws PropertyVetoException
	{
		final Component oldValue=getValue();	//get the old value
		if(!Objects.equals(oldValue, newValue))	//if a new component is given
		{
			final Container container=getOwner();	//get the layout's container
			if(container==null)	//if we haven't been installed into a container
			{
				throw new IllegalStateException("Layout does not have container.");
			}
			if(newValue!=null && !container.contains(newValue))	//if there is a new component that isn't contained in the container
			{
					//create a custom validation exception
				final ValidationException validationException=new ValidationException(format(getSession().dereferenceString(VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE_REFERENCE), newValue.toString()), newValue);
				throw createPropertyVetoException(this, validationException, VALUE_PROPERTY, oldValue, newValue);	//throw a property veto exception representing the validation error
			}
			if(oldValue instanceof Activeable)	//if the old value is activable
			{
				((Activeable)oldValue).setActive(false);	//tell the old card it is no longer active
			}
			isSettingValue=true;	//indicate that we're setting the value
			try
			{
				selectedIndex=-1;	//uncache the selected index
				getValueModel().setValue(newValue);	//set the new value normally
			}
			finally
			{
				isSettingValue=false;	//indicate that we're no longer setting the value
			}
			if(newValue instanceof Activeable)	//if the new value is activable
			{
				((Activeable)newValue).setActive(true);	//tell the new card it is active
			}
		}
	}

	/**Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators.
	No validation occurs.
	This version resets the cached selected index so that it can be recalculated.
	@see #VALUE_PROPERTY
	*/
	public void clearValue()
	{
		selectedIndex=-1;	//uncache the selected index
		getValueModel().clearValue();
	}

	/**Resets the value to a default value, which may be invalid according to any installed validators.
	No validation occurs.
	This version resets the cached selected index so that it can be recalculated.
	@see #VALUE_PROPERTY
	*/
	public void resetValue()
	{
		selectedIndex=-1;	//uncache the selected index
		getValueModel().resetValue();
	}

	/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
	public Validator<Component> getValidator() {return getValueModel().getValidator();}

	/**Sets the validator.
	This is a bound property
	@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	@see #VALIDATOR_PROPERTY
	*/
	public void setValidator(final Validator<Component> newValidator) {getValueModel().setValidator(newValidator);}

	/**Determines whether the value of this model is valid.
	@return Whether the value of this model is valid.
	*/
	public boolean isValidValue() {return getValueModel().isValidValue();}

	/**Validates the value of this model, throwing an exception if the model is not valid.
	@exception ValidationException if the value of this model is not valid.	
	*/
	public void validateValue() throws ValidationException {getValueModel().validateValue();}

	/**@return The class representing the type of value this model can hold.*/
	public Class<Component> getValueClass() {return getValueModel().getValueClass();}

	/**A property change listener that listens for changes in a constraint object's properties and fires a layout constraints property change event in response.
	This version also fires model {@link ValuePropertyChangeEvent}s if appropriate.
	A {@link LayoutConstraintsPropertyChangeEvent} will be fired for each component associated with the constraints for which a property changed
	@author Garret Wilson
	@see ValuePropertyChangeEvent
	*/
/*TODO decide if we need this
	protected class CardConstraintsPropertyChangeListener extends ConstraintsPropertyChangeListener
	{
*/
		/**Refires a constraint property change event for the layout in the form of a {@link LayoutConstraintsPropertyChangeEvent}.
		This version also fires a model {@link ValuePropertyChangeEvent} if appropriate to satisfy the list select model contract for value state changes.
		@param component The component for which a constraint value changed.
		@param constraints The constraints for which a value changed.
		@param propertyName The name of the property being changed.
		@param oldValue The old property value.
		@param newValue The new property value.
		*/
/*TODO decide if we need this
		protected <V> void refirePropertyChange(final Component component, final Constraints constraints, final String propertyName, final V oldValue, final V newValue)
		{
			super.refirePropertyChange(component, constraints, propertyName, oldValue, newValue);	//refire the event normally
			if(Constraints.ENABLED_PROPERTY.equals(propertyName))	//if the enabled constraint changed
			{
				listSelectModel.fireValuePropertyChange(component, propertyName, oldValue, newValue);	//tell the model to fire its own event to satisfy the model's contract
			}			
		}
	}
*/

}
