package edu.utexas.tacc.tapis.security.client.model;

import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;

public class SKSecretMetaParms
 extends SKSecretBaseParms<SKSecretMetaParms>
{
    // Constructor.
    public SKSecretMetaParms(SecretType secretType) 
    throws TapisClientException 
    {
        super(secretType);
    }
}
