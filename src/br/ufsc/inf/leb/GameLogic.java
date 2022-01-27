//package br.ufsc.inf.leb;
//
//import com.smartfoxserver.v2.entities.Room;
//import com.smartfoxserver.v2.entities.User;
//import com.smartfoxserver.v2.entities.data.SFSArray;
//import com.smartfoxserver.v2.entities.variables.RoomVariable;
//import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
//import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
//import com.smartfoxserver.v2.entities.variables.UserVariable;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//public class GameLogic {
//    //Attributes:
//    private MikeHelpMeRoomExtension mikeHelpMeRoomExtension;
//    private Room room;
//    private ScheduledFuture timerRunnable;
//    private ScheduledFuture checkUserStoryRunnable;
//    private ScheduledFuture checkUsersReceivedAllStoriesRunnable;
//    private int timerCount = 0;
//    int checkUserStoryTimer = 0;
//    int receivedStoryTimer = 0;
//
//    //Constructor:
//    public GameLogic(MikeHelpMeRoomExtension roomExtension, Room room) {
//        this.mikeHelpMeRoomExtension = roomExtension;
//        this.room = room;
//    }
//
//    //Methods:
//    public void start() {
//        mikeHelpMeRoomExtension.trace("Game on " + this.room.getName() + "#" + this.room.getId() + " has started.");
//
//        timerRunnable = mikeHelpMeRoomExtension.getApi().getSystemScheduler().scheduleAtFixedRate(() -> {
//            timerCount++;
//            checkGameState();
//        }, 0, 1, TimeUnit.SECONDS);
//    }
//
//    private void checkGameState() {
//        if (room.getPlayersList().size() >= ServerSetupVariables.MIN_PLAYERS_TO_START.getIntValue()) {
//            String turn = (String) (room.getVariable("turn").getValue());
//            mikeHelpMeRoomExtension.trace(timerCount);
//
//            switch (turn.substring(0, 2)) {
//                case "RP":
//                    if (timerCount == ServerSetupVariables.TIMER_COUNT_READING_PHASE.getIntValue()) {
//                        String userStory = turn.substring(2);
//
//                        updateTurnRoomVariable("PP" + userStory);
//
//                        timerCount = 0;
//                    }
//                    break;
//
//                case "PP":
//                    if (timerCount == ServerSetupVariables.TIMER_COUNT_PROPOSING_PHASE.getIntValue()) {
//                        String userStory = turn.substring(2);
//
//                        checkIfAllUsersSentTheirStories(userStory);
//
//                        checkIfAllUsersReceivedAllStories();
//
//                        updateTurnRoomVariable("EP" + userStory);
//
//                        timerCount = 0;
//
//                    }
//                    break;
//
//                case "EP":
//                    if (timerCount == ServerSetupVariables.TIMER_COUNT_EVALUATING_PHASE.getIntValue()) {
//                        String userStory = turn.substring(2);
//
//                        //checkIfAllUsersEvaluatedAllStories();
//
//                        updateTurnRoomVariable("SP" + userStory);
//
//                        timerCount = 0;
//
//                    }
//                    break;
//
//                case "SP":
//                    if (timerCount == ServerSetupVariables.TIMER_COUNT_SCORE_PHASE.getIntValue()) {
//                        int userStory = Integer.parseInt(turn.substring(2));
//
//                        if (!(userStory >= ServerSetupVariables.NUMBER_OF_USER_STORIES.getIntValue())) {
//                            updateTurnRoomVariable("RP" + String.valueOf(userStory + 1));
//                            updateMaxPointsVariable();
//
//                            timerCount = 0;
//
//                            //Clear control variable mikeHelpMeRoomExtension:
//                            mikeHelpMeRoomExtension.setUsersThatSentStory(new ArrayList<>()); //Probably not needed!!!!
//                            resetUserVariables();
//                        } else {
//                            RoomVariable newTurn = new SFSRoomVariable("turn", "ENDEP");
//                            newTurn.setGlobal(true);
//                            newTurn.setPrivate(true);
//                            updateMaxPointsVariable();
//
//                            traceRoomTurnUpdate(room, (String) (newTurn.getValue()));
//                            mikeHelpMeRoomExtension.getApi().setRoomVariables(null, room, Arrays.asList(newTurn));
//                            timerCount = 0;
//                            timerRunnable.cancel(true);
//                            //Salvar Historias
//                        }
//                    }
//                    break;
//            }
//
//        } else {
//            RoomVariable newTurn = new SFSRoomVariable("turn", "ENDEP");
//            newTurn.setGlobal(true);
//            newTurn.setPrivate(true);
//
//            traceRoomTurnUpdate(room, (String) (newTurn.getValue()));
//            mikeHelpMeRoomExtension.getApi().setRoomVariables(null, room, Arrays.asList(newTurn));
//            timerCount = 0;
//            timerRunnable.cancel(true);
//            //Salvar Historias
//        }
//    }
//
//    private void updateMaxPointsVariable() {
//        int oldMaxPoints = room.getVariable("maxPoints").getIntValue();
//
//        int maxVotesOnTurn = (room.getPlayersList().size() - 2) * (room.getPlayersList().size() - 1);
//
//        RoomVariable maxPoints = new SFSRoomVariable("maxPoints", (oldMaxPoints + maxVotesOnTurn));
//        maxPoints.setGlobal(true);
//
//        mikeHelpMeRoomExtension.getApi().setRoomVariables(null, room, Arrays.asList(maxPoints));
//    }
//
//    private void resetUserVariables() {
//        UserVariable receivedAllStories = new SFSUserVariable("receivedAllStories", false );
//        UserVariable evaluatedAllStories = new SFSUserVariable("evaluatedAllStories", false );
//        UserVariable idListOfEvaluatedUsers = new SFSUserVariable( "idListOfEvaluatedUsers", new SFSArray());
//        idListOfEvaluatedUsers.setHidden(true);
//
//        for (User iterationUser : room.getPlayersList()) {
//            mikeHelpMeRoomExtension.getApi().setUserVariables(iterationUser, Arrays.asList(receivedAllStories, evaluatedAllStories, idListOfEvaluatedUsers ));
//        }
//    }
//
//    private void checkIfAllUsersSentTheirStories(String userStory) {
//        if (mikeHelpMeRoomExtension.getUsersThatSentStory().size() != room.getPlayersList().size()) {
//            AtomicBoolean endTimeoutTimer = new AtomicBoolean(false);
//
//            checkUserStoryRunnable = mikeHelpMeRoomExtension.getApi().getSystemScheduler().scheduleAtFixedRate(() -> {
//                if (checkUserStoryTimer >= ServerSetupVariables.WAITING_FOR_SOTRY_TIMETOUT.getIntValue()) {
//                    for (User user : room.getPlayersList()) {
//                        if (!user.containsVariable("H" + userStory)) {
//                            mikeHelpMeRoomExtension.getApi().leaveRoom(user, user.getLastJoinedRoom());
//                            mikeHelpMeRoomExtension.getApi().disconnectUser(user);
//                            mikeHelpMeRoomExtension.trace("Room " + room.getName() + "#" + room.getId() + ": "
//                                    + "Disconnected user " + user.getName() + "#" + user.getId() + " due to inactivity.");
//                        }
//                    }
//                    checkUserStoryTimer = 0;
//                    endTimeoutTimer.set(true);
//                    checkUserStoryRunnable.cancel(true);
//                } else {
//                    boolean allUsersSentTheirStories = true;
//
//                    for (User user : room.getPlayersList()) {
//                        if (!user.containsVariable("H" + userStory)) {
//                            allUsersSentTheirStories = false;
//                            break;
//                        }
//                    }
//                    if (allUsersSentTheirStories) {
//                        checkUserStoryTimer = 0;
//                        endTimeoutTimer.set(true);
//                        checkUserStoryRunnable.cancel(true);
//                    } else {
//                        checkUserStoryTimer++;
//                        mikeHelpMeRoomExtension.trace("Waiting for players to send their user story... Timer: " + checkUserStoryTimer);
//                    }
//                }
//            }, 0, 1, TimeUnit.SECONDS);
//
//            while (!endTimeoutTimer.get()) ;
//
//        }
//    }
//
//    private void checkIfAllUsersReceivedAllStories() {
//        AtomicBoolean endTimeoutTimer = new AtomicBoolean(false);
//
//        checkUsersReceivedAllStoriesRunnable = mikeHelpMeRoomExtension.getApi().getSystemScheduler().scheduleAtFixedRate(() -> {
//            if (receivedStoryTimer >= ServerSetupVariables.WAITING_FOR_RECIEVE_TIMETOUT.getIntValue()) {
//                for (User user : room.getPlayersList()) {
//                    if (!user.getVariable("receivedAllStories").getBoolValue()) {
//                        mikeHelpMeRoomExtension.getApi().leaveRoom(user, user.getLastJoinedRoom());
//                        mikeHelpMeRoomExtension.getApi().disconnectUser(user);
//                        mikeHelpMeRoomExtension.trace("Room " + room.getName() + "#" + room.getId() + ": "
//                                + "Disconnected user " + user.getName() + "#" + user.getId() + " due to inactivity.");
//                    }
//                }
//                receivedStoryTimer = 0;
//                endTimeoutTimer.set(true);
//                checkUsersReceivedAllStoriesRunnable.cancel(true);
//            } else {
//                boolean allUsersHaveAllStories = true;
//
//                for (User user : room.getPlayersList()) {
//                    if (!user.getVariable("receivedAllStories").getBoolValue()) {
//                        allUsersHaveAllStories = false;
//                        break;
//                    }
//                }
//                if (allUsersHaveAllStories) {
//                    receivedStoryTimer = 0;
//                    endTimeoutTimer.set(true);
//                    checkUsersReceivedAllStoriesRunnable.cancel(true);
//                } else {
//                    receivedStoryTimer++;
//                    mikeHelpMeRoomExtension.trace("Waiting for players to send their user story... Timer: " + receivedStoryTimer);
//                }
//            }
//        }, 0, 1, TimeUnit.SECONDS);
//
//        while (!endTimeoutTimer.get()) ;
//    }
//
//    private void traceRoomTurnUpdate(Room room, String newTurn) {
//        mikeHelpMeRoomExtension.trace("Room " + room.getName() + "#" + room.getId() + " turn changed: " + newTurn);
//    }
//
//    public ScheduledFuture getTimerRunnable() {
//        return timerRunnable;
//    }
//
//    public ScheduledFuture getCheckUserStoryRunnable() {
//        return checkUserStoryRunnable;
//    }
//
//    public ScheduledFuture getCheckUsersReceivedAllStoriesRunnable() {
//        return checkUsersReceivedAllStoriesRunnable;
//    }
//
//    private void updateTurnRoomVariable(String newTurnValue) {
//        RoomVariable newTurn = new SFSRoomVariable("turn", newTurnValue);
//        newTurn.setGlobal(true);
//        newTurn.setPrivate(true);
//
//        traceRoomTurnUpdate(room, (String) (newTurn.getValue()));
//        mikeHelpMeRoomExtension.getApi().setRoomVariables(null, room, Arrays.asList(newTurn));
//    }
//}
