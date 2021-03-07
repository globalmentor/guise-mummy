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

package io.guise.mesh;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static io.guise.mesh.DefaultMeshInterpolator.findInterpolation;
import static io.guise.mesh.DefaultMeshInterpolator.hasInterpolation;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.globalmentor.text.ASCII;

public class DefaultMeshInterpolatorTest {

	/**
	 * Tests the low-level general interpolation implementation that uses an expression evaluation function.
	 * @see DefaultMeshInterpolator#findInterpolation(CharSequence, String, String, java.util.function.Function)
	 */
	@Test
	void testBaseFindInterpolationMethod() {
		assertThat("No expressions in empty string.", findInterpolation("", "^{", "}", ASCII::toUpperCase).map(Object::toString), isEmpty());
		assertThat("No expressions to find.", findInterpolation("foobar", "^{", "}", ASCII::toUpperCase).map(Object::toString), isEmpty());
		assertThat("False signal character.", findInterpolation("foo^bar", "^{", "}", ASCII::toUpperCase).map(Object::toString), isEmpty());
		assertThat("False signal character at end of string.", findInterpolation("foobar^", "^{", "}", ASCII::toUpperCase).map(Object::toString), isEmpty());
		assertThat("False left delimiter.", findInterpolation("foo{bar", "^{", "}", ASCII::toUpperCase).map(Object::toString), isEmpty());
		assertThat("False right delimiter.", findInterpolation("foo}bar", "^{", "}", ASCII::toUpperCase).map(Object::toString), isEmpty());
		assertThat("Entire string is expression", findInterpolation("^{foo}", "^{", "}", ASCII::toUpperCase).map(Object::toString), isPresentAndIs("FOO"));
		assertThat("Expression at start of string.", findInterpolation("^{foo}bar", "^{", "}", ASCII::toUpperCase).map(Object::toString), isPresentAndIs("FOObar"));
		assertThat("Expression in middle of string.", findInterpolation("fo^{oba}r", "^{", "}", ASCII::toUpperCase).map(Object::toString),
				isPresentAndIs("foOBAr"));
		assertThat("Expression at end of string.", findInterpolation("foo^{bar}", "^{", "}", ASCII::toUpperCase).map(Object::toString), isPresentAndIs("fooBAR"));
		assertThat("Two subsequent expressions.", findInterpolation("^{foo}^{bar}", "^{", "}", ASCII::toUpperCase).map(Object::toString), isPresentAndIs("FOOBAR"));
		assertThat("Two separated expressions.", findInterpolation("f^{oo}b^{a}r", "^{", "}", ASCII::toUpperCase).map(Object::toString), isPresentAndIs("fOObAr"));
		assertThat("Three separated expressions.", findInterpolation("^{f}oo^{b}a^{r}", "^{", "}", ASCII::toUpperCase).map(Object::toString),
				isPresentAndIs("FooBaR"));
		assertThat("Expression after a false signal character.", findInterpolation("f^o^{oba}r", "^{", "}", ASCII::toUpperCase).map(Object::toString),
				isPresentAndIs("f^oOBAr"));
		assertThat("Two expressions separated by a false signal character.",
				findInterpolation("^{fo}o^b^{ar}", "^{", "}", ASCII::toUpperCase).map(Object::toString), isPresentAndIs("FOo^bAR"));
		assertThat("Two expressions separated by two false signal characters.",
				findInterpolation("a^{br}a^ca^d^{abr}a", "^{", "}", ASCII::toUpperCase).map(Object::toString), isPresentAndIs("aBRa^ca^dABRa"));
		assertThrows(MeshInterpolationException.class, () -> findInterpolation("^{foobar", "^{", "}", ASCII::toUpperCase),
				"Incomplete expression at start of string.");
		assertThrows(MeshInterpolationException.class, () -> findInterpolation("foo^{bar", "^{", "}", ASCII::toUpperCase),
				"Incomplete expression in middle of string.");
		assertThrows(MeshInterpolationException.class, () -> findInterpolation("foobar^{", "^{", "}", ASCII::toUpperCase),
				"Incomplete expression at end of string.");
	}

	/**
	 * Tests the general interpolation detection implementation.
	 * @see DefaultMeshInterpolator#hasInterpolation(CharSequence, String, String)
	 */
	@Test
	void testBaseHasInterpolationMethod() {
		assertThat("No expressions in empty string.", hasInterpolation("", "^{", "}"), is(false));
		assertThat("No expressions to find.", hasInterpolation("foobar", "^{", "}"), is(false));
		assertThat("False signal character.", hasInterpolation("foo^bar", "^{", "}"), is(false));
		assertThat("False signal character at end of string.", hasInterpolation("foobar^", "^{", "}"), is(false));
		assertThat("False left delimiter.", hasInterpolation("foo{bar", "^{", "}"), is(false));
		assertThat("False right delimiter.", hasInterpolation("foo}bar", "^{", "}"), is(false));
		assertThat("Entire string is expression", hasInterpolation("^{foo}", "^{", "}"), is(true));
		assertThat("Expression at start of string.", hasInterpolation("^{foo}bar", "^{", "}"), is(true));
		assertThat("Expression in middle of string.", hasInterpolation("fo^{oba}r", "^{", "}"), is(true));
		assertThat("Expression at end of string.", hasInterpolation("foo^{bar}", "^{", "}"), is(true));
		assertThat("Two subsequent expressions.", hasInterpolation("^{foo}^{bar}", "^{", "}"), is(true));
		assertThat("Two separated expressions.", hasInterpolation("f^{oo}b^{a}r", "^{", "}"), is(true));
		assertThat("Three separated expressions.", hasInterpolation("^{f}oo^{b}a^{r}", "^{", "}"), is(true));
		assertThat("Expression after a false signal character.", hasInterpolation("f^o^{oba}r", "^{", "}"), is(true));
		assertThat("Two expressions separated by a false signal character.", hasInterpolation("^{fo}o^b^{ar}", "^{", "}"), is(true));
		assertThat("Two expressions separated by two false signal characters.", hasInterpolation("a^{br}a^ca^d^{abr}a", "^{", "}"), is(true));
	}

}
