package com.keepersecurity.spring.ksm.config;

import com.keepersecurity.secretsManager.core.AppData;
import com.keepersecurity.secretsManager.core.InMemoryStorage;
import com.keepersecurity.secretsManager.core.KeeperFolder;
import com.keepersecurity.secretsManager.core.KeeperRecord;
import com.keepersecurity.secretsManager.core.KeeperRecordData;
import com.keepersecurity.secretsManager.core.KeeperSecrets;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KsmRecordResolverTest {

    @Test
    void isUidRecognizesPattern() {
        KsmRecordResolver resolver = new KsmRecordResolver(new SecretsManagerOptions(new InMemoryStorage((String) null)));
        assertTrue(resolver.isUid("A".repeat(22)));
        assertFalse(resolver.isUid("not-a-uid"));
    }

    @Test
    void resolvesFolderAndTitleToUid() {
        KsmRecordResolver resolver = new KsmRecordResolver(new SecretsManagerOptions(new InMemoryStorage((String) null)));
        String folderUid = "BBBBBBBBBBBBBBBBBBBBBB";
        KeeperFolder folder = new KeeperFolder(new byte[0], folderUid, null, "MyFolder");
        List<KeeperFolder> folders = List.of(folder);

        String recordUid = "AAAAAAAAAAAAAAAAAAAAAA";
        KeeperRecordData data = new KeeperRecordData("MyRecord", "login", new ArrayList<>(), null, null);
        KeeperRecord record = new KeeperRecord(new byte[0], recordUid, folderUid, null, null, data, 0L, List.of());
        KeeperSecrets secrets = new KeeperSecrets(new AppData("app", "type"), List.of(record), Instant.now(), List.of());

        assertEquals(recordUid, resolver.resolve("MyFolder/MyRecord", secrets, folders));
    }

    @Test
    void throwsWhenFolderMissing() {
        KsmRecordResolver resolver = new KsmRecordResolver(new SecretsManagerOptions(new InMemoryStorage((String) null)));
        String folderUid = "BBBBBBBBBBBBBBBBBBBBBB";
        KeeperFolder folder = new KeeperFolder(new byte[0], folderUid, null, "MyFolder");
        List<KeeperFolder> folders = List.of(folder);

        String recordUid = "AAAAAAAAAAAAAAAAAAAAAA";
        KeeperRecordData data = new KeeperRecordData("MyRecord", "login", new ArrayList<>(), null, null);
        KeeperRecord record = new KeeperRecord(new byte[0], recordUid, folderUid, null, null, data, 0L, List.of());
        KeeperSecrets secrets = new KeeperSecrets(new AppData("app", "type"), List.of(record), Instant.now(), List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve("OtherFolder/MyRecord", secrets, folders));
        assertEquals("Folder not found: OtherFolder", ex.getMessage());
    }

    @Test
    void throwsWhenRecordMissing() {
        KsmRecordResolver resolver = new KsmRecordResolver(new SecretsManagerOptions(new InMemoryStorage((String) null)));
        String folderUid = "BBBBBBBBBBBBBBBBBBBBBB";
        KeeperFolder folder = new KeeperFolder(new byte[0], folderUid, null, "MyFolder");
        List<KeeperFolder> folders = List.of(folder);

        String recordUid = "AAAAAAAAAAAAAAAAAAAAAA";
        KeeperRecordData data = new KeeperRecordData("MyRecord", "login", new ArrayList<>(), null, null);
        KeeperRecord record = new KeeperRecord(new byte[0], recordUid, folderUid, null, null, data, 0L, List.of());
        KeeperSecrets secrets = new KeeperSecrets(new AppData("app", "type"), List.of(record), Instant.now(), List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve("MyFolder/Missing", secrets, folders));
        assertEquals("Record 'Missing' not found in folder 'MyFolder'", ex.getMessage());
    }
}
