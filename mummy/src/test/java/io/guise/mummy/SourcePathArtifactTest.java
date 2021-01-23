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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.regex.Matcher;

import org.junit.jupiter.api.*;

/**
 * Tests of {@link SourcePathArtifact}.
 * @author Garret Wilson
 */
public class SourcePathArtifactTest {

	/** @see SourcePathArtifact#POST_FILENAME_PATTERN */
	@Test
	public void testPostFilenamePatternSimple() {
		final Matcher matcher = SourcePathArtifact.POST_FILENAME_PATTERN.matcher("@2010-09-08-foobar.md");
		assertThat(matcher.matches(), is(true));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_DATE_GROUP), is("2010-09-08"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_YEAR_GROUP), is("2010"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_MONTH_GROUP), is("09"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_DAY_GROUP), is("08"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_FILENAME_GROUP), is("foobar.md"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_SLUG_GROUP), is("foobar"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_EXT_GROUP), is("md"));
		assertThat(SourcePathArtifact.POST_FILENAME_PATTERN.matcher("2010-09-08-foobar.md").matches(), is(false));
		assertThat(SourcePathArtifact.POST_FILENAME_PATTERN.matcher("@20100-09-08-foobar.md").matches(), is(false));
		assertThat(SourcePathArtifact.POST_FILENAME_PATTERN.matcher("@201009-08-foobar.md").matches(), is(false));
		assertThat(SourcePathArtifact.POST_FILENAME_PATTERN.matcher("@2010-9-08-foobar.md").matches(), is(false));
		assertThat(SourcePathArtifact.POST_FILENAME_PATTERN.matcher("@2010-09-08-.md").matches(), is(false));
		assertThat(SourcePathArtifact.POST_FILENAME_PATTERN.matcher("@2010-09-08-foobar.").matches(), is(false));
		assertThat(SourcePathArtifact.POST_FILENAME_PATTERN.matcher("@2010-09-08-foobar").matches(), is(false));
		assertThat(SourcePathArtifact.POST_FILENAME_PATTERN.matcher("@2010-09-08foobar.md").matches(), is(false));
	}

	/** @see SourcePathArtifact#POST_FILENAME_PATTERN */
	@Test
	public void testPostFilenamePatternComplex() {
		final Matcher matcher = SourcePathArtifact.POST_FILENAME_PATTERN.matcher("@2010-09-08-this-And that@touché ^test.foo.$bar123");
		assertThat(matcher.matches(), is(true));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_DATE_GROUP), is("2010-09-08"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_YEAR_GROUP), is("2010"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_MONTH_GROUP), is("09"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_DAY_GROUP), is("08"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_FILENAME_GROUP), is("this-And that@touché ^test.foo.$bar123"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_SLUG_GROUP), is("this-And that@touché ^test"));
		assertThat(matcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_EXT_GROUP), is("foo.$bar123"));
	}

}
