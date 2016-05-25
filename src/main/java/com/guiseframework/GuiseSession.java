/*
 * Copyright © 2005-2011 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.Principal;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.*;

import org.urframework.URFResource;

import com.globalmentor.beans.PropertyBindable;

import static com.globalmentor.java.Classes.*;
import com.globalmentor.net.*;
import com.globalmentor.text.CollatorFactory;
import com.globalmentor.util.DataException;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.Orientation;
import com.guiseframework.event.*;
import com.guiseframework.input.*;
import com.guiseframework.model.*;
import com.guiseframework.platform.Platform;
import com.guiseframework.prototype.ActionPrototype;
import com.guiseframework.style.*;
import com.guiseframework.theme.Theme;

import io.csar.Concerned;

/**
 * Represents a session with a user. A Swing-based client application may have only one session, while a web server application will likely have multiple
 * sessions.
 * @author Garret Wilson
 */
public interface GuiseSession extends PropertyBindable, CollatorFactory, Concerned {

	/** The input strategy bound property. */
	public static final String INPUT_STRATEGY_PROPERTY = getPropertyName(GuiseSession.class, "inputStrategy");
	/** The locale bound property. */
	public static final String LOCALE_PROPERTY = getPropertyName(GuiseSession.class, "locale");
	/** The orientation bound property. */
	public static final String ORIENTATION_PROPERTY = getPropertyName(GuiseSession.class, "orientation");
	/** The principal (e.g. user) bound property. */
	public static final String PRINCIPAL_PROPERTY = getPropertyName(GuiseSession.class, "principal");
	/** The theme URI bound property. */
	public static final String THEME_URI_PROPERTY = getPropertyName(GuiseSession.class, "themeURI");
	/** The time zone bound property. */
	public static final String TIME_ZONE_PROPERTY = getPropertyName(GuiseSession.class, "timeZone");

	/** @return The unique identifier of this session. */
	public UUID getUUID();

	/** @return The Guise application to which this session belongs. */
	public GuiseApplication getApplication();

	/** @return The writer for writing to the log file, which may not be thread-safe. */
	public Writer getLogWriter();

	/**
	 * Sets the log writer.
	 * @param logWriter The writer for writing to the log file, which may not be thread-safe.
	 * @throws NullPointerException if the given log writer is <code>null</code>.
	 */
	public void setLogWriter(final Writer logWriter);

	/**
	 * Retrieves the saved preference properties for a given class.
	 * @param objectClass The class for which preference properties should be returned.
	 * @return The saved preference properties for the given class.
	 * @throws NullPointerException if the given class is <code>null</code>.
	 * @throws IOException if there was an error retrieving preferences.
	 */
	public URFResource getPreferences(final Class<?> objectClass) throws IOException;

	/**
	 * Saves preference properties for a given class.
	 * @param objectClass The class for which preference properties should be saved.
	 * @param preferences The preferences to save for the given class.
	 * @throws NullPointerException if the given class and/or preferences is <code>null</code>.
	 * @throws IOException if there was an error storing preferences.
	 */
	public void setPreferences(final Class<?> objectClass, final URFResource preferences) throws IOException;

	/**
	 * Reports the current depiction root URI of the session. The depiction root URI is an absolute plain root URI. The session depiction root URI may be
	 * different for different sessions, and may not be equal to the application navigation base path resolved to the container's base URI.
	 * @return The depiction root URI currently representing the Guise session.
	 */
	public URI getDepictionRootURI();

	/**
	 * Sets the depiction root URI of the session. The depiction root URI is an absolute plain root URI.
	 * @param depictionRootURI The new depiction root URI of the session.
	 * @throws NullPointerException if the given depiction root URI is <code>null</code>.
	 * @throws IllegalArgumentException if the provided URI specifies a query and/or fragment.
	 * @throws IllegalArgumentException if the provided URI is not absolute.
	 * @throws IllegalArgumentException if the provided URI is not a root URI.
	 */
	public void setDepictionRootURI(final URI depictionRootURI);

	/**
	 * Determines the URI to use for depiction based upon a navigation path. The path will first be dereferenced for the current session and then resolved to the
	 * application base path. The resulting URI may not be absolute, but can be made absolute by resolving it against the depiction root URI.
	 * @param navigationPath The navigation path, which may be absolute or relative to the application.
	 * @param suffixes The suffixes, if any, to append to a resource key in a URI reference.
	 * @return A URI suitable for depiction, deferenced and resolved to the application base path.
	 * @see #dereferenceURI(URI, String...)
	 * @see #getDepictionRootURI()
	 * @see GuiseApplication#getDepictionURI(URI, URIPath)
	 */
	public URI getDepictionURI(final URIPath navigationPath, final String... suffixes);

	/**
	 * Determines the URI to use for depiction based upon a navigation URI. The URI will first be dereferenced for the current session and then resolved to the
	 * application base path. The resulting URI may not be absolute, but can be made absolute by resolving it against the depiction root URI.
	 * @param navigationURI The navigation URI, which may be absolute or have an absolute path or a path relative to the application.
	 * @param suffixes The suffixes, if any, to append to a resource key in a URI reference.
	 * @return A URI suitable for depiction, dereferenced and resolved to the application base path.
	 * @see #dereferenceURI(URI, String...)
	 * @see #getDepictionRootURI()
	 * @see GuiseApplication#getDepictionURI(URI, URI)
	 */
	public URI getDepictionURI(final URI navigationURI, final String... suffixes);

	/** @return The application frame. */
	public ApplicationFrame getApplicationFrame();

	/** @return The platform on which Guise objects are depicted. */
	public Platform getPlatform();

