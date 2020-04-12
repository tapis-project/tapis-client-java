package edu.utexas.tacc.tapis.systems.client;

import java.util.List;

import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateCredential;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;

import edu.utexas.tacc.tapis.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.shared.utils.TapisGsonUtils;
import edu.utexas.tacc.tapis.systems.client.gen.ApiClient;
import edu.utexas.tacc.tapis.systems.client.gen.ApiException;
import edu.utexas.tacc.tapis.systems.client.gen.Configuration;
import edu.utexas.tacc.tapis.systems.client.gen.api.CredentialsApi;
import edu.utexas.tacc.tapis.systems.client.gen.api.PermissionsApi;
import edu.utexas.tacc.tapis.systems.client.gen.api.SystemsApi;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqCreateSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.ReqPerms;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespChangeCount;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespCredential;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespNameArray;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespResourceUrl;
import edu.utexas.tacc.tapis.systems.client.gen.model.RespSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.TSystem;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability;
import edu.utexas.tacc.tapis.systems.client.gen.model.Capability.CategoryEnum;
import edu.utexas.tacc.tapis.systems.client.gen.model.Credential;

/**
 * Class providing a convenient front-end to the automatically generated client code
 * for the Systems Service REST API.
 * Underlying client classes with "gen" in the package name are generated by
 * openapi-generator each time a build is run.
 */
public class SystemsClient
{
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************

  // Header key for JWT
  public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";

  // Error msg to use in unlikely event we are unable to extract one from underlying exception
  private static final String ERR_MSG = SystemsClient.class.getSimpleName() +
              ": Exception encountered but unable extract message from response or underlying exception";

  // ************************************************************************
  // *********************** Enums ******************************************
  // ************************************************************************
  // Define AccessMethod here to be used in place of the auto-generated model enum
  //   because the auto-generated enum is named DefaultAccessMethodEnum which is misleading.
  public enum AccessMethod {PASSWORD, PKI_KEYS, CERT, ACCESS_KEY}

  // ************************************************************************
  // *********************** Fields *****************************************
  // ************************************************************************
  // Response body serializer
  private static final Gson gson = TapisGsonUtils.getGson();
  private final SystemsApi sysApi;
  private final PermissionsApi permsApi;
  private final CredentialsApi credsApi;

  // ************************************************************************
  // *********************** Constructors ***********************************
  // ************************************************************************

  public SystemsClient() { sysApi = new SystemsApi(); permsApi = new PermissionsApi(); credsApi = new CredentialsApi(); }

