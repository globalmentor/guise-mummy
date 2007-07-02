package com.guiseframework.platform;

/**The default implementation of the identification of a branded product, such as a user agent or a plugin, on a particular platform.
@author Garret Wilson
*/
public class AbstractBrandedProduct<BRAND extends Enum<BRAND> & BrandedProduct.Brand> extends AbstractProduct implements BrandedProduct<BRAND>
{

	/**The brand of the product, or <code>null</code> if the brand is not known.*/
	private final BRAND brand;

		/**@return The brand of the product.*/
		public BRAND getBrand() {return brand;}

	/**ID, brand, name, and version constructor.
	@param id The identifying string of the product, or <code>null</code> if the ID is not known.
	@param brand The brand of the product, or <code>null</code> if the brand is not known.
	@param name The canonical name of the product, or <code>null</code> if the name is not known.
	@param version The version string provided by the product, or <code>null</code> if there is no string version of the product.
	@param versionNumber The version number provided by the product, or {@link Double#NaN} if there is no version number of the product.
	@param versionNumbers The version number components provided by the product, or <code>null</code> if there are no version number components of the product.
	*/
	public AbstractBrandedProduct(final String id, final BRAND brand, final String name, final String version, final double versionNumber, final int[] versionNumbers)
	{
		super(id, name, version, versionNumber, versionNumbers);	//construct the parent class
		this.brand=brand;	//save the brand
	}

}
