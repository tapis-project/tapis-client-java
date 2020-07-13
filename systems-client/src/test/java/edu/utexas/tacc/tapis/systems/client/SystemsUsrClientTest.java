package edu.utexas.tacc.tapis.systems.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonObject;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateCredential;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqUpdateSystem;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem.SystemTypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability.CategoryEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem;
import edu.utexas.tacc.tapis.systems.client.SystemsClient.AccessMethod;
import edu.utexas.tacc.tapis.auth.client.AuthClient;
import edu.utexas.tacc.tapis.tokens.client.TokensClient;

/**
 * Test the Systems API client acting as a user calling the systems service.
 * Tests that retrieve credentials act as a files service client calling the systems service.
 *  - Systems service base URL comes from the env or the default hard coded base URL.
 *  - Auth and tokens base URL comes from the env or the default hard coded base URL.
 *  - Auth service is used to get a short term JWT.
 *  - Tokens service is used to get a files service JWT.
 *  - Files service password must come from the environment: TAPIS_FILES_SVC_PASSWORD
 *
 *  To override base URLs use the following env variables:
 *    TAPIS_SVC_URL_SYSTEMS
 *    TAPIS_BASE_URL (for auth, tokens services)
 *  To override systems service port use:
 *    TAPIS_SERVICE_PORT
 *  NOTE that service port is ignored if TAPIS_SVC_URL_SYSTEMS is set
 */
@Test(groups={"integration"})
public class SystemsUsrClientTest {
  private static final String tenantName = "dev";
  // TAPIS_BASE_URL_SUFFIX should be set according to the dev, staging or prod environment
  // dev     -> develop.tapis.io
  // staging -> staging.tapis.io
  // prod    -> tapis.io
  // Default URLs. These can be overridden by env variables
  private static final String DEFAULT_BASE_URL = "https://" + tenantName + ".develop.tapis.io";
  private static final String DEFAULT_BASE_URL_SYSTEMS = "http://localhost";
  private static final String DEFAULT_SVC_PORT = "8080";
  // Env variables for setting URLs, passwords, etc
  private static final String TAPIS_ENV_BASE_URL = "TAPIS_BASE_URL";
  private static final String TAPIS_ENV_SVC_URL_SYSTEMS = "TAPIS_SVC_URL_SYSTEMS";
  private static final String TAPIS_ENV_FILES_SVC_PASSWORD = "TAPIS_FILES_SVC_PASSWORD";
  private static final String TAPIS_ENV_SVC_PORT = "TAPIS_SERVICE_PORT";

  // Test data
  private static final String ownerUser = "testuser2";
  private static final String newOwnerUser = "testuser3";
  private static final String adminUser = "testSystemsAdminUsr";
  private static final String newPermsUser = "testuser4";
  private static final String masterTenantName = "master";
  private static final String filesSvcName = "files";
  private static final String sysType = SystemTypeEnum.LINUX.name();
  private static final int prot1Port = -1, prot1ProxyPort = -1, prot2Port = 22, prot2ProxyPort = 222;
  private static final boolean prot1UseProxy = false, prot2UseProxy = true;
  private static final String prot1ProxyHost = "proxyhost1", prot2ProxyHost = "proxyhost2";
  private static final List<ReqCreateSystem.TransferMethodsEnum> prot1TxfrMethodsC =
          Arrays.asList(ReqCreateSystem.TransferMethodsEnum.SFTP, ReqCreateSystem.TransferMethodsEnum.S3);
  private static final List<TSystem.TransferMethodsEnum> prot1TxfrMethodsT =
          Arrays.asList(TSystem.TransferMethodsEnum.SFTP, TSystem.TransferMethodsEnum.S3);
  private static final List<ReqUpdateSystem.TransferMethodsEnum> prot2TxfrMethodsU =
          Collections.singletonList(ReqUpdateSystem.TransferMethodsEnum.SFTP);
  private static final List<TSystem.TransferMethodsEnum> prot2TxfrMethodsT =
          Collections.singletonList(TSystem.TransferMethodsEnum.SFTP);
  private static final AccessMethod prot1AccessMethod = AccessMethod.PKI_KEYS;
  private static final AccessMethod prot2AccessMethod = AccessMethod.ACCESS_KEY;
  private static final List<String> tags1 = Arrays.asList("value1", "value2", "a",
    "a long tag with spaces and numbers (1 3 2) and special characters [_ $ - & * % @ + = ! ^ ? < > , . ( ) { } / \\ | ]. Backslashes must be escaped.");
  private static final List<String> tags2 = Arrays.asList("value3", "value4");
  private static final JsonObject notes1JO =
          ClientTapisGsonUtils.getGson().fromJson("{\"project\":\"myproj1\", \"testdata\":\"abc1\"}", JsonObject.class);
  private static final JsonObject notes2JO =
          ClientTapisGsonUtils.getGson().fromJson("{\"project\":\"myproj2\", \"testdata\":\"abc2\"}", JsonObject.class);
  private static final List<String> testPerms = new ArrayList<>(List.of("READ", "MODIFY"));

