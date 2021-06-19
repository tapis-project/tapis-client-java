package edu.utexas.tacc.tapis.systems.client;

import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_COMPUTETOTAL;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_LIMIT;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_ORDERBY;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SEARCH;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SELECT_ALL;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SELECT_SUMMARY;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SKIP;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_STARTAFTER;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.ITapisClient;
import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.systems.client.gen.ApiClient;
import edu.utexas.tacc.tapis.systems.client.gen.ApiException;
import edu.utexas.tacc.tapis.systems.client.gen.api.CredentialsApi;
import edu.utexas.tacc.tapis.systems.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.systems.client.gen.api.PermissionsApi;
import edu.utexas.tacc.tapis.systems.client.gen.api.SystemsApi;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability;
import edu.utexas.tacc.tapis.systems.client.gen.model.CategoryEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;
import edu.utexas.tacc.tapis.systems.client.gen.model.DatatypeEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.LogicalQueue;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqMatchConstraints;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqPerms;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqPutSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqSearchSystems;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqPatchSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespBoolean;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespChangeCount;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespCredential;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespNameArray;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespResourceUrl;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespSystems;
import edu.utexas.tacc.tapis.systems.client.gen.model.TapisSystem;

/**
 * Class providing a convenient front-end to the automatically generated client code
 * for the Systems Service REST API.
 * Underlying client classes with "gen" in the package name are generated by
 * openapi-generator each time a build is run.
 */
public class SystemsClient implements ITapisClient
{
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************

  // Header key for JWT
  public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";

  // ************************************************************************
  // *********************** Enums ******************************************
  // ************************************************************************
  // Define AuthnMethod here to be used in place of the auto-generated model enum
  //   because the auto-generated enum is named DefaultAuthnMethodEnum which is misleading.
  public enum AuthnMethod {PASSWORD, PKI_KEYS, ACCESS_KEY, CERT}

  // ************************************************************************
  // *********************** Fields *****************************************
  // ************************************************************************
  // Instance of the underlying autogenerated client.
  private final ApiClient apiClient;
  private final SystemsApi sysApi;
  private final PermissionsApi permsApi;
  private final CredentialsApi credsApi;
  private final GeneralApi generalApi;

  // ************************************************************************
  // *********************** Constructors ***********************************
  // ************************************************************************

