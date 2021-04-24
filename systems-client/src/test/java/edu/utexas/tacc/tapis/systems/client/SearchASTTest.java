package edu.utexas.tacc.tapis.systems.client;

import edu.utexas.tacc.tapis.auth.client.AuthClient;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqSearchSystems;
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

import static edu.utexas.tacc.tapis.systems.client.Utils.adminUser;
import static edu.utexas.tacc.tapis.systems.client.Utils.getClientUsr;
import static edu.utexas.tacc.tapis.systems.client.Utils.testUser1;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot1AuthnMethod;

import static edu.utexas.tacc.tapis.systems.client.Utils.*;
import static org.testng.Assert.assertEquals;

/**
 * Test the Systems API client acting as a user fetching systems using searchSystems() with search conditions.
 * 
 * See Utils in this package for information on environment required to run the tests.
 */
@Test(groups={"integration"})
public class SearchASTTest
{
  // Test data
  private static final String testKey = "CltSrchAst";
  private static final String sysNameLikeAll = "id LIKE '%" + testKey + "%'";

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
    } catch (Exception e) {
      throw new Exception("Exception while creating tokens or auth service", e);
    }
    // Basic check of JWTs
    if (StringUtils.isBlank(testUser1JWT)) throw new Exception("Authn service returned invalid owner user1 JWT");
    if (StringUtils.isBlank(testUser2JWT)) throw new Exception("Authn service returned invalid owner user2 JWT");
    if (StringUtils.isBlank(adminUserJWT)) throw new Exception("Authn service returned invalid admin user JWT");

    // Cleanup anything leftover from previous failed run
    tearDown();

