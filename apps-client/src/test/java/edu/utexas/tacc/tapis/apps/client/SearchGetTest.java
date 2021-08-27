package edu.utexas.tacc.tapis.apps.client;

import edu.utexas.tacc.tapis.auth.client.AuthClient;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
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

import static edu.utexas.tacc.tapis.apps.client.Utils.*;
import static org.testng.Assert.assertEquals;

/*
 * Test the Apps API client acting as a user fetching apps using getApps() with search conditions.
 * 
 * See Utils in this package for information on environment required to run the tests.
 */
@Test(groups={"integration"})
public class SearchGetTest
{
//  // Test data
//  private static final String testKey = "CltSrchGet";
//  private static final String appNameLikeAll = "*CltSrchGet*";
//
//  // Timestamps in various formats
//  private static final String longPast1 =   "1800-01-01T00:00:00.123456Z";
//  private static final String farFuture1 =  "2200-04-29T14:15:52.123456-06:00";
//  private static final String farFuture2 =  "2200-04-29T14:15:52.123Z";
//  private static final String farFuture3 =  "2200-04-29T14:15:52.123";
//  private static final String farFuture4 =  "2200-04-29T14:15:52-06:00";
//  private static final String farFuture5 =  "2200-04-29T14:15:52";
//  private static final String farFuture6 =  "2200-04-29T14:15-06:00";
//  private static final String farFuture7 =  "2200-04-29T14:15";
//  private static final String farFuture8 =  "2200-04-29T14-06:00";
//  private static final String farFuture9 =  "2200-04-29T14";
//  private static final String farFuture10 = "2200-04-29-06:00";
//  private static final String farFuture11 = "2200-04-29";
//  private static final String farFuture12 = "2200-04Z";
//  private static final String farFuture13 = "2200-04";
//  private static final String farFuture14 = "2200Z";
//  private static final String farFuture15 = "2200";
//
//  // Strings for char relational testings
//  private static final String hostName1 = "host" + testKey + "_001";
//  private static final String hostName7 = "host" + testKey + "_007";
//
//  private String serviceURL, ownerUser1JWT, ownerUser2JWT, adminUserJWT;
//
//  private final int numApps = 20;
//  private final Map<Integer, String[]> apps = Utils.makeApps(numApps, testKey);
//  private final Map<Integer, App> appsMap = new HashMap<>();
//
//  private LocalDateTime createBegin;
//  private LocalDateTime createEnd;
//
//  @BeforeSuite
//  public void setUp() throws Exception {
//    // Get the base URLs from the environment so the test can be used in environments other than dev
//    System.out.println("****** Executing BeforeSuite setup method for class: " + this.getClass().getSimpleName());
//    // Set service port for apps service. Check for port set as env var
//    // NOTE: This is ignored if TAPIS_ENV_SVC_URL_APPS is set
//    String servicePort = Utils.getServicePort();
//    // Set base URL for apps service. Check for URL set as env var
//    serviceURL = Utils.getServiceURL(servicePort);
//    // Get base URL suffix from env or from default
//    String baseURL = Utils.getBaseURL();
//    // Log URLs being used
//    System.out.println("Using Apps URL: " + serviceURL);
//    System.out.println("Using Authenticator URL: " + baseURL);
//    System.out.println("Using Tokens URL: " + baseURL);
//    // Get short term user JWT from tokens service
//    var authClient = new AuthClient(baseURL);
//    try {
//      ownerUser1JWT = authClient.getToken(ownerUser1, ownerUser1);
//      ownerUser2JWT = authClient.getToken(ownerUser2, ownerUser2);
//      adminUserJWT = authClient.getToken(adminUser, adminUser);
//    } catch (Exception e) {
//      throw new Exception("Exception while creating tokens or auth service", e);
//    }
//    // Basic check of JWTs
//    if (StringUtils.isBlank(ownerUser1JWT)) throw new Exception("Authn service returned invalid owner user1 JWT");
//    if (StringUtils.isBlank(ownerUser2JWT)) throw new Exception("Authn service returned invalid owner user2 JWT");
//    if (StringUtils.isBlank(adminUserJWT)) throw new Exception("Authn service returned invalid admin user JWT");
//
//    // Cleanup anything leftover from previous failed run
//    tearDown();
//
////    String[] tenantName = 0, name = 1, "description " + suffix = 2, sysType = 3, ownerUser = 4, "host"+suffix = 5,
////             "effUser"+suffix = 6, "fakePassword"+suffix = 7,"bucket"+suffix = 8, "/root"+suffix = 9,
////             "jobLocalWorkDir"+suffix = 10, "jobLocalArchDir"+suffix = 11,
////            "jobRemoteArchSystem"+suffix = 12, "jobRemoteArchDir"+suffix = 13};
//
//    // For half the apps change the owner
//    for (int i = numApps/2 + 1; i <= numApps; i++) { apps.get(i)[4] = ownerUser2; }
//
//    // For one app update description to have some special characters. 7 special chars in value: ,()~*!\
//    //   and update archiveLocalDir for testing an escaped comma in a list value
//    apps.get(numApps-1)[2] = specialChar7Str;
//    apps.get(numApps-1)[11] = escapedCommaInListValue;
//
//    // Create all the apps in the dB using the in-memory objects, recording start and end times
//    createBegin = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
//    Thread.sleep(500);
//    // Check for a app. If it is there assume data is already properly seeded.
//    // This seems like a reasonable approach since there is not a way to clean up (i.e., hard delete
//    // apps and other resources) using the client.
//    App tmpApp;
//    try {
//      tmpApp = getClientUsr(serviceURL, ownerUser1JWT).getApp(apps.get(1)[1], apps.get(1)[2]);
//    } catch (TapisClientException e) {
//      Assert.assertEquals(e.getCode(), 404);
//      tmpApp = null;
//    }
//    if (tmpApp == null)
//    {
//      System.out.println("Test data not found. Test apps will be created.");
//      for (int i = 1; i <= numApps; i++)
//      {
//        String[] app0 = apps.get(i);
//        int port = i;
//        if (i <= numApps / 2)
//        {
//          // Vary port # for checking numeric relational searches
//          Utils.createApp(getClientUsr(serviceURL, ownerUser1JWT), app0);
//          tmpApp = getClientUsr(serviceURL, ownerUser1JWT).getApp(app0[1], app0[2]);
//        } else
//        {
//          Utils.createApp(getClientUsr(serviceURL, ownerUser2JWT), app0);
//          tmpApp = getClientUsr(serviceURL, ownerUser2JWT).getApp(app0[1], app0[2]);
//        }
//        Assert.assertNotNull(tmpApp);
//        appsMap.put(i, tmpApp);
//      }
//    } else {
//      System.out.println("Test data found. Test apps will not be created.");
//    }
//    Thread.sleep(500);
//    createEnd = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
//  }
//
//  @AfterSuite
//  public void tearDown()
//  {
//    // Currently no way to hard delete from client (by design)
//  }
//
//  /*
//   * Check valid cases
//   */
//  @Test(groups={"integration"})
//  public void testValidCases() throws Exception
//  {
//    App app0 = appsMap.get(1);
//    String app0Name = app0.getId();
//    String nameList = "noSuchName1,noSuchName2," + app0Name + ",noSuchName3";
//    // Create all input and validation data for tests
//    // NOTE: Some cases require "name.like." + appNameLikeAll in the list of conditions since maven runs the tests in
//    //       parallel and not all attribute names are unique across integration tests
//    class CaseData {public final int count; public final String searchStr; CaseData(int c, String s) { count = c; searchStr = s; }}
//    var validCaseInputs = new HashMap<Integer, CaseData>();
//    // Test basic types and operators
//    validCaseInputs.put( 1,new CaseData(1, "name.eq." + app0Name)); // 1 has specific name
//    validCaseInputs.put( 2,new CaseData(1, "description.eq." + app0.getDescription()));
//    validCaseInputs.put(10,new CaseData(numApps/2, "(name.like." + appNameLikeAll + ")~(owner.eq." + ownerUser1 + ")"));  // Half owned by one user
//    validCaseInputs.put(11,new CaseData(numApps/2, "(name.like." + appNameLikeAll + ")~(owner.eq." + ownerUser2 + ")")); // and half owned by another
//    validCaseInputs.put(12,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(enabled.eq.true)"));  // All are enabled
//    validCaseInputs.put(13,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(deleted.eq.false)")); // none are deleted
//    validCaseInputs.put(14,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(deleted.neq.true)")); // none are deleted
//    validCaseInputs.put(15,new CaseData(0, "(name.like." + appNameLikeAll + ")~(deleted.eq.true)"));   // none are deleted
//    validCaseInputs.put(16,new CaseData(1, "name.like." + app0Name));
//    validCaseInputs.put(17,new CaseData(0, "name.like.NOSUCHAPPxFM2c29bc8RpKWeE2sht7aZrJzQf3s"));
//    validCaseInputs.put(18,new CaseData(numApps, "name.like." + appNameLikeAll));
//    validCaseInputs.put(19,new CaseData(numApps-1, "(name.like." + appNameLikeAll + ")~(name.nlike." + app0Name + ")"));
//    validCaseInputs.put(20,new CaseData(1, "(name.like." + appNameLikeAll + ")~(name.in." + nameList + ")"));
//    validCaseInputs.put(21,new CaseData(numApps-1, "(name.like." + appNameLikeAll + ")~(name.nin." + nameList + ")"));
//    validCaseInputs.put(22,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(app_type.eq.LINUX)"));
//    validCaseInputs.put(23,new CaseData(numApps/2, "(name.like." + appNameLikeAll + ")~(app_type.eq.LINUX)~(owner.neq." + ownerUser2 + ")"));
//    // Test numeric relational
//    validCaseInputs.put(40,new CaseData(numApps/2, "(name.like." + appNameLikeAll + ")~(port.between.1," + numApps/2 + ")"));
//    validCaseInputs.put(41,new CaseData(numApps/2-1, "(name.like." + appNameLikeAll + ")~(port.between.2," + numApps/2 + ")"));
//    validCaseInputs.put(42,new CaseData(numApps/2, "(name.like." + appNameLikeAll + ")~(port.nbetween.1," + numApps/2 + ")"));
//    validCaseInputs.put(43,new CaseData(13, "(name.like." + appNameLikeAll + ")~(enabled.eq.true)~(port.lte.13)"));
//    validCaseInputs.put(44,new CaseData(5, "(name.like." + appNameLikeAll + ")~(enabled.eq.true)~(port.gt.1)~(port.lt.7)"));
//    // Test char relational
//    validCaseInputs.put(50,new CaseData(1, "(name.like." + appNameLikeAll + ")~(host.lte."+hostName1+")"));
//    validCaseInputs.put(51,new CaseData(numApps-7, "(name.like." + appNameLikeAll + ")~(enabled.eq.true)~(host.gt."+hostName7+")"));
//    validCaseInputs.put(52,new CaseData(5, "(name.like." + appNameLikeAll + ")~(host.gt."+hostName1+")~(host.lt."+hostName7+")"));
//    validCaseInputs.put(53,new CaseData(0, "(name.like." + appNameLikeAll + ")~(host.lt."+hostName1+")~(host.gt."+hostName7+")"));
//    validCaseInputs.put(54,new CaseData(7, "(name.like." + appNameLikeAll + ")~(host.between."+hostName1+","+hostName7+")"));
//    validCaseInputs.put(55,new CaseData(numApps-7, "(name.like." + appNameLikeAll + ")~(host.nbetween."+hostName1+","+hostName7+")"));
//    // Test timestamp relational
//    validCaseInputs.put(60,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.gt." + longPast1+")"));
//    validCaseInputs.put(61,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture1+")"));
//    validCaseInputs.put(62,new CaseData(0, "(name.like." + appNameLikeAll + ")~(created.lte." + longPast1+")"));
//    validCaseInputs.put(63,new CaseData(0, "(name.like." + appNameLikeAll + ")~(created.gte." + farFuture1+")"));
//    validCaseInputs.put(64,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.between." + longPast1 + "," + farFuture1+")"));
//    validCaseInputs.put(65,new CaseData(0, "(name.like." + appNameLikeAll + ")~(created.nbetween." + longPast1 + "," + farFuture1+")"));
//    // Variations of timestamp format
//    validCaseInputs.put(66,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture2+")"));
//    validCaseInputs.put(67,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture3+")"));
//    validCaseInputs.put(68,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture4+")"));
//    validCaseInputs.put(69,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture5+")"));
//    validCaseInputs.put(70,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture6+")"));
//    validCaseInputs.put(71,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture7+")"));
//    validCaseInputs.put(72,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture8+")"));
//    validCaseInputs.put(73,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture9+")"));
//    validCaseInputs.put(74,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture10+")"));
//    validCaseInputs.put(75,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture11+")"));
//    validCaseInputs.put(76,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture12+")"));
//    validCaseInputs.put(77,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture13+")"));
//    validCaseInputs.put(78,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture14+")"));
//    validCaseInputs.put(79,new CaseData(numApps, "(name.like." + appNameLikeAll + ")~(created.lt." + farFuture15+")"));
//    // Test wildcards
//    validCaseInputs.put(80,new CaseData(numApps, "(enabled.eq.true)~(host.like.host" + testKey + "*)"));
//    validCaseInputs.put(81,new CaseData(0, "(name.like." + appNameLikeAll + ")~(enabled.eq.true)~(host.nlike.host" + testKey + "*)"));
//    validCaseInputs.put(82,new CaseData(9, "(name.like." + appNameLikeAll + ")~(enabled.eq.true)~(host.like.host" + testKey + "_00!)"));
//    validCaseInputs.put(83,new CaseData(11, "(name.like." + appNameLikeAll + ")~(enabled.eq.true)~(host.nlike.host" + testKey + "_00!)"));
//    // Test that underscore and % get escaped as needed before being used as SQL
//    validCaseInputs.put(90,new CaseData(0, "(name.like." + appNameLikeAll + ")~(host.like.host" + testKey + "_00_)"));
//    validCaseInputs.put(91,new CaseData(0, "(name.like." + appNameLikeAll + ")~(host.like.host" + testKey + "_00%)"));
//    // Check various special characters in description. 7 special chars in value: ,()~*!\
//    validCaseInputs.put(101,new CaseData(1, "(name.like." + appNameLikeAll + ")~(description.like." + specialChar7LikeSearchStr+")"));
//    validCaseInputs.put(102,new CaseData(numApps-1, "(name.like." + appNameLikeAll + ")~(description.nlike." + specialChar7LikeSearchStr+")"));
//    validCaseInputs.put(103,new CaseData(1, "(name.like." + appNameLikeAll + ")~(description.eq." + specialChar7EqSearchStr+")"));
//    validCaseInputs.put(104,new CaseData(numApps-1, "(name.like." + appNameLikeAll + ")~(description.neq." + specialChar7EqSearchStr+")"));
//    // Escaped comma in a list of values
//    validCaseInputs.put(110,new CaseData(1, "(name.like." + appNameLikeAll + ")~(job_local_archive_dir.in.noSuchDir," + escapedCommaInListValue +")"));
//
//    // Iterate over valid cases
//    for (Map.Entry<Integer,CaseData> item : validCaseInputs.entrySet())
//    {
//      CaseData cd = item.getValue();
//      int caseNum = item.getKey();
//      System.out.println("Checking case # " + caseNum + " Input: " + cd.searchStr);
//      List<App> searchResults = getClientUsr(serviceURL, adminUserJWT).getApps(cd.searchStr);
//      assertEquals(searchResults.size(), cd.count);
//    }
//  }
}

