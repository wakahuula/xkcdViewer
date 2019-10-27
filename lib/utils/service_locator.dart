import 'dart:io';

import 'package:get_it/get_it.dart';
import 'package:hive/hive.dart';
import 'package:path_provider/path_provider.dart';
import 'package:xkcd/services/favorites_service.dart';
import 'package:xkcd/services/hive_favorites_service.dart';
import 'package:xkcd/services/hive_persistence_service.dart';
import 'package:xkcd/services/persistence_service.dart';
import 'package:xkcd/utils/constants.dart';

GetIt sl = GetIt.instance;

Future<void> registerServices() async {
  final Directory dir = await getApplicationDocumentsDirectory();
  Hive.init(dir.path);
  final Box persistenceBox = await Hive.openBox(Constants.persistenceBox);
  final Box favoritesBox = await Hive.openBox(Constants.favoritesBox);

  sl.registerLazySingleton<PersistenceService>(
    () => HivePersistenceService(persistenceBox),
  );
  sl.registerLazySingleton<FavoritesService>(
    () => HiveFavoritesService(favoritesBox),
  );
}
