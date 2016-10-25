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

package com.guiseframework.style;

import com.guiseframework.Guise;
import com.guiseframework.GuiseSession;

import static com.globalmentor.java.Objects.*;

/**
 * A color that retrieves its values from the Guise session resources based upon a resource key. When color-related access methods are called the calling thread
 * must have access to the current Guise session.
 * @see GuiseSession#getColorResource(String)
 * @author Garret Wilson
 */
public class ResourceColor implements Color {

	/** The key to accessing the color defined in the resources. */
	private final String resourceKey;

	/** @return The key to accessing the color defined in the resources. */
	public String getResourceKey() {
		return resourceKey;
	}

	/**
	 * Resource key constructor.
	 * @param resourceKey The key to accessing the color defined in the resources.
	 * @throws NullPointerException if the given resource key is <code>null</code>.
	 */
	public ResourceColor(final String resourceKey) {
		this.resourceKey = checkInstance(resourceKey, "Resource key cannot be null.");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation retrieves a color from the Guise session resources and delegates to that object.
	 * </p>
	 */
	@Override
	public HSLColor asHSL() {
		return Guise.getInstance().getGuiseSession().getColorResource(getResourceKey()).asHSL(); //delegate to a color from the resources
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation retrieves a color from the Guise session resources and delegates to that object.
	 * </p>
	 */
	@Override
	public RGBColor asRGB() {
		return Guise.getInstance().getGuiseSession().getColorResource(getResourceKey()).asRGB(); //delegate to a color from the resources
	}

	@Override
	public int hashCode() {
		return getResourceKey().hashCode(); //calculate a hash code from the resource key
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns whether the object is another resource color with the same resource key.
	 * </p>
	 * @see #getResourceKey()
	 */
	@Override
	public boolean equals(final Object object) {
		return object instanceof ResourceColor && ((ResourceColor)object).getResourceKey().equals(getResourceKey()); //see if the object is a resource color with the same resource key
	}

}
