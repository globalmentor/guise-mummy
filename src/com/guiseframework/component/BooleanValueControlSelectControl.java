package com.guiseframework.component;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.beans.AbstractGenericPropertyChangeListener;
import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.garretwilson.beans.GenericPropertyChangeListener;
import com.garretwilson.util.ArrayUtilities;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.IntegerUtilities.*;

import com.guiseframework.GuiseApplication;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.converter.AbstractStringLiteralConverter;
import com.guiseframework.event.ListEvent;
import com.guiseframework.event.ListListener;
import com.guiseframework.event.ListSelectionEvent;
import com.guiseframework.event.ListSelectionListener;
import com.guiseframework.model.*;
import com.guiseframework.theme.Theme;

//TODO del when works public class ComponentListSelectControl<V, C extends LayoutComponent<C> & ListSelectControl<V, C>> extends AbstractListSelectControl<V, C> implements LayoutComponent<C>, ListSelectControl<V, C>

/**A list select control that uses child components to show the list items.
Only {@link ValueControl}s of type {@link Boolean} will be recognized as able to receive user input to select items in the list.
Only a {@link Layout} that supports default constraints can be used.
@param <V> The type of values to select.
@author Garret Wilson
*/
public class BooleanValueControlSelectControl<V> extends AbstractListSelectControl<V, BooleanValueControlSelectControl<V>> implements LayoutComponent<BooleanValueControlSelectControl<V>>, ListSelectControl<V, BooleanValueControlSelectControl<V>>
{

	//TODO make sure newly created components have the correct value set automatically

	/**Whether the value is editable and the control will allow the the user to change the value.*/
	private boolean editable=true;	//TODO fix or del if not needed

