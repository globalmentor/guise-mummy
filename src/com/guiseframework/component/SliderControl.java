package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import java.net.URI;
import java.util.MissingResourceException;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.converter.AbstractStringLiteralConverter;
import com.guiseframework.converter.Converter;
import com.guiseframework.model.DefaultValueModel;
import com.guiseframework.model.ValueModel;
import com.guiseframework.validator.ValidationException;


/**A value control that represents its value by a slider.
If a thumb or track image resource key is set, a resource will be retrieved first using an appended physical axis designator (".x" or ".y").
For example, if a thumb image resource key of "thumb.image" is set, first a resource of "thumb.image.x" will be retrieved (for Western orientation and a line axis),
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
	/**The thumb image resource key bound property.*/
	public final static String THUMB_IMAGE_RESOURCE_KEY_PROPERTY=getPropertyName(SliderControl.class, "thumbImageResourceKey");
	/**The track image bound property.*/
	public final static String TRACK_IMAGE_PROPERTY=getPropertyName(SliderControl.class, "trackImage");
	/**The track image resource key bound property.*/
	public final static String TRACK_IMAGE_RESOURCE_KEY_PROPERTY=getPropertyName(SliderControl.class, "trackImageResourceKey");

	/**The base resource bundle key for the slider thumb image URI.*/
	public final static String THUMB_IMAGE_RESOURCE_KEY="slider.thumb.image";
	/**The base resource bundle key for the slider track image URI.*/
	public final static String TRACK_IMAGE_RESOURCE_KEY="slider.track.image";

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

	/**The thumb image URI, or <code>null</code> if there is no thumb image URI.*/
	private URI thumbImage=null;

		/**Determines the URI of the thumb image.
		If an image is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
		@return The thumb image URI, or <code>null</code> if there is no thumb image URI.
		@exception MissingResourceException if there was an error loading the value from the resources.
		@see #getThumbImageResourceKey()
		*/
		public URI getThumbImage() throws MissingResourceException
		{
			return getURI(thumbImage, getThumbImageResourceKey(), getAxis());	//get the value or the resource, if available, taking the flow axis into account
		}

		/**Sets the URI of the thumb image.
		This is a bound property of type <code>URI</code>.
		@param newThumbImage The new URI of the image.
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

	/**The thumb image URI resource key, or <code>null</code> if there is no thumb image URI resource specified.*/
	private String thumbImageResourceKey=THUMB_IMAGE_RESOURCE_KEY;

		/**@return The thumb image URI resource key, or <code>null</code> if there is no thumb image URI resource specified.*/
		public String getThumbImageResourceKey() {return thumbImageResourceKey;}

		/**Sets the key identifying the URI of the thumb image in the resources.
		This is a bound property.
		@param newThumbImageResourceKey The new image URI resource key.
		@see #THUMB_IMAGE_RESOURCE_KEY_PROPERTY
		*/
		public void setThumbImageResourceKey(final String newThumbImageResourceKey)
		{
			if(!ObjectUtilities.equals(thumbImageResourceKey, newThumbImageResourceKey))	//if the value is really changing
			{
				final String oldThumbImageResourceKey=thumbImageResourceKey;	//get the old value
				thumbImageResourceKey=newThumbImageResourceKey;	//actually change the value
				firePropertyChange(THUMB_IMAGE_RESOURCE_KEY_PROPERTY, oldThumbImageResourceKey, newThumbImageResourceKey);	//indicate that the value changed
			}
		}

		/**The track image URI, or <code>null</code> if there is no track image URI.*/
		private URI trackImage=null;

			/**Determines the URI of the track image.
			If an image is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
			@return The track image URI, or <code>null</code> if there is no track image URI.
			@exception MissingResourceException if there was an error loading the value from the resources.
			@see #getTrackImageResourceKey()
			*/
			public URI getTrackImage() throws MissingResourceException
			{
				return getURI(trackImage, getTrackImageResourceKey(), getAxis());	//get the value or the resource, if available, taking the flow axis into account
			}

			/**Sets the URI of the track image.
			This is a bound property of type <code>URI</code>.
			@param newTrackImage The new URI of the image.
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

		/**The track image URI resource key, or <code>null</code> if there is no track image URI resource specified.*/
		private String trackImageResourceKey=TRACK_IMAGE_RESOURCE_KEY;

			/**@return The track image URI resource key, or <code>null</code> if there is no track image URI resource specified.*/
			public String getTrackImageResourceKey() {return trackImageResourceKey;}

			/**Sets the key identifying the URI of the track image in the resources.
			This is a bound property.
			@param newTrackImageResourceKey The new image URI resource key.
			@see #TRACK_IMAGE_RESOURCE_KEY_PROPERTY
			*/
			public void setTrackImageResourceKey(final String newTrackImageResourceKey)
			{
				if(!ObjectUtilities.equals(trackImageResourceKey, newTrackImageResourceKey))	//if the value is really changing
				{
					final String oldTrackImageResourceKey=trackImageResourceKey;	//get the old value
					trackImageResourceKey=newTrackImageResourceKey;	//actually change the value
					firePropertyChange(TRACK_IMAGE_RESOURCE_KEY_PROPERTY, oldTrackImageResourceKey, newTrackImageResourceKey);	//indicate that the value changed
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

	/**Session, model, and axis constructor with a default converter.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, model, and/or axis is <code>null</code>.
	*/
	public SliderControl(final GuiseSession session, final ValueModel<V> model, final Flow axis)
	{
		this(session, null, model, axis);	//construct the component, indicating that a default ID should be used
	}

	
	/**Session, model, converter, and axis constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param converter The string literal value converter for this component.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, model, converter, and/or axis is <code>null</code>.
	*/
	public SliderControl(final GuiseSession session, final ValueModel<V> model, final Converter<V, String> converter, final Flow axis)
	{
		this(session, null, model, converter, axis);	//construct the component, indicating that a default ID should be used
	}
	
	/**Session and axis constructor with a default data model to represent a given type and a default converter.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, value class, and/or axis is <code>null</code>.
	*/
	public SliderControl(final GuiseSession session, final Class<V> valueClass, final Flow axis)
	{
		this(session, null, valueClass, axis);	//construct the component, indicating that a default ID should be used
	}

	/**Session, converter, and axis constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param converter The string literal value converter for this component.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, value class, converter, and/or axis is <code>null</code>.
	*/
	public SliderControl(final GuiseSession session, final Class<V> valueClass, final Converter<V, String> converter, final Flow axis)
	{
		this(session, null, valueClass, converter, axis);	//construct the component, indicating that a default ID should be used
	}
	
	/**Session, ID and axis constructor with a default data model to represent a given type and a default converter.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, value class, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public SliderControl(final GuiseSession session, final String id, final Class<V> valueClass, final Flow axis)
	{
		this(session, id, new DefaultValueModel<V>(session, valueClass), axis);	//construct the class with a default model
	}

	/**Session, ID, converter, and axis constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param converter The string literal value converter for this component.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, value class, converter, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public SliderControl(final GuiseSession session, final String id, final Class<V> valueClass, final Converter<V, String> converter, final Flow axis)
	{
		this(session, id, new DefaultValueModel<V>(session, valueClass), converter, axis);	//construct the class with a default model
	}
	
	/**Session, ID, model, and axis constructor with a default converter.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, model, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if no default converter is available for the given model's value class.
	*/
	public SliderControl(final GuiseSession session, final String id, final ValueModel<V> model, final Flow axis)
	{
		this(session, id, model, AbstractStringLiteralConverter.getInstance(session, model.getValueClass()), axis);	//construct the class with a default converter
	}

	/**Session, ID, model, and axis constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param converter The string literal value converter for this component.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, model, converter, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public SliderControl(final GuiseSession session, final String id, final ValueModel<V> model, final Converter<V, String> converter, final Flow axis)
	{
		super(session, id, model);	//construct the parent class
		this.converter=checkInstance(converter, "Converter cannot be null");	//save the converter
		this.axis=checkInstance(axis, "Flow axis cannot be null.");
	}
	
}
