import 'package:hive/hive.dart';
import 'package:xkcd/services/persistence_service.dart';

class HivePersistenceService implements PersistenceService {
  HivePersistenceService(this.box);

  final Box box;

  @override
  dynamic getValue(dynamic key) => box.get(key);

  @override
  Future<void> setValue(dynamic key, value) => box.put(key, value);

  @override
  Future<void> removeValue(dynamic key) => box.delete(key);

  @override
  List<dynamic> getKeys() => box.keys.toList();
}
