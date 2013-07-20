package ohmdb.replication.rpc;

import com.google.protobuf.MessageLite;
import ohmdb.replication.Raft;

/**
 * Wrap a rpc message.
 */
public class RpcMessage {
    public final long to;
    public final long from;
    public final long messageId;

    public final MessageLite message;

    public RpcMessage(long to, long from, long messageId, MessageLite message) {
        this.to = to;
        this.from = from;
        this.messageId = messageId;

        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("From: %d to: %d message: %d contents: %s", from, to, messageId, message);
    }

    public boolean isAppendMessage() {
        return message instanceof Raft.AppendEntries;
    }
    public boolean isRequestVoteMessage() {
        return message instanceof Raft.RequestVote;
    }
    public boolean isAppendReplyMessage() {
        return message instanceof Raft.AppendEntriesReply;
    }
    public boolean isRequestVoteReplyMessage() {
        return message instanceof Raft.RequestVoteReply;
    }

    public Raft.AppendEntries getAppendMessage() {
        assert isAppendMessage();

        return (Raft.AppendEntries) message;
    }
    public Raft.AppendEntriesReply getAppendReplyMessage() {
        assert isAppendReplyMessage();

        return (Raft.AppendEntriesReply) message;
    }

    public Raft.RequestVote getRequestVoteMessage() {
        assert isRequestVoteMessage();

        return (Raft.RequestVote) message;
    }

    public Raft.RequestVoteReply getRequestVoteReplyMessage() {
        assert isRequestVoteReplyMessage();

        return (Raft.RequestVoteReply) message;
    }
}
