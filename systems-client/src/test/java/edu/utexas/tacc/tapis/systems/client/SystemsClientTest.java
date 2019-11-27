package edu.utexas.tacc.tapis.systems.client;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.tacc.tapis.shared.exceptions.TapisClientException;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem.AccessMechanismEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem.TransferMechanismsEnum;
import edu.utexas.tacc.tapis.tokens.client.TokensClient;

import static edu.utexas.tacc.tapis.shared.TapisConstants.SERVICE_NAME_SYSTEMS;

// TODO Update tests to check "tags" value
// TODO Update tests to check "notes" value

/**
 * Test the Systems API client against the systems service.
 * Use a base URL from the env or the default hard coded base URL.
 * Tokens service is used to get a short term JWT.
 * Tokens service URL comes from the env or the default hard coded URL.
 */
@Test(groups={"integration"})
public class SystemsClientTest
{
  // Default URLs. These can be overridden by env variables
  private static final String DEFAULT_BASE_URL_SYSTEMS = "https://dev.develop.tapis.io";
  private static final String DEFAULT_BASE_URL_TOKENS = "https://dev.develop.tapis.io";
  // Env variables for setting URLs
  private static final String TAPIS_ENV_SVC_URL_SYSTEMS = "TAPIS_SVC_URL_SYSTEMS";
  private static final String TAPIS_ENV_SVC_URL_TOKENS = "TAPIS_SVC_URL_TOKENS";


  // Test data
  private static final String tenantName = "dev";
  private static final int prot1Port = -1;
  private static final boolean prot1UseProxy = false;
  private static final String prot1ProxyHost = "";
  private static final int prot1ProxyPort = -1;
  private static final TransferMechanismsEnum[] prot1TxfrMechs = {TransferMechanismsEnum.SFTP, TransferMechanismsEnum.S3};
  private static final AccessMechanismEnum prot1AccessMechanism = AccessMechanismEnum.NONE;
  private static final String tags = "{\"key1\":\"a\", \"key2\":\"b\"}";
  private static final String notes = "{\"project\":\"myproj1\", \"testdata\":\"abc\"}";

  private static final String[] sys1 = {tenantName, "Csys1", "description 1", "owner1", "host1", "bucket1", "/root1",
    "jobInputDir1", "jobOutputDir1", "workDir1", "scratchDir1", "effUser1", tags, notes, "fakePassword1"};
  private static final String[] sys2 = {tenantName, "Csys2", "description 2", "owner2", "host2", "bucket2", "/root2",
    "jobInputDir2", "jobOutputDir2", "workDir2", "scratchDir2", "effUser2", tags, notes, "fakePassword2"};
  private static final String[] sys3 = {tenantName, "Csys3", "description 3", "owner3", "host3", "bucket3", "/root3",
    "jobInputDir3", "jobOutputDir3", "workDir3", "scratchDir3", "effUser3", tags, notes, "fakePassword3"};
  private static final String[] sys4 = {tenantName, "Csys4", "description 4", "owner4", "host4", "bucket4", "/root4",
    "jobInputDir4", "jobOutputDir4", "workDir4", "scratchDir4", "effUser4", tags, notes, "fakePassword4"};
  private static final String[] sys5 = {tenantName, "Csys5", "description 5", "owner5", "host5", "bucket5", "/root5",
    "jobInputDir5", "jobOutputDir5", "workDir5", "scratchDir5", "effUser5", tags, notes, "fakePassword5"};
  private static final String[] sys6 = {tenantName, "Csys6", "description 6", "owner6", "host6", "bucket6", "/root6",
    "jobInputDir6", "jobOutputDir6", "workDir6", "scratchDir6", "effUser6", tags, notes, "fakePassword6"};
  private static final String[] sys7 = {tenantName, "Csys7", "description 7", "owner7", "host7", "bucket7", "/root7",
          "jobInputDir7", "jobOutputDir7", "workDir7", "scratchDir7", "effUser7", tags, notes, "fakePassword7"};
  private static final String[] sys8 = {tenantName, "Csys8", "description 8", "owner8", "host8", "bucket8", "/root8",
          "jobInputDir8", "jobOutputDir8", "workDir8", "scratchDir8", "effUser8", tags, notes, "fakePassword8"};