  private static final String[] sys1 = {tenantName, "Csys1", "description 1", sysType, ownerUser, "host1", "effUser1", "fakePassword1",
          "bucket1", "/root1", "jobLocalWorkDir1", "jobLocalArchDir1", "jobRemoteArchSystem1", "jobRemoteArchDir1"};
  private static final String[] sys2 = {tenantName, "Csys2", "description 2", sysType, ownerUser, "host2", "effUser2", "fakePassword2",
          "bucket2", "/root2", "jobLocalWorkDir2", "jobLocalArchDir2", "jobRemoteArchSystem2", "jobRemoteArchDir2"};
  private static final String[] sys3 = {tenantName, "Csys3", "description 3", sysType, ownerUser, "host3", "effUser3", "fakePassword3",
          "bucket3", "/root3", "jobLocalWorkDir3", "jobLocalArchDir3", "jobRemoteArchSystem3", "jobRemoteArchDir3"};
  private static final String[] sys4 = {tenantName, "Csys4", "description 4", sysType, ownerUser, "host4", "effUser4", "fakePassword4",
          "bucket4", "/root4", "jobLocalWorkDir4", "jobLocalArchDir4", "jobRemoteArchSystem4", "jobRemoteArchDir4"};
  private static final String[] sys5 = {tenantName, "Csys5", "description 5", sysType, ownerUser, "host5", "effUser5", "fakePassword5",
          "bucket5", "/root5", "jobLocalWorkDir5", "jobLocalArchDir5", "jobRemoteArchSystem5", "jobRemoteArchDir5"};
  private static final String[] sys6 = {tenantName, "Csys6", "description 6", sysType, ownerUser, "host6", "effUser6", "fakePassword6",
          "bucket6", "/root6", "jobLocalWorkDir6", "jobLocalArchDir6", "jobRemoteArchSystem6", "jobRemoteArchDir6"};
  private static final String[] sys7 = {tenantName, "Csys7", "description 7", sysType, ownerUser, "host7", "effUser7", "fakePassword7",
          "bucket7", "/root7", "jobLocalWorkDir7", "jobLocalArchDir7", "jobRemoteArchSystem7", "jobRemoteArchDir7"};
  private static final String[] sys8 = {tenantName, "Csys8", "description 8", sysType, ownerUser, "host8", "effUser8", "fakePassword8",
          "", "/root8", "jobLocalWorkDir8", "jobLocalArchDir8", "jobRemoteArchSystem8", "jobRemoteArchDir8"};
  private static final String[] sys9 = {tenantName, "Csys9", "description 9", sysType, ownerUser, "host9", "effUser9", "fakePassword9",
          "bucket9", "/root9", "jobLocalWorkDir9", "jobLocalArchDir9", "jobRemoteArchSystem9", "jobRemoteArchDir9"};
  private static final String[] sysA = {tenantName, "CsysA", "description A", sysType, ownerUser, "hostA", "effUserA", "fakePasswordA",
          "bucketA", "/rootA", "jobLocalWorkDirA", "jobLocalArchDirA", "jobRemoteArchSystemA", "jobRemoteArchDirA"};
  private static final String[] sysB = {tenantName, "CsysB", "description B", sysType, ownerUser, "hostB", "${apiUserId}", "fakePasswordB",
          "bucketB", "/rootB", "jobLocalWorkDirB", "jobLocalArchDirB", "jobRemoteArchSystemB", "jobRemoteArchDirB"};
  private static final String[] sysC = {tenantName, "CsysC", "description C", sysType, ownerUser, "hostC", "effUserC", "fakePasswordC",
          "bucketC", "/rootC", "jobLocalWorkDirC", "jobLocalArchDirC", "jobRemoteArchSystemC", "jobRemoteArchDirC"};
  private static final String[] sysD = {tenantName, "CsysD", "description D", sysType, ownerUser, "hostD", "effUserD", "fakePasswordD",
          "bucketD", "/rootD", "jobLocalWorkDirD", "jobLocalArchDirD", "jobRemoteArchSystemD", "jobRemoteArchDirD"};
  private static final String[] sysE = {tenantName, "CsysE", null, sysType, null, "hostE", null, null,
          null, null, null, null, null, null};
  private static final String[] sysF = {tenantName, "CsysF", "description F", sysType, ownerUser, "hostF", "effUserF", "fakePasswordF",
          "bucketF", "/rootF", "jobLocalWorkDirF", "jobLocalArchDirF", "jobRemoteArchSystemF", "jobRemoteArchDirF"};
  private static final String[] sysF2 = {tenantName, "CsysF", "description PATCHED", sysType, ownerUser, "hostPATCHED", "effUserPATCHED",
          "fakePasswordF", "bucketF", "/rootF", "jobLocalWorkDirF", "jobLocalArchDirF", "jobRemoteArchSystemF", "jobRemoteArchDirF"};
  private static final String[] sysG = {tenantName, "CsysG", "description G", sysType, ownerUser, "hostG", null, null,
          "bucketG", null, null, null, null, null};

