package edu.utexas.tacc.tapis.files.client;

/* Shared data and methods for running client tests.
 *
 * Notes on environment required to run the tests:
 *  - Service base URL comes from the env or the default hard coded base URL.
 *  - Auth and tokens base URL comes from the env or the default hard coded base URL.
 *  - Auth service is used to get a short term JWT.
 *
 * TAPIS_BASE_URL_SUFFIX should be set according to the dev, staging or prod environment
 *   dev     -> develop.tapis.io
 *   staging -> staging.tapis.io
 *   prod    -> tapis.io
 *
 *  To override base URLs use the following env variables:
 *    TAPIS_SVC_URL
 *    TAPIS_BASE_URL (for auth, tokens services)
 *  To override service port use:
 *    TAPIS_SERVICE_PORT
 *  NOTE that service port is ignored if TAPIS_SVC_URL is set
 */

import org.apache.commons.lang3.StringUtils;

/*
 * Utilities and data for integration testing
 */
public final class Utils
{
  // Test data
  public static final String tenantName = "dev";
  public static final String testUser1 = "testuser1";
  public static final String testUser2 = "testuser2";
  public static final String testUser3 = "testuser3";
  public static final String testUser4 = "testuser4";
  public static final String testUser9 = "testuser9";
  // testuser9 must be given role "$!tenant_admin"
  public static final String adminUser = testUser9;
  public static final String ownerUser1 = testUser1;
  public static final String ownerUser2 = testUser2;
  public static final String adminTenantName = "admin";
  // TAPIS_BASE_URL_SUFFIX should be set according to the dev, staging or prod environment
  // dev     -> develop.tapis.io
  // staging -> staging.tapis.io
  // prod    -> tapis.io
  // Default URLs. These can be overridden by env variables
  public static final String DEFAULT_BASE_URL = "https://" + tenantName + ".develop.tapis.io";
  public static final String DEFAULT_SVC_PORT = "8080";
  // Env variables for setting URLs, passwords, etc
  public static final String TAPIS_ENV_BASE_URL = "TAPIS_BASE_URL";
  public static final String TAPIS_ENV_SVC_URL = "TAPIS_SVC_URL";
  public static final String TAPIS_ENV_FILES_SVC_PASSWORD = "TAPIS_FILES_SERVICE_PASSWORD";
  public static final String TAPIS_ENV_SVC_PORT = "TAPIS_SERVICE_PORT";


  public static String getServicePort()
  {
    String s = System.getenv(TAPIS_ENV_SVC_PORT);
    System.out.println("Service port from ENV: " + s);
    if (StringUtils.isBlank(s)) s = DEFAULT_SVC_PORT;
    return s;
  }
  public static String getServiceURL(String servicePort)
  {
    String s = System.getenv(TAPIS_ENV_SVC_URL);
    System.out.println("Service URL from ENV: " + s);
//    if (StringUtils.isBlank(s)) s = DEFAULT_BASE_URL + ":" + servicePort;
    if (StringUtils.isBlank(s)) s = DEFAULT_BASE_URL;
    return s;
  }
  public static String getBaseURL()
  {
    String s = System.getenv(TAPIS_ENV_BASE_URL);
    if (StringUtils.isBlank(s)) s = DEFAULT_BASE_URL;
    return s;
  }

  public static FilesClient getClientUsr(String serviceURL, String userJWT)
  {
    // Create the client each time due to issue with setting different headers needed by svc vs usr client

    // Creating a separate client for svc is not working because headers end up being used for all clients.
    // Underlying defaultHeaderMap is static so adding headers impacts all clients.
//    sysClientSvc = new SystemsClient(systemsURL, svcJWT);
//    sysClientSvc.addDefaultHeader("X-Tapis-User", sysOwner);
//    sysClientSvc.addDefaultHeader("X-Tapis-Tenant", tenantName);
    return new FilesClient(serviceURL, userJWT);
  }

}
