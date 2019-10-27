import 'dart:convert';

import 'package:hive/hive.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/services/favorites_service.dart';

class HiveFavoritesService implements FavoritesService {
  HiveFavoritesService(this.box);
  final Box box;

  @override
  List<Comic> getFavoriteComics() => (List<String>.from(box.values))
      .map((String json) => Comic.fromJson(jsonDecode(json)))
      .toList();

  @override
  Future<void> addToFavorites(Comic comic) {
    return box.put(comic.id, jsonEncode(comic));
  }

  @override
  Future<void> removeFromFavorites(int id) => box.delete(id);

  @override
  bool isFavorite(int id) => box.containsKey(id);

  @override
  Future<void> clearFavorites() => box.clear();
}