	/** @return The strategy for processing input. */
	public InputStrategy getInputStrategy();

	/**
	 * Sets the strategy for processing input. A default input strategy is installed that, if replaced, should be set as the parent of the new input strategy. To
	 * handle new types of input, a new input strategy should create the appropriate {@link InputEvent} and dispatch it via the application frame; if the event is
	 * not consumed, it should be passed to the parent input strategy. This is a bound property.
	 * @param newInputStrategy The new strategy for processing input.
	 * @throws NullPointerException if the given input strategy is <code>null</code>.
	 * @see #INPUT_STRATEGY_PROPERTY
	 * @see GuiseSession#getApplicationFrame()
	 * @see Component#dispatchInputEvent(InputEvent)
	 */
	public void setInputStrategy(final InputStrategy newInputStrategy);

	/** @return The current session time zone. */
	public TimeZone getTimeZone();

	/**
	 * Sets the current session time zone. This is a bound property.
	 * @param newTimeZone The new session time zone.
	 * @throws NullPointerException if the given time zone is <code>null</code>.
	 * @see GuiseSession#TIME_ZONE_PROPERTY
	 */
	public void setTimeZone(final TimeZone newTimeZone);

	/** @return The current session locale. */
	public Locale getLocale();

	/**
	 * Sets the current session locale. The default orientation will be updated if needed to reflect the new locale. This is a bound property.
	 * @param newLocale The new session locale.
	 * @throws NullPointerException if the given locale is <code>null</code>.
	 * @see #LOCALE_PROPERTY
	 * @see #setOrientation(Orientation)
	 */
	public void setLocale(final Locale newLocale);

	/**
	 * Requests that the locale be changed to one of the given locales. Each of the locales in the list are examined in order, and the first one supported by the
	 * application is used. A requested locale is accepted if a more general locale is supported. (i.e. <code>en-US</code> is accepted if <code>en</code> is
	 * supported.)
	 * @param requestedLocales The locales requested, in order of preference.
	 * @return The accepted locale (which may be a variation of this locale), or <code>null</code> if none of the given locales are supported by the application.
	 * @see GuiseApplication#getSupportedLocales()
	 * @see #setLocale(Locale)
	 */
	public Locale requestLocale(final List<Locale> requestedLocales);

	/** @return The default internationalization orientation of components for this session. */
	public Orientation getOrientation();

	/**
	 * Sets the default orientation. This is a bound property
	 * @param newOrientation The new default internationalization orientation of components for this session.
	 * @throws NullPointerException if the given orientation is <code>null</code>.
	 * @see #ORIENTATION_PROPERTY
	 */
	public void setOrientation(final Orientation newOrientation);

	/**
	 * Initializes a component, optionally with a description in a TURF resource file. This method first tries to load a PLOOP URF description of the component in
	 * a TURF file with the same name as the class file in the same directory, with an <code>.turf</code> extension. That is, for the class
	 * <code>MyComponent.class</code> this method first tries to load <code>MyComponent.turf</code> from the same directory. If this is successful, the component
	 * is initialized from this URF description. This implementation calls {@link #initializeComponent(Component, InputStream)}. The component's
	 * {@link Component#initialize()} is called whether there is an URF description. This method synchronizes on {@link #getDocumentBuilder()}.
	 * @param component The component to initialize.
	 * @throws MissingResourceException if no resource could be found associated with the given key.
	 * @throws IllegalArgumentException if the URF description does not provide a resource description of the same type as the specified component.
	 * @throws IllegalStateException if the given component has already been initialized, or there was some other problem initializing the component.
	 * @see Component#initialize()
	 * @see <a href="http://www.ploop.org/">PLOOP</a>
	 */
	public void initializeComponent(final Component component);

	/**
	 * Initializes a component with a description in a TURF resource file. This method calls {@link Component#initialize()} after initializing the component from
	 * the description. This implementation calls {@link #initializeComponent(Component, InputStream)}. This method synchronizes on {@link #getDocumentBuilder()}.
	 * @param component The component to initialize.
	 * @param resourceKey The key to a TURF description resource file.
	 * @throws MissingResourceException if no resource could be found associated with the given key.
	 * @throws IllegalArgumentException if the URF description does not provide a resource description of the same type as the specified component.
	 * @throws IllegalStateException if the given component has already been initialized.
	 * @throws DataException if the data was incorrect for component initialization.
	 * @throws InvocationTargetException if a given resource indicates a Java class the constructor of which throws an exception.
	 * @see Component#initialize()
	 */
	public void initializeComponentFromResource(final Component component, final String resourceKey) throws DataException, InvocationTargetException;

	/**
	 * Initializes a component from the contents of an URF description input stream. This method calls {@link Component#initialize()} after initializing the
	 * component from the description. This method synchronizes on {@link #getDocumentBuilder()}.
	 * @param component The component to initialize.
	 * @param descriptionInputStream The input stream containing an URF description.
	 * @throws IllegalArgumentException if the URF description does not provide a resource description of the same type as the specified component.
	 * @throws IllegalStateException if the given component has already been initialized.
	 * @throws IOException if there is an error reading from the input stream.
	 * @throws DataException if the data was incorrect for component initialization.
	 * @throws InvocationTargetException if a given resource indicates a Java class the constructor of which throws an exception.
	 * @see Component#initialize()
	 */
	public void initializeComponent(final Component component, final InputStream descriptionInputStream) throws IOException, DataException,
			InvocationTargetException;

