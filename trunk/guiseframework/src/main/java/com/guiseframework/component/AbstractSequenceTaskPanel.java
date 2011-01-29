/*
 * Copyright © 2000-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static com.globalmentor.java.Objects.*;

import com.globalmentor.beans.*;
import com.globalmentor.model.SequenceTask;
import com.globalmentor.model.Verifiable;
import com.globalmentor.util.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.controller.*;
import com.guiseframework.input.*;

/**Abstract base class for a panel that allows progression in a sequence.
<p>When progressing through the sequence, this panel attempts to verify
	 the content component, if it is <code>Verifiable</code>, before changing
	 position in the sequence and before finishing.</p>
@author Garret Wilson
@see Verifiable
*/
public abstract class AbstractSequenceTaskPanel extends AbstractPanel
{

	/**The button for starting the sequence; created from the corresponding action prototype.*/
	private final ButtonControl startButton;

		/**@return The action for starting the sequence; created from the corresponding action prototype.
		@see SequenceTaskController#getStartActionPrototype()
		*/
		private ButtonControl getStartButton() {return startButton;}		

	/**The button for going to the previous step; created from the corresponding action prototype.*/
	private final ButtonControl previousButton;

		/**@return The action for going to the previous step; created from the corresponding action prototype.
		@see SequenceTaskController#getPreviousActionPrototype()
		*/
		private ButtonControl getPreviousButton() {return previousButton;}
		
	/**The button for going to the next step; created from the corresponding action prototype.*/
	private final ButtonControl nextButton;

		/**@return The action for going to the next step; created from the corresponding action prototype.
		@see SequenceTaskController#getNextActionPrototype()
		*/
		private ButtonControl getNextButton() {return nextButton;}
		
	/**The button for finishing the sequence; created from the corresponding action prototype.*/
	private final ButtonControl finishButton;

		/**@return The action for finishing the sequence; created from the corresponding action prototype.
		@see SequenceTaskController#getFinishActionPrototype()
		*/
		private ButtonControl getFinishButton() {return finishButton;}		

	/**The button for advancing in the sequence; created from the corresponding action prototype.*/
	private final ButtonControl advanceButton;

		/**@return The action for advancing in sequence; created from the corresponding action prototype.
		@see SequenceTaskController#getAdvanceActionPrototype()
		*/
		private ButtonControl getAdvanceButton() {return advanceButton;}		

	/**The toolbar on which the controls are located.*/
	private final Toolbar toolbar;
		
	/**Whether the advance buttons are distinct or dual-duty.*/
	private boolean distinctAdvance;

		/**@return Whether the advance buttons are distinct or dual-duty;
			this defaults to <code>false</code>.
		*/
		public boolean isDistinctAdvance() {return distinctAdvance;}

		/**Sets whether the advance buttons are distinct or dual-duty.
		This method updates the toolbar if this property changes.
		@param newDistinctAdvance <code>true</code> if there should be distinct buttons for
			start, next, and finish, or <code>false</code> if one button should share
			these responsibilitiese.
		*/
		public void setDistinctAdvance(final boolean newDistinctAdvance)
		{
			final boolean oldDistinctAdvance=distinctAdvance;;
			if(oldDistinctAdvance!=newDistinctAdvance)
			{
				distinctAdvance=newDistinctAdvance;
				configureToolbar();	//reconfigure the toolbar so that the the buttons will reflect this setting
			}
		}

	/**The object controlling the sequence task.*/
	private final SequenceTaskController taskController;

		/**@return The object controlling the sequence task.*/
		public SequenceTaskController getTaskController() {return taskController;}

