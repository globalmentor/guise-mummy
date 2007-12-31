package com.guiseframework.input;

import com.garretwilson.util.AbstractHashObject;

import static com.garretwilson.lang.Objects.*;

/**User input in the form of a command.
@author Garret Wilson
*/
public class CommandInput extends AbstractHashObject implements Input
{

	/**The command.*/
	private final Command command;

		/**The command.*/
		public Command getCommand() {return command;}

	/**Command constructor.
	@param command The command.
	@exception NullPointerException if the given command is <code>null</code>.
	*/
	public CommandInput(final Command command)
	{
		super(checkInstance(command, "Command cannot be null."));	//construct the parent class
		this.command=command;	//save the command
	}

}
