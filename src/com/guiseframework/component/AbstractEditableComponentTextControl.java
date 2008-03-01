package com.guiseframework.component;

import java.beans.*;


import static com.globalmentor.java.Objects.*;


import com.globalmentor.beans.*;
import com.globalmentor.util.Debug;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.input.*;
import com.guiseframework.prototype.*;
import com.guiseframework.theme.Theme;

/**Control that allows some component text property to be edited in-place.
Editing can be started by calling {@link #setMode(AbstractEditableComponentTextControl.Mode)} with {@link Mode#EDIT}.
<p>This class binds a single left mouse click input to {@link #getEditActionPrototype()},
the command {@link ProcessCommand#CONTINUE} to {@link #getAcceptActionPrototype()},
and the command {@link ProcessCommand#ABORT} to {@link #getRejectActionPrototype()}.</p>
@param <EC> The type of component being edited.
@author Garret Wilson
*/
public abstract class AbstractEditableComponentTextControl<EC extends Component> extends AbstractContainerControl implements ModalComponent<AbstractEditableComponentTextControl.Mode>, EditComponent
{

	/**The mode of this component; whether the component is being edited.*/
	public enum Mode implements com.guiseframework.component.Mode
	{
		EDIT;
	}

	/**The label component bound property.*/
//TODO del if not used	public final static String LABEL_COMPONENT_PROPERTY=getPropertyName(EditableComponentLabelControl.class, "labelComponent");
	/**The edit control bound property.*/
//TODO del if not used	public final static String EDIT_CONTROL_PROPERTY=getPropertyName(EditableComponentLabelControl.class, "editControl");

	/**Whether the value is editable and the component will allow the the user to change the value.*/
	private boolean editable=true;

		/**@return Whether the value is editable and the component will allow the the user to change the value.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the value is editable and the component will allow the the user to change the value.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the component should allow the user to change the value.
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

	/**The current mode of interaction, or <code>null</code> if the component is in a modeless state.*/
	private Mode mode=null;

		/**@return The current mode of interaction, or <code>null</code> if the component is in a modeless state.*/
		public Mode getMode() {return mode;}

		/**Sets the mode of interaction.
		This is a bound property.
		Changing the mode to {@link Mode#EDIT} initiates the editing process; changing the mode from {@link Mode#EDIT} clears the value from the edit control.
		If the mode changes, this method will call {@link #update()}.
		@param newMode The new mode of component interaction.
		@exception IllegalStateException If editing is initiated and the current text of the component cannot be edited in the edit control.
		@see #MODE_PROPERTY 
		*/
		public void setMode(final Mode newMode)
		{
			if(mode!=newMode)	//if the value is really changing
			{
				final Mode oldMode=mode;	//get the old value
				mode=newMode;	//actually change the value
				final ValueControl<String> editControl=getEditControl();	//get the edit control
				if(newMode==Mode.EDIT)	//if we are switching to edit mode
				{
					try
					{
						editControl.setValue(getText(getEditedComponent()));	//initialize the edit control with the value of the edited component's text
					}
					catch(final PropertyVetoException propertyVetoException)	//if we can't store the value in the edit control 
					{
						throw new IllegalStateException(propertyVetoException);
					}					
				}
				else if(oldMode==Mode.EDIT)	//if we're switching out of edit mode
				{
					editControl.clearValue();	//release the value from the edit control
				}
				update();	//update this component to reflect the new mode
				firePropertyChange(MODE_PROPERTY, oldMode, newMode);	//indicate that the value changed
			}			
		}

	/**The component the text of which is to be edited.*/
	private EC editedComponent;

		/**The component the text of which is to be edited.*/
		public EC getEditedComponent() {return editedComponent;}

		/**Sets the component the label of which is to be edited.
		This is a bound property.
		@param newLabelComponent The component the label of which is to be edited.
		@exception NullPointerException if the given component is <code>null</code>.
		@see #LABEL_COMPONENT_PROPERTY
		*/
/*TODO del if not used; this will require updating the layout
		public void setLabelComponent(final Component<?> newLabelComponent)
		{
			if(labelComponent.equals(checkInstance(newLabelComponent, "Label component cannot be null.")))	//if the value is really changing
			{
				final Component<?> oldLabelComponent=labelComponent;	//get the old value
				labelComponent=newLabelComponent;	//actually change the value
				firePropertyChange(LABEL_COMPONENT_PROPERTY, oldLabelComponent, newLabelComponent);	//indicate that the value changed
			}			
		}
*/

	/**The control used to edit the text.*/
	private ValueControl<String> editControl;

		/**The control used to edit the text.*/
		public ValueControl<String> getEditControl() {return editControl;}

