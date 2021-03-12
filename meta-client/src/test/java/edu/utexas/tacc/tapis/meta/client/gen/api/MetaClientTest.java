package edu.utexas.tacc.tapis.meta.client.gen.api;

import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.meta.client.MetaClient;
import edu.utexas.tacc.tapis.meta.client.gen.ApiClient;
import edu.utexas.tacc.tapis.meta.client.gen.auth.ApiKeyAuth;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.List;


@Test(groups={"integration"})
public class MetaClientTest {
  private MetaClient metaClient;
  private String dbEtag;
  private String collEtag;
  private String db = "MetaTstDB";
  private String collection = "MetaTstCollection";
  private String document = "metaTstDocument";
  private String tmpDB = "tmpDB";
  private String ifMatch = "";
  private String body = "";
  

  /*------------------------------------------------------------------------
   *  Assumptions for tests  see README in meta-client module
   *    - full metav3 service requires security, core and mongodb
   *    - connectivity to SK and Tenants is required
   *  - Manual requirements before test run
   *    - use meta/admin user for running tests, permissions set in SK.
   *    - presetup of MetaTstDB ( has collections and docs )
   *    - drop tmpDB if exists ( for creation and deletion tests )
   * -----------------------------------------------------------------------*/
  @BeforeSuite
  public void setUp() throws Exception {
    System.out.println("****** Executing BeforeSuite setup method for class: " + this.getClass().getSimpleName());
    
    String basePath = System.getenv("basePath"); // "https://localhost:8080/v3"; //"https://dev.develop.tapis.io/v3"
    String jwt = System.getenv("jwt");
  
    metaClient = new MetaClient(basePath, jwt);
  
    ApiClient apiClient = metaClient.getApiClient();
    ApiKeyAuth TapisJWT = (ApiKeyAuth) apiClient.getAuthentication("TapisJWT");
    TapisJWT.setApiKey(jwt);
    apiClient.addDefaultHeader("X-Tapis-User", "meta");
    apiClient.addDefaultHeader("X-Tapis-Tenant", "admin");
  }
  
  @AfterSuite
  public void tearDown() {
    System.out.println("****** Executing AfterSuite teardown method for class: " + this.getClass().getSimpleName());
    metaClient.close();
  }
  
