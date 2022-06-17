package edu.utexas.tacc.tapis.files.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import edu.utexas.tacc.tapis.client.shared.ITapisClient;
import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.files.client.gen.ApiClient;
import edu.utexas.tacc.tapis.files.client.gen.ApiException;
import edu.utexas.tacc.tapis.files.client.gen.JSON;
import edu.utexas.tacc.tapis.files.client.gen.api.ContentApi;
import edu.utexas.tacc.tapis.files.client.gen.api.FileOperationsApi;
import edu.utexas.tacc.tapis.files.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.files.client.gen.api.PermissionsApi;
import edu.utexas.tacc.tapis.files.client.gen.api.SharingApi;
import edu.utexas.tacc.tapis.files.client.gen.api.TransfersApi;
import edu.utexas.tacc.tapis.files.client.gen.model.FileInfo;
import edu.utexas.tacc.tapis.files.client.gen.model.FilePermission;
import edu.utexas.tacc.tapis.files.client.gen.model.TransferTask;

import edu.utexas.tacc.tapis.files.client.gen.model.ReqCreatePerm;
import edu.utexas.tacc.tapis.files.client.gen.model.ReqMkdir;
import edu.utexas.tacc.tapis.files.client.gen.model.ReqMoveCopy;
import edu.utexas.tacc.tapis.files.client.gen.model.ReqShareUpdate;
import edu.utexas.tacc.tapis.files.client.gen.model.ReqTransfer;
import edu.utexas.tacc.tapis.files.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.files.client.gen.model.RespFileList;
import edu.utexas.tacc.tapis.files.client.gen.model.RespPerm;
import edu.utexas.tacc.tapis.files.client.gen.model.RespShareInfo;
import edu.utexas.tacc.tapis.files.client.gen.model.RespString;
import edu.utexas.tacc.tapis.files.client.gen.model.RespTransferTask;
import edu.utexas.tacc.tapis.files.client.gen.model.RespTransferTaskList;
import edu.utexas.tacc.tapis.files.client.gen.model.ResultShareInfo;

/**
 * Class providing a convenient front-end to the automatically generated client code
 *   for the Files Service REST API.
 * Underlying client classes with "gen" in the package name are generated by
 *   openapi-generator each time a build is run.
 */
public class FilesClient implements ITapisClient
{
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************

  // Header key for JWT
  public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";

  // Named null values to make it clear what is being passed in to a method
  private static final String nullImpersonationId = null;

    // Instance of the underlying autogenerated client.
    private final FileOperationsApi fileOperations;
    private final PermissionsApi filePermissions;
    private final SharingApi fileSharing;
    private final ContentApi fileContents;
    private final TransfersApi fileTransfers;
    private final GeneralApi fileHealth;
    private final ApiClient apiClient;

  /**
   * Default constructor which uses the compiled-in basePath based on the openapi spec
   *   used to autogenerate the client.
   */
  public FilesClient()
  {
    apiClient = new ApiClient();
    apiClient.setConnectTimeout(30000);
    apiClient.setReadTimeout(30000);
    fileOperations = new FileOperationsApi(apiClient);
    fileContents = new ContentApi(apiClient);
    filePermissions = new PermissionsApi(apiClient);
    fileSharing = new SharingApi(apiClient);
    fileTransfers = new TransfersApi(apiClient);
    fileHealth = new GeneralApi(apiClient);
  }

  /** 
   * Close connections and stop threads that can sometimes prevent JVM shutdown.
   */
  public void close()
  {
      try {
          // Best effort attempt to shut things down.
          var okClient = apiClient.getHttpClient();
          if (okClient != null) {
              var pool = okClient.connectionPool();
              if (pool != null) pool.evictAll();
          }
      } catch (Exception e) {}      
  }

