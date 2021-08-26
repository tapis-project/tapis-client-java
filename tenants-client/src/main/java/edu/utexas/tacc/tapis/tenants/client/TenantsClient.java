package edu.utexas.tacc.tapis.tenants.client;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.ITapisClient;
import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.tenants.client.gen.ApiClient;
import edu.utexas.tacc.tapis.tenants.client.gen.ApiException;
import edu.utexas.tacc.tapis.tenants.client.gen.api.SitesApi;
import edu.utexas.tacc.tapis.tenants.client.gen.api.TenantsApi;
import edu.utexas.tacc.tapis.tenants.client.gen.model.Site;
import edu.utexas.tacc.tapis.tenants.client.gen.model.Tenant;

/** Client wrapper for Tenant and Site APIs served by the Tenants Service.
 */
public class TenantsClient 
 implements ITapisClient
{
    /* **************************************************************************** */
    /*                                   Constants                                  */
    /* **************************************************************************** */
    // Configuration defaults.
    private static final String TENANTS_CLIENT_USER_AGENT = "TenantsClient";

    // Response serializer.
    private static final Gson _gson = ClientTapisGsonUtils.getGson();

    /* **************************************************************************** */
    /*                                    Fields                                    */
    /* **************************************************************************** */
    // This client instance's underlying generated client.
    private final ApiClient _apiClient;
    
    /* **************************************************************************** */
    /*                                 Constructors                                 */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* constructor:                                                                 */
    /* ---------------------------------------------------------------------------- */
    /** Constructor that uses the compiled-in basePath value in ApiClient.  This
     * constructor is only appropriate for test code.
     */ 
    public TenantsClient() {this(null);}
    
    /* ---------------------------------------------------------------------------- */
    /* constructor:                                                                 */
    /* ---------------------------------------------------------------------------- */
    /** Constructor that overrides the compiled-in basePath value in ApiClient.  This
     * constructor typically used in production.
     * 
     * The path includes the URL prefix up to and including the service root.  By
     * default this value is http://localhost:8080/v3.  In more production-like
     * environments the protocol will be https and the host/port will be specific to 
     * that environment.  For example, a development environment might define its
     * base url as https://tenant1.develop.tapis.io/v3.
     */
    public TenantsClient(String path)
    {
    	_apiClient = new ApiClient();
    	if (!StringUtils.isBlank(path)) _apiClient.setBasePath(path);
        
        // Other defaults.
        _apiClient.setUserAgent(TENANTS_CLIENT_USER_AGENT);
    }
 
    /* **************************************************************************** */
    /*                            Public Tenants Methods                            */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* getTenant:                                                                   */
    /* ---------------------------------------------------------------------------- */
    /** Get all Tenant info given tenant name.  */
    public Tenant getTenant(String tenantName) throws TapisClientException
    {
      // Make the service call.
      Map resp = null;
      try { 
          var tenantsApi = new TenantsApi(_apiClient);
          resp = (Map) tenantsApi.getTenant(tenantName); 
      }
      catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
      catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
      
      // Marshal only the result from the map.
      String json = _gson.toJson(resp.get("result"));
      if (StringUtils.isBlank(json)) return null;
      return _gson.fromJson(json, Tenant.class);
    }

    /* ---------------------------------------------------------------------------- */
    /* getTenants:                                                                  */
    /* ---------------------------------------------------------------------------- */
    /** Get all Tenant info. */
    public List<Tenant> getTenants() throws TapisClientException
    {
        return getTenants(null, null);
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getTenants:                                                                  */
    /* ---------------------------------------------------------------------------- */
    /** Get a possibly limited number of Tenant objects. */
    public List<Tenant> getTenants(Integer limit, Integer offset) throws TapisClientException
    {
      // Make the service call.
      Map resp = null;
      try { 
          var tenantsApi = new TenantsApi(_apiClient);
          resp = (Map) tenantsApi.listTenants(limit, offset); 
      }
      catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
      catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

      // Marshal only the result from the map.
      String json = _gson.toJson(resp.get("result"));
      if (StringUtils.isBlank(json)) return null;
      Type tenantListType = new TypeToken<List<Tenant>>(){}.getType();
      List<Tenant> list = _gson.fromJson(json, tenantListType);
      return list;
    }
    
    /* **************************************************************************** */
    /*                              Public Site Methods                             */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* getSite:                                                                     */
    /* ---------------------------------------------------------------------------- */
    /** Get all Site info given site id. */
    public Site getSite(String siteId) throws TapisClientException
    {
      // Make the service call.
      Map resp = null;
      try { 
          var sitesApi = new SitesApi(_apiClient);
          resp = (Map) sitesApi.getSite(siteId); 
      }
      catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
      catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
      
      // Marshal only the result from the map.
      String json = _gson.toJson(resp.get("result"));
      if (StringUtils.isBlank(json)) return null;
      return _gson.fromJson(json, Site.class);
    }

    /* ---------------------------------------------------------------------------- */
    /* getSites:                                                                    */
    /* ---------------------------------------------------------------------------- */
    /** Get all Site info. */
    public List<Site> getSites() throws TapisClientException
    {
        return getSites(null, null);
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getSites:                                                                    */
    /* ---------------------------------------------------------------------------- */
    /** Get a possibly limited number Site objects. */
    public List<Site> getSites(Integer limit, Integer offset) throws TapisClientException
    {
      // Make the service call.
      Map resp = null;
      try { 
          var sitesApi = new SitesApi(_apiClient);
          resp = (Map) sitesApi.listSites(limit, offset); 
      }
      catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
      catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

      // Marshal only the result from the map.
      String json = _gson.toJson(resp.get("result"));
      if (StringUtils.isBlank(json)) return null;
      Type siteListType = new TypeToken<List<Site>>(){}.getType();
      List<Site> list = _gson.fromJson(json, siteListType);
      return list;
    }
    
    /* **************************************************************************** */
    /*                                Utility Methods                               */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* setBasePath:                                                                 */
    /* ---------------------------------------------------------------------------- */
    public TenantsClient setBasePath(String path)
    {
        _apiClient.setBasePath(path);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* addDefaultHeader:                                                            */
    /* ---------------------------------------------------------------------------- */
    public TenantsClient addDefaultHeader(String key, String value)
    {
        _apiClient.addDefaultHeader(key, value);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setUserAgent:                                                                */
    /* ---------------------------------------------------------------------------- */
    public TenantsClient setUserAgent(String userAgent) 
    {
        _apiClient.setUserAgent(userAgent);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setConnectTimeout:                                                           */
    /* ---------------------------------------------------------------------------- */
    /** Set the connection timeout
     * 
     * @param millis the connection timeout in milliseconds; 0 means forever.
     * @return this object
     */
    public TenantsClient setConnectTimeout(int millis)
    {
        _apiClient.setConnectTimeout(millis);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setReadTimeout:                                                              */
    /* ---------------------------------------------------------------------------- */
    /** Set the read timeout
     * 
     * @param millis the read timeout in milliseconds; 0 means forever.
     * @return this object
     */
    public TenantsClient setReadTimeout(int millis)
    {
        _apiClient.setReadTimeout(millis);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setDebugging:                                                                */
    /* ---------------------------------------------------------------------------- */
    public TenantsClient setDebugging(boolean debugging) 
    {
        _apiClient.setDebugging(debugging);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getConnectTimeout:                                                           */
    /* ---------------------------------------------------------------------------- */
    /** Get the connection timeout.
     * 
     * @return the connection timeout in milliseconds
     */
    public int getConnectTimeout()
    {
        return _apiClient.getConnectTimeout();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getReadTimeout:                                                              */
    /* ---------------------------------------------------------------------------- */
    /** Get the read timeout.
     * 
     * @return read timeout in milliseconds
     */
    public int getReadTimeout()
    {
        return _apiClient.getReadTimeout();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* isDebugging:                                                                 */
    /* ---------------------------------------------------------------------------- */
    public boolean isDebugging() 
    {
        return _apiClient.isDebugging();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* close:                                                                       */
    /* ---------------------------------------------------------------------------- */
    /** Close connections and stop threads that can sometimes prevent JVM shutdown.
     */
    public void close()
    {
        try {
            // Best effort attempt to shut things down.
            var okClient = _apiClient.getHttpClient();
            if (okClient != null) {
                var pool = okClient.connectionPool();
                if (pool != null) pool.evictAll();
            }
        } catch (Exception e) {}      
    }
}
