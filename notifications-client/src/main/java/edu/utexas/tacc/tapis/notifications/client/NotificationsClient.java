package edu.utexas.tacc.tapis.notifications.client;

import java.time.OffsetDateTime;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.Gson;
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
import edu.utexas.tacc.tapis.notifications.client.gen.model.ReqPostEvent;
import edu.utexas.tacc.tapis.notifications.client.gen.model.ReqPatchSubscription;
import edu.utexas.tacc.tapis.notifications.client.gen.model.ReqPostSubscription;
import edu.utexas.tacc.tapis.notifications.client.gen.model.ReqPutSubscription;
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
   * @param id Id of resource
   * @param req Request body specifying attributes
   * @return url pointing to updated resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String patchSubscription(String id, String version, ReqPatchSubscription req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = subscriptionsApi.patchSubscription(id, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update all attributes of a subscription
   * NOTE: Not all attributes are updatable.
   * See the helper method buildReqPutSubscription() for an example of how to build a pre-populated
   *   ReqPutSubscription instance from a TapisSubscription instance.
   *
   * @param id - Id of resource to be updated
   * @param req - Pre-populated ReqPutSubscription instance
   * @return url pointing to updated resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String putSubscription(String id, ReqPutSubscription req) throws TapisClientException
  {
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = subscriptionsApi.putSubscription(id, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Update enabled attribute to true.
   *
   * @param id Subscription id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int enableSubscription(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = subscriptionsApi.enableSubscription(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Update enabled attribute to false.
   *
   * @param id Subscription id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int disableSubscription(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = subscriptionsApi.disableSubscription(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Delete a subscription
   *
   * @param id Subscription id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int deleteSubscription(String id) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = subscriptionsApi.deleteSubscription(id); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Change subscription owner given the resource id and new owner id.
   *
   * @param id Subscription id
   * @param newOwnerName New owner id
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int changeSubscriptionOwner(String id, String newOwnerName) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = subscriptionsApi.changeSubscriptionOwner(id, newOwnerName); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  /**
   * Get a subscription, return all attributes
   *
   * @param subscriptionId Id of the subscription
   * @return The subscription or null if resource not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSubscription getSubscription(String subscriptionId) throws TapisClientException
  {
    return getSubscription(subscriptionId, DEFAULT_SELECT_ALL);
  }

  /**
   * Get a subscription using all supported parameters.
   *
   * @param subscriptionId Id of the subscription
   * @param selectStr - Attributes to be included in result. For example select=id,version,owner
   * @return The subscription or null if resource not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TapisSubscription getSubscription(String subscriptionId, String selectStr) throws TapisClientException
  {
    String selectStr1 = DEFAULT_SELECT_ALL;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    RespSubscription resp = null;
    try {resp = subscriptionsApi.getSubscription(subscriptionId, selectStr1); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    // Postprocess the subscription
    TapisSubscription subscription = postProcessSubscription(resp.getResult());
    return subscription;
  }

  /**
   * Retrieve subscriptions.
   *
   * @return Subscriptions accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSubscription> getSubscriptions() throws TapisClientException
  {
    return getSubscriptions(DEFAULT_SEARCH);
  }

  /**
   * Retrieve subscriptions. Use search query parameter to limit results.
   * For example search=(id.like.MySub*)~(enabled.eq.true)
   *
   * @param searchStr Search string. Empty or null to return all notifications.
   * @return Subscriptions accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSubscription> getSubscriptions(String searchStr) throws TapisClientException
  {
    return getSubscriptions(searchStr, DEFAULT_SELECT_SUMMARY);
  }

  /**
   * Retrieve subscriptions. Use search and select query parameters to limit results.
   * For example search=(id.like.MySub*)~(enabled.eq.true)
   *
   * @param searchStr Search string. Empty or null to return all notifications.
   * @param selectStr - Attributes to be included in result. For example select=id,owner
   * @return Subscriptions accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSubscription> getSubscriptions(String searchStr, String selectStr) throws TapisClientException
  {
    return getSubscriptions(searchStr, DEFAULT_LIMIT, DEFAULT_ORDERBY, DEFAULT_SKIP, DEFAULT_STARTAFTER,
                            selectStr, false);
  }

  /**
   * Get list using all supported parameters: searchStr, limit, orderBy, skip, startAfter, select, showDeleted
   * Retrieve subscriptions. Use search and select query parameters to limit results.
   * For example search=(id.like.MySub*)~(enabled.eq.true)
   *
   * @param searchStr Search string. Empty or null to return all notifications.
   * @param selectStr - Attributes to be included in result. For example select=id,owner
   * @return Subscriptions accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSubscription> getSubscriptions(String searchStr, int limit, String orderBy, int skip, String startAfter,
                                String selectStr, boolean showDeleted) throws TapisClientException
  {
    RespSubscriptions resp = null;
    String selectStr1 = DEFAULT_SELECT_SUMMARY;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;

    try
    {
      resp = subscriptionsApi.getSubscriptions(searchStr, limit, orderBy, skip, startAfter, DEFAULT_COMPUTETOTAL, selectStr1);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    // Postprocess Subscriptions in the result
    for (TapisSubscription subscription : resp.getResult()) postProcessSubscription(subscription);
    return resp.getResult();
  }

  /**
   * Get subscriptions using search based on an array of strings representing an SQL-like WHERE clause
   *
   * @param req Request body specifying SQL-like search strings.
   * @return Subscriptions accessible to the caller
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TapisSubscription> searchSubscriptions(ReqSearchSubscriptions req, String selectStr) throws TapisClientException
  {
    RespSubscriptions resp = null;
    String selectStr1 = DEFAULT_SELECT_SUMMARY;
    if (!StringUtils.isBlank(selectStr)) selectStr1 = selectStr;
    try { resp = subscriptionsApi.searchSubscriptionsRequestBody(req, DEFAULT_LIMIT, DEFAULT_ORDERBY, DEFAULT_SKIP, DEFAULT_STARTAFTER,
                                              DEFAULT_COMPUTETOTAL, selectStr1); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Check if resource is enabled
   *
   * @return boolean indicating if enabled
   * @throws TapisClientException - If api call throws an exception
   */
  public boolean isEnabled(String subscriptionId) throws TapisClientException
  {
    // Submit the request and return the response
    RespBoolean resp = null;
    try { resp = subscriptionsApi.isEnabled(subscriptionId); }
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
  // ------------------------- Events -------------------------------
  // -----------------------------------------------------------------------
  /**
   * Publish an event
   * See the helper method buildReqPostEvent() for an example of how to build a pre-populated
   *   ReqPostEvent instance.
   *
   * @param req Request body specifying attributes
   * @throws TapisClientException - If api call throws an exception
   */
  public void postEvent(ReqPostEvent req) throws TapisClientException
  {
    // Submit the request
    try { eventsApi.postEvent(req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
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
    rSubscription.id(subscription.getId());
    rSubscription.description(subscription.getDescription());
    rSubscription.owner(subscription.getOwner());
    rSubscription.enabled(subscription.getEnabled());
    rSubscription.typeFilter(subscription.getTypeFilter());
    rSubscription.subjectFilter(subscription.getSubjectFilter());
    rSubscription.deliveryMethods(subscription.getDeliveryMethods());
    // Notes requires special handling. It must be null or a JsonObject
    Object notes = subscription.getNotes();
    if (notes == null) rSubscription.notes(null);
    else if (notes instanceof String) rSubscription.notes(ClientTapisGsonUtils.getGson().fromJson((String) notes, JsonObject.class));
    else if (notes instanceof JsonObject) rSubscription.notes(notes);
    else rSubscription.notes(null);
    return rSubscription;
  }

  /**
   * Utility method to build a ReqPutSubscription object using attributes from a TapisSubscription.
   */
  public static ReqPutSubscription buildReqPutSubscription(TapisSubscription subscription)
  {
    if (subscription == null) return null;
    ReqPutSubscription rSubscription = new ReqPutSubscription();
    rSubscription.description(subscription.getDescription());
    rSubscription.typeFilter(subscription.getTypeFilter());
    rSubscription.subjectFilter(subscription.getSubjectFilter());
    rSubscription.deliveryMethods(subscription.getDeliveryMethods());
    // Notes requires special handling. It must be null or a JsonObject
    Object notes = subscription.getNotes();
    if (notes == null) rSubscription.notes(null);
    else if (notes instanceof String) rSubscription.notes(ClientTapisGsonUtils.getGson().fromJson((String) notes, JsonObject.class));
    else if (notes instanceof JsonObject) rSubscription.notes(notes);
    else rSubscription.notes(null);
    return rSubscription;
  }

  /**
   * Utility method to build a ReqPostEvent object.
   */
  public static ReqPostEvent buildReqPostEvent(String source, String type, String subject, OffsetDateTime timestamp)
  {
    // If any required attributes null then return null.
    if (StringUtils.isBlank(source) || StringUtils.isBlank(type) || timestamp == null) return null;
    ReqPostEvent rEvent = new ReqPostEvent();
    rEvent.source(source);
    rEvent.type(type);
    rEvent.subject(subject);
    rEvent.time(timestamp);
    return rEvent;
  }

  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************
  /**
   * Do any client side postprocessing of a returned resource.
   * Currently, this just involves transforming the notes attribute into a json string
   * @param subscription Subscription to process
   * @return - Resulting Subscription
   */
  TapisSubscription postProcessSubscription(TapisSubscription subscription)
  {
    // If we have a notes attribute convert it from a LinkedTreeMap to a string with json.
    if (subscription != null && subscription.getNotes() != null)
    {
      LinkedTreeMap lmap = (LinkedTreeMap) subscription.getNotes();
      JsonObject tmpNotes = ClientTapisGsonUtils.getGson().fromJson(lmap.toString(), JsonObject.class);
      subscription.setNotes(tmpNotes.toString());
    }
    return subscription;
  }
}
