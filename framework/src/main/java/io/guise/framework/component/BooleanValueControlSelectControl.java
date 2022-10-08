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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.*;

import static com.globalmentor.java.Integers.*;

import com.globalmentor.beans.*;
import com.globalmentor.java.Arrays;

import io.guise.framework.GuiseApplication;
import io.guise.framework.component.layout.*;
import io.guise.framework.converter.AbstractStringLiteralConverter;
import io.guise.framework.event.*;
import io.guise.framework.model.*;
import io.guise.framework.theme.Theme;

/**
 * A list select control that uses child components to show the list items. Only {@link ValueControl}s of type {@link Boolean} will be recognized as able to
 * receive user input to select items in the list. Only a {@link Layout} that supports default constraints can be used.
 * @param <V> The type of values to select.
 * @author Garret Wilson
 */
public class BooleanValueControlSelectControl<V> extends AbstractListSelectControl<V> implements LayoutComponent, ListSelectControl<V> {

	//TODO make sure newly created components have the correct value set automatically

	/** The layout definition for the component. */
	private Layout<? extends Constraints> layout;

	@Override
	public Layout<? extends Constraints> getLayout() {
		return layout;
	}

	/**
	 * Sets the layout definition for the component. This is a bound property. The layout is marked as not yet having a theme applied, as the specific theme rules
	 * applied to the layout may depend on the layout's owner.
	 * @param <T> The type of the constraint.
	 * @param newLayout The new layout definition for the container.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 * @see #LAYOUT_PROPERTY
	 */
	protected <T extends Constraints> void setLayout(final Layout<T> newLayout) {
		if(layout != requireNonNull(newLayout, "Layout cannot be null.")) { //if the value is really changing
			final Layout<? extends Constraints> oldLayout = layout; //get the old value
			oldLayout.setOwner(null); //tell the old layout it is no longer installed
			layout = newLayout; //actually change the value
			layout.setOwner(this); //tell the new layout which container owns it
			for(final Component childComponent : getChildComponents()) { //for each child component
				newLayout.getConstraints(childComponent); //make sure the constraints of all components are compatible with the layout TODO do we even need to do this? why not wait until later? but this may be OK---perhaps we can assume that if components are installed before the layout, they will be used with this layout and not another
			}
			setLayoutThemeApplied(false); //indicate that a theme haven't yet been set for this layout, as the specific rules applied may depend on the layout's owner
			firePropertyChange(LAYOUT_PROPERTY, oldLayout, newLayout); //indicate that the value changed
		}
	}

	/** Whether a theme has been applied to this component's layout. */
	private boolean layoutThemeApplied = false;

	@Override
	public boolean isLayoutThemeApplied() {
		return layoutThemeApplied;
	}

