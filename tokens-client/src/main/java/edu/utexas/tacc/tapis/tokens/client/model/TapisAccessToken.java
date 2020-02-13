package edu.utexas.tacc.tapis.tokens.client.model;

import org.apache.commons.lang3.StringUtils;

public final class TapisAccessToken 
 extends TapisBaseToken
{
    // Fields.
    private String accessToken; // serialized token
    
    // Accessors.
    public String getAccessToken() {return accessToken;}
    public void setAccessToken(String accessToken) {this.accessToken = accessToken;}

    // Make all fields are filled in.
    @Override
    public boolean isValid() {
        if (StringUtils.isBlank(accessToken)) return false;
        return super.isValid();        
    }
}
