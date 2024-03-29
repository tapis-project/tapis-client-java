package edu.utexas.tacc.tapis.apps.client;

import java.lang.reflect.Type;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.ITapisClient;
import edu.utexas.tacc.tapis.apps.client.gen.ApiClient;
import edu.utexas.tacc.tapis.apps.client.gen.ApiException;
import edu.utexas.tacc.tapis.apps.client.gen.api.ApplicationsApi;
import edu.utexas.tacc.tapis.apps.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.apps.client.gen.api.PermissionsApi;
import edu.utexas.tacc.tapis.apps.client.gen.model.AppArgSpec;
import edu.utexas.tacc.tapis.apps.client.gen.model.KeyValuePair;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqPutApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespApps;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespBoolean;
import edu.utexas.tacc.tapis.apps.client.gen.model.ListTypeEnum;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqPostApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqPerms;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqSearchApps;
import edu.utexas.tacc.tapis.apps.client.gen.model.ReqPatchApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespChangeCount;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespNameArray;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespResourceUrl;
import edu.utexas.tacc.tapis.apps.client.gen.model.RespApp;
import edu.utexas.tacc.tapis.apps.client.gen.model.TapisApp;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_COMPUTETOTAL;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_LIMIT;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SEARCH;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SELECT_ALL;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SELECT_SUMMARY;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SKIP;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_ORDERBY;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_STARTAFTER;

/**
 * Class providing a convenient front-end to the automatically generated client code
 * for the Apps Service REST API.
 * Underlying client classes with "gen" in the package name are generated by
 * openapi-generator each time a build is run.
 */
public class AppsClient implements ITapisClient
{
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************

  // Header key for JWT
  public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";

  // Create a TypeToken to be used by gson for processing of LinkedTreeMap objects
  private static final Type linkedTreeMapType = new TypeToken<LinkedTreeMap<Object,Object>>(){}.getType();

  // Named null values to make it clear what is being passed in to a method
  private static final String impersonationIdNull = null;
  private static final String resourceTenantNull = null;

  // Defaults
  public static final boolean DEFAULT_STRICT_FILE_INPUTS = false;
  public static final boolean DEFAULT_FILE_INPUT_AUTO_MOUNT_LOCAL = true;
  public static final int DEFAULT_MAX_JOBS = Integer.MAX_VALUE;
  private static final ListTypeEnum DEFAULT_LIST_TYPE_ENUM = ListTypeEnum.ALL;

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
  public AppsClient setBasePath(String basePath) { apiClient.setBasePath(basePath); return this;}

