package com.guiseframework.component;

import java.net.URI;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**Selectable action value control for which the selected state is distinct from the contained value.
@param <V> The type of value the control represents.
@author Garret Wilson
*/
public abstract class AbstractSelectActionValueControl<V, C extends SelectActionControl<C> & ActionValueControl<V, C>> extends AbstractActionValueControl<V, C> implements SelectActionControl<C>
{

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
		
	/**The selected icon URI, or <code>null</code> if there is no selected icon URI.*/
	private URI selectedIcon=null;

		/**@return The selected icon URI, or <code>null</code> if there is no selected icon URI.*/
		public URI getSelectedIcon() {return selectedIcon;}

		/**Sets the URI of the selected icon.
		This is a bound property of type <code>URI</code>.
		@param newSelectedIcon The new URI of the selected icon.
		@see #SELECTED_ICON_PROPERTY
		*/
		public void setSelectedIcon(final URI newSelectedIcon)
		{
			if(!ObjectUtilities.equals(selectedIcon, newSelectedIcon))	//if the value is really changing
			{
				final URI oldSelectedIcon=selectedIcon;	//get the old value
				selectedIcon=newSelectedIcon;	//actually change the value
				firePropertyChange(SELECTED_ICON_PROPERTY, oldSelectedIcon, newSelectedIcon);	//indicate that the value changed
			}			
		}

	/**The selected icon URI resource key, or <code>null</code> if there is no selected icon URI resource specified.*/
	private String selectedIconResourceKey=null;

		/**@return The selected icon URI resource key, or <code>null</code> if there is no selected icon URI resource specified.*/
		public String getSelectedIconResourceKey() {return selectedIconResourceKey;}

		/**Sets the key identifying the URI of the selected icon in the resources.
		This is a bound property.
		@param newSelectedIconResourceKey The new selected icon URI resource key.
		@see #SELECTED_ICON_RESOURCE_KEY_PROPERTY
		*/
		public void setSelectedIconResourceKey(final String newSelectedIconResourceKey)
		{
			if(!ObjectUtilities.equals(selectedIconResourceKey, newSelectedIconResourceKey))	//if the value is really changing
			{
				final String oldSelectedIconResourceKey=selectedIconResourceKey;	//get the old value
				selectedIconResourceKey=newSelectedIconResourceKey;	//actually change the value
				firePropertyChange(SELECTED_ICON_RESOURCE_KEY_PROPERTY, oldSelectedIconResourceKey, newSelectedIconResourceKey);	//indicate that the value changed
			}
		}
		
	/**Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.*/
	private boolean toggle=false;

		/**@return Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.*/
		public boolean isToggle() {return toggle;}

		/**Sets whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.
		This is a bound property of type <code>Boolean</code>.
		@param newToggle <code>true</code> if the component should act as a toggle, else <code>false</code> if the action should unconditionally set the value to <code>true</code>.
		@see #TOGGLE_PROPERTY
		*/
		public void setToggle(final boolean newToggle)
		{
			if(toggle!=newToggle)	//if the value is really changing
			{
				final boolean oldToggle=toggle;	//get the current value
				toggle=newToggle;	//update the value
				firePropertyChange(TOGGLE_PROPERTY, Boolean.valueOf(oldToggle), Boolean.valueOf(newToggle));
			}
		}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractSelectActionValueControl(final GuiseSession session, final String id, final ValueModel<V> model)
	{
		super(session, id, model);	//construct the parent class
		addActionListener(new ActionListener()	//listen for an action and set the selected state accordingly
				{		
					public void actionPerformed(final ActionEvent actionEvent)	//if an action occurs
					{
						setSelected(isToggle() ? !isSelected() : true);	//if we should toggle, switch the selected state; otherwise, just switch to the selected state
					}
				});
	}

}
