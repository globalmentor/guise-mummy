/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.event;

/**An event indicating an action should take place.
The event target indicates the component that originally initiated the action.
@author Garret Wilson
@see ActionListener
*/
public class ActionEvent extends AbstractTargetedGuiseEvent
{

	/**The default action option.*/
	public final static int DEFAULT_OPTION=0;

	/**The default action force.*/
	public final static int DEFAULT_FORCE=1;

	/**The commands that can be represented by an action.*/
	public enum Command
	{
		/**The action requests an item to be selected.
		Traditionally this is indicated by a left mouse button single click.
		*/
		SELECT,
		/**The action requests contextual information.
		Traditionally this is indicated by a right mouse button single click.
		*/
		INFO,
		/**The action requests activitation,
		Traditionally this is indicated by a left mouse button double click.
		*/
		ACTIVATE;
	};
	
	/**The zero-based option indicated by this action.*/
	private final int option;

		/**Returns the option indicated by this action.
		The option is zero-based and represents any alternate option indicated by the user.
		If the action was initiated by a mouse click, for instance, the left button traditionally will indicate the default option (0),
		while the right button will indicate a secondary option (1).
		*/
		public int getOption() {return option;}

	/**The force with which the action was initiated.*/
	private final int force;

		/**Returns the force with which the action was initiated.
		A force of zero indicates no force.
		A mouse single click should generate a force of 1, while a double single click should generate a force of 2.
		*/
		public int getForce() {return force;}

	/**Determines the conventional command represented by this action.
	@return The conventional command represented by this action.
	 */
	public Command getCommand()
	{
		switch(getForce())	//check the force
		{
			case DEFAULT_FORCE:	//if the default force (0) is used
				switch(getOption())	//see which option was requested
				{
					case DEFAULT_OPTION:	//if the default option (0) was requested
						return Command.SELECT;	//a simple selection is intended
					default:	//if any other option was used
						return Command.INFO;	//information was requested
				}
			default:	//if a stronger force is used
				return Command.ACTIVATE;	//activate was intended
		}
	}

	/**Source constructor with a default force and option.
	The target will be set to be the same as the given source.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ActionEvent(final Object source)
	{
		this(source, DEFAULT_FORCE, DEFAULT_OPTION);	//construct the class with the default force and option
	}

	/**Source, force, and option constructor.
	The target will be set to be the same as the given source.
	@param source The object on which the event initially occurred.
	@param force The zero-based force, such as 0 for no force or 1 for a mouse single click.
	@param option The zero-based option, such as 0 for a mouse left button click or 1 for a mouse right button click.
	@exception NullPointerException if the given source and/or target is <code>null</code>.
	@exception IllegalArgumentException if the given force and/or option is negative.
	*/
	public ActionEvent(final Object source, final int force, final int option)
	{
		this(source, source, force, option);	//construct the class with the same target as the source
	}

	/**Source, target, force, and option constructor.
	@param source The object on which the event initially occurred.
	@param target The target of the event.
	@param force The zero-based force, such as 0 for no force or 1 for a mouse single click.
	@param option The zero-based option, such as 0 for a mouse left button click or 1 for a mouse right button click.
	@exception NullPointerException if the given source and/or target is <code>null</code>.
	@exception IllegalArgumentException if the given force and/or option is negative.
	*/
	public ActionEvent(final Object source, final Object target, final int force, final int option)
	{
		super(source, target);	//construct the parent class
		if(force<0)	//if the force is negative
		{
			throw new IllegalArgumentException("Force cannot be negative: "+force);
		}
		this.force=force;	//save the option
		if(option<0)	//if the option is negative
		{
			throw new IllegalArgumentException("Option cannot be negative: "+option);
		}
		this.option=option;	//save the option
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@param actionEvent The event the properties of which will be copied.
	@exception NullPointerException if the given source and/or event is <code>null</code>.
	*/
	public ActionEvent(final Object source, final ActionEvent actionEvent)
	{
		this(source, actionEvent.getTarget(), actionEvent.getForce(), actionEvent.getOption());	//construct the class with the same target		
	}

}