	/**
	 * Retrieves a resource bundle to be used by this session. One of the <code>getXXXResource()</code> should be used in preference to using this method
	 * directly. If this session does not yet have a resource bundle, one will be created based upon the current theme and locale. The returned resource bundle
	 * should only be used temporarily and should not be saved, as the resource bundle may change if the session locale or the application resource bundle base
	 * name changes. The resource bundle retrieved will allow hierarchical resolution in the following priority:
	 * <ol>
	 * <li>Any resource defined by the application.</li>
	 * <li>Any resource defined by the theme.</li>
	 * <li>Any resource defined by default by Guise.</li>
	 * </ol>
	 * @return The resource bundle containing the resources for this session, based upon the locale.
	 * @throws MissingResourceException if no resource bundle for the application's specified base name can be found or there was an error loading a resource
	 *           bundle.
	 * @see GuiseApplication#loadResourceBundle(Theme, Locale)
	 * @see #getTheme()
	 * @see #getLocale()
	 * @see #getStringResource(String)
	 * @see #getStringResource(String, String)
	 * @see #getBooleanResource(String)
	 * @see #getBooleanResource(String, Boolean)
	 * @see #getIntegerResource(String)
	 * @see #getIntegerResource(String, Integer)
	 * @see #getURIResource(String)
	 * @see #getURIResource(String, URI)
	 */
	public ResourceBundle getResourceBundle() throws MissingResourceException;

	/**
	 * Retrieves an object resource from the resource bundle. Every resource access method should eventually call this method. This is a preferred convenience
	 * method for accessing the resources in the session's resource bundle. This method involves an implicit cast that will throw a class cast exception after the
	 * method ends if the resource is not of the expected type.
	 * @param resourceKey The key of the resource to retrieve.
	 * @return The resource associated with the specified resource key.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws MissingResourceException if no resource could be found associated with the given key.
	 * @see #getResourceBundle()
	 * @see #getResource(String, Object)
	 */
	public <T> T getResource(final String resourceKey) throws MissingResourceException;

	/**
	 * Retrieves an object resource from the resource bundle, using a specified default if no such resource is available. This is a preferred convenience method
	 * for accessing the resources in the session's resource bundle. This method involves an implicit cast that will throw a class cast exception after the method
	 * ends if the resource is not of the expected type.
	 * @param resourceKey The key of the resource to retrieve.
	 * @param defaultValue The default value to use if there is no resource associated with the given key.
	 * @return The resource associated with the specified resource key or the default if none is available.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @see #getResourceBundle()
	 * @see #getResource(String)
	 */
	public <T> T getResource(final String resourceKey, final T defaultValue) throws MissingResourceException;

	/**
	 * Retrieves a string resource from the resource bundle. If the resource cannot be found in the resource bundle, it will be loaded from the application's
	 * resources, if possible, treating the resource key as a locale-sensitive resource path in the application resource area. This is a preferred convenience
	 * method for accessing the resources in the session's resource bundle.
	 * @param resourceKey The key of the resource to retrieve, or a relative path to the resource in the application's resource area.
	 * @return The resource associated with the specified resource key.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws MissingResourceException if no resource could be found associated with the given key.
	 * @throws ClassCastException if the resource associated with the given key is not an instance of <code>String</code>.
	 * @see #getResourceBundle()
	 * @see #getStringResource(String, String)
	 */
	public String getStringResource(final String resourceKey) throws MissingResourceException;

	/**
	 * Retrieves a string resource from the resource bundle, using a specified default if no such resource is available. If the resource cannot be found in the
	 * resource bundle, it will be loaded from the application's resources, if possible, treating the resource key as a locale-sensitive resource path in the
	 * application resource area. This is a preferred convenience method for accessing the resources in the session's resource bundle.
	 * @param resourceKey The key of the resource to retrieve, or a relative path to the resource in the application's resource area.
	 * @param defaultValue The default value to use if there is no resource associated with the given key.
	 * @return The resource associated with the specified resource key or the default if none is available.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws ClassCastException if the resource associated with the given key is not an instance of <code>String</code>.
	 * @see #getResourceBundle()
	 * @see #getStringResource(String)
	 */
	public String getStringResource(final String resourceKey, final String defaultValue) throws MissingResourceException;

	/**
	 * Retrieves a <code>Boolean</code> resource from the resource bundle. If the given resource is a string, it will be interpreted according to the
	 * {@link Boolean#valueOf(java.lang.String)} rules. This is a preferred convenience method for accessing the resources in the session's resource bundle.
	 * @param resourceKey The key of the resource to retrieve.
	 * @return The resource associated with the specified resource key.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws MissingResourceException if no resource could be found associated with the given key.
	 * @throws ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>Boolean</code> object.
	 * @see #getResourceBundle()
	 * @see #getBooleanResource(String, Boolean)
	 */
	public Boolean getBooleanResource(final String resourceKey) throws MissingResourceException;

	/**
	 * Retrieves a <code>Boolean</code> resource from the resource bundle, using a specified default if no such resource is available. If the given resource is a
	 * string, it will be interpreted according to the {@link Boolean#valueOf(java.lang.String)} rules. This is a preferred convenience method for accessing the
	 * resources in the session's resource bundle.
	 * @param resourceKey The key of the resource to retrieve.
	 * @param defaultValue The default value to use if there is no resource associated with the given key.
	 * @return The resource associated with the specified resource key or the default if none is available.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>Boolean</code> object.
	 * @see #getResourceBundle()
	 * @see #getBooleanResource(String)
	 */
	public Boolean getBooleanResource(final String resourceKey, final Boolean defaultValue) throws MissingResourceException;

