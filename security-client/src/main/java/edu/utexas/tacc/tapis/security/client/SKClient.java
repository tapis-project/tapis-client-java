package edu.utexas.tacc.tapis.security.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import edu.utexas.tacc.tapis.security.client.gen.ApiClient;
import edu.utexas.tacc.tapis.security.client.gen.ApiException;
import edu.utexas.tacc.tapis.security.client.gen.Configuration;
import edu.utexas.tacc.tapis.security.client.gen.api.GeneralApi;
import edu.utexas.tacc.tapis.security.client.gen.api.RoleApi;
import edu.utexas.tacc.tapis.security.client.gen.api.UserApi;
import edu.utexas.tacc.tapis.security.client.gen.api.VaultApi;
import edu.utexas.tacc.tapis.security.client.gen.model.Options;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqAddChildRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqAddRolePermission;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqCreateRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqGrantUserPermission;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqGrantUserRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqGrantUserRoleWithPermission;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqPreviewPathPrefix;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqRemoveChildRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqRemoveRolePermission;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqReplacePathPrefix;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqRevokeUserPermission;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqRevokeUserRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUpdateRoleDescription;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUpdateRoleName;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUserHasRole;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUserHasRoleMulti;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUserIsPermitted;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqUserIsPermittedMulti;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqVersions;
import edu.utexas.tacc.tapis.security.client.gen.model.ReqWriteSecret;
import edu.utexas.tacc.tapis.security.client.gen.model.RespAuthorized;
import edu.utexas.tacc.tapis.security.client.gen.model.RespBasic;
import edu.utexas.tacc.tapis.security.client.gen.model.RespChangeCount;
import edu.utexas.tacc.tapis.security.client.gen.model.RespName;
import edu.utexas.tacc.tapis.security.client.gen.model.RespNameArray;
import edu.utexas.tacc.tapis.security.client.gen.model.RespPathPrefixes;
import edu.utexas.tacc.tapis.security.client.gen.model.RespResourceUrl;
import edu.utexas.tacc.tapis.security.client.gen.model.RespRole;
import edu.utexas.tacc.tapis.security.client.gen.model.RespSecret;
import edu.utexas.tacc.tapis.security.client.gen.model.RespSecretList;
import edu.utexas.tacc.tapis.security.client.gen.model.RespSecretMeta;
import edu.utexas.tacc.tapis.security.client.gen.model.RespSecretVersionMetadata;
import edu.utexas.tacc.tapis.security.client.gen.model.RespVersions;
import edu.utexas.tacc.tapis.security.client.gen.model.SkRole;
import edu.utexas.tacc.tapis.security.client.gen.model.SkSecret;
import edu.utexas.tacc.tapis.security.client.gen.model.SkSecretList;
import edu.utexas.tacc.tapis.security.client.gen.model.SkSecretMetadata;
import edu.utexas.tacc.tapis.security.client.gen.model.SkSecretVersionMetadata;
import edu.utexas.tacc.tapis.security.client.gen.model.Transformation;
import edu.utexas.tacc.tapis.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.shared.utils.TapisGsonUtils;


public class SKClient 
{
    /* **************************************************************************** */
    /*                                   Constants                                  */
    /* **************************************************************************** */
    // Response status.
    public static final String STATUS_SUCCESS = "success";
    
    // Header key for jwts.
    public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";
    
    // Configuration defaults.
    private static final String SKCLIENT_USER_AGENT = "SKClient";
    
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
    public SKClient() {this(null, null);}
    
    /* ---------------------------------------------------------------------------- */
    /* constructor:                                                                 */
    /* ---------------------------------------------------------------------------- */
    /** Constructor that overrides the compiled-in basePath value in ApiClient.  This
     * constructor typically used in production.
     * 
     * The path includes the URL prefix up to and including the service root.  By
     * default this value is http://localhost:8080/security.  In production environments
     * the protocol is https and the host/port will be specific to that environment. 
     * 
     * The jwt is the base64url representation of a Tapis JWT.  If not null or empty,
     * the TAPIS_JWT_HEADER key will be set to the jwt value. 
     * 
     * The user-agent is automatically set to SKClient.
     * 
     * Instances of this class are currently limited to using the default ApiClient.
     * This implies that the RoleApi, UserApi and GeneralApi implementations also
     * are expected to be using the same default ApiClient object.
     * 
     * @param path the base path 
     */
    public SKClient(String path, String jwt) 
    {
        // Process input.
        ApiClient apiClient = Configuration.getDefaultApiClient();
        if (!StringUtils.isBlank(path)) apiClient.setBasePath(path);
        if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
        
        // Other defaults.
        apiClient.setUserAgent(SKCLIENT_USER_AGENT);
    }
    
