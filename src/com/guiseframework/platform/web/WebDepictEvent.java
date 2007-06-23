package com.guiseframework.platform.web;

import com.guiseframework.platform.*;

/**An event to or from a depicted object on the web platform.
The source of the event is the depicted object.
@param <O> The type of depicted object.
@author Garret Wilson
*/
public interface WebDepictEvent<O extends DepictedObject> extends DepictEvent<O>
{

}
