package edu.utexas.tacc.tapis.systems.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqUpdateSystem;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability;
import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem;
import edu.utexas.tacc.tapis.systems.client.SystemsClient.AccessMethod;
import edu.utexas.tacc.tapis.auth.client.AuthClient;

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
 * See IntegrationUtils in this package for information on environment required to run the tests.
 * 
 */
@Test(groups={"integration"})
public class UserTest
{
  // Test data
  int numSystems = 13;
  Map<Integer, String[]> systems = Utils.makeSystems(numSystems, "CltUsr");
  
  private static final String newOwnerUser = testUser3;
  private static final String newPermsUser = testUser4;

  // Create a single static client. Must do it this way because headers are static and JWT is in the header.
  // Updating client dynamically would give false sense of security since tests might be run in parallel and there
  //   would be concurrency issues.
  private static SystemsClient usrClient;

  @BeforeSuite
  public void setUp() throws Exception {
    // Get the base URLs from the environment so the test can be used in environments other than dev
    System.out.println("****** Executing BeforeSuite setup method for class: " + this.getClass().getSimpleName());
    // Set service port for systems service. Check for port set as env var
    // NOTE: This is ignored if TAPIS_ENV_SVC_URL_SYSTEMS is set
    String servicePort = Utils.getServicePort();
    // Set base URL for systems service. Check for URL set as env var
    String serviceURL = Utils.getServiceURL(servicePort);
    // Get base URL suffix from env or from default
    String baseURL = Utils.getBaseURL();
    // Log URLs being used
    System.out.println("Using Systems URL: " + serviceURL);
    System.out.println("Using Authenticator URL: " + baseURL);
    System.out.println("Using Tokens URL: " + baseURL);
    // Get short term user JWT from tokens service
    var authClient = new AuthClient(baseURL);
    String ownerUserJWT;
    String newOwnerUserJWT;
    try {
      ownerUserJWT = authClient.getToken(ownerUser1, ownerUser1);
      newOwnerUserJWT = authClient.getToken(newOwnerUser, newOwnerUser);
    } catch (Exception e) {
      throw new Exception("Exception while creating tokens or auth service", e);
    }
    // Basic check of JWTs
    if (StringUtils.isBlank(ownerUserJWT)) throw new Exception("Authn service returned invalid owner user JWT");
    if (StringUtils.isBlank(newOwnerUserJWT)) throw new Exception("Authn service returned invalid new owner user JWT");

    // Create user clients
    usrClient = getClientUsr(serviceURL, ownerUserJWT);

    // Cleanup anything leftover from previous failed run
    tearDown();
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
    Credential cred0 = null;
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }

  // Create a system using minimal attributes:
  //   name, systemType, host, defaultAccessMethod, jobCanExec
  @Test
  public void testCreateSystemMinimal()
  {
    // Create a system
    String[] sys0 = systems.get(2);
    System.out.println("Creating system with name: " + sys0[1]);
    // Set optional attributes to null
//    private static final String[] sysE = {tenantName, "CsysE", null, sysType, null, "hostE", null, null,
//            null, null, null, null, null, null};
    sys0[2] = null; sys0[4] = null; sys0[6] = null; sys0[7] = null; sys0[9] = null;
    sys0[10] = null; sys0[11] = null; sys0[12] = null; sys0[13] = null;

    try {
      String respUrl = Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, null, null);
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
    Credential cred0 = null;
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
    // Now attempt to create it again, should throw exception
    System.out.println("Creating system with name: " + sys0[1]);
    Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
    Assert.fail("Exception should have been thrown");
  }

