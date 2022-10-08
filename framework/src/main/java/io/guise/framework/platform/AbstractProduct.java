/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform;

/**
 * The default implementation of the identification of a product, such as a user agent or a plugin, on a particular platform.
 * @author Garret Wilson
 */
public class AbstractProduct implements Product {

	/** The identifying string of the product. */
	private final String id;

	@Override
	public String getID() {
		return id;
	}

	/** The canonical name of the product. */
	private final String name;

	@Override
	public String getName() {
		return name;
	}

	/** The version string provided by the product, or <code>null</code> if there is no string version of the product. */
	private final String version;

	@Override
	public String getVersion() {
		return version;
	}

	/** The version number provided by the product, or {@link Double#NaN} if there is no version number of the product. */
	private final double versionNumber;

	@Override
	public double getVersionNumber() {
		return versionNumber;
	}

	/** The version number components provided by the product, or <code>null</code> if there are no version number components of the product. */
	private final int[] versionNumbers;

	@Override
	public int[] getVersionNumbers() {
		return versionNumbers;
	}

	/**
	 * ID, name, and version constructor.
	 * @param id The identifying string of the product, or <code>null</code> if the ID is not known.
	 * @param name The canonical name of the product, or <code>null</code> if the name is not known.
	 * @param version The version string provided by the product, or <code>null</code> if there is no string version of the product.
	 * @param versionNumber The version number provided by the product, or {@link Double#NaN} if there is no version number of the product.
	 * @param versionNumbers The version number components provided by the product, or <code>null</code> if there are no version number components of the product.
	 */
	public AbstractProduct(final String id, final String name, final String version, final double versionNumber, final int[] versionNumbers) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.versionNumber = versionNumber;
		this.versionNumbers = versionNumbers;
	}

}
