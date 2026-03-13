/*
 * Copyright © 2026 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package dev.guise.mummy;

import static com.globalmentor.collections.Sets.*;

import java.util.Set;

/// Walks artifact trees in depth-first pre-order, visiting all
/// [comprised artifacts][CompositeArtifact#comprisedArtifacts()] including
/// [subsumed][CompositeArtifact#getSubsumedArtifacts()] artifacts.
///
/// The [Visitor] receives the subsumption status of each artifact, allowing it to decide how to handle
/// subsumed artifacts — skip for counting, include for indexing, etc.
///
/// @implNote The current implementation uses recursion for simplicity. The artifact tree is abstract and
/// not necessarily backed by a filesystem, so no particular depth bound is assumed. For foreseeable usage
/// (website artifact trees), recursion depth is well within Java's default stack limits. If a future data
/// source produced trees with unusual depth characteristics, the implementation could switch to an
/// iterative approach using an explicit stack without changing the API.
/// @see MummyPlan#walk(ArtifactTreeWalker.Visitor)
public final class ArtifactTreeWalker {

	/// A callback for receiving artifacts during tree traversal.
	///
	/// @apiNote The `subsumed` flag communicates whether the artifact has been absorbed into its parent
	/// composite — a property the visitor cannot derive from the artifact alone. Intrinsic artifact
	/// properties such as navigability or asset designation are available directly on the [Artifact]
	/// interface.
	@FunctionalInterface
	public interface Visitor {

		/// Visits an artifact during tree traversal.
		///
		/// @param artifact The artifact being visited.
		/// @param subsumed Whether this artifact has been subsumed by its parent composite artifact and should
		///        not appear as a separate IRI path reference.
		void visit(Artifact artifact, boolean subsumed);

	}

	private ArtifactTreeWalker() { // prevent instantiation
	}

	/// Walks the artifact tree rooted at the given artifact, visiting each artifact in depth-first pre-order.
	///
	/// The walk descends into all [comprised artifacts][CompositeArtifact#comprisedArtifacts()] of each
	/// [CompositeArtifact], including [subsumed][CompositeArtifact#getSubsumedArtifacts()] artifacts. The
	/// visitor receives the subsumption status of each artifact, allowing it to decide how to handle subsumed
	/// artifacts — skip for counting, include for indexing, etc.
	///
	/// @apiNote This provides a single traversal mechanism for artifact tree walks, replacing the repeated
	/// recursion boilerplate in deployment planning, plan description, and other artifact-processing code.
	/// @param root The artifact to start the walk from. If not a [CompositeArtifact], the visitor is called
	///        once for this artifact and the walk completes.
	/// @param visitor The visitor to invoke for each artifact.
	/// @see MummyPlan#walk(ArtifactTreeWalker.Visitor)
	public static void walk(final Artifact root, final Visitor visitor) {
		walk(root, false, visitor);
	}

	/// Walks the artifact tree rooted at the given artifact with the given subsumption status.
	///
	/// @param artifact The current artifact being visited.
	/// @param subsumed Whether this artifact is subsumed by its parent composite.
	/// @param visitor The visitor receiving each artifact.
	private static void walk(final Artifact artifact, final boolean subsumed, final Visitor visitor) {
		visitor.visit(artifact, subsumed);
		if(artifact instanceof CompositeArtifact compositeArtifact) {
			final Set<Artifact> subsumedArtifacts = toSet(compositeArtifact.getSubsumedArtifacts());
			compositeArtifact.comprisedArtifacts().forEach(comprisedArtifact -> walk(comprisedArtifact, subsumedArtifacts.contains(comprisedArtifact), visitor));
		}
	}

}
