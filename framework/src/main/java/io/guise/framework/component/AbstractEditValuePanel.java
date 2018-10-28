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

import static io.guise.framework.component.Components.*;

import java.util.ArrayList;

import io.guise.framework.component.layout.*;
import io.guise.framework.event.*;

/**
 * An abstract panel that edits a value. Changing the editable status will recursively update the editable status of all top-level {@link EditComponent}
 * descendant components. Edit events of all top-level {@link EditComponent} descendant components will be repeated to all edit listeners of this component
 * @param <V> The type of value displayed within the component.
 * @author Garret Wilson
 */
public abstract class AbstractEditValuePanel<V> extends AbstractValuedPanel<V> implements EditComponent {

	/** A lazily-created edit listener to repeat copies of events received, using this component as the source while retaining the original target. */
	private EditListener repeatEditListener = null;

	/** @return An edit listener to repeat copies of events received, using this component as the source while retaining the original target. */
	protected EditListener getRepeatEditListener() { //TODO synchronize
		if(repeatEditListener == null) { //if we have not yet created the repeater listener
			repeatEditListener = new EditListener() { //create a listener to listen for a edits

				@Override
				public void edited(final EditEvent editEvent) { //if editing occurs
					fireEdited(new EditEvent(AbstractEditValuePanel.this, editEvent)); //fire a copy of the event, retaining the original target
				}

			};
		}
		return repeatEditListener; //return the repeater edit listener
	}

	/** Whether the value is editable and the component will allow the the user to change the value. */
	private boolean editable = true;

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public void setEditable(final boolean newEditable) {
		if(editable != newEditable) { //if the value is really changing
			final boolean oldEditable = editable; //get the old value
			editable = newEditable; //actually change the value
			firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable)); //indicate that the value changed
		}
		for(final EditComponent editComponent : Components.getChildComponents(this, EditComponent.class, new ArrayList<EditComponent>(), true, false)) { //set the editable status of all the child components; do this even if our value didn't change, just to make sure all child components have the same editable status
			editComponent.setEditable(newEditable); //update the edit status of the component
		}
	}

	/**
	 * Value class and layout constructor.
	 * @param valueClass The class indicating the type of value displayed within the component.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given value class and/or layout is <code>null</code>.
	 */
	public AbstractEditValuePanel(final Class<V> valueClass, final Layout<? extends Constraints> layout) {
		super(valueClass, layout); //construct the parent class
		addCompositeComponentListener(new CompositeComponentListener() { //listen for components being added or removed anywhere below this component

			@Override
			public void childComponentAdded(final ComponentEvent childComponentEvent) { //if a descendant component is added
				for(Component parent = (CompositeComponent)childComponentEvent.getSource(); parent != AbstractEditValuePanel.this; parent = parent.getParent()) { //go up the parent hierarchy of the added component until we reach this component
					assert parent != null : "Composite component event did not refer to child of this component.";
					if(parent instanceof EditComponent) { //if we find an edit component above the added component that is a child of this component
						return; //ignore non-top-level edit components
					}
				}
				for(final EditComponent addedEditComponent : getComponents(childComponentEvent.getComponent(), EditComponent.class, new ArrayList<EditComponent>(),
						true, false)) { //for all the top-level edit components of the added component
					addedEditComponent.addEditListener(getRepeatEditListener()); //repeat its edit events to our listeners
				}
			}

			@Override
			public void childComponentRemoved(ComponentEvent childComponentEvent) { //if a descendant component is removed
				for(Component parent = (CompositeComponent)childComponentEvent.getSource(); parent != AbstractEditValuePanel.this; parent = parent.getParent()) { //go up the parent hierarchy of the removed component until we reach this component TODO refactor into a separate method
					assert parent != null : "Composite component event did not refer to child of this component.";
					if(parent instanceof EditComponent) { //if we find an edit component above the removed component that is a child of this component
						return; //ignore non-top-level edit components
					}
				}
				for(final EditComponent removedEditComponent : getComponents(childComponentEvent.getComponent(), EditComponent.class, new ArrayList<EditComponent>(),
						true, false)) { //for all the top-level edit components of the removed component
					removedEditComponent.removeEditListener(getRepeatEditListener()); //stop repeating its edit events to our listeners
				}
			}

		});
	}

	//EditComponent implementation

	@Override
	public void addEditListener(final EditListener editListener) {
		getEventListenerManager().add(EditListener.class, editListener); //add the listener
	}

	@Override
	public void removeEditListener(final EditListener editListener) {
		getEventListenerManager().remove(EditListener.class, editListener); //remove the listener
	}

	/**
	 * Fires an edit event to all registered edit listeners. This method delegates to {@link #fireEdited(EditEvent)}.
	 * @see EditListener
	 * @see EditEvent
	 */
	protected void fireEdited() {
		if(getEventListenerManager().hasListeners(EditListener.class)) { //if there are edit listeners registered
			fireEdited(new EditEvent(this)); //create and fire a new edit event
		}
	}

	/**
	 * Fires a given edit event to all registered edit listeners.
	 * @param editEvent The edit event to fire.
	 */
	protected void fireEdited(final EditEvent editEvent) {
		for(final EditListener editListener : getEventListenerManager().getListeners(EditListener.class)) { //for each edit listener
			editListener.edited(editEvent); //dispatch the edit event to the listener
		}
	}
}
