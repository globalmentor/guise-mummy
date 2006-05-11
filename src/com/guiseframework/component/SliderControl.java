package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import java.net.URI;

import com.garretwilson.lang.ObjectUtilities;
import static com.guiseframework.Resources.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.converter.AbstractStringLiteralConverter;
import com.guiseframework.converter.Converter;
import com.guiseframework.model.DefaultValueModel;
import com.guiseframework.model.ValueModel;
import com.guiseframework.validator.ValidationException;

/**A value control that represents its value by a slider.
If a thumb or track image resource key is set, a resource will be retrieved first using an appended physical axis designator (".X" or ".Y").
For example, if a thumb image resource key of "thumb.image" is set, first a resource of "thumb.image.Y" will be retrieved (for Western orientation and a line axis),
after which a resource for "thumb.image" will be retrieved if there is no resource for "thumb.image.x".
@author Garret Wilson
@param <V> The type of value the slider represents.
*/
public class SliderControl<V extends Number> extends AbstractValueControl<V, SliderControl<V>>
{

	/**The axis bound property.*/
	public final static String AXIS_PROPERTY=getPropertyName(SliderControl.class, "axis");
	/**The interval step bound property.*/
	public final static String INTERVAL_PROPERTY=getPropertyName(SliderControl.class, "intervalStep");
	/**The bound property of the sliding state.*/
	public final static String SLIDING_PROPERTY=getPropertyName(SliderControl.class, "sliding");
	/**The thumb image bound property.*/
	public final static String THUMB_IMAGE_PROPERTY=getPropertyName(SliderControl.class, "thumbImage");
	/**The track image bound property.*/
	public final static String TRACK_IMAGE_PROPERTY=getPropertyName(SliderControl.class, "trackImage");

	/**The base resource URI for the slider thumb image URI.*/
	public final static URI THUMB_IMAGE_RESOURCE_URI=createURIResourceReference("theme.slider.thumb.image");
	/**The base resource URI for the slider track image URI.*/
	public final static URI TRACK_IMAGE_RESOURCE_URI=createURIResourceReference("theme.slider.track.image");

	/**The flow axis.*/
	private Flow axis;

		/**@return The flow axis.*/
		public Flow getAxis() {return axis;}

		/**Sets the flow axis.
		This is a bound property
		@param newAxis The flow axis.
		@exception NullPointerException if the given axis is <code>null</code>.
		@see #AXIS_PROPERTY
		*/
		public void setAxis(final Flow newAxis)
		{
			if(axis!=checkInstance(newAxis, "Flow axis cannot be null."))	//if the value is really changing
			{
				final Flow oldAxis=axis;	//get the old value
				axis=newAxis;	//actually change the value
				firePropertyChange(AXIS_PROPERTY, oldAxis, newAxis);	//indicate that the value changed
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

	/**The thumb image URI, which may be a resource URI, or <code>null</code> if there is no thumb image URI.*/
	private URI thumbImage=THUMB_IMAGE_RESOURCE_URI;

		/**@return The thumb image URI, which may be a resource URI, or <code>null</code> if there is no thumb image URI.*/
		public URI getThumbImage() {return thumbImage;}

		/**Sets the URI of the thumb image.
		This is a bound property of type <code>URI</code>.
		@param newThumbImage The new URI of the image, which may be a resource URI.
		@see #THUMB_IMAGE_PROPERTY
		*/
		public void setThumbImage(final URI newThumbImage)
		{
			if(!ObjectUtilities.equals(thumbImage, newThumbImage))	//if the value is really changing
			{
				final URI oldThumbImage=thumbImage;	//get the old value
				thumbImage=newThumbImage;	//actually change the value
				firePropertyChange(THUMB_IMAGE_PROPERTY, oldThumbImage, newThumbImage);	//indicate that the value changed
			}			
		}

	/**The track image URI, which may be a resource URI, or <code>null</code> if there is no track image URI.*/
	private URI trackImage=TRACK_IMAGE_RESOURCE_URI;

		/**@return The track image URI, which may be a resource URI, or <code>null</code> if there is no track image URI.*/
		public URI getTrackImage() {return trackImage;}

		/**Sets the URI of the track image.
		This is a bound property of type <code>URI</code>.
		@param newTrackImage The new URI of the image, which may be a resource URI.
		@see #TRACK_IMAGE_PROPERTY
		*/
		public void setTrackImage(final URI newTrackImage)
		{
			if(!ObjectUtilities.equals(trackImage, newTrackImage))	//if the value is really changing
			{
				final URI oldTrackImage=trackImage;	//get the old value
				trackImage=newTrackImage;	//actually change the value
				firePropertyChange(TRACK_IMAGE_PROPERTY, oldTrackImage, newTrackImage);	//indicate that the value changed
			}			
		}

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

	/**Value class and axis constructor with a default data model to represent a given type and a default converter.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the value class and/or axis is <code>null</code>.
	*/
	public SliderControl(final Class<V> valueClass, final Flow axis)
	{
		this(new DefaultValueModel<V>(valueClass), axis);	//construct the class with a default model
	}

	/**Value class, converter and axis constructor with a default value model to represent a given type.
	@param valueClass The class indicating the type of value held in the model.
	@param converter The string literal value converter for this component.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given value class, converter, and/or axis is <code>null</code>.
	*/
	public SliderControl(final Class<V> valueClass, final Converter<V, String> converter, final Flow axis)
	{
		this(new DefaultValueModel<V>(valueClass), converter, axis);	//construct the class with a default model
	}
	
	/**Value model and axis constructor with a default converter.
	@param valueModel The component value model.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given value model and/or axis is <code>null</code>.
	@exception IllegalArgumentException if no default converter is available for the given model's value class.
	*/
	public SliderControl(final ValueModel<V> valueModel, final Flow axis)
	{
		this(valueModel, AbstractStringLiteralConverter.getInstance(valueModel.getValueClass()), axis);	//construct the class with a default converter
	}

	/**Value model, converter, and axis constructor.
	@param valueModel The component value model.
	@param converter The string literal value converter for this component.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given value model, converter, and/or axis is <code>null</code>.
	*/
	public SliderControl(final ValueModel<V> valueModel, final Converter<V, String> converter, final Flow axis)
	{
		super(valueModel);	//construct the parent class
		this.converter=checkInstance(converter, "Converter cannot be null");	//save the converter
		this.axis=checkInstance(axis, "Flow axis cannot be null.");
	}
	
}