  // Add http header to default client
  public AppsClient addDefaultHeader(String key, String val) { apiClient.addDefaultHeader(key, val); return this;}

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
   * @return Service ready status as a string
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
   * See the helper method buildReqPostApp() for an example of how to build a pre-populated
   *   ReqPostApp instance from a TapisApp instance.
   *
   * @param req Request body specifying attributes
   * @return url pointing to created resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String createApp(ReqPostApp req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = appApi.createAppVersion(req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update selected attributes of an app
   *
   * @param id Id of the application
   * @param version Version of the application
   * @param req Request body specifying attributes
   * @return url pointing to updated resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String patchApp(String id, String version, ReqPatchApp req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = appApi.patchApp(id, version, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update all attributes of an application
   * NOTE: Not all attributes are updatable.
   * See the helper method buildReqPutApp() for an example of how to build a pre-populated
   *   ReqPutApp instance from a TapisApp instance.
   *
   * @param id - Id of resource to be updated
   * @param version Version of the application
   * @param req - Pre-populated ReqPutApp instance
   * @return url pointing to updated resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String putApp(String id, String version, ReqPutApp req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = appApi.putApp(id, version, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update enabled attribute to true.
   *
   * @param id App id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int enableApp(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = appApi.enableApp(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Update enabled attribute to false.
   *
   * @param id App id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int disableApp(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = appApi.disableApp(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Update deleted attribute to true.
   *
   * @param id App id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int deleteApp(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = appApi.deleteApp(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Update deleted to false.
   *
   * @param id App id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int undeleteApp(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = appApi.undeleteApp(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
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
    try { resp = appApi.changeAppOwner(id, newOwnerName); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Get most recent version of an app using minimal attributes.
   *
   * Attributes named *notes* contain free form json and are represented as java Object type in generated TapisApp class.
   * Client code converts all *notes* attributes to String type, so each *notes* Object can safely be cast to String.
   * *notes* attributes are found at TapisApp top level and in collections under TapisApp.JobAttributes.ParameterSet
   *
   * @param appId id of the application
   * @return The app or null if app not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisApp getApp(String appId) throws TapisClientException
  {
    return getAppLatestVersion(appId, Boolean.FALSE, DEFAULT_SELECT_ALL, resourceTenantNull);
  }

  // Simple wrapper for backward compatibility.
  public TapisApp getAppLatestVersion(String appId, Boolean requireExecPerm, String selectStr)
          throws TapisClientException
  {
    return getAppLatestVersion(appId, requireExecPerm, selectStr, resourceTenantNull);
  }

  /**
   * Get the most recent version of an app using all supported parameters
   *
   * Attributes named *notes* contain free form json and are represented as java Object type in generated TapisApp class.
   * Client code converts all *notes* attributes to String type, so each *notes* Object can safely be cast to String.
   * *notes* attributes are found at TapisApp top level and in collections under TapisApp.JobAttributes.ParameterSet
   *
   * @param appId id of the application
   * @param requireExecPerm Check for EXECUTE permission as well as READ permission
   * @param selectStr - Attributes to be included in result. For example select=id,version,owner
   * @return Most recent version of the app
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisApp getAppLatestVersion(String appId, Boolean requireExecPerm, String selectStr, String resourceTenant)
          throws TapisClientException
  {
    String selectStr1 = DEFAULT_SELECT_ALL;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    RespApp resp = null;
    try {resp = appApi.getAppLatestVersion(appId, requireExecPerm, selectStr1, resourceTenant); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    // Postprocess the app
    TapisApp app = postProcessApp(resp.getResult());
    return app;
  }

  /**
   * Get a specific version of an app using minimal attributes
   *
   * Attributes named *notes* contain free form json and are represented as java Object type in generated TapisApp class.
   * Client code converts all *notes* attributes to String type, so each *notes* Object can safely be cast to String.
   * *notes* attributes are found at TapisApp top level and in collections under TapisApp.JobAttributes.ParameterSet
   *
   * @param appId id of the application
   * @param appVersion Version of the application
   * @return The app or null if app not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisApp getApp(String appId, String appVersion) throws TapisClientException
  {
    return getApp(appId, appVersion, Boolean.FALSE, impersonationIdNull, DEFAULT_SELECT_ALL, resourceTenantNull);
  }

  /**
   * Get a specific version of an app including the two auth related flags
   *
   * Attributes named *notes* contain free form json and are represented as java Object type in generated TapisApp class.
   * Client code converts all *notes* attributes to String type, so each *notes* Object can safely be cast to String.
   * *notes* attributes are found at TapisApp top level and in collections under TapisApp.JobAttributes.ParameterSet
   *
   * @param appId id of the application
   * @param appVersion Version of the application
   * @param requireExecPerm Check for EXECUTE permission as well as READ permission
   * @param impersonationId - use provided Tapis username instead of oboUser
   * @return The app or null if app not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisApp getApp(String appId, String appVersion, Boolean requireExecPerm, String impersonationId)
          throws TapisClientException
  {
    return getApp(appId, appVersion, requireExecPerm, impersonationId, DEFAULT_SELECT_ALL, resourceTenantNull);
  }
  public TapisApp getApp(String appId, String appVersion, Boolean requireExecPerm) throws TapisClientException
  {
    return getApp(appId, appVersion, requireExecPerm, impersonationIdNull, DEFAULT_SELECT_ALL, resourceTenantNull);
  }
  public TapisApp getApp(String appId, String appVersion, Boolean requireExecPerm, String impersonationId, String selectStr)
          throws TapisClientException
  {
    return getApp(appId, appVersion, requireExecPerm, impersonationId, selectStr, resourceTenantNull);
  }

  /**
   * Get a specific version of an app using all supported parameters.
   *
   * Attributes named *notes* contain free form json and are represented as java Object type in generated TapisApp class.
   * Client code converts all *notes* attributes to String type, so each *notes* Object can safely be cast to String.
   * *notes* attributes are found at TapisApp top level and in collections under TapisApp.JobAttributes.ParameterSet
   *
   * @param appId id of the application
   * @param appVersion Version of the application
   * @param requireExecPerm Check for EXECUTE permission as well as READ permission
   * @param impersonationId - use provided Tapis username instead of oboUser
   * @param selectStr - Attributes to be included in result. For example select=id,version,owner
   * @return The app or null if app not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisApp getApp(String appId, String appVersion, Boolean requireExecPerm, String impersonationId,
                         String selectStr, String resourceTenant)
          throws TapisClientException
  {
    String selectStr1 = DEFAULT_SELECT_ALL;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    RespApp resp = null;
    try {resp = appApi.getApp(appId, appVersion, requireExecPerm, impersonationId, selectStr1, resourceTenant); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    // Postprocess the app
    TapisApp app = postProcessApp(resp.getResult());
    return app;
  }

  /**
   * Retrieve applications.
   *
   * Attributes named *notes* contain free form json and are represented as java Object type in generated TapisApp class.
   * Client code converts all *notes* attributes to String type, so each *notes* Object can safely be cast to String.
   * *notes* attributes are found at TapisApp top level and in collections under TapisApp.JobAttributes.ParameterSet
   *
   * Most recent version of each app is returned.
   *
   * @return Apps accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisApp> getApps() throws TapisClientException
  {
    return getApps(DEFAULT_SEARCH);
  }

  /**
   * Retrieve applications. Use search query parameter to limit results.
   * For example search=(id.like.MyApp*)~(enabled.eq.true)
   * By default most recent version of each app is returned.
   *
   * Attributes named *notes* contain free form json and are represented as java Object type in generated TapisApp class.
   * Client code converts all *notes* attributes to String type, so each *notes* Object can safely be cast to String.
   * *notes* attributes are found at TapisApp top level and in collections under TapisApp.JobAttributes.ParameterSet
   *
   * @param searchStr Search string. Empty or null to return all apps.
   * @return Apps accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisApp> getApps(String searchStr) throws TapisClientException
  {
    return getApps(searchStr, DEFAULT_SELECT_SUMMARY);
  }

  /**
   * Retrieve applications. Use search and select query parameters to limit results.
   * For example search=(id.like.MyApp*)~(enabled.eq.true)
   * By default most recent version of each app is returned.
   *
   * Attributes named *notes* contain free form json and are represented as java Object type in generated TapisApp class.
   * Client code converts all *notes* attributes to String type, so each *notes* Object can safely be cast to String.
   * *notes* attributes are found at TapisApp top level and in collections under TapisApp.JobAttributes.ParameterSet
   *
   * @param searchStr Search string. Empty or null to return all apps.
   * @param selectStr - Attributes to be included in result. For example select=id,owner
   * @return Apps accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisApp> getApps(String searchStr, String selectStr) throws TapisClientException
  {
    return getApps(searchStr, DEFAULT_LIMIT, DEFAULT_ORDERBY, DEFAULT_SKIP, DEFAULT_STARTAFTER, selectStr, false);
  }


  public List<TapisApp> getApps(String searchStr, int limit, String orderBy, int skip, String startAfter,
                                String selectStr, boolean showDeleted) throws TapisClientException
  {
    return getApps(searchStr, DEFAULT_LIST_TYPE_ENUM, DEFAULT_LIMIT, DEFAULT_ORDERBY, DEFAULT_SKIP, DEFAULT_STARTAFTER, selectStr, false);
  }

  /**
   * Get list using all supported parameters: searchStr, limit, orderBy, skip, startAfter, select, showDeleted
   * Retrieve applications. Use search and select query parameters to limit results.
   * For example search=(id.like.MyApp*)~(enabled.eq.true)
   * By default most recent version of each app is returned.
   *
   * Attributes named *notes* contain free form json and are represented as java Object type in generated TapisApp class.
   * Client code converts all *notes* attributes to String type, so each *notes* Object can safely be cast to String.
   * *notes* attributes are found at TapisApp top level and in collections under TapisApp.JobAttributes.ParameterSet
   *
   * @param searchStr Search string. Empty or null to return all apps.
   * @param selectStr - Attributes to be included in result. For example select=id,owner
   * @param showDeleted Indicates if Applications marked as deleted should be included.
   * @return Apps accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisApp> getApps(String searchStr, ListTypeEnum listTypeEnum, int limit, String orderBy, int skip,
                                String startAfter, String selectStr, boolean showDeleted) throws TapisClientException
  {
    RespApps resp = null;
    String selectStr1 = DEFAULT_SELECT_SUMMARY;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;

    try
    {
      resp = appApi.getApps(searchStr, listTypeEnum, limit, orderBy, skip, startAfter, DEFAULT_COMPUTETOTAL,
                            selectStr1, showDeleted);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    // Postprocess Apps in the result
    for (TapisApp app : resp.getResult()) postProcessApp(app);
    return resp.getResult();
  }

  /**
   * Get apps using search based on an array of strings representing an SQL-like WHERE clause
   * By default most recent version of each app is returned.
   *
   * Attributes named *notes* contain free form json and are represented as java Object type in generated TapisApp class.
   * Client code converts all *notes* attributes to String type, so each *notes* Object can safely be cast to String.
   * *notes* attributes are found at TapisApp top level and in collections under TapisApp.JobAttributes.ParameterSet
   *
   * @param req Request body specifying SQL-like search strings.
   * @return Apps accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisApp> searchApps(ReqSearchApps req, String selectStr) throws TapisClientException
  {
    RespApps resp = null;
    String selectStr1 = DEFAULT_SELECT_SUMMARY;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    try { resp = appApi.searchAppsRequestBody(req, DEFAULT_LIST_TYPE_ENUM, DEFAULT_LIMIT, DEFAULT_ORDERBY,
                                              DEFAULT_SKIP, DEFAULT_STARTAFTER, DEFAULT_COMPUTETOTAL, selectStr1); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    // Postprocess Apps in the result
    for (TapisApp app : resp.getResult()) postProcessApp(app);
    return resp.getResult();
  }

  /*
   * Check if resource is enabled
   * Simple wrapper for backward compatibility
   */
  public boolean isEnabled(String appId) throws TapisClientException
  {
    return isEnabled(appId, null);
  }

  /**
   * Check if resource is enabled
   *
   * @param appId Application to check
   * @param appVersion Optional, check both top level attr enabled and attribute versionEnabled.
   * @return boolean indicating if enabled
   * @throws TapisClientException - If api call throws an exception
   */
  public boolean isEnabled(String appId, String appVersion) throws TapisClientException
  {
    // Submit the request and return the response
    RespBoolean resp = null;
    try { resp = appApi.isEnabled(appId, appVersion); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getaBool() != null)
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
    try { permsApi.grantUserPerms(appId, userName, req); }
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
    try { resp = permsApi.getUserPerms(appId, userName); }
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
    try { permsApi.revokeUserPerms(appId, userName, req); }
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
    try { permsApi.revokeUserPerm(appId, userName, permission); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  // -----------------------------------------------------------------------
  // --------------------------- Utility Methods ---------------------------
  // -----------------------------------------------------------------------

  /**
   * Utility method to build a ReqPostApp object using attributes from a TapisApp.
   */
  public static ReqPostApp buildReqPostApp(TapisApp app)
  {
    if (app == null) return null;
    ReqPostApp rApp = new ReqPostApp();
    rApp.id(app.getId());
    rApp.version(app.getVersion());
    rApp.description(app.getDescription());
    rApp.owner(app.getOwner());
    rApp.enabled(app.getEnabled());
    rApp.runtime(app.getRuntime());
    rApp.runtimeVersion(app.getRuntimeVersion());
    rApp.runtimeOptions(app.getRuntimeOptions());
    rApp.containerImage(app.getContainerImage());
    rApp.jobType(app.getJobType());
    rApp.maxJobs(app.getMaxJobs()).maxJobsPerUser(app.getMaxJobsPerUser());
    rApp.strictFileInputs(app.getStrictFileInputs());
    rApp.jobAttributes(app.getJobAttributes());
    rApp.tags(app.getTags());
    // Notes requires special handling. It must be null or a JsonObject
    Object notes = app.getNotes();
    if (notes == null) rApp.notes(null);
    else if (notes instanceof String) rApp.notes(ClientTapisGsonUtils.getGson().fromJson((String) notes, JsonObject.class));
    else if (notes instanceof JsonObject) rApp.notes(notes);
    else rApp.notes(null);
    return rApp;
  }

  /**
   * Utility method to build a ReqPutApp object using attributes from a TapisApp.
   */
  public static ReqPutApp buildReqPutApp(TapisApp app)
  {
    if (app == null) return null;
    ReqPutApp rApp = new ReqPutApp();
    rApp.description(app.getDescription());
    rApp.runtime(app.getRuntime());
    rApp.runtimeVersion(app.getRuntimeVersion());
    rApp.runtimeOptions(app.getRuntimeOptions());
    rApp.containerImage(app.getContainerImage());
    rApp.maxJobs(app.getMaxJobs()).maxJobsPerUser(app.getMaxJobsPerUser());
    rApp.strictFileInputs(app.getStrictFileInputs());
    rApp.jobAttributes(app.getJobAttributes());
    rApp.tags(app.getTags());
    // Notes requires special handling. It must be null or a JsonObject
    Object notes = app.getNotes();
    if (notes == null) rApp.notes(null);
    else if (notes instanceof String) rApp.notes(ClientTapisGsonUtils.getGson().fromJson((String) notes, JsonObject.class));
    else if (notes instanceof JsonObject) rApp.notes(notes);
    else rApp.notes(null);
    return rApp;
  }

  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************
  /**
   * Do any client side postprocessing of a returned app.
   * This involves transforming any notes attributes from a LinkedTreeMap into a json string.
   *
   * Notes attributes are found at the top level and in AppArgSpec values in TapisApp.JobAttributes.ParameterSet.
   * Collections in parameterSet are appArgs, containerArgs and schedulerOptions.
   * @param app App to process
   * @return - Resulting App
   * @throws TapisClientException if a notes object is not of type LinkedTreeMap
   */
  TapisApp postProcessApp(TapisApp app) throws TapisClientException
  {
    // If no app then nothing to do.
    if (app == null) return app;
    String appId = app.getId();

    // Convert top level notes if present.
    Object topNotes = app.getNotes();
    if (topNotes != null) app.setNotes(convertLinkedTreeMapToString(topNotes, appId, "TopNotes"));

    // Convert any notes attributes in ArgSpec objects
    if (app.getJobAttributes() != null && app.getJobAttributes().getParameterSet() != null)
    {
      var appArgs = app.getJobAttributes().getParameterSet().getAppArgs();
      var containerArgs = app.getJobAttributes().getParameterSet().getContainerArgs();
      var schedulerOptions = app.getJobAttributes().getParameterSet().getSchedulerOptions();
      var envVariables = app.getJobAttributes().getParameterSet().getEnvVariables();
      // Process appArgs, containerArgs, schedulerOptions and envVariables
      if (appArgs != null)
      {
        for (AppArgSpec argSpec : appArgs)
        {
          argSpec.setNotes(convertLinkedTreeMapToString(argSpec.getNotes(), appId, "AppArg"));
        }
      }
      if (containerArgs != null)
      {
        for (AppArgSpec argSpec : containerArgs)
        {
          argSpec.setNotes(convertLinkedTreeMapToString(argSpec.getNotes(), appId, "ContainerArg"));
        }
      }
      if (schedulerOptions != null)
      {
        for (AppArgSpec argSpec : schedulerOptions)
        {
          argSpec.setNotes(convertLinkedTreeMapToString(argSpec.getNotes(), appId, "SchedulerOption"));
        }
      }
      if (envVariables != null)
      {
        for (KeyValuePair kvp : envVariables)
        {
          kvp.setNotes(convertLinkedTreeMapToString(kvp.getNotes(), appId, "EnvVariable"));
        }
      }
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

  /*
   * Convert a notes LinkedTreeMap to a json string.
   * If notes is not of type LinedTreeMap log an error and throw exception.
   */
  private static Object convertLinkedTreeMapToString(Object notes, String appId, String notesLabel)
          throws TapisClientException
  {
    // We expect notes to be of type com.google.gson.internal.LinkedTreeMap. Make sure that is the case.
    if (!(notes instanceof LinkedTreeMap<?,?>))
    {
      // Log an error and throw exception
      String msg =
         String.format("ERROR: Notes attribute in application not of type LinkedTreeMap. App: %s. Where found: %s. Notes: %s",
                       appId, notesLabel, notes);
      throw new TapisClientException(msg);
    }
    // Convert the gson LinkedTreeMap to a string.
    var lmap = (LinkedTreeMap<String, String>) notes;
    return  ClientTapisGsonUtils.getGson().toJson(lmap, linkedTreeMapType);
  }
}
