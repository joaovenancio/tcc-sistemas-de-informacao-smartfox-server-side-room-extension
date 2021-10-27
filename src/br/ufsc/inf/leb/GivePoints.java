package br.ufsc.inf.leb;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.util.Arrays;

public class GivePoints extends BaseClientRequestHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {

        if (!SharedData.getInstance().getTurn().equals(user.getLastJoinedRoom().getVariable("turn").getStringValue())) {
            SharedData.getInstance().setTurn(user.getLastJoinedRoom().getVariable("turn").getStringValue());
            SharedData.getInstance().getVoteLists().clear();
            trace(SharedData.getInstance().getTurn());
            trace(user.getLastJoinedRoom().getVariable("turn").getStringValue());
            trace("!!!!!!!!!!!!!!!!!!!Novo Turno!!!!!!!!!!!!!!!!!!!");
        }

        trace("TAMANHO: " + SharedData.getInstance().getVoteLists().size());

        Vote voteRecieved = new Vote(isfsObject.getInt("playerVoted"), isfsObject.getInt("playerNotVoted"));
        boolean found = false;

        if (SharedData.getInstance().getVoteLists().size() > 0) {
            for (VoteList voteList : SharedData.getInstance().getVoteLists()) {
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
                SharedData.getInstance().getVoteLists().add(voteList);
                updtateAndPersistEvaluation(user, isfsObject);
            }
        } else {
            trace("##############Está vazio");
            VoteList voteList = new VoteList(user);
            voteList.addVote(voteRecieved);
            SharedData.getInstance().getVoteLists().add(voteList);
            updtateAndPersistEvaluation(user, isfsObject);
        }

        trace("TAMANHO FINAL: " + SharedData.getInstance().getVoteLists().size());
    }

    private void updtateAndPersistEvaluation(User user, ISFSObject isfsObject) {
//        trace(user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getName());
//        trace(user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getVariable(("E" + turn.substring(2))));
//        trace(user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getVariable(("E" + turn.substring(2))).getIntValue());

        int totalScore = user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getVariable("score").getIntValue();

        UserVariable userScore = new SFSUserVariable("score", totalScore + ServerSetupVariables.POINTS_TO_GIVE.getIntValue());
        UserVariable storyEvaluation = new SFSUserVariable( "E"+ (user.getLastJoinedRoom().getVariable("turn").getStringValue().substring(2)),
                    user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getVariable(("E" + SharedData.getInstance().getTurn().substring(2))).getIntValue()
                            + ServerSetupVariables.POINTS_TO_GIVE.getIntValue());

        trace("User " + user.getName() + "voted on User " + user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")).getName() + ". User Story score: " + storyEvaluation.getIntValue() );

        //trace(storyEvaluation.getIntValue().toString());
        getApi().setUserVariables(user.getLastJoinedRoom().getUserById(isfsObject.getInt("playerVoted")), Arrays.asList(storyEvaluation, userScore) );
    }
}
