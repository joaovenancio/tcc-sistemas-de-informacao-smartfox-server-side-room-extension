package br.ufsc.inf.leb;

public enum ServerSetupVariables {
    //Enums:
    MIN_PLAYERS_TO_START("3"),
    MAX_PLAYERS_IN_ROOM("5"),
    TIMER_COUNT_READING_PHASE("20"),
    TIMER_COUNT_PROPOSING_PHASE("20"),
    TIMER_COUNT_EVALUATING_PHASE("20"),
    TIMER_COUNT_SCORE_PHASE("10"),
    WAITING_FOR_SOTRY_TIMETOUT("15"),
    WAITING_FOR_RECIEVE_TIMETOUT("15"),
    NUMBER_OF_USER_STORIES("2"),
    POINTS_TO_GIVE("1");

    //Atributes:
    private String value;

    //Constructor:
    ServerSetupVariables(String value) {
        this.value = value;
    }

    //Methods:
    private String getValue() {
        return value;
    }

    public int getIntValue() {
        return Integer.parseInt(value);
    }
}
