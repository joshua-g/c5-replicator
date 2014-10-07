/*
 * Copyright 2014 WANdisco
 *
 *  WANdisco licenses this file to you under the Apache License,
 *  version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package c5db.interfaces;

import c5db.interfaces.replication.Replicator;
import c5db.messages.generated.ModuleType;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;

/**
 * Replication is the method by which we get multiple copies of the C5 write-ahead-log
 * to multiple machines. It forms the core of C5's ability to survive machine failures.
 * <p>
 * The replication module manages multiple replicator instances, each one responsible for a
 * small chunk of the data-space, also known as a tablet (or region), each one participating
 * with a set of other replicators known as its quorum, identified by a String called the
 * quorum ID.
 * <p>
 * More generally, a Replicator can replicate, and organize the consensus, of an arbitrary
 * sequence of data, not just write-ahead logs.
 */
@DependsOn({DiscoveryModule.class, LogModule.class})
@ModuleTypeBinding(ModuleType.Replication)
public interface ReplicationModule extends C5Module {
  /**
   * Create a new instance of Replicator associated with the given quorum ID.
   * <p>
   * A given C5 node may host replication services from several different quorums.
   * A "quorum" refers to the set of Replicators on different nodes coordinating in
   * replicating one sequence of logged data. The replicators accomplish this by
   * making use of some persistence local to the node on which they are hosted.
   * This persistence of data creates a continuity of replication for a given node
   * and quorum ID, that may span different Replicator instances, different
   * ReplicationModule instances, or different C5Module instances.
   * <p>
   * In other words, in case of a server crash, it should be possible to boot up a
   * new server and pick up where the previous one left off. In that case, the new
   * server can recreate its Replicator for the desired quorum simply by invoking
   * this method with the ID of the quorum the caller wishes to continue.
   * <p>
   * If the given quorum ID has no prior history on this node, it will be established
   * with the given collection of cooperating peers, identified by their node IDs. If
   * on the other hand the quorum does have prior history, the passed peer collection
   * (if any) will be ignored, and instead the quorum's history will be used to
   * determine who are the peers.
   * <p>
   * The peer collection should include every node the caller wants to be part of the
   * quorum, including this node.
   * TODO test and document the possibility of creating a Replicator that observes a
   * TODO  quorum without actively participating in its replication.
   *
   * @param quorumId ID (identifying String key) of the quorum the caller wants to
   *                 create a Replicator for
   * @param peers    Collection of peers to be included in a new quorum; ignored for
   *                 existing ones
   * @return A future which will return the desired Replicator. Exceptions
   * encountered in the creation of the Replicator will be set to the future. The
   * Replicator will not yet be started, so the recipient must call start() before
   * using it.
   */
  ListenableFuture<Replicator> createReplicator(String quorumId,
                                                Collection<Long> peers);

}
