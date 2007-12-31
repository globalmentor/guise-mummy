package com.guiseframework.component;

import java.math.BigDecimal;
import java.net.URI;
import java.util.EnumSet;
import java.util.Set;

import static java.util.Collections.*;

import com.garretwilson.lang.Objects;
import com.guiseframework.geometry.CompassPoint;

import static com.garretwilson.lang.Objects.*;
import static com.guiseframework.Resources.*;

/**Abstract implementation of a frame for flyovers.
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
public abstract class AbstractFlyoverFrame extends AbstractFrame implements FlyoverFrame
{
/**TODO del comment if no longer accurate
	For example, with a tether bearing of 250 and a tether resource key of "myTether", a resource key will be requested using "myTether.WSW", "myTether.SWbW", "myTether.SW", etc.
	until all relevant compass points are exhausted, after which a resource key of "myTether" will be requested.
*/

	/**The base resource URI for the flyover tether image URI.*/
	public final static URI TETHER_IMAGE_RESOURCE_URI=createURIResourceReference("theme.flyover.frame.tether.image");

	/**The bearing of the tether in relation to the frame.*/
	private BigDecimal tetherBearing=CompassPoint.NORTHWEST_BY_WEST.getBearing();

		/**@return The bearing of the tether in relation to the frame.*/
		public BigDecimal getTetherBearing() {return tetherBearing;}

		/**Sets the bearing of the tether in relation to the frame.
		This implementation changes the bearing to that of the nearest supported tether bearing compass point.
		This is a bound property.
		@param newTetherBearing The new bearing of the tether in relation to the frame.
		@see FlyoverFrame#TETHER_BEARING_PROPERTY
		@exception NullPointerException if the given bearing is <code>null</code>.
		@exception IllegalArgumentException if the given bearing is greater than 360.
		*/
		public void setTetherBearing(BigDecimal newTetherBearing)
		{
//TODO del Debug.trace("setting flyover tether bearing to", newTetherBearing);
			checkInstance(newTetherBearing, "Tether bearing cannot be null.");
			final Set<CompassPoint> tetherBearingCompassPoints=getTetherBearingCompassPoints();	//get the tether bearing compass points available
			if(!tetherBearingCompassPoints.isEmpty())	//if some tether bearings are supported TOOD fix; right now, if there are no compass points then all bearings are supported
			{
				CompassPoint compassPoint=CompassPoint.getCompassPoint(newTetherBearing);	//get the compass point nearest the bearing
				if(!tetherBearingCompassPoints.contains(compassPoint))	//if this is not a supported compass point, look for compass points lying near this point
				{
					final int ordinal=compassPoint.ordinal();	//get the ordinal of the compass point nearest the bearing
					final CompassPoint[] compassPoints=CompassPoint.values();	//get the compass point values
					final int compassPointCount=compassPoints.length;	//find out how many compass points there are
					final int maxDelta=compassPointCount/2;	//look half-way in each direction
					for(int delta=1; delta<maxDelta; ++delta)	//cycle through the deltas
					{
						final int previousOrdinal=ordinal-delta;	//look to the left
						compassPoint=compassPoints[previousOrdinal>=0 ? previousOrdinal : compassPointCount+previousOrdinal];	//get the previous compass point, wrapping around if necessary
						if(tetherBearingCompassPoints.contains(compassPoint))	//if we support the previous compass point
						{
							break;	//use this value
						}
						final int nextOrdinal=ordinal+delta;	//look to the right
						compassPoint=compassPoints[nextOrdinal<compassPointCount ? nextOrdinal : compassPointCount-nextOrdinal];	//get the next compass point, wrapping around if necessary
						if(tetherBearingCompassPoints.contains(compassPoint))	//if we support the next compass point
						{
							break;	//use this value
						}
					}
					newTetherBearing=compassPoint.getBearing();	//at this point we will have found a supported compass point; use that compass point's bearing
				}
//TODO del Debug.trace("deciding to really use compass point", compassPoint);
			}
			if(!tetherBearing.equals(newTetherBearing))	//if the value is really changing
			{
				final BigDecimal oldTetherBearing=tetherBearing;	//get the current value
				tetherBearing=CompassPoint.checkBearing(newTetherBearing);	//update the value
				firePropertyChange(TETHER_BEARING_PROPERTY, oldTetherBearing, newTetherBearing);
			}
		}

	/**The compass points supported for tether bearing.*/
	private Set<CompassPoint> tetherBearingCompassPoints;

		/**@return The bearing of the tether in relation to the frame.*/
		public Set<CompassPoint> getTetherBearingCompassPoints() {return tetherBearingCompassPoints;}

		/**Sets the compass points supported for tether bearing.
		This is a bound property.
		@param newTetherBearingCompassPoints The new set of compass points supported for tether bearing.
		@see FlyoverFrame#TETHER_BEARING_COMPASS_POINTS_PROPERTY
		@exception NullPointerException if the given set is <code>null</code>.
		*/
		public void setTetherBearingCompassPoints(final Set<CompassPoint> newTetherBearingCompassPoints)
		{
			if(tetherBearing!=tetherBearingCompassPoints)	//if the value is really changing
			{
				final Set<CompassPoint> oldTetherBearingCompassPoints=tetherBearingCompassPoints;	//get the current value
				tetherBearingCompassPoints=checkInstance(newTetherBearingCompassPoints, "Tether bearing compass points cannot be null.");	//update the value
				firePropertyChange(TETHER_BEARING_PROPERTY, oldTetherBearingCompassPoints, newTetherBearingCompassPoints);
			}
		}

	/**The tether image URI, which may be a resource URI, or <code>null</code> if there is no tether image URI.*/
	private URI tetherImage=TETHER_IMAGE_RESOURCE_URI;

		/**@return The tether image URI, which may be a resource URI, or <code>null</code> if there is no tether image URI.*/
		public URI getTetherImage() {return tetherImage;}

		/**Sets the URI of the tether image.
		This is a bound property of type <code>URI</code>.
		@param newTetherImage The new URI of the image, which may be a resource URI.
		@see FlyoverFrame#TETHER_IMAGE_PROPERTY
		*/
		public void setTetherImage(final URI newTetherImage)
		{
			if(!Objects.equals(tetherImage, newTetherImage))	//if the value is really changing
			{
				final URI oldTetherImage=tetherImage;	//get the old value
				tetherImage=newTetherImage;	//actually change the value
				firePropertyChange(TETHER_IMAGE_PROPERTY, oldTetherImage, newTetherImage);	//indicate that the value changed
			}			
		}

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	*/
	public AbstractFlyoverFrame(final Component component)
	{
		super(component);	//construct the parent class
		setModal(false);	//default to being a nonmodal frame
		setMovable(false);	//default to being nonmovable
		setResizable(false);	//default to not allowing resizing
		setTitleVisible(false);	//don't show a title
		tetherBearingCompassPoints=unmodifiableSet(EnumSet.of(	//establish the default accepted tether bearings
				CompassPoint.NORTHEAST_BY_NORTH,
				CompassPoint.NORTHEAST_BY_EAST,
				CompassPoint.SOUTHEAST_BY_EAST,
				CompassPoint.SOUTHEAST_BY_SOUTH,
				CompassPoint.SOUTHWEST_BY_SOUTH,
				CompassPoint.SOUTHWEST_BY_WEST,
				CompassPoint.NORTHWEST_BY_WEST,
				CompassPoint.NORTHWEST_BY_NORTH));
	}

}
