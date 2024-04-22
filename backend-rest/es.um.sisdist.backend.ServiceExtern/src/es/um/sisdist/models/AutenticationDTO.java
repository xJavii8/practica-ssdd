package es.um.sisdist.models;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AutenticationDTO {
   
    private String user;
    private String date;
    private String authToken;

    public AutenticationDTO() {
    }


    public AutenticationDTO(String user, String date, String authToken) {
        this.user = user;
        this.date = date;
        this.authToken = authToken;
    }

    /**
     * 
     * @return the user id
     */
    public String getUser() {
        return user;
    }
    /**
     * 
     * @param user the user id to set
     */
    public void setUser(String user) {
        this.user = user;
    }
    /**
     * 
     * @return the auth token
     */
    public String getAuthToken() {
        return authToken;
    }
    /**
     * 
     * @return the current date in ISO8601 format 
     */
    public String getDate() {
        return date;
    }
    /**
     * 
     * @param date set the date, use ISO8601 format please
     */
    public void setDate(String date) {
        this.date = date;
    }
    /**
     * 
     * @param authToken set the authToken of the petition
     */
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
   
}
