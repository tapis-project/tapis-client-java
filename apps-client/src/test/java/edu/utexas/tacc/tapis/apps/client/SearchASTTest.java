package edu.utexas.tacc.tapis.apps.client;

import edu.utexas.tacc.tapis.auth.client.AuthClient;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqSearchApps;
import edu.utexas.tacc.tapis.apps.client.gen.model.App;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.utexas.tacc.tapis.apps.client.Utils.adminUser;
import static edu.utexas.tacc.tapis.apps.client.Utils.getClientUsr;
import static edu.utexas.tacc.tapis.apps.client.Utils.ownerUser1;
import static edu.utexas.tacc.tapis.apps.client.Utils.ownerUser2;

import static edu.utexas.tacc.tapis.apps.client.Utils.*;
import static org.testng.Assert.assertEquals;

/**
 * Test the Apps API client acting as a user fetching apps using searchApps() with search conditions.
 * 
 * See Utils in this package for information on environment required to run the tests.
 */
@Test(groups={"integration"})
public class SearchASTTest
{
  // Test data
  private static final String testKey = "CltSrchAst";
  private static final String appNameLikeAll = sq("%" + testKey + "%");

  private String serviceURL, ownerUser1JWT, ownerUser2JWT, adminUserJWT;

  private final int numApps = 20;
  private final Map<Integer, String[]> apps = Utils.makeApps(numApps, testKey);
  private final Map<Integer, App> appsMap = new HashMap<>();

  private LocalDateTime createBegin;
  private LocalDateTime createEnd;

  @BeforeSuite
  public void setUp() throws Exception {
    // Get the base URLs from the environment so the test can be used in environments other than dev
    System.out.println("****** Executing BeforeSuite setup method for class: " + this.getClass().getSimpleName());
    // Set service port for apps service. Check for port set as env var
    // NOTE: This is ignored if TAPIS_ENV_SVC_URL_APPS is set
    String servicePort = Utils.getServicePort();
    // Set base URL for apps service. Check for URL set as env var
    serviceURL = Utils.getServiceURL(servicePort);
    // Get base URL suffix from env or from default
    String baseURL = Utils.getBaseURL();
    // Log URLs being used
    System.out.println("Using Apps URL: " + serviceURL);
    System.out.println("Using Authenticator URL: " + baseURL);
    System.out.println("Using Tokens URL: " + baseURL);
    // Get short term user JWT from tokens service
    var authClient = new AuthClient(baseURL);
    try {
      ownerUser1JWT = authClient.getToken(ownerUser1, ownerUser1);
      ownerUser2JWT = authClient.getToken(ownerUser2, ownerUser2);
      adminUserJWT = authClient.getToken(adminUser, adminUser);
    } catch (Exception e) {
      throw new Exception("Exception while creating tokens or auth service", e);
    }
    // Basic check of JWTs
    if (StringUtils.isBlank(ownerUser1JWT)) throw new Exception("Authn service returned invalid owner user1 JWT");
    if (StringUtils.isBlank(ownerUser2JWT)) throw new Exception("Authn service returned invalid owner user2 JWT");
    if (StringUtils.isBlank(adminUserJWT)) throw new Exception("Authn service returned invalid admin user JWT");

    // Cleanup anything leftover from previous failed run
    tearDown();

//    String[] tenantName = 0, name = 1, "description " + suffix = 2, appType = 3, ownerUser = 4, "host"+suffix = 5,
//             "effUser"+suffix = 6, "fakePassword"+suffix = 7,"bucket"+suffix = 8, "/root"+suffix = 9,
//             "jobLocalWorkDir"+suffix = 10, "jobLocalArchDir"+suffix = 11,
//            "jobRemoteArchApptem"+suffix = 12, "jobRemoteArchDir"+suffix = 13};

    // For half the apps change the owner
    for (int i = numApps/2 + 1; i <= numApps; i++) { apps.get(i)[4] = ownerUser2; }

    // For one app update description to have some special characters. 7 special chars in value: ,()~*!\
    //   and update archiveLocalDir for testing an escaped comma in a list value
    apps.get(numApps-1)[2] = specialChar7Str;
    apps.get(numApps-1)[11] = escapedCommaInListValue;

    // Create all the apps in the dB using the in-memory objects, recording start and end times
    createBegin = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
    Thread.sleep(500);
    // Check for an app. If it is there assume data is already properly seeded.
    // This seems like a reasonable approach since there is not a way to clean up (i.e., hard delete
    // apps and other resources) using the client.
    App tmpApp;
    try {
      tmpApp = getClientUsr(serviceURL, ownerUser1JWT).getApp(apps.get(1)[1], apps.get(1)[2]);
    } catch (TapisClientException e) {
      assertEquals(e.getCode(), 404);
      tmpApp = null;
    }
    if (tmpApp == null)
    {
      System.out.println("Test data not found. Test apps will be created.");
      for (int i = 1; i <= numApps; i++)
      {
        String[] app0 = apps.get(i);
        int port = i;
        if (i <= numApps / 2)
        {
          // Vary port # for checking numeric relational searches
          Utils.createApp(getClientUsr(serviceURL, ownerUser1JWT), app0);
          tmpApp = getClientUsr(serviceURL, ownerUser1JWT).getApp(app0[1], app0[2]);
        }
        else
        {
          Utils.createApp(getClientUsr(serviceURL, ownerUser2JWT), app0);
          tmpApp = getClientUsr(serviceURL, ownerUser2JWT).getApp(app0[1], app0[2]);
        }
        Assert.assertNotNull(tmpApp);
        appsMap.put(i, tmpApp);
      }
    }
    else
    {
      System.out.println("Test data found. Test apps will not be created.");
      for (int i = 1; i <= numApps; i++)
      {
        String[] app0 = apps.get(i);
        tmpApp = getClientUsr(serviceURL, adminUserJWT).getApp(app0[1], app0[2]);
        Assert.assertNotNull(tmpApp);
        appsMap.put(i, tmpApp);
      }
    }
    Thread.sleep(500);
    createEnd = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
  }
  @AfterSuite
  public void tearDown()
  {
    // Currently no way to hard delete from client (by design)
  }

