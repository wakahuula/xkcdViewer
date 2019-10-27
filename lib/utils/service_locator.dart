import 'dart:io';

import 'package:get_it/get_it.dart';
import 'package:hive/hive.dart';
import 'package:path_provider/path_provider.dart';
import 'package:xkcd/services/hive_persistence_service.dart';
import 'package:xkcd/services/persistence_service.dart';

GetIt sl = GetIt.instance;

Future<void> registerServices() async {
  final Directory dir = await getApplicationDocumentsDirectory();
  Hive.init(dir.path);
  final Box preferencesBox = await Hive.openBox('preferences');

  sl.registerLazySingleton<PersistenceService>(
    () => HivePersistenceService(preferencesBox),
  );
}
