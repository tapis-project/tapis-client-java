package edu.utexas.tacc.tapis.tokens.client.model;

public final class TapisAccessToken 
 extends TapisBaseToken
{
    // Fields.
    private String accessToken; // serialized token
    
    // Accessors.
    public String getAccessToken() {return accessToken;}
    public void setAccessToken(String accessToken) {this.accessToken = accessToken;}
}
