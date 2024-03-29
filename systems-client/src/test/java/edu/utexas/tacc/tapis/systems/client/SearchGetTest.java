package edu.utexas.tacc.tapis.systems.client;

import edu.utexas.tacc.tapis.auth.client.AuthClient;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqPostSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.SchedulerTypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.TapisSystem;
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

import static edu.utexas.tacc.tapis.client.shared.Utils.*;
import static edu.utexas.tacc.tapis.systems.client.Utils.*;
import static org.testng.Assert.assertEquals;

/*
 * Test the Systems API client acting as a user fetching systems using getSystems()
 *  with search conditions and sort options.
 * 
 * See Utils in this package for information on environment required to run the tests.
 */
@Test(groups={"integration"})
public class SearchGetTest
{
  // Test data
  private static final String testKey = "CltSrchGet";
  private static final String sysNameLikeAll = "*CltSrchGet*";

  // Timestamps in various formats
  private static final String longPast1 =   "1800-01-01T00:00:00.123456Z";
  private static final String farFuture1 =  "2200-04-29T14:15:52.123456-06:00";
  private static final String farFuture2 =  "2200-04-29T14:15:52.123Z";
  private static final String farFuture3 =  "2200-04-29T14:15:52.123";
  private static final String farFuture4 =  "2200-04-29T14:15:52-06:00";
  private static final String farFuture5 =  "2200-04-29T14:15:52";
  private static final String farFuture6 =  "2200-04-29T14:15-06:00";
  private static final String farFuture7 =  "2200-04-29T14:15";
  private static final String farFuture8 =  "2200-04-29T14-06:00";
  private static final String farFuture9 =  "2200-04-29T14";
  private static final String farFuture10 = "2200-04-29-06:00";
  private static final String farFuture11 = "2200-04-29";
  private static final String farFuture12 = "2200-04Z";
  private static final String farFuture13 = "2200-04";
  private static final String farFuture14 = "2200Z";
  private static final String farFuture15 = "2200";

  // Strings for char relational testings
  private static final String hostName1 = "host" + testKey + "001.test.org";
  private static final String hostName7 = "host" + testKey + "007.test.org";

  private String serviceURL, testUser1JWT, testUser2JWT, adminUserJWT;

  private final int numSystems = 20;
  private final Map<Integer, String[]> systems = Utils.makeSystems(numSystems, testKey);
  private final Map<Integer, TapisSystem> systemsMap = new HashMap<>();

  private LocalDateTime createBegin;
  private LocalDateTime createEnd;

