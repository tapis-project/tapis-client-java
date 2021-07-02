package edu.utexas.tacc.tapis.apps.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import edu.utexas.tacc.tapis.apps.client.gen.model.ArgSpec;
import edu.utexas.tacc.tapis.apps.client.gen.model.FileInputDefinition;
import edu.utexas.tacc.tapis.apps.client.gen.model.JobAttributes;
import edu.utexas.tacc.tapis.apps.client.gen.model.KeyValuePair;
import edu.utexas.tacc.tapis.apps.client.gen.model.NotificationSubscription;
import edu.utexas.tacc.tapis.apps.client.gen.model.ParameterSet;
import edu.utexas.tacc.tapis.apps.client.gen.model.ParameterSetArchiveFilter;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqPatchApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqPutApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.RuntimeOptionEnum;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.apps.client.gen.model.TapisApp;

import static edu.utexas.tacc.tapis.apps.client.Utils.*;

/**
 * Test the Apps API client acting as a single specific user calling the apps service.
 *
 * See Utils in this package for information on environment required to run the tests.
 * 
 */
@Test(groups={"integration"})
public class UserTest
{
  // Test data
  int numApps = 17;
  Map<Integer, String[]> apps = Utils.makeApps(numApps, "CltUsr");
  
  private static final String newOwnerUser = testUser3;
  private static final String newPermsUser = testUser4;

  // Create a single static client. Must do it this way because headers are static and JWT is in the header.
  // Updating client dynamically would give false sense of security since tests might be run in parallel and there
  //   would be concurrency issues.
  private static AppsClient usrClient;
  private static String serviceURL;
  private static String ownerUserJWT;
  private static String newOwnerUserJWT;

