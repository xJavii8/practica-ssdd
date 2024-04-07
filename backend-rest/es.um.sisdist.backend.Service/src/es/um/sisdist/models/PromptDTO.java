package es.um.sisdist.models;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PromptDTO {
    private String question;
    private String answer;

    public PromptDTO() {
    }

    public PromptDTO(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

}

