package com.guiseframework.platform.web;

import java.util.*;
import static java.util.Collections.*;

import static com.garretwilson.lang.Objects.*;
import com.garretwilson.util.NameValuePair;
import static com.garretwilson.util.MapUtilities.*;

/**A command message to or from the web platform.
@param <C> The type of command.
@author Garret Wilson
*/
public class WebCommandMessage<C extends Enum<C> & WebPlatformCommand> extends AbstractWebMessage implements WebPlatformCommandMessage<C>
{

	/**The command.*/
	private final WebPlatformCommand command;

		/**@return The command.*/
		@SuppressWarnings("unchecked")
		public C getCommand() {return (C)command;}

	/**The read-only map of parameters, which will be encoded in JavaScript Object Notation (JSON).*/
	private final Map<String, Object> parameters;

		/**@return The read-only map of parameters, which will be encoded in JavaScript Object Notation (JSON).*/
		public Map<String, Object> getParameters() {return parameters;}

	/**Command and parameters constructor.
	@param command The command.
	@param parameters The parameters of the command; parameters with duplicate names replace earlier parameters of the same name.
	@exception NullPointerException if the given command and/or parameters is <code>null</code>.
	*/
	public WebCommandMessage(final C command, final NameValuePair<String, Object>... parameters)
	{
		this.command=checkInstance(command, "Command cannot be null.");
		this.parameters=unmodifiableMap(addAll(new HashMap<String, Object>(parameters.length), parameters));	//add all the parameters to a new map
	}

	/**Command and parameters map constructor.
	@param command The command.
	@param parameters The map representing the parameters of the command.
	@exception NullPointerException if the given command and/or parameters is <code>null</code>.
	*/
	public WebCommandMessage(final C command, final Map<String, Object> parameters)
	{
		this.command=checkInstance(command, "Command cannot be null.");
		this.parameters=unmodifiableMap(new HashMap<String, Object>(checkInstance(parameters, "Parameters cannot be null.")));
	}

}
