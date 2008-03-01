package com.guiseframework.prototype;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.util.AbstractHashObject;
import com.globalmentor.util.Debug;

/**Prototype provision information indicating order, hierarchy, and location for generated components.
This description is usually used to generate components and place them in menus and/or toolbars. 
@param <P> The type of prototype being provided.
@author Garret Wilson
*/
public class PrototypeProvision<P extends Prototype> extends AbstractHashObject implements Comparable<PrototypeProvision<P>>
{

	/**No order.*/
	public final static double NO_ORDER=-1;
	/**The minimum allowed order.*/
	public final static double MIN_ORDER=0;
	/**The maximum allowed order.*/
	public final static double MAX_ORDER=Double.MAX_VALUE;

		//top-level prototype orders
	public final static double RESOURCE_MENU_ORDER=100;
	public final static double EDIT_MENU_ORDER=200;
	public final static double INSERT_MENU_ORDER=300;
	public final static double VIEW_MENU_ORDER=400;
	public final static double TOOL_MENU_ORDER=500;
	public final static double CONFIGURE_MENU_ORDER=600;
	public final static double WINDOW_MENU_ORDER=700;
	public final static double HELP_MENU_ORDER=9900;

		//resource menu order
	public final static double RESOURCE_MENU_NEW_ORDER=RESOURCE_MENU_ORDER+1;
	public final static double RESOURCE_MENU_ADD_ORDER=RESOURCE_MENU_ORDER+2;
	public final static double RESOURCE_MENU_OPEN_ORDER=RESOURCE_MENU_ORDER+3;
	public final static double RESOURCE_MENU_CLOSE_ORDER=RESOURCE_MENU_ORDER+4;
	public final static double RESOURCE_MENU_EDIT_ORDER=RESOURCE_MENU_ORDER+5;
	public final static double RESOURCE_MENU_SAVE_ORDER=RESOURCE_MENU_ORDER+6;
	public final static double RESOURCE_MENU_SAVE_AS_ORDER=RESOURCE_MENU_ORDER+7;
	public final static double RESOURCE_MENU_SAVE_ALL_ORDER=RESOURCE_MENU_ORDER+8;
	public final static double RESOURCE_MENU_REVERT_ORDER=RESOURCE_MENU_ORDER+9;
	public final static double RESOURCE_MENU_MOVE_ORDER=RESOURCE_MENU_ORDER+10;
	public final static double RESOURCE_MENU_RENAME_ORDER=RESOURCE_MENU_ORDER+11;
	public final static double RESOURCE_MENU_REFRESH_ORDER=RESOURCE_MENU_ORDER+12;
	public final static double RESOURCE_MENU_DELETE_ORDER=RESOURCE_MENU_ORDER+13;
	public final static double RESOURCE_MENU_PREVIOUS_ORDER=RESOURCE_MENU_ORDER+14;
	public final static double RESOURCE_MENU_RECEDE_ORDER=RESOURCE_MENU_ORDER+15;
	public final static double RESOURCE_MENU_PLAY_ORDER=RESOURCE_MENU_ORDER+16;
	public final static double RESOURCE_MENU_PAUSE_ORDER=RESOURCE_MENU_ORDER+17;
	public final static double RESOURCE_MENU_RECORD_ORDER=RESOURCE_MENU_ORDER+18;
	public final static double RESOURCE_MENU_STOP_ORDER=RESOURCE_MENU_ORDER+19;
	public final static double RESOURCE_MENU_ADVANCE_ORDER=RESOURCE_MENU_ORDER+20;
	public final static double RESOURCE_MENU_NEXT_ORDER=RESOURCE_MENU_ORDER+21;
	public final static double RESOURCE_MENU_RETRIEVE_ORDER=RESOURCE_MENU_ORDER+22;
	public final static double RESOURCE_MENU_PROPERTIES_ORDER=RESOURCE_MENU_ORDER+23;
	public final static double RESOURCE_MENU_EXIT_ORDER=RESOURCE_MENU_ORDER+99;