  @BeforeSuite
  public void setUp() throws Exception {
    // Get the base URLs from the environment so the test can be used in environments other than dev
    System.out.println("****** Executing BeforeSuite setup method for class: " + this.getClass().getSimpleName());
    // Set service port for systems service. Check for port set as env var
    // NOTE: This is ignored if TAPIS_ENV_SVC_URL_SYSTEMS is set
    String servicePort = Utils.getServicePort();
    // Set base URL for systems service. Check for URL set as env var
    serviceURL = Utils.getServiceURL(servicePort);
    // Get base URL suffix from env or from default
    String baseURL = Utils.getBaseURL();
    // Log URLs being used
    System.out.println("Using Systems URL: " + serviceURL);
    System.out.println("Using Authenticator URL: " + baseURL);
    System.out.println("Using Tokens URL: " + baseURL);
    // Get short term user JWT from tokens service
    var authClient = new AuthClient(baseURL);
    try {
      testUser1JWT = authClient.getToken(testUser1, testUser1);
      testUser2JWT = authClient.getToken(testUser2, testUser2);
      adminUserJWT = authClient.getToken(adminUser, adminUser);
//      testUser1JWT = Utils.testUser1JWT;
//      testUser2JWT = Utils.testUser2JWT;
//      adminUserJWT = Utils.adminUserJWT;
    } catch (Exception e) {
      throw new Exception("Exception while creating tokens or auth service", e);
    }
    // Basic check of JWTs
    if (StringUtils.isBlank(testUser1JWT)) throw new Exception("Authn service returned invalid owner user1 JWT");
    if (StringUtils.isBlank(testUser2JWT)) throw new Exception("Authn service returned invalid owner user2 JWT");
    if (StringUtils.isBlank(adminUserJWT)) throw new Exception("Authn service returned invalid admin user JWT");

    // Cleanup anything leftover from previous failed run
    tearDown();

// String[] sys0 = 0=tenantName, 1=name, 2=description, 3=sysType, 4=testUser1, 5=host, 6=effUser, 7=password,
//                 8=bucketName, 9=rootDir, 10=jobWorkingDir, 11=batchScheduler, 12=batchDefaultLogicalQueue
//                 13=batchSchedulerProfile
    // For half the systems change the owner, batchScheduler
    for (int i = numSystems/2 + 1; i <= numSystems; i++)
    {
      systems.get(i)[4] = testUser2;
      systems.get(i)[11] = SchedulerTypeEnum.CONDOR.name();
    }

    // For one system update description to have some special characters. 7 special chars in value: ,()~*!\
    //   and update jobWorkingDir for testing an escaped comma in a list value
    systems.get(numSystems-1)[2] = specialChar7Str;
    systems.get(numSystems-1)[10] = escapedCommaInListValue;

    // Create all the systems in the dB using the in-memory objects, recording start and end times
    createBegin = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
    Thread.sleep(500);
    // Check for a system. If it is there assume data is already properly seeded.
    // This seems like a reasonable approach since there is not a way to clean up (i.e., hard delete
    // systems and other resources) using the client.
    TapisSystem tmpSys;
    try {
      tmpSys = getClientUsr(serviceURL, testUser1JWT).getSystem(systems.get(1)[1]);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      tmpSys = null;
    }
    if (tmpSys == null)
    {
      System.out.println("Test data not found. Test systems will be created.");
      // Create systems
      for (int i = 1; i <= numSystems; i++)
      {
        String[] sys0 = systems.get(i);
        // Vary port # for checking numeric relational searches
        int port = i;
        ReqPostSystem rSys;
        // Create half the systems owned by testUser1 and half by testUser2
        if (i <= numSystems / 2)
        {
          rSys = Utils.createReqSystem(sys0, port, prot1AuthnMethod, null);
          getClientUsr(serviceURL, testUser1JWT).createSystem(rSys);
          tmpSys = getClientUsr(serviceURL, testUser1JWT).getSystem(sys0[1]);
        }
        else
        {
          rSys = Utils.createReqSystem(sys0, port, prot1AuthnMethod, null);
          getClientUsr(serviceURL, testUser2JWT).createSystem(rSys);
          tmpSys = getClientUsr(serviceURL, testUser2JWT).getSystem(sys0[1]);
        }
        Assert.assertNotNull(tmpSys);
        systemsMap.put(i, tmpSys);
      }
    }
    else
    {
      System.out.println("Test data found. Test systems will not be created.");
      for (int i = 1; i <= numSystems; i++)
      {
        String[] sys0 = systems.get(i);
        tmpSys = getClientUsr(serviceURL, adminUserJWT).getSystem(sys0[1]);
        Assert.assertNotNull(tmpSys);
        systemsMap.put(i, tmpSys);
      }
    }
    Thread.sleep(500);
    createEnd = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
  }

  @AfterSuite
  public void tearDown()
  {
    System.out.println("****** Executing AfterSuite teardown method for class: " + this.getClass().getSimpleName());
    // Remove all objects created by tests, ignore any exceptions
    // This is a soft delete but still should be done to clean up SK artifacts.
    for (int i = 1; i <= numSystems; i++)
    {
      String systemId = systems.get(i)[1];
      if (i <= numSystems / 2)
      {
        try { getClientUsr(serviceURL, testUser1JWT).deleteSystem(systemId); }
        catch (Exception e)
        {
          System.out.println("Caught exception when deleting system: " + systemId + " Exception: " + e);
        }
      }
      else
      {
        try { getClientUsr(serviceURL, testUser2JWT).deleteSystem(systemId); }
        catch (Exception e)
        {
          System.out.println("Caught exception when deleting system: " + systemId + " Exception: " + e);
        }
      }
    }
  }

