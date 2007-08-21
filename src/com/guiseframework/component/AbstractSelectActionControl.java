package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.net.URI;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**Abstract selectable action control.
@author Garret Wilson
*/
public abstract class AbstractSelectActionControl extends AbstractActionControl implements SelectActionControl
{

	/**Whether this control automatically sets or toggles the selection state when the action occurs.*/
	private boolean autoSelect=true;

		/**@return Whether this control automatically sets or toggles the selection state when the action occurs.*/
		public boolean isAutoSelect() {return autoSelect;}

		/**Sets whether this control automatically sets or toggles the selection state when the action occurs.
		This is a bound property of type <code>Boolean</code>.
		@param newAutoSelect <code>true</code> if the control should automatically set or toggle the selection state when an action occurs, or <code>false</code> if no selection occurs automatically.
		@see #AUTO_SELECT_PROPERTY
		*/
		public void setAutoSelect(final boolean newAutoSelect)
		{
			if(autoSelect!=newAutoSelect)	//if the value is really changing
			{
				final boolean oldAutoSelect=autoSelect;	//get the current value
				autoSelect=newAutoSelect;	//update the value
				firePropertyChange(AUTO_SELECT_PROPERTY, Boolean.valueOf(oldAutoSelect), Boolean.valueOf(newAutoSelect));
			}
		}

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

	/**The selected icon URI, which may be a resource URI, or <code>null</code> if there is no selected icon URI.*/
	private URI selectedIcon=null;

		/**@return The selected icon URI, which may be a resource URI, or <code>null</code> if there is no selected icon URI.*/
		public URI getSelectedGlyphURI() {return selectedIcon;}

		/**Sets the URI of the selected icon.
		This is a bound property of type <code>URI</code>.
		@param newSelectedIcon The new URI of the selected icon, which may be a resource URI.
		@see #SELECTED_GLYPH_URI_PROPERTY
		*/
		public void setSelectedGlyphURI(final URI newSelectedIcon)
		{
			if(!ObjectUtilities.equals(selectedIcon, newSelectedIcon))	//if the value is really changing
			{
				final URI oldSelectedIcon=selectedIcon;	//get the old value
				selectedIcon=newSelectedIcon;	//actually change the value
				firePropertyChange(SELECTED_GLYPH_URI_PROPERTY, oldSelectedIcon, newSelectedIcon);	//indicate that the value changed
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

	/**The unselected icon URI, which may be a resource URI, or <code>null</code> if there is no unselected icon URI.*/
	private URI unselectedIcon=null;

		/**@return The unselected icon URI, which may be a resource URI, or <code>null</code> if there is no unselected icon URI.*/
		public URI getUnselectedGlyphURI() {return unselectedIcon;}

		/**Sets the URI of the unselected icon.
		This is a bound property of type <code>URI</code>.
		@param newUnselectedIcon The new URI of the unselected icon, which may be a resource URI.
		@see #UNSELECTED_GLYPH_URI_PROPERTY
		*/
		public void setUnselectedGlyphURI(final URI newUnselectedIcon)
		{
			if(!ObjectUtilities.equals(unselectedIcon, newUnselectedIcon))	//if the value is really changing
			{
				final URI oldUnselectedIcon=unselectedIcon;	//get the old value
				unselectedIcon=newUnselectedIcon;	//actually change the value
				firePropertyChange(UNSELECTED_GLYPH_URI_PROPERTY, oldUnselectedIcon, newUnselectedIcon);	//indicate that the value changed
			}			
		}

	/**Label model, action model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public AbstractSelectActionControl(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable)
	{
		super(labelModel, actionModel, enableable);	//construct the parent class
		addActionListener(new SelectActionListener(this));	//listen for an action and set the selected state accordingly
	}
		
	/**An action listener that selects a select action listener if auto-select is turned on, toggling the select status if necessary.
	@author Garret Wilson
	*/
	public static class SelectActionListener implements ActionListener
	{
		
		/**The control to select.*/
		protected final SelectActionControl selectActionControl;
		
		/**Select action control constructor.
		@param selectActionControl The control to select when the action occurs.
		@exception NullPointerException if the given select action control is <code>null</code>.
		*/
		public SelectActionListener(final SelectActionControl selectActionControl)
		{
			this.selectActionControl=checkInstance(selectActionControl, "Select action control cannot be null.");
		}
		
		/**Called when an action is initiated.
		This implementation auto-selects the select action control if auto-select is turned on, toggling if appropriate.
		@param actionEvent The event indicating the source of the action.
		@see SelectActionControl#isAutoSelect()
		@see SelectActionControl#isToggle()
		@see SelectActionControl#setSelected(boolean)
		*/
		public void actionPerformed(final ActionEvent actionEvent)	//if an action occurs
		{
			if(selectActionControl.isAutoSelect())	//if we should automatically select the control
			{
				selectActionControl.setSelected(selectActionControl.isToggle() ? !selectActionControl.isSelected() : true);	//if we should toggle, switch the selected state; otherwise, just switch to the selected state
			}
		}
	}	

}
