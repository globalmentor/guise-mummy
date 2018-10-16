/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.platform;

/**
 * The default implementation of the identification of a branded product, such as a user agent or a plugin, on a particular platform.
 * @author Garret Wilson
 */
public class AbstractBrandedProduct<BRAND extends Enum<BRAND> & BrandedProduct.Brand> extends AbstractProduct implements BrandedProduct<BRAND> {

	/** The brand of the product, or <code>null</code> if the brand is not known. */
	private final BRAND brand;

	@Override
	public BRAND getBrand() {
		return brand;
	}

	/**
	 * ID, brand, name, and version constructor.
	 * @param id The identifying string of the product, or <code>null</code> if the ID is not known.
	 * @param brand The brand of the product, or <code>null</code> if the brand is not known.
	 * @param name The canonical name of the product, or <code>null</code> if the name is not known.
	 * @param version The version string provided by the product, or <code>null</code> if there is no string version of the product.
	 * @param versionNumber The version number provided by the product, or {@link Double#NaN} if there is no version number of the product.
	 * @param versionNumbers The version number components provided by the product, or <code>null</code> if there are no version number components of the product.
	 */
	public AbstractBrandedProduct(final String id, final BRAND brand, final String name, final String version, final double versionNumber,
			final int[] versionNumbers) {
		super(id, name, version, versionNumber, versionNumbers); //construct the parent class
		this.brand = brand; //save the brand
	}

	@Override
	public boolean isBrandVersionNumber(final BRAND brand, final double versionNumber) {
		return getBrand() == brand && getVersionNumber() == versionNumber; //return whether we have the requested brand and version
	}

	@Override
	public boolean isBrandLessThanVersionNumber(final BRAND brand, final double versionNumber) {
		return getBrand() == brand && getVersionNumber() < versionNumber; //return whether we have the requested brand and smaller version
	}
}
