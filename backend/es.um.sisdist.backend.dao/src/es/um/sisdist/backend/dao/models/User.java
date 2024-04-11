/**
 *
 */
package es.um.sisdist.backend.dao.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import es.um.sisdist.backend.dao.models.utils.UserUtils;

public class User {
    private String id;
    private String email;
    private String password_hash;
    private String name;

    private String token;

    private int promptCalls;
    private int createdConvs;

    private List<Conversation> conversations;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final String uid) {
        this.id = uid;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * @return the password_hash
     */
    public String getPassword_hash() {
        return password_hash;
    }

    /**
     * @param password_hash the password_hash to set
     */
    public void setPassword_hash(final String password_hash) {
        this.password_hash = password_hash;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the TOKEN
     */
    public String getToken() {
        return token;
    }

    /**
     * @param tOKEN the tOKEN to set
     */
    public void setToken(final String TOKEN) {
        this.token = TOKEN;
    }

    /**
     * @return the promptCalls
     */
    public int getPromptCalls() {
        return promptCalls;
    }

    /**
     * @param promptCalls the promptCalls to set
     */
    public void setPromptCalls(int promptCalls) {
        this.promptCalls = promptCalls;
    }

    /**
     * 
     * @return the conversations
     */
    public List<Conversation> getConversations() {
        return conversations;
    }

    /**
     * 
     * @param conversations
     */
    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public void updatePromptCalls() {
        this.promptCalls = (promptCalls + 1);
    }

    public int getCreatedConvs() {
        return createdConvs;
    }

    public void setCreatedConvs(int createdConvs) {
        this.createdConvs = createdConvs;
    }

    public User(String email, String password_hash, String name, String tOKEN, int promptCalls, int createdConvs) {
        this(email, email, password_hash, name, tOKEN, promptCalls, createdConvs);
        this.id = UserUtils.md5pass(email);
    }

    public User(String id, String email, String password_hash, String name, String tOKEN, int promptCalls, int createdConvs) {
        this.id = id;
        this.email = email;
        this.password_hash = password_hash;
        this.name = name;
        token = tOKEN;
        this.promptCalls = promptCalls;
        this.conversations = new ArrayList<Conversation>();
        this.createdConvs = createdConvs;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", email=" + email + ", password_hash=" + password_hash + ", name=" + name
                + ", TOKEN=" + token + ", promptCalls=" + promptCalls + ", createdConvs=" + createdConvs + "]";
    }

    public User() {
    }

    public Optional<Conversation> createConversation(String convName) {
        Conversation c = new Conversation(this.id, convName);
        this.conversations.add(c);
        this.createdConvs = (createdConvs + 1);
        return Optional.of(c);
    }

    public Optional<List<Conversation>> endConversation(String convID) {
        Optional<Conversation> conv = conversations.stream()
                .filter(conversation -> convID.equals(conversation.getID()))
                .findFirst();

        if (conv.isPresent()) {
            Conversation c = conv.get();
            c.setStatus(Conversation.FINISHED);
            int index = conversations.indexOf(c);
            conversations.set(index, c);
            return Optional.of(conversations);
        }

        return Optional.empty();
    }

    public boolean delConversation(String convID) {
        Optional<Conversation> conv = conversations.stream()
                .filter(conversation -> convID.equals(conversation.getID()))
                .findFirst();

        if (conv.isPresent()) {
            Conversation c = conv.get();
            conversations.remove(c);
            return true;
        }

        return false;
    }

    public List<Conversation> delAllConvs() {
        this.conversations = new ArrayList<Conversation>();
        return this.conversations;
    }

    public Optional<List<Conversation>> addDialogue(String convID, Dialogue dialogue) {
        Optional<Conversation> conv = conversations.stream()
                .filter(conversations -> convID.equals(conversations.getID()))
                .findFirst();

        if (conv.isPresent()) {
            Conversation c = conv.get();
            c.addDialogue(dialogue);
            c.setStatus(Conversation.BUSY);
            c.setNewTimestamp(this.id, dialogue.getTimestamp());
            int index = conversations.indexOf(c);
            conversations.set(index, c);
            return Optional.of(conversations);
        }
        return Optional.empty();
    }

    public Optional<List<Conversation>> addResponse(String convID, String dialogueID, String response) {
        Optional<Conversation> conv = conversations.stream()
                .filter(conversations -> convID.equals(conversations.getID()))
                .findFirst();

        if (conv.isPresent()) {
            Conversation c = conv.get();

            if (!c.addResponse(dialogueID, response)) {
                return Optional.empty();
            }
            c.setStatus(Conversation.READY);
            int index = conversations.indexOf(c);
            conversations.set(index, c);
            return Optional.of(conversations);
        }
        return Optional.empty();
    }

}