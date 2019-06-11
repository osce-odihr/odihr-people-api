package com.max.appengine.odihr.people.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.api.services.people.v1.model.ContactGroup;
import com.google.api.services.people.v1.model.Person;

public class ApiResponseContacts extends ApiResponseBase {
  private List<Person> contacts;

  private List<ContactGroup> contactGroups;
  
  public ApiResponseContacts(List<Person> contacts, List<ContactGroup> contactGroups, Locale locale) {
    super();
    this.setDate(new Date());
    this.setOk();
    this.setLocale(locale);
    this.setContacts(contacts);
    this.setContactGroups(contactGroups);
  }

  public List<Person> getContacts() {
    return contacts;
  }

  public void setContacts(List<Person> contacts) {
    this.contacts = contacts;
  }

  public List<ContactGroup> getContactGroups() {
    return contactGroups;
  }

  public void setContactGroups(List<ContactGroup> contactGroups) {
    this.contactGroups = contactGroups;
  }
  
}
