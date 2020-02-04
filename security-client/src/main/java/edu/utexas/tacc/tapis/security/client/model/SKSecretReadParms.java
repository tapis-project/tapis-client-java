package edu.utexas.tacc.tapis.security.client.model;

import edu.utexas.tacc.tapis.shared.exceptions.TapisException;

public class SKSecretReadParms
 extends SKSecretBaseParms<SKSecretReadParms>
{
    // Fields.
    private Integer version;
    
    // Constructor.
    public SKSecretReadParms(SecretType secretType) 
    throws TapisException 
    {
        super(secretType);
    }

    // Accessors
    public Integer getVersion() {return version;}
    public SKSecretReadParms setVersion(Integer version) 
        {this.version = version; return this;}
}
