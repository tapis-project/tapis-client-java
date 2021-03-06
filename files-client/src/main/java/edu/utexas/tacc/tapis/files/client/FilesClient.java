package edu.utexas.tacc.tapis.files.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import edu.utexas.tacc.tapis.files.client.gen.Pair;
import edu.utexas.tacc.tapis.files.client.gen.model.HeaderByteRange;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
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
import edu.utexas.tacc.tapis.files.client.gen.api.HealthApi;
import edu.utexas.tacc.tapis.files.client.gen.api.PermissionsApi;
import edu.utexas.tacc.tapis.files.client.gen.api.ShareApi;
import edu.utexas.tacc.tapis.files.client.gen.api.TransfersApi;
import edu.utexas.tacc.tapis.files.client.gen.model.CreatePermissionRequest;
import edu.utexas.tacc.tapis.files.client.gen.model.FileInfo;
import edu.utexas.tacc.tapis.files.client.gen.model.FileListingResponse;
import edu.utexas.tacc.tapis.files.client.gen.model.FilePermission;
import edu.utexas.tacc.tapis.files.client.gen.model.FilePermissionResponse;
import edu.utexas.tacc.tapis.files.client.gen.model.FileStringResponse;
import edu.utexas.tacc.tapis.files.client.gen.model.HealthCheckResponse;
import edu.utexas.tacc.tapis.files.client.gen.model.MkdirRequest;
import edu.utexas.tacc.tapis.files.client.gen.model.MoveCopyRequest;
import edu.utexas.tacc.tapis.files.client.gen.model.MoveCopyRequest.OperationEnum;
import edu.utexas.tacc.tapis.files.client.gen.model.StringResponse;
import edu.utexas.tacc.tapis.files.client.gen.model.TransferTask;
import edu.utexas.tacc.tapis.files.client.gen.model.TransferTaskListResponse;
import edu.utexas.tacc.tapis.files.client.gen.model.TransferTaskRequest;
import edu.utexas.tacc.tapis.files.client.gen.model.TransferTaskResponse;


/**
 * Class providing a convenient front-end to the automatically generated client code
 * for the Files Service REST API.
 * Underlying client classes with "gen" in the package name are generated by
 * openapi-generator each time a build is run.
 */
public class FilesClient
 implements ITapisClient
{

  private static final class InstantAdapter extends TypeAdapter<Instant> {
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
  // ************************************************************************
  // *********************** Constants **************************************
  // ************************************************************************

    // Header key for JWT
    public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";

    // Instance of the underlying autogenerated client.
    private final FileOperationsApi fileOperations;
    private final PermissionsApi filePermissions;
    private final ShareApi fileShares;
    private final ContentApi fileContents;
    private final TransfersApi fileTransfers;
    private final HealthApi fileHealth;
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
    fileShares = new ShareApi(apiClient);
    fileTransfers = new TransfersApi(apiClient);
    fileHealth = new HealthApi(apiClient);
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
        fileShares = new ShareApi(apiClient);
        fileTransfers = new TransfersApi(apiClient);
        fileHealth = new HealthApi(apiClient);
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
    HealthCheckResponse resp = null;
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
   * @param meta Flag indicating if metadata should be include. Will slow down request.
   * @return list of FileInfo objects
   * @throws TapisClientException - If api call throws an exception
   */
  public List<FileInfo> listFiles(String systemId, String path, int limit, long offset, boolean meta)
          throws TapisClientException
  {
    FileListingResponse resp = null;
    try { resp = fileOperations.listFiles(systemId, path, limit, offset); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp.getResult(); else return null;
  }

  /**
   * Retrieve a file from a system.
   *
   * @param systemId system
   * @param path path relative to system rootDir
   * @param zip Flag indicating if contents of a folder should be zipped
   * @return transfer task
   * @throws TapisClientException - If api call throws an exception
   */
  public StreamedFile getFileContents(String systemId, String path, boolean zip)
          throws TapisClientException
  {

      // TODO: Fix name
      InputStream stream = null;
      String filename = FilenameUtils.getName(path);

      try {
          Call call = fileContents.getContentsCall(systemId, path, null, zip, null, null);
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
      Call call = fileContents.getContentsCall(systemId, path, null, true, null, null);
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
   * @return FileStringResponse
   * @throws TapisClientException - If api call throws an exception
   */
  public FileStringResponse moveCopy(String systemId, String path, String newPAth, OperationEnum operation)
          throws TapisClientException
  {
    FileStringResponse resp = null;
    var renameReq = new MoveCopyRequest();
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
   * @return FileStringResponse
   * @throws TapisClientException - If api call throws an exception
   */
  public FileStringResponse insert(String systemId, String path, InputStream inputStream)
          throws TapisClientException
  {

    File tmp = null;
    FileStringResponse resp = null;
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
   * @return FileStringResponse
   * @throws TapisClientException - If api call throws an exception
   */
  public FileStringResponse delete(String systemId, String path)
          throws TapisClientException
  {
    FileStringResponse resp = null;
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
   * @return FileStringResponse
   * @throws TapisClientException - If api call throws an exception
   */
  public FileStringResponse mkdir(String systemId, String path)
          throws TapisClientException
  {
    FileStringResponse resp = null;
    var req = new MkdirRequest();
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
    FilePermissionResponse resp = null;
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
  public FilePermission grantPermissions(String systemId, String path, CreatePermissionRequest req)
          throws TapisClientException
  {
    FilePermissionResponse resp = null;
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
   * @return FilePermissionStringResponse
   * @throws TapisClientException - If api call throws an exception
   */
  public String removePermissions(String systemId, String path, String username)
          throws TapisClientException
  {
    StringResponse resp = null;
    try { resp = filePermissions.deletePermissions(systemId, path, username); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp == null || resp.getResult() == null) return null;
    return resp.getResult();
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
  public TransferTask createTransferTask(TransferTaskRequest req) throws TapisClientException
  {
    // Submit the request and return the result
    TransferTaskResponse resp = null;
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
    TransferTaskResponse resp = null;
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
    TransferTaskListResponse resp = null;
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
    TransferTaskResponse resp = null;
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
  public StringResponse cancelTransferTask(String transferTaskId) throws TapisClientException
  {
    StringResponse resp = null;
    try { resp = fileTransfers.cancelTransferTask(transferTaskId); }
    catch (ApiException e) { Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e); }
    catch (Exception e) { Utils.throwTapisClientException(-1, null, e); }
    if (resp != null && resp.getResult() != null) return resp; else return null;
  }


    public FileOperationsApi operations() {
        return fileOperations;
    }

    public PermissionsApi permissions() {
        return filePermissions;
    }

    public ShareApi shares() {
        return fileShares;
    }

    public ContentApi contents() {
        return fileContents;
    }

    public TransfersApi transfers() {
        return fileTransfers;
    }

    public HealthApi health() {
        return fileHealth;
    }

    public static class StreamedFile {
      private InputStream inputStream;
      private String name;

      public StreamedFile(InputStream is, String name) {
        this.inputStream = is;
        this.name = name;
      }

      public String getName() {
        return name;
      }

      public void setName(String name) {
        this.name = name;
      }

      public InputStream getInputStream() {
        return inputStream;
      }

      public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
      }
    }



}
