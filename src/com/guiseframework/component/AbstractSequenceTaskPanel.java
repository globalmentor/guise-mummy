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
import com.globalmentor.util.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.controller.*;
import com.guiseframework.prototype.*;

/**Abstract base class for a panel that allows progression in a sequence.
<p>When progressing through the sequence, this panel attempts to verify
	 the content component, if it is <code>Verifiable</code>, before changing
	 position in the sequence and before finishing.</p>
@author Garret Wilson
@see Verifiable
*/
public abstract class AbstractSequenceTaskPanel extends AbstractPanel
{

	/**The button for staring the sequence; created from the corresponding action.*/
//TODO fix	private final JButton startButton;

		/**@return The action for starting the sequence; created from the corresponding action.
		@see #getStartAction
		*/
//TODO fix		private JButton getStartButton() {return startButton;}		

	/**The button for going to the previous component; created from the corresponding action.*/
//TODO fix	private final JButton previousButton;

		/**@return The action for going to the previous component; created from the corresponding action.
		@see #getPreviousAction
		*/
//TODO fix		private JButton getPreviousButton() {return previousButton;}
		
	/**The button for going to the next component; created from the corresponding action.*/
//TODO fix	private final JButton nextButton;

		/**@return The action for going to the next component; created from the corresponding action.
		@see #getNextAction
		*/
//TODO fix		private JButton getNextButton() {return nextButton;}
		
	/**The button for finishing the sequence; created from the corresponding action.*/
//TODO fix	private final JButton finishButton;

		/**@return The action for finishing the sequence; created from the corresponding action.
		@see #getFinishAction
		*/
//TODO fix		private JButton getFinishButton() {return finishButton;}		

	/**The button for advancing in the sequence; created from the corresponding action.*/
//TODO fix	private final JButton advanceButton;

		/**@return The action for advancing in sequence; created from the corresponding action.
		@see #getAdvanceAction
		*/
//TODO fix		private JButton getAdvanceButton() {return advanceButton;}		

	/**Whether the advance buttons are distinct or dual-duty.*/
	private boolean distinctAdvance;

		/**@return Whether the advance buttons are distinct or dual-duty;
			this defaults to <code>false</code>.
		*/
		public boolean isDistinctAdvance() {return distinctAdvance;}

		/**Sets whether the advance buttons are distinct or dual-duty.
		@param distinct <code>true</code> if there should be distinct buttons for
			start, next, and finish, or <code>false</code> if one button should share
			these responsibilitiese.
		*/
		public void setDistinctAdvance(final boolean distinct) {distinctAdvance=distinct;}

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
		final Button startButton=new Button(taskController.getStartActionPrototype());	//create the buttons
		final Button previousButton=new Button(taskController.getPreviousActionPrototype());
		final Button nextButton=new Button(taskController.getNextActionPrototype());
		final Button finishButton=new Button(taskController.getFinishActionPrototype());
		final Button advanceButton=new Button(taskController.getAdvanceActionPrototype());
		distinctAdvance=false;	//default to shared actions for advancing
		final Toolbar toolbar=new Toolbar();
		if(isDistinctAdvance())	//if we should have distinct advance, use separate actions
		{
			toolbar.add(taskController.getStartActionPrototype());
//TODo fix			actionManager.addToolAction(new ActionManager.SeparatorAction());
			toolbar.add(taskController.getPreviousActionPrototype());
			toolbar.add(taskController.getNextActionPrototype());
		}
		else	//if we should not have distinct advance, use a dual-use action
		{
			toolbar.add(taskController.getPreviousActionPrototype());
			toolbar.add(taskController.getAdvanceActionPrototype());
		}
//TODO fix		actionManager.addToolAction(new ActionManager.SeparatorAction());
		toolbar.add(taskController.getConfirmActionPrototype());
		add(toolbar, new RegionConstraints(Region.PAGE_END));
	}

	/**Initializes actions in the action manager.
	@param actionManager The implementation that manages actions.
	*/
