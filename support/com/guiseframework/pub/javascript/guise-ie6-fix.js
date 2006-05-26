/**Guise(TM) IE6 bug-fix JavaScript routines
Copyright (c) 2005-2006 GlobalMentor, Inc.
This file should be included after the main guise.js file and after all stylesheet references.
If a stylesheet has already been fixed by the server, its first style selector text will be "guiseIE6Fix".
*/

//TODO add support for pseudo elements

/**A class for representing a selector for a single element (which can be a component in a nested element selector).
This class can automatically determine if the element selector has already been fixed for IE6.
@param selectorText The string form of the element selector.
var selectorText The string form of the element selector.
var ie6FixSelectorText The selector text to use in IE6, in the form element.guiseIE6_-class1_-class2_-class3:pseudoClass, with the class selectors in alphabetical order.
var ie6FixClassName The selector class to use in an element on IE6, in the form guiseIE6_-class1_-class2_-class3:pseudoClass, with the class selectors in alphabetical order, or null if no special selector class is needed on IE6.
var elementName The name of the element selected, or null if no element name was selected; this name could be in proper case if loaded manually, or in uppercase if derived from IE stylesheet.
var classes The classes selected by the selector.
var pseudoClass The pseudo-class specified by the selector, or null if no pseudo-class was specified.
var isMultipleClassSelector Whether this element selector selects on multiple classes.
*/
function CSSElementSelector(selectorText)
{
	this.selectorText=selectorText;	//save the selector text
	var selectorTextPseudoClass=selectorText.split(":");	//split out the pseudo-class, if there is one
	var bareSelectorText=selectorTextPseudoClass[0];	//get the selector text without a pseudo class
		//TODO assert selectorTextPseudoClass.length==1 || selectorTextPseudoClass.length==2
	this.pseudoClass=selectorTextPseudoClass.length>1 ? selectorTextPseudoClass[1] : null;	//save the pseudo-class, if there is one
	this.classes=bareSelectorText.split(".");	//get the name and classes of this selector
//TODO del	var elementName=this.classes.remove(0).toLowerCase();	//the first "selector" was an element name, so remove it (changing it to lowercase in case some browser put it in uppercase)
	var elementName=this.classes.remove(0);	//the first "selector" was an element name, so remove it
	this.elementName=elementName ? elementName : null;	//if there was no element name (i.e. the string started with "."), the element name will be empty
	if(this.classes.length>1)	//if there are multiple classes being selected
	{
		this.classes.sort();	//sort the classes in alphabetical order
		this.ie6FixClassName=GuiseIE6Fix.GUISE_IE6_FIX_CLASS_EXTENDED_PREFIX+this.classes.join(GuiseIE6Fix.GUISE_IE6_FIX_CLASS_DELIMITER);	//create a new class in the form guiseIE6_-class1_-class2_-class3
		this.ie6FixSelectorText=elementName+"."+this.ie6FixClassName;	//create new selector text in the form elementName.guiseIE6_-class1_-class2_-class3 (use the removed element name, which is never null)
		if(this.pseudoClass!=null)	//if there was a pseudo-class
		{
			this.ie6FixSelectorText+=":"+this.pseudoClass;	//append the pseudo-class to the IE6 fix selector
		}
	}
	else	//if multiple classes are not being selected
	{
		if(this.classes.length==1 && this.classes[0].startsWith(GuiseIE6Fix.GUISE_IE6_FIX_CLASS_EXTENDED_PREFIX))	//if there is a single class selected that has been fixed for IE6, do the reverse operation to get the original classes
		{
			this.ie6FixClassName=this.classes[0];	//the class is already fixed for IE6
//TODO del alert("original fixed class name: "+this.ie6FixClassName);
			this.ie6FixSelectorText=elementName+"."+this.ie6FixClassName;	//create new selector text in the form elementName.guiseIE6_-class1_-class2_-class3 (use the removed element name, which is never null)
			if(this.pseudoClass!=null)	//if there was a pseudo-class
			{
				this.ie6FixSelectorText+=":"+this.pseudoClass;	//append the pseudo-class to the IE6 fix selector
			}
//TODO del alert("fix selector text: "+this.ie6FixSelectorText);
			var className=this.ie6FixClassName.substring(GuiseIE6Fix.GUISE_IE6_FIX_CLASS_EXTENDED_PREFIX.length);	//remove the prepended identifier
			this.classes=className.split(GuiseIE6Fix.GUISE_IE6_FIX_CLASS_DELIMITER);	//split out the original classes
			for(var i=0; i<this.classes.length; ++i)
			{
//TODO del alert("found class: "+i+" is: "+this.classes[i]);
			}
			
			this.selectorText=elementName+"."+this.classes.join(".");	//create the original selector text (use the removed element name, which is never null)
			if(this.pseudoClass!=null)	//if there was a pseudo-class
			{
				this.selectorText+=":"+this.pseudoClass;	//append the pseudo-class to the original selector
			}
//TODO del alert("this has been Guise IE6 fixed; new selector text: "+this.selectorText);
		}
		else	//if this was not a fixed IE6 class
		{
			this.ie6FixClassName=null;	//don't create a special IE 6 class
			this.ie6FixSelectorText=this.selectorText;	//use the standard selector text
		}
	}
	this.isMultipleClassSelector=this.classes.length>1;	//determine if the element selectors selects on multiple classes

	if(!CSSElementSelector.prototype._initialized)
	{
		CSSElementSelector.prototype._initialized=true;

		/**Determines if the given element is selected by this element selector.
		@param element The element to check.
		@param elementClassNameSet The optional associative array of element class name keys with true values; if not included, they will be calculated from the given element.
		@return true if this selector selects the given element.
		*/
		CSSElementSelector.prototype.selects=function(element, elementClassNameSet)
		{
			if(this.elementName!=null && this.elementName.toLowerCase()!=element.nodeName.toLowerCase())	//if this selector doesn't match the element name
				return false;	//the element name doesn't match
			if(!elementClassNameSet)	//if no element class names have been calculated
			{
				var elementClassName=element.className;	//get the element class name
				elementClassNameSet=elementClassName ? elementClassName.splitSet(/\s/) : new Object();	//split out the class names into a set
			}
			for(var i=this.classes.length-1; i>=0; --i)	//for each class
			{
				if(!elementClassNameSet[this.classes[i]])	//if this class name isn't included
				{
					return false;	//this element is missing a class name
				}
			}
			return true;	//the element passed all the tests
		};

	}
}

