package br.ufsc.inf.leb;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.util.Arrays;

public class StartHandler extends BaseClientRequestHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {

        trace( "Initializing game on Room " + user.getLastJoinedRoom().getName() + "#" +
                user.getLastJoinedRoom().getId() + ".");

        if ( (user.getLastJoinedRoom().getPlayersList().size() <= user.getLastJoinedRoom().getMaxUsers()) &&
                ( user.getLastJoinedRoom().getPlayersList().size() >= ServerSetupVariables.MIN_PLAYERS_TO_START.getIntValue() ) ){

            if (user.getLastJoinedRoom().containsVariable("turn")) {

                RoomVariable turn = new SFSRoomVariable("turn", "RP1");
                turn.setGlobal(true);
                turn.setPrivate(true);

                getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(turn));

                UserVariable receivedAllStories = new SFSUserVariable("receivedAllStories", false );
                UserVariable evaluatedAllStories = new SFSUserVariable("evaluatedAllStories", false );
                UserVariable score = new SFSUserVariable("score", 0 );
                UserVariable idListOfEvaluatedUsers = new SFSUserVariable( "idListOfEvaluatedUsers", new SFSArray());
                idListOfEvaluatedUsers.setHidden(true);

                for (User iterationUser : user.getLastJoinedRoom().getPlayersList()) {
                    getApi().setUserVariables(iterationUser, Arrays.asList(receivedAllStories, evaluatedAllStories, score, idListOfEvaluatedUsers ));
                }

                MikeHelpMeRoomExtension roomExtension = (MikeHelpMeRoomExtension) this.getParentExtension();
                GameLogic gameLogic = new GameLogic((MikeHelpMeRoomExtension) this.getParentExtension(), user.getLastJoinedRoom());
                roomExtension.setGameLogic(gameLogic);
                gameLogic.start();

            }

        }

        trace("Room " + user.getLastJoinedRoom().getName() + "#" + user.getLastJoinedRoom().getId() + " turn changed: " +
                user.getLastJoinedRoom().getVariable("turn").getStringValue());

    }
}
