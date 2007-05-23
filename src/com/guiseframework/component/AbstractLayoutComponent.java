package com.guiseframework.component;

import java.io.IOException;

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

	/**Adds a component to the layout component with default constraints.
	@param component The component to add.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException if the installed layout does not support default constraints.
	*/
	protected boolean addComponent(final Component<?> component)
	{
//TODO del when works		return add(component, null);	//add the component, indicating default constraints should be used
		if(super.addComponent(component))	//add the component normally; if the child components changed
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
		@see #setThemeApplied(boolean) 
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
				setLayoutThemeApplied(false);	//indicate that the theme hasn't yet been set to this layout, as the specific rules applied may depend on the layout's owner
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

	/**Update's this component's theme.
	This method checks whether a theme has been applied to this component.
	If no theme has been applied to the component, the current session theme will be applied by delegating to {@link #applyTheme(Theme)}.
	If the theme is successfully applied, this method updates the theme applied status.
	This method is called recursively for any child components before applying any theme on the component itself,
	to assure that child theme updates have already occured before theme updates occur for this component.
	There is normally no need to override this method or to call this method directly by applications.
	This version checks to see if the theme needs to be applied to the given layout.
	@exception IOException if there was an error loading or applying the theme.
	@see #applyTheme(Theme)
	@see #isThemeApplied()
	@see GuiseSession#getTheme()
	*/
	public void updateTheme() throws IOException
	{
		super.updateTheme();	//update the theme normally
		if(!isLayoutThemeApplied())	//if the theme hasn't been applied to the layout (which also means that our version of applyTheme() hasn't been called, or it would have updated the layout theme applied status) 
		{
			applyTheme(getSession().getTheme());	//get the theme and apply it
		}		
	}

	/**Applies a theme and its parents to this component.
	The theme's rules will be applied to this component and any related objects.
	Theme application occurs unconditionally, regardless of whether themes have been applied to this component before.
	This method may be overridden to effectively override theme settings by ensuring state of important properties after theme application. 
	There is normally no need to call this method directly by applications.
	If the theme is successfully applied, this method updates the theme applied status.
	This version applies the theme to the current layout and update's the layout theme applied status.
	@param theme The theme to apply to the component.
	@see #setThemeApplied(boolean)
	@see #setLayoutThemeApplied(boolean)
	*/
	public void applyTheme(final Theme theme)
	{
		super.applyTheme(theme);	//apply the theme to this component normally
		theme.apply(getLayout());	//apply the theme to the currently installed layout
		setLayoutThemeApplied(true);	//indicate that we've applied the theme to the layout as well
	}

}
