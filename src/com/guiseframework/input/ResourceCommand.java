package com.guiseframework.input;

/**Commands for working with resources.
@author Garret Wilson
*/
public enum ResourceCommand implements Command
{

	/**The command for editing a resource.*/
	EDIT,
	/**The command deleting a resource. This command is usually bound to the Delete key.*/
	DELETE;

}
