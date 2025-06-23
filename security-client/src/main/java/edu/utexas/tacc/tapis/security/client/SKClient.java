package edu.utexas.tacc.tapis.security.client;

import java.util.Arrays;
import java.util.List;

import edu.utexas.tacc.tapis.security.client.gen.model.ReqGrantRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqGrantRoleWithPermission;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqRevokeRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqRolePermits;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqValidatePwd;
import edu.utexas.tacc.tapis.security.client.gen.model.RoleTypeEnum;
import org.apache.commons.lang3.StringUtils;

import edu.utexas.tacc.tapis.client.shared.ITapisClient;
import edu.utexas.tacc.tapis.client.shared.Utils;
import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.security.client.gen.ApiClient;
import edu.utexas.tacc.tapis.security.client.gen.ApiException;
import edu.utexas.tacc.tapis.security.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.security.client.gen.api.RoleApi;
import edu.utexas.tacc.tapis.security.client.gen.api.ShareApi;
import edu.utexas.tacc.tapis.security.client.gen.api.UserApi;
import edu.utexas.tacc.tapis.security.client.gen.api.VaultApi;
import edu.utexas.tacc.tapis.security.client.gen.model.Options;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqAddChildRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqAddRolePermission;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqCreateRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqGrantUserPermission;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqPreviewPathPrefix;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqRemoveChildRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqRemovePermissionFromAllRoles;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqRemoveRolePermission;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqReplacePathPrefix;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqRevokeUserPermission;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqShareResource;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUpdateRoleDescription;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUpdateRoleName;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUpdateRoleOwner;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUserHasRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUserHasRoleMulti;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUserIsAdmin;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUserIsPermitted;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUserIsPermittedMulti;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqVersions;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqWriteSecret;
import edu.utexas.tacc.tapis.security.client.gen.model.RespAuthorized;
import edu.utexas.tacc.tapis.security.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.security.client.gen.model.RespBoolean;
import edu.utexas.tacc.tapis.security.client.gen.model.RespChangeCount;
import edu.utexas.tacc.tapis.security.client.gen.model.RespName;
import edu.utexas.tacc.tapis.security.client.gen.model.RespNameArray;
import edu.utexas.tacc.tapis.security.client.gen.model.RespPathPrefixes;
import edu.utexas.tacc.tapis.security.client.gen.model.RespProbe;
import edu.utexas.tacc.tapis.security.client.gen.model.RespResourceUrl;
import edu.utexas.tacc.tapis.security.client.gen.model.RespRole;
import edu.utexas.tacc.tapis.security.client.gen.model.RespSecret;
import edu.utexas.tacc.tapis.security.client.gen.model.RespSecretList;
import edu.utexas.tacc.tapis.security.client.gen.model.RespSecretMeta;
import edu.utexas.tacc.tapis.security.client.gen.model.RespSecretVersionMetadata;
import edu.utexas.tacc.tapis.security.client.gen.model.RespShare;
import edu.utexas.tacc.tapis.security.client.gen.model.RespShareList;
import edu.utexas.tacc.tapis.security.client.gen.model.RespVersions;
import edu.utexas.tacc.tapis.security.client.gen.model.SkRole;
import edu.utexas.tacc.tapis.security.client.gen.model.SkSecret;
import edu.utexas.tacc.tapis.security.client.gen.model.SkSecretList;
import edu.utexas.tacc.tapis.security.client.gen.model.SkSecretMetadata;
import edu.utexas.tacc.tapis.security.client.gen.model.SkSecretVersionMetadata;
import edu.utexas.tacc.tapis.security.client.gen.model.SkShare;
import edu.utexas.tacc.tapis.security.client.gen.model.SkShareList;
import edu.utexas.tacc.tapis.security.client.gen.model.Transformation;
import edu.utexas.tacc.tapis.security.client.model.SKSecretDeleteParms;
import edu.utexas.tacc.tapis.security.client.model.SKSecretMetaParms;
import edu.utexas.tacc.tapis.security.client.model.SKSecretReadParms;
import edu.utexas.tacc.tapis.security.client.model.SKSecretWriteParms;
import edu.utexas.tacc.tapis.security.client.model.SKShareDeleteShareParms;
import edu.utexas.tacc.tapis.security.client.model.SKShareGetSharesParms;
import edu.utexas.tacc.tapis.security.client.model.SKShareHasPrivilegeParms;

