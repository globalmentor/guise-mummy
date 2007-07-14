package com.guiseframework.platform;

/**A local file on a platform.
@author Garret Wilson
*/
public interface PlatformFile
{

	/**@return The name of the file.*/
	public String getName();
	/**@return The size of the file, or -1 if the size is unknown.*/
	public long getSize();

}