		/**@return Whether the value is editable and the control will allow the the user to change the value.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the value is editable and the control will allow the the user to change the value.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the control should allow the user to change the value.
		@see #EDITABLE_PROPERTY
		*/
		public void setEditable(final boolean newEditable)
		{
			if(editable!=newEditable)	//if the value is really changing
			{
				final boolean oldEditable=editable;	//get the old value
				editable=newEditable;	//actually change the value
				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}

	/**The layout definition for the component.*/
	private Layout<?> layout;

		/**@return The layout definition for the component.*/
		public Layout<?> getLayout() {return layout;}

		/**Sets the layout definition for the component.
		This is a bound property.
		The layout is specified as not yet having a theme applied, as the specific theme rules applied to the layout may depend on the layout's owner.
		@param newLayout The new layout definition for the container.
		@exception NullPointerException if the given layout is <code>null</code>.
		@see #LAYOUT_PROPERTY 
		@see #setPropertiesInitialized(boolean) 
		*/
		protected <T extends Constraints> void setLayout(final Layout<T> newLayout)
		{
			if(layout!=checkInstance(newLayout, "Layout cannot be null."))	//if the value is really changing
			{
				final Layout<?> oldLayout=layout;	//get the old value
				oldLayout.setOwner(null);	//tell the old layout it is no longer installed
				layout=newLayout;	//actually change the value
				layout.setOwner(this);	//tell the new layout which container owns it
				for(final Component<?> childComponent:getChildren())	//for each child component
				{
					newLayout.getConstraints(childComponent);	//make sure the constraints of all components are compatible with the layout TODO do we even need to do this? why not wait until later? but this may be OK---perhaps we can assume that if components are installed before the layout, they will be used with this layout and not another
				}
				setLayoutPropertiesInitialized(false);	//indicate that the properties haven't yet been initiaqlized for this layout, as the specific rules applied may depend on the layout's owner
				firePropertyChange(LAYOUT_PROPERTY, oldLayout, newLayout);	//indicate that the value changed
			}			
		}

	/**Whether the properties of this component's layout have been initialized.*/
	private boolean layoutPropertiesInitialized=false;

		/**@return Whether the properties of this component's layout have been initialized.*/
		public boolean isLayoutPropertiesInitialized() {return layoutPropertiesInitialized;}

		/**Sets whether the properties of this component's layout have been initialized.
		This is a bound property of type {@link Boolean}.
		@param newLayoutPropertiesInitialized <code>true</code> if the properties of this component's layout have been initialized, else <code>false</code>.
		@see #LAYOUT_PROPERTIES_INITIALIZED_PROPERTY
		*/
		public void setLayoutPropertiesInitialized(final boolean newLayoutPropertiesInitialized)
		{
			if(layoutPropertiesInitialized!=newLayoutPropertiesInitialized)	//if the value is really changing
			{
				final boolean oldLayoutPropertiesInitialized=layoutPropertiesInitialized;	//get the current value
				layoutPropertiesInitialized=newLayoutPropertiesInitialized;	//update the value
				firePropertyChange(LAYOUT_PROPERTIES_INITIALIZED_PROPERTY, Boolean.valueOf(oldLayoutPropertiesInitialized), Boolean.valueOf(newLayoutPropertiesInitialized));
			}
		}

	/**Returns an iterable to child components.
	This version returns the children in the same order as the list values.
	@return An iterable to child components.
	*/
	public Iterable<Component<?>> getChildren()
	{
		final List<Component<?>> children=new ArrayList<Component<?>>(size());	//create a list big enough to hold components for all values; the size could change before we get the iterator, so don't create a fixed array just in case
		for(final V value:this)	//for each value
		{
			children.add(determineComponentState(value).getComponent());	//determine the component state for this value and add it to the list of children
		}
		return children;	//return the children we found
	}

	/**The value policy model group for boolean value models, or <code>null</code> if there is no value policy model group in use.*/
	private final ValuePolicyModelGroup<Boolean> valuePolicyModelGroup;

		/**@return The value policy model group for boolean value models, or <code>null</code> if there is no value policy model group in use.*/
		protected ValuePolicyModelGroup<Boolean> getValuePolicyModelGroup() {return valuePolicyModelGroup;}

	/**The listener that detects changes to a child component's {@link Boolean} value and updates the control's selected values accordingly.*/
	protected final GenericPropertyChangeListener<Boolean> componentValueChangeListener=new AbstractGenericPropertyChangeListener<Boolean>()
			{
				public void propertyChange(final GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent)	//if the property of this control changes, update all the values rather than trying to keep track with them; this brute-force method is simplest and ensures everything stays in-synch
				{
					updateListSelectedValues();	//update the list selected values based upon the new child control values
				}
			};

	/**Value class constructor with a default data model to represent a given type with multiple selection and a {@link FlowLayout} with {@link Flow#PAGE} flow.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final Class<V> valueClass)
	{
		this(new DefaultListSelectModel<V>(valueClass));	//construct the class with a default model
	}

	/**Value class and value representation strategy constructor with a default data model to represent a given type with multiple selection and a {@link FlowLayout} with {@link Flow#PAGE} flow.
	@param valueClass The class indicating the type of value held in the model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@exception NullPointerException if the given value class and/or value representation strategy is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final Class<V> valueClass, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		this(new DefaultListSelectModel<V>(valueClass), valueRepresentationStrategy);	//construct the class with a default model
	}

	/**Value class and selection strategy constructor with a default data model to represent a given type and a {@link FlowLayout} with {@link Flow#PAGE} flow.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@exception NullPointerException if the given value class and/or selection strategy is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy)
	{
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy));	//construct the class with a default model
	}
	
	/**Value class, selection strategy, and value representation strategy constructor with a default data model to represent a given type and a {@link FlowLayout} with {@link Flow#PAGE} flow.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@exception NullPointerException if the given value class, selection strategy, and/or value representation strategy is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy), valueRepresentationStrategy);	//construct the class with a default model
	}	

	/**List select model constructor with a {@link FlowLayout} with {@link Flow#PAGE} flow.
	@param listSelectModel The component list select model.
	@exception NullPointerException if the given list select model is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final ListSelectModel<V> listSelectModel)
	{
		this(listSelectModel, new DefaultValueRepresentationStrategy<V>(AbstractStringLiteralConverter.getInstance(listSelectModel.getValueClass())));	//construct the class with a default representation strategy
	}

	/**List select model and value representation strategy constructor with a {@link FlowLayout} with {@link Flow#PAGE} flow.
	@param listSelectModel The component list select model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@exception NullPointerException if the given list select model and/or value representation strategy is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final ListSelectModel<V> listSelectModel, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		this(listSelectModel, new FlowLayout(Flow.PAGE), valueRepresentationStrategy);	//construct the control with page flow layout
	}
	
	/**Value class and layout constructor with a default data model to represent a given type with multiple selection.
	@param valueClass The class indicating the type of value held in the model.
	@param layout The layout definition for the component.
	@exception NullPointerException if the given value class and/or layout is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final Class<V> valueClass, final Layout<?> layout)
	{
		this(new DefaultListSelectModel<V>(valueClass));	//construct the class with a default model
	}

	/**Value class, layout, and value representation strategy constructor with a default data model to represent a given type with multiple selection.
	@param valueClass The class indicating the type of value held in the model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param layout The layout definition for the component.
	@exception NullPointerException if the given value class, layout, and/or value representation strategy is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final Class<V> valueClass, final Layout<?> layout, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		this(new DefaultListSelectModel<V>(valueClass), valueRepresentationStrategy);	//construct the class with a default model
	}

	/**Value class, selection strategy, and layout constructor with a default data model to represent a given type.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param layout The layout definition for the component.
	@exception NullPointerException if the given value class, selection strategy, and/or layout is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Layout<?> layout)
	{
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy));	//construct the class with a default model
	}

	/**Value class, selection strategy, layout, and value representation strategy constructor with a default data model to represent a given type.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param layout The layout definition for the component.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@exception NullPointerException if the given value class, selection strategy, layout, and/or value representation strategy is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Layout<?> layout, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy), valueRepresentationStrategy);	//construct the class with a default model
	}
			
	/**List select model and layout constructor.
	@param listSelectModel The component list select model.
	@param layout The layout definition for the component.
	@exception NullPointerException if the given list select model and/or layout is <code>null</code>.
	*/
	public BooleanValueControlSelectControl(final ListSelectModel<V> listSelectModel, final Layout<?> layout)
	{
		this(listSelectModel, new DefaultValueRepresentationStrategy<V>(AbstractStringLiteralConverter.getInstance(listSelectModel.getValueClass())));	//construct the class with a default representation strategy
	}

	/**List select model, layout, and value representation strategy constructor.
	@param listSelectModel The component list select model.
	@param layout The layout definition for the component.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@exception NullPointerException if the given list select model, layout, and/or value representation strategy is <code>null</code>.
	*/
	@SuppressWarnings("unchecked")	//an empty varargs doesn't need a generics cast, but Java requires one anyway
	public BooleanValueControlSelectControl(final ListSelectModel<V> listSelectModel, final Layout<?> layout, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		super(listSelectModel, valueRepresentationStrategy);	//construct the parent class
		this.layout=checkInstance(layout, "Layout cannot be null.");	//save the layout
		layout.setOwner(this);	//tell the layout which composite component owns it
		valuePolicyModelGroup=getSelectionPolicy() instanceof SingleListSelectionPolicy ? new MutualExclusionPolicyModelGroup() : null;	//if we're selecting only one item at a time, use a mutual exclusion policy group model
		addListSelectionListener(new ListSelectionListener<V>()	//if the list values change, update the components
				{
					public void listSelectionChanged(final ListSelectionEvent<V> selectionEvent)	//if the list selection changes
					{
						updateChildControlValues();	//update the values of the child controls
					}		
				});
	}

	/**Adds a component to the layout component with default constraints.
	@param component The component to add.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException if the installed layout does not support default constraints.
	*/
	protected boolean addComponent(final Component<?> component)
	{
		if(super.addComponent(component))	//add the component normally; if the child components changed
		{
			getLayout().addComponent(component);	//add the component to the layout
			if(component instanceof ValueControl && ((ValueControl<?, ?>)component).getValueClass().equals(Boolean.class))	//if the component is a Boolean value control
			{
				final ValueControl<Boolean, ?> booleanValueControl=(ValueControl<Boolean, ?>)component;	//get the component as a boolean value control
				final ValuePolicyModelGroup<Boolean> valuePolicyModelGroup=getValuePolicyModelGroup();	//get the value policy model group, if any
				if(valuePolicyModelGroup!=null)	//if there is a policy group
				{
					valuePolicyModelGroup.add(booleanValueControl);	//add the component to the group
				}
				booleanValueControl.addPropertyChangeListener(ValueControl.VALUE_PROPERTY, componentValueChangeListener);	//listen for the child component's value changing and update the selected values accordingly
			}
			return true;	//indicate that the child components changed
		}
		else	//if the component list did not change
		{
			return false;	//indicate that the child components did not change
		}
	}

	/**Removes a component from the layout component.
	@param component The component to remove.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component is not a member of this composite component.
	*/
	protected boolean removeComponent(final Component<?> component)
	{
		if(component instanceof ValueControl && ((ValueControl<?, ?>)component).getValueClass().equals(Boolean.class))	//if the component is a Boolean value control
		{
			final ValueControl<Boolean, ?> booleanValueControl=(ValueControl<Boolean, ?>)component;	//get the component as a boolean value control
			final ValuePolicyModelGroup<Boolean> valuePolicyModelGroup=getValuePolicyModelGroup();	//get the value policy model group, if any
			if(valuePolicyModelGroup!=null)	//if there is a policy group
			{
				valuePolicyModelGroup.remove(booleanValueControl);	//remove this component from the group
			}
			booleanValueControl.removePropertyChangeListener(ValueControl.VALUE_PROPERTY, componentValueChangeListener);	//stop listening for changes in the child component's value
		}
		getLayout().removeComponent(component);	//remove the component from the layout
		super.removeComponent(component);	//do the default removal
		return true;	//indicate that the child components changed
	}

	/**The atomic flag that allows us to know whether we're synchronizing values, either from the list to the child controls or vice versa.*/
	protected final AtomicBoolean synchronizingValuesFlag=new AtomicBoolean(false);

	/**Updates the list selected values based upon the current values of the child controls.*/
	protected void updateListSelectedValues()
	{
		if(synchronizingValuesFlag.compareAndSet(false, true))	//if we can synchronize values
		{
			try
			{
				final Set<Integer> selectedIndexes=new HashSet<Integer>();	//create a set in which to store selected indexes
				int i=0;	//keep track of the index
				for(final Component<?> component:getChildren())	//look at all the child components
				{
					if(component instanceof ValueControl && ((ValueControl<?, ?>)component).getValueClass().equals(Boolean.class) && Boolean.TRUE.equals(((ValueControl<Boolean, ?>)component).getValue()))	//if the component is a Boolean value control set to TRUE
					{
						selectedIndexes.add(new Integer(i));	//add this index to the set of selected indexes
					}
					++i;	//go to the next index
				}
				try
				{
					setSelectedIndexes(toIntArray(selectedIndexes.toArray(new Integer[selectedIndexes.size()])));	//convert the selected indexes to an int array and set the selected indexes
				}
				catch(final PropertyVetoException propertyVetoException)	//ignore any problems setting the new value
				{
				}
			}
			finally	//always clear the synchronizing flag
			{
				synchronizingValuesFlag.set(false);	//indicate that we're finished synchronizing values
			}
		}
	}

	/**Updates the values of child controls based upon the current state of the list selected values.*/
	protected void updateChildControlValues()
	{
		if(synchronizingValuesFlag.compareAndSet(false, true))	//if we can synchronize values
		{
			try
			{
				final int[] selectedIndexes=getSelectedIndexes();	//get the selected indexes
				int i=0;	//keep track of the index
				for(final Component<?> component:getChildren())	//look at all the child components
				{
					if(component instanceof ValueControl && ((ValueControl<?, ?>)component).getValueClass().equals(Boolean.class))	//if the component is a Boolean value control set to TRUE
					{
						try
						{
							((ValueControl<Boolean, ?>)component).setValue(Boolean.valueOf(ArrayUtilities.contains(selectedIndexes, i)));	//select or unselect this control, based upon whether this index is selected
						}
						catch(final PropertyVetoException propertyVetoException)	//we must ignore any problems setting the new value, because transitioning between boolean controls may result in a temporary state with no controls selected or with two controls selected, temporarily violating a validator, and there's no way to know if the change is transitory
						{
						}
					}
					++i;	//go to the next index
				}
			}
			finally	//always clear the synchronizing flag
			{
				synchronizingValuesFlag.set(false);	//indicate that we're finished synchronizing values
			}
		}
	}

	/**Update's this object's properties.
	This method checks whether properties have been updated for this object.
	If this object's properties have not been updated, this method calls {@link #initializeProperties()}.
	This method is called for any child components before initializing the properties of the component itself,
	to assure that child property updates have already occured before property updates occur for this component.
	There is normally no need to override this method or to call this method directly by applications.
	This version checks to see if the theme needs to be applied to the given layout.
	@exception IOException if there was an error loading or setting properties.
	@see #isPropertiesInitialized()
	@see #isLayoutPropertiesInitialized()
	@see #initializeProperties()
	*/
	public void updateProperties() throws IOException
	{
		super.updateProperties();	//update the properties normally
		if(!isLayoutPropertiesInitialized())	//if the layout properties haven't yet been initialized (which also means that our version of initializeProperties() hasn't been called, or it would have updated the layout theme applied status) 
		{
			initializeProperties();	//initialize this component's properties
		}		
	}

	/**Initializes the properties of this component.
	This includes loading and applying the current theme as well as loading any preferences.
	Themes are only applied of the application is themed.
	This method may be overridden to effectively override theme settings and preference loading by ensuring the state of important properties after the default operations have occurred. 
	If properties are successfully updated, this method updates the properties initialized status.
	This version initializes the properties of the current layout and updates the layout properties initialized status.
	@exception IOException if there was an error loading or setting properties.
	@see GuiseApplication#isThemed()
	@see #applyTheme(Theme)
	@see #setPropertiesInitialized(boolean)
	@see #setLayoutPropertiesInitialized(boolean)
	*/
	public void initializeProperties() throws IOException
	{
		super.initializeProperties();	//apply properties to this component normally
		setLayoutPropertiesInitialized(true);	//indicate that we've applied the theme to the layout as well
	}

	/**Applies a theme and its parents to this component.
	The theme's rules will be applied to this component and any related objects.
	Theme application occurs unconditionally, regardless of whether themes have been applied to this component before.
	This method may be overridden to effectively override theme settings by ensuring state of important properties after theme application. 
	There is normally no need to call this method directly by applications.
	This version applies the theme to the current layout.
	@param theme The theme to apply to the component.
	*/
	public void applyTheme(final Theme theme)
	{
		super.applyTheme(theme);	//apply the theme to this component normally
		theme.apply(getLayout());	//apply the theme to the currently installed layout
	}

}
