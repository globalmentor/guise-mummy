package com.javaguise.component;

import java.text.MessageFormat;
import static java.util.Arrays.*;
import java.util.Set;

import static com.garretwilson.util.SetUtilities.*;

import com.javaguise.event.ActionEvent;
import com.javaguise.event.ActionListener;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;

/**Default implementation of a frame for communication of an option such as "OK" or "Cancel".
An option frame defaults to a single composite child panel with a row of options along the bottom.
A center content component within the child panel may be specified.
@author Garret Wilson
*/
public class DefaultOptionDialogFrame extends AbstractOptionDialogFrame<DefaultOptionDialogFrame.Option, DefaultOptionDialogFrame>
{

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
	@param session The Guise session that owns this component.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final Option... options)
	{
		this(session, createEnumSet(Option.class, options));	//construct the component, creating a set of options
	}

	/**Session and options constructor.
	@param session The Guise session that owns this component.
	@param options The set of available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final Set<Option> options)
	{
		this(session, (String)null, options);	//construct the component, indicating that a default ID should be used
	}

	/**Session, component, and options constructor.
	@param session The Guise session that owns this component.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final Component<?> component, final Option... options)
	{
		this(session, component, createEnumSet(Option.class, options));	//construct the component, creating a set of options
	}

	/**Session, component, and options constructor.
	@param session The Guise session that owns this component.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The set of available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final Component<?> component, final Set<Option> options)
	{
		this(session, (String)null, component, options);	//construct the component, indicating that a default ID should be used
	}

	/**Session, model, and options constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final ValueModel<Option> model, final Option... options)
	{
		this(session, model, createEnumSet(Option.class, options));	//construct the component, creating a set of options
	}

	/**Session, model, and options constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param options The set of available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final ValueModel<Option> model, final Set<Option> options)
	{
		this(session, (String)null, model, options);	//construct the component, indicating that a default ID should be used
	}

	/**Session, model, component, and options constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final ValueModel<Option> model, final Component<?> component, final Option... options)
	{
		this(session, model, component, createEnumSet(Option.class, options));	//construct the component, creating a set of options
	}

	/**Session, model, component, and options constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The set of available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final ValueModel<Option> model, final Component<?> component, final Set<Option> options)
	{
		this(session, (String)null, model, component, options);	//construct the component, indicating that a default ID should be used
	}

	/**Session, ID, and options constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final Option... options)
	{
		this(session, id, createEnumSet(Option.class, options));	//construct the component, creating a set of options
	}

	/**Session, ID, and options constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param options The set of available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final Set<Option> options)
	{
		this(session, id, new DefaultValueModel<Option>(session, Option.class), options);	//use a default value model
	}

	/**Session, ID, component, and options constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The available options.
	@exception NullPointerException if the given session and/or optoins is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final Component<?> component, final Option... options)
	{
		this(session, id, component, createEnumSet(Option.class, options));	//construct the component, creating a set of options
	}

	/**Session, ID, component, and options constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@param options The set of available options.
	@exception NullPointerException if the given session and/or optoins is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final Component<?> component, final Set<Option> options)
	{
		this(session, id, new DefaultValueModel<Option>(session, Option.class), component, options);	//use a default value model
	}

	/**Session, ID, model, and options constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param options The available options.
	@exception NullPointerException if the given session, model, and or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final ValueModel<Option> model, final Option... options)
	{
		this(session, id, model, createEnumSet(Option.class, options));	//construct the component, creating a set of options
	}

	/**Session, ID, model, and options constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param options The set of available options.
	@exception NullPointerException if the given session, model, and or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final ValueModel<Option> model, final Set<Option> options)
	{
		this(session, id, model, null, options);	//default to no component
	}

	/**Session, ID, model, component, and options constructor.
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
		this(session, id, model, component, createEnumSet(Option.class, options));	//construct the component, creating a set of options
	}

	/**Session, ID, model, component, and options constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The component representing the content of the option dialog frame, or <code>null</code> if there is no content component.
	@param options The set of available options.
	@exception NullPointerException if the given session, model, and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultOptionDialogFrame(final GuiseSession session, final String id, final ValueModel<Option> model, final Component<?> component, final Set<Option> options)
	{
		super(session, id, model, component, options);	//construct the parent class
	}

	/**Initializes the option container with the available options.
	@param optionContainer The container to the options.
	@param options The available options.
	*/
	protected void initializeOptionContainer(final Container<?> optionContainer, final Set<Option> options)
	{
		final GuiseSession session=getSession();	//get the session
		final Option[] sortedOptions=options.toArray(new Option[options.size()]);	//convert the collection to an array
		sort(sortedOptions);	//sort the array
//TODO del		final Option[] sortedOptions=getSorted(options);	//sort the options
		for(final Option option:sortedOptions)	//for each option
		{
			final ActionModel actionModel=new DefaultActionModel(session);	//create an action model
			actionModel.setLabelResourceKey(MessageFormat.format("frame.dialog.options.{0}.label", option.toString()));	//set the option action label
			actionModel.addActionListener(new ActionListener<ActionModel>()	//listen for the action being performed
					{
						public void actionPerformed(ActionEvent<ActionModel> actionEvent)	//if the action is performed
						{
							try
							{
								DefaultOptionDialogFrame.this.getModel().setValue(option);	//chose this option
								close();	//close the frame
							}
							catch(final ValidationException validationException)	//we don't expect a validation exception
							{
								throw new AssertionError(validationException);
							}	
						}
					});
			final Button button=new Button(session, actionModel);	//create a new button
			optionContainer.add(button);	//add the button to the container
		}

	}

}
