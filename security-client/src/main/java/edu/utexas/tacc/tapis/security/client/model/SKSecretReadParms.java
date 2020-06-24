package edu.utexas.tacc.tapis.security.client.model;

import edu.utexas.tacc.tapis.client.shared.exceptions.TException;

public class SKSecretReadParms
 extends SKSecretBaseParms<SKSecretReadParms>
{
    // Fields.
    private Integer version;
    
    // Constructor.
    public SKSecretReadParms(SecretType secretType) 
    throws TException 
    {
        super(secretType);
    }

    // Accessors
    public Integer getVersion() {return version;}
    public SKSecretReadParms setVersion(Integer version) 
        {this.version = version; return this;}
}