/**A class for representing a selector.
@param selectorText The string form of the selector.
var selectorText The string form of the selector.
var elementSelectors The CSSElementSelector objects representing each element selector in the selector.
var isMultipleClassSelector Whether one of the element selectors selects on multiple classes.
*/
function CSSSelector(selectorText)
{
	this.selectorText=selectorText;	//save the selector text
	var elementSelectors=selectorText.split(/\s/);
	this.elementSelectors=new Array(elementSelectors.length);	//create a new array in which to hold each element selector object
	this.isMultipleClassSelector=false;	//we'll determine if one of the element selectors selects on multiple classes
	var ie6FixSelectorTexts=new Array();	//create an array of IE6 selector text
	for(var elementSelectorIndex=0; elementSelectorIndex<elementSelectors.length; ++elementSelectorIndex)
	{
		var elementSelector=new CSSElementSelector(elementSelectors[elementSelectorIndex]);	//create an element selector object from this element selector string
		this.elementSelectors[elementSelectorIndex]=elementSelector;	//save the element selector
		ie6FixSelectorTexts.add(elementSelector.ie6FixSelectorText);	//save the IE6 selector text for this selector
		if(elementSelector.isMultipleClassSelector)	//if this element selector selects on multiple classes
		{
			this.isMultipleClassSelector=true;	//show that this is a multiple-class selector
		}
	}
	this.ie6FixSelectorText=ie6FixSelectorTexts.join(" ");	//join the element IE6 selector texts with spaces to get the overall IE6 selector text
}

