package edu.utexas.tacc.tapis.globusproxy.client;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.ITapisClient;

import edu.utexas.tacc.tapis.globusproxy.client.gen.ApiClient;
import edu.utexas.tacc.tapis.globusproxy.client.gen.ApiException;
import edu.utexas.tacc.tapis.globusproxy.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.globusproxy.client.gen.api.AuthApi;
import edu.utexas.tacc.tapis.globusproxy.client.gen.api.FileOperationsApi;
import edu.utexas.tacc.tapis.globusproxy.client.gen.api.TransfersApi;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.AuthTokens;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.FileInfo;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.ReqCreateTransfer;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.ReqMakeDir;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.ReqRename;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.RespAuthTokens;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.RespCancelTask;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.RespFileList;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.RespTransferTask;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.RespUrl;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.ResultCancelTask;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.TransferTask;

/**
 * Class providing a convenient front-end to the automatically generated client code
 *   for the GlobusProxy Service REST API.
 * Underlying client classes with "gen" in the package name are generated by openapi-generator each time a build is run.
 */
public class GlobusProxyClient implements ITapisClient
{
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************

  // Header key for JWT
  public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";

  // Defaults
//  public static final boolean DEFAULT_STRICT_FILE_INPUTS = false;
//  public static final int DEFAULT_MAX_JOBS = Integer.MAX_VALUE;

  // ************************************************************************
  // *********************** Fields *****************************************
  // ************************************************************************
  // Response body serializer
  private static final Gson gson = ClientTapisGsonUtils.getGson();

  // Instance of the underlying autogenerated client.
  private final ApiClient apiClient;
  private final AuthApi authApi;
  private final FileOperationsApi operationsApi;
  private final TransfersApi transfersApi;
  private final GeneralApi generalApi;

  // ************************************************************************
  // *********************** Constructors ***********************************
  // ************************************************************************

  /**
   * Default constructor which uses the compiled-in basePath based on the openapi spec
   *   used to autogenerate the client.
   */
  public GlobusProxyClient()
  {
    apiClient = new ApiClient();
    authApi = new AuthApi(apiClient);
    operationsApi = new FileOperationsApi(apiClient);
    transfersApi = new TransfersApi(apiClient);
    generalApi = new GeneralApi(apiClient);
  }

  /**
   * Constructor that overrides the compiled-in basePath value in ApiClient.
   * The path should include the URL prefix up to and including the service root.
   * In production environments the protocol should be https and the host/port will
   * be specific to that environment.
   *
   * @param path the base path URL prefix up to and including the service root
   * @param jwt the token to set in an HTTP header
   */
  public GlobusProxyClient(String path, String jwt)
  {
    apiClient = new ApiClient();
    if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
    if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
    authApi = new AuthApi(apiClient);
    operationsApi = new FileOperationsApi(apiClient);
    transfersApi = new TransfersApi(apiClient);
    generalApi = new GeneralApi(apiClient);
  }

  // ************************************************************************
  // *********************** Public Methods *********************************
  // ************************************************************************

  // getApiClient: Return underlying ApiClient
  public ApiClient getApiClient() { return apiClient; }

  // Update base path for default client.
  public String getBasePath() { return apiClient.getBasePath(); }

  // Update base path for default client.
  public GlobusProxyClient setBasePath(String basePath) { apiClient.setBasePath(basePath); return this; }

  // Add http header to default client
  public GlobusProxyClient addDefaultHeader(String key, String val) {apiClient.addDefaultHeader(key,val);return this;}

  /**
   *  Close connections and stop threads that can sometimes prevent JVM shutdown.
   */
  public void close()
  {
    try {
      // Best effort attempt to shut things down.
      var okClient = apiClient.getHttpClient();
      if (okClient != null)
      {
        var pool = okClient.connectionPool();
        if (pool != null) pool.evictAll();
      }
    } catch (Exception e) {}
  }

  // -----------------------------------------------------------------------
  // ------------------------- General -------------------------------
  // -----------------------------------------------------------------------

