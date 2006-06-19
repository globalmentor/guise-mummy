package com.guiseframework.model;

/**A transition in a sequence.
@author Garret Wilson
*/
public enum SequenceTransition
{

	/**Start the sequence.*/
	START,

	/**Transition to the first item in a sequence.*/
	FIRST,
	
	/**Transition to the previous item in a sequence.*/
	PREVIOUS,
	
	/**Transition to the current item in a sequence.*/
	CURRENT,

	/**Transition to the next item in a sequence.*/
	NEXT,

	/**Transition to the last item in a sequence.*/
	LAST,

	/**Finish the sequence.*/
	FINISH;
}
