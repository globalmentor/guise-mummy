package com.guiseframework.component;

import com.guiseframework.component.layout.Flow;

/**Control that allows a text component's text to be edited in-place.
@author Garret Wilson
*/
public class EditComponentTextControl extends AbstractEditComponentTextControl<Text>  
{

	/**Default constructor with a default text component, default text edit control, and {@link Flow#LINE} layout.*/
	public EditComponentTextControl()
	{
		this(Flow.LINE);	//construct the class with line flow layout
	}

	/**Flow constructor with a default text component and text edit control.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the flow axis is <code>null</code>.
	*/
	public EditComponentTextControl(final Flow flow)
	{
		this(new Text(), flow);	//construct the parent class with a default text component
	}

	/**Text component constructor with default text edit control and {@link Flow#LINE} flow.
	@param textComponent The component the text of which is to be edited.
	@exception NullPointerException if the text component is <code>null</code>.
	*/
	public EditComponentTextControl(final Text textComponent)
	{
		this(textComponent, new TextControl<String>(String.class));	//construct the class with a default text control
	}

	/**Text component and flow constructor with default text edit control.
	@param textComponent The component the text of which is to be edited.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the text component and/or flow is <code>null</code>.
	*/
	public EditComponentTextControl(final Text textComponent, final Flow flow)
	{
		this(textComponent, new TextControl<String>(String.class), flow);	//construct the class with a default text control
	}

	/**Text component and value control constructor with default {@link Flow#LINE} flow.
	@param textComponent The component the text of which is to be edited.
	@param editControl The control used to edit the text.
	@exception NullPointerException if the text component and/or edit control is <code>null</code>.
	*/
	public EditComponentTextControl(final Text textComponent, final ValueControl<String> editControl)
	{
		this(textComponent, editControl, Flow.LINE);	//construct the class with line flow
	}

	/**Text component, value control, and flow constructor.
	@param textComponent The component the text of which is to be edited.
	@param editControl The control used to edit the text.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the text component, value control, and/or flow axis is <code>null</code>.
	*/
	public EditComponentTextControl(final Text textComponent, final ValueControl<String> editControl, final Flow flow)
	{
		super(textComponent, Text.TEXT_PROPERTY, editControl, flow);	//construct the parent class with a flow layout
	}

	/**Retrieves the text from the edited component.
	This version returns the value of {@link Text#getText()}
	@param editedComponent The component the text of which is to be edited.
	@return The current text of the edited component
	*/
	protected String getText(final Text editedComponent)
	{
		return editedComponent.getText();	//return the component's text
	}

	/**Updates the text of the edited component.
	This version updates the text of the component using {@link Text#setText(String)}
	@param editedComponent The component the text of which is to be edited.
	@param newText The new text to set in the edited component.
	*/
	protected void setText(final Text editedComponent, final String newText)
	{
		editedComponent.setText(newText);	//set the component's text		
	}

}
