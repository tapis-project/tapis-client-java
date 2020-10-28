package edu.utexas.tacc.tapis.systems.client;

import com.google.gson.JsonObject;
import edu.utexas.tacc.tapis.auth.client.AuthClient;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.systems.client.SystemsClient.AccessMethod;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability;
import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqUpdateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem;
import edu.utexas.tacc.tapis.tokens.client.TokensClient;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_TARGET_SITE;
import static edu.utexas.tacc.tapis.systems.client.Utils.createSystem;
import static edu.utexas.tacc.tapis.systems.client.Utils.filesSvcName;
import static edu.utexas.tacc.tapis.systems.client.Utils.getClientUsr;
import static edu.utexas.tacc.tapis.systems.client.Utils.jobCaps1;
import static edu.utexas.tacc.tapis.systems.client.Utils.jobCaps2;
import static edu.utexas.tacc.tapis.systems.client.Utils.masterTenantName;
import static edu.utexas.tacc.tapis.systems.client.Utils.notes1JO;
import static edu.utexas.tacc.tapis.systems.client.Utils.notes2JO;
import static edu.utexas.tacc.tapis.systems.client.Utils.ownerUser1;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot1AccessMethod;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot1Port;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot1ProxyHost;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot1ProxyPort;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot1TxfrMethodsC;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot1TxfrMethodsT;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot1UseProxy;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot2AccessMethod;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot2Port;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot2ProxyHost;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot2ProxyPort;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot2TxfrMethodsT;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot2TxfrMethodsU;
import static edu.utexas.tacc.tapis.systems.client.Utils.prot2UseProxy;
import static edu.utexas.tacc.tapis.systems.client.Utils.tags1;
import static edu.utexas.tacc.tapis.systems.client.Utils.tags2;
import static edu.utexas.tacc.tapis.systems.client.Utils.tenantName;
import static edu.utexas.tacc.tapis.systems.client.Utils.testPerms;
import static edu.utexas.tacc.tapis.systems.client.Utils.testUser3;
import static edu.utexas.tacc.tapis.systems.client.Utils.testUser4;

/**
 * Test the Systems API client acting as the files service calling the systems service.
 *
 * NOTE: Because client code stores headers statically cannot mix a user client and
 *       a service client in one program. This is because user JWTs cannot have
 *       X-Tapis-User, X-Tapis-Tenant headers set and service JWTs must have those headers set
 *       and there does not appear to be an easy way to unset headers.
 *       So instead have user client tests in one program and service client tests in another.
 * Note: Tests that retrieve credentials must act as a files service client calling the systems service.
 * 
 * See IntegrationUtils in this package for information on environment required to run the tests.
 * 
 * Create all systems in setup as user client before switching to files service client for running the tests.
 */
@Test(groups={"integration"})
public class FilesSvcTest
{
  // Test data
  int numSystems = 1;
  Map<Integer, String[]> systems = Utils.makeSystems(numSystems, "CltFiles");

  private String serviceURL;

  // Create a single client. Must do it this way because headers are static and JWT is in the header.
  private SystemsClient sysClient;

  private final Credential cred0 = SystemsClient.buildCredential("fakePassword", "fakePrivateKey", "fakePublicKey",
                                                                 "fakeAccessKey", "fakeAccessSecret", "fakeCert");

  @BeforeSuite
  public void setUp() throws Exception {
    // Get the base URLs from the environment so the test can be used in environments other than dev
    System.out.println("****** Executing BeforeSuite setup method for class: " + this.getClass().getSimpleName());
    // Get files service password from env
    String filesSvcPasswd = Utils.getFilesSvcPassword();
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
    var tokClient = new TokensClient(baseURL, filesSvcName, filesSvcPasswd);
    String filesServiceJWT;
    String userJWT;
    try {
      userJWT = authClient.getToken(ownerUser1, ownerUser1);
      filesServiceJWT = tokClient.getSvcToken(masterTenantName, filesSvcName, DEFAULT_TARGET_SITE);
    } catch (Exception e) {
      throw new Exception("Exception while creating tokens or auth service", e);
    }
    // Basic check of JWTs
    if (StringUtils.isBlank(userJWT)) throw new Exception("Authn service returned invalid owner user JWT");
    if (StringUtils.isBlank(filesServiceJWT)) throw new Exception("Tokens service returned invalid files svc JWT");

    // Create all systems we will need acting as a user.
    sysClient = getClientUsr(serviceURL, userJWT);
    for (int i = 1; i <= numSystems; i++)
    {
      String[] sys0 = systems.get(i);
      System.out.println("Creating system with name: " + sys0[1]);
      try {
        String respUrl = Utils.createSystem(sysClient, sys0, prot1Port, prot1AccessMethod, cred0, prot1TxfrMethodsC);
        System.out.println("Created system: " + respUrl);
        Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      } catch (Exception e) {
        System.out.println("Caught exception: " + e);
        Assert.fail();
      }
    }

    // Update client to be the files service. All tests will be run acting as the files service.
    sysClient = getClientFilesSvc(tenantName, ownerUser1, filesServiceJWT);

    // Cleanup anything leftover from previous failed run
    tearDown();
  }

