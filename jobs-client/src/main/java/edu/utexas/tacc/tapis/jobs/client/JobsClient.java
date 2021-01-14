package edu.utexas.tacc.tapis.jobs.client;

import org.apache.commons.lang3.StringUtils;

import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.jobs.client.gen.ApiClient;
import edu.utexas.tacc.tapis.jobs.client.gen.ApiException;
import edu.utexas.tacc.tapis.jobs.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.jobs.client.gen.api.JobsApi;
import edu.utexas.tacc.tapis.jobs.client.gen.model.Job;
import edu.utexas.tacc.tapis.jobs.client.gen.model.ReqSubmitJob;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespProbe;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespSubmitJob;

public class JobsClient 
{
    /* **************************************************************************** */
    /*                                   Constants                                  */
    /* **************************************************************************** */
    // Response status.
    public static final String STATUS_SUCCESS = "success";
    
    // Header keys for tapis.
    public static final String TAPIS_JWT_HEADER  = "X-Tapis-Token";
    public static final String TAPIS_JWT_TENANT  = "X-Tapis-Tenant";
    public static final String TAPIS_JWT_USER    = "X-Tapis-User";
    public static final String TAPIS_HASH_HEADER = "X-Tapis-User-Token-Hash";
    
    // Configuration defaults.
    private static final String JOBS_CLIENT_USER_AGENT = "JobsClient";
    
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
    public JobsClient() {this(null, null);}
    
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
     * 
     * The jwt is the base64url representation of a Tapis JWT.  If not null or empty,
     * the TAPIS_JWT_HEADER key will be set to the jwt value. 
     * 
     * The user-agent is automatically set to JobsClient.
     * 
     * @param path the base path 
     */
    public JobsClient(String path, String jwt) 
    {
    	// Create actual client.
    	_apiClient = new ApiClient();
    	
        // Process input.
        if (!StringUtils.isBlank(path)) _apiClient.setBasePath(path);
        if (!StringUtils.isBlank(jwt))  _apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
        
        // Other defaults.
        _apiClient.setUserAgent(JOBS_CLIENT_USER_AGENT);
    }
    
    /* **************************************************************************** */
    /*                                Utility Methods                               */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* setBasePath:                                                                 */
    /* ---------------------------------------------------------------------------- */
    public JobsClient setBasePath(String path)
    {
        _apiClient.setBasePath(path);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* addDefaultHeader:                                                            */
    /* ---------------------------------------------------------------------------- */
    public JobsClient addDefaultHeader(String key, String value)
    {
        _apiClient.addDefaultHeader(key, value);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setUserAgent:                                                                */
    /* ---------------------------------------------------------------------------- */
    public JobsClient setUserAgent(String userAgent) 
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
    public JobsClient setConnectTimeout(int millis)
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
    public JobsClient setReadTimeout(int millis)
    {
        _apiClient.setReadTimeout(millis);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setDebugging:                                                                */
    /* ---------------------------------------------------------------------------- */
    public JobsClient setDebugging(boolean debugging) 
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
    
    /* **************************************************************************** */
    /*                              Public Jobs Methods                             */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* submitJob:                                                                   */
    /* ---------------------------------------------------------------------------- */
    public Job submitJob(ReqSubmitJob reqSubmitJob)
     throws TapisClientException
    {
        RespSubmitJob resp = null;
        try {
            // Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
            resp = jobsApi.submitJob(reqSubmitJob, false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp == null ? null : resp.getResult();
    }

    /* ---------------------------------------------------------------------------- */
    /* resubmitJob:                                                                 */
    /* ---------------------------------------------------------------------------- */
    public Job resubmitJob(String jobUuid)
     throws TapisClientException
    {
        RespSubmitJob resp = null;
        try {
            // Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
            resp = jobsApi.resubmitJob(jobUuid, false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp == null ? null : resp.getResult();
    }

    /* **************************************************************************** */
    /*                            Public General Methods                            */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* hello:                                                                       */
    /* ---------------------------------------------------------------------------- */
    public String hello()
     throws TapisClientException
    {
        // Make the REST call.
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            var generalApi = new GeneralApi(_apiClient);
            resp = generalApi.sayHello(false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value as a string.
        Object obj = resp.getResult();
        return obj == null ? null : obj.toString();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* checkHealth:                                                                 */
    /* ---------------------------------------------------------------------------- */
    public String checkHealth()
     throws TapisClientException
    {
        // Make the REST call.
        RespProbe resp = null;
        try {
            // Get the API object using default networking.
            var generalApi = new GeneralApi(_apiClient);
            resp = generalApi.checkHealth();
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value as a string.
        Object obj = resp.getResult();
        return obj == null ? null : obj.toString();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* ready:                                                                       */
    /* ---------------------------------------------------------------------------- */
    public String ready()
     throws TapisClientException
    {
        // Make the REST call.
        RespProbe resp = null;
        try {
            // Get the API object using default networking.
            var generalApi = new GeneralApi(_apiClient);
            resp = generalApi.ready();
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value as a string.
        Object obj = resp.getResult();
        return obj == null ? null : obj.toString();
    }
}