	/**
	 * Retrieves a {@link Color} resource from the resource bundle. If the given resource is a string, it will be resolved and converted to a color using
	 * {@link AbstractModeledColor#valueOf(CharSequence)}. This is a preferred convenience method for accessing the resources in the session's resource bundle.
	 * @param resourceKey The key of the resource to retrieve.
	 * @return The resource associated with the specified resource key.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws MissingResourceException if no resource could be found associated with the given key.
	 * @throws ClassCastException if the resource associated with the given key is not an instance of {@link String} or {@link Color}.
	 * @throws IllegalArgumentException if a string is provided that is not a valid color.
	 * @see #getResourceBundle()
	 * @see #getColorResource(String, Color)
	 * @see AbstractModeledColor#valueOf(CharSequence)
	 */
	public Color getColorResource(final String resourceKey) throws MissingResourceException;

	/**
	 * Retrieves a {@link Color} resource from the resource bundle, using a specified default if no such resource is available. If the given resource is a string,
	 * it will be resolved and converted to a color using {@link AbstractModeledColor#valueOf(CharSequence)}. This is a preferred convenience method for accessing
	 * the resources in the session's resource bundle.
	 * @param resourceKey The key of the resource to retrieve.
	 * @param defaultValue The default value to use if there is no resource associated with the given key.
	 * @return The resource associated with the specified resource key or the default if none is available.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws ClassCastException if the resource associated with the given key is not an instance of {@link String} or {@link Color}.
	 * @see #getResourceBundle()
	 * @see #getColorResource(String)
	 * @see AbstractModeledColor#valueOf(CharSequence)
	 */
	public Color getColorResource(final String resourceKey, final Color defaultValue) throws MissingResourceException;

	/**
	 * Retrieves an <code>Integer</code> resource from the resource bundle. If the given resource is a string, it will be interpreted according to the
	 * {@link Integer#valueOf(java.lang.String)} rules. This is a preferred convenience method for accessing the resources in the session's resource bundle.
	 * @param resourceKey The key of the resource to retrieve.
	 * @return The resource associated with the specified resource key.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws MissingResourceException if no resource could be found associated with the given key.
	 * @throws ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>Integer</code>.
	 * @throws NumberFormatException if the resource key identifies a string that is not a valid integer.
	 * @see #getResourceBundle()
	 * @see #getIntegerResource(String, Integer)
	 */
	public Integer getIntegerResource(final String resourceKey) throws MissingResourceException;

	/**
	 * Retrieves an <code>Integer</code> resource from the resource bundle, using a specified default if no such resource is available. If the given resource is a
	 * string, it will be interpreted according to the {@link Integer#valueOf(java.lang.String)} rules. This is a preferred convenience method for accessing the
	 * resources in the session's resource bundle.
	 * @param resourceKey The key of the resource to retrieve.
	 * @param defaultValue The default value to use if there is no resource associated with the given key.
	 * @return The resource associated with the specified resource key or the default if none is available.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>Integer</code>.
	 * @see #getResourceBundle()
	 * @see #getIntegerResource(String)
	 */
	public Integer getIntegerResource(final String resourceKey, final Integer defaultValue) throws MissingResourceException;

	/**
	 * Retrieves a <code>URI</code> resource from the resource bundle. If the given resource is a string, it will be converted to a URI. This is a preferred
	 * convenience method for accessing the resources in the session's resource bundle.
	 * @param resourceKey The key of the resource to retrieve.
	 * @return The resource associated with the specified resource key.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws MissingResourceException if no resource could be found associated with the given key.
	 * @throws ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>URI</code> object.
	 * @throws IllegalArgumentException if a string is provided that is not a valid URI.
	 * @see #getResourceBundle()
	 * @see #getURIResource(String, URI)
	 */
	public URI getURIResource(final String resourceKey) throws MissingResourceException;

	/**
	 * Retrieves a <code>URI</code> resource from the resource bundle, using a specified default if no such resource is available. If the given resource is a
	 * string, it will be converted to a URI. This is a preferred convenience method for accessing the resources in the session's resource bundle.
	 * @param resourceKey The key of the resource to retrieve.
	 * @param defaultValue The default value to use if there is no resource associated with the given key.
	 * @return The resource associated with the specified resource key or the default if none is available.
	 * @throws NullPointerException if the provided resource key is <code>null</code>.
	 * @throws ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>URI</code> object.
	 * @see #getResourceBundle()
	 * @see #getURIResource(String)
	 */
	public URI getURIResource(final String resourceKey, final URI defaultValue) throws MissingResourceException;

	/**
	 * Retrieves an instance of a collator appropriate for the current locale. The returned collator instance performs collations based upon the current locale.
	 * @return An instance of a collator appropriate for the current locale.
	 * @see #getLocale()
	 */
	public Collator getCollatorInstance();

	/** @return The current principal (e.g. logged-in user), or <code>null</code> if there is no principal authenticated for this session. */
	public Principal getPrincipal();

	/**
	 * Sets the current principal (e.g. logged-in user). This is a bound property.
	 * @param newPrincipal The new principal, or <code>null</code> if there should be no associated principal (e.g. the user should be logged off).
	 * @see #PRINCIPAL_PROPERTY
	 */
	public void setPrincipal(final Principal newPrincipal);

	/**
	 * Returns the current session theme. If this session's theme has not yet been loaded, this method loads the theme.
	 * @return The current session theme.
	 * @throws IOException if there is an error loading the theme.
	 * @see #getThemeURI()
	 */
	public Theme getTheme() throws IOException;

	/** @return The URI of the session theme, to be resolved against the application base path. */
	public URI getThemeURI();

