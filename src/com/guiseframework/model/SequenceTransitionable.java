package com.guiseframework.model;

/**An object that can be transitioned in a sequence.
@author Garret Wilson
*/
public interface SequenceTransitionable
{

	/**Determines whether transition can occur to the given relative index.
	@param indexDelta The index relative to this position in the sequence to which transition will occur.
	@return <code>true</code> if transition can occur, else <code>false</code> if the transition should not occur.
	*/
	public boolean canTransition(final int indexDelta);

}
