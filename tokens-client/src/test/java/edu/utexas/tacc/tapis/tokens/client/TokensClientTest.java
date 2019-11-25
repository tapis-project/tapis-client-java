package edu.utexas.tacc.tapis.tokens.client;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;


/**
 *  Test the tokens client by retrieving a service token and a user token
 *  Use a base URL from the env or the default hard coded base URL.
 */
@Test(groups={"integration"})
public class TokensClientTest
{
  // Default URLs. These can be overridden by env variables
  private static final String DEFAULT_BASE_URL_TOKENS = "https://dev.develop.tapis.io";
  // Env variables for setting URLs
  private static final String TAPIS_ENV_SVC_URL_TOKENS = "TAPIS_SVC_URL_TOKENS";

  // Test data
  private static final String tenantName = "dev";
  private static final String userName = "testuser1";
  private static final String serviceName = "test_service";

  private TokensClient tokensClient;

  @BeforeSuite
  public void setUp() throws Exception
  {
    System.out.println("Executing BeforeSuite setup method");
    // Create the client
    // Get token using URL from env or from default
    String tokensURL = System.getenv(TAPIS_ENV_SVC_URL_TOKENS);
    if (StringUtils.isBlank(tokensURL)) tokensURL = DEFAULT_BASE_URL_TOKENS;
    tokensClient = new TokensClient(tokensURL);
  }

  @Test
  public void testGetUserToken() throws Exception
  {
    String usrToken = tokensClient.getUsrToken(tenantName, userName);
    System.out.println("Got token for user: " + userName);
    System.out.println("Token: " + usrToken);
    Assert.assertFalse(StringUtils.isBlank(usrToken), "User token should not be blank");
  }

  @Test
  public void testGetSvcToken() throws Exception
  {
    String svcToken = tokensClient.getSvcToken(tenantName, serviceName);
    System.out.println("Got token for service: " + serviceName);
    System.out.println("Token: " + svcToken);
    Assert.assertFalse(StringUtils.isBlank(svcToken), "Service token should not be blank");
  }

  @AfterSuite
  public void tearDown()
  {
    System.out.println("Executing AfterSuite teardown method");
    //Remove all objects created by tests, ignore any exceptions
//    try { tokensClient.delete???("id"); } catch (Exception e) {}
  }

}
