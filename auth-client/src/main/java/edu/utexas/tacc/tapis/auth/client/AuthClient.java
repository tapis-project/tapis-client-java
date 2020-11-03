package edu.utexas.tacc.tapis.auth.client;


import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.auth.client.gen.ApiException;
import edu.utexas.tacc.tapis.auth.client.gen.api.TokensApi;
import edu.utexas.tacc.tapis.auth.client.gen.ApiClient;
import edu.utexas.tacc.tapis.auth.client.gen.Configuration;
import edu.utexas.tacc.tapis.auth.client.model.GetTokenParms;
import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.ClientTapisGsonUtils;

import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Map;

/**
 * Class providing a convenient front-end for the automatically generated client code
 * for the Authenticator Service REST API.
 * Underlying client classes with "gen" in the package name are generated by
 * openapi-generator each time a build is run.
 */
public class AuthClient
{
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************
  private static final String GRANT_TYPE = "password";

  // ************************************************************************
  // ************************* Enums ****************************************
  // ************************************************************************

  // ************************************************************************
  // *********************** Fields *****************************************
  // ************************************************************************
  // Response serializer.
  private static final Gson _gson = ClientTapisGsonUtils.getGson();

  // ************************************************************************
  // *********************** Constructors ***********************************
  // ************************************************************************

  public AuthClient() { }

  /**
   * Constructor that overrides the compiled-in basePath value in ApiClient.  This
   * constructor is typically used in production.
   * <p>
   * The path includes the URL prefix up to and including the service root.  By
   * default this value is http://localhost:8080/service_name.  In production environments
   * the protocol is https and the host/port will be specific to that environment.
   *
   * @param path the base path
   */
  public AuthClient(String path)
  {
    ApiClient apiClient = Configuration.getDefaultApiClient();
    if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
  }

  // ************************************************************************
  // *********************** Public Methods *********************************
  // ************************************************************************

  /**
   * getApiClient: Return underlying ApiClient
   */
  public ApiClient getApiClient() { return Configuration.getDefaultApiClient(); }

  /**
   * addDefaultHeader: Add http header to client
   */
  public ApiClient addDefaultHeader(String key, String val)
  {
    return Configuration.getDefaultApiClient().addDefaultHeader(key, val);
  }

  /**
   * The create_token request handler that gets a token from the authenticator service based on
   * the parameters provided. Null is return if invalid information is returned by the
   * authenticator service.
   *
   * @param userName
   * @param userPassword
   * @return a string representing a user JWT.
   * @throws TapisClientException on error
   */
  public String getToken(String userName, String userPassword) throws TapisClientException
  {
    String result = null;
    // Build the request
    var req = new GetTokenParms();
    req.setGrantType(GRANT_TYPE);
    req.setUsername(userName);
    req.setPassword(userPassword);

    // Make the call and return the result
    Map resp = null;
    try
    {
      var tokApi = new TokensApi();
      resp = (Map) tokApi.createToken(req);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }

    // If response came back null return null
    if (resp == null) return result;

    // Marshal only the result from the map.
    String json = _gson.toJson(resp.get("result"));
    // If no result return null
    if (StringUtils.isBlank(json)) return result;

    // Get the access token string.
    JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
    result = jsonObj.get("access_token").getAsJsonObject().get("access_token").getAsString();
    return result;
  }
}