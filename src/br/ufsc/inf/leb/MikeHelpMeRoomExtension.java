package br.ufsc.inf.leb;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.util.ArrayList;

public class MikeHelpMeRoomExtension extends SFSExtension {

    private GameLogic gameLogic;
    private ArrayList<User> usersThatSentStory;

    @Override
    public void init() {
        this.usersThatSentStory = new ArrayList<>();

        //Custom Requests Handlers:
        this.addRequestHandler("start", StartHandler.class);
        this.addRequestHandler("receiveStory", ReceiveStory.class);
        this.addRequestHandler("haveAllStories", HaveAllStories.class);
        this.addRequestHandler("takePoints", GivePoints.class);
    }

    @Override
    public void destroy() {
        gameLogic.getTimerRunnable().cancel(true);
        if (gameLogic.getCheckUserStoryRunnable() != null &&
                (!gameLogic.getCheckUserStoryRunnable().isDone() || !gameLogic.getCheckUserStoryRunnable().isCancelled())) {
            gameLogic.getCheckUserStoryRunnable().cancel(true);
        }
        if (gameLogic.getCheckUsersReceivedAllStoriesRunnable() != null &&
                (!gameLogic.getCheckUsersReceivedAllStoriesRunnable().isDone() || !gameLogic.getCheckUsersReceivedAllStoriesRunnable().isCancelled())) {
            gameLogic.getCheckUsersReceivedAllStoriesRunnable().cancel(true);
        }
    }

    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public ArrayList<User> getUsersThatSentStory() {
        return usersThatSentStory;
    }

    public void setUsersThatSentStory(ArrayList<User> usersThatSentStory) {
        this.usersThatSentStory = usersThatSentStory;
    }

}