//    String[] tenantName = 0, id = 1, "description " + suffix = 2, sysType = 3, testUser = 4, "host"+suffix = 5,
//             "effUser"+suffix = 6, "fakePassword"+suffix = 7,"bucket"+suffix = 8, "/root"+suffix = 9,
//             "jobLocalWorkDir"+suffix = 10, "jobLocalArchDir"+suffix = 11,
//            "jobRemoteArchSystem"+suffix = 12, "jobRemoteArchDir"+suffix = 13};

    // For half the systems change the owner
    for (int i = numSystems/2 + 1; i <= numSystems; i++) { systems.get(i)[4] = testUser2; }

    // For one system update description to have some special characters. 7 special chars in value: ,()~*!\
    //   and update archiveLocalDir for testing an escaped comma in a list value
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
      assertEquals(e.getCode(), 404);
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
        ReqCreateSystem rSys;
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
//    SystemsClient sysClient = getClientUsr(serviceURL, testUser1JWT);
//    for (int i = 1; i <= numSystems / 2; i++)
//    {
//      String systemId = systems.get(i)[1];
//      try { sysClient.deleteSystem(systemId, true); }
//      catch (Exception e) {System.out.println("Caught exception when deleting system: "+systemId+" Exception: "+e);}
//    }
//    sysClient = getClientUsr(serviceURL, testUser2JWT);
//    for (int i = (numSystems/2)+1; i <= numSystems;  i++)
//    {
//      String systemId = systems.get(i)[1];
//      try { sysClient.deleteSystem(systemId, true); }
//      catch (Exception e) {System.out.println("Caught exception when deleting system: "+systemId+" Exception: "+e);}
//    }
  }

  /*
   * Check valid cases
   */
  @Test(groups={"integration"})
  public void testValidCases() throws Exception
  {
    TapisSystem sys0 = systemsMap.get(1);
    String sys0Name = sys0.getId();
    String nameList = String.format("('noSuchName1','noSuchName2','%s','noSuchName3')",sys0Name);
    // Create all input and validation data for tests
    // NOTE: Some cases require "name.like." + sysNameLikeAll in the list of conditions since maven runs the tests in
    //       parallel and not all attribute names are unique across integration tests
    class CaseData {public final int count; public final String searchStr; CaseData(int c, String s) { count = c; searchStr = s; }}
    var validCaseInputs = new HashMap<Integer, CaseData>();
    // Test basic types and operators
    // TODO/TBD: Must surround values in single quotes for some cases, e.g. when _ or . is present in value.
    validCaseInputs.put( 1,new CaseData(1, String.format("id = '%s'",sys0Name))); // 1 has specific name
    validCaseInputs.put( 2,new CaseData(1, String.format("description = '%s'",sys0.getDescription())));
    validCaseInputs.put( 3,new CaseData(1, String.format("host = '%s'",sys0.getHost())));
    validCaseInputs.put( 4,new CaseData(1, String.format("bucket_name = '%s'",sys0.getBucketName())));
    validCaseInputs.put( 5,new CaseData(1, String.format("root_dir = '%s'", sys0.getRootDir()))); //TODO underscore
    validCaseInputs.put( 6,new CaseData(1, String.format("job_working_dir = '%s'",sys0.getJobWorkingDir())));
    validCaseInputs.put( 7,new CaseData(numSystems, sysNameLikeAll + " AND batch_scheduler = " + SchedulerTypeEnum.SLURM.name()));
    validCaseInputs.put( 8,new CaseData(1, String.format("batch_default_logical_queue = '%s'",sys0.getBatchDefaultLogicalQueue())));
    validCaseInputs.put(10,new CaseData(numSystems/2, sysNameLikeAll + String.format(" AND owner = '%s'",testUser1)));  // Half owned by one user
    validCaseInputs.put(11,new CaseData(numSystems/2, sysNameLikeAll + String.format(" AND owner = '%s'",testUser2))); // and half owned by another
    validCaseInputs.put(12,new CaseData(numSystems, sysNameLikeAll + " AND enabled = true"));  // All are enabled
    validCaseInputs.put(13,new CaseData(numSystems, sysNameLikeAll + " AND deleted = false")); // none are deleted
    validCaseInputs.put(14,new CaseData(numSystems, sysNameLikeAll + " AND deleted <> true")); // none are deleted
    validCaseInputs.put(15,new CaseData(0, sysNameLikeAll + " AND deleted = true"));           // none are deleted
    validCaseInputs.put(16,new CaseData(1, String.format("id LIKE '%s'",sys0Name)));
    validCaseInputs.put(17,new CaseData(0, "id LIKE 'NOSUCHSYSTEMxFM2c29bc8RpKWeE2sht7aZrJzQf3s'"));
    validCaseInputs.put(18,new CaseData(numSystems, sysNameLikeAll));
//    validCaseInputs.put(19,new CaseData(numSystems-1, sysNameLikeAll + String.format(" AND id NLIKE '%s'",sys0Name))); // TODO support NLIKE
    validCaseInputs.put(20,new CaseData(1, sysNameLikeAll + " AND id IN " + nameList));
//    validCaseInputs.put(21,new CaseData(numSystems-1, sysNameLikeAll + " AND id NIN " + nameList)); // TODO support NIN
    validCaseInputs.put(22,new CaseData(numSystems, sysNameLikeAll + " AND system_type = LINUX"));
    validCaseInputs.put(23,new CaseData(numSystems/2, sysNameLikeAll + String.format(" AND system_type = LINUX AND owner <> '%s'",testUser2)));

    // Iterate over valid cases
    for (Map.Entry<Integer,CaseData> item : validCaseInputs.entrySet())
    {
      CaseData cd = item.getValue();
      int caseNum = item.getKey();
      System.out.println("Checking case # " + caseNum + " Input: " + cd.searchStr);
      // Build request
      ReqSearchSystems req = new ReqSearchSystems();
      req.addSearchItem(cd.searchStr);
      // Submit search request
      List<TapisSystem> searchResults = getClientUsr(serviceURL, adminUserJWT).searchSystems(req);
      assertEquals(searchResults.size(), cd.count);
    }
  }
}