/*TODO fix
	protected void initializeActions(final ActionManager actionManager)
	{
		super.initializeActions(actionManager);	//do the default initialization
		if(isDistinctAdvance())	//if we should have distinct advance, use separate actions
		{
			actionManager.addToolAction(getStartActionPrototype());
			actionManager.addToolAction(new ActionManager.SeparatorAction());
			actionManager.addToolAction(getPreviousActionPrototype());
			actionManager.addToolAction(getNextActionPrototype());
		}
		else	//if we should not have distinct advance, use a dual-use action
		{
			actionManager.addToolAction(getPreviousActionPrototype());
			actionManager.addToolAction(getAdvanceAction());
		} 
		actionManager.addToolAction(new ActionManager.SeparatorAction());
		actionManager.addToolAction(getConfirmActionPrototype());
	}
*/

	/**Initializes the user interface.*/
/*TODO fix
	protected void initializeUI()
	{
		if(getToolBar()!=null)	//if we have a toolbar
			getToolBar().setButtonTextVisible(true);	//show text on the toolbar buttons
		super.initializeUI();	//do the default initialization
		previousButton.setHorizontalTextPosition(SwingConstants.LEADING);	//change the text position of the previous button
		setContentComponent(getDefaultComponent());	//start with the default component		
		setPreferredSize(new Dimension(300, 200));	//set an arbitrary preferred size
	}
*/

	/**Updates the states of the prototypes, including enabled/disabled status, proxied actions, etc.
	*/
/*TODO fix; decide in which class to place
	public void updateStates()
	{
		super.updateStatus(); //update the default actions
		getStartActionPrototype().setEnabled(getAdvanceAction().getProxiedAction()!=getStartActionPrototype()); //only allow starting if we haven't started, yet
		getPreviousActionPrototype().setEnabled(hasPrevious()); //only allow going backwards if we have a previous step
		getNextActionPrototype().setEnabled(hasNext()); //only allow going backwards if we have a next step
		getFinishActionPrototype().setEnabled(!hasNext()); //only allow finishing if there are no next components
		getConfirmActionPrototype().setEnabled(isConfirmNavigation() && getConfirmingActionPrototype()!=null); //only allow confirmation if confirmation is enabled and there is an action waiting to be confirmed
		if(getToolBar()!=null)	//if we have a toolbar
		{
			final Component confirmComponent=getToolBar().getComponent(getConfirmActionPrototype());	//see if the confirm action is on the toolbar
			if(confirmComponent!=null)	//if the action has a corresponding component on the toolbar
			{
				confirmComponent.setVisible(isConfirmNavigation());	//only show the confirm action if navigation confirmation is enabled
			}
		}
		if(getAdvanceAction().getProxiedAction()!=getStartActionPrototype())	//if we've already started
		{
				//determine if advancing should go to the next item in the sequence or finish
			getAdvanceAction().setProxiedAction(hasNext() ? getNextActionPrototype() : getFinishActionPrototype());			
		}
		final JRootPane rootPane=getRootPane();	//get the ancestor root pane, if there is one
		if(rootPane!=null)	//if there is a root pane
		{
			final JButton defaultButton;	//determind the default button
			if(isDistinctAdvance())	//if we're using distinct buttons for advance
			{
				defaultButton=hasNext() ? getNextButton() : getFinishButton();	//set the next button as the default unless we're finished; in that case, set the finish button as the default
			}
			else	//if we're using a dual-use button for advance
			{					
				defaultButton=getAdvanceButton();	//set the advance button as the default	
			}
			rootPane.setDefaultButton(defaultButton);	//update the default button	
		}
		final Action confirmingAction=getConfirmingActionPrototype();	//see if there is an action waiting to be confirmed
		if(confirmingAction!=null)	//if there is an action waiting to be confirmed
		{
			confirmingAction.setEnabled(false);	//disable the confirming action, because it will be accessed indirectly through the confirmation action
		}
	}
*/

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
