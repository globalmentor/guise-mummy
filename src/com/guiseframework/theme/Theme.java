package com.guiseframework.theme;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.*;
import static java.util.Collections.*;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import com.garretwilson.rdf.*;
import static com.garretwilson.rdf.RDFUtilities.*;
import static com.garretwilson.rdf.xpackage.XMLOntologyConstants.*;

import com.garretwilson.urf.ploop.PLOOPProcessor;
import com.garretwilson.util.*;

import com.guiseframework.style.*;
import static com.guiseframework.Resources.*;

/**Guise theme specification.
@author Garret Wilson
*/
public class Theme extends ClassTypedRDFResource
{

	/**The recommended prefix to the theme ontology namespace.*/
	public final static String THEME_NAMESPACE_PREFIX="theme";
	/**The URI to the theme ontology namespace.*/
	public final static URI THEME_NAMESPACE_URI=URI.create("http://guiseframework.com/namespaces/theme#");

		//theme classes
	/**The rule class name; the local name of <code>theme:Rule</code>.*/
	public final static String RULE_CLASS_NAME="Rule";
	/**The selector class name; the local name of <code>theme:Selector</code>.*/
	public final static String SELECTOR_CLASS_NAME="Selector";
	/**The template class name; the local name of <code>theme:Template</code>.*/
	public final static String TEMPLATE_CLASS_NAME="Template";

		//theme properties
	/**The apply property name; the local name of <code>theme:apply</code>.*/
	public final static String APPLY_PROPERTY_NAME="apply";
	/**The class property name; the local name of <code>theme:class</code>.*/
	public final static String CLASS_PROPERTY_NAME="class";
	/**The declarations property name; the local name of <code>theme:declarations</code>.*/
	public final static String DECLARATIONS_PROPERTY_NAME="declarations";
	/**The property property name; the local name of <code>theme:property</code>.*/
	public final static String PROPERTY_PROPERTY_NAME="property";
	/**The resources property name; the local name of <code>theme:resources</code>.*/
	public final static String RESOURCES_PROPERTY_NAME="resources";
	/**The select property name; the local name of <code>theme:select</code>.*/
	public final static String SELECT_PROPERTY_NAME="select";

	/**The theme parent, or <code>null</code> if there is no resolving parent.*/
	private Theme parent=null;

		/**@return The theme parent, or <code>null</code> if there is no resolving parent.*/
		public Theme getParent() {return parent;}

		/**Sets the theme parent.
		@param newParent The new theme parent, or <code>null</code> if there should be no resolving parent.
		*/
		public void setParent(final Theme newParent) {parent=newParent;}	//TODO maybe remove and create custom ThemeIO

	/**The map of sets of rules that have selectors selecting classes.*/
	private final CollectionMap<Class<?>, Rule, Set<Rule>> classRuleMap=new HashSetHashMap<Class<?>, Rule>();	//TODO make this store a sorted set, and use a comparator based on order

	/**Retrieves the set of rules that selects the class of the given object, including parent classes.
	It is not guaranteed that the object will match all or any of the returned rules; only that the object's class is used as part of the selections of the returned rules.
	@param object The object for which class-selected rules should be returned.
	@return A set of all rules that reference a class that selects the given object's class.
	@exception NullPointerException if the given object is <code>null</code>.
	*/
	public Set<Rule> getClassRules(final Object object)
	{
		final Class<?> objectClass=checkInstance(object, "Object cannot be null").getClass();	//get the object's class
		Set<Rule> combinedRuleSet=null;	//we'll create the rule set only if needed
		final List<Class<?>> ancestorClasses=getAncestorClasses(objectClass);	//get the class ancestor hierarchy of this class TODO cache these
		for(final Class<?> ancestorClass:ancestorClasses)	//for each ancestor class TODO iterate the list in the correct order; send back the rules in the correct order
		{
			final Set<Rule> ruleSet=classRuleMap.get(ancestorClass);	//try to get a rule for the object's ancestor class
			if(ruleSet!=null)	//if we found a rule set
			{
				if(combinedRuleSet==null)	//if we haven't yet created the combined rule set
				{
					combinedRuleSet=new HashSet<Rule>();	//create a new hash set
				}
				combinedRuleSet.addAll(ruleSet);	//add all the rules for the ancestor class to the combined rule set
			}
		}
		return combinedRuleSet!=null ? combinedRuleSet : (Set<Rule>)EMPTY_SET;	//return the combined set of rules we've found (Java won't allow emptySet() to be used in this context, but a warning here is better than alternate, less-efficient methods)
	}

