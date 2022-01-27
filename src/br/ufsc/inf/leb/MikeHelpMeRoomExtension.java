package br.ufsc.inf.leb;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.util.ArrayList;

public class MikeHelpMeRoomExtension extends SFSExtension {


    @Override
    public void init() {

        //Custom Requests Handlers:
        this.addRequestHandler("start", StartHandler.class);
        this.addRequestHandler("receiveStory", ReceiveStory.class);
        this.addRequestHandler("haveAllStories", HaveAllStories.class);
        this.addRequestHandler("takePoints", GivePoints.class);

    }

}
