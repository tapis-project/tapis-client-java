package edu.utexas.tacc.tapis.meta.client;

import com.google.gson.Gson;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.meta.client.gen.ApiClient;
import edu.utexas.tacc.tapis.meta.client.gen.ApiException;
import edu.utexas.tacc.tapis.meta.client.gen.api.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;


public class MetaClient {
  
  // Header keys for tapis.
  public static final String TAPIS_JWT_HEADER  = "X-Tapis-Token";
  public static final String TAPIS_JWT_TENANT  = "X-Tapis-Tenant";
  public static final String TAPIS_JWT_USER    = "X-Tapis-User";
  public static final String TAPIS_HASH_HEADER = "X-Tapis-User-Token-Hash";
  
  // Configuration defaults.
  private static final String METACLIENT_USER_AGENT = "MetaClient";
  
  // Response body serializer
  private static final Gson gson = ClientTapisGsonUtils.getGson();
  
  private final ApiClient apiClient;
  
  private final GeneralApi generalApi;
  private final RootApi rootApi;
  private final DbApi dbApi;
  private final CollectionApi collectionApi;
  private final DocumentApi documentApi;
  private final IndexApi indexApi;
  private final AggregationApi aggregationApi;
  
  
  /*------------------------------------------------------------------------
   *                              Constructors
   * -----------------------------------------------------------------------*/
  /**
   * Default constructor which uses the compiled-in basePath based on the openapi spec
   *   used to autogenerate the client.
   */
  public MetaClient(){
    apiClient = new ApiClient();
    generalApi = new GeneralApi(apiClient);
    rootApi = new RootApi(apiClient);
    dbApi = new DbApi(apiClient);
    collectionApi = new CollectionApi(apiClient);
    documentApi = new DocumentApi(apiClient);
    indexApi = new IndexApi(apiClient);
    aggregationApi = new AggregationApi(apiClient);
    
  }
  
  /** Constructor that overrides the compiled-in basePath value in ApiClient.  This
   * constructor typically used in production.
   *
   * The jwt is the base64url representation of a Tapis JWT.  If not null or empty,
   * the TAPIS_JWT_HEADER key will be set to the jwt value.
   *
   * The user-agent is automatically set to MetaClient.
   *
   * @param path the base path
   */
  public MetaClient(String path, String jwt)
  {
    apiClient = new ApiClient();
    if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
    if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
    apiClient.setUserAgent(METACLIENT_USER_AGENT);
    generalApi = new GeneralApi(apiClient);
    rootApi = new RootApi(apiClient);
    dbApi = new DbApi(apiClient);
    collectionApi = new CollectionApi(apiClient);
    documentApi = new DocumentApi(apiClient);
    indexApi = new IndexApi(apiClient);
    aggregationApi = new AggregationApi(apiClient);
  }
  
  /*------------------------------------------------------------------------
   *                             Public methods
   * -----------------------------------------------------------------------*/
  // getApiClient: Return underlying ApiClient
  public ApiClient getApiClient() { return apiClient; }
  public GeneralApi getGeneralApi() { return generalApi; }
  public RootApi getRootApi() { return rootApi; }
  public DbApi getDbApi() { return dbApi; }
  
  public void setBasePath(String path) { apiClient.setBasePath(path); }
  
  public void addDefaultHeader(String key, String value) { apiClient.addDefaultHeader(key, value); }
  
  public void setUserAgent(String userAgent) { apiClient.setUserAgent(userAgent); }
  
  /** Set the connection timeout
   * @param millis the connection timeout in milliseconds; 0 means forever.
   * @return this object
   */
  public void setConnectTimeout(int millis) { apiClient.setConnectTimeout(millis); }
  
  /** Set the read timeout
   *
   * @param millis the read timeout in milliseconds; 0 means forever.
   * @return this object
   */
  public void setReadTimeout(int millis) { apiClient.setReadTimeout(millis); }
  
  public void setDebugging(boolean debugging) { apiClient.setDebugging(debugging);}
  
  /** Get the connection timeout.
   *
   * @return the connection timeout in milliseconds
   */
  public int getConnectTimeout()
  {
    return apiClient.getConnectTimeout();
  }
  
