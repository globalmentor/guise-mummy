package com.guiseframework.model;

/**Levels of logged or reported information.
The first two levels, {@link #TRACE} and {@link #INFO}, are debug-only log levels; their information is meant for system analysis use, and are usually relegated to the debug log file.
@author Garret Wilson
*/
public enum InformationLevel
{
	/**Indicates the program's execution path.*/
	TRACE,
	/**Indicates useful information that should nonetheless not be logged.*/
	INFO,
	/**Specific information which should be logged but which are adversity-neutral.*/
	LOG,
	/**Information used for tracking actions for later reports.*/
	TRACK,
	/**Indications that conditions are possibly adverse.*/
	WARN,
	/**Indicates an unexpected condition representing an error.*/
	ERROR;

	/**Whether this is a debug information level.*/
//TODO del	private final boolean debug;

		/**Whether this is a debug information level.*/
//TODO del		private final boolean debug;

//TODO del	private InformationLevel(final boolean )
};
