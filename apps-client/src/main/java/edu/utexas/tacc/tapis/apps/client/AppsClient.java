package edu.utexas.tacc.tapis.apps.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import edu.utexas.tacc.tapis.apps.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.apps.client.gen.model.ArgMetaSpec;
import edu.utexas.tacc.tapis.apps.client.gen.model.ArgSpec;
import edu.utexas.tacc.tapis.apps.client.gen.model.KeyValuePair;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespAppArray;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespBoolean;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;

import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.apps.client.gen.ApiClient;
import edu.utexas.tacc.tapis.apps.client.gen.ApiException;
import edu.utexas.tacc.tapis.apps.client.gen.api.PermissionsApi;
import edu.utexas.tacc.tapis.apps.client.gen.api.ApplicationsApi;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqCreateApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqPerms;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqSearchApps;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqUpdateApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespChangeCount;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespNameArray;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespResourceUrl;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.App;

/**
 * Class providing a convenient front-end to the automatically generated client code
 * for the Apps Service REST API.
 * Underlying client classes with "gen" in the package name are generated by
 * openapi-generator each time a build is run.
 */
public class AppsClient
{
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************

  // Header key for JWT
  public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";

  // Defaults
  public static final boolean DEFAULT_STRICT_FILE_INPUTS = false;
  public static final boolean DEFAULT_FILE_INPUT_IN_PLACE = false;
  public static final boolean DEFAULT_FILE_INPUT_META_REQUIRED = false;
  public static final int DEFAULT_MAX_JOBS = Integer.MAX_VALUE;

  // ************************************************************************
  // *********************** Fields *****************************************
  // ************************************************************************
  // Response body serializer
  private static final Gson gson = ClientTapisGsonUtils.getGson();

  // Instance of the underlying autogenerated client.
  private final ApiClient apiClient;
  private final ApplicationsApi appApi;
  private final PermissionsApi permsApi;
  private final GeneralApi generalApi;

  // ************************************************************************
  // *********************** Constructors ***********************************
  // ************************************************************************

