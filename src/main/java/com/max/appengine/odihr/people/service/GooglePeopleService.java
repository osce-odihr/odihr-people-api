package com.max.appengine.odihr.people.service;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.OrgUnit;
import com.google.api.services.admin.directory.model.OrgUnits;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.Users;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;

@Service
public class GooglePeopleService {
  protected static final Logger log = LoggerFactory.getLogger(GooglePeopleService.class);

  private static final String APPLICATION_NAME = "OSCE ODIHR Google People Service";

  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static final List<String> SCOPES_CONTACTS = Arrays.asList(PeopleServiceScopes.CONTACTS);

  private static final List<String> SCOPES_DIRECTORY =
      Arrays.asList(DirectoryScopes.ADMIN_DIRECTORY_USER, DirectoryScopes.ADMIN_DIRECTORY_ORGUNIT);

  private static final String CREDENTIALS_FILE_PATH = "/osce-odihr.p12";

  private static final String DOMAIN = "odihr.org.ua";

  private static final String CUSTOMER_ID = "C0410nfbq";
  
  private static final String SERVICE_ACCOUNT_ID = "osce-odihr@appspot.gserviceaccount.com";
  
  private final NetHttpTransport HTTP_TRANSPORT;

  private final Directory directoryService;

  private final File KEY_FILE;
  
  private PeopleService service;

  public GooglePeopleService() throws Exception {
    HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    KEY_FILE = new File(GooglePeopleService.class.getResource(CREDENTIALS_FILE_PATH).toURI());

    GoogleCredential credential = new GoogleCredential.Builder()
        .setTransport(HTTP_TRANSPORT)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountId(SERVICE_ACCOUNT_ID)
        .setServiceAccountUser("it@odihr.org.ua")
        .setServiceAccountScopes(SCOPES_DIRECTORY)
        .setServiceAccountPrivateKeyFromP12File(KEY_FILE)
        .build();

    this.directoryService = new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME).build();
  }

  public Credential authorize(String userEmail) throws IOException, GeneralSecurityException {
    // Load client secrets.

    GoogleCredential credential = new GoogleCredential.Builder()
        .setTransport(HTTP_TRANSPORT)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountId(SERVICE_ACCOUNT_ID)
        .setServiceAccountUser(userEmail)
        .setServiceAccountScopes(SCOPES_CONTACTS)
        .setServiceAccountPrivateKeyFromP12File(KEY_FILE)
        .build();

    return credential;
  }

  public List<Person> getAllContacts(String userEmail) throws IOException, GeneralSecurityException {
    this.service = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize(userEmail))
        .setApplicationName(APPLICATION_NAME)
        .build();

    ListConnectionsResponse response = this.service.people().connections().list("people/me")
        .setPersonFields(
            "addresses,ageRanges,biographies,birthdays,braggingRights,coverPhotos,emailAddresses,events,genders,imClients,interests,locales,memberships,metadata,names,nicknames,occupations,organizations,phoneNumbers,photos,relations,relationshipInterests,relationshipStatuses,residences,sipAddresses,skills,taglines,urls,userDefined")
        .execute();
    return response.getConnections();
  }

  public List<OrgUnit> getAllUnits() throws IOException {
    OrgUnits result = directoryService.orgunits()
        .list(CUSTOMER_ID)
        .execute();

    List<OrgUnit> units = result.getOrganizationUnits();

    return units;
  }

  public List<User> getAllUsers() throws IOException {
    Users result = directoryService.users().list()
        .setDomain(DOMAIN)
        .setMaxResults(10)
        .setOrderBy("email")
        .execute();
    List<User> users = result.getUsers();

    return users;
  }
}
