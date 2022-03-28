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
import edu.utexas.tacc.tapis.apps.client.gen.model.AppFileInputArray;
import edu.utexas.tacc.tapis.apps.client.gen.model.JobTypeEnum;
import edu.utexas.tacc.tapis.apps.client.gen.model.AppArgSpec;
import edu.utexas.tacc.tapis.apps.client.gen.model.AppFileInput;
import edu.utexas.tacc.tapis.apps.client.gen.model.ArgInputModeEnum;
import edu.utexas.tacc.tapis.apps.client.gen.model.FileInputModeEnum;
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
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqPostApp;
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

  // Long term JWT for testuser1 for DEV - expires approx 1 Sep 2022
  public static final String testUser1JWT ="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI4MGU3YzljZi1kYjFlLTQzNzQtYTg3NC0zZjFiM2ZmYjdhYzQiLCJpc3MiOiJodHRwczovL2Rldi5kZXZlbG9wLnRhcGlzLmlvL3YzL3Rva2VucyIsInN1YiI6InRlc3R1c2VyMUBkZXYiLCJ0YXBpcy90ZW5hbnRfaWQiOiJkZXYiLCJ0YXBpcy90b2tlbl90eXBlIjoiYWNjZXNzIiwidGFwaXMvZGVsZWdhdGlvbiI6ZmFsc2UsInRhcGlzL2RlbGVnYXRpb25fc3ViIjpudWxsLCJ0YXBpcy91c2VybmFtZSI6InRlc3R1c2VyMSIsInRhcGlzL2FjY291bnRfdHlwZSI6InVzZXIiLCJleHAiOjE2NjM5MDMyNjJ9.O9ID46YpTL-cgZvhuisytEkde6vFGMJ9Lu4HcEsvTAS6fJqLVds9w9cBah_FfSLZcUu38ddw7cjYHjSGi5crW2G32fWKPjOA8mrk9EE8Q-BNB_bzSYVXOt7-4dRBAyQnEu7d7OYqGJo4-2F4U7210JXtfNog0CH1S0oH8j0ZaiuAA5ula9bhUxXUmJYZhQcyXvxcgBzD_2fEzS2c0h5NWRb-O9abKmuD51ASvYpgrOB8_kmU1P_A91P5YP2H3Kx9E6ijm10GzQJH9euy2nLqKyH20pmvEysQMoq9u0tirThXN4WXLvkyIOtllOyICgSrfxkz1x6yoTkMf-YTrlryuA";
  // Long term JWT for testuser3 for DEV - expires approx 1 Sep 2022
  public static final String testUser3JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJjZDg4MDBkYy05ZGQ5LTQ2ZmItYWZjMi0wZjM5YzVlNjAxN2EiLCJpc3MiOiJodHRwczovL2Rldi5kZXZlbG9wLnRhcGlzLmlvL3YzL3Rva2VucyIsInN1YiI6InRlc3R1c2VyM0BkZXYiLCJ0YXBpcy90ZW5hbnRfaWQiOiJkZXYiLCJ0YXBpcy90b2tlbl90eXBlIjoiYWNjZXNzIiwidGFwaXMvZGVsZWdhdGlvbiI6ZmFsc2UsInRhcGlzL2RlbGVnYXRpb25fc3ViIjpudWxsLCJ0YXBpcy91c2VybmFtZSI6InRlc3R1c2VyMyIsInRhcGlzL2FjY291bnRfdHlwZSI6InVzZXIiLCJleHAiOjE2NjM5MDMzNzF9.gujOdOU9ZOorB1U-yEjjC3D0mKMJQIatjh-ETdSpj-yw7uPsjqpNxIyaVG0UUEdfpqgtFPoCz04iaWfaw-y83W0It2YJMz-4DB8fPViAi0YA4cSTe_dKUJDCYLJEJpt3UexAmr7aAdEnVD6gNqnx9hoExbI5hNZaXApnQMI1Gmp42htxm9LseL7dnMo9Mh-cOeXJcyriPwyk2G-ejuDaRW8MrBSZ3_AxNdookfPluIV6VCwfOvg_6Vh1qYWgVAnW0cm_4lIZIUC8wFVXlG6AjaVLOZ66svTSDrVoXeUNhuYIiwvhWbKFFInqR3OKcHRTeu5a1d9QEqLFoWdHmvT8OA";


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
  public static final String description1 = "";
  public static final String defaultDescription = null;
  public static final String defaultNotesStr = "{}";
  public static final Boolean defaultIsEnabled = true;
  public static final Boolean defaultDeleted = false;
  public static final RuntimeEnum defaultRuntime = RuntimeEnum.DOCKER;
  public static final String defaultRuntimeVersion = null;
  public static final String defaultContainerImage = "containerImage";
  public static final Integer defaultMaxJobs = 0;
  public static final Integer defaultMaxJobsPerUser = 0;
  public static final Boolean defaultStrictFileInputs = false;
  public static final Boolean defaultDynamicExecSystem = false;
  public static final Boolean defaultArchiveOnAppError = true;
  public static final Integer defaultNodeCount = 1;
  public static final Integer defaultCoresPerNode = 1;
  public static final Integer defaultMemoryMB = 100;
  public static final Integer defaultMaxMinutes = 10;
  public static final Boolean defaultIncludeLaunchFiles = true;

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
  public static final String appVersion1 = "0.0.1";
  public static final String appVersion2 = "0.0.2";
  public static final JobTypeEnum jobTypeBatch = JobTypeEnum.BATCH;
  public static final JobTypeEnum jobTypeFork = JobTypeEnum.FORK;
  public static final String appDescription1 =  "app description 1";
  public static final String appDescription2 =  "app description 2";
  public static final Boolean isEnabledTrue = true;
  public static final Boolean isEnabledFalse = false;
  public static final RuntimeEnum runtime1 = RuntimeEnum.DOCKER;
  public static final RuntimeEnum runtime2 = RuntimeEnum.SINGULARITY;
  public static final String runtimeVersion1 = "0.0.1";
  public static final String runtimeVersion2 = "0.0.2";
  public static final List<RuntimeOptionEnum> runtimeOptions1 = new ArrayList<>(List.of(RuntimeOptionEnum.SINGULARITY_RUN));
  public static final List<RuntimeOptionEnum> runtimeOptions2 = new ArrayList<>(List.of(RuntimeOptionEnum.SINGULARITY_START));
  public static final List<RuntimeOptionEnum> runtimeOptionsNull = null;
  public static final String containerImage1 = "containerImage1";
  public static final String containerImage2 = "containerImage2";
  public static final Boolean dynamicExecSystemTrue = true;
  public static final Boolean dynamicExecSystemFalse = false;
  public static final List<String> execSystemConstraints1 = Arrays.asList("Constraint1a AND", "Constraint1b");
  public static final List<String> execSystemConstraints2 = Arrays.asList("Constraint2a AND", "Constraint2b");
  public static final String execSystemId1 = "tapisv3-exec3";
  // TODO Create a new exec system that supports BATCH and has different logicalQueue name.
  public static final String execSystemId2 = "tapisv3-exec3";
  public static final String execSystemExecDir1 = "execSystemExecDir1";
  public static final String execSystemExecDir2 = "execSystemExecDir2";
  public static final String execSystemInputDir1 = "execSystemInputDir1";
  public static final String execSystemInputDir2 = "execSystemInputDir2";
  public static final String execSystemOutputDir1 = "execSystemOutputDir1";
  public static final String execSystemOutputDir2 = "execSystemOutputDir2";
  public static final String execSystemLogicalQueue1 = "dsnormal";
  public static final String execSystemLogicalQueue2 = "dsnormal";
  public static final String archiveSystemId1 = "tapisv3-storage";
  public static final String archiveSystemId2 = "tapisv3-exec3";
  public static final String archiveSystemIdNull = null;
  public static final String archiveSystemDir1 = "archiveSystemDir1";
  public static final String archiveSystemDir2 = "archiveSystemDir2";
  public static final Boolean archiveOnAppErrorTrue = true;
  public static final Boolean archiveOnAppErrorFalse = false;
  public static final String mpiCmd1 = "mpirun1";
  public static final String mpiCmd2 = "mpirun2";
  public static final String jobDescription1 = "job description 1";
  public static final String jobDescription2 = "job description 2";
  public static final Integer maxJobs1 = 1;
  public static final Integer maxJobs2 = 2;
  public static final Integer maxJobsMAX = Integer.MAX_VALUE;
  public static final Integer maxJobsPerUser1 = 1;
  public static final Integer maxJobsPerUser2 = 2;
  public static final Integer maxJobsPerUserMAX = Integer.MAX_VALUE;
  public static final Boolean strictFileInputsTrue = true;
  public static final Boolean strictFileInputsFalse = false;
  public static final Integer nodeCount1 = 10;
  public static final Integer nodeCount2 = 20;
  public static final Integer coresPerNode1 = 10;
  public static final Integer coresPerNode2 = 20;
  public static final Integer memoryMb1 = 32;
  public static final Integer memoryMb2 = 64;
  public static final Integer maxMinutes1 = 10;
  public static final Integer maxMinutes2 = 20;
  public static final FileInputModeEnum fileInputModeRequired = FileInputModeEnum.REQUIRED;
  public static final FileInputModeEnum fileInputModeOptional = FileInputModeEnum.OPTIONAL;
  public static final FileInputModeEnum fileInputModeFixed = FileInputModeEnum.FIXED;
  public static final FileInputModeEnum defaultFileInputMode = FileInputModeEnum.OPTIONAL;
  public static final ArgInputModeEnum argInputModeRequired = ArgInputModeEnum.REQUIRED;
  public static final ArgInputModeEnum argInputModeFixed = ArgInputModeEnum.FIXED;
  public static final ArgInputModeEnum appInputModeIncludeOnDemand = ArgInputModeEnum.INCLUDE_ON_DEMAND;
  public static final ArgInputModeEnum appInputModeIncludeByDefault = ArgInputModeEnum.INCLUDE_BY_DEFAULT;
  public static final ArgInputModeEnum defaultArgInputMode = ArgInputModeEnum.INCLUDE_ON_DEMAND;
  public static final Boolean deletedFalse = false;
  public static final Instant createdNull = null;
  public static final Instant updatedNull = null;
  public static final Boolean autoMountLocalTrue = true;
  public static final Boolean defaultAutoMountLocal = true;
  public static final String defaultKeyValuePairValue = "";

