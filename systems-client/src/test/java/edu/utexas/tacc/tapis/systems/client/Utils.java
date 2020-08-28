package edu.utexas.tacc.tapis.systems.client;

/* Shared data and methods for running the systems client tests.
 *
 * Notes on enviornment required to run the tests:
 *  - Systems service base URL comes from the env or the default hard coded base URL.
 *  - Auth and tokens base URL comes from the env or the default hard coded base URL.
 *  - Auth service is used to get a short term JWT.
 *  - Tokens service is used to get a files service JWT.
 *  - Files service password must come from the environment: TAPIS_FILES_SVC_PASSWORD
 *
 * TAPIS_BASE_URL_SUFFIX should be set according to the dev, staging or prod environment
 *   dev     -> develop.tapis.io
 *   staging -> staging.tapis.io
 *   prod    -> tapis.io
 *
 *  To override base URLs use the following env variables:
 *    TAPIS_SVC_URL_SYSTEMS
 *    TAPIS_BASE_URL (for auth, tokens services)
 *  To override systems service port use:
 *    TAPIS_SERVICE_PORT
 *  NOTE that service port is ignored if TAPIS_SVC_URL_SYSTEMS is set
 */

import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Utilities and data for integration testing
 */
public final class Utils
{
  // Test data
  public static final String tenantName = "dev";
  public static final String ownerUser = "testuser2";
  public static final String adminUser = "testSystemsAdminUsr";
  public static final String masterTenantName = "master";
  public static final String filesSvcName = "files";
  public static final String sysType = ReqCreateSystem.SystemTypeEnum.LINUX.name();
  // TAPIS_BASE_URL_SUFFIX should be set according to the dev, staging or prod environment
  // dev     -> develop.tapis.io
  // staging -> staging.tapis.io
  // prod    -> tapis.io
  // Default URLs. These can be overridden by env variables
  public static final String DEFAULT_BASE_URL = "https://" + tenantName + ".develop.tapis.io";
  public static final String DEFAULT_BASE_URL_SYSTEMS = "http://localhost";
  public static final String DEFAULT_SVC_PORT = "8080";
  // Env variables for setting URLs, passwords, etc
  public static final String TAPIS_ENV_BASE_URL = "TAPIS_BASE_URL";
  public static final String TAPIS_ENV_SVC_URL_SYSTEMS = "TAPIS_SVC_URL_SYSTEMS";
  public static final String TAPIS_ENV_FILES_SVC_PASSWORD = "TAPIS_FILES_SERVICE_PASSWORD";
  public static final String TAPIS_ENV_SVC_PORT = "TAPIS_SERVICE_PORT";

  public static final String ownerUser2 = "owner2";
  public static final String apiUser = "testApiUser";
  public static final String sysNamePrefix = "CSys";

  /**
   * Create an array of TSystem objects in memory
   * Names will be of format TestSys_K_NNN where K is the key and NNN runs from 000 to 999
   * We need a key because maven runs the tests in parallel so each set of systems created by an integration
   *   test will need its own namespace.
   * @param n number of systems to create
   * @return array of TSystem objects
   */
  public static Map<Integer, String[]> makeSystems(int n, String key)
  {
    Map<Integer, String[]> systems = new HashMap<>();
    for (int i = 1; i <= n; i++)
    {
      // Suffix which should be unique for each system within each integration test
      String suffix = key + "_" + String.format("%03d", i);
      String name = sysNamePrefix + "_" + suffix;
      // Constructor initializes all attributes except for JobCapabilities and Credential
      String[] sys0 = {tenantName, name, "description " + suffix, sysType, ownerUser, "host"+suffix, "effUser"+suffix,
              "fakePassword"+suffix,"bucket"+suffix, "/root"+suffix, "jobLocalWorkDir"+suffix, "jobLocalArchDir"+suffix,
              "jobRemoteArchSystem"+suffix, "jobRemoteArchDir"+suffix};
      systems.put(i, sys0);
    }
    return systems;
  }

  public static String getFilesSvcPassword()
  {
    String s = System.getenv(TAPIS_ENV_FILES_SVC_PASSWORD);
    if (StringUtils.isBlank(s))
    {
      System.out.println("ERROR: Files service password must be set using environment variable:  " + TAPIS_ENV_FILES_SVC_PASSWORD);
      System.exit(1);
    }
    return s;
  }
  public static String getServicePort()
  {
    String s = System.getenv(TAPIS_ENV_SVC_PORT);
    System.out.println("Systems Service port from ENV: " + s);
    if (StringUtils.isBlank(s)) s = DEFAULT_SVC_PORT;
    return s;
  }
  public static String getServiceURL(String servicePort)
  {
    String s = System.getenv(TAPIS_ENV_SVC_URL_SYSTEMS);
    System.out.println("Systems Service URL from ENV: " + s);
    if (StringUtils.isBlank(s)) s = DEFAULT_BASE_URL_SYSTEMS + ":" + servicePort;
    return s;
  }
  public static String getBaseURL()
  {
    String s = System.getenv(TAPIS_ENV_BASE_URL);
    if (StringUtils.isBlank(s)) s = DEFAULT_BASE_URL;
    return s;
  }
}
