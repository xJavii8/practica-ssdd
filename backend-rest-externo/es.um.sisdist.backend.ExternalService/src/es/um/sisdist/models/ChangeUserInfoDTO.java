package es.um.sisdist.models;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ChangeUserInfoDTO
{
    private String actualEmail;
    private String newMail;
    private String name;
    private String password;

    /**
     * @return the email
     */
    public String getActualEmail()
    {
        return actualEmail;
    }

    /**
     * @param email the email to set
     */
    public void setActualEmail(String email)
    {
        this.actualEmail = email;
    }

    /**
     * @return the email
     */
    public String getNewMail()
    {
        return newMail;
    }

    /**
     * @param email the email to set
     */
    public void setNewMail(String email)
    {
        this.newMail = email;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public ChangeUserInfoDTO(String actualEmail, String newMail, String name, String password)
    {
        super();
        this.actualEmail = actualEmail;
        this.newMail = newMail;
        this.password = password;
        this.name = name;
    }

    public ChangeUserInfoDTO()
    {
    }
}
