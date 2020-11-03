package edu.utexas.tacc.tapis.systems.client;

import java.util.List;

import edu.utexas.tacc.tapis.systems.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespSystemArray;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;

import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.systems.client.gen.ApiClient;
import edu.utexas.tacc.tapis.systems.client.gen.ApiException;
import edu.utexas.tacc.tapis.systems.client.gen.Configuration;
import edu.utexas.tacc.tapis.systems.client.gen.api.CredentialsApi;
import edu.utexas.tacc.tapis.systems.client.gen.api.PermissionsApi;
import edu.utexas.tacc.tapis.systems.client.gen.api.SystemsApi;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateCredential;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqPerms;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqSearchSystems;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqUpdateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespChangeCount;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespCredential;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespNameArray;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespResourceUrl;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability.CategoryEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;

import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_LIMIT;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SKIP;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SEARCH;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SORTBY;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_STARTAFTER;

/**
 * Class providing a convenient front-end to the automatically generated client code
 * for the Systems Service REST API.
 * Underlying client classes with "gen" in the package name are generated by
 * openapi-generator each time a build is run.
 */
public class SystemsClient
{
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************

  // Header key for JWT
  public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";

  // ************************************************************************
  // *********************** Enums ******************************************
  // ************************************************************************
  // Define AccessMethod here to be used in place of the auto-generated model enum
  //   because the auto-generated enum is named DefaultAccessMethodEnum which is misleading.
  public enum AccessMethod {PASSWORD, PKI_KEYS, ACCESS_KEY, CERT}

  // ************************************************************************
  // *********************** Fields *****************************************
  // ************************************************************************
  // Response body serializer
  private static final Gson gson = ClientTapisGsonUtils.getGson();
  private final SystemsApi sysApi;
  private final PermissionsApi permsApi;
  private final CredentialsApi credsApi;
  private final GeneralApi generalApi;

  // ************************************************************************
  // *********************** Constructors ***********************************
  // ************************************************************************

  public SystemsClient()
  {
    sysApi = new SystemsApi();
    permsApi = new PermissionsApi();
    credsApi = new CredentialsApi();
    generalApi = new GeneralApi();
  }

  /**
   * Constructor that overrides the compiled-in basePath value in ApiClient.  This
   * constructor is typically used in production.
   * <p>
   * The path includes the URL prefix up to and including the service root.  By
   * default this value is http://localhost/v3/service_name.  In production environments
   * the protocol is https and the host/port will be specific to that environment.
   *
   * @param path the base path
   * @param jwt the token to set in an HTTP header
   */
  public SystemsClient(String path, String jwt)
  {
    ApiClient apiClient = Configuration.getDefaultApiClient();
    if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
    if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
    sysApi = new SystemsApi();
    permsApi = new PermissionsApi();
    credsApi = new CredentialsApi();
    generalApi = new GeneralApi();
  }

  // ************************************************************************
  // *********************** Public Methods *********************************
  // ************************************************************************

  /**
   * getApiClient: Return underlying ApiClient
   */
  public ApiClient getApiClient()
  {
    return Configuration.getDefaultApiClient();
  }

  /**
   * Update base path for default client.
   */
  public SystemsClient setBasePath(String basePath)
  {
    Configuration.getDefaultApiClient().setBasePath(basePath);
    return this;
  }

