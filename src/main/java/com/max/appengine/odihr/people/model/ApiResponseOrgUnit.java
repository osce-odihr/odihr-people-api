package com.max.appengine.odihr.people.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.api.services.admin.directory.model.OrgUnit;

public class ApiResponseOrgUnit extends ApiResponseBase {
  private List<OrgUnit> units;

  public ApiResponseOrgUnit() {
    super();
  }

  public ApiResponseOrgUnit(List<OrgUnit> units, Locale locale) {
    super();
    this.setDate(new Date());
    this.setOk();
    this.setLocale(locale);
    this.setUnits(units);
  }

  public List<OrgUnit> getUnits() {
    return units;
  }

  public void setUnits(List<OrgUnit> units) {
    this.units = units;
  }


}
