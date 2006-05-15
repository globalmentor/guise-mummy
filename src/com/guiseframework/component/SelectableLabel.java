package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.model.*;
import com.guiseframework.style.Color;
import com.guiseframework.theme.Theme;

import static com.guiseframework.theme.Theme.*;

/**A label component that is able to indicate a selected state.
This component has no facility for interacting with the user.
@author Garret Wilson
*/
public class SelectableLabel extends Label implements Selectable
{

	/**The bound property of the selected background color.*/
	public final static String SELECTED_BACKGROUND_COLOR_PROPERTY=getPropertyName(SelectableLabel.class, "selectedBackgroundColor");

	/**Whether the component is selected.*/
	private boolean selected=false;

		/**@return Whether the component is selected.*/
		public boolean isSelected() {return selected;}

		/**Sets whether the component is selected.
		This is a bound property of type <code>Boolean</code>.
		@param newSelected <code>true</code> if the component should be selected, else <code>false</code>.
		@see #SELECTED_PROPERTY
		*/
		public void setSelected(final boolean newSelected)
		{
			if(selected!=newSelected)	//if the value is really changing
			{
				final boolean oldSelected=selected;	//get the current value
				selected=newSelected;	//update the value
				firePropertyChange(SELECTED_PROPERTY, Boolean.valueOf(oldSelected), Boolean.valueOf(newSelected));
			}
		}

	/**The selected background color of the component, or <code>null</code> if no selected background color is specified for this component.*/
	private Color<?> selectedBackgroundColor=COLOR_SELECTED_BACKGROUND;

		/**Returns the selected background color of the component.
		The default value is {@link Theme#COLOR_SELECTED_BACKGROUND}.
		@return The selected background color of the component, or <code>null</code> if no selected background color is specified for this component.
		*/
		public Color<?> getSelectedBackgroundColor() {return selectedBackgroundColor;}

		/**Sets the selected background color of the component.
		This is a bound property.
		@param newSelectedBackgroundColor The selected background color of the component, or <code>null</code> if the default selected background color should be used.
		@see #SELECTED_BACKGROUND_COLOR_PROPERTY 
		*/
		public void setSelectedBackgroundColor(final Color<?> newSelectedBackgroundColor)
		{
			if(!ObjectUtilities.equals(selectedBackgroundColor, newSelectedBackgroundColor))	//if the value is really changing
			{
				final Color<?> oldSelectedBackgroundColor=selectedBackgroundColor;	//get the old value
				selectedBackgroundColor=newSelectedBackgroundColor;	//actually change the value
				firePropertyChange(SELECTED_BACKGROUND_COLOR_PROPERTY, oldSelectedBackgroundColor, newSelectedBackgroundColor);	//indicate that the value changed
			}			
		}

	/**Default constructor with a default label model.*/
	public SelectableLabel()
	{
		this(new DefaultLabelModel());	//construct the class with a default label model
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public SelectableLabel(LabelModel labelModel)
	{
		super(labelModel);	//construct the parent class
	}
}