  private SystemsClient sysClient;

  @BeforeSuite
  public void setUp() throws Exception
  {
    // Get the base URLs from the environment so the test can be used in environments other than dev
    System.out.println("Executing BeforeSuite setup method");
    // Get token using URL from env or from default
    String tokensURL = System.getenv(TAPIS_ENV_SVC_URL_TOKENS);
    if (StringUtils.isBlank(tokensURL)) tokensURL = DEFAULT_BASE_URL_TOKENS;
    // Get short term JWT from tokens service
    var tokClient = new TokensClient(tokensURL);
    String svcJWT;
    try {svcJWT = tokClient.getSvcToken(tenantName, SERVICE_NAME_SYSTEMS);}
    catch (Exception e) {throw new Exception("Exception from Tokens service", e);}
    System.out.println("Got svcJWT: " + svcJWT);
    // Basic check of JWT
    if (StringUtils.isBlank(svcJWT)) throw new Exception("Token service returned invalid JWT");
    // Create the client
    // Check for URL set as env var
    String systemsURL = System.getenv(TAPIS_ENV_SVC_URL_SYSTEMS);
    if (StringUtils.isBlank(systemsURL)) systemsURL = DEFAULT_BASE_URL_SYSTEMS;
    sysClient = new SystemsClient(systemsURL, svcJWT);
  }

