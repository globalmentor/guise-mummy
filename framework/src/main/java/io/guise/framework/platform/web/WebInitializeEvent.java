/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform.web;

import java.net.URI;

import io.guise.framework.platform.DepictContext;

import static java.util.Objects.*;

/**
 * A web platform event indicating that initialization should occur.
 * @author Garret Wilson
 */
public class WebInitializeEvent extends AbstractWebPlatformEvent {

	/** The hour of the browser. */
	private final int hour;

	/** @return The hour of the browser. */
	public int getHour() {
		return hour;
	}

	/** The current offset, in milliseconds, from UTC. */
	private final int utcOffset;

	/** @return The current offset, in milliseconds, from UTC. */
	public int getUTCOffset() {
		return utcOffset;
	}

	/** The offset, in milliseconds, from UTC in January of the current year. */
	private final int utcOffset01;

	/** @return The offset, in milliseconds, from UTC in January of the current year. */
	public int getUTCOffset01() {
		return utcOffset01;
	}

	/** The offset, in milliseconds, from UTC in June of the current year. */
	private final int utcOffset06;

	/** @return The offset, in milliseconds, from UTC in June of the current year. */
	public int getUTCOffset06() {
		return utcOffset06;
	}

	/** The time zone offset from GMT, which will vary according to DST. */
	//TODO del	private final int timezone;

	/** @return The time zone offset from GMT, which will vary according to DST. */
	//TODO del		public int getTimeZone() {return timezone;}

	/** The user language. */
	private final String language;

	/** @return The user language. */
	public String getLanguage() {
		return language;
	}

	/** The user color depth. */
	private final int colorDepth;

	/** @return The user color depth. */
	public int getColorDepth() {
		return colorDepth;
	}

	/** The width of the screen. */
	private final int screenWidth;

	/** @return The width of the screen. */
	public int getScreenWidth() {
		return screenWidth;
	}

	/** The height of the screen. */
	private final int screenHeight;

	/** @return The height of the screen. */
	public int getScreenHeight() {
		return screenHeight;
	}

	/** The width of the browser. */
	private final int browserWidth;

	/** @return The width of the browser. */
	public int getBrowserWidth() {
		return browserWidth;
	}

	/** The height of the browser. */
	private final int browserHeight;

	/** @return The height of the browser. */
	public int getBrowserHeight() {
		return browserHeight;
	}

	/** The version of JavaScript supported by the client, or <code>null</code> if JavaScript is not supported. */
	private final String javascriptVersion;

	/** @return The version of JavaScript supported by the client, or <code>null</code> if JavaScript is not supported. */
	public String getJavaScriptVersion() {
		return javascriptVersion;
	}

	/** Whether Java is enabled for the user. */
	private final boolean javaEnabled;

	/** @return Whether Java is enabled for the user. */
	public boolean isJavaEnabled() {
		return javaEnabled;
	}

	/** The referring URI of the document, or <code>null</code> if there is no referrer. */
	private final URI referrerURI;

	/** @return The referring URI of the document, or <code>null</code> if there is no referrer. */
	public URI getReferrerURI() {
		return referrerURI;
	}

	/**
	 * Constructor.
	 * @param context The context in which this control event was produced.
	 * @param hour The hour of the browser. //TODO del @param timezone The time zone offset from GMT.
	 * @param utcOffset The current offset, in milliseconds, from UTC.
	 * @param utcOffset01 The offset, in milliseconds, from UTC in January of the current year.
	 * @param utcOffset06 The offset, in milliseconds, from UTC in June of the current year.
	 * @param language The user language.
	 * @param colorDepth The user color depth.
	 * @param screenWidth The width of the screen.
	 * @param screenHeight The height of the screen.
	 * @param browserWidth The width of the browser.
	 * @param browserHeight The height of the browser.
	 * @param javascriptVersion The version of JavaScript supported by the client, or <code>null</code> if JavaScript is not supported.
	 * @param javaEnabled Whether Java is enabled for the user.
	 * @param referrerURI The referring URI of the document, or <code>null</code> if there is no referrer.
	 * @throws NullPointerException if the given context and/or language is <code>null</code>.
	 */
	public WebInitializeEvent(final DepictContext context, final int hour, /*TODO del final int timezone, */final int utcOffset, final int utcOffset01,
			final int utcOffset06, final String language, final int colorDepth, final int screenWidth, final int screenHeight, final int browserWidth,
			final int browserHeight, final String javascriptVersion, final boolean javaEnabled, final URI referrerURI) {
		super(context); //construct the parent class
		this.hour = hour;
		this.utcOffset = utcOffset;
		this.utcOffset01 = utcOffset01;
		this.utcOffset06 = utcOffset06;
		//TODO del		this.timezone=timezone;
		this.language = requireNonNull(language, "Language cannot be null.");
		this.colorDepth = colorDepth;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.browserWidth = browserWidth;
		this.browserHeight = browserHeight;
		this.javascriptVersion = javascriptVersion;
		this.javaEnabled = javaEnabled;
		this.referrerURI = referrerURI;
	}
}
