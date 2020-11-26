package edu.utexas.tacc.tapis.systems.client;

/* Shared data and methods for running the systems client tests.
 *
 * Notes on environment required to run the tests:
 *  - Systems service base URL comes from the env or the default hard coded base URL.
 *  - Auth and tokens base URL comes from the env or the default hard coded base URL.
 *  - Auth service is used to get a short term JWT.
 *  - Tokens service is used to get a files service JWT.
 *  - Files service password must come from the environment: TAPIS_FILES_SVC_PASSWORD
 *
 * TAPIS_BASE_URL_SUFFIX should be set according to the dev, staging or prod environment
 *   dev     -> develop.tapis.io
 *   staging -> staging.tapis.io
 *   prod    -> tapis.io
 *
 *  To override base URLs use the following env variables:
 *    TAPIS_SVC_URL_SYSTEMS
 *    TAPIS_BASE_URL (for auth, tokens services)
 *  To override systems service port use:
 *    TAPIS_SERVICE_PORT
 *  NOTE that service port is ignored if TAPIS_SVC_URL_SYSTEMS is set
 */

import com.google.gson.JsonObject;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability.CategoryEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability.DatatypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;
import edu.utexas.tacc.tapis.systems.client.gen.model.LogicalQueue;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqUpdateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Utilities and data for integration testing
 */
public final class Utils
{
  // Test data
  public static final String tenantName = "dev";
  public static final String testUser1 = "testuser1";
  public static final String testUser2 = "testuser2";
  public static final String testUser3 = "testuser3";
  public static final String testUser4 = "testuser4";
  public static final String testUser9 = "testuser9";
  public static final String adminUser = testUser9;
  public static final String ownerUser1 = testUser1;
  public static final String ownerUser2 = testUser2;
  public static final String masterTenantName = "master";
  public static final String filesSvcName = "files";
  public static final String sysType = ReqCreateSystem.SystemTypeEnum.LINUX.name();
  // TAPIS_BASE_URL_SUFFIX should be set according to the dev, staging or prod environment
  // dev     -> develop.tapis.io
  // staging -> staging.tapis.io
  // prod    -> tapis.io
  // Default URLs. These can be overridden by env variables
  public static final String DEFAULT_BASE_URL = "https://" + tenantName + ".develop.tapis.io";
  public static final String DEFAULT_BASE_URL_SYSTEMS = "http://localhost";
  public static final String DEFAULT_SVC_PORT = "8080";
  // Env variables for setting URLs, passwords, etc
  public static final String TAPIS_ENV_BASE_URL = "TAPIS_BASE_URL";
  public static final String TAPIS_ENV_SVC_URL_SYSTEMS = "TAPIS_SVC_URL_SYSTEMS";
  public static final String TAPIS_ENV_FILES_SVC_PASSWORD = "TAPIS_FILES_SERVICE_PASSWORD";
  public static final String TAPIS_ENV_SVC_PORT = "TAPIS_SERVICE_PORT";

  public static final int prot1Port = 22, prot1ProxyPort = 0, prot2Port = 0, prot2ProxyPort = 2222;
  public static final boolean prot1UseProxy = false, prot2UseProxy = true;
  public static final String prot1ProxyHost = "proxyhost1", prot2ProxyHost = "proxyhost2";

  // Default system attributes
  public static final String defaultDescription = null;
  public static final String defaultRootDir = null;
  public static final String defaultBucketName = null;
  public static final String defaultJobWorkingDir = null;
  public static final String defaultBatchScheduler = null;
  public static final String defaultBatchDefaultLogicalQueue = null;
  public static final String defaultImportRefId = null;
  public static final String defaultEffectiveUserId = "${apiUserId}";
  public static final String defaultNotesStr = "{}";
  public static final boolean defaultIsEnabled = true;
  public static final int defaultPort = -1;
  public static final boolean defaultUseProxy = false;
  public static final int defaultProxyPort = -1;
  public static final String defaultProxyHost = "";
  public static final boolean defaultJobIsBatch = false;
  public static final int defaultJobMaxJobs = -1;
  public static final int defaultJobMaxJobsPerUser = -1;
  public static final int defaultQMaxJobs = -1;
  public static final int defaultQMaxJobsPerUser = -1;
  public static final int defaultQMaxNodeCount = -1;
  public static final int defaultQMaxCoresPerNode = -1;
  public static final int defaultQMaxMemoryMB = -1;
  public static final int defaultQMinutes = -1;
  public static final int defaultCapPrecedence = 100;