  // Test retrieving a system including default access method
  //   and test retrieving for specified access method.
  // NOTE: Credential is created for effectiveUserId
  @Test
  public void testGetSystemByName() throws Exception {
    String[] sys0 = systems.get(1);
    TSystem tmpSys = sysClient.getSystemByName(sys0[1], null);
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
    // Verify credentials. Only cred for default accessMethod is returned. In this case PKI_KEYS.
//    Credential cred = tmpSys.getAccessCredential();
//    Assert.assertNotNull(cred, "AccessCredential should not be null");
// TODO: Getting cred along with system is currently broken when called from client.
// TODO Does work in systems service integration test. Parameters to SK appear to be the same so not clear why it fails here
// TODO: Figure out why this works using getUserCred and when called directly from svc but not when getting system using client
// Cred retrieved should be for effectiveUserId = effUser2, so far now as a test retrieve cred directly which does work
    Credential cred = sysClient.getUserCredential(sys0[1], sys0[6], AccessMethod.PKI_KEYS);
// TODO
    Assert.assertEquals(cred.getPrivateKey(), cred0.getPrivateKey());
    Assert.assertEquals(cred.getPublicKey(), cred0.getPublicKey());
    Assert.assertNull(cred.getPassword(), "AccessCredential password should be null");
    Assert.assertNull(cred.getAccessKey(), "AccessCredential access key should be null");
    Assert.assertNull(cred.getAccessSecret(), "AccessCredential access secret should be null");
    Assert.assertNull(cred.getCertificate(), "AccessCredential certificate should be null");
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

    // Test retrieval using specified access method
    tmpSys = sysClient.getSystemByName(sys0[1], AccessMethod.PASSWORD);
    // Verify credentials. Only cred for default accessMethod is returned. In this case PASSWORD.
    cred = tmpSys.getAccessCredential();
    Assert.assertNotNull(cred, "AccessCredential should not be null");
// TODO Not working as described above. For now test by getting cred directly
// TODO fix it
    cred = sysClient.getUserCredential(sys0[1], sys0[6], AccessMethod.PASSWORD);
// TODO
    Assert.assertEquals(cred.getPassword(), cred0.getPassword());
    Assert.assertNull(cred.getPrivateKey(), "AccessCredential private key should be null");
    Assert.assertNull(cred.getPublicKey(), "AccessCredential public key should be null");
    Assert.assertNull(cred.getAccessKey(), "AccessCredential access key should be null");
    Assert.assertNull(cred.getAccessSecret(), "AccessCredential access secret should be null");
    Assert.assertNull(cred.getCertificate(), "AccessCredential certificate should be null");
  }

//  // Test creating, reading and deleting user credentials for a system after system created
//  @Test
//  public void testUserCredentials()
//  {
//    // Create a system
//    String[] sys0 = systems.get(2);
//    System.out.println("Creating system with name: " + sys0[1]);
//    try {
//      String respUrl = Utils.createSystem(getClientUsr(serviceURL, ownerUserJWT), sys0, prot1Port, prot1AccessMethod, null, prot1TxfrMethodsC);
//      System.out.println("Created system: " + respUrl);
//      System.out.println("Testing credentials for user: " + newPermsUser);
//      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
//      ReqCreateCredential reqCred = new ReqCreateCredential();
//      reqCred.password(sys0[7]).privateKey("fakePrivateKey").publicKey("fakePublicKey")
//           .accessKey("fakeAccessKey").accessSecret("fakeAccessSecret").certificate("fakeCert");
//      // Store and retrieve multiple secret types: password, ssh keys, access key and secret
//      getClientUsr(serviceURL, ownerUserJWT).updateUserCredential(sys0[1], newPermsUser, reqCred);
//      Credential cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.PASSWORD);
//      // Verify credentials
//      Assert.assertEquals(cred1.getPassword(), reqCred.getPassword());
//      cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.PKI_KEYS);
//      Assert.assertEquals(cred1.getPublicKey(), reqCred.getPublicKey());
//      Assert.assertEquals(cred1.getPrivateKey(), reqCred.getPrivateKey());
//      cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.ACCESS_KEY);
//      Assert.assertEquals(cred1.getAccessKey(), reqCred.getAccessKey());
//      Assert.assertEquals(cred1.getAccessSecret(), reqCred.getAccessSecret());
//      // Verify we get credentials for default accessMethod if we do not specify an access method
//      cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser);
//      Assert.assertEquals(cred1.getPublicKey(), reqCred.getPublicKey());
//      Assert.assertEquals(cred1.getPrivateKey(), reqCred.getPrivateKey());
//
//      // Delete credentials and verify they were destroyed
//      getClientUsr(serviceURL, ownerUserJWT).deleteUserCredential(sys0[1], newPermsUser);
//      try {
//        cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.PASSWORD);
//      } catch (TapisClientException tce) {
//        Assert.assertTrue(tce.getTapisMessage().startsWith("SYSAPI_CRED_NOT_FOUND"), "Wrong exception message: " + tce.getTapisMessage());
//        cred1 = null;
//      }
//      Assert.assertNull(cred1, "Credential not deleted. System name: " + sys0[1] + " User name: " + newPermsUser);
//
//      // Attempt to delete again, should not throw an exception
//      getClientUsr(serviceURL, ownerUserJWT).deleteUserCredential(sys0[1], newPermsUser);
//
//      // Set just ACCESS_KEY only and test
//      reqCred = new ReqCreateCredential().accessKey("fakeAccessKey2").accessSecret("fakeAccessSecret2");
//      getClientUsr(serviceURL, ownerUserJWT).updateUserCredential(sys0[1], newPermsUser, reqCred);
//      cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.ACCESS_KEY);
//      Assert.assertEquals(cred1.getAccessKey(), reqCred.getAccessKey());
//      Assert.assertEquals(cred1.getAccessSecret(), reqCred.getAccessSecret());
//      // Attempt to retrieve secret that has not been set
//      try {
//        cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.PKI_KEYS);
//      } catch (TapisClientException tce) {
//        Assert.assertTrue(tce.getTapisMessage().startsWith("SYSAPI_CRED_NOT_FOUND"), "Wrong exception message: " + tce.getTapisMessage());
//        cred1 = null;
//      }
//      Assert.assertNull(cred1, "Credential was non-null for missing secret. System name: " + sys0[1] + " User name: " + newPermsUser);
//      // Delete credentials and verify they were destroyed
//      getClientUsr(serviceURL, ownerUserJWT).deleteUserCredential(sys0[1], newPermsUser);
//      try {
//        cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.ACCESS_KEY);
//      } catch (TapisClientException tce) {
//        Assert.assertTrue(tce.getTapisMessage().startsWith("SYSAPI_CRED_NOT_FOUND"), "Wrong exception message: " + tce.getTapisMessage());
//        cred1 = null;
//      }
//      Assert.assertNull(cred1, "Credential not deleted. System name: " + sys0[1] + " User name: " + newPermsUser);
//      // Attempt to retrieve secret from non-existent system
//      try {
//        cred1 = getClientFilesSvc().getUserCredential("AMissingSystemName", newPermsUser, AccessMethod.PKI_KEYS);
//      } catch (TapisClientException tce) {
//        Assert.assertTrue(tce.getTapisMessage().startsWith("SYSAPI_NOSYSTEM"), "Wrong exception message: " + tce.getTapisMessage());
//        cred1 = null;
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//      Assert.fail();
//    }
//  }

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
//        getClientUsr(serviceURL, ownerUserJWT).deleteSystemByName(systems.get(i)[1]);
//      } catch (Exception e)
//      {
//      }
//    }
  }

  private SystemsClient getClientFilesSvc(String tenantName, String ownerUser, String filesServiceJWT)
  {
    // Create the client each time due to issue with setting different headers needed by svc vs usr client
    SystemsClient clt = new SystemsClient(serviceURL, filesServiceJWT);
    clt.addDefaultHeader("X-Tapis-User", ownerUser);
    clt.addDefaultHeader("X-Tapis-Tenant", tenantName);
    return clt;
  }
}
