package com.max.appengine.odihr.people.service;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import com.google.api.services.people.v1.model.ContactGroup;
import com.google.api.services.people.v1.model.ContactGroupMembership;
import com.google.api.services.people.v1.model.CreateContactGroupRequest;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.ListContactGroupsResponse;
import com.google.api.services.people.v1.model.Membership;
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
  
  private static final String CONTACT_GROUP_MY = "My Contacts";
  
  private static final String CONTACT_GROUP_CORE_TEAM = "Core team";
  
  private static final String CONTACT_GROUP_NATIONAL_STAFF = "National Staff";
  
  private static final String CONTACT_GROUP_LTO = "LTOs";
  
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
    PeopleService peopleService = buildPeopleService(userEmail);
    
    Map<String, String> odihrContactsGroups = prepareOdihrContactsGroups(peopleService);
    
    List<Person> contacts = getContactsFromSheet(sheet, odihrContactsGroups);

    if (contacts != null) {
      for (Person person : contacts) {
         this.addContact(peopleService, person);
      }
      
      return true;
    } else {
      return false;
    }
  }
  
  public void deletePersonFromContacts(String userEmail, String resourceName)
      throws IOException, GeneralSecurityException {
    PeopleService peopleService = buildPeopleService(userEmail);

    peopleService.people().deleteContact(resourceName).execute();
  }
  
  public void deleteAllFromContacts(String userEmail)
      throws IOException, GeneralSecurityException {
    PeopleService peopleService = buildPeopleService(userEmail);

    Map<String, String> odihrContactsGroups = prepareOdihrContactsGroups(peopleService);
    
    ListConnectionsResponse response = peopleService.people().connections().list("people/me")
        .setPersonFields(PERSON_FIELDS).execute();
    
    for (Person person : response.getConnections()) {
      // check if in sys group
      boolean toDelete = false;

      for (Membership membership : person.getMemberships()) {
        if (membership.getContactGroupMembership().getContactGroupResourceName()
            .equals(odihrContactsGroups.get(CONTACT_GROUP_CORE_TEAM))) {
          toDelete = true;
        }
        
        if (membership.getContactGroupMembership().getContactGroupResourceName()
            .equals(odihrContactsGroups.get(CONTACT_GROUP_NATIONAL_STAFF))) {
          toDelete = true;
        }
        
        if (membership.getContactGroupMembership().getContactGroupResourceName()
            .equals(odihrContactsGroups.get(CONTACT_GROUP_LTO))) {
          toDelete = true;
        }
      }

      if (toDelete) {
        peopleService.people().deleteContact(person.getResourceName()).execute();
      }
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
  
  private Map<String, String> prepareOdihrContactsGroups(
      PeopleService peopleService) throws IOException {
    
    Map<String, String> result = loadOdihrContactsGroups(peopleService);
    
    if (!result.containsKey(CONTACT_GROUP_CORE_TEAM)
        || !result.containsKey(CONTACT_GROUP_NATIONAL_STAFF)
        || !result.containsKey(CONTACT_GROUP_LTO)) {
      if (!result.containsKey(CONTACT_GROUP_CORE_TEAM)) {
        CreateContactGroupRequest requestCreate = new CreateContactGroupRequest();
        requestCreate.setContactGroup(new ContactGroup().setName(CONTACT_GROUP_CORE_TEAM));

        peopleService.contactGroups().create(requestCreate).execute();
      }

      if (!result.containsKey(CONTACT_GROUP_NATIONAL_STAFF)) {
        CreateContactGroupRequest requestCreate = new CreateContactGroupRequest();
        requestCreate.setContactGroup(new ContactGroup().setName(CONTACT_GROUP_NATIONAL_STAFF));

        peopleService.contactGroups().create(requestCreate).execute();
      }
      
      if (!result.containsKey(CONTACT_GROUP_LTO)) {
        CreateContactGroupRequest requestCreate = new CreateContactGroupRequest();
        requestCreate.setContactGroup(new ContactGroup().setName(CONTACT_GROUP_LTO));

        peopleService.contactGroups().create(requestCreate).execute();
      }
      
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
      }
      
      result = loadOdihrContactsGroups(peopleService);
    }
    
    return result;
  }

  private Map<String, String> loadOdihrContactsGroups(PeopleService peopleService)
      throws IOException {
    
    ListContactGroupsResponse responseGroups = peopleService.contactGroups().list().execute();

    Map<String, String> result = new HashMap<String, String>();

    for (ContactGroup contactGroup : responseGroups.getContactGroups()) {
      if (contactGroup.getFormattedName().equals(CONTACT_GROUP_MY)) {
        result.put(CONTACT_GROUP_MY, contactGroup.getResourceName());
      }

      if (contactGroup.getFormattedName().equals(CONTACT_GROUP_CORE_TEAM)) {
        result.put(CONTACT_GROUP_CORE_TEAM, contactGroup.getResourceName());
      }

      if (contactGroup.getFormattedName().equals(CONTACT_GROUP_NATIONAL_STAFF)) {
        result.put(CONTACT_GROUP_NATIONAL_STAFF, contactGroup.getResourceName());
      }

      if (contactGroup.getFormattedName().equals(CONTACT_GROUP_LTO)) {
        result.put(CONTACT_GROUP_LTO, contactGroup.getResourceName());
      }
    }

    return result;
  }
  
  private void addContact(PeopleService peopleService, Person person) throws IOException, GeneralSecurityException {
    Person createdContact = peopleService.people().createContact(person).execute();
    System.out.println("createdContact = " + createdContact);
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  private List<Person> getContactsFromSheet(String spreadsheetId,
      Map<String, String> odihrContactsGroups) throws IOException {
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
          
          List memberships = new ArrayList<>();
          
          if (row.get(4).toString().equals("* Core team ::: * myContacts")) {
            memberships.add(new Membership().setContactGroupMembership(new ContactGroupMembership()
                .setContactGroupResourceName(odihrContactsGroups.get(CONTACT_GROUP_CORE_TEAM))));
          } else if (row.get(4).toString().equals("* National Staff ::: * myContacts")) {
            memberships.add(new Membership().setContactGroupMembership(new ContactGroupMembership()
                .setContactGroupResourceName(odihrContactsGroups.get(CONTACT_GROUP_NATIONAL_STAFF))));
          } else if (row.get(4).toString().equals("* LTOs ::: * myContacts")) {
            memberships.add(new Membership().setContactGroupMembership(new ContactGroupMembership()
                .setContactGroupResourceName(odihrContactsGroups.get(CONTACT_GROUP_LTO))));
          } else {
            memberships.add(new Membership().setContactGroupMembership(new ContactGroupMembership()
                .setContactGroupId(odihrContactsGroups.get(CONTACT_GROUP_MY))));
          }
          personNew.setMemberships(memberships);
          
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
