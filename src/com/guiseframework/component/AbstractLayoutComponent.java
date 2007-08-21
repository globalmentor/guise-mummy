package com.guiseframework.component;

import java.io.IOException;

import com.guiseframework.GuiseApplication;
import com.guiseframework.component.layout.*;
import com.guiseframework.model.DefaultLabelModel;
import com.guiseframework.model.LabelModel;
import com.guiseframework.theme.Theme;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a layout component.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public abstract class AbstractLayoutComponent extends AbstractListCompositeComponent implements LayoutComponent
{

	/**Adds a child component at the specified index.
	Any class that overrides this method must call this version.
	@param index The index at which the component should be added.
	@param component The component to add to this component.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException if the installed layout does not support default constraints.
	*/
	protected boolean addComponent(final int index, final Component component)
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
	protected boolean add(final Component component, final Constraints constraints)
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
	protected boolean removeComponent(final Component component)
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
	private Layout<? extends Constraints> layout;

		/**@return The layout definition for the component.*/
		public Layout<? extends Constraints> getLayout() {return layout;}

		/**Sets the layout definition for the component.
		This is a bound property.
		The layout is marked as not yet having a theme applied, as the specific theme rules applied to the layout may depend on the layout's owner.
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
				for(final Component childComponent:getChildren())	//for each child component
				{
					newLayout.getConstraints(childComponent);	//make sure the constraints of all components are compatible with the layout TODO do we even need to do this? why not wait until later? but this may be OK---perhaps we can assume that if components are installed before the layout, they will be used with this layout and not another
				}
				setLayoutThemeApplied(false);	//indicate that a theme haven't yet been set for this layout, as the specific rules applied may depend on the layout's owner
				firePropertyChange(LAYOUT_PROPERTY, oldLayout, newLayout);	//indicate that the value changed
			}			
		}

	/**Whether a theme has been applied to this component's layout.*/
	private boolean layoutThemeApplied=false;

		/**@return Whether a theme has been applied to this component's layout.*/
		public boolean isLayoutThemeApplied() {return layoutThemeApplied;}

		/**Sets whether a theme has been applied to this component's layout.
		This is a bound property of type {@link Boolean}.
		@param newLayoutThemeApplied <code>true</code> if a theme has been applied to this component's layout, else <code>false</code>.
		@see #LAYOUT_THEME_APPLIED_PROPERTY
		@see #setThemeApplied(boolean)
		*/
		public void setLayoutThemeApplied(final boolean newLayoutThemeApplied)
		{
			if(layoutThemeApplied!=newLayoutThemeApplied)	//if the value is really changing
			{
				final boolean oldLayoutThemeApplied=layoutThemeApplied;	//get the current value
				layoutThemeApplied=newLayoutThemeApplied;	//update the value
				firePropertyChange(LAYOUT_THEME_APPLIED_PROPERTY, Boolean.valueOf(oldLayoutThemeApplied), Boolean.valueOf(newLayoutThemeApplied));
			}
		}

	/**Layout constructor with a default label model.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public AbstractLayoutComponent(final Layout<? extends Constraints> layout)
	{
		this(new DefaultLabelModel(), layout);	//construct the class with a default label model
	}

	/**Label model and layout constructor.
	@param labelModel The component label model.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given label model and/or layout is <code>null</code>.
	*/
	public AbstractLayoutComponent(final LabelModel labelModel, final Layout<? extends Constraints> layout)
	{
		super(labelModel);	//construct the parent class
		this.layout=checkInstance(layout, "Layout cannot be null.");	//save the layout
		layout.setOwner(this);	//tell the layout which container owns it
	}

	/**Update's this object's theme.
	This method checks whether a theme has been applied to this object.
	If a theme has not been applied to this object this method calls {@link #applyTheme()}.
	This method is called for any child components before initializing applying the theme to the component itself,
	to assure that child theme updates have already occured before theme updates occur for this component.
	There is normally no need to override this method or to call this method directly by applications.
	This version checks to see if the theme needs to be applied to the given layout.
	@exception IOException if there was an error loading or applying a theme.
	@see #isThemeApplied()
	@see #isLayoutThemeApplied()
	@see #applyTheme()
	*/
	public void updateTheme() throws IOException
	{
		super.updateTheme();	//update the theme
		if(!isLayoutThemeApplied())	//if the theme haven't yet been applied to the layout (which also means that our version of applyTheme() hasn't been called, or it would have updated the layout theme applied status) 
		{
			applyTheme();	//apply the theme to this component
		}		
	}

	/**Applies the theme to this objectd.
	Themes are only applied of the application is themed.
	This method may be overridden to effectively override theme settings by ensuring the state of important properties after the theme has been set. 
	If the theme is successfully applied, this method updates the theme applied status.
	This version applies the theme to the current layout and updates the layout theme applied status.
	@exception IOException if there was an error loading or applying a theme.
	@see GuiseApplication#isThemed()
	@see #getTheme()
	@see #applyTheme(Theme)
	@see #setThemeApplied(boolean)
	@see #setLayoutThemeApplied(boolean)
	*/
	public void applyTheme() throws IOException
	{
		super.applyTheme();	//apply the theme to this component normally
		setLayoutThemeApplied(true);	//indicate that we've applied the theme to the layout as well
	}

	/**Applies a theme and its parents to this object.
	The theme's rules will be applied to this object and any related objects.
	Theme application occurs unconditionally, regardless of whether themes have been applied to this component before.
	There is normally no need to call this method directly by applications.
	This version applies the theme to the current layout.
	@param theme The theme to apply to the object.
	*/
	public void applyTheme(final Theme theme)
	{
		super.applyTheme(theme);	//apply the theme to this component normally
		theme.apply(getLayout());	//apply the theme to the currently installed layout
	}

}
