package edu.utexas.tacc.tapis.systems.client;

import edu.utexas.tacc.tapis.auth.client.AuthClient;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
//import edu.utexas.tacc.tapis.systems.client.gen.model.ReqMatchConstraints;
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
import static edu.utexas.tacc.tapis.systems.client.Utils.escapedCommaInListValue;
import static edu.utexas.tacc.tapis.systems.client.Utils.getClientUsr;
import static edu.utexas.tacc.tapis.systems.client.Utils.testUser1;
import static edu.utexas.tacc.tapis.systems.client.Utils.testUser2;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot1AuthnMethod;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot1TxfrMethodsC;
import static edu.utexas.tacc.tapis.systems.client.Utils.specialChar7Str;
import static org.testng.Assert.assertEquals;

/**
 * TODO Code copied from SearchASTTest. Update to test constraint matching
 * Test the Systems API client acting as a user fetching systems using matchConstraints().
 * 
 * See Utils in this package for information on environment required to run the tests.
 */
@Test(groups={"integration"})
public class MatchConstraintsTest
{
//  // Test data
//  private static final String testKey = "CltMatch";
//  private static final String sysNameLikeAll = sq("%" + testKey + "%");
//
//  private String serviceURL, ownerUser1JWT, ownerUser2JWT, adminUserJWT;
//
//  // Keep numSystems even since some data is modified for half the systems.
//  private final int numSystems = 4;
//  private final Map<Integer, String[]> systems = Utils.makeSystems(numSystems, testKey);
//  private final Map<Integer, ResultSystem> systemsMap = new HashMap<>();
//
//  private LocalDateTime createBegin;
//  private LocalDateTime createEnd;
//
//  @BeforeSuite
//  public void setUp() throws Exception {
//    // Get the base URLs from the environment so the test can be used in environments other than dev
//    System.out.println("****** Executing BeforeSuite setup method for class: " + this.getClass().getSimpleName());
//    // Set service port for systems service. Check for port set as env var
//    // NOTE: This is ignored if TAPIS_ENV_SVC_URL_SYSTEMS is set
//    String servicePort = Utils.getServicePort();
//    // Set base URL for systems service. Check for URL set as env var
//    serviceURL = Utils.getServiceURL(servicePort);
//    // Get base URL suffix from env or from default
//    String baseURL = Utils.getBaseURL();
//    // Log URLs being used
//    System.out.println("Using Systems URL: " + serviceURL);
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
//    // For half the systems change the owner
//    for (int i = numSystems/2 + 1; i <= numSystems; i++) { systems.get(i)[4] = ownerUser2; }
//
//    // For one system update description to have some special characters. 7 special chars in value: ,()~*!\
//    //   and update archiveLocalDir for testing an escaped comma in a list value
//    systems.get(numSystems-1)[2] = specialChar7Str;
//    systems.get(numSystems-1)[11] = escapedCommaInListValue;
//
//    // Create all the systems in the dB using the in-memory objects, recording start and end times
//    createBegin = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
//    Thread.sleep(500);
//    // Check for a system. If it is there assume data is already properly seeded.
//    // This seems like a reasonable approach since there is not a way to clean up (i.e., hard delete
//    // systems and other resources) using the client.
//    ResultSystem tmpSys;
//    try {
//      tmpSys = getClientUsr(serviceURL, ownerUser1JWT).getSystem(systems.get(1)[1]);
//    } catch (TapisClientException e) {
//      assertEquals(e.getCode(), 404);
//      tmpSys = null;
//    }
//    if (tmpSys == null)
//    {
//      System.out.println("Test data not found. Test systems will be created.");
//      // Create systems
//      for (int i = 1; i <= numSystems; i++)
//      {
//        String[] sys0 = systems.get(i);
//        // Vary port # for checking numeric relational searches
//        int port = i;
//        ReqCreateSystem rSys;
//        // Create half the systems owned by ownerUser1 and half by ownerUser2
//        if (i <= numSystems / 2)
//        {
//          rSys = Utils.createReqSystem(sys0, port, prot1AuthnMethod, null, prot1TxfrMethodsC);
//          getClientUsr(serviceURL, ownerUser1JWT).createSystem(rSys);
//          tmpSys = getClientUsr(serviceURL, ownerUser1JWT).getSystem(sys0[1]);
//        }
//        else
//        {
//          rSys = Utils.createReqSystem(sys0, port, prot1AuthnMethod, null, prot1TxfrMethodsC);
//          getClientUsr(serviceURL, ownerUser2JWT).createSystem(rSys);
//          tmpSys = getClientUsr(serviceURL, ownerUser2JWT).getSystem(sys0[1]);
//        }
//        Assert.assertNotNull(tmpSys);
//        systemsMap.put(i, tmpSys);
//      }
//    }
//    else
//    {
//      System.out.println("Test data found. Test systems will not be created.");
//      for (int i = 1; i <= numSystems; i++)
//      {
//        String[] sys0 = systems.get(i);
//        tmpSys = getClientUsr(serviceURL, adminUserJWT).getSystem(sys0[1]);
//        Assert.assertNotNull(tmpSys);
//        systemsMap.put(i, tmpSys);
//      }
//    }
//    Thread.sleep(500);
//    createEnd = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
//  }
//
//  @AfterSuite
//  public void tearDown()
//  {
//    System.out.println("****** Executing AfterSuite teardown method for class: " + this.getClass().getSimpleName());
//    SystemsClient sysClient;
//    // Remove all objects created by tests, ignore any exceptions
//    // This is a soft delete but still should be done to clean up SK artifacts.
//    sysClient = getClientUsr(serviceURL, ownerUser1JWT);
//    for (int i = 1; i <= numSystems / 2; i++)
//    {
//      String systemId = systems.get(i)[1];
//      try { sysClient.deleteSystem(systemId, true); }
//      catch (Exception e) {System.out.println("Caught exception when deleting system: "+systemId+" Exception: "+e);}
//    }
//    sysClient = getClientUsr(serviceURL, ownerUser2JWT);
//    for (int i = (numSystems/2)+1; i <= numSystems;  i++)
//    {
//      String systemId = systems.get(i)[1];
//      try { sysClient.deleteSystem(systemId, true); }
//      catch (Exception e) {System.out.println("Caught exception when deleting system: "+systemId+" Exception: "+e);}
//    }
//  }
//
//  /*
//   * Check valid cases
//   */
//  @Test(groups={"integration"})
//  public void testValidCases() throws Exception
//  {
//    ResultSystem sys0 = systemsMap.get(1);
//    String sys0Name = sys0.getId();
//    String nameList = "noSuchName1,noSuchName2," + sys0Name + ",noSuchName3";
//    // Create all input and validation data for tests
//    // NOTE: Some cases require "name.like." + sysNameLikeAll in the list of conditions since maven runs the tests in
//    //       parallel and not all attribute names are unique across integration tests
//    class CaseData {public final int count; public final String matchStr; CaseData(int c, String s) { count = c; matchStr = s; }}
//    var validCaseInputs = new HashMap<Integer, CaseData>();
//
//    // TODO Test simple match
//    //  Currently matchConstraints is a WIP since it should always return all systems
//    validCaseInputs.put( 1,new CaseData(numSystems, "Scheduler$Type = 'Slurm'")); // TODO: All are Slurm?
//
////    // Test basic types and operators
////    validCaseInputs.put( 1,new CaseData(1, "name = " + sys0Name)); // 1 has specific name
//////    validCaseInputs.put( 2,new CaseData(1, "description = " + sys0.getDescription())); // TODO handle underscore character properly. how?
////    validCaseInputs.put( 3,new CaseData(1, "host = " + sys0.getHost()));
////    validCaseInputs.put( 4,new CaseData(1, "bucket_name = " + sys0.getBucketName()));
//////    validCaseInputs.put( 5,new CaseData(1, "root_dir = " + sys0.getRootDir())); // TODO underscore
////    validCaseInputs.put( 6,new CaseData(1, "job_local_working_dir = " + sys0.getJobLocalWorkingDir()));
////    validCaseInputs.put( 7,new CaseData(1, "job_local_archive_dir = " + sys0.getJobLocalArchiveDir()));
////    validCaseInputs.put( 8,new CaseData(1, "job_remote_archive_system = " + sys0.getJobRemoteArchiveSystem()));
////    validCaseInputs.put( 9,new CaseData(1, "job_remote_archive_dir = " + sys0.getJobRemoteArchiveDir()));
////    validCaseInputs.put(10,new CaseData(numSystems/2, "name LIKE " + sysNameLikeAll + " AND owner = " + sq(ownerUser1)));  // Half owned by one user
////    validCaseInputs.put(11,new CaseData(numSystems/2, "name LIKE " + sysNameLikeAll + " AND owner = " + sq(ownerUser2))); // and half owned by another
////    validCaseInputs.put(12,new CaseData(numSystems, "name LIKE " + sysNameLikeAll + " AND enabled = true"));  // All are enabled
////    validCaseInputs.put(13,new CaseData(numSystems, "name LIKE " + sysNameLikeAll + " AND deleted = false")); // none are deleted
////    validCaseInputs.put(14,new CaseData(numSystems, "name LIKE " + sysNameLikeAll + " AND deleted <> true")); // none are deleted
////    validCaseInputs.put(15,new CaseData(0, "name LIKE " + sysNameLikeAll + " AND deleted = true"));           // none are deleted
////    validCaseInputs.put(16,new CaseData(1, "name LIKE " + sq(sys0Name)));
////    validCaseInputs.put(17,new CaseData(0, "name LIKE 'NOSUCHSYSTEMxFM2c29bc8RpKWeE2sht7aZrJzQf3s'"));
////    validCaseInputs.put(18,new CaseData(numSystems, "name LIKE " + sysNameLikeAll));
//
//    // Iterate over valid cases
//    for (Map.Entry<Integer,CaseData> item : validCaseInputs.entrySet())
//    {
//      CaseData cd = item.getValue();
//      int caseNum = item.getKey();
//      System.out.println("Checking case # " + caseNum + " Input: " + cd.matchStr);
//      // Build request
//      ReqMatchConstraints req = new ReqMatchConstraints();
//      req.addMatchItem(cd.matchStr);
//      // Submit match request
//      List<ResultSystem> matchResults = getClientUsr(serviceURL, adminUserJWT).matchConstraints(req);
//// TODO: re-enable when this is actually implemented
////      assertEquals(matchResults.size(), cd.count);
//    }
//  }
//
//  private static String sq(String s) { return "'" + s + "'"; }
}

