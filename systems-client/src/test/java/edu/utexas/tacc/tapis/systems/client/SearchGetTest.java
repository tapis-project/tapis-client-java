package edu.utexas.tacc.tapis.systems.client;

import com.google.gson.JsonObject;
import edu.utexas.tacc.tapis.auth.client.AuthClient;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.systems.client.SystemsClient.AccessMethod;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability.CategoryEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateCredential;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem.SystemTypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqUpdateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem;
import edu.utexas.tacc.tapis.tokens.client.TokensClient;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static edu.utexas.tacc.tapis.systems.client.Utils.filesSvcName;
import static edu.utexas.tacc.tapis.systems.client.Utils.masterTenantName;
import static edu.utexas.tacc.tapis.systems.client.Utils.ownerUser;

/*
 * Test the Systems API client acting as a user fetching systems using getSystems() with search conditions.
 * 
 * See IntegrationUtils in this package for information on environment required to run the tests.
 */
@Test(groups={"integration"})
public class SearchGetTest
{
  // Test data
  int numSystems = 20;
  Map<Integer, String[]> systems = Utils.makeSystems(numSystems, "CltSrchGet");

  private static final String newOwnerUser = "testuser3";
  private static final String newPermsUser = "testuser4";
  private static final int prot1Port = -1, prot1ProxyPort = -1, prot2Port = 22, prot2ProxyPort = 222;
  private static final boolean prot1UseProxy = false, prot2UseProxy = true;
  private static final String prot1ProxyHost = "proxyhost1", prot2ProxyHost = "proxyhost2";
  private static final List<ReqCreateSystem.TransferMethodsEnum> prot1TxfrMethodsC =
          Arrays.asList(ReqCreateSystem.TransferMethodsEnum.SFTP, ReqCreateSystem.TransferMethodsEnum.S3);
  private static final List<TSystem.TransferMethodsEnum> prot1TxfrMethodsT =
          Arrays.asList(TSystem.TransferMethodsEnum.SFTP, TSystem.TransferMethodsEnum.S3);
  private static final List<ReqUpdateSystem.TransferMethodsEnum> prot2TxfrMethodsU =
          Collections.singletonList(ReqUpdateSystem.TransferMethodsEnum.SFTP);
  private static final List<TSystem.TransferMethodsEnum> prot2TxfrMethodsT =
          Collections.singletonList(TSystem.TransferMethodsEnum.SFTP);
  private static final AccessMethod prot1AccessMethod = AccessMethod.PKI_KEYS;
  private static final AccessMethod prot2AccessMethod = AccessMethod.ACCESS_KEY;
  private static final List<String> tags1 = Arrays.asList("value1", "value2", "a",
          "a long tag with spaces and numbers (1 3 2) and special characters [_ $ - & * % @ + = ! ^ ? < > , . ( ) { } / \\ | ]. Backslashes must be escaped.");
  private static final List<String> tags2 = Arrays.asList("value3", "value4");
  private static final JsonObject notes1JO =
          ClientTapisGsonUtils.getGson().fromJson("{\"project\":\"myproj1\", \"testdata\":\"abc1\"}", JsonObject.class);
  private static final JsonObject notes2JO =
          ClientTapisGsonUtils.getGson().fromJson("{\"project\":\"myproj2\", \"testdata\":\"abc2\"}", JsonObject.class);
  private static final List<String> testPerms = new ArrayList<>(List.of("READ", "MODIFY"));

  private static final Capability capA1 = SystemsClient.buildCapability(CategoryEnum.SCHEDULER, "Type", "Slurm");
  private static final Capability capB1 = SystemsClient.buildCapability(CategoryEnum.HARDWARE, "CoresPerNode", "4");
  private static final Capability capC1 = SystemsClient.buildCapability(CategoryEnum.SOFTWARE, "OpenMP", "4.5");
  private static final Capability capA2 = SystemsClient.buildCapability(CategoryEnum.SCHEDULER, "Type", "Condor");
  private static final Capability capB2 = SystemsClient.buildCapability(CategoryEnum.HARDWARE, "CoresPerNode", "128");
  private static final Capability capC2 = SystemsClient.buildCapability(CategoryEnum.SOFTWARE, "OpenMP", "3.1");
  private static final Capability capD2 = SystemsClient.buildCapability(CategoryEnum.CONTAINER, "Singularity", null);
  private static final List<Capability> jobCaps1 = new ArrayList<>(List.of(capA1, capB1, capC1));
  private static final List<Capability> jobCaps2 = new ArrayList<>(List.of(capA2, capB2, capC2, capD2));

  private String serviceURL, ownerUserJWT, newOwnerUserJWT;

  @BeforeSuite
  public void setUp() throws Exception {
    // Get the base URLs from the environment so the test can be used in environments other than dev
    System.out.println("Executing BeforeSuite setup method");
    // Set service port for systems service. Check for port set as env var
    // NOTE: This is ignored if TAPIS_ENV_SVC_URL_SYSTEMS is set
    String servicePort = Utils.getServicePort();
    // Set base URL for systems service. Check for URL set as env var
    serviceURL = Utils.getServiceURL(servicePort);
    // Get base URL suffix from env or from default
    String baseURL = Utils.getBaseURL();
    // Log URLs being used
    System.out.println("Using Systems URL: " + serviceURL);
    System.out.println("Using Authenticator URL: " + baseURL);
    System.out.println("Using Tokens URL: " + baseURL);
    // Get short term user JWT from tokens service
    var authClient = new AuthClient(baseURL);
    try {
      ownerUserJWT = authClient.getToken(ownerUser, ownerUser);
      newOwnerUserJWT = authClient.getToken(newOwnerUser, newOwnerUser);
    } catch (Exception e) {
      throw new Exception("Exception while creating tokens or auth service", e);
    }
    // Basic check of JWTs
    if (StringUtils.isBlank(ownerUserJWT)) throw new Exception("Authn service returned invalid owner user JWT");
    if (StringUtils.isBlank(newOwnerUserJWT)) throw new Exception("Authn service returned invalid new owner user JWT");
    // Cleanup anything leftover from previous failed run
    tearDown();
  }

  @AfterSuite
  public void tearDown() {
    System.out.println("Executing AfterSuite teardown method");
    // TODO: Run SQL to hard delete objects
    //Remove all objects created by tests, ignore any exceptions
    for (int i = 0; i < numSystems; i++)
    {
      try
      {
        getClientUsr().deleteSystemByName(systems.get(i)[1]);
      } catch (Exception e)
      {
      }
    }
  }

  private SystemsClient getClientUsr()
  {
    // Create the client each time due to issue with setting different headers needed by svc vs usr client
    SystemsClient clt = new SystemsClient(serviceURL, ownerUserJWT);
    // Creating a separate client for svc is not working because headers end up being used for all clients.
    // Underlying defaultHeaderMap is static so adding headers impacts all clients.
//    sysClientSvc = new SystemsClient(systemsURL, svcJWT);
//    sysClientSvc.addDefaultHeader("X-Tapis-User", sysOwner);
//    sysClientSvc.addDefaultHeader("X-Tapis-Tenant", tenantName);
    return clt;
  }
}