  @BeforeSuite
  public void setUp() throws Exception {
    // Get the base URLs from the environment so the test can be used in environments other than dev
    System.out.println("****** Executing BeforeSuite setup method for class: " + this.getClass().getSimpleName());
    // Get files service password from env
//    String filesSvcPasswd = Utils.getFilesSvcPassword();
    // Set service port for apps service. Check for port set as env var
    // NOTE: This is ignored if TAPIS_ENV_SVC_URL_APPS is set
    String servicePort = Utils.getServicePort();
    // Set base URL for apps service. Check for URL set as env var
    serviceURL = Utils.getServiceURL(servicePort);
    // Get base URL suffix from env or from default
    String baseURL = Utils.getBaseURL();
    // Log URLs being used
    System.out.println("Using Apps URL: " + serviceURL);
    System.out.println("Using Authenticator URL: " + baseURL);
    System.out.println("Using Tokens URL: " + baseURL);
    // Get short term user JWT from tokens service
//    var authClient = new AuthClient(baseURL);
//    var tokClient = new TokensClient(baseURL, filesSvcName, filesSvcPasswd);
    try {
//      ownerUserJWT = authClient.getToken(ownerUser1, ownerUser1);
//      newOwnerUserJWT = authClient.getToken(newOwnerUser, newOwnerUser);
      // Sometimes auth or tokens service is down. Use long term tokens instead.
      // Long term JWT for testuser1 - expires approx 1 July 2026
      ownerUserJWT = testUser1JWT;
      // Long term JWT for testuser3 - expires approx 1 July 2026
      newOwnerUserJWT = testUser3JWT;
//      filesServiceJWT = tokClient.getSvcToken(adminTenantName, filesSvcName, DEFAULT_TARGET_SITE);
    } catch (Exception e) {
      throw new Exception("Exception while creating tokens or auth service", e);
    }
    // Basic check of JWTs
    if (StringUtils.isBlank(ownerUserJWT)) throw new Exception("Authn service returned invalid owner user JWT");
    if (StringUtils.isBlank(newOwnerUserJWT)) throw new Exception("Authn service returned invalid new owner user JWT");
//    if (StringUtils.isBlank(filesServiceJWT)) throw new Exception("Tokens service returned invalid files svc JWT");
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
    for (int i = 1; i <= numApps; i++)
    {
      String appId = apps.get(i)[1];
      try { usrClient.deleteApp(appId); }
      catch (Exception e)
      {
        System.out.println("Caught exception when soft deleting app: "+ appId + " Exception: " + e);
      }
    }
    // One app may have had owner changed so use new owner.
    String appId = apps.get(16)[1];
    usrClient = getClientUsr(serviceURL, newOwnerUserJWT);
    try { usrClient.deleteApp(appId); }
    catch (Exception e)
    {
      System.out.println("Caught exception when soft deleting app: "+ appId + " Exception: " + e);
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
  public void testCreateApp() {
    // Create a app
    String[] app0 = apps.get(1);
    System.out.println("Creating app with name: " + app0[1]);
    try {
      String respUrl = Utils.createApp(usrClient, app0);
      System.out.println("Created app: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }

  // Create an app using minimal attributes
  // Confirm that defaults are as expected
  @Test
  public void testCreateAndGetAppMinimal() throws Exception
  {
    // Create an app using only required attributes
    String[] app0 = apps.get(14);
    String appId = app0[1];
    System.out.println("Creating app with name: " + appId);
    app0[3] = null; app0[4] = null; app0[5] = null;

    try {
      String respUrl = createAppMinimal(usrClient, app0);
      System.out.println("Created app: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }

    // Get the app and check the defaults
    TapisApp tmpApp = usrClient.getApp(appId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + appId);
    System.out.println("Found item: " + tmpApp.getId());
    Utils.verifyAppDefaults(tmpApp, appId);
  }

  // Create an app using minimal attributes, get the app, use modified result to
  //  create a new app and update the original app using PUT.
  // Confirm that defaults are as expected
  @Test
  public void testMinimalCreateGetPutAndCreate() throws Exception
  {
    // Create a BATCH app using minimal attributes
    String[] app0 = apps.get(5);
    String appId = app0[1];
    System.out.println("Creating app with name: " + appId);
    try {
      String respUrl = Utils.createAppMinimal(usrClient, app0);
      System.out.println("Created app: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }

    // Get the app and check the defaults
    TapisApp tmpApp = usrClient.getApp(appId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + appId);
    System.out.println("Found item: " + tmpApp.getId());
    Utils.verifyAppDefaults(tmpApp, appId);

    // Modify result and create a new app
    String newId = appId + "new";
    tmpApp.setId(newId);
    try {
      String respUrl = Utils.createAppFromTapisApp(usrClient, tmpApp);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
    // Get the app and check the defaults
    tmpApp = usrClient.getApp(newId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + newId);
    System.out.println("Found item: " + tmpApp.getId());
    Utils.verifyAppDefaults(tmpApp, newId);

    // For the new app do not modify result and use PUT to update. Nothing should change.
    tmpApp.setId(newId);
    try {
      ReqPutApp reqPutApp = AppsClient.buildReqPutApp(tmpApp);
      String respUrl = usrClient.putApp(newId, tmpApp.getVersion(),  reqPutApp);
      System.out.println("Updated application using PUT. Application: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
    // Get the new app and check the defaults. Nothing should have changed.
    tmpApp = usrClient.getApp(newId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + newId);
    System.out.println("Found item: " + tmpApp.getId());
    Utils.verifyAppDefaults(tmpApp, newId);

    // For the new app modify result and use PUT to update. Verify updated attribute
    tmpApp.setId(newId);
    String newContainerImage = defaultContainerImage + "New";
    tmpApp.setContainerImage(newContainerImage);
    try {
      ReqPutApp reqPutApp = AppsClient.buildReqPutApp(tmpApp);
      String respUrl = usrClient.putApp(newId, tmpApp.getVersion(),  reqPutApp);
      System.out.println("Updated application using PUT. App: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
    // Get the new app and check the updated attribute.
    tmpApp = usrClient.getApp(newId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + newId);
    System.out.println("Found item: " + tmpApp.getId());
    Assert.assertEquals(tmpApp.getContainerImage(), newContainerImage, "Failed to update containerImage using PUT");
  }

  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = "^APPAPI_APP_EXISTS.*")
  public void testCreateAppAlreadyExists() throws Exception {
    // Create a app
    String[] app0 = apps.get(7);
    System.out.println("Creating app with name: " + app0[1]);
    try {
      String respUrl = Utils.createApp(usrClient, app0);
      System.out.println("Created app: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
    // Now attempt to create it again, should throw exception
    System.out.println("Creating app with name: " + app0[1]);
    Utils.createApp(usrClient, app0);
    Assert.fail("Exception should have been thrown");
  }

  // Test retrieving a app by name.
  //  String[] app0 = {tenantName, appId, appVersion, "description " + suffix, appType, ownerUser1};
  @Test
  public void testGetApp() throws Exception {
    String[] app0 = apps.get(2);
    String respUrl = Utils.createApp(usrClient, app0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    TapisApp tmpApp = usrClient.getApp(app0[1], app0[2]);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + app0[1]);
    System.out.println("Found item: " + tmpApp.getId());
    verifyAppAttributes(tmpApp, app0);
  }

  @Test
  public void testPatchApp() {
    String[] app0 = apps.get(8);
    String appId = app0[1];
    String appVersion = app0[2];
//    String[] app0 = {tenantName, appId, appVersion, "description "+suffix, appTypeBatch.name(), ownerUser1,
//            runtime.name(), runtimeVersion+suffix, containerImage+suffix, jobDescription+suffix,
//            execSystemId, execSystemExecDir+suffix, execSystemInputDir+suffix, execSystemOutputDir+suffix,
//            execSystemLogicalQueue+suffix, archiveSystemId, archiveSystemDir+suffix};
    String newDescription = "description PATCHED";
    String newContainerImage = "containerImagePATCHED";
    String[] appF2 = app0.clone();
    appF2[3] = newDescription;
    appF2[8] = newContainerImage;
    // Create a patch app request that updates: description, containerImage, tags, notes.
    ReqPatchApp rApp = createPatchApp(newDescription, newContainerImage);
    System.out.println("Creating and updating app with name: " + appId);
    try {
      // Create an app
      String respUrl = Utils.createApp(usrClient, app0);
      System.out.println("Created app: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Update the app
      respUrl = usrClient.patchApp(appId, appVersion, rApp);
      System.out.println("Patched app: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Verify patched attributes
      app0 = appF2;
      TapisApp tmpApp = usrClient.getApp(appId);
      Assert.assertNotNull(tmpApp, "Failed to create item: " + appId);
      System.out.println("Found item: " + tmpApp.getId());
      Assert.assertEquals(tmpApp.getId(), appId);
      Assert.assertEquals(tmpApp.getVersion(), appVersion);
      Assert.assertEquals(tmpApp.getDescription(), newDescription);
      Assert.assertEquals(tmpApp.getContainerImage(), newContainerImage);
      // Verify tags
      List<String> tmpTags = tmpApp.getTags();
      Assert.assertNotNull(tmpTags, "Tags value was null");
      Assert.assertEquals(tmpTags.size(), tags2.size(), "Wrong number of tags");
      for (String tagStr : tags2)
      {
        Assert.assertTrue(tmpTags.contains(tagStr), "List of tags did not contain a tag named: " + tagStr);
        System.out.println("Found tag: " + tagStr);
      }
      // Verify notes
      String tmpNotesStr = (String) tmpApp.getNotes();
      System.out.println("Found notes: " + tmpNotesStr);
      JsonObject tmpNotes = ClientTapisGsonUtils.getGson().fromJson(tmpNotesStr, JsonObject.class);
      Assert.assertNotNull(tmpNotes);
      System.out.println("Found notes: " + tmpNotesStr);
      Assert.assertTrue(tmpNotes.has("project"), "Notes json did not contain project");
      Assert.assertEquals(tmpNotes.get("project").getAsString(), notes2JO.get("project").getAsString());
      Assert.assertTrue(tmpNotes.has("testdata"), "Notes json did not contain testdata");
      Assert.assertEquals(tmpNotes.get("testdata").getAsString(), notes2JO.get("testdata").getAsString());

      // Verify unpatched attributes
      // TODO currently this is mostly copy/paste from Utils.verifyAppAttributes()
      // TODO can we use Utils.verifyAppAttrs? or maybe refactor to have a verifyCommonAttrs?
      Assert.assertEquals(tmpApp.getTenant(), tenantName);
      Assert.assertEquals(tmpApp.getId(), appId);
      Assert.assertEquals(tmpApp.getVersion(), appVersion);
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
      Assert.assertEquals(tmpApp.getMaxJobs(), Integer.valueOf(maxJobsMAX));
      Assert.assertEquals(tmpApp.getMaxJobsPerUser(), Integer.valueOf(maxJobsPerUserMAX));
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
      Assert.assertNotNull(tmpApp.getCreated(), "Fetched created timestamp should not be null.");
      Assert.assertNotNull(tmpApp.getUpdated(), "Fetched updated timestamp should not be null.");
    }
    catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }

  @Test
  public void testChangeOwner() throws Exception {
    // Create the app
    String[] app0 = apps.get(16);
    String appId = app0[1];
    String respUrl = Utils.createApp(usrClient, app0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    TapisApp tmpApp = usrClient.getApp(appId, app0[2]);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + appId);
    usrClient.changeAppOwner(appId, newOwnerUser);
    // Now that owner has given away ownership we need to be newOwnerUser or admin to get the app
    tmpApp = Utils.getClientUsr(serviceURL, newOwnerUserJWT).getApp(appId, app0[2]);
    Assert.assertNotNull(tmpApp, "Unable to get app after change of owner. App: " + appId);
    System.out.println("Found item: " + tmpApp.getId());
    Assert.assertEquals(tmpApp.getOwner(), newOwnerUser);
  }

  @Test
  public void testGetApps() throws Exception {
    // Create 2 apps
    String[] app0 = apps.get(3);
    String respUrl = Utils.createApp(usrClient, app0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    app0 = apps.get(4);
    respUrl = Utils.createApp(usrClient, app0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Get list of all app names
    List<TapisApp> appsList = usrClient.getApps(null, null);
    Assert.assertNotNull(appsList);
    Assert.assertFalse(appsList.isEmpty());
    var appNames = new ArrayList<String>();
    for (TapisApp app : appsList) {
      System.out.println("Found item: " + app.getId());
      appNames.add(app.getId());
    }
    Assert.assertTrue(appNames.contains(apps.get(3)[1]), "List of apps did not contain app name: " + apps.get(3)[1]);
    Assert.assertTrue(appNames.contains(apps.get(4)[1]), "List of apps did not contain app name: " + apps.get(4)[1]);
  }

  @Test
  public void testEnableDisable() throws Exception {
    String[] app0 = apps.get(17);
    String appId = app0[1];
    String appVer = app0[2];
    String respUrl = Utils.createApp(usrClient, app0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    // Enabled should start off true, then become false and finally true again.
    TapisApp tmpApp = usrClient.getApp(appId, appVer);
    Assert.assertNotNull(tmpApp.getEnabled());
    Assert.assertTrue(tmpApp.getEnabled());
    Assert.assertTrue(usrClient.isEnabled(appId));

    int changeCount = usrClient.disableApp(appId);
    Assert.assertEquals(changeCount, 1, "ChangeCount should be 1");
    tmpApp = usrClient.getApp(appId, appVer);
    Assert.assertNotNull(tmpApp.getEnabled());
    Assert.assertFalse(tmpApp.getEnabled());
    Assert.assertFalse(usrClient.isEnabled(appId));

    changeCount = usrClient.enableApp(appId);
    Assert.assertEquals(changeCount, 1, "ChangeCount should be 1");
    tmpApp = usrClient.getApp(appId, appVer);
    Assert.assertNotNull(tmpApp.getEnabled());
    Assert.assertTrue(tmpApp.getEnabled());
    Assert.assertTrue(usrClient.isEnabled(appId));
  }

  @Test
  public void testDelete() throws Exception {
    // Create the app
    String[] app0 = apps.get(6);
    String appId = app0[1];
    String respUrl = Utils.createApp(usrClient, app0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Delete the app
    usrClient.deleteApp(appId);
    try {
      usrClient.getApp(appId, app0[2]);
      Assert.fail("App not deleted. App name: " + appId);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
    }
  }

  // Test creating, reading and deleting user permissions for a app
  @Test
  public void testUserPerms() {
    String[] app0 = apps.get(10);
    String appId = app0[1];
    // Create a app
    System.out.println("Creating app with name: " + appId);
    try {
      String respUrl = Utils.createApp(usrClient, app0);
      System.out.println("Created app: " + respUrl);
      System.out.println("Testing perms for user: " + newPermsUser);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Create user perms for the app
      usrClient.grantUserPermissions(appId, newPermsUser, testPerms);
      // Get the app perms for the user and make sure permissions are there
      List<String> userPerms = usrClient.getAppPermissions(appId, newPermsUser);
      Assert.assertNotNull(userPerms, "Null returned when retrieving perms.");
      for (String perm : userPerms) {
        System.out.println("After grant found user perm: " + perm);
      }
      Assert.assertEquals(userPerms.size(), testPerms.size(), "Incorrect number of perms returned.");
      for (String perm : testPerms) {
        if (!userPerms.contains(perm)) Assert.fail("User perms should contain permission: " + perm);
      }
      // Remove perms for the user
      usrClient.revokeUserPermissions(appId, newPermsUser, testPerms);
      // Get the app perms for the user and make sure permissions are gone.
      userPerms = usrClient.getAppPermissions(appId, newPermsUser);
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

  // Test various cases when app is missing
  //  - get app, isEnabled, enable/disable, delete/undelete, changeOwner
  //  - get perms, grant perms, revoke perms
  @Test
  public void testMissingApp()
  {
    String fakeAppName = "AMissingAppName";
    String fakeUserName = "AMissingUserName";

    boolean pass = false;

    // Get App
    try {
      usrClient.getApp(fakeAppName);
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // isEnabled
    pass = false;
    try {
      usrClient.isEnabled(fakeAppName);
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Enable App
    pass = false;
    try {
      usrClient.enableApp(fakeAppName);
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Disable App
    pass = false;
    try {
      usrClient.disableApp(fakeAppName);
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Delete App
    pass = false;
    try {
      usrClient.deleteApp(fakeAppName);
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Undelete App
    pass = false;
    try {
      usrClient.undeleteApp(fakeAppName);
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Change Owner
    pass = false;
    try {
      usrClient.changeAppOwner(fakeAppName, newOwnerUser);
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Get Perms
    pass = false;
    try {
      usrClient.getAppPermissions(fakeAppName, testUser1);
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Grant Perms
    pass = false;
    try {
      usrClient.grantUserPermissions(fakeAppName, fakeUserName, testPerms);
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Revoke Perms
    pass = false;
    try {
      usrClient.revokeUserPermissions(fakeAppName, fakeUserName, testPerms);
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);

    // Revoke Perm
    pass = false;
    try {
      usrClient.revokeUserPermission(fakeAppName, fakeUserName, testREADPerm.get(0));
      Assert.fail("Missing App did not throw exception. App name: " + fakeAppName);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
      pass = true;
    }
    Assert.assertTrue(pass);
  }


  // =====================================================================
  // =========  Private methods ==========================================
  // =====================================================================

  private static ReqPatchApp createPatchApp(String newDescription, String newContainerImage)
  {
    ReqPatchApp pApp = new ReqPatchApp();
    pApp.description(newDescription);
    pApp.containerImage(newContainerImage);
    pApp.tags(tags2);
    pApp.notes(notes2JO);
    return pApp;
  }

//  private AppsClient getClientFilesSvc()
//  {
//    // Create the client each time due to issue with setting different headers needed by svc vs usr client
//    AppsClient clt = new AppsClient(serviceURL, filesServiceJWT);
//    clt.addDefaultHeader("X-Tapis-User", ownerUser1);
//    clt.addDefaultHeader("X-Tapis-Tenant", tenantName);
//    return clt;
//  }
}

