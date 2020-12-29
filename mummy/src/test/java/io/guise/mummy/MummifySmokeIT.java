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

import static com.globalmentor.io.ClassResources.*;
import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.Paths.*;
import static io.guise.mummy.GuiseMummy.*;
import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.Files.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import org.junit.jupiter.api.*;

import io.guise.mummy.mummify.image.BaseImageMummifierTest;
import io.guise.mummy.mummify.page.MarkdownPageMummifierTest;

/**
 * A Guise Mummy smoke test.
 * @author Garret Wilson
 */
public class MummifySmokeIT extends BaseEndToEndIT {

	public static final String SMOKE_MESH_XHTML_RESOURCE_NAME = "smoke-mesh.xhtml";

	/** Predefined seed for reproducible tests. */
	private static final long RANDOM_SEED = 20201122;

	/** Deterministic random bytes to place in a binary file. */
	private static byte[] randomBinContent = new byte[(1 << 10) + 11]; //1024 and a little more

	@BeforeAll
	private static void generateRandomBinContent() {
		new Random(RANDOM_SEED).nextBytes(randomBinContent);
	}

	@Override
	protected void populateSiteSourceDirectory(final Path siteSourceDirectory) throws IOException {
		super.populateSiteSourceDirectory(siteSourceDirectory);
		//…/src/site/simple.md
		copy(MarkdownPageMummifierTest.class, siteSourceDirectory, MarkdownPageMummifierTest.SIMPLE_MARKDOWN_RESOURCE_NAME);
		//…/src/site/mesh.xhtml
		copy(getClass(), SMOKE_MESH_XHTML_RESOURCE_NAME, siteSourceDirectory.resolve("mesh.xhtml"));
		//…/src/site/random.bin
		write(siteSourceDirectory.resolve("random.bin"), randomBinContent);
		//…/src/site/$assets/images/gate-turret-reduced.jpg
		copy(BaseImageMummifierTest.class, resolve(siteSourceDirectory, "$assets", "images"), BaseImageMummifierTest.GATE_TURRET_REDUCED_JPEG_RESOURCE_NAME);
	}

	/** Main smoke test. */
	@Test
	public void smokeTest() throws IOException {
		mummify(LifeCyclePhase.MUMMIFY);
		//…/target/site/simple.html
		assertThat("Markdown files are being converted to HTML.",
				exists(getSiteTargetDirectory().resolve(changeExtension(MarkdownPageMummifierTest.SIMPLE_MARKDOWN_RESOURCE_NAME, "html"))), is(true));
		//…/target/site/mesh.html
		final Path targetMeshFile = getSiteTargetDirectory().resolve("mesh.html");
		assertThat(exists(targetMeshFile), is(true));
		assertThat("XHTML files are being meshed.", readString(targetMeshFile, UTF_8), containsString("<h1>Mesh Test</h1>"));
		//…/target/site/random.bin
		final Path targetRandomBinFile = getSiteTargetDirectory().resolve("random.bin");
		assertThat("Opaque files are being copied to the target.", exists(targetRandomBinFile), is(true));
		assertThat("Target opaque files contain correct data.", readAllBytes(targetRandomBinFile), is(randomBinContent));
		//…/target/site/assets/images/gate-turret-reduced.jpg
		final Path targetGateTurretJpegFile = resolve(getSiteTargetDirectory(), "assets", "images", BaseImageMummifierTest.GATE_TURRET_REDUCED_JPEG_RESOURCE_NAME);
		assertThat("Image files are being mumified.", exists(targetGateTurretJpegFile), is(true));
		assertThat("Small target image files contain correct data.", readAllBytes(targetGateTurretJpegFile),
				is(readBytes(BaseImageMummifierTest.class, BaseImageMummifierTest.GATE_TURRET_REDUCED_JPEG_RESOURCE_NAME)));
	}

}
