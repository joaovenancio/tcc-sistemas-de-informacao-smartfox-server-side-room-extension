package br.ufsc.inf.leb;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReceiveStory extends BaseClientRequestHandler {

    private int turnIndex;

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        trace("User " + user.getName() + "sent a user story on game " + user.getLastJoinedRoom().getName() + "#" + user.getLastJoinedRoom().getId());
        MikeHelpMeRoomExtension roomExtension = (MikeHelpMeRoomExtension) getParentExtension();

        for (User iterationUser : roomExtension.getUsersThatSentStory()) {
            if (iterationUser.getId() == user.getId()) {
                return;
            }
        }

        UserVariable story = new SFSUserVariable( "H"+ (user.getLastJoinedRoom().getVariable("turn").getStringValue().substring(2)),
                isfsObject.getUtfString("story"));
        UserVariable storyEvaluation = new SFSUserVariable( "E"+ (user.getLastJoinedRoom().getVariable("turn").getStringValue().substring(2)),
                0);

        trace(story.getName());
        trace(story.getStringValue());

        getApi().setUserVariables(user, Arrays.asList(story, storyEvaluation) );

    }

}
