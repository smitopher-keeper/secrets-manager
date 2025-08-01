package com.keepersecurity.spring.ksm.config;

import com.keepersecurity.secretsManager.core.KeeperFolder;
import com.keepersecurity.secretsManager.core.KeeperRecord;
import com.keepersecurity.secretsManager.core.KeeperSecrets;
import com.keepersecurity.secretsManager.core.SecretsManager;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class to resolve record specifiers to UIDs.
 */
class KsmRecordResolver {

  private static final Pattern UID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{22}$");

  private final SecretsManagerOptions options;

  KsmRecordResolver(SecretsManagerOptions options) {
    this.options = options;
  }

  boolean isUid(String spec) {
    return UID_PATTERN.matcher(spec).matches();
  }

  String resolve(String spec, KeeperSecrets allSecrets, List<KeeperFolder> folders) {
    if (isUid(spec)) {
      return spec;
    }
    int idx = spec.indexOf('/');
    if (idx < 0) {
      throw new IllegalArgumentException("Invalid record specifier: " + spec);
    }
    String folderName = spec.substring(0, idx);
    String title = spec.substring(idx + 1);
    String folderUid = folders.stream()
        .filter(f -> folderName.equals(f.getName()))
        .map(KeeperFolder::getFolderUid)
        .findFirst()
        .orElseThrow(() ->
            new IllegalArgumentException("Folder not found: " + folderName));
    return allSecrets.getRecords().stream()
        .filter(r -> folderUid.equals(r.getFolderUid()) && title.equals(r.getData().getTitle()))
        .map(KeeperRecord::getRecordUid)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Record '" + title + "' not found in folder '" + folderName + "'"));
  }

  KeeperSecrets loadAllSecrets() {
    return SecretsManager.getSecrets(options);
  }

  List<KeeperFolder> loadAllFolders() {
    return SecretsManager.getFolders(options);
  }
}
