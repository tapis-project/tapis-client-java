package edu.utexas.tacc.tapis.files.client;


import edu.utexas.tacc.tapis.auth.client.AuthClient;
import edu.utexas.tacc.tapis.auth.client.gen.model.TokenResponse;
import edu.utexas.tacc.tapis.files.client.gen.model.FileInfo;
import edu.utexas.tacc.tapis.files.client.gen.model.FilePermission;
import edu.utexas.tacc.tapis.files.client.gen.model.MkdirRequest;
import edu.utexas.tacc.tapis.files.client.gen.model.TransferTask;
import edu.utexas.tacc.tapis.files.client.gen.model.TransferTaskListResponse;
import edu.utexas.tacc.tapis.files.client.gen.model.TransferTaskRequest;
import edu.utexas.tacc.tapis.files.client.gen.model.TransferTaskRequestElement;
import edu.utexas.tacc.tapis.tokens.client.TokensClient;
import edu.utexas.tacc.tapis.tokens.client.model.CreateTokenParms;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Test(groups = {"e2e"})
public class ITestFilesClient {

    private String basepath = "https://dev.develop.tapis.io";
    private final String password = System.getenv("TESTUSER2_PASSWORD");
    private final String systemId = "tapis-demo";
    private final String username = "testuser2";

    public void testGetTransfer() throws Exception {
        AuthClient authClient = new AuthClient(basepath);
        String jwt = authClient.getToken(username, password);
        FilesClient client = new FilesClient(basepath, jwt);

        client.mkdir(systemId, "a");

        TransferTaskRequest req = new TransferTaskRequest();
        TransferTaskRequestElement element = new TransferTaskRequestElement();
        element.setDestinationURI("tapis://tapis-demo/b/");
        element.setSourceURI("tapis://tapis-demo/a/");
        req.addElementsItem(element);
        req.setTag("e2e-test");
        TransferTask newTask = client.createTransferTask(req);

        //Now make sure that we can get the task back;
        TransferTask t = client.getTransferTask(newTask.getUuid().toString());
        Assert.assertNotNull(t);
        Assert.assertEquals(t.getUuid(), newTask.getUuid());

    }

    public void testListFiles() throws Exception {

        AuthClient authClient = new AuthClient(basepath);
        String jwt = authClient.getToken(username, password);
        FilesClient client = new FilesClient(basepath, jwt);

        client.mkdir(systemId, "a");
        List<FileInfo> listing = client.listFiles(systemId, "/", 100, 0, false);
        Assert.assertNotNull(listing);
        Assert.assertTrue(listing.size() > 0);
    }

    public void testMkdirAndDeleteDir() throws Exception {
        AuthClient authClient = new AuthClient(basepath);
        String jwt = authClient.getToken(username, password);
        FilesClient client = new FilesClient(basepath, jwt);

        client.mkdir(systemId, "test-directory-e2e");
        List<FileInfo> listing = client.listFiles(systemId, "/test-directory-e2e", 100, 0, false);
        Assert.assertNotNull(listing);
        Assert.assertTrue(listing.size() > 0);

        client.delete(systemId, "test-directory-e2e");
        Assert.assertThrows(Exception.class, ()-> client.listFiles(systemId, "/test-directory-e2e", 100, 0, false));


    }

    public void testInsert() throws Exception {
        AuthClient authClient = new AuthClient(basepath);
        String jwt = authClient.getToken(username, password);
        FilesClient client = new FilesClient(basepath, jwt);
        final File initialFile = new File("src/test/resources/e2etestfile.txt");
        client.insert(systemId, "test-directory-e2e/e2e-test-file.txt", FileUtils.openInputStream(initialFile));
        List<FileInfo> listing = client.listFiles(systemId, "/test-directory-e2e", 100, 0, false);
        Assert.assertNotNull(listing);
        Assert.assertEquals(listing.size(), 1);
        Assert.assertEquals(listing.get(0).getPath(), "test-directory-e2e/e2e-test-file.txt");
        Assert.assertTrue(listing.get(0).getSize() > 0);
        client.delete(systemId, "test-directory-e2e");
        Assert.assertThrows(Exception.class, ()-> client.listFiles(systemId, "/test-directory-e2e", 100, 0, false));
    }

    public void testGetPerms() throws Exception {
        AuthClient authClient = new AuthClient(basepath);
        String jwt = authClient.getToken(username, password);
        FilesClient client = new FilesClient(basepath, jwt);
        final File initialFile = new File("src/test/resources/e2etestfile.txt");
        client.insert(systemId, "test-directory-e2e/e2e-test-file.txt", FileUtils.openInputStream(initialFile));
        FilePermission perms = client.getFilePermissions(systemId, "/test-directory-e2e", null);
        Assert.assertNotNull(perms);
    }

    public void testZipper() throws Exception {
        AuthClient authClient = new AuthClient(basepath);
        String jwt = authClient.getToken(username, password);
        FilesClient client = new FilesClient(basepath, jwt);
        final File initialFile = new File("src/test/resources/e2etestfile.txt");
        client.insert(systemId, "test-directory-e2e/e2e-test-file.txt", FileUtils.openInputStream(initialFile));
        client.insert(systemId, "test-directory-e2e/e2e-test-file2.txt", FileUtils.openInputStream(initialFile));
        client.insert(systemId, "test-directory-e2e/e2e-test-file3.txt", FileUtils.openInputStream(initialFile));
        client.insert(systemId, "test-directory-e2e/dir1/e2e-test-file3.txt", FileUtils.openInputStream(initialFile));

        FilesClient.StreamedFile zippedFile = client.getZip(systemId, "/test-directory-e2e/");
        Assert.assertNotNull(zippedFile);
        ZipInputStream zis = new ZipInputStream(zippedFile.getInputStream());
        ZipEntry ze;
        int count=0;
        while ((ze = zis.getNextEntry()) != null) {
            System.out.println(ze.toString());
            count++;
        }
        Assert.assertEquals(zippedFile.getName(), "test-directory-e2e.zip");
        Assert.assertEquals(count, 4);
    }

    public void testGetContents() throws Exception {
        AuthClient authClient = new AuthClient(basepath);
        String jwt = authClient.getToken(username, password);
        FilesClient client = new FilesClient(basepath, jwt);
        final File initialFile = new File("src/test/resources/e2etestfile.txt");
        client.insert(systemId, "test-directory-e2e/e2e-test-file.txt", FileUtils.openInputStream(initialFile));

        FilesClient.StreamedFile streamedFile = client.getFileContents(systemId, "/test-directory-e2e/e2e-test-file.txt", false);
        Assert.assertNotNull(streamedFile);
        Assert.assertEquals(streamedFile.getName(), "e2e-test-file.txt");
    }



}