  /**
   * Default constructor which uses the compiled-in basePath based on the openapi spec
   *   used to autogenerate the client.
   */
  public AppsClient()
  {
    apiClient = new ApiClient();
    appApi = new ApplicationsApi(apiClient);
    permsApi = new PermissionsApi(apiClient);
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
  public AppsClient(String path, String jwt)
  {
    apiClient = new ApiClient();
    if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
    if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
    appApi = new ApplicationsApi(apiClient);
    permsApi = new PermissionsApi(apiClient);
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
  public void setBasePath(String basePath) { apiClient.setBasePath(basePath); }

  // Add http header to default client
  public void addDefaultHeader(String key, String val) { apiClient.addDefaultHeader(key, val); }

  // -----------------------------------------------------------------------
  // ------------------------- Apps -------------------------------------
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
   * Create an app
   *
   * @param req Request body specifying attributes
   * @return url pointing to created resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String createApp(ReqCreateApp req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = appApi.createAppVersion(req, false); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update an app
   *
   * @param id Id of the application
   * @param version Version of the application
   * @param req Request body specifying attributes
   * @return url pointing to updated resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String updateApp(String id, String version, ReqUpdateApp req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = appApi.updateApp(id, version, req, false); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Change app owner given the app id and new owner id.
   *
   * @param id App id
   * @param newOwnerName New owner id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int changeAppOwner(String id, String newOwnerName) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = appApi.changeAppOwner(id, newOwnerName, false); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Get latest version of an app.
   *
   * @param appId Id of the application
   * @param requireExecPerm Check for EXECUTE permission as well as READ permission
   * @return Latest version of the app
   * @throws TapisClientException - If api call throws an exception
   */
  public App getAppLatestVersion(String appId, Boolean requireExecPerm) throws TapisClientException
  {
    RespApp resp = null;
    try {resp = appApi.getAppLatestVersion(appId, false, requireExecPerm); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    // Postprocess the app
    App app = postProcessApp(resp.getResult());
    return app;
  }

  /**
   * Get a specific version of an app using all supported parameters.
   *
   * @param appId Id of the application
   * @param appVersion Version of the application
   * @param requireExecPerm Check for EXECUTE permission as well as READ permission
   * @return The app or null if app not found
   * @throws TapisClientException - If api call throws an exception
   */
  public App getApp(String appId, String appVersion, Boolean requireExecPerm) throws TapisClientException
  {
    RespApp resp = null;
    try {resp = appApi.getApp(appId, appVersion, false, requireExecPerm); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    // Postprocess the app
    App app = postProcessApp(resp.getResult());
    return app;
  }

  /**
   * Get a specific version of an app
   *
   * @param appId Id of the application
   * @param appVersion Version of the application
   * @return The app or null if app not found
   * @throws TapisClientException - If api call throws an exception
   */
  public App getApp(String appId, String appVersion) throws TapisClientException
  {
    return getApp(appId, appVersion, Boolean.FALSE);
  }

  /**
   * Retrieve applications. Use search query parameters to limit results.
   * For example search=(id.like.MyApp*)~(enabled.eq.true)
   * TODO/TBD: By default latest version of each app is returned.
   *
   * @param searchStr Search string. Empty or null to return all apps.
   * @return Apps accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<App> getApps(String searchStr) throws TapisClientException
  {
    RespAppArray resp = null;
    try { resp = appApi.getApps(false, searchStr); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    // Postprocess Apps in the result
    for (App app : resp.getResult()) postProcessApp(app);
    return resp.getResult();
  }

  /**
   * Get apps using search based on an array of strings representing an SQL-like WHERE clause
   * TODO/TBD: By default latest version of each app is returned.
   *
   * @param req Request body specifying SQL-like search strings.
   * @return Apps accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<App> searchApps(ReqSearchApps req) throws TapisClientException
  {
    RespAppArray resp = null;
    try { resp = appApi.searchAppsRequestBody(req, false); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Delete an app given the app id.
   * @param confirm Confirm the action
   * Return 1 if record was deleted
   * Return 0 if record not present
   *
   * @param id App id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int deleteApp(String id, Boolean confirm) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = appApi.deleteApp(id, false, confirm); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Check if resource is enabled
   *
   * @return boolean indicating if enabled
   * @throws TapisClientException - If api call throws an exception
   */
  public boolean isEnabled(String appId) throws TapisClientException
  {
    // Submit the request and return the response
    RespBoolean resp = null;
    try { resp = appApi.isEnabled(appId, false); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null)
    {
      return resp.getResult();
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
   * Grant permissions for given app and user.
   *
   * @param appId id of app
   * @param userName Id of user
   * @param permissions list of permissions to grant.
   * @throws TapisClientException - If api call throws an exception
   */
  public void grantUserPermissions(String appId, String userName, List<String> permissions)
          throws TapisClientException
  {
    // Build the request
    var req = new ReqPerms();
    req.setPermissions(permissions);
    // Submit the request
    try { permsApi.grantUserPerms(appId, userName, req, false); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Get list of permissions for given app and user.
   *
   * @param appId id of app
   * @param userName Name of user
   * @throws TapisClientException - If api call throws an exception
   */
  public List<String> getAppPermissions(String appId, String userName) throws TapisClientException
  {
    RespNameArray resp = null;
    try { resp = permsApi.getUserPerms(appId, userName, false); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getNames(); else return null;
  }

  /**
   * Revoke permissions for given app and user.
   *
   * @param appId id of app
   * @param userName name of user
   * @param permissions list of permissions to revoke.
   * @throws TapisClientException - If api call throws an exception
   */
  public void revokeUserPermissions(String appId, String userName, List<String> permissions)
          throws TapisClientException
  {
    // Build the request
    var req = new ReqPerms();
    req.setPermissions(permissions);
    // Submit the request
    try { permsApi.revokeUserPerms(appId, userName, req, false); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Revoke single permission for given app and user.
   *
   * @param appId id of app
   * @param userName name of user
   * @param permission permission to revoke
   * @throws TapisClientException - if api call throws an exception
   */
  public void revokeUserPermission(String appId, String userName, String permission)
          throws TapisClientException
  {
    // Submit the request
    try { permsApi.revokeUserPerm(appId, userName, permission, false); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  // -----------------------------------------------------------------------
  // --------------------------- Utility Methods ---------------------------
  // -----------------------------------------------------------------------

  /**
   * Utility method to build an ArgSpec given value, metaName, metaRequired and metaKeyValuePairs
   *
   * @param value
   * @param metaName
   * @param metaDescription
   * @param metaRequired
   * @param metaKVPairs - List of Strings in the form key=value.
   * @return a new ArgSpec object
   */
  public static ArgSpec buildArg(String value, String metaName, String metaDescription,
                                 boolean metaRequired, List<String> metaKVPairs)
  {
    var arg = new ArgSpec();
    var argMeta = new ArgMetaSpec();
    var argMetaKVPairs = new ArrayList<KeyValuePair>();
    List<String> argKVPairs = Collections.emptyList();
    if (metaKVPairs != null) argKVPairs = metaKVPairs;
    // Convert strings in the form key=value into KeyValuePair objects
    for (String kvPairStr : argKVPairs)
    {
      argMetaKVPairs.add(kvPairFromString(kvPairStr));
    }
    argMeta.setName(metaName);
    argMeta.setDescription(metaDescription);
    argMeta.setRequired(metaRequired);
    argMeta.setKeyValuePairs(argMetaKVPairs);
    arg.setArg(value);
    arg.setMeta(argMeta);
    return arg;
  }

  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************
  /**
   * Do any client side postprocessing of a returned app.
   * Currently this just involves transforming the notes attribute into a json string
   * @param app App to process
   * @return - Resulting App
   */
  App postProcessApp(App app)
  {
    // If we have a notes attribute convert it from a LinkedTreeMap to a string with json.
    if (app != null && app.getNotes() != null)
    {
      LinkedTreeMap lmap = (LinkedTreeMap) app.getNotes();
      JsonObject tmpNotes = ClientTapisGsonUtils.getGson().fromJson(lmap.toString(), JsonObject.class);
      app.setNotes(tmpNotes.toString());
    }
    return app;
  }

  private static KeyValuePair kvPairFromString(String s)
  {
    if (StringUtils.isBlank(s)) return new KeyValuePair().key("").value("");
    int e1 = s.indexOf('=');
    String k = s.substring(0, e1);
    String v = "";
    // Everything after "=" is the value
    if (e1 > 0) v = s.substring(e1+1, s.length()-1);
    return new KeyValuePair().key(k).value(v);
  }
}