	/**Default constructor.*/
	public Theme()
	{
		this(null);	//construct the class with no reference URI
	}

	/**Reference URI constructor.
	@param referenceURI The reference URI for the new resource.
	*/
	public Theme(final URI referenceURI)
	{
		super(referenceURI, THEME_NAMESPACE_URI);  //construct the parent class
	}

	/**Retrieves the resources RDF resources in arbitrary order.
	Each resource may indicate an external set of resources to load by providing a reference URI, as well as contain resource definitions.
	@return The list of resources that indicate resources locations and/or contain resource definitions.
	*/
	public Iterable<RDFObject> getResourcesObjects(final Locale locale)	//TODO use the locale to narrow down the resources
	{
		return getPropertyValues(THEME_NAMESPACE_URI, RESOURCES_PROPERTY_NAME);	//return all the theme:resource properties
	}

	/**@return The list of declarations, or <code>null</code> if there is no rule list.*/
	public RDFListResource<?> getDeclarations()
	{
		return asListResource(getPropertyValue(THEME_NAMESPACE_URI, DECLARATIONS_PROPERTY_NAME));	//return the theme:declarations list		
	}

	/**Retrieves an iterable to the XML style resources, represented by <code>x:style</code> properties.
	@return An iterable to the styles, if any.
	*/
	public Iterable<RDFResource> getStyles()
	{
		return getPropertyValues(XML_ONTOLOGY_NAMESPACE_URI, STYLE_PROPERTY_NAME, RDFResource.class); //return an iterable to style properties
	}

	/**Updates the internal maps of rules.
	@exception ClassNotFoundException if one of the rules selects a class that cannot be found.
	@see PropertySelector#getSelectClass()
	*/
	public void updateRules() throws ClassNotFoundException
	{
//Debug.trace("updating rules for theme", this);
		classRuleMap.clear();	//clear the map of rules
		final RDFListResource<?> declarations=getDeclarations();	//get the declarations
		if(declarations!=null)	//if there is a rule list
		{
			for(final RDFObject declarationObject:declarations)	//for each resource in the list
			{
				if(declarationObject instanceof Rule)	//if this is a rule
				{
					final Rule rule=(Rule)declarationObject;	//get the rule
					final Selector selector=rule.getSelect();	//get what this rule selects
					if(selector!=null)	//if there is a selector for this rule
					{
						updateRules(rule, selector);	//update the rules with this selector
					}
				}
			}
		}
	}

	/**Updates the internal maps of rules based upon a selector and its subselectors.
	Rules with {@link OperatorSelector}s will be updated recursively.
	@param rule The rule with which the theme will be updated.
	@param selector The selector which may result in the theme being updated with this rule.
	@exception NullPointerException if the given rule and/or selector is <code>null</code>.
	@exception ClassNotFoundException if one of the selectors selects a class that cannot be found.
	@see PropertySelector#getSelectClass()
	*/
	protected void updateRules(final Rule rule, final Selector selector) throws ClassNotFoundException
	{
		checkInstance(rule, "Rule cannot be null.");
		checkInstance(selector, "Selector cannot be null.");
		if(selector instanceof ClassSelector)	//if this is a class selector
		{
			final Class<?> selectClass=((ClassSelector)selector).getSelectClass();	//get the class selected by the selector
//Debug.trace("selected class", selectedClass);
			if(selectClass!=null)	//if we have a selected class
			{
				classRuleMap.addItem(selectClass, rule);	//add this rule to our map
			}
		}
		else if(selector instanceof OperatorSelector)	//if this is an operator selector
		{
			for(final Selector subselector:((OperatorSelector)selector).getSelects())	//for each subselector
			{
				updateRules(rule, subselector);	//update the rules for each subselector
			}
		}
		
	}

