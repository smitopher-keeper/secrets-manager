package com.keepersecurity.spring.ksm.config;

import com.keepersecurity.secretsManager.core.AppData;
import com.keepersecurity.secretsManager.core.InMemoryStorage;
import com.keepersecurity.secretsManager.core.KeeperRecord;
import com.keepersecurity.secretsManager.core.KeeperRecordData;
import com.keepersecurity.secretsManager.core.KeeperRecordField;
import com.keepersecurity.secretsManager.core.KeeperSecrets;
import com.keepersecurity.secretsManager.core.Password;
import com.keepersecurity.secretsManager.core.SecretsManager;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import com.keepersecurity.spring.ksm.autoconfig.KeeperKsmProperties;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KsmPropertySourceLocatorTest {

    @Test
    void loadsRecordAsProperties() throws Exception {
        String config = Files.readString(Path.of("src/test/resources/starter-ksm-config.json"));
        SecretsManagerOptions options = new SecretsManagerOptions(new InMemoryStorage(config));

        String recordUid = "AAAAAAAAAAAAAAAAAAAAAA";
        var ctor = KeeperKsmProperties.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        KeeperKsmProperties props = ctor.newInstance();
        props.setRecords(List.of(recordUid));

        Password password = new Password(List.of("my-secret"));
        KeeperRecordData data = new KeeperRecordData("Test Record", "login", List.of((KeeperRecordField) password), null, null);
        KeeperRecord record = new KeeperRecord(new byte[0], recordUid, null, null, null, data, 0L, List.of());
        KeeperSecrets secrets = new KeeperSecrets(new AppData("app", "type"), List.of(record), Instant.now(), List.of());

        try (MockedStatic<SecretsManager> sm = Mockito.mockStatic(SecretsManager.class)) {
            sm.when(() -> SecretsManager.getSecrets(Mockito.any(SecretsManagerOptions.class), Mockito.anyList()))
              .thenReturn(secrets);

            KsmPropertySourceLocator locator = new KsmPropertySourceLocator(options, props);
            PropertySource<?> ps = locator.locate(new MockEnvironment());

            assertEquals("my-secret", ps.getProperty("keeper://" + recordUid + "/field/password"));
            assertEquals("Test Record", ps.getProperty("keeper://" + recordUid + "/title"));
        }
    }
}
