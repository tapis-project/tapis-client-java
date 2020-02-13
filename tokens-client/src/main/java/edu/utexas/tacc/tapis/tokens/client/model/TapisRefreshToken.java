package edu.utexas.tacc.tapis.tokens.client.model;

import org.apache.commons.lang3.StringUtils;

public final class TapisRefreshToken 
 extends TapisBaseToken
{
    // Fields.
    private String refreshToken; // serialized token
    
    // Accessors.
    public String getRefreshToken() {return refreshToken;}
    public void setRefreshToken(String accessToken) {this.refreshToken = accessToken;}

    // Make all fields are filled in.
    @Override
    public boolean validate() {
        if (StringUtils.isBlank(refreshToken)) return false;
        return super.validate();        
    }
}