  private static final Capability capA1 = SystemsClient.buildCapability(CategoryEnum.SCHEDULER, "Type", "Slurm");
  private static final Capability capB1 = SystemsClient.buildCapability(CategoryEnum.HARDWARE, "CoresPerNode", "4");
  private static final Capability capC1 = SystemsClient.buildCapability(CategoryEnum.SOFTWARE, "OpenMP", "4.5");
  private static final Capability capA2 = SystemsClient.buildCapability(CategoryEnum.SCHEDULER, "Type", "Condor");
  private static final Capability capB2 = SystemsClient.buildCapability(CategoryEnum.HARDWARE, "CoresPerNode", "128");
  private static final Capability capC2 = SystemsClient.buildCapability(CategoryEnum.SOFTWARE, "OpenMP", "3.1");
  private static final Capability capD2 = SystemsClient.buildCapability(CategoryEnum.CONTAINER, "Singularity", null);
  private static final List<Capability> jobCaps1 = new ArrayList<>(List.of(capA1, capB1, capC1));
  private static final List<Capability> jobCaps2 = new ArrayList<>(List.of(capA2, capB2, capC2, capD2));

  private String serviceURL, servicePort, ownerUserJWT, newOwnerUserJWT, filesServiceJWT;

  @BeforeSuite
  public void setUp() throws Exception {
    // Get the base URLs from the environment so the test can be used in environments other than dev
    System.out.println("Executing BeforeSuite setup method");
    // Get files service password from env
    String filesSvcPasswd = System.getenv(TAPIS_ENV_FILES_SVC_PASSWORD);
    if (StringUtils.isBlank(filesSvcPasswd))
    {
      System.out.println("ERROR: Files service password must be set using environment variable:  " + TAPIS_ENV_FILES_SVC_PASSWORD);
      System.exit(1);
    }
    // Set service port for systems service. Check for port set as env var
    // NOTE: This is ignored if TAPIS_ENV_SVC_URL_SYSTEMS is set
    servicePort = System.getenv(TAPIS_ENV_SVC_PORT);
    System.out.println("Systems Service port from ENV: " + serviceURL);
    if (StringUtils.isBlank(servicePort)) servicePort = DEFAULT_SVC_PORT;
    // Set base URL for systems service. Check for URL set as env var
    serviceURL = System.getenv(TAPIS_ENV_SVC_URL_SYSTEMS);
    System.out.println("Systems Service URL from ENV: " + serviceURL);
    if (StringUtils.isBlank(serviceURL)) serviceURL = DEFAULT_BASE_URL_SYSTEMS + ":" + servicePort;
    // Get base URL suffix from env or from default
    String baseURL = System.getenv(TAPIS_ENV_BASE_URL);
    if (StringUtils.isBlank(baseURL)) baseURL = DEFAULT_BASE_URL;
    // Log URLs being used
    System.out.println("Using Systems URL: " + serviceURL);
    System.out.println("Using Authenticator URL: " + baseURL);
    System.out.println("Using Tokens URL: " + baseURL);
    // Get short term user JWT from tokens service
    var authClient = new AuthClient(baseURL);
    var tokClient = new TokensClient(baseURL, filesSvcName, filesSvcPasswd);
    try {
      ownerUserJWT = authClient.getToken(ownerUser, ownerUser);
      newOwnerUserJWT = authClient.getToken(newOwnerUser, newOwnerUser);
      filesServiceJWT = tokClient.getSvcToken(masterTenantName, filesSvcName);
    } catch (Exception e) {
      throw new Exception("Exception while creating tokens or auth service", e);
    }
    // Basic check of JWTs
    if (StringUtils.isBlank(ownerUserJWT)) throw new Exception("Authn service returned invalid owner user JWT");
    if (StringUtils.isBlank(newOwnerUserJWT)) throw new Exception("Authn service returned invalid new owner user JWT");
    if (StringUtils.isBlank(filesServiceJWT)) throw new Exception("Tokens service returned invalid files svc JWT");
    // Cleanup anything leftover from previous failed run
    tearDown();
  }

