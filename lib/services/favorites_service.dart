import 'package:xkcd/data/comic.dart';

abstract class FavoritesService {
  bool isFavorite(int id);
  List<Comic> getFavoriteComics();
  Future<void> addToFavorites(Comic comic);
  Future<void> removeFromFavorites(int id);
}
