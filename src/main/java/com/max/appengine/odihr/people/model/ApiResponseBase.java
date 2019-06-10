package com.max.appengine.odihr.people.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.api.services.admin.directory.model.User;

public class ApiResponseBase {
  private boolean ok;
  private String msg;
  private Date date;
  private Locale locale;
  private List<User> contacts;

  public ApiResponseBase() {
    super();
  }

  public ApiResponseBase(Locale locale) {
    super();
    this.date = new Date();
    this.ok = true;
    this.locale = locale;
  }

  public ApiResponseBase(List<User> feed, Locale locale) {
    super();
    this.date = new Date();
    this.ok = true;
    this.locale = locale;
    this.contacts = feed;
  }

  public boolean isOk() {
    return ok;
  }

  public void setOk() {
    this.ok = true;
  }

  public void setOk(boolean ok) {
    this.ok = ok;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public List<User> getFeed() {
    return contacts;
  }

  public void setFeed(List<User> contacts) {
    this.contacts = contacts;
  }


}
