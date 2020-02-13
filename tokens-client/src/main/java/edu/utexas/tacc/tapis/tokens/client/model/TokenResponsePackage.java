package edu.utexas.tacc.tapis.tokens.client.model;

public final class TokenResponsePackage 
{
    // Fields.
    private TapisAccessToken  accessToken;
    private TapisRefreshToken refreshToken;
    
    // Accessors.
    public TapisAccessToken getAccessToken() {return accessToken;}
    public void setAccessToken(TapisAccessToken accessToken) 
        {this.accessToken = accessToken;}
    public TapisRefreshToken getRefreshToken() {return refreshToken;}
    public void setRefreshToken(TapisRefreshToken refresToken) 
        {this.refreshToken = refresToken;}
    
    // Make sure all fields are filled in.
    public boolean validateAccessToken() {
        if (accessToken == null) return false;
        return accessToken.validate();
    }
    public boolean validateRefreshToken() {
        if (refreshToken == null) return false;
        return refreshToken.validate();
    }
}
