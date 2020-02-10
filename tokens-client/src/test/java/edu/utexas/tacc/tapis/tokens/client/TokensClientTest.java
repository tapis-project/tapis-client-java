package edu.utexas.tacc.tapis.tokens.client;

import static org.testng.Assert.assertNotNull;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.tokens.client.gen.model.InlineObject1.AccountTypeEnum;
import edu.utexas.tacc.tapis.tokens.client.model.CreateTokenParms;
import edu.utexas.tacc.tapis.tokens.client.model.RefreshTokenParms;


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

  @Test(enabled=true)
  public void testGetUserToken() throws Exception
  {
    String usrToken = tokensClient.getUsrToken(tenantName, userName);
    System.out.println("Got token for user: " + userName);
    System.out.println("Token: " + usrToken);
    Assert.assertFalse(StringUtils.isBlank(usrToken), "User token should not be blank");
  }

  @Test(enabled=true)
  public void testGetSvcToken() throws Exception
  {
    String svcToken = tokensClient.getSvcToken(tenantName, serviceName);
    System.out.println("Got token for service: " + serviceName);
    System.out.println("Token: " + svcToken);
    Assert.assertFalse(StringUtils.isBlank(svcToken), "Service token should not be blank");
  }
  
  @Test(enabled=true)
  public void testNewAndRefreshToken() throws Exception
  {
      // Populate the parameters object to configure a refresh token.
      var createParms = new CreateTokenParms();
      createParms.setTokenTenantId(tenantName);
      createParms.setTokenUsername(serviceName);
      createParms.setAccountType(AccountTypeEnum.SERVICE);
      createParms.setAccessTokenTtl(360);
      createParms.generateRefreshToken(true);
      createParms.setRefreshTokenTtl(600);
      var tokpkg = tokensClient.createToken(createParms);
      
      // Check create token results.
      Assert.assertNotNull(tokpkg.getAccessToken(), "No access token created.");
      Assert.assertNotNull(tokpkg.getAccessToken().getAccessToken(), "Null access token string.");
      Assert.assertNotNull(tokpkg.getAccessToken().getExpiresAt(), "Null access token expiresAt.");
      Assert.assertNotNull(tokpkg.getAccessToken().getExpiresIn(), "Null access token expiresIn.");
      Assert.assertNotNull(tokpkg.getRefreshToken(), "No refresh token created.");
      Assert.assertNotNull(tokpkg.getRefreshToken().getRefreshToken(), "Null refresh token string.");
      Assert.assertNotNull(tokpkg.getRefreshToken().getExpiresAt(), "Null refresh token expiresAt.");
      Assert.assertNotNull(tokpkg.getRefreshToken().getExpiresIn(), "Null refresh token expiresIn.");
  
      // Temporay code that forces the refreshed JWTs 
      // to be different from the originals. This should
      // be removed when unique ids are added to tokens.
      Thread.currentThread().sleep(1000);
      
      // Issue the refresh call.
      var refreshParms = new RefreshTokenParms();
      refreshParms.setRefreshToken(tokpkg.getRefreshToken().getRefreshToken());
      var tokpkg2 = tokensClient.refreshToken(refreshParms);
      
      // Check refresh token results.
      Assert.assertNotNull(tokpkg2.getAccessToken(), "No access token created.");
      Assert.assertNotNull(tokpkg2.getAccessToken().getAccessToken(), "Null access token string.");
      Assert.assertNotNull(tokpkg2.getAccessToken().getExpiresAt(), "Null access token expiresAt.");
      Assert.assertNotNull(tokpkg2.getAccessToken().getExpiresIn(), "Null access token expiresIn.");
      Assert.assertNotNull(tokpkg2.getRefreshToken(), "No refresh token created.");
      Assert.assertNotNull(tokpkg2.getRefreshToken().getRefreshToken(), "Null refresh token string.");
      Assert.assertNotNull(tokpkg2.getRefreshToken().getExpiresAt(), "Null refresh token expiresAt.");
      Assert.assertNotNull(tokpkg2.getRefreshToken().getExpiresIn(), "Null refresh token expiresIn.");
  
      // We like different JWTs.
      Assert.assertNotEquals(tokpkg.getAccessToken().getAccessToken(), 
                             tokpkg2.getAccessToken().getAccessToken(), 
                             "Refreshed access token is the same as the original.");
      Assert.assertNotEquals(tokpkg.getRefreshToken().getRefreshToken(), 
                             tokpkg2.getRefreshToken().getRefreshToken(), 
                             "Refreshed refresh token is the same as the original.");
  
  }


  @AfterSuite
  public void tearDown()
  {
    System.out.println("Executing AfterSuite teardown method");
    //Remove all objects created by tests, ignore any exceptions
//    try { tokensClient.delete???("id"); } catch (Exception e) {}
  }

}
