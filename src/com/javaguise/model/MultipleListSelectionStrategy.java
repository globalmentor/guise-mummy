package com.javaguise.model;

/**An implementation of a list selection strategy for a list select model allowing unlimited selections.
This class is marked final because it demarcates certain selection semantics that, if they can be assumed, may be offloaded to a component's view in certain circumstances. 
This class is thread-safe, and assumes that the corresponding select model is thread-safe, synchronized on itself.
@param <V> The type of values contained in the select model.
@author Garret Wilson
@see ListSelectModel
*/
public final class MultipleListSelectionStrategy<V> extends AbstractListSelectionStrategy<V>
{
}
