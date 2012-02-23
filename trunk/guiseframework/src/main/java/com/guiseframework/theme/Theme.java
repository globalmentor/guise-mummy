/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.theme;

import java.net.URI;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.ploop.PLOOPURFProcessor;

import static java.util.Collections.*;

import com.globalmentor.collections.CollectionMap;
import com.globalmentor.collections.HashSetHashMap;
import com.globalmentor.net.ContentType;
import com.globalmentor.urf.*;
import com.globalmentor.urf.select.*;
import com.globalmentor.urf.xml.XML;
import com.globalmentor.util.*;
import com.guiseframework.style.*;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.urf.URF.*;
import static com.guiseframework.Resources.*;

/**
 * Guise theme specification.
 * @author Garret Wilson
 */
public class Theme extends URFListResource<Rule>
{

	/** The extension for Guise theme resource names. */
	public final static String NAME_EXTENSION = "guisetheme";
	/** The content type for theme resources: <code>application/theme+turf</code>. */
	public static final ContentType CONTENT_TYPE = ContentType.getInstance(ContentType.APPLICATION_PRIMARY_TYPE, "theme"
			+ ContentType.SUBTYPE_SUFFIX_DELIMITER_CHAR + TURF.SUBTYPE_SUFFIX);

	/** The recommended prefix to the theme ontology namespace. */
	public final static String THEME_NAMESPACE_PREFIX = "theme";
	/** The URI to the theme ontology namespace. */
	public final static URI THEME_NAMESPACE_URI = URI.create("http://guiseframework.com/namespaces/theme/");

	//classes
	/** The URI of the template class. */
	public final static URI TEMPLATE_CLASS_URI = createResourceURI(THEME_NAMESPACE_URI, getLocalName(Template.class));

	//properties
	/** The apply property name. */
	public final static URI APPLY_PROPERTY_URI = createResourceURI(THEME_NAMESPACE_URI, "apply");
	/** The property for the URI of the theme's parent. */
	public final static URI PARENT_URI_PROPERTY_URI = createResourceURI(THEME_NAMESPACE_URI, "parentURI");
	/** The resources property name. */
	public final static URI RESOURCES_PROPERTY_URI = createResourceURI(THEME_NAMESPACE_URI, "resources");

	/** The theme parent, or <code>null</code> if there is no resolving parent. */
	private Theme parent = null;

	/** @return The theme parent, or <code>null</code> if there is no resolving parent. */
	public Theme getParent()
	{
		return parent;
	}

	/**
	 * Sets the theme parent.
	 * @param newParent The new theme parent, or <code>null</code> if there should be no resolving parent.
	 */
	public void setParent(final Theme newParent)
	{
		parent = newParent;
	} //TODO maybe remove and create custom ThemeIO

	/** The map of sets of rules that have selectors selecting classes. */
	private final CollectionMap<Class<?>, Rule, Set<Rule>> classRuleMap = new HashSetHashMap<Class<?>, Rule>(); //TODO make this store a sorted set, and use a comparator based on order

	/**
	 * Retrieves the set of rules that selects the class of the given object, including parent classes. It is not guaranteed that the object will match all or any
	 * of the returned rules; only that the object's class is used as part of the selections of the returned rules.
	 * @param object The object for which class-selected rules should be returned.
	 * @return A set of all rules that reference a class that selects the given object's class.
	 * @throws NullPointerException if the given object is <code>null</code>.
	 */
	public Set<Rule> getClassRules(final Object object)
	{
		final Class<?> objectClass = checkInstance(object, "Object cannot be null").getClass(); //get the object's class
		Set<Rule> combinedRuleSet = null; //we'll create the rule set only if needed
		final List<Class<?>> ancestorClasses = getAncestorClasses(objectClass); //get the class ancestor hierarchy of this class TODO cache these
		for(final Class<?> ancestorClass : ancestorClasses) //for each ancestor class TODO iterate the list in the correct order; send back the rules in the correct order
		{
			final Set<Rule> ruleSet = classRuleMap.get(ancestorClass); //try to get a rule for the object's ancestor class
			if(ruleSet != null) //if we found a rule set
			{
				if(combinedRuleSet == null) //if we haven't yet created the combined rule set
				{
					combinedRuleSet = new HashSet<Rule>(); //create a new hash set
				}
				combinedRuleSet.addAll(ruleSet); //add all the rules for the ancestor class to the combined rule set
			}
		}
		return combinedRuleSet != null ? combinedRuleSet : Collections.<Rule>emptySet(); //return the combined set of rules we've found (Java won't allow emptySet() to be used in this context, but a warning here is better than alternate, less-efficient methods)
	}

