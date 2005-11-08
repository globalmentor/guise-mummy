package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.MissingResourceException;
import java.util.Set;

import com.javaguise.geometry.CompassPoint;

/**A frame for flyovers.
A flyover frame by default is nonmodal, immovable, and not resizable.
When loading the tether image from the resources, a resource key will be generated based upon the compass point of the tether bearing.
For example, with a tether bearing of 250 and a tether resource key of "myTether", a resource key will be requested using "myTether.WSW", "myTether.SWbW", "myTether.SW", etc.
	until all compass points are exhausted, after which a resource key of "myTether" will be requested.
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
public interface FlyoverFrame<C extends FlyoverFrame<C>> extends Frame<C>
{

	/**The tether bearing bound property.*/
	public final static String TETHER_BEARING_PROPERTY=getPropertyName(FlyoverFrame.class, "tetherBearing");
	/**The tether bearing compass points bound property.*/
	public final static String TETHER_BEARING_COMPASS_POINTS_PROPERTY=getPropertyName(FlyoverFrame.class, "tetherBearingCompassPoints");
	/**The tether image bound property.*/
	public final static String TETHER_IMAGE_PROPERTY=getPropertyName(FlyoverFrame.class, "tetherImage");
	/**The tether image resource key bound property.*/
	public final static String TETHER_IMAGE_RESOURCE_KEY_PROPERTY=getPropertyName(FlyoverFrame.class, "tetherImageResourceKey");

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

	/**Determines the URI of the tether image.
	If an image is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The tether image URI, or <code>null</code> if there is no tether image URI.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getTetherImageResourceKey()
	*/
	public URI getTetherImage() throws MissingResourceException;

	/**Sets the URI of the tether image.
	This is a bound property of type <code>URI</code>.
	@param newTetherImage The new URI of the image.
	@see #TETHER_IMAGE_PROPERTY
	*/
	public void setTetherImage(final URI newTetherImage);

	/**@return The tether image URI resource key, or <code>null</code> if there is no tether image URI resource specified.*/
	public String getTetherImageResourceKey();

	/**Sets the key identifying the URI of the tether image in the resources.
	This is a bound property.
	@param newTetherImageResourceKey The new image URI resource key.
	@see #TETHER_IMAGE_RESOURCE_KEY_PROPERTY
	*/
	public void setTetherImageResourceKey(final String newTetherImageResourceKey);

}