  public static final List<ReqCreateSystem.TransferMethodsEnum> prot1TxfrMethodsC =
          Arrays.asList(ReqCreateSystem.TransferMethodsEnum.SFTP, ReqCreateSystem.TransferMethodsEnum.S3);
  public static final List<TSystem.TransferMethodsEnum> prot1TxfrMethodsT =
          Arrays.asList(TSystem.TransferMethodsEnum.SFTP, TSystem.TransferMethodsEnum.S3);
  public static final List<ReqUpdateSystem.TransferMethodsEnum> prot2TxfrMethodsU =
          Collections.singletonList(ReqUpdateSystem.TransferMethodsEnum.SFTP);
  public static final List<TSystem.TransferMethodsEnum> prot2TxfrMethodsT =
          Collections.singletonList(TSystem.TransferMethodsEnum.SFTP);
  public static final SystemsClient.AuthnMethod prot1AuthnMethod = SystemsClient.AuthnMethod.PKI_KEYS;
  public static final SystemsClient.AuthnMethod prot2AuthnMethod = SystemsClient.AuthnMethod.ACCESS_KEY;
  public static final boolean canExec = true;
  public static final List<String> jobEnvVariables = Arrays.asList("a=b", "HOME=/home/testuser2", "TMP=/tmp");
  public static final boolean jobIsBatch = true;
  public static final int jobMaxJobs = -1;
  public static final int jobMaxJobsPerUser = -1;

  public static final LogicalQueue q1 = SystemsClient.buildLogicalQueue("logicalQueue1", 1, 1, 1, 1, 1, 1);
  public static final LogicalQueue q2 = SystemsClient.buildLogicalQueue("logicalQueue2", 2, 2, 2, 2, 2, 2);
  public static final List<LogicalQueue> jobQueues1 = new ArrayList<>(List.of(q1, q2));

  public static final List<String> tags1 = Arrays.asList("value1", "value2", "a",
          "Long tag (1 3 2) special chars [_ $ - & * % @ + = ! ^ ? < > , . ( ) { } / \\ | ]. Backslashes must be escaped.");
  public static final List<String> tags2 = Arrays.asList("value3", "value4");
  public static final JsonObject notes1JO =
          ClientTapisGsonUtils.getGson().fromJson("{\"project\":\"myproj1\", \"testdata\":\"abc1\"}", JsonObject.class);
  public static final JsonObject notes2JO =
          ClientTapisGsonUtils.getGson().fromJson("{\"project\":\"myproj2\", \"testdata\":\"abc2\"}", JsonObject.class);
  public static final List<String> testPerms = new ArrayList<>(List.of("READ", "MODIFY"));
  public static final List<String> testREADPerm = new ArrayList<>(List.of("READ"));
  public static final List<String> testREAD_EXECUTEPerms = new ArrayList<>(List.of("READ", "EXECUTE"));

  private static final int precedence = 100;
  private static final String subcategory = "";
  private static final Capability capA1 = SystemsClient.buildCapability(CategoryEnum.SCHEDULER, subcategory,  "Type",
                                                                        DatatypeEnum.STRING, precedence, "Slurm");
  private static final Capability capB1 = SystemsClient.buildCapability(CategoryEnum.HARDWARE, subcategory, "CoresPerNode",
                                                                        DatatypeEnum.INTEGER, precedence, "4");
  private static final Capability capC1 = SystemsClient.buildCapability(CategoryEnum.SOFTWARE, subcategory, "OpenMP",
                                                                        DatatypeEnum.STRING, precedence,"4.5");
  public static final List<Capability> jobCaps1 = new ArrayList<>(List.of(capA1, capB1, capC1));
  private static final Capability capA2 = SystemsClient.buildCapability(CategoryEnum.SCHEDULER, subcategory, "Type",
                                                                        DatatypeEnum.STRING, precedence, "Condor");
  private static final Capability capB2 = SystemsClient.buildCapability(CategoryEnum.HARDWARE, subcategory, "CoresPerNode",
                                                                        DatatypeEnum.INTEGER, precedence, "128");
  private static final Capability capC2 = SystemsClient.buildCapability(CategoryEnum.SOFTWARE, subcategory, "OpenMP",
                                                                        DatatypeEnum.STRING, precedence, "3.1");
  private static final Capability capD2 = SystemsClient.buildCapability(CategoryEnum.CONTAINER, subcategory, "Singularity",
                                                                        DatatypeEnum.STRING, precedence, null);
  public static final List<Capability> jobCaps2 = new ArrayList<>(List.of(capA2, capB2, capC2, capD2));
  public static final boolean isDeleted = false;
  public static final String importRefId = null;
  public static final Instant created = null;
  public static final Instant updated = null;
  public static final int qMaxJobs = -1;
  public static final int qMaxJobsPerUser = -1;
  public static final int qMaxNodeCount = -1;
  public static final int qMaxCoresPerNode = -1;
  public static final int qMaxMemoryMB = -1;
  public static final int qMaxMinutes = -1;

