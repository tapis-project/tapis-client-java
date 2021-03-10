package edu.utexas.tacc.tapis.files.client;


import edu.utexas.tacc.tapis.files.client.gen.model.TransferTask;
import edu.utexas.tacc.tapis.files.client.gen.model.TransferTaskRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"integration"})
public class ITestTransferTasks {

    private String userJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIyYmRiNThiNi00MGQ3LTQwNmMtYTc5Ni04MmExZTIxNjlhM2EiLCJpc3MiOiJodHRwczovL2Rldi5kZXZlbG9wLnRhcGlzLmlvL3YzL3Rva2VucyIsInN1YiI6InRlc3R1c2VyMkBkZXYiLCJ0YXBpcy90ZW5hbnRfaWQiOiJkZXYiLCJ0YXBpcy90b2tlbl90eXBlIjoiYWNjZXNzIiwidGFwaXMvZGVsZWdhdGlvbiI6ZmFsc2UsInRhcGlzL2RlbGVnYXRpb25fc3ViIjpudWxsLCJ0YXBpcy91c2VybmFtZSI6InRlc3R1c2VyMiIsInRhcGlzL2FjY291bnRfdHlwZSI6InVzZXIiLCJleHAiOjE2MTU0MjQwNDcsInRhcGlzL2NsaWVudF9pZCI6bnVsbCwidGFwaXMvZ3JhbnRfdHlwZSI6InBhc3N3b3JkIn0.kiy9CQGFL_2VM8URhJMrVY4SVDQe-c51PiMyp3bcalSbzC1zfY9X0B3MDKksyMmo8y9gObwv-l8OSgAqcR_hdjBoQvuJtMs5TQJIMl4sAnYR8dNrlD4bc17IixQV34bIS2ZzAKBZN2T78WLURP_1aMdd29uXRi599ZEOmVki4OOUdBLyInZ1DwYd5FTpqalFkIWlmUgGFFMUwKECgS2BMlGPH1jtXML1fhhfFJYtc5d1a5QRYgDjX47KeqJDWsoaD8WuDSTj4le9cdZ_vkl7jinU3pXhXdMDaLXkORFWAFxKRmIOSXHl9Fa5xtzvMkRcmTpEHpJUUUh9fr24OpYTbQ";
    public void testGetTransfer() throws Exception {
        FilesClient client = new FilesClient("https://dev.develop.tapis.io", userJwt);
        TransferTaskRequest req = new TransferTaskRequest();
        TransferTask t = client.getTransferTask("d85e660d-24ef-4806-bb18-5e56557dd0c1");
        Assert.assertNotNull(t);

    }

}
