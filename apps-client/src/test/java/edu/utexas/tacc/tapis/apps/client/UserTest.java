package edu.utexas.tacc.tapis.apps.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.utexas.tacc.tapis.apps.client.gen.model.JobAttributes;
import edu.utexas.tacc.tapis.apps.client.gen.model.ParameterSet;
import edu.utexas.tacc.tapis.apps.client.gen.model.ParameterSetArchiveFilter;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqPatchApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqPutApp;
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
  int numApps = 13;
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
  public void setUp() throws Exception
  {
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
  public void tearDown()
  {
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
    String appId = apps.get(12)[1];
    usrClient = getClientUsr(serviceURL, newOwnerUserJWT);
    try { usrClient.deleteApp(appId); }
    catch (Exception e)
    {
      System.out.println("Caught exception when soft deleting app: "+ appId + " Exception: " + e);
    }
    usrClient = getClientUsr(serviceURL, ownerUserJWT);
  }

  @Test
  public void testHealthAndReady()
  {
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
  public void testCreateApp() throws Exception
  {
    // Create a app
    String[] app0 = apps.get(1);
    System.out.println("Creating app with name: " + app0[1]);
    String respUrl = Utils.createApp(usrClient, app0);
    System.out.println("Created app: " + respUrl);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
  }

  // Create an app using minimal attributes
  // Confirm that defaults are as expected
  @Test
  public void testCreateAndGetAppMinimal() throws Exception
  {
    // Create an app using only required attributes
    String[] app0 = apps.get(11);
    String appId = app0[1];
    System.out.println("Creating app with name: " + appId);
    app0[3] = null; app0[4] = null; app0[5] = null;

    String respUrl = createAppMinimal(usrClient, app0);
    System.out.println("Created app: " + respUrl);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Get the app and check the defaults
    TapisApp tmpApp = usrClient.getApp(appId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + appId);
    System.out.println("Found item: " + tmpApp.getId());
    Utils.verifyAppDefaults(tmpApp, appId);
  }

  // Create an app using mostly minimal attributes including at least one of each:
  //   jobAttrs->(fileInput, fileInputArray),
  //   jobAttrs->parameterSet->(appArg, containerArg, schedulerOption, envVariable->keyValPair, archiveFilter)
  // Confirm that defaults are as expected
  @Test
  public void testCreateAndGetAppMinimal2() throws Exception
  {
    // Create an app using only mostly minimal attributes
    String[] app0 = apps.get(9);
    String appId = app0[1];
    System.out.println("Creating app with name: " + appId);
    app0[3] = null; app0[4] = null; app0[5] = null;

    String respUrl = createAppMinimal2(usrClient, app0);
    System.out.println("Created app: " + respUrl);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Get the app and check the defaults
    TapisApp tmpApp = usrClient.getApp(appId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + appId);
    System.out.println("Found item: " + tmpApp.getId());
    Utils.verifyAppDefaults2(tmpApp, appId);
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
    String respUrl = Utils.createAppMinimal(usrClient, app0);
    System.out.println("Created app: " + respUrl);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Get the app and check the defaults
    TapisApp tmpApp = usrClient.getApp(appId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + appId);
    System.out.println("Found item: " + tmpApp.getId());
    Utils.verifyAppDefaults(tmpApp, appId);

    // Modify result and create a new app
    String newId = appId + "new";
    tmpApp.setId(newId);
    respUrl = Utils.createAppFromTapisApp(usrClient, tmpApp);
    System.out.println("Created system: " + respUrl);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Get the app and check the defaults
    tmpApp = usrClient.getApp(newId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + newId);
    System.out.println("Found item: " + tmpApp.getId());
    Utils.verifyAppDefaults(tmpApp, newId);

    // For the new app do not modify result and use PUT to update. Nothing should change.
    tmpApp.setId(newId);
    ReqPutApp reqPutApp = AppsClient.buildReqPutApp(tmpApp);
    respUrl = usrClient.putApp(newId, tmpApp.getVersion(),  reqPutApp);
    System.out.println("Updated application using PUT. Application: " + respUrl);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    // Get the new app and check the defaults. Nothing should have changed.
    tmpApp = usrClient.getApp(newId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + newId);
    System.out.println("Found item: " + tmpApp.getId());
    Utils.verifyAppDefaults(tmpApp, newId);

    // For the new app modify result and use PUT to update. Verify updated attribute
    tmpApp.setId(newId);
    String newContainerImage = defaultContainerImage + "New";
    tmpApp.setContainerImage(newContainerImage);
    reqPutApp = AppsClient.buildReqPutApp(tmpApp);
    respUrl = usrClient.putApp(newId, tmpApp.getVersion(),  reqPutApp);
    System.out.println("Updated application using PUT. App: " + respUrl);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    // Get the new app and check the updated attribute.
    tmpApp = usrClient.getApp(newId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + newId);
    System.out.println("Found item: " + tmpApp.getId());
    Assert.assertEquals(tmpApp.getContainerImage(), newContainerImage, "Failed to update containerImage using PUT");
  }

  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = "^APPAPI_APP_EXISTS.*")
  public void testCreateAppAlreadyExists() throws Exception
  {
    // Create a app
    String[] app0 = apps.get(7);
    System.out.println("Creating app with name: " + app0[1]);
    String respUrl = Utils.createApp(usrClient, app0);
      System.out.println("Created app: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    // Now attempt to create it again, should throw exception
    System.out.println("Creating app with name: " + app0[1]);
    Utils.createApp(usrClient, app0);
    Assert.fail("Exception should have been thrown");
  }

  // Test retrieving a app by name.
  //  String[] app0 = {tenantName, appId, appVersion, "description " + suffix, appType, ownerUser1};
  @Test
  public void testGetApp() throws Exception
  {
    String[] app0 = apps.get(2);
    String respUrl = Utils.createApp(usrClient, app0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    TapisApp tmpApp = usrClient.getApp(app0[1], app0[2]);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + app0[1]);
    System.out.println("Found item: " + tmpApp.getId());
    verifyAppAttributes(tmpApp, app0, isEnabledTrue, runtimeOptions1, maxJobs1, maxJobsPerUser1, strictFileInputsFalse,
            dynamicExecSystemTrue, execSystemConstraints1, archiveOnAppErrorTrue, appArgs1, containerArgs1,
            schedulerOptions1, envVariables1, archiveFilter1, nodeCount1, coresPerNode1, memoryMb1, maxMinutes1,
            fileInputs1, fileInputArrays1, jobTags1, notifList1, tags1, notes1JO);
  }

  // Test patching most updatable attributes
  @Test
  public void testPatchApp() throws Exception
  {
    String[] app0 = apps.get(8);
    String appId = app0[1];
    String appVersion = app0[2];
    // Create an app
    String respUrl = Utils.createApp(usrClient, app0);
    System.out.println("Created app: " + respUrl);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    TapisApp tmpApp = usrClient.getApp(appId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + appId);
    System.out.println("Found item: " + tmpApp.getId());
    verifyAppAttributes(tmpApp, app0, isEnabledTrue, runtimeOptions1, maxJobs1, maxJobsPerUser1, strictFileInputsFalse,
            dynamicExecSystemTrue, execSystemConstraints1, archiveOnAppErrorTrue, appArgs1, containerArgs1,
            schedulerOptions1, envVariables1, archiveFilter1, nodeCount1, coresPerNode1, memoryMb1, maxMinutes1,
            fileInputs1, fileInputArrays1, jobTags1, notifList1, tags1, notes1JO);

    // Create a patch app request that updates: description, containerImage, tags, notes.
    ReqPatchApp rApp = createPatchApp();
    System.out.println("Creating and updating app with name: " + appId);
    // Patch the app
    respUrl = usrClient.patchApp(appId, appVersion, rApp);
    System.out.println("Patched app: " + respUrl);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Retrieve the patched app
    tmpApp = usrClient.getApp(appId);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + appId);
    System.out.println("Found item: " + tmpApp.getId());
//    app0 = {0=tenantName, 1=appId, 2=appVersion, 3=description, 4=appType, 5=ownerUser1,
//              6=runtime, 7=runtimeVersion, 8=containerImage, 9=jobDescription,
//              10=execSystemId, 11=execSystemExecDir, 12=execSystemInputDir, 13=execSystemOutputDir,
//              14=execSystemLogicalQueue, 15=archiveSystemId, 16=archiveSystemDir};
    // Update values in String[] app0 so we can use verifyAppAttributes utitlity.
    app0[3] = appDescription2;
    app0[6] = runtime2.name();
    app0[7] = runtimeVersion2;
    app0[8] = containerImage2;
    app0[9] = jobDescription2;
    app0[10] = execSystemId2;
    app0[11] = execSystemExecDir2;
    app0[12] = execSystemInputDir2;
    app0[13] = execSystemOutputDir2;
    app0[14] = execSystemLogicalQueue2;
    app0[15] = archiveSystemId2;
    app0[16] = archiveSystemDir2;

    // Verify patched attributes
    verifyAppAttributes(tmpApp, app0, isEnabledTrue, runtimeOptions2, maxJobs2, maxJobsPerUser2, strictFileInputsTrue,
            dynamicExecSystemFalse, execSystemConstraints2, archiveOnAppErrorFalse, appArgs2, containerArgs2,
            schedulerOptions2, envVariables2, archiveFilter2, nodeCount2, coresPerNode2, memoryMb2, maxMinutes2,
            fileInputs2, fileInputArrays2, jobTags2, notifList2, tags2, notes2JO);
  }

  @Test
  public void testChangeOwner() throws Exception
  {
    // Create the app
    String[] app0 = apps.get(12);
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
  public void testGetApps() throws Exception
  {
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
  public void testEnableDisable() throws Exception
  {
    String[] app0 = apps.get(13);
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
  public void testDelete() throws Exception
  {
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
  public void testUserPerms() throws Exception
  {
    String[] app0 = apps.get(10);
    String appId = app0[1];
    // Create a app
    System.out.println("Creating app with name: " + appId);
    String respUrl = Utils.createApp(usrClient, app0);
    System.out.println("Created app: " + respUrl);
    System.out.println("Testing perms for user: " + newPermsUser);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    // Create user perms for the app
    usrClient.grantUserPermissions(appId, newPermsUser, testPerms);
    // Get the app perms for the user and make sure permissions are there
    List<String> userPerms = usrClient.getAppPermissions(appId, newPermsUser);
    Assert.assertNotNull(userPerms, "Null returned when retrieving perms.");
    for (String perm : userPerms)
    {
      System.out.println("After grant found user perm: " + perm);
    }
    Assert.assertEquals(userPerms.size(), testPerms.size(), "Incorrect number of perms returned.");
    for (String perm : testPerms)
    {
      if (!userPerms.contains(perm)) Assert.fail("User perms should contain permission: " + perm);
    }
    // Remove perms for the user
    usrClient.revokeUserPermissions(appId, newPermsUser, testPerms);
    // Get the app perms for the user and make sure permissions are gone.
    userPerms = usrClient.getAppPermissions(appId, newPermsUser);
    Assert.assertNotNull(userPerms, "Null returned when retrieving perms.");
    for (String perm : userPerms)
    {
      System.out.println("After revoke found user perm: " + perm);
    }
    for (String perm : testPerms)
    {
      if (userPerms.contains(perm)) Assert.fail("User perms should not contain permission: " + perm);
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

  private static ReqPatchApp createPatchApp()
  {
    ReqPatchApp pApp = new ReqPatchApp();
    JobAttributes jobAttributes = new JobAttributes();
    ParameterSet parameterSet = new ParameterSet();

    pApp.description(appDescription2);
    pApp.runtime(runtime2);
    pApp.runtimeVersion(runtimeVersion2);
    pApp.runtimeOptions(runtimeOptions2);
    pApp.containerImage(containerImage2);
    pApp.maxJobs(maxJobs2);
    pApp.maxJobsPerUser(maxJobsPerUser2);
    pApp.strictFileInputs(strictFileInputsTrue);
    jobAttributes.description(jobDescription2);
    jobAttributes.dynamicExecSystem(dynamicExecSystemFalse);
    jobAttributes.execSystemConstraints(execSystemConstraints2);
    jobAttributes.execSystemId(execSystemId2);
    jobAttributes.execSystemExecDir(execSystemExecDir2);
    jobAttributes.execSystemInputDir(execSystemInputDir2);
    jobAttributes.execSystemOutputDir(execSystemOutputDir2);
    jobAttributes.execSystemLogicalQueue(execSystemLogicalQueue2);
    jobAttributes.archiveSystemId(archiveSystemId2);
    jobAttributes.archiveSystemDir(archiveSystemDir2);
    jobAttributes.archiveOnAppError(archiveOnAppErrorFalse);
    parameterSet.appArgs(appArgs2);
    parameterSet.containerArgs(containerArgs2);
    parameterSet.schedulerOptions(schedulerOptions2);
    parameterSet.envVariables(envVariables2);
    ParameterSetArchiveFilter archiveFilter = new ParameterSetArchiveFilter();
    archiveFilter.setIncludes(archiveIncludes2);
    archiveFilter.setExcludes(archiveExcludes2);
    archiveFilter.includeLaunchFiles(includeLaunchFilesFalse);
    parameterSet.setArchiveFilter(archiveFilter);
    jobAttributes.parameterSet(parameterSet);
    jobAttributes.fileInputs(fileInputs2);
    jobAttributes.fileInputArrays(fileInputArrays2);
    jobAttributes.nodeCount(nodeCount2);
    jobAttributes.coresPerNode(coresPerNode2);
    jobAttributes.memoryMB(memoryMb2);
    jobAttributes.maxMinutes(maxMinutes2);
    jobAttributes.tags(jobTags2);
    jobAttributes.subscriptions(notifList2);

    pApp.jobAttributes(jobAttributes);

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
