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

import java.io.IOException;

import javax.annotation.*;

import io.guise.mummy.*;

/**
 * Access to a Domain Name System (DNS).
 * @author Garret Wilson
 */
public interface Dns {

	/**
	 * Common, known resource record types.
	 * @apiNote This list is not exhaustive, but provides merely a convenience, type-safe approach for indicating common types.
	 * @see <a href="https://en.wikipedia.org/wiki/List_of_DNS_record_types">List of DNS record types</a>
	 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
	 * @see <a href="https://tools.ietf.org/html/rfc2308">RFC 2308</a>
	 */
	public static enum ResourceRecordType {

		/**
		 * A host address.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		A(1),
		/**
		 * The canonical name for an alias
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		CNAME(5),
		/**
		 * Mailbox or mail list information.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		HINFO(14),
		/**
		 * A mailbox domain name.
		 * @apiNote Experimental.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		MB(7),
		/**
		 * A mail destination.
		 * @deprecated Obsolete; use {@link #MX}.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		@Deprecated
		MD(3),
		/**
		 * A mail forwarder.
		 * @deprecated Obsolete; use {@link #MX}.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		@Deprecated
		MF(3),
		/**
		 * A mail group member.
		 * @apiNote Experimental.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		MG(8),
		/**
		 * A mail rename domain name.
		 * @apiNote Experimental.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		MR(9),
		/**
		 * Mail exchange.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 * @see <a href="https://tools.ietf.org/html/rfc7505">RFC 7505</a>
		 */
		MX(15),
		/**
		 * An authoritative name server.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		NS(2),
		/**
		 * A null resource record.
		 * @apiNote Experimental.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		NULL(10),
		/**
		 * A domain name pointer.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		PTR(12),
		/**
		 * Marks the start of a zone of authority.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 * @see <a href="https://tools.ietf.org/html/rfc2308">RFC 2308</a>
		 */
		SOA(6),
		/**
		 * Text strings.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		TXT(16),
		/**
		 * A well known service description.
		 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
		 */
		WKS(11);

		private final int id;

		/** @return The type ID. */
		public int getId() {
			return id;
		}

		/**
		 * Constructor.
		 * @param id The type ID.
		 */
		private ResourceRecordType(final int id) {
			this.id = id;
		}

	}

	/**
	 * Prepares the DNS for deploying. This may include creating any accounts or record zones, for example.
	 * @param context The context of static site generation.
	 * @throws IOException if there is an I/O error during site deployment preparation.
	 */
	public void prepare(@Nonnull final MummyContext context) throws IOException;

	//TODO document
	public default void setResourceRecord(@Nonnull final ResourceRecordType type, @Nonnull final String name, @Nonnull final String value) throws IOException {
		setResourceRecord(type.toString(), name, value);
	}

	//TODO document
	public void setResourceRecord(@Nonnull final String type, @Nonnull final String name, @Nonnull final String value) throws IOException;

}