	/** Default constructor. */
	public Theme()
	{
		this((URI)null); //construct the class with no reference URI
	}

	/**
	 * Reference URI constructor.
	 * @param referenceURI The reference URI for the new resource.
	 */
	public Theme(final URI referenceURI)
	{
		super(referenceURI, createResourceURI(THEME_NAMESPACE_URI, getLocalName(Theme.class))); //construct the parent class, using a type based upon the name of this class
	}

	/**
	 * Collection constructor with no URI. The elements of the specified collection will be added to this list in the order they are returned by the collection's
	 * iterator.
	 * @param collection The collection whose elements are to be placed into this list.
	 * @throws NullPointerException if the specified collection is <code>null</code>.
	 */
	public Theme(final Collection<? extends Rule> collection)
	{
		this(null, collection); //construct the class with no URI
	}

	/**
	 * URI and collection constructor. The elements of the specified collection will be added to this list in the order they are returned by the collection's
	 * iterator.
	 * @param uri The URI for the resource, or <code>null</code> if the resource should have no URI.
	 * @param collection The collection whose elements are to be placed into this list.
	 * @throws NullPointerException if the specified collection is <code>null</code>.
	 */
	public Theme(final URI uri, final Collection<? extends Rule> collection)
	{
		this(uri); //construct the class with the URI
		addAll(collection); //add all the collection elements to the list
	}

	/**
	 * Parent theme constructor.
	 * @param parent The theme to serve as the parent of this theme, or <code>null</code> if this theme should have no parent.
	 */
	public Theme(final Theme parent)
	{
		this.parent = parent; //save the parent theme
	}

	/**
	 * Retrieves the URI indicating the parent theme.
	 * @return The URI indicating the parent theme, or <code>null</code> if no parent theme is indicated or the value is not a URI.
	 */
	public URI getParentURI()
	{
		return asURI(getPropertyValue(PARENT_URI_PROPERTY_URI)); //return the theme.parent property as a URI
	}

	/**
	 * Retrieves the resources URF resources. Each resource may indicate an external set of resources to load by providing a reference URI, as well as contain
	 * resource definitions.
	 * @return The list of resources that indicate resources locations and/or contain resource definitions.
	 */
	public Iterable<URFResource> getResourceResources(final Locale locale) //TODO use the locale to narrow down the resources
	{
		return getPropertyValues(RESOURCES_PROPERTY_URI); //return all the theme.resource properties
	}

	/**
	 * Retrieves an iterable to the XML styles.
	 * @return The styles.
	 */
	public Iterable<URFResource> getStyles()
	{
		return XML.getStyles(this); //return the styles
	}

	/**
	 * Updates the internal maps of rules. This method should be called after rules are modified so that rules will be applied correctly in the future.
	 * @throws ClassNotFoundException if one of the rules selects a class that cannot be found.
	 * @see PropertySelector#getSelectClass()
	 */
	public void updateRules() throws ClassNotFoundException
	{
		classRuleMap.clear(); //clear the map of rules
		for(final Rule rule : this) //for each rule in this theme
		{
			final Selector selector = rule.getSelector(); //get what this rule selects
			if(selector != null) //if there is a selector for this rule
			{
				updateRules(rule, selector); //update the rules with this selector
			}
		}
	}