  public static final String sysNamePrefix = "CSys";

  // Strings for searches involving special characters
  public static final String specialChar7Str = ",()~*!\\"; // These 7 may need escaping
  public static final String specialChar7LikeSearchStr = "\\,\\(\\)\\~\\*\\!\\\\"; // All need escaping for LIKE/NLIKE
  public static final String specialChar7EqSearchStr = "\\,\\(\\)\\~*!\\"; // All but *! need escaping for other operators

  // String for search involving an escaped comma in a list of values
  public static final String escapedCommaInListValue = "abc\\,def";

  /**
   * Create an array of TSystem objects in memory
   * Names will be of format TestSys_K_NNN where K is the key and NNN runs from 000 to 999
   * We need a key because maven runs the tests in parallel so each set of systems created by an integration
   *   test will need its own namespace.
   * @param n number of systems to create
   * @return array of TSystem objects
   */
  public static Map<Integer, String[]> makeSystems(int n, String key)
  {
    Map<Integer, String[]> systems = new HashMap<>();
    for (int i = 1; i <= n; i++)
    {
      // Suffix which should be unique for each system within each integration test
      String suffix = key + "_" + String.format("%03d", i);
      String name = getSysName(key, i);
      // Constructor initializes all attributes except for JobCapabilities and Credential
      // String[] sys0 = 0=tenantName, 1=name, 2=description, 3=sysType, 4=ownerUser1, 5=host, 6=effUser, 7=password,
      //                 8=bucketName, 9=rootDir, 10=jobWorkingDir, 11=batchScheduler, 12=batchDefaultLogicalQueue
      String[] sys0 = {tenantName, name, "description " + suffix, sysType, ownerUser1, "host"+suffix, "effUser"+suffix,
              "fakePassword"+suffix,"bucket"+suffix, "/root"+suffix, "jobWorkDir"+suffix, "batchScheduler"+suffix,
              "batchDefaultLogicalQueue"+suffix};
      systems.put(i, sys0);
    }
    return systems;
  }

  public static String getFilesSvcPassword()
  {
    String s = System.getenv(TAPIS_ENV_FILES_SVC_PASSWORD);
    if (StringUtils.isBlank(s))
    {
      System.out.println("ERROR: Files service password must be set using environment variable:  " + TAPIS_ENV_FILES_SVC_PASSWORD);
      System.exit(1);
    }
    return s;
  }
  public static String getServicePort()
  {
    String s = System.getenv(TAPIS_ENV_SVC_PORT);
    System.out.println("Systems Service port from ENV: " + s);
    if (StringUtils.isBlank(s)) s = DEFAULT_SVC_PORT;
    return s;
  }
  public static String getServiceURL(String servicePort)
  {
    String s = System.getenv(TAPIS_ENV_SVC_URL_SYSTEMS);
    System.out.println("Systems Service URL from ENV: " + s);
    if (StringUtils.isBlank(s)) s = DEFAULT_BASE_URL_SYSTEMS + ":" + servicePort;
    return s;
  }
  public static String getBaseURL()
  {
    String s = System.getenv(TAPIS_ENV_BASE_URL);
    if (StringUtils.isBlank(s)) s = DEFAULT_BASE_URL;
    return s;
  }