    /* **************************************************************************** */
    /*                                Utility Methods                               */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* addDefaultHeader:                                                            */
    /* ---------------------------------------------------------------------------- */
    public SKClient addDefaultHeader(String key, String value)
    {
        Configuration.getDefaultApiClient().addDefaultHeader(key, value);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setUserAgent:                                                                */
    /* ---------------------------------------------------------------------------- */
    public SKClient setUserAgent(String userAgent) 
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
    public SKClient setConnectTimeout(int millis)
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
    public SKClient setReadTimeout(int millis)
    {
        Configuration.getDefaultApiClient().setReadTimeout(millis);
        return this;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* setDebugging:                                                                */
    /* ---------------------------------------------------------------------------- */
    public SKClient setDebugging(boolean debugging) 
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
    
    /* **************************************************************************** */
    /*                              Public Role Methods                             */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* getRoleNames:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<String> getRoleNames()
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.getRoleNames(false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getRoleByName:                                                               */
    /* ---------------------------------------------------------------------------- */
    public SkRole getRoleByName(String roleName)
     throws TapisClientException
    {
        // Make the REST call.
        RespRole resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.getRoleByName(roleName, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* createRole:                                                                  */
    /* ---------------------------------------------------------------------------- */
    public String createRole(String roleName, String description)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqCreateRole();
        body.setRoleName(roleName);
        body.setDescription(description);
        
        // Make the REST call.
        RespResourceUrl resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.createRole(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult().getUrl();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* deleteRoleByName:                                                            */
    /* ---------------------------------------------------------------------------- */
    public int deleteRoleByName(String roleName)
     throws TapisClientException
    {
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.deleteRoleByName(roleName, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* updateRoleName:                                                              */
    /* ---------------------------------------------------------------------------- */
    public void updateRoleName(String roleName, String newRoleName)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUpdateRoleName();
        body.setNewRoleName(newRoleName);
        
        // Make the REST call.
        @SuppressWarnings("unused")
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.updateRoleName(roleName, body,false);
        }
        catch (Exception e) {throwTapisClientException(e);}
    }
    
    /* ---------------------------------------------------------------------------- */
    /* updateRoleDescription:                                                       */
    /* ---------------------------------------------------------------------------- */
    public void updateRoleDescription(String roleName, String description)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUpdateRoleDescription();
        body.setDescription(description);
        
        // Make the REST call.
        @SuppressWarnings("unused")
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.updateRoleDescription(roleName, body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
    }
    
    /* ---------------------------------------------------------------------------- */
    /* addRolePermission:                                                           */
    /* ---------------------------------------------------------------------------- */
    public int addRolePermission(String roleName, String permSpec)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqAddRolePermission();
        body.setRoleName(roleName);
        body.setPermSpec(permSpec);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.addRolePermission(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* removeRolePermission:                                                        */
    /* ---------------------------------------------------------------------------- */
    public int removeRolePermission(String roleName, String permSpec)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqRemoveRolePermission();
        body.setRoleName(roleName);
        body.setPermSpec(permSpec);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.removeRolePermission(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* addChildRole:                                                                */
    /* ---------------------------------------------------------------------------- */
    public int addChildRole(String parentRoleName, String childRoleName)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqAddChildRole();
        body.setParentRoleName(parentRoleName);
        body.setChildRoleName(childRoleName);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.addChildRole(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* removeChildRole:                                                             */
    /* ---------------------------------------------------------------------------- */
    public int removeChildRole(String parentRoleName, String childRoleName)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqRemoveChildRole();
        body.setParentRoleName(parentRoleName);
        body.setChildRoleName(childRoleName);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.removeChildRole(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* previewPathPrefix:                                                           */
    /* ---------------------------------------------------------------------------- */
    public List<Transformation> previewPathPrefix(String schema, String roleName,
                                                  String oldSystemId, String newSystemId,
                                                  String oldPrefix, String newPrefix)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqPreviewPathPrefix();
        body.setSchema(schema);
        body.setRoleName(roleName);
        body.setOldSystemId(oldSystemId);
        body.setNewSystemId(newSystemId);
        body.setOldPrefix(oldPrefix);
        body.setNewPrefix(newPrefix);
        
        // Make the REST call.
        RespPathPrefixes resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.previewPathPrefix(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* replacePathPrefix:                                                           */
    /* ---------------------------------------------------------------------------- */
    public int replacePathPrefix(String schema, String roleName,
                                               String oldSystemId, String newSystemId,
                                               String oldPrefix, String newPrefix)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqReplacePathPrefix();
        body.setSchema(schema);
        body.setRoleName(roleName);
        body.setOldSystemId(oldSystemId);
        body.setNewSystemId(newSystemId);
        body.setOldPrefix(oldPrefix);
        body.setNewPrefix(newPrefix);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            RoleApi roleApi = new RoleApi();
            resp = roleApi.replacePathPrefix(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
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
    public List<String> getUserNames()
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.getUserNames(false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getUserNames:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUserRoles(String user)
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.getUserRoles(user, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getUserPerms:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUserPerms(String user)
     throws TapisClientException
    {
        return getUserPerms(user, null, null);
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getUserPerms:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUserPerms(String user, String implies, 
                                        String impliedBy)
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.getUserPerms(user, implies, impliedBy, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* grantUserRole:                                                               */
    /* ---------------------------------------------------------------------------- */
    public int grantUserRole(String user, String roleName)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqGrantUserRole();
        body.setUser(user);
        body.setRoleName(roleName);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.grantRole(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* revokeUserRole:                                                              */
    /* ---------------------------------------------------------------------------- */
    public int revokeUserRole(String user, String roleName)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqRevokeUserRole();
        body.setUser(user);
        body.setRoleName(roleName);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.revokeUserRole(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* grantRoleWithPermission:                                                     */
    /* ---------------------------------------------------------------------------- */
    public int grantRoleWithPermission(String user, String roleName, String permSpec)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqGrantUserRoleWithPermission();
        body.setUser(user);
        body.setRoleName(roleName);
        body.setPermSpec(permSpec);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.grantRoleWithPermission(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* grantRoleWithPermission:                                                     */
    /* ---------------------------------------------------------------------------- */
    public int grantUserPermission(String user, String permSpec)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqGrantUserPermission();
        body.setUser(user);
        body.setPermSpec(permSpec);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.grantUserPermission(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* revokeRoleWithPermission:                                                    */
    /* ---------------------------------------------------------------------------- */
    public int revokeUserPermission(String user, String permSpec)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqRevokeUserPermission();
        body.setUser(user);
        body.setPermSpec(permSpec);
        
        // Make the REST call.
        RespChangeCount resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.revokeUserPermission(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Integer x = resp.getResult().getChanges();
        return x == null ? 0 : x;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* hasRole:                                                                     */
    /* ---------------------------------------------------------------------------- */
    public boolean hasRole(String user, String roleName)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserHasRole();
        body.setUser(user);
        body.setRoleName(roleName);
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.hasRole(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* hasAnyRole:                                                                  */
    /* ---------------------------------------------------------------------------- */
    public boolean hasRoleAny(String user, String[] roleNames)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserHasRoleMulti();
        body.setUser(user);
        body.setRoleNames(Arrays.asList(roleNames));
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.hasRoleAny(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* hasAllRole:                                                                  */
    /* ---------------------------------------------------------------------------- */
    public boolean hasRoleAll(String user, String[] roleNames)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserHasRoleMulti();
        body.setUser(user);
        body.setRoleNames(Arrays.asList(roleNames));
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.hasRoleAll(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* isPermitted:                                                                 */
    /* ---------------------------------------------------------------------------- */
    public boolean isPermitted(String user, String permSpec)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserIsPermitted();
        body.setUser(user);
        body.setPermSpec(permSpec);
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.isPermitted(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* isPermittedAny:                                                              */
    /* ---------------------------------------------------------------------------- */
    public boolean isPermittedAny(String user, String[] permSpecs)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserIsPermittedMulti();
        body.setUser(user);
        body.setPermSpecs(Arrays.asList(permSpecs));
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.isPermittedAny(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* isPermittedAll:                                                              */
    /* ---------------------------------------------------------------------------- */
    public boolean isPermittedAll(String user, String[] permSpecs)
     throws TapisClientException
    {
        // Assign input body.
        var body = new ReqUserIsPermittedMulti();
        body.setUser(user);
        body.setPermSpecs(Arrays.asList(permSpecs));
        
        // Make the REST call.
        RespAuthorized resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.isPermittedAll(body, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        Boolean b = resp.getResult().getIsAuthorized();
        return b == null ? false : b;
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getUsersWithRole:                                                            */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUsersWithRole(String roleName)
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.getUsersWithRole(roleName, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult().getNames();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* getUsersWithPermission:                                                      */
    /* ---------------------------------------------------------------------------- */
    public List<String> getUsersWithPermission(String permSpec)
     throws TapisClientException
    {
        // Make the REST call.
        RespNameArray resp = null;
        try {
            // Get the API object using default networking.
            var userApi = new UserApi();
            resp = userApi.getUsersWithPermission(permSpec, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
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
            var userApi = new UserApi();
            resp = userApi.getDefaultUserRole1(user, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult().getName();
    }
    
    /* **************************************************************************** */
    /*                             Public Vault Methods                             */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* readSecret:                                                                  */
    /* ---------------------------------------------------------------------------- */
    public SkSecret readSecret(String secretName, Integer version)
     throws TapisClientException
    {
        // Make the REST call.
        RespSecret resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi();
            resp = vaultApi.readSecret(secretName, version, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* writeSecret:                                                                  */
    /* ---------------------------------------------------------------------------- */
    public SkSecretMetadata writeSecret(String secretName, Map<String,String> secrets,
                                        Options options)
     throws TapisClientException
    {
        // Package the input.
        ReqWriteSecret reqWriteSecret = new ReqWriteSecret();
        reqWriteSecret.setData(secrets);
        if (options == null) options = new Options();
        reqWriteSecret.setOptions(options);
        
        // Make the REST call.
        RespSecretMeta resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi();
            resp = vaultApi.writeSecret(secretName, reqWriteSecret, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* deleteSecret:                                                                */
    /* ---------------------------------------------------------------------------- */
    public List<Integer> deleteSecret(String secretName, List<Integer> versions)
     throws TapisClientException
    {
        // Package the input.
        ReqVersions reqVersions = new ReqVersions();
        reqVersions.setVersions(versions);
        
        // Make the REST call.
        RespVersions resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi();
            resp = vaultApi.deleteSecret(secretName, reqVersions, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* undeleteSecret:                                                              */
    /* ---------------------------------------------------------------------------- */
    public List<Integer> undeleteSecret(String secretName, List<Integer> versions)
     throws TapisClientException
    {
        // Package the input.
        ReqVersions reqVersions = new ReqVersions();
        reqVersions.setVersions(versions);
        
        // Make the REST call.
        RespVersions resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi();
            resp = vaultApi.undeleteSecret(secretName, reqVersions, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* destroySecret:                                                               */
    /* ---------------------------------------------------------------------------- */
    public List<Integer> destroySecret(String secretName, List<Integer> versions)
     throws TapisClientException
    {
        // Package the input.
        ReqVersions reqVersions = new ReqVersions();
        reqVersions.setVersions(versions);
        
        // Make the REST call.
        RespVersions resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi();
            resp = vaultApi.destroySecret(secretName, reqVersions, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* readSecretMeta:                                                              */
    /* ---------------------------------------------------------------------------- */
    public SkSecretVersionMetadata readSecretMeta(String secretName)
     throws TapisClientException
    {
        // Make the REST call.
        RespSecretVersionMetadata resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi();
            resp = vaultApi.readSecretMeta(secretName, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* listSecretMeta:                                                              */
    /* ---------------------------------------------------------------------------- */
    public SkSecretList listSecretMeta(String secretName)
     throws TapisClientException
    {
        // Make the REST call.
        RespSecretList resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi();
            resp = vaultApi.listSecretMeta(secretName, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value.
        return resp.getResult();
    }
    
    /* ---------------------------------------------------------------------------- */
    /* destroySecretMeta:                                                           */
    /* ---------------------------------------------------------------------------- */
    public void destroySecretMeta(String secretName)
     throws TapisClientException
    {
        // Make the REST call.
        @SuppressWarnings("unused")
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            var vaultApi = new VaultApi();
            resp = vaultApi.destroySecretMeta(secretName, false);
        }
        catch (Exception e) {throwTapisClientException(e);}
    }
    
    /* **************************************************************************** */
    /*                            Public General Methods                            */
    /* **************************************************************************** */
    /* ---------------------------------------------------------------------------- */
    /* sayHello:                                                                    */
    /* ---------------------------------------------------------------------------- */
    public String sayHello()
     throws TapisClientException
    {
        // Make the REST call.
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            var generalApi = new GeneralApi();
            resp = generalApi.sayHello(false);
        }
        catch (Exception e) {throwTapisClientException(e);}
        
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
        RespBasic resp = null;
        try {
            // Get the API object using default networking.
            var generalApi = new GeneralApi();
            resp = generalApi.checkHealth();
        }
        catch (Exception e) {throwTapisClientException(e);}
        
        // Return result value as a string.
        Object obj = resp.getResult();
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
