package com.guiseframework.component;


import java.net.URI;

import static com.garretwilson.lang.ClassUtilities.*;
import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.component.layout.*;
import com.guiseframework.converter.AbstractStringLiteralConverter;
import com.guiseframework.converter.Converter;
import com.guiseframework.geometry.Axis;
import com.guiseframework.model.*;
import com.guiseframework.prototype.ValuePrototype;

import static com.guiseframework.theme.Theme.*;
import com.guiseframework.validator.ValidationException;

/**A value control that represents its value by a slider.
@author Garret Wilson
@param <V> The type of value the slider represents.
*/
public class SliderControl<V extends Number> extends AbstractValueControl<V, SliderControl<V>>
{

	/**The flow bound property.*/
	public final static String FLOW_PROPERTY=getPropertyName(SliderControl.class, "flow");
	/**The interval step bound property.*/
	public final static String INTERVAL_PROPERTY=getPropertyName(SliderControl.class, "intervalStep");
	/**The bound property of the sliding state.*/
	public final static String SLIDING_PROPERTY=getPropertyName(SliderControl.class, "sliding");
	/**The bound property of the horizontal thumb image.*/
	public final static String THUMB_X_IMAGE_PROPERTY=getPropertyName(SliderControl.class, "thumbXImage");
	/**The bound property of the vertical thumb image.*/
	public final static String THUMB_Y_IMAGE_PROPERTY=getPropertyName(SliderControl.class, "thumbYImage");	
	/**The bound property of the horizontal track image.*/
	public final static String TRACK_X_IMAGE_PROPERTY=getPropertyName(SliderControl.class, "trackXImage");
	/**The bound property of the vertical track image.*/
	public final static String TRACK_Y_IMAGE_PROPERTY=getPropertyName(SliderControl.class, "trackYImage");

	/**The flow along which the slider is oriented.*/
	private Flow flow;

		/**@return The flow along which the slider is oriented.*/
		public Flow getFlow() {return flow;}

		/**Sets the flow of the slider.
		This is a bound property
		@param newFlow The flow along which the slider is oriented.
		@exception NullPointerException if the given flow is <code>null</code>.
		@see #FLOW_PROPERTY
		*/
		public void setFlow(final Flow newFlow)
		{
			if(flow!=checkInstance(newFlow, "Flow cannot be null."))	//if the value is really changing
			{
				final Flow oldAxis=flow;	//get the old value
				flow=newFlow;	//actually change the value
				firePropertyChange(FLOW_PROPERTY, oldAxis, newFlow);	//indicate that the value changed
			}
		}

	/**The converter for this component.*/
	private Converter<V, String> converter;

		/**@return The converter for this component.*/
		public Converter<V, String> getConverter() {return converter;}

		/**Sets the converter.
		This is a bound property
		@param newConverter The converter for this component.
		@exception NullPointerException if the given converter is <code>null</code>.
		@see #CONVERTER_PROPERTY
		*/
		public void setConverter(final Converter<V, String> newConverter)
		{
			if(converter!=newConverter)	//if the value is really changing
			{
				final Converter<V, String> oldConverter=converter;	//get the old value
				converter=checkInstance(newConverter, "Converter cannot be null.");	//actually change the value
				firePropertyChange(CONVERTER_PROPERTY, oldConverter, newConverter);	//indicate that the value changed
			}
		}

	/**The value of the intervals, or <code>null</code> if a default interval should be used.*/
	private V interval=null;

		/**@return The value of the intervals, or <code>null</code> if a default interval should be used.*/
		public V getInterval() {return interval;}

		/**Sets the value of the intervals.
		This is a bound property.
		@param newInterval The new value of the intervals, or <code>null</code> if a default interval should be used.
		@see #INTERVAL_PROPERTY
		*/
		public void setInterval(final V newInterval) throws ValidationException
		{
			if(!ObjectUtilities.equals(interval, newInterval))	//if the value is really changing (compare their values, rather than identity)
			{
				final V oldInterval=interval;	//get the old value
				interval=newInterval;	//actually change the value
				firePropertyChange(INTERVAL_PROPERTY, oldInterval, newInterval);	//indicate that the value changed
			}			
		}

	/**The array of axis thumb images.*/
	private URI[] axisThumbImages;

	/**The properties corresponding to the axis thumb images.*/
	private final static String[] AXIS_THUMB_IMAGE_PROPERTIES;

		static
		{
			AXIS_THUMB_IMAGE_PROPERTIES=new String[Axis.values().length];	//create the array of properties and fill it with corresponding properties
			AXIS_THUMB_IMAGE_PROPERTIES[Axis.X.ordinal()]=THUMB_X_IMAGE_PROPERTY;
			AXIS_THUMB_IMAGE_PROPERTIES[Axis.Y.ordinal()]=THUMB_Y_IMAGE_PROPERTY;
		}
	
