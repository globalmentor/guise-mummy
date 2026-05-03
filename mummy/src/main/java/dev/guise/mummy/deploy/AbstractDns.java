/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.guise.mummy.deploy;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.jspecify.annotations.*;

import org.slf4j.Logger;

import com.globalmentor.net.*;

import static com.globalmentor.net.DnsName.*;

import io.clogr.Clogged;
import dev.guise.mummy.*;

/// Abstract base class for a DNS implementation, including necessary functionality for setting configured zone records.
/// @author Garret Wilson
public abstract class AbstractDns implements Dns, Clogged {

	/// {@inheritDoc}
	/// @implSpec This implementation returns a set containing only `dns`, indicating the DNS protocol.
	/// @see <a href="https://tools.ietf.org/html/rfc4501">RFC 4501: Domain Name System Uniform Resource Identifiers</a>
	@Override
	public Set<String> getSupportedProtocols() {
		return Set.of("dns"); //TODO use a constant
	}

	private DomainName origin;

	@Override
	public DomainName getOrigin() {
		return origin;
	}

	private List<ResourceRecord> resourceRecords;

	/// Returns the resource records to be created during deployment.
	/// @return The resource records to be created during deployment.
	public List<ResourceRecord> getResourceRecords() {
		return resourceRecords;
	}

	/// Origin and resource records constructor.
	/// @param origin The fully qualified base domain name for the DNS zone.
	/// @param resourceRecords The resource records to be created during deployment; may be empty.
	/// @throws IllegalArgumentException if the given origin is not absolute.
	public AbstractDns(@NonNull final DomainName origin, @NonNull final Collection<ResourceRecord> resourceRecords) {
		this.origin = checkArgumentAbsolute(origin);
		this.resourceRecords = List.copyOf(resourceRecords);
	}

	/// {@inheritDoc}
	/// @implSpec This implementation sets all resource records defined for the DNS by delegating to [#setResourceRecords(Collection)].
	/// @see #getResourceRecords()
	@Override
	public Optional<URI> deploy(final MummyContext context, final Artifact rootArtifact) throws IOException {
		final Logger logger = getLogger();
		final DomainName origin = getOrigin();
		for(final ResourceRecord resourceRecord : getResourceRecords()) {
			final DnsName recordName = resourceRecord.getName().map(origin::resolve).orElse(origin);
			logger.info("Deploying DNS resource record [{}] `{}` = `{}` ({}).", resourceRecord.getType(), recordName, resourceRecord.getValue(),
					resourceRecord.getTtl().orElse(DEFAULT_TTL));
		}
		setResourceRecords(getResourceRecords());
		return Optional.empty();
	}

}