	/**
	 * Sets the URI of the session theme. The current theme, if any, will be released and loaded the next time {@link #getTheme()} is called. This is a bound
	 * property.
	 * @param newThemeURI The URI of the new session theme.
	 * @throws NullPointerException if the given theme URI is <code>null</code>.
	 * @see #THEME_URI_PROPERTY
	 * @see #getTheme()
	 */
	public void setThemeURI(final URI newThemeURI);

	/** @return The action prototype for presenting application information. */
	public ActionPrototype getAboutApplicationActionPrototype();

	/**
	 * Retrieves the component bound to the given destination. If a component has already been created and cached, it will be be returned; otherwise, one will be
	 * created and cached.
	 * @param destination The destination for which a component should be returned.
	 * @return The component bound to the given destination.
	 * @throws NullPointerException if the destination is <code>null</code>.
	 * @throws IllegalStateException if the component class bound to the destination does not provide appropriate constructors, is an interface, is abstract, or
	 *           throws an exception during instantiation.
	 */
	public Component getDestinationComponent(final ComponentDestination destination);

	/**
	 * Releases the component bound to the given destination.
	 * @param destination The destination for which any bound component should be released.
	 * @return The component previously bound to the given destination, or <code>null</code> if no component was bound to the given destination.
	 * @throws NullPointerException if the destination is <code>null</code>.
	 */
	public Component releaseDestinationComponent(final ComponentDestination destination);

	/**
	 * Retrieves the component bound to the given application context-relative path. This is a convenience method that retrieves the component associated with the
	 * component destination for the given navigation path. This method calls {@link GuiseApplication#getDestination(String)}. This method calls
	 * {@link #getDestinationComponent(ComponentDestination)}.
	 * @param path The application context-relative path within the Guise container context.
	 * @return The component bound to the given path.
	 * @throws NullPointerException if the path is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path is absolute.
	 * @throws IllegalArgumentException if no component is appropriate to associated the given navigation path (i.e. the given navigation path is not associated
	 *           with a component destination).
	 * @throws IllegalStateException if the component class bound to the path does not provide appropriate constructors, is an interface, is abstract, or throws
	 *           an exception during instantiation.
	 * @see ComponentDestination
	 */
	public Component getNavigationComponent(final URIPath path);

	/**
	 * Returns a description of the resource for the given navigation path and bookmark. This is a convenience method that delegates to the appropriate
	 * destination.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in
	 *          navigation.
	 * @return A description of the indicated navigation path, or <code>null</code> if nothing exists at the given navigation path.
	 * @throws IOException if there is an error accessing the navigation path.
	 * @see Destination#getDescription(GuiseSession, URIPath, Bookmark, URI)
	 */
	public URFResource getNavigationDescription(final URIPath navigationPath, final Bookmark bookmark) throws IOException;

	/** @return Whether the session is in a modal navigation state. */
	public boolean isModalNavigation();

	/** @return The current modal navigation state, or <code>null</code> if there are no modal navigations. */
	public ModalNavigation getModalNavigation();

	/**
	 * Begins modal interaction for a particular modal panel. The modal navigation is pushed onto the stack, and an event is fired to the modal listener of the
	 * modal navigation.
	 * @param modalNavigationPanel The panel for which modal navigation state should begin.
	 * @param modalNavigation The state of modal navigation.
	 */
	public void beginModalNavigation(final ModalNavigationPanel<?> modalNavigationPanel, final ModalNavigation modalNavigation);

	/**
	 * Ends modal interaction for a particular modal panel. The panel is released from the cache so that new navigation will create a new modal panel. This method
	 * is called by modal panels and should seldom if ever be called directly. If the current modal state corresponds to the current navigation state, the current
	 * modal state is removed, the modal state's event is fired, and modal state is handed to the previous modal state, if any. Otherwise, navigation is
	 * transferred to the modal panel's referring URI, if any. If the given modal panel is not the panel at the current navigation path, the modal state is not
	 * changed, although navigation and releasal will still occur.
	 * @param modalNavigationPanel The panel for which modal navigation state should be ended.
	 * @return true if modality actually ended for the given panel.
	 * @see Frame#getReferrerURI()
	 * @see #releaseDestinationComponent(String)
	 */
	public boolean endModalNavigation(final ModalNavigationPanel<?> modalNavigationPanel);

	/**
	 * Reports the navigation path relative to the application context path.
	 * @return The path representing the current navigation location of the Guise application.
	 * @throws IllegalStateException if this message has been called before the navigation path has been initialized.
	 */
	public URIPath getNavigationPath();

	/**
	 * Returns a description of the resource for the current navigation path and bookmark. This is a convenience method that delegates to the appropriate
	 * destination for the current navigation path.
	 * @return A description of the indicated navigation path, or <code>null</code> if nothing exists at the current navigation path.
	 * @throws IOException if there is an error accessing the navigation path.
	 * @see #getNavigationPath()
	 * @see #getBookmark()
	 * @see #getNavigationDescription(URIPath, Bookmark)
	 */
	public URFResource getNavigationDescription() throws IOException;

	/**
	 * Changes the navigation path of the session. This method does not actually cause navigation to occur. If the given navigation path is the same as the
	 * current navigation path, no action occurs.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path is absolute.
	 * @throws IllegalArgumentException if the navigation path is not recognized (e.g. there is no destination associated with the navigation path).
	 * @see #navigate(URIPath)
	 * @see #navigate(URI)
	 * @see #navigateModal(URIPath, ModalNavigationListener)
	 * @see #navigateModal(URI, ModalNavigationListener)
	 */
	public void setNavigationPath(final URIPath navigationPath);