		/**Returns the thumb image for the indicated axis.
		@param axis The axis for which a thumb image should be returned.
		@return The thumb image for the given axis.
		*/
		public URI getThumbImage(final Axis axis) {return axisThumbImages[axis.ordinal()];}

		/**Returns the thumb image for the X axis.
		@return The thumb image for the indicated axis.
		*/
		public URI ThumbXImage() {return getThumbImage(Axis.X);}

		/**Returns the thumb image for the Y axis.
		@return The thumb image for the indicated axis.
		*/
		public URI ThumbYImage() {return getThumbImage(Axis.Y);}

		/**Sets the thumb image for a given axis.
		The thumb image of each axis represents a bound property.
		@param axis The axis for which the thumb image should be set.
		@param newThumbImage The thumb image.
		@exception NullPointerException if the given axis and/or thumb image is <code>null</code>. 
		@see #THUMB_X_IMAGE_PROPERTY
		@see #THUMB_Y_IMAGE_PROPERTY
		*/
		public void setThumbImage(final Axis axis, final URI newThumbImage)
		{
			final int axisOrdinal=checkInstance(axis, "Axis cannot be null").ordinal();	//get the ordinal of the axis
			final URI oldThumbImage=axisThumbImages[axisOrdinal];	//get the old value
			if(!oldThumbImage.equals(checkInstance(newThumbImage, "Thumb image cannot be null.")))	//if the value is really changing
			{
				axisThumbImages[axisOrdinal]=newThumbImage;	//actually change the value
				firePropertyChange(AXIS_THUMB_IMAGE_PROPERTIES[axisOrdinal], oldThumbImage, newThumbImage);	//indicate that the value changed
			}			
		}

		/**Sets the thumb image for the X axis.
		This is a bound property.
		@param newThumbImage The thumb image.
		@exception NullPointerException if the given thumb image is <code>null</code>.
		@see #THUMB_X_IMAGE_PROPERTY
		*/
		public void setThumbXImage(final URI newThumbImage) {setThumbImage(Axis.X, newThumbImage);}

		/**Sets the thumb image for the Y axis.
		This is a bound property.
		@param newThumbImage The thumb image.
		@exception NullPointerException if the given thumb image is <code>null</code>.
		@see #THUMB_Y_IMAGE_PROPERTY
		*/
		public void setThumbYImage(final URI newThumbImage) {setThumbImage(Axis.Y, newThumbImage);}

	/**The array of axis track images.*/
	private URI[] axisTrackImages;

	/**The properties corresponding to the axis track images.*/
	private final static String[] AXIS_TRACK_IMAGE_PROPERTIES;

		static
		{
			AXIS_TRACK_IMAGE_PROPERTIES=new String[Axis.values().length];	//create the array of properties and fill it with corresponding properties
			AXIS_TRACK_IMAGE_PROPERTIES[Axis.X.ordinal()]=TRACK_X_IMAGE_PROPERTY;
			AXIS_TRACK_IMAGE_PROPERTIES[Axis.Y.ordinal()]=TRACK_Y_IMAGE_PROPERTY;
		}
	
		/**Returns the track image for the indicated axis.
		@param axis The axis for which a track image should be returned.
		@return The track image for the given axis.
		*/
		public URI getTrackImage(final Axis axis) {return axisTrackImages[axis.ordinal()];}

		/**Returns the track image for the X axis.
		@return The track image for the indicated axis.
		*/
		public URI TrackXImage() {return getTrackImage(Axis.X);}

		/**Returns the track image for the Y axis.
		@return The track image for the indicated axis.
		*/
		public URI TrackYImage() {return getTrackImage(Axis.Y);}

		/**Sets the track image for a given axis.
		The track image of each axis represents a bound property.
		@param axis The axis for which the track image should be set.
		@param newTrackImage The track image.
		@exception NullPointerException if the given axis and/or track image is <code>null</code>. 
		@see #TRACK_X_IMAGE_PROPERTY
		@see #TRACK_Y_IMAGE_PROPERTY
		*/
		public void setTrackImage(final Axis axis, final URI newTrackImage)
		{
			final int axisOrdinal=checkInstance(axis, "Axis cannot be null").ordinal();	//get the ordinal of the axis
			final URI oldTrackImage=axisTrackImages[axisOrdinal];	//get the old value
			if(!oldTrackImage.equals(checkInstance(newTrackImage, "Track image cannot be null.")))	//if the value is really changing
			{
				axisTrackImages[axisOrdinal]=newTrackImage;	//actually change the value
				firePropertyChange(AXIS_TRACK_IMAGE_PROPERTIES[axisOrdinal], oldTrackImage, newTrackImage);	//indicate that the value changed
			}			
		}

