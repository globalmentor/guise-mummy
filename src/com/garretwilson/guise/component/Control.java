package com.garretwilson.guise.component;

import com.garretwilson.guise.model.Model;

/**A component that accepts user interaction to manipulate a data model.
@author Garret Wilson
*/
public interface Control<M extends Model> extends ModelComponent<M>
{
}
