package edu.utexas.tacc.tapis.security.client.model;

public class SKShareHasPrivilegeParms 
{
    private String  grantee;
    private String  tenant;
    private String  resourceType;
    private String  resourceId1;     
    private String  resourceId2;     
    private String  privilege;
    private boolean excludePublic = true;
    private boolean excludePublicNoAuthn = true;
    
    // Accessors.
    public String getGrantee() { return grantee; }
    public void setGrantee(String grantee) {
        this.grantee = grantee;
    }
    public String getTenant() { return tenant; }
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
    public boolean isExcludePublic() {
        return excludePublic;
    }
    public void setExcludePublic(boolean excludePublic) {
        this.excludePublic = excludePublic;
    }
    public boolean isExcludePublicNoAuthn() {
        return excludePublicNoAuthn;
    }
    public void setExcludePublicNoAuthn(boolean excludePublicNoAuthn) {
        this.excludePublicNoAuthn = excludePublicNoAuthn;
    }
}
