package edu.utexas.tacc.tapis.security.client.model;

import java.util.List;
import edu.utexas.tacc.tapis.client.shared.exceptions.TException;

public class SKSecretDeleteParms
 extends SKSecretBaseParms<SKSecretDeleteParms>
{
    // Fields.
    private List<Integer> versions;
    
    // Constructor.
    public SKSecretDeleteParms(SecretType secretType) 
    throws TException 
    {
        super(secretType);
    }

    // Accessors.
    public List<Integer> getVersions() {return versions;}

    public SKSecretDeleteParms setVersions(List<Integer> versions) 
        {this.versions = versions; return this;}
}
