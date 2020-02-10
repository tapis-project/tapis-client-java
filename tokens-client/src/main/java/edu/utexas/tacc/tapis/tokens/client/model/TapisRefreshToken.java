package edu.utexas.tacc.tapis.tokens.client.model;

public final class TapisRefreshToken 
 extends TapisBaseToken
{
    // Fields.
    private String refreshToken; // serialized token
    
    // Accessors.
    public String getRefreshToken() {return refreshToken;}
    public void setRefreshToken(String accessToken) {this.refreshToken = accessToken;}
}
