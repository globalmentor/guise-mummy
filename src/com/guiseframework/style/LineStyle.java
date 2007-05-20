package com.guiseframework.style;

/**The style of a line, such as used for a border or divider.
@author Garret Wilson
@see <a href="http://www.w3.org/TR/css3-background/#border-style">CSS3 Backgrounds and Borders Module: border-style</a>
*/
public enum LineStyle
{
	/**A series of dots.*/
	DOTTED,
	/**A series of dashes.*/
	DASHED,
	/**A single line segment.*/
	SOLID,
	/**Two parallel solid lines with some spaces between them, the lines together with the space equalling any line width specified.*/
	DOUBLE,
	/**Alternating dots and dashes.*/
	DOT_DASH,
	/**Two dots and a dash.*/
	DOT_DOT_DASH,
	/**A wavy line.*/
	WAVE,
	/**Effect emulating carving on a canvas; typically achieved by using two colors slightly lighter and darker than the line color.*/
	GROOVE,
	/**Effect simulating a line coming out of the canvas.*/
	RIDGE,
	/**Effect simulating a sunken canvas.*/
	INSET,
	/**Effect simulating a raised canvas.*/
}
