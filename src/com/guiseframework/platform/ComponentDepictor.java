package com.guiseframework.platform;

import com.guiseframework.component.Component;

/**A strategy for depicting components on some platform.
All component depictors must implement this interface.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public interface ComponentDepictor<C extends Component> extends Depictor<C>
{
}
