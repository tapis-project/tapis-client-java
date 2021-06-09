package edu.utexas.tacc.tapis.systems.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.auth.client.AuthClient;

import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqUpdateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.TapisSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.SchedulerTypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.SystemTypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.AuthnEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability;
import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;
import edu.utexas.tacc.tapis.systems.client.SystemsClient.AuthnMethod;

import static edu.utexas.tacc.tapis.systems.client.Utils.*;

/**
 * Test the Systems API client acting as a single specific user calling the systems service.
 *
 * NOTE: Because client code stores headers statically cannot mix a user client and
 *       a service client in one program or even 2 user clients. This is because user JWTs cannot have
 *       X-Tapis-User, X-Tapis-Tenant headers set and service JWTs must have those headers set
 *       and there does not appear to be an easy way to unset headers.
 *       So instead have user client tests in one program and service client tests in another.

 * Note: Tests that retrieve credentials must act as a files service client calling the systems service.
 *
 * See Utils in this package for information on environment required to run the tests.
 * 
 */
@Test(groups={"integration"})
public class UserTest
{
  // Test data
  // NOTE: We create a certain number of systems but during refactoring some ended up not being used.
  //       When creating a new test look for an unused system and remove it from this list.
  //  List of unused systems: 6
  //  If list is empty then increment numSystems by 1 and use it.
  int numSystems = 14;
  Map<Integer, String[]> systems = Utils.makeSystems(numSystems, "CltUsr");
  
  private static final String newOwnerUser = testUser3;
  private static final String newPermsUser = testUser4;

