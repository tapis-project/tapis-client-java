package edu.utexas.tacc.tapis.apps.client;

/* Shared data and methods for running the apps client tests.
 *
 * Notes on environment required to run the tests:
 *  - Apps service base URL comes from the env or the default hard coded base URL.
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
 *    TAPIS_SVC_URL_APPS
 *    TAPIS_BASE_URL (for auth, tokens services)
 *  To override apps service port use:
 *    TAPIS_SERVICE_PORT
 *  NOTE that service port is ignored if TAPIS_SVC_URL_APPS is set
 */

import com.google.gson.JsonObject;
import edu.utexas.tacc.tapis.apps.client.gen.model.AppTypeEnum;
import edu.utexas.tacc.tapis.apps.client.gen.model.ArgMetaSpec;
import edu.utexas.tacc.tapis.apps.client.gen.model.ArgSpec;
import edu.utexas.tacc.tapis.apps.client.gen.model.FileInputDefinition;
import edu.utexas.tacc.tapis.apps.client.gen.model.JobAttributes;
import edu.utexas.tacc.tapis.apps.client.gen.model.KeyValuePair;
import edu.utexas.tacc.tapis.apps.client.gen.model.NotificationMechanism;
import edu.utexas.tacc.tapis.apps.client.gen.model.NotificationMechanismEnum;
import edu.utexas.tacc.tapis.apps.client.gen.model.NotificationSubscription;
import edu.utexas.tacc.tapis.apps.client.gen.model.ParameterSet;
import edu.utexas.tacc.tapis.apps.client.gen.model.ParameterSetArchiveFilter;
import edu.utexas.tacc.tapis.apps.client.gen.model.RuntimeEnum;
import edu.utexas.tacc.tapis.apps.client.gen.model.RuntimeOptionEnum;
import edu.utexas.tacc.tapis.apps.client.gen.model.TapisApp;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqCreateApp;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Utilities and data for testing
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
  // testuser9 must be given role "$!tenant_admin"
  public static final String adminUser = testUser9;
  public static final String ownerUser1 = testUser1;
  public static final String ownerUser2 = testUser2;
  public static final String adminTenantName = "admin";
  public static final String filesSvcName = "files";

  // Long term JWT for testuser1 - expires approx 1 July 2026
  public static final String testUser1JWT ="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIwZDg0YWRlOC0yMzQwLTQzOGQtOGJiMy1jZTFhYjg0M2I1NjYiLCJpc3MiOiJodHRwczovL2Rldi5kZXZlbG9wLnRhcGlzLmlvL3YzL3Rva2VucyIsInN1YiI6InRlc3R1c2VyMUBkZXYiLCJ0YXBpcy90ZW5hbnRfaWQiOiJkZXYiLCJ0YXBpcy90b2tlbl90eXBlIjoiYWNjZXNzIiwidGFwaXMvZGVsZWdhdGlvbiI6ZmFsc2UsInRhcGlzL2RlbGVnYXRpb25fc3ViIjpudWxsLCJ0YXBpcy91c2VybmFtZSI6InRlc3R1c2VyMSIsInRhcGlzL2FjY291bnRfdHlwZSI6InVzZXIiLCJleHAiOjE3ODI4Mzg0MzZ9.OElQtm2H-BZTsmK1V-Ey36jgQJmzME4wfBu0QQ9CwnQ7IJT8qQMlU_cbFZPiNAfAj9xCpOC9-NskUE0ZzYcbvmFt-rzAwzjwLSS1Akx4B2aENsOEZLmLYnqo8eY_qde0rYbyVt0KtemsAZrx2Y7vrEiwWDKRyvAE-b52Knpc_Xoqmv9NcyinYi7Bi2x9S0IswGev3KZr2D4nwZAmTrgHQ3lp1NbyySJE0HKTXfr4P4gIo2FBFm0Kk_k9xJlJlcT4d2Jf-7YRtIMM9G8Y4sateVepxBA0v8F6b_OxX-LeEHeH-MeD-7MNLFayi2MIQGjXNB3J6Zrl6qWFBMDlxA8PDw";
  // Long term JWT for testuser3 - expires approx 1 July 2026
  public static final String testUser3JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJlNmIwMTRiOC05MGY1LTRjY2EtODhmYy1iYmIwYWJkMWZkODciLCJpc3MiOiJodHRwczovL2Rldi5kZXZlbG9wLnRhcGlzLmlvL3YzL3Rva2VucyIsInN1YiI6InRlc3R1c2VyM0BkZXYiLCJ0YXBpcy90ZW5hbnRfaWQiOiJkZXYiLCJ0YXBpcy90b2tlbl90eXBlIjoiYWNjZXNzIiwidGFwaXMvZGVsZWdhdGlvbiI6ZmFsc2UsInRhcGlzL2RlbGVnYXRpb25fc3ViIjpudWxsLCJ0YXBpcy91c2VybmFtZSI6InRlc3R1c2VyMyIsInRhcGlzL2FjY291bnRfdHlwZSI6InVzZXIiLCJleHAiOjE3ODI4Mzg2MjV9.yhAHhvFy5APE0gLw47qEv_aay3RmJZkd5Ik7WGmjh0QHrV9gCCxNIVGLKUSBOeKnocDLN9_dpGD0DL4OFiGcKrE9MaI9bYuL4j8BrxEf3Faa64B3IK38zml2Bx1nzpTAxkP6SJy6NENWFhbGg3MRa05oo8R2XctXoOBq8lb3I__zrwzKxQymF9L25hPzivBKaAkpIfVgsd2EGG8UdiExkK8yBRUDddK_I9BNn0VrzaJ0teLg_aOi-vyZN7JfT2VlQqgdMfvVGvH2O8Lt8BBvBs3dm7ODfSbxz1S5FHCYHvSV0H7h4lHA-yumU1I9vDi4K-_6gamHVfMMCqyYRLdJxA";


  // TAPIS_BASE_URL_SUFFIX should be set according to the dev, staging or prod environment
  // dev     -> develop.tapis.io
  // staging -> staging.tapis.io
  // prod    -> tapis.io
  // Default URLs. These can be overridden by env variables
  public static final String DEFAULT_BASE_URL = "https://" + tenantName + ".develop.tapis.io";
  public static final String DEFAULT_BASE_URL_APPS = "http://localhost";
  public static final String DEFAULT_SVC_PORT = "8080";
  // Env variables for setting URLs, passwords, etc
  public static final String TAPIS_ENV_BASE_URL = "TAPIS_BASE_URL";
  public static final String TAPIS_ENV_SVC_URL_APPS = "TAPIS_SVC_URL_APPS";
  public static final String TAPIS_ENV_FILES_SVC_PASSWORD = "TAPIS_FILES_SERVICE_PASSWORD";
  public static final String TAPIS_ENV_SVC_PORT = "TAPIS_SERVICE_PORT";

  // Default attributes
  public static final String defaultDescription = null;
  public static final String defaultNotesStr = "{}";
  public static final boolean defaultIsEnabled = true;
  public static final boolean defaultDeleted = false;
  public static final RuntimeEnum defaultRuntime = RuntimeEnum.DOCKER;
  public static final String defaultRuntimeVersion = null;
  public static final String defaultContainerImage = "containerImage";
  public static final int defaultMaxJobs = 0;
  public static final int defaultMaxJobsPerUser = 0;
  public static final boolean defaultStrictFileInputs = false;
  public static final boolean defaultDynamicExecSystem = false;

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

  // App attributes test data
  public static final String appNamePrefix = "CApp";
  public static final String appVersion = "0.0.1";
  public static final AppTypeEnum appTypeBatch = AppTypeEnum.BATCH;
  public static final boolean isEnabledTrue = true;
  public static final RuntimeEnum runtime = RuntimeEnum.DOCKER;
  public static final String runtimeVersion = "0.0.1";
  public static final List<RuntimeOptionEnum> runtimeOptions1 = new ArrayList<>(List.of(RuntimeOptionEnum.SINGULARITY_RUN));
  public static final List<RuntimeOptionEnum> runtimeOptions2 = new ArrayList<>(List.of(RuntimeOptionEnum.SINGULARITY_START));
  public static final List<RuntimeOptionEnum> runtimeOptionsNull = null;
  public static final boolean dynamicExecSystemTrue = true;
  public static final List<String> execSystemConstraints = Arrays.asList("Constraint1 AND", "Constraint2");
  public static final String execSystemId = "tapisv3-exec3";
  public static final String execSystemExecDir = "execSystemExecDir";
  public static final String execSystemInputDir = "execSystemInputDir";
  public static final String execSystemOutputDir = "execSystemOutputDir";
  public static final String execSystemLogicalQueue = "dsnormal";
  public static final String archiveSystemId = "tapisv3-storage";
  public static final String archiveSystemIdNull = null;
  public static final String archiveSystemDir = "archiveSystemDir";
  public static final boolean archiveOnAppErrorTrue = true;
  public static final String jobDescription = "job description";
  public static final int maxJobs = 1;
  public static final int maxJobsMAX = Integer.MAX_VALUE;
  public static final int maxJobsPerUser = 1;
  public static final int maxJobsPerUserMAX = Integer.MAX_VALUE;
  public static final boolean strictFileInputsFalse = false;
  public static final int nodeCount = 10;
  public static final int coresPerNode = 10;
  public static final int memoryMb = 32;
  public static final int maxMinutes = 10;
  public static final boolean metaRequiredTrue = true;
  public static final boolean metaRequiredFalse = false;
  public static final boolean deletedFalse = false;
  public static final Instant createdNull = null;
  public static final Instant updatedNull = null;
  public static final boolean inPlaceTrue = true;

  public static final List<String> metaKVPairs = Arrays.asList("metaKey1=metaVal1", "metaKey2=metaVal2");
  public static final KeyValuePair kv1 = new KeyValuePair().key("key1").value("value1");
  public static final KeyValuePair kv2 = new KeyValuePair().key("HOME").value("/home/testuser2");
  public static final KeyValuePair kv3 = new KeyValuePair().key("TMP").value("/tmp");
  public static final List<KeyValuePair> envVariables = new ArrayList<>(List.of(kv1,kv2,kv3));
  public static final List<String> archiveIncludes = Arrays.asList("/archiveInclude1", "/archiveInclude2");
  public static final List<String> archiveExcludes = Arrays.asList("/archiveExclude1", "/archiveExclude2");
  public static final List<String> jobTags = Arrays.asList("jobtag1", "jobtag2");

  public static final String srcA1 = "https://example.com/srcA1";
  public static final String srcB1 = "https://example.com/srcB1";
  public static final List<KeyValuePair> kvPairsFinA1 = new ArrayList<>(List.of(kv1,kv2,kv3));
  public static final ArgMetaSpec argMetaFinA1 = new ArgMetaSpec().name("finA1").description("File input A1")
                                                         .required(metaRequiredTrue).keyValuePairs(kvPairsFinA1);
  public static final FileInputDefinition finA1 = new FileInputDefinition().sourceUrl(srcA1).targetPath("/targetA1")
                                                         .inPlace(inPlaceTrue).meta(argMetaFinA1);
  public static final List<KeyValuePair> kvPairsFinB1 = new ArrayList<>(List.of(kv1,kv2,kv3));
  public static final ArgMetaSpec argMetaFinB1 = new ArgMetaSpec().name("finB1").description("File input B1")
          .required(metaRequiredTrue).keyValuePairs(kvPairsFinB1);
  public static final FileInputDefinition finB1 = new FileInputDefinition().sourceUrl(srcB1).targetPath("/targetB1")
          .inPlace(inPlaceTrue).meta(argMetaFinB1);
  public static final List<FileInputDefinition> fileInputDefinitions = new ArrayList<>(List.of(finA1, finB1));

  public static final NotificationMechanism notifMechA1 =
          new NotificationMechanism().mechanism(NotificationMechanismEnum.WEBHOOK).webhookURL("webhookUrlA1")
                  .emailAddress("emailAddressA1");
  public static final NotificationMechanism notifMechA2 =
          new NotificationMechanism().mechanism(NotificationMechanismEnum.EMAIL).webhookURL("webhookUrlA2")
                  .emailAddress("emailAddressA2");
  public static final List<NotificationMechanism> notifMechsA = new ArrayList<>(List.of(notifMechA1, notifMechA2));
  public static final NotificationSubscription notifA1 = new NotificationSubscription().filter("filterA1")
                                .notificationMechanisms(notifMechsA);
  public static final NotificationMechanism notifMechB1 =
          new NotificationMechanism().mechanism(NotificationMechanismEnum.WEBHOOK).webhookURL("webhookUrlB1")
                  .emailAddress("emailAddressB1");
  public static final NotificationMechanism notifMechB2 =
          new NotificationMechanism().mechanism(NotificationMechanismEnum.EMAIL).webhookURL("webhookUrlB2")
                  .emailAddress("emailAddressB2");
  public static final List<NotificationMechanism> notifMechsB = new ArrayList<>(List.of(notifMechB1, notifMechB2));
  public static final NotificationSubscription notifB1 = new NotificationSubscription().filter("filterB1")
          .notificationMechanisms(notifMechsB);
  public static final List<NotificationSubscription> notifList1 = new ArrayList<>(List.of(notifB1, notifB1));

  public static final ArgSpec appArgA1 = AppsClient.buildArg("valueA1", "appArgA1", "App arg A1", metaRequiredTrue, metaKVPairs);
  public static final ArgSpec appArgB1 = AppsClient.buildArg("valueB1", "appArgB1", "App arg B1", metaRequiredFalse, metaKVPairs);
  public static final List<ArgSpec> appArgs = new ArrayList<>(List.of(appArgA1, appArgB1));
  public static final ArgSpec containerArgA1 = AppsClient.buildArg("valueA1", "containerArgA1", "container arg A1", metaRequiredTrue, metaKVPairs);
  public static final ArgSpec containerArgB1 = AppsClient.buildArg("valueB1", "containerArgB1", "container arg B1", metaRequiredFalse, metaKVPairs);
  public static final List<ArgSpec> containerArgs = new ArrayList<>(List.of(containerArgA1, containerArgB1));
  public static final ArgSpec schedulerOptionA1 = AppsClient.buildArg("valueA1", "schedulerOptionA1", "scheduler option A1", metaRequiredTrue, metaKVPairs);
  public static final ArgSpec schedulerOptionB1 = AppsClient.buildArg("valueB1", "schedulerOptionB1", "scheduler optionApp B1", metaRequiredFalse, metaKVPairs);
  public static final List<ArgSpec> schedulerOptions = new ArrayList<>(List.of(schedulerOptionA1, schedulerOptionB1));

  // Strings for searches involving special characters
  public static final String specialChar7Str = ",()~*!\\"; // These 7 may need escaping
  public static final String specialChar7LikeSearchStr = "\\,\\(\\)\\~\\*\\!\\\\"; // All need escaping for LIKE/NLIKE
  public static final String specialChar7EqSearchStr = "\\,\\(\\)\\~*!\\"; // All but *! need escaping for other operators

  // String for search involving an escaped comma in a list of values
  public static final String escapedCommaInListValue = "abc\\,def";

  /**
   * Create an array of App objects in memory
   * Names will be of format TestApp_K_NNN where K is the key and NNN runs from 000 to 999
   * We need a key because maven runs the tests in parallel so each set of apps created by an integration
   *   test will need its own namespace.
   * @param n number of apps to create
   * @return array of App objects
   */
  public static Map<Integer, String[]> makeApps(int n, String key)
  {
    Map<Integer, String[]> apps = new HashMap<>();
    for (int i = 1; i <= n; i++)
    {
      // Suffix which should be unique for each app within each integration test
      String suffix = key + "_" + String.format("%03d", i);
      String appId = appNamePrefix + "_" + suffix;
      // Constructor initializes all attributes except for TBD
//    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=appType, 5=ownerUser1,
//              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
//              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
//              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
      String[] app0 = {tenantName, appId, appVersion, "description "+suffix, appTypeBatch.name(), ownerUser1,
                       runtime.name(), runtimeVersion+suffix, defaultContainerImage, jobDescription+suffix,
                       execSystemId, execSystemExecDir+suffix, execSystemInputDir+suffix, execSystemOutputDir+suffix,
                       execSystemLogicalQueue, archiveSystemId, archiveSystemDir+suffix};
      apps.put(i, app0);
    }
    return apps;
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
    System.out.println("Apps Service port from ENV: " + s);
    if (StringUtils.isBlank(s)) s = DEFAULT_SVC_PORT;
    return s;
  }
  public static String getServiceURL(String servicePort)
  {
    String s = System.getenv(TAPIS_ENV_SVC_URL_APPS);
    System.out.println("Apps Service URL from ENV: " + s);
    if (StringUtils.isBlank(s)) s = DEFAULT_BASE_URL_APPS + ":" + servicePort;
    return s;
  }
  public static String getBaseURL()
  {
    String s = System.getenv(TAPIS_ENV_BASE_URL);
    if (StringUtils.isBlank(s)) s = DEFAULT_BASE_URL;
    return s;
  }

  public static String createApp(AppsClient clt, String[] app)
          throws TapisClientException
  {
    ReqCreateApp rApp = new ReqCreateApp();
    rApp.setId(app[1]);
    rApp.setVersion(app[2]);
    rApp.description(app[3]);
    rApp.setAppType(AppTypeEnum.valueOf(app[4]));
    rApp.owner(app[5]);
    rApp.enabled(isEnabledTrue);
    rApp.setRuntime(RuntimeEnum.valueOf(app[6]));
    rApp.setRuntimeVersion(app[7]);
    rApp.setRuntimeOptions(runtimeOptions1);
    rApp.setContainerImage(app[8]);
    rApp.setMaxJobs(maxJobs);
    rApp.setMaxJobsPerUser(maxJobsPerUser);
    rApp.strictFileInputs(strictFileInputsFalse);

    // === Start Job Attributes
    JobAttributes jobAttrs = new JobAttributes();
    jobAttrs.setDescription(app[9]);
    jobAttrs.setDynamicExecSystem(dynamicExecSystemTrue);
    jobAttrs.setExecSystemConstraints(execSystemConstraints);
    jobAttrs.setExecSystemId(app[10]);
    jobAttrs.setExecSystemExecDir(app[11]);
    jobAttrs.setExecSystemInputDir(app[12]);
    jobAttrs.setExecSystemOutputDir(app[13]);
    jobAttrs.setExecSystemLogicalQueue(app[14]);
    jobAttrs.setArchiveSystemId(app[15]);
    jobAttrs.setArchiveSystemDir(app[16]);
    jobAttrs.setArchiveOnAppError(archiveOnAppErrorTrue);
    // ====== Start Parameter Set
    ParameterSet parameterSet = new ParameterSet();
    parameterSet.setAppArgs(appArgs);
    parameterSet.setContainerArgs(containerArgs);
    parameterSet.setSchedulerOptions(schedulerOptions);
    parameterSet.setEnvVariables(envVariables);
    ParameterSetArchiveFilter archiveFilter = new ParameterSetArchiveFilter();
    archiveFilter.setIncludes(archiveIncludes);
    archiveFilter.setExcludes(archiveExcludes);
    parameterSet.setArchiveFilter(archiveFilter);
    // ====== End Parameter Set
    jobAttrs.setParameterSet(parameterSet);

    jobAttrs.setFileInputDefinitions(fileInputDefinitions);
    jobAttrs.setNodeCount(nodeCount);
    jobAttrs.setCoresPerNode(coresPerNode);
    jobAttrs.setMemoryMB(memoryMb);
    jobAttrs.setMaxMinutes(maxMinutes);
    jobAttrs.setSubscriptions(notifList1);
    jobAttrs.setTags(jobTags);
    // === End Job Attributes
    rApp.setJobAttributes(jobAttrs);

    rApp.tags(tags1);
    rApp.notes(notes1JO);

    // Create the app
    return clt.createApp(rApp);
  }

  /**
   * Create an application using only required attributes.
   * In simplest case these are required: id, version, appType, containerImage, execSystemId
   * Use attributes from a string array.
   *    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=appType, 5=ownerUser1,
   *              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
   *              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
   *              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
   *
   * @param clt - Apps client
   * @param app - Array of attributes that can be represented as strings.
   * @return Response from the createApp client call
   * @throws TapisClientException - on error
   */
  public static String createAppMinimal(AppsClient clt, String[] app) throws TapisClientException
  {
    ReqCreateApp rApp = new ReqCreateApp();
    // Id, version and type are always required
    rApp.setId(app[1]);
    rApp.setVersion(app[2]);
    rApp.setAppType(appTypeBatch);
    // Containerized so container image must be set. NOTE: currently only containerized supported
    rApp.setContainerImage(defaultContainerImage);
    // === Start Job Attributes
    JobAttributes jobAttrs = new JobAttributes();
    // dynamiceExecSystem defaults to false so execSystemId must be set. This is the simplest minimal App
    jobAttrs.setExecSystemId(app[10]);
    rApp.setJobAttributes(jobAttrs);

    // Use client to create the app
    return clt.createApp(rApp);
  }

  /**
   * Create an application based on attributes from a TapisApp.
   *
   * @param clt - Apps client
   * @param app - TapisApp
   * @return Response from the createApp client call
   * @throws TapisClientException - on error
   */
  public static String createAppFromTapisApp(AppsClient clt, TapisApp app) throws TapisClientException
  {
    ReqCreateApp rApp = makeReqCreateAppFromTapisApp(app);
    // Use client to create the app
    return clt.createApp(rApp);
  }

  /**
   * Verify most attributes for an App
   *
   * @param tmpApp - app retrieved from the service
   * @param app0 - Data used to create the app
   */
  public static void verifyAppAttributes(TapisApp tmpApp, String[] app0)
  {
//    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=appType, 5=ownerUser1,
//              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
//              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
//              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
    Assert.assertEquals(tmpApp.getTenant(), app0[0]);
    Assert.assertEquals(tmpApp.getId(), app0[1]);
    Assert.assertEquals(tmpApp.getVersion(), app0[2]);
    Assert.assertEquals(tmpApp.getDescription(), app0[3]);
    Assert.assertNotNull(tmpApp.getAppType());
    Assert.assertEquals(tmpApp.getAppType().name(), app0[4]);
    Assert.assertEquals(tmpApp.getOwner(), app0[5]);
    Assert.assertEquals(tmpApp.getEnabled(), Boolean.valueOf(isEnabledTrue));
    Assert.assertNotNull(tmpApp.getRuntime());
    Assert.assertEquals(tmpApp.getRuntime().name(), app0[6]);
    Assert.assertEquals(tmpApp.getRuntimeVersion(), app0[7]);
    // Verify runtimeOptions
    List<RuntimeOptionEnum> rtOps = tmpApp.getRuntimeOptions();
    Assert.assertNotNull(rtOps);
    for (RuntimeOptionEnum rtOption : runtimeOptions1)
    {
      Assert.assertTrue(rtOps.contains(rtOption), "List of runtime options did not contain: " + rtOption.name());
    }

    Assert.assertEquals(tmpApp.getContainerImage(), app0[8]);
    Assert.assertEquals(tmpApp.getMaxJobs(), Integer.valueOf(maxJobs));
    Assert.assertEquals(tmpApp.getMaxJobsPerUser(), Integer.valueOf(maxJobsPerUser));
    Assert.assertEquals(tmpApp.getStrictFileInputs(), Boolean.valueOf(strictFileInputsFalse));

    // ========== JobAttributes
    JobAttributes jobAttributes = tmpApp.getJobAttributes();
    Assert.assertNotNull(jobAttributes);
    Assert.assertEquals(jobAttributes.getDescription(), app0[9]);
    Assert.assertEquals(jobAttributes.getDynamicExecSystem(), Boolean.valueOf(dynamicExecSystemTrue));
    Assert.assertEquals(jobAttributes.getExecSystemId(), app0[10]);
    Assert.assertEquals(jobAttributes.getExecSystemExecDir(), app0[11]);
    Assert.assertEquals(jobAttributes.getExecSystemInputDir(), app0[12]);
    Assert.assertEquals(jobAttributes.getExecSystemOutputDir(), app0[13]);
    Assert.assertEquals(jobAttributes.getExecSystemLogicalQueue(), app0[14]);
    Assert.assertEquals(jobAttributes.getArchiveSystemId(), app0[15]);
    Assert.assertEquals(jobAttributes.getArchiveSystemDir(), app0[16]);
    Assert.assertEquals(jobAttributes.getArchiveOnAppError(), Boolean.valueOf(archiveOnAppErrorTrue));
    Assert.assertEquals(jobAttributes.getNodeCount(), Integer.valueOf(nodeCount));
    Assert.assertEquals(jobAttributes.getCoresPerNode(), Integer.valueOf(coresPerNode));
    Assert.assertEquals(jobAttributes.getMemoryMB(), Integer.valueOf(memoryMb));
    Assert.assertEquals(jobAttributes.getMaxMinutes(), Integer.valueOf(maxMinutes));

    // TODO Verify fileInputs
    // TODO only meta.name is checked
    List<FileInputDefinition> tFileInputs = jobAttributes.getFileInputDefinitions();
    Assert.assertNotNull(tFileInputs, "FileInputs list should not be null.");
    Assert.assertEquals(tFileInputs.size(), fileInputDefinitions.size(), "Wrong number of FileInputs");
    var metaNamesFound = new ArrayList<String>();
    for (FileInputDefinition itemFound : tFileInputs)
    {
      Assert.assertNotNull(itemFound.getMeta(), "FileInput meta value should not be null.");
      metaNamesFound.add(itemFound.getMeta().getName());
    }
    for (FileInputDefinition itemSeedItem : fileInputDefinitions)
    {
      Assert.assertNotNull(itemSeedItem.getMeta());
      Assert.assertTrue(metaNamesFound.contains(itemSeedItem.getMeta().getName()),
              "List of fileInputs did not contain an item with metaName: " + itemSeedItem.getMeta().getName());
    }

    // TODO Verify notificationSubscriptions
    // TODO: Filter is checked but not mechanisms
    List<NotificationSubscription> tSubscriptions = jobAttributes.getSubscriptions();
    Assert.assertNotNull(tSubscriptions, "Subscriptions list should not be null.");
    Assert.assertEquals(tSubscriptions.size(), notifList1.size(), "Wrong number of Subscriptions");
    var filtersFound = new ArrayList<String>();
    for (NotificationSubscription itemFound : tSubscriptions)
    {
      Assert.assertNotNull(itemFound.getFilter(), "Subscription filter should not be null.");
      filtersFound.add(itemFound.getFilter());
    }
    for (NotificationSubscription itemSeedItem : notifList1)
    {
      Assert.assertTrue(filtersFound.contains(itemSeedItem.getFilter()),
              "List of subscriptions did not contain a filter: " + itemSeedItem.getFilter());
    }

    // Verify jobTags
    List<String> tmpJobTags = jobAttributes.getTags();
    Assert.assertNotNull(tmpJobTags, "jobTags value was null");
    Assert.assertEquals(tmpJobTags.size(), jobTags.size(), "Wrong number of jobTags");
    for (String tagStr : jobTags)
    {
      Assert.assertTrue(tmpJobTags.contains(tagStr));
      System.out.println("Found jobTag: " + tagStr);
    }

    // ========== ParameterSet
    ParameterSet parameterSet = jobAttributes.getParameterSet();
    Assert.assertNotNull(parameterSet, "ParameterSet should not be null.");
    // TODO Verify appArgs
//    // TODO Arg value is checked but not arg metadata
    List<ArgSpec> tmpArgs = parameterSet.getAppArgs();
    Assert.assertNotNull(tmpArgs, "Fetched appArgs was null");
    Assert.assertEquals(tmpArgs.size(), appArgs.size());
    var argValuesFound = new ArrayList<String>();
    for (ArgSpec itemFound : tmpArgs) {argValuesFound.add(itemFound.getArg());}
    for (ArgSpec itemSeedItem : appArgs)
    {
      Assert.assertTrue(argValuesFound.contains(itemSeedItem.getArg()),
              "List of appArgs did not contain an item with arg value: " + itemSeedItem.getArg());
    }

    // Verify containerArgs
    // TODO: Check metadata
    tmpArgs = parameterSet.getContainerArgs();
    Assert.assertNotNull(tmpArgs, "Fetched containerArgs was null");
    Assert.assertEquals(tmpArgs.size(), containerArgs.size());
    argValuesFound = new ArrayList<>();
    for (ArgSpec itemFound : tmpArgs) {argValuesFound.add(itemFound.getArg());}
    for (ArgSpec itemSeedItem : containerArgs)
    {
      Assert.assertTrue(argValuesFound.contains(itemSeedItem.getArg()),
              "List of containerArgs did not contain an item with arg value: " + itemSeedItem.getArg());
    }

    // Verify schedulerOptions
    // TODO: Check metadata
    tmpArgs = parameterSet.getSchedulerOptions();
    Assert.assertNotNull(tmpArgs, "Fetched schedulerOptions was null");
    Assert.assertEquals(tmpArgs.size(), schedulerOptions.size());
    argValuesFound = new ArrayList<>();
    for (ArgSpec itemFound : tmpArgs) {argValuesFound.add(itemFound.getArg());}
    for (ArgSpec itemSeedItem : schedulerOptions)
    {
      Assert.assertTrue(argValuesFound.contains(itemSeedItem.getArg()),
              "List of schedulerOptions did not contain an item with arg value: " + itemSeedItem.getArg());
    }

    // Verify envVariables
    List<KeyValuePair> tmpEnvVariables = parameterSet.getEnvVariables();
    Assert.assertNotNull(tmpEnvVariables, "ParameterSet envVariables value was null");
    Assert.assertEquals(tmpEnvVariables.size(), envVariables.size(), "Wrong number of envVariables");
    List<String> kvFoundList = new ArrayList<>();
    for (KeyValuePair kv : tmpEnvVariables)
    {
      kvFoundList.add(kv.getKey() + "=" + kv.getValue());
      System.out.println("Found envVariable: " + kv.getKey() + "=" + kv.getValue());
    }
    for (KeyValuePair kv : envVariables)
    {
      String kvStr = kv.getKey() + "=" + kv.getValue();
      Assert.assertTrue(kvFoundList.contains(kvStr),
              "List of envVariables did not contain an item with key=value of : " + kvStr);
    }

    // Verify archiveFilter includes and excludes
    ParameterSetArchiveFilter archiveFilter = parameterSet.getArchiveFilter();
    Assert.assertNotNull(archiveFilter, "ParameterSetArchiveFilter was null");
    List<String> tmpFileList = archiveFilter.getIncludes();
    Assert.assertNotNull(tmpFileList, "includes list from ParameterSetArchiveFilter was null");
    Assert.assertEquals(tmpFileList.size(), archiveIncludes.size(), "Wrong number of archiveIncludes");
    for (String tmpStr : archiveIncludes)
    {
      Assert.assertTrue(tmpFileList.contains(tmpStr));
      System.out.println("Found archiveInclude: " + tmpStr);
    }
    tmpFileList = archiveFilter.getExcludes();
    Assert.assertNotNull(tmpFileList, "excludes list from ParameterSetArchiveFilter was null");
    Assert.assertEquals(tmpFileList.size(), archiveExcludes.size(), "Wrong number of archiveExcludes");
    for (String tmpStr : archiveExcludes)
    {
      Assert.assertTrue(tmpFileList.contains(tmpStr));
      System.out.println("Found archiveExclude: " + tmpStr);
    }

    // Verify tags
    List<String> tmpTags = tmpApp.getTags();
    Assert.assertNotNull(tmpTags, "Tags value was null");
    Assert.assertEquals(tmpTags.size(), tags1.size(), "Wrong number of tags");
    for (String tagStr : tags1)
    {
      Assert.assertTrue(tmpTags.contains(tagStr));
      System.out.println("Found tag: " + tagStr);
    }
    // Verify notes
    String tmpNotesStr = (String) tmpApp.getNotes();
    System.out.println("Found notes: " + tmpNotesStr);
    JsonObject tmpNotes = ClientTapisGsonUtils.getGson().fromJson(tmpNotesStr, JsonObject.class);
    Assert.assertNotNull(tmpNotes, "Fetched Notes should not be null.");
    JsonObject origNotes = notes1JO;
    Assert.assertTrue(tmpNotes.has("project"));
    String projStr = origNotes.get("project").getAsString();
    Assert.assertEquals(tmpNotes.get("project").getAsString(), projStr);
    Assert.assertTrue(tmpNotes.has("testdata"));
    String testdataStr = origNotes.get("testdata").getAsString();
    Assert.assertEquals(tmpNotes.get("testdata").getAsString(), testdataStr);

    Assert.assertNotNull(tmpApp.getCreated(), "Fetched created timestamp should not be null.");
    Assert.assertNotNull(tmpApp.getUpdated(), "Fetched updated timestamp should not be null.");
  }

  public static AppsClient getClientUsr(String serviceURL, String userJWT)
  {
    // Create the client each time due to issue with setting different headers needed by svc vs usr client

    // Creating a separate client for svc is not working because headers end up being used for all clients.
    // Underlying defaultHeaderMap is static so adding headers impacts all clients.
//    appClientSvc = new AppsClient(appsURL, svcJWT);
//    appClientSvc.addDefaultHeader("X-Tapis-User", appOwner);
//    appClientSvc.addDefaultHeader("X-Tapis-Tenant", tenantName);
    return new AppsClient(serviceURL, userJWT);
  }

  /**
   * Verify the required attributes for a TapisApp
   *   and verify that other attributes are set to expected defaults.
   //    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=appType, 5=ownerUser1,
   //              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
   //              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
   //              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
   String[] app0 = {tenantName, appId, appVersion, "description "+suffix, appTypeBatch.name(), ownerUser1,
   runtime.name(), runtimeVersion+suffix, containerImage+suffix, jobDescription+suffix,
   execSystemId, execSystemExecDir+suffix, execSystemInputDir+suffix, execSystemOutputDir+suffix,
   execSystemLogicalQueue, archiveSystemId, archiveSystemDir+suffix};
   * @param tmpApp - app retrieved from the service
   */
  public static void verifyAppDefaults(TapisApp tmpApp, String appId)
  {
    Assert.assertEquals(tmpApp.getTenant(), tenantName);
    // Verify required attributes
    Assert.assertEquals(tmpApp.getId(), appId);
    Assert.assertEquals(tmpApp.getVersion(), appVersion);
    Assert.assertNotNull(tmpApp.getAppType());
    Assert.assertEquals(tmpApp.getAppType().name(), appTypeBatch.name());
    Assert.assertNotNull(tmpApp.getJobAttributes());

    // Verify optional attributes have been set to defaults
    // Owner should have defaulted to user who created the system
    Assert.assertEquals(tmpApp.getOwner(), ownerUser1);
    Assert.assertEquals(tmpApp.getDescription(), defaultDescription);
    Assert.assertEquals(tmpApp.getEnabled(), Boolean.valueOf(defaultIsEnabled));
    Assert.assertEquals(tmpApp.getRuntime(), defaultRuntime);
    Assert.assertEquals(tmpApp.getRuntimeVersion(), defaultRuntimeVersion);
    Assert.assertNull(tmpApp.getRuntimeOptions());
    Assert.assertEquals(tmpApp.getContainerImage(), defaultContainerImage);
    Assert.assertNotNull(tmpApp.getMaxJobs());
    Assert.assertEquals(tmpApp.getMaxJobs().intValue(), maxJobsMAX);
    Assert.assertNotNull(tmpApp.getMaxJobsPerUser());
    Assert.assertEquals(tmpApp.getMaxJobsPerUser().intValue(), maxJobsPerUserMAX);
    Assert.assertEquals(tmpApp.getStrictFileInputs(), Boolean.valueOf(defaultStrictFileInputs));
    Assert.assertNotNull(tmpApp.getTags());
    Assert.assertTrue(tmpApp.getTags().isEmpty());
    Assert.assertNotNull(tmpApp.getNotes());
    Assert.assertEquals((String) tmpApp.getNotes(), defaultNotesStr);
    Assert.assertEquals(tmpApp.getDeleted(), Boolean.valueOf(defaultDeleted));
    // jobAttributes
    JobAttributes jobAttrs = tmpApp.getJobAttributes();
    Assert.assertEquals(jobAttrs.getDescription(), defaultDescription);
    Assert.assertNotNull(jobAttrs.getDynamicExecSystem());
    Assert.assertFalse(jobAttrs.getDynamicExecSystem());
    Assert.assertNull(jobAttrs.getExecSystemExecDir());
    Assert.assertNull(jobAttrs.getExecSystemInputDir());
    Assert.assertNull(jobAttrs.getExecSystemOutputDir());
    Assert.assertNull(jobAttrs.getExecSystemLogicalQueue());
    Assert.assertNull(jobAttrs.getArchiveSystemId());
    Assert.assertNull(jobAttrs.getArchiveSystemDir());
    Assert.assertNotNull(jobAttrs.getArchiveOnAppError());
    Assert.assertTrue(jobAttrs.getArchiveOnAppError());
    Assert.assertNotNull(jobAttrs.getFileInputDefinitions());
    Assert.assertTrue(jobAttrs.getFileInputDefinitions().isEmpty());
    Assert.assertNotNull(jobAttrs.getNodeCount());
    Assert.assertEquals(jobAttrs.getNodeCount().intValue(), 1);
    Assert.assertNotNull(jobAttrs.getCoresPerNode());
    Assert.assertEquals(jobAttrs.getCoresPerNode().intValue(), 1);
    Assert.assertNotNull(jobAttrs.getMemoryMB());
    Assert.assertEquals(jobAttrs.getMemoryMB().intValue(), 100);
    Assert.assertNotNull(jobAttrs.getMaxMinutes());
    Assert.assertEquals(jobAttrs.getMaxMinutes().intValue(), 10);
    Assert.assertNotNull(jobAttrs.getSubscriptions());
    Assert.assertTrue(jobAttrs.getSubscriptions().isEmpty());
    Assert.assertNotNull(jobAttrs.getTags());
    Assert.assertTrue(jobAttrs.getTags().isEmpty());
    // jobAttributes.parameterSet
    ParameterSet parmSet = jobAttrs.getParameterSet();
    Assert.assertNotNull(parmSet);
    Assert.assertNotNull(parmSet.getAppArgs());
    Assert.assertTrue(parmSet.getAppArgs().isEmpty());
    Assert.assertNotNull(parmSet.getSchedulerOptions());
    Assert.assertTrue(parmSet.getSchedulerOptions().isEmpty());
    Assert.assertNotNull(parmSet.getEnvVariables());
    Assert.assertTrue(parmSet.getEnvVariables().isEmpty());
    // parameterSet.archiveFilter
    ParameterSetArchiveFilter archiveFilter = parmSet.getArchiveFilter();
    Assert.assertNotNull(archiveFilter);
    Assert.assertNotNull(archiveFilter.getIncludes());
    Assert.assertTrue(archiveFilter.getIncludes().isEmpty());
    Assert.assertNotNull(archiveFilter.getExcludes());
    Assert.assertTrue(archiveFilter.getExcludes().isEmpty());
    Assert.assertNotNull(archiveFilter.getIncludeLaunchFiles());
    Assert.assertTrue(archiveFilter.getIncludeLaunchFiles());
  }

  /*
   * Generate a full ReqCreateApp using attributes from a TapisApp
   */
  private static ReqCreateApp makeReqCreateAppFromTapisApp(TapisApp app) throws TapisClientException
  {
    ReqCreateApp rApp = new ReqCreateApp();
    // Id, version and type
    rApp.setId(app.getId());
    rApp.setVersion(app.getVersion());
    rApp.setAppType(app.getAppType());
    rApp.setDescription(app.getDescription());
    rApp.setOwner(app.getOwner());
    rApp.setContainerImage(app.getContainerImage());
// TODO ???
    rApp.setJobAttributes(app.getJobAttributes());

    return rApp;
  }
}
