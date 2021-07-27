package br.ufsc.inf.leb;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.util.Arrays;

public class HaveAllStories extends BaseClientRequestHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {

        UserVariable receivedAllStories = new SFSUserVariable( "receivedAllStories", true);

        trace("User " + user.getName() + "#" + user.getId() + " have all user stories on " + user.getLastJoinedRoom().getVariable("turn").getStringValue());

        getApi().setUserVariables(user, Arrays.asList(receivedAllStories) );
    }
}
