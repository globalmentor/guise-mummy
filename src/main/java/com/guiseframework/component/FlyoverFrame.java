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

package com.guiseframework.component;

import static com.globalmentor.java.Classes.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Set;

import com.guiseframework.geometry.CompassPoint;

/**
 * A frame for flyovers. A flyover frame by default is nonmodal, immovable, and not resizable. When loading the tether image from the resources, a resource key
 * will be generated based upon the compass point of the tether bearing. For example, with a tether bearing of 250 and a tether resource key of "myTether", a
 * resource key will be requested using "myTether.WSW", after which a resource key of "myTether" will be requested if that resource is not available.
 * <p>
 * This implementation defaults to accepting tether bearings of:
 * </p>
 * <ul>
 * <li>{@link CompassPoint#NORTHEAST_BY_NORTH}</li>
 * <li>{@link CompassPoint#NORTHEAST_BY_EAST}</li>
 * <li>{@link CompassPoint#SOUTHEAST_BY_EAST}</li>
 * <li>{@link CompassPoint#SOUTHEAST_BY_SOUTH}</li>
 * <li>{@link CompassPoint#SOUTHWEST_BY_SOUTH}</li>
 * <li>{@link CompassPoint#SOUTHWEST_BY_WEST}</li>
 * <li>{@link CompassPoint#NORTHWEST_BY_WEST}</li>
 * <li>{@link CompassPoint#NORTHWEST_BY_NORTH}</li>
 * </ul>
 * @author Garret Wilson
 */
public interface FlyoverFrame extends Frame {

	/** The tether bearing bound property. */
	public static final String TETHER_BEARING_PROPERTY = getPropertyName(FlyoverFrame.class, "tetherBearing");
	/** The tether bearing compass points bound property. */
	public static final String TETHER_BEARING_COMPASS_POINTS_PROPERTY = getPropertyName(FlyoverFrame.class, "tetherBearingCompassPoints");
	/** The tether image bound property. */
	public static final String TETHER_IMAGE_PROPERTY = getPropertyName(FlyoverFrame.class, "tetherImage");

	/** @return The bearing of the tether in relation to the frame. */
	public BigDecimal getTetherBearing();

	/**
	 * Sets the bearing of the tether in relation to the frame. This is a bound property.
	 * @param newTetherBearing The new bearing of the tether in relation to the frame.
	 * @see #TETHER_BEARING_PROPERTY
	 * @throws NullPointerException if the given bearing is <code>null</code>.
	 * @throws IllegalArgumentException if the given bearing is greater than 360.
	 */
	public void setTetherBearing(final BigDecimal newTetherBearing);

	/** @return The bearing of the tether in relation to the frame. */
	public Set<CompassPoint> getTetherBearingCompassPoints();

	/**
	 * Sets the compass points supported for tether bearing. This is a bound property.
	 * @param newTetherBearingCompassPoints The new set of compass points supported for tether bearing.
	 * @see #TETHER_BEARING_COMPASS_POINTS_PROPERTY
	 * @throws NullPointerException if the given set is <code>null</code>.
	 */
	public void setTetherBearingCompassPoints(final Set<CompassPoint> newTetherBearingCompassPoints);

	/** @return The tether image URI, which may be a resource URI, or <code>null</code> if there is no tether image URI. */
	public URI getTetherImage();

	/**
	 * Sets the URI of the tether image. This is a bound property of type <code>URI</code>.
	 * @param newTetherImage The new URI of the image, which may be a resource URI.
	 * @see #TETHER_IMAGE_PROPERTY
	 */
	public void setTetherImage(final URI newTetherImage);
}
