import 'package:scoped_model/scoped_model.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/services/favorites_service.dart';
import 'package:xkcd/utils/service_locator.dart';

class FavoritesModel extends Model {
  FavoritesModel() {
    favoriteComics = _favoritesService.getFavoriteComics();
    notifyListeners();
  }

  final FavoritesService _favoritesService = sl<FavoritesService>();

  List<Comic> favoriteComics = [];

  void _updateList() {
    favoriteComics = _favoritesService.getFavoriteComics();
  }

  Future<void> addToFavorites(Comic comic) async {
    await _favoritesService.addToFavorites(comic);
    _updateList();
    notifyListeners();
  }

  Future<void> removeFromFavorites(int id) async {
    await _favoritesService.removeFromFavorites(id);
    _updateList();
    notifyListeners();
  }

  bool isFavorite(int id) => _favoritesService.isFavorite(id);
}
