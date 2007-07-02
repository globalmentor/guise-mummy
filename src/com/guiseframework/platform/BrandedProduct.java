package com.guiseframework.platform;

/**The identification of a branded product, such as a user agent or a plugin, on the platform.
@author Garret Wilson
*/
public interface BrandedProduct<BRAND extends Enum<BRAND> & BrandedProduct.Brand>
{

	/**The individual brand of the product.*/
	public interface Brand
	{
	}

	/**@return The brand of the product, or <code>null</code> if the brand is not known.*/
	public BRAND getBrand();

}
