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

package io.guise.mummy;

import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.annotation.*;

import io.urf.model.UrfResourceDescription;

/**
 * An artifact representing a generated page serving as a post.
 * @apiNote Currently this class serves as a semantic marker, with no additional functionality in the class itself.
 * @author Garret Wilson
 */
public class PostArtifact extends PageArtifact {

	/**
	 * The pattern for matching a filename indicating a post, e.g. for a blog.
	 * @see #FILENAME_PATTERN_DATE_GROUP
	 * @see #FILENAME_PATTERN_YEAR_GROUP
	 * @see #FILENAME_PATTERN_MONTH_GROUP
	 * @see #FILENAME_PATTERN_DAY_GROUP
	 * @see #FILENAME_PATTERN_FILENAME_GROUP
	 * @see #FILENAME_PATTERN_SLUG_GROUP
	 * @see #FILENAME_PATTERN_EXT_GROUP
	 */
	public static final Pattern FILENAME_PATTERN = Pattern.compile("@((\\d{4})-(\\d{2})-(\\d{2}))-(([^.]+)\\.(.+))");
	public static final int FILENAME_PATTERN_DATE_GROUP = 1;
	public static final int FILENAME_PATTERN_YEAR_GROUP = 2;
	public static final int FILENAME_PATTERN_MONTH_GROUP = 3;
	public static final int FILENAME_PATTERN_DAY_GROUP = 4;
	public static final int FILENAME_PATTERN_FILENAME_GROUP = 5;
	public static final int FILENAME_PATTERN_SLUG_GROUP = 6;
	public static final int FILENAME_PATTERN_EXT_GROUP = 7;

	/**
	 * Constructor.
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourceFile The file containing the source of this artifact.
	 * @param outputFile The file where the artifact will be generated.
	 * @param description The description of the artifact.
	 */
	public PostArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path outputFile,
			@Nonnull final UrfResourceDescription description) {
		super(mummifier, sourceFile, outputFile, description);
	}

}