  /** Get the read timeout.
   *
   * @return read timeout in milliseconds
   */
  public int getReadTimeout()
  {
    return apiClient.getReadTimeout();
  }
  
  public boolean isDebugging()
  {
    return apiClient.isDebugging();
  }
  
  /** Close connections and stop threads that can sometimes prevent JVM shutdown.
   */
  public void close()
  {
    try {
      // Best effort attempt to shut things down.
      var okClient = apiClient.getHttpClient();
      if (okClient != null) {
        okClient.connectionPool().evictAll();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /*------------------------------------------------------------------------
   *                              Gen
   * -----------------------------------------------------------------------*/
  //----------------------------healthcheck ----------------------------
  public String healthcheck() throws TapisClientException {
    // Submit the request and return the response
    Object resp = null;
    try { resp = generalApi.healthcheck(); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    // Return result value as a string.
    Object obj = resp;
    return obj == null ? null : obj.toString();
  }
  
  /*------------------------------------------------------------------------
   *                              Root Resource
   * -----------------------------------------------------------------------*/
  //---------------------------- listDBNames ----------------------------
  public List<String> listDBNames()  throws TapisClientException {
    List<String> resp = null;
    try { resp = rootApi.listDBNames(); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    // Return result value as a List<String>.
    if (resp == null) return Collections.emptyList();
    return resp;
  }
  
  /*------------------------------------------------------------------------
   *                              DB Resource
   * -----------------------------------------------------------------------*/
  //---------------------------- createDB ----------------------------
  public void createDB(String db)  throws TapisClientException {
    // TODO currently this endpoint is quiet unless there is an exception. We should return a basic response.
    try { dbApi.createDB(db);}
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  
  //---------------------------- listCollectionNames ----------------------------
  public List<String> listCollectionNames(String db) throws TapisClientException {
    List<String> resp = null;
    try { resp = dbApi.listCollectionNames(db); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    // Return result value as a List<String>.
    if (resp == null) return Collections.emptyList();
    return resp;
  }
  
  //---------------------------- deleteDB ----------------------------
  public void deleteDB(String ifMatch, String db) throws TapisClientException {
    // TODO again endpoint is quiet need a basic response
    try { dbApi.deleteDB(ifMatch, db);}
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  
  //---------------------------- getDBMetadata ----------------------------
  public Object getDBMetadata(String db) throws TapisClientException {
    Object resp = null;
    try { resp = dbApi.getDBMetadata(db); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  
    // Return result value as a string.
    Object obj = resp;
    return obj == null ? null : obj.toString();
  }
  
  /*------------------------------------------------------------------------
   *                              Collection Resource
   * -----------------------------------------------------------------------*/
  //---------------------------- createCollection ----------------------------
  public void createCollection(String db, String collection) throws TapisClientException {
    // TODO currently this endpoint is quiet unless there is an exception. We should return a basic response.
    try { collectionApi.createCollection(db,collection); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  
  //---------------------------- listDocuments -------------------------------
  public List<Object> listDocuments(String db, String collection, Integer page, Integer pagesize,
                                    Object filter, Object sort, List<String> keys) throws TapisClientException {
    List<Object> resp = null;
    try { resp = collectionApi.listDocuments(db,collection,null,null,null,null,null); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  
    // Return result value as a string.
    return resp == null ? null : resp;
  }
  
  //---------------------------- deleteCollection ----------------------------
  public void deleteCollection(String ifMatch, String db, String collection) throws TapisClientException {
    // TODO currently this endpoint is quiet unless there is an exception. We should return a basic response.
    try { collectionApi.deleteCollection(ifMatch,db,collection); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  
  //---------------------------- getCollectionMetadata ---------------------------
  public Object getCollectionMetadata(String db, String collection) throws TapisClientException {
    Object resp = null;
    try {
      resp = collectionApi.getCollectionMetadata(db,collection);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    
    // Return result value as a string.
    Object obj = resp;
    return obj == null ? null : obj.toString();
  }
  
  //---------------------------- getCollectionSize ----------------------------
  public String getCollectionSize(String db, String collection) throws TapisClientException {
    String resp = null;
    try {
      resp = collectionApi.getCollectionSize(db,collection);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    
    // Return result value as a string.
    return resp == null ? null : resp;
  }
  
  //---------------------------- submitLargeQuery ----------------------------
  public List<Object> submitLargeQuery(String db, String collection, String page,
                                       String pagesize, Object filter, Object sort,
                                       List<String> keys, Object body) throws TapisClientException {
    List<Object> resp = null;
    try { resp = collectionApi.submitLargeQuery(db,collection,page,pagesize,sort,keys,body); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  
    // Return result value as a string.
    return resp == null ? null : resp;
  }
  
  /*------------------------------------------------------------------------
   *                              Document Resource
   * -----------------------------------------------------------------------*/
  //---------------------------- createDocument ----------------------------
  public String createDocument(String db, String collection, Boolean basic, Object body) throws TapisClientException {
    Object resp = null;
    try { resp = documentApi.createDocument(db,collection,basic,body); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  
    // Return result value as a string.
    Object obj = resp;
    return obj == null ? null : obj.toString();
  }
  
  //---------------------------- getDocument -------------------------------
  public Object getDocument(String db, String collection, String docId) throws TapisClientException {
    Object resp = null;
    try { resp = documentApi.getDocument(db,collection,docId); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  
    // Return result value as a string.
    Object obj = resp;
    return obj == null ? null : obj.toString();
  }
  
  //---------------------------- replaceDocument ---------------------------
  public void replaceDocument(String db, String collection, String docId, Object body) throws TapisClientException {
    try { documentApi.replaceDocument(db,collection,docId,body); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  
  //---------------------------- modifyDocument ----------------------------
  public void modifyDocument(String db, String collection, String docId, Boolean np, Object body) throws TapisClientException {
    try { documentApi.modifyDocument(db,collection,docId,np,body); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  //---------------------------- deleteDocument ----------------------------
  public void deleteDocument(String db, String collection, String docId) throws TapisClientException {
    try { documentApi.deleteDocument(db,collection,docId); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  
  /*------------------------------------------------------------------------
   *                              Index Resource
   * -----------------------------------------------------------------------*/
  //---------------------------- createIndex ----------------------------
  public void createIndex(String db, String collection, String indexName, Object body) throws TapisClientException {
    try { indexApi.createIndex(db,collection,indexName,body); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  
  //---------------------------- listIndexes ----------------------------
  public List<Object> listIndexes(String db, String collection) throws TapisClientException {
    List<Object> resp = null;
    try { resp = indexApi.listIndexes(db,collection); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    
    // Return result value as a string.
    return resp == null ? null : resp;
  }
  
  //---------------------------- deleteIndex ----------------------------
  public void deleteIndex(String db, String collection, String indexName) throws TapisClientException {
    try { indexApi.deleteIndex(db,collection,indexName); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  
  /*------------------------------------------------------------------------
   *                              Aggregation Resource
   * -----------------------------------------------------------------------*/
  //---------------------------- addAggregation ----------------------------
  public void addAggregation(String db, String collection, String aggregation, Object body) throws TapisClientException {
    try { aggregationApi.addAggregation(db,collection,aggregation,body); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  
  //---------------------------- deleteAggregation ----------------------------
  public void deleteAggregation(String db, String collection, String aggregation) throws TapisClientException {
    try { aggregationApi.deleteAggregation(db,collection,aggregation); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }
  
  //---------------------------- submitLargeAggregation ----------------------------
  public Object submitLargeAggregation(String db, String collection, String aggregation, Integer page, Integer pagesize, List<String> keys, Object body) throws TapisClientException {
    Object resp = null;
    try { aggregationApi.submitLargeAggregation(db,collection,aggregation,page,pagesize,keys,body); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  
    // Return result value as a string.
    Object obj = resp;
    return obj == null ? null : obj.toString();
  }
  
  //---------------------------- useAggregation ----------------------------
  public void useAggregation(String db, String collection, String aggregation) throws TapisClientException {
    try { aggregationApi.useAggregation(db,collection,aggregation); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  
  
}
