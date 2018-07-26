package com.quorum.tessera.ssl.context;

import com.quorum.tessera.ssl.trust.TrustAllManager;
import com.quorum.tessera.ssl.trust.TrustOnFirstUseManager;
import com.quorum.tessera.ssl.trust.WhiteListTrustManager;
import com.quorum.tessera.ssl.util.TlsUtils;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;

public class SSLContextBuilder {

    private static final String PROTOCOL = "TLS";

    private Path keyStore;
    private String keyStorePassword;
    private Path key;
    private Path certificate;
    private Path trustStore;
    private String trustStorePassword;
    private List<Path> trustedCertificates;

    private SSLContext sslContext;

    private SSLContextBuilder(Path keyStore,
                              String keyStorePassword,
                              Path key,
                              Path certificate,
                              Path trustStore,
                              String trustStorePassword,
                              List<Path> trustedCertificates) throws NoSuchAlgorithmException {
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.key = key;
        this.certificate = certificate;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        this.trustedCertificates = trustedCertificates;

        this.sslContext = SSLContext.getInstance(PROTOCOL);
    }


    public static SSLContextBuilder createBuilder(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword) throws NoSuchAlgorithmException {
        return new SSLContextBuilder(
            keyStore,
            keyStorePassword,
            null,
            null,
            trustStore,
            trustStorePassword,
            null);
    }

    public static SSLContextBuilder createBuilder(Path key, Path certificate, List<Path> trustedCertificates) throws NoSuchAlgorithmException {
        return new SSLContextBuilder(
            null,
            null,
            key,
            certificate,
            null,
            null,
            trustedCertificates);
    }

    public SSLContext build() {
        return sslContext;
    }


    public SSLContextBuilder forWhiteList(Path knownHosts) throws GeneralSecurityException, IOException, OperatorCreationException {

        sslContext.init(buildKeyManagers(), new TrustManager[]{new WhiteListTrustManager(knownHosts)}, null);

        return this;
    }


    public SSLContextBuilder forCASignedCertificates() throws GeneralSecurityException, IOException, OperatorCreationException {

        final KeyManager[] keyManagers = buildKeyManagers();

        final TrustManager[] trustManagers = buildTrustManagers();

        sslContext.init(keyManagers, trustManagers, new SecureRandom());

        return this;
    }


    public SSLContextBuilder forAllCertificates() throws GeneralSecurityException, IOException, OperatorCreationException {

        sslContext.init(buildKeyManagers(), new TrustManager[]{new TrustAllManager()}, null);

        return this;
    }


    public SSLContextBuilder forTrustOnFirstUse(Path knownHostsFile) throws GeneralSecurityException, IOException, OperatorCreationException {

        final KeyManager[] keyManagers = buildKeyManagers();

        sslContext.init(keyManagers,
            new TrustManager[]{new TrustOnFirstUseManager(knownHostsFile)}, null);

        return this;
    }


    private KeyManager[] buildKeyManagers() throws GeneralSecurityException, IOException, OperatorCreationException {

        if (Files.notExists(this.keyStore)) {
            TlsUtils.create().generateKeyStoreWithSelfSignedCertificate(this.keyStore, this.keyStorePassword);
        }

        if (Objects.nonNull(this.keyStore)) {
            return SSLKeyStoreLoader.fromJksKeyStore(this.keyStore, this.keyStorePassword);
        } else {
            return SSLKeyStoreLoader.fromPemKeyFile(this.key, this.certificate);
        }
    }


    private TrustManager[] buildTrustManagers() throws GeneralSecurityException, IOException {

        if (Objects.nonNull(this.trustStore)) {
            return SSLKeyStoreLoader.fromJksTrustStore(this.trustStore, this.trustStorePassword);
        } else {
            return SSLKeyStoreLoader.fromPemCertificatesFile(this.trustedCertificates);
        }
    }

}