public class SKClient implements ITapisClient
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
    
    // Public pseudo-grantees.
    public static final String PUBLIC_GRANTEE = "~public";
    public static final String PUBLIC_NO_AUTHN_GRANTEE = "~public_no_authn";
    
    // Configuration defaults.
    private static final String SKCLIENT_USER_AGENT = "SKClient";
    
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
    public SKClient() {this(null, null);}
    
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
     * The user-agent is automatically set to SKClient.
     * 
     * @param path the base path 
     */
    public SKClient(String path, String jwt) 
    {
    	// Create actual client.
    	_apiClient = new ApiClient();
    	
        // Process input.
        if (!StringUtils.isBlank(path)) _apiClient.setBasePath(path);
        if (!StringUtils.isBlank(jwt))  _apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
        
        // Other defaults.
        _apiClient.setUserAgent(SKCLIENT_USER_AGENT);
    }
    
    /* **************************************************************************** */
    /*                                Utility Methods                               */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* setBasePath:                                                                 */
    /* ---------------------------------------------------------------------------- */
    public SKClient setBasePath(String path)
    {
        _apiClient.setBasePath(path);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* addDefaultHeader:                                                            */
    /* ---------------------------------------------------------------------------- */
    public SKClient addDefaultHeader(String key, String value)
    {
        _apiClient.addDefaultHeader(key, value);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setUserAgent:                                                                */
    /* ---------------------------------------------------------------------------- */
    public SKClient setUserAgent(String userAgent) 
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
    public SKClient setConnectTimeout(int millis)
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
    public SKClient setReadTimeout(int millis)
    {
        _apiClient.setReadTimeout(millis);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setDebugging:                                                                */
    /* ---------------------------------------------------------------------------- */
    public SKClient setDebugging(boolean debugging) 
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
    /*                              Public Role Methods                             */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* getRoleNames:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<String> getRoleNames(String tenant)
            throws TapisClientException {
        return getRoleNames(tenant, RoleTypeEnum.USER);
    }

    public List<String> getRoleNames(String tenant, RoleTypeEnum roleType)
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.getRoleNames(tenant, roleType);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getRoleByName:                                                               */
    /* ---------------------------------------------------------------------------- */
    public SkRole getRoleByName(String tenant, String roleName)
            throws TapisClientException
    {
        return getRoleByName(tenant, RoleTypeEnum.USER, roleName);
    }

    public SkRole getRoleByName(String tenant, RoleTypeEnum roleType, String roleName)
     throws TapisClientException
    {
        // Make the REST call.
        RespRole resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.getRoleByName(roleName, tenant, roleType);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* createRole:                                                                  */
    /* ---------------------------------------------------------------------------- */
    public String createRole(String roleTenant, String roleName, String description)
            throws TapisClientException
    {
       return createRole(roleTenant, roleName, RoleTypeEnum.USER, description);
    }

    public String createRole(String roleTenant, String roleName, RoleTypeEnum roleType, String description)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqCreateRole();
        body.setRoleTenant(roleTenant);
        body.setRoleName(roleName);
        body.setRoleType(roleType);
        body.setDescription(description);
        
        // Make the REST call.
        RespResourceUrl resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.createRole(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult().getUrl();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* deleteRoleByName:                                                            */
    /* ---------------------------------------------------------------------------- */
    public int deleteRoleByName(String tenant, String roleName)
            throws TapisClientException
    {
        return deleteRoleByName(tenant, roleName, RoleTypeEnum.USER);
    }

    public int deleteRoleByName(String tenant, String roleName, RoleTypeEnum roleType)
     throws TapisClientException
    {
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.deleteRoleByName(roleName, tenant, roleType);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* updateRoleName:                                                              */
    /* ---------------------------------------------------------------------------- */
    public void updateRoleName(String roleTenant, String roleName, String newRoleName)
            throws TapisClientException
    {
        updateRoleName(roleTenant, roleName, RoleTypeEnum.USER, newRoleName);
    }
    public void updateRoleName(String roleTenant, String roleName, RoleTypeEnum roleType, String newRoleName)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUpdateRoleName();
        body.setRoleTenant(roleTenant);
        body.setNewRoleName(newRoleName);
        body.setRoleType(roleType);

        // Make the REST call.
        @SuppressWarnings("unused")
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.updateRoleName(roleName, body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
    }
    
    /* ---------------------------------------------------------------------------- */
    /* updateRoleOwner:                                                             */
    /* ---------------------------------------------------------------------------- */
    public void updateRoleOwner(String tenant, String roleName, String newOwner)
            throws TapisClientException
    {
        updateRoleOwner(tenant, roleName, RoleTypeEnum.USER, newOwner);
    }
    public void updateRoleOwner(String tenant, String roleName, RoleTypeEnum roleType, String newOwner)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUpdateRoleOwner();
        body.setRoleTenant(tenant);
        body.setNewOwner(newOwner);
        body.setRoleType(roleType);

        // Make the REST call.
        @SuppressWarnings("unused")
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.updateRoleOwner(roleName, body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
    }
    
    /* ---------------------------------------------------------------------------- */
    /* updateRoleDescription:                                                       */
    /* ---------------------------------------------------------------------------- */
    public void updateRoleDescription(String roleTenant, String roleName, String newDescription)
            throws TapisClientException
    {
        updateRoleDescription(roleTenant, roleName, RoleTypeEnum.USER, newDescription);
    }
    public void updateRoleDescription(String roleTenant, String roleName, RoleTypeEnum roleType, String newDescription)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUpdateRoleDescription();
        body.setRoleTenant(roleTenant);
        body.setNewDescription(newDescription);
        body.setRoleType(roleType);

        // Make the REST call.
        @SuppressWarnings("unused")
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.updateRoleDescription(roleName, body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getRolePermissions:                                                          */
    /* ---------------------------------------------------------------------------- */
    public List<String> getRolePermissions(String roleTenant, String roleName, boolean immediate)
     throws TapisClientException
    {
        // Make the REST call.
    	RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.getRolePermissions(roleName, roleTenant, immediate);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        // Return result value.
        return resp.getResult().getNames();
    }
    
   /* ---------------------------------------------------------------------------- */
    /* addRolePermission:                                                           */
    /* ---------------------------------------------------------------------------- */
    public int addRolePermission(String roleTenant, String roleName, String permSpec)
           throws TapisClientException {
        return addRolePermission(roleTenant, roleName, RoleTypeEnum.USER, permSpec);
    }

    public int addRolePermission(String roleTenant, String roleName, RoleTypeEnum roleType, String permSpec)
     throws TapisClientException {
        // Assign input body.
        var body = new ReqAddRolePermission();
        body.setRoleTenant(roleTenant);
        body.setRoleName(roleName);
        body.setPermSpec(permSpec);
        body.setRoleType(roleType);

        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.addRolePermission(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* removeRolePermission:                                                        */
    /* ---------------------------------------------------------------------------- */
    public int removeRolePermission(String roleTenant, String roleName, String permSpec)
            throws TapisClientException {
       return removeRolePermission(roleTenant, roleName, RoleTypeEnum.USER, permSpec);
    }

    public int removeRolePermission(String roleTenant, String roleName, RoleTypeEnum roleType, String permSpec)
     throws TapisClientException {
        // Assign input body.
        var body = new ReqRemoveRolePermission();
        body.setRoleTenant(roleTenant);
        body.setRoleName(roleName);
        body.setPermSpec(permSpec);
        body.setRoleType(roleType);

        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.removeRolePermission(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* addChildRole:                                                                */
    /* ---------------------------------------------------------------------------- */
    public int addChildRole(String roleTenant, String parentRoleName, String childRoleName)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqAddChildRole();
        body.setRoleTenant(roleTenant);
        body.setParentRoleName(parentRoleName);
        body.setChildRoleName(childRoleName);

        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.addChildRole(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* removeChildRole:                                                             */
    /* ---------------------------------------------------------------------------- */
    public int removeChildRole(String roleTenant, String parentRoleName, String childRoleName)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqRemoveChildRole();
        body.setRoleTenant(roleTenant);
        body.setParentRoleName(parentRoleName);
        body.setChildRoleName(childRoleName);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.removeChildRole(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* previewPathPrefix:                                                           */
    /* ---------------------------------------------------------------------------- */
    public List<Transformation> previewPathPrefix(String tenant, String schema, String roleName,
                                                  String oldSystemId, String newSystemId,
                                                  String oldPrefix, String newPrefix)
            throws TapisClientException {
       return previewPathPrefix(tenant, schema, roleName, RoleTypeEnum.USER, oldSystemId, newSystemId, oldPrefix, newPrefix);
    }
    public List<Transformation> previewPathPrefix(String tenant, String schema, String roleName, RoleTypeEnum roleType,
                                                  String oldSystemId, String newSystemId,
                                                  String oldPrefix, String newPrefix)
     throws TapisClientException {
        // Assign input body.
        var body = new ReqPreviewPathPrefix();
        body.setTenant(tenant);
        body.setSchema(schema);
        body.setRoleName(roleName);
        body.setOldSystemId(oldSystemId);
        body.setNewSystemId(newSystemId);
        body.setOldPrefix(oldPrefix);
        body.setNewPrefix(newPrefix);
        body.setRoleType(roleType);

        // Make the REST call.
        RespPathPrefixes resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.previewPathPrefix(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* replacePathPrefix:                                                           */
    /* ---------------------------------------------------------------------------- */
    public int replacePathPrefix(String tenant, String schema, String roleName,
                                 String oldSystemId, String newSystemId,
                                 String oldPrefix, String newPrefix)
            throws TapisClientException {
        return replacePathPrefix(tenant, schema, roleName, RoleTypeEnum.USER, oldSystemId, newSystemId, oldPrefix, newPrefix);
    }

    public int replacePathPrefix(String tenant, String schema, String roleName, RoleTypeEnum roleType,
                                 String oldSystemId, String newSystemId,
                                 String oldPrefix, String newPrefix)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqReplacePathPrefix();
        body.setTenant(tenant);
        body.setSchema(schema);
        body.setRoleName(roleName);
        body.setOldSystemId(oldSystemId);
        body.setNewSystemId(newSystemId);
        body.setOldPrefix(oldPrefix);
        body.setNewPrefix(newPrefix);
        body.setRoleType(roleType);

        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi(_apiClient);
            resp = roleApi.replacePathPrefix(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* **************************************************************************** */
    /*                              Public User Methods                             */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* getUserNames:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUserNames(String tenant)
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.getUserNames(tenant);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getUserRoles:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUserRoles(String tenant, String user)
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.getUserRoles(user, tenant);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getUserPerms:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUserPerms(String tenant, String user)
     throws TapisClientException
    {
        return getUserPerms(tenant, user, null, null);
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getUserPerms:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUserPerms(String tenant, String user, String implies, 
                                     String impliedBy)
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.getUserPerms(user, tenant, implies, impliedBy);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* grantUserRole:                                                               */
    /* ---------------------------------------------------------------------------- */
    public int grantUserRole(String tenant, String user, String roleName)
            throws TapisClientException {
        return grantRole(tenant, user, roleName, RoleTypeEnum.USER);
    }

    public int grantRole(String tenant, String user, String roleName, RoleTypeEnum roleType)
     throws TapisClientException {
        // Assign input body.
        var body = new ReqGrantRole();
        body.setTenant(tenant);
        body.setUser(user);
        body.setRoleName(roleName);
        body.setRoleType(roleType);

        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.grantRole(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* revokeUserRole:                                                              */
    /* ---------------------------------------------------------------------------- */
    public int revokeUserRole(String tenant, String user, String roleName)
            throws TapisClientException {
        return revokeRole(tenant, user, roleName, RoleTypeEnum.USER);
    }

    public int revokeRole(String tenant, String user, String roleName, RoleTypeEnum roleType)
     throws TapisClientException {
        // Assign input body.
        var body = new ReqRevokeRole();
        body.setTenant(tenant);
        body.setUser(user);
        body.setRoleName(roleName);
        body.setRoleType(roleType);

        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.revokeRole(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getAdmins:                                                                   */
    /* ---------------------------------------------------------------------------- */
    public List<String> getAdmins(String tenant)
     throws TapisClientException
    {
      // Make the REST call.
      RespNameArray resp = null;
      try {
        // Get the API object using default networking.
        var userApi = new UserApi(_apiClient);
        resp = userApi.getAdmins(tenant);
      }
      catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
      catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}

      // Return result value.
      return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* grantRoleWithPermission:                                                     */
    /* ---------------------------------------------------------------------------- */
    public int grantRoleWithPermission(String tenant, String user, String roleName,
                                       String permSpec)
            throws TapisClientException {
        return grantRoleWithPermission(tenant, user, roleName, RoleTypeEnum.USER, permSpec);
    }

    public int grantRoleWithPermission(String tenant, String user, String roleName, RoleTypeEnum roleType,
                                       String permSpec)
     throws TapisClientException {
        // Assign input body.
        var body = new ReqGrantRoleWithPermission();
        body.setTenant(tenant);
        body.setUser(user);
        body.setRoleName(roleName);
        body.setPermSpec(permSpec);
        body.setRoleType(roleType);

        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.grantRoleWithPermission(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* grantUserPermission:                                                         */
    /* ---------------------------------------------------------------------------- */
    public int grantUserPermission(String tenant, String user, String permSpec)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqGrantUserPermission();
        body.setTenant(tenant);
        body.setUser(user);
        body.setPermSpec(permSpec);

        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.grantUserPermission(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* revokeRoleWithPermission:                                                    */
    /* ---------------------------------------------------------------------------- */
    public int revokeUserPermission(String tenant, String user, String permSpec)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqRevokeUserPermission();
        body.setTenant(tenant);
        body.setUser(user);
        body.setPermSpec(permSpec);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.revokeUserPermission(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* hasRole:                                                                     */
    /* ---------------------------------------------------------------------------- */
    public boolean hasRole(String tenant, String user, String roleName)
            throws TapisClientException {
        return hasRole(tenant, user, roleName, RoleTypeEnum.USER);
    }

    public boolean hasRole(String tenant, String user, String roleName, RoleTypeEnum roleType)
     throws TapisClientException {
        // Assign input body.
        var body = new ReqUserHasRole();
        body.setTenant(tenant);
        body.setUser(user);
        body.setRoleName(roleName);
        body.setRoleType(roleType);

        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.hasRole(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* hasAnyRole:                                                                  */
    /* ---------------------------------------------------------------------------- */
    public boolean hasRoleAny(String tenant, String user, String[] roleNames)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserHasRoleMulti();
        body.setTenant(tenant);
        body.setUser(user);
        body.setRoleNames(Arrays.asList(roleNames));

        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.hasRoleAny(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* hasAllRole:                                                                  */
    /* ---------------------------------------------------------------------------- */
    public boolean hasRoleAll(String tenant, String user, String[] roleNames)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserHasRoleMulti();
        body.setTenant(tenant);
        body.setUser(user);
        body.setRoleNames(Arrays.asList(roleNames));
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.hasRoleAll(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }

  /* ---------------------------------------------------------------------------- */
  /* isAdmin:                                                                     */
  /* ---------------------------------------------------------------------------- */
  public boolean isAdmin(String tenant, String user)
          throws TapisClientException
  {
    // Assign input body.
    var body = new ReqUserIsAdmin();
    body.setTenant(tenant);
    body.setUser(user);

    // Make the REST call.
    RespAuthorized resp = null;
    try {
      // Get the API object using default networking.
      var userApi = new UserApi(_apiClient);
      resp = userApi.isAdmin(body);
    }
    catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
    catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}

    // Return result value.
    Boolean b = resp.getResult().getIsAuthorized();
    return b == null ? false : b;
  }

  /* ---------------------------------------------------------------------------- */
    /* isPermitted:                                                                 */
    /* ---------------------------------------------------------------------------- */
    public boolean isPermitted(String tenant, String user, String permSpec)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserIsPermitted();
        body.setTenant(tenant);
        body.setUser(user);
        body.setPermSpec(permSpec);
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.isPermitted(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* isPermittedAny:                                                              */
    /* ---------------------------------------------------------------------------- */
    public boolean isPermittedAny(String tenant, String user, String[] permSpecs)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserIsPermittedMulti();
        body.setTenant(tenant);
        body.setUser(user);
        body.setPermSpecs(Arrays.asList(permSpecs));
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.isPermittedAny(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* isPermittedAll:                                                              */
    /* ---------------------------------------------------------------------------- */
    public boolean isPermittedAll(String tenant, String user, String[] permSpecs)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserIsPermittedMulti();
        body.setTenant(tenant);
        body.setUser(user);
        body.setPermSpecs(Arrays.asList(permSpecs));
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.isPermittedAll(body);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getUsersWithRole:                                                            */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUsersWithRole(String tenant, String roleName)
            throws TapisClientException {
        return getUsersWithRole(tenant, roleName, RoleTypeEnum.USER);
    }

    public List<String> getUsersWithRole(String tenant, String roleName, RoleTypeEnum roleType)
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.getUsersWithRole(roleName, tenant, roleType);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getUsersWithPermission:                                                      */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUsersWithPermission(String tenant, String permSpec)
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.getUsersWithPermission(permSpec, tenant);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getDefaultUserRole:                                                          */
    /* ---------------------------------------------------------------------------- */
    public String getDefaultUserRole(String user)
     throws TapisClientException
    {
        // Make the REST call.
        RespName resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi(_apiClient);
            resp = userApi.getDefaultUserRole1(user);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult().getName();
    }

    /* ---------------------------------------------------------------------------- */
    /* removePathPermissionFromAllRoles                                             */
    /* ---------------------------------------------------------------------------- */
    public int removePathPermissionFromAllRoles(String tenant, String permSpec)
        throws TapisClientException
    {
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var roleApi = new RoleApi(_apiClient);
            var req = new ReqRemovePermissionFromAllRoles();
            req.setTenant(tenant);
            req.setPermSpec(permSpec);
            resp = roleApi.removePathPermissionFromAllRoles(req);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}

        // Return result value.
        return resp.getResult().getChanges();
    }

    /* ---------------------------------------------------------------------------- */
    /* removepermissionFromAllRoles                                             */
    /* ---------------------------------------------------------------------------- */
    public int removePermissionFromAllRoles(String tenant, String permSpec)
        throws TapisClientException
    {
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var roleApi = new RoleApi(_apiClient);
            var req = new ReqRemovePermissionFromAllRoles();
            req.setTenant(tenant);
            req.setPermSpec(permSpec);
            resp = roleApi.removePermissionFromAllRoles(req);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}

        // Return result value.
        return resp.getResult().getChanges();
    }
    
    /* **************************************************************************** */
    /*                             Public Share Methods                             */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* shareResource:                                                               */
    /* ---------------------------------------------------------------------------- */
    public String shareResource(ReqShareResource reqShareResource)
       throws TapisClientException
    {
        RespResourceUrl resp = null;
        var shareApi = new ShareApi(_apiClient);
        try {resp = shareApi.shareResource(reqShareResource);}
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp.getResult().getUrl();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getShare:                                                                    */
    /* ---------------------------------------------------------------------------- */
    public SkShare getShare(int id, String tenant) throws TapisClientException
    {
        RespShare resp = null;
        var shareApi = new ShareApi(_apiClient);
        try {resp = shareApi.getShare(id, tenant);}
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getShares:                                                                   */
    /* ---------------------------------------------------------------------------- */
    public SkShareList getShares(SKShareGetSharesParms p) throws TapisClientException
    {
        RespShareList resp = null;
        var shareApi = new ShareApi(_apiClient);
        try {resp = shareApi.getShares(p.getGrantor(), p.getGrantee(), p.getTenant(), p.getResourceType(),
                p.getResourceId1(), p.getResourceId2(), p.getPrivilege(), p.getCreatedBy(),
                p.getCreatedByTenant(), p.isIncludePublicGrantees(), p.isRequireNullId2(), 
                p.getId());}
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* deleteShareById:                                                             */
    /* ---------------------------------------------------------------------------- */
    public int deleteShareById(int id, String tenant) throws TapisClientException
    {
        RespChangeCount resp = null;
        var shareApi = new ShareApi(_apiClient);
        try {resp = shareApi.deleteShareById(id, tenant);}
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        var changes = resp.getResult().getChanges();
        return changes == null ? 0 : changes;
    }

    /* ---------------------------------------------------------------------------- */
    /* deleteShare:                                                                 */
    /* ---------------------------------------------------------------------------- */
    public int deleteShare(SKShareDeleteShareParms p) throws TapisClientException
    {
        RespChangeCount resp = null;
        var shareApi = new ShareApi(_apiClient);
        try {resp = shareApi.deleteShare(p.getGrantor(), p.getGrantee(), p.getTenant(), p.getResourceType(),
                p.getResourceId1(), p.getResourceId2(), p.getPrivilege());}
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        var changes = resp.getResult().getChanges();
        return changes == null ? 0 : changes;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* hasPrivilege:                                                                */
    /* ---------------------------------------------------------------------------- */
    public boolean hasPrivilege(SKShareHasPrivilegeParms p)
        throws TapisClientException
    {
        RespBoolean resp = null;
        var shareApi = new ShareApi(_apiClient);
        try {resp = shareApi.hasPrivilege(p.getGrantee(), p.getTenant(), p.getResourceType(),
                p.getResourceId1(), p.getResourceId2(), p.getPrivilege(), 
                p.isExcludePublic(), p.isExcludePublicNoAuthn());}
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        var bool = resp.getResult().getaBool();
        return bool == null ? false : bool;
    }
    
    /* **************************************************************************** */
    /*                             Public Vault Methods                             */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* readSecret:                                                                  */
    /* ---------------------------------------------------------------------------- */

    /**
     * readSecret
     * @param parms parameters used to look up secret
     * @return the secret, or null if not found (404)
     * @throws TapisClientException on error
     */
    public SkSecret readSecret(SKSecretReadParms parms) throws TapisClientException
    {
        // Make the REST call.
        RespSecret resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi(_apiClient);
            resp = vaultApi.readSecret(parms.getSecretType().getUrlText(),
                                       parms.getSecretName(),
                                       parms.getTenant(),
                                       parms.getUser(),
                                       parms.getVersion(),
                                       parms.getSysId(),
                                       parms.getSysUser(),
                                       parms.getKeyType().name(),
                                       parms.getDbHost(),
                                       parms.getDbName(),
                                       parms.getDbService());
        }
        catch (ApiException e)
        {
          // If 404 return null
          if (e.getCode() == 404) return null;
          Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);
        }
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}

        // Should never be null
        if (resp == null || resp.getResult() == null) Utils.throwTapisClientException(-1, null, null);

        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* writeSecret:                                                                  */
    /* ---------------------------------------------------------------------------- */
    public SkSecretMetadata writeSecret(String tenant, String user, SKSecretWriteParms parms)
     throws TapisClientException
    {
        // Package the input.
        ReqWriteSecret reqWriteSecret = new ReqWriteSecret();
        reqWriteSecret.setTenant(tenant);
        reqWriteSecret.setUser(user);
        reqWriteSecret.setData(parms.getData());
        if (parms.getOptions() == null) reqWriteSecret.setOptions(new Options());
         else reqWriteSecret.setOptions(parms.getOptions());

        // Make the REST call.
        RespSecretMeta resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi(_apiClient);
            resp = vaultApi.writeSecret(parms.getSecretType().getUrlText(),
                                        parms.getSecretName(),
                                        reqWriteSecret,
                                        parms.getSysId(),
                                        parms.getSysUser(),
                                        parms.getKeyType().name(),
                                        parms.getDbHost(),
                                        parms.getDbName(),
                                        parms.getDbService());

        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* deleteSecret:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<Integer> deleteSecret(String tenant, String user, SKSecretDeleteParms parms)
     throws TapisClientException
    {
        // Package the input.
        ReqVersions reqVersions = new ReqVersions();
        reqVersions.setTenant(tenant);
        reqVersions.setUser(user);
        reqVersions.setVersions(parms.getVersions());

        // Make the REST call.
        RespVersions resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi(_apiClient);
            resp = vaultApi.deleteSecret(parms.getSecretType().getUrlText(),
                                         parms.getSecretName(),
                                         reqVersions,
                                         parms.getSysId(),
                                         parms.getSysUser(),
                                         parms.getKeyType().name(),
                                         parms.getDbHost(),
                                         parms.getDbName(),
                                         parms.getDbService());
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* undeleteSecret:                                                              */
    /* ---------------------------------------------------------------------------- */
    public List<Integer> undeleteSecret(String tenant, String user, SKSecretDeleteParms parms)
     throws TapisClientException
    {
        // Package the input.
        ReqVersions reqVersions = new ReqVersions();
        reqVersions.setTenant(tenant);
        reqVersions.setUser(user);
        reqVersions.setVersions(parms.getVersions());
        
        // Make the REST call.
        RespVersions resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi(_apiClient);
            resp = vaultApi.undeleteSecret(parms.getSecretType().getUrlText(),
                                           parms.getSecretName(),
                                           reqVersions,
                                           parms.getSysId(),
                                           parms.getSysUser(),
                                           parms.getKeyType().name(),
                                           parms.getDbHost(),
                                           parms.getDbName(),
                                           parms.getDbService());
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* destroySecret:                                                               */
    /* ---------------------------------------------------------------------------- */
    public List<Integer> destroySecret(String tenant, String user, SKSecretDeleteParms parms)
     throws TapisClientException
    {
        // Package the input.
        ReqVersions reqVersions = new ReqVersions();
        reqVersions.setTenant(tenant);
        reqVersions.setUser(user);
        reqVersions.setVersions(parms.getVersions());
        
        // Make the REST call.
        RespVersions resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi(_apiClient);
            resp = vaultApi.destroySecret(parms.getSecretType().getUrlText(),
                                          parms.getSecretName(),
                                          reqVersions,
                                          parms.getSysId(),
                                          parms.getSysUser(),
                                          parms.getKeyType().name(),
                                          parms.getDbHost(),
                                          parms.getDbName(),
                                          parms.getDbService());
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult();
    }

    public boolean rolePermits(String roleName, String roleTenant, RoleTypeEnum roleType, String permSpec, boolean immediate)
            throws TapisClientException
    {
        // Assign input body.
        var body = new ReqRolePermits();
        body.setRoleTenant(roleTenant);
        body.setRoleType(roleType);
        body.setPermSpec(permSpec);

        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var roleApi = new RoleApi(_apiClient);
            resp = roleApi.rolePermits(roleName, body, immediate);
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}

        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }

    /* ---------------------------------------------------------------------------- */
    /* readSecretMeta:                                                              */
    /* ---------------------------------------------------------------------------- */
    /**
     * readSecretMeta
     * @param parms parameters used to look up secret metadata
     * @return the secret, or null if not found (404)
     * @throws TapisClientException on error
     */
    public SkSecretVersionMetadata readSecretMeta(SKSecretMetaParms parms) throws TapisClientException
    {
        // Make the REST call.
        RespSecretVersionMetadata resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi(_apiClient);
            resp = vaultApi.readSecretMeta(parms.getSecretType().getUrlText(),
                                           parms.getSecretName(),
                                           parms.getTenant(),
                                           parms.getUser(),
                                           parms.getSysId(),
                                           parms.getSysUser(),
                                           parms.getKeyType().name(),
                                           parms.getDbHost(),
                                           parms.getDbName(),
                                           parms.getDbService());
        }
        catch (ApiException e)
        {
            // If 404 return null
            if (e.getCode() == 404) return null;
          Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);
        }
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}

        // Should never be null
        if (resp == null || resp.getResult() == null) Utils.throwTapisClientException(-1, null, null);

        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* listSecretMeta:                                                              */
    /* ---------------------------------------------------------------------------- */
    public SkSecretList listSecretMeta(SKSecretMetaParms parms)
     throws TapisClientException
    {
        // Make the REST call.
        RespSecretList resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi(_apiClient);
            resp = vaultApi.listSecretMeta(parms.getSecretType().getUrlText(),
                                           parms.getTenant(),
                                           parms.getUser(),
                                           parms.getSysId(),
                                           parms.getSysUser(),
                                           parms.getKeyType().name(),
                                           parms.getDbHost(),
                                           parms.getDbName(),
                                           parms.getDbService());
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* destroySecretMeta:                                                           */
    /* ---------------------------------------------------------------------------- */
    public void destroySecretMeta(SKSecretMetaParms parms)
     throws TapisClientException
    {
        // Make the REST call.
        @SuppressWarnings("unused")
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi(_apiClient);
            resp = vaultApi.destroySecretMeta(parms.getSecretType().getUrlText(),
                                              parms.getSecretName(),
                                              parms.getTenant(),
                                              parms.getUser(),
                                              parms.getSysId(),
                                              parms.getSysUser(),
                                              parms.getKeyType().name(),
                                              parms.getDbHost(),
                                              parms.getDbName(),
                                              parms.getDbService());
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
    }
    
    /* ---------------------------------------------------------------------------- */
    /* validateServicePassword:                                                     */
    /* ---------------------------------------------------------------------------- */
    public boolean validateServicePassword(String tenant, String user, 
                                           String serviceName, String password) 
     throws TapisClientException
    {
        // Initialize parameter.
        var reqValidateServicePwd = new ReqValidatePwd();
        reqValidateServicePwd.setTenant(tenant);
        reqValidateServicePwd.setUser(user);
        reqValidateServicePwd.setPassword(password);
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi(_apiClient);
            resp = vaultApi.validateServicePassword(serviceName, 
                                                    reqValidateServicePwd);
            
        }
        catch (ApiException e) {Utils.throwTapisClientException(e.getCode(), e.getResponseBody(), e);}
        catch (Exception e) {Utils.throwTapisClientException(-1, null, e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* **************************************************************************** */
    /*                            Public General Methods                            */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* hello:                                                                       */
    /* ---------------------------------------------------------------------------- */
    public String readycheck()
     throws TapisClientException
    {
        // Make the REST call.
        RespProbe resp = null;
        try {
            // Get the API object using default networking.
            var generalApi = new GeneralApi(_apiClient);
            resp = generalApi.readycheck()

            ;
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
    
    /* **************************************************************************** */
    /*                               Private Methods                                */
    /* **************************************************************************** */

}
