package edu.utexas.tacc.tapis.files.client;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.auth.client.AuthClient;
import edu.utexas.tacc.tapis.files.client.gen.model.FileInfo;
import static edu.utexas.tacc.tapis.files.client.Utils.*;

/**
 * Test the API client acting as a single specific user calling the service.
 *
 * NOTE: Because client code stores headers statically cannot mix a user client and
 *       a service client in one program or even 2 user clients. This is because user JWTs cannot have
 *       X-Tapis-User, X-Tapis-Tenant headers set and service JWTs must have those headers set
 *       and there does not appear to be an easy way to unset headers.
 *       So instead have user client tests in one program and service client tests in another.
 *
 * See Utils in this package for information on environment required to run the tests.
 * 
 */
@Test(groups={"integration"})
public class UserTest
{
  // Test data

  // Create a single static client. Must do it this way because headers are static and JWT is in the header.
  // Updating client dynamically would give false sense of security since tests might be run in parallel and there
  //   would be concurrency issues.
  private static FilesClient usrClient;
  private static String serviceURL;
  private static String ownerUserJWT;

  @BeforeSuite
  public void setUp() throws Exception {
    // Get the base URLs from the environment so the test can be used in environments other than dev
    System.out.println("****** Executing BeforeSuite setup method for class: " + this.getClass().getSimpleName());
    // Set service port for service. Check for port set as env var
    // NOTE: This is ignored if TAPIS_ENV_SVC_URL is set
    String servicePort = Utils.getServicePort();
    // Set base URL for service. Check for URL set as env var
    serviceURL = Utils.getServiceURL(servicePort);
    // Get base URL suffix from env or from default
    String baseURL = Utils.getBaseURL();
    // Log URLs being used
    System.out.println("Using Service URL: " + serviceURL);
    System.out.println("Using Authenticator URL: " + baseURL);
    System.out.println("Using Tokens URL: " + baseURL);
    // Get short term user JWT from auth service
    var authClient = new AuthClient(baseURL);
    try {
      ownerUserJWT = authClient.getToken(testUser2, testUser2);
    } catch (Exception e) {
      throw new Exception("Exception while creating tokens or auth service", e);
    }
    // Basic check of JWTs
    if (StringUtils.isBlank(ownerUserJWT)) throw new Exception("Authn service returned invalid owner user JWT");

    // Cleanup anything leftover from previous failed run
    tearDown();

    // Create user client
    usrClient = getClientUsr(serviceURL, ownerUserJWT);

  }

  @AfterSuite
  public void tearDown() {
    System.out.println("****** Executing AfterSuite teardown method for class: " + this.getClass().getSimpleName());
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
//      System.out.println("Checking ready status");
//      status = usrClient.checkReady();
//      System.out.println("Ready status: " + status);
//      Assert.assertNotNull(status);
//      Assert.assertFalse(StringUtils.isBlank(status), "Invalid response: " + status);
//      Assert.assertEquals(status, "success", "Service failed ready check");
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }

  @Test
  public void testListFiles() throws Exception {
    // System id to list
    String sysId = "tapisv3-exec";
    // Make call to list files
    List<FileInfo> fileList = usrClient.listFiles(sysId,"/filestest", 100, 0, true);
    Assert.assertNotNull(fileList, "Returned fileList should not be null");
    System.out.println("Number of files returned: " + fileList.size());
    Assert.assertTrue(fileList.size() > 0);
    for (FileInfo fileInfo : fileList)
    {
      System.out.println("Found file: " + fileInfo.getName());
      System.out.println("  size: " + fileInfo.getSize());
      System.out.println("  time: " + fileInfo.getLastModified());
      System.out.println("  path: " + fileInfo.getPath());
    }
  }
}
