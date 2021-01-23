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

import static com.globalmentor.io.Paths.*;

import java.nio.file.Path;
import java.util.regex.*;

import javax.annotation.*;

/**
 * Artifact that conceptually originates from a source path in a file system.
 * @apiNote Eventually all information in {@link Artifact} related to a source path in a file system will be moved to this interface.
 * @author Garret Wilson
 */
public interface SourcePathArtifact extends Artifact {

	/**
	 * The pattern for matching a filename indicating a post, e.g. for a blog. Example: <code>@2021-01-23-foo-bar</code>.
	 * @see #POST_FILENAME_PATTERN_DATE_GROUP
	 * @see #POST_FILENAME_PATTERN_YEAR_GROUP
	 * @see #POST_FILENAME_PATTERN_MONTH_GROUP
	 * @see #POST_FILENAME_PATTERN_DAY_GROUP
	 * @see #POST_FILENAME_PATTERN_FILENAME_GROUP
	 * @see #POST_FILENAME_PATTERN_SLUG_GROUP
	 * @see #POST_FILENAME_PATTERN_EXT_GROUP
	 */
	public static final Pattern POST_FILENAME_PATTERN = Pattern.compile("@((\\d{4})-(\\d{2})-(\\d{2}))-(([^.]+)\\.(.+))");
	public static final int POST_FILENAME_PATTERN_DATE_GROUP = 1;
	public static final int POST_FILENAME_PATTERN_YEAR_GROUP = 2;
	public static final int POST_FILENAME_PATTERN_MONTH_GROUP = 3;
	public static final int POST_FILENAME_PATTERN_DAY_GROUP = 4;
	public static final int POST_FILENAME_PATTERN_FILENAME_GROUP = 5;
	public static final int POST_FILENAME_PATTERN_SLUG_GROUP = 6;
	public static final int POST_FILENAME_PATTERN_EXT_GROUP = 7;

	/**
	 * Determines whether a path has a filename recognized as a post.
	 * @param path The path to check.
	 * @return <code>true</code> if the path filename matches the {@link #POST_FILENAME_PATTERN}.
	 */
	public static boolean hasPostFilename(@Nonnull final Path path) {
		return findFilename(path).map(POST_FILENAME_PATTERN::matcher).map(Matcher::matches).orElse(false);
	}

}