  @Test
  public void testHealthAndReady() {
    try {
      System.out.println("Checking health status");
      String status = getClientUsr().checkHealth();
      System.out.println("Health status: " + status);
      Assert.assertNotNull(status);
      Assert.assertFalse(StringUtils.isBlank(status), "Invalid response: " + status);
      Assert.assertEquals(status, "success", "Service failed health check");
      System.out.println("Checking ready status");
      status = getClientUsr().checkReady();
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
    String[] sys0 = sys1;
    Credential cred0 = null;
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = createSystem(sys0, prot1AccessMethod, cred0, prot1TxfrMethodsC, jobCaps1);
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
    String[] sys0 = sysE;
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = createSystem(sys0, prot1AccessMethod, null, null, null);
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
    Credential cred0 = null;
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = createSystem(sys0, prot1AccessMethod, cred0, prot1TxfrMethodsC, null);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
    // Now attempt to create it again, should throw exception
    System.out.println("Creating system with name: " + sys7[1]);
    createSystem(sys7, prot1AccessMethod, cred0, prot1TxfrMethodsC, null);
    Assert.fail("Exception should have been thrown");
  }

  // Test that bucketName is required if transfer methods include S3
  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = ".*SYSAPI_S3_NOBUCKET_INPUT.*")
  public void testCreateSystemS3NoBucketName() throws Exception {
    // Create a system
    String[] sys0 = sys8;
    Credential cred0 = null;
    System.out.println("Creating system with name: " + sys0[1]);
    createSystem(sys0, prot1AccessMethod, cred0, prot1TxfrMethodsC, jobCaps1);
    Assert.fail("Exception should have been thrown");
  }

