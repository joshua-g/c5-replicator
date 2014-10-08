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

package c5db.interfaces.replication;

import com.google.common.util.concurrent.ListenableFuture;
import org.jetlang.channels.Subscriber;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

/**
 * A replicator instance that is used to keep logs in sync across a quorum.
 */
public interface Replicator {
  /**
   * Return the ID of the quorum (that is, group of coordinating replicators) that this
   * replicator belongs to. For a given Replicator instance, this will always return the
   * same value.
   */
  String getQuorumId();

  /**
   * @return The numerical ID for the server, or node, on which this Replicator resides. More
   * than one Replicator may have the same node ID, but any two Replicators operating at the same
   * time with the same ID will have different quorum IDs. And, considering all the Replicators
   * operating at some time within a single quorum, all must have different node IDs.
   */
  long getId();

  /**
   * Return a future containing the Replicator's current quorum configuration; that is,
   * the set of peers it recognizes as being part of its quorum. This set can change
   * if directed to locally (for instance using the method changeQuorum) or if a remote
   * leader initiates a quorum change.
   */
  ListenableFuture<QuorumConfiguration> getQuorumConfiguration();

  /**
   * Change the members of the quorum to a new collection of peers (which may include peers in
   * the current quorum).
   *
   * @param newPeers The collection of peer IDs in the new quorum.
   * @return a future which will return the replicator receipt for logging the transitional
   * quorum configuration entry, when the entry's index known. The transitional quorum
   * configuration combines the current group of peers with the given collection of new peers.
   * When that transitional configuration is committed, the quorum configuration is guaranteed
   * to go through; prior to that commit, it is possible that a fault will cancel the quorum
   * change operation.
   * <p>
   * The actual completion of the quorum change will be signaled by a ReplicatorInstanceEvent
   * indicating the commitment of the quorum configuration consisting of the given peers,
   */
  ListenableFuture<ReplicatorReceipt> changeQuorum(Collection<Long> newPeers) throws InterruptedException;

  /**
   * Submit data to be replicated.
   * TODO we may want a variation of this method which does not block
   *
   * @param data Some data to log.
   * @return a listenable for a receipt for the log request, OR null if we aren't the leader.
   * The receipt gives information about the replication request that can be used, in conjunction
   * with commit notices, to determine if and when the request was successful.
   */
  ListenableFuture<ReplicatorReceipt> logData(List<ByteBuffer> data) throws InterruptedException;

  /**
   * Start the Replicator. Before this method is called, none of the Subscribers returned by
   * this interface's methods will emit any data. So, subscription should be done before calling
   * start. Afterwards, it may not be deterministic whether or not a notice will be received.
   * // TODO in the future it may make sense to break start out of this interface, along with
   * // TODO the Subscriber methods, since they make no sense to call on a running Replicator.
   */
  void start();

  /**
   * Each time the Replicator changes State, it emits that State from this Subscriber.
   */
  Subscriber<State> getStateChannel();

  // What state is this instance in?
  public enum State {
    FOLLOWER,
    CANDIDATE,
    LEADER,
  }

  /**
   * The Replicator issues events from this Subscriber on various conditions. See the comments on
   * {@link c5db.interfaces.replication.ReplicatorInstanceEvent} for more information.
   */
  Subscriber<ReplicatorInstanceEvent> getEventChannel();

  /**
   * Get the Replicator's commit notice channel. By matching these issued IndexCommitNotices
   * against the ReplicatorReceipts returned when logging entries or changing quorums, users
   * of the Replicator can determine whether those submissions were successfully replicated
   * to the quorum. See {@link c5db.interfaces.replication.IndexCommitNotice} for more info.
   */
  Subscriber<IndexCommitNotice> getCommitNoticeChannel();
}
