package com.guiseframework.input;

/**Predefined commands for reacting to a decision to be made during a process.
@author Garret Wilson
*/
public enum ProcessCommand implements Command
{

	/**The predefined command for continuing a process. This command is usually bound to the Enter key and signifies that some step in a process should be approved.*/
	CONTINUE,
	/**The predefined command for aborting a process. This command is usually bound to the Escape key and signifies that the process should be canceled.*/
	ABORT;

}
