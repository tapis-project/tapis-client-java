package edu.utexas.tacc.tapis.globusproxy.client;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ITapisClient;

import edu.utexas.tacc.tapis.globusproxy.client.gen.model.TransferItem;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.V3GlobusProxyTransfersClientIdTransferItems;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.InlineObject;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.ResultGlobusAuthInfo;
import edu.utexas.tacc.tapis.globusproxy.client.gen.ApiClient;
import edu.utexas.tacc.tapis.globusproxy.client.gen.ApiException;
import edu.utexas.tacc.tapis.globusproxy.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.globusproxy.client.gen.api.AuthApi;
import edu.utexas.tacc.tapis.globusproxy.client.gen.api.FileOperationsApi;
import edu.utexas.tacc.tapis.globusproxy.client.gen.api.TransfersApi;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.AuthTokens;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.GlobusFileInfo;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.ReqCreateTransfer;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.ReqMakeDir;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.ReqRename;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.ResultCancelTask;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.GlobusTransferTask;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.InlineObject1;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.InlineObject2;
import edu.utexas.tacc.tapis.globusproxy.client.gen.model.InlineResponse2004Result;

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
    String retVal = null;
    // Submit the request
    try
    {
      var resp = generalApi.healthCheck();
      // If response came back null return null
      if (resp == null) return null;
      retVal = (String) resp.getResult();
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    return retVal;
  }

  // -----------------------------------------------------------------------
  // ------------------------- Auth -------------------------------
  // -----------------------------------------------------------------------
  /**
   * Return auth URL that can be used to obtain a Globus Native App Authorization Code
   *
   * @param clientId Id of the Globus client
   * @return The authorization URL
   * @throws TapisClientException - If api call throws an exception
   */
  public ResultGlobusAuthInfo getAuthInfo(String clientId) throws TapisClientException
  {
    if (StringUtils.isBlank(clientId)) return null;

    ResultGlobusAuthInfo authInfo = new ResultGlobusAuthInfo();
    // Submit the request
    try
    {
      var resp = authApi.getAuthInfo(clientId);
      // If response came back null return null
      if (resp == null || resp.getResult() == null) return null;
      // Marshal only the result from the response.
      var result = resp.getResult();
      if (result == null) return null;
      authInfo.setUrl(result.getUrl());
      authInfo.setSessionId(result.getSessionId());
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return authInfo;
  }

  /**
   * Exchange auth code for access and refresh token pair
   *
   * @param authCode - Globus Native App Authorization Code
   * @return tokens
   * @throws TapisClientException - If api call throws an exception
   */
  public AuthTokens getTokens(String clientId, String sessionId, String authCode) throws TapisClientException
  {
    if (StringUtils.isBlank(authCode)) return null;

    AuthTokens authTokens = new AuthTokens();
    // Submit the request
    try
    {
      var resp = authApi.getTokens(clientId, sessionId, authCode);
      // If response came back null return null
      if (resp == null || resp.getResult() == null) return null;
      var result = resp.getResult();
      authTokens.setAccessToken(result.getAccessToken());
      authTokens.setRefreshToken(result.getRefreshToken());
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return authTokens;
  }

  /**
   * Check token pair and refresh as needed.
   *
   * @param clientId Id of the client
   * @param endpointId - Id of endpoint
   * @param accessToken - globus access token
   * @param refreshToken - globus refresh token
   * @return tokens
   * @throws TapisClientException - If api call throws an exception
   */
  public AuthTokens checkTokens(String clientId, String endpointId, String accessToken, String refreshToken)
          throws TapisClientException
  {
    if (StringUtils.isBlank(endpointId) || StringUtils.isBlank(accessToken) || StringUtils.isBlank(refreshToken))
      return null;

    AuthTokens authTokens = new AuthTokens();
    try
    {
      var resp = authApi.checkTokens(clientId, endpointId, accessToken, refreshToken);
      if (resp == null || resp.getResult() == null) return null;
      var result = resp.getResult();
      authTokens.setAccessToken(result.getAccessToken());
      authTokens.setRefreshToken(result.getRefreshToken());
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return authTokens;
  }

  // -----------------------------------------------------------------------
  // ------------------------- File Operations -----------------------------
  // -----------------------------------------------------------------------
  /**
   * Get list of files for an endpoint
   *
   * @param clientId Id of client
   * @param endpointId - Id of endpoint
   * @param accessToken - globus access token
   * @return list of files
   * @throws TapisClientException - If api call throws an exception
   */
  public List<GlobusFileInfo> listFiles(String clientId, String endpointId, String accessToken, String refreshToken,
                                        String path, int limit, int offset, String filter)
          throws TapisClientException
  {
    if (StringUtils.isBlank(endpointId) || StringUtils.isBlank(accessToken)) return null;

    ArrayList<GlobusFileInfo> fileInfoList =  new ArrayList<>();
    try
    {
      var resp = operationsApi.listFiles(clientId, endpointId, path, accessToken, refreshToken, limit, offset, filter);
      // Make sure we are non-null down to the data
      if (resp == null || resp.getResult() == null || resp.getResult().getDATA() == null) return fileInfoList;

      // So we should have a list of file info objects
      var resultList = resp.getResult().getDATA();
      for (int i = 0; i < resultList.size(); i++)
      {
        var fiRaw = resultList.get(i);
        GlobusFileInfo fi = new GlobusFileInfo();
        fi.setType(fiRaw.getType());
        fi.setUser(fiRaw.getUser());
        fi.setGroup(fiRaw.getGroup());
        fi.setName(fiRaw.getName());
        fi.setPath(fiRaw.getPath());
        fi.setSize(fiRaw.getSize());
        fi.setLastModified(fiRaw.getLastModified());
        fi.setPermissions(fiRaw.getPermissions());
        fileInfoList.add(fi);
      }
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return fileInfoList;
  }

  /**
   * Delete a path
   *
   * @param clientId Id of client
   * @param endpointId - Id of endpoint
   * @param accessToken - globus access token
   * @return status
   * @throws TapisClientException - If api call throws an exception
   */
  public String deletePath(String clientId, String endpointId, String accessToken, String refreshToken, String path, boolean recurse)
          throws TapisClientException
  {
    if (StringUtils.isBlank(endpointId) || StringUtils.isBlank(accessToken)) return null;

    String retVal = null;
    try
    {
      var resp = operationsApi.deletePath(clientId, endpointId, path, accessToken, refreshToken, recurse);
      if (resp == null) return null;
      retVal = resp.getStatus();
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return retVal;
  }

  /**
   * Create a directory
   *
   * @param clientId Id of client
   * @param endpointId - Id of endpoint
   * @param accessToken - globus access token
   * @param refreshToken - globus refresh token
   * @param path - path to create
   * @return status
   * @throws TapisClientException - If api call throws an exception
   */
  public String makeDir(String clientId, String endpointId, String accessToken, String refreshToken, String path)
          throws TapisClientException
  {
    if (StringUtils.isBlank(endpointId) || StringUtils.isBlank(accessToken) || StringUtils.isBlank(path)) return null;

    String retVal = null;
    // NOTE: IF openapi spec changes this class name may change. Not clear how to make code more robust and
    //       not depend on the generated class names.
//    var makeDirPath = new ReqMakeDir();
//    var makeDirPath = new InlineObject1();
//    makeDirPath.setPath(path);
    // TODO/TBD: Looks like globusProxy endpoint expects the path to be a path parameter. But the POST also seems to
    //           require a body. So currently we require a request body but ignore it.
    try
    {
      var resp = operationsApi.makeDir(clientId, endpointId, path, accessToken, refreshToken, null);
      if (resp == null || resp.getResult() == null) return null;
      retVal = resp.getStatus();
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return retVal;
  }

  /**
   * Rename a path
   *
   * @param clientId Id of client
   * @param endpointId - Id of endpoint
   * @param accessToken - globus access token
   * @param refreshToken - globus refresh token
   * @param srcPath - path to rename
   * @param destPath - new path name
   * @return status
   * @throws TapisClientException - If api call throws an exception
   */
  public String renamePath(String clientId, String endpointId, String srcPath, String destPath,
                           String accessToken, String refreshToken)
          throws TapisClientException
  {
    if (StringUtils.isBlank(endpointId) || StringUtils.isBlank(accessToken) || StringUtils.isBlank(srcPath)
            || StringUtils.isBlank(destPath)) return null;

    String retVal = null;
    // NOTE: IF openapi spec changes this class name may change. Not clear how to make code more robust and
    //       not depend on the generated class names.
    InlineObject reqBody = new InlineObject();
    reqBody.setDestination(destPath);
    try
    {
      var resp = operationsApi.renamePath(clientId, endpointId, srcPath, accessToken, refreshToken, reqBody);
      if (resp == null || resp.getResult() == null) return null;
      retVal = resp.getStatus();
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return retVal;
  }

  // -----------------------------------------------------------------------
  // --------------------------- Transfers -------------------------------
  // -----------------------------------------------------------------------
  /**
   * Create a transfer task
   *
   * @param clientId Id of client
   * @param accessToken - globus access token
   * @param refreshToken - globus refresh token
   * @return transfer task
   * @throws TapisClientException - If api call throws an exception
   */
  public GlobusTransferTask createTransferTask(String clientId, String accessToken, String refreshToken,
                                         ReqCreateTransfer reqCreateTransfer)
          throws TapisClientException
  {
    if (StringUtils.isBlank(clientId) || StringUtils.isBlank(accessToken) || reqCreateTransfer == null) return null;

    GlobusTransferTask transferTask = new GlobusTransferTask();
    // NOTE: IF openapi spec changes this class name may change. Not clear how to make code more robust and
    //       not depend on the generated class names.

    // Create the request object required by the API call.
    var reqCreateTransferApiObj = new InlineObject2();

    // Add all transfer items to the request
    var txfrItems = reqCreateTransferApiObj.getTransferItems();
    txfrItems.addAll(reqCreateTransfer.getTransferItems());
    reqCreateTransferApiObj.setTransferItems(txfrItems);
    // Set source and destination endpoints
    reqCreateTransferApiObj.setDestinationEndpoint(reqCreateTransfer.getDestinationEndpoint());
    reqCreateTransferApiObj.setSourceEndpoint(reqCreateTransfer.getSourceEndpoint());
    try
    {
      var resp = transfersApi.createTransferTask(clientId, accessToken, refreshToken, reqCreateTransferApiObj);
      if (resp == null || resp.getResult() == null) return null;
      var r = resp.getResult();
      // Create transfer task from result.
      transferTask = buildTransferTask(r);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return transferTask;
  }

  /**
   * Get a transfer task
   *
   * @param clientId Id of client
   * @param accessToken - globus access token
   * @param refreshToken - globus refresh token
   * @param taskId task id
   * @return transfer task
   * @throws TapisClientException - If api call throws an exception
   */
  public GlobusTransferTask getTransferTask(String clientId, String accessToken, String refreshToken, String taskId)
          throws TapisClientException
  {
    if (StringUtils.isBlank(clientId) || StringUtils.isBlank(accessToken) || StringUtils.isBlank(taskId)) return null;

    GlobusTransferTask transferTask = null;

    try
    {
      // NOTE: IF openapi spec changes this class name may change. Not clear how to make code more robust and
      //       not depend on the generated class names.
      var resp = transfersApi.getTransferTask(clientId, taskId, accessToken, refreshToken);
      if (resp == null || resp.getResult() == null) return null;
      var r = resp.getResult();

      // Create transfer task from result.
      transferTask = buildTransferTask(r);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return transferTask;
  }

  /**
   * Request to cancel a transfer task
   *
   * @param clientId Id of client
   * @param accessToken - globus access token
   * @param taskId task id
   * @return cancel result
   * @throws TapisClientException - If api call throws an exception
   */
  public ResultCancelTask cancelTransferTask(String clientId, String taskId, String accessToken, String refreshToken)
          throws TapisClientException
  {
    if (StringUtils.isBlank(clientId) || StringUtils.isBlank(accessToken) || StringUtils.isBlank(taskId)) return null;

    ResultCancelTask resultCancelTask = new ResultCancelTask();
    try
    {
      // NOTE: IF openapi spec changes this class name may change. Not clear how to make code more robust and
      //       not depend on the generated class names.
      var resp = transfersApi.cancelTransferTask(clientId, taskId, accessToken, refreshToken);
      if (resp == null || resp.getResult() == null) return null;
      var r = resp.getResult();
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return resultCancelTask;
  }

  // -----------------------------------------------------------------------
  // --------------------------- Utility Methods ---------------------------
  // -----------------------------------------------------------------------

  /**
   * Utility method to build a ReqCreateTransfer object.
   * Note that null is returned if any incoming parameters are null or the strings are empty.
   */
  public static ReqCreateTransfer buildReqCreateTransfer(String srcEndpoint, String dstEndpoint,
                                                         List<TransferItem> txfrItems)
  {
    if (StringUtils.isBlank(srcEndpoint) || StringUtils.isBlank(dstEndpoint) || txfrItems == null) return null;
    var rTransfer = new ReqCreateTransfer();
    rTransfer.setSourceEndpoint(srcEndpoint);
    rTransfer.setDestinationEndpoint(dstEndpoint);
    // Build up list of transfer items
    var rTxfrItems = rTransfer.getTransferItems();
    for (TransferItem txfrItem : txfrItems)
    {
      var item = new V3GlobusProxyTransfersClientIdTransferItems();
      item.setSourcePath(txfrItem.getSourcePath());
      item.setDestinationPath(txfrItem.getDestinationPath());
      item.setRecursive(txfrItem.getRecursive());
      rTxfrItems.add(item);
    }

    return rTransfer;
  }

  /**
   * Utility method to build a transfer item that can be part of a ReqCreateTransfer.
   * If item is a directory then recursive must be set to true.
   *
   * Note that null is returned if any incoming parameters are null or the strings are empty.
   */
  public static TransferItem buildTransferItem(String srcPath, String dstPath, boolean recursive)
  {
    if (StringUtils.isBlank(srcPath) || StringUtils.isBlank(dstPath)) return null;
    var txfrItem = new TransferItem();
    txfrItem.setSourcePath(srcPath);
    txfrItem.setDestinationPath(dstPath);
    txfrItem.setRecursive(recursive);
    return txfrItem;
  }

  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************

  /*
   * Create and populate a GlobusTransferTask given the result from a call to transfersApi.getTransferTask()
   *  or transfersApi.createTransferTask()
   * Note that the type for the input parameter is from autogenerated code so could change when the
   *   openapi spec is changed.
   */
  GlobusTransferTask buildTransferTask(InlineResponse2004Result r)
  {
    GlobusTransferTask transferTask = new GlobusTransferTask();
    transferTask.setBytesTransferred(r.getBytesTransferred());
    transferTask.setCompletionTime(r.getCompletionTime());
    transferTask.setDeadline(r.getDeadline());
    transferTask.setDestinationEndpointDisplayName(r.getDestinationEndpointDisplayName());
    transferTask.setDestinationEndpointId(r.getDestinationEndpointId());
    transferTask.setDirectories(r.getDirectories());
    transferTask.setEffectiveBytesPerSecond(r.getEffectiveBytesPerSecond());
    transferTask.setEncryptData(r.getEncryptData());
    transferTask.setFailOnQuotaErrors(r.getFailOnQuotaErrors());
    transferTask.setFatalError(r.getFatalError());
    transferTask.setFaults(r.getFaults());
    transferTask.setFiles(r.getFiles());
    transferTask.setFilesSkipped(r.getFilesSkipped());
    transferTask.setFilesTransferred(r.getFilesTransferred());
    transferTask.setHistoryDeleted(r.getHistoryDeleted());
    transferTask.setIsOk(r.getIsOk());
    transferTask.setIsPaused(r.getIsPaused());
    transferTask.setLabel(r.getLabel());
    transferTask.setOwnerId(r.getOwnerId());
    transferTask.setRequestTime(r.getRequestTime());
    transferTask.setSkipSourceErrors(r.getSkipSourceErrors());
    transferTask.setSourceEndpointDisplayName(r.getSourceEndpointDisplayName());
    transferTask.setSourceEndpointId(r.getSourceEndpointId());
    transferTask.setSymlinks(r.getSymlinks());
    transferTask.setSyncLevel(r.getSyncLevel());
    transferTask.setTaskId(r.getTaskId());
    transferTask.setVerifyChecksum(r.getVerifyChecksum());
    // NOTE that although the generated code has two enum types for both Status and Type,
    //   they come from the same sources defined in the openapi spec. Namely, GlobusTransferTaskStatusEnum
    //   and GlobusTaskTypeEnum defined in the openapi spec. So it should be OK to set one from the other
    String statusStr = r.getStatus() == null ? "" : r.getStatus().name();
    String typeStr = r.getType() == null ? "" : r.getType().name();
    transferTask.setStatus(GlobusTransferTask.StatusEnum.valueOf(statusStr));
    transferTask.setType(GlobusTransferTask.TypeEnum.valueOf(typeStr));
    return transferTask;
  }
}