	/**Applies this theme to the given object.
	Any parent theme is first applied to the object before this theme is applied.
	@param object The object to which this theme should be applied.
	@exception NullPointerException if the given object is <code>null</code>.
	@exception IllegalStateException if a class was specified and the indicated class cannot be found, or if a theme object a Java class the constructor of which throws an exception.
	*/
	public void apply(final Object object)
	{
		try
		{
			final Theme parent=getParent();	//get the parent theme
			if(parent!=null)	//if there is a parent theme
			{
				parent.apply(object);	//first apply the ancestor hierarchy to this object
			}
			final PLOOPProcessor ploopProcessor=new PLOOPProcessor();	//use the same PLOOP processor for all the rules of this theme
			final Set<Rule> classRules=getClassRules(object);	//get all the rules applying to the object class
			for(final Rule rule:classRules)	//for each rule
			{
				rule.apply(object, ploopProcessor);	//apply the rule to the component, if the rule is applicable
			}
		}
		catch(final ClassNotFoundException classNotFoundException)
		{
			throw new IllegalStateException(classNotFoundException);
		}
		catch(final InvocationTargetException invocationTargetException)
		{
			throw new IllegalStateException(invocationTargetException);
		}
	}

		//standard colors
	public final static Color COLOR_SELECTED_BACKGROUND=new ResourceColor("theme_color_selected_background");

