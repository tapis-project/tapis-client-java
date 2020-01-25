package edu.utexas.tacc.tapis.systems.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import edu.utexas.tacc.tapis.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.shared.utils.TapisGsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem.AccessMethodEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem.TransferMethodsEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem.SystemTypeEnum;
import edu.utexas.tacc.tapis.tokens.client.TokensClient;

/**
 * Test the Systems API client against the systems service.
 * Use a base URL from the env or the default hard coded base URL.
 * Tokens service is used to get a short term JWT.
 * Tokens service URL comes from the env or the default hard coded URL.
 */
@Test(groups={"integration"})
public class SystemsClientTest {
  // Default URLs. These can be overridden by env variables
  private static final String DEFAULT_BASE_URL_SYSTEMS = "https://dev.develop.tapis.io";
  private static final String DEFAULT_BASE_URL_TOKENS = "https://dev.develop.tapis.io";
  // Env variables for setting URLs
  private static final String TAPIS_ENV_SVC_URL_SYSTEMS = "TAPIS_SVC_URL_SYSTEMS";
  private static final String TAPIS_ENV_SVC_URL_TOKENS = "TAPIS_SVC_URL_TOKENS";


  // Test data
  private static final String tenantName = "dev";
  private static final String testUser2 = "testuser2";
  private static final String sysType = SystemTypeEnum.LINUX.name();
  private static final String sysOwner = "sysOwner";
  private static final int prot1Port = -1;
  private static final boolean prot1UseProxy = false;
  private static final String prot1ProxyHost = "a";
  private static final int prot1ProxyPort = -1;
  private static final TransferMethodsEnum[] prot1TxfrMethods = {TransferMethodsEnum.SFTP, TransferMethodsEnum.S3};
  private static final AccessMethodEnum prot1AccessMethod = AccessMethodEnum.PASSWORD;
  private static final String tags = "{\"key1\":\"a\", \"key2\":\"b\"}";
  private static final String notes = "{\"project\":\"myproj1\", \"testdata\":\"abc\"}";
  // TODO/TBD: No perms enum in auto-generated model class. Why not?
//  private static final List<String> testPerms = new ArrayList<>(List.of(TSystem.Permissions.READ.name(),TSystem.Permissions.MODIFY.name(),
//          TSystem.Permissions.DELETE.name()));
  private static final List<String> testPerms = new ArrayList<>(List.of("READ", "MODIFY", "DELETE"));

  private static final String[] sys1 = {tenantName, "Csys1", "description 1", sysType, sysOwner, "host1", "effUser1", "fakePassword1",
          "bucket1", "/root1", "jobLocalWorkDir1", "jobLocalArchDir1", "jobRemoteArchSystem1", "jobRemoteArchDir1", tags, notes};
  private static final String[] sys2 = {tenantName, "Csys2", "description 2", sysType, sysOwner, "host2", "effUser2", "fakePassword2",
          "bucket2", "/root2", "jobLocalWorkDir2", "jobLocalArchDir2", "jobRemoteArchSystem2", "jobRemoteArchDir2", tags, notes};
  private static final String[] sys3 = {tenantName, "Csys3", "description 3", sysType, sysOwner, "host3", "effUser3", "fakePassword3",
          "bucket3", "/root3", "jobLocalWorkDir3", "jobLocalArchDir3", "jobRemoteArchSystem3", "jobRemoteArchDir3", tags, notes};
  private static final String[] sys4 = {tenantName, "Csys4", "description 4", sysType, sysOwner, "host4", "effUser4", "fakePassword4",
          "bucket4", "/root4", "jobLocalWorkDir4", "jobLocalArchDir4", "jobRemoteArchSystem4", "jobRemoteArchDir4", tags, notes};
  private static final String[] sys5 = {tenantName, "Csys5", "description 5", sysType, sysOwner, "host5", "effUser5", "fakePassword5",
          "bucket5", "/root5", "jobLocalWorkDir5", "jobLocalArchDir5", "jobRemoteArchSystem5", "jobRemoteArchDir5", tags, notes};
  private static final String[] sys6 = {tenantName, "Csys6", "description 6", sysType, sysOwner, "host6", "effUser6", "fakePassword6",
          "bucket6", "/root6", "jobLocalWorkDir6", "jobLocalArchDir6", "jobRemoteArchSystem6", "jobRemoteArchDir6", tags, notes};
  private static final String[] sys7 = {tenantName, "Csys7", "description 7", sysType, sysOwner, "host7", "effUser7", "fakePassword7",
          "bucket7", "/root7", "jobLocalWorkDir7", "jobLocalArchDir7", "jobRemoteArchSystem7", "jobRemoteArchDir7", tags, notes};
  private static final String[] sys8 = {tenantName, "Csys8", "description 8", sysType, sysOwner, "host8", "effUser8", "fakePassword8",
          "", "/root8", "jobLocalWorkDir8", "jobLocalArchDir8", "jobRemoteArchSystem8", "jobRemoteArchDir8", tags, notes};
  private static final String[] sys9 = {tenantName, "Csys9", "description 9", sysType, sysOwner, "host9", "effUser9", "fakePassword9",
          "bucket9", "/root9", "jobLocalWorkDir9", "jobLocalArchDir9", "jobRemoteArchSystem9", "jobRemoteArchDir9", tags, notes};
  private static final String[] sysA = {tenantName, "CsysA", "description A", sysType, sysOwner, "hostA", "effUserA", "fakePasswordA",
          "bucketA", "/rootA", "jobLocalWorkDirA", "jobLocalArchDirA", "jobRemoteArchSystemA", "jobRemoteArchDirA", tags, notes};
  private static final String[] sysB = {tenantName, "CsysB", "description B", sysType, sysOwner, "hostB", "${apiUserId}", "fakePasswordB",
          "bucketB", "/rootB", "jobLocalWorkDirB", "jobLocalArchDirB", "jobRemoteArchSystemB", "jobRemoteArchDirB", tags, notes};

