package com.guiseframework.component;

import static com.globalmentor.java.ClassUtilities.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Set;

import com.guiseframework.geometry.CompassPoint;

/**A frame for flyovers.
A flyover frame by default is nonmodal, immovable, and not resizable.
When loading the tether image from the resources, a resource key will be generated based upon the compass point of the tether bearing.
For example, with a tether bearing of 250 and a tether resource key of "myTether", a resource key will be requested using "myTether.WSW",
	after which a resource key of "myTether" will be requested if that resource is not available.
<p>This implementation defaults to accepting tether bearings of:</p>
<ul>
	<li>{@link CompassPoint#NORTHEAST_BY_NORTH}</li>
	<li>{@link CompassPoint#NORTHEAST_BY_EAST}</li>
	<li>{@link CompassPoint#SOUTHEAST_BY_EAST}</li>
	<li>{@link CompassPoint#SOUTHEAST_BY_SOUTH}</li>
	<li>{@link CompassPoint#SOUTHWEST_BY_SOUTH}</li>
	<li>{@link CompassPoint#SOUTHWEST_BY_WEST}</li>
	<li>{@link CompassPoint#NORTHWEST_BY_WEST}</li>
	<li>{@link CompassPoint#NORTHWEST_BY_NORTH}</li>
</ul>
@author Garret Wilson
*/
public interface FlyoverFrame extends Frame
{

	/**The tether bearing bound property.*/
	public final static String TETHER_BEARING_PROPERTY=getPropertyName(FlyoverFrame.class, "tetherBearing");
	/**The tether bearing compass points bound property.*/
	public final static String TETHER_BEARING_COMPASS_POINTS_PROPERTY=getPropertyName(FlyoverFrame.class, "tetherBearingCompassPoints");
	/**The tether image bound property.*/
	public final static String TETHER_IMAGE_PROPERTY=getPropertyName(FlyoverFrame.class, "tetherImage");

	/**@return The bearing of the tether in relation to the frame.*/
	public BigDecimal getTetherBearing();

	/**Sets the bearing of the tether in relation to the frame.
	This is a bound property.
	@param newTetherBearing The new bearing of the tether in relation to the frame.
	@see #TETHER_BEARING_PROPERTY
	@exception NullPointerException if the given bearing is <code>null</code>.
	@exception IllegalArgumentException if the given bearing is greater than 360.
	*/
	public void setTetherBearing(final BigDecimal newTetherBearing);

	/**@return The bearing of the tether in relation to the frame.*/
	public Set<CompassPoint> getTetherBearingCompassPoints();

	/**Sets the compass points supported for tether bearing.
	This is a bound property.
	@param newTetherBearingCompassPoints The new set of compass points supported for tether bearing.
	@see #TETHER_BEARING_COMPASS_POINTS_PROPERTY
	@exception NullPointerException if the given set is <code>null</code>.
	*/
	public void setTetherBearingCompassPoints(final Set<CompassPoint> newTetherBearingCompassPoints);

	/**@return The tether image URI, which may be a resource URI, or <code>null</code> if there is no tether image URI.*/
	public URI getTetherImage();

	/**Sets the URI of the tether image.
	This is a bound property of type <code>URI</code>.
	@param newTetherImage The new URI of the image, which may be a resource URI.
	@see #TETHER_IMAGE_PROPERTY
	*/
	public void setTetherImage(final URI newTetherImage);
}
