package com.javaguise.model;

import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import com.javaguise.component.Component;
import com.javaguise.component.SelectControl;
import com.javaguise.session.GuiseSession;
import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

/**The default implementation of a column in a table.
@param <V> The type of values contained in the table column.
@author Garret Wilson
*/
public class DefaultTableColumnModel<V> extends DefaultLabelModel implements TableColumnModel<V>
{

	/**The class representing the type of values this model can hold.*/
	private final Class<V> valueClass;

		/**@return The class representing the type of values this model can hold.*/
		public Class<V> getValueClass() {return valueClass;}

	/**The strategy used to generate a component to represent each value in the model.*/
//TODO del when works	private ValueRepresentationStrategy<V, ? extends Component<?>> valueRepresentationStrategy;

		/**@return The strategy used to generate a component to represent each value in the model.*/
//	TODO del when works		public ValueRepresentationStrategy<V, ? extends Component<?>> getValueRepresentationStrategy() {return valueRepresentationStrategy;}

		/**Sets the strategy used to generate a component to represent each value in the model.
		This is a bound property
		@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
		@exception NullPointerException if the provided value representation strategy is <code>null</code>.
		@see TableColumnModel#VALUE_REPRESENTATION_STRATEGY_PROPERTY
		*/
/*TODO del when works
		public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V, ? extends Component<?>> newValueRepresentationStrategy)
		{
			if(valueRepresentationStrategy!=newValueRepresentationStrategy)	//if the value is really changing
			{
				final ValueRepresentationStrategy<V, ? extends Component<?>> oldValueRepresentationStrategy=valueRepresentationStrategy;	//get the old value
				valueRepresentationStrategy=checkNull(newValueRepresentationStrategy, "Value representation strategy cannot be null.");	//actually change the value
				firePropertyChange(VALUE_REPRESENTATION_STRATEGY_PROPERTY, oldValueRepresentationStrategy, newValueRepresentationStrategy);	//indicate that the value changed
			}
		}
*/

	/**Session and value class constructor.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public DefaultTableColumnModel(final GuiseSession<?> session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the class indicating that a default ID should be generated
	}

	/**Session, value class, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@param valueRepresentationStrategy The strategy to create controls to represent this model's values.
	@exception NullPointerException if the given session, value class, and/or value representation strategy is <code>null</code>.
	*/
/*TODO del when works
	public DefaultTableColumnModel(final GuiseSession<?> session, final Class<V> valueClass, final ValueRepresentationStrategy<V, ? extends Component<?>> valueRepresentationStrategy)
	{
		this(session, null, valueClass, valueRepresentationStrategy);	//construct the class indicating that a default ID should be generated
	}
*/

	/**Session, ID, and value class constructor.
	@param session The Guise session that owns this component.
	@param id The column identifier, or <code>null</code> if a default column identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
/*TODO del when works
	public DefaultTableColumnModel(final GuiseSession<?> session, final String id, final Class<V> valueClass)
	{
		this(session, id, valueClass);	//construct the class with a default value representation strategy
//TODO del when works		this(session, id, valueClass, new DefaultValueRepresentationStrategy<V>(session));	//construct the class with a default value representation strategy
	}
*/
	
	/**Session, ID, value class, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param id The column identifier, or <code>null</code> if a default column identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@param valueRepresentationStrategy The strategy to create controls to represent this model's values.
	@exception NullPointerException if the given session, value class, and/or value representation strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultTableColumnModel(final GuiseSession<?> session, final String id, final Class<V> valueClass/*TODO del when works, final ValueRepresentationStrategy<V, ? extends Component<?>> valueRepresentationStrategy*/)
	{
		super(session);	//construct the parent class
		this.valueClass=valueClass;	//save the value class
//TODO del when works		this.valueRepresentationStrategy=checkNull(valueRepresentationStrategy, "Value representation strategy cannot be null.");
	}

}