  private SystemsClient sysClient;

  @BeforeSuite
  public void setUp() throws Exception {
    // Get the base URLs from the environment so the test can be used in environments other than dev
    System.out.println("Executing BeforeSuite setup method");
    // Get token using URL from env or from default
    String tokensURL = System.getenv(TAPIS_ENV_SVC_URL_TOKENS);
    if (StringUtils.isBlank(tokensURL)) tokensURL = DEFAULT_BASE_URL_TOKENS;
    // Get short term user JWT from tokens service
    var tokClient = new TokensClient(tokensURL);
    String usrJWT;
//    try {usrJWT = tokClient.getSvcToken(tenantName, SERVICE_NAME_SYSTEMS);}
    try {
      usrJWT = tokClient.getUsrToken(tenantName, sysOwner);
    } catch (Exception e) {
      throw new Exception("Exception from Tokens service", e);
    }
    System.out.println("Got usrJWT: " + usrJWT);
    // Basic check of JWT
    if (StringUtils.isBlank(usrJWT)) throw new Exception("Token service returned invalid JWT");
    // Create the client
    // Check for URL set as env var
    String systemsURL = System.getenv(TAPIS_ENV_SVC_URL_SYSTEMS);
    if (StringUtils.isBlank(systemsURL)) systemsURL = DEFAULT_BASE_URL_SYSTEMS;
    sysClient = new SystemsClient(systemsURL, usrJWT);
  }

