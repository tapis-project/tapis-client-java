package edu.utexas.tacc.tapis.auth.client;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.Map;

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
  private static final String tenantName = "dev";
  private static final String userName = "testuser1";

  private AuthClient authClient;

  @BeforeSuite
  public void setUp()
  {
    System.out.println("Executing BeforeSuite setup method");
    // Create the client
    // Get service URL from env or from default
    String serviceURL = System.getenv(TAPIS_ENV_SVC_URL_AUTH);
    if (StringUtils.isBlank(serviceURL)) serviceURL = DEFAULT_BASE_URL;
    authClient = new AuthClient(serviceURL);
  }

  @Test
  public void testGetUserToken() throws Exception
  {
    String usrToken = authClient.getToken(userName, userName);
    System.out.println("Got token for user: " + userName);
    System.out.println("Token: " + usrToken);
    Assert.assertFalse(StringUtils.isBlank(usrToken), "User token should not be blank");
    // Decode token and print some info
    // Decode the jwt
    DecodedJWT unverifiedJwt = JWT.decode(usrToken);
    // Get claims. If no claims then abort
    Map<String, Claim> claims = unverifiedJwt.getClaims();
    Assert.assertNotNull(claims);
    Assert.assertFalse(claims.isEmpty());

    // Check that tenant_id is dev, username is testuser1, account_type is user
    String jwtTokenType = claims.get("tapis/token_type").asString();
    System.out.println("tapis/token_type: " + jwtTokenType);
    String jwtTenant = claims.get("tapis/tenant_id").asString();
    System.out.println("tapis/tenant_id: " + jwtTenant);
    String jwtUser = claims.get("tapis/username").asString();
    System.out.println("tapis/username: " + jwtUser);
    String jwtAccountType = claims.get("tapis/account_type").asString();
    System.out.println("tapis/account_type: " + jwtAccountType);
    String jwtGrantType = claims.get("tapis/grant_type").asString();
    System.out.println("tapis/grant_type: " + jwtGrantType);
    Assert.assertEquals(jwtTokenType, "access");
    Assert.assertEquals(jwtTenant, tenantName);
    Assert.assertEquals(jwtUser, userName);
    Assert.assertEquals(jwtAccountType, "user");
    Assert.assertEquals(jwtGrantType, "password");
  }

  @AfterSuite
  public void tearDown()
  {
    System.out.println("Executing AfterSuite teardown method");
    //Remove all objects created by tests, ignore any exceptions
  }
}