	/**
	 * Updates the internal maps of rules based upon a selector and its subselectors. Rules with {@link OperatorSelector}s will be updated recursively.
	 * @param rule The rule with which the theme will be updated.
	 * @param selector The selector which may result in the theme being updated with this rule.
	 * @throws NullPointerException if the given rule and/or selector is <code>null</code>.
	 * @throws ClassNotFoundException if one of the selectors selects a class that cannot be found.
	 * @see PropertySelector#getSelectClass()
	 */
	protected void updateRules(final Rule rule, final Selector selector) throws ClassNotFoundException
	{
		checkInstance(rule, "Rule cannot be null.");
		checkInstance(selector, "Selector cannot be null.");
		if(selector instanceof ObjectClassSelector) //if this is a class selector
		{
			final Class<?> selectClass = ((ObjectClassSelector)selector).getSelectClass(); //get the class selected by the selector
			if(selectClass != null) //if we have a selected class
			{
				classRuleMap.addItem(selectClass, rule); //add this rule to our map
			}
			else
			{
				throw new IllegalStateException("Objcect class selector missing class selection property.");
			}
		}
		else if(selector instanceof OperatorSelector) //if this is an operator selector
		{
			for(final Selector subselector : ((OperatorSelector)selector).getSelectors()) //for each subselector
			{
				updateRules(rule, subselector); //update the rules for each subselector
			}
		}
	}

	/**
	 * Applies this theme to the given object. Any parent theme is first applied to the object before this theme is applied.
	 * @param object The object to which this theme should be applied.
	 * @throws NullPointerException if the given object is <code>null</code>.
	 * @throws IllegalStateException if a resource is a Java-typed resource the class of which cannot be found.
	 * @throws IllegalStateException if a particular value is not an appropriate argument for the corresponding property.
	 * @throws IllegalStateException If a particular property could not be accessed.
	 * @throws IllegalStateException if a resource indicates a Java class the constructor of which throws an exception.
	 */
	public void apply(final Object object)
	{
		try
		{
			final Theme parent = getParent(); //get the parent theme
			if(parent != null) //if there is a parent theme
			{
				parent.apply(object); //first apply the ancestor hierarchy to this object
			}
			final PLOOPURFProcessor ploopProcessor = new PLOOPURFProcessor(); //use the same PLOOP processor for all the rules of this theme
			final Set<Rule> classRules = getClassRules(object); //get all the rules applying to the object class
			for(final Rule rule : classRules) //for each rule
			{
				rule.apply(object, ploopProcessor); //apply the rule to the component, if the rule is applicable
			}
		}
		catch(final DataException dataException)
		{
			throw new IllegalStateException(dataException);
		}
		catch(final InvocationTargetException invocationTargetException)
		{
			throw new IllegalStateException(invocationTargetException);
		}
	}

	//standard colors
	public final static Color COLOR_SELECTED_BACKGROUND = new ResourceColor("theme.color.selected.background");