  @Test
  public void testCreateSystem() {
    // Create a system
    String[] sys0 = sys1;
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = createSystem(sys0, prot1AccessMethod, prot1TxfrMethods);
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
    String[] sys0 = sys7;
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = createSystem(sys0, prot1AccessMethod, prot1TxfrMethods);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e.getMessage() + "\n Stack trace: " + e.getStackTrace());
      Assert.fail();
    }
    // Now attempt to create it again, should throw exception
    System.out.println("Creating system with name: " + sys7[1]);
    createSystem(sys7, prot1AccessMethod, prot1TxfrMethods);
    Assert.fail("Exception should have been thrown");
  }

  // Test that bucketName is required if transfer mechanisms include S3
  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = ".*SYSAPI_S3_NOBUCKET_INPUT.*")
  public void testCreateSystemS3NoBucketName() throws Exception {
    // Create a system
    String[] sys0 = sys8;
    System.out.println("Creating system with name: " + sys0[1]);
    createSystem(sys0, prot1AccessMethod, prot1TxfrMethods);
    Assert.fail("Exception should have been thrown");
  }

  // Test that access mechanism of CERT and static owner is not allowed
  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = ".*SYSAPI_INVALID_EFFECTIVEUSERID_INPUT.*")
  public void testCreateSystemInvalidEffUserId() throws Exception {
    // Create a system
    String[] sys0 = sys9;
    System.out.println("Creating system with name: " + sys0[1]);
    createSystem(sys0, AccessMethodEnum.CERT, prot1TxfrMethods);
    Assert.fail("Exception should have been thrown");
  }

  // Test that providing credentials for dynamic effective user is not allowed
  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = ".*SYSAPI_CRED_DISALLOWED_INPUT.*")
  public void testCreateSystemCredDisallowed() throws Exception {
    // Create a system
    String[] sys0 = sysB;
    System.out.println("Creating system with name: " + sys0[1]);
    createSystem(sys0, prot1AccessMethod, prot1TxfrMethods);
    Assert.fail("Exception should have been thrown");
  }

  @Test
  public void testGetSystemByName() throws Exception {
    String[] sys0 = sys2;
    String respUrl = createSystem(sys0, prot1AccessMethod, prot1TxfrMethods);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    TSystem tmpSys = sysClient.getSystemByName(sys0[1], false);
    Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
    System.out.println("Found item: " + sys0[1]);
//    sys2 = {tenantName, "Csys2", "description 2", sysType, sysOwner, "host2", "effUser2", "fakePassword2",
//            "bucket2", "/root2", "jobLocalWorkDir2", "jobLocalArchDir2", "jobRemoteArchSystem2", "jobRemoteArchDir2", tags, notes};
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
    Assert.assertEquals(tmpSys.getAccessMethod(), prot1AccessMethod);
    Assert.assertEquals(tmpSys.getPort().intValue(), prot1Port);
    Assert.assertEquals(tmpSys.getUseProxy().booleanValue(), prot1UseProxy);
    Assert.assertEquals(tmpSys.getProxyHost(), prot1ProxyHost);
    Assert.assertEquals(tmpSys.getProxyPort().intValue(), prot1ProxyPort);
    List<TransferMethodsEnum> tMethodsList = tmpSys.getTransferMethods();
    Assert.assertNotNull(tMethodsList);
    Assert.assertTrue(tMethodsList.contains(TransferMethodsEnum.S3), "List of transfer mechanisms did not contain: " + TransferMethodsEnum.S3.name());
    Assert.assertTrue(tMethodsList.contains(TransferMethodsEnum.SFTP), "List of transfer mechanisms did not contain: " + TransferMethodsEnum.SFTP.name());
    // Retrieve tags, convert to json, verify keys and values
    String tags = tmpSys.getTags();
    System.out.println("Found tags: " + tags);
    // Get the Json object and prepare to extract info from it
    JsonObject obj = TapisGsonUtils.getGson().fromJson(tags, JsonObject.class);
    Assert.assertTrue(obj.has("key1"));
    Assert.assertEquals(obj.get("key1").getAsString(), "a");
    Assert.assertTrue(obj.has("key2"));
    Assert.assertEquals(obj.get("key2").getAsString(), "b");
    // Retrieve notes, convert to json, verify elements
    String notes = tmpSys.getNotes();
    System.out.println("Found notes: " + notes);
    obj = TapisGsonUtils.getGson().fromJson(notes, JsonObject.class);
    Assert.assertTrue(obj.has("project"));
    Assert.assertEquals(obj.get("project").getAsString(), "myproj1");
    Assert.assertTrue(obj.has("testdata"));
    Assert.assertEquals(obj.get("testdata").getAsString(), "abc");
  }

  @Test
  public void testGetSystemNames() throws Exception {
    // Create 2 systems
    String[] sys0 = sys3;
    String respUrl = createSystem(sys0, prot1AccessMethod, prot1TxfrMethods);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    sys0 = sys4;
    respUrl = createSystem(sys0, prot1AccessMethod, prot1TxfrMethods);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Get list of all system names
    List<String> systemNames = sysClient.getSystemNames();
    for (String name : systemNames) {
      System.out.println("Found item: " + name);
    }
    Assert.assertTrue(systemNames.contains(sys3[1]), "List of systems did not contain system name: " + sys3[1]);
    Assert.assertTrue(systemNames.contains(sys4[1]), "List of systems did not contain system name: " + sys4[1]);
  }

