package com.guiseframework.event;

/**An object that listens for commands.
@author Garret Wilson
*/
public interface CommandListener extends GuiseEventListener
{

	/**Called when a command is invoked.
	@param commandEvent The event providing command information
	*/
	public void commanded(final CommandEvent commandEvent);

}