	/**
	 * Reports the current bookmark relative to the current navigation path.
	 * @return The bookmark relative to the current navigation path, or <code>null</code> if there is no bookmark specified.
	 */
	public Bookmark getBookmark();

	/**
	 * Changes the bookmark of the current navigation path. This method does not necessarily cause navigation to occur, but instead "publishes" the bookmark to
	 * indicate that it is representative of the current state of the current navigation.
	 * @param bookmark The bookmark relative to the current navigation path, or <code>null</code> if there should be no bookmark.
	 */
	public void setBookmark(final Bookmark bookmark);

	/**
	 * Sets the new navigation path and bookmark, firing a navigation event if appropriate. If the navigation path and/or bookmark has changed, this method fires
	 * an event to all {@link NavigationListener}s in the component hierarchy, with the session as the source of the {@link NavigationEvent}. This method calls
	 * {@link #setNavigationPath(URIPath)} and {@link #setBookmark(Bookmark)}.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in
	 *          navigation.
	 * @param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 * @see #setNavigationPath(URIPath)
	 * @see #setBookmark(Bookmark)
	 * @see #getApplicationFrame()
	 */
	public void setNavigation(final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI);

	/**
	 * Fires a {@link NavigationEvent} to all {@link NavigationListener}s in the session application frame hierarchy.
	 * @param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	 * @see #getNavigationPath()
	 * @see #getBookmark()
	 * @see #getApplicationFrame()
	 * @see NavigationListener
	 * @see NavigationEvent
	 */
	public void fireNavigated(final URI referrerURI);

	/** @return The requested navigation, or <code>null</code> if no navigation has been requested. */
	public Navigation getRequestedNavigation();

	/** Removes any requests for navigation. */
	public void clearRequestedNavigation();

	/**
	 * Requests navigation to the specified path. The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later
	 * point. Later requested navigation before navigation occurs will override this request.
	 * @param path A path that is either relative to the application context path or is absolute.
	 * @throws NullPointerException if the given path is <code>null</code>.
	 * @see #navigate(URI)
	 */
	public void navigate(final URIPath path);

	/**
	 * Requests navigation to the specified path in an identified viewport. The session need not perform navigation immediately or ever, and may postpone or deny
	 * navigation at some later point. Later requested navigation before navigation occurs will override this request.
	 * @param path A path that is either relative to the application context path or is absolute.
	 * @param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	 * @throws NullPointerException if the given path is <code>null</code>.
	 * @see #navigate(URI, String)
	 */
	public void navigate(final URIPath path, final String viewportID);

	/**
	 * Requests navigation to the specified path and bookmark. The session need not perform navigation immediately or ever, and may postpone or deny navigation at
	 * some later point. Later requested navigation before navigation occurs will override this request.
	 * @param path A path that is either relative to the application context path or is absolute.
	 * @param bookmark The bookmark at the given path, or <code>null</code> if no bookmark should be included in the navigation.
	 * @throws NullPointerException if the given path is <code>null</code>.
	 * @see #navigate(URI)
	 */
	public void navigate(final URIPath path, final Bookmark bookmark);

	/**
	 * Requests navigation to the specified path and bookmark in an identified viewport. The session need not perform navigation immediately or ever, and may
	 * postpone or deny navigation at some later point. Later requested navigation before navigation occurs will override this request.
	 * @param path A path that is either relative to the application context path or is absolute.
	 * @param bookmark The bookmark at the given path, or <code>null</code> if no bookmark should be included in the navigation.
	 * @param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	 * @throws NullPointerException if the given path is <code>null</code>.
	 * @see #navigate(URI, String)
	 */
	public void navigate(final URIPath path, final Bookmark bookmark, final String viewportID);

	/**
	 * Requests navigation to the specified URI. The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later
	 * point. Later requested navigation before navigation occurs will override this request.
	 * @param uri Either a relative or absolute path, or an absolute URI.
	 * @throws NullPointerException if the given URI is <code>null</code>.
	 */
	public void navigate(final URI uri);

	/**
	 * Requests navigation to the specified URI in an identified viewport. The session need not perform navigation immediately or ever, and may postpone or deny
	 * navigation at some later point. Later requested navigation before navigation occurs will override this request.
	 * @param uri Either a relative or absolute path, or an absolute URI.
	 * @param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	 * @throws NullPointerException if the given URI is <code>null</code>.
	 */
	public void navigate(final URI uri, final String viewportID);

	/**
	 * Requests modal navigation to the specified path. The session need not perform navigation immediately or ever, and may postpone or deny navigation at some
	 * later point. Later requested navigation before navigation occurs will override this request.
	 * @param path A path that is either relative to the application context path or is absolute.
	 * @param modalListener The listener to respond to the end of modal interaction.
	 * @throws NullPointerException if the given path is <code>null</code>.
	 * @see #navigateModal(URI, ModalNavigationListener)
	 */
	public void navigateModal(final URIPath path, final ModalNavigationListener modalListener);

	/**
	 * Requests modal navigation to the specified path and bookmark. The session need not perform navigation immediately or ever, and may postpone or deny
	 * navigation at some later point. Later requested navigation before navigation occurs will override this request.
	 * @param path A path that is either relative to the application context path or is absolute.
	 * @param bookmark The bookmark at the given path, or <code>null</code> if no bookmark should be included in the navigation.
	 * @param modalListener The listener to respond to the end of modal interaction.
	 * @throws NullPointerException if the given path is <code>null</code>.
	 * @see #navigateModal(URI, ModalNavigationListener)
	 */
	public void navigateModal(final URIPath path, final Bookmark bookmark, final ModalNavigationListener modalListener);

