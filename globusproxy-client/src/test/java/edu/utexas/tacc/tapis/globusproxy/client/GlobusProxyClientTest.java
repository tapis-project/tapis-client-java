package edu.utexas.tacc.tapis.globusproxy.client;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.globusproxy.client.gen.model.ResultGlobusAuthInfo;

/**
 *  Test the globusproxy client
 *  Use a base URL from the env or the default hard coded base URL.
 */
@Test(groups={"integration"})
public class GlobusProxyClientTest
{
  // Default URLs. These can be overridden by env variables
  private static final String DEFAULT_BASE_URL = "https://dev.develop.tapis.io";
  // Env variables for setting URLs
  private static final String TAPIS_ENV_SVC_URL_AUTH = "TAPIS_SVC_URL_AUTHENTICATOR";

  // Test data
  private static final String tenantName = "dev";
  private static final String userName = "testuser1";
  private static final String clientId = "0259148a-8ae0-44b7-80b5-a4060e92dd3e";
  private static final String endpointId = "1784148a-8ae0-44b7-80b5-b5999e92de3a";
  // Long term JWT for testuser1 for DEV - expires approx 1 Sep 2022
  public static final String testUser1JWT ="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI4MGU3YzljZi1kYjFlLTQzNzQtYTg3NC0zZjFiM2ZmYjdhYzQiLCJpc3MiOiJodHRwczovL2Rldi5kZXZlbG9wLnRhcGlzLmlvL3YzL3Rva2VucyIsInN1YiI6InRlc3R1c2VyMUBkZXYiLCJ0YXBpcy90ZW5hbnRfaWQiOiJkZXYiLCJ0YXBpcy90b2tlbl90eXBlIjoiYWNjZXNzIiwidGFwaXMvZGVsZWdhdGlvbiI6ZmFsc2UsInRhcGlzL2RlbGVnYXRpb25fc3ViIjpudWxsLCJ0YXBpcy91c2VybmFtZSI6InRlc3R1c2VyMSIsInRhcGlzL2FjY291bnRfdHlwZSI6InVzZXIiLCJleHAiOjE2NjM5MDMyNjJ9.O9ID46YpTL-cgZvhuisytEkde6vFGMJ9Lu4HcEsvTAS6fJqLVds9w9cBah_FfSLZcUu38ddw7cjYHjSGi5crW2G32fWKPjOA8mrk9EE8Q-BNB_bzSYVXOt7-4dRBAyQnEu7d7OYqGJo4-2F4U7210JXtfNog0CH1S0oH8j0ZaiuAA5ula9bhUxXUmJYZhQcyXvxcgBzD_2fEzS2c0h5NWRb-O9abKmuD51ASvYpgrOB8_kmU1P_A91P5YP2H3Kx9E6ijm10GzQJH9euy2nLqKyH20pmvEysQMoq9u0tirThXN4WXLvkyIOtllOyICgSrfxkz1x6yoTkMf-YTrlryuA";

  private GlobusProxyClient globusProxyClient;

  @BeforeSuite
  public void setUp()
  {
    System.out.println("Executing BeforeSuite setup method");
    // Create the client
    // Get service URL from env or from default
    String serviceURL = System.getenv(TAPIS_ENV_SVC_URL_AUTH);
    if (StringUtils.isBlank(serviceURL)) serviceURL = DEFAULT_BASE_URL;
    globusProxyClient = new GlobusProxyClient(serviceURL, testUser1JWT);
  }

  @Test
  public void testGetAuthInfo() throws Exception
  {
    ResultGlobusAuthInfo authInfo = globusProxyClient.getAuthInfo(clientId, endpointId);
    System.out.printf("Got authInfo for clientId: %s%n", userName);
    System.out.printf("authInfo. url: %s sessionId: %s%n", authInfo.getUrl(), authInfo.getSessionId());
    Assert.assertFalse(StringUtils.isBlank(authInfo.getUrl()), "authInfo.url should not be blank");
    Assert.assertFalse(StringUtils.isBlank(authInfo.getSessionId()), "authInfo.sessionId should not be blank");
  }

  @AfterSuite
  public void tearDown()
  {
    System.out.println("Executing AfterSuite teardown method");
    //Remove all objects created by tests, ignore any exceptions
  }
}
