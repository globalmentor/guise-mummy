package com.guiseframework.platform;

import static com.garretwilson.lang.ObjectUtilities.*;

/**An abstract implementation of a local file on a platform.
@author Garret Wilson
*/
public abstract class AbstractPlatformFile implements PlatformFile
{

	/**The name of the file.*/
	private final String name;

		/**@return The name of the file.*/
		public String getName() {return name;}

	/**The size of the file, or -1 if the size is unknown.*/
	private final long size;

		/**@return The size of the file, or -1 if the size is unknown.*/
		public long getSize() {return size;}

	/**Name and size constructor.
	@param name The name of the file.
	@param size The size of the file, or -1 if the size is unknown.
	@exception NullPointerException if the given name is <code>null</code>.
	*/
	public AbstractPlatformFile(final String name, final long size)
	{
		this.name=checkInstance(name, "Name cannot be null.");
		this.size=size;
	}

	/**@return A string representation of this platform file.*/
	public String toString()
	{
		return getName();	//return the name of the file as a representation
	}
}
