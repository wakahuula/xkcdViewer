import 'package:hive/hive.dart';
import 'package:xkcd/services/persistence_service.dart';

class HivePersistenceService implements PersistenceService {
  HivePersistenceService(this.box);

  final Box box;

  @override
  dynamic getValue(String key) => box.get(key);

  @override
  Future<void> setValue(String key, value) => box.put(key, value);

  @override
  Future<void> removeValue(String key) => box.delete(key);
}
