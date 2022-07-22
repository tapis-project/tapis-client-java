package edu.utexas.tacc.tapis.client.shared;

import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;

/**
 * Utility class containing code shared among clients.
 * This class is non-instantiable
 */
public class Utils
{
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************

  // Defaults for searching and sorting
  public static final String DEFAULT_SEARCH = "";
  public static final int DEFAULT_LIMIT = -1;
  public static final String DEFAULT_ORDERBY = "";
  public static final int DEFAULT_SKIP = -1;
  public static final String DEFAULT_STARTAFTER = "";
  public static final boolean DEFAULT_COMPUTETOTAL = false;
  public static final String DEFAULT_SELECT_ALL = "allAttributes";
  public static final String DEFAULT_SELECT_SUMMARY = "summaryAttributes";
  public static final Boolean DEFAULT_SKIP_CREDENTIAL_CHECK = false;

  // Default target site for service JWTs
  public static final String DEFAULT_TARGET_SITE = "tacc";

  // Default Notification Subscription TTL
  public static final int DEFAULT_SUBSCRIPTION_TTL = 60;

  // Empty json body
  public static final String EMPTY_JSON = "{}";

  // Error msg to use in unlikely event we are unable to extract one from underlying exception
  private static final String ERR_MSG = "Exception encountered but unable to extract message from response or underlying exception";

  // ************************************************************************
  // ************************* Enums ****************************************
  // ************************************************************************
  // Custom error messages that may be reported by methods.
  public enum EMsg {NO_RESPONSE, ERROR_STATUS, UNKNOWN_RESPONSE_TYPE}
    
  // ************************************************************************
  // *********************** Fields *****************************************
  // ************************************************************************
  // Response serializer.
  private static final Gson _gson = ClientTapisGsonUtils.getGson();

  // ************************************************************************
  // *********************** Constructors ***********************************
  // ************************************************************************
  // Private constructor to make it non-instantiable
  private Utils() { throw new AssertionError(); }

  // ************************************************************************
  // *********************** Public Methods *********************************
  // ************************************************************************
  /* ---------------------------------------------------------------------------- */
  /* throwTapisClientException:                                                   */
  /* ---------------------------------------------------------------------------- */
  public static void throwTapisClientException(int code, String respBody, Exception e)
          throws TapisClientException
  {
    // Initialize fields to be assigned to tapis exception.
    TapisResponse tapisResponse = null;
    String msg = null;

    // Use information from the thrown ApiException.  If the body was sent by
    // SK, then it should be json.  Otherwise, we treat it as plain text.
    if (respBody != null)
    {
      try {tapisResponse = _gson.fromJson(respBody, TapisResponse.class);}
      catch (Exception e1) {msg = respBody;} // not proper json
    }
    else
    {
      msg = (e == null ? ERR_MSG : e.getMessage());
    }
    // If top level msg is empty attempt to use msg from response body
    // If no other msg available fall back to default msg
    if (StringUtils.isBlank(msg)) msg = (tapisResponse == null ? ERR_MSG : tapisResponse.message);

    // Create the client exception.
    var clientException = new TapisClientException(msg, e);

    // Fill in as many of the tapis exception fields as possible.
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

  /* **************************************************************************** */
  /*                               Private Methods                                */
  /* **************************************************************************** */

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
