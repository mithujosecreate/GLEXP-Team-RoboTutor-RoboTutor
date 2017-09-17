package cmu.xprize.comp_logging;

import java.lang.reflect.Field;

/**
 * Created by kevindeland on 9/13/17.
 */

public class PerformanceLogItem {

    private long timestamp;
    private String userId;
    private String sessionId;
    private String gameId;
    private String language;
    private String tutorName;
    private String problemName;
    private int problemNumber;
    private String levelName;

    private int totalSubsteps;
    private int substepNumber;
    private int substepProblem;
    private int attemptNumber;
    private String taskName;

    private String expectedAnswer;
    private String userResponse;
    private String correctness;

    private String distractors;
    private String scaffolding;
    private String promptType;
    private String feedbackType;

    // iterative way to print fields in the desired order
    private static final String[] orderedFieldsToPrint =
            {"userId", "sessionId", "gameId", "tutorName", "levelName", "taskName", "problemNumber", "problemName", "substepNumber",
                    "expectedAnswer", "userResponse", "correctness", "timestamp"};



    public PerformanceLogItem() {

    }

    public String toString() {
        StringBuilder msg = new StringBuilder();

        String SEP = ",";

        for (String fieldName : orderedFieldsToPrint) {
            try {
                Field field = this.getClass().getDeclaredField(fieldName);
                msg.append(fieldName);
                msg.append(":");
                msg.append(field.get(this));
                msg.append(",");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        String result = msg.toString();
        return result.substring(0, result.length() - 1); // don't forget to slice off that last comma
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public int getProblemNumber() {
        return problemNumber;
    }

    public void setProblemNumber(int problemNumber) {
        this.problemNumber = problemNumber;
    }

    public int getTotalSubsteps() {
        return totalSubsteps;
    }

    public void setTotalSubsteps(int totalSubsteps) {
        this.totalSubsteps = totalSubsteps;
    }

    public int getSubstepNumber() {
        return substepNumber;
    }

    public void setSubstepNumber(int substepNumber) {
        this.substepNumber = substepNumber;
    }

    public int getSubstepProblem() {
        return substepProblem;
    }

    public void setSubstepProblem(int substepProblem) {
        this.substepProblem = substepProblem;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }

    public String getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(String userResponse) {
        this.userResponse = userResponse;
    }

    public String getCorrectness() {
        return correctness;
    }

    public void setCorrectness(String correctness) {
        this.correctness = correctness;
    }

    public String getScaffolding() {
        return scaffolding;
    }

    public void setScaffolding(String scaffolding) {
        this.scaffolding = scaffolding;
    }

    public String getPromptType() {
        return promptType;
    }

    public void setPromptType(String promptType) {
        this.promptType = promptType;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDistractors() {
        return distractors;
    }

    public void setDistractors(String distractors) {
        this.distractors = distractors;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}