	/**Task controller constructor.
	@param taskController The object controlling the sequence task.
	@exception NullPointerException if the given task controller is <code>null</code>.
	*/
	public AbstractSequenceTaskPanel(final SequenceTaskController taskController)
	{
		super(new RegionLayout());	//construct the parent class
		this.taskController=checkInstance(taskController, "Task controller cannot be null.");
		startButton=new Button(taskController.getStartActionPrototype());	//create the buttons
		previousButton=new Button(taskController.getPreviousActionPrototype());
		nextButton=new Button(taskController.getNextActionPrototype());
		finishButton=new Button(taskController.getFinishActionPrototype());
		advanceButton=new Button(taskController.getAdvanceActionPrototype());
		distinctAdvance=false;	//default to shared actions for advancing
		toolbar=new Toolbar();
		add(toolbar, new RegionConstraints(Region.PAGE_END));
		configureToolbar();	//arrange the controls on the toolbar for the first time
		taskController.addPropertyChangeListener(SequenceTaskController.CONFIRM_NAVIGATION_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>()	//listen for the confirm navigation property changing
				{
					public void propertyChange(final GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent)
					{
						configureToolbar();	//reconfigure the toolbar if the confirm navigation specification changes, because this will determine which buttons go on the toolbar
					}
				});
		taskController.getTask().addPropertyChangeListener(SequenceTask.SEQUENCE_INDEX_PROPERTY, new AbstractGenericPropertyChangeListener<Integer>()	//listen for the sequence index changing
				{
					public void propertyChange(final GenericPropertyChangeEvent<Integer> genericPropertyChangeEvent)
					{
						onSequenceIndexChange(genericPropertyChangeEvent.getOldValue().intValue(), genericPropertyChangeEvent.getNewValue().intValue());
					}
				});
		final BindingInputStrategy bindingInputStrategy=new BindingInputStrategy(getInputStrategy());	//create a new input strategy based upon the current input strategy (if any)
		bindingInputStrategy.bind(new CommandInput(ProcessCommand.CONTINUE), getTaskController().getAdvanceActionPrototype());	//map the "continue" command to the card panel continue action prototype
		setInputStrategy(bindingInputStrategy);	//switch to our new input strategy
	}

	/**Clears and the appropriate components to the toolbar based upon the current settings.
	@see #isDistinctAdvance()
	*/
	protected void configureToolbar()
	{
		if(isDistinctAdvance())	//if we should have distinct advance, use separate actions
		{
			toolbar.add(taskController.getStartActionPrototype());
//TODO fix			actionManager.addToolAction(new ActionManager.SeparatorAction());
			toolbar.add(taskController.getPreviousActionPrototype());
			toolbar.add(taskController.getNextActionPrototype());
		}
		else	//if we should not have distinct advance, use a dual-use action
		{
			toolbar.add(taskController.getPreviousActionPrototype());
			toolbar.add(taskController.getAdvanceActionPrototype());
		}
//TODO fix		actionManager.addToolAction(new ActionManager.SeparatorAction());
		if(getTaskController().isConfirmNavigation())	//if we should confirm navigation
		{
			toolbar.add(taskController.getConfirmActionPrototype());
		}
	}

	/**Called after the sequence index changes.
	Any derived class that overrides this method should call this version.
	@param oldIndex The old index, or <code>-1</code> if there was no old index.
	@param newIndex The new index, or <code>-1</code> if there was no new index.
	*/
	protected void onSequenceIndexChange(final int oldIndex, final int newIndex)
	{
		getTaskController().setConfirmingActionPrototype(null);	//make sure no actions are waiting for confirmation
		update();	//update the prototypes
//TODO fix		scheduleTimeStatusUpdate();	//schedule a time status update if we need to for this new item
	}

	/**Verifies the contents of the current component, if the current component
		can be verified.
	@return <code>true</code> if the current component was verified or is not
		verifiable, else <code>false</code> if the current component was verifiable
		but returned <code>false</code> when verified.
	@see Verifiable#verify
	*/
/*TODO del if not needed; see validate()
	protected boolean verifyCurrentComponent()
	{
		final Component currentComponent=getContentComponent();	//get the current content component
		if(currentComponent instanceof Verifiable)	//if we can verify the component's contents
		{
			if(!((Verifiable)currentComponent).verify())	//if the current component's contents do not verify
			{
				return false;	//show that the component verified incorrectly
			}
		}
		return true;	//show that the component didn't verify incorrectly
	}
*/

}