  /*
   * Make client call to create a system given most attributes for the system.
   */
  public static String createSystem(SystemsClient clt, String[] sys, int port, SystemsClient.AuthnMethod authnMethod, Credential credential,
                             List<ReqCreateSystem.TransferMethodsEnum> txfrMethods)
          throws TapisClientException
  {
    var accMethod = authnMethod != null ? authnMethod : prot1AuthnMethod;
    var tMethods = txfrMethods != null ? txfrMethods : prot1TxfrMethodsC;
    ReqCreateSystem rSys = new ReqCreateSystem();
    rSys.setName(sys[1]);
    rSys.description(sys[2]);
    rSys.setSystemType(ReqCreateSystem.SystemTypeEnum.valueOf(sys[3]));
    rSys.owner(sys[4]);
    rSys.setHost(sys[5]);
    rSys.enabled(true);
    rSys.effectiveUserId(sys[6]);
    rSys.defaultAuthnMethod(ReqCreateSystem.DefaultAuthnMethodEnum.valueOf(accMethod.name()));
    rSys.authnCredential(credential);
    rSys.bucketName(sys[8]);
    rSys.rootDir(sys[9]);
    rSys.setTransferMethods(tMethods);
    rSys.port(port).useProxy(prot1UseProxy).proxyHost(prot1ProxyHost).proxyPort(prot1ProxyPort);
    rSys.canExec(canExec);
    rSys.jobWorkingDir(sys[10]);
    rSys.jobEnvVariables(jobEnvVariables);
    rSys.jobMaxJobs(jobMaxJobs).jobMaxJobsPerUser(jobMaxJobsPerUser);
    rSys.jobIsBatch(jobIsBatch);
    rSys.batchScheduler(sys[11]).batchDefaultLogicalQueue(sys[12]);
    rSys.batchLogicalQueues(jobQueues1);
    rSys.jobCapabilities(jobCaps1);
    rSys.tags(tags1);
    rSys.notes(notes1JO);
    // Convert list of TransferMethod enums to list of strings
//    List<String> transferMethods = Stream.of(txfrMethodsStrList).map(TransferMethodsEnum::name).collect(Collectors.toList());
    // Create the system
    return clt.createSystem(rSys);
  }

  /*
   * Make client call to create a system using minimal attributes for the system.
   *  String[] sys0 = 0=tenantName, 1=name, 2=description, 3=sysType, 4=ownerUser1, 5=host, 6=effUser, 7=password,
   *                  8=bucketName, 9=rootDir, 10=jobWorkingDir, 11=batchScheduler, 12=batchDefaultLogicalQueue
   */
  public static String createSystemMinimal(SystemsClient clt, String[] sys)
          throws TapisClientException
  {
    SystemsClient.AuthnMethod accMethod = prot1AuthnMethod;
    ReqCreateSystem rSys = new ReqCreateSystem();
    rSys.setName(sys[1]);
    rSys.setSystemType(ReqCreateSystem.SystemTypeEnum.valueOf(sys[3]));
    rSys.setHost(sys[5]);
    rSys.defaultAuthnMethod(ReqCreateSystem.DefaultAuthnMethodEnum.valueOf(accMethod.name()));
    rSys.canExec(canExec);
    // If systemType is LINUX then rootDir is required
    if (sys[3].equals(TSystem.SystemTypeEnum.LINUX.name())) rSys.rootDir(sys[9]);
    // If systemType is OBJECT_STORE then bucketName is required
    if (sys[3].equals(TSystem.SystemTypeEnum.OBJECT_STORE.name())) rSys.bucketName(sys[8]);
    return clt.createSystem(rSys);
  }

  public static SystemsClient getClientUsr(String serviceURL, String userJWT)
  {
    // Create the client each time due to issue with setting different headers needed by svc vs usr client

    // Creating a separate client for svc is not working because headers end up being used for all clients.
    // Underlying defaultHeaderMap is static so adding headers impacts all clients.
//    sysClientSvc = new SystemsClient(systemsURL, svcJWT);
//    sysClientSvc.addDefaultHeader("X-Tapis-User", sysOwner);
//    sysClientSvc.addDefaultHeader("X-Tapis-Tenant", tenantName);
    return new SystemsClient(serviceURL, userJWT);
  }

  public static String getSysName(String key, int idx)
  {
    String suffix = key + "_" + String.format("%03d", idx);
    return sysNamePrefix + "_" + suffix;
  }

