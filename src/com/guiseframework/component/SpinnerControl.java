package com.guiseframework.component;

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

	/**Layout and value model constructor.
	@param layout The layout definition for the container.
	@param valueModel The component value model.
	@exception NullPointerException if the given layout and/or value model is <code>null</code>.
	*/
	public SpinnerControl(final FlowLayout layout, final ValueModel<V> valueModel)
	{
		super(layout/*TODO fix, model*/);	//construct the parent class
	}

//TODO fix to use one of new abstract composite component classes; make sure determineValid() is implemented correctly and that updateValid() is called at the appropriate time
}
