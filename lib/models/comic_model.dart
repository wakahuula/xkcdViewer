import 'package:scoped_model/scoped_model.dart';
import 'package:share/share.dart';
import 'package:xkcd/api/comic_api_client.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/utils/constants.dart';
import 'package:xkcd/utils/preferences.dart';

class ComicModel extends Model {
  final apiClient = ComicApiClient();
  final prefs = Preferences.prefs;

  Comic comic;

  bool isLoading = false;

  void loadFirstComic() async {
    setLoading(true);
    comic = await apiClient.fetchLatestComic();
    setLoading(false);
  }

  void fetchLatest() async {
    setLoading(true);
    comic = await apiClient.fetchLatestComic();
    setLoading(false);
  }

  void fetchNext(int incrementValue) async {
    setLoading(true);
    comic = await apiClient.fetchNextComic(comic.num, incrementValue);
    setLoading(false);
  }

  void fetchRandom() async {
    setLoading(true);
    comic = await apiClient.fetchRandomComic();
    setLoading(false);
  }

  void explainCurrent() {
    apiClient.explainCurrentComic();
  }

  void onFavorite() async {
    var num = comic.num.toString();
    List<String> favorites = prefs.getStringList(Constants.favorites);
    if (favorites == null || favorites.isEmpty) {
      favorites = [num];
    } else if (favorites.contains(num)) {
      favorites.remove(num);
    } else {
      favorites.add(num);
    }
    prefs.setStringList(Constants.favorites, favorites);
    notifyListeners();
  }

  bool isFavorite() {
    if (comic != null) {
      var favorites = Preferences.prefs.getStringList(Constants.favorites);
      var num = comic.num.toString();
      return favorites?.contains(num) ?? false;
    }
    return false;
  }

  void shareComic() {
    String url = '${ComicApiClient.baseUrl}${comic.num}/';
    Share.share(url);
  }

  void setLoading(bool loading) {
    isLoading = loading;
    notifyListeners();
  }
}
