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

/**
 * A broadcast that indicates that Replicator entries have become visible, i.e., been
 * committed, up to and including a certain sequence number. If several IndexCommitNotices
 * are sent out in a row, each one describes the set of new entries that have been
 * committed since the one immediately preceding it.
 * <p>
 * The set of entries described by this notice must correspond to a single election term,
 * so if a given range comprises multiple terms and they are all committed at once,
 * multiple IndexCommitNotices will be needed to signal that fact -- one for each term.
 * <p>
 * The first IndexCommitNotice emitted by a new Replicator is special; of course it does
 * not have any preceding notice, so it describes an open range: it indicates that all
 * sequence numbers less than or equal to a certain sequence number are committed. Also,
 * in that case, the term number only represents the term of the last entry in the range.
 */
public class IndexCommitNotice {
  public final String quorumId;
  public final long nodeId;
  public final long upToAndIncludingSeqNum;
  public final long term;

  public IndexCommitNotice(String quorumId,
                           long nodeId,
                           long upToAndIncludingSeqNum,
                           long term) {
    this.quorumId = quorumId;
    this.nodeId = nodeId;
    this.upToAndIncludingSeqNum = upToAndIncludingSeqNum;
    this.term = term;
  }

  @Override
  public String toString() {
    return "IndexCommitNotice{" +
        "quorumId=" + quorumId +
        ", nodeId=" + nodeId +
        ", upToAndIncludingSeqNum=" + upToAndIncludingSeqNum +
        ", term=" + term +
        '}';
  }
}