//  @Test
//  public void testGetSystems() throws Exception
//  {
//    String respUrl = createSystem(sys5);
//    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
//    List<TSystem> systems = sysClient.getSystems();
//    for (TSystem system : systems) {
//      System.out.println("Found item with id: " + system.getId() + " and name: " + system.getName());
//    }
//  }

  @Test
  public void testDelete() throws Exception {
    // Create the system
    String[] sys0 = sys6;
    String respUrl = createSystem(sys0, prot1AccessMethod, prot1TxfrMethods);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Delete the system
    sysClient.deleteSystemByName(sys0[1]);
    try {
      TSystem tmpSys2 = sysClient.getSystemByName(sys0[1], false);
      Assert.fail("System not deleted. System name: " + sys0[1]);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
    }
  }

  // Test creating, reading and deleting user permissions for a system
  @Test(enabled = true)
  public void testUserPerms() {
    String[] sys0 = sysA;
    // Create a system
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = createSystem(sys0, prot1AccessMethod, prot1TxfrMethods);
      System.out.println("Created system: " + respUrl);
      System.out.println("Testing perms for user: " + testUser2);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Create user perms for the system
      sysClient.grantUserPermissions(sys0[1], testUser2, testPerms);
      // Get the system perms for the user and make sure permissions are there
      List<String> userPerms = sysClient.getSystemPermissions(sys0[1], testUser2);
      Assert.assertNotNull(userPerms, "Null returned when retrieving perms.");
      for (String perm : userPerms) {
        System.out.println("After grant found user perm: " + perm);
      }
      Assert.assertEquals(userPerms.size(), testPerms.size(), "Incorrect number of perms returned.");
      for (String perm : testPerms) {
        if (!userPerms.contains(perm)) Assert.fail("User perms should contain permission: " + perm);
      }
      // Remove perms for the user
      sysClient.revokeUserPermissions(sys0[1], testUser2, testPerms);
      // Get the system perms for the user and make sure permissions are gone.
      userPerms = sysClient.getSystemPermissions(sys0[1], testUser2);
      Assert.assertNotNull(userPerms, "Null returned when retrieving perms.");
      for (String perm : userPerms) {
        System.out.println("After revoke found user perm: " + perm);
      }
      for (String perm : testPerms) {
        if (userPerms.contains(perm)) Assert.fail("User perms should not contain permission: " + perm);
      }
    } catch (Exception e) {
      System.out.println("Caught exception: " + e.getMessage() + "\n Stack trace: " + e.getStackTrace());
      Assert.fail();
    }

  }

  @AfterSuite
  public void tearDown() {
    System.out.println("Executing AfterSuite teardown method");
    //Remove all objects created by tests, ignore any exceptions
    try {
      sysClient.deleteSystemByName(sys1[1]);
    } catch (Exception e) {
    }
    try {
      sysClient.deleteSystemByName(sys2[1]);
    } catch (Exception e) {
    }
    try {
      sysClient.deleteSystemByName(sys3[1]);
    } catch (Exception e) {
    }
    try {
      sysClient.deleteSystemByName(sys4[1]);
    } catch (Exception e) {
    }
    try {
      sysClient.deleteSystemByName(sys5[1]);
    } catch (Exception e) {
    }
    try {
      sysClient.deleteSystemByName(sys6[1]);
    } catch (Exception e) {
    }
    try {
      sysClient.deleteSystemByName(sys7[1]);
    } catch (Exception e) {
    }
    try {
      sysClient.deleteSystemByName(sys8[1]);
    } catch (Exception e) {
    }
    try {
      sysClient.deleteSystemByName(sys9[1]);
    } catch (Exception e) {
    }
    try {
      sysClient.deleteSystemByName(sysA[1]);
    } catch (Exception e) {
    }
  }

  private String createSystem(String[] sys, AccessMethodEnum accessMethod, TransferMethodsEnum[] txfrMethods) throws TapisClientException {
    // Convert list of TransferMethod enums to list of strings
    List<String> transferMethods = Stream.of(txfrMethods).map(TransferMethodsEnum::name).collect(Collectors.toList());
    // If password is set then create a credential
    Credential credential = null;
    String password = sys[7];
    if (!StringUtils.isBlank(password))
    {
      credential = sysClient.buildCredential(password, null, null, null, null, null);
    }
    // Create the system
    return sysClient.createSystem(sys[1], sys[2], sys[3], sys[4], sys[5], true,
                                  sys[6], accessMethod.name(), credential, sys[8], sys[9], transferMethods,
                                  prot1Port, prot1UseProxy, prot1ProxyHost, prot1ProxyPort,
                                  true, sys[10], sys[11], sys[12], sys[13],null, sys[14], sys[15]);
  }
}