	/**
	 * Requests modal navigation to the specified URI. The session need not perform navigation immediately or ever, and may postpone or deny navigation at some
	 * later point. Later requested navigation before navigation occurs will override this request.
	 * @param uri Either a relative or absolute path, or an absolute URI.
	 * @param modalListener The listener to respond to the end of modal interaction.
	 * @throws NullPointerException if the given URI is <code>null</code>.
	 */
	public void navigateModal(final URI uri, final ModalNavigationListener modalListener);

	/**
	 * Determines the name of the site at the current navigation path.
	 * @return The name of the site, or <code>null</code> if the site has no name.
	 */
	public String getSiteName();

	/**
	 * Retrieves a breadcrumb for a particular navigation path.
	 * @param navigationPath The navigation path which a breadcrumb should be returned.
	 * @return A breadcrumb for the given navigation URI.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 */
	public Breadcrumb getBreadcrumb(final URIPath navigationPath);

	/**
	 * Retrieves breadcrumbs for all the segments of a particular navigation path. This method delegates to {@link #getBreadcrumb(URIPath)} to create each segment
	 * breadcrumb.
	 * @param navigationPath The navigation path which breadcrumbs should be returned.
	 * @return A list of breadcrumbs for the given navigation URI.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 */
	public List<Breadcrumb> getBreadcrumbs(final URIPath navigationPath);

	/**
	 * Called when the session is initialized.
	 * @throws IllegalStateException if the session is already initialized.
	 * @see #destroy()
	 */
	public void initialize();

	/**
	 * Called when the session is destroyed.
	 * @throws IllegalStateException if the session has not yet been initialized or has already been destroyed.
	 * @see #initialize()
	 */
	public void destroy();

	/**
	 * Creates a temporary resource available at a public application navigation path but with access restricted to this session. The file will be created in the
	 * application's temporary file directory. If the resource is restricted to the current Guise session, the resource will be deleted when the current Guise
	 * session ends. This is a convenience method that delegates to {@link GuiseApplication#createTempAsset(String, String, GuiseSession)}.
	 * @param baseName The base filename to be used in generating the filename.
	 * @param extension The extension to use for the temporary file.
	 * @return A public application navigation path that can be used to access the resource only from this session.
	 * @throws NullPointerException if the given base name and/or extension is <code>null</code>.
	 * @throws IllegalArgumentException if the base name is the empty string.
	 * @throws IOException if there is a problem creating the public resource.
	 * @see GuiseApplication#createTempAsset(String, String, GuiseSession)
	 * @see GuiseApplication#getTempDirectory()
	 */
	public URIPath createTempPublicResource(final String baseName, final String extension) throws IOException;

	/**
	 * Creates a component to indicate Guise busy status.
	 * @return A component to indicate Guise busy status.
	 * @see Theme#GLYPH_BUSY
	 */
	public Component createBusyComponent();

	/**
	 * Processes input such as a keystroke, a mouse click, or a command. A new {@link InputEvent} will be created and dispatched via the application frame. If an
	 * input event is still not consumed after dispatching, its input is processed by the installed input strategy, if any.
	 * @param input The input to process.
	 * @return <code>true</code> if the input was consumed and should not be processed further.
	 * @throws NullPointerException if the given input is <code>null</code>.
	 * @throws IllegalArgumentException if input was given that this session does not know how to process.
	 * @see GuiseSession#getApplicationFrame()
	 * @see Component#dispatchInputEvent(InputEvent)
	 * @see #getInputStrategy()
	 * @see InputStrategy#input(Input)
	 * @see InputEvent#isConsumed()
	 */
	public boolean input(final Input input);

	/**
	 * Logs the given session-related information with a default log level of {@link InformationLevel#LOG}. This is a convenience method that delegates to
	 * {@link #log(InformationLevel, String, String, String, Map, CharSequence)}.
	 * @param subject The log subject identification, or <code>null</code> if there is no related subject.
	 * @param predicate The log predicate identification, or <code>null</code> if there is no related predicate.
	 * @param object The log object identification, or <code>null</code> if there is no related object.
	 * @param parameters The map of log parameters, or <code>null</code> if there are no parameters.
	 * @param comment The log comment, or <code>null</code> if there is no log comment.
	 * @throws NullPointerException if the given log level is <code>null</code>.
	 */
	public void log(final String subject, final String predicate, final String object, final Map<?, ?> parameters, final CharSequence comment);

	/**
	 * Logs the given session-related information.
	 * @param level The log information level.
	 * @param subject The log subject identification, or <code>null</code> if there is no related subject.
	 * @param predicate The log predicate identification, or <code>null</code> if there is no related predicate.
	 * @param object The log object identification, or <code>null</code> if there is no related object.
	 * @param parameters The map of log parameters, or <code>null</code> if there are no parameters.
	 * @param comment The log comment, or <code>null</code> if there is no log comment.
	 * @throws NullPointerException if the given log level is <code>null</code>.
	 */
	public void log(final InformationLevel level, final String subject, final String predicate, final String object, final Map<?, ?> parameters,
			final CharSequence comment);

	/**
	 * Notifies the user of one or more notifications to be presented in sequence. The notification's label and/or icon, if specified, will be used as the dialog
	 * title and icon, respectively; if either is not specified, a label and/or icon based upon the notification's severity will be used. If the selected option
	 * to any notification is fatal, the remaining notifications will not be performed. The absence of an option selection is considered fatal only if a fatal
	 * option was presented for a given notification. This is a convenience method that delegates to {@link #notify(Runnable, Notification...)}.
	 * @param notifications One or more notification informations to relay.
	 * @throws NullPointerException if the given notifications is <code>null</code>.
	 * @throws IllegalArgumentException if no notifications are given.
	 */
	public void notify(final Notification... notifications);