  /*
   * Check valid cases
   */
  @Test(groups={"integration"})
  public void testValidCases() throws Exception
  {
    App app0 = appsMap.get(1);
    String app0Name = app0.getName();
    String nameList = "noSuchName1,noSuchName2," + app0Name + ",noSuchName3";
    // Create all input and validation data for tests
    // NOTE: Some cases require "name.like." + appNameLikeAll in the list of conditions since maven runs the tests in
    //       parallel and not all attribute names are unique across integration tests
    class CaseData {public final int count; public final String searchStr; CaseData(int c, String s) { count = c; searchStr = s; }}
    var validCaseInputs = new HashMap<Integer, CaseData>();
    // Test basic types and operators
    validCaseInputs.put( 1,new CaseData(1, "name = " + app0Name)); // 1 has specific name
//    validCaseInputs.put( 2,new CaseData(1, "description = " + app0.getDescription())); // TODO handle underscore character properly. how?
    validCaseInputs.put(10,new CaseData(numApps/2, "name LIKE " + appNameLikeAll + " AND owner = " + sq(ownerUser1)));  // Half owned by one user
    validCaseInputs.put(11,new CaseData(numApps/2, "name LIKE " + appNameLikeAll + " AND owner = " + sq(ownerUser2))); // and half owned by another
    validCaseInputs.put(12,new CaseData(numApps, "name LIKE " + appNameLikeAll + " AND enabled = true"));  // All are enabled
    validCaseInputs.put(13,new CaseData(numApps, "name LIKE " + appNameLikeAll + " AND deleted = false")); // none are deleted
    validCaseInputs.put(14,new CaseData(numApps, "name LIKE " + appNameLikeAll + " AND deleted <> true")); // none are deleted
    validCaseInputs.put(15,new CaseData(0, "name LIKE " + appNameLikeAll + " AND deleted = true"));           // none are deleted
    validCaseInputs.put(16,new CaseData(1, "name LIKE " + sq(app0Name)));
    validCaseInputs.put(17,new CaseData(0, "name LIKE 'NOSUCHAPPxFM2c29bc8RpKWeE2sht7aZrJzQf3s'"));
    validCaseInputs.put(18,new CaseData(numApps, "name LIKE " + appNameLikeAll));

    // TODO Add more test cases, see SearchASTDaoTest in tapis-java

    // Iterate over valid cases
    for (Map.Entry<Integer,CaseData> item : validCaseInputs.entrySet())
    {
      CaseData cd = item.getValue();
      int caseNum = item.getKey();
      System.out.println("Checking case # " + caseNum + " Input: " + cd.searchStr);
      // Build request
      ReqSearchApps req = new ReqSearchApps();
      req.addSearchItem(cd.searchStr);
      // Submit search request
      List<App> searchResults = getClientUsr(serviceURL, adminUserJWT).searchApps(req);
      assertEquals(searchResults.size(), cd.count);
    }
  }

  private static String sq(String s) { return "'" + s + "'"; }
}