  /**
   * Constructor that overrides the compiled-in basePath value in ApiClient.  This
   * constructor is typically used in production.
   * <p>
   * The path includes the URL prefix up to and including the service root.  By
   * default this value is http://localhost/v3/systems.  In production environments
   * the protocol is https and the host/port will be specific to that environment.
   *
   * @param path the base path
   * @param jwt the token to set in an HTTP header
   */
  public SystemsClient(String path, String jwt)
  {
    ApiClient apiClient = Configuration.getDefaultApiClient();
    if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
    if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
    sysApi = new SystemsApi();
    permsApi = new PermissionsApi();
    credsApi = new CredentialsApi();
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
  public SystemsClient setBasePath(String basePath)
  {
    Configuration.getDefaultApiClient().setBasePath(basePath);
    return this;
  }

  /**
   * Add http header to default client
   */
  public SystemsClient addDefaultHeader(String key, String val)
  {
    Configuration.getDefaultApiClient().addDefaultHeader(key, val);
    return this;
  }

  // -----------------------------------------------------------------------
  // ------------------------- Systems -------------------------------------
  // -----------------------------------------------------------------------

  /**
   * Create a system
   *
   * @return url pointing to created resource
   * @throws TapisClientException - If api call throws an exception
   */
  public String createSystem(TSystem tSystem) throws TapisClientException
  {
    // Build the request
    var req = new ReqCreateSystem();
    req.settSystem(tSystem);
    // Submit the request and return the response
    RespResourceUrl resp = null;
    try { resp = sysApi.createSystem(req, false); }
    catch (Exception e) { throwTapisClientException(e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getUrl(); else return null;
  }

  /**
   * Get a system by name without returning credentials
   *
   * @param name System name
   * @return The system or null if system not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TSystem getSystemByName(String name) throws TapisClientException
  {
    RespSystem resp = null;
    try {resp = sysApi.getSystemByName(name, false, null, false); }
    catch (Exception e) { throwTapisClientException(e); }
    if (resp != null) return resp.getResult(); else return null;
  }

  /**
   * Get a system by name returning credentials for specified access method.
   * If accessMethod is null then default access method for the system is used.
   *
   * @param name System name
   * @param accessMethod - Desired access method used when fetching credentials,
   *                    default access method used if this is null
   * @return The system or null if system not found
   * @throws TapisClientException - If api call throws an exception
   */
  public TSystem getSystemByName(String name, AccessMethod accessMethod) throws TapisClientException
  {
    RespSystem resp = null;
    String accessMethodStr = (accessMethod==null ? null : accessMethod.name());
    try {resp = sysApi.getSystemByName(name, true, accessMethodStr, false); }
    catch (Exception e) { throwTapisClientException(e); }
    if (resp != null) return resp.getResult(); else return null;
  }

  /**
   * Get list of system names
   */
  public List<String> getSystemNames() throws TapisClientException
  {
    RespNameArray resp = null;
    try { resp = sysApi.getSystemNames(false); }
    catch (Exception e) { throwTapisClientException(e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getNames(); else return null;
  }

  /**
   * Delete a system given the system name.
   * Return 1 if record was deleted
   * Return 0 if record not present
   *
   * @param name System name
   * @return number of records modified as a result of the action
   * @throws TapisClientException - If api call throws an exception
   */
  public int deleteSystemByName(String name) throws TapisClientException
  {
    RespChangeCount resp = null;
    try { resp = sysApi.deleteSystemByName(name, false); }
    catch (Exception e) { throwTapisClientException(e); }
    if (resp != null && resp.getResult() != null && resp.getResult().getChanges() != null) return resp.getResult().getChanges();
    else return -1;
  }

  // -----------------------------------------------------------------------
  // --------------------------- Permissions -------------------------------
  // -----------------------------------------------------------------------

  /**
   * Grant permissions for given system and user.
   *
   * @throws TapisClientException - If api call throws an exception
   */
  public void grantUserPermissions(String systemName, String userName, List<String> permissions)
          throws TapisClientException
  {
    // Build the request
    var req = new ReqPerms();
    req.setPermissions(permissions);
    // Submit the request and return the response
    RespBasic resp = null;
    try { resp = permsApi.grantUserPerms(systemName, userName, req, false); }
    catch (Exception e) { throwTapisClientException(e); }
  }

  /**
   * Get list of permissions for given system and user.
   */
  public List<String> getSystemPermissions(String systemName, String userName) throws TapisClientException
  {
    RespNameArray resp = null;
    try { resp = permsApi.getUserPerms(systemName, userName, false); }
    catch (Exception e) { throwTapisClientException(e); }
    if (resp != null && resp.getResult() != null) return resp.getResult().getNames(); else return null;
  }

  /**
   * Revoke permissions for given system and user.
   *
   * @throws TapisClientException - if api call throws an exception
   */
  public void revokeUserPermissions(String systemName, String userName, List<String> permissions)
          throws TapisClientException
  {
    // Build the request
    var req = new ReqPerms();
    req.setPermissions(permissions);
    // Submit the request and return the response
    RespBasic resp = null;
    try { resp = permsApi.revokeUserPerms(systemName, userName, req, false); }
    catch (Exception e) { throwTapisClientException(e); }
  }

  /**
   * Revoke single permission for given system and user.
   *
   * @throws TapisClientException - if api call throws an exception
   */
  public void revokeUserPermission(String systemName, String userName, String permission)
          throws TapisClientException
  {
    // Submit the request and return the response
    RespBasic resp = null;
    try { resp = permsApi.revokeUserPerm(systemName, userName, permission, false); }
    catch (Exception e) { throwTapisClientException(e); }
  }

  // -----------------------------------------------------------------------
  // ---------------------------- Credentials ------------------------------
  // -----------------------------------------------------------------------

  /**
   * Create or update credential for given system and user.
   *
   * @throws TapisClientException - If api call throws an exception
   */
  public void updateUserCredential(String systemName, String userName, Credential cred) throws TapisClientException
  {
    // Build the request
    var req = new ReqCreateCredential();
    req.setCredential(cred);
    // Submit the request and return the response
    RespBasic resp = null;
    try { resp = credsApi.createUserCredential(systemName, userName, req, false); }
    catch (Exception e) { throwTapisClientException(e); }
  }

  /**
   * Retrieve credential for given system, user and access method.
   * If access method is null return credential for default access method defined for the system.
   *
   * @throws TapisClientException - If api call throws an exception
   */
  public Credential getUserCredential(String systemName, String userName, AccessMethod accessMethod)
          throws TapisClientException
  {
    RespCredential resp = null;
    String accessMethodStr = (accessMethod==null ? null : accessMethod.name());
    try {resp = credsApi.getUserCredential(systemName, userName, accessMethodStr, false); }
    catch (Exception e) { throwTapisClientException(e); }
    if (resp != null) return resp.getResult(); else return null;
  }

  /**
   * Retrieve a credential for given system and user for the default access method defined for the system
   *
   * @throws TapisClientException - If api call throws an exception
   */
  public Credential getUserCredential(String systemName, String userName) throws TapisClientException
  {
    return getUserCredential(systemName, userName, null);
  }

  /**
   * Delete credential for given system and user.
   *
   * @throws TapisClientException - if api call throws an exception
   */
  public void deleteUserCredential(String systemName, String userName)
          throws TapisClientException
  {
    // Submit the request and return the response
    RespBasic resp = null;
    try { resp = credsApi.removeUserCredential(systemName, userName, false); }
    catch (Exception e) { throwTapisClientException(e); }
  }

  /**
   * Utility method to build a credential object given secrets.
   */
  public static Credential buildCredential(String password, String privateKey, String publicKey,
                                    String accessKey, String accessSecret, String certificate)
  {
    var cred = new Credential();
    cred.setPassword(password);
    cred.setPrivateKey(privateKey);
    cred.setPublicKey(publicKey);
    cred.setAccessKey(accessKey);
    cred.setAccessSecret(accessSecret);
    cred.setCertificate(certificate);
    return cred;
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

  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************

  /**
   * throwTapisClientException
   * Attempt to extract info from any exception thrown by ApiClient and wrap it in a TapisClientException.
   * This method always throws a TapisClientException
   *
   * @param e - Exception thrown by ApiClient, typically ApiException
   * @throws TapisClientException - Always thrown
   */
  private void throwTapisClientException(Exception e) throws TapisClientException
  {
    TapisResponse tapisResponse = null; // For storing results of parsed response body
    int code = 0; // Code from ApiException, e.g. 404 for not found
    String msg = null; // Top level message to put in final exception

    // If it is an ApiException then we should be able to extract info from response body
    // If it is not an ApiException then pass along message from the original Exception
    if (e instanceof ApiException) {
      var apiException = (ApiException) e;
      code = apiException.getCode();
      // Attempt to parse response body
      String respBody = apiException.getResponseBody();
      if (respBody != null)
        try {tapisResponse = gson.fromJson(respBody, TapisResponse.class);}
        catch (Exception e1) {msg = respBody;} // response body was not json, use it as top level msg
    }
    else msg = e.getMessage();

    // If top level msg is empty attempt to use msg from response body
    if (StringUtils.isBlank(msg))
    {
      if (tapisResponse != null) msg = tapisResponse.message;
      else msg = ERR_MSG;
    }

    // Create the client exception.
    var clientException = new TapisClientException(msg, e);

    // Fill in as many of the tapis client exception fields as possible.
    clientException.setCode(code);
    if (tapisResponse != null)
    {
      clientException.setStatus(tapisResponse.status);
      clientException.setTapisMessage(tapisResponse.message);
      clientException.setVersion(tapisResponse.version);
      clientException.setResult(tapisResponse.result);
    }
    // Throw the client exception.
    throw clientException;
  }

  /**
   * Class used when attempting to parse ResponseBody found in ApiException
   */
  private static final class TapisResponse
  {
    private String status;
    private String message;
    private String version;
    private Object result;
  }
}
