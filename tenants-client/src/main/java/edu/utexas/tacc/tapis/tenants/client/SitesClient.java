package edu.utexas.tacc.tapis.tenants.client;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;
import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.tenants.client.gen.ApiClient;
import edu.utexas.tacc.tapis.tenants.client.gen.ApiException;
import edu.utexas.tacc.tapis.tenants.client.gen.api.SitesApi;
import edu.utexas.tacc.tapis.tenants.client.gen.model.Site;

public class SitesClient 
{
    /* **************************************************************************** */
    /*                                   Constants                                  */
    /* **************************************************************************** */
    // Configuration defaults.
    private static final String SITES_CLIENT_USER_AGENT = "SitesClient";

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
    public SitesClient() {this(null);}
    
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
    public SitesClient(String path)
    {
    	_apiClient = new ApiClient();
    	if (!StringUtils.isBlank(path)) _apiClient.setBasePath(path);
        
        // Other defaults.
        _apiClient.setUserAgent(SITES_CLIENT_USER_AGENT);
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
    /** Get all Site info. */
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
    public SitesClient setBasePath(String path)
    {
        _apiClient.setBasePath(path);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* addDefaultHeader:                                                            */
    /* ---------------------------------------------------------------------------- */
    public SitesClient addDefaultHeader(String key, String value)
    {
        _apiClient.addDefaultHeader(key, value);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setUserAgent:                                                                */
    /* ---------------------------------------------------------------------------- */
    public SitesClient setUserAgent(String userAgent) 
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
    public SitesClient setConnectTimeout(int millis)
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
    public SitesClient setReadTimeout(int millis)
    {
        _apiClient.setReadTimeout(millis);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setDebugging:                                                                */
    /* ---------------------------------------------------------------------------- */
    public SitesClient setDebugging(boolean debugging) 
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