		//help prototype orders
	public final static double HELP_MENU_ABOUT_ORDER=HELP_MENU_ORDER+1;

	/**The prototype description's parent, or <code>null</code> if the prototype description has no parent.*/
	private final PrototypeProvision<?> parentPrototypeProvision;

		/**@return The prototype's parent, or <code>null</code> if the prototype description has no parent.*/
		public PrototypeProvision<?> getParentPrototypeProvision() {return parentPrototypeProvision;}

	/**The prototype being described.*/
	private final P prototype;

		/**@return The prototype being described.*/
		public P getPrototype() {return prototype;}

	/**The order of the prototype.*/
	private final double order;

		/**@return The order of the prototype.*/
		public double getOrder() {return order;}

	/**Whether this prototype should be used in a menu if available.*/ 
	private final boolean isMenu;

		/**@return Whether this prototype should be used in a menu if available.*/ 
		public boolean isMenu() {return isMenu;}

	/**Whether this prototype should be used in a toolbar if available.*/ 
	private final boolean isTool;

		/**@return Whether this prototype should be used in a toolbar if available.*/ 
		public boolean isTool() {return isTool;}

	/**Prototype constructor with no prototype description parent.
	@param prototype The prototype.
	@param order The order of the prototype.
	@param isMenu Whether this prototype should be used in a menu if available.
	@param isTool Whether this prototype should be used in a toolbar if available.
	@exception NullPointerException if the given prototype is <code>null</code>.
	@exception IllegalArgumentException if the given order is not {@value #NO_ORDER} and does not come between {@value #MIN_ORDER} and {@value #MAX_ORDER}, inclusive.
	*/
	public PrototypeProvision(final P prototype, final double order, final boolean isMenu, final boolean isTool)
	{
		this(null, prototype, order, isMenu, isTool);	//construct the class with no parent prototype description
	}

	/**Parent prototype description and prototype constructor.
	@param parentPrototypeProvision The prototype description's parent, or <code>null</code> if the prototype description has no parent.
	@param prototype The prototype.
	@param order The order of the prototype.
	@param isMenu Whether this prototype should be used in a menu if available.
	@param isTool Whether this prototype should be used in a toolbar if available.
	@exception NullPointerException if the given prototype is <code>null</code>.
	@exception IllegalArgumentException if the given order is not {@value #NO_ORDER} and does not come between {@value #MIN_ORDER} and {@value #MAX_ORDER}, inclusive.
	*/
	public PrototypeProvision(final PrototypeProvision<?> parentPrototypeProvision, final P prototype, final double order, final boolean isMenu, final boolean isTool)
	{
		super(parentPrototypeProvision, checkInstance(prototype, "Prototype cannot be null."), Double.valueOf(order));	//construct the parent class
		if(order!=NO_ORDER && (order<MIN_ORDER || order>MAX_ORDER))	//if the order is invalid
		{
			throw new IllegalArgumentException("Invalid order: "+order);
		}
		this.parentPrototypeProvision=parentPrototypeProvision;
		this.prototype=prototype;
		this.order=order;
		this.isMenu=isMenu;
		this.isTool=isTool;
	}

	/**Compares this object with the specified object for order.
	@param prototypeDescription The object to be compared.
	@return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	*/
	public int compareTo(final PrototypeProvision<P> prototypeDescription)
	{
		int value=Double.compare(getOrder(), prototypeDescription.getOrder());	//compare the orders
		if(value==0 && !equals(prototypeDescription))	//if the orders were the same but the objects are not equal
		{
				//TODO compare the labels
			value=hashCode()-prototypeDescription.hashCode();	//create an arbitrary ordering based upon the hash code
			if(value==0)	//if the hash codes are the same but the objects are not equal, the JVM must have assigned two distinct objects identical hash codes; this shouldn't bring down the application, but if it happens frequently this logic should be updated
			{
				Debug.warn("Distinct prototype description not comparing correctly.");	//TODO add creation order property to all prototypes to aid in comparison
			}
		}
		return value;	//return the comparation value
	}
}
