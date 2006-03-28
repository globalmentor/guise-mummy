package com.guiseframework.component;

import java.text.MessageFormat;

import com.guiseframework.GuiseSession;
import com.guiseframework.event.ActionEvent;
import com.guiseframework.event.ActionListener;
import com.guiseframework.model.*;
import com.guiseframework.validator.ValidationException;

/**Default implementation of a frame for communication of an option such as "OK" or "Cancel".
An option frame defaults to a single composite child panel with a row of options along the bottom.
The contents of an option dialog frame should be accessed by {@link #getOptionContent()} and {@link #setOptionContent(Component)}.
The labels and icons are accessed from the resources using resouce keys
	<code>frame.dialog.options.<var>option</var>.label</code> and <code>frame.dialog.options.<var>option</var>.icon</code>,
	respectively, where <var>option</var> represents the option enum value such as "OK".
@author Garret Wilson
*/
public class DefaultOptionDialogFrame extends AbstractOptionDialogFrame<DefaultOptionDialogFrame.Option, DefaultOptionDialogFrame>
{

	/**The resource key format pattern for each option label.*/
	public final static String FRAME_DIALOG_OPTION_LABEL_RESOURCE_KEY_FORMAT_PATTERN="frame.dialog.option.{0}.label";
	/**The resource key format pattern for each option icon.*/
	public final static String FRAME_DIALOG_OPTION_ICON_RESOURCE_KEY_FORMAT_PATTERN="frame.dialog.option.{0}.icon";
	
	/**The options which can be returned from this frame.
	The option ordinals represent the order in which they should be presented.
	*/
	public enum Option
	{
		OK,
		YES,
		YES_ALL,
		NO,
		NO_ALL,
		ABORT,
		RETRY,
		FAIL,
		STOP,
		CANCEL
	}

	/**Session and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final Option... options)
	{
		this(session, (String)null, options);	//construct the component, indicating that a default ID should be used
	}

	/**Session, component, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final Component<?> component, final Option... options)
	{
		this(session, (String)null, component, options);	//construct the component, indicating that a default ID should be used
	}

	/**Session, model, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final ValueModel<Option> model, final Option... options)
	{
		this(session, (String)null, model, options);	//construct the component, indicating that a default ID should be used
	}

	/**Session, model, component, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final ValueModel<Option> model, final Component<?> component, final Option... options)
	{
		this(session, (String)null, model, component, options);	//construct the component, indicating that a default ID should be used
	}

	/**Session, ID, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final Option... options)
	{
		this(session, id, new DefaultValueModel<Option>(session, Option.class), options);	//use a default value model
	}

	/**Session, ID, component, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final Component<?> component, final Option... options)
	{
		this(session, id, new DefaultValueModel<Option>(session, Option.class), component, options);	//use a default value model
	}

	/**Session, ID, model, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param options The available options.
	@exception NullPointerException if the given session, model, and or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final ValueModel<Option> model, final Option... options)
	{
		this(session, id, model, null, options);	//default to no component
	}

	/**Session, ID, model, component, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The component representing the content of the option dialog frame, or <code>null</code> if there is no content component.
	@param options The available options.
	@exception NullPointerException if the given session, model, and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final ValueModel<Option> model, final Component<?> component, final Option... options)
	{
		super(session, id, model, component, options);	//construct the parent class
	}

	/**Creates a component to represent the given option.
	This implementation creates a button for the given option.
	@param option The option for which a component should be created.
	*/
	protected Component<?> createOptionComponent(final Option option)
	{
		final GuiseSession session=getSession();	//get the session
		final Button button=new Button(session);	//create a new button
		final String optionString=option.toString();	//get the string form of the option
		button.setLabelResourceKey(MessageFormat.format(FRAME_DIALOG_OPTION_LABEL_RESOURCE_KEY_FORMAT_PATTERN, optionString));	//set the option action label
		button.setIconResourceKey(MessageFormat.format(FRAME_DIALOG_OPTION_ICON_RESOURCE_KEY_FORMAT_PATTERN, optionString));	//set the option action icon
		button.addActionListener(new ActionListener()	//listen for the action being performed
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action is performed
					{
						try
						{
							DefaultOptionDialogFrame.this.setValue(option);	//chose this option
							close();	//close the frame
						}
						catch(final ValidationException validationException)	//we don't expect a validation exception
						{
							throw new AssertionError(validationException);
						}	
					}
				});
		return button;	//return the created button
	}
	
}
