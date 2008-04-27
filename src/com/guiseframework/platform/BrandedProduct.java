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
