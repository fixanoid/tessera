package com.quorum.tessera.ssl.strategy;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TrustModeTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private Path tmpFile;

    private Path tmpKnownHosts;

    @Before
    public void setUp() {
        tmpFile = Paths.get(tmpDir.getRoot().getPath(), "tmpFile");
        tmpKnownHosts = Paths.get(tmpDir.getRoot().getPath(),"knownHosts");
    }


    @Test
    public void testNone() throws IOException, GeneralSecurityException, OperatorCreationException {

        assertThat(
            TrustMode.NONE.createSSLContext(
                tmpFile,
                "quorum",
                tmpFile,
                "quorum",
                tmpKnownHosts
            )
        ).isNotNull();

    }

    @Test
    public void testWhiteList() throws IOException, GeneralSecurityException, OperatorCreationException {
        assertThat(
            TrustMode.getValueIfPresent("WHITELIST").get().createSSLContext(
                tmpFile,
                "quorum",
                tmpFile,
                "quorum",
                tmpKnownHosts
            )
        ).isNotNull();
    }

    @Test
    public void testCA() throws IOException, GeneralSecurityException, OperatorCreationException {
        assertThat(
            TrustMode.getValueIfPresent("CA").get().createSSLContext(
                tmpFile,
                "quorum",
                tmpFile,
                "quorum",
                tmpKnownHosts
            )
        ).isNotNull();
    }

    @Test
    public void testTOFU() throws IOException, GeneralSecurityException, OperatorCreationException {
        assertThat(
            TrustMode.getValueIfPresent("TOFU").get().createSSLContext(
                tmpFile,
                "quorum",
                tmpFile,
                "quorum",
                tmpKnownHosts
            )
        ).isNotNull();
    }

    @Test
    public void testInvalidMode() {
        assertThat(TrustMode.getValueIfPresent("SOMETHING").isPresent()).isFalse();
    }
}
