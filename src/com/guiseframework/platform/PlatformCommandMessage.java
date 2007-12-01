package com.guiseframework.platform;

/**A command message to or from the platform on which objects are being depicted.
@param <C> The type of command.
@author Garret Wilson
*/
public interface PlatformCommandMessage<C extends Enum<C> & PlatformCommand> extends PlatformMessage
{

	/**@return The command.*/
	public C getCommand();

}