		//standard theme labels
	public final static String LABEL_ABOUT=createStringResourceReference("theme_label_about");
	public final static String LABEL_ABOUT_X=createStringResourceReference("theme_label_about_x");
	public final static String LABEL_ACCEPT=createStringResourceReference("theme_label_accept");
	public final static String LABEL_ACCESS=createStringResourceReference("theme_label_access");
	public final static String LABEL_ACCESS_X=createStringResourceReference("theme_label_access_x");
	public final static String LABEL_ADD=createStringResourceReference("theme_label_add");
	public final static String LABEL_AUDIO=createStringResourceReference("theme_label_audio");
	public final static String LABEL_BROWSE=createStringResourceReference("theme_label_browse");
	public final static String LABEL_CANCEL=createStringResourceReference("theme_label_cancel");
	public final static String LABEL_CLOSE=createStringResourceReference("theme_label_close");
	public final static String LABEL_DEFAULT=createStringResourceReference("theme_label_default");
	public final static String LABEL_DELETE=createStringResourceReference("theme_label_delete");
	public final static String LABEL_DELETE_X=createStringResourceReference("theme_label_delete_x");
	public final static String LABEL_DOWNLOAD=createStringResourceReference("theme_label_download");
	public final static String LABEL_EDIT=createStringResourceReference("theme_label_edit");
	public final static String LABEL_EMAIL=createStringResourceReference("theme_label_email");
	public final static String LABEL_FINISH=createStringResourceReference("theme_label_finish");
	public final static String LABEL_FIRST=createStringResourceReference("theme_label_first");
	public final static String LABEL_FOLDER=createStringResourceReference("theme_label_folder");
	public final static String LABEL_HELP=createStringResourceReference("theme_label_help");
	public final static String LABEL_HOME=createStringResourceReference("theme_label_home");
	public final static String LABEL_IMAGE=createStringResourceReference("theme_label_image");
	public final static String LABEL_INSERT=createStringResourceReference("theme_label_insert");
	public final static String LABEL_JOIN=createStringResourceReference("theme_label_join");
	public final static String LABEL_JOIN_X=createStringResourceReference("theme_label_join_x");
	public final static String LABEL_LAST=createStringResourceReference("theme_label_last");
	public final static String LABEL_LOGIN=createStringResourceReference("theme_label_login");
	public final static String LABEL_LOGOUT=createStringResourceReference("theme_label_logout");
	public final static String LABEL_LOWER=createStringResourceReference("theme_label_lower");
	public final static String LABEL_NEXT=createStringResourceReference("theme_label_next");
	public final static String LABEL_NEW=createStringResourceReference("theme_label_new");
	public final static String LABEL_NEW_X=createStringResourceReference("theme_label_new_x");
	public final static String LABEL_PASSWORD=createStringResourceReference("theme_label_password");
	public final static String LABEL_PASSWORD_VERIFICATION=createStringResourceReference("theme_label_password_verification");
	public final static String LABEL_PREVIOUS=createStringResourceReference("theme_label_previous");
	public final static String LABEL_RAISE=createStringResourceReference("theme_label_raise");
	public final static String LABEL_REFRESH=createStringResourceReference("theme_label_refresh");
	public final static String LABEL_REJECT=createStringResourceReference("theme_label_reject");
	public final static String LABEL_REMOVE=createStringResourceReference("theme_label_remove");
	public final static String LABEL_RENAME=createStringResourceReference("theme_label_rename");
	public final static String LABEL_RENAME_X=createStringResourceReference("theme_label_rename_x");
	public final static String LABEL_RESOURCE=createStringResourceReference("theme_label_resource");
	public final static String LABEL_RETRY=createStringResourceReference("theme_label_retry");
	public final static String LABEL_SUBMIT=createStringResourceReference("theme_label_submit");
	public final static String LABEL_SUBTRACT=createStringResourceReference("theme_label_subtract");
	public final static String LABEL_TOTAL=createStringResourceReference("theme_label_total");
	public final static String LABEL_UNKNOWN=createStringResourceReference("theme_label_unknown");
	public final static String LABEL_UPLOAD=createStringResourceReference("theme_label_upload");
	public final static String LABEL_UPLOAD_TO_X=createStringResourceReference("theme_label_upload_to_x");
	public final static String LABEL_USERNAME=createStringResourceReference("theme_label_username");
	public final static String LABEL_VERIFTY=createStringResourceReference("theme_label_verify");
	public final static String LABEL_VERSION=createStringResourceReference("theme_label_version");
	public final static String LABEL_VIEW=createStringResourceReference("theme_label_view");
		//standard theme icons
	public final static URI GLYPH_ABOUT=createURIResourceReference("theme_glyph_about");
	public final static URI GLYPH_ACCEPT=createURIResourceReference("theme_glyph_accept");
	public final static URI GLYPH_ACCEPT_MULTIPLE=createURIResourceReference("theme_glyph_accept_multiple");
	public final static URI GLYPH_ACCESS=createURIResourceReference("theme_glyph_access");
	public final static URI GLYPH_ADD=createURIResourceReference("theme_glyph_add");
	public final static URI GLYPH_ANIMATION=createURIResourceReference("theme_glyph_animation");
	public final static URI GLYPH_ARROW_DOWN=createURIResourceReference("theme_glyph_arrow_down");
	public final static URI GLYPH_ARROW_LEFT=createURIResourceReference("theme_glyph_arrow_left");
	public final static URI GLYPH_ARROW_RIGHT=createURIResourceReference("theme_glyph_arrow_right");
	public final static URI GLYPH_ARROW_UP=createURIResourceReference("theme_glyph_arrow_up");
	public final static URI GLYPH_AUDIO=createURIResourceReference("theme_glyph_audio");
	public final static URI GLYPH_BLANK=createURIResourceReference("theme_glyph_blank");
	public final static URI GLYPH_BROWSE=createURIResourceReference("theme_glyph_browse");
	public final static URI GLYPH_BUSY=createURIResourceReference("theme_glyph_busy");
	public final static URI GLYPH_CANCEL=createURIResourceReference("theme_glyph_cancel");
	public final static URI GLYPH_CLOSE=createURIResourceReference("theme_glyph_close");
	public final static URI GLYPH_DELETE=createURIResourceReference("theme_glyph_delete");
	public final static URI GLYPH_DOCUMENT=createURIResourceReference("theme_glyph_document");
	public final static URI GLYPH_DOCUMENT_CONTENT=createURIResourceReference("theme_glyph_document_content");
	public final static URI GLYPH_DOCUMENT_NEW=createURIResourceReference("theme_glyph_document_new");
	public final static URI GLYPH_DOCUMENT_PREVIEW=createURIResourceReference("theme_glyph_document_preview");
	public final static URI GLYPH_DOCUMENT_RICH_CONTENT=createURIResourceReference("theme_glyph_document_rich_content");
	public final static URI GLYPH_DOCUMENT_STACk=createURIResourceReference("theme_glyph_document_stack");
	public final static URI GLYPH_DOWNLOAD=createURIResourceReference("theme_glyph_download");
	public final static URI GLYPH_EDIT=createURIResourceReference("theme_glyph_edit");
	public final static URI GLYPH_EMAIL=createURIResourceReference("theme_glyph_email");
	public final static URI GLYPH_ENTER=createURIResourceReference("theme_glyph_enter");
	public final static URI GLYPH_ERROR=createURIResourceReference("theme_glyph_error");
	public final static URI GLYPH_EXIT=createURIResourceReference("theme_glyph_exit");
	public final static URI GLYPH_EXCLAMATION=createURIResourceReference("theme_glyph_exclamation");
	public final static URI GLYPH_EYEGLASSES=createURIResourceReference("theme_glyph_eyeglasses");
	public final static URI GLYPH_FINISH=createURIResourceReference("theme_glyph_finish");
	public final static URI GLYPH_FIRST=createURIResourceReference("theme_glyph_first");
	public final static URI GLYPH_FOLDER=createURIResourceReference("theme_glyph_folder");
	public final static URI GLYPH_FOLDER_CLOSED=createURIResourceReference("theme_glyph_folder_closed");
	public final static URI GLYPH_FOLDER_OPEN=createURIResourceReference("theme_glyph_folder_open");
	public final static URI GLYPH_FOLDER_TREE=createURIResourceReference("theme_glyph_folder_tree");
	public final static URI GLYPH_HELP=createURIResourceReference("theme_glyph_help");
	public final static URI GLYPH_HIDE=createURIResourceReference("theme_glyph_hide");
	public final static URI GLYPH_HIERARCHY=createURIResourceReference("theme_glyph_hierarchy");
	public final static URI GLYPH_HOME=createURIResourceReference("theme_glyph_home");
	public final static URI GLYPH_IMAGE=createURIResourceReference("theme_glyph_image");
	public final static URI GLYPH_INFO=createURIResourceReference("theme_glyph_info");
	public final static URI GLYPH_INSERT=createURIResourceReference("theme_glyph_insert");
	public final static URI GLYPH_JOIN=createURIResourceReference("theme_glyph_join");
	public final static URI GLYPH_KEY=createURIResourceReference("theme_glyph_key");
	public final static URI GLYPH_LAST=createURIResourceReference("theme_glyph_last");
	public final static URI GLYPH_LIST=createURIResourceReference("theme_glyph_list");
	public final static URI GLYPH_LOCK_CLOSED=createURIResourceReference("theme_glyph_lock_closed");
	public final static URI GLYPH_LOCK_OPEN=createURIResourceReference("theme_glyph_lock_open");
	public final static URI GLYPH_LOGIN=createURIResourceReference("theme_glyph_login");
	public final static URI GLYPH_LOGOUT=createURIResourceReference("theme_glyph_logout");
	public final static URI GLYPH_MEDIA_ADVANCE=createURIResourceReference("theme_glyph_media_advance");
	public final static URI GLYPH_MEDIA_NEXT=createURIResourceReference("theme_glyph_media_next");
	public final static URI GLYPH_MEDIA_PAUSE=createURIResourceReference("theme_glyph_media_pause");
	public final static URI GLYPH_MEDIA_PLAY=createURIResourceReference("theme_glyph_media_play");
	public final static URI GLYPH_MEDIA_PREVIOUS=createURIResourceReference("theme_glyph_media_previous");
	public final static URI GLYPH_MEDIA_RECEDE=createURIResourceReference("theme_glyph_media_recede");
	public final static URI GLYPH_MEDIA_RECORD=createURIResourceReference("theme_glyph_media_record");
	public final static URI GLYPH_MEDIA_STOP=createURIResourceReference("theme_glyph_media_stop");
	public final static URI GLYPH_MUSIC=createURIResourceReference("theme_glyph_music");
	public final static URI GLYPH_NEXT=createURIResourceReference("theme_glyph_next");
	public final static URI GLYPH_PASSWORD=createURIResourceReference("theme_glyph_password");
	public final static URI GLYPH_PICTURE=createURIResourceReference("theme_glyph_picture");
	public final static URI GLYPH_POLYGON_CURVED=createURIResourceReference("theme_glyph_polygon_curved");
	public final static URI GLYPH_POLYGON_POINTS=createURIResourceReference("theme_glyph_polygon_points");
	public final static URI GLYPH_PREVIEW=createURIResourceReference("theme_glyph_preview");
	public final static URI GLYPH_PREVIOUS=createURIResourceReference("theme_glyph_previous");
	public final static URI GLYPH_QUESTION=createURIResourceReference("theme_glyph_question");
	public final static URI GLYPH_REFRESH=createURIResourceReference("theme_glyph_refresh");
	public final static URI GLYPH_REMOVE=createURIResourceReference("theme_glyph_remove");
	public final static URI GLYPH_REJECT=createURIResourceReference("theme_glyph_reject");
	public final static URI GLYPH_REJECT_MULTIPLE=createURIResourceReference("theme_glyph_reject_multiple");
	public final static URI GLYPH_RENAME=createURIResourceReference("theme_glyph_rename");
	public final static URI GLYPH_RESOURCE=createURIResourceReference("theme_glyph_resource");
	public final static URI GLYPH_RETRY=createURIResourceReference("theme_glyph_retry");
	public final static URI GLYPH_SELECTED=createURIResourceReference("theme_glyph_selected");
	public final static URI GLYPH_STOP=createURIResourceReference("theme_glyph_stop");
	public final static URI GLYPH_STRING_EDIT=createURIResourceReference("theme_glyph_string_edit");
	public final static URI GLYPH_SUBMIT=createURIResourceReference("theme_glyph_submit");
	public final static URI GLYPH_SUBTRACT=createURIResourceReference("theme_glyph_subtract");
	public final static URI GLYPH_THUMBNAILS=createURIResourceReference("theme_glyph_thumbnails");
	public final static URI GLYPH_TREE=createURIResourceReference("theme_glyph_tree");
	public final static URI GLYPH_UNSELECTED=createURIResourceReference("theme_glyph_unselected");
	public final static URI GLYPH_UPLOAD=createURIResourceReference("theme_glyph_upload");
	public final static URI GLYPH_USER=createURIResourceReference("theme_glyph_user");
	public final static URI GLYPH_VERIFY=createURIResourceReference("theme_glyph_verify");
	public final static URI GLYPH_VIEW=createURIResourceReference("theme_glyph_view");
	public final static URI GLYPH_WARN=createURIResourceReference("theme_glyph_warn");
		//standard theme messages
	public final static String MESSAGE_BUSY=createStringResourceReference("theme_message_busy");
	public final static String MESSAGE_PASSWORD_INVALID=createStringResourceReference("theme_message_password_invalid");
	public final static String MESSAGE_PASSWORD_UNVERIFIED=createStringResourceReference("theme_message_password_unverified");
	public final static String MESSAGE_TASK_SUCCESS=createStringResourceReference("theme_message_task_success");
	public final static String MESSAGE_USER_INVALID=createStringResourceReference("theme_message_user_invalid");
	public final static String MESSAGE_USER_EXISTS=createStringResourceReference("theme_message_user_exists");
		//standard theme cursors
	public final static URI CURSOR_CROSSHAIR=createURIResourceReference("theme_cursor_crosshair");
	public final static URI CURSOR_DEFAULT=createURIResourceReference("theme_cursor_default");
	public final static URI CURSOR_HELP=createURIResourceReference("theme_cursor_help");
	public final static URI CURSOR_MOVE=createURIResourceReference("theme_cursor_move");
	public final static URI CURSOR_POINTER=createURIResourceReference("theme_cursor_pointer");
	public final static URI CURSOR_PROGRESS=createURIResourceReference("theme_cursor_progress");
	public final static URI CURSOR_RESIZE_LINE_FAR=createURIResourceReference("theme_cursor_resize_line_far");
	public final static URI CURSOR_RESIZE_LINE_FAR_PAGE_FAR=createURIResourceReference("theme_cursor_resize_line_far_page_far");
	public final static URI CURSOR_RESIZE_LINE_FAR_PAGE_NEAR=createURIResourceReference("theme_cursor_resize_line_far_page_near");
	public final static URI CURSOR_RESIZE_LINE_NEAR=createURIResourceReference("theme_cursor_resize_line_near");
	public final static URI CURSOR_RESIZE_LINE_NEAR_PAGE_FAR=createURIResourceReference("theme_cursor_resize_line_near_page_far");
	public final static URI CURSOR_RESIZE_LINE_NEAR_PAGE_NEAR=createURIResourceReference("theme_cursor_resize_line_near_page_near");
	public final static URI CURSOR_RESIZE_PAGE_FAR=createURIResourceReference("theme_cursor_resize_page_far");
	public final static URI CURSOR_RESIZE_PAGE_NEAR=createURIResourceReference("theme_cursor_resize_page_near");
	public final static URI CURSOR_TEXT=createURIResourceReference("theme_cursor_text");
	public final static URI CURSOR_WAIT=createURIResourceReference("theme_cursor_wait");
		//components
	public final static URI SLIDER_THUMB_X_IMAGE=createURIResourceReference("theme_slider_thumb_x_image");
	public final static URI SLIDER_THUMB_Y_IMAGE=createURIResourceReference("theme_slider_thumb_y_image");
	public final static URI SLIDER_TRACK_X_IMAGE=createURIResourceReference("theme_slider_track_x_image");
	public final static URI SLIDER_TRACK_Y_IMAGE=createURIResourceReference("theme_slider_track_y_image");

}
