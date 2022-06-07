package edu.utexas.tacc.tapis.notifications.client;

import java.util.List;

import com.google.gson.Gson;
import edu.utexas.tacc.tapis.notifications.client.gen.api.TestApi;
import org.apache.commons.lang3.StringUtils;

import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.ITapisClient;


import edu.utexas.tacc.tapis.notifications.client.gen.ApiClient;
import edu.utexas.tacc.tapis.notifications.client.gen.ApiException;
import edu.utexas.tacc.tapis.notifications.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.notifications.client.gen.api.EventsApi;
import edu.utexas.tacc.tapis.notifications.client.gen.api.SubscriptionsApi;
import edu.utexas.tacc.tapis.notifications.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.notifications.client.gen.model.RespBoolean;
import edu.utexas.tacc.tapis.notifications.client.gen.model.RespChangeCount;
import edu.utexas.tacc.tapis.notifications.client.gen.model.RespResourceUrl;
import edu.utexas.tacc.tapis.notifications.client.gen.model.RespSubscription;
import edu.utexas.tacc.tapis.notifications.client.gen.model.RespSubscriptions;
import edu.utexas.tacc.tapis.notifications.client.gen.model.Event;
import edu.utexas.tacc.tapis.notifications.client.gen.model.ReqPatchSubscription;
import edu.utexas.tacc.tapis.notifications.client.gen.model.ReqPostSubscription;
import edu.utexas.tacc.tapis.notifications.client.gen.model.ReqSearchSubscriptions;
import edu.utexas.tacc.tapis.notifications.client.gen.model.TapisSubscription;

import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_COMPUTETOTAL;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_LIMIT;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SEARCH;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SELECT_ALL;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SELECT_SUMMARY;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SKIP;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_ORDERBY;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_STARTAFTER;
import static edu.utexas.tacc.tapis.client.shared.Utils.DEFAULT_SUBSCRIPTION_TTL;
import static edu.utexas.tacc.tapis.client.shared.Utils.EMPTY_JSON;

/**
 * Class providing a convenient front-end to the automatically generated client code
 * for the Notifications Service REST API.
 * Underlying client classes with "gen" in the package name are generated by
 * openapi-generator each time a build is run.
 */
public class NotificationsClient implements ITapisClient
{
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************

  // Filter wildcard
  public static final String FILTER_WILDCARD = "*";

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
  private final SubscriptionsApi subscriptionsApi;
  private final EventsApi eventsApi;
  private final GeneralApi generalApi;
  private final TestApi testApi;

  // ************************************************************************
  // *********************** Constructors ***********************************
  // ************************************************************************