  // Test that bucketName is required if transfer methods include S3
  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = ".*SYSAPI_S3_NOBUCKET_INPUT.*")
  public void testCreateSystemS3NoBucketName() throws Exception {
    // Create a system
    String[] sys0 = systems.get(4);
    // Set bucketName to empty string
    sys0[8] = "";
    Credential cred0 = null;
    System.out.println("Creating system with name: " + sys0[1]);
    Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
    Assert.fail("Exception should have been thrown");
  }

  // Test that access method of CERT and static owner is not allowed
  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = ".*SYSAPI_INVALID_EFFECTIVEUSERID_INPUT.*")
  public void testCreateSystemInvalidEffUserId() throws Exception {
    // Create a system
    String[] sys0 = systems.get(5);
    Credential cred0 = null;
    System.out.println("Creating system with name: " + sys0[1]);
    Utils.createSystem(usrClient, sys0, prot1Port, AccessMethod.CERT, cred0, prot1TxfrMethodsC);
    Assert.fail("Exception should have been thrown");
  }

  // Test that providing credentials for dynamic effective user is not allowed
  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = ".*SYSAPI_CRED_DISALLOWED_INPUT.*")
  public void testCreateSystemCredDisallowed() throws Exception {
    // Create a system
    String[] sys0 = systems.get(6);
    // Set effectiveUserId to api user
    sys0[6] = "${apiUserId}";
    Credential cred0 = SystemsClient.buildCredential(sys0[7], "fakePrivateKey", "fakePublicKey",
                                           "fakeAccessKey", "fakeAccessSecret", "fakeCert");
    System.out.println("Creating system with name: " + sys0[1]);
    Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
    Assert.fail("Exception should have been thrown");
  }

  // Test retrieving a system including default access method
  //   and test retrieving for specified access method.
  @Test
  public void testGetSystemByName() throws Exception {
    String[] sys0 = systems.get(7);
    Credential cred0 = null;
    String respUrl = Utils.createSystem(usrClient, sys0, prot1Port, AccessMethod.PKI_KEYS, cred0, prot1TxfrMethodsC);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    TSystem tmpSys = usrClient.getSystemByName(sys0[1]);
    Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
    System.out.println("Found item: " + sys0[1]);
    Assert.assertEquals(tmpSys.getName(), sys0[1]);
    Assert.assertEquals(tmpSys.getDescription(), sys0[2]);
    Assert.assertEquals(tmpSys.getSystemType().name(), sys0[3]);
    Assert.assertEquals(tmpSys.getOwner(), sys0[4]);
    Assert.assertEquals(tmpSys.getHost(), sys0[5]);
    Assert.assertEquals(tmpSys.getEffectiveUserId(), sys0[6]);
    Assert.assertEquals(tmpSys.getBucketName(), sys0[8]);
    Assert.assertEquals(tmpSys.getRootDir(), sys0[9]);
    Assert.assertEquals(tmpSys.getJobLocalWorkingDir(), sys0[10]);
    Assert.assertEquals(tmpSys.getJobLocalArchiveDir(), sys0[11]);
    Assert.assertEquals(tmpSys.getJobRemoteArchiveSystem(), sys0[12]);
    Assert.assertEquals(tmpSys.getJobRemoteArchiveDir(), sys0[13]);
    Assert.assertEquals(tmpSys.getPort().intValue(), prot1Port);
    Assert.assertEquals(tmpSys.getUseProxy().booleanValue(), prot1UseProxy);
    Assert.assertEquals(tmpSys.getProxyHost(), prot1ProxyHost);
    Assert.assertEquals(tmpSys.getProxyPort().intValue(), prot1ProxyPort);
    Assert.assertEquals(tmpSys.getDefaultAccessMethod().name(), prot1AccessMethod.name());
    // Verify transfer methods
    List<TSystem.TransferMethodsEnum> tMethodsList = tmpSys.getTransferMethods();
    Assert.assertNotNull(tMethodsList, "TransferMethods list should not be null");
    for (TSystem.TransferMethodsEnum txfrMethod : prot1TxfrMethodsT)
    {
      Assert.assertTrue(tMethodsList.contains(txfrMethod), "List of transfer methods did not contain: " + txfrMethod.name());
    }
    // Verify capabilities
    List<Capability> jobCaps = tmpSys.getJobCapabilities();
    Assert.assertNotNull(jobCaps);
    Assert.assertEquals(jobCaps.size(), jobCaps1.size());
    var capNamesFound = new ArrayList<String>();
    for (Capability capFound : jobCaps) {capNamesFound.add(capFound.getName());}
    for (Capability capSeed : jobCaps1)
    {
      Assert.assertTrue(capNamesFound.contains(capSeed.getName()), "List of capabilities did not contain a capability named: " + capSeed.getName());
    }
    // Verify tags
    List<String> tmpTags = tmpSys.getTags();
    Assert.assertNotNull(tmpTags, "Tags value was null");
    Assert.assertEquals(tmpTags.size(), tags1.size(), "Wrong number of tags");
    for (String tagStr : tags1)
    {
      Assert.assertTrue(tmpTags.contains(tagStr));
      System.out.println("Found tag: " + tagStr);
    }
    // Verify notes
    String tmpNotesStr = (String) tmpSys.getNotes();
    System.out.println("Found notes: " + tmpNotesStr);
    JsonObject tmpNotes = ClientTapisGsonUtils.getGson().fromJson(tmpNotesStr, JsonObject.class);
    Assert.assertNotNull(tmpNotes, "Fetched Notes should not be null");
    JsonObject origNotes = notes1JO;
    Assert.assertTrue(tmpNotes.has("project"));
    String projStr = origNotes.get("project").getAsString();
    Assert.assertEquals(tmpNotes.get("project").getAsString(), projStr);
    Assert.assertTrue(tmpNotes.has("testdata"));
    String testdataStr = origNotes.get("testdata").getAsString();
    Assert.assertEquals(tmpNotes.get("testdata").getAsString(), testdataStr);
  }

  @Test
  public void testUpdateSystem() {
    String[] sys0 = systems.get(8);
    Credential cred0 = null;
//    private static final String[] sysF2 = {tenantName, "CsysF", "description PATCHED", sysType, ownerUser, "hostPATCHED", "effUserPATCHED",
//            "fakePasswordF", "bucketF", "/rootF", "jobLocalWorkDirF", "jobLocalArchDirF", "jobRemoteArchSystemF", "jobRemoteArchDirF"};
    String[] sysF2 = sys0.clone();
    sysF2[2] = "description PATCHED"; sysF2[5] = "hostPATCHED"; sysF2[6] = "effUserPATCHED";
    ReqUpdateSystem rSystem = createPatchSystem(sysF2);
    System.out.println("Creating and updating system with name: " + sys0[1]);
    try {
      // Create a system
      String respUrl = Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Update the system
      respUrl = usrClient.updateSystem(sys0[1], rSystem);
      System.out.println("Updated system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Verify attributes
      sys0 = sysF2;
      TSystem tmpSys = usrClient.getSystemByName(sys0[1]);
      Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
      System.out.println("Found item: " + sys0[1]);
      Assert.assertEquals(tmpSys.getName(), sys0[1]);
      Assert.assertEquals(tmpSys.getDescription(), sys0[2]);
      Assert.assertEquals(tmpSys.getSystemType().name(), sys0[3]);
      Assert.assertEquals(tmpSys.getOwner(), sys0[4]);
      Assert.assertEquals(tmpSys.getHost(), sys0[5]);
      Assert.assertEquals(tmpSys.getEffectiveUserId(), sys0[6]);
      Assert.assertEquals(tmpSys.getBucketName(), sys0[8]);
      Assert.assertEquals(tmpSys.getRootDir(), sys0[9]);
      Assert.assertEquals(tmpSys.getJobLocalWorkingDir(), sys0[10]);
      Assert.assertEquals(tmpSys.getJobLocalArchiveDir(), sys0[11]);
      Assert.assertEquals(tmpSys.getJobRemoteArchiveSystem(), sys0[12]);
      Assert.assertEquals(tmpSys.getJobRemoteArchiveDir(), sys0[13]);
      Assert.assertEquals(tmpSys.getPort().intValue(), prot2Port);
      Assert.assertEquals(tmpSys.getUseProxy().booleanValue(), prot2UseProxy);
      Assert.assertEquals(tmpSys.getProxyHost(), prot2ProxyHost);
      Assert.assertEquals(tmpSys.getProxyPort().intValue(), prot2ProxyPort);
      Assert.assertEquals(tmpSys.getDefaultAccessMethod().name(), prot2AccessMethod.name());
      // Verify transfer methods
      List<TSystem.TransferMethodsEnum> tMethodsList = tmpSys.getTransferMethods();
      Assert.assertNotNull(tMethodsList, "TransferMethods list should not be null");
      for (TSystem.TransferMethodsEnum txfrMethod : prot2TxfrMethodsT)
      {
        Assert.assertTrue(tMethodsList.contains(txfrMethod), "List of transfer methods did not contain: " + txfrMethod.name());
      }
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
        Assert.assertTrue(tmpTags.contains(tagStr));
        System.out.println("Found tag: " + tagStr);
      }
      // Verify notes
      String tmpNotesStr = (String) tmpSys.getNotes();
      JsonObject tmpNotes = ClientTapisGsonUtils.getGson().fromJson(tmpNotesStr, JsonObject.class);
      Assert.assertNotNull(tmpNotes);
      System.out.println("Found notes: " + tmpNotesStr);
      Assert.assertTrue(tmpNotes.has("project"));
      Assert.assertEquals(tmpNotes.get("project").getAsString(), notes2JO.get("project").getAsString());
      Assert.assertTrue(tmpNotes.has("testdata"));
      Assert.assertEquals(tmpNotes.get("testdata").getAsString(), notes2JO.get("testdata").getAsString());
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }

  /**
   * Check that as a user we can give away ownership. Since we can have only one client (i.e. one user) then all we
   * can do after giving away ownership is check that we can no longer modify the system
   * @throws Exception
   */
  @Test
  public void testChangeOwner() throws Exception {
    // Create the system
    String[] sys0 = systems.get(9);
    String respUrl = Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, null, prot1TxfrMethodsC);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    TSystem tmpSys = usrClient.getSystemByName(sys0[1]);
    Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
    usrClient.changeSystemOwner(sys0[1], newOwnerUser);
    // Now that owner has given away ownership we should no longer be able to modify.
    try {
      usrClient.deleteSystemByName(sys0[1]);
      Assert.fail("Original owner should not have permission to update system after change of ownership. System name: " +
                  sys0[1] + " Old owner: " + ownerUser1 + " New Owner: " + newOwnerUser);
    } catch (TapisClientException e) {
      Assert.assertTrue(e.getMessage().contains("HTTP 401 Unauthorized"));
    }
  }

  @Test
  public void testGetSystems() throws Exception {
    // Create 2 systems
    String[] sys0 = systems.get(10);
    Credential cred0 = null;
    String respUrl = Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    sys0 = systems.get(11);
    respUrl = Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Get list of all systems
    List<TSystem> systemsList = usrClient.getSystems();
    Assert.assertNotNull(systemsList);
    Assert.assertFalse(systemsList.isEmpty());
    var systemNames = new ArrayList<String>();
    for (TSystem system : systemsList) {
      System.out.println("Found item: " + system.getName());
      systemNames.add(system.getName());
    }
    Assert.assertTrue(systemNames.contains(systems.get(10)[1]), "List of systems did not contain system name: " + systems.get(10)[1]);
    Assert.assertTrue(systemNames.contains(systems.get(11)[1]), "List of systems did not contain system name: " + systems.get(11)[1]);
  }

  @Test
  public void testDelete() throws Exception {
    // Create the system
    String[] sys0 = systems.get(12);
    Credential cred0 = null;
    String respUrl = Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Delete the system
    usrClient.deleteSystemByName(sys0[1]);
    try {
      TSystem tmpSys2 = usrClient.getSystemByName(sys0[1]);
      Assert.fail("System not deleted. System name: " + sys0[1]);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
    }
  }

  // Test creating, reading and deleting user permissions for a system
  @Test
  public void testUserPerms() {
    String[] sys0 = systems.get(13);
    Credential cred0 = null;
    // Create a system
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = Utils.createSystem(usrClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
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

  @AfterSuite
  public void tearDown() {
// Currently no way to hard delete from client (by design)
//    System.out.println("****** Executing AfterSuite teardown method for class: " + this.getClass().getSimpleName());
//    // TODO: Run SQL to hard delete objects
//    //Remove all objects created by tests, ignore any exceptions
//    for (int i = 0; i < numSystems; i++)
//    {
//      try
//      {
//        usrClientOwner.deleteSystemByName(systems.get(i)[1]);
//      } catch (Exception e)
//      {
//      }
//    }
  }

  private static ReqUpdateSystem createPatchSystem(String[] sys)
  {
    ReqUpdateSystem pSys = new ReqUpdateSystem();
    pSys.description(sys[2]);
    pSys.host(sys[5]);
    pSys.enabled(false);
    pSys.effectiveUserId(sys[6]);
    pSys.defaultAccessMethod(ReqUpdateSystem.DefaultAccessMethodEnum.valueOf(prot2AccessMethod.name()));
    pSys.transferMethods(prot2TxfrMethodsU);
    pSys.port(prot2Port).useProxy(prot2UseProxy).proxyHost(prot2ProxyHost).proxyPort(prot2ProxyPort);
    pSys.jobCapabilities(jobCaps2);
    pSys.tags(tags2);
    pSys.notes(notes2JO);
    return pSys;
  }
}
