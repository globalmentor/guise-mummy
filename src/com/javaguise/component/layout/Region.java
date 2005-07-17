package com.javaguise.component.layout;

/**Indicates a region of a larger area in internationalized relative terms.
@author Garret Wilson
*/
public enum Region
{

	/**At the beginning of a line; "left" in left-to-right, top-to-botom orientation.*/
	LINE_START,

	/**At the end of a line; "right" in left-to-right, top-to-botom orientation.*/
	LINE_END,

	/**At the beginning of a page; "top" in left-to-right, top-to-botom orientation.*/
	PAGE_START,

	/**At the end of a page; "bottom" in left-to-right, top-to-botom orientation.*/
	PAGE_END,
	
	/**In the center of the region.*/
	CENTER;

}