  /*------------------------------------------------------------------------
   *                              General Resource
   * -----------------------------------------------------------------------*/
  @Test
  public void testHealth() {
    System.out.println("* Checking health status");
    try {
      String response = metaClient.healthcheck();
      System.out.println("Health status: " + response);
      Assert.assertNotNull(response);
      Assert.assertFalse(StringUtils.isBlank(response), "Invalid response: " + response);
      Assert.assertTrue(StringUtils.contains(response,"status=200"),"Success");
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  /*------------------------------------------------------------------------
   *                              Root Resource
   * -----------------------------------------------------------------------*/
  @Test
  public void testListDBNames(){
    System.out.println("* List DB Names");
    List<String> response;
    try {
      response = metaClient.listDBNames();
      System.out.println("List DB Names : " + response);
      Assert.assertNotNull(response);
      Assert.assertFalse(StringUtils.isBlank(response.toString()), "Invalid response: " + response);
      Assert.assertTrue(StringUtils.contains(response.toString(),"MetaTstDB"),"Success");
    } catch (TapisClientException e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  /*------------------------------------------------------------------------
   *                              DB Resource
   * -----------------------------------------------------------------------*/
  @Test(enabled = false)
  public void testCreateDB(){

    System.out.println("Create DB TODO");
    try{ metaClient.createDB(tmpDB); }
    catch (TapisClientException e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  @Test
  public void testListCollectionNames(){
    System.out.println("List Collection Names");
    List<String> response;
    try {
      response = metaClient.listCollectionNames(db);
      System.out.println("List Collection Names for "+db+" : " + response);
      Assert.assertNotNull(response);
      Assert.assertFalse(StringUtils.isBlank(response.toString()), "Invalid response: " + response);
      Assert.assertTrue(StringUtils.contains(response.toString(),"MetaTstCollection"),"Success");
    } catch (TapisClientException e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  @Test(enabled = false)
  public void testDeleteDB(){
    System.out.println("Delete DB TODO");
    // TODO handle deletion
  }
  
  @Test
  public void testGetDBMetadata(){
    System.out.println("Get DB Metadata");
    try {
      Object response = metaClient.getDBMetadata(db);
      System.out.println("Get DB Metadata: " + response);
      Assert.assertNotNull(response);
      Assert.assertFalse(StringUtils.isBlank(response.toString()), "Invalid response: " + response);
      Assert.assertTrue(StringUtils.contains(response.toString(),"_meta"),"Success");
    } catch (TapisClientException e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  /*------------------------------------------------------------------------
   *                              Collection Resource
   * -----------------------------------------------------------------------*/

  @Test(enabled = false)
  public void testCreateCollection(){
    System.out.println("Create Collection");
    try{
      // call returns void
      metaClient.createCollection(tmpDB,collection);
    } catch (TapisClientException e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  @Test(enabled = false)
  public void testDeleteCollection(){
    System.out.println("Create Collection");
    try{
      String ifMatch = "";
      // call returns void
      metaClient.deleteCollection(ifMatch,tmpDB,collection);
    } catch (TapisClientException e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  @Test
  public void testListDocuments() {
    System.out.println("List Documents");
    try {
      List<Object> response = metaClient.listDocuments(db, collection, null, null, null, null, null);
      System.out.println("List Documents: \n" + response);
      Assert.assertNotNull(response);
      Assert.assertFalse(StringUtils.isBlank(response.toString()), "Invalid response: " + response);
      Assert.assertTrue(StringUtils.contains(response.toString(), "metaTstDocument"), "Success");
    } catch (TapisClientException e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  @Test
  public void testGetCollectionMetadata() {
    System.out.println("Get Collection Metadata");
    try{
      Object response = metaClient.getCollectionMetadata(db,collection);
      System.out.println("Get Collection  Metadata: \n" + response);
      Assert.assertNotNull(response);
      Assert.assertFalse(StringUtils.isBlank(response.toString()), "Invalid response: " + response);
      Assert.assertTrue(StringUtils.contains(response.toString(),"_id=_meta"),"Success");
    } catch (TapisClientException e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  @Test
  public void testGetCollectionSize() {
    System.out.println("Get Collection Size");
    try{
      String response = metaClient.getCollectionSize(db,collection);
      System.out.println("Get Collection Size: \n" + response);
      Assert.assertNotNull(response);
      Assert.assertFalse(StringUtils.isBlank(response.toString()), "Invalid response: " + response);
      Assert.assertTrue(StringUtils.contains(response.toString(),"size"),"Success");
    } catch (TapisClientException e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  @Test(enabled = false)
  public void testSubmitLargeQuery() {
    System.out.println("Submit Large Query");
    
    try {
      List<Object> response = metaClient.submitLargeQuery(db, collection, null, null, null, null, null, body);
      System.out.println("List Documents: \n" + response);
      Assert.assertNotNull(response);
      Assert.assertFalse(StringUtils.isBlank(response.toString()), "Invalid response: " + response);
      Assert.assertTrue(StringUtils.contains(response.toString(), "_id"), "Success");
    } catch (TapisClientException e) {
      System.out.println("Caught exception: " + e);
      Assert.fail();
    }
  }
  
  /*------------------------------------------------------------------------
   *                              Document Resource
   * -----------------------------------------------------------------------*/
  //---------------------------- createDocument ----------------------------
  //---------------------------- getDocument -------------------------------
  //---------------------------- replaceDocument ---------------------------
  //---------------------------- modifyDocument ----------------------------
  //---------------------------- deleteDocument ----------------------------
  
  
  /*------------------------------------------------------------------------
   *                              Index Resource
   * -----------------------------------------------------------------------*/
  //---------------------------- deleteIndex ----------------------------
  //---------------------------- listIndexes ----------------------------
  //---------------------------- createIndex ----------------------------

  /*------------------------------------------------------------------------
   *                              Aggregation Resource
   * -----------------------------------------------------------------------*/
  //---------------------------- addAggregation ----------------------------
  //---------------------------- useAggregation ----------------------------
  //---------------------------- submitLargeAggregation ----------------------------
  //---------------------------- deleteAggregation ----------------------------
  
}