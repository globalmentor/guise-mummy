package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.MouseEvent;
import com.guiseframework.event.MouseListener;
import com.guiseframework.geometry.Point;
import com.guiseframework.geometry.Rectangle;
import com.guiseframework.model.ValueModel;

/**A spinner control.
@param <V> The type of value the spinner represents.
@author Garret Wilson
*/
public abstract class SpinnerControl<V> extends AbstractContainer<SpinnerControl<V>> implements ValueControl<V, SpinnerControl<V>>  
{

	/**@return The layout definition for the component.*/
	public FlowLayout getLayout() {return (FlowLayout)super.getLayout();}

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public SpinnerControl(final GuiseSession session, final String id, final FlowLayout layout, final ValueModel<V> model)
	{
		super(session, id, layout/*TODO fix, model*/);	//construct the parent class
	}

//TODO fix to use one of new abstract composite component classes; make sure determineValid() is implemented correctly and that updateValid() is called at the appropriate time
}
