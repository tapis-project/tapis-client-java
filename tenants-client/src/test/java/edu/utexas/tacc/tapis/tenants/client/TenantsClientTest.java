package edu.utexas.tacc.tapis.tenants.client;

import edu.utexas.tacc.tapis.tenants.client.gen.model.Tenant;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 *  Test the tenants client by retrieving the dev tenant
 */
@Test(groups={"integration"})
public class TenantsClientTest
{
  // Test data
  private static final String tenantName = "dev";
  private static final String SK_URL = "https://dev.develop.tapis.io/v3/security";
  private static final String TOK_URL = "https://dev.develop.tapis.io/v3/tokens";
  private static final String AUTH_URL = "https://dev.develop.tapis.io/v3/oauth2";

  private TenantsClient tenantsClient;

  @BeforeSuite
  public void setUp() throws Exception
  {
    System.out.println("Executing BeforeSuite setup method");
    // Create the client
    // Check for URL set as env var
    String tenantsURL = System.getenv("TAPIS_SVC_URL_TENANTS");
    if (StringUtils.isBlank(tenantsURL)) tenantsURL ="https://dev.develop.tapis.io";
    tenantsClient = new TenantsClient(tenantsURL);
  }

  @Test
  public void testGetTenantByName() throws Exception
  {
    Tenant tenant1  = tenantsClient.getTenant(tenantName);

    Assert.assertNotNull(tenant1, "Failed to retrieve tenant with name: " + tenantName);
    System.out.println("Found tenant with name: " + tenantName);
    System.out.println("  Tenant Id: " + tenant1.getTenantId());
    System.out.println("  Authenticator URL: " + tenant1.getAuthenticator());
    System.out.println("  Token Svc URL: " + tenant1.getTokenService());
    System.out.println("  SK Svc URL: " + tenant1.getSecurityKernel());
    System.out.println("  Public Key: " + tenant1.getPublicKey());
    Assert.assertFalse(StringUtils.isBlank(tenant1.getTenantId()), "Tenant Id should not be blank");
    Assert.assertEquals(tenant1.getSecurityKernel(), SK_URL);
    Assert.assertEquals(tenant1.getTokenService(), TOK_URL);
    Assert.assertEquals(tenant1.getAuthenticator(), AUTH_URL);
    Assert.assertFalse(StringUtils.isBlank(tenant1.getPublicKey()), "Public key should not be blank");
  }

  @AfterSuite
  public void tearDown()
  {
    System.out.println("Executing AfterSuite teardown method");
    //Remove all objects created by tests, ignore any exceptions
//    try { tenantsClient.delete???("id"); } catch (Exception e) {}
  }
}
