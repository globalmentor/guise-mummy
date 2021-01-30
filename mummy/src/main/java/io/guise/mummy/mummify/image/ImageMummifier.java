/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy.mummify.image;

import io.guise.mummy.mummify.Mummifier;

/**
 * Mummifier for generating images.
 * @author Garret Wilson
 */
public interface ImageMummifier extends Mummifier {

	/** The configuration indicating the integer size (usually expressed as a {@link Long} in bytes beyond which an image should be processed. */
	public static final String CONFIG_KEY_MUMMY_IMAGE_PROCESS_THRESHOLD_FILE_SIZE = "mummy.image.processThresholdFileSize";

	/** The configuration indicating the floating point compression quality (between 0.0 and 1.0) to use when writing images. */
	public static final String CONFIG_KEY_MUMMY_IMAGE_COMPRESSION_QUALITY = "mummy.image.compressionQuality";

	/** The configuration indicating the maximum integer length in pixels of each axis (width and height) when scaling images. */
	public static final String CONFIG_KEY_MUMMY_IMAGE_SCALE_MAX_LENGTH = "mummy.image.scaleMaxLength";

	/** The configuration indicating the aspects (by string IDs) to generate for processed images. */
	public static final String CONFIG_KEY_MUMMY_IMAGE_WITH_ASPECTS = "mummy.image.withAspects";

	//aspect definitions

	/**
	 * The configuration key string format pattern indicating the floating point compression quality (between 0.0 and 1.0) to use when writing scaled images of
	 * aspect <code><var>_</var></code>, to be replaced using {@link String#format(String, Object...)}.
	 */
	public static final String CONFIG_KEY_FORMAT_MUMMY_IMAGE_ASPECT___COMPRESSION_QUALITY = "mummy.image.aspect.%s.compressionQuality";

	/**
	 * The configuration key string format pattern indicating the maximum integer length in pixels of each axis (width and height) when scaling images of aspect
	 * <code><var>_</var></code>, to be replaced using {@link String#format(String, Object...)}.
	 */
	public static final String CONFIG_KEY_FORMAT_MUMMY_IMAGE_ASPECT___SCALE_MAX_LENGTH = "mummy.image.aspect.%s.scaleMaxLength";

}
