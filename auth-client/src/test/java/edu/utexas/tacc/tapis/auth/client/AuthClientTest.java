package edu.utexas.tacc.tapis.auth.client;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

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
    // Code copied from tapis-shared-api JWTValidateRequestFilter
    // Lop off the signature part of the encoding so that the
    // jjwt library can parse it without attempting validation.
    // We expect the jwt to contain exactly two periods in
    // the following encoded format: header.body.signature
    // We need to remove the signature but leave both periods.
    String remnant = usrToken;
    int lastDot = usrToken.lastIndexOf(".");
    if (lastDot + 1 < usrToken.length()) remnant = usrToken.substring(0, lastDot + 1); // should always be true

    // Parse the header and claims. If for some reason the remnant
    // isn't of the form header.body. then parsing will fail.
    var jwt = Jwts.parser().parse(remnant);
    // Check that tenant_id is dev, username is testuser1, account_type is user
    // Get the claims.
    Claims claims = (Claims) jwt.getBody();
    String jwtTokenType = (String) claims.get("tapis/token_type");
    System.out.println("tapis/account_type: " + jwtTokenType);
    String jwtTenant = (String) claims.get("tapis/tenant_id");
    System.out.println("tapis/tenant_id: " + jwtTenant);
    String jwtUser = (String) claims.get("tapis/username");
    System.out.println("tapis/username: " + jwtUser);
    String jwtAccountType = (String) claims.get("tapis/account_type");
    System.out.println("tapis/account_type: " + jwtAccountType);
    String jwtGrantType = (String) claims.get("tapis/grant_type");
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