  /**
   * Check service health status
   *
   * @return Service health status as a string
   * @throws TapisClientException - If api call throws an exception
   */
  public String checkHealth() throws TapisClientException
  {
    String result = null;
    // Submit the request
    Map resp = null;
    try { resp = (Map) generalApi.healthCheck(); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    // If response came back null return null
    if (resp == null) return result;

    // Marshal only the result from the map.
    result = gson.toJson(resp.get("result"));
    return result;
  }

  // -----------------------------------------------------------------------
  // ------------------------- Auth -------------------------------
  // -----------------------------------------------------------------------
  /**
   * Return auth URL that can be used to obtain a Globus Native App Authorization Code
   *
   * @param clientId Id of the client
   * @return The authorization URL
   * @throws TapisClientException - If api call throws an exception
   */
  public String getAuthUrl(String clientId) throws TapisClientException
  {
    String result = null;
    // Submit the request
    Map resp = null;
    try { resp = (Map) authApi.getAuthUrl(clientId); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    // If response came back null return null
    if (resp == null) return result;

    // Marshal only the result from the map.
    String json = gson.toJson(resp.get("result"));
    // If no result return null
    if (StringUtils.isBlank(json)) return result;

    // Get the result as a string.
    JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
    result = jsonObj.get("url").getAsString();
    return result;
  }

  /**
   * Exchange auth code for access and refresh token pair
   *
   * @param authCode - Globus Native App Authorization Code
   * @return tokens
   * @throws TapisClientException - If api call throws an exception
   */
  public AuthTokens getTokens(String authCode) throws TapisClientException
  {
    AuthTokens result = null;
    // Submit the request
    Map resp = null;
    try { resp = (Map) authApi.getTokens(authCode); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    // If response came back null return null
    if (resp == null) return result;

    // Marshal only the result from the map.
    String json = gson.toJson(resp.get("result"));
    // If no result return null
    if (StringUtils.isBlank(json)) return result;

    // Get the access and refresh tokens from the result
    JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
    String accessToken = jsonObj.get("access_token").getAsString();
    String refreshToken = jsonObj.get("refresh_token").getAsString();
    result = new AuthTokens();
    result.setAccessToken(accessToken);
    result.setRefreshToken(refreshToken);
    return result;
  }

//  /**
//   * Check token pair and refresh as needed.
//   *
//   * @param endpointId - Id of endpoint
//   * @param accessToken - globus access token
//   * @param refreshToken - globus refresh token
//   * @return tokens
//   * @throws TapisClientException - If api call throws an exception
//   */
//  public AuthTokens checkTokens(String endpointId, String accessToken, String refreshToken)
//          throws TapisClientException
//  {
//    return null;
////    RespAuthTokens resp = null;
////    try {resp = authApi.checkTokens(endpointId, accessToken, refreshToken); }
////    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
////    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
////    if (resp == null || resp.getResult() == null) return null;
////    return resp.getResult();
//  }
//
//  // -----------------------------------------------------------------------
//  // ------------------------- File Operations -----------------------------
//  // -----------------------------------------------------------------------
//  /**
//   * Get list files for an endpoint
//   *
//   * @param endpointId - Id of endpoint
//   * @param accessToken - globus access token
//   * @return list of files
//   * @throws TapisClientException - If api call throws an exception
//   */
//  public List<FileInfo> listFiles(String endpointId, String accessToken, String path, boolean recurse)
//          throws TapisClientException
//  {
//    return null;
////    RespFileList resp = null;
////    try { resp = operationsApi.listFiles(endpointId, path, accessToken, recurse); }
////    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
////    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
////    if (resp == null || resp.getResult() == null) return null;
////    return resp.getResult();
//  }
//
//  /**
//   * Delete a path
//   *
//   * @param endpointId - Id of endpoint
//   * @param accessToken - globus access token
//   * @return status
//   * @throws TapisClientException - If api call throws an exception
//   */
//  public String deletePath(String endpointId, String accessToken, String path, boolean recurse)
//          throws TapisClientException
//  {
//    return null;
////    RespBasic resp = null;
////    try { resp = operationsApi.deletePath(endpointId, path, accessToken, recurse); }
////    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
////    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
////    if (resp == null || resp.getResult() == null) return null;
////    return resp.getStatus();
//  }
//
//  /**
//   * Create a directory
//   *
//   * @param endpointId - Id of endpoint
//   * @param accessToken - globus access token
//   * @return status
//   * @throws TapisClientException - If api call throws an exception
//   */
//  public String makeDir(String endpointId, String accessToken, ReqMakeDir reqMakeDir) throws TapisClientException
//  {
//    return null;
////    RespBasic resp = null;
////    try { resp = operationsApi.makeDir(endpointId, accessToken, reqMakeDir); }
////    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
////    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
////    if (resp == null || resp.getResult() == null) return null;
////    return resp.getStatus();
//  }
//
//  /**
//   * Rename a path
//   *
//   * @param endpointId - Id of endpoint
//   * @param accessToken - globus access token
//   * @return status
//   * @throws TapisClientException - If api call throws an exception
//   */
//  public String renamePath(String endpointId, String accessToken, ReqRename reqRename) throws TapisClientException
//  {
//    return null;
////    RespBasic resp = null;
////    try { resp = operationsApi.renamePath(endpointId, accessToken, reqRename); }
////    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
////    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
////    if (resp == null || resp.getResult() == null) return null;
////    return resp.getStatus();
//  }
//
//  // -----------------------------------------------------------------------
//  // --------------------------- Transfers -------------------------------
//  // -----------------------------------------------------------------------
//  /**
//   * Create a transfer task
//   *
//   * @param accessToken - globus access token
//   * @return transfer task
//   * @throws TapisClientException - If api call throws an exception
//   */
//  public TransferTask createTransferTask(String accessToken, ReqCreateTransfer reqCreateTransfer)
//          throws TapisClientException
//  {
//    return null;
////    RespTransferTask resp = null;
////    try { resp = transfersApi.createTransferTask(accessToken, reqCreateTransfer); }
////    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
////    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
////    if (resp == null || resp.getResult() == null) return null;
////    return resp.getResult();
//  }
//
//  /**
//   * Get a transfer task
//   *
//   * @param accessToken - globus access token
//   * @param taskId task id
//   * @return transfer task
//   * @throws TapisClientException - If api call throws an exception
//   */
//  public TransferTask getTransferTask(String accessToken, String taskId) throws TapisClientException
//  {
//    return null;
////    RespTransferTask resp = null;
////    try { resp = transfersApi.getTransferTask(taskId, accessToken); }
////    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
////    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
////    if (resp == null || resp.getResult() == null) return null;
////    return resp.getResult();
//  }
//
//  /**
//   * Request to cancel a transfer task
//   *
//   * @param accessToken - globus access token
//   * @param taskId task id
//   * @return transfer task
//   * @throws TapisClientException - If api call throws an exception
//   */
//  public ResultCancelTask cancelTransferTask(String accessToken, String taskId) throws TapisClientException
//  {
//    return null;
////    RespCancelTask resp = null;
////    try { resp = transfersApi.cancelTransferTask(taskId, accessToken); }
////    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
////    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
////    if (resp == null || resp.getResult() == null) return null;
////    return resp.getResult();
//  }
//
  // -----------------------------------------------------------------------
  // --------------------------- Utility Methods ---------------------------
  // -----------------------------------------------------------------------
//  /**
//   * Utility method to build a ReqCreateTransfer object.
//   */
//  public static ReqCreateTransfer buildReqCreateTransfer()
//  {
//    if (subscription == null) return null;
//    ReqPostSubscription rSubscription = new ReqPostSubscription();
//    rSubscription.id(subscription.getId());
//    rSubscription.description(subscription.getDescription());
//    rSubscription.owner(subscription.getOwner());
//    rSubscription.enabled(subscription.getEnabled());
//    rSubscription.typeFilter(subscription.getTypeFilter());
//    rSubscription.subjectFilter(subscription.getSubjectFilter());
//    rSubscription.deliveryMethods(subscription.getDeliveryMethods());
//    // Notes requires special handling. It must be null or a JsonObject
//    Object notes = subscription.getNotes();
//    if (notes == null) rSubscription.notes(null);
//    else if (notes instanceof String) rSubscription.notes(ClientTapisGsonUtils.getGson().fromJson((String) notes, JsonObject.class));
//    else if (notes instanceof JsonObject) rSubscription.notes(notes);
//    else rSubscription.notes(null);
//    return rSubscription;
//  }
//  /**
//   * Utility method to build a ReqPostEvent object.
//   */
//  public static ReqPostEvent buildReqPostEvent(String source, String type, String subject, OffsetDateTime timestamp)
//  {
//    // If any required attributes null then return null.
//    if (StringUtils.isBlank(source) || StringUtils.isBlank(type) || timestamp == null) return null;
//    ReqPostEvent rEvent = new ReqPostEvent();
//    rEvent.source(source);
//    rEvent.type(type);
//    rEvent.subject(subject);
//    rEvent.time(timestamp);
//    return rEvent;
//  }

  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************
}
