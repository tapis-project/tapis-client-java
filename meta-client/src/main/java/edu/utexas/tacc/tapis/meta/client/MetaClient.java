package edu.utexas.tacc.tapis.meta.client;

import com.google.gson.Gson;
import edu.utexas.tacc.tapis.meta.client.gen.ApiClient;
import edu.utexas.tacc.tapis.meta.client.gen.ApiException;
import edu.utexas.tacc.tapis.meta.client.gen.Configuration;
import edu.utexas.tacc.tapis.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.shared.utils.TapisGsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;


public class MetaClient
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
  private static final String MetaClient_USER_AGENT = "MetaClient";
  
  /* **************************************************************************** */
  /*                                     Enums                                    */
  /* **************************************************************************** */
  // Custom error messages that may be reported by methods.
  public enum EMsg {NO_RESPONSE, ERROR_STATUS, UNKNOWN_RESPONSE_TYPE}
  
  /* **************************************************************************** */
  /*                                    Fields                                    */
  /* **************************************************************************** */
  // Response serializer.
  private static final Gson _gson = TapisGsonUtils.getGson();
  
  /* **************************************************************************** */
  /*                                 Constructors                                 */
  /* **************************************************************************** */
  /* ---------------------------------------------------------------------------- */
  /* constructor:                                                                 */
  /* ---------------------------------------------------------------------------- */
  /** Constructor that uses the compiled-in basePath value in ApiClient.  This
   * constructor is only appropriate for test code.
   */
  public MetaClient() {this(null, null);}
  
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
   * The user-agent is automatically set to MetaClient.
   *
   * Instances of this class are currently limited to using the default ApiClient.
   * This implies that the RoleApi, UserApi and GeneralApi implementations also
   * are expected to be using the same default ApiClient object.
   *
   * @param path the base path
   */
  public MetaClient(String path, String jwt)
  {
    // Process input.
    ApiClient apiClient = Configuration.getDefaultApiClient();
    if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
    if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
    
    // Other defaults.
    apiClient.setUserAgent(MetaClient_USER_AGENT);
  }
  
  /* **************************************************************************** */
  /*                                Utility Methods                               */
  /* **************************************************************************** */
  /* ---------------------------------------------------------------------------- */
  /* setBasePath:                                                                 */
  /* ---------------------------------------------------------------------------- */
  public MetaClient setBasePath(String path)
  {
    Configuration.getDefaultApiClient().setBasePath(path);
    return this;
  }
  
  /* ---------------------------------------------------------------------------- */
  /* addDefaultHeader:                                                            */
  /* ---------------------------------------------------------------------------- */
  public MetaClient addDefaultHeader(String key, String value)
  {
    Configuration.getDefaultApiClient().addDefaultHeader(key, value);
    return this;
  }
  
  /* ---------------------------------------------------------------------------- */
  /* setUserAgent:                                                                */
  /* ---------------------------------------------------------------------------- */
  public MetaClient setUserAgent(String userAgent)
  {
    Configuration.getDefaultApiClient().setUserAgent(userAgent);
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
  public MetaClient setConnectTimeout(int millis)
  {
    Configuration.getDefaultApiClient().setConnectTimeout(millis);
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
  public MetaClient setReadTimeout(int millis)
  {
    Configuration.getDefaultApiClient().setReadTimeout(millis);
    return this;
  }
  
  /* ---------------------------------------------------------------------------- */
  /* setDebugging:                                                                */
  /* ---------------------------------------------------------------------------- */
  public MetaClient setDebugging(boolean debugging)
  {
    Configuration.getDefaultApiClient().setDebugging(debugging);
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
    return Configuration.getDefaultApiClient().getConnectTimeout();
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
    return Configuration.getDefaultApiClient().getReadTimeout();
  }
  
  /* ---------------------------------------------------------------------------- */
  /* isDebugging:                                                                 */
  /* ---------------------------------------------------------------------------- */
  public boolean isDebugging()
  {
    return Configuration.getDefaultApiClient().isDebugging();
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
      var okClient = Configuration.getDefaultApiClient().getHttpClient();
      if (okClient != null) {
        var pool = okClient.connectionPool();
        if (pool != null) pool.evictAll();
      }
    } catch (Exception e) {}
  }
  
  /* **************************************************************************** */
  /*                            Public Info Methods                            */
  /* **************************************************************************** */
  /* ---------------------------------------------------------------------------- */
  /* sayHello:                                                                    */
  /* ---------------------------------------------------------------------------- */
  public String sayHello()
      throws TapisClientException
  {
    // Make the REST call.
    Object resp = null;
    try {
      // Get the API object using default networking.
      //var generalApi = new GeneralApi();
      //resp = generalApi.sayHello(false);
    }
    catch (Exception e) {throwTapisClientException(e);}
    
    // Return result value as a string.
    Object obj = resp.toString();
    return obj == null ? null : obj.toString();
  }
  
  /* ---------------------------------------------------------------------------- */
  /* checkHealth:                                                                 */
  /* ---------------------------------------------------------------------------- */
  public String checkHealth()
      throws TapisClientException
  {
    // Make the REST call.
    Object resp = null;
    try {
      // Get the API object using default networking.
      //var generalApi = new GeneralApi();
      //resp = generalApi.checkHealth();
    }
    catch (Exception e) {throwTapisClientException(e);}
    
    // Return result value as a string.
    Object obj = resp.toString();
    return obj == null ? null : obj.toString();
  }
  
  /* **************************************************************************** */
  /*                               Private Methods                                */
  /* **************************************************************************** */
  /* ---------------------------------------------------------------------------- */
  /* throwTapisClientException:                                                   */
  /* ---------------------------------------------------------------------------- */
  private void throwTapisClientException(Exception e)
      throws TapisClientException
  {
    // Initialize fields to be assigned to tapis exception.
    TapisResponse tapisResponse = null;
    int code = 0;
    String msg = null;
    
    // This should always be true.
    if (e instanceof ApiException) {
      // Extract information from the thrown exception.  If the body was sent by
      // SK, then it should be json.  Otherwise, we treat it as plain text.
      var apiException = (ApiException) e;
      String respBody = apiException.getResponseBody();
      if (respBody != null)
        try {tapisResponse = _gson.fromJson(respBody, TapisResponse.class);}
        catch (Exception e1) {msg = respBody;} // not proper json
      
      // Get the other parts of the exception.
      code = apiException.getCode();
    }
    else msg = e.getMessage();
    
    // Use the extracted information if there's any.
    if (StringUtils.isBlank(msg))
      if (tapisResponse != null) msg = tapisResponse.message;
      else msg = EMsg.ERROR_STATUS.name();
    
    // Create the client exception.
    var clientException = new TapisClientException(msg, e);
    
    // Fill in as many of the tapis exception fields as possible.
    clientException.setCode(code);
    if (tapisResponse != null) {
      clientException.setStatus(tapisResponse.status);
      clientException.setTapisMessage(tapisResponse.message);
      clientException.setVersion(tapisResponse.version);
      clientException.setResult(tapisResponse.result);
    }
    
    // Throw the client exception.
    throw clientException;
  }
  
  /* **************************************************************************** */
  /*                                TapisResponse                                 */
  /* **************************************************************************** */
  // Data transfer class to hold generic response content temporarily.
  private static final class TapisResponse
  {
    private String status;
    private String message;
    private String version;
    private Object result;
  }
  
}

