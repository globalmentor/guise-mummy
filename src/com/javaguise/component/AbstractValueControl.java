package com.javaguise.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.converter.*;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;

/**Abstract implementation of a control to accept input from the user.
@param <V> The type of value to represent.
@author Garret Wilson
*/
public abstract class AbstractValueControl<V, C extends ValueControl<V, C>> extends AbstractControl<C> implements ValueControl<V, C>
{

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ValueModel<V> getModel() {return (ValueModel<V>)super.getModel();}

	/**Session constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public AbstractValueControl(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractValueControl(final GuiseSession session, final String id, final Class<V> valueClass)
	{
		this(session, id, new DefaultValueModel<V>(session, valueClass));	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractValueControl(final GuiseSession session, final String id, final ValueModel<V> model)
	{
		super(session, id, model);	//construct the parent class
	}

	/**Creates a default string literal converter for the value type represented by the given value class.
	Default converters are available for the following types:
	<ul>
		<li><code>char[]</code></li>
		<li><code>java.lang.Boolean</code></li>
		<li><code>java.lang.Float</code></li>
		<li><code>java.lang.Integer</code></li>
		<li><code>java.lang.String</code></li>
	</ul>
	@param <VV> The type of value represented.
	@param session The Guise session that will own the converter.
	@param valueClass The class of the represented value.
	@return The default converter for the value type represented by the given value class.
	@exception NullPointerException if the given value class is <code>null</code>.
	@exception IllegalArgumentException if no default converter is available for the given value class.
	*/
	@SuppressWarnings("unchecked")	//we check the value class before generic casting
	protected static <VV> Converter<VV, String> createDefaultStringLiteralConverter(final GuiseSession session, final Class<VV> valueClass)
	{
		checkNull(valueClass, "Value class cannot be null.");
		if(char[].class.equals(valueClass))	//char[]
		{
			return (Converter<VV, String>)new CharArrayStringLiteralConverter(session);
		}
		else if(Boolean.class.equals(valueClass))	//Boolean
		{
			return (Converter<VV, String>)new BooleanStringLiteralConverter(session);
		}
		else if(Float.class.equals(valueClass))	//Float
		{
			return (Converter<VV, String>)new FloatStringLiteralConverter(session);
		}
		else if(Integer.class.equals(valueClass))	//Integer
		{
			return (Converter<VV, String>)new IntegerStringLiteralConverter(session);
		}
		else if(String.class.equals(valueClass))	//String
		{
			return (Converter<VV, String>)new StringStringLiteralConverter(session);
		}
		else	//if we don't recognize the value class
		{
			throw new IllegalArgumentException("Unrecognized value class: "+valueClass);
		}
	}

}
