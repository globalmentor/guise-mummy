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

	/** The configuration indicating the floating point compression quality (between 0.0 and 1.0) to use when writing scaled images. */
	public static final String CONFIG_KEY_MUMMY_IMAGE_SCALE_COMPRESSION_QUALITY = "mummy.image.scaleCompressionQuality";

	/** The configuration indicating the maximum integer length in pixels of each axis (width and height) when scaling images. */
	public static final String CONFIG_KEY_MUMMY_IMAGE_SCALE_MAX_LENGTH = "mummy.image.scaleMaxLength";

	/** The configuration indicating the integer size in bytes beyond which an image should be scaled in an attempt to reduce file size. */
	public static final String CONFIG_KEY_MUMMY_IMAGE_SCALE_THRESHOLD_FILE_SIZE = "mummy.image.scaleThresholdFildSize";

}
