package br.ufsc.inf.leb;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

                RoomVariable maxPoints = new SFSRoomVariable("maxPoints", 0);
                maxPoints.setGlobal(true);

                ISFSArray sfsa = new SFSArray();
                RoomVariable usersThatSentStory = new SFSRoomVariable("usersThatSentStory", sfsa);
                usersThatSentStory.setGlobal(false);
                usersThatSentStory.setPrivate(true);

                getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(turn, maxPoints, usersThatSentStory));

                UserVariable receivedAllStories = new SFSUserVariable("receivedAllStories", false );
                UserVariable evaluatedAllStories = new SFSUserVariable("evaluatedAllStories", false );
                UserVariable score = new SFSUserVariable("score", 0 );
                UserVariable idListOfEvaluatedUsers = new SFSUserVariable( "idListOfEvaluatedUsers", new SFSArray());
                idListOfEvaluatedUsers.setHidden(true);

                for (User iterationUser : user.getLastJoinedRoom().getPlayersList()) {
                    getApi().setUserVariables(iterationUser, Arrays.asList(receivedAllStories, evaluatedAllStories, score, idListOfEvaluatedUsers ));
                }

                startGame(user);

            }

        }

        traceRoomTurnUpdate(user.getLastJoinedRoom(), user.getLastJoinedRoom().getVariable("turn").getStringValue());

    }

    //Var list:
    //timerCount
    //usersThatSentStory
    private void startGame(User user) {
        ScheduledFuture checkUserStoryRunnable;
        ScheduledFuture checkUsersReceivedAllStoriesRunnable;

        trace("Game on " + user.getLastJoinedRoom().getName() + "#" + user.getLastJoinedRoom().getId() + " has started.");

        user.getLastJoinedRoom().setProperty("timerRunnable", getApi().getSystemScheduler().scheduleAtFixedRate(() -> {
            int timerCount = user.getLastJoinedRoom().getVariable("timer").getIntValue();
            trace("Antes: " + timerCount);
            RoomVariable timer = new SFSRoomVariable("timer", (timerCount + 1) );
            trace("Depois: "+ timer.getIntValue());
            timer.setGlobal(false);
            timer.setPrivate(true);
            getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(timer));
            //trace("DEBUG: "+ user); !!!
            checkGameState(user);
        }, 0, 1, TimeUnit.SECONDS));

    }

    private void checkGameState(User user) {
        if (user.getLastJoinedRoom().getPlayersList().size() >= ServerSetupVariables.MIN_PLAYERS_TO_START.getIntValue()) {
            String turn = (String) (user.getLastJoinedRoom().getVariable("turn").getValue());
            int timerCount = user.getLastJoinedRoom().getVariable("timer").getIntValue();
            trace(timerCount);

            switch (turn.substring(0, 2)) {
                case "RP":
                    if (timerCount == ServerSetupVariables.TIMER_COUNT_READING_PHASE.getIntValue()) {
                        String userStory = turn.substring(2);

                        updateTurnRoomVariable(user,"PP" + userStory);

                        updateTimer(user, 0);
                    }
                    break;

                case "PP":
                    if (timerCount == ServerSetupVariables.TIMER_COUNT_PROPOSING_PHASE.getIntValue()) {
                        String userStory = turn.substring(2);

                        //checkIfAllUsersSentTheirStories(user, userStory);

                        //checkIfAllUsersReceivedAllStories(user);

                        updateTurnRoomVariable(user, "EP" + userStory);

                        updateTimer(user, 0);

                    }
                    break;

                case "EP":
                    if (timerCount == ServerSetupVariables.TIMER_COUNT_EVALUATING_PHASE.getIntValue()) {
                        String userStory = turn.substring(2);

                        //checkIfAllUsersEvaluatedAllStories();

                        updateTurnRoomVariable(user,"SP" + userStory);

                        updateTimer(user, 0);

                    }
                    break;

                case "SP":
                    if (timerCount == ServerSetupVariables.TIMER_COUNT_SCORE_PHASE.getIntValue()) {
                        int userStory = Integer.parseInt(turn.substring(2));

                        if (!(userStory >= ServerSetupVariables.NUMBER_OF_USER_STORIES.getIntValue())) {
                            updateTurnRoomVariable(user,"RP" + String.valueOf(userStory + 1));
                            updateMaxPointsVariable(user);

                            updateTimer(user, 0);

                            //Clear control variable mikeHelpMeRoomExtension:
                            ISFSArray sfsa = new SFSArray();
                            RoomVariable usersThatSentStory = new SFSRoomVariable("usersThatSentStory", sfsa);
                            usersThatSentStory.setGlobal(false);
                            usersThatSentStory.setPrivate(true);

                            getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(usersThatSentStory));

                            resetUserVariables(user);
                        } else {
                            RoomVariable newTurn = new SFSRoomVariable("turn", "ENDEP");
                            newTurn.setGlobal(true);
                            newTurn.setPrivate(true);
                            updateMaxPointsVariable(user);

                            traceRoomTurnUpdate(user.getLastJoinedRoom(), (String) (newTurn.getValue()));
                            getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(newTurn));
                            updateTimer(user, 0);
                            ScheduledFuture timerRunnable = (ScheduledFuture) user.getLastJoinedRoom().getProperty("timerRunnable");
                            timerRunnable.cancel(true);
                            //Salvar Historias
                        }
                    }
                    break;
            }

        } else {
            RoomVariable newTurn = new SFSRoomVariable("turn", "ENDEP");
            newTurn.setGlobal(true);
            newTurn.setPrivate(true);

            traceRoomTurnUpdate(user.getLastJoinedRoom(), (String) (newTurn.getValue()));
            getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(newTurn));
            updateTimer(user, 0);
            ScheduledFuture timerRunnable = (ScheduledFuture) user.getLastJoinedRoom().getProperty("timerRunnable");
            timerRunnable.cancel(true);
            //Salvar Historias
        }
    }

    private void updateTurnRoomVariable(User user, String newTurnValue) {
        RoomVariable newTurn = new SFSRoomVariable("turn", newTurnValue);
        newTurn.setGlobal(true);
        newTurn.setPrivate(true);

        Room room = user.getLastJoinedRoom();

        traceRoomTurnUpdate(room, (String) (newTurn.getValue()));
        getApi().setRoomVariables(null, room, Arrays.asList(newTurn));
    }

    private void traceRoomTurnUpdate(Room room, String newTurn) {
        trace("Room " + room.getName() + "#" + room.getId() + " turn changed: " + newTurn);
    }

    private void updateTimer (User user, int value) {
        RoomVariable timer = new SFSRoomVariable("timer", value );
        timer.setGlobal(false);
        timer.setPrivate(true);
        getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(timer));
    }

    private void checkIfAllUsersSentTheirStories(User user, String userStory) {
        RoomVariable endTimeoutTimer = new SFSRoomVariable("endTimeoutTimer", false );
        endTimeoutTimer.setGlobal(false);
        endTimeoutTimer.setPrivate(true);

        RoomVariable checkUserStoryTimer = new SFSRoomVariable("checkUserStoryTimer", 0 );
        checkUserStoryTimer.setGlobal(false);
        checkUserStoryTimer.setPrivate(true);

        getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(endTimeoutTimer, checkUserStoryTimer));

        ScheduledFuture checkUserStoryRunnable = null;
        Room room = user.getLastJoinedRoom();
        ISFSArray sfsa = user.getLastJoinedRoom().getVariable("usersThatSentStory").getSFSArrayValue();

        if (sfsa.size() != user.getLastJoinedRoom().getPlayersList().size()) {

            ScheduledFuture finalCheckUserStoryRunnable = checkUserStoryRunnable;
            checkUserStoryRunnable = getApi().getSystemScheduler().scheduleAtFixedRate(() -> {
                if (user.getLastJoinedRoom().getVariable("checkUserStoryTimer").getIntValue() >= ServerSetupVariables.WAITING_FOR_SOTRY_TIMETOUT.getIntValue()) {
                    for (User userInPlayerList : room.getPlayersList()) {
                        if (!user.containsVariable("H" + userStory)) {
                            getApi().leaveRoom(user, user.getLastJoinedRoom());
                            getApi().disconnectUser(user);
                            trace("Room " + room.getName() + "#" + room.getId() + ": "
                                    + "Disconnected user " + user.getName() + "#" + user.getId() + " due to inactivity.");
                        }
                    }
                    RoomVariable updateCheckUserStoryTimer = new SFSRoomVariable("checkUserStoryTimer", 0 );
                    updateCheckUserStoryTimer.setGlobal(false);
                    updateCheckUserStoryTimer.setPrivate(true);

                    RoomVariable updateEndTimeoutTimer = new SFSRoomVariable("endTimeoutTimer", true );
                    updateEndTimeoutTimer.setGlobal(false);
                    updateEndTimeoutTimer.setPrivate(true);

                    getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(updateCheckUserStoryTimer,updateEndTimeoutTimer));

                    finalCheckUserStoryRunnable.cancel(true);
                } else {
                    boolean allUsersSentTheirStories = true;

                    for (User userInPlayerList : room.getPlayersList()) {
                        if (!user.containsVariable("H" + userStory)) {
                            allUsersSentTheirStories = false;
                            break;
                        }
                    }
                    if (allUsersSentTheirStories) {
                        RoomVariable updateCheckUserStoryTimer = new SFSRoomVariable("checkUserStoryTimer", 0 );
                        updateCheckUserStoryTimer.setGlobal(false);
                        updateCheckUserStoryTimer.setPrivate(true);

                        RoomVariable updateEndTimeoutTimer = new SFSRoomVariable("endTimeoutTimer", true );
                        updateEndTimeoutTimer.setGlobal(false);
                        updateEndTimeoutTimer.setPrivate(true);

                        getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(updateCheckUserStoryTimer,updateEndTimeoutTimer));

                        finalCheckUserStoryRunnable.cancel(true);
                    } else {
                        int temp = user.getLastJoinedRoom().getVariable("checkUserStoryTimer").getIntValue();
                        RoomVariable updateCheckUserStoryTimer = new SFSRoomVariable("checkUserStoryTimer", temp++ );
                        updateCheckUserStoryTimer.setGlobal(false);
                        updateCheckUserStoryTimer.setPrivate(true);

                        getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(updateCheckUserStoryTimer));

                        trace("Waiting for players to send their user story... Timer: " + updateCheckUserStoryTimer.getIntValue());
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);

            while (!user.getLastJoinedRoom().getVariable("endTimeoutTimer").getBoolValue()) ;

        }
    }

    private void checkIfAllUsersReceivedAllStories(User user) {
        ScheduledFuture checkUsersReceivedAllStoriesRunnable = null;
        RoomVariable endTimeoutTimer = new SFSRoomVariable("endTimeoutTimer", false );
        endTimeoutTimer.setGlobal(false);
        endTimeoutTimer.setPrivate(true);

        RoomVariable receivedStoryTimer = new SFSRoomVariable("receivedStoryTimer", 0 );
        receivedStoryTimer.setGlobal(false);
        receivedStoryTimer.setPrivate(true);

        getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(endTimeoutTimer, receivedStoryTimer));

        ScheduledFuture finalCheckUsersReceivedAllStoriesRunnable = checkUsersReceivedAllStoriesRunnable;
        checkUsersReceivedAllStoriesRunnable = getApi().getSystemScheduler().scheduleAtFixedRate(() -> {
            if (user.getLastJoinedRoom().getVariable("receivedStoryTimer").getIntValue() >= ServerSetupVariables.WAITING_FOR_RECIEVE_TIMETOUT.getIntValue()) {
                for (User userInPlayerList : user.getLastJoinedRoom().getPlayersList()) {
                    if (!userInPlayerList.getVariable("receivedAllStories").getBoolValue()) {
                        getApi().leaveRoom(userInPlayerList, userInPlayerList.getLastJoinedRoom());
                        getApi().disconnectUser(userInPlayerList);
                        trace("Room " + user.getLastJoinedRoom().getName() + "#" + user.getLastJoinedRoom().getId() + ": "
                                + "Disconnected user " + userInPlayerList.getName() + "#" + userInPlayerList.getId() + " due to inactivity.");
                    }
                }
                RoomVariable updateReceivedStoryTimer = new SFSRoomVariable("receivedStoryTimer", 0 );
                updateReceivedStoryTimer.setGlobal(false);
                updateReceivedStoryTimer.setPrivate(true);

                RoomVariable updateEndTimeoutTimer = new SFSRoomVariable("endTimeoutTimer", true );
                updateEndTimeoutTimer.setGlobal(false);
                updateEndTimeoutTimer.setPrivate(true);

                getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(updateReceivedStoryTimer, updateEndTimeoutTimer));

                finalCheckUsersReceivedAllStoriesRunnable.cancel(true);
            } else {
                boolean allUsersHaveAllStories = true;

                for (User userInPlayerList : user.getLastJoinedRoom().getPlayersList()) {
                    if (!userInPlayerList.getVariable("receivedAllStories").getBoolValue()) {
                        allUsersHaveAllStories = false;
                        break;
                    }
                }
                if (allUsersHaveAllStories) {
                    RoomVariable updateReceivedStoryTimer = new SFSRoomVariable("receivedStoryTimer", 0 );
                    updateReceivedStoryTimer.setGlobal(false);
                    updateReceivedStoryTimer.setPrivate(true);

                    RoomVariable updateEndTimeoutTimer = new SFSRoomVariable("endTimeoutTimer", true );
                    updateEndTimeoutTimer.setGlobal(false);
                    updateEndTimeoutTimer.setPrivate(true);

                    getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(updateReceivedStoryTimer, updateEndTimeoutTimer));

                    finalCheckUsersReceivedAllStoriesRunnable.cancel(true);
                } else {
                    int temp = user.getLastJoinedRoom().getVariable("receivedStoryTimer").getIntValue();
                    RoomVariable updateReceivedStoryTimer = new SFSRoomVariable("receivedStoryTimer", temp++ );
                    updateReceivedStoryTimer.setGlobal(false);
                    updateReceivedStoryTimer.setPrivate(true);

                    getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(updateReceivedStoryTimer));

                    trace("Waiting for players to send their user story... Timer: " + receivedStoryTimer);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        while (!user.getLastJoinedRoom().getVariable("endTimeoutTimer").getBoolValue()) ;
    }

    private void updateMaxPointsVariable(User user) {
        int oldMaxPoints = user.getLastJoinedRoom().getVariable("maxPoints").getIntValue();

        int maxVotesOnTurn = (user.getLastJoinedRoom().getPlayersList().size() - 2) * (user.getLastJoinedRoom().getPlayersList().size() - 1);

        RoomVariable maxPoints = new SFSRoomVariable("maxPoints", (oldMaxPoints + maxVotesOnTurn));
        maxPoints.setGlobal(true);

        getApi().setRoomVariables(null, user.getLastJoinedRoom(), Arrays.asList(maxPoints));
    }

    private void resetUserVariables(User user) {
        UserVariable receivedAllStories = new SFSUserVariable("receivedAllStories", false );
        UserVariable evaluatedAllStories = new SFSUserVariable("evaluatedAllStories", false );
        UserVariable idListOfEvaluatedUsers = new SFSUserVariable( "idListOfEvaluatedUsers", new SFSArray());
        idListOfEvaluatedUsers.setHidden(true);

        for (User iterationUser : user.getLastJoinedRoom().getPlayersList()) {
            getApi().setUserVariables(iterationUser, Arrays.asList(receivedAllStories, evaluatedAllStories, idListOfEvaluatedUsers ));
        }
    }

}
