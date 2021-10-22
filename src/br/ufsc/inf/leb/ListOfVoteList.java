package br.ufsc.inf.leb;

import java.util.ArrayList;

public class ListOfVoteList {

    private static ListOfVoteList instance;
    private ArrayList<VoteList> voteLists;

    private ListOfVoteList() {
        this.voteLists = new ArrayList<VoteList>();
    }

    public synchronized void clear () {
        this.voteLists.clear();
    }

    public synchronized int size () {
        return this.voteLists.size();
    }

    public static synchronized ListOfVoteList getInstance() {
        if (instance == null) {
            instance = new ListOfVoteList();
        }

        return instance;
    }

    public ArrayList<VoteList> getVoteLists() {
        return voteLists;
    }
}
