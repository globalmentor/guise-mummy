package com.guiseframework.prototype;

import java.util.Set;

import com.globalmentor.beans.PropertyBindable;

import static com.globalmentor.java.Classes.*;

/**An object that provides prototypes.
When the available prototypes change, a {@link #PROTOTYPE_PROVISIONS_PROPERTY} property change event is fired.
Typically provided prototypes are merged with other prototypes into menus and/or toolbars.
@author Garret Wilson
*/
public interface PrototypeProvider extends PropertyBindable
{

	/**The prototype provisions property.*/
	public final static String PROTOTYPE_PROVISIONS_PROPERTY=getPropertyName(PrototypeProvider.class, "prototypeProvisions");

	/**Returns the prototypes provisions currently provided by this provider.
	This is a read-only bound property.
	@return The prototypes provisions currently provided by this provider.
	@see #PROTOTYPE_PROVISIONS_PROPERTY
	*/
	public Set<PrototypeProvision<?>> getPrototypeProvisions();

}