  /**
   * Default constructor which uses the compiled-in basePath based on the openapi spec
   *   used to autogenerate the client.
   */
  public SystemsClient()
  {
    apiClient = new ApiClient();
    sysApi = new SystemsApi(apiClient);
    permsApi = new PermissionsApi(apiClient);
    credsApi = new CredentialsApi(apiClient);
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
  public SystemsClient(String path, String jwt)
  {
    apiClient = new ApiClient();
    if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
    if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
    sysApi = new SystemsApi(apiClient);
    permsApi = new PermissionsApi(apiClient);
    credsApi = new CredentialsApi(apiClient);
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
  public SystemsClient setBasePath(String basePath) { apiClient.setBasePath(basePath); return this;}

  // Add http header to default client
  public SystemsClient addDefaultHeader(String key, String val) { apiClient.addDefaultHeader(key, val); return this;}

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
  // ------------------------- Systems -------------------------------------
  // -----------------------------------------------------------------------

  /**
   * Check service health status
   *
   * @return Service health status as a string
   * @throws TapisClientException - If api call throws an exception
   */
  public String checkHealth() throws TapisClientException
  {
    // Submit the request and return the response
    RespBasic resp = null;
    try { resp = generalApi.healthCheck(); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getStatus(); else return null;
  }

  /**
   * Check service ready status
   *
   * @return Service ready status status as a string
   * @throws TapisClientException - If api call throws an exception
   */
  public String checkReady() throws TapisClientException
  {
    // Submit the request and return the response
    RespBasic resp = null;
    try { resp = generalApi.readyCheck(); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getStatus(); else return null;
  }

  /**
   * Create a system
   *
   * @param req - Pre-populated ReqCreateSystem instance
   * @return url pointing to created resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String createSystem(ReqCreateSystem req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = sysApi.createSystem(req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update selected attributes of a system
   *
   * @param systemId - Id of resource to be updated
   * @param req - Pre-populated ReqPatchSystem instance
   * @return url pointing to updated resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String patchSystem(String systemId, ReqPatchSystem req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = sysApi.patchSystem(systemId, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update all attributes of a system
   * NOTE: Not all attributes are updatable.
   * See the helper method buildReqPutSystem() for an example of how to build a pre-populated
   *   ReqPutSystem instance from a TapisSystem instance.
   *
   * @param systemId - Id of resource to be updated
   * @param req - Pre-populated ReqPutSystem instance
   * @return url pointing to updated resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String putSystem(String systemId, ReqPutSystem req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = sysApi.putSystem(systemId, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update enabled attribute to true.
   *
   * @param id System id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int enableSystem(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = sysApi.enableSystem(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Update enabled attribute to false.
   *
   * @param id System id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int disableSystem(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = sysApi.disableSystem(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Update deleted attribute to true.
   *
   * @param id System id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int deleteSystem(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = sysApi.deleteSystem(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Update deleted to false.
   *
   * @param id System id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int undeleteSystem(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = sysApi.undeleteSystem(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Change system owner given the system systemId and new owner systemId.
   *
   * @param systemId System Id
   * @param newOwnerId User Id of new owner
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int changeSystemOwner(String systemId, String newOwnerId) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = sysApi.changeSystemOwner(systemId, newOwnerId); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Get a system by systemId without returning credentials
   *
   * @param systemId System systemId
   * @return The system or null if system not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSystem getSystem(String systemId) throws TapisClientException
  {
    return getSystem(systemId, false, null, false, DEFAULT_SELECT_ALL);
  }

  /**
   * Get a system using most supported parameters.
   * Fetching of credentials is highly restricted. Only certain Tapis services are authorized.
   * If authnMethod is null then default authn method for the system is used.
   * Use of this method is highly restricted.
   *
   * @param systemId System systemId
   * @param returnCredentials - Include credentials in returned system object
   * @param authnMethod - Desired authn method used when fetching credentials, for default pass in null.
   * @param requireExecPerm Check for EXECUTE permission as well as READ permission
   * @return The system or null if system not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSystem getSystem(String systemId, Boolean returnCredentials, AuthnMethod authnMethod,
                               Boolean requireExecPerm)
          throws TapisClientException
  {
    return getSystem(systemId, returnCredentials, authnMethod, requireExecPerm, DEFAULT_SELECT_ALL);
  }

  /**
   * Get a system by systemId returning credentials for specified authn method.
   * If authnMethod is null then default authn method for the system is used.
   * Use of this method is highly restricted. Only certain Tapis services are
   * authorized to call this method.
   *
   * @param systemId System systemId
   * @param authnMethod - Desired authentication method used when fetching credentials,
   *                      default authentication method used if this is null
   * @return The system or null if system not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSystem getSystemWithCredentials(String systemId, AuthnMethod authnMethod) throws TapisClientException
  {
    return getSystem(systemId, true, authnMethod, false);
  }

  /**
   * Get a system by systemId returning credentials for default authn method.
   * Use of this method is highly restricted. Only certain Tapis services are
   * authorized to call this method.
   *
   * @param systemId System Id
   * @return The system or null if system not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSystem getSystemWithCredentials(String systemId) throws TapisClientException
  {
    return getSystemWithCredentials(systemId, null);
  }

  /**
   * Get a system using all supported parameters.
   * Fetching of credentials is highly restricted. Only certain Tapis services are authorized.
   * If authnMethod is null then default authn method for the system is used.
   * Use of this method is highly restricted.
   *
   * @param systemId System Id
   * @param returnCredentials - Include credentials in returned system object
   * @param authnMethod - Desired authn method used when fetching credentials, for default pass in null.
   * @param requireExecPerm Check for EXECUTE permission as well as READ permission
   * @param selectStr - Attributes to be included in result. For example select=id,owner,host
   * @return The system or null if system not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSystem getSystem(String systemId, Boolean returnCredentials, AuthnMethod authnMethod,
                               Boolean requireExecPerm, String selectStr)
          throws TapisClientException
  {
    String selectStr1 = DEFAULT_SELECT_ALL;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    RespSystem resp = null;
    String authnMethodStr = (authnMethod==null ? null : authnMethod.name());
    try {resp = sysApi.getSystem(systemId, returnCredentials, authnMethodStr, requireExecPerm, selectStr1); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    // Postprocess the TapisSystem
    return postProcessSystem(resp.getResult());
  }

  /**
   * Get list of all systems
   *
   * @return List of all systems available to the caller.
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSystem> getSystems() throws TapisClientException
  {
    return getSystems(DEFAULT_SEARCH);
  }

  /**
   * Get list of systems using search. For example search=(id.like.MySys*)~(enabled.eq.true)
   *
   * @param searchStr list of conditions used for searching
   * @return list of systems available to the caller and matching search conditions.
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSystem> getSystems(String searchStr) throws TapisClientException
  {
    return getSystems(searchStr, DEFAULT_LIMIT, DEFAULT_ORDERBY, DEFAULT_SKIP, DEFAULT_STARTAFTER, DEFAULT_SELECT_SUMMARY, false);
  }

  /**
   * Get list of systems using all supported parameters: searchStr, limit, orderBy, skip, startAfter, select, showDeleted
   * For example search=(id.like.MySys*)~(enabled.eq.true)&limit=10&orderBy=id(asc)&startAfter=my.sys1
   * Use only one of skip or startAfter
   * When using startAfter orderBy must be specified.
   *
   * @param searchStr list of conditions used for searching
   * @param limit
   * @param orderBy
   * @param skip
   * @param startAfter
   * @param selectStr
   * @param showDeleted
   * @return list of systems available to the caller and matching search conditions.
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSystem> getSystems(String searchStr, int limit, String orderBy, int skip, String startAfter,
                                      String selectStr, boolean showDeleted)
          throws TapisClientException
  {
    String selectStr1 = DEFAULT_SELECT_SUMMARY;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    RespSystems resp = null;
    try
    {
      resp = sysApi.getSystems(searchStr, limit, orderBy, skip, startAfter, DEFAULT_COMPUTETOTAL, selectStr1, showDeleted);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return Collections.emptyList();
    // Postprocess TapisSystems in the result
    for (TapisSystem tSys : resp.getResult()) postProcessSystem(tSys);
    return resp.getResult();
  }

  /**
   * Dedicated search endpoint using all supported parameters
   * Search for systems using an array of strings that represent an SQL-like WHERE clause
   * and using query parameters for sorting.
   * For example limit=10&orderBy=id(asc)&startAfter=my.sys1
   * Use only one of skip or startAfter
   * When using startAfter orderBy must be specified.
   *
   * @param req
   * @param limit
   * @param orderBy
   * @param skip
   * @param startAfter
   * @param selectStr
   * @return list of systems available to the caller and matching search conditions.
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSystem> searchSystems(ReqSearchSystems req, int limit, String orderBy, int skip, String startAfter,
                                         String selectStr)
          throws TapisClientException
  {
    String selectStr1 = DEFAULT_SELECT_SUMMARY;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    RespSystems resp = null;
    try { resp = sysApi.searchSystemsRequestBody(req, limit, orderBy, skip, startAfter, DEFAULT_COMPUTETOTAL, selectStr1); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null || resp.getResult() == null) return Collections.emptyList();
    // Postprocess TapisSystems in the result
    for (TapisSystem tSys : resp.getResult()) postProcessSystem(tSys);
    return resp.getResult();
  }

  /**
   * Dedicated search endpoint using requestBody only
   * Search for systems using an array of strings that represent an SQL-like WHERE clause
   *
   * @param req Request body containing an array of strings representing an SQL-like WHERE clause.
   * @return list of systems available to the caller and matching search conditions.
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSystem> searchSystems(ReqSearchSystems req) throws TapisClientException
  {
    return searchSystems(req, DEFAULT_LIMIT, DEFAULT_ORDERBY, DEFAULT_SKIP, DEFAULT_STARTAFTER, DEFAULT_SELECT_SUMMARY);
  }

  /**
   * Dedicated search endpoint for retrieving systems that match a list of constraint conditions
   * The constraint conditions are passed in as an array of strings that represent an SQL-like WHERE clause
   */
  public List<TapisSystem> matchConstraints(ReqMatchConstraints req) throws TapisClientException
  {
    RespSystems resp = null;
    try { resp = sysApi.matchConstraints(req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return Collections.emptyList();
    // Postprocess TapisSystems in the result
    for (TapisSystem tSys : resp.getResult()) postProcessSystem(tSys);
    return resp.getResult();
  }

  /**
   * Check if resource is enabled
   *
   * @param systemId System Id
   * @return boolean indicating if enabled
   * @throws TapisClientException - If api call throws an exception
   */
  public boolean isEnabled(String systemId) throws TapisClientException
  {
    // Submit the request and return the response
    RespBoolean resp = null;
    try { resp = sysApi.isEnabled(systemId); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null)
    {
      return resp.getResult().getaBool();
    }
    else
    {
      throw new TapisClientException("isEnabled did not return a result");
    }
  }


  // -----------------------------------------------------------------------
  // --------------------------- Permissions -------------------------------
  // -----------------------------------------------------------------------

  /**
   * Grant permissions for given system and user.
   *
   * @param systemId System Id
   * @param userName Id of user
   * @param permissions list of permissions to grant.
   * @throws TapisClientException - If api call throws an exception
   */
  public void grantUserPermissions(String systemId, String userName, List<String> permissions)
          throws TapisClientException
  {
    // Build the request
    var req = new ReqPerms();
    req.setPermissions(permissions);
    // Submit the request
    try { permsApi.grantUserPerms(systemId, userName, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Get list of permissions for given system and user.
   *
   * @param systemId System Id
   * @param userName Id of user
   * @throws TapisClientException - If api call throws an exception
   */
  public List<String> getSystemPermissions(String systemId, String userName) throws TapisClientException
  {
    RespNameArray resp = null;
    try { resp = permsApi.getUserPerms(systemId, userName); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null)
    {
      return resp.getResult().getNames();
    }
    else
    {
      return Collections.emptyList();
    }
  }

  /**
   * Revoke permissions for given system and user.
   *
   * @param systemId System Id
   * @param userName Id of user
   * @param permissions list of permissions to revoke.
   * @throws TapisClientException - if api call throws an exception
   */
  public void revokeUserPermissions(String systemId, String userName, List<String> permissions)
          throws TapisClientException
  {
    // Build the request
    var req = new ReqPerms();
    req.setPermissions(permissions);
    // Submit the request
    try { permsApi.revokeUserPerms(systemId, userName, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Revoke single permission for given system and user.
   *
   * @param systemId System Id
   * @param permission permission to revoke
   * @param userName Id of user
   * @throws TapisClientException - If api call throws an exception
   */
  public void revokeUserPermission(String systemId, String userName, String permission) throws TapisClientException
  {
    // Submit the request
    try { permsApi.revokeUserPerm(systemId, userName, permission); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  // -----------------------------------------------------------------------
  // ---------------------------- Credentials ------------------------------
  // -----------------------------------------------------------------------

  /**
   * Create or update credential for given system and user.
   *
   * @param systemId System Id
   * @param userName Id of user
   * @param req Request containing credentials (password, keys, etc).
   * @throws TapisClientException - If api call throws an exception
   */
  public void updateUserCredential(String systemId, String userName, Credential req) throws TapisClientException
  {
    // Submit the request
    try { credsApi.createUserCredential(systemId, userName, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Retrieve credential for given system, user and authn method.
   * If authn method is null return credential for default authn method defined for the system.
   *
   * @param systemId System Id
   * @param userName Id of user
   * @param authnMethod - Desired authn method used when fetching credentials, for default pass in null.
   * @throws TapisClientException - If api call throws an exception
   */
  public Credential getUserCredential(String systemId, String userName, AuthnMethod authnMethod)
          throws TapisClientException
  {
    RespCredential resp = null;
    String authnMethodStr = (authnMethod==null ? null : authnMethod.name());
    try {resp = credsApi.getUserCredential(systemId, userName, authnMethodStr); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null) return resp.getResult(); else return null;
  }

  /**
   * Retrieve a credential for given system and user for the default authn method defined for the system
   *
   * @param systemId System Id
   * @param userName Id of user
   * @throws TapisClientException - If api call throws an exception
   */
  public Credential getUserCredential(String systemId, String userName) throws TapisClientException
  {
    return getUserCredential(systemId, userName, null);
  }

  /**
   * Delete credential for given system and user.
   *
   * @param systemId System Id
   * @param userName Id of user
   * @throws TapisClientException - if api call throws an exception
   */
  public void deleteUserCredential(String systemId, String userName)
          throws TapisClientException
  {
    // Submit the request
    try { credsApi.removeUserCredential(systemId, userName); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Utility method to build a batch LogicalQueue
   */
  public static LogicalQueue buildLogicalQueue(String name, String hpcQueueName, int maxJobs, int maxJobsPerUser,
                                 int minNodeCount, int maxNodeCount, int minCoresPerNode, int maxCoresPerNode,
                                 int minMemoryMB, int maxMemoryMB, int minMinutes, int maxMinutes)
  {
    var q = new LogicalQueue();
    q.setName(name);
    q.setHpcQueueName(hpcQueueName);
    q.setMaxJobs(maxJobs);
    q.setMaxJobsPerUser(maxJobsPerUser);
    q.setMinNodeCount(minNodeCount);
    q.setMaxNodeCount(maxNodeCount);
    q.setMinCoresPerNode(minCoresPerNode);
    q.setMaxCoresPerNode(maxCoresPerNode);
    q.setMinMemoryMB(minMemoryMB);
    q.setMaxMemoryMB(maxMemoryMB);
    q.setMinMinutes(minMinutes);
    q.setMaxMinutes(maxMinutes);
    return q;
  }

  /**
   * Utility method to build a ReqPutSystem object using attributes from a TapisSystem.
   */
  public static ReqPutSystem buildReqPutSystem(TapisSystem sys)
  {
    ReqPutSystem rSys = new ReqPutSystem();
    rSys.description(sys.getDescription());
    rSys.setHost(sys.getHost());
    rSys.effectiveUserId(sys.getEffectiveUserId());
    rSys.defaultAuthnMethod(sys.getDefaultAuthnMethod());
    rSys.authnCredential(sys.getAuthnCredential());
    rSys.port(sys.getPort()).useProxy(sys.getUseProxy()).proxyHost(sys.getProxyHost()).proxyPort(sys.getProxyPort());
    rSys.dtnSystemId(sys.getDtnSystemId());
    rSys.dtnMountPoint(sys.getDtnMountPoint()).dtnMountSourcePath(sys.getDtnMountSourcePath());
    rSys.setJobRuntimes(sys.getJobRuntimes());
    rSys.jobWorkingDir(sys.getJobWorkingDir());
    rSys.jobEnvVariables(sys.getJobEnvVariables());
    rSys.jobMaxJobs(sys.getJobMaxJobs()).jobMaxJobsPerUser(sys.getJobMaxJobsPerUser());
    rSys.jobIsBatch(sys.getJobIsBatch());
    rSys.batchScheduler(sys.getBatchScheduler());
    rSys.batchLogicalQueues(sys.getBatchLogicalQueues());
    rSys.batchDefaultLogicalQueue(sys.getBatchDefaultLogicalQueue());
    rSys.jobCapabilities(sys.getJobCapabilities());
    rSys.tags(sys.getTags());
    // Notes requires special handling. It must be null or a JsonObject
    Object notes = sys.getNotes();
    if (notes == null) rSys.notes(null);
    else if (notes instanceof String) rSys.notes(ClientTapisGsonUtils.getGson().fromJson((String) notes, JsonObject.class));
    else if (notes instanceof JsonObject) rSys.notes(notes);
    else rSys.notes(null);
    return rSys;
  }

  /**
   * Utility method to build a credential object given secrets.
   */
  public static Credential buildCredential(String password, String privateKey, String publicKey,
                                    String accessKey, String accessSecret, String certificate)
  {
    var cred = new Credential();
    cred.setPassword(password);
    cred.setPrivateKey(privateKey);
    cred.setPublicKey(publicKey);
    cred.setAccessKey(accessKey);
    cred.setAccessSecret(accessSecret);
    cred.setCertificate(certificate);
    return cred;
  }

  /**
   * Utility method to build a Capability object given category, name and value
   */
  public static Capability buildCapability(CategoryEnum category, String name,
                                           DatatypeEnum datatype, int precedence, String value)
  {
    var cap = new Capability();
    cap.setCategory(category);
    cap.setName(name);
    cap.setDatatype(datatype);
    cap.setPrecedence(precedence);
    cap.setValue(value);
    return cap;
  }

  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************

  /**
   * Do any client side postprocessing of a returned system.
   * Currently this just involves transforming the notes attribute into a json string
   * @param tSys - TapisSystem to process
   * @return - Resulting TapisSystem
   */
  TapisSystem postProcessSystem(TapisSystem tSys)
  {
    // If we have a notes attribute convert it from a LinkedTreeMap to a string with json.
    if (tSys != null && tSys.getNotes() != null)
    {
      LinkedTreeMap lmap = (LinkedTreeMap) tSys.getNotes();
      JsonObject tmpNotes = ClientTapisGsonUtils.getGson().fromJson(lmap.toString(), JsonObject.class);
      tSys.setNotes(tmpNotes.toString());
    }
    return tSys;
  }
}
