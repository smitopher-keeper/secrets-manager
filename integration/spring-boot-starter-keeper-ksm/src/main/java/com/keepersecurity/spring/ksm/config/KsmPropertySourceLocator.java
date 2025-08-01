package com.keepersecurity.spring.ksm.config;

import com.keepersecurity.secretsManager.core.KeeperFolder;
import com.keepersecurity.secretsManager.core.KeeperRecord;
import com.keepersecurity.secretsManager.core.KeeperRecordField;
import com.keepersecurity.secretsManager.core.KeeperSecrets;
import com.keepersecurity.secretsManager.core.Notation;
import com.keepersecurity.secretsManager.core.SecretsManager;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import com.keepersecurity.spring.ksm.autoconfig.KeeperKsmProperties;
import com.keepersecurity.spring.ksm.config.KsmRecordResolver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * Loads Keeper Secrets Manager records and exposes them as Spring configuration
 * properties.
 */
@ConditionalOnClass(SecretsManager.class)
public class KsmPropertySourceLocator implements PropertySourceLocator {

  private final SecretsManagerOptions options;
  private final KeeperKsmProperties properties;
  private final KsmRecordResolver resolver;

  public KsmPropertySourceLocator(SecretsManagerOptions options, KeeperKsmProperties properties) {
    this.options = options;
    this.properties = properties;
    this.resolver = new KsmRecordResolver(options);
  }

  @Override
  public PropertySource<?> locate(Environment environment) {
    List<String> specs = properties.getRecords();
    if (specs == null || specs.isEmpty()) {
      return null;
    }
    List<String> recordIds = new java.util.ArrayList<>();
    KeeperSecrets allSecrets = null;
    List<KeeperFolder> folders = null;
    for (String spec : specs) {
      if (resolver.isUid(spec)) {
        recordIds.add(spec);
      } else {
        if (allSecrets == null) {
          allSecrets = resolver.loadAllSecrets();
          folders = resolver.loadAllFolders();
        }
        recordIds.add(resolver.resolve(spec, allSecrets, folders));
      }
    }
    KeeperSecrets secrets = SecretsManager.getSecrets(options, recordIds);
    Map<String, Object> flat = new HashMap<>();
    for (KeeperRecord record : secrets.getRecords()) {
      flattenRecord(record, flat);
    }
    return new MapPropertySource("keeperKsm", flat);
  }

  private void flattenRecord(KeeperRecord record, Map<String, Object> flat) {
    String prefix = record.getRecordUid();
    flat.put(prefix + ".title", record.getData().getTitle());
    flat.put(prefix + ".type", record.getData().getType());
    if (record.getData().getNotes() != null) {
      flat.put(prefix + ".notes", record.getData().getNotes());
    }
    record.getData().getFields().forEach(f -> addField(prefix, f, flat));
    if (record.getData().getCustom() != null) {
      record.getData().getCustom().forEach(f -> addField(prefix, f, flat));
    }
  }

  private void addField(String prefix, KeeperRecordField field, Map<String, Object> flat) {
    String type = Notation.fieldType(field);
    String label = field.getLabel();
    String name = (label != null && !label.isBlank()) ? label : type;
    String key = prefix + "." + name;
    Object value = extractValue(field);
    flat.put(key, value);
  }

  private Object extractValue(KeeperRecordField field) {
    try {
      Object val = field.getClass().getMethod("getValue").invoke(field);
      if (val instanceof List<?> list) {
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
      }
      return val != null ? val.toString() : null;
    } catch (Exception e) {
      return null;
    }
  }
}
