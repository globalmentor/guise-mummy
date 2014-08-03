/*
 * Copyright © 2011 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.globalmentor.facebook;

import java.net.URI;

/**
 * Values and definitions for working with Facebook.
 * @author Garret Wilson
 * @see <a href="https://www.facebook.com/>Facebook</a>
 * @see <a href="https://developers.facebook.com/docs/opengraph/">Facebook Developers: Open Graph protocol</a>
 */
public class Facebook {

	/** The Facebook namespace. */
	public final static URI NAMESPACE_URI = URI.create("https://www.facebook.com/2008/fbml");
	/** The default prefix for the Facebook namespace, e.g. in XML documents. */
	public final static String NAMESPACE_PREFIX = "fb";

	/** A comma-separated list of IDs of Facebook user that administers a page. */
	public final static String ADMINS_LOCAL_NAME = "admins";
	/** The ID of the Facebook Platform application that administers this page. */
	public final static String APP_ID_LOCAL_NAME = "app_id";

	/** The URI for the Facebook Like plugin. */
	public final static URI FACEBOOK_PLUGIN_LIKE_URI = URI.create("http://www.facebook.com/plugins/like.php");

}
