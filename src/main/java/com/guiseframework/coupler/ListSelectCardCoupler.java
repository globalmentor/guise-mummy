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

package com.guiseframework.coupler;

import static com.globalmentor.java.Classes.*;

import java.beans.*;

import com.globalmentor.beans.*;
import com.globalmentor.java.Objects;

import com.guiseframework.component.*;
import com.guiseframework.model.*;

/**
 * Coupler that associates a {@link ListSelectControl} with a card in a {@link CardControl}.
 * @param <V> The type of values to select. When the specified value is selected, the first displayed and enabled specified card within the card control will be
 *          selected. When any of the the associated cards are selected, the specified value is selected. If the card's constraints implement
 *          {@link Displayable}, the list selected value will be displayed based upon the card constraints' displayed status. If the card's constraints
 *          implement {@link Enableable}, the list selected value will be enabled based upon the card constraints' enabled status. This coupler is only
 *          functional when the given card is contained within a {@link CardControl}.
 * @author Garret Wilson
 */
public class ListSelectCardCoupler<V> extends AbstractCardCoupler {

	/** The bound property of the list select control. */
	public static final String LIST_SELECT_PROPERTY = getPropertyName(ListSelectCardCoupler.class, "listSelect");
	/** The value bound property. */
	public static final String VALUE_PROPERTY = getPropertyName(ListSelectCardCoupler.class, "value");

	/** The private flag to keep track of whether we are reverting the list select control to keep from re-reverting ad infinitum. */
	private boolean isRevertingListSelect = false;

	/** The property change listener to listen for the value property of the list select control changing. */
	private final PropertyChangeListener listSelectValueChangeListener = new AbstractGenericPropertyChangeListener<V>() {

		@Override
		public void propertyChange(final GenericPropertyChangeEvent<V> propertyChangeEvent) { //if the list select value changed
			final V newValue = propertyChangeEvent.getNewValue(); //get the new selected value
			if(newValue != null && Objects.equals(newValue, getValue())) { //if the connected value was selected
				//TODO del				Log.trace("tab changed to", getListSelect().indexOf(newValue), " trying to select new card to match; is reverting list select?", isRevertingListSelect);
				try {
					selectCard(); //select a connected card
				} catch(final PropertyVetoException propertyVetoException) { //if the value can't be selected
					if(isRevertingListSelect) { //if we're reverting a previous change, we've went in a loop; break the loop TODO a better way might be to install a VetoableChangeListener and select the card there, so that if we couldn't select the new card we could veto the tab change rather than reverting to the old card
						throw new AssertionError(
								"Infinite loop detected in list select card coupler; it's likely that one of the cards isn't listed as part of the coupler and the change to a new card was vetoed, as was the reversion back to the non-included card.");
					}
					isRevertingListSelect = true; //show that we're reverting the list select to its old value, so that we can detect infinite loops
					//TODO del Log.trace("card change was vetoed; trying to revert to tab", getListSelect().indexOf(propertyChangeEvent.getOldValue()), "is reverting list select?", isRevertingListSelect);
					try {
						listSelect.setValue(propertyChangeEvent.getOldValue()); //go back to the old selected value, if we can
					} catch(final PropertyVetoException propertyVetoException2) { //if the old value can't be reselected, just ignore the error
					} finally {
						isRevertingListSelect = false; //show that we're finished reverting the list select to its old value							
					}
				}
			}
		}

	};

	/** The list select control to connect to the cards, or <code>null</code> if there is no control coupled with the cards. */
	private ListSelectControl<V> listSelect = null;

	/** @return The list select control to connect to the cards, or <code>null</code> if there is no control coupled with the cards. */
	public ListSelectControl<V> getListSelect() {
		return listSelect;
	}

