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

package c5db.replication;

import c5db.RpcMatchers;
import c5db.interfaces.log.SequentialEntry;
import c5db.interfaces.replication.QuorumConfiguration;
import c5db.interfaces.replication.ReplicatorInstanceEvent;
import c5db.replication.generated.LogEntry;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Assorted matchers used for replicator tests.
 */
class ReplicationMatchers {

  static Matcher<ReplicatorInstanceEvent> leaderElectedEvent(Matcher<Long> leaderMatcher,
                                                             Matcher<Long> termMatcher) {
    return new TypeSafeMatcher<ReplicatorInstanceEvent>() {
      @Override
      protected boolean matchesSafely(ReplicatorInstanceEvent item) {
        return item.eventType == ReplicatorInstanceEvent.EventType.LEADER_ELECTED
            && leaderMatcher.matches(item.newLeader)
            && termMatcher.matches(item.leaderElectedTerm);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a ReplicatorInstanceEvent indicating a leader was elected")
            .appendText(" with id ").appendDescriptionOf(leaderMatcher)
            .appendText(" with term ").appendDescriptionOf(termMatcher);

      }
    };
  }

  static Matcher<ReplicatorInstanceEvent> aQuorumChangeCommittedEvent(QuorumConfiguration configuration,
                                                                      Matcher<Long> fromMatcher) {
    return new TypeSafeMatcher<ReplicatorInstanceEvent>() {
      @Override
      protected boolean matchesSafely(ReplicatorInstanceEvent item) {
        return item.eventType == ReplicatorInstanceEvent.EventType.QUORUM_CONFIGURATION_COMMITTED
            && fromMatcher.matches(item.instance.getId())
            && item.configuration.equals(configuration);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a ReplicatorInstanceEvent indicating quorum configuration ").appendValue(configuration)
            .appendText(" was committed from replicator with ID ").appendDescriptionOf(fromMatcher);
      }
    };
  }

  static Matcher<ReplicatorInstanceEvent> aReplicatorEvent(ReplicatorInstanceEvent.EventType type) {
    return new TypeSafeMatcher<ReplicatorInstanceEvent>() {
      @Override
      protected boolean matchesSafely(ReplicatorInstanceEvent item) {
        return item.eventType == type;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a ReplicatorInstanceEvent of type ").appendValue(type);
      }
    };
  }

  static Matcher<InRamTest.PeerController> theLeader() {
    return new TypeSafeMatcher<InRamTest.PeerController>() {
      @Override
      protected boolean matchesSafely(InRamTest.PeerController peer) {
        return peer.isCurrentLeader();
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("The peer who is the leader of the current term");
      }
    };
  }

  static Matcher<InRamTest.PeerController> wonAnElectionWithTerm(Matcher<Long> termMatcher) {
    return new TypeSafeMatcher<InRamTest.PeerController>() {
      @Override
      protected boolean matchesSafely(InRamTest.PeerController peer) {
        return peer.hasWonAnElection(termMatcher);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a peer who won an election with term ").appendDescriptionOf(termMatcher);
      }
    };
  }

  static Matcher<InRamTest.PeerController> hasCommittedEntriesUpTo(long index) {
    return new TypeSafeMatcher<InRamTest.PeerController>() {
      @Override
      protected boolean matchesSafely(InRamTest.PeerController peer) {
        return peer.hasCommittedEntriesUpTo(index);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Peer has committed log entries up to index" + index);
      }
    };
  }

  static Matcher<InRamTest.PeerController> willCommitEntriesUpTo(long index) {
    return new TypeSafeMatcher<InRamTest.PeerController>() {
      Throwable matchException;

      @Override
      protected boolean matchesSafely(InRamTest.PeerController peer) {
        try {
          peer.waitForCommit(index);
          assert peer.log.getLastIndex() >= index;
        } catch (Exception e) {
          matchException = e;
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Peer will commit log entries up to index " + index);
      }

      @Override
      public void describeMismatchSafely(InRamTest.PeerController peer, Description description) {
        if (matchException != null) {
          description.appendValue(matchException.toString());
        }
      }
    };
  }

  static Matcher<InRamTest.PeerController> willCommitConfiguration(QuorumConfiguration configuration) {
    return new TypeSafeMatcher<InRamTest.PeerController>() {
      Throwable matchException;

      @Override
      protected boolean matchesSafely(InRamTest.PeerController peer) {
        try {
          peer.waitForQuorumCommit(configuration);
        } catch (Exception e) {
          matchException = e;
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Peer will commit the quorum configuration ").appendValue(configuration);
      }

      @Override
      public void describeMismatchSafely(InRamTest.PeerController peer, Description description) {
        if (matchException != null) {
          description.appendValue(matchException.toString());
        }
      }
    };
  }

  static Matcher<InRamTest.PeerController> willRespondToAnAppendRequest(long minimumTerm) {
    return new TypeSafeMatcher<InRamTest.PeerController>() {
      Throwable matchException;

      @Override
      protected boolean matchesSafely(InRamTest.PeerController peer) {
        try {
          peer.waitForAppendReply(greaterThanOrEqualTo(minimumTerm));
        } catch (Exception e) {
          matchException = e;
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Peer will respond to an AppendEntries request");
      }

      @Override
      public void describeMismatchSafely(InRamTest.PeerController peer, Description description) {
        if (matchException != null) {
          description.appendValue(matchException.toString());
        }
      }
    };
  }

  static Matcher<InRamTest.PeerController> willSend(RpcMatchers.RequestMatcher requestMatcher) {
    return new TypeSafeMatcher<InRamTest.PeerController>() {
      Throwable matchException;

      @Override
      protected boolean matchesSafely(InRamTest.PeerController peer) {
        try {
          peer.waitForRequest(requestMatcher);
        } catch (Exception e) {
          matchException = e;
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Peer will send an AppendEntries request ").appendDescriptionOf(requestMatcher);
      }

      @Override
      public void describeMismatchSafely(InRamTest.PeerController peer, Description description) {
        if (matchException != null) {
          description.appendValue(matchException.toString());
        }
      }
    };
  }

  static Matcher<List<LogEntry>> aListOfEntriesWithConsecutiveSeqNums(long start, long end) {
    return new TypeSafeMatcher<List<LogEntry>>() {
      @Override
      protected boolean matchesSafely(List<LogEntry> entries) {
        if (entries.size() != (end - start)) {
          return false;
        }
        long expectedIndex = start;
        for (LogEntry entry : entries) {
          if (entry.getIndex() != expectedIndex) {
            return false;
          }
          expectedIndex++;
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a list of LogEntry with consecutive indexes from ")
            .appendValue(start).appendText(" inclusive to ").appendValue(end).appendText(" exclusive");
      }
    };
  }

  static Matcher<SequentialEntry> aSequentialEntryWithSeqNum(Matcher<Long> seqNumMatcher) {
    return new TypeSafeMatcher<SequentialEntry>() {
      @Override
      protected boolean matchesSafely(SequentialEntry item) {
        return seqNumMatcher.matches(item.getSeqNum());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a SequentialEntry with sequence number ").appendDescriptionOf(seqNumMatcher);
      }
    };
  }
}
