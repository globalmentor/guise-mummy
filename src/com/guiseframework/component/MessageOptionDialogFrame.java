package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.geometry.Extent;

/**An option dialog frame displaying a message.
@author Garret Wilson
*/
public class MessageOptionDialogFrame extends DefaultOptionDialogFrame
{

	/**The message displayed in the frame.*/
	private final Message message;

		/**@return The message displayed in the frame.*/
		public Message getMessage() {return message;}

	/**Session and options constructor with no message.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public MessageOptionDialogFrame(final GuiseSession session, final Option... options)
	{
		this(session, (String)null, (String)null, options);	//construct the component, indicating that a default ID should be used				
	}

	/**Session, ID, and options constructor with no message.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
/*TODO del
	public MessageOptionDialogFrame(final GuiseSession session, final String id, final Option... options)
	{
		this(session, null, options);	//construct the component with no message
	}
*/

	/**Session, message, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param message The message to display in the frame, or <code>null</code> if no message should be displayed.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	*/
	public MessageOptionDialogFrame(final GuiseSession session, final String message, final Option... options)
	{
		this(session, null, message, options);	//construct the component, indicating that a default ID should be used		
	}

	/**Session, ID, message, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param message The message to display in the frame, or <code>null</code> if no message should be displayed.
	@param options The available options.
	@exception NullPointerException if the given session and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public MessageOptionDialogFrame(final GuiseSession session, final String id, final String message, final Option... options)
	{
		this(session, id, createMessage(session, message), options);	//construct the component with a default message model
	}

	/**Session, message model, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param messageModel The model of the message to display in the frame.
	@param options The available options.
	@exception NullPointerException if the given session, message model, and/or options is <code>null</code>.
	*/
/*TODO del or fix
	public MessageOptionDialogFrame(final GuiseSession session, final MessageModel messageModel, final Option... options)
	{
		this(session, null, messageModel, options);	//construct the component, indicating that a default ID should be used
	}
*/
	
	/**Session, ID, message model, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param messageModel The model of the message to display in the frame.
	@param options The available options.
	@exception NullPointerException if the given session, message model, and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
/*TODO fix or del
	public MessageOptionDialogFrame(final GuiseSession session, final String id, final MessageModel messageModel, final Option... options)
	{
		this(session, id, new Message(session, messageModel), options);	//construct the class with a message component
	}
*/

	/**Session, message component, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param messageComponent The message component to display in the frame.
	@param options The available options.
	@exception NullPointerException if the given session, message component, and/or options is <code>null</code>.
	*/
	public MessageOptionDialogFrame(final GuiseSession session, final Message messageComponent, final Option... options)
	{
		this(session, null, messageComponent, options);	//construct the component, indicating that a default ID should be used
	}

	/**Session, ID, message component, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param messageComponent The message component to display in the frame.
	@param options The available options.
	@exception NullPointerException if the given session, message component, and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public MessageOptionDialogFrame(final GuiseSession session, final String id, final Message messageComponent, final Option... options)
	{
		super(session, id, messageComponent, options);	//construct the parent class
		this.message=checkNull(messageComponent, "Message component cannot be null");
		setPreferredWidth(new Extent(25, Extent.Unit.EM));	//set the default preferred size
		setPreferredHeight(new Extent(10, Extent.Unit.EM));
	}

	/**Creates a message component with a message.
	@param session The Guise session that owns this component.
	@param messageText The text of the message component.
	@return A new message component with the given message text.
	*/
	protected static Message createMessage(final GuiseSession session, final String messageText)
	{
		final Message message=new Message(session);	//create the message component
		message.setMessage(messageText);	//set the message text
		return message;	//return the message component
	}
}
