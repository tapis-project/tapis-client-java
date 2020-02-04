package edu.utexas.tacc.tapis.security.client.model;

import edu.utexas.tacc.tapis.shared.exceptions.TapisException;

public class SKSecretMetaParms
 extends SKSecretBaseParms<SKSecretMetaParms>
{
    // Constructor.
    public SKSecretMetaParms(SecretType secretType) 
    throws TapisException 
    {
        super(secretType);
    }
}
