package com.javaguise.component;

import java.math.BigDecimal;
import java.net.URI;
import java.util.MissingResourceException;
import java.util.Set;

import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.Debug;

import static com.garretwilson.util.SetUtilities.*;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.GuiseSession;
import com.javaguise.geometry.CompassPoint;
import com.javaguise.model.LabelModel;

/**Abstract implementation of a frame for flyovers.
A flyover frame by default is nonmodal, immovable, and not resizable.
For example, with a tether bearing of 250 and a tether resource key of "myTether", a resource key will be requested using "myTether.WSW", "myTether.SWbW", "myTether.SW", etc.
	until all relevant compass points are exhausted, after which a resource key of "myTether" will be requested.
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
public abstract class AbstractFlyoverFrame<C extends FlyoverFrame<C>> extends AbstractFrame<C> implements FlyoverFrame<C>
{

	/**The base resource bundle key for the flyover tether image URI.*/
	public final static String TETHER_IMAGE_RESOURCE_KEY="flyover.frame.tether.image";

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
			checkNull(newTetherBearing, "Tether bearing cannot be null.");
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
				tetherBearingCompassPoints=checkNull(newTetherBearingCompassPoints, "Tether bearing compass points cannot be null.");	//update the value
				firePropertyChange(TETHER_BEARING_PROPERTY, oldTetherBearingCompassPoints, newTetherBearingCompassPoints);
			}
		}
		
	/**The tether image URI, or <code>null</code> if there is no tether image URI.*/
	private URI tetherImage=null;

		/**Determines the URI of the tether image.
		If an image is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
		@return The tether image URI, or <code>null</code> if there is no tether image URI.
		@exception MissingResourceException if there was an error loading the value from the resources.
		@see #getTetherImageResourceKey()
		*/
		public URI getTetherImage() throws MissingResourceException
		{
			return getURI(tetherImage, getTetherImageResourceKey(), getTetherBearing());	//get the value or the resource, if available, taking the tether bearing into account
		}

		/**Sets the URI of the tether image.
		This is a bound property of type <code>URI</code>.
		@param newTetherImage The new URI of the image.
		@see FlyoverFrame#TETHER_IMAGE_PROPERTY
		*/
		public void setTetherImage(final URI newTetherImage)
		{
			if(!ObjectUtilities.equals(tetherImage, newTetherImage))	//if the value is really changing
			{
				final URI oldTetherImage=tetherImage;	//get the old value
				tetherImage=newTetherImage;	//actually change the value
				firePropertyChange(TETHER_IMAGE_PROPERTY, oldTetherImage, newTetherImage);	//indicate that the value changed
			}			
		}

	/**The tether image URI resource key, or <code>null</code> if there is no tether image URI resource specified.*/
	private String tetherImageResourceKey=TETHER_IMAGE_RESOURCE_KEY;

		/**@return The tether image URI resource key, or <code>null</code> if there is no tether image URI resource specified.*/
		public String getTetherImageResourceKey() {return tetherImageResourceKey;}

		/**Sets the key identifying the URI of the tether image in the resources.
		This is a bound property.
		@param newTetherImageResourceKey The new image URI resource key.
		@see FlyoverFrame#TETHER_IMAGE_RESOURCE_KEY_PROPERTY
		*/
		public void setTetherImageResourceKey(final String newTetherImageResourceKey)
		{
			if(!ObjectUtilities.equals(tetherImageResourceKey, newTetherImageResourceKey))	//if the value is really changing
			{
				final String oldTetherImageResourceKey=tetherImageResourceKey;	//get the old value
				tetherImageResourceKey=newTetherImageResourceKey;	//actually change the value
				firePropertyChange(TETHER_IMAGE_RESOURCE_KEY_PROPERTY, oldTetherImageResourceKey, newTetherImageResourceKey);	//indicate that the value changed
			}
		}

	/**Session, ID, model, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFlyoverFrame(final GuiseSession session, final String id, final LabelModel model, final Component<?> component)
	{
		super(session, id, model, component);	//construct the parent class
		setModal(false);	//default to being a nonmodal frame
		setMovable(false);	//default to being nonmovable
		setResizable(false);	//default to not allowing resizing
		setTitleVisible(false);	//don't show a title
		tetherBearingCompassPoints=createEnumSet(CompassPoint.class,	//establish the default accepted tether bearings
				CompassPoint.NORTHEAST_BY_NORTH,
				CompassPoint.NORTHEAST_BY_EAST,
				CompassPoint.SOUTHEAST_BY_EAST,
				CompassPoint.SOUTHEAST_BY_SOUTH,
				CompassPoint.SOUTHWEST_BY_SOUTH,
				CompassPoint.SOUTHWEST_BY_WEST,
				CompassPoint.NORTHWEST_BY_WEST,
				CompassPoint.NORTHWEST_BY_NORTH);
	}

}