  /**
   * Verify most attributes for a TSystem using default create data for following attributes:
   *     port, useProxy, proxyHost, proxyPort, defaultAuthnMethod, transferMethods,
   *     canExec, jobWorkingDir, jobMaxJobs, jobMaxJobsPerUser, jobIsBatch, batchScheduler, batchDefaultLogicalQueue,
   *     jobEnvVariables, jobLogicalQueues, capabilities, tags, notes
   * @param tmpSys - system retrieved from the service
   * @param sys0 - Data used to create the system
   */
  public static void verifySystemAttributes(TSystem tmpSys, String[] sys0)
  {
    Assert.assertEquals(tmpSys.getName(), sys0[1]);
    Assert.assertEquals(tmpSys.getDescription(), sys0[2]);
    Assert.assertEquals(tmpSys.getSystemType().name(), sys0[3]);
    Assert.assertEquals(tmpSys.getOwner(), sys0[4]);
    Assert.assertEquals(tmpSys.getHost(), sys0[5]);
    Assert.assertEquals(tmpSys.getEnabled(), Boolean.valueOf(defaultIsEnabled));
    Assert.assertEquals(tmpSys.getEffectiveUserId(), sys0[6]);
    Assert.assertEquals(tmpSys.getDefaultAuthnMethod().name(), prot1AuthnMethod.name());
    Assert.assertEquals(tmpSys.getBucketName(), sys0[8]);
    Assert.assertEquals(tmpSys.getRootDir(), sys0[9]);
    Assert.assertEquals(tmpSys.getPort().intValue(), prot1Port);
    Assert.assertEquals(tmpSys.getUseProxy().booleanValue(), prot1UseProxy);
    Assert.assertEquals(tmpSys.getProxyHost(), prot1ProxyHost);
    Assert.assertEquals(tmpSys.getProxyPort().intValue(), prot1ProxyPort);
    // Verify transfer methods
    List<TSystem.TransferMethodsEnum> tMethodsList = tmpSys.getTransferMethods();
    Assert.assertNotNull(tMethodsList, "TransferMethods list should not be null");
    for (TSystem.TransferMethodsEnum txfrMethod : prot1TxfrMethodsT)
    {
      Assert.assertTrue(tMethodsList.contains(txfrMethod), "List of transfer methods did not contain: " + txfrMethod.name());
    }
    Assert.assertEquals(tmpSys.getCanExec(), Boolean.valueOf(canExec));
    Assert.assertEquals(tmpSys.getJobWorkingDir(), sys0[10]);
    Assert.assertEquals(tmpSys.getJobMaxJobs().intValue(), jobMaxJobs);
    Assert.assertEquals(tmpSys.getJobMaxJobsPerUser().intValue(), jobMaxJobsPerUser);
    Assert.assertEquals(tmpSys.getJobIsBatch(), Boolean.valueOf(jobIsBatch));
    Assert.assertEquals(tmpSys.getBatchScheduler(), sys0[11]);
    Assert.assertEquals(tmpSys.getBatchDefaultLogicalQueue(), sys0[12]);
    // Verify jobEnvVariables
    List<String> tmpJobEnvVariables = tmpSys.getJobEnvVariables();
    Assert.assertNotNull(tmpJobEnvVariables, "JobEnvVariables value was null");
    Assert.assertEquals(tmpJobEnvVariables.size(), jobEnvVariables.size(), "Wrong number of JobEnvVariables");
    for (String varStr : jobEnvVariables)
    {
      Assert.assertTrue(tmpJobEnvVariables.contains(varStr));
      System.out.println("Found JobEnvVariable: " + varStr);
    }
    // Verify batchLogicalQueues
    List<LogicalQueue> jobQueues = tmpSys.getBatchLogicalQueues();
// TODO logical queues not yet implemented.
//    Assert.assertNotNull(jobQueues);
//    Assert.assertEquals(jobQueues.size(), jobQueues1.size());
//    var qNamesFound = new ArrayList<String>();
//    for (LogicalQueue qFound : jobQueues) {qNamesFound.add(qFound.getName());}
//    for (LogicalQueue qSeed : jobQueues1)
//    {
//      Assert.assertTrue(qNamesFound.contains(qSeed.getName()), "List of logical queues did not contain a queue named: " + qSeed.getName());
//    }
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
    // TODO: Currently the client converts notes from a gson LinkedTreeMap to a string of json so we cast it to String here.
    // TODO/TBD: Can we update jsonschema and model on server to make it a String instead of Object?
    //           Previously this caused issues with json serialization.
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

  /**
   * Verify the required attributes for a TSystem
   *   and verify that other attributes are set to expected defaults.
   * @param tmpSys - system retrieved from the service
   * @param sys0 - Data used to create the system
   */
  public static void verifySystemDefaults(TSystem tmpSys, String[] sys0)
  {
    // Verify required attributes
    Assert.assertEquals(tmpSys.getName(), sys0[1]);
    Assert.assertEquals(tmpSys.getSystemType().name(), sys0[3]);
    Assert.assertEquals(tmpSys.getHost(), sys0[5]);
    Assert.assertEquals(tmpSys.getDefaultAuthnMethod().name(), prot1AuthnMethod.name());
    Assert.assertEquals(tmpSys.getCanExec(), Boolean.valueOf(canExec));

    // If systemType is LINUX then rootDir is required
    if (tmpSys.getSystemType() == TSystem.SystemTypeEnum.LINUX) Assert.assertEquals(tmpSys.getRootDir(), sys0[9]);
    else Assert.assertEquals(tmpSys.getRootDir(), defaultRootDir);
    // If systemType is OBJECT_STORE then bucketName is required
    if (tmpSys.getSystemType() == TSystem.SystemTypeEnum.OBJECT_STORE) Assert.assertEquals(tmpSys.getBucketName(), sys0[8]);
    else Assert.assertEquals(tmpSys.getBucketName(), defaultBucketName);

    // Verify optional attributes have been set to defaults
    // Owner should have defaulted to user who created the system
    Assert.assertEquals(tmpSys.getOwner(), ownerUser1);
    Assert.assertEquals(tmpSys.getDescription(), defaultDescription);
    Assert.assertEquals(tmpSys.getEnabled(), Boolean.valueOf(defaultIsEnabled));
    // Effective user should result to requestor which in this case is testuser1
    Assert.assertEquals(tmpSys.getEffectiveUserId(), testUser1);
    Assert.assertNotNull(tmpSys.getTransferMethods());
    Assert.assertTrue(tmpSys.getTransferMethods().isEmpty());
    Assert.assertEquals(tmpSys.getPort().intValue(), defaultPort);
    Assert.assertEquals(tmpSys.getUseProxy().booleanValue(), defaultUseProxy);
    Assert.assertEquals(tmpSys.getProxyHost(), defaultProxyHost);
    Assert.assertEquals(tmpSys.getProxyPort().intValue(), defaultProxyPort);
    Assert.assertEquals(tmpSys.getJobWorkingDir(), defaultJobWorkingDir);
    Assert.assertNotNull(tmpSys.getJobEnvVariables());
    Assert.assertTrue(tmpSys.getJobEnvVariables().isEmpty());
    Assert.assertEquals(tmpSys.getJobMaxJobs().intValue(), defaultJobMaxJobs);
    Assert.assertEquals(tmpSys.getJobMaxJobsPerUser().intValue(), defaultJobMaxJobsPerUser);
    Assert.assertEquals(tmpSys.getJobIsBatch(), Boolean.valueOf(defaultJobIsBatch));
    Assert.assertEquals(tmpSys.getBatchScheduler(), defaultBatchScheduler);
    Assert.assertEquals(tmpSys.getBatchDefaultLogicalQueue(), defaultBatchDefaultLogicalQueue);
    Assert.assertNotNull(tmpSys.getBatchLogicalQueues());
    Assert.assertTrue(tmpSys.getBatchLogicalQueues().isEmpty());
    Assert.assertNotNull(tmpSys.getJobCapabilities());
    Assert.assertTrue(tmpSys.getJobCapabilities().isEmpty());
    Assert.assertNotNull(tmpSys.getTags());
    Assert.assertTrue(tmpSys.getTags().isEmpty());
    Assert.assertNotNull(tmpSys.getNotes());
    Assert.assertEquals((String) tmpSys.getNotes(), defaultNotesStr);
    Assert.assertEquals(tmpSys.getImportRefId(), defaultImportRefId);
  }
}
