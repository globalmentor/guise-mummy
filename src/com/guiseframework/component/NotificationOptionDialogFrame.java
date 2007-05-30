package com.guiseframework.component;

import java.beans.PropertyVetoException;

import javax.mail.internet.ContentType;

import com.guiseframework.event.*;
import com.guiseframework.input.BindingInputStrategy;
import com.guiseframework.input.CommandInput;
import com.guiseframework.input.ProcessCommand;
import com.guiseframework.model.*;
import com.guiseframework.model.Notification.Option;

/**Default implementation of a frame for communication of an option such as "OK" or "Cancel".
An option frame defaults to a single composite child panel with a row of options along the bottom.
The contents of an option dialog frame should be accessed by {@link #getOptionContent()} and {@link #setOptionContent(Component)}.
<p>This class binds the command {@link ProcessCommand#CONTINUE} to the button for the first non-fatal option.</p>
@author Garret Wilson
*/
public class NotificationOptionDialogFrame extends AbstractOptionDialogFrame<Notification.Option, NotificationOptionDialogFrame>
{

	/**Our input strategy for mapping the "continue" command to the first non-fatal option. As the options are created in the parent contructor, we'll have to lazily create this instance.*/
	private BindingInputStrategy bindingInputStrategy=null;

	/**Options constructor.
	Duplicate options are ignored.
	@param options The available options.
	@exception NullPointerException if the given options is <code>null</code>.
	*/
	public NotificationOptionDialogFrame(final Option... options)
	{
		this(new DefaultValueModel<Option>(Option.class), options);	//use a default value model
	}

	/**Notification constructor.
	Duplicate options are ignored.
	@param notification The notification that specifies the message and options.
	@exception NullPointerException if the given notification is <code>null</code>.
	#see {@link Text}
	*/
	public NotificationOptionDialogFrame(final Notification notification)
	{
		this(notification.getMessage(), notification.getMessageContentType(), notification.getOptions().toArray(new Option[notification.getOptions().size()]));	//create a dialog from the contents of the notification
	}
	
	/**Text constructor with a default {@link Model#PLAIN_TEXT_CONTENT_TYPE} content type.
	Duplicate options are ignored.
	@param text The text, which may include a resource reference, or <code>null</code> if there is no text.
	@param options The available options.
	@exception NullPointerException if the given options is <code>null</code>.
	#see {@link Text}
	*/
	public NotificationOptionDialogFrame(final String text, final Option... options)
	{
		this(text, PLAIN_TEXT_CONTENT_TYPE, options);	//construct the class with a plain text content type
	}

	/**Text and content type constructor
	Duplicate options are ignored.
	@param text The text, which may include a resource reference, or <code>null</code> if there is no text.
	@param textContentType The content type of the text.
	@param options The available options.
	@exception NullPointerException if the given content type and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	#see {@link Text}
	*/
	public NotificationOptionDialogFrame(final String text, final ContentType textContentType, final Option... options)
	{
		this(new Text(text, textContentType), options);	//create a dialog using a Text component created from the given text and content type
	}	
	
	/**Component and options constructor.
	Duplicate options are ignored.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The available options.
	@exception NullPointerException if the given options is <code>null</code>.
	*/
	public NotificationOptionDialogFrame(final Component<?> component, final Option... options)
	{
		this(new DefaultValueModel<Option>(Option.class), component, options);	//use a default value model
	}

	/**Value model, and options constructor.
	Duplicate options are ignored.
	@param valueModel The frame value  model.
	@param options The available options.
	@exception NullPointerException if the given value model and or options is <code>null</code>.
	*/
	public NotificationOptionDialogFrame(final ValueModel<Option> valueModel, final Option... options)
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
	public NotificationOptionDialogFrame(final ValueModel<Option> valueModel, final Component<?> component, final Option... options)
	{
		super(valueModel, component, options);	//construct the parent class
	}

	/**Creates a component to represent the given option.
	This implementation creates a button for the given option.
	@param option The option for which a component should be created.
	*/
	protected Component<?> createOptionComponent(final Option option)
	{
		if(bindingInputStrategy==null)	//if we haven't yet created our input strategy (we have to use lazy creation because this method is called from the parent constructor)
		{
			bindingInputStrategy=new BindingInputStrategy(getInputStrategy());	//create a new input strategy based upon the current input strategy (if any)
			setInputStrategy(bindingInputStrategy);	//switch to our new input strategy; we'll add bindings later
		}
		final Button button=new Button();	//create a new button
		button.setLabel(option.getLabel());	//set the option action label
		button.setIcon(option.getGlyph());	//set the option action icon
		button.addActionListener(new ActionListener()	//listen for the action being performed
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action is performed
					{
						try
						{
							if(option.isFatal() || validate())	//if this is not a fatal option, first see if the frame validates
							{
								NotificationOptionDialogFrame.this.setValue(option);	//chose this option
								close();	//close the frame
							}
						}
						catch(final PropertyVetoException propertyVetoException)	//we don't expect a validation exception
						{
//TODO fix							throw new AssertionError(validationException);
						}	
					}
				});

		final CommandInput continueCommandInput=new CommandInput(ProcessCommand.CONTINUE);	//create a command for continue
		if(!bindingInputStrategy.isBound(continueCommandInput))	//if the continue command isn't yet bound to anything
		{
			if(!option.isFatal())	//if this is not a fatal option
			{
				bindingInputStrategy.bind(continueCommandInput, button);	//map the "continue" command to the option button
			}
		}
		return button;	//return the created button
	}


}
