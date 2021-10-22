package br.ufsc.inf.leb;

import com.smartfoxserver.v2.entities.User;

public class Vote {

    private int userVoted;
    private int userNotVoted;

    public Vote(int userVoted, int userNotVoted) {
        this.userVoted = userVoted;
        this.userNotVoted = userNotVoted;
    }

    public boolean sameVote(int voted, int notVoted) {

        if ((voted == this.userVoted) &&
        notVoted == this.userNotVoted)  {
            return true;
        }

        return false;
    }

    public boolean sameVote(Vote vote) {

        if ((vote.getUserVoted() == this.userVoted) &&
                vote.getUserNotVoted() == this.userNotVoted)  {
            return true;
        }

        return false;
    }

    public int getUserVoted() {
        return userVoted;
    }

    public void setUserVoted(int userVoted) {
        this.userVoted = userVoted;
    }

    public int getUserNotVoted() {
        return userNotVoted;
    }

    public void setUserNotVoted(int userNotVoted) {
        this.userNotVoted = userNotVoted;
    }

}