		/**Sets the track image for the X axis.
		This is a bound property.
		@param newTrackImage The track image.
		@exception NullPointerException if the given track image is <code>null</code>.
		@see #TRACK_X_IMAGE_PROPERTY
		*/
		public void setTrackXImage(final URI newTrackImage) {setTrackImage(Axis.X, newTrackImage);}

		/**Sets the track image for the Y axis.
		This is a bound property.
		@param newTrackImage The track image.
		@exception NullPointerException if the given track image is <code>null</code>.
		@see #TRACK_Y_IMAGE_PROPERTY
		*/
		public void setTrackYImage(final URI newTrackImage) {setTrackImage(Axis.Y, newTrackImage);}

	/**Whether the slider is being slid.*/
	private boolean sliding=false;

		/**@return Whether the slider is being slid.*/
		public boolean isSliding() {return sliding;}

		/**Sets whether the slider is being slid.
		This is a bound property of type <code>Boolean</code>.
		@param newSliding <code>true</code> if the slider is sliding, else <code>false</code>.
		@see #SLIDING_PROPERTY
		*/
		public void setSliding(final boolean newSliding)
		{
			if(sliding!=newSliding)	//if the value is really changing
			{
				final boolean oldSliding=sliding;	//get the current value
				sliding=newSliding;	//update the value
				firePropertyChange(SLIDING_PROPERTY, Boolean.valueOf(oldSliding), Boolean.valueOf(newSliding));
			}
		}

	/**Value class and flow constructor with a default data model to represent a given type and a default converter.
	@param valueClass The class indicating the type of value held in the model.
	@param flow The flow along which the slider is oriented.
	@exception NullPointerException if the value class and/or flow is <code>null</code>.
	*/
	public SliderControl(final Class<V> valueClass, final Flow flow)
	{
		this(new DefaultValueModel<V>(valueClass), flow);	//construct the class with a default model
	}

	/**Value class, converter and flow constructor with a default value model to represent a given type.
	@param valueClass The class indicating the type of value held in the model.
	@param converter The string literal value converter for this component.
	@param flow The flow along which the slider is oriented.
	@exception NullPointerException if the given value class, converter, and/or flow is <code>null</code>.
	*/
	public SliderControl(final Class<V> valueClass, final Converter<V, String> converter, final Flow flow)
	{
		this(new DefaultValueModel<V>(valueClass), converter, flow);	//construct the class with a default model
	}
	
	/**Value model and flow constructor with a default converter.
	@param valueModel The component value model.
	@param flow The flow along which the slider is oriented.
	@exception NullPointerException if the given value model and/or flow is <code>null</code>.
	@exception IllegalArgumentException if no default converter is available for the given model's value class.
	*/
	public SliderControl(final ValueModel<V> valueModel, final Flow flow)
	{
		this(valueModel, AbstractStringLiteralConverter.getInstance(valueModel.getValueClass()), flow);	//construct the class with a default converter
	}

	/**Value model, converter, and flow constructor.
	@param valueModel The component value model.
	@param converter The string literal value converter for this component.
	@param flow The flow along which the slider is oriented.
	@exception NullPointerException if the given value model, converter, and/or flow is <code>null</code>.
	*/
	public SliderControl(final ValueModel<V> valueModel, final Converter<V, String> converter, final Flow flow)
	{
		super(new DefaultLabelModel(), valueModel, new DefaultEnableable());	//construct the parent class
		this.converter=checkInstance(converter, "Converter cannot be null");	//save the converter
		this.flow=checkInstance(flow, "Flow cannot be null.");
		axisThumbImages=new URI[Axis.values().length];	//create the array of thumb images and fill it with corresponding properties
		axisThumbImages[Axis.X.ordinal()]=SLIDER_THUMB_X_IMAGE;
		axisThumbImages[Axis.Y.ordinal()]=SLIDER_THUMB_Y_IMAGE;
		axisTrackImages=new URI[Axis.values().length];	//create the array of track images and fill it with corresponding properties
		axisTrackImages[Axis.X.ordinal()]=SLIDER_TRACK_X_IMAGE;
		axisTrackImages[Axis.Y.ordinal()]=SLIDER_TRACK_Y_IMAGE;
	}

	/**Prototype and flow constructor.
	@param valuePrototype The prototype on which this component should be based.
	@param flow The flow along which the slider is oriented.
	@exception NullPointerException if the given prototype and/or flow is <code>null</code>.
	*/
	public SliderControl(final ValuePrototype<V> valuePrototype, final Flow flow)
	{
		this((ValueModel<V>)valuePrototype, flow);	//use the value prototype as every needed model
	}

}