  @Test
  public void testCreateSystem()
  {
    // Create a system
    System.out.println("Creating system with name: " + sys1[1]);
    try
    {
      String respUrl = createSystem(sys1);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e.getMessage() + "\n Stack trace: " + e.getStackTrace());
      Assert.fail();
    }
  }

  @Test(expectedExceptions = {TapisClientException.class}, expectedExceptionsMessageRegExp = "^SYSAPI_SYS_EXISTS.*")
  public void testCreateSystemAlreadyExists() throws Exception
  {
    // Create a system
    System.out.println("Creating system with name: " + sys7[1]);
    try
    {
      String respUrl = createSystem(sys7);
      System.out.println("Created system: " + respUrl);
      Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    } catch (Exception e) {
      System.out.println("Caught exception: " + e.getMessage() + "\n Stack trace: " + e.getStackTrace());
      Assert.fail();
    }
    // Now attempt to create it again, should throw exception
    System.out.println("Creating system with name: " + sys7[1]);
    createSystem(sys7);
    Assert.fail("Exception should have been thrown");
  }

  @Test
  public void testGetSystemByName() throws Exception
  {
    String[] sys0 = sys2;
    String respUrl = createSystem(sys0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    TSystem tmpSys = sysClient.getSystemByName(sys0[1], false);
    Assert.assertNotNull(tmpSys, "Failed to create item: " + sys0[1]);
    System.out.println("Found item: " + sys0[1]);
    Assert.assertEquals(tmpSys.getName(), sys0[1]);
    Assert.assertEquals(tmpSys.getDescription(), sys0[2]);
    Assert.assertEquals(tmpSys.getOwner(), sys0[3]);
    Assert.assertEquals(tmpSys.getHost(), sys0[4]);
    Assert.assertEquals(tmpSys.getBucketName(), sys0[5]);
    Assert.assertEquals(tmpSys.getRootDir(), sys0[6]);
    Assert.assertEquals(tmpSys.getJobInputDir(), sys0[7]);
    Assert.assertEquals(tmpSys.getJobOutputDir(), sys0[8]);
    Assert.assertEquals(tmpSys.getWorkDir(), sys0[9]);
    Assert.assertEquals(tmpSys.getScratchDir(), sys0[10]);
    Assert.assertEquals(tmpSys.getEffectiveUserId(), sys0[11]);
    System.out.println("Found tags: " + tmpSys.getTags());
    System.out.println("Found notes: " + tmpSys.getNotes());
    Assert.assertEquals(tmpSys.getAccessMechanism(), prot1AccessMechanism);
    Assert.assertEquals(tmpSys.getPort().intValue(), prot1Port);
    Assert.assertEquals(tmpSys.getUseProxy().booleanValue(), prot1UseProxy);
    Assert.assertEquals(tmpSys.getProxyHost(), prot1ProxyHost);
    Assert.assertEquals(tmpSys.getProxyPort().intValue(), prot1ProxyPort);
    List<TransferMechanismsEnum> tmechsList = tmpSys.getTransferMechanisms();
    Assert.assertNotNull(tmechsList);
    Assert.assertTrue(tmechsList.contains(TransferMechanismsEnum.S3), "List of transfer mechanisms did not contain: " + TransferMechanismsEnum.S3.name());
    Assert.assertTrue(tmechsList.contains(TransferMechanismsEnum.SFTP), "List of transfer mechanisms did not contain: " + TransferMechanismsEnum.SFTP.name());
  }

  @Test
  public void testGetSystemNames() throws Exception
  {
    // Create 2 systems
    String[] sys0 = sys3;
    String respUrl = createSystem(sys0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
    sys0 = sys4;
    respUrl = createSystem(sys0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Get list of all system names
    List<String> systemNames = sysClient.getSystemNames();
    for (String name : systemNames) {
      System.out.println("Found item: " + name);
    }
    Assert.assertTrue(systemNames.contains(sys3[1]), "List of systems did not contain system name: " + sys3[1]);
    Assert.assertTrue(systemNames.contains(sys4[1]), "List of systems did not contain system name: " + sys4[1]);
  }

//  @Test
//  public void testGetSystems() throws Exception
//  {
//    String respUrl = createSystem(sys5);
//    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);
//    List<TSystem> systems = sysClient.getSystems();
//    for (TSystem system : systems) {
//      System.out.println("Found item with id: " + system.getId() + " and name: " + system.getName());
//    }
//  }

  @Test
  public void testDelete() throws Exception
  {
    // Create the system
    String[] sys0 = sys6;
    String respUrl = createSystem(sys0);
    Assert.assertFalse(StringUtils.isBlank(respUrl), "Invalid response: " + respUrl);

    // Delete the system
    sysClient.deleteSystemByName(sys0[1]);
    try
    {
      TSystem tmpSys2 = sysClient.getSystemByName(sys0[1], false);
      Assert.fail("System not deleted. System name: " + sys0[1]);
    } catch (TapisClientException e)
    {
      Assert.assertEquals(e.getCode(), 404);
    }
  }

  @AfterSuite
  public void tearDown()
  {
    System.out.println("Executing AfterSuite teardown method");
    //Remove all objects created by tests, ignore any exceptions
    try { sysClient.deleteSystemByName(sys1[1]); } catch (Exception e) {}
    try { sysClient.deleteSystemByName(sys2[1]); } catch (Exception e) {}
    try { sysClient.deleteSystemByName(sys3[1]); } catch (Exception e) {}
    try { sysClient.deleteSystemByName(sys4[1]); } catch (Exception e) {}
    try { sysClient.deleteSystemByName(sys5[1]); } catch (Exception e) {}
    try { sysClient.deleteSystemByName(sys6[1]); } catch (Exception e) {}
    try { sysClient.deleteSystemByName(sys7[1]); } catch (Exception e) {}
  }

  private String createSystem(String[] sys) throws TapisClientException
  {
    // Convert list of TransferMechanism enums to list of strings
    List<String> transferMechs = Stream.of(prot1TxfrMechs).map(TransferMechanismsEnum::name).collect(Collectors.toList());
    // Create the system
    return sysClient.createSystem(sys[1], sys[2], sys[3], sys[4], true, sys[5], sys[6],
                            sys[7], sys[8], sys[9], sys[10], sys[11], sys[12], sys[13],
                            sys[14], prot1AccessMechanism.name(), transferMechs,
                            prot1Port, prot1UseProxy, prot1ProxyHost, prot1ProxyPort);
  }
}