	@Override
	public void setLayoutThemeApplied(final boolean newLayoutThemeApplied) {
		if(layoutThemeApplied != newLayoutThemeApplied) { //if the value is really changing
			final boolean oldLayoutThemeApplied = layoutThemeApplied; //get the current value
			layoutThemeApplied = newLayoutThemeApplied; //update the value
			firePropertyChange(LAYOUT_THEME_APPLIED_PROPERTY, Boolean.valueOf(oldLayoutThemeApplied), Boolean.valueOf(newLayoutThemeApplied));
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns the children in the same order as the list values.
	 * </p>
	 */
	@Override
	public Iterable<Component> getChildComponents() {
		final List<Component> children = new ArrayList<Component>(size()); //create a list big enough to hold components for all values; the size could change before we get the iterator, so don't create a fixed array just in case
		for(final V value : this) { //for each value
			children.add(determineComponentState(value).getComponent()); //determine the component state for this value and add it to the list of children
		}
		return children; //return the children we found
	}

	/** The value policy model group for boolean value models, or <code>null</code> if there is no value policy model group in use. */
	private final ValuePolicyModelGroup<Boolean> valuePolicyModelGroup;

	/** @return The value policy model group for boolean value models, or <code>null</code> if there is no value policy model group in use. */
	protected ValuePolicyModelGroup<Boolean> getValuePolicyModelGroup() {
		return valuePolicyModelGroup;
	}

	/** The listener that detects changes to a child component's {@link Boolean} value and updates the control's selected values accordingly. */
	protected final GenericPropertyChangeListener<Boolean> componentValueChangeListener = new AbstractGenericPropertyChangeListener<Boolean>() {

		@Override
		public void propertyChange(final GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent) { //if the property of this control changes, update all the values rather than trying to keep track with them; this brute-force method is simplest and ensures everything stays in-synch
			updateListSelectedValues(); //update the list selected values based upon the new child control values
		}

	};

	/**
	 * Value class constructor with a default data model to represent a given type with multiple selection and a {@link FlowLayout} with {@link Flow#PAGE} flow.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final Class<V> valueClass) {
		this(new DefaultListSelectModel<V>(valueClass)); //construct the class with a default model
	}

	/**
	 * Value class and value representation strategy constructor with a default data model to represent a given type with multiple selection and a
	 * {@link FlowLayout} with {@link Flow#PAGE} flow.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	 * @throws NullPointerException if the given value class and/or value representation strategy is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final Class<V> valueClass, final ValueRepresentationStrategy<V> valueRepresentationStrategy) {
		this(new DefaultListSelectModel<V>(valueClass), valueRepresentationStrategy); //construct the class with a default model
	}

	/**
	 * Value class and selection strategy constructor with a default data model to represent a given type and a {@link FlowLayout} with {@link Flow#PAGE} flow.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param selectionStrategy The strategy for selecting values in the model.
	 * @throws NullPointerException if the given value class and/or selection strategy is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy) {
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy)); //construct the class with a default model
	}

	/**
	 * Value class, selection strategy, and value representation strategy constructor with a default data model to represent a given type and a {@link FlowLayout}
	 * with {@link Flow#PAGE} flow.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param selectionStrategy The strategy for selecting values in the model.
	 * @param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	 * @throws NullPointerException if the given value class, selection strategy, and/or value representation strategy is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy,
			final ValueRepresentationStrategy<V> valueRepresentationStrategy) {
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy), valueRepresentationStrategy); //construct the class with a default model
	}

	/**
	 * List select model constructor with a {@link FlowLayout} with {@link Flow#PAGE} flow.
	 * @param listSelectModel The component list select model.
	 * @throws NullPointerException if the given list select model is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final ListSelectModel<V> listSelectModel) {
		this(listSelectModel, new DefaultValueRepresentationStrategy<V>(AbstractStringLiteralConverter.getInstance(listSelectModel.getValueClass()))); //construct the class with a default representation strategy
	}

	/**
	 * List select model and value representation strategy constructor with a {@link FlowLayout} with {@link Flow#PAGE} flow.
	 * @param listSelectModel The component list select model.
	 * @param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	 * @throws NullPointerException if the given list select model and/or value representation strategy is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final ListSelectModel<V> listSelectModel, final ValueRepresentationStrategy<V> valueRepresentationStrategy) {
		this(listSelectModel, new FlowLayout(Flow.PAGE), valueRepresentationStrategy); //construct the control with page flow layout
	}

	/**
	 * Value class and layout constructor with a default data model to represent a given type with multiple selection.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param layout The layout definition for the component.
	 * @throws NullPointerException if the given value class and/or layout is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final Class<V> valueClass, final Layout<?> layout) {
		this(new DefaultListSelectModel<V>(valueClass)); //construct the class with a default model
	}

	/**
	 * Value class, layout, and value representation strategy constructor with a default data model to represent a given type with multiple selection.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	 * @param layout The layout definition for the component.
	 * @throws NullPointerException if the given value class, layout, and/or value representation strategy is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final Class<V> valueClass, final Layout<?> layout, final ValueRepresentationStrategy<V> valueRepresentationStrategy) {
		this(new DefaultListSelectModel<V>(valueClass), valueRepresentationStrategy); //construct the class with a default model
	}

	/**
	 * Value class, selection strategy, and layout constructor with a default data model to represent a given type.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param selectionStrategy The strategy for selecting values in the model.
	 * @param layout The layout definition for the component.
	 * @throws NullPointerException if the given value class, selection strategy, and/or layout is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Layout<?> layout) {
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy)); //construct the class with a default model
	}

	/**
	 * Value class, selection strategy, layout, and value representation strategy constructor with a default data model to represent a given type.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param selectionStrategy The strategy for selecting values in the model.
	 * @param layout The layout definition for the component.
	 * @param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	 * @throws NullPointerException if the given value class, selection strategy, layout, and/or value representation strategy is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Layout<?> layout,
			final ValueRepresentationStrategy<V> valueRepresentationStrategy) {
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy), valueRepresentationStrategy); //construct the class with a default model
	}

	/**
	 * List select model and layout constructor.
	 * @param listSelectModel The component list select model.
	 * @param layout The layout definition for the component.
	 * @throws NullPointerException if the given list select model and/or layout is <code>null</code>.
	 */
	public BooleanValueControlSelectControl(final ListSelectModel<V> listSelectModel, final Layout<?> layout) {
		this(listSelectModel, new DefaultValueRepresentationStrategy<V>(AbstractStringLiteralConverter.getInstance(listSelectModel.getValueClass()))); //construct the class with a default representation strategy
	}

	/**
	 * List select model, layout, and value representation strategy constructor.
	 * @param listSelectModel The component list select model.
	 * @param layout The layout definition for the component.
	 * @param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	 * @throws NullPointerException if the given list select model, layout, and/or value representation strategy is <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	//an empty varargs doesn't need a generics cast, but Java requires one anyway
	public BooleanValueControlSelectControl(final ListSelectModel<V> listSelectModel, final Layout<?> layout,
			final ValueRepresentationStrategy<V> valueRepresentationStrategy) {
		super(listSelectModel, valueRepresentationStrategy); //construct the parent class
		this.layout = requireNonNull(layout, "Layout cannot be null."); //save the layout
		layout.setOwner(this); //tell the layout which composite component owns it
		valuePolicyModelGroup = getSelectionPolicy() instanceof SingleListSelectionPolicy ? new MutualExclusionPolicyModelGroup() : null; //if we're selecting only one item at a time, use a mutual exclusion policy group model
		addListSelectionListener(new ListSelectionListener<V>() { //if the list values change, update the components

			@Override
			public void listSelectionChanged(final ListSelectionEvent<V> selectionEvent) { //if the list selection changes
				updateChildControlValues(); //update the values of the child controls
			}

		});
	}

	@Override
	protected void addComponent(final Component childComponent) {
		super.addComponent(childComponent); //add the component normally
		getLayout().addComponent(childComponent); //add the component to the layout
		if(childComponent instanceof ValueControl && ((ValueControl<?>)childComponent).getValueClass().equals(Boolean.class)) { //if the component is a Boolean value control
			final ValueControl<Boolean> booleanValueControl = (ValueControl<Boolean>)childComponent; //get the component as a boolean value control
			final ValuePolicyModelGroup<Boolean> valuePolicyModelGroup = getValuePolicyModelGroup(); //get the value policy model group, if any
			if(valuePolicyModelGroup != null) { //if there is a policy group
				valuePolicyModelGroup.add(booleanValueControl); //add the component to the group
			}
			booleanValueControl.addPropertyChangeListener(ValueControl.VALUE_PROPERTY, componentValueChangeListener); //listen for the child component's value changing and update the selected values accordingly
		}
	}

	@Override
	protected void removeComponent(final Component childComponent) {
		//TODO check the order of uninitialization; if an error occurs during removal, this could result in an inconsistent state
		if(childComponent instanceof ValueControl && ((ValueControl<?>)childComponent).getValueClass().equals(Boolean.class)) { //if the component is a Boolean value control
			final ValueControl<Boolean> booleanValueControl = (ValueControl<Boolean>)childComponent; //get the component as a boolean value control
			final ValuePolicyModelGroup<Boolean> valuePolicyModelGroup = getValuePolicyModelGroup(); //get the value policy model group, if any
			if(valuePolicyModelGroup != null) { //if there is a policy group
				valuePolicyModelGroup.remove(booleanValueControl); //remove this component from the group
			}
			booleanValueControl.removePropertyChangeListener(ValueControl.VALUE_PROPERTY, componentValueChangeListener); //stop listening for changes in the child component's value
		}
		getLayout().removeComponent(childComponent); //remove the component from the layout
		super.removeComponent(childComponent); //do the default removal
	}

	/** The atomic flag that allows us to know whether we're synchronizing values, either from the list to the child controls or vice versa. */
	protected final AtomicBoolean synchronizingValuesFlag = new AtomicBoolean(false);

	/** Updates the list selected values based upon the current values of the child controls. */
	protected void updateListSelectedValues() {
		if(synchronizingValuesFlag.compareAndSet(false, true)) { //if we can synchronize values
			try {
				final Set<Integer> selectedIndexes = new HashSet<Integer>(); //create a set in which to store selected indexes
				int i = 0; //keep track of the index
				for(final Component component : getChildComponents()) { //look at all the child components
					if(component instanceof ValueControl && ((ValueControl<?>)component).getValueClass().equals(Boolean.class)
							&& Boolean.TRUE.equals(((ValueControl<Boolean>)component).getValue())) { //if the component is a Boolean value control set to TRUE
						selectedIndexes.add(i); //add this index to the set of selected indexes
					}
					++i; //go to the next index
				}
				try {
					setSelectedIndexes(toIntArray(selectedIndexes.toArray(new Integer[selectedIndexes.size()]))); //convert the selected indexes to an int array and set the selected indexes
				} catch(final PropertyVetoException propertyVetoException) { //ignore any problems setting the new value
				}
			} finally //always clear the synchronizing flag
			{
				synchronizingValuesFlag.set(false); //indicate that we're finished synchronizing values
			}
		}
	}

	/** Updates the values of child controls based upon the current state of the list selected values. */
	protected void updateChildControlValues() {
		if(synchronizingValuesFlag.compareAndSet(false, true)) { //if we can synchronize values
			try {
				final int[] selectedIndexes = getSelectedIndexes(); //get the selected indexes
				int i = 0; //keep track of the index
				for(final Component component : getChildComponents()) { //look at all the child components
					if(component instanceof ValueControl && ((ValueControl<?>)component).getValueClass().equals(Boolean.class)) { //if the component is a Boolean value control set to TRUE
						try {
							((ValueControl<Boolean>)component).setValue(Boolean.valueOf(Arrays.contains(selectedIndexes, i))); //select or unselect this control, based upon whether this index is selected
						} catch(final PropertyVetoException propertyVetoException) { //we must ignore any problems setting the new value, because transitioning between boolean controls may result in a temporary state with no controls selected or with two controls selected, temporarily violating a validator, and there's no way to know if the change is transitory
						}
					}
					++i; //go to the next index
				}
			} finally //always clear the synchronizing flag
			{
				synchronizingValuesFlag.set(false); //indicate that we're finished synchronizing values
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version resets the theme of the given layout.
	 * </p>
	 */
	@Override
	public void resetTheme() {
		super.resetTheme(); //reset the theme
		setLayoutThemeApplied(false); //indicate that no theme has been applied to the layout
	}

	/**
	 * {@inheritDoc} <p>This version checks to see if the theme needs to be applied to the given layout.</p>
	 */
	@Override
	public void updateTheme() throws IOException {
		super.updateTheme(); //update the theme
		if(!isLayoutThemeApplied()) { //if the theme haven't yet been applied to the layout (which also means that our version of applyTheme() hasn't been called, or it would have updated the layout theme applied status) 
			applyTheme(); //apply the theme to this component
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version applies the theme to the current layout and updates the layout theme applied status.
	 * </p>
	 */
	@Override
	public void applyTheme() throws IOException {
		super.applyTheme(); //apply the theme to this component normally
		setLayoutThemeApplied(true); //indicate that we've applied the theme to the layout as well
	}

	@Override
	public void applyTheme(final Theme theme) {
		super.applyTheme(theme); //apply the theme to this component normally
		theme.apply(getLayout()); //apply the theme to the currently installed layout
	}

}
