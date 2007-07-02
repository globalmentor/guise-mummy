package com.guiseframework.component;

import com.guiseframework.component.layout.*;

/**Control that allows a component's label to be edited in-place.
@author Garret Wilson
*/
public class EditableComponentLabelControl extends AbstractEditableComponentTextControl<Component>  
{

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
		this(new Label(), flow);	//construct the parent class with a default label component
	}

	/**Label component constructor with default text edit control and {@link Flow#LINE} flow.
	@param labelComponent The component the label of which is to be edited.
	@exception NullPointerException if the label component is <code>null</code>.
	*/
	public EditableComponentLabelControl(final Component labelComponent)
	{
		this(labelComponent, new TextControl<String>(String.class));	//construct the class with a default text control
	}

	/**Label component and flow constructor with default text edit control.
	@param labelComponent The component the label of which is to be edited.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the label component and/or flow is <code>null</code>.
	*/
	public EditableComponentLabelControl(final Component labelComponent, final Flow flow)
	{
		this(labelComponent, new TextControl<String>(String.class), flow);	//construct the class with a default text control
	}

	/**Label component and value control constructor with default {@link Flow#LINE} flow.
	@param labelComponent The component the label of which is to be edited.
	@param editControl The control used to edit the label.
	@exception NullPointerException if the label component and/or edit control is <code>null</code>.
	*/
	public EditableComponentLabelControl(final Component labelComponent, final ValueControl<String> editControl)
	{
		this(labelComponent, editControl, Flow.LINE);	//construct the class with line flow
	}

	/**Label component, value control, and flow constructor.
	@param labelComponent The component the label of which is to be edited.
	@param editControl The control used to edit the label.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the label component, value control, and/or flow axis is <code>null</code>.
	*/
	public EditableComponentLabelControl(final Component labelComponent, final ValueControl<String> editControl, final Flow flow)
	{
		super(labelComponent, Component.LABEL_PROPERTY, editControl, flow);	//construct the parent class with a flow layout
	}

	/**Retrieves the text from the edited component.
	This version returns the value of {@link Component#getLabel()}
	@param editedComponent The component the text of which is to be edited.
	@return The current text of the edited component
	*/
	protected String getText(final Component editedComponent)
	{
		return editedComponent.getLabel();	//return the component's label
	}

	/**Updates the text of the edited component.
	This version updates the text of the component using {@link Component#setLabel(String)}
	@param editedComponent The component the text of which is to be edited.
	@param newText The new text to set in the edited component.
	*/
	protected void setText(final Component editedComponent, final String newText)
	{
		editedComponent.setLabel(newText);	//set the component's label		
	}

}
