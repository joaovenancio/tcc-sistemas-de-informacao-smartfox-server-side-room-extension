package br.ufsc.inf.leb;

import com.smartfoxserver.v2.entities.User;

import java.util.ArrayList;

public class VoteList {

    private User whoVoted;
    private ArrayList<Vote> voteList;

    public VoteList(User whoVoted) {
        this.whoVoted = whoVoted;
        this.voteList = new ArrayList<Vote>();
    }

    public boolean containsVote (Vote vote) {
        for (Vote voteInList : voteList ) {
            if (vote.sameVote(voteInList)) {
                return true;
            }
        }
        return false;
    }

    public boolean isUserWhoVoted (User whoVoted) {
        if (whoVoted.getId() == this.whoVoted.getId()) {
            return true;
        }
        return false;
    }

    public void clearVoteList() {
        this.voteList.clear();
        this.voteList = new ArrayList<Vote>();
    }

    public boolean addVote (Vote vote) {
        return this.voteList.add(vote);
    }

}