/**

var cssMultiClassSelectors: An array of CSSSelector objects that include a multiple class selection.
*/
function GuiseIE6Fix()
{

	this.cssMultipleClassElementSelectors=new Array();	//create a new array of element selectors that select on multiple classes

	//TODO del var selectorText="a b.c d.e.f .g";

	if(!GuiseIE6Fix.prototype._initialized)
	{
		GuiseIE6Fix.prototype._initialized=true;

		/**Check to see if this element is affected by IE6 selection bugs and, if so, updates the classes so that the class may be properly selected.
		@param element The element to fix.
		*/
		GuiseIE6Fix.prototype.fixElementClassName=function(element)
		{
			var fixedElementClassName=this.getFixedElementClassName(element);	//get the fixed version of the class name
			if(fixedElementClassName!=null)	//if the class name changed
			{
				element.className=fixedElementClassName;	//update the element class name
//TODO fix alert("affected element "+element.nodeName+" id "+element.id+" with new class "+element.className);
			}
		};

		/**Retrieves the fixed form of the element class name.
		@param element The element to fix.
		@param elementClassName The optional proposed class name of the element; if no element name is provided, the element's current class name will be used.
		@return The fixed class name, if different from the proposed class name, or null if the fixed element class name would be no different than the proposed class name.
		*/
		GuiseIE6Fix.prototype.getFixedElementClassName=function(element, elementClassName)
		{
			if(typeof elementClassName=="undefined" || elementClassName==null)	//if no class name was provided
			{
				elementClassName=element.className;	//get the class name from the element
			}
			if(elementClassName=="")	//if there is no element class name, multiple selectors are irrelevent, so don't do any further checks
			{
				return null;	//indicate that there should be no changes
			}
			var classNameModified=false;	//we'll note whether we modifiy the class names
			var elementClassNameSet=elementClassName.splitSet(/\s/);	//split out the class names into a set
				//remove the IE6 fix classes, if any are already there
			for(var className in elementClassNameSet)	//for each class name
			{
				if(className.startsWith(GuiseIE6Fix.GUISE_IE6_FIX_CLASS_PREFIX))	//if this is a special Guise IE6 fix class name
				{
					delete elementClassNameSet[className];	//remove this special Guise IE6 fix class name
					classNameModified=true;	//note the we modified the class name
				}
			}
			var newClassNames=null;	//we'll create an array of new class names if we have to add fixed element selectors
			for(var elementSelectorIndex=this.cssMultipleClassElementSelectors.length-1; elementSelectorIndex>=0; --elementSelectorIndex)	//for each multiple class element selector
			{
				var elementSelector=this.cssMultipleClassElementSelectors[elementSelectorIndex];	//get a reference to this element selector
				if(elementSelector.selects(element, elementClassNameSet))	//if this element selector selects this element
				{
					if(newClassNames==null)	//if there are no new class names
					{
						newClassNames=new Array();	//create the array of new class names
					}
					newClassNames.add(elementSelector.ie6FixClassName);	//add the IE6 fix class for this selector						
					classNameModified=true;	//note the we modified the class name
				}
			}
			if(classNameModified)	//if we touched up the class names
			{
				if(newClassNames==null)	//if there are no new class names
				{
					newClassNames=new Array();	//create the array of new class names
				}
				for(var className in elementClassNameSet)	//for each remaining class name
				{
					newClassNames.add(className);	//add this class name to the array
				}
				return newClassNames.join(" ");	//join the new class names back together and return them
//TODO fix alert("affected element "+element.nodeName+" id "+element.id+" with new class "+element.className);
			}
			else	//if we didn't modify the class names
			{
				return null;	//indicate that nothing changed
			}
		};

		/**Fixes a stylesheet, and all its imported stylesheets.
		@param stylesheet The styleheet to fix.
		*/
		GuiseIE6Fix.prototype._fixStylesheet=function(stylesheet)
		{
			var rules=stylesheet.cssRules ? stylesheet.cssRules : stylesheet.rules;	//get a reference to the stylesheet rules, compensating for IE
			var guiseIE6Fixed=rules.length>0 && rules[0].selectorText==GuiseIE6Fix.GUISE_IE6_FIX_SELECTOR_TEXT;	//see if this stylesheet has already been fixed by the server
			if(guiseIE6Fixed)	//if this stylesheet has already been fixed, go through all the styles and create multiple class selector objects as appropriate to be used for dynamic fixups
			{
//TODO del alert("already been fixed; no need to reload");
				for(var ruleIndex=0; ruleIndex<rules.length; ++ruleIndex)	//for each rule in this stylesheet
				{
					var rule=rules[ruleIndex];	//get a reference to this rule
					var selectorText=rule.selectorText;	//get the selector text from the stylesheet, which we can trust because it has been fixed
//TODO del alert("looking at selector: "+selectorText);
					var selector=new CSSSelector(selectorText);	//create a selector from this selector text
					if(selector.isMultipleClassSelector)	//if this is a multi-class selector
					{
//TODO del alert("adding multiple class selector: "+selector.selectorText);
						var elementSelectors=selector.elementSelectors;	//get the element selectors for this selector
						for(var elementSelectorIndex=elementSelectors.length-1; elementSelectorIndex>=0; --elementSelectorIndex)	//for each element selector
						{
							var elementSelector=elementSelectors[elementSelectorIndex];	//get a reference to this element selector
							if(elementSelector.isMultipleClassSelector)	//if this is a multi-class element selector
							{
								this.cssMultipleClassElementSelectors.add(elementSelector);	//add this element selector to our list of multiple class element selectors; we don't need to change the stylesheet
							}
						}
					}
				}
			}
			else	//if we need to fix this stylesheet (which takes much longer from the client side, because we'll have to reload the stylesheet manually and process it)
			{
				var httpCommunicator=new HTTPCommunicator();	//create a communicator to connect back to the server
				var xmlHTTP=httpCommunicator.get(stylesheet.href);	//load this stylesheet
				if(xmlHTTP.status==200)	//if we were able to load the stylesheet
				{
					var stylesheetText=xmlHTTP.responseText;	//get the text of the stylesheet
		//TODO del alert("finished communicating; stylesheet text: "+stylesheetText);
					var noComments=stylesheetText.replace(/\/\*(.|[\r\n])*?\*\//gm, "");	//remove comments (see http://ostermiller.org/findcomment.html)
		//TODO del alert("no comments: "+noComments);
					var noDefinitions=noComments.replace(/\{(.|[\r\n])*?\}/gm, ",");	//replace definitions with commas
		//TODO del alert("no definitions: "+noDefinitions);
					var noImports=noDefinitions.replace(/@import.*;/gm, "");	//remove stylesheet imports
	//TODO del alert("no imports: "+noImports);
					var selectors=noImports.split(/\s*,\s*/m);	//split out the selectors
					var selectorCount=selectors.length==1 && selectors[0].trim().length==0 ? 0 : selectors.length;	//compensate for the special case of no selectors, which would still leave us with one blank selector
					if(selectorCount!=rules.length)	//if we don't recognize as many selectors as IE recognizes, we're in trouble
					{
						alert("We found selector count "+selectors.length+", but IE has rule count: "+rules.length);	//TODO fix
						return;	//don't process this stylesheet further
					}
					for(var ruleIndex=0; ruleIndex<rules.length; ++ruleIndex)	//for each rule in this stylesheet
					{
						var rule=rules[ruleIndex];	//get a reference to this rule
		//TODO fix					var selectorText=rule.selectorText;	//get the selector text
						var selectorText=selectors[ruleIndex];	//get our own version of the selector text, because IE throws away some information for multiple-class selectors
		//alert("looking at selector: "+selectorText);	
						var selector=new CSSSelector(selectorText);	//create a selector from this selector text
						if(selector.isMultipleClassSelector)	//if this is a multi-class selector
						{
							var cssText=rule.style.cssText;	//get the text of the rule (even though Danny Goodman's _JavaScript Bible_, Fifth Edition says that IE doesn't support this property
							if(cssText.length>0)	//if there is actually text for this rule (IE won't allow us to add a rule with no text, but an empty rule doesn't do anything anyway, so ignore it)
							{
								var elementSelectors=selector.elementSelectors;	//get the element selectors for this selector
								for(var elementSelectorIndex=elementSelectors.length-1; elementSelectorIndex>=0; --elementSelectorIndex)	//for each element selector
								{
									var elementSelector=elementSelectors[elementSelectorIndex];	//get a reference to this element selector
									if(elementSelector.isMultipleClassSelector)	//if this is a multi-class element selector
									{
										this.cssMultipleClassElementSelectors.add(elementSelector);	//add this element selector to our list of multiple class element selectors
									}
								}
								stylesheet.removeRule(ruleIndex);	//remove the rule at this index (using an IE6-specific method)
								stylesheet.addRule(selector.ie6FixSelectorText, cssText, ruleIndex);	//add a new rule in its place with the new selector text (using an IE6-specific method)
							}
						}
					}
				}
			}
				//fix any imported stylesheets
			for(var stylesheetIndex=0; stylesheetIndex<stylesheet.imports.length; ++stylesheetIndex)	//for each imported stylesheet
			{
				var importedStylesheet=stylesheet.imports[stylesheetIndex];	//get a reference to this imported stylesheet
				this._fixStylesheet(importedStylesheet);	//fix this imported stylesheet
			}
			
		};

		/**Fixes all stylesheets of the current document.
		Must only be called once per loaded document.
		*/
		GuiseIE6Fix.prototype.fixStylesheets=function()
		{
			for(var stylesheetIndex=0; stylesheetIndex<document.styleSheets.length; ++stylesheetIndex)	//for each stylesheet
			{
				var stylesheet=document.styleSheets[stylesheetIndex];	//get a reference to this stylesheet
		//TODO del alert("stylesheet: "+stylesheet.href);
				this._fixStylesheet(stylesheet);	//fix this stylesheet
			}

		};
	}
/*TODO del when works
		//fix stylesheets
	for(var stylesheetIndex=0; stylesheetIndex<document.styleSheets.length; ++stylesheetIndex)	//for each stylesheet
	{
		var stylesheet=document.styleSheets[stylesheetIndex];	//get a reference to this stylesheet
//TODO del alert("stylesheet: "+stylesheet.href);
		this._fixStylesheet(stylesheet);	//fix this stylesheet
	}
*/

};

/**The selector text for indicating that a stylesheet has been fixed for IE6.*/
GuiseIE6Fix.GUISE_IE6_FIX_SELECTOR_TEXT="guiseIE6Fix";

/**The prefix for Guise IE6 fixup classes.*/
GuiseIE6Fix.GUISE_IE6_FIX_CLASS_PREFIX="guiseIE6";

/**The class delimiter for classes that have been fixed for IE6.*/
GuiseIE6Fix.GUISE_IE6_FIX_CLASS_DELIMITER="_-";

/**The extended prefix for Guise IE6 fixup classes, including the first delimiter.*/
GuiseIE6Fix.GUISE_IE6_FIX_CLASS_EXTENDED_PREFIX=GuiseIE6Fix.GUISE_IE6_FIX_CLASS_PREFIX+GuiseIE6Fix.GUISE_IE6_FIX_CLASS_DELIMITER;

var guiseIE6Fix=new GuiseIE6Fix();	//create and initialize the IE 6 fix routines
