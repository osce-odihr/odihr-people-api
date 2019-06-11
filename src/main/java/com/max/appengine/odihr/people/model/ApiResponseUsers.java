package com.max.appengine.odihr.people.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.api.services.admin.directory.model.OrgUnit;
import com.google.api.services.admin.directory.model.User;

public class ApiResponseUsers extends ApiResponseBase {
  private List<OrgUnit> units;
  private List<User> users;

  public ApiResponseUsers() {
    super();
  }

  public ApiResponseUsers(List<OrgUnit> units, List<User> users, Locale locale) {
    super();
    this.setDate(new Date());
    this.setOk();
    this.setLocale(locale);
    this.setUnits(units);
    this.setUsers(users);
  }

  public List<OrgUnit> getUnits() {
    return units;
  }

  public void setUnits(List<OrgUnit> units) {
    this.units = units;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

}
