package com.guiseframework.platform;

/**The identification of a product, such as a user agent or a plugin, on the platform.
@author Garret Wilson
*/
public interface Product
{

	/**@return The identifying string of the product, or <code>null</code> if the ID is not known.*/
	public String getID();

	/**@return The canonical name of the product, or <code>null</code> if the name is not known.*/
	public String getName();

	/**@return The version string provided by the product, or <code>null</code> if there is no string version of the product.*/
	public String getVersion();

	/**@return The version number provided by the product, or {@link Double#NaN} if there is no version number of the product.*/
	public double getVersionNumber();

	/**@return The version number components provided by the product, or <code>null</code> if there are no version number components of the product.*/
	public int[] getVersionNumbers();

}
