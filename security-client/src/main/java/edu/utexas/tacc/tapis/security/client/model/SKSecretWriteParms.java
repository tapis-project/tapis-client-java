package edu.utexas.tacc.tapis.security.client.model;

import java.util.Map;

import edu.utexas.tacc.tapis.client.shared.exceptions.TException;
import edu.utexas.tacc.tapis.security.client.gen.model.Options;

public class SKSecretWriteParms
 extends SKSecretBaseParms<SKSecretWriteParms>
{
    // Fields.
    private Map<String, String> data;
    private Options options;
    
    // Constructor.
    public SKSecretWriteParms(SecretType secretType) 
    throws TException
    {
        super(secretType);
    }

    // Accessors
    public Map<String, String> getData() {return data;}
    public SKSecretWriteParms setData(Map<String, String> data) 
        {this.data = data; return this;}

    public Options getOptions() {return options;}
    public SKSecretWriteParms setOptions(Options options) 
        {this.options = options; return this;}
}
