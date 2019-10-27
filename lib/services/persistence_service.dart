abstract class PersistenceService {
  dynamic getValue(String key);
  Future<void> setValue(String key, dynamic value);
  Future<void> removeValue(String key);
}
