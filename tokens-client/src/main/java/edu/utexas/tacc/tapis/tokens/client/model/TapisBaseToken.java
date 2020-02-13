package edu.utexas.tacc.tapis.tokens.client.model;

import java.time.Instant;

public abstract class TapisBaseToken 
{
    // Fields.
    private Instant  expiresAt;   // UTC expiration time
    private Integer  expiresIn;   // approximate seconds to expiration
    
    // Accessors.
    public Instant getExpiresAt() {return expiresAt;}
    public void setExpiresAt(Instant utc) {this.expiresAt = utc;}
    public void setExpiresAt(String utc) {
        // Some protection.
        if (utc == null) {expiresAt = null; return;}
        
        // The token generator returns a string like 2020-02-09 00:59:07.697305,
        // which is close but not exactly RFC 8601 compliant. The fix here is
        // harmless if a Zulu time string is actually passed in, but it allows 
        // strings like the above to be parsed.
        // 
        utc = utc.replace(' ', 'T');
        utc = utc.endsWith("Z") ? utc : utc + "Z";
        this.expiresAt = Instant.parse(utc);
    }

    public Integer getExpiresIn() {return expiresIn; }
    public void setExpiresIn(Integer expiresIn) {this.expiresIn = expiresIn;}

    // Make sure both fields are filled in.
    public boolean validate() {
        if (expiresAt == null || expiresIn == null) return false;
        return true;
    }
}
