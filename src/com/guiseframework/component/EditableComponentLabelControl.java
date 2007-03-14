package com.guiseframework.component;

import java.beans.PropertyVetoException;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.prototype.*;
import com.guiseframework.theme.Theme;

/**Control that allows a component's label to be edited in-place.
Editing can be started by calling {@link #setMode(com.guiseframework.component.EditableComponentLabelControl.Mode)} with {@link Mode#EDIT}.
@author Garret Wilson
*/
public class EditableComponentLabelControl extends AbstractContainerControl<EditableComponentLabelControl> implements ModalComponent<EditableComponentLabelControl.Mode, EditableComponentLabelControl>  
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

	/**Whether the value is editable and the control will allow the the user to change the value.*/
	private boolean editable=true;	//TODO fix

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

	/**The current mode of interaction, or <code>null</code> if the component is in a modeless state.*/
	private Mode mode=null;

		/**@return The current mode of interaction, or <code>null</code> if the component is in a modeless state.*/
		public Mode getMode() {return mode;}

		/**Sets the mode of interaction.
		This is a bound property.
		Changing the mode to {@link Mode#EDIT} initiates the editing process; changing the mode from {@link Mode#EDIT} clears the value from the edit control.
		If the mode changes, this method will call {@link #update()}.
		@param newMode The new mode of component interaction.
		@exception IllegalStateException If editing is initiated and the current label of the component cannot be edited in the edit control.
		@see #MODE_PROPERTY 
		*/
		public void setMode(final Mode newMode)
		{
			if(mode!=newMode)	//if the value is really changing
			{
				final Mode oldMode=mode;	//get the old value
				mode=newMode;	//actually change the value
				final ValueControl<String, ?> editControl=getEditControl();	//get the edit control
				if(newMode==Mode.EDIT)	//if we are switching to edit mode
				{
					try
					{
						editControl.setValue(getLabelComponent().getLabel());	//initialize the edit control with the value of the component's label
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

	/**The component the label of which is to be edited.*/
	private Component<?> labelComponent;

		/**The component the label of which is to be edited.*/
		public Component<?> getLabelComponent() {return labelComponent;}

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

	/**The control used to edit the label.*/
	private ValueControl<String, ?> editControl;

		/**The control used to edit the label.*/
		public ValueControl<String, ?> getEditControl() {return editControl;}

		/**Sets the control used to edit the label.
		This is a bound property.
		@param newEditControl The control used to edit the label.
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

	/**The action prototype for editing the label.*/
	private final ActionPrototype editActionPrototype;

		/**@return The action prototype for editing the label.*/
		public ActionPrototype getEditActionPrototype() {return editActionPrototype;}

	/**The action prototype for accepting edits.*/
	private final ActionPrototype acceptActionPrototype;

		/**@return The action prototype for accepting edits.*/
		public ActionPrototype getAcceptActionPrototype() {return acceptActionPrototype;}

	/**The action prototype for rejecting edits.*/
	private final ActionPrototype rejectActionPrototype;

		/**@return The action prototype for rejecting edits.*/
		public ActionPrototype getRejectActionPrototype() {return rejectActionPrototype;}

	/**The action prototype for deleting the label.*/
	private final ActionPrototype deleteActionPrototype;

		/**@return The action prototype for deleting the label.*/
		public ActionPrototype getDeleteActionPrototype() {return deleteActionPrototype;}

	/**Default constructor with a default label component, default text edit control, and {@link Flow#LINE} layout.*/
	public EditableComponentLabelControl()
	{
		this(Flow.LINE);	//construct the class with line flow layout
	}

	/**Flow constructor with a default label component and text edit control.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the flow axis is <code>null</code>.
	*/
	public EditableComponentLabelControl(final Flow flow)
	{
		this(new Label(), flow);	//construct the parent class with a default label
	}

	/**Label component with default text edit control and {@link Flow#LINE} flow.
	@param labelComponent The component the label of which is to be edited.
	@exception NullPointerException if the label component is <code>null</code>.
	*/
	public EditableComponentLabelControl(final Component<?> labelComponent)
	{
		this(labelComponent, new TextControl<String>(String.class));	//construct the class with a default text control
	}

	/**Label component and flow constructor with default text edit control.
	@param labelComponent The component the label of which is to be edited.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the label component and/or flow is <code>null</code>.
	*/
	public EditableComponentLabelControl(final Component<?> labelComponent, final Flow flow)
	{
		this(labelComponent, new TextControl<String>(String.class), flow);	//construct the class with a default text control
	}

	/**Label component and value control constructor with default {@link Flow#LINE} flow.
	@param labelComponent The component the label of which is to be edited.
	@param editControl The control used to edit the label.
	@exception NullPointerException if the label component and/or edit control is <code>null</code>.
	*/
	public EditableComponentLabelControl(final Component<?> labelComponent, final ValueControl<String, ?> editControl)
	{
		this(labelComponent, editControl, Flow.LINE);	//construct the class with line flow
	}

	/**Label component, value control, and flow constructor.
	@param labelComponent The component the label of which is to be edited.
	@param editControl The control used to edit the label.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the label component, value control, and/or flow axis is <code>null</code>.
	*/
	public EditableComponentLabelControl(final Component<?> labelComponent, final ValueControl<String, ?> editControl, final Flow flow)
	{
		super(new FlowLayout(flow));	//construct the parent class with a flow layout
		this.labelComponent=checkInstance(labelComponent, "Label component cannot be null.");
		this.editControl=checkInstance(editControl, "Edit control cannot be null.");
		this.labelComponent.setDisplayed(true);	//display the label component
		add(labelComponent);	//add the label component
		this.editControl.setDisplayed(false);	//hide the label component
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
		acceptActionPrototype.setIcon(Theme.ICON_ACCEPT);
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
		rejectActionPrototype.setIcon(Theme.ICON_REJECT);
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
		deleteActionPrototype.setIcon(Theme.ICON_DELETE);
		deleteActionPrototype.setLabel(Theme.LABEL_DELETE);
		deleteActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent actionEvent)
					{
						deleteLabel();	//delete the current label
					}
				});
		update();	//update the component to initialize the child components and prototypes
	}

	/**Initiates editing.*/
	public void editLabel()
	{
		setMode(Mode.EDIT);	//switch to edit mode, updating the component
	}

	/**Accepts edits.*/
	public void acceptEdit()
	{
		getLabelComponent().setLabel(getEditControl().getValue());	//update the label component's label with the contents of the edit control
		setMode(null);	//switch out of edit mode, updating the component
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
		getLabelComponent().setLabel(null);	//remove the label
		update();	//update the component and prototypes
	}

	/**Update the states of the components and prototypes based upon the current state of the component.*/ 
	protected void update()
	{
		final Mode mode=getMode();	//get the current mode
			//label component
		final Component<?> labelComponent=getLabelComponent();
		labelComponent.setDisplayed(mode==null);	//only show the label component if we're not editing
		final String label=labelComponent.getLabel();	//get the current label
			//edit control
		final ValueControl<String, ?> editControl=getEditControl();
		editControl.setDisplayed(mode==Mode.EDIT);	//only show the edit component if we're editing
			//edit action prototype
		final ActionPrototype editActionPrototype=getEditActionPrototype();
		editActionPrototype.setEnabled(mode!=Mode.EDIT);	//only enable the edit prototype if we aren't editing
		if(label==null)	//if there is currently no label
		{
			editActionPrototype.setIcon(Theme.ICON_ADD);	//show the add icon
			editActionPrototype.setLabel(Theme.LABEL_ADD);	//show the add label
		}
		else	//if there is a label
		{
			editActionPrototype.setIcon(Theme.ICON_EDIT);	//show the edit icon
			editActionPrototype.setLabel(Theme.LABEL_EDIT);	//show the edit label
		}
			//accept action prototype
		final ActionPrototype acceptActionPrototype=getAcceptActionPrototype();
		acceptActionPrototype.setEnabled(mode==Mode.EDIT);	//only enable the accept action prototype if we're editing
			//reject action prototype
		final ActionPrototype rejectActionPrototype=getRejectActionPrototype();
		rejectActionPrototype.setEnabled(mode==Mode.EDIT);	//only enable the reject action prototype if we're editing
			//delete action prototype
		final ActionPrototype deleteActionPrototype=getDeleteActionPrototype();
		deleteActionPrototype.setEnabled(label!=null);	//only enable the delete action prototype if there is a label to delete
	}

}
