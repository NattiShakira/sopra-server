package ch.uzh.ifi.hase.soprafs23.rest.dto;

import lombok.SneakyThrows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Column;

public class UserPutDTO {

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String birthday;

    public Date getBirthday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        try {
            return dateFormat.parse(birthday);
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}