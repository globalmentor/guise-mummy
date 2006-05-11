package com.guiseframework.component;

import java.beans.PropertyVetoException;
import java.text.MessageFormat;

import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import com.guiseframework.event.ActionEvent;
import com.guiseframework.event.ActionListener;
import com.guiseframework.model.*;

/**Default implementation of a frame for communication of an option such as "OK" or "Cancel".
An option frame defaults to a single composite child panel with a row of options along the bottom.
The contents of an option dialog frame should be accessed by {@link #getOptionContent()} and {@link #setOptionContent(Component)}.
The labels and icons are accessed from the resources using resource keys
	<code>frame.dialog.options.<var>option</var>.label</code> and <code>frame.dialog.options.<var>option</var>.icon</code>,
	respectively, where <var>option</var> represents the option enum value such as "OK".
@author Garret Wilson
*/
public class DefaultOptionDialogFrame extends AbstractOptionDialogFrame<DefaultOptionDialogFrame.Option, DefaultOptionDialogFrame>
{

	/**The resource key format pattern for each option label.*/
	public final static String OPTION_DIALOG_FRAME_LABEL_RESOURCE_KEY_FORMAT_PATTERN="theme.option.dialog.frame.option.{0}.label";
	/**The resource key format pattern for each option icon.*/
	public final static String OPTION_DIALOG_FRAME_ICON_RESOURCE_KEY_FORMAT_PATTERN="theme.option.dialog.frame.option.{0}.icon";
	
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
		REMOVE,
		CANCEL
	}

	/**Options constructor.
	Duplicate options are ignored.
	@param options The available options.
	@exception NullPointerException if the given options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final Option... options)
	{
		this(new DefaultValueModel<Option>(Option.class), options);	//use a default value model
	}

	/**Component and options constructor.
	Duplicate options are ignored.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The available options.
	@exception NullPointerException if the given options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final Component<?> component, final Option... options)
	{
		this(new DefaultValueModel<Option>(Option.class), component, options);	//use a default value model
	}

	/**Value model, and options constructor.
	Duplicate options are ignored.
	@param valueModel The frame value  model.
	@param options The available options.
	@exception NullPointerException if the given value model and or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final ValueModel<Option> valueModel, final Option... options)
	{
		this(valueModel, null, options);	//default to no component
	}

	/**Value model, component, and options constructor.
	Duplicate options are ignored.
	@param valueModel The frame value model.
	@param component The component representing the content of the option dialog frame, or <code>null</code> if there is no content component.
	@param options The available options.
	@exception NullPointerException if the given value model and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final ValueModel<Option> valueModel, final Component<?> component, final Option... options)
	{
		super(valueModel, component, options);	//construct the parent class
	}

	/**Creates a component to represent the given option.
	This implementation creates a button for the given option.
	@param option The option for which a component should be created.
	*/
	protected Component<?> createOptionComponent(final Option option)
	{
		final Button button=new Button();	//create a new button
		final String optionString=option.toString();	//get the string form of the option
		button.setLabel(getSession().createStringResourceReference(MessageFormat.format(OPTION_DIALOG_FRAME_LABEL_RESOURCE_KEY_FORMAT_PATTERN, optionString)));	//set the option action label
		button.setIcon(createURI(RESOURCE_SCHEME, MessageFormat.format(OPTION_DIALOG_FRAME_ICON_RESOURCE_KEY_FORMAT_PATTERN, optionString)));	//set the option action icon
		button.addActionListener(new ActionListener()	//listen for the action being performed
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action is performed
					{
						try
						{
							DefaultOptionDialogFrame.this.setValue(option);	//chose this option
							close();	//close the frame
						}
						catch(final PropertyVetoException propertyVetoException)	//we don't expect a validation exception
						{
//TODO fix							throw new AssertionError(validationException);
						}	
					}
				});
		return button;	//return the created button
	}
	
}