	/**
	 * Notifies the user of one or more notifications to be presented in sequence, with optional logic to be executed after all notifications have taken place.
	 * The notification's label and/or icon, if specified, will be used as the dialog title and icon, respectively; if either is not specified, a label and/or
	 * icon based upon the notification's severity will be used. If the selected option to any notification is fatal, the remaining notifications and the
	 * specified logic, if any, will not be performed. The absence of an option selection is considered fatal only if a fatal option was presented for a given
	 * notification.
	 * @param notifications One or more notification informations to relay.
	 * @param afterNotify The code that executes after notification has taken place, or <code>null</code> if no action should be taken after notification.
	 * @throws NullPointerException if the given notifications is <code>null</code>.
	 * @throws IllegalArgumentException if no notifications are given.
	 */
	public void notify(final Runnable afterNotify, final Notification... notifications);

	/**
	 * Notifies the user of the given errors in sequence. If the selected option to any notification is fatal, the remaining notifications will not be performed.
	 * The absence of an option selection is considered fatal only if a fatal option was presented for a given notification. This is a convenience method that
	 * delegates to {@link #notify(Runnable, Throwable...)}.
	 * @param errors The errors with which to notify the user.
	 * @throws NullPointerException if the given errors is <code>null</code>.
	 * @throws IllegalArgumentException if no errors are given.
	 */
	public void notify(final Throwable... errors);

	/**
	 * Notifies the user of the given error in sequence, with optional logic to be executed after notification takes place. If the selected option to any
	 * notification is fatal, the remaining notifications and the specified logic, if any, will not be performed. The absence of an option selection is considered
	 * fatal only if a fatal option was presented for a given notification. This is a convenience method that delegates to
	 * {@link #notify(Runnable, Notification...)}.
	 * @param error The error with which to notify the user.
	 * @param afterNotify The code that executes after notification has taken place, or <code>null</code> if no action should be taken after notification.
	 * @throws NullPointerException if the given errors is <code>null</code>.
	 * @throws IllegalArgumentException if no errors are given.
	 */
	public void notify(final Runnable afterNotify, final Throwable... errors);

	/**
	 * Dereferences a string by replacing any string references with a string from the resources. A string reference begins with the Start of String (
	 * <code>SOS</code>) control character (U+0098) and ends with a String Terminator (<code>ST</code>) control character (U+009C). The string between these
	 * delimiters will be used to look up a string resource using {@link #getStringResource(String)}. Strings retrieved from resources will be recursively
	 * dereferenced.
	 * <p>
	 * String references appearing between an <code>SOS</code>/<code>ST</code> pair that that begin with the character
	 * {@value Resources#STRING_VALUE_REFERENCE_PREFIX_CHAR} will be considered string values and, after they are recursively dereferenced, will be applied as
	 * formatting arguments to the remaining dereferenced text using {@link MessageFormat#format(String, Object...)}.
	 * </p>
	 * @param string The string to be dereferenced.
	 * @return The dereferenced string with any string references replaced with the appropriate string from the resources.
	 * @throws NullPointerException if the given string is <code>null</code>.
	 * @throws IllegalArgumentException if a string reference has no ending String Terminator control character (U+009C).
	 * @throws MissingResourceException if no resource could be found associated with a string reference.
	 * @throws ClassCastException if the resource associated with a string reference is not an instance of <code>String</code>.
	 * @see Resources#createStringResourceReference(String)
	 * @see Resources#createStringValueReference(String)
	 * @see #getStringResource(String)
	 */
	public String dereferenceString(final String string) throws MissingResourceException;

	/**
	 * Dereferences a URI by looking up any references from the resources if necessary. If the URI has the {@value URIs#RESOURCE_SCHEME} scheme, its
	 * scheme-specific part will be used to look up the actual URI using {@link #getURIResource(String)}. If suffixes are given, they will be appended to the
	 * resource key in order, separated by '.'. If no resource is associated with that resource key, a resource will be retrieved using the unadorned resource
	 * key. URIs retrieved from resources will be recursively dereferenced without suffixes.
	 * @param uri The URI to be dereferenced.
	 * @param suffixes The suffixes, if any, to append to a resource key in a URI reference.
	 * @return The URI dereferenced from the resources.
	 * @throws NullPointerException if the given URI is <code>null</code>.
	 * @throws MissingResourceException if no resource could be found associated with a string reference.
	 * @see Resources#createURIResourceReference(String)
	 * @see #getURIResource(String)
	 */
	public URI dereferenceURI(URI uri, final String... suffixes) throws MissingResourceException;

	/**
	 * Resolves a URI against the application base path, looking up the URI from the resources if necessary. The URI will be dereferenced before it is resolved.
	 * Relative paths will be resolved relative to the application base path. Absolute paths will be considered already resolved, as will absolute URIs. For an
	 * application base path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path", while resolving "/absolute/path"
	 * will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	 * @param uri The URI to be resolved.
	 * @param suffixes The suffixes, if any, to append to a resource key in a URI reference.
	 * @return The uri resolved against resources the application base path.
	 * @throws NullPointerException if the given URI is <code>null</code>.
	 * @throws MissingResourceException if no resource could be found associated with a string reference.
	 * @see #dereferenceURI(URI, String...)
	 * @see GuiseApplication#resolveURI(URI)
	 */
	public URI resolveURI(final URI uri, final String... suffixes) throws MissingResourceException;
}
