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
 * for authorization reasons.  
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
public class UserJwtAuthTest 
{
    /* ********************************************************************** */
    /*                               Constants                                */
    /* ********************************************************************** */
	// Point to SK.
	private static final String DEFAULT_SKCLIENT_BASEURL = "http://localhost:8080/v3";
	private static final String DEFAULT_TENANT = "dev";
	
	// Environment variable names.
	private static final String ENV_FILES_JWT = "FILES_JWT";
	private static final String ENV_ADMIN_JWT = "ADMIN_JWT";
	private static final String ENV_TESTUSER1958_JWT = "TESTUSER1958_JWT";
	private static final String ENV_TENANT = "TEST_TENANT";
	
    /* ********************************************************************** */
    /*                                 Fields                                 */
    /* ********************************************************************** */
	// Files service JWT passed in as an environment variable.
	private String _filesJwt; 
	private String _adminJwt;
	private String _testuser1958Jwt;
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
		_adminJwt = System.getenv(ENV_ADMIN_JWT);
		_testuser1958Jwt = System.getenv(ENV_TESTUSER1958_JWT);
		
		// Allow optional tenant override.
		_tenant = System.getenv(ENV_TENANT);
		if (StringUtils.isBlank(_tenant)) _tenant = DEFAULT_TENANT;
		
