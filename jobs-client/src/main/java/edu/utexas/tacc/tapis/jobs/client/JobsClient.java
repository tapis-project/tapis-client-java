package edu.utexas.tacc.tapis.jobs.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.utexas.tacc.tapis.client.shared.ITapisClient;
import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.jobs.client.gen.ApiClient;
import edu.utexas.tacc.tapis.jobs.client.gen.ApiException;
import edu.utexas.tacc.tapis.jobs.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.jobs.client.gen.api.JobsApi;
import edu.utexas.tacc.tapis.jobs.client.gen.api.ShareApi;
import edu.utexas.tacc.tapis.jobs.client.gen.api.SubscriptionsApi;
import edu.utexas.tacc.tapis.jobs.client.gen.model.FileInfo;
import edu.utexas.tacc.tapis.jobs.client.gen.model.Job;
import edu.utexas.tacc.tapis.jobs.client.gen.model.JobCancelDisplay;
import edu.utexas.tacc.tapis.jobs.client.gen.model.JobHideDisplay;
import edu.utexas.tacc.tapis.jobs.client.gen.model.JobHistoryDisplayDTO;
import edu.utexas.tacc.tapis.jobs.client.gen.model.JobListDTO;
import edu.utexas.tacc.tapis.jobs.client.gen.model.JobShareDisplay;
import edu.utexas.tacc.tapis.jobs.client.gen.model.JobShareListDTO;
import edu.utexas.tacc.tapis.jobs.client.gen.model.JobStatusDisplay;
import edu.utexas.tacc.tapis.jobs.client.gen.model.JobUnShareDisplay;
import edu.utexas.tacc.tapis.jobs.client.gen.model.ReqShareJob;
import edu.utexas.tacc.tapis.jobs.client.gen.model.ReqSubmitJob;
import edu.utexas.tacc.tapis.jobs.client.gen.model.ReqSubscribe;
import edu.utexas.tacc.tapis.jobs.client.gen.model.ReqUserEvent;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespCancelJob;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespGetJob;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespGetJobList;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespGetJobOutputList;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespGetJobShareList;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespGetJobStatus;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespGetSubscriptions;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespHideJob;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespJobHistory;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespJobSearchAllAttributes;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespProbe;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespResourceUrl;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespShareJob;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespSubmitJob;
import edu.utexas.tacc.tapis.jobs.client.gen.model.RespUnShareJob;
import edu.utexas.tacc.tapis.jobs.client.gen.model.ResultChangeCount;
import edu.utexas.tacc.tapis.jobs.client.gen.model.TapisSubscription;
import okhttp3.Call;
import okhttp3.Response;

