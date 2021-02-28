package edu.utexas.tacc.tapis.files.client;

import edu.utexas.tacc.tapis.files.client.gen.ApiClient;
import edu.utexas.tacc.tapis.files.client.gen.Configuration;
import edu.utexas.tacc.tapis.files.client.gen.api.ContentApi;
import edu.utexas.tacc.tapis.files.client.gen.api.FileOperationsApi;
import edu.utexas.tacc.tapis.files.client.gen.api.HealthApi;
import edu.utexas.tacc.tapis.files.client.gen.api.PermissionsApi;
import edu.utexas.tacc.tapis.files.client.gen.api.ShareApi;
import edu.utexas.tacc.tapis.files.client.gen.api.TransfersApi;
import org.apache.commons.lang3.StringUtils;

public class FilesClient {

    public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";

    private final FileOperationsApi fileOperations;
    private final PermissionsApi filePermissions;
    private final ShareApi fileShares;
    private final ContentApi fileContents;
    private final TransfersApi fileTransfers;
    private final HealthApi fileHealth;



    public FilesClient(String basePath, String jwt) {
        ApiClient apiClient = Configuration.getDefaultApiClient();
        if (!StringUtils.isBlank(basePath)) apiClient.setBasePath(basePath);
        if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
        fileOperations = new FileOperationsApi();
        fileContents = new ContentApi();
        filePermissions = new PermissionsApi();
        fileShares = new ShareApi();
        fileTransfers = new TransfersApi();
        fileHealth = new HealthApi();
    }

    public ApiClient getApiClient()
    {
        return Configuration.getDefaultApiClient();
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
        Configuration.getDefaultApiClient().setBasePath(basePath);
        return this;
    }

    /**
     * Add http header to default client
     */
    public FilesClient addDefaultHeader(String key, String val)
    {
        Configuration.getDefaultApiClient().addDefaultHeader(key, val);
        return this;
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



}
