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

import static com.globalmentor.io.ClassResources.*;
import static io.guise.mummy.GuiseMummy.*;
import static io.guise.mummy.mummify.image.BaseImageMummifierTest.*;
import static java.nio.file.Files.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.*;

import io.guise.mummy.BaseEndToEndIT;

/**
 * A Guise Mummy smoke test.
 * @author Garret Wilson
 */
public class DefaultImageMummifierIT extends BaseEndToEndIT {

	@Override
	protected void populateSiteSourceDirectory(final Path siteSourceDirectory) throws IOException {
		super.populateSiteSourceDirectory(siteSourceDirectory);
		//…/src/site/gate-turret-reduced.jpg
		copy(BaseImageMummifierTest.class, siteSourceDirectory, GATE_TURRET_REDUCED_JPEG_RESOURCE_NAME);
	}

	@Test
	public void shouldScaleImageOverThresholdFileSize() throws IOException {
		//…/src/site/gate-turret-reduced.jpg
		final Path sourceImageFile = getSiteSourceDirectory().resolve(GATE_TURRET_REDUCED_JPEG_RESOURCE_NAME);
		assertThat(size(sourceImageFile), is(GATE_TURRET_REDUCED_JPEG_FILE_SIZE));
		final BufferedImage sourceImage = ImageIO.read(sourceImageFile.toFile()); //TODO add more efficient library method to read dimensions directly from reader; see DefaultImageMummifier and https://stackoverflow.com/a/12164026 
		assertThat(sourceImage.getWidth(), is(GATE_TURRET_REDUCED_JPEG_WIDTH));
		assertThat(sourceImage.getHeight(), is(GATE_TURRET_REDUCED_JPEG_HEIGHT));

		getFixtureProjectSettings().put(DefaultImageMummifier.CONFIG_KEY_MUMMY_IMAGE_SCALE_THRESHOLD_FILE_SIZE, 70_000);
		getFixtureProjectSettings().put(DefaultImageMummifier.CONFIG_KEY_MUMMY_IMAGE_SCALE_MAX_LENGTH, 600);
		getFixtureProjectSettings().put(DefaultImageMummifier.CONFIG_KEY_MUMMY_IMAGE_SCALE_COMPRESSION_QUALITY, 0.5);
		mummify(LifeCyclePhase.MUMMIFY);

		//…/target/site/gate-turret-reduced.jpg
		final Path targetImageFile = getSiteTargetDirectory().resolve(GATE_TURRET_REDUCED_JPEG_RESOURCE_NAME);
		assertThat(size(targetImageFile), is(lessThan(GATE_TURRET_REDUCED_JPEG_FILE_SIZE))); //TODO get dimensions using a more efficient utility method
		final BufferedImage targetImage = ImageIO.read(targetImageFile.toFile());
		assertThat(targetImage.getWidth(), is(600));
		assertThat(targetImage.getHeight(), is(400));
	}

}
