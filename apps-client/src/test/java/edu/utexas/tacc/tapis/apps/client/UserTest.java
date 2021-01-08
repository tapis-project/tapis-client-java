package edu.utexas.tacc.tapis.apps.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqUpdateApp;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.apps.client.gen.model.App;
import edu.utexas.tacc.tapis.auth.client.AuthClient;

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
  int numApps = 16;
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
    var authClient = new AuthClient(baseURL);
//    var tokClient = new TokensClient(baseURL, filesSvcName, filesSvcPasswd);
    try {
      ownerUserJWT = authClient.getToken(ownerUser1, ownerUser1);
      newOwnerUserJWT = authClient.getToken(newOwnerUser, newOwnerUser);
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
      try
      {
        usrClient.deleteApp(appId);
      }
      catch (Exception e)
      {
        System.out.println("Caught exception when soft deleting app: "+ appId + " Exception: " + e);
      }
    }
//    // One app may have had owner changed so use new owner.
//    String appId = apps.get(9)[1];
//    usrClient = getClientUsr(serviceURL, newOwnerUserJWT);
//    try
//    {
//      usrClient.deleteApp(appId);
//    }
//    catch (Exception e)
//    {
//      System.out.println("Caught exception when soft deleting app: "+ appId + " Exception: " + e);
//    }
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

  // Create a app using minimal attributes:
  //   name, appType
  @Test
  public void testCreateAppMinimal()
  {
    // Create a app
    String[] app0 = apps.get(14);
    System.out.println("Creating app with name: " + app0[1]);
    // Set optional attributes to null
//    String[] app0 = {tenantName, appId, appVersion, "description " + suffix, appType, ownerUser1};
    app0[3] = null; app0[4] = null; app0[5] = null;

    try {
      String respUrl = createApp(usrClient, app0);
      System.out.println("Created app: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
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

    App tmpApp = usrClient.getApp(app0[1], app0[2]);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + app0[1]);
    System.out.println("Found item: " + app0[1]);
    verifyAppAttributes(tmpApp, app0);
  }

  @Test
  public void testUpdateApp() {
    String[] app0 = apps.get(15);
//    private static final String[] sysF2 = {tenantName, "CsysF", "description PATCHED", sysType, ownerUser, "hostPATCHED", "effUserPATCHED",
//            "fakePasswordF", "bucketF", "/rootF", "jobLocalWorkDirF", "jobLocalArchDirF", "jobRemoteArchAppF", "jobRemoteArchDirF"};
    String[] appF2 = app0.clone();
    appF2[2] = "description PATCHED"; appF2[5] = "hostPATCHED"; appF2[6] = "effUserPATCHED";
    ReqUpdateApp rApp = createPatchApp(appF2);
    System.out.println("Creating and updating app with name: " + app0[1]);
    try {
      // Create a app
      String respUrl = Utils.createApp(usrClient, app0);
      System.out.println("Created app: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Update the app
      respUrl = usrClient.updateApp(app0[1], app0[2], rApp);
      System.out.println("Updated app: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Verify attributes
      app0 = appF2;
      App tmpApp = usrClient.getApp(app0[1], app0[2]);
      Assert.assertNotNull(tmpApp, "Failed to create item: " + app0[1]);
      System.out.println("Found item: " + app0[1]);
      Assert.assertEquals(tmpApp.getId(), app0[1]);
      Assert.assertEquals(tmpApp.getDescription(), app0[2]);
//      Assert.assertEquals(tmpApp.getAppType().name(), app0[3]);
      Assert.assertEquals(tmpApp.getOwner(), app0[4]);
      // Verify capabilities
//      List<Capability> jobCaps = tmpApp.getJobCapabilities();
//      Assert.assertNotNull(jobCaps);
//      Assert.assertEquals(jobCaps.size(), jobCaps2.size());
//      var capNamesFound = new ArrayList<String>();
//      for (Capability capFound : jobCaps) {capNamesFound.add(capFound.getName());}
//      for (Capability capSeed : jobCaps2)
//      {
//        Assert.assertTrue(capNamesFound.contains(capSeed.getName()), "List of capabilities did not contain a capability named: " + capSeed.getName());
//      }
      // Verify tags
      List<String> tmpTags = tmpApp.getTags();
      Assert.assertNotNull(tmpTags, "Tags value was null");
      Assert.assertEquals(tmpTags.size(), tags2.size(), "Wrong number of tags");
      for (String tagStr : tags2)
      {
        Assert.assertTrue(tmpTags.contains(tagStr));
        System.out.println("Found tag: " + tagStr);
      }
      // Verify notes
      String tmpNotesStr = (String) tmpApp.getNotes();
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
    // Create the app
    String[] app0 = apps.get(16);
    String respUrl = Utils.createApp(usrClient, app0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    App tmpApp = usrClient.getApp(app0[1], app0[2]);
    Assert.assertNotNull(tmpApp, "Failed to create item: " + app0[1]);
    usrClient.changeAppOwner(app0[1], newOwnerUser);
    // Now that owner has given away ownership we need to be newOwnerUser or admin to get the app
    tmpApp = Utils.getClientUsr(serviceURL, newOwnerUserJWT).getApp(app0[1], app0[2]);
    Assert.assertNotNull(tmpApp, "Unable to get app after change of owner. App: " + app0[1]);
    System.out.println("Found item: " + app0[1]);
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
    List<App> appsList = usrClient.getApps(null);
    Assert.assertNotNull(appsList);
    Assert.assertFalse(appsList.isEmpty());
    var appNames = new ArrayList<String>();
    for (App app : appsList) {
      System.out.println("Found item: " + app.getId());
      appNames.add(app.getId());
    }
    Assert.assertTrue(appNames.contains(apps.get(3)[1]), "List of apps did not contain app name: " + apps.get(3)[1]);
    Assert.assertTrue(appNames.contains(apps.get(4)[1]), "List of apps did not contain app name: " + apps.get(4)[1]);
  }

  @Test
  public void testDelete() throws Exception {
    // Create the app
    String[] app0 = apps.get(6);
    String respUrl = Utils.createApp(usrClient, app0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Delete the app
    usrClient.deleteApp(app0[1]);
    try {
      App tmpApp2 = usrClient.getApp(app0[1], app0[2]);
      Assert.fail("App not deleted. App name: " + app0[1]);
    } catch (TapisClientException e) {
      Assert.assertEquals(e.getCode(), 404);
    }
  }

  // Test creating, reading and deleting user permissions for a app
  @Test
  public void testUserPerms() {
    String[] app0 = apps.get(10);
    // Create a app
    System.out.println("Creating app with name: " + app0[1]);
    try {
      String respUrl = Utils.createApp(usrClient, app0);
      System.out.println("Created app: " + respUrl);
      System.out.println("Testing perms for user: " + newPermsUser);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
      // Create user perms for the app
      usrClient.grantUserPermissions(app0[1], newPermsUser, testPerms);
      // Get the app perms for the user and make sure permissions are there
      List<String> userPerms = usrClient.getAppPermissions(app0[1], newPermsUser);
      Assert.assertNotNull(userPerms, "Null returned when retrieving perms.");
      for (String perm : userPerms) {
        System.out.println("After grant found user perm: " + perm);
      }
      Assert.assertEquals(userPerms.size(), testPerms.size(), "Incorrect number of perms returned.");
      for (String perm : testPerms) {
        if (!userPerms.contains(perm)) Assert.fail("User perms should contain permission: " + perm);
      }
      // Remove perms for the user
      usrClient.revokeUserPermissions(app0[1], newPermsUser, testPerms);
      // Get the app perms for the user and make sure permissions are gone.
      userPerms = usrClient.getAppPermissions(app0[1], newPermsUser);
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

  private static ReqUpdateApp createPatchApp(String[] app)
  {
    ReqUpdateApp pApp = new ReqUpdateApp();
    pApp.description(app[2]);
    pApp.enabled(false);
//    pApp.jobCapabilities(jobCaps2);
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

