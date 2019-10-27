abstract class PersistenceService {
  dynamic getValue(dynamic key);
  List<dynamic> getKeys();
  Future<void> setValue(dynamic key, dynamic value);
  Future<void> removeValue(dynamic key);
}
