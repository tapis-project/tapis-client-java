package edu.utexas.tacc.tapis.security.client;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import edu.utexas.tacc.tapis.security.client.gen.model.SkRole;

/** This test uses two clients, each initialized with different JWT identities,
 * to test the owner authorization enforcement.  Several calls that require
 * role ownership are made to validate that the calls are rejected by SK
 * for authorization reasons as part of the test.  
 * 
 * When this test completes successfully, the SK database tables are restored
 * to their original state.
 * 
 * Required environment variables:  FILES_JWT, SYSTEMS_JWT
 * Optional environment variables:  TEST_TENANT
 * 
 * @author rich
 */
@Test(groups={"integration"})
public class ServiceJwtAuthTest 
{
    /* ********************************************************************** */
    /*                               Constants                                */
    /* ********************************************************************** */
	// Point to SK.
	private static final String DEFAULT_SKCLIENT_BASEURL = "http://localhost:8080/v3";
	private static final String DEFAULT_TENANT = "dev";
	
	// Environment variable names.
	private static final String ENV_FILES_JWT = "FILES_JWT";
	private static final String ENV_SYSTEMS_JWT = "SYSTEMS_JWT";
	private static final String ENV_TENANT = "TEST_TENANT";
	
    /* ********************************************************************** */
    /*                                 Fields                                 */
    /* ********************************************************************** */
	// Files service JWT passed in as an environment variable.
	private String _filesJwt; 
	private String _systemsJwt;
	private String _tenant;
	
    /* ********************************************************************** */
    /*                            Before Processing                           */
    /* ********************************************************************** */
    /* ---------------------------------------------------------------------- */
    /* BeforeSuite:                                                           */
    /* ---------------------------------------------------------------------- */
	@BeforeSuite
	public void beforeSuite()
	{
		// Required environment values.
		_filesJwt = System.getenv(ENV_FILES_JWT);
		_systemsJwt = System.getenv(ENV_SYSTEMS_JWT);
		
		// Allow optional tenant override.
		_tenant = System.getenv(ENV_TENANT);
		if (StringUtils.isBlank(_tenant)) _tenant = DEFAULT_TENANT;
		
		// Check JWT.
		if (StringUtils.isBlank(_filesJwt) || StringUtils.isBlank(_systemsJwt)) 
		{
			String msg = "Unable to run test due to missing JWT.\n\n"
					+ "This test requires that a service JWT be defined for both "
					+ "the *files* service and the *systems* service in the test tenant. "
					+ "The FILES_JWT and SYSTEMS_JWT environment variables must be "
					+ "assigned their respective JWT values.\n\n"
					+ "The default test tenant is \"" + DEFAULT_TENANT +"\", but this can be "
					+ "overridden by defining the TEST_TENANT environment variable." ;
			throw new IllegalArgumentException(msg);
		}
	}

