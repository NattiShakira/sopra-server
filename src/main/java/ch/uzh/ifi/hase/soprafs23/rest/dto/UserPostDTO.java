package ch.uzh.ifi.hase.soprafs23.rest.dto;

import javax.persistence.Column;
import java.util.Date;

public class UserPostDTO {

//  private String name;

  private String username;

//  public String getName() {
//    return name;
//  }
//
//  public void setName(String name) {
//    this.name = name;
//  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

    private String password;

  private Date creation_date;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public Date getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

}