	/**
	 * Sets the connected list select control. This is a bound property.
	 * @param newListSelect The new list select control to connect to the card, or <code>null</code> if the list select control should not be coupled with the
	 *          cards.
	 * @see #LIST_SELECT_PROPERTY
	 */
	public void setListSelect(final ListSelectControl<V> newListSelect) {
		if(listSelect != newListSelect) { //if the value is really changing
			final ListSelectControl<V> oldListSelect = listSelect; //get the old value
			if(oldListSelect != null) { //if there is an old list select control
				oldListSelect.removePropertyChangeListener(ListSelectControl.VALUE_PROPERTY, listSelectValueChangeListener); //stop listening for list selected value changes
			}
			listSelect = newListSelect; //actually change the value
			if(newListSelect != null) { //if there is a new action
				newListSelect.addPropertyChangeListener(ListSelectControl.VALUE_PROPERTY, listSelectValueChangeListener); //list for list selected value changes
			}
			firePropertyChange(LIST_SELECT_PROPERTY, oldListSelect, newListSelect); //indicate that the value changed
			//TODO replace all this with some sort of update() method in the abstract class
			updateSelected(); //update the control selection based upon the selected card
			updateDisplayed(); //update the displayed status based upon the selected card
			updateEnabled(); //update the enabled status based upon the selected card
			updateTaskState(); //update the task status based upon the selected card
		}
	}

	/** The list select value to indicate selection, or <code>null</code> if there is no value. */
	private V value = null;

	/** @return The list select value to indicate selection, or <code>null</code> if there is no value. */
	public V getValue() {
		return value;
	}

	/**
	 * Sets the list select value to indicate selection. This is a bound property.
	 * @param newValue The list select value to indicate selection, or <code>null</code> if there is no value.
	 * @see #VALUE_PROPERTY
	 */
	public void setValue(final V newValue) {
		if(!Objects.equals(value, newValue)) { //if the value is really changing
			final V oldValue = value; //get the old value
			value = newValue; //actually change the value
			firePropertyChange(VALUE_PROPERTY, oldValue, newValue); //indicate that the value changed
			//TODO replace all this with some sort of update() method in the abstract class
			updateSelected(); //update the control selection based upon the selected card
			updateDisplayed(); //update the displayed status based upon the selected card
			updateEnabled(); //update the enabled status based upon the selected card
			updateTaskState(); //update the task status based upon the selected card
		}
	}

	/** Default constructor. */
	public ListSelectCardCoupler() {
		this(null, null); //construct the class with no list select control, value, or cards
	}

	/**
	 * List select, value, and cards constructor.
	 * @param listSelect The list select control to connect to the cards, or <code>null</code> if there is no control coupled with the cards.
	 * @param value The value in the list to indicate the cards should be selected, or <code>null</code> if there is no value to indicate selection.
	 * @param cards The new cards to connect, if any.
	 */
	public ListSelectCardCoupler(final ListSelectControl<V> listSelect, final V value, final Component... cards) {
		super(cards); //construct the parent class
		setListSelect(listSelect); //set the list select control
		setValue(value); //set the value
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation updates the list select control's displayed status of the connected value. If no list select control or no value is connected, no
	 * action occurs.
	 * </p>
	 */
	@Override
	protected void updateDisplayed(final boolean displayed) {
		final ListSelectControl<V> listSelect = getListSelect(); //get the list select control
		final V value = getValue(); //get the specified value
		if(listSelect != null && value != null) { //if there is a list select control and value specified
			listSelect.setValueDisplayed(value, displayed); //update the displayed status of the list select control for the specified value
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation updates the list select control's enabled status of the connected value. If no list select control or no value is connected, no action
	 * occurs.
	 * </p>
	 */
	@Override
	protected void updateEnabled(final boolean enabled) {
		final ListSelectControl<V> listSelect = getListSelect(); //get the list select control
		final V value = getValue(); //get the specified value
		if(listSelect != null && value != null) { //if there is a list select control and value specified
			listSelect.setValueEnabled(value, enabled); //update the enabled status of the list select control for the specified value
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation selects the connected value in the connected list select control if the new selected state is selected. If no list select control or no
	 * value is connected, no action occurs.
	 * </p>
	 */
	@Override
	protected void updateSelected(final boolean selected) {
		if(selected) { //if one of the connected cards is selected
			final ListSelectControl<V> listSelect = getListSelect(); //get the list select control
			final V value = getValue(); //get the specified value
			if(listSelect != null && value != null) { //if there is a list select control and value specified
				try {
					listSelect.setValue(value); //select the requested value
					//TODO why does this throw a ClassCastException?					listSelect.setSelectedValues(value);	//select the requested value
				} catch(final PropertyVetoException propertyVetoException) { //if the value can't be selected, just ignore the error
				}
			}
		}
	}
}
