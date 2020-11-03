package edu.utexas.tacc.tapis.apps.client;

import java.util.List;

import edu.utexas.tacc.tapis.apps.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespAppArray;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;

import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.apps.client.gen.ApiClient;
import edu.utexas.tacc.tapis.apps.client.gen.ApiException;
import edu.utexas.tacc.tapis.apps.client.gen.Configuration;
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
import edu.utexas.tacc.tapis.apps.client.gen.model.Capability;
import edu.utexas.tacc.tapis.apps.client.gen.model.Capability.CategoryEnum;

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
  private final ApplicationsApi appApi;
  private final PermissionsApi permsApi;
  private final GeneralApi generalApi;

  // ************************************************************************
  // *********************** Constructors ***********************************
  // ************************************************************************

  public AppsClient()
  {
    appApi = new ApplicationsApi();
    permsApi = new PermissionsApi();
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
  public AppsClient(String path, String jwt)
  {
    ApiClient apiClient = Configuration.getDefaultApiClient();
    if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
    if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
    appApi = new ApplicationsApi();
    permsApi = new PermissionsApi();
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
  public AppsClient setBasePath(String basePath)
  {
    Configuration.getDefaultApiClient().setBasePath(basePath);
    return this;
  }

  /**
   * Add http header to default client
   */
  public AppsClient addDefaultHeader(String key, String val)
  {
    Configuration.getDefaultApiClient().addDefaultHeader(key, val);
    return this;
  }

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
   * @return url pointing to created resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String createApp(ReqCreateApp req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = appApi.createApp(req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update an app
   *
   * @return url pointing to updated resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String updateApp(String name, ReqUpdateApp req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = appApi.updateApp(name, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Change app owner given the app name and new owner name.
   *
   * @param name App name
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int changeAppOwner(String name, String newOwnerName) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = appApi.changeAppOwner(name, newOwnerName); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Get an app by name
   *
   * @param name App name
   * @return The app or null if app not found
   * @throws TapisClientException - If api call throws an exception
   */
  public App getAppByName(String name) throws TapisClientException
  {
    RespApp resp = null;
    try {resp = appApi.getAppByName(name); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null) return resp.getResult(); else return null;
  }

  /**
   * Get list of apps
   */
  public List<App> getApps(String searchStr) throws TapisClientException
  {
    RespAppArray resp = null;
    try { resp = appApi.getApps(false, searchStr); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Search for apps using an array of strings that represent an SQL-like WHERE clause
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
   * Delete an app given the app name.
   * Return 1 if record was deleted
   * Return 0 if record not present
   *
   * @param name App name
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int deleteAppByName(String name) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = appApi.deleteAppByName(name); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  // -----------------------------------------------------------------------
  // --------------------------- Permissions -------------------------------
  // -----------------------------------------------------------------------

  /**
   * Grant permissions for given app and user.
   *
   * @throws TapisClientException - If api call throws an exception
   */
  public void grantUserPermissions(String appName, String userName, List<String> permissions)
          throws TapisClientException
  {
    // Build the request
    var req = new ReqPerms();
    req.setPermissions(permissions);
    // Submit the request
    try { permsApi.grantUserPerms(appName, userName, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Get list of permissions for given app and user.
   */
  public List<String> getAppPermissions(String appName, String userName) throws TapisClientException
  {
    RespNameArray resp = null;
    try { resp = permsApi.getUserPerms(appName, userName); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getNames(); else return null;
  }

  /**
   * Revoke permissions for given app and user.
   *
   * @throws TapisClientException - if api call throws an exception
   */
  public void revokeUserPermissions(String appName, String userName, List<String> permissions)
          throws TapisClientException
  {
    // Build the request
    var req = new ReqPerms();
    req.setPermissions(permissions);
    // Submit the request
    try { permsApi.revokeUserPerms(appName, userName, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Revoke single permission for given app and user.
   *
   * @throws TapisClientException - if api call throws an exception
   */
  public void revokeUserPermission(String appName, String userName, String permission)
          throws TapisClientException
  {
    // Submit the request
    try { permsApi.revokeUserPerm(appName, userName, permission); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
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

  // -----------------------------------------------------------------------
  // --------------------------- Utility Methods ---------------------------
  // -----------------------------------------------------------------------


  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************

}