package com.max.appengine.odihr.people.model;

import java.util.Date;
import java.util.Locale;

public class ApiResponseBase {
  private boolean ok;
  private String msg;
  private Date date;
  private Locale locale;

  public ApiResponseBase() {
    super();
  }

  public ApiResponseBase(Locale locale) {
    super();
    this.date = new Date();
    this.ok = true;
    this.locale = locale;
  }

  public ApiResponseBase(boolean ok, String msg, Locale locale) {
    super();
    this.ok = ok;
    this.msg = msg;
    this.date = new Date();
    this.locale = locale;
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
}
