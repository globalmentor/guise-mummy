package com.guiseframework.platform;

/**The default implementation of the identification of a product, such as a user agent or a plugin, on a particular platform.
@author Garret Wilson
*/
public class AbstractProduct implements Product
{

	/**The identifying string of the product.*/
	private final String id;

		/**@return The identifying string of the product.*/
		public String getID() {return id;}

	/**The canonical name of the product.*/
	private final String name;

		/**@return The canonical name of the product.*/
		public String getName() {return name;}

	/**The version string provided by the product, or <code>null</code> if there is no string version of the product.*/
	private final String version;

		/**@return The version string provided by the product, or <code>null</code> if there is no string version of the product.*/
		public String getVersion() {return version;}

	/**The version number provided by the product, or {@link Double#NaN} if there is no version number of the product.*/
	private final double versionNumber;

		/**@return The version number provided by the product, or {@link Double#NaN} if there is no version number of the product.*/
		public double getVersionNumber() {return versionNumber;}

	/**The version number components provided by the product, or <code>null</code> if there are no version number components of the product.*/
	private final int[] versionNumbers;

		/**@return The version number components provided by the product, or <code>null</code> if there are no version number components of the product.*/
		public int[] getVersionNumbers() {return versionNumbers;}

	/**ID, name, and version constructor.
	@param id The identifying string of the product, or <code>null</code> if the ID is not known.
	@param name The canonical name of the product, or <code>null</code> if the name is not known.
	@param version The version string provided by the product, or <code>null</code> if there is no string version of the product.
	@param versionNumber The version number provided by the product, or {@link Double#NaN} if there is no version number of the product.
	@param versionNumbers The version number components provided by the product, or <code>null</code> if there are no version number components of the product.
	*/
	public AbstractProduct(final String id, final String name, final String version, final double versionNumber, final int[] versionNumbers)
	{
		this.id=id;
		this.name=name;
		this.version=version;
		this.versionNumber=versionNumber;
		this.versionNumbers=versionNumbers;
	}

}
