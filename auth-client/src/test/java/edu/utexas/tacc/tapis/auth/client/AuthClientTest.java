package edu.utexas.tacc.tapis.auth.client;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 *  Test the auth client by retrieving a user token
 *  Use a base URL from the env or the default hard coded base URL.
 */
@Test(groups={"integration"})
public class AuthClientTest
{
  // Default URLs. These can be overridden by env variables
  private static final String DEFAULT_BASE_URL = "https://dev.develop.tapis.io";
  // Env variables for setting URLs
  private static final String TAPIS_ENV_SVC_URL_AUTH = "TAPIS_SVC_URL_AUTHENTICATOR";

  // Test data
  private static final String userName = "testuser1";

  private AuthClient authClient;

  @BeforeSuite
  public void setUp() throws Exception
  {
    System.out.println("Executing BeforeSuite setup method");
    // Create the client
    // Get service URL from env or from default
    String serviceURL = System.getenv(TAPIS_ENV_SVC_URL_AUTH);
    if (StringUtils.isBlank(serviceURL)) serviceURL = DEFAULT_BASE_URL;
    authClient = new AuthClient(serviceURL);
  }

  @Test(enabled=true)
  public void testGetUserToken() throws Exception
  {
    String usrToken = authClient.getToken(userName, userName);
    System.out.println("Got token for user: " + userName);
    System.out.println("Token: " + usrToken);
    Assert.assertFalse(StringUtils.isBlank(usrToken), "User token should not be blank");
    // TODO Further validation
    // TODO decode token and print some info
  }

  @AfterSuite
  public void tearDown()
  {
    System.out.println("Executing AfterSuite teardown method");
    //Remove all objects created by tests, ignore any exceptions
  }
}
