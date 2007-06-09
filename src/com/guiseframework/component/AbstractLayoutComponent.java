package com.guiseframework.component;

import java.io.IOException;

import com.guiseframework.GuiseApplication;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.model.DefaultLabelModel;
import com.guiseframework.model.LabelModel;
import com.guiseframework.theme.Theme;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a layout component.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public abstract class AbstractLayoutComponent<C extends LayoutComponent<C>> extends AbstractListCompositeComponent<C> implements LayoutComponent<C>
{

	/**Adds a child component at the specified index.
	Any class that overrides this method must call this version.
	@param index The index at which the component should be added.
	@param component The component to add to this component.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException if the installed layout does not support default constraints.
	*/
	protected boolean addComponent(final int index, final Component<?> component)
	{
		if(super.addComponent(index, component))	//add the component normally; if the child components changed
		{
			getLayout().addComponent(component);	//add the component to the layout
	//TODO fix		fireContainerModified(indexOf(component), component, null);	//indicate the component was added at the index
			return true;	//indicate that the child components changed
		}
		else	//if the component list did not change
		{
			return false;	//indicate that the chidl components did not change
		}
	}

	/**Adds a component to the container along with constraints.
	This is a convenience method that first set the constraints of the component. 
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
/*TODO del if not needed
	protected boolean add(final Component<?> component, final Constraints constraints)
	{
		component.setConstraints(constraints);	//set the constraints in the component
		return add(component);	//add the component, now that its constraints have been set
	}
*/

	/**Removes a component from the layout component.
	@param component The component to remove.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component is not a member of this composite component.
	*/
	protected boolean removeComponent(final Component<?> component)
	{
		final int index=indexOf(component);	//get the index of the component
		if(component.getParent()!=this || index<0)	//if this component is not a member of this container TODO maybe make a better check than indexOf(), like hasComponent(); index may be needed eventually, though, if container events get promoted to composite componente events
		{
			throw new IllegalArgumentException("Component "+component+" is not member of composite component "+this+".");
		}
		getLayout().removeComponent(component);	//remove the component from the layout
//TODO del when works		getLayout().removeConstraints(component);	//remove the constraints for this component
		super.removeComponent(component);	//do the default removal
//TODO move to here when we promote this event type		fireContainerModified(index, null, component);	//indicate the component was removed from the index
		return true;	//indicate that the child components changed
	}

	/**The layout definition for the component.*/
	private Layout<?> layout;

		/**@return The layout definition for the component.*/
		public Layout<?> getLayout() {return layout;}

		/**Sets the layout definition for the component.
		This is a bound property.
		The layout is specified as not yet having a theme applied, as the specific theme rules applied to the layout may depend on the layout's owner.
		@param newLayout The new layout definition for the container.
		@exception NullPointerException if the given layout is <code>null</code>.
		@see #LAYOUT_PROPERTY
		@see #setPropertiesInitialized(boolean) 
		*/
		protected <T extends Constraints> void setLayout(final Layout<T> newLayout)
		{
			if(layout!=checkInstance(newLayout, "Layout cannot be null."))	//if the value is really changing
			{
				final Layout<?> oldLayout=layout;	//get the old value
				oldLayout.setOwner(null);	//tell the old layout it is no longer installed
				layout=newLayout;	//actually change the value
				layout.setOwner(this);	//tell the new layout which container owns it
				for(final Component<?> childComponent:getChildren())	//for each child component
				{
					newLayout.getConstraints(childComponent);	//make sure the constraints of all components are compatible with the layout TODO do we even need to do this? why not wait until later? but this may be OK---perhaps we can assume that if components are installed before the layout, they will be used with this layout and not another
				}
				setLayoutPropertiesInitialized(false);	//indicate that the properties haven't yet been initiaqlized for this layout, as the specific rules applied may depend on the layout's owner
				firePropertyChange(LAYOUT_PROPERTY, oldLayout, newLayout);	//indicate that the value changed
			}			
		}

	/**Whether the properties of this component's layout have been initialized.*/
	private boolean layoutPropertiesInitialized=false;

		/**@return Whether the properties of this component's layout have been initialized.*/
		public boolean isLayoutPropertiesInitialized() {return layoutPropertiesInitialized;}

		/**Sets whether the properties of this component's layout have been initialized.
		This is a bound property of type {@link Boolean}.
		@param newLayoutPropertiesInitialized <code>true</code> if the properties of this component's layout have been initialized, else <code>false</code>.
		@see #LAYOUT_PROPERTIES_INITIALIZED_PROPERTY
		*/
		public void setLayoutPropertiesInitialized(final boolean newLayoutPropertiesInitialized)
		{
			if(layoutPropertiesInitialized!=newLayoutPropertiesInitialized)	//if the value is really changing
			{
				final boolean oldLayoutPropertiesInitialized=layoutPropertiesInitialized;	//get the current value
				layoutPropertiesInitialized=newLayoutPropertiesInitialized;	//update the value
				firePropertyChange(LAYOUT_PROPERTIES_INITIALIZED_PROPERTY, Boolean.valueOf(oldLayoutPropertiesInitialized), Boolean.valueOf(newLayoutPropertiesInitialized));
			}
		}

	/**Layout constructor with a default label model.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public AbstractLayoutComponent(final Layout<?> layout)
	{
		this(new DefaultLabelModel(), layout);	//construct the class with a default label model
	}

	/**Label model and layout constructor.
	@param labelModel The component label model.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given label model and/or layout is <code>null</code>.
	*/
	public AbstractLayoutComponent(final LabelModel labelModel, final Layout<?> layout)
	{
		super(labelModel);	//construct the parent class
		this.layout=checkInstance(layout, "Layout cannot be null.");	//save the layout
		layout.setOwner(this);	//tell the layout which container owns it
	}

	/**Update's this object's properties.
	This method checks whether properties have been updated for this object.
	If this object's properties have not been updated, this method calls {@link #initializeProperties()}.
	This method is called for any child components before initializing the properties of the component itself,
	to assure that child property updates have already occured before property updates occur for this component.
	There is normally no need to override this method or to call this method directly by applications.
	This version checks to see if the theme needs to be applied to the given layout.
	@exception IOException if there was an error loading or setting properties.
	@see #isPropertiesInitialized()
	@see #isLayoutPropertiesInitialized()
	@see #initializeProperties()
	*/
	public void updateProperties() throws IOException
	{
		super.updateProperties();	//update the properties normally
		if(!isLayoutPropertiesInitialized())	//if the layout properties haven't yet been initialized (which also means that our version of initializeProperties() hasn't been called, or it would have updated the layout theme applied status) 
		{
			initializeProperties();	//initialize this component's properties
		}		
	}

	/**Initializes the properties of this component.
	This includes loading and applying the current theme as well as loading any preferences.
	Themes are only applied of the application is themed.
	This method may be overridden to effectively override theme settings and preference loading by ensuring the state of important properties after the default operations have occurred. 
	If properties are successfully updated, this method updates the properties initialized status.
	This version initializes the properties of the current layout and updates the layout properties initialized status.
	@exception IOException if there was an error loading or setting properties.
	@see GuiseApplication#isThemed()
	@see #applyTheme(Theme)
	@see #setPropertiesInitialized(boolean)
	@see #setLayoutPropertiesInitialized(boolean)
	*/
	public void initializeProperties() throws IOException
	{
		super.initializeProperties();	//apply properties to this component normally
		setLayoutPropertiesInitialized(true);	//indicate that we've applied the theme to the layout as well
	}

	/**Applies a theme and its parents to this component.
	The theme's rules will be applied to this component and any related objects.
	Theme application occurs unconditionally, regardless of whether themes have been applied to this component before.
	This method may be overridden to effectively override theme settings by ensuring state of important properties after theme application. 
	There is normally no need to call this method directly by applications.
	This version applies the theme to the current layout.
	@param theme The theme to apply to the component.
	*/
	public void applyTheme(final Theme theme)
	{
		super.applyTheme(theme);	//apply the theme to this component normally
		theme.apply(getLayout());	//apply the theme to the currently installed layout
	}
}
