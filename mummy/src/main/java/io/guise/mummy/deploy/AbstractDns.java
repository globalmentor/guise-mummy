/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy.deploy;

import static com.globalmentor.collections.Lists.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.annotation.*;

import org.slf4j.Logger;

import com.globalmentor.net.*;

import io.clogr.Clogged;
import io.guise.mummy.*;
import io.guise.mummy.deploy.Dns;

/**
 * Abstract base class for a DNS implementation, including necessary functionality for setting configured zone records.
 * @author Garret Wilson
 */
public abstract class AbstractDns implements Dns, Clogged {

	private DomainName origin;

	@Override
	public DomainName getOrigin() {
		return origin;
	}

	private List<ResourceRecord> resourceRecords;

	/** @return The resource records to be created during deployment. */
	public List<ResourceRecord> getResourceRecords() {
		return resourceRecords;
	}

	/**
	 * Origin and resource records constructor.
	 * @param origin The fully qualified base domain name for the DNS zone.
	 * @param resourceRecords The resource records to be created during deployment; may be empty.
	 * @throws IllegalArgumentException if the given origin is not absolute.
	 */
	public AbstractDns(@Nonnull final DomainName origin, @Nonnull final Collection<ResourceRecord> resourceRecords) {
		this.origin = origin.checkArgumentAbsolute();
		this.resourceRecords = immutableListOf(resourceRecords);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation sets all resource records defined for the DNS by delegating to {@link #setResourceRecords(Iterable)}.
	 * @see #getResourceRecords()
	 */
	@Override
	public Optional<URI> deploy(final MummyContext context, final Artifact rootArtifact) throws IOException {
		final Logger logger = getLogger();
		for(final ResourceRecord resourceRecord : getResourceRecords()) {
			logger.info("Deploying DNS resource record [{}] `{}` = `{}` ({}).", resourceRecord.getType(),
					getOrigin().resolve(resourceRecord.getName().orElse(DomainName.EMPTY)), resourceRecord.getValue(), resourceRecord.getTtl().orElse(DEFAULT_TTL));
		}
		setResourceRecords(getResourceRecords());
		return Optional.empty();
	}

}
