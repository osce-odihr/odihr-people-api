//package com.max.appengine.odihr.people.service;
//
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import com.google.gdata.client.contacts.ContactsService;
//import com.google.gdata.data.contacts.ContactFeed;
//import com.google.gdata.util.ServiceException;
//
//@Service
//public class GoogleContactsService {
//  private final URL feedUrl;
//
//  private final ContactsService service;
//  
//  @Autowired
//  public GoogleContactsService() throws MalformedURLException {
//    this.service = new ContactsService("Google-Contacts-Service");
//    this.feedUrl = new URL("https://www.google.com/m8/feeds/");
//  }
//  
//  public ContactFeed getAllContacts() throws IOException, ServiceException {
//    String userName = "test@odihr.org.ua";
//    String password = "testtest2";
//    if (userName == null || password == null) {
//      return null;
//    }
//    service.setUserCredentials(userName, password);
//    
//    // Request the feed
//    URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
//    return service.getFeed(feedUrl, ContactFeed.class);
//  }
//
//}
