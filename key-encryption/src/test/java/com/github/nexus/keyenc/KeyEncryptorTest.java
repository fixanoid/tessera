package com.github.nexus.keyenc;

import com.github.nexus.argon2.Argon2;
import com.github.nexus.argon2.ArgonOptions;
import com.github.nexus.argon2.ArgonResult;
import com.github.nexus.config.PrivateKey;
import com.github.nexus.config.PrivateKeyData;
import com.github.nexus.config.PrivateKeyType;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.NaclFacade;
import com.github.nexus.nacl.Nonce;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class KeyEncryptorTest {

    private Argon2 argon2;

    private NaclFacade nacl;

    private KeyEncryptor keyEncryptor;

    @Before
    public void init() {

        this.argon2 = mock(Argon2.class);
        this.nacl = mock(NaclFacade.class);

        this.keyEncryptor = new KeyEncryptorImpl(argon2, nacl);

    }

    @Test
    public void encryptingKeyReturnsCorrectJson() {

        final Key key = new Key(new byte[]{1, 2, 3, 4, 5});
        final String password = "pass";
        final ArgonResult result = new ArgonResult(new ArgonOptions("i", 1, 1, 1), new byte[]{}, new byte[]{});

        Mockito.doReturn(result).when(argon2).hash(eq(password), any(byte[].class));
        Mockito.doReturn(new Nonce(new byte[]{})).when(nacl).randomNonce();
        doReturn(new byte[]{}).when(nacl).sealAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

        final PrivateKey privateKey = keyEncryptor.encryptPrivateKey(key, password);

        final com.github.nexus.config.ArgonOptions aopts = privateKey.getArgonOptions();

        Assertions.assertThat(privateKey.getSbox()).isNotNull();
        Assertions.assertThat(privateKey.getAsalt()).isNotNull();
        Assertions.assertThat(privateKey.getSnonce()).isNotNull();

        Assertions.assertThat(aopts).isNotNull();
        Assertions.assertThat(aopts.getMemory()).isNotNull();
        Assertions.assertThat(aopts.getParallelism()).isNotNull();
        Assertions.assertThat(aopts.getIterations()).isNotNull();
        Assertions.assertThat(aopts.getAlgorithm()).isNotNull();

        verify(argon2).hash(eq(password), any(byte[].class));
        verify(nacl).randomNonce();
        verify(nacl).sealAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

    }

    @Test
    public void nullKeyGivesError() {

        final Throwable throwable = catchThrowable(() -> keyEncryptor.encryptPrivateKey(null, ""));

        assertThat(throwable).isInstanceOf(NullPointerException.class);

    }

    @Test
    public void correntJsonGivesDecryptedKey() {

        final String password = "pass";

        com.github.nexus.config.ArgonOptions argonOptions = new com.github.nexus.config.ArgonOptions("i", 1, 1, 1);
        PrivateKeyData privateKeyData = new PrivateKeyData("", "", "uZAfjmMwEepP8kzZCnmH6g==", "", argonOptions, password);

        PrivateKey privateKey = new PrivateKey(privateKeyData, PrivateKeyType.LOCKED);

        doReturn(new byte[]{1, 2, 3})
                .when(nacl)
                .openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

        Mockito.doReturn(new ArgonResult(null, new byte[]{}, new byte[]{4, 5, 6}))
                .when(argon2)
                .hash(any(ArgonOptions.class), eq(password), any(byte[].class));

        final Key key = keyEncryptor.decryptPrivateKey(privateKey);

        Assertions.assertThat(key.getKeyBytes()).isEqualTo(new byte[]{1, 2, 3});

        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));
        verify(argon2).hash(any(ArgonOptions.class), eq(password), any(byte[].class));

    }

}