		/**Sets the control used to edit the text.
		This is a bound property.
		@param newEditControl The control used to edit the text.
		@exception NullPointerException if the given component is <code>null</code>.
		@see #EDIT_CONTROL_PROPERTY
		*/
/*TODO del if not used; this will require updating the layout
		public void setEditControl(final ValueControl<String, ?> newEditControl)
		{
			if(editControl.equals(checkInstance(newEditControl, "Value control cannot be null.")))	//if the value is really changing
			{
				final ValueControl<String, ?> oldEditControl=editControl;	//get the old value
				editControl=newEditControl;	//actually change the value
				firePropertyChange(EDIT_CONTROL_PROPERTY, oldEditControl, newEditControl);	//indicate that the value changed
			}			
		}
*/

	/**The action prototype for editing the text.*/
	private final ActionPrototype editActionPrototype;

		/**@return The action prototype for editing the text.*/
		public ActionPrototype getEditActionPrototype() {return editActionPrototype;}

	/**The action prototype for accepting edits.*/
	private final ActionPrototype acceptActionPrototype;

		/**@return The action prototype for accepting edits.*/
		public ActionPrototype getAcceptActionPrototype() {return acceptActionPrototype;}

	/**The action prototype for rejecting edits.*/
	private final ActionPrototype rejectActionPrototype;

		/**@return The action prototype for rejecting edits.*/
		public ActionPrototype getRejectActionPrototype() {return rejectActionPrototype;}

	/**The action prototype for deleting the text.*/
	private final ActionPrototype deleteActionPrototype;

		/**@return The action prototype for deleting the text.*/
		public ActionPrototype getDeleteActionPrototype() {return deleteActionPrototype;}

