package com.max.appengine.odihr.people.controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.api.services.people.v1.model.Person;
import com.google.gdata.util.ServiceException;
import com.max.appengine.odihr.people.model.ApiResponseBase;
import com.max.appengine.odihr.people.service.GooglePeopleService;

@RestController
public class GoogleContactsRest {

  private final GooglePeopleService googlePeopleService;

  public GoogleContactsRest(GooglePeopleService googlePeopleService) {
    this.googlePeopleService = googlePeopleService;
  }

  @RequestMapping("/")
  public ResponseEntity<ApiResponseBase> index() {
    return new ResponseEntity<ApiResponseBase>(new ApiResponseBase(Locale.ENGLISH), HttpStatus.OK);
  }


  @RequestMapping("/contacts")
  public ResponseEntity<ApiResponseBase> contacts() throws IOException, ServiceException {
    List<Person> feed = this.googlePeopleService.getAllContacts();

    return new ResponseEntity<ApiResponseBase>(new ApiResponseBase(feed, Locale.ENGLISH),
        HttpStatus.OK);
  }

}
