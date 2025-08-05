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

  /**
   * Creates a new resolver that will use the supplied options when interacting with the Keeper
   * Secrets Manager.
   *
   * @param options configuration used to fetch secrets and folders
   */
  KsmRecordResolver(SecretsManagerOptions options) {
    this.options = options;
  }

  /**
   * Checks whether the provided record specifier is already a Keeper record UID.
   *
   * @param spec record specifier to evaluate
   * @return {@code true} if the specifier matches the UID format, otherwise {@code false}
   */
  boolean isUid(String spec) {
    return UID_PATTERN.matcher(spec).matches();
  }

  /**
   * Resolves a record specifier to a UID. The specifier may be either a UID or a
   * {@code "folder/title"} combination. When the specifier is not a UID, this method searches the
   * provided folders and records to locate the matching entry.
   *
   * @param spec record specifier to resolve
   * @param allSecrets secrets retrieved via {@link #loadAllSecrets()}
   * @param folders folder list retrieved via {@link #loadAllFolders()}
   * @return the UID of the resolved record
   * @throws IllegalArgumentException if the specifier is malformed, the folder cannot be found or
   *     the record title does not exist within the folder
   */
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

  /**
   * Retrieves all secrets using the configured options.
   *
   * @return the full set of secrets available to the client
   * @throws RuntimeException if the underlying Secrets Manager fails to load the secrets
   */
  KeeperSecrets loadAllSecrets() {
    return SecretsManager.getSecrets(options);
  }

  /**
   * Retrieves all folders using the configured options.
   *
   * @return list of folders available to the client
   * @throws RuntimeException if the underlying Secrets Manager fails to load the folders
   */
  List<KeeperFolder> loadAllFolders() {
    return SecretsManager.getFolders(options);
  }
}
