package edu.utexas.tacc.tapis.security.client;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/** This test uses an SK client to test the role and permission management.
 * When this test completes successfully, the SK database tables are restored
 * to their original state.
 * 
 * Required environment variables:  FILES_JWT
 * Optional environment variables:  TEST_TENANT
 * 
 * @author rich
 */
@Test(groups={"integration"})
public class ServiceJwtRoleTest 
{
    /* ********************************************************************** */
    /*                               Constants                                */
    /* ********************************************************************** */
	// Point to SK.
	private static final String DEFAULT_SKCLIENT_BASEURL = "http://localhost:8080/v3";
	private static final String DEFAULT_TENANT = "dev";
	
	// Environment variable names.
	private static final String ENV_FILES_JWT = "FILES_JWT";
	private static final String ENV_TENANT = "TEST_TENANT";
	
    /* ********************************************************************** */
    /*                                 Fields                                 */
    /* ********************************************************************** */
	// Files service JWT passed in as an environment variable.
	private String _filesJwt; 
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
		// Required environment value.
		_filesJwt = System.getenv(ENV_FILES_JWT);
		
		// Allow optional tenant override.
		_tenant = System.getenv(ENV_TENANT);
		if (StringUtils.isBlank(_tenant)) _tenant = DEFAULT_TENANT;
		
		// Check JWT.
		if (StringUtils.isBlank(_filesJwt)) {
			String msg = "Unable to run test due to missing JWT.\n\n"
					+ "This test requires that a service JWT be defined for the *files* service "
					+ "in the test tenant. "
					+ "The FILES_JWT environment variable must be assigned this JWT value.\n\n"
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
    	// Set up.
    	SKClient skClient = new SKClient(DEFAULT_SKCLIENT_BASEURL, _filesJwt);
    	skClient.addDefaultHeader("X-Tapis-User", "files");
    	skClient.addDefaultHeader("X-Tapis-Tenant", "master");
        final String roleName = "files_role1";
        final String user1 = "bud";
        final String user2 = "jane";
        final String perm1 = "systems:banana:*:xx";
        final String perm2 = "meta:horse:buggie";
        
        // Get role names.
        List<String> respList;
        try {respList = skClient.getRoleNames(_tenant);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleNames: " + respList);
        
        // Create a role.
        String resp1;
        try {resp1 = skClient.createRole(_tenant, roleName, "you little piggy.");}
            catch (Exception e) {
                System.out.println(e.toString());
                throw e;
            }
        System.out.println("createRole: " + resp1);
        
        // Get role names.
        try {respList = skClient.getRoleNames(_tenant);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleNames: " + respList);
        Assert.assertTrue(respList.contains(roleName), 
        		          "Expected " + roleName + " in list.");
        
        // Assign a user the role.
        int respInt;
        try {respInt = skClient.grantUserRole(_tenant, user1, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("grantUserRole: " + respInt);
        
        // Assign a user the role.
        try {respInt = skClient.grantUserRole(_tenant, user2, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("grantUserRole: " + respInt);
        
        // Get users names.
        try {respList = skClient.getUserNames(_tenant);}
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
        try {respList = skClient.getUsersWithRole(_tenant, roleName);}
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
        try {respList = skClient.getUserRoles(_tenant, user1);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getUserRoles: " + respList);
        Assert.assertTrue(respList.contains(roleName), 
                          "Expected " + roleName + " in list.");
        
        // Add a permission to a role.
        try {respInt = skClient.addRolePermission(_tenant, roleName, perm1);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("addRolePermission: " + respInt);
        
        // Add a permission to a role.
        try {respInt = skClient.addRolePermission(_tenant, roleName, perm2);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("addRolePermission: " + respInt);
        
        // Get all the permissions in a role.
        try {respList = skClient.getRolePermissions(_tenant, roleName, false);}
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
        try {respList = skClient.getUserPerms(_tenant, user1);}
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
        try {respBool = skClient.hasRole(_tenant, user1, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("hasRole: " + respBool);
        Assert.assertTrue(respBool, "Expected " + user1 + " to have role " + roleName + ".");
        
        // See if a user has a role (negative case).
        try {respBool = skClient.hasRole(_tenant, user1, "no_role");}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("hasRole: " + respBool);
        Assert.assertFalse(respBool, "Expected " + user1 + " to not have role no_role.");
        
        // See of a user has a matching permission (negative case).
        try {respBool = skClient.isPermitted(_tenant, user1, perm2);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("isPermitted: " + respBool);
        Assert.assertTrue(respBool, "Expected " + user1 + " to have permission " + perm2 + ".");
        
        // See of a user has a matching permission (positive case).
        try {respBool = skClient.isPermitted(_tenant, user1, "meta:horse:X");}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("isPermitted: " + respBool);
        Assert.assertFalse(respBool, "Expected " + user1 + " to not have permission meta:horse:X.");
        
        // See of a user has a matching permission (positive case).
        try {respBool = skClient.isPermittedAny(_tenant, user1, 
        		          new String[] {"not:a:perm", perm2});}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("isPermittedAny: " + respBool);
        Assert.assertTrue(respBool, "Expected " + user1 + " to have permission " + perm2 + ".");
        
        // See of a user has a matching permission (negative case).
        try {respBool = skClient.isPermittedAll(_tenant, user1, 
        		          new String[] {"not:a:perm", perm2});}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("isPermittedAll: " + respBool);
        Assert.assertFalse(respBool, "Expected " + user1 + " to not have permission not:a:perm.");
        
        // Remove a permission from a role.
        try {respInt = skClient.removeRolePermission(_tenant, roleName, perm2);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("removeRolePermission: " + respInt);
        
        // See of a user has a matching permission (positive case).
        try {respBool = skClient.isPermitted(_tenant, user1, perm2);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("isPermitted: " + respBool);
        Assert.assertFalse(respBool, "Expected " + user1 + " to not have permission " + perm2 + ".");
        
        // Get all the permissions in a role.
        try {respList = skClient.getRolePermissions(_tenant, roleName, false);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRolePermissions: " + respList);
        Assert.assertTrue(respList.contains(perm1), "Expected " + perm1 + " in list.");
        Assert.assertEquals(respList.size(), 1, "Expected list with exactly 1 role in it.");
        
        // Delete a role.
        Integer resp5;
        try {resp5 = skClient.deleteRoleByName(_tenant, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("deleteRoleByName: " + resp5);
        
        // Check that the role is gone.
        try {respList = skClient.getRoleNames(_tenant);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleNames: " + respList);
        Assert.assertFalse(respList.contains(roleName), 
                           "Expected " + roleName + " to be deleted.");
        
    }
}