  /**
   * Constructor that overrides the compiled-in basePath value in ApiClient.
   * The path should include the URL prefix up to and including the service root.
   * In production environments the protocol should be https and the host/port will
   * be specific to that environment.
   *
   * @param basePath the base path URL prefix up to and including the service root
   * @param jwt the token to set in an HTTP header
   */
    public FilesClient(String basePath, String jwt) {
        apiClient = new ApiClient();
        JSON mapper = new JSON();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
        mapper.setGson(gson);
        apiClient.setJSON(mapper);
        if (!StringUtils.isBlank(basePath)) apiClient.setBasePath(basePath);
        if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader("x-tapis-token", jwt);
        fileOperations = new FileOperationsApi(apiClient);
        fileContents = new ContentApi(apiClient);
        filePermissions = new PermissionsApi(apiClient);
        fileSharing = new SharingApi(apiClient);
        fileTransfers = new TransfersApi(apiClient);
        fileHealth = new GeneralApi(apiClient);
    }

    // getApiClient: Return underlying ApiClient
    public ApiClient getApiClient()
    {
        return apiClient;
    }

    /**
     * Get the base path.
     */
    public String getBasePath()
    {
        return getApiClient().getBasePath();
    }

    /**
     * Update base path for default client.
     */
    public FilesClient setBasePath(String basePath)
    {
        apiClient.setBasePath(basePath);
        return this;
    }

    /**
     * Add http header to default client
     */
    public FilesClient addDefaultHeader(String key, String val)
    {
        apiClient.addDefaultHeader(key, val);
        return this;
    }

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
    try { resp = fileHealth.healthCheck(); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getStatus(); else return null;
  }

  // -----------------------------------------------------------------------
  // --------------------------- File operations ---------------------------
  // -----------------------------------------------------------------------

