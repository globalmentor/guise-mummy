package com.guiseframework.platform.web;

import java.util.Collections;
import java.util.Queue;

import com.guiseframework.platform.*;

/**An abstract depictor for the web.
@param <O> The type of object being depicted.
@author Garret Wilson
*/
public abstract class AbstractWebDepictor<O extends DepictedObject> extends AbstractDepictor<O>
{

	/**@return The web platform on which this depictor is depicting ojects.*/
	public GuiseWebPlatform getPlatform() {return (GuiseWebPlatform)super.getPlatform();}

//TODO fix	private final Queue<E>
}
