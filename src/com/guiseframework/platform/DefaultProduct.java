package com.guiseframework.platform;

/**The default implementation of a product with no brand.
@author Garret Wilson
*/
public class DefaultProduct extends AbstractProduct
{

	/**ID, name, and version constructor.
	@param id The identifying string of the product, or <code>null</code> if the ID is not known.
	@param name The canonical name of the product, or <code>null</code> if the name is not known.
	@param version The version string provided by the product, or <code>null</code> if there is no string version of the product.
	@param versionNumber The version number provided by the product, or {@link Double#NaN} if there is no version number of the product.
	@exception NullPointerException if the given ID and/or name is <code>null</code>.
	*/
	public DefaultProduct(final String id, final String name, final String version, final double versionNumber)
	{
		this(id, name, version, versionNumber, null);	//construct the class with no version numbers specified
	}

	/**ID, name, and version constructor.
	@param id The identifying string of the product, or <code>null</code> if the ID is not known.
	@param name The canonical name of the product, or <code>null</code> if the name is not known.
	@param version The version string provided by the product, or <code>null</code> if there is no string version of the product.
	@param versionNumber The version number provided by the product, or {@link Double#NaN} if there is no version number of the product.
	@param versionNumbers The version number components provided by the product, or <code>null</code> if there are no version number components of the product.
	@exception NullPointerException if the given ID and/or name is <code>null</code>.
	*/
	public DefaultProduct(final String id, final String name, final String version, final double versionNumber, final int[] versionNumbers)
	{
		super(id, name, version, versionNumber, versionNumbers);	//construct the parent class
	}

}
