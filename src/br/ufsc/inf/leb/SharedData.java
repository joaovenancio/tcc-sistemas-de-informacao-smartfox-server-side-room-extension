package br.ufsc.inf.leb;

import java.util.ArrayList;

public class SharedData {

    private static SharedData instance;
    private ArrayList<VoteList> voteLists;
    private String turn;

    private SharedData() {
        this.voteLists = new ArrayList<VoteList>();
        this.turn = "EP1";
    }

    public synchronized void clear () {
        this.voteLists.clear();
    }

    public synchronized int size () {
        return this.voteLists.size();
    }

    public static synchronized SharedData getInstance() {
        if (instance == null) {
            instance = new SharedData();
        }

        return instance;
    }

    public ArrayList<VoteList> getVoteLists() {
        return voteLists;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }
}