  /**
   * Default constructor which uses the compiled-in basePath based on the openapi spec
   *   used to autogenerate the client.
   */
  public NotificationsClient()
  {
    apiClient = new ApiClient();
    subscriptionsApi = new SubscriptionsApi(apiClient);
    eventsApi = new EventsApi(apiClient);
    generalApi = new GeneralApi(apiClient);
    testApi = new TestApi(apiClient);
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
  public NotificationsClient(String path, String jwt)
  {
    apiClient = new ApiClient();
    if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
    if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
    subscriptionsApi = new SubscriptionsApi(apiClient);
    eventsApi = new EventsApi(apiClient);
    generalApi = new GeneralApi(apiClient);
    testApi = new TestApi(apiClient);
  }

  // ************************************************************************
  // *********************** Public Methods *********************************
  // ************************************************************************

  // getApiClient: Return underlying ApiClient
  public ApiClient getApiClient() { return apiClient; }

  // Update base path for default client.
  public String getBasePath() { return apiClient.getBasePath(); }

  // Update base path for default client.
  public NotificationsClient setBasePath(String basePath) { apiClient.setBasePath(basePath); return this;}

  // Add http header to default client
  public NotificationsClient addDefaultHeader(String key, String val) { apiClient.addDefaultHeader(key, val); return this;}

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
  // ------------------------- Notifications -------------------------------
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

  // -----------------------------------------------------------------------
  // ------------------------- Subscriptions -------------------------------
  // -----------------------------------------------------------------------
  /**
   * Create a subscription
   * See the helper method buildReqPostSubscription() for an example of how to build a pre-populated
   *   ReqPostSubscription instance from a TapisSubscription instance.
   *
   * @param req Request body specifying attributes
   * @return url pointing to created resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String postSubscription(ReqPostSubscription req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = subscriptionsApi.postSubscription(req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update selected attributes of a subscription
   *
   * @param name name of resource
   * @param req Request body specifying attributes
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return url pointing to updated resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String patchSubscription(String name, ReqPatchSubscription req, String ownedBy) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = subscriptionsApi.patchSubscriptionByName(name, req, ownedBy); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update enabled attribute to true.
   *
   * @param name Subscription name
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int enableSubscription(String name, String ownedBy) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = subscriptionsApi.enableSubscription(name, ownedBy); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Update enabled attribute to false.
   *
   * @param name Subscription name
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int disableSubscription(String name, String ownedBy) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = subscriptionsApi.disableSubscription(name, ownedBy); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Check if subscription is enabled
   *
   * @param name Name of the subscription
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return boolean indicating if enabled
   * @throws TapisClientException - If api call throws an exception
   */
  public boolean isEnabled(String name, String ownedBy) throws TapisClientException
  {
    // Submit the request and return the response
    RespBoolean resp = null;
    try { resp = subscriptionsApi.isEnabled(name, ownedBy); }
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

  /**
   * Get a subscription by name, return all attributes
   *
   * @param name Name of the subscription
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return The subscription or null if resource not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSubscription getSubscriptionByName(String name, String ownedBy) throws TapisClientException
  {
    return getSubscriptionByName(name, DEFAULT_SELECT_ALL, ownedBy);
  }

  /**
   * Get a subscription by name using all supported parameters.
   *
   * @param name Name of the subscription
   * @param selectStr - Attributes to be included in result. For example select=name,version,owner
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return The subscription or null if resource not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSubscription getSubscriptionByName(String name, String selectStr, String ownedBy) throws TapisClientException
  {
    String selectStr1 = DEFAULT_SELECT_ALL;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    RespSubscription resp = null;
    try {resp = subscriptionsApi.getSubscriptionByName(name, selectStr1, ownedBy); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null) return null;
    return resp.getResult();
  }

  /**
   * Delete a subscription by name
   *
   * @param name Subscription name
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int deleteSubscriptionByName(String name, String ownedBy) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = subscriptionsApi.deleteSubscriptionByName(name, ownedBy); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Get a subscription by UUID, return all attributes
   *
   * @param uuid UUID of the subscription
   * @return The subscription or null if resource not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSubscription getSubscriptionByUuid(String uuid) throws TapisClientException
  {
    return getSubscriptionByUuid(uuid, DEFAULT_SELECT_ALL);
  }

  /**
   * Get a subscription by UUID using all supported parameters.
   *
   * @param uuid UUID of the subscription
   * @param selectStr - Attributes to be included in result. For example select=name,version,owner
   * @return The subscription or null if resource not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSubscription getSubscriptionByUuid(String uuid, String selectStr) throws TapisClientException
  {
    if (StringUtils.isBlank(uuid))
      throw new IllegalArgumentException("Invalid UUID. Subscription UUID may not be blank.");
    String selectStr1 = DEFAULT_SELECT_ALL;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    RespSubscription resp = null;
    try {resp = subscriptionsApi.getSubscriptionByUuid(uuid, selectStr1); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null) return null;
    return resp.getResult();
  }

  /**
   * Delete a subscription given the subscription UUID
   *
   * @param uuid - UUID of the subscription to be deleted.
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   * @throws IllegalArgumentException - If UUID is blank
   */
  public int deleteSubscriptionByUUID(String uuid) throws TapisClientException, IllegalArgumentException
  {
    if (StringUtils.isBlank(uuid))
      throw new IllegalArgumentException("Invalid UUID. Subscription UUID may not be blank.");
    RespChangeCount resp = null;
    try { resp = subscriptionsApi.deleteSubscriptionByUuid(uuid); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Retrieve subscriptions owned by requesting user.
   *
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return Subscriptions accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSubscription> getSubscriptions(String ownedBy) throws TapisClientException
  {
    return getSubscriptions(DEFAULT_SEARCH, ownedBy);
  }

  /**
   * Retrieve subscriptions. Use search query parameter to limit results.
   * For example search=(name.like.MySub*)~(enabled.eq.true)
   *
   * @param searchStr Search string. Empty or null to return all notifications.
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return Subscriptions accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSubscription> getSubscriptions(String searchStr, String ownedBy) throws TapisClientException
  {
    return getSubscriptions(searchStr, DEFAULT_SELECT_SUMMARY, ownedBy);
  }

  /**
   * Retrieve subscriptions. Use search and select query parameters to limit results.
   * For example search=(name.like.MySub*)~(enabled.eq.true)
   *
   * @param searchStr Search string. Empty or null to return all notifications.
   * @param selectStr - Attributes to be included in result. For example select=name,owner
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return Subscriptions accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSubscription> getSubscriptions(String searchStr, String selectStr, String ownedBy) throws TapisClientException
  {
    return getSubscriptions(searchStr, DEFAULT_LIMIT, DEFAULT_ORDERBY, DEFAULT_SKIP, DEFAULT_STARTAFTER, selectStr,
                            ownedBy, false);
  }

  /**
   * Get list using all supported parameters: searchStr, limit, orderBy, skip, startAfter, select, ownedBy, anyOwner
   * Retrieve subscriptions. Use search and select query parameters to limit results.
   * For example search=(name.like.MySub*)~(enabled.eq.true)
   *
   * @param searchStr Search string. Empty or null to return all notifications.
   * @param selectStr - Attributes to be included in result. For example select=name,owner
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @param anyOwner - If true retrieve all subscriptions owned by any user. ownedBy will be ignored.
   * @return Subscriptions accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSubscription> getSubscriptions(String searchStr, int limit, String orderBy, int skip, String startAfter,
                                String selectStr, String ownedBy, boolean anyOwner) throws TapisClientException
  {
    RespSubscriptions resp = null;
    String selectStr1 = DEFAULT_SELECT_SUMMARY;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;

    try
    {
      resp = subscriptionsApi.getSubscriptions(searchStr, limit, orderBy, skip, startAfter, DEFAULT_COMPUTETOTAL,
                                               selectStr1, ownedBy, anyOwner);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null) return null;
    return resp.getResult();
  }

  /**
   * Get subscriptions using search based on an array of strings representing an SQL-like WHERE clause
   *
   * @param req Request body specifying SQL-like search strings.
   * @param ownedBy - Use specified user in place of the requesting user. Leave null or blank to use requesting user.
   * @return Subscriptions accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSubscription> searchSubscriptions(ReqSearchSubscriptions req, String selectStr, String ownedBy,
                                                     boolean anyOwner)
          throws TapisClientException
  {
    RespSubscriptions resp = null;
    String selectStr1 = DEFAULT_SELECT_SUMMARY;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    try { resp = subscriptionsApi.searchSubscriptionsRequestBody(req, DEFAULT_LIMIT, DEFAULT_ORDERBY, DEFAULT_SKIP, DEFAULT_STARTAFTER,
                                                                 DEFAULT_COMPUTETOTAL, selectStr1, ownedBy); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Get all subscriptions owned by requesting user and where subjectFilter matches a specific subject.
   * Use flag anyOwner=true to get all subscriptions regardless of owner.
   * Use ownedBy to see subscriptions owned by a specific user other than the requesting user.
   * Note that anyOwner=true has precedence over ownedBy.
   *
   * @param subject a specific subject. Wildcard not allowed.
   * @param limit - indicates maximum number of results to be included, -1 for unlimited
   * @param orderBy - orderBy for sorting, e.g. orderBy=created(desc).
   * @param skip - number of results to skip (may not be used with startAfter)
   * @param startAfter - where to start when sorting, e.g. limit=10&orderBy=name(asc)&startAfter=101 (may not be used with skip)
   * @param ownedBy - Get subscriptions owned by a user other than the requesting user. Ignored if anyOwner=true
   * @param anyOwner - If true retrieve all subscriptions owned by any user. ownedBy will be ignored.
   * @return - Full response from the api call, including metadata and list of subscriptions.
   * @throws TapisClientException - If api call throws an exception
   * @throws IllegalArgumentException - If subject is empty or the wildcard string
   */
  public RespSubscriptions getSubscriptionsBySubject(String subject, int limit, String orderBy, int skip,
                                                     String startAfter, String ownedBy, boolean anyOwner)
          throws TapisClientException, IllegalArgumentException
  {
    if (StringUtils.isBlank(subject) || FILTER_WILDCARD.equals(subject))
      throw new IllegalArgumentException("Invalid subject. subject may not be empty or equal to '*'");

    //Build a search string for subjectFilter
    String searchStr = String.format("search=(subject_filter.eq.%s)", subject);
    RespSubscriptions resp = null;
    String selectStr = DEFAULT_SELECT_ALL;
    try
    {
      resp = subscriptionsApi.getSubscriptions(searchStr, limit, orderBy, skip, startAfter, DEFAULT_COMPUTETOTAL,
                                               selectStr, ownedBy, anyOwner);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    return resp;
  }

  /**
   * Get all subscriptions where subjectFilter matches a specific subject.
   *
   * @param subject a specific subject. Wildcard not allowed.
   * @param limit - indicates maximum number of results to be included, -1 for unlimited
   * @param orderBy - orderBy for sorting, e.g. orderBy=created(desc).
   * @param skip - number of results to skip (may not be used with startAfter)
   * @param startAfter - where to start when sorting, e.g. limit=10&orderBy=name(asc)&startAfter=101 (may not be used with skip)
   * @return - Full response from the api call, including metadata and list of subscriptions.
   * @throws TapisClientException - If api call throws an exception
   * @throws IllegalArgumentException - If subjectFilter is empty or the wildcard string
   */
  public RespSubscriptions getSubscriptionsBySubjectForAllOwners(String subject, int limit, String orderBy,
                                                                 int skip, String startAfter)
          throws TapisClientException, IllegalArgumentException
  {
    String ownedBy = null;
    boolean anyOwnerTrue = true;
    return getSubscriptionsBySubject(subject, limit, orderBy, skip, startAfter, ownedBy, anyOwnerTrue);
  }

  /**
   * Delete all subscriptions where subjectFilter matches a specific subject
   *   and subscription is owned by any user.
   *
   * @param subject a specific subject. Wildcard not allowed.
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   * @throws IllegalArgumentException - If subject is empty or the wildcard string
   */
  public int deleteSubscriptionsBySubjectForAllOwners(String subject)
          throws TapisClientException, IllegalArgumentException
  {
    if (StringUtils.isBlank(subject) || FILTER_WILDCARD.equals(subject))
      throw new IllegalArgumentException("Invalid subject. subject may not be empty or equal to '*'");

    String ownedBy = null;
    boolean anyOwnerTrue = true;
    RespChangeCount resp = null;
    try { resp = subscriptionsApi.deleteSubscriptionsBySubject(subject, ownedBy, anyOwnerTrue); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  // -----------------------------------------------------------------------
  // ------------------------- Events -------------------------------
  // -----------------------------------------------------------------------
  /**
   * Publish an event
   * See the helper method buildReqPostEvent() for an example of how to build a pre-populated
   *   Event instance.
   *
   * @param req Request body specifying attributes
   * @throws TapisClientException - If api call throws an exception
   */
  public void postEvent(Event req) throws TapisClientException
  {
    // Submit the request
    try { eventsApi.postEvent(req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  // -----------------------------------------------------------------------
  // ------------------------- TestSequence -------------------------------
  // -----------------------------------------------------------------------
  /**
   * Start a test sequence
   *
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSubscription beginTestSequence(Integer subscriptionTTL) throws TapisClientException
  {
    RespSubscription resp = null;
    Integer subscrTTL = subscriptionTTL;
    if (subscriptionTTL == null) subscrTTL = DEFAULT_SUBSCRIPTION_TTL;
    // Submit the request
    // Json body is not used but appears to be required when generating the client from the openapi spec.
    try { resp = testApi.beginTestSequence(subscrTTL, EMPTY_JSON); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    if (resp != null && resp.getResult() != null)
    {
      return resp.getResult();
    }
    else
    {
      throw new TapisClientException("beginTestSequence did not return a result");
    }
  }

  // -----------------------------------------------------------------------
  // --------------------------- Permissions -------------------------------
  // -----------------------------------------------------------------------

  // -----------------------------------------------------------------------
  // --------------------------- Utility Methods ---------------------------
  // -----------------------------------------------------------------------

  /**
   * Utility method to build a ReqPostSubscription object using attributes from a TapisSubscription.
   */
  public static ReqPostSubscription buildReqPostSubscription(TapisSubscription subscription)
  {
    if (subscription == null) return null;
    ReqPostSubscription rSubscription = new ReqPostSubscription();
    rSubscription.name(subscription.getName());
    rSubscription.description(subscription.getDescription());
    rSubscription.owner(subscription.getOwner());
    rSubscription.enabled(subscription.getEnabled());
    rSubscription.typeFilter(subscription.getTypeFilter());
    rSubscription.subjectFilter(subscription.getSubjectFilter());
    rSubscription.deliveryTargets(subscription.getDeliveryTargets());
    rSubscription.ttlMinutes(subscription.getTtlMinutes());
    return rSubscription;
  }

  /**
   * Utility method to build an Event object.
   */
  public static Event buildEvent(String source, String type, String subject, String data, String seriesId,
                                 String timestamp)
  {
    // If any required attributes null then return null.
    if (StringUtils.isBlank(source) || StringUtils.isBlank(type) || timestamp == null) return null;
    Event rEvent = new Event();
    rEvent.source(source);
    rEvent.type(type);
    rEvent.subject(subject);
    rEvent.data(data);
    rEvent.seriesId(seriesId);
    rEvent.timestamp(timestamp);
    return rEvent;
  }

  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************
 }
