package com.max.appengine.odihr.people.controller;

import java.io.IOException;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.util.ServiceException;
import com.max.appengine.odihr.people.model.ApiResponseBase;
import com.max.appengine.odihr.people.service.GoogleContactsService;

@RestController
public class GoogleContactsRest {

  private final GoogleContactsService googleContactsService;

  public GoogleContactsRest(GoogleContactsService googleContactsService) {
    this.googleContactsService = googleContactsService;
  }

  @RequestMapping("/")
  public ResponseEntity<ApiResponseBase> index()  {
    return new ResponseEntity<ApiResponseBase>(new ApiResponseBase(Locale.ENGLISH), HttpStatus.OK);
  }


  @RequestMapping("/contacts")
  public ResponseEntity<ApiResponseBase> contacts() throws IOException, ServiceException {
    ContactFeed feed = this.googleContactsService.getAllContacts();

    return new ResponseEntity<ApiResponseBase>(new ApiResponseBase(feed, Locale.ENGLISH),
        HttpStatus.OK);
  }

}
