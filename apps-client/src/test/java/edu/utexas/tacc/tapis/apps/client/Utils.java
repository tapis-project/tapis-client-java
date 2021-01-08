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
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqCreateApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.App;
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

  // Default system attributes
  public static final String defaultDescription = null;
  public static final String defaultImportRefId = null;
  public static final String defaultNotesStr = "{}";

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
  public static final AppTypeEnum appType = AppTypeEnum.BATCH;
  public static final boolean isEnabled = true;
  public static final RuntimeEnum runtime = RuntimeEnum.DOCKER;
  public static final String runtimeVersion = "0.0.1";
  public static final String containerImage = "containerImage";
  public static final boolean dynamicExecSystem = true;
  public static final List<String> execSystemConstraints = Arrays.asList("Constraint1 AND", "Constraint2");
  public static final String execSystemId = "execSystem";
  public static final String execSystemExecDir = "execSystemExecDir";
  public static final String execSystemInputDir = "execSystemInputDir";
  public static final String execSystemOutputDir = "execSystemOutputDir";
  public static final String execSystemLogicalQueue = "execSystemLogicalQueue";
  public static final String archiveSystemId = "archiveSystem";
  public static final String archiveSystemDir = "archiveSystemDir";
  public static final boolean archiveOnAppError = true;
  public static final String jobDescription = "job description";
  public static final int maxJobs = 1;
  public static final int maxJobsPerUser = 1;
  public static final boolean strictFileInputs = false;
  public static final int nodeCount = 1;
  public static final int coresPerNode = 1;
  public static final int memoryMb = 1;
  public static final int maxMinutes = 1;
  public static final boolean metaRequiredTrue = true;
  public static final boolean metaRequiredFalse = false;
  public static final String importRefIdNull = null;
  public static final boolean deletedFalse = false;
  public static final Instant createdNull = null;
  public static final Instant updatedNull = null;
  public static final boolean inPlaceTrue = true;

  public static final List<String> metaKVPairs = Arrays.asList("metaKey1=metaVal1", "metaKey2=metaVal2");
  public static final KeyValuePair kv1 = new KeyValuePair().key("a").value("b");
  public static final KeyValuePair kv2 = new KeyValuePair().key("HOME").value("/home/testuser2");
  public static final KeyValuePair kv3 = new KeyValuePair().key("TMP").value("/tmp");
  public static final List<KeyValuePair> envVariables = new ArrayList<>(List.of(kv1,kv2,kv3));
  public static final List<String> archiveIncludes = Arrays.asList("/archiveInclude1", "/archiveInclude2");
  public static final List<String> archiveExcludes = Arrays.asList("/archiveExclude1", "/archiveExclude2");
  public static final List<String> jobTags = Arrays.asList("jobtag1", "jobtag2");

  public static final List<KeyValuePair> kvPairsFinA1 = new ArrayList<>(List.of(kv1,kv2,kv3));
  public static final ArgMetaSpec argMetaFinA1 = new ArgMetaSpec().name("finA1").description("File input A1")
                                                         .required(metaRequiredTrue).keyValuePairs(kvPairsFinA1);
  public static final FileInputDefinition finA1 = new FileInputDefinition().sourceUrl("/srcA1").targetPath("/targetA1")
                                                         .inPlace(inPlaceTrue).meta(argMetaFinA1);
  public static final List<KeyValuePair> kvPairsFinB1 = new ArrayList<>(List.of(kv1,kv2,kv3));
  public static final ArgMetaSpec argMetaFinB1 = new ArgMetaSpec().name("finB1").description("File input B1")
          .required(metaRequiredTrue).keyValuePairs(kvPairsFinB1);
  public static final FileInputDefinition finB1 = new FileInputDefinition().sourceUrl("/srcB1").targetPath("/targetB1")
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
      String[] app0 = {tenantName, appId, appVersion, "description "+suffix, appType.name(), ownerUser1,
                       runtime.name(), runtimeVersion+suffix, containerImage+suffix, jobDescription+suffix,
                       execSystemId+suffix, execSystemExecDir+suffix, execSystemInputDir+suffix, execSystemOutputDir+suffix,
                       execSystemLogicalQueue+suffix, archiveSystemId+suffix, archiveSystemDir+suffix};
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
    rApp.enabled(isEnabled);
    rApp.setRuntime(RuntimeEnum.valueOf(app[6]));
    rApp.setRuntimeVersion(app[7]);
    rApp.setContainerImage(app[8]);
    rApp.setMaxJobs(maxJobs);
    rApp.setMaxJobsPerUser(maxJobsPerUser);
    rApp.strictFileInputs(strictFileInputs);

    // === Start Job Attributes
    JobAttributes jobAttrs = new JobAttributes();
    jobAttrs.setDescription(app[9]);
    jobAttrs.setDynamicExecSystem(dynamicExecSystem);
    jobAttrs.setExecSystemConstraints(execSystemConstraints);
    jobAttrs.setExecSystemId(app[10]);
    jobAttrs.setExecSystemExecDir(app[11]);
    jobAttrs.setExecSystemInputDir(app[12]);
    jobAttrs.setExecSystemOutputDir(app[13]);
    jobAttrs.setExecSystemLogicalQueue(app[14]);
    jobAttrs.setArchiveSystemId(app[15]);
    jobAttrs.setArchiveSystemDir(app[16]);
    jobAttrs.setArchiveOnAppError(archiveOnAppError);
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
   * Verify most attributes for an App using default create data for following attributes:
   *     port, useProxy, proxyHost, proxyPort, defaultAuthnMethod, transferMethods,
   *     canExec, jobWorkingDir, jobMaxJobs, jobMaxJobsPerUser, jobIsBatch, batchScheduler, batchDefaultLogicalQueue,
   *     jobEnvVariables, jobLogicalQueues, capabilities, tags, notes
   * @param tmpApp - app retrieved from the service
   * @param app0 - Data used to create the app
   */
  public static void verifyAppAttributes(App tmpApp, String[] app0)
  {
//    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=appType, 5=ownerUser1,
//              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
//              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
//              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
    Assert.assertEquals(tmpApp.getTenant(), app0[0]);
    Assert.assertEquals(tmpApp.getId(), app0[1]);
    Assert.assertEquals(tmpApp.getVersion(), app0[2]);
    Assert.assertEquals(tmpApp.getDescription(), app0[3]);
    Assert.assertEquals(tmpApp.getAppType().name(), app0[4]);
    Assert.assertEquals(tmpApp.getOwner(), app0[5]);
    Assert.assertEquals(tmpApp.getEnabled(), Boolean.valueOf(isEnabled));

//    // Verify transfer methods
//    List<TransferMethodEnum> tMethodsList = tmpApp.getTransferMethods();
//    Assert.assertNotNull(tMethodsList, "TransferMethods list should not be null");
//    for (TransferMethodEnum txfrMethod : prot1TxfrMethodsT)
//    {
//      Assert.assertTrue(tMethodsList.contains(txfrMethod), "List of transfer methods did not contain: " + txfrMethod.name());
//    }
//    Assert.assertEquals(tmpApp.getCanExec(), Boolean.valueOf(canExec));
//    Assert.assertEquals(tmpApp.getJobWorkingDir(), app0[10]);
//    Assert.assertEquals(tmpApp.getJobMaxJobs().intValue(), jobMaxJobs);
//    Assert.assertEquals(tmpApp.getJobMaxJobsPerUser().intValue(), jobMaxJobsPerUser);
//    Assert.assertEquals(tmpApp.getJobIsBatch(), Boolean.valueOf(jobIsBatch));
//    Assert.assertEquals(tmpApp.getBatchScheduler(), app0[11]);
//    Assert.assertEquals(tmpApp.getBatchDefaultLogicalQueue(), app0[12]);
//    // Verify jobEnvVariables
//    List<KeyValuePair> tmpJobEnvVariables = tmpApp.getJobEnvVariables();
//    Assert.assertNotNull(tmpJobEnvVariables, "JobEnvVariables value was null");
//    Assert.assertEquals(tmpJobEnvVariables.size(), jobEnvVariables.size(), "Wrong number of JobEnvVariables");
//    for (KeyValuePair kv : jobEnvVariables)
//    {
//      // TODO
////      Assert.assertTrue(tmpJobEnvVariables.contains(varStr));
//      System.out.println("Found JobEnvVariable: " + kv.getKey() + "=" + kv.getValue());
//    }
//    // Verify batchLogicalQueues
//    List<LogicalQueue> jobQueues = tmpApp.getBatchLogicalQueues();
//// TODO logical queues not yet implemented.
////    Assert.assertNotNull(jobQueues);
////    Assert.assertEquals(jobQueues.size(), jobQueues1.size());
////    var qNamesFound = new ArrayList<String>();
////    for (LogicalQueue qFound : jobQueues) {qNamesFound.add(qFound.getName());}
////    for (LogicalQueue qSeed : jobQueues1)
////    {
////      Assert.assertTrue(qNamesFound.contains(qSeed.getName()), "List of logical queues did not contain a queue named: " + qSeed.getName());
////    }
//    // Verify capabilities
//    List<Capability> jobCaps = tmpApp.getJobCapabilities();
//    Assert.assertNotNull(jobCaps);
//    Assert.assertEquals(jobCaps.size(), jobCaps1.size());
//    var capNamesFound = new ArrayList<String>();
//    for (Capability capFound : jobCaps) {capNamesFound.add(capFound.getName());}
//    for (Capability capSeed : jobCaps1)
//    {
//      Assert.assertTrue(capNamesFound.contains(capSeed.getName()), "List of capabilities did not contain a capability named: " + capSeed.getName());
//    }
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
    // TODO: Currently the client converts notes from a gson LinkedTreeMap to a string of json so we cast it to String here.
    // TODO/TBD: Can we update jsonschema and model on server to make it a String instead of Object?
    //           Previously this caused issues with json serialization.
    String tmpNotesStr = (String) tmpApp.getNotes();
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
}
