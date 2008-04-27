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

package com.guiseframework.component;

import java.beans.PropertyVetoException;

import javax.mail.internet.ContentType;

import com.globalmentor.beans.*;
import com.guiseframework.event.*;
import com.guiseframework.input.*;
import com.guiseframework.model.*;
import com.guiseframework.model.Notification.Option;

/**Default implementation of a frame for communication of an option such as "OK" or "Cancel".
An option frame defaults to a single composite child panel with a row of options along the bottom.
The contents of an option dialog frame should be accessed by {@link #getOptionContent()} and {@link #setOptionContent(Component)}.
<p>This class binds the command {@link ProcessCommand#CONTINUE} to the button for the first non-fatal option.</p>
@author Garret Wilson
*/
public class NotificationOptionDialogFrame extends AbstractOptionDialogFrame<Notification.Option>
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
	public NotificationOptionDialogFrame(final Component component, final Option... options)
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
	public NotificationOptionDialogFrame(final ValueModel<Option> valueModel, final Component component, final Option... options)
	{
		super(valueModel, component, options);	//construct the parent class
	}

	/**Opens the frame as modal with a {@link Runnable} to be performed after modality ends successfully.
	When the frame closes, if a non-fatal option was chosen the given runnable is executed.
	This is a convenience method that delegates to {@link Frame#open(com.globalmentor.beans.GenericPropertyChangeListener)}.
	If the selected option to any notification is fatal, the specified logic, if any, will not be performed.
	The absence of an option selection is considered fatal only if a fatal option was presented for a given notification.  
	@param afterNotify The code that executes after notification has taken place, or <code>null</code> if no action should be taken after notification.
	@see Frame#open(com.globalmentor.beans.GenericPropertyChangeListener) 
	@see Option#isFatal()
	*/
	public void open(final Runnable afterNotify)
	{
		open(new AbstractGenericPropertyChangeListener<Frame.Mode>()	//open modally
	  		{
			  	public void propertyChange(final GenericPropertyChangeEvent<Frame.Mode> genericPropertyChangeEvent)	//if the frame modal state changes
			  	{
						if(genericPropertyChangeEvent.getNewValue()==null && afterNotify!=null)	//if the dialog is now nonmodal and there is logic that should take place after notification
						{
							final Notification.Option selectedOption=getValue();	//get the selected option, if any
								//we'll determine if the user selection is fatal and therefore we should not perform the given logic
							if(selectedOption!=null)	//if an option was selected
							{
								if(selectedOption.isFatal())	//if a fatal option was selected
								{
									return;	//don't perform the given logic
								}
							}
							else	//if no option was selected, determine if this should be considered fatal
							{
								for(final Notification.Option option:getOptions())	//look at the given options; if there is a fatal option available, consider the absence of an option selected to be fatal
								{
									if(option.isFatal())	//if a fatal option is available
									{
										return;	//don't perform the given logic										
									}
								}
							}
							afterNotify.run();	//run the code that takes place after notification
						}
			  	}
	  		});
	}

	/**Creates a component to represent the given option.
	This implementation creates a button for the given option.
	@param option The option for which a component should be created.
	*/
	protected Component createOptionComponent(final Option option)
	{
		if(bindingInputStrategy==null)	//if we haven't yet created our input strategy (we have to use lazy creation because this method is called from the parent constructor)
		{
			bindingInputStrategy=new BindingInputStrategy(getInputStrategy());	//create a new input strategy based upon the current input strategy (if any)
			setInputStrategy(bindingInputStrategy);	//switch to our new input strategy; we'll add bindings later
		}
		final Button button=new Button();	//create a new button
		button.setLabel(option.getLabel());	//set the option action label
		button.setGlyphURI(option.getGlyph());	//set the option action icon
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
