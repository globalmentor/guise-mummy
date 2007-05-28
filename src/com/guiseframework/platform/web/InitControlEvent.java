package com.guiseframework.platform.web;

import java.net.URI;

import com.guiseframework.context.GuiseContext;
import com.guiseframework.controller.AbstractControlEvent;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A control event indicating that initialization should occur.
@author Garret Wilson
*/
public class InitControlEvent extends AbstractControlEvent
{

	/**The hour of the browser.*/
	private final int hour;

		/**@return The hour of the browser.*/
		public int getHour() {return hour;}

	/**The time zone offset from GMT.*/
	private final int timezone;

		/**@return The time zone offset from GMT.*/
		public int getTimeZone() {return timezone;}

	/**The user language.*/
	private final String language;

		/**@return The user language.*/
		public String getLanguage() {return language;}

	/**The user color depth.*/
	private final int colorDepth;

		/**@return The user color depth.*/
		public int getColorDepth() {return colorDepth;}

	/**The width of the screen.*/
	private final int screenWidth;

		/**@return The width of the screen.*/
		public int getScreenWidth() {return screenWidth;}

	/**The height of the screen.*/
	private final int screenHeight;

		/**@return The height of the screen.*/
		public int getScreenHeight() {return screenHeight;}

	/**The width of the browser.*/
	private final int browserWidth;

		/**@return The width of the browser.*/
		public int getBrowserWidth() {return browserWidth;}

	/**The height of the browser.*/
	private final int browserHeight;

		/**@return The height of the browser.*/
		public int getBrowserHeight() {return browserHeight;}

	/**The version of JavaScript supported by the client, or <code>null</code> if JavaScript is not supported.*/
	private final String javascriptVersion;

		/**@return The version of JavaScript supported by the client, or <code>null</code> if JavaScript is not supported.*/
		public String getJavaScriptVersion() {return javascriptVersion;}

	/**Whether Java is enabled for the user.*/
	private final boolean javaEnabled;

		/**@return Whether Java is enabled for the user.*/
		public boolean isJavaEnabled() {return javaEnabled;}

	/**The referring URI of the document, or <code>null</code> if there is no referrer.*/
	private final URI referrerURI;

		/**@return The referring URI of the document, or <code>null</code> if there is no referrer.*/
		public URI getReferrerURI() {return referrerURI;}

	/**Constructor.
	@param context The context in which this control event was produced.
	@param hour The hour of the browser.
	@param timezone The time zone offset from GMT.
	@param language The user language.
	@param colorDepth The user color depth.
	@param screenWidth The width of the screen.
	@param screenHeight The height of the screen.
	@param browserWidth The width of the browser.
	@param browserHeight The height of the browser.
	@param javascriptVersion The version of JavaScript supported by the client, or <code>null</code> if JavaScript is not supported.
	@param javaEnabled Whether Java is enabled for the user.
	@param referrerURI The referring URI of the document, or <code>null</code> if there is no referrer.
	@exception NullPointerException if the given context and/or language is <code>null</code>.
	*/
	public InitControlEvent(final GuiseContext context, final int hour, final int timezone, final String language,
			final int colorDepth, final int screenWidth, final int screenHeight, final int browserWidth, final int browserHeight,
			final String javascriptVersion, final boolean javaEnabled, final URI referrerURI)
	{
		super(context);	//construct the parent class
		this.hour=hour;
		this.timezone=timezone;
		this.language=checkInstance(language, "Language cannot be null.");
		this.colorDepth=colorDepth;
		this.screenWidth=screenWidth;
		this.screenHeight=screenHeight;
		this.browserWidth=browserWidth;
		this.browserHeight=browserHeight;
		this.javascriptVersion=javascriptVersion;
		this.javaEnabled=javaEnabled;
		this.referrerURI=referrerURI;
	}
}