  // Test that access method of CERT and static owner is not allowed
  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = ".*SYSAPI_INVALID_EFFECTIVEUSERID_INPUT.*")
  public void testCreateSystemInvalidEffUserId() throws Exception {
    // Create a system
    String[] sys0 = sys9;
    Credential cred0 = null;
    System.out.println("Creating system with name: " + sys0[1]);
    createSystem(sys0, AccessMethod.CERT, cred0, prot1TxfrMethodsC, null);
    Assert.fail("Exception should have been thrown");
  }

  // Test that providing credentials for dynamic effective user is not allowed
  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = ".*SYSAPI_CRED_DISALLOWED_INPUT.*")
  public void testCreateSystemCredDisallowed() throws Exception {
    // Create a system
    String[] sys0 = sysB;
    Credential cred0 = SystemsClient.buildCredential(sys0[7], "fakePrivateKey", "fakePublicKey",
                                           "fakeAccessKey", "fakeAccessSecret", "fakeCert");
    System.out.println("Creating system with name: " + sys0[1]);
    createSystem(sys0, prot1AccessMethod, cred0, prot1TxfrMethodsC, null);
    Assert.fail("Exception should have been thrown");
  }

  // Test retrieving a system including default access method
  //   and test retrieving for specified access method.
  // NOTE: Credential is created for effectiveUserId
  @Test
  public void testGetSystemByName() throws Exception {
    String[] sys0 = sys2;
    Credential cred0 = SystemsClient.buildCredential(sys0[7], "fakePrivateKey", "fakePublicKey",
                                           "fakeAccessKey", "fakeAccessSecret", "fakeCert");
    String respUrl = createSystem(sys0, AccessMethod.PKI_KEYS, cred0, prot1TxfrMethodsC, jobCaps1);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Must be files or jobs service to get credentials
    TSystem tmpSys = getClientFilesSvc().getSystemByName(sys0[1], null);
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
    Credential cred = tmpSys.getAccessCredential();
    Assert.assertNotNull(cred, "AccessCredential should not be null");
// TODO: Getting cred along with system is currently broken when called from client.
// TODO Does work in systems service integration test. Parameters to SK appear to be the same so not clear why it fails here
// TODO: Figure out why this works using getUserCred and when called directly from svc but not when getting system using client  
// Cred retrieved should be for effectiveUserId = effUser2, so far now as a test retrieve cred directly which does work
    cred = getClientFilesSvc().getUserCredential(sys0[1], sys0[6], AccessMethod.PKI_KEYS);
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

    // Need service client to get creds. Currently unable to use both user client and service client in same program
    // Test retrieval using specified access method
    tmpSys = getClientFilesSvc().getSystemByName(sys0[1], AccessMethod.PASSWORD);
    // Verify credentials. Only cred for default accessMethod is returned. In this case PASSWORD.
    cred = tmpSys.getAccessCredential();
    Assert.assertNotNull(cred, "AccessCredential should not be null");
// TODO Not working as described above. For now test by getting cred directly
// TODO fix it
    cred = getClientFilesSvc().getUserCredential(sys0[1], sys0[6], AccessMethod.PASSWORD);

    Assert.assertEquals(cred.getPassword(), cred0.getPassword());
    Assert.assertNull(cred.getPrivateKey(), "AccessCredential private key should be null");
    Assert.assertNull(cred.getPublicKey(), "AccessCredential public key should be null");
    Assert.assertNull(cred.getAccessKey(), "AccessCredential access key should be null");
    Assert.assertNull(cred.getAccessSecret(), "AccessCredential access secret should be null");
    Assert.assertNull(cred.getCertificate(), "AccessCredential certificate should be null");
  }

  @Test
  public void testUpdateSystem() {
    String[] sys0 = sysF;
    Credential cred0 = null;
    ReqUpdateSystem rSystem = createPatchSystem(sysF2);
    System.out.println("Creating and updating system with name: " + sys0[1]);
    try {
      // Create a system
      String respUrl = createSystem(sys0, prot1AccessMethod, cred0, prot1TxfrMethodsC, jobCaps1);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Update the system
      respUrl = getClientUsr().updateSystem(sys0[1], rSystem);
      System.out.println("Updated system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Verify attributes
      sys0 = sysF2;
      TSystem tmpSys = getClientUsr().getSystemByName(sys0[1]);
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

  @Test
  public void testChangeOwner() throws Exception {
    // Create the system
    String[] sys0 = sysG;
    String respUrl = createSystem(sys0, prot1AccessMethod, null, prot1TxfrMethodsC, jobCaps1);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    TSystem tmpSys = getClientUsr().getSystemByName(sys0[1]);
    Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
    getClientUsr().changeSystemOwner(sys0[1], newOwnerUser);
    // Now that owner has given away ownership we need to be newOwnerUser or admin to get the system
    tmpSys = getClientUsr2().getSystemByName(sys0[1]);
    Assert.assertNotNull(tmpSys, "Unable to get system after change of owner. System: " + sys0[1]);
    System.out.println("Found item: " + sys0[1]);
    Assert.assertEquals(tmpSys.getOwner(), newOwnerUser);
  }

  // Test retrieving a system using only the name. No credentials returned.
  @Test
  public void testGetSystemByNameOnly() throws Exception {
    String[] sys0 = sysD;
    Credential cred0 = SystemsClient.buildCredential(sys0[7], "fakePrivateKey", "fakePublicKey",
            "fakeAccessKey", "fakeAccessSecret", "fakeCert");
    String respUrl = createSystem(sys0, prot1AccessMethod, cred0, prot1TxfrMethodsC, jobCaps1);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    TSystem tmpSys = getClientUsr().getSystemByName(sys0[1]);
    Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
    System.out.println("Found item: " + sys0[1]);
//    sys2 = {tenantName, "Csys2", "description 2", sysType, sysOwner, "host2", "effUser2", "fakePassword2",
//            "bucket2", "/root2", "jobLocalWorkDir2", "jobLocalArchDir2", "jobRemoteArchSystem2", "jobRemoteArchDir2"};
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
    Assert.assertEquals(tmpSys.getDefaultAccessMethod().name(), prot1AccessMethod.name());
    Assert.assertEquals(tmpSys.getPort().intValue(), prot1Port);
    Assert.assertEquals(tmpSys.getUseProxy().booleanValue(), prot1UseProxy);
    Assert.assertEquals(tmpSys.getProxyHost(), prot1ProxyHost);
    Assert.assertEquals(tmpSys.getProxyPort().intValue(), prot1ProxyPort);
    Assert.assertNull(tmpSys.getAccessCredential(), "AccessCredential should be null");
  }

  @Test
  public void testGetSystems() throws Exception {
    // Create 2 systems
    String[] sys0 = sys3;
    Credential cred0 = null;
    String respUrl = createSystem(sys0, prot1AccessMethod, cred0, prot1TxfrMethodsC, jobCaps1);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    sys0 = sys4;
    respUrl = createSystem(sys0, prot1AccessMethod, cred0, prot1TxfrMethodsC, jobCaps2);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Get list of all system names
    List<TSystem> systems = getClientUsr().getSystems();
    Assert.assertNotNull(systems);
    Assert.assertFalse(systems.isEmpty());
    var systemNames = new ArrayList<String>();
    for (TSystem system : systems) {
      System.out.println("Found item: " + system.getName());
      systemNames.add(system.getName());
    }
    Assert.assertTrue(systemNames.contains(sys3[1]), "List of systems did not contain system name: " + sys3[1]);
    Assert.assertTrue(systemNames.contains(sys4[1]), "List of systems did not contain system name: " + sys4[1]);
  }

  @Test
  public void testDelete() throws Exception {
    // Create the system
    String[] sys0 = sys6;
    Credential cred0 = null;
    String respUrl = createSystem(sys0, prot1AccessMethod, cred0, prot1TxfrMethodsC, jobCaps1);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Delete the system
    getClientUsr().deleteSystemByName(sys0[1]);
    try {
      TSystem tmpSys2 = getClientUsr().getSystemByName(sys0[1]);
      Assert.fail("System not deleted. System name: " + sys0[1]);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
    }
  }

  // Test creating, reading and deleting user permissions for a system
  @Test
  public void testUserPerms() {
    String[] sys0 = sysA;
    Credential cred0 = null;
    // Create a system
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = createSystem(sys0, prot1AccessMethod, cred0, prot1TxfrMethodsC, null);
      System.out.println("Created system: " + respUrl);
      System.out.println("Testing perms for user: " + newPermsUser);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Create user perms for the system
      getClientUsr().grantUserPermissions(sys0[1], newPermsUser, testPerms);
      // Get the system perms for the user and make sure permissions are there
      List<String> userPerms = getClientUsr().getSystemPermissions(sys0[1], newPermsUser);
      Assert.assertNotNull(userPerms, "Null returned when retrieving perms.");
      for (String perm : userPerms) {
        System.out.println("After grant found user perm: " + perm);
      }
      Assert.assertEquals(userPerms.size(), testPerms.size(), "Incorrect number of perms returned.");
      for (String perm : testPerms) {
        if (!userPerms.contains(perm)) Assert.fail("User perms should contain permission: " + perm);
      }
      // Remove perms for the user
      getClientUsr().revokeUserPermissions(sys0[1], newPermsUser, testPerms);
      // Get the system perms for the user and make sure permissions are gone.
      userPerms = getClientUsr().getSystemPermissions(sys0[1], newPermsUser);
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

  // Test creating, reading and deleting user credentials for a system after system created
  @Test
  public void testUserCredentials()
  {
    // Create a system
    String[] sys0 = sysC;
    System.out.println("Creating system with name: " + sys0[1]);
    try {
      String respUrl = createSystem(sys0, prot1AccessMethod, null, prot1TxfrMethodsC, null);
      System.out.println("Created system: " + respUrl);
      System.out.println("Testing credentials for user: " + newPermsUser);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      ReqCreateCredential reqCred = new ReqCreateCredential();
      reqCred.password(sys0[7]).privateKey("fakePrivateKey").publicKey("fakePublicKey")
           .accessKey("fakeAccessKey").accessSecret("fakeAccessSecret").certificate("fakeCert");
      // Store and retrieve multiple secret types: password, ssh keys, access key and secret
      getClientUsr().updateUserCredential(sys0[1], newPermsUser, reqCred);
      Credential cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.PASSWORD);
      // Verify credentials
      Assert.assertEquals(cred1.getPassword(), reqCred.getPassword());
      cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.PKI_KEYS);
      Assert.assertEquals(cred1.getPublicKey(), reqCred.getPublicKey());
      Assert.assertEquals(cred1.getPrivateKey(), reqCred.getPrivateKey());
      cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.ACCESS_KEY);
      Assert.assertEquals(cred1.getAccessKey(), reqCred.getAccessKey());
      Assert.assertEquals(cred1.getAccessSecret(), reqCred.getAccessSecret());
      // Verify we get credentials for default accessMethod if we do not specify an access method
      cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser);
      Assert.assertEquals(cred1.getPublicKey(), reqCred.getPublicKey());
      Assert.assertEquals(cred1.getPrivateKey(), reqCred.getPrivateKey());

      // Delete credentials and verify they were destroyed
      getClientUsr().deleteUserCredential(sys0[1], newPermsUser);
      try {
        cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.PASSWORD);
      } catch (TapisClientException tce) {
        Assert.assertTrue(tce.getTapisMessage().startsWith("SYSAPI_CRED_NOT_FOUND"), "Wrong exception message: " + tce.getTapisMessage());
        cred1 = null;
      }
      Assert.assertNull(cred1, "Credential not deleted. System name: " + sys0[1] + " User name: " + newPermsUser);

      // Attempt to delete again, should not throw an exception
      getClientUsr().deleteUserCredential(sys0[1], newPermsUser);

      // Set just ACCESS_KEY only and test
      reqCred = new ReqCreateCredential().accessKey("fakeAccessKey2").accessSecret("fakeAccessSecret2");
      getClientUsr().updateUserCredential(sys0[1], newPermsUser, reqCred);
      cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.ACCESS_KEY);
      Assert.assertEquals(cred1.getAccessKey(), reqCred.getAccessKey());
      Assert.assertEquals(cred1.getAccessSecret(), reqCred.getAccessSecret());
      // Attempt to retrieve secret that has not been set
      try {
        cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.PKI_KEYS);
      } catch (TapisClientException tce) {
        Assert.assertTrue(tce.getTapisMessage().startsWith("SYSAPI_CRED_NOT_FOUND"), "Wrong exception message: " + tce.getTapisMessage());
        cred1 = null;
      }
      Assert.assertNull(cred1, "Credential was non-null for missing secret. System name: " + sys0[1] + " User name: " + newPermsUser);
      // Delete credentials and verify they were destroyed
      getClientUsr().deleteUserCredential(sys0[1], newPermsUser);
      try {
        cred1 = getClientFilesSvc().getUserCredential(sys0[1], newPermsUser, AccessMethod.ACCESS_KEY);
      } catch (TapisClientException tce) {
        Assert.assertTrue(tce.getTapisMessage().startsWith("SYSAPI_CRED_NOT_FOUND"), "Wrong exception message: " + tce.getTapisMessage());
        cred1 = null;
      }
      Assert.assertNull(cred1, "Credential not deleted. System name: " + sys0[1] + " User name: " + newPermsUser);
      // Attempt to retrieve secret from non-existent system
      try {
        cred1 = getClientFilesSvc().getUserCredential("AMissingSystemName", newPermsUser, AccessMethod.PKI_KEYS);
      } catch (TapisClientException tce) {
        Assert.assertTrue(tce.getTapisMessage().startsWith("SYSAPI_NOSYSTEM"), "Wrong exception message: " + tce.getTapisMessage());
        cred1 = null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @AfterSuite
  public void tearDown() {
    System.out.println("Executing AfterSuite teardown method");
    // TODO: Run SQL to hard delete objects
    //Remove all objects created by tests, ignore any exceptions
    try {
      getClientUsr().deleteSystemByName(sys1[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sys2[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sys3[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sys4[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sys5[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sys6[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sys7[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sys8[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sys9[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sysA[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sysB[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sysC[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sysD[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sysE[1]);
    } catch (Exception e) {    }
    try {
      getClientUsr().deleteSystemByName(sysF[1]);
    } catch (Exception e) {    }
  }

  private String createSystem(String[] sys, AccessMethod accessMethod, Credential credential,
                              List<ReqCreateSystem.TransferMethodsEnum> txfrMethods, List<Capability> jobCaps)
          throws TapisClientException
  {
    ReqCreateSystem rSys = new ReqCreateSystem();
    rSys.setName(sys[1]);
    rSys.description(sys[2]);
    rSys.setSystemType(ReqCreateSystem.SystemTypeEnum.valueOf(sys[3]));
    rSys.owner(sys[4]);
    rSys.setHost(sys[5]);
    rSys.enabled(true);
    rSys.effectiveUserId(sys[6]);
    rSys.defaultAccessMethod(ReqCreateSystem.DefaultAccessMethodEnum.valueOf(accessMethod.name()));
    rSys.accessCredential(credential);
    rSys.bucketName(sys[8]);
    rSys.rootDir(sys[9]);
    rSys.setTransferMethods(txfrMethods);
    rSys.port(prot1Port).useProxy(prot1UseProxy).proxyHost(prot1ProxyHost).proxyPort(prot1ProxyPort);
    rSys.jobCanExec(true);
    rSys.jobLocalWorkingDir(sys[10]).jobLocalArchiveDir(sys[11]).jobRemoteArchiveSystem(sys[12]).jobRemoteArchiveDir(sys[13]);
    rSys.jobCapabilities(jobCaps);
    rSys.tags(tags1);
    rSys.notes(notes1JO);
    // Convert list of TransferMethod enums to list of strings
//    List<String> transferMethods = Stream.of(txfrMethodsStrList).map(TransferMethodsEnum::name).collect(Collectors.toList());
    // Create the system
    return getClientUsr().createSystem(rSys);
  }

  private ReqUpdateSystem createPatchSystem(String[] sys)
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

  private SystemsClient getClientUsr()
  {
    // Create the client each time due to issue with setting different headers needed by svc vs usr client
    SystemsClient clt = new SystemsClient(serviceURL, ownerUserJWT);
    // Creating a separate client for svc is not working because headers end up being used for all clients.
    // Underlying defaultHeaderMap is static so adding headers impacts all clients.
//    sysClientSvc = new SystemsClient(systemsURL, svcJWT);
//    sysClientSvc.addDefaultHeader("X-Tapis-User", sysOwner);
//    sysClientSvc.addDefaultHeader("X-Tapis-Tenant", tenantName);
    return clt;
  }
  private SystemsClient getClientUsr2()
  {
    // Create the client each time due to issue with setting different headers needed by svc vs usr client
    SystemsClient clt = new SystemsClient(serviceURL, newOwnerUserJWT);
    return clt;
  }
  private SystemsClient getClientFilesSvc()
  {
    // Create the client each time due to issue with setting different headers needed by svc vs usr client
    SystemsClient clt = new SystemsClient(serviceURL, filesServiceJWT);
    clt.addDefaultHeader("X-Tapis-User", ownerUser);
    clt.addDefaultHeader("X-Tapis-Tenant", tenantName);
    return clt;
  }

}

