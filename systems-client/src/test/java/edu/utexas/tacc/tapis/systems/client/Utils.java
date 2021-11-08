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
import edu.utexas.tacc.tapis.systems.client.gen.model.CategoryEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.DatatypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.AuthnEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;
import edu.utexas.tacc.tapis.systems.client.gen.model.JobRuntime;
import edu.utexas.tacc.tapis.systems.client.gen.model.KeyValuePair;
import edu.utexas.tacc.tapis.systems.client.gen.model.LogicalQueue;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSchedulerProfile;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.RuntimeTypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.SchedulerHiddenOptionEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.SchedulerTypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.SystemTypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.TapisSystem;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
  public static final String adminUser = "testadmin";
  public static final String adminTenantName = "admin";
  public static final String filesSvcName = "files";
  public static final String sysType = SystemTypeEnum.LINUX.name();
  public static final String schedulerProfileName = "taccTest";

  // Long term JWTs expire approx 1 Sep 2022 (DEV Env only)
  public static final String testUser1JWT ="eyJ0eXATODO";
  public static final String testUser2JWT ="eyJ0eXATODO";
  public static final String testUser3JWT = "eyJ0eXATODO";
  public static final String adminUserJWT ="eyJ0eXATODO";
  public static final String filesSvcJWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJmMDA1Njg5Mi1iMGYyLTRkMjQtYjNjNy1hM2I2ODYxOGY3MzQiLCJpc3MiOiJodHRwczovL2FkbWluLmRldmVsb3AudGFwaXMuaW8vdjMvdG9rZW5zIiwic3ViIjoiZmlsZXNAYWRtaW4iLCJ0YXBpcy90ZW5hbnRfaWQiOiJhZG1pbiIsInRhcGlzL3Rva2VuX3R5cGUiOiJhY2Nlc3MiLCJ0YXBpcy9kZWxlZ2F0aW9uIjpmYWxzZSwidGFwaXMvZGVsZWdhdGlvbl9zdWIiOm51bGwsInRhcGlzL3VzZXJuYW1lIjoiZmlsZXMiLCJ0YXBpcy9hY2NvdW50X3R5cGUiOiJzZXJ2aWNlIiwiZXhwIjoxNjYyMTQ0MTg2LCJ0YXBpcy90YXJnZXRfc2l0ZSI6InRhY2MifQ.DpNR3WEh91b05-aBCMn2CjqvznP80rgCLvDi4Z1J8uTPXgOBrIjDrmBT8cT2W3sJ12Fp4N5Twlf90TPAisHdpkzaPxOH-9LvMplDAkw7AZ_3BIkqBLLTKRNMkFvm35D_pTV3Az9fu_zL0u-yD8QM_sWqD7VXCimAC3h8LO7NBOR3dT8tNeVvfs-JI_emeG1yA3eN29YiSg2vpHrPWRZY3MVX8UG_YhSO0HS34OT6V2WwLqJfQ_BXUdEOC8ru_jK1b9Grs0_uYLaPEvs_RJ8egRNg8UKc6kiLSSMZ4L3wEfGZvrXuGTFG0Av99h89hSOfUlw1byUp_CJq9erFfqRhpg";
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

  public static final String hostPatchedId = "patched.system.org";
  public static final String hostMinimalId = "minimal.system.org";

  // Default attributes
  public static final String defaultDescription = null;
  public static final String defaultRootDir = null;
  public static final String defaultBucketName = null;
  public static final String defaultJobWorkingDir = null;
  public static final String defaultBatchScheduler = SchedulerTypeEnum.SLURM.toString();
  public static final String defaultBatchDefaultLogicalQueue = null;
  public static final String defaultBatchSchedulerProfile = null;
  public static final String defaultEffectiveUserId = "${apiUserId}";
  public static final String defaultNotesStr = "{}";
  public static final boolean defaultIsEnabled = true;
  public static final int defaultPort = -1;
  public static final boolean defaultUseProxy = false;
  public static final int defaultProxyPort = -1;
  public static final String defaultProxyHost = null;
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

  public static final SystemsClient.AuthnMethod prot1AuthnMethod = SystemsClient.AuthnMethod.PKI_KEYS;
  public static final SystemsClient.AuthnMethod prot2AuthnMethod = SystemsClient.AuthnMethod.ACCESS_KEY;
  public static final boolean canExecTrue = true;
  public static final boolean canExecFalse = false;
  public static final KeyValuePair kv1 = new KeyValuePair().key("a").value("b");
  public static final KeyValuePair kv2 = new KeyValuePair().key("HOME").value("/home/testuser2");
  public static final KeyValuePair kv3 = new KeyValuePair().key("TMP").value("/tmp");
  public static final List<KeyValuePair> jobEnvVariables = new ArrayList<>(List.of(kv1,kv2,kv3));
  public static final boolean jobIsBatchTrue = true;
  public static final boolean jobIsBatchFalse = false;
  public static final int jobMaxJobs = -1;
  public static final int jobMaxJobsMAX = Integer.MAX_VALUE;
  public static final int jobMaxJobsPerUser = -1;
  public static final int jobMaxJobsPerUserMAX = Integer.MAX_VALUE;

  public static final List<JobRuntime> jobRuntimesEmpty = new ArrayList<>();
  public static final JobRuntime runtimeDocker =
          new JobRuntime().runtimeType(RuntimeTypeEnum.DOCKER).version("0.0.1runtime");
  public static final List<JobRuntime> jobRuntimes1 = new ArrayList<>(List.of(runtimeDocker));

  public static final LogicalQueue q1 = SystemsClient.buildLogicalQueue("logicalQ1", "hpcQ1", 1, 1, 0, 1, 0, 1, 0, 1, 0, 1);
  public static final LogicalQueue q2 = SystemsClient.buildLogicalQueue("logicalQ2", "hpcQ2", 2, 2, 0, 2, 0, 2, 0, 2, 0, 2);
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
  private static final Capability capA1 = SystemsClient.buildCapability(CategoryEnum.SCHEDULER, "Type",
                                                                        DatatypeEnum.STRING, precedence, "Slurm");
  private static final Capability capB1 = SystemsClient.buildCapability(CategoryEnum.HARDWARE, "CoresPerNode",
                                                                        DatatypeEnum.INTEGER, precedence, "4");
  private static final Capability capC1 = SystemsClient.buildCapability(CategoryEnum.SOFTWARE, "OpenMP",
                                                                        DatatypeEnum.STRING, precedence,"4.5");
  public static final List<Capability> jobCaps1 = new ArrayList<>(List.of(capA1, capB1, capC1));
  private static final Capability capA2 = SystemsClient.buildCapability(CategoryEnum.SCHEDULER, "Type",
                                                                        DatatypeEnum.STRING, precedence, "Condor");
  private static final Capability capB2 = SystemsClient.buildCapability(CategoryEnum.HARDWARE, "CoresPerNode",
                                                                        DatatypeEnum.INTEGER, precedence, "128");
  private static final Capability capC2 = SystemsClient.buildCapability(CategoryEnum.SOFTWARE, "OpenMP",
                                                                        DatatypeEnum.STRING, precedence, "3.1");
  private static final Capability capD2 = SystemsClient.buildCapability(CategoryEnum.CONTAINER, "Singularity",
                                                                        DatatypeEnum.STRING, precedence, null);
  public static final List<Capability> jobCaps2 = new ArrayList<>(List.of(capA2, capB2, capC2, capD2));
  public static final boolean isDeleted = false;
  public static final Instant created = null;
  public static final Instant updated = null;
  public static final int qMaxJobs = -1;
  public static final int qMaxJobsPerUser = -1;
  public static final int qMaxNodeCount = -1;
  public static final int qMaxCoresPerNode = -1;
  public static final int qMaxMemoryMB = -1;
  public static final int qMaxMinutes = -1;

  public static final Credential credNull = null;

  public static final String sysNamePrefix = "CSys";
  public static final String schedProfileNamePrefix = "CTestSchedProfile";

  public static final List<String> modulesToLoad = Arrays.asList("value1", "value2");
  public static final List<SchedulerHiddenOptionEnum> hiddenOptions = Arrays.asList(SchedulerHiddenOptionEnum.MEM);
  // Strings for searches involving special characters
  public static final String specialChar7Str = ",()~*!\\"; // These 7 may need escaping
  public static final String specialChar7LikeSearchStr = "\\,\\(\\)\\~\\*\\!\\\\"; // All need escaping for LIKE/NLIKE
  public static final String specialChar7EqSearchStr = "\\,\\(\\)\\~*!\\"; // All but *! need escaping for other operators

  // String for search involving an escaped comma in a list of values
  public static final String escapedCommaInListValue = "abc\\,def";

  /**
   * Create an array of TapisSystem objects in memory
   * Names will be of format TestSys_K_NNN where K is the key and NNN runs from 000 to 999
   * We need a key because maven runs the tests in parallel so each set of systems created by an integration
   *   test will need its own namespace.
   * @param n number of systems to create
   * @return array of TapisSystem objects
   */
  public static Map<Integer, String[]> makeSystems(int n, String key)
  {
    Map<Integer, String[]> systems = new HashMap<>();
    for (int i = 1; i <= n; i++)
    {
      // Suffix which should be unique for each system within each integration test
      String iStr = String.format("%03d", i);
      String suffix = key + "_" + iStr;
      String id = getSysId(key, i);
      String hostName = "host" + key + iStr + ".test.org";
      // Constructor initializes all attributes except for JobCapabilities and Credential
      // String[] sys0 = 0=tenantName, 1=name, 2=description, 3=sysType, 4=ownerUser1, 5=host, 6=effUser, 7=password,
      //                 8=bucketName, 9=rootDir, 10=jobWorkingDir, 11=batchScheduler, 12=batchDefaultLogicalQueue,
      //                 13=batchSchedulerProfile
      String[] sys0 = {tenantName, id, "description "+suffix, sysType, testUser1, hostName, "effUser"+suffix,
              "fakePassword"+suffix,"bucket"+suffix, "/root"+suffix, "jobWorkDir"+suffix, SchedulerTypeEnum.SLURM.name(),
              "batchDefaultLogicalQueue"+suffix, schedulerProfileName};
      systems.put(i, sys0);
    }
    return systems;
  }

  /**
   * Create an array of SchedulerProfile objects in memory
   * Names will be of format TestSchedProfile_K_NNN where K is the key and NNN runs from 000 to 999
   * We need a key because maven runs the tests in parallel so each set of profiles created by an integration
   *   test will need its own namespace.
   * @param n number of objects to create
   * @return array of objects
   */
  public static Map<Integer, String[]> makeSchedulerProfiles(int n, String key)
  {
    Map<Integer, String[]> profiles = new HashMap<>();
    for (int i = 1; i <= n; i++)
    {
      // Suffix which should be unique for each profile within each integration test
      String iStr = String.format("%03d", i);
      String suffix = key + "_" + iStr;
      String name = getSchedulerProfileName(key, i);
      String moduleLoadCmd = "module load" + suffix;
      String[] p0 = {tenantName, name, "description "+suffix, testUser1, moduleLoadCmd};
      profiles.put(i, p0);
    }
    return profiles;
  }
  public static String getFilesSvcPassword()
  {
    String s = System.getenv(TAPIS_ENV_FILES_SVC_PASSWORD);
//    if (StringUtils.isBlank(s))
//    {
//      System.out.println("ERROR: Files service password must be set using environment variable:  " + TAPIS_ENV_FILES_SVC_PASSWORD);
//      System.exit(1);
//    }
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
   * Build a ReqCreateSystem object to be used for client call to create a system given most attributes for the system.
   */
  public static ReqCreateSystem createReqSystem(String[] sys, int port, SystemsClient.AuthnMethod authnMethod,
                                                Credential credential)
  {
    var accMethod = authnMethod != null ? authnMethod : prot1AuthnMethod;
    ReqCreateSystem rSys = new ReqCreateSystem();
    rSys.setId(sys[1]);
    rSys.description(sys[2]);
    rSys.setSystemType(SystemTypeEnum.valueOf(sys[3]));
    rSys.owner(sys[4]);
    rSys.setHost(sys[5]);
    rSys.enabled(true);
    rSys.effectiveUserId(sys[6]);
    rSys.defaultAuthnMethod(AuthnEnum.valueOf(accMethod.name()));
    rSys.authnCredential(SystemsClient.buildReqCreateCredential(credential));
    rSys.bucketName(sys[8]);
    rSys.rootDir(sys[9]);
    rSys.port(port).useProxy(prot1UseProxy).proxyHost(prot1ProxyHost).proxyPort(prot1ProxyPort);
    rSys.canExec(canExecTrue);
    rSys.setJobRuntimes(jobRuntimes1);
    rSys.jobWorkingDir(sys[10]);
    rSys.jobEnvVariables(jobEnvVariables);
    rSys.jobMaxJobs(jobMaxJobs).jobMaxJobsPerUser(jobMaxJobsPerUser);
    rSys.jobIsBatch(jobIsBatchFalse);
    rSys.batchScheduler(SchedulerTypeEnum.fromValue(sys[11])).batchDefaultLogicalQueue(sys[12]);
    rSys.batchSchedulerProfile(sys[13]).batchLogicalQueues(jobQueues1);
    rSys.jobCapabilities(jobCaps1);
    rSys.tags(tags1);
    rSys.notes(notes1JO);
    return rSys;
  }

  /*
   * Make client call to create a system using minimal attributes for the system.
   *  Use attributes from:
   *  String[] sys0 = 0=tenantName, 1=name, 2=description, 3=sysType, 4=ownerUser1, 5=host, 6=effUser, 7=password,
   *                  8=bucketName, 9=rootDir, 10=jobWorkingDir, 11=batchScheduler, 12=batchDefaultLogicalQueue
   */
  public static String createSystemMinimal(SystemsClient clt, String[] sys)
          throws TapisClientException
  {
    ReqCreateSystem rSys = new ReqCreateSystem();
    rSys.setId(sys[1]);
    rSys.setSystemType(SystemTypeEnum.valueOf(sys[3]));
    rSys.setHost(sys[5]);
    rSys.defaultAuthnMethod(AuthnEnum.valueOf(prot1AuthnMethod.name()));
    rSys.canExec(canExecFalse);
    // If systemType is LINUX then rootDir is required
    if (sys[3].equals(SystemTypeEnum.LINUX.name())) rSys.rootDir(sys[9]);
    // If systemType is S3 then bucketName is required
    if (sys[3].equals(SystemTypeEnum.S3.name())) rSys.bucketName(sys[8]);
    return clt.createSystem(rSys);
  }

  /*
   * Make client call to create a system using attributes from a TapisSystem.
   * Use attributes from sys
   */
  public static String createSystemFromTapisSystem(SystemsClient clt, TapisSystem sys)
          throws TapisClientException
  {
    ReqCreateSystem rSys = SystemsClient.buildReqCreateSystem(sys);
    return clt.createSystem(rSys);
  }

  /*
   * Build a ReqCreateSchedulerProfile object to be used for client call to create a profile give most attributes.
   */
  public static ReqCreateSchedulerProfile createReqSchedulerProfile(String[] profile)
  {
    ReqCreateSchedulerProfile rProfile = new ReqCreateSchedulerProfile();
    rProfile.setName(profile[1]);
    rProfile.description(profile[2]);
    rProfile.owner(profile[3]);
    rProfile.moduleLoadCommand(profile[4]);
    rProfile.modulesToLoad(modulesToLoad);
    rProfile.hiddenOptions(hiddenOptions);
    return rProfile;
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

  public static String getSysId(String key, int idx)
  {
    String suffix = key + "_" + String.format("%03d", idx);
    return sysNamePrefix + "_" + suffix;
  }

  public static String getSchedulerProfileName(String key, int idx)
  {
    String suffix = key + "_" + String.format("%03d", idx);
    return schedProfileNamePrefix + "_" + suffix;
  }

  /**
   * Verify most attributes for a TapisSystem using default create data for following attributes:
   *     port, useProxy, proxyHost, proxyPort, defaultAuthnMethod,
   *     canExec, jobWorkingDir, jobMaxJobs, jobMaxJobsPerUser, jobIsBatch, batchScheduler, batchDefaultLogicalQueue,
   *     batchSchedulerProfile, jobEnvVariables, jobLogicalQueues, capabilities, tags, notes
   * @param tmpSys - system retrieved from the service
   * @param sys0 - Data used to create the system
   */
  public static void verifySystemAttributes(TapisSystem tmpSys, String[] sys0)
  {
    Assert.assertEquals(tmpSys.getId(), sys0[1]);
    Assert.assertEquals(tmpSys.getDescription(), sys0[2]);
    Assert.assertNotNull(tmpSys.getSystemType());
    Assert.assertEquals(tmpSys.getSystemType().name(), sys0[3]);
    Assert.assertEquals(tmpSys.getOwner(), sys0[4]);
    Assert.assertEquals(tmpSys.getHost(), sys0[5]);
    Assert.assertEquals(tmpSys.getEnabled(), Boolean.valueOf(defaultIsEnabled));
    Assert.assertEquals(tmpSys.getEffectiveUserId(), sys0[6]);
    Assert.assertNotNull(tmpSys.getDefaultAuthnMethod());
    Assert.assertEquals(tmpSys.getDefaultAuthnMethod().name(), prot1AuthnMethod.name());
    Assert.assertEquals(tmpSys.getBucketName(), sys0[8]);
    Assert.assertEquals(tmpSys.getRootDir(), sys0[9]);
    Assert.assertNotNull(tmpSys.getPort());
    Assert.assertEquals(tmpSys.getPort().intValue(), prot1Port);
    Assert.assertNotNull(tmpSys.getUseProxy());
    Assert.assertEquals(tmpSys.getUseProxy().booleanValue(), prot1UseProxy);
    Assert.assertEquals(tmpSys.getProxyHost(), prot1ProxyHost);
    Assert.assertNotNull(tmpSys.getProxyPort());
    Assert.assertEquals(tmpSys.getProxyPort().intValue(), prot1ProxyPort);
    Assert.assertEquals(tmpSys.getCanExec(), Boolean.valueOf(canExecTrue));
    Assert.assertEquals(tmpSys.getJobWorkingDir(), sys0[10]);
    // TODO check jobRuntimes
    Assert.assertNotNull(tmpSys.getJobMaxJobs());
    Assert.assertEquals(tmpSys.getJobMaxJobs().intValue(), jobMaxJobsMAX);
    Assert.assertNotNull(tmpSys.getJobMaxJobsPerUser());
    Assert.assertEquals(tmpSys.getJobMaxJobsPerUser().intValue(), jobMaxJobsPerUserMAX);
    Assert.assertEquals(tmpSys.getJobIsBatch(), Boolean.valueOf(jobIsBatchFalse));
    Assert.assertEquals(tmpSys.getBatchScheduler(), SchedulerTypeEnum.valueOf(sys0[11]));
    Assert.assertEquals(tmpSys.getBatchDefaultLogicalQueue(), sys0[12]);
    Assert.assertEquals(tmpSys.getBatchSchedulerProfile(), sys0[13]);
    // Verify jobEnvVariables
    List<KeyValuePair> tmpJobEnvVariables = tmpSys.getJobEnvVariables();
    Assert.assertNotNull(tmpJobEnvVariables, "JobEnvVariables value was null");
    Assert.assertEquals(tmpJobEnvVariables.size(), jobEnvVariables.size(), "Wrong number of JobEnvVariables");
    for (KeyValuePair kv : jobEnvVariables)
    {
      // TODO
//      Assert.assertTrue(tmpJobEnvVariables.contains(varStr));
      System.out.println("Found JobEnvVariable: " + kv.getKey() + "=" + kv.getValue());
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

    Assert.assertNotNull(tmpSys.getCreated(), "Fetched created timestamp should not be null.");
    Assert.assertNotNull(tmpSys.getUpdated(), "Fetched updated timestamp should not be null.");
  }

  /**
   * Verify the required attributes for a TapisSystem
   *   and verify that other attributes are set to expected defaults.
   * @param tmpSys - system retrieved from the service
   * @param sys0 - Data used to create the system
   */
  public static void verifySystemDefaults(TapisSystem tmpSys, String[] sys0, String sysId)
  {
    Assert.assertEquals(tmpSys.getTenant(), tenantName);
    // Verify required attributes
    Assert.assertEquals(tmpSys.getId(), sysId);
    Assert.assertNotNull(tmpSys.getSystemType());
    Assert.assertEquals(tmpSys.getSystemType().name(), sys0[3]);
    Assert.assertEquals(tmpSys.getHost(), sys0[5]);
    Assert.assertNotNull(tmpSys.getDefaultAuthnMethod());
    Assert.assertEquals(tmpSys.getDefaultAuthnMethod().name(), prot1AuthnMethod.name());
    Assert.assertEquals(tmpSys.getCanExec(), Boolean.valueOf(canExecFalse));

    SchedulerTypeEnum schedulerType = (tmpSys.getBatchScheduler() == null) ? null : SchedulerTypeEnum.valueOf(defaultBatchScheduler);
    // If systemType is LINUX then rootDir is required
    if (tmpSys.getSystemType() == SystemTypeEnum.LINUX) Assert.assertEquals(tmpSys.getRootDir(), sys0[9]);
    else Assert.assertEquals(tmpSys.getRootDir(), defaultRootDir);
    // If systemType is S3 then bucketName is required
    if (tmpSys.getSystemType() == SystemTypeEnum.S3) Assert.assertEquals(tmpSys.getBucketName(), sys0[8]);
    else Assert.assertEquals(tmpSys.getBucketName(), defaultBucketName);

    // Verify optional attributes have been set to defaults
    // Owner should have defaulted to user who created the system
    Assert.assertEquals(tmpSys.getOwner(), testUser1);
    Assert.assertEquals(tmpSys.getDescription(), defaultDescription);
    Assert.assertEquals(tmpSys.getEnabled(), Boolean.valueOf(defaultIsEnabled));
    // Effective user should result to requestor which in this case is testuser1
    Assert.assertEquals(tmpSys.getEffectiveUserId(), testUser1);
    Assert.assertNull(tmpSys.getJobRuntimes());
    Assert.assertNotNull(tmpSys.getPort());
    Assert.assertEquals(tmpSys.getPort().intValue(), defaultPort);
    Assert.assertNotNull(tmpSys.getUseProxy());
    Assert.assertEquals(tmpSys.getUseProxy().booleanValue(), defaultUseProxy);
    Assert.assertEquals(tmpSys.getProxyHost(), defaultProxyHost);
    Assert.assertNotNull(tmpSys.getProxyPort());
    Assert.assertEquals(tmpSys.getProxyPort().intValue(), defaultProxyPort);
    Assert.assertEquals(tmpSys.getJobWorkingDir(), defaultJobWorkingDir);
    Assert.assertNotNull(tmpSys.getJobEnvVariables());
    Assert.assertTrue(tmpSys.getJobEnvVariables().isEmpty());
    Assert.assertNotNull(tmpSys.getJobMaxJobs());
    Assert.assertEquals(tmpSys.getJobMaxJobs().intValue(), jobMaxJobsMAX);
    Assert.assertNotNull(tmpSys.getJobMaxJobsPerUser());
    Assert.assertEquals(tmpSys.getJobMaxJobsPerUser().intValue(), jobMaxJobsPerUserMAX);
    Assert.assertEquals(tmpSys.getJobIsBatch(), Boolean.valueOf(defaultJobIsBatch));
    Assert.assertEquals(tmpSys.getBatchScheduler(), schedulerType);
    Assert.assertEquals(tmpSys.getBatchDefaultLogicalQueue(), defaultBatchDefaultLogicalQueue);
    Assert.assertEquals(tmpSys.getBatchSchedulerProfile(), defaultBatchSchedulerProfile);
    Assert.assertNotNull(tmpSys.getBatchLogicalQueues());
    Assert.assertTrue(tmpSys.getBatchLogicalQueues().isEmpty());
    Assert.assertNotNull(tmpSys.getJobCapabilities());
    Assert.assertTrue(tmpSys.getJobCapabilities().isEmpty());
    Assert.assertNotNull(tmpSys.getTags());
    Assert.assertTrue(tmpSys.getTags().isEmpty());
    Assert.assertNotNull(tmpSys.getNotes());
    Assert.assertEquals((String) tmpSys.getNotes(), defaultNotesStr);
  }
}