  /**
   * List files/objects in a storage system.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @param limit pagination limit
   * @param offset pagination offset
   * @param recurse Flag indicating if a recursive listing is to be provided.
   * @param impersonationId - use provided Tapis username instead of oboUser when checking auth and
   *                          resolving effectiveUserId
   * @return list of FileInfo objects
   * @throws TapisClientException - If api call throws an exception
   */
  public List<FileInfo> listFiles(String systemId, String path, int limit, long offset, boolean recurse,
                                  String impersonationId)
          throws TapisClientException
  {
    RespFileList resp = null;
    try { resp = fileOperations.listFiles(systemId, path, limit, offset, recurse, impersonationId); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  public List<FileInfo> listFiles(String systemId, String path, int limit, long offset, boolean recurse)
          throws TapisClientException
  {
    return listFiles(systemId, path, limit, offset, recurse, nullImpersonationId);
  }

  /**
   * Retrieve a file from a system.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @param zip Flag indicating if contents of a folder should be zipped
   * @param impersonationId - use provided Tapis username instead of oboUser when checking auth and
   *                          resolving effectiveUserId
   * @return data stream
   * @throws TapisClientException - If api call throws an exception
   */
  public StreamedFile getFileContents(String systemId, String path, boolean zip, String impersonationId)
          throws TapisClientException
  {

      // TODO: Fix name
      InputStream stream = null;
      String filename = FilenameUtils.getName(path);

      try {
          Call call = fileContents.getContentsCall(systemId, path, null, zip, null, impersonationId, null);
          Response response =  call.execute();
          stream = response.body().byteStream();

          if (zip) {
            filename = FilenameUtils.removeExtension(filename) + ".zip";
          }
      }
      catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
      catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
      return new StreamedFile(stream, filename);
  }

  public StreamedFile getFileContents(String systemId, String path, boolean zip) throws TapisClientException
  {
    String nullImpersonationId = null;
    return getFileContents(systemId, path, zip, nullImpersonationId);
  }

  /**
   * Retrieve a zip file from a system.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @return transfer task
   * @throws TapisClientException - If api call throws an exception
   */
  public StreamedFile getZip(String systemId, String path)
      throws TapisClientException
  {
    InputStream zipStream = null;
    String filename = FilenameUtils.getName(StringUtils.stripEnd(path, "/"));

    try {
      Call call = fileContents.getContentsCall(systemId, path, null, true, null, nullImpersonationId, null);
      Response response =  call.execute();
      filename = FilenameUtils.removeExtension(filename) + ".zip";
      zipStream = response.body().byteStream();
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    return new StreamedFile(zipStream, filename);
  }

  /**
   * Rename a file or folder
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @param newPAth The new path of the file/folder
   * @return RespString
   * @throws TapisClientException - If api call throws an exception
   */
  public RespString moveCopy(String systemId, String path, String newPAth, ReqMoveCopy.OperationEnum operation)
          throws TapisClientException
  {
    RespString resp = null;
    var renameReq = new ReqMoveCopy();
    renameReq.setOperation(operation);
    renameReq.setNewPath(newPAth);
    try { resp = fileOperations.moveCopy(systemId, path, renameReq); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp; else return null;
  }

  /**
   * Upload a file.
   * The file will be added at the {path} independent of the original file name
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @param inputStream file to upload
   * @return RespString
   * @throws TapisClientException - If api call throws an exception
   */
  public RespString insert(String systemId, String path, InputStream inputStream) throws TapisClientException
  {
    File tmp = null;
    RespString resp = null;
    try {
      tmp = File.createTempFile("files-java-client", null);
      try (FileOutputStream out = new FileOutputStream(tmp)) {
        IOUtils.copy(inputStream, out);
      }
      tmp.deleteOnExit();
      resp = fileOperations.insert(systemId, path, tmp);
    }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    finally {
      if (tmp != null) tmp.delete();
    }
    if (resp != null && resp.getResult() != null) return resp; else return null;
  }

  /**
   * Delete a file or folder
   * Delete a file in {systemID} at path {path}.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @return RespString
   * @throws TapisClientException - If api call throws an exception
   */
  public RespString delete(String systemId, String path) throws TapisClientException
  {
    RespString resp = null;
    try { resp = fileOperations.delete(systemId, path); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp; else return null;
  }

  /**
   * Create a directory
   * Create a directory in the system at path the given path
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @return RespString
   * @throws TapisClientException - If api call throws an exception
   */
  public RespString mkdir(String systemId, String path) throws TapisClientException
  {
    RespString resp = null;
    var req = new ReqMkdir();
    req.setPath(path);
    try { resp = fileOperations.mkdir(systemId, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp; else return null;
  }

  // -----------------------------------------------------------------------
  // --------------------------- Permissions -------------------------------
  // -----------------------------------------------------------------------

  /**
   * Get the API user's permissions on a file or folder.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @param username Username to list
   * @return List of FilePermission objects
   * @throws TapisClientException - If api call throws an exception
   */
  public FilePermission getFilePermissions(String systemId, String path, String username)
          throws TapisClientException
  {
    RespPerm resp = null;
    try { resp = filePermissions.getPermissions(systemId, path, username); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null) return null;

    return resp.getResult();
  }

  /**
   * Add permissions on an object.
   * Add a user to a file/folder.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @param req Request containing username and permission to add
   * @return FilePermission object
   * @throws TapisClientException - If api call throws an exception
   */
  public FilePermission grantPermissions(String systemId, String path, ReqCreatePerm req)
          throws TapisClientException
  {
    RespPerm resp = null;
    try { resp = filePermissions.grantPermissions(systemId, path, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    return resp.getResult();
  }

  /**
   * Remove permissions on an object for a user.
   * Remove user permissions to a file/folder.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @param username Username to list
   * @return FilePermissionRespString
   * @throws TapisClientException - If api call throws an exception
   */
  public String removePermissions(String systemId, String path, String username) throws TapisClientException
  {
    RespString resp = null;
    try { resp = filePermissions.deletePermissions(systemId, path, username); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    return resp.getResult();
  }

  // -----------------------------------------------------------------------
  // --------------------------- Shares ------------------------------------
  // -----------------------------------------------------------------------

  /**
   * Get ShareInfo for a path
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @return ShareInfo - isPublic, list of users
   * @throws TapisClientException - If api call throws an exception
   */
  public ResultShareInfo getShareInfo(String systemId, String path) throws TapisClientException
  {
    RespShareInfo resp = null;
    try { resp = fileSharing.getShareInfo(systemId, path); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null) return null;

    return resp.getResult();
  }

  /**
   * Create/update share entries for a path
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @param userList list of users.
   * @throws TapisClientException - If api call throws an exception
   */
  public void sharePath(String systemId, String path, List<String> userList)
          throws TapisClientException
  {
    ReqShareUpdate req = new ReqShareUpdate();
    req.setUsers(userList);

    try { fileSharing.sharePath(systemId, path, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Unshare a path for one or more users.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @param userList list of users.
   * @throws TapisClientException - If api call throws an exception
   */
  public void unsharePath(String systemId, String path, List<String> userList ) throws TapisClientException
  {
    ReqShareUpdate req = new ReqShareUpdate();
    req.setUsers(userList);
    try { fileSharing.unSharePath(systemId, path, req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Share a path on a system publicly with all users in the tenant.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @throws TapisClientException - If api call throws an exception
   */
  public void sharePathPublic(String systemId, String path)
          throws TapisClientException
  {
    try { fileSharing.sharePathPublic(systemId, path); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  /**
   * Remove public access for a path on a system.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @throws TapisClientException - If api call throws an exception
   */
  public void unsharePathPublic(String systemId, String path) throws TapisClientException
  {
    try { fileSharing.unSharePathPublic(systemId, path); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
  }

  // -----------------------------------------------------------------------
  // --------------------------- Transfers -------------------------------
  // -----------------------------------------------------------------------

  /**
   * Create a background task which will transfer files to a system.
   *
   * @param req Request body specifying attributes
   * @return transfer task
   * @throws TapisClientException - If api call throws an exception
   */
  public TransferTask createTransferTask(ReqTransfer req) throws TapisClientException
  {
    // Submit the request and return the result
    RespTransferTask resp = null;
    try { resp = fileTransfers.createTransferTask(req); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Get a transfer task
   *
   * @param transferTaskId Transfer task ID
   * @return transfer task
   * @throws TapisClientException - If api call throws an exception
   */
  public TransferTask getTransferTask(String transferTaskId) throws TapisClientException
  {
    RespTransferTask resp = null;
    try { resp = fileTransfers.getTransferTask(transferTaskId); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Get a list of recent transfer tasks
   *
   * @return list of recent transfer tasks
   * @throws TapisClientException - If api call throws an exception
   */
  public List<TransferTask> getRecentTransferTasks(int limit, int offset) throws TapisClientException
  {
    RespTransferTaskList resp = null;
    try { resp = fileTransfers.getRecentTransferTasks(limit, offset); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Get history of a transfer task
   *
   * @param transferTaskId Transfer task ID
   * @return transfer task with history
   * @throws TapisClientException - If api call throws an exception
   */
  public TransferTask getTransferTaskHistory(String transferTaskId) throws TapisClientException
  {
    RespTransferTask resp = null;
    try { resp = fileTransfers.getTransferTaskDetails(transferTaskId); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Stop/Cancel a transfer task
   *
   * @param transferTaskId Transfer task ID
   * @return transfer task
   * @throws TapisClientException - If api call throws an exception
   */
  public RespString cancelTransferTask(String transferTaskId) throws TapisClientException
  {
    RespString resp = null;
    try { resp = fileTransfers.cancelTransferTask(transferTaskId); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp; else return null;
  }

  // Support returning healthCheck as a Response. Used by Jobs code
  public GeneralApi getGeneralApi() { return fileHealth; }

  // ************************************************************************
  // *********************** Private Methods ********************************
  // ************************************************************************

  // ************************************************************************
  // *********************** Classes ****************************************
  // ************************************************************************
  /*
   * Encapsulate a named InputStream
   */
  public static class StreamedFile
  {
    private final InputStream inputStream;
    private final String name;

    public StreamedFile(InputStream inputStream1, String name1)
    {
      inputStream = inputStream1;
      name = name1;
    }

    public String getName() {
      return name;
    }
    public InputStream getInputStream() {
      return inputStream;
    }
  }

  /*
   *
   */
  private static final class InstantAdapter extends TypeAdapter<Instant>
  {
    @Override
    public void write( final JsonWriter jsonWriter, final Instant instant ) throws IOException {
      jsonWriter.value(instant.toString());
    }

    @Override
    public Instant read( final JsonReader in ) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      String instantStr = in.nextString();
      return Instant.parse(instantStr);
    }
  }
}