  // Create a single static client. Must do it this way because headers are static and JWT is in the header.
  // Updating client dynamically would give false sense of security since tests might be run in parallel and there
  //   would be concurrency issues.
  private static SystemsClient usrClient;
  private static String serviceURL;
  private static String ownerUserJWT;
  private static String newOwnerUserJWT;

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
      ownerUserJWT = authClient.getToken(testUser1, testUser1);
      newOwnerUserJWT = authClient.getToken(newOwnerUser, newOwnerUser);
    } catch (Exception e) {
      throw new Exception("Exception while creating tokens or auth service", e);
    }
    // Basic check of JWTs
    if (StringUtils.isBlank(ownerUserJWT)) throw new Exception("Authn service returned invalid owner user JWT");
    if (StringUtils.isBlank(newOwnerUserJWT)) throw new Exception("Authn service returned invalid new owner user JWT");

    // Cleanup anything leftover from previous failed run
    tearDown();

    // Create user client
    usrClient = getClientUsr(serviceURL, ownerUserJWT);

  }

  @AfterSuite
  public void tearDown() {
    System.out.println("****** Executing AfterSuite teardown method for class: " + this.getClass().getSimpleName());
    usrClient = getClientUsr(serviceURL, ownerUserJWT);
    // Remove all objects created by tests, ignore any exceptions
    // This is a soft delete but still should be done to clean up SK artifacts.
    for (int i = 1; i <= numSystems; i++)
    {
      String systemId = systems.get(i)[1];
      try
      {
        usrClient.deleteSystem(systemId);
      }
      catch (Exception e)
      {
        System.out.println("Caught exception when soft deleting system: "+ systemId + " Exception: " + e);
      }
    }
    // One system may have had owner changed so use new owner.
    String systemId = systems.get(9)[1];
    usrClient = getClientUsr(serviceURL, newOwnerUserJWT);
    try
    {
      usrClient.deleteSystem(systemId);
    }
    catch (Exception e)
    {
      System.out.println("Caught exception when deleting system: "+ systemId + " Exception: " + e);
    }
    usrClient = getClientUsr(serviceURL, ownerUserJWT);
  }

  @Test
  public void testHealthAndReady() {
    try {
      System.out.println("Checking health status");
      String status = usrClient.checkHealth();
      System.out.println("Health status: " + status);
      Assert.assertNotNull(status);
      Assert.assertFalse(StringUtils.isBlank(status), "Invalid response: " + status);
      Assert.assertEquals(status, "success", "Service failed health check");
      System.out.println("Checking ready status");
      status = usrClient.checkReady();
      System.out.println("Ready status: " + status);
      Assert.assertNotNull(status);
      Assert.assertFalse(StringUtils.isBlank(status), "Invalid response: " + status);
      Assert.assertEquals(status, "success", "Service failed ready check");
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }

  @Test
  public void testCreateSystem() {
    // Create a system
    String[] sys0 = systems.get(1);
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl =
              usrClient.createSystem(createReqSystem(sys0, prot1Port, prot1AuthnMethod, credNull));
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }

  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = "^SYSAPI_SYS_EXISTS.*")
  public void testCreateSystemAlreadyExists() throws Exception {
    // Create a system
    String[] sys0 = systems.get(3);
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl =
              usrClient.createSystem(createReqSystem(sys0, prot1Port, prot1AuthnMethod, credNull));
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
    // Now attempt to create it again, should throw exception
    System.out.println("Creating system with name: " + sys0[1]);
    usrClient.createSystem(createReqSystem(sys0, prot1Port, prot1AuthnMethod, credNull));
    Assert.fail("Exception should have been thrown");
  }

  // Test various restrictions on system attributes at system creation time
  public void testSystemCreateRestrictions() {
    String[] sys0 = systems.get(4);
    ReqCreateSystem rSys = createReqSystem(sys0, prot1Port, prot1AuthnMethod, null);

    // Attempt to create an S3 system with no bucketName
    boolean pass = false;
    System.out.println("Attempting to create S3 system with no bucketName. System name: " + sys0[1]);
    rSys.setSystemType(SystemTypeEnum.S3);
    rSys.setBucketName(null);
    rSys.setCanExec(false);
    try { usrClient.createSystem(rSys); }
    catch (TapisClientException tce)
    {
      System.out.println("Caught exception: " + tce.getMessage());
      Assert.assertTrue(tce.getMessage().contains("SYSLIB_OBJSTORE_NOBUCKET_INPUT"));
      pass = true;
    }
    Assert.assertTrue(pass, "Should not be able to create system with S3 and no bucketName");

    // Attempt to create an S3 system with canExec=true
    rSys.setSystemType(SystemTypeEnum.S3);
    rSys.setBucketName(sys0[8]);
    rSys.setCanExec(true);
    pass = false;
    System.out.println("Attempting to create system of type S3 with canExec=true. System name: " + sys0[1]);
    try { usrClient.createSystem(rSys); }
    catch (TapisClientException tce)
    {
      System.out.println("Caught exception: " + tce.getMessage());
      Assert.assertTrue(tce.getMessage().contains("SYSLIB_OBJSTORE_CANEXEC_INPUT"));
      pass = true;
    }
    Assert.assertTrue(pass, "Should not be able to create system of type S3 with canExec=true");
    rSys.setSystemType(SystemTypeEnum.LINUX);

    // Test that authn method of CERT and static owner is not allowed
    pass = false;
    System.out.println("Attempting to create system with authnMethod=CERT and static owner. System name: " + sys0[1]);
    try
    {
      usrClient.createSystem(createReqSystem(sys0, prot1Port, AuthnMethod.CERT, credNull));
    }
    catch (TapisClientException tce)
    {
      System.out.println("Caught exception: " + tce.getMessage());
      Assert.assertTrue(tce.getMessage().contains("SYSLIB_INVALID_EFFECTIVEUSERID_INPUT"));
      pass = true;
    }
    Assert.assertTrue(pass, "Should not be able to create system authnMethod=CERT and static owner");

    // Test that providing credentials for dynamic effective user is not allowed
    pass = false;
    System.out.println("Attempting to create system with credentials and apiUserId. System name: " + sys0[1]);
    String tmpEffUser = sys0[6];
    sys0[6] = "${apiUserId}";
    Credential cred0 = SystemsClient.buildCredential(sys0[7], "fakePrivateKey", "fakePublicKey",
                                           "fakeAccessKey", "fakeAccessSecret", "fakeCert");
    try
    {
      usrClient.createSystem(createReqSystem(sys0, prot1Port, prot1AuthnMethod, cred0));
    }
    catch (TapisClientException tce)
    {
      System.out.println("Caught exception: " + tce.getMessage());
      Assert.assertTrue(tce.getMessage().contains("SYSLIB_CRED_DISALLOWED_INPUT"));
      pass = true;
    }
    Assert.assertTrue(pass, "Should not be able to create system with credentials and apiUserId");
    sys0[6] = tmpEffUser;

    // Attempt to create a system with with canExec = true and no jobWorkingDir
    pass = false;
    System.out.println("Attempting to create system with canExec=true and no jobWorkingDir. System name: " + sys0[1]);
    rSys.setJobWorkingDir(null);
    try { usrClient.createSystem(rSys); }
    catch (TapisClientException tce)
    {
      System.out.println("Caught exception: " + tce.getMessage());
      Assert.assertTrue(tce.getMessage().contains("SYSLIB_CANEXEC_NO_JOBWORKINGDIR_INPUT"));
      pass = true;
    }
    Assert.assertTrue(pass, "Should not be able to create system with canExec=true and no jobWorkingDir");

    // Attempt to create a system with with canExec = true and null jobRuntimes
    pass = false;
    System.out.println("Attempting to create system with canExec=true and no jobRuntimes. System name: " + sys0[1]);
    rSys.setJobWorkingDir(sys0[10]);
    rSys.setJobRuntimes(null);
    try { usrClient.createSystem(rSys); }
    catch (TapisClientException tce)
    {
      System.out.println("Caught exception: " + tce.getMessage());
      Assert.assertTrue(tce.getMessage().contains("SYSLIB_CANEXEC_NO_JOBRUNTIME_INPUT"));
      pass = true;
    }
    Assert.assertTrue(pass, "Should not be able to create system with canExec=true and no jobRuntimes");

    // Attempt to create a system with with canExec = true and empty jobRuntimes
    // Jsonschema check should reject
    pass = false;
    System.out.println("Attempting to create system with canExec=true and no jobRuntimes. System name: " + sys0[1]);
    rSys.setJobRuntimes(jobRuntimesEmpty);
    try { usrClient.createSystem(rSys); }
    catch (TapisClientException tce)
    {
      System.out.println("Caught exception: " + tce.getMessage());
//      Assert.assertTrue(tce.getMessage().contains("SYSAPI_CANEXEC_NO_JOBRUNTIME_INPUT"));
      Assert.assertTrue(tce.getMessage().contains("TAPIS_JSON_VALIDATION_FAILURE"));
      pass = true;
    }
    Assert.assertTrue(pass, "Should not be able to create system with canExec=true and no jobRuntimes");
  }

  // Test retrieving a system including default authn method
  //   and test retrieving for specified authn method.
  @Test
  public void testGetSystem() throws Exception {
    String[] sys0 = systems.get(7);
    String respUrl =
            usrClient.createSystem(createReqSystem(sys0, prot1Port, AuthnMethod.PKI_KEYS, credNull));
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    TapisSystem tmpSys = usrClient.getSystem(sys0[1]);
    Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
    System.out.println("Found item: " + sys0[1]);
    verifySystemAttributes(tmpSys, sys0);
  }

  // Create a system using minimal attributes for LINUX:
  //   id, systemType, host, defaultAuthnMethod, rootDir, canExec=false
  // Confirm that defaults are as expected
  @Test
  public void testCreateAndGetSystemMinimal() throws Exception
  {
    // Create a system
    String[] sys0 = systems.get(2);
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = Utils.createSystemMinimal(usrClient, sys0);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
    // Get the system and check the defaults
    TapisSystem tmpSys = usrClient.getSystem(sys0[1]);
    Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
    System.out.println("Found item: " + sys0[1]);
    Utils.verifySystemDefaults(tmpSys, sys0, sys0[1]);
  }

  // Create a system using minimal attributes, get the system, use modified result to create new system.
  // Confirm that defaults are as expected
  @Test
  public void testMinimalCreateGetCreate() throws Exception
  {
    // Create a LINUX system using minimal attributes
    String[] sys0 = systems.get(5);
    String newId = sys0[1] + "new";
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = Utils.createSystemMinimal(usrClient, sys0);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }

    // Get the system and check the defaults
    TapisSystem tmpSys = usrClient.getSystem(sys0[1]);
    Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
    System.out.println("Found item: " + sys0[1]);
    Utils.verifySystemDefaults(tmpSys, sys0, sys0[1]);

    // Modify result and create a new system
    tmpSys.setId(newId);
    try {
      String respUrl = Utils.createSystemMinimal2(usrClient, tmpSys);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
    // Get the system and check the defaults
    tmpSys = usrClient.getSystem(tmpSys.getId());
    Assert.assertNotNull(tmpSys, "Failed to create item: " + newId);
    System.out.println("Found item: " + newId);
    Utils.verifySystemDefaults(tmpSys, sys0, newId);
  }

  @Test
  public void testUpdateSystem() {
    String[] sys0 = systems.get(8);
//    private static final String[] sysF2 = {tenantName, "CsysF", "description PATCHED", sysType, ownerUser, "hostPATCHED", "effUserPATCHED",
//            "fakePasswordF", "bucketF", "/rootF", "jobLocalWorkDirF", "jobLocalArchDirF", "jobRemoteArchSystemF", "jobRemoteArchDirF"};
    String[] sysF2 = sys0.clone();
    sysF2[2] = "description PATCHED"; sysF2[5] = hostPatchedId; sysF2[6] = "effUserPATCHED";
    ReqUpdateSystem rSystem = createPatchSystem(sysF2);
    System.out.println("Creating and updating system with name: " + sys0[1]);
    try {
      // Create a system
      String respUrl =
              usrClient.createSystem(createReqSystem(sys0, prot1Port, prot1AuthnMethod, credNull));
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Update the system
      respUrl = usrClient.updateSystem(sys0[1], rSystem);
      System.out.println("Updated system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Verify attributes
      sys0 = sysF2;
      TapisSystem tmpSys = usrClient.getSystem(sys0[1]);
      Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
      System.out.println("Found item: " + sys0[1]);
      Assert.assertEquals(tmpSys.getId(), sys0[1]);
      Assert.assertEquals(tmpSys.getDescription(), sys0[2]);
      Assert.assertNotNull(tmpSys.getSystemType());
      Assert.assertEquals(tmpSys.getSystemType().name(), sys0[3]);
      Assert.assertEquals(tmpSys.getOwner(), sys0[4]);
      Assert.assertEquals(tmpSys.getHost(), sys0[5]);
      Assert.assertEquals(tmpSys.getEffectiveUserId(), sys0[6]);
      Assert.assertEquals(tmpSys.getBucketName(), sys0[8]);
      Assert.assertEquals(tmpSys.getRootDir(), sys0[9]);
      Assert.assertEquals(tmpSys.getJobWorkingDir(), sys0[10]);
      Assert.assertEquals(tmpSys.getBatchScheduler(), SchedulerTypeEnum.valueOf(sys0[11]));
      Assert.assertEquals(tmpSys.getBatchDefaultLogicalQueue(), sys0[12]);
// TODO logical queues?      Assert.assertEquals(tmpSys.getJobRemoteArchiveDir(), sys0[13]);
      Assert.assertNotNull(tmpSys.getPort());
      Assert.assertEquals(tmpSys.getPort().intValue(), prot2Port);
      Assert.assertNotNull(tmpSys.getUseProxy());
      Assert.assertEquals(tmpSys.getUseProxy().booleanValue(), prot2UseProxy);
      Assert.assertEquals(tmpSys.getProxyHost(), prot2ProxyHost);
      Assert.assertNotNull(tmpSys.getProxyPort());
      Assert.assertEquals(tmpSys.getProxyPort().intValue(), prot2ProxyPort);
      Assert.assertNotNull(tmpSys.getDefaultAuthnMethod());
      Assert.assertEquals(tmpSys.getDefaultAuthnMethod().name(), prot2AuthnMethod.name());
      // Verify capabilities
      List<Capability> jobCaps = tmpSys.getJobCapabilities();
      Assert.assertNotNull(jobCaps);
      Assert.assertEquals(jobCaps.size(), jobCaps2.size());
      var capNamesFound = new ArrayList<String>();
      for (Capability capFound : jobCaps) {capNamesFound.add(capFound.getName());}
      for (Capability capSeed : jobCaps2)
      {
        Assert.assertTrue(capNamesFound.contains(capSeed.getName()), "List of capabilities did not contain a capability named: " + capSeed.getName());
      }
      // Verify tags
      List<String> tmpTags = tmpSys.getTags();
      Assert.assertNotNull(tmpTags, "Tags value was null");
      Assert.assertEquals(tmpTags.size(), tags2.size(), "Wrong number of tags");
      for (String tagStr : tags2)
      {
        Assert.assertTrue(tmpTags.contains(tagStr), "List of tags did not contain a tag named: " + tagStr);
        System.out.println("Found tag: " + tagStr);
      }
      // Verify notes
      String tmpNotesStr = (String) tmpSys.getNotes();
      System.out.println("Found notes: " + tmpNotesStr);
      JsonObject tmpNotes = ClientTapisGsonUtils.getGson().fromJson(tmpNotesStr, JsonObject.class);
      Assert.assertNotNull(tmpNotes);
      System.out.println("Found notes: " + tmpNotesStr);
      Assert.assertTrue(tmpNotes.has("project"), "Notes json did not contain project");
      Assert.assertEquals(tmpNotes.get("project").getAsString(), notes2JO.get("project").getAsString());
      Assert.assertTrue(tmpNotes.has("testdata"), "Notes json did not contain testdata");
      Assert.assertEquals(tmpNotes.get("testdata").getAsString(), notes2JO.get("testdata").getAsString());
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }

  /**
   * Check that as a user we can give away ownership. Since we can have only one client (i.e. one user) then all we
   * can do after giving away ownership is check that we can no longer modify the system
   */
  @Test
  public void testChangeOwner() throws Exception {
    // Create the system
    String[] sys0 = systems.get(9);
    String respUrl =
            usrClient.createSystem(createReqSystem(sys0, prot1Port, prot1AuthnMethod, null));
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    TapisSystem tmpSys = usrClient.getSystem(sys0[1]);
    Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
    usrClient.changeSystemOwner(sys0[1], newOwnerUser);
    // Now that owner has given away ownership they should no longer be able to modify or read.
    try {
      usrClient.deleteSystem(sys0[1]);
      Assert.fail("Original owner should not have permission to update system after change of ownership. System name: " +
                  sys0[1] + " Old owner: " + testUser1 + " New Owner: " + newOwnerUser);
    } catch (TapisClientException e) {
      Assert.assertTrue(e.getMessage().contains("SYSLIB_UNAUTH"));
    }
    // TODO figure out why this fails
    //      passes manually, auth denied when manually attempting to retrieve as testuser2 when system owned by testuser3
//    try {
//      usrClient.getSystem(sys0[1]);
//      Assert.fail("Original owner should not have permission to read system after change of ownership. System name: " +
//              sys0[1] + " Old owner: " + ownerUser1 + " New Owner: " + newOwnerUser);
//    } catch (TapisClientException e) {
//      Assert.assertTrue(e.getMessage().contains("SYSLIB_UNAUTH"));
//    }
  }

  @Test
  public void testGetSystems() throws Exception {
    // Create 2 systems
    String[] sys1 = systems.get(10);
    String respUrl =
            usrClient.createSystem(createReqSystem(sys1, prot1Port, prot1AuthnMethod, credNull));
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    String[] sys2 = systems.get(11);
    respUrl =
            usrClient.createSystem(createReqSystem(sys2, prot1Port, prot1AuthnMethod, credNull));
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Get list of all systems
    List<TapisSystem> systemsList = usrClient.getSystems(null, -1, null, -1, null, null, false);
    Assert.assertNotNull(systemsList);
    Assert.assertFalse(systemsList.isEmpty());
    var systemNames = new ArrayList<String>();
    for (TapisSystem system : systemsList) {
      System.out.println("Found item: " + system.getId());
      systemNames.add(system.getId());
    }
    TapisSystem tmpSys = usrClient.getSystem(sys1[1]);
    verifySystemAttributes(tmpSys, sys1);
    tmpSys = usrClient.getSystem(sys2[1]);
    verifySystemAttributes(tmpSys, sys2);
    Assert.assertTrue(systemNames.contains(systems.get(10)[1]), "List of systems did not contain system name: " + systems.get(10)[1]);
    Assert.assertTrue(systemNames.contains(systems.get(11)[1]), "List of systems did not contain system name: " + systems.get(11)[1]);
  }

  @Test
  public void testEnableDisable() throws Exception
  {
    String[] sys0 = systems.get(14);
    String sysId = sys0[1];
    String respUrl =
            usrClient.createSystem(createReqSystem(sys0, prot1Port, prot1AuthnMethod, credNull));
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    // Enabled should start off true, then become false and finally true again.
    TapisSystem tmpSys = usrClient.getSystem(sysId);
    Assert.assertNotNull(tmpSys);
    Assert.assertSame(Boolean.TRUE, tmpSys.getEnabled());
    Assert.assertTrue(usrClient.isEnabled(sysId));

    int changeCount = usrClient.disableSystem(sysId);
    Assert.assertEquals(changeCount, 1);
    tmpSys = usrClient.getSystem(sysId);
    Assert.assertNotSame(Boolean.TRUE, tmpSys.getEnabled());
    Assert.assertFalse(usrClient.isEnabled(sysId));

    changeCount = usrClient.enableSystem(sysId);
    Assert.assertEquals(changeCount, 1);
    tmpSys = usrClient.getSystem(sysId);
    Assert.assertSame(Boolean.TRUE, tmpSys.getEnabled());
    Assert.assertTrue(usrClient.isEnabled(sysId));
  }

  @Test
  public void testDelete() throws Exception {
    // Create the system
    String[] sys0 = systems.get(12);
    String respUrl =
            usrClient.createSystem(createReqSystem(sys0, prot1Port, prot1AuthnMethod, credNull));
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Delete the system
    usrClient.deleteSystem(sys0[1]);
    try {
      usrClient.getSystem(sys0[1]);
      Assert.fail("System not deleted. System name: " + sys0[1]);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
    }
  }

  // Test creating, reading and deleting user permissions for a system
  @Test
  public void testUserPerms() {
    String[] sys0 = systems.get(13);
    // Create a system
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl =
              usrClient.createSystem(createReqSystem(sys0, prot1Port, prot1AuthnMethod, credNull));
      System.out.println("Created system: " + respUrl);
      System.out.println("Testing perms for user: " + newPermsUser);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Create user perms for the system
      usrClient.grantUserPermissions(sys0[1], newPermsUser, testPerms);
      // Get the system perms for the user and make sure permissions are there
      List<String> userPerms = usrClient.getSystemPermissions(sys0[1], newPermsUser);
      Assert.assertNotNull(userPerms, "Null returned when retrieving perms.");
      for (String perm : userPerms) {
        System.out.println("After grant found user perm: " + perm);
      }
      Assert.assertEquals(userPerms.size(), testPerms.size(), "Incorrect number of perms returned.");
      for (String perm : testPerms) {
        if (!userPerms.contains(perm)) Assert.fail("User perms should contain permission: " + perm);
      }
      // Remove perms for the user
      usrClient.revokeUserPermissions(sys0[1], newPermsUser, testPerms);
      // Get the system perms for the user and make sure permissions are gone.
      userPerms = usrClient.getSystemPermissions(sys0[1], newPermsUser);
      Assert.assertNotNull(userPerms, "Null returned when retrieving perms.");
      for (String perm : userPerms) {
        System.out.println("After revoke found user perm: " + perm);
      }
      for (String perm : testPerms) {
        if (userPerms.contains(perm)) Assert.fail("User perms should not contain permission: " + perm);
      }
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  // Test various cases when system is missing
  //  - get system, isEnabled, enable/disable, delete/undelete, changeOwner
  //  - get perms, grant perms, revoke perms
  // NOTE: Credential calls are not checked because they are not allowed for users
  @Test
  public void testMissingSystem()
  {
    String fakeSystemName = "AMissingSystemName";
    String fakeUserName = "AMissingUserName";

    boolean pass = false;

    // Get system
    try {
      usrClient.getSystem(fakeSystemName);
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // isEnabled
    pass = false;
    try {
      usrClient.isEnabled(fakeSystemName);
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Enable system
    pass = false;
    try {
      usrClient.enableSystem(fakeSystemName);
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Disable system
    pass = false;
    try {
      usrClient.disableSystem(fakeSystemName);
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Delete system
    pass = false;
    try {
      usrClient.deleteSystem(fakeSystemName);
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Undelete system
    pass = false;
    try {
      usrClient.undeleteSystem(fakeSystemName);
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Change Owner
    pass = false;
    try {
      usrClient.changeSystemOwner(fakeSystemName, newOwnerUser);
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Get Perms
    pass = false;
    try {
      usrClient.getSystemPermissions(fakeSystemName, testUser1);
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Grant Perms
    pass = false;
    try {
      usrClient.grantUserPermissions(fakeSystemName, fakeUserName, testPerms);
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Revoke Perms
    pass = false;
    try {
      usrClient.revokeUserPermissions(fakeSystemName, fakeUserName, testPerms);
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Revoke Perm
    pass = false;
    try {
      usrClient.revokeUserPermission(fakeSystemName, fakeUserName, testREADPerm.get(0));
      Assert.fail("Missing system did not throw exception. System name: " + fakeSystemName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);
  }


  // =====================================================================
  // =========  Private methods ==========================================
  // =====================================================================

  private static ReqUpdateSystem createPatchSystem(String[] sys)
  {
    ReqUpdateSystem pSys = new ReqUpdateSystem();
    pSys.description(sys[2]);
    pSys.host(sys[5]);
    pSys.effectiveUserId(sys[6]);
    pSys.defaultAuthnMethod(AuthnEnum.valueOf(prot2AuthnMethod.name()));
    pSys.port(prot2Port).useProxy(prot2UseProxy).proxyHost(prot2ProxyHost).proxyPort(prot2ProxyPort);
    pSys.jobCapabilities(jobCaps2);
    pSys.tags(tags2);
    pSys.notes(notes2JO);
    return pSys;
  }
}
