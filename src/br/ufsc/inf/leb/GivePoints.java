package br.ufsc.inf.leb;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.util.ArrayList;
import java.util.Arrays;

public class GivePoints extends BaseClientRequestHandler {

    private String turn = "EP1";

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {

        if (!turn.equals(user.getLastJoinedRoom().getVariable("turn").getStringValue())) {
            turn = user.getLastJoinedRoom().getVariable("turn").getStringValue();
            ListOfVoteList.getInstance().getVoteLists().clear();
            trace("!!!!!!!!!!!!!!!!!!!Novo Turno!!!!!!!!!!!!!!!!!!!");
        }

        trace("TAMANHO: " + ListOfVoteList.getInstance().getVoteLists().size());

        Vote voteRecieved = new Vote(isfsObject.getInt("playerVoted"), isfsObject.getInt("playerNotVoted"));
        boolean found = false;

        if (ListOfVoteList.getInstance().getVoteLists().size() > 0) {
            for (VoteList voteList : ListOfVoteList.getInstance().getVoteLists()) {
                if (voteList.isUserWhoVoted(user)) {
                    trace("###########Econtrei o broski");
                    found = true;
                    if (voteList.containsVote(voteRecieved)) {
                        trace("!!!Player already voted!!!" );
                        return;
                    } else {
                        trace("!!!Não votei ainda!!!");
                        voteList.addVote(voteRecieved);
                        updtateAndPersistEvaluation(user, isfsObject);
                    }
                }
            }

            if (!found) {
                trace("###########Não encontrei");
                VoteList voteList = new VoteList(user);
                voteList.addVote(voteRecieved);
                ListOfVoteList.getInstance().getVoteLists().add(voteList);
                updtateAndPersistEvaluation(user, isfsObject);
            }
        } else {
            trace("##############Está vazio");
            VoteList voteList = new VoteList(user);
            voteList.addVote(voteRecieved);
            ListOfVoteList.getInstance().getVoteLists().add(voteList);
            updtateAndPersistEvaluation(user, isfsObject);
        }

        trace("TAMANHO FINAL: " + ListOfVoteList.getInstance().getVoteLists().size());
    }

    private void updtateAndPersistEvaluation(User user, ISFSObject isfsObject) {
        trace("cheguei");
        trace("Jogador: " + isfsObject.getInt("playerVoted"));
        trace("Turno :" + turn.substring(2));
        trace(user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getName());
        trace(user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getVariable(("E" + turn.substring(2))));
        trace(user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getVariable(("E" + turn.substring(2))).getIntValue());

        UserVariable storyEvaluation = new SFSUserVariable( "E"+ (user.getLastJoinedRoom().getVariable("turn").getStringValue().substring(2)),
                    user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getVariable(("E" + turn.substring(2))).getIntValue()
                            + ServerSetupVariables.POINTS_TO_GIVE.getIntValue());

        trace("User " + user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getName() + " voted on " + storyEvaluation.getName() + ". User Story score: " + storyEvaluation.getIntValue() );

        //trace(storyEvaluation.getIntValue().toString());
        getApi().setUserVariables(user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")), Arrays.asList(storyEvaluation) );
    }
}
