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

package io.guise.mummy.mummify;

import static com.globalmentor.io.Paths.*;
import static com.globalmentor.util.Optionals.*;
import static java.nio.file.Files.*;
import static java.util.Objects.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.*;

import io.clogr.Clogr;
import io.guise.mummy.Artifact;
import io.guise.mummy.MummyContext;
import io.urf.URF;
import io.urf.model.*;
import io.urf.turf.TurfParser;

/**
 * An abstract mummifier to serve as a base class for mummifiers generally.
 * @author Garret Wilson
 */
public abstract class AbstractMummifier implements Mummifier {

	/**
	 * {@inheritDoc}
	 * @implSpec This version merely returns the given filename unmodified.
	 */
	@Override
	public String planArtifactTargetFilename(final MummyContext context, final String filename) {
		return requireNonNull(filename);
	}

	/**
	 * Determines the output file path for an artifact description in the site description target directory for the given artifact.
	 * @implSpec This implementation delegates to {@link #getArtifactDescriptionFile(MummyContext, Path)} using {@link Artifact#getTargetPath()}.
	 * @param context The context of static site generation.
	 * @param artifact The artifact for which a description file should be returned.
	 * @return The path in the site description target directory to which a description may be generated.
	 * @throws IllegalArgumentException if the given source file is not in the target source tree.
	 * @see Artifact#getTargetPath()
	 */
	protected Path getArtifactDescriptionFile(final @Nonnull MummyContext context, final @Nonnull Artifact artifact) {
		return getArtifactDescriptionFile(context, artifact.getTargetPath());
	}

	/**
	 * Determines the output file path for an artifact description in the site description target directory based upon the target path in the site target
	 * directory.
	 * @implSpec The default implementation produces a filename based upon the target path filename, but in the
	 *           {@link MummyContext#getSiteDescriptionTargetDirectory()} directory.
	 * @param context The context of static site generation.
	 * @param targetPath The path in the site source directory.
	 * @return The path in the site description target directory to which a description may be generated.
	 * @throws IllegalArgumentException if the given source file is not in the target source tree.
	 * @see MummyContext#getSiteDescriptionTargetDirectory()
	 */
	protected Path getArtifactDescriptionFile(final @Nonnull MummyContext context, final @Nonnull Path targetPath) {
		return addExtension(changeBase(targetPath, context.getSiteTargetDirectory(), context.getSiteDescriptionTargetDirectory()), "@.turf"); //TODO use constant
	}

	/**
	 * Loads the generated target description if any of a source file.
	 * @param context The context of static site generation.
	 * @param targetPath The path in the site target directory.
	 * @throws IllegalArgumentException if the given source file is not in the site source tree.
	 * @return The generated target description, if present, of the resource being mummified.
	 * @throws IOException if there is an I/O error retrieving the description, including if the metadata is invalid.
	 * @see #getArtifactDescriptionFile(MummyContext, Path)
	 */
	protected Optional<UrfResourceDescription> loadTargetDescription(@Nonnull MummyContext context, @Nonnull final Path targetPath) throws IOException {
		final Path descriptionFile = getArtifactDescriptionFile(context, targetPath);
		if(!isRegularFile(descriptionFile)) {
			return Optional.empty();
		}
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(descriptionFile))) {
			return new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream).stream().filter(UrfResourceDescription.class::isInstance)
					.map(UrfResourceDescription.class::cast).findFirst();
		}
	}

	/** The pattern describing the ad-hoc property name from which a {@link LocalDate} type will be inferred . */
	static final Pattern AD_HOC_PROPERTY_LOCAL_DATE_NAME_PATTERN = Pattern.compile(".+On");

	/**
	 * Parses a metadata property value from its lexical representation. It allows determination of value type based upon property name conventions, as well as
	 * defined ontology rules for vocabularies. If a property value does not conform to a defined ontology, an exception will be thrown. If a property value
	 * cannot be parsed based upon property name conventions, only a warning will be logged.
	 * <p>
	 * If no particular type can be determined for a property, the {@link CharSequence} value is converted to a {@link String}. This allows a flexible value
	 * source, while ensuring that unrecognized lexical values are still stored as strings.
	 * </p>
	 * @apiNote This method is appropriate if the underlying format, such as HTML, exposes metadata values only in string format. If the underlying format
	 *          provides rich metadata value types (such as TURF embedded in Markdown), property value type inference such as provided by this method should
	 *          normally not be used unless the format's type system provides insufficient types (such as YAML embedded in Markdown); in this case this method
	 *          should be called on the properties with string values.
	 * @implSpec If the property is in the URF ad-hoc namespace, the property value type is inferred from the property name by convention, as defined by the
	 *           following regular expressions:
	 *           <dl>
	 *           <dt><code>/.+On/</code> (e.g. <code>publishedOn</code> or <code>fromAtoZOn</code>)</dt>
	 *           <dd>{@link LocalDate}</dd>
	 *           </dl>
	 * @implSpec This implementation does not yet support ontology definitions for namespaced vocabularies.
	 * @param propertyTag The tag of the property the value of which is being parsed.
	 * @param propertyLexicalValue The lexical property value to be parsed; typically a {@link String}.
	 * @return The resulting property value, perhaps in another type.
	 * @throws IllegalArgumentException if a property value cannot be parsed conforming to ontology rules.
	 */
	protected static Object parseMetadataPropertyValue(@Nonnull final URI propertyTag, @Nonnull final CharSequence propertyLexicalValue) {
		if(isPresentAndEquals(URF.Tag.findNamespace(propertyTag), URF.AD_HOC_NAMESPACE)) {
			final String propertyName = URF.Tag.findName(propertyTag).orElse(null);
			if(propertyName != null) {
				if(AD_HOC_PROPERTY_LOCAL_DATE_NAME_PATTERN.matcher(propertyName).matches()) { //`xxxOn` - LocalDate
					try {
						return LocalDate.parse(propertyLexicalValue);
					} catch(final DateTimeParseException dateTimeParseException) {
						Clogr.getLogger(AbstractMummifier.class).warn("Property `{}` value `{}` cannot be parsed as a local date: {}.", propertyName, propertyLexicalValue,
								dateTimeParseException.getLocalizedMessage());
					}
				}
			}
		} else {
			//TODO add support for custom namespaces with defined ontologies
		}
		return propertyLexicalValue.toString(); //default to the string form of the lexical value
	}

}