    /* ********************************************************************** */
    /*                              Test Methods                              */
    /* ********************************************************************** */
    /* ---------------------------------------------------------------------- */
    /* roleLifecycleTest:                                                     */
    /* ---------------------------------------------------------------------- */
    @Test(enabled = true)
    public void filesTest() throws Exception
    {
    	// Set up the two clients.
    	SKClient skFilesClient = new SKClient(DEFAULT_SKCLIENT_BASEURL, _filesJwt);
    	skFilesClient.addDefaultHeader("X-Tapis-User", "files");
    	skFilesClient.addDefaultHeader("X-Tapis-Tenant", "master");
    	SKClient skSysClient = new SKClient(DEFAULT_SKCLIENT_BASEURL, _systemsJwt);
    	skSysClient.addDefaultHeader("X-Tapis-User", "systems");
    	skSysClient.addDefaultHeader("X-Tapis-Tenant", "master");
    	
    	// Assign test values.
        final String roleName = "files_role2";
        final String user1 = "bud";
        final String user2 = "jane";
        final String perm1 = "systems:moon:*:yy";
        final String perm2 = "meta:dog:sled";
        
        // Get role names.
        List<String> respList;
        try {respList = skFilesClient.getRoleNames(_tenant);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleNames: " + respList);
        
        // Create a role.
        String resp1;
        try {resp1 = skFilesClient.createRole(_tenant, roleName, "you little piggy.");}
            catch (Exception e) {
                System.out.println(e.toString());
                throw e;
            }
        System.out.println("createRole: " + resp1);
        
        // Get role names.
        try {respList = skFilesClient.getRoleNames(_tenant);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleNames: " + respList);
        Assert.assertTrue(respList.contains(roleName), 
        		          "Expected " + roleName + " in list.");
        
        // Get role names.
        try {respList = skSysClient.getRoleNames(_tenant);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleNames: " + respList);
        Assert.assertTrue(respList.contains(roleName), 
        		          "Expected " + roleName + " in list.");
        
        // Try to assign a user the role as not the role owner.
        int respInt = -1;
        try {respInt = skSysClient.grantUserRole(_tenant, user1, roleName);}
        catch (Exception e) {
        	boolean expected = e.getMessage().startsWith("SK_API_AUTHORIZATION_FAILED");
        	Assert.assertTrue(expected, "Expected authorization error due to improper owner.");
        	if (!expected) throw e;
        }
        Assert.assertEquals(respInt, -1, "No authorization exception thrown.");
        System.out.println("grantUserRole: failed as expected because of owner");
        
        // Assign a user the role.
        try {respInt = skFilesClient.grantUserRole(_tenant, user1, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("grantUserRole: " + respInt);
        
        // Assign a user the role.
        try {respInt = skFilesClient.grantUserRole(_tenant, user2, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("grantUserRole: " + respInt);
        
        // Get users names.
        try {respList = skFilesClient.getUserNames(_tenant);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getUserNames: " + respList);
        Assert.assertTrue(respList.contains(user1), 
		                  "Expected " + user1 + " in list.");
        Assert.assertTrue(respList.contains(user2), 
                	      "Expected " + user2 + " in list.");
        
        // Get users with role.
        try {respList = skSysClient.getUsersWithRole(_tenant, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getUsersWithRole: " + respList);
        Assert.assertTrue(respList.contains(user1), 
                          "Expected " + user1 + " in list.");
        Assert.assertTrue(respList.contains(user2), 
      	                  "Expected " + user2 + " in list.");
        
        // Get a user's roles.
        try {respList = skSysClient.getUserRoles(_tenant, user1);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getUserRoles: " + respList);
        Assert.assertTrue(respList.contains(roleName), 
                          "Expected " + roleName + " in list.");
        
        // Try to add a permission to a role as not owner.
        respInt = -1;
        try {respInt = skSysClient.addRolePermission(_tenant, roleName, perm1);}
        catch (Exception e) {
        	boolean expected = e.getMessage().startsWith("SK_API_AUTHORIZATION_FAILED");
        	Assert.assertTrue(expected, "Expected authorization error due to improper owner.");
        	if (!expected) throw e;
        }
        Assert.assertEquals(respInt, -1, "No authorization exception thrown.");
        System.out.println("addRolePermission: failed as expected because of owner");
        
        // Add a permission to a role.
        try {respInt = skFilesClient.addRolePermission(_tenant, roleName, perm1);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("addRolePermission: " + respInt);
        
        // Add a permission to a role.
        try {respInt = skFilesClient.addRolePermission(_tenant, roleName, perm2);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("addRolePermission: " + respInt);
        
        // Get all the permissions in a role.
        try {respList = skSysClient.getRolePermissions(_tenant, roleName, false);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRolePermissions: " + respList);
        Assert.assertTrue(respList.contains(perm1), 
                          "Expected " + perm1 + " in list.");
        Assert.assertTrue(respList.contains(perm2), 
                          "Expected " + perm2 + " in list.");
        
        // Get all the permissions a user has.
        try {respList = skSysClient.getUserPerms(_tenant, user1);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getUserPerms: " + respList);
        Assert.assertTrue(respList.contains(perm1), 
                          "Expected " + perm1 + " in list.");
        Assert.assertTrue(respList.contains(perm2), 
                          "Expected " + perm2 + " in list.");
        
        // See if a user has a role (positive case).
        boolean respBool;
        try {respBool = skSysClient.hasRole(_tenant, user1, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("hasRole: " + respBool);
        Assert.assertTrue(respBool, "Expected " + user1 + " to have role " + roleName + ".");
        
        // See of a user has a matching permission (positive case).
        try {respBool = skSysClient.isPermitted(_tenant, user1, perm2);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("isPermitted: " + respBool);
        Assert.assertTrue(respBool, "Expected " + user1 + " to have permission " + perm2 + ".");
        
        // See of a user has a matching permission (positive case).
        try {respBool = skSysClient.isPermittedAny(_tenant, user1, 
        		          new String[] {"not:a:perm", perm2});}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("isPermittedAny: " + respBool);
        Assert.assertTrue(respBool, "Expected " + user1 + " to have permission " + perm2 + ".");
        
        // See of a user has a matching permission (negative case).
        try {respBool = skSysClient.isPermittedAll(_tenant, user1, 
        		          new String[] {"not:a:perm", perm2});}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("isPermittedAll: " + respBool);
        Assert.assertFalse(respBool, "Expected " + user1 + " to not have permission not:a:perm.");
        
        // Remove a permission from a role.
        respInt = -1;
        try {respInt = skSysClient.removeRolePermission(_tenant, roleName, perm2);}
        catch (Exception e) {
        	boolean expected = e.getMessage().startsWith("SK_API_AUTHORIZATION_FAILED");
        	Assert.assertTrue(expected, "Expected authorization error due to improper owner.");
        	if (!expected) throw e;
        }
        Assert.assertEquals(respInt, -1, "No authorization exception thrown.");
        System.out.println("removeRolePermission: failed as expected because of owner");
        
        // Remove a permission from a role.
        try {respInt = skFilesClient.removeRolePermission(_tenant, roleName, perm2);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("removeRolePermission: " + respInt);
        
        // See of a user has a matching permission (positive case).
        try {respBool = skFilesClient.isPermitted(_tenant, user1, perm2);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("isPermitted: " + respBool);
        Assert.assertFalse(respBool, "Expected " + user1 + " to not have permission " + perm2 + ".");
        
        // Get all the permissions in a role.
        try {respList = skSysClient.getRolePermissions(_tenant, roleName, false);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRolePermissions: " + respList);
        Assert.assertTrue(respList.contains(perm1), "Expected " + perm1 + " in list.");
        Assert.assertEquals(respList.size(), 1, "Expected list with exactly 1 role in it.");
        
        // Get the role.
        SkRole skRoleOriginal;
        try {skRoleOriginal = skFilesClient.getRoleByName(_tenant, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleByName: " + skRoleOriginal);
        
        // Change the role's description.
        final String newDesc = "Hey, this is the updated description";
        try {skFilesClient.updateRoleDescription(_tenant, roleName, newDesc);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        
        // Get the role.
        SkRole skRoleNewDesc;
        try {skRoleNewDesc = skSysClient.getRoleByName(_tenant, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleByName: " + skRoleNewDesc.getDescription());
        Assert.assertEquals(skRoleNewDesc.getDescription(), newDesc,
        		           "Incorrect new description.");  
        
        // Change the role's description.
        final String newRoleName = "new_" + roleName;
        try {skFilesClient.updateRoleName(_tenant, roleName, newRoleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        
        // Get the role.
        SkRole skRoleNewName;
        try {skRoleNewName = skSysClient.getRoleByName(_tenant, newRoleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleByName: " + skRoleNewName.getName());
        Assert.assertEquals(skRoleNewName.getName(), newRoleName,
        		           "Incorrect new role name.");  
        
        // Change the role owner from files to systems.
        final String newOwner = "systems";
        try {skFilesClient.updateRoleOwner(_tenant, newRoleName, newOwner);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        
        // Get the role.
        SkRole skRoleNewOwner;
        try {skRoleNewOwner = skSysClient.getRoleByName(_tenant, newRoleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleByName: " + skRoleNewOwner.getOwner());
        Assert.assertEquals(skRoleNewOwner.getOwner(), newOwner,
        		           "Incorrect new role owner.");  
        
        // Try to delete the role using the old owner.
        respInt = -1;
        try {respInt = skFilesClient.deleteRoleByName(_tenant, newRoleName);}
        catch (Exception e) {
        	boolean expected = e.getMessage().startsWith("SK_API_AUTHORIZATION_FAILED");
        	Assert.assertTrue(expected, "Expected authorization error due to improper owner.");
        	if (!expected) throw e;
        }
        Assert.assertEquals(respInt, -1, "No authorization exception thrown.");
        System.out.println("deleteRoleByName: failed as expected because of owner");
        
        // Delete the role.
        try {respInt = skSysClient.deleteRoleByName(_tenant, newRoleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("deleteRoleByName: " + respInt);
        
        // Check that the role is gone.
        try {respList = skFilesClient.getRoleNames(_tenant);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleNames: " + respList);
        Assert.assertFalse(respList.contains(newRoleName), 
                           "Expected " + newRoleName + " to be deleted.");
        Assert.assertFalse(respList.contains(roleName), 
                           "Expected " + roleName + " to be deleted.");
        
    }
}
