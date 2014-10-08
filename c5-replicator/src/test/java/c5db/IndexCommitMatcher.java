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

package c5db;

import c5db.interfaces.replication.IndexCommitNotice;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class IndexCommitMatcher extends TypeSafeMatcher<IndexCommitNotice> {
  private final List<Predicate<IndexCommitNotice>> predicates = new ArrayList<>();
  private final List<Consumer<Description>> describers = new ArrayList<>();

  @Override
  protected boolean matchesSafely(IndexCommitNotice item) {
    return predicates.stream().allMatch((predicate) -> predicate.test(item));
  }

  @Override
  public void describeTo(Description description) {
    describers.forEach((describer) -> describer.accept(description));
  }

  public static IndexCommitMatcher aCommitNotice() {
    return new IndexCommitMatcher().addCriterion(
        (item) -> true,
        (description) -> description
            .appendText("an IndexCommitNotice"));
  }

  public IndexCommitMatcher withSeqNum(Matcher<Long> seqNumMatcher) {
    return addCriterion(
        (item) ->
            seqNumMatcher.matches(item.upToAndIncludingSeqNum),
        (description) ->
            description.appendText(" including log index ")
                .appendDescriptionOf(seqNumMatcher));
  }

  public IndexCommitMatcher withTerm(Matcher<Long> termMatcher) {
    return addCriterion(
        (item) -> termMatcher.matches(item.term),
        (description) ->
            description.appendText(" with election term ").appendDescriptionOf(termMatcher));
  }

  public IndexCommitMatcher issuedFromPeer(long peerId) {
    return addCriterion(
        (item) -> item.nodeId == peerId,
        (description) ->
            description.appendText(" from peer ").appendValue(peerId));
  }

  private IndexCommitMatcher addCriterion(Predicate<IndexCommitNotice> predicate, Consumer<Description> describer) {
    IndexCommitMatcher copy = new IndexCommitMatcher();
    copy.predicates.addAll(this.predicates);
    copy.predicates.add(predicate);
    copy.describers.addAll(this.describers);
    copy.describers.add(describer);
    return copy;
  }
}
