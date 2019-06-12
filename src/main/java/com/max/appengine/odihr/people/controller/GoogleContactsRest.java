package com.max.appengine.odihr.people.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.api.services.admin.directory.model.OrgUnit;
import com.google.api.services.admin.directory.model.User;
import com.max.appengine.odihr.people.model.ApiResponseBase;
import com.max.appengine.odihr.people.model.ApiResponseContacts;
import com.max.appengine.odihr.people.model.ApiResponseUsers;
import com.max.appengine.odihr.people.service.GooglePeopleService;

@CrossOrigin
@RestController
public class GoogleContactsRest {

  private final GooglePeopleService googlePeopleService;

  public GoogleContactsRest(GooglePeopleService googlePeopleService) {
    this.googlePeopleService = googlePeopleService;
  }

  @RequestMapping("/")
  public ResponseEntity<ApiResponseBase> index()
      throws IOException, GeneralSecurityException, URISyntaxException {
    return new ResponseEntity<ApiResponseBase>(new ApiResponseBase(Locale.ENGLISH), HttpStatus.OK);
  }

  @RequestMapping("/users")
  public ResponseEntity<ApiResponseBase> users() throws IOException {
    List<User> users = this.googlePeopleService.getAllUsers();
    List<OrgUnit> units = this.googlePeopleService.getAllUnits();

    return new ResponseEntity<ApiResponseBase>(new ApiResponseUsers(units, users, Locale.ENGLISH),
        HttpStatus.OK);
  }

  @RequestMapping("/contacts")
  public ResponseEntity<ApiResponseBase> contacts(@RequestParam String userEmail)
      throws IOException, GeneralSecurityException {

    ApiResponseContacts responseContacts =
        this.googlePeopleService.createResponseContacts(userEmail, Locale.ENGLISH);

    return new ResponseEntity<ApiResponseBase>(responseContacts, HttpStatus.OK);
  }
  
  @RequestMapping("/contactsUpload")
  public ResponseEntity<ApiResponseBase> contactsUpload(@RequestParam String userEmail,
      @RequestParam String contactsFile) throws IOException, GeneralSecurityException {

    boolean result = this.googlePeopleService.updateContactsFromSheet(userEmail, contactsFile);

    ApiResponseBase responseContacts;
    if (result) {
      responseContacts = this.googlePeopleService.createResponseContacts(userEmail, Locale.ENGLISH);
    } else {
      responseContacts = new ApiResponseBase(false, "Contacts Sheet not valid", Locale.ENGLISH);
    }

    return new ResponseEntity<ApiResponseBase>(responseContacts, HttpStatus.OK);
  }
  
  @RequestMapping("/contactDelete")
  public ResponseEntity<ApiResponseBase> contactDelete(@RequestParam String userEmail,
      @RequestParam String resourceName) throws IOException, GeneralSecurityException {

    this.googlePeopleService.deletePersonFromContacts(userEmail, resourceName);

    ApiResponseContacts responseContacts =
        this.googlePeopleService.createResponseContacts(userEmail, Locale.ENGLISH);

    return new ResponseEntity<ApiResponseBase>(responseContacts, HttpStatus.OK);
  }
  
  @RequestMapping("/contactDeleteAll")
  public ResponseEntity<ApiResponseBase> contactDeleteAll(@RequestParam String userEmail) 
      throws IOException, GeneralSecurityException {

    this.googlePeopleService.deleteAllFromContacts(userEmail);

    ApiResponseContacts responseContacts =
        this.googlePeopleService.createResponseContacts(userEmail, Locale.ENGLISH);

    return new ResponseEntity<ApiResponseBase>(responseContacts, HttpStatus.OK);
  }

}