	//standard theme labels
	public final static String LABEL_ABOUT = createStringResourceReference("theme.label.about");
	public final static String LABEL_ABOUT_X = createStringResourceReference("theme.label.about.x");
	public final static String LABEL_ACCEPT = createStringResourceReference("theme.label.accept");
	public final static String LABEL_ACCESS = createStringResourceReference("theme.label.access");
	public final static String LABEL_ACCESS_X = createStringResourceReference("theme.label.access.x");
	public final static String LABEL_ADD = createStringResourceReference("theme.label.add");
	public final static String LABEL_ADD_X = createStringResourceReference("theme.label.add.x");
	public final static String LABEL_AUDIO = createStringResourceReference("theme.label.audio");
	public final static String LABEL_BROWSE = createStringResourceReference("theme.label.browse");
	public final static String LABEL_CALENDAR = createStringResourceReference("theme.label.calendar");
	public final static String LABEL_CANCEL = createStringResourceReference("theme.label.cancel");
	public final static String LABEL_CLOSE = createStringResourceReference("theme.label.close");
	public final static String LABEL_CONFIRM = createStringResourceReference("theme.label.confirm");
	public final static String LABEL_DATE = createStringResourceReference("theme.label.date");
	public final static String LABEL_DEFAULT = createStringResourceReference("theme.label.default");
	public final static String LABEL_DELETE = createStringResourceReference("theme.label.delete");
	public final static String LABEL_DELETE_X = createStringResourceReference("theme.label.delete.x");
	public final static String LABEL_DOWNLOAD = createStringResourceReference("theme.label.download");
	public final static String LABEL_EDIT = createStringResourceReference("theme.label.edit");
	public final static String LABEL_EMAIL = createStringResourceReference("theme.label.email");
	public final static String LABEL_ERROR = createStringResourceReference("theme.label.error");
	public final static String LABEL_FINISH = createStringResourceReference("theme.label.finish");
	public final static String LABEL_FIRST = createStringResourceReference("theme.label.first");
	public final static String LABEL_FOLDER = createStringResourceReference("theme.label.folder");
	public final static String LABEL_GENERAL = createStringResourceReference("theme.label.general");
	public final static String LABEL_HELP = createStringResourceReference("theme.label.help");
	public final static String LABEL_HOME = createStringResourceReference("theme.label.home");
	public final static String LABEL_IMAGE = createStringResourceReference("theme.label.image");
	public final static String LABEL_INSERT = createStringResourceReference("theme.label.insert");
	public final static String LABEL_JOIN = createStringResourceReference("theme.label.join");
	public final static String LABEL_JOIN_X = createStringResourceReference("theme.label.join.x");
	public final static String LABEL_LAST = createStringResourceReference("theme.label.last");
	public final static String LABEL_LOGIN = createStringResourceReference("theme.label.login");
	public final static String LABEL_LOGOUT = createStringResourceReference("theme.label.logout");
	public final static String LABEL_LOWER = createStringResourceReference("theme.label.lower");
	public final static String LABEL_MISSING = createStringResourceReference("theme.label.missing");
	public final static String LABEL_NEXT = createStringResourceReference("theme.label.next");
	public final static String LABEL_NEW = createStringResourceReference("theme.label.new");
	public final static String LABEL_NEW_X = createStringResourceReference("theme.label.new.x");
	public final static String LABEL_ORDER = createStringResourceReference("theme.label.order");
	public final static String LABEL_PASSWORD = createStringResourceReference("theme.label.password");
	public final static String LABEL_PASSWORD_VERIFICATION = createStringResourceReference("theme.label.password_verification");
	public final static String LABEL_PREVIOUS = createStringResourceReference("theme.label.previous");
	public final static String LABEL_PRODUCT_JAVA = createStringResourceReference("theme.label.product.java");
	public final static String LABEL_PROPERTIES = createStringResourceReference("theme.label.properties");
	public final static String LABEL_X_PROPERTIES = createStringResourceReference("theme.label.x.properties");
	public final static String LABEL_RAISE = createStringResourceReference("theme.label.raise");
	public final static String LABEL_READ = createStringResourceReference("theme.label.read");
	public final static String LABEL_REFRESH = createStringResourceReference("theme.label.refresh");
	public final static String LABEL_REJECT = createStringResourceReference("theme.label.reject");
	public final static String LABEL_REMOVE = createStringResourceReference("theme.label.remove");
	public final static String LABEL_RENAME = createStringResourceReference("theme.label.rename");
	public final static String LABEL_RENAME_X = createStringResourceReference("theme.label.rename.x");
	public final static String LABEL_RESOURCE = createStringResourceReference("theme.label.resource");
	public final static String LABEL_RETRY = createStringResourceReference("theme.label.retry");
	public final static String LABEL_SAVE = createStringResourceReference("theme.label.save");
	public final static String LABEL_SORT = createStringResourceReference("theme.label.sort");
	public final static String LABEL_START = createStringResourceReference("theme.label.start");
	public final static String LABEL_STOP = createStringResourceReference("theme.label.stop");
	public final static String LABEL_SUBMIT = createStringResourceReference("theme.label.submit");
	public final static String LABEL_SUBTRACT = createStringResourceReference("theme.label.subtract");
	public final static String LABEL_TIME = createStringResourceReference("theme.label.time");
	public final static String LABEL_TOTAL = createStringResourceReference("theme.label.total");
	public final static String LABEL_TYPE = createStringResourceReference("theme.label.type");
	public final static String LABEL_UNKNOWN = createStringResourceReference("theme.label.unknown");
	public final static String LABEL_UPLOAD = createStringResourceReference("theme.label.upload");
	public final static String LABEL_UPLOAD_TO_X = createStringResourceReference("theme.label.upload.to.x");
	public final static String LABEL_URI = createStringResourceReference("theme.label.uri");
	public final static String LABEL_USERNAME = createStringResourceReference("theme.label.username");
	public final static String LABEL_VERIFTY = createStringResourceReference("theme.label.verify");
	public final static String LABEL_VERSION = createStringResourceReference("theme.label.version");
	public final static String LABEL_VIEW = createStringResourceReference("theme.label.view");
	//standard theme icons
	public final static URI ICON_ABOUT = createURIResourceReference("theme.icon.about");
	public final static URI ICON_ERROR = createURIResourceReference("theme.icon.error");
	public final static URI ICON_HELP = createURIResourceReference("theme.icon.help");
	public final static URI ICON_INFO = createURIResourceReference("theme.icon.info");
	public final static URI ICON_QUESTION = createURIResourceReference("theme.icon.question");
	public final static URI ICON_STOP = createURIResourceReference("theme.icon.stop");
	public final static URI ICON_WARN = createURIResourceReference("theme.icon.warn");
	//standard theme glyph icons
	public final static URI GLYPH_ABOUT = createURIResourceReference("theme.glyph.about");
	public final static URI GLYPH_ACCEPT = createURIResourceReference("theme.glyph.accept");
	public final static URI GLYPH_ACCEPT_MULTIPLE = createURIResourceReference("theme.glyph.accept.multiple");
	public final static URI GLYPH_ACCESS = createURIResourceReference("theme.glyph.access");
	public final static URI GLYPH_ADD = createURIResourceReference("theme.glyph.add");
	public final static URI GLYPH_ANIMATION = createURIResourceReference("theme.glyph.animation");
	public final static URI GLYPH_ARROW_DOWN = createURIResourceReference("theme.glyph.arrow.down");
	public final static URI GLYPH_ARROW_LEFT = createURIResourceReference("theme.glyph.arrow.left");
	public final static URI GLYPH_ARROW_RIGHT = createURIResourceReference("theme.glyph.arrow.right");
	public final static URI GLYPH_ARROW_UP = createURIResourceReference("theme.glyph.arrow.up");
	public final static URI GLYPH_AUDIO = createURIResourceReference("theme.glyph.audio");
	public final static URI GLYPH_BLANK = createURIResourceReference("theme.glyph.blank");
	public final static URI GLYPH_BROWSE = createURIResourceReference("theme.glyph.browse");
	public final static URI GLYPH_BUSY = createURIResourceReference("theme.glyph.busy");
	public final static URI GLYPH_CALENDAR = createURIResourceReference("theme.glyph.calendar");
	public final static URI GLYPH_CANCEL = createURIResourceReference("theme.glyph.cancel");
	public final static URI GLYPH_CLOSE = createURIResourceReference("theme.glyph.close");
	public final static URI GLYPH_CONFIRM = createURIResourceReference("theme.glyph.confirm");
	public final static URI GLYPH_DELETE = createURIResourceReference("theme.glyph.delete");
	public final static URI GLYPH_DOCUMENT = createURIResourceReference("theme.glyph.document");
	public final static URI GLYPH_DOCUMENT_BROKEN = createURIResourceReference("theme.glyph.document.broken");
	public final static URI GLYPH_DOCUMENT_CONTENT = createURIResourceReference("theme.glyph.document.content");
	public final static URI GLYPH_DOCUMENT_NEW = createURIResourceReference("theme.glyph.document.new");
	public final static URI GLYPH_DOCUMENT_PREVIEW = createURIResourceReference("theme.glyph.document.preview");
	public final static URI GLYPH_DOCUMENT_RICH_CONTENT = createURIResourceReference("theme.glyph.document.rich.content");
	public final static URI GLYPH_DOCUMENT_STACK = createURIResourceReference("theme.glyph.document.stack");
	public final static URI GLYPH_DOWNLOAD = createURIResourceReference("theme.glyph.download");
	public final static URI GLYPH_EDIT = createURIResourceReference("theme.glyph.edit");
	public final static URI GLYPH_EMAIL = createURIResourceReference("theme.glyph.email");
	public final static URI GLYPH_ENTER = createURIResourceReference("theme.glyph.enter");
	public final static URI GLYPH_ERROR = createURIResourceReference("theme.glyph.error");
	public final static URI GLYPH_EXIT = createURIResourceReference("theme.glyph.exit");
	public final static URI GLYPH_EXCLAMATION = createURIResourceReference("theme.glyph.exclamation");
	public final static URI GLYPH_EYEGLASSES = createURIResourceReference("theme.glyph.eyeglasses");
	public final static URI GLYPH_FINISH = createURIResourceReference("theme.glyph.finish");
	public final static URI GLYPH_FIRST = createURIResourceReference("theme.glyph.first");
	public final static URI GLYPH_FOLDER = createURIResourceReference("theme.glyph.folder");
	public final static URI GLYPH_FOLDER_CLOSED = createURIResourceReference("theme.glyph.folder.closed");
	public final static URI GLYPH_FOLDER_OPEN = createURIResourceReference("theme.glyph.folder.open");
	public final static URI GLYPH_FOLDER_RICH_CONTENT = createURIResourceReference("theme.glyph.folder.rich_content");
	public final static URI GLYPH_FOLDER_TREE = createURIResourceReference("theme.glyph.folder.tree");
	public final static URI GLYPH_HELP = createURIResourceReference("theme.glyph.help");
	public final static URI GLYPH_HIDE = createURIResourceReference("theme.glyph.hide");
	public final static URI GLYPH_HIERARCHY = createURIResourceReference("theme.glyph.hierarchy");
	public final static URI GLYPH_HOME = createURIResourceReference("theme.glyph.home");
	public final static URI GLYPH_IMAGE = createURIResourceReference("theme.glyph.image");
	public final static URI GLYPH_INFO = createURIResourceReference("theme.glyph.info");
	public final static URI GLYPH_INSERT = createURIResourceReference("theme.glyph.insert");
	public final static URI GLYPH_JOIN = createURIResourceReference("theme.glyph.join");
	public final static URI GLYPH_KEY = createURIResourceReference("theme.glyph.key");
	public final static URI GLYPH_LAST = createURIResourceReference("theme.glyph.last");
	public final static URI GLYPH_LIST = createURIResourceReference("theme.glyph.list");
	public final static URI GLYPH_LOCK_CLOSED = createURIResourceReference("theme.glyph.lock.closed");
	public final static URI GLYPH_LOCK_OPEN = createURIResourceReference("theme.glyph.lock.open");
	public final static URI GLYPH_LOGIN = createURIResourceReference("theme.glyph.login");
	public final static URI GLYPH_LOGOUT = createURIResourceReference("theme.glyph.logout");
	public final static URI GLYPH_MEDIA_ADVANCE = createURIResourceReference("theme.glyph.media.advance");
	public final static URI GLYPH_MEDIA_NEXT = createURIResourceReference("theme.glyph.media.next");
	public final static URI GLYPH_MEDIA_PAUSE = createURIResourceReference("theme.glyph.media.pause");
	public final static URI GLYPH_MEDIA_PLAY = createURIResourceReference("theme.glyph.media.play");
	public final static URI GLYPH_MEDIA_PREVIOUS = createURIResourceReference("theme.glyph.media.previous");
	public final static URI GLYPH_MEDIA_RECEDE = createURIResourceReference("theme.glyph.media.recede");
	public final static URI GLYPH_MEDIA_RECORD = createURIResourceReference("theme.glyph.media.record");
	public final static URI GLYPH_MEDIA_STOP = createURIResourceReference("theme.glyph.media.stop");
	public final static URI GLYPH_MISSING = createURIResourceReference("theme.glyph.missing");
	public final static URI GLYPH_MUSIC = createURIResourceReference("theme.glyph.music");
	public final static URI GLYPH_NEXT = createURIResourceReference("theme.glyph.next");
	public final static URI GLYPH_ORDER = createURIResourceReference("theme.glyph.order");
	public final static URI GLYPH_PASSWORD = createURIResourceReference("theme.glyph.password");
	public final static URI GLYPH_PICTURE = createURIResourceReference("theme.glyph.picture");
	public final static URI GLYPH_POLYGON_CURVED = createURIResourceReference("theme.glyph.polygon.curved");
	public final static URI GLYPH_POLYGON_POINTS = createURIResourceReference("theme.glyph.polygon.points");
	public final static URI GLYPH_PREVIEW = createURIResourceReference("theme.glyph.preview");
	public final static URI GLYPH_PREVIOUS = createURIResourceReference("theme.glyph.previous");
	public final static URI GLYPH_PRODUCT_JAVA = createURIResourceReference("theme.glyph.product.java");
	public final static URI GLYPH_PROPERTIES = createURIResourceReference("theme.glyph.properties");
	public final static URI GLYPH_QUESTION = createURIResourceReference("theme.glyph.question");
	public final static URI GLYPH_REFRESH = createURIResourceReference("theme.glyph.refresh");
	public final static URI GLYPH_REMOVE = createURIResourceReference("theme.glyph.remove");
	public final static URI GLYPH_REJECT = createURIResourceReference("theme.glyph.reject");
	public final static URI GLYPH_REJECT_MULTIPLE = createURIResourceReference("theme.glyph.reject.multiple");
	public final static URI GLYPH_RENAME = createURIResourceReference("theme.glyph.rename");
	public final static URI GLYPH_RESOURCE = createURIResourceReference("theme.glyph.resource");
	public final static URI GLYPH_RETRY = createURIResourceReference("theme.glyph.retry");
	public final static URI GLYPH_SAVE = createURIResourceReference("theme.glyph.save");
	public final static URI GLYPH_SELECTED = createURIResourceReference("theme.glyph.selected");
	public final static URI GLYPH_SORT = createURIResourceReference("theme.glyph.sort");
	public final static URI GLYPH_SPEECH_BUBBLE = createURIResourceReference("theme.glyph.speech.bubble");
	public final static URI GLYPH_SPEECH_BUBBLE_TEXT = createURIResourceReference("theme.glyph.speech.bubble.text");
	public final static URI GLYPH_SPEECH_BUBBLE_TEXT_ADD = createURIResourceReference("theme.glyph.speech.bubble.text.add");
	public final static URI GLYPH_START = createURIResourceReference("theme.glyph.start");
	public final static URI GLYPH_STOP = createURIResourceReference("theme.glyph.stop");
	public final static URI GLYPH_STRING_EDIT = createURIResourceReference("theme.glyph.string.edit");
	public final static URI GLYPH_SUBMIT = createURIResourceReference("theme.glyph.submit");
	public final static URI GLYPH_SUBTRACT = createURIResourceReference("theme.glyph.subtract");
	public final static URI GLYPH_THUMBNAILS = createURIResourceReference("theme.glyph.thumbnails");
	public final static URI GLYPH_TREE = createURIResourceReference("theme.glyph.tree");
	public final static URI GLYPH_UNSELECTED = createURIResourceReference("theme.glyph.unselected");
	public final static URI GLYPH_UPLOAD = createURIResourceReference("theme.glyph.upload");
	public final static URI GLYPH_USER = createURIResourceReference("theme.glyph.user");
	public final static URI GLYPH_VERIFY = createURIResourceReference("theme.glyph.verify");
	public final static URI GLYPH_VIEW = createURIResourceReference("theme.glyph.view");
	public final static URI GLYPH_WARN = createURIResourceReference("theme.glyph.warn");
	//standard theme messages
	public final static String MESSAGE_BUSY = createStringResourceReference("theme.message.busy");
	public final static String MESSAGE_PASSWORD_INVALID = createStringResourceReference("theme.message.password.invalid");
	public final static String MESSAGE_PASSWORD_UNVERIFIED = createStringResourceReference("theme.message.password.unverified");
	public final static String MESSAGE_TASK_SUCCESS = createStringResourceReference("theme.message.task.success");
	public final static String MESSAGE_USER_INVALID = createStringResourceReference("theme.message.user.invalid");
	public final static String MESSAGE_USER_EXISTS = createStringResourceReference("theme.message.user.exists");
	//standard theme cursors
	public final static URI CURSOR_CROSSHAIR = createURIResourceReference("theme.cursor.crosshair");
	public final static URI CURSOR_DEFAULT = createURIResourceReference("theme.cursor.default");
	public final static URI CURSOR_HELP = createURIResourceReference("theme.cursor.help");
	public final static URI CURSOR_MOVE = createURIResourceReference("theme.cursor.move");
	public final static URI CURSOR_POINTER = createURIResourceReference("theme.cursor.pointer");
	public final static URI CURSOR_PROGRESS = createURIResourceReference("theme.cursor.progress");
	public final static URI CURSOR_RESIZE_LINE_FAR = createURIResourceReference("theme.cursor.resize.line.far");
	public final static URI CURSOR_RESIZE_LINE_FAR_PAGE_FAR = createURIResourceReference("theme.cursor.resize.line.far.page.far");
	public final static URI CURSOR_RESIZE_LINE_FAR_PAGE_NEAR = createURIResourceReference("theme.cursor.resize.line.far.page.near");
	public final static URI CURSOR_RESIZE_LINE_NEAR = createURIResourceReference("theme.cursor.resize.line.near");
	public final static URI CURSOR_RESIZE_LINE_NEAR_PAGE_FAR = createURIResourceReference("theme.cursor.resize.line.near.page.far");
	public final static URI CURSOR_RESIZE_LINE_NEAR_PAGE_NEAR = createURIResourceReference("theme.cursor.resize.line.near.page.near");
	public final static URI CURSOR_RESIZE_PAGE_FAR = createURIResourceReference("theme.cursor.resize.page.far");
	public final static URI CURSOR_RESIZE_PAGE_NEAR = createURIResourceReference("theme.cursor.resize.page.near");
	public final static URI CURSOR_TEXT = createURIResourceReference("theme.cursor.text");
	public final static URI CURSOR_WAIT = createURIResourceReference("theme.cursor.wait");
	//components
	public final static URI SLIDER_THUMB_X_IMAGE = createURIResourceReference("theme.slider.thumb.x.image");
	public final static URI SLIDER_THUMB_Y_IMAGE = createURIResourceReference("theme.slider.thumb.y.image");
	public final static URI SLIDER_TRACK_X_IMAGE = createURIResourceReference("theme.slider.track.x.image");
	public final static URI SLIDER_TRACK_Y_IMAGE = createURIResourceReference("theme.slider.track.y.image");

}