public class JobsClient 
 implements ITapisClient
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
    
    // The distinguished string value of an environment variable that is interpreted 
    // as the variable being unset (as opposed to set to the empty string, which is 
    // a valid value).
    public static final String TAPIS_ENV_VAR_UNSET = "!tapis_not_set";
    
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

    /* ---------------------------------------------------------------------------- */
    /* getJob:                                                                      */
    /* ---------------------------------------------------------------------------- */
    public Job getJob(String jobUuid)
     throws TapisClientException
    {
        RespGetJob resp = null;
        try {
            // Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
            resp = jobsApi.getJob(jobUuid, false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp == null ? null : resp.getResult();
    }

    /* ---------------------------------------------------------------------------- */
    /* getJobStatus:                                                                      */
    /* ---------------------------------------------------------------------------- */
    public JobStatusDisplay getJobStatus(String jobUuid)
     throws TapisClientException
    {
        RespGetJobStatus resp = null;
        try {
            // Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
            resp = jobsApi.getJobStatus(jobUuid,false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp == null ? null : resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* sendEvent:                                                                   */
    /* ---------------------------------------------------------------------------- */
    public String sendEvent(String jobUuid, ReqUserEvent reqSubmitJob)
     throws TapisClientException
    {
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
            resp = jobsApi.sendEvent(jobUuid, reqSubmitJob, false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value as a string.
        Object obj = resp.getResult();
        return obj == null ? null : obj.toString();
    }

    /* ---------------------------------------------------------------------------- */
    /* cancelJob:                                                                   */
    /* ---------------------------------------------------------------------------- */
    public JobCancelDisplay cancelJob(String jobUuid)
     throws TapisClientException
    {
        RespCancelJob resp = null;
        try {
            // Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
            resp = jobsApi.cancelJob(jobUuid, false);
            		
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp == null ? null : resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getJobHistory:                                                               */
    /* ---------------------------------------------------------------------------- */
   public List<JobHistoryDisplayDTO> getJobHistory(String jobUuid, int limit, int skip)
     throws TapisClientException
    {
        RespJobHistory resp = null;
        try {
            // Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
            resp = jobsApi.getJobHistory(jobUuid, limit, skip, false);
            		
            		
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp == null ? null : resp.getResult();
    }
    /* ---------------------------------------------------------------------------- */
    /* getJobList:                                                                  */
    /* ---------------------------------------------------------------------------- */
    public List<JobListDTO> getJobList(int limit, int skip, int startAfter, String orderBy, 
                                       boolean computeTotal, String listType)
     throws TapisClientException
    {
    	RespGetJobList resp = null;
        try {
            // Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
            resp = jobsApi.getJobList(limit, skip, startAfter, orderBy, computeTotal, listType, false);
            		
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp == null ? null : resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getJobSearchList:                                                            */
    /* ---------------------------------------------------------------------------- */
    public List<Job> getJobSearchList(int limit, int skip, int startAfter, String orderBy, 
                                      boolean computeTotal, String select, String listType)
     throws TapisClientException
    {
    	RespJobSearchAllAttributes resp = null;
        try {
            // Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
            resp = jobsApi.getJobSearchList(limit, skip, startAfter, orderBy, computeTotal, select, listType, false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp == null ? null : resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getJobOutputList:                                                            */
    /* ---------------------------------------------------------------------------- */
    public List<FileInfo> getJobOutputList(String jobUuid, String path, boolean allowIfRunning, int limit, int skip)
     throws TapisClientException
    {
    	RespGetJobOutputList resp = null;
        try {
            // Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
            resp = jobsApi.getJobOutputList(jobUuid, path, limit, skip, allowIfRunning=false,false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp == null ? null : resp.getResult();
    }
    /* ---------------------------------------------------------------------------- */
    /* getJobOutputDownload:                                                        */
    /* ---------------------------------------------------------------------------- */
    public InputStream getJobOutputDownload(String jobUuid, String path, boolean compress, String format, boolean allowIfRunning) throws TapisClientException, IOException {
		
    	       
    	InputStream stream = null;
    	
    	
       
        	// Get the API object using default networking.
            var jobsApi = new JobsApi(_apiClient);
             try {
				Call outputFile = jobsApi.getJobOutputDownloadCall(jobUuid, path, compress, format, allowIfRunning=false,false,null);
						Response response =  outputFile.execute();
		          stream = response.body().byteStream();
             } catch (ApiException e) {
				Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);
			}
           return stream;
       
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getJobSearchListByPostSqlStr:                                                */
    /* ---------------------------------------------------------------------------- */
    public List<Job> getJobSearchListByPostSqlStr(String [] req, int limit, int skip, 
    		int startAfter, String orderBy, boolean computeTotal, String select, String listType)
     throws TapisClientException
    {

    	RespJobSearchAllAttributes resp = null;
        try {  var jobsApi = new JobsApi(_apiClient);
        	resp = jobsApi.getJobSearchListByPostSqlStr(limit, skip, startAfter, orderBy, computeTotal,
        			select, listType, false, req);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}

        return resp == null ? null : resp.getResult();
    }

    /* ---------------------------------------------------------------------------- */
    /* hideJob:                                                                     */
    /* ---------------------------------------------------------------------------- */
    public JobHideDisplay hideJob(String jobUuid)
    	     throws TapisClientException
    {	
    	RespHideJob resp = new RespHideJob();
    	try {
    		var jobsApi = new JobsApi(_apiClient);
    		resp=jobsApi.hideJob(jobUuid, false);
    	}catch (ApiException e) {Utils.throwTapisClientException(e.getCode(),e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
    	return resp == null ? null : resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* hideJob:                                                                     */
    /* ---------------------------------------------------------------------------- */
    public JobHideDisplay unhideJob(String jobUuid)
    	     throws TapisClientException
    {	
    	RespHideJob resp = new RespHideJob();
    	try {
    		var jobsApi = new JobsApi(_apiClient);
    		resp = jobsApi.unhideJob(jobUuid, false);
    	}catch (ApiException e) {Utils.throwTapisClientException(e.getCode(),e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
    	return resp == null ? null : resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* sharejob:                                                                    */
    /* ---------------------------------------------------------------------------- */
    public JobShareDisplay shareJob(String jobUuid, ReqShareJob req)
    		throws TapisClientException
    {
    	RespShareJob resp = new  RespShareJob();
    	try {
    		var shareApi = new ShareApi(_apiClient);
    		resp = shareApi.shareJob(jobUuid, req, false);
    	}catch (ApiException e) {Utils.throwTapisClientException(e.getCode(),e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
    	return resp == null ? null : resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getJobShare:                                                                 */
    /* ---------------------------------------------------------------------------- */
    public List<JobShareListDTO> getJobShare(String jobUuid, int limit, int skip)
    		throws TapisClientException
    {
    	RespGetJobShareList resp = new  RespGetJobShareList();
    	try {
    		var shareApi = new ShareApi(_apiClient);
    		resp = shareApi.getJobShare(jobUuid, limit, skip, false);
    	}catch (ApiException e) {Utils.throwTapisClientException(e.getCode(),e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
    	return resp == null ? null : resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* deleteJobShare:                                                              */
    /* ---------------------------------------------------------------------------- */
    public JobUnShareDisplay deleteJobShare(String jobUuid, String user)
    		throws TapisClientException
    {
    	RespUnShareJob resp = new  RespUnShareJob() ;
    	try {
    		var shareApi = new ShareApi(_apiClient);
    		resp = shareApi.deleteJobShare(jobUuid, user, false);
    	}catch (ApiException e) {Utils.throwTapisClientException(e.getCode(),e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
    	return resp == null ? null : resp.getResult();
    }
    
    /* **************************************************************************** */
    /*                           Subscription Methods                               */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* subscribe:                                                                   */
    /* ---------------------------------------------------------------------------- */
    public String subscribe(String jobUuid, ReqSubscribe reqSubscribe)
     throws TapisClientException
    {
        RespResourceUrl resp = null;
        try {
            // Get the API object using default networking.
            var subApi = new SubscriptionsApi(_apiClient);
            resp = subApi.subscribe(jobUuid, reqSubscribe, false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
               
        var resultUrl = resp == null ? null : resp.getResult();
        return resultUrl == null ? null : resultUrl.getUrl();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getSubscriptions:                                                            */
    /* ---------------------------------------------------------------------------- */
    public List<TapisSubscription> getSubscriptions(String jobUuid, int limit, int skip)
     throws TapisClientException
    {
        RespGetSubscriptions resp = null;
        try {
            // Get the API object using default networking.
            var subApi = new SubscriptionsApi(_apiClient);
            resp = subApi.getSubscriptions(jobUuid, limit, skip, false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
               
        return resp == null ? null : resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* deleteSubscriptions:                                                         */
    /* ---------------------------------------------------------------------------- */
    public int deleteSubscriptions(String jobUuid)
     throws TapisClientException
    {
        ResultChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var subApi = new SubscriptionsApi(_apiClient);
            resp = subApi.deleteSubscriptions(jobUuid, false);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
               
        var changes = resp == null ? null : resp.getChanges();
        return changes == null ? 0 : changes;
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