		// Check JWT.
		if (StringUtils.isBlank(_filesJwt)  || 
		    StringUtils.isBlank(_adminJwt)  ||
		    StringUtils.isBlank(_testuser1958Jwt))
		{
			String msg = "Unable to run test due to missing JWT.\n\n"
					+ "This test requires that a service JWT be defined for "
					+ "the *files* service and that user JWTs be defined for "
					+ "the *admin* and *testuser1958* users, all in the test tenant. "
					+ "These JWTs are assigned by reading the FILES_JWT, ADMIN_JWT "
					+ "and TESTUSER1958_JWT environment variables.\n\n"
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
    	// Set up the 3 clients.  Only service JWTs assign the X-Tapis headers.
    	SKClient skFilesClient = new SKClient(DEFAULT_SKCLIENT_BASEURL, _filesJwt);
    	skFilesClient.addDefaultHeader("X-Tapis-User", "files");
    	skFilesClient.addDefaultHeader("X-Tapis-Tenant", "master");
    	SKClient skAdminClient = new SKClient(DEFAULT_SKCLIENT_BASEURL, _adminJwt);
    	SKClient sk1958Client = new SKClient(DEFAULT_SKCLIENT_BASEURL, _testuser1958Jwt);
    	
    	// Assign test values.
        final String roleName = "admin_role3";
        final String user1 = "bud";
        final String user2 = "jane";
        final String perm1 = "systems:mars:*:zz";
        final String perm2 = "meta:cat:pillow";
        
        // Get role names.
        List<String> respList;
        try {respList = sk1958Client.getRoleNames(_tenant);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("getRoleNames: " + respList);
        
        // Try to create a role using as a non-admin user.
        String resp1 = null;
        try {resp1 = sk1958Client.createRole(_tenant, roleName, "you little piggy.");}
            catch (Exception e) {
            	boolean expected = e.getMessage().startsWith("SK_API_AUTHORIZATION_FAILED");
            	Assert.assertTrue(expected, "Expected authorization error due to improper owner.");
            	if (!expected) throw e;
            }
        Assert.assertNull(resp1, "Expected authorization error due to improper owner.");
        System.out.println("createRole: No role created by testuser1958");
        
        // Create a role.
        try {resp1 = skAdminClient.createRole(_tenant, roleName, "you little piggy.");}
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
        
        // Try to assign a user the role as a non-owner service.
        int respInt = -1;
        try {respInt = skFilesClient.grantUserRole(_tenant, user1, roleName);}
        catch (Exception e) {
        	boolean expected = e.getMessage().startsWith("SK_API_AUTHORIZATION_FAILED");
        	Assert.assertTrue(expected, "Expected authorization error due to improper owner.");
        	if (!expected) throw e;
        }
        Assert.assertEquals(respInt, -1, "No authorization exception thrown.");
        System.out.println("grantUserRole:  failed as expected because of owner");
      
        // Try to add a permission to a role as non-owner service.
        respInt = -1;
        try {respInt = skFilesClient.addRolePermission(_tenant, roleName, perm1);}
        catch (Exception e) {
        	boolean expected = e.getMessage().startsWith("SK_API_AUTHORIZATION_FAILED");
        	Assert.assertTrue(expected, "Expected authorization error due to improper owner.");
        	if (!expected) throw e;
        }
        Assert.assertEquals(respInt, -1, "No authorization exception thrown.");
        System.out.println("addRolePermission: failed as expected because of owner");
      
        // Try to assign a user the role as a non-owner, regular user.
        respInt = -1;
        try {respInt = sk1958Client.grantUserRole(_tenant, user1, roleName);}
        catch (Exception e) {
        	boolean expected = e.getMessage().startsWith("SK_API_AUTHORIZATION_FAILED");
        	Assert.assertTrue(expected, "Expected authorization error due to improper owner.");
        	if (!expected) throw e;
        }
        Assert.assertEquals(respInt, -1, "No authorization exception thrown.");
        System.out.println("grantUserRole:  failed as expected because of owner");
      
        // Try to add a permission to a role as non-owner, regular user.
        respInt = -1;
        try {respInt = sk1958Client.addRolePermission(_tenant, roleName, perm1);}
        catch (Exception e) {
        	boolean expected = e.getMessage().startsWith("SK_API_AUTHORIZATION_FAILED");
        	Assert.assertTrue(expected, "Expected authorization error due to improper owner.");
        	if (!expected) throw e;
        }
        Assert.assertEquals(respInt, -1, "No authorization exception thrown.");
        System.out.println("addRolePermission: failed as expected because of owner");
      
        // Change the role owner from files to testuser1958.
        final String newOwner = "testuser1958";
        try {skAdminClient.updateRoleOwner(_tenant, roleName, newOwner);}
        catch (Exception e) {
        	System.out.println(e.toString());
        	throw e;
        }
      
        // Get the role.
        SkRole skRoleNewOwner;
        try {skRoleNewOwner = sk1958Client.getRoleByName(_tenant, roleName);}
        catch (Exception e) {
        	System.out.println(e.toString());
        	throw e;
        }
        System.out.println("getRoleByName: " + skRoleNewOwner.getOwner());
        Assert.assertEquals(skRoleNewOwner.getOwner(), newOwner,
      		                "Incorrect new role owner.");  
      
        // Try to assign a user.
        try {respInt = sk1958Client.grantUserRole(_tenant, user1, roleName);}
        catch (Exception e) {
        	System.out.println(e.toString());
        	throw e;
        }
        System.out.println("grantUserRole: " + respInt);
      
        // Try to add a permission to a role.
        try {respInt = sk1958Client.addRolePermission(_tenant, roleName, perm1);}
        catch (Exception e) {
        	System.out.println(e.toString());
        	throw e;
        }
        System.out.println("addRolePermission: " + respInt);
      
        // Try to assign a user.
        try {respInt = skAdminClient.grantUserRole(_tenant, user2, roleName);}
        catch (Exception e) {
        	System.out.println(e.toString());
        	throw e;
        }
        System.out.println("grantUserRole: " + respInt);
      
        // Try to add a permission to a role.
        try {respInt = skAdminClient.addRolePermission(_tenant, roleName, perm2);}
        catch (Exception e) {
        	System.out.println(e.toString());
        	throw e;
        }
        System.out.println("addRolePermission: " + respInt);
      
        // Delete the role.
        try {respInt = skAdminClient.deleteRoleByName(_tenant, roleName);}
        catch (Exception e) {
            System.out.println(e.toString());
            throw e;
        }
        System.out.println("deleteRoleByName: " + respInt);
        Assert.assertEquals(respInt, 1, "Expected to delete role " + roleName + ".");
        
    }
}
