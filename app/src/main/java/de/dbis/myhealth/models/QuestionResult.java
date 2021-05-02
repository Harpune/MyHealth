package de.dbis.myhealth.models;

public class QuestionResult {

    private Integer value;
    private Long duration;
    private int questionNumber;
    private boolean removed;

    public QuestionResult() {

    }

    public QuestionResult(Integer value, Long duration, int questionNumber, boolean removed) {
        this.value = value;
        this.duration = duration;
        this.questionNumber = questionNumber;
        this.removed = removed;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    @Override
    public String toString() {
        return "QuestionResult{" +
                "value=" + value +
                ", duration=" + duration +
                ", questionNumber=" + questionNumber +
                ", removed=" + removed +
                '}';
    }
}