  /**
   * Add http header to default client
   */
  public SystemsClient addDefaultHeader(String key, String val)
  {
    Configuration.getDefaultApiClient().addDefaultHeader(key, val);
    return this;
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
   * Update a system
   *
   * @return url pointing to updated resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String updateSystem(String name, ReqUpdateSystem req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = sysApi.updateSystem(name, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Change system owner given the system name and new owner name.
   *
   * @param name System name
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int changeSystemOwner(String name, String newOwnerName) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = sysApi.changeSystemOwner(name, newOwnerName); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Get a system by name without returning credentials
   *
   * @param name System name
   * @return The system or null if system not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TSystem getSystemByName(String name) throws TapisClientException
  {
    RespSystem resp = null;
    try {resp = sysApi.getSystemByName(name, false, null); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null) return resp.getResult(); else return null;
  }

  /**
   * Get a system by name returning credentials for specified access method.
   * If accessMethod is null then default access method for the system is used.
   * Use of this method is highly restricted. Only certain Tapis services are
   * authorized to call this method.
   *
   * @param name System name
   * @param accessMethod - Desired access method used when fetching credentials,
   *                    default access method used if this is null
   * @return The system or null if system not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TSystem getSystemByName(String name, AccessMethod accessMethod) throws TapisClientException
  {
    RespSystem resp = null;
    String accessMethodStr = (accessMethod==null ? null : accessMethod.name());
    try {resp = sysApi.getSystemByName(name, true, accessMethodStr); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null) return resp.getResult(); else return null;
  }

  /**
   * Get list of all systems
   */
  public List<TSystem> getSystems() throws TapisClientException
  {
    RespSystemArray resp = null;
    try { resp = sysApi.getSystems(false, DEFAULT_SEARCH, DEFAULT_LIMIT, DEFAULT_SORTBY, DEFAULT_SKIP, DEFAULT_STARTAFTER); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Get list of systems using search. For example search=(name.like.MySys*)~(enabled.eq.true)
   */
  public List<TSystem> getSystems(String searchStr) throws TapisClientException
  {
    RespSystemArray resp = null;
    try { resp = sysApi.getSystems(false, searchStr, DEFAULT_LIMIT, DEFAULT_SORTBY, DEFAULT_SKIP, DEFAULT_STARTAFTER); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Get list of systems using search and sort.
   * For example search=(name.like.MySys*)~(enabled.eq.true)&limit=10&sortBy=id(asc)&startAfter=101
   * Use only one of skip or startAfter
   * When using startAfter sortBy must be specified.
   */
  public List<TSystem> getSystems(String searchStr, int limit, String sortBy, int skip, String startAfter) throws TapisClientException
  {
    RespSystemArray resp = null;
    try { resp = sysApi.getSystems(false, searchStr, limit, sortBy, skip, startAfter); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Search for systems using an array of strings that represent an SQL-like WHERE clause
   */
  public List<TSystem> searchSystems(ReqSearchSystems req) throws TapisClientException
  {
    RespSystemArray resp = null;
    try { resp = sysApi.searchSystemsRequestBody(req, false, DEFAULT_LIMIT, DEFAULT_SORTBY, DEFAULT_SKIP, DEFAULT_STARTAFTER); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Search for systems using an array of strings that represent an SQL-like WHERE clause
   * and using query parameters for sorting.
   * For example limit=10&sortBy=id(asc)&startAfter=101
   * Use only one of skip or startAfter
   * When using startAfter sortBy must be specified.
   */
  public List<TSystem> searchSystems(ReqSearchSystems req, int limit, String sortBy, int skip, String startAfter) throws TapisClientException
  {
    RespSystemArray resp = null;
    try { resp = sysApi.searchSystemsRequestBody(req, false, limit, sortBy, skip, startAfter); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Delete a system given the system name.
   * Return 1 if record was deleted
   * Return 0 if record not present
   *
   * @param name System name
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int deleteSystemByName(String name) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = sysApi.deleteSystemByName(name); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  // -----------------------------------------------------------------------
  // --------------------------- Permissions -------------------------------
  // -----------------------------------------------------------------------

  /**
   * Grant permissions for given system and user.
   *
   * @throws TapisClientException - If api call throws an exception
   */
  public void grantUserPermissions(String systemName, String userName, List<String> permissions)
          throws TapisClientException
  {
    // Build the request
    var req = new ReqPerms();
    req.setPermissions(permissions);
    // Submit the request
    try { permsApi.grantUserPerms(systemName, userName, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Get list of permissions for given system and user.
   */
  public List<String> getSystemPermissions(String systemName, String userName) throws TapisClientException
  {
    RespNameArray resp = null;
    try { resp = permsApi.getUserPerms(systemName, userName); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getNames(); else return null;
  }

  /**
   * Revoke permissions for given system and user.
   *
   * @throws TapisClientException - if api call throws an exception
   */
  public void revokeUserPermissions(String systemName, String userName, List<String> permissions)
          throws TapisClientException
  {
    // Build the request
    var req = new ReqPerms();
    req.setPermissions(permissions);
    // Submit the request
    try { permsApi.revokeUserPerms(systemName, userName, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Revoke single permission for given system and user.
   *
   * @throws TapisClientException - if api call throws an exception
   */
  public void revokeUserPermission(String systemName, String userName, String permission)
          throws TapisClientException
  {
    // Submit the request
    try { permsApi.revokeUserPerm(systemName, userName, permission); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  // -----------------------------------------------------------------------
  // ---------------------------- Credentials ------------------------------
  // -----------------------------------------------------------------------

  /**
   * Create or update credential for given system and user.
   *
   * @throws TapisClientException - If api call throws an exception
   */
  public void updateUserCredential(String systemName, String userName, ReqCreateCredential req) throws TapisClientException
  {
    // Submit the request
    try { credsApi.createUserCredential(systemName, userName, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Retrieve credential for given system, user and access method.
   * If access method is null return credential for default access method defined for the system.
   *
   * @throws TapisClientException - If api call throws an exception
   */
  public Credential getUserCredential(String systemName, String userName, AccessMethod accessMethod)
          throws TapisClientException
  {
    RespCredential resp = null;
    String accessMethodStr = (accessMethod==null ? null : accessMethod.name());
    try {resp = credsApi.getUserCredential(systemName, userName, accessMethodStr); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null) return resp.getResult(); else return null;
  }

  /**
   * Retrieve a credential for given system and user for the default access method defined for the system
   *
   * @throws TapisClientException - If api call throws an exception
   */
  public Credential getUserCredential(String systemName, String userName) throws TapisClientException
  {
    return getUserCredential(systemName, userName, null);
  }

  /**
   * Delete credential for given system and user.
   *
   * @throws TapisClientException - if api call throws an exception
   */
  public void deleteUserCredential(String systemName, String userName)
          throws TapisClientException
  {
    // Submit the request
    try { credsApi.removeUserCredential(systemName, userName); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
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
  public static Capability buildCapability(CategoryEnum category, String name, String value)
  {
    var cap = new Capability();
    cap.setCategory(category);
    cap.setName(name);
    cap.setValue(value);
    return cap;
  }

  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************

}
