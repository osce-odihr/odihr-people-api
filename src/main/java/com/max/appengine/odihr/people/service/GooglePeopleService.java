package com.max.appengine.odihr.people.service;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.ListContactGroupsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.max.appengine.odihr.people.model.ApiResponseContacts;

@Service
public class GooglePeopleService {
  protected static final Logger log = LoggerFactory.getLogger(GooglePeopleService.class);

  private static final String APPLICATION_NAME = "OSCE ODIHR Google People Service";

  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static final List<String> SCOPES_CONTACTS = Arrays.asList(PeopleServiceScopes.CONTACTS);

  private static final List<String> SCOPES_DIRECTORY =
      Arrays.asList(DirectoryScopes.ADMIN_DIRECTORY_USER, DirectoryScopes.ADMIN_DIRECTORY_ORGUNIT);

  private static final List<String> SCOPES_SHEETS = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);
  
  private static final String CREDENTIALS_FILE_PATH = "/osce-odihr.p12";

  private static final String DOMAIN = "odihr.org.ua";

  private static final String CUSTOMER_ID = "C0410nfbq";
  
  private static final String SERVICE_ACCOUNT_ID = "osce-odihr@appspot.gserviceaccount.com";
  
  private static final String PERSON_FIELDS = "addresses,ageRanges,biographies,birthdays,braggingRights,coverPhotos,emailAddresses,events,genders,imClients,interests,locales,memberships,metadata,names,nicknames,occupations,organizations,phoneNumbers,photos,relations,relationshipInterests,relationshipStatuses,residences,sipAddresses,skills,taglines,urls,userDefined";
  
  private final NetHttpTransport HTTP_TRANSPORT;

  private final Directory directoryService;
  
  private final Sheets sheetsService;

  private final File KEY_FILE;

  public GooglePeopleService() throws Exception {
    HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    KEY_FILE = new File(GooglePeopleService.class.getResource(CREDENTIALS_FILE_PATH).toURI());

    GoogleCredential credentialDirectory = new GoogleCredential.Builder()
        .setTransport(HTTP_TRANSPORT)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountId(SERVICE_ACCOUNT_ID)
        .setServiceAccountUser("it@odihr.org.ua")
        .setServiceAccountScopes(SCOPES_DIRECTORY)
        .setServiceAccountPrivateKeyFromP12File(KEY_FILE)
        .build();

    this.directoryService = new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentialDirectory)
        .setApplicationName(APPLICATION_NAME).build();
    
    GoogleCredential credentialSheets = new GoogleCredential.Builder()
        .setTransport(HTTP_TRANSPORT)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountId(SERVICE_ACCOUNT_ID)
        .setServiceAccountUser("it@odihr.org.ua")
        .setServiceAccountScopes(SCOPES_SHEETS)
        .setServiceAccountPrivateKeyFromP12File(KEY_FILE)
        .build();
    this.sheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentialSheets)
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
  
  public ApiResponseContacts createResponseContacts(String userEmail, Locale locale)
      throws IOException, GeneralSecurityException {
    PeopleService service = buildPeopleService(userEmail);

    ListConnectionsResponse response =
        service.people().connections().list("people/me").setPersonFields(PERSON_FIELDS).execute();

    ListContactGroupsResponse responseGroups = service.contactGroups().list().execute();

    return new ApiResponseContacts(response.getConnections(), responseGroups.getContactGroups(),
        locale);
  }
  
  public boolean updateContactsFromSheet(String userEmail, String sheet)
      throws IOException, GeneralSecurityException {
    List<Person> contacts = getContactsFromSheet(sheet);

    if (contacts != null) {
      PeopleService peopleService = buildPeopleService(userEmail);
  
      for (Person person : contacts) {
         this.addContact(peopleService, person);
      }
      
      return true;
    } else {
      return false;
    }
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
        .setMaxResults(500)
        .setOrderBy("email")
        .execute();
    List<User> users = result.getUsers();

    return users;
  }
  
  private void addContact(PeopleService peopleService, Person person) throws IOException, GeneralSecurityException {
    Person createdContact = peopleService.people().createContact(person).execute();
    System.out.println(createdContact);
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  private List<Person> getContactsFromSheet(String spreadsheetId) throws IOException {
    final String range = "A:I";

    ValueRange response =
        this.sheetsService.spreadsheets().values().get(spreadsheetId, range).execute();

    List<List<Object>> values = response.getValues();

    List<Person> contactsList = new ArrayList<Person>();

    if (values != null && !values.isEmpty()) {
      int index = 0;
      for (List row : values) {
        System.out.println(row);
        
        if (index++ == 0) {
          // check headers
          if (!row.get(0).equals("Name"))
            return null;
          if (!row.get(1).equals("Given Name"))
            return null;
          if (!row.get(2).equals("Additional Name"))
            return null;
          if (!row.get(3).equals("Family Name"))
            return null;
          if (!row.get(4).equals("Group Membership"))
            return null;
          if (!row.get(5).equals("E-mail 1 - Type"))
            return null;
          if (!row.get(6).equals("E-mail 1 - Value"))
            return null;
          if (!row.get(7).equals("Phone 1 - Type"))
            return null;
          if (!row.get(8).equals("Phone 1 - Value"))
            return null;
        } else {
          Person personNew = new Person();

          List names = new ArrayList<>();
          names.add(new Name().setGivenName(row.get(1).toString()).setFamilyName(row.get(3).toString()));
          personNew.setNames(names);
          
          List emailAddresses = new ArrayList<>();
          emailAddresses.add(new EmailAddress().setValue(row.get(6).toString()).setType("home"));
          personNew.setEmailAddresses(emailAddresses);
          
          List phones = new ArrayList<>();
          phones.add(new PhoneNumber().setValue(row.get(8).toString()).setType("mobile"));
          personNew.setPhoneNumbers(phones);
          
          contactsList.add(personNew);
        }
      }
    }

    return contactsList;
  }
  
  private PeopleService buildPeopleService(String userEmail) throws IOException, GeneralSecurityException {
    return new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize(userEmail))
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
}