//  public static final List<KeyValuePair> metaKVPairs1 = Arrays.asList(new KeyValuePair().key("metaKey1A").value("metaVal1A"),
//                                                                     new KeyValuePair().key("metaKey1B").value("metaVal1B"));
//  public static final List<KeyValuePair> metaKVPairs2 = Arrays.asList(new KeyValuePair().key("metaKey2A").value("metaVal2A"),
//          new KeyValuePair().key("metaKey2B").value("metaVal2B"));

  public static final KeyValuePair kv1a = new KeyValuePair().key("key1").value("value1");
  public static final KeyValuePair kv1b = new KeyValuePair().key("HOME1").value("/home/testuser1");
  public static final KeyValuePair kv1c = new KeyValuePair().key("TMP1").value("/tmp1");
  public static final List<KeyValuePair> envVariables1 = new ArrayList<>(List.of(kv1a, kv1b, kv1c));
  public static final KeyValuePair kv2a = new KeyValuePair().key("key2").value("value2");
  public static final KeyValuePair kv2b = new KeyValuePair().key("HOME2").value("/home/testuser2");
  public static final KeyValuePair kv2c = new KeyValuePair().key("TMP2").value("/tmp2");
  public static final List<KeyValuePair> envVariables2 = new ArrayList<>(List.of(kv2a, kv2b, kv2c));
  public static final KeyValuePair kvMin = new KeyValuePair().key("keyMin");
  public static final List<KeyValuePair> envVariablesMin = new ArrayList<>(List.of(kvMin));

  public static final List<String> archiveIncludes1 = Arrays.asList("/archiveInclude1A", "/archiveInclude1B");
  public static final List<String> archiveExcludes1 = Arrays.asList("/archiveExclude1A", "/archiveExclude1B");
  public static final List<String> archiveIncludes2 = Arrays.asList("/archiveInclude2A", "/archiveInclude2B");
  public static final List<String> archiveExcludes2 = Arrays.asList("/archiveExclude2A", "/archiveExclude2B");
  public static final Boolean includeLaunchFilesTrue = true;
  public static final Boolean includeLaunchFilesFalse = false;
  public static final ParameterSetArchiveFilter archiveFilter1 = new ParameterSetArchiveFilter().includes(archiveIncludes1).excludes(archiveExcludes1).includeLaunchFiles(includeLaunchFilesTrue);
  public static final ParameterSetArchiveFilter archiveFilter2 = new ParameterSetArchiveFilter().includes(archiveIncludes2).excludes(archiveExcludes2).includeLaunchFiles(includeLaunchFilesFalse);
  public static final ParameterSetArchiveFilter archiveFilterMin = new ParameterSetArchiveFilter();

  public static final List<String> jobTags1 = Arrays.asList("jobtag1A", "jobtag1B");
  public static final List<String> jobTags2 = Arrays.asList("jobtag2A", "jobtag2B");

  public static final String src1A = "https://example.com/src1A";
  public static final String src1B = "https://example.com/src1B";
  public static final List<KeyValuePair> kvPairsFin1A = new ArrayList<>(List.of(kv1a, kv1b, kv1c));
  public static final List<KeyValuePair> kvPairsFin1B = new ArrayList<>(List.of(kv1a, kv1b, kv1c));
  public static final AppFileInput fin1A = new AppFileInput().name("fin1A").description("File input 1A")
                                                       .inputMode(fileInputModeRequired).autoMountLocal(autoMountLocalTrue)
                                                       .sourceUrl(src1A).targetPath("/target1A");
  public static final AppFileInput fin1B = new AppFileInput().name("finB1").description("File input 1B")
                                                       .inputMode(fileInputModeOptional).autoMountLocal(autoMountLocalTrue)
                                                       .sourceUrl(src1B).targetPath("/target1B");
  public static final List<AppFileInput> fileInputs1 = new ArrayList<>(List.of(fin1A, fin1B));

  public static final List<String> srcUrls1A = new ArrayList<>(List.of("https://example.com/src1Aa", "https://example.com/src1Ab"));
  public static final List<String> srcUrls1B = new ArrayList<>(List.of("https://example.com/src1Ba", "https://example.com/src1Bb"));
  public static final AppFileInputArray fia1A = new AppFileInputArray().name("fia1A").description("File input array 1A")
          .inputMode(fileInputModeRequired).targetDir("/targetDir1A").sourceUrls(srcUrls1A);
  public static final AppFileInputArray fia1B = new AppFileInputArray().name("fia1B").description("File input array 1B")
          .inputMode(fileInputModeFixed).targetDir("/targetDir1B").sourceUrls(srcUrls1B);
  public static final List<AppFileInputArray> fileInputArrays1 = new ArrayList<>(List.of(fia1A, fia1B));

  public static final String src2A = "https://example.com/src2A";
  public static final String src2B = "https://example.com/src2B";
  public static final List<KeyValuePair> kvPairsFin2A = new ArrayList<>(List.of(kv2a, kv2b, kv2c));
  public static final List<KeyValuePair> kvPairsFin2B = new ArrayList<>(List.of(kv2a, kv2b, kv2c));
  public static final AppFileInput fin2A = new AppFileInput().name("fin2A").description("File input 2A")
          .inputMode(fileInputModeRequired).autoMountLocal(autoMountLocalTrue)
          .sourceUrl(src2A).targetPath("/target2A");
  public static final AppFileInput fin2B = new AppFileInput().name("fin2B").description("File input 2B")
          .inputMode(fileInputModeFixed).autoMountLocal(autoMountLocalTrue)
          .sourceUrl(src2B).targetPath("/target2B");
  public static final List<AppFileInput> fileInputs2 = new ArrayList<>(List.of(fin2A, fin2B));

  public static final List<String> srcUrls2A = new ArrayList<>(List.of("https://example.com/src2Aa", "https://example.com/src2Ab"));
  public static final List<String> srcUrls2B = new ArrayList<>(List.of("https://example.com/src2Ba", "https://example.com/src2Bb"));
  public static final AppFileInputArray fia2A = new AppFileInputArray().name("fia2A").description("File input array 2A")
          .inputMode(fileInputModeRequired).targetDir("/targetDir2A").sourceUrls(srcUrls2A);
  public static final AppFileInputArray fia2B = new AppFileInputArray().name("fia2B").description("File input array 2B")
          .inputMode(fileInputModeFixed).targetDir("/targetDir2B").sourceUrls(srcUrls2B);
  public static final List<AppFileInputArray> fileInputArrays2 = new ArrayList<>(List.of(fia2A, fia2B));

  public static final AppFileInput finMin = new AppFileInput().name("finMin").targetPath("/targetMin");
  public static final List<AppFileInput> fileInputsMin = new ArrayList<>(List.of(finMin));

  public static final AppFileInputArray fiaMin = new AppFileInputArray().name("fiaMin").targetDir("/targetDirMin");
  public static final List<AppFileInputArray> fileInputArraysMin = new ArrayList<>(List.of(fiaMin));

  // NotificationSubscriptions
  public static final NotificationMechanism notifMech1Aa = new NotificationMechanism().mechanism(NotificationMechanismEnum.WEBHOOK).webhookURL("webhookUrl1Aa").emailAddress("emailAddress1Aa");
  public static final NotificationMechanism notifMech1Ab = new NotificationMechanism().mechanism(NotificationMechanismEnum.WEBHOOK).webhookURL("webhookUrl1Ab").emailAddress("emailAddress1Ab");
  public static final List<NotificationMechanism> notifMechList1A = new ArrayList<>(List.of(notifMech1Aa, notifMech1Ab));
  public static final NotificationMechanism notifMech1Ba = new NotificationMechanism().mechanism(NotificationMechanismEnum.WEBHOOK).webhookURL("webhookUrl1Ba").emailAddress("emailAddress1Ba");
  public static final NotificationMechanism notifMech1Bb = new NotificationMechanism().mechanism(NotificationMechanismEnum.WEBHOOK).webhookURL("webhookUrl1Bb").emailAddress("emailAddress1Bb");
  public static final List<NotificationMechanism> notifMechList1B = new ArrayList<>(List.of(notifMech1Ba, notifMech1Bb));
  public static final NotificationSubscription notif1A = new NotificationSubscription().filter("filter1A");
  public static final NotificationSubscription notif1B = new NotificationSubscription().filter("filter1B");
  static {
    notif1A.setNotificationMechanisms(notifMechList1A);
    notif1B.setNotificationMechanisms(notifMechList1B);
  }
  public static final List<NotificationSubscription> notifList1 = new ArrayList<>(List.of(notif1A, notif1B));

  public static final NotificationMechanism notifMech2Aa = new NotificationMechanism().mechanism(NotificationMechanismEnum.WEBHOOK).webhookURL("webhookUrl2Aa").emailAddress("emailAddress2Aa");
  public static final NotificationMechanism notifMech2Ab = new NotificationMechanism().mechanism(NotificationMechanismEnum.WEBHOOK).webhookURL("webhookUrl2Ab").emailAddress("emailAddress2Ab");
  public static final List<NotificationMechanism> notifMechList2A = new ArrayList<>(List.of(notifMech2Aa, notifMech2Ab));
  public static final NotificationMechanism notifMech2Ba = new NotificationMechanism().mechanism(NotificationMechanismEnum.WEBHOOK).webhookURL("webhookUrl2Ba").emailAddress("emailAddress2Ba");
  public static final NotificationMechanism notifMech2Bb = new NotificationMechanism().mechanism(NotificationMechanismEnum.WEBHOOK).webhookURL("webhookUrl2Bb").emailAddress("emailAddress2Bb");
  public static final List<NotificationMechanism> notifMechList2B = new ArrayList<>(List.of(notifMech2Ba, notifMech2Bb));
  public static final NotificationSubscription notif2A = new NotificationSubscription().filter("filter2A");
  public static final NotificationSubscription notif2B = new NotificationSubscription().filter("filter2B");
  static {
    notif2A.setNotificationMechanisms(notifMechList2A);
    notif2B.setNotificationMechanisms(notifMechList2B);
  }
  public static final List<NotificationSubscription> notifList2 = new ArrayList<>(List.of(notif2A, notif2B));
  public static final List<NotificationSubscription> notifListNull = null;

  public static final AppArgSpec appArgA1 = (new AppArgSpec()).name("appArgA1").arg("valueA1").description("App arg A1").inputMode(argInputModeRequired);
  public static final AppArgSpec appArgB1 = (new AppArgSpec()).name("appArgB1").arg("valueB1").description("App arg B1").inputMode(argInputModeFixed);
  public static final List<AppArgSpec> appArgs1 = new ArrayList<>(List.of(appArgA1, appArgB1));
  public static final AppArgSpec containerArgA1 = (new AppArgSpec()).name("containerArgA1").arg("valueA1").description("container arg A1").inputMode(argInputModeRequired);
  public static final AppArgSpec containerArgB1 = (new AppArgSpec()).name("containerArgB1").arg("valueB1").description("container arg B1").inputMode(argInputModeFixed);
  public static final List<AppArgSpec> containerArgs1 = new ArrayList<>(List.of(containerArgA1, containerArgB1));
  public static final AppArgSpec schedulerOptionA1 = (new AppArgSpec()).name("schedulerOptionA1").arg("valueA1").description("scheduler option A1").inputMode(argInputModeRequired);
  public static final AppArgSpec schedulerOptionB1 = (new AppArgSpec()).name("schedulerOptionB1").arg("valueB1").description("scheduler option B1").inputMode(argInputModeFixed);
  public static final List<AppArgSpec> schedulerOptions1 = new ArrayList<>(List.of(schedulerOptionA1, schedulerOptionB1));

  public static final AppArgSpec appArgA2 = (new AppArgSpec()).name("appArgA2").arg("valueA2").description("App arg A2").inputMode(argInputModeRequired);
  public static final AppArgSpec appArgB2 = (new AppArgSpec()).name("appArgB2").arg("valueB2").description("App arg B2").inputMode(argInputModeFixed);
  public static final List<AppArgSpec> appArgs2 = new ArrayList<>(List.of(appArgA2, appArgB2));
  public static final AppArgSpec containerArgA2 = (new AppArgSpec()).name("containerArgA2").arg("valueA2").description("container arg A2").inputMode(argInputModeRequired);
  public static final AppArgSpec containerArgB2 = (new AppArgSpec()).name("containerArgB2").arg("valueB2").description("container arg B2").inputMode(argInputModeFixed);
  public static final List<AppArgSpec> containerArgs2 = new ArrayList<>(List.of(containerArgA2, containerArgB2));
  public static final AppArgSpec schedulerOptionA2 = (new AppArgSpec()).name("schedulerOptionA2").arg("valueA2").description("scheduler option A2").inputMode(argInputModeRequired);
  public static final AppArgSpec schedulerOptionB2 = (new AppArgSpec()).name("schedulerOptionB2").arg("valueB2").description("scheduler option B2").inputMode(argInputModeFixed);
  public static final List<AppArgSpec> schedulerOptions2 = new ArrayList<>(List.of(schedulerOptionA2, schedulerOptionB2));

  public static final AppArgSpec appArgMin = (new AppArgSpec()).name("appArgMin");
  public static final List<AppArgSpec> appArgsMin = new ArrayList<>(List.of(appArgMin));
  public static final AppArgSpec containerArgMin = (new AppArgSpec()).name("containerArgMin");
  public static final List<AppArgSpec> containerArgsMin = new ArrayList<>(List.of(containerArgMin));
  public static final AppArgSpec schedulerOptionMin = (new AppArgSpec()).name("schedulerOptionMin");
  public static final List<AppArgSpec> schedulerOptionsMin = new ArrayList<>(List.of(schedulerOptionMin));

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
//    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=jobType, 5=ownerUser1,
//              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
//              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
//              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
      String[] app0 = {tenantName, appId, appVersion1, appDescription1+suffix, jobTypeBatch.name(), ownerUser1,
                       runtime1.name(), runtimeVersion1+suffix, defaultContainerImage, jobDescription1+suffix,
              execSystemId1, execSystemExecDir1+suffix, execSystemInputDir1+suffix, execSystemOutputDir1+suffix,
              execSystemLogicalQueue1, archiveSystemId1, archiveSystemDir1+suffix};
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
    ReqPostApp rApp = new ReqPostApp();
    rApp.setId(app[1]);
    rApp.setVersion(app[2]);
    rApp.description(app[3]);
    rApp.owner(app[5]);
    rApp.enabled(isEnabledTrue);
    rApp.setRuntime(RuntimeEnum.valueOf(app[6]));
    rApp.setRuntimeVersion(app[7]);
    rApp.setRuntimeOptions(runtimeOptions1);
    rApp.setContainerImage(app[8]);
    rApp.setJobType(JobTypeEnum.valueOf(app[4]));
    rApp.setMaxJobs(maxJobs1);
    rApp.setMaxJobsPerUser(maxJobsPerUser1);
    rApp.strictFileInputs(strictFileInputsFalse);

    // === Start Job Attributes
    JobAttributes jobAttrs = new JobAttributes();
    jobAttrs.setDescription(app[9]);
    jobAttrs.setDynamicExecSystem(dynamicExecSystemTrue);
    jobAttrs.setExecSystemConstraints(execSystemConstraints1);
    jobAttrs.setExecSystemId(app[10]);
    jobAttrs.setExecSystemExecDir(app[11]);
    jobAttrs.setExecSystemInputDir(app[12]);
    jobAttrs.setExecSystemOutputDir(app[13]);
    jobAttrs.setExecSystemLogicalQueue(app[14]);
    jobAttrs.setArchiveSystemId(app[15]);
    jobAttrs.setArchiveSystemDir(app[16]);
    jobAttrs.setArchiveOnAppError(archiveOnAppErrorTrue);
    jobAttrs.setMpiCmd(mpiCmd1);
    // ====== Start Parameter Set
    ParameterSet parameterSet = new ParameterSet();
    parameterSet.setAppArgs(appArgs1);
    parameterSet.setContainerArgs(containerArgs1);
    parameterSet.setSchedulerOptions(schedulerOptions1);
    parameterSet.setEnvVariables(envVariables1);
    ParameterSetArchiveFilter archiveFilter = new ParameterSetArchiveFilter();
    archiveFilter.setIncludes(archiveIncludes1);
    archiveFilter.setExcludes(archiveExcludes1);
    archiveFilter.includeLaunchFiles(includeLaunchFilesTrue);
    parameterSet.setArchiveFilter(archiveFilter);
    // ====== End Parameter Set
    jobAttrs.setParameterSet(parameterSet);

    jobAttrs.setFileInputs(fileInputs1);
    jobAttrs.setFileInputArrays(fileInputArrays1);
    jobAttrs.setNodeCount(nodeCount1);
    jobAttrs.setCoresPerNode(coresPerNode1);
    jobAttrs.setMemoryMB(memoryMb1);
    jobAttrs.setMaxMinutes(maxMinutes1);
    jobAttrs.setSubscriptions(notifList1);
    jobAttrs.setTags(jobTags1);
    // === End Job Attributes
    rApp.setJobAttributes(jobAttrs);

    rApp.tags(tags1);
    rApp.notes(notes1JO);

    // Create the app
    return clt.createApp(rApp);
  }

  /**
   * Create an application using only required attributes.
   * In simplest case these are required: id, version, containerImage, execSystemId
   * Use attributes from a string array.
   *    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=jobType, 5=ownerUser1,
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
    ReqPostApp rApp = new ReqPostApp();
    // Id, version are always required
    rApp.setId(app[1]);
    rApp.setVersion(app[2]);
    // Containerized so container image must be set. NOTE: currently only containerized supported
    rApp.setContainerImage(defaultContainerImage);

    // Use client to create the app
    return clt.createApp(rApp);
  }

  /**
   * Create an application using minimal attributes plus:
   *   jobAttrs->(fileInput, fileInputArray),
   *   jobAttrs->parameterSet->(appArg, containerArg, schedulerOption, envVariable->keyValPair, archiveFilter)
   * In simplest case these are required: id, version, containerImage, execSystemId
   * Use attributes from a string array.
   *    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=jobType, 5=ownerUser1,
   *              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
   *              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
   *              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
   *
   * @param clt - Apps client
   * @param app - Array of attributes that can be represented as strings.
   * @return Response from the createApp client call
   * @throws TapisClientException - on error
   */
  public static String createAppMinimal2(AppsClient clt, String[] app) throws TapisClientException
  {
    ReqPostApp rApp = new ReqPostApp();
    // Id, version and type are always required
    rApp.setId(app[1]);
    rApp.setVersion(app[2]);
    // Containerized so container image must be set. NOTE: currently only containerized supported
    rApp.setContainerImage(defaultContainerImage);
    // === Start Job Attributes
    JobAttributes jobAttrs = new JobAttributes();
    // dynamiceExecSystem defaults to false so execSystemId must be set. This is the simplest minimal App
    jobAttrs.setExecSystemId(app[10]);
    // Minimal fileInput, fileInputArray, appArg, etc.
    jobAttrs.setFileInputs(fileInputsMin);
    jobAttrs.setFileInputArrays(fileInputArraysMin);

    ParameterSet parameterSet = new ParameterSet();
    parameterSet.setAppArgs(appArgsMin);
    parameterSet.setContainerArgs(containerArgsMin);
    parameterSet.setSchedulerOptions(schedulerOptionsMin);
    parameterSet.setEnvVariables(envVariablesMin);
    parameterSet.setArchiveFilter(archiveFilterMin);

    jobAttrs.setParameterSet(parameterSet);
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
    ReqPostApp rApp = makeReqPostAppFromTapisApp(app);
    // Use client to create the app
    return clt.createApp(rApp);
  }

  /**
   * Verify most attributes for an App
   * Some attributes are in String[] app0, others must be passed in.
   *
   * @param tmpApp - app retrieved from the service
   * @param app0 - Data used to create the app
   */
  public static void verifyAppAttributes(TapisApp tmpApp, String[] app0, Boolean isEnabled,
                                         List<RuntimeOptionEnum> runtimeOptions, Integer maxJobs, Integer maxJobsPerUser,
                                         Boolean strictFileInputs, Boolean dynamicExecSystem,
                                         List<String> execSystemConstraints, Boolean archiveOnAppError, String mpiCmd,
                                         List<AppArgSpec> appArgs, List<AppArgSpec> containerArgs,List<AppArgSpec> schedulerOptions,
                                         List<KeyValuePair> envVariables, ParameterSetArchiveFilter archiveFilter,
                                         Integer nodeCount, Integer coresPerNode, Integer memoryMb, Integer maxMinutes,
                                         List<AppFileInput> fileInputs, List<AppFileInputArray> fileInputArrays,
                                         List<String> jobTags, List<NotificationSubscription> notificationSubscriptions,
                                         List<String> tags, JsonObject notes)
  {
//    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=jobType, 5=ownerUser1,
//              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
//              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
//              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
    Assert.assertEquals(tmpApp.getTenant(), app0[0]);
    Assert.assertEquals(tmpApp.getId(), app0[1]);
    Assert.assertEquals(tmpApp.getVersion(), app0[2]);
    Assert.assertEquals(tmpApp.getDescription(), app0[3]);
    Assert.assertNotNull(tmpApp.getJobType());
    Assert.assertEquals(tmpApp.getJobType().name(), app0[4]);
    Assert.assertEquals(tmpApp.getOwner(), app0[5]);
    Assert.assertEquals(tmpApp.getEnabled(), isEnabled);
    Assert.assertNotNull(tmpApp.getRuntime());
    Assert.assertEquals(tmpApp.getRuntime().name(), app0[6]);
    Assert.assertEquals(tmpApp.getRuntimeVersion(), app0[7]);
    // Verify runtimeOptions
    List<RuntimeOptionEnum> rtOps = tmpApp.getRuntimeOptions();
    Assert.assertNotNull(rtOps);
    for (RuntimeOptionEnum rtOption : runtimeOptions)
    {
      Assert.assertTrue(rtOps.contains(rtOption), "List of runtime options did not contain: " + rtOption.name());
    }
    Assert.assertEquals(tmpApp.getContainerImage(), app0[8]);
    Assert.assertEquals(tmpApp.getMaxJobs(), maxJobs);
    Assert.assertEquals(tmpApp.getMaxJobsPerUser(), maxJobsPerUser);
    Assert.assertEquals(tmpApp.getStrictFileInputs(), strictFileInputs);

    // ========== JobAttributes
    JobAttributes jobAttributes = tmpApp.getJobAttributes();
    Assert.assertNotNull(jobAttributes);
    Assert.assertEquals(jobAttributes.getDescription(), app0[9]);
    Assert.assertEquals(jobAttributes.getDynamicExecSystem(), dynamicExecSystem);
    // Verify execSystemConstraints
    List<String> origExecSystemConstraints = execSystemConstraints;
    List<String> tmpExecSystemConstraints = jobAttributes.getExecSystemConstraints();
    Assert.assertNotNull(tmpExecSystemConstraints, "execSystemConstraints value was null");
    Assert.assertEquals(tmpExecSystemConstraints.size(), origExecSystemConstraints.size(), "Wrong number of constraints");
    for (String execSystemConstraintStr : origExecSystemConstraints)
    {
      Assert.assertTrue(tmpExecSystemConstraints.contains(execSystemConstraintStr));
      System.out.println("Found execSystemConstraint: " + execSystemConstraintStr);
    }
    Assert.assertEquals(jobAttributes.getExecSystemId(), app0[10]);
    Assert.assertEquals(jobAttributes.getExecSystemExecDir(), app0[11]);
    Assert.assertEquals(jobAttributes.getExecSystemInputDir(), app0[12]);
    Assert.assertEquals(jobAttributes.getExecSystemOutputDir(), app0[13]);
    Assert.assertEquals(jobAttributes.getExecSystemLogicalQueue(), app0[14]);
    Assert.assertEquals(jobAttributes.getArchiveSystemId(), app0[15]);
    Assert.assertEquals(jobAttributes.getArchiveSystemDir(), app0[16]);
    Assert.assertEquals(jobAttributes.getArchiveOnAppError(), archiveOnAppError);
    Assert.assertEquals(jobAttributes.getMpiCmd(), mpiCmd);

    // Verify parameterSet
    ParameterSet parmSet = jobAttributes.getParameterSet();
    Assert.assertNotNull(parmSet, "parameterSet was null");
    verifyAppArgs("App Arg", appArgs, parmSet.getAppArgs());
    verifyAppArgs("Container Arg", containerArgs, parmSet.getContainerArgs());
    verifyAppArgs("Scheduler Option Arg", schedulerOptions, parmSet.getSchedulerOptions());

    // Verify envVariables
    verifyKeyValuePairs("Env Var", envVariables, parmSet.getEnvVariables());

    // Verify archiveFilter in parameterSet
    ParameterSetArchiveFilter tmpArchiveFilter = parmSet.getArchiveFilter();
    Assert.assertNotNull(tmpArchiveFilter, "archiveFilter was null");
    Assert.assertEquals(tmpArchiveFilter.getIncludeLaunchFiles(), archiveFilter.getIncludeLaunchFiles());
    // Verify archiveIncludes
    List<String> archiveIncludesList = tmpArchiveFilter.getIncludes();
    Assert.assertNotNull(archiveIncludesList, "archiveIncludes value was null");
    Assert.assertEquals(archiveIncludesList.size(), archiveFilter.getIncludes().size(), "Wrong number of archiveIncludes");
    for (String archiveIncludeStr : archiveFilter.getIncludes())
    {
      Assert.assertTrue(archiveIncludesList.contains(archiveIncludeStr));
      System.out.println("Found archiveInclude: " + archiveIncludeStr);
    }
    // Verify archiveExcludes
    List<String> archiveExcludesList = tmpArchiveFilter.getExcludes();
    Assert.assertNotNull(archiveExcludesList, "archiveExcludes value was null");
    Assert.assertEquals(archiveExcludesList.size(), archiveFilter.getExcludes().size(), "Wrong number of archiveExcludes");
    for (String archiveExcludeStr : archiveFilter.getExcludes())
    {
      Assert.assertTrue(archiveExcludesList.contains(archiveExcludeStr));
      System.out.println("Found archiveExclude: " + archiveExcludeStr);
    }

    // Verify file inputs and file input arrays
    verifyFileInputs(fileInputs, jobAttributes.getFileInputs());
    verifyFileInputArrays(fileInputArrays, jobAttributes.getFileInputArrays());

    Assert.assertEquals(jobAttributes.getNodeCount(), nodeCount);
    Assert.assertEquals(jobAttributes.getCoresPerNode(), coresPerNode);
    Assert.assertEquals(jobAttributes.getMemoryMB(), memoryMb);
    Assert.assertEquals(jobAttributes.getMaxMinutes(), maxMinutes);

    verifySubscriptions(notificationSubscriptions, jobAttributes.getSubscriptions());
    // ???????????????/
    // TODO Verify notificationSubscriptions
    // TODO: Filter is checked but not mechanisms
//    List<NotificationSubscription> tSubscriptions = jobAttributes.getSubscriptions();
//    Assert.assertNotNull(tSubscriptions, "Subscriptions list should not be null.");
//    Assert.assertEquals(tSubscriptions.size(), notifList1.size(), "Wrong number of Subscriptions");
//    var filtersFound = new ArrayList<String>();
//    for (NotificationSubscription itemFound : tSubscriptions)
//    {
//      Assert.assertNotNull(itemFound.getFilter(), "Subscription filter should not be null.");
//      filtersFound.add(itemFound.getFilter());
//    }
//    for (NotificationSubscription itemSeedItem : notifList1)
//    {
//      Assert.assertTrue(filtersFound.contains(itemSeedItem.getFilter()),
//              "List of subscriptions did not contain a filter: " + itemSeedItem.getFilter());
//    }
//    // Verify jobTags
//    List<String> tmpJobTags = jobAttributes.getTags();
//    Assert.assertNotNull(tmpJobTags, "jobTags value was null");
//    Assert.assertEquals(tmpJobTags.size(), jobTags1.size(), "Wrong number of jobTags");
//    for (String tagStr : jobTags1)
//    {
//      Assert.assertTrue(tmpJobTags.contains(tagStr));
//      System.out.println("Found jobTag: " + tagStr);
//    }
    // ????????????????//
    // TODO Verify notificationSubscriptions
    // TODO: Filter is checked but not mechanisms
//    List<NotificationSubscription> tSubscriptions = jobAttributes.getSubscriptions();
//    Assert.assertNotNull(tSubscriptions, "Subscriptions list should not be null.");
//    Assert.assertEquals(tSubscriptions.size(), notifList1.size(), "Wrong number of Subscriptions");
//    var filtersFound = new ArrayList<String>();
//    for (NotificationSubscription itemFound : tSubscriptions)
//    {
//      Assert.assertNotNull(itemFound.getFilter(), "Subscription filter should not be null.");
//      filtersFound.add(itemFound.getFilter());
//    }
//    for (NotificationSubscription itemSeedItem : notificationSubscriptions)
//    {
//      Assert.assertTrue(filtersFound.contains(itemSeedItem.getFilter()),
//              "List of subscriptions did not contain a filter: " + itemSeedItem.getFilter());
//    }
    // Verify jobTags
    List<String> tmpJobTags = jobAttributes.getTags();
    Assert.assertNotNull(tmpJobTags, "jobTags value was null");
    Assert.assertEquals(tmpJobTags.size(), jobTags.size(), "Wrong number of jobTags");
    for (String tagStr : jobTags)
    {
      Assert.assertTrue(tmpJobTags.contains(tagStr));
      System.out.println("Found jobTag: " + tagStr);
    }

    // Verify tags
    List<String> tmpTags = tmpApp.getTags();
    Assert.assertNotNull(tmpTags, "Tags value was null");
    Assert.assertEquals(tmpTags.size(), tags.size(), "Wrong number of tags");
    for (String tagStr : tags)
    {
      Assert.assertTrue(tmpTags.contains(tagStr));
      System.out.println("Found tag: " + tagStr);
    }
    // Verify notes
    String tmpNotesStr = (String) tmpApp.getNotes();
    System.out.println("Found notes: " + tmpNotesStr);
    JsonObject tmpNotes = ClientTapisGsonUtils.getGson().fromJson(tmpNotesStr, JsonObject.class);
    Assert.assertNotNull(tmpNotes, "Fetched Notes should not be null.");
    JsonObject origNotes = notes;
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
   //    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=jobType, 5=ownerUser1,
   //              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
   //              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
   //              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
   String[] app0 = {tenantName, appId, appVersion, "description "+suffix, jobTypeBatch.name(), ownerUser1,
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
    Assert.assertEquals(tmpApp.getVersion(), appVersion1);
    Assert.assertNotNull(tmpApp.getJobType());
    Assert.assertEquals(tmpApp.getJobType().name(), jobTypeBatch.name());
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
    Assert.assertEquals(tmpApp.getMaxJobs(), maxJobsMAX);
    Assert.assertNotNull(tmpApp.getMaxJobsPerUser());
    Assert.assertEquals(tmpApp.getMaxJobsPerUser(), maxJobsPerUserMAX);
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
    Assert.assertNull(jobAttrs.getMpiCmd());
    Assert.assertNotNull(jobAttrs.getFileInputs());
    Assert.assertTrue(jobAttrs.getFileInputs().isEmpty());
    Assert.assertNotNull(jobAttrs.getFileInputArrays());
    Assert.assertTrue(jobAttrs.getFileInputArrays().isEmpty());
    Assert.assertEquals(jobAttrs.getNodeCount(), defaultNodeCount);
    Assert.assertEquals(jobAttrs.getCoresPerNode(), defaultCoresPerNode);
    Assert.assertEquals(jobAttrs.getMemoryMB(), defaultMemoryMB);
    Assert.assertEquals(jobAttrs.getMaxMinutes(), defaultMaxMinutes);
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
    Assert.assertEquals(archiveFilter.getIncludeLaunchFiles(), defaultIncludeLaunchFiles);
  }

  /**
   * Verify expected defaults are found for testCreateAndGetAppMinimal2
   //    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=jobType, 5=ownerUser1,
   //              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
   //              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
   //              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
   String[] app0 = {tenantName, appId, appVersion, "description "+suffix, jobTypeBatch.name(), ownerUser1,
   runtime.name(), runtimeVersion+suffix, containerImage+suffix, jobDescription+suffix,
   execSystemId, execSystemExecDir+suffix, execSystemInputDir+suffix, execSystemOutputDir+suffix,
   execSystemLogicalQueue, archiveSystemId, archiveSystemDir+suffix};
   * @param tmpApp - app retrieved from the service
   */
  public static void verifyAppDefaults2(TapisApp tmpApp, String appId)
  {
    Assert.assertEquals(tmpApp.getTenant(), tenantName);
    // Verify required attributes
    Assert.assertEquals(tmpApp.getId(), appId);
    Assert.assertEquals(tmpApp.getVersion(), appVersion1);
    Assert.assertNotNull(tmpApp.getJobType());
    Assert.assertEquals(tmpApp.getJobType().name(), jobTypeBatch.name());
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
    Assert.assertEquals(tmpApp.getMaxJobs(), maxJobsMAX);
    Assert.assertNotNull(tmpApp.getMaxJobsPerUser());
    Assert.assertEquals(tmpApp.getMaxJobsPerUser(), maxJobsPerUserMAX);
    Assert.assertEquals(tmpApp.getStrictFileInputs(), Boolean.valueOf(defaultStrictFileInputs));
    Assert.assertNotNull(tmpApp.getTags());
    Assert.assertTrue(tmpApp.getTags().isEmpty());
    Assert.assertNotNull(tmpApp.getNotes());
    Assert.assertEquals((String) tmpApp.getNotes(), defaultNotesStr);
    Assert.assertEquals(tmpApp.getDeleted(), Boolean.valueOf(defaultDeleted));
    // jobAttributes
    JobAttributes jobAttrs = tmpApp.getJobAttributes();
    Assert.assertEquals(jobAttrs.getDescription(), defaultDescription);
    Assert.assertEquals(jobAttrs.getDynamicExecSystem(), defaultDynamicExecSystem);
    Assert.assertNull(jobAttrs.getExecSystemExecDir());
    Assert.assertNull(jobAttrs.getExecSystemInputDir());
    Assert.assertNull(jobAttrs.getExecSystemOutputDir());
    Assert.assertNull(jobAttrs.getExecSystemLogicalQueue());
    Assert.assertNull(jobAttrs.getArchiveSystemId());
    Assert.assertNull(jobAttrs.getArchiveSystemDir());
    Assert.assertNotNull(jobAttrs.getArchiveOnAppError());
    Assert.assertEquals(jobAttrs.getArchiveOnAppError(), defaultArchiveOnAppError);
    Assert.assertNull(jobAttrs.getMpiCmd());
    // Check file inputs and file input arrays
    Assert.assertNotNull(jobAttrs.getFileInputs());
    Assert.assertEquals(jobAttrs.getFileInputs().size(), fileInputsMin.size());
    Assert.assertEquals(jobAttrs.getFileInputs().get(0).getName(), fileInputsMin.get(0).getName());
    Assert.assertEquals(jobAttrs.getFileInputs().get(0).getTargetPath(), fileInputsMin.get(0).getTargetPath());
    Assert.assertEquals(jobAttrs.getFileInputs().get(0).getAutoMountLocal(), defaultAutoMountLocal);
    Assert.assertEquals(jobAttrs.getFileInputs().get(0).getInputMode(), defaultFileInputMode);

    Assert.assertNotNull(jobAttrs.getFileInputArrays());
    Assert.assertEquals(jobAttrs.getFileInputArrays().size(), fileInputArraysMin.size());
    Assert.assertEquals(jobAttrs.getFileInputArrays().get(0).getInputMode(), defaultFileInputMode);
    Assert.assertEquals(jobAttrs.getFileInputArrays().get(0).getName(), fileInputArraysMin.get(0).getName());
    Assert.assertEquals(jobAttrs.getFileInputArrays().get(0).getTargetDir(), fileInputArraysMin.get(0).getTargetDir());

    Assert.assertEquals(jobAttrs.getNodeCount(), defaultNodeCount);
    Assert.assertEquals(jobAttrs.getCoresPerNode(), defaultCoresPerNode);
    Assert.assertEquals(jobAttrs.getMemoryMB(), defaultMemoryMB);
    Assert.assertEquals(jobAttrs.getMaxMinutes(), defaultMaxMinutes);
    Assert.assertNotNull(jobAttrs.getSubscriptions());
    Assert.assertTrue(jobAttrs.getSubscriptions().isEmpty());
    Assert.assertNotNull(jobAttrs.getTags());
    Assert.assertTrue(jobAttrs.getTags().isEmpty());
    // jobAttributes.parameterSet
    ParameterSet parmSet = jobAttrs.getParameterSet();
    Assert.assertNotNull(parmSet);
    Assert.assertNotNull(parmSet.getAppArgs());
    Assert.assertEquals(parmSet.getAppArgs().size(), appArgsMin.size());
    Assert.assertEquals(parmSet.getAppArgs().get(0).getName(), appArgsMin.get(0).getName());
    Assert.assertEquals(parmSet.getAppArgs().get(0).getInputMode(), defaultArgInputMode);
    Assert.assertNotNull(parmSet.getContainerArgs());
    Assert.assertEquals(parmSet.getContainerArgs().size(), containerArgsMin.size());
    Assert.assertEquals(parmSet.getContainerArgs().get(0).getName(), containerArgsMin.get(0).getName());
    Assert.assertEquals(parmSet.getContainerArgs().get(0).getInputMode(), defaultArgInputMode);
    Assert.assertNotNull(parmSet.getSchedulerOptions());
    Assert.assertEquals(parmSet.getSchedulerOptions().size(), schedulerOptionsMin.size());
    Assert.assertEquals(parmSet.getSchedulerOptions().get(0).getName(), schedulerOptionsMin.get(0).getName());
    Assert.assertEquals(parmSet.getSchedulerOptions().get(0).getInputMode(), defaultArgInputMode);
    Assert.assertNotNull(parmSet.getEnvVariables());
    Assert.assertEquals(parmSet.getEnvVariables().size(), envVariablesMin.size());
    Assert.assertEquals(parmSet.getEnvVariables().get(0).getKey(), envVariablesMin.get(0).getKey());
    Assert.assertEquals(parmSet.getEnvVariables().get(0).getValue(), defaultKeyValuePairValue);

    // parameterSet.archiveFilter
    ParameterSetArchiveFilter archiveFilter = parmSet.getArchiveFilter();
    Assert.assertNotNull(archiveFilter);
    Assert.assertNotNull(archiveFilter.getIncludes());
    Assert.assertTrue(archiveFilter.getIncludes().isEmpty());
    Assert.assertNotNull(archiveFilter.getExcludes());
    Assert.assertTrue(archiveFilter.getExcludes().isEmpty());
    Assert.assertEquals(archiveFilter.getIncludeLaunchFiles(), defaultIncludeLaunchFiles);
  }
  /*
   * Generate a full ReqPostApp using attributes from a TapisApp
   */
  private static ReqPostApp makeReqPostAppFromTapisApp(TapisApp app) throws TapisClientException
  {
    ReqPostApp rApp = new ReqPostApp();
    // Id, version
    rApp.setId(app.getId());
    rApp.setVersion(app.getVersion());
    rApp.setDescription(app.getDescription());
    rApp.setOwner(app.getOwner());
    rApp.setContainerImage(app.getContainerImage());
// TODO ???
    rApp.setJobAttributes(app.getJobAttributes());
    rApp.setJobType(app.getJobType());
    return rApp;
  }

  // Verify that original list of AppArgs matches the fetched list
  private static void verifyAppArgs(String argType, List<AppArgSpec> origArgs, List<AppArgSpec> fetchedArgs)
  {
    System.out.println("Verifying fetched AppArgs of type: " + argType);
    Assert.assertNotNull(origArgs, "Orig AppArgs is null");
    Assert.assertNotNull(fetchedArgs, "Fetched AppArgs is null");
    Assert.assertEquals(fetchedArgs.size(), origArgs.size());
    // Create hash maps of orig and fetched with name as key
    var origMap = new HashMap<String, AppArgSpec>();
    var fetchedMap = new HashMap<String, AppArgSpec>();
    for (AppArgSpec a : origArgs) origMap.put(a.getName(), a);
    for (AppArgSpec a : fetchedArgs) fetchedMap.put(a.getName(), a);
    // Go through origMap and check properties
    for (String argName : origMap.keySet())
    {
      Assert.assertTrue(fetchedMap.containsKey(argName), "Fetched list does not contain original item: " + argName);
      AppArgSpec fetchedArg = fetchedMap.get(argName);
      AppArgSpec origArg = origMap.get(argName);
      System.out.println("Found fetched item: " + argName);
      Assert.assertEquals(fetchedArg.getArg(), origArg.getArg());
      Assert.assertEquals(fetchedArg .getDescription(), origArg.getDescription());
      Assert.assertEquals(fetchedArg.getInputMode(), origArg.getInputMode());
//      verifyKeyValuePairs(argType, fetchedArg.getMeta(), origArg.getMeta());
    }
  }

  // Verify that original list of KeyValuePairs matches the fetched list
  public static void verifyKeyValuePairs(String argType, List<KeyValuePair> origKVs, List<KeyValuePair> fetchedKVs)
  {
    System.out.println("Verifying fetched KV pairs of type: " + argType);
    Assert.assertNotNull(origKVs, "Orig KVs is null");
    Assert.assertNotNull(fetchedKVs, "Fetched KVs is null");
    Assert.assertEquals(fetchedKVs.size(), origKVs.size());
    // Create hash maps of orig and fetched with KV key as key
    var origMap = new HashMap<String, KeyValuePair>();
    var fetchedMap = new HashMap<String, KeyValuePair>();
    for (KeyValuePair kv : origKVs) origMap.put(kv.getKey(), kv);
    for (KeyValuePair kv : fetchedKVs) fetchedMap.put(kv.getKey(), kv);
    // Go through origMap and check properties
    for (String kvKey : origMap.keySet())
    {
      Assert.assertTrue(fetchedMap.containsKey(kvKey), "Fetched list does not contain original item: " + kvKey);
      KeyValuePair fetchedKV = fetchedMap.get(kvKey);
      System.out.println("Found fetched KeyValuePair: " + fetchedKV);
      Assert.assertEquals(fetchedMap.get(kvKey).toString(), origMap.get(kvKey).toString());
    }
  }

  // Verify that original list of FileInputs matches the fetched list
  public static void verifyFileInputs(List<AppFileInput> origFileInputs, List<AppFileInput> fetchedFileInputs)
  {
    System.out.println("Verifying list of FileInputs");
    Assert.assertNotNull(origFileInputs, "Orig FileInputs is null");
    Assert.assertNotNull(fetchedFileInputs, "Fetched FileInputs is null");
    Assert.assertEquals(fetchedFileInputs.size(), origFileInputs.size());
    // Create hash maps of orig and fetched with name as key
    var origMap = new HashMap<String, AppFileInput>();
    var fetchedMap = new HashMap<String, AppFileInput>();
    for (AppFileInput fi : origFileInputs) origMap.put(fi.getName(), fi);
    for (AppFileInput fi : fetchedFileInputs) fetchedMap.put(fi.getName(), fi);
    // Go through origMap and check properties
    for (String fiName : origMap.keySet())
    {
      Assert.assertTrue(fetchedMap.containsKey(fiName), "Fetched list does not contain original item: " + fiName);
      AppFileInput fetchedFileInput = fetchedMap.get(fiName);
      AppFileInput origFileInput = origMap.get(fiName);
      System.out.println("Found fetched FileInput: " + fiName);
      Assert.assertEquals(fetchedFileInput.getSourceUrl(), origFileInput.getSourceUrl());
      Assert.assertEquals(fetchedFileInput.getTargetPath(), origFileInput.getTargetPath());
      Assert.assertEquals(fetchedFileInput.getDescription(), origFileInput.getDescription());
      Assert.assertEquals(fetchedFileInput.getInputMode(), origFileInput.getInputMode());
//      verifyKeyValuePairs("FileInput", fetchedFileInput.getMeta(), origFileInput.getMeta());
    }
  }

  // Verify that original list of FileInputArrays matches the fetched list
  public static void verifyFileInputArrays(List<AppFileInputArray> origFileInputArrays, List<AppFileInputArray> fetchedFileInputArrays)
  {
    System.out.println("Verifying list of FileInputArrays");
    Assert.assertNotNull(origFileInputArrays, "Orig FileInputArrays is null");
    Assert.assertNotNull(fetchedFileInputArrays, "Fetched FileInputArrays is null");
    Assert.assertEquals(fetchedFileInputArrays.size(), origFileInputArrays.size());
    // Create hash maps of orig and fetched with name as key
    var origMap = new HashMap<String, AppFileInputArray>();
    var fetchedMap = new HashMap<String, AppFileInputArray>();
    for (AppFileInputArray fia : origFileInputArrays) origMap.put(fia.getName(), fia);
    for (AppFileInputArray fia : fetchedFileInputArrays) fetchedMap.put(fia.getName(), fia);
    // Go through origMap and check properties
    for (String fiaName : origMap.keySet())
    {
      Assert.assertTrue(fetchedMap.containsKey(fiaName), "Fetched list does not contain original item: " + fiaName);
      AppFileInputArray fetchedFileInputArray = fetchedMap.get(fiaName);
      AppFileInputArray origFileInputArray = origMap.get(fiaName);
      System.out.println("Found fetched FileInputArray: " + fiaName);
      if (origFileInputArray.getSourceUrls() == null)
      {
        Assert.assertNull(fetchedFileInputArray.getSourceUrls());
      }
      else
      {
        Assert.assertNotNull(fetchedFileInputArray.getSourceUrls());
        Assert.assertEquals(fetchedFileInputArray.getSourceUrls().size(), origFileInputArray.getSourceUrls().size());
      }
      Assert.assertEquals(fetchedFileInputArray.getTargetDir(), origFileInputArray.getTargetDir());
      Assert.assertEquals(fetchedFileInputArray.getDescription(), origFileInputArray.getDescription());
      Assert.assertEquals(fetchedFileInputArray.getInputMode(), origFileInputArray.getInputMode());
    }
  }

  // Verify that original list of Subscriptions matches the fetched list
  public static void verifySubscriptions(List<NotificationSubscription> origSubscriptions, List<NotificationSubscription> fetchedSubscriptions)
  {
    System.out.println("Verifying list of Subscriptions");
    Assert.assertNotNull(origSubscriptions, "Orig Subscriptions is null");
    Assert.assertNotNull(fetchedSubscriptions, "Fetched Subscriptions is null");
    Assert.assertEquals(fetchedSubscriptions.size(), origSubscriptions.size());
    var filtersFound = new ArrayList<String>();
    for (NotificationSubscription itemFound : fetchedSubscriptions) {filtersFound.add(itemFound.getFilter());}
    for (NotificationSubscription itemSeedItem : origSubscriptions)
    {
      System.out.println("Found fetched subscription with filter: " + itemSeedItem.getFilter());
      Assert.assertTrue(filtersFound.contains(itemSeedItem.getFilter()),
              "List of notificationSubscriptions did not contain an item with filter: " + itemSeedItem.getFilter());
      System.out.println("Found fetched subscription with filter: " + itemSeedItem.getFilter());
    }
// TODO    // Create hash maps of orig and fetched with name as key
//    var origMap = new HashMap<String, AppFileInput>();
//    var fetchedMap = new HashMap<String, AppFileInput>();
//    for (NotificationSubscription s : origSubscriptions) origMap.put(s.getName(), s);
//    for (NotificationSubscription s : fetchedSubscriptions) fetchedMap.put(s.getName(), s);
//    // Go through origMap and check properties
//    for (String fiName : origMap.keySet())
//    {
//      Assert.assertTrue(fetchedMap.containsKey(fiName), "Fetched list does not contain original item: " + fiName);
//      AppFileInput fetchedFileInput = fetchedMap.get(fiName);
//      AppFileInput origFileInput = origMap.get(fiName);
//      System.out.println("Found fetched FileInput: " + fiName);
//      Assert.assertEquals(fetchedFileInput.getSourceUrl(), origFileInput.getSourceUrl());
//      Assert.assertEquals(fetchedFileInput.getTargetPath(), origFileInput.getTargetPath());
//      Assert.assertEquals(fetchedFileInput.getDescription(), origFileInput.getDescription());
//      Assert.assertEquals(fetchedFileInput.getInputMode(), origFileInput.getInputMode());
//      verifyKeyValuePairs("FileInput", fetchedFileInput.getMeta(), origFileInput.getMeta());
//    }
  }
}
