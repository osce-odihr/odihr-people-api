package com.max.appengine.odihr.people.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.api.services.people.v1.model.Person;

public class ApiResponseContacts extends ApiResponseBase {
  private List<Person> contacts;

  public ApiResponseContacts(List<Person> contacts, Locale locale) {
    super();
    this.setDate(new Date());
    this.setOk();
    this.setLocale(locale);
    this.setContacts(contacts);
  }

  public List<Person> getContacts() {
    return contacts;
  }

  public void setContacts(List<Person> contacts) {
    this.contacts = contacts;
  }
}