	/**Edited component, value control, and flow constructor.
	@param editedComponent The component the text of which is to be edited.
	@param editedProperty The component property that will be affected when the text changes.
	@param editControl The control used to edit the text.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the edited component, value control, text property, and/or flow axis is <code>null</code>.
	*/
	public AbstractEditableComponentTextControl(final EC editedComponent, final String editedProperty, final ValueControl<String> editControl, final Flow flow)
	{
		super(new FlowLayout(flow));	//construct the parent class with a flow layout
		this.editedComponent=checkInstance(editedComponent, "Edited component cannot be null.");
		this.editControl=checkInstance(editControl, "Edit control cannot be null.");
		this.editedComponent.setDisplayed(true);	//display the edited component
		add(editedComponent);	//add the edited component
		this.editControl.setDisplayed(false);	//hide the edit control
		add(editControl);	//add the edit control
			//edit action prototype
		editActionPrototype=new ActionPrototype();
		editActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent actionEvent)
					{
						editLabel();	//initiate editing
					}
				});
			//accept action prototype
		acceptActionPrototype=new ActionPrototype();
		acceptActionPrototype.setGlyphURI(Theme.GLYPH_ACCEPT);
		acceptActionPrototype.setLabel(Theme.LABEL_ACCEPT);
		acceptActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent actionEvent)
					{
						acceptEdit();	//accept edits
					}
				});
			//reject action prototype
		rejectActionPrototype=new ActionPrototype();
		rejectActionPrototype.setGlyphURI(Theme.GLYPH_REJECT);
		rejectActionPrototype.setLabel(Theme.LABEL_REJECT);
		rejectActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent actionEvent)
					{
						rejectEdit();	//cancel editing
					}
				});
			//delete action prototype
		deleteActionPrototype=new ActionPrototype();
		deleteActionPrototype.setGlyphURI(Theme.GLYPH_DELETE);
		deleteActionPrototype.setLabel(Theme.LABEL_DELETE);
		deleteActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent actionEvent)
					{
						deleteLabel();	//delete the current label
					}
				});
		addPropertyChangeListener(EDITABLE_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>()	//listen for the editable property changing
				{
					public void propertyChange(final GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent)	//if the editable property changes
					{
						update();	//update this component TODO maybe make an automatic generator for a property change listener that calls update()
					}
				});		
		editedComponent.addPropertyChangeListener(checkInstance(editedProperty, "Edited property cannot be null."), new PropertyChangeListener()	//listen for the edited property changing
				{
					public void propertyChange(final PropertyChangeEvent propertyChangeEvent)	//if the edited property changes
					{
						update();	//update this component TODO maybe make an automatic generator for a property change listener that calls update()
					}
				});		
		update();	//update the component to initialize the child components and prototypes

		final BindingInputStrategy bindingInputStrategy=new BindingInputStrategy(getInputStrategy());	//create a new input strategy based upon the current input strategy (if any)
		bindingInputStrategy.bind(new MouseClickInput(MouseButton.LEFT, 1), editActionPrototype);	//map a single mouse click to the edit action prototype
		bindingInputStrategy.bind(new CommandInput(ProcessCommand.ABORT), rejectActionPrototype);	//map the "abort" command to the reject action prototype
		bindingInputStrategy.bind(new CommandInput(ProcessCommand.CONTINUE), acceptActionPrototype);	//map the "continue" command to the accept action prototype
		setInputStrategy(bindingInputStrategy);	//switch to our new input strategy
	}

	/**Initiates editing.*/
	public void editLabel()
	{
		setMode(Mode.EDIT);	//switch to edit mode, updating the component
	}

	/**Accepts edits.*/
	public void acceptEdit()
	{
		setText(getEditedComponent(), getEditControl().getValue());	//update the edited component's text with the contents of the edit control
		setMode(null);	//switch out of edit mode, updating the component
		fireEdited();	//inform listeners that an edit has occurred
	}

	/**Cancels edits.*/
	public void rejectEdit()
	{
		setMode(null);	//switch out of edit mode without saving the edits, updating the component
	}

	/**Removes the current label by setting it to <code>null</code>.*/
	public void deleteLabel()
	{
		setMode(null);	//make sure we're not in editing mode
		setText(getEditedComponent(), null);	//remove the label
		update();	//update the component and prototypes
	}

	/**Update the states of the components and prototypes based upon the current state of the component.*/ 
	protected void update()
	{
		final boolean isEditable=isEditable();	//see if this component is editable
		final Mode mode=getMode();	//get the current mode
			//edited component
		final EC editedComponent=getEditedComponent();
		editedComponent.setDisplayed(mode==null);	//only show the edited component if we're not editing
		final String label=getText(editedComponent);	//get the current text of the edited component
			//edit control
		final ValueControl<String> editControl=getEditControl();
		editControl.setDisplayed(mode==Mode.EDIT);	//only show the edit component if we're editing
			//edit action prototype
		final ActionPrototype editActionPrototype=getEditActionPrototype();
		editActionPrototype.setEnabled(isEditable && mode!=Mode.EDIT);	//only enable the edit prototype if we aren't editing
		if(label==null)	//if there is currently no label
		{
			editActionPrototype.setGlyphURI(Theme.GLYPH_ADD);	//show the add icon
			editActionPrototype.setLabel(Theme.LABEL_ADD);	//show the add label
		}
		else	//if there is a label
		{
			editActionPrototype.setGlyphURI(Theme.GLYPH_EDIT);	//show the edit icon
			editActionPrototype.setLabel(Theme.LABEL_EDIT);	//show the edit label
		}
			//accept action prototype
		final ActionPrototype acceptActionPrototype=getAcceptActionPrototype();
		acceptActionPrototype.setEnabled(isEditable && mode==Mode.EDIT);	//only enable the accept action prototype if we're editing
			//reject action prototype
		final ActionPrototype rejectActionPrototype=getRejectActionPrototype();
		rejectActionPrototype.setEnabled(isEditable && mode==Mode.EDIT);	//only enable the reject action prototype if we're editing
			//delete action prototype
		final ActionPrototype deleteActionPrototype=getDeleteActionPrototype();
		deleteActionPrototype.setEnabled(isEditable && mode==Mode.EDIT && label!=null);	//only enable the delete action prototype if there is a label to delete TODO fix editing mode requirement; probably provide an option
	}

	/**Retrieves the text from the edited component.
	@param editedComponent The component the text of which is to be edited.
	@return The current text of the edited component
	*/
	protected abstract String getText(final EC editedComponent);

	/**Updates the text of the edited component.
	@param editedComponent The component the text of which is to be edited.
	@param newText The new text to set in the edited component.
	*/
	protected abstract void setText(final EC editedComponent, final String newText);

	//EditComponent implementation

	/**Adds an edit listener.
	@param editListener The edit listener to add.
	*/
	public void addEditListener(final EditListener editListener)
	{
		getEventListenerManager().add(EditListener.class, editListener);	//add the listener
	}

	/**Removes an edit listener.
	@param editListener The edit listener to remove.
	*/
	public void removeEditListener(final EditListener editListener)
	{
		getEventListenerManager().remove(EditListener.class, editListener);	//remove the listener
	}

	/**Fires an edit event to all registered edit listeners.
	This method delegates to {@link #fireEdited(EditEvent)}.
	@see EditListener
	@see EditEvent
	*/
	protected void fireEdited()
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(EditListener.class))	//if there are edit listeners registered
		{
			fireEdited(new EditEvent(this));	//create and fire a new edit event
		}
	}

	/**Fires a given edit event to all registered edit listeners.
	@param editEvent The edit event to fire.
	*/
	protected void fireEdited(final EditEvent editEvent)
	{
		for(final EditListener editListener:getEventListenerManager().getListeners(EditListener.class))	//for each edit listener
		{
			editListener.edited(editEvent);	//dispatch the edit event to the listener
		}
	}

}
