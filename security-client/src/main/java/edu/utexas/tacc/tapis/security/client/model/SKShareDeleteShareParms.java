package edu.utexas.tacc.tapis.security.client.model;

public class SKShareDeleteShareParms 
{
    private String  grantee;
    private String  grantor;
    private String  tenant;
    private String  resourceType;    
    private String  resourceId1;     
    private String  resourceId2;     
    private String  privilege;
    
    // Accessors.
    public String getGrantor() {
      return grantor;
    }
    public void setGrantor(String grantor) {
    this.grantor = grantor;
  }
    public String getGrantee() {
    return grantee;
  }
    public void setGrantee(String grantee) {
    this.grantee = grantee;
  }
    public String getTenant() {
        return tenant;
    }
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    public String getResourceType() {
        return resourceType;
    }
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    public String getResourceId1() {
        return resourceId1;
    }
    public void setResourceId1(String resourceId1) {
        this.resourceId1 = resourceId1;
    }
    public String getResourceId2() {
        return resourceId2;
    }
    public void setResourceId2(String resourceId2) {
        this.resourceId2 = resourceId2;
    }
    public String getPrivilege() {
        return privilege;
    }
    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }
}