  /*
   * Check valid cases
   */
  @Test(groups={"integration"})
  public void testValidCases() throws Exception
  {
    TapisSystem sys0 = systemsMap.get(1);
    String sys0Name = sys0.getId();
    String nameList = "noSuchName1,noSuchName2," + sys0Name + ",noSuchName3";
    // Create all input and validation data for tests
    // NOTE: Some cases require "id.like." + sysNameLikeAll in the list of conditions since maven runs the tests in
    //       parallel and not all attribute names are unique across integration tests
    class CaseData {public final int count; public final String searchStr; CaseData(int c, String s) { count = c; searchStr = s; }}
    var validCaseInputs = new HashMap<Integer, CaseData>();
    // Test basic types and operators
    validCaseInputs.put( 1,new CaseData(1, "id.eq." + sys0Name)); // 1 has specific name
    validCaseInputs.put( 2,new CaseData(1, "description.eq." + sys0.getDescription()));
    validCaseInputs.put( 3,new CaseData(1, "host.eq." + sys0.getHost()));
    validCaseInputs.put( 4,new CaseData(1, "bucket_name.eq." + sys0.getBucketName()));
    validCaseInputs.put( 5,new CaseData(1, "root_dir.eq." + sys0.getRootDir()));
    validCaseInputs.put( 6,new CaseData(1, "job_working_dir.eq." + sys0.getJobWorkingDir()));
    validCaseInputs.put( 7,new CaseData(numSystems/2, "(id.like." + sysNameLikeAll + ")~(batch_scheduler.eq." + SchedulerTypeEnum.CONDOR.name() + ")"));
    validCaseInputs.put( 8,new CaseData(1, "batch_default_logical_queue.eq." + sys0.getBatchDefaultLogicalQueue()));
    validCaseInputs.put( 9,new CaseData(1, "batch_scheduler_profile.eq." + sys0.getBatchSchedulerProfile()));
    validCaseInputs.put(10,new CaseData(numSystems/2, "(id.like." + sysNameLikeAll + ")~(owner.eq." + testUser1 + ")"));  // Half owned by one user
    validCaseInputs.put(11,new CaseData(numSystems/2, "(id.like." + sysNameLikeAll + ")~(owner.eq." + testUser2 + ")")); // and half owned by another
    validCaseInputs.put(12,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(enabled.eq.true)"));  // All are enabled
    validCaseInputs.put(13,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(deleted.eq.false)")); // none are deleted
    validCaseInputs.put(14,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(deleted.neq.true)")); // none are deleted
    validCaseInputs.put(15,new CaseData(0, "(id.like." + sysNameLikeAll + ")~(deleted.eq.true)"));   // none are deleted
    validCaseInputs.put(16,new CaseData(1, "id.like." + sys0Name));
    validCaseInputs.put(17,new CaseData(0, "id.like.NOSUCHSYSTEMxFM2c29bc8RpKWeE2sht7aZrJzQf3s"));
    validCaseInputs.put(18,new CaseData(numSystems, "id.like." + sysNameLikeAll));
    validCaseInputs.put(19,new CaseData(numSystems-1, "(id.like." + sysNameLikeAll + ")~(id.nlike." + sys0Name + ")"));
    validCaseInputs.put(20,new CaseData(1, "(id.like." + sysNameLikeAll + ")~(id.in." + nameList + ")"));
    validCaseInputs.put(21,new CaseData(numSystems-1, "(id.like." + sysNameLikeAll + ")~(id.nin." + nameList + ")"));
    validCaseInputs.put(22,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(system_type.eq.LINUX)"));
    validCaseInputs.put(23,new CaseData(numSystems/2, "(id.like." + sysNameLikeAll + ")~(system_type.eq.LINUX)~(owner.neq." + testUser2 + ")"));
    // Test numeric relational
    validCaseInputs.put(40,new CaseData(numSystems/2, "(id.like." + sysNameLikeAll + ")~(port.between.1," + numSystems/2 + ")"));
    validCaseInputs.put(41,new CaseData(numSystems/2-1, "(id.like." + sysNameLikeAll + ")~(port.between.2," + numSystems/2 + ")"));
    validCaseInputs.put(42,new CaseData(numSystems/2, "(id.like." + sysNameLikeAll + ")~(port.nbetween.1," + numSystems/2 + ")"));
    validCaseInputs.put(43,new CaseData(13, "(id.like." + sysNameLikeAll + ")~(enabled.eq.true)~(port.lte.13)"));
    validCaseInputs.put(44,new CaseData(5, "(id.like." + sysNameLikeAll + ")~(enabled.eq.true)~(port.gt.1)~(port.lt.7)"));
    // Test char relational
    validCaseInputs.put(50,new CaseData(1, "(id.like." + sysNameLikeAll + ")~(host.lte."+hostName1+")"));
    validCaseInputs.put(51,new CaseData(numSystems-7, "(id.like." + sysNameLikeAll + ")~(enabled.eq.true)~(host.gt."+hostName7+")"));
    validCaseInputs.put(52,new CaseData(5, "(id.like." + sysNameLikeAll + ")~(host.gt."+hostName1+")~(host.lt."+hostName7+")"));
    validCaseInputs.put(53,new CaseData(0, "(id.like." + sysNameLikeAll + ")~(host.lt."+hostName1+")~(host.gt."+hostName7+")"));
    validCaseInputs.put(54,new CaseData(7, "(id.like." + sysNameLikeAll + ")~(host.between."+hostName1+","+hostName7+")"));
    validCaseInputs.put(55,new CaseData(numSystems-7, "(id.like." + sysNameLikeAll + ")~(host.nbetween."+hostName1+","+hostName7+")"));
    // Test timestamp relational
    validCaseInputs.put(60,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.gt." + longPast1+")"));
    validCaseInputs.put(61,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture1+")"));
    validCaseInputs.put(62,new CaseData(0, "(id.like." + sysNameLikeAll + ")~(created.lte." + longPast1+")"));
    validCaseInputs.put(63,new CaseData(0, "(id.like." + sysNameLikeAll + ")~(created.gte." + farFuture1+")"));
    validCaseInputs.put(64,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.between." + longPast1 + "," + farFuture1+")"));
    validCaseInputs.put(65,new CaseData(0, "(id.like." + sysNameLikeAll + ")~(created.nbetween." + longPast1 + "," + farFuture1+")"));
    // Variations of timestamp format
    validCaseInputs.put(66,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture2+")"));
    validCaseInputs.put(67,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture3+")"));
    validCaseInputs.put(68,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture4+")"));
    validCaseInputs.put(69,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture5+")"));
    validCaseInputs.put(70,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture6+")"));
    validCaseInputs.put(71,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture7+")"));
    validCaseInputs.put(72,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture8+")"));
    validCaseInputs.put(73,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture9+")"));
    validCaseInputs.put(74,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture10+")"));
    validCaseInputs.put(75,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture11+")"));
    validCaseInputs.put(76,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture12+")"));
    validCaseInputs.put(77,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture13+")"));
    validCaseInputs.put(78,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture14+")"));
    validCaseInputs.put(79,new CaseData(numSystems, "(id.like." + sysNameLikeAll + ")~(created.lt." + farFuture15+")"));
    // Test wildcards
    validCaseInputs.put(80,new CaseData(numSystems, "(enabled.eq.true)~(host.like.host" + testKey + "*)"));
    validCaseInputs.put(81,new CaseData(0, "(id.like." + sysNameLikeAll + ")~(enabled.eq.true)~(host.nlike.host" + testKey + "*)"));
    validCaseInputs.put(82,new CaseData(9, "(id.like." + sysNameLikeAll + ")~(enabled.eq.true)~(host.like.host" + testKey + "00!.test.org)"));
    validCaseInputs.put(83,new CaseData(11, "(id.like." + sysNameLikeAll + ")~(enabled.eq.true)~(host.nlike.host" + testKey + "00!.test.org)"));
    // Test that underscore and % get escaped as needed before being used as SQL
    validCaseInputs.put(90,new CaseData(0, "(id.like." + sysNameLikeAll + ")~(host.like.host" + testKey + "00_)"));
    validCaseInputs.put(91,new CaseData(0, "(id.like." + sysNameLikeAll + ")~(host.like.host" + testKey + "00%)"));
    // Check various special characters in description. 7 special chars in value: ,()~*!\
    validCaseInputs.put(101,new CaseData(1, "(id.like." + sysNameLikeAll + ")~(description.like." + specialChar7LikeSearchStr+")"));
    validCaseInputs.put(102,new CaseData(numSystems-1, "(id.like." + sysNameLikeAll + ")~(description.nlike." + specialChar7LikeSearchStr+")"));
    validCaseInputs.put(103,new CaseData(1, "(id.like." + sysNameLikeAll + ")~(description.eq." + specialChar7EqSearchStr+")"));
    validCaseInputs.put(104,new CaseData(numSystems-1, "(id.like." + sysNameLikeAll + ")~(description.neq." + specialChar7EqSearchStr+")"));
    // Escaped comma in a list of values
    validCaseInputs.put(110,new CaseData(1, "(id.like." + sysNameLikeAll + ")~(job_working_dir.in.noSuchDir," + escapedCommaInListValue +")"));

    // Iterate over valid cases
    for (Map.Entry<Integer,CaseData> item : validCaseInputs.entrySet())
    {
      CaseData cd = item.getValue();
      int caseNum = item.getKey();
      System.out.println("Checking case # " + caseNum + " Input: " + cd.searchStr);
      List<TapisSystem> searchResults = getClientUsr(serviceURL, adminUserJWT).getSystems(cd.searchStr);
      assertEquals(searchResults.size(), cd.count);
    }
  }

  /*
   * Test sorting: limit, sortBy, skip
   */
  @Test(groups={"integration"})
  public void testSortingSkip() throws Exception
  {
    String searchStr = "id.like." + sysNameLikeAll;
    List<TapisSystem> searchResults;

    String selectStr = null;
    String sortBy;
    int limit;
    int skip;
    // Sort and check order with no limit or skip
    sortBy = "id(asc)";
    searchResults = getClientUsr(serviceURL, adminUserJWT).getSystems(searchStr, DEFAULT_LIMIT, sortBy, DEFAULT_SKIP, DEFAULT_STARTAFTER, selectStr, false);
    assertEquals(searchResults.size(), numSystems, "Incorrect result count");
    checkOrder(searchResults, 1, numSystems);
    sortBy = "id(desc)";
    searchResults = getClientUsr(serviceURL, adminUserJWT).getSystems(searchStr, DEFAULT_LIMIT, sortBy, DEFAULT_SKIP, DEFAULT_STARTAFTER, selectStr, false);
    assertEquals(searchResults.size(), numSystems, "Incorrect result count");
    checkOrder(searchResults, numSystems, 1);
    // Sort and check order with limit and no skip
    sortBy = "id(asc)";
    limit = 4;
    searchResults = getClientUsr(serviceURL, adminUserJWT).getSystems(searchStr, limit, sortBy, DEFAULT_SKIP, DEFAULT_STARTAFTER, selectStr, false);
    assertEquals(searchResults.size(), limit, "Incorrect result count");
    checkOrder(searchResults, 1, limit);
    sortBy = "id(desc)";
    limit = 19;
    searchResults = getClientUsr(serviceURL, adminUserJWT).getSystems(searchStr, limit, sortBy, DEFAULT_SKIP, DEFAULT_STARTAFTER, selectStr, false);
    assertEquals(searchResults.size(), limit, "Incorrect result count");
    checkOrder(searchResults, numSystems, numSystems - (limit-1));
    // Sort and check order with limit and skip
    sortBy = "id(asc)";
    limit = 2;
    skip = 5;
    searchResults = getClientUsr(serviceURL, adminUserJWT).getSystems(searchStr, limit, sortBy, skip, DEFAULT_STARTAFTER, selectStr, false);
    assertEquals(searchResults.size(), limit, "Incorrect result count");
    // Should get systems named SrchGet_006 to SrchGet_007
    checkOrder(searchResults, skip + 1, skip + limit);
    sortBy = "id(desc)";
    limit = 4;
    skip = 3;
    searchResults = getClientUsr(serviceURL, adminUserJWT).getSystems(searchStr, limit, sortBy, skip, DEFAULT_STARTAFTER, selectStr, false);
    assertEquals(searchResults.size(), limit, "Incorrect result count");
    // Should get systems named SrchGet_017 to SrchGet_014
    checkOrder(searchResults, numSystems - skip, numSystems - limit);
  }

  /* ********************************************************************** */
  /*                             Private Methods                            */
  /* ********************************************************************** */

  /**
   * Check that results were sorted in correct order when sorting on system name
   */
  private void checkOrder(List<TapisSystem> searchResults, int start, int end)
  {
    int idx = 1; // Position in result
    // Name should match for loop counter i
    if (start < end)
    {
      for (int i = start; i <= end; i++)
      {
        String sysName = getSysId(testKey, i);
        assertEquals(searchResults.get(idx-1).getId(), sysName, "Incorrect system name at position: " + (idx));
        idx++;
      }
    }
    else
    {
      for (int i = start; i >= end; i--)
      {
        String sysName = getSysId(testKey, i);
        assertEquals(searchResults.get(idx-1).getId(), sysName, "Incorrect system name at position: " + (idx));
        idx++;
      }
    }
  }
}
