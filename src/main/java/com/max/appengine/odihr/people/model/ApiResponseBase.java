package com.max.appengine.odihr.people.model;

import java.util.Date;
import java.util.Locale;
import com.google.gdata.data.contacts.ContactFeed;

public class ApiResponseBase {
  private boolean ok;
  private String msg;
  private Date date;
  private Locale locale;
  private ContactFeed feed;

  public ApiResponseBase() {
    super();
  }
  
  public ApiResponseBase(Locale locale) {
    super();
    this.date = new Date();
    this.ok = true;
    this.locale = locale;
  }
  
  public ApiResponseBase(ContactFeed feed, Locale locale) {
    super();
    this.date = new Date();
    this.ok = true;
    this.locale = locale;
    this.feed = feed;
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

  public ContactFeed getFeed() {
    return feed;
  }

  public void setFeed(ContactFeed feed) {
    this.feed = feed;
  }


}
