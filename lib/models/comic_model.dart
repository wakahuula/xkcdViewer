import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_cache_manager/flutter_cache_manager.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:image_downloader/image_downloader.dart';
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
    comic = await apiClient.fetchNextComic(incrementValue);
    setLoading(false);
  }

  void fetchRandom() async {
    setLoading(true);
    comic = await apiClient.fetchRandomComic();
    setLoading(false);
  }

  Future<Comic> fetchComic(BuildContext context, String num) async {
    return await apiClient.fetchComic(context, int.parse(num));
  }

  void selectComic(Comic comic) {
    setLoading(true);
    apiClient.selectComic(comic);
    this.comic = comic;
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

  Future<void> saveComic({comic}) async {
    var comicToSave = comic ?? this.comic;
    if (comicToSave.img == null) {
      return;
    }
    print(comicToSave.img);
    await ImageDownloader.downloadImage(comicToSave.img);

    Fluttertoast.showToast(
      msg: '${comicToSave.num}: ${comicToSave.title} saved',
      backgroundColor: Color(0xFFD0D0D0),
      textColor: Color(0xFF000000),
      toastLength: Toast.LENGTH_SHORT,
      gravity: ToastGravity.CENTER,
    );
  }

  Future<File> getImageFromNetwork(String url) async {
    var fileInfo = await DefaultCacheManager().getFileFromCache(url);
    return fileInfo.file;
  }

  void shareComic({comic}) {
    var comicToShare = comic ?? this.comic;
    String url = '${ComicApiClient.baseUrl}${comicToShare.num}/';
    Share.share(url);
  }

  void setLoading(bool loading) {
    isLoading = loading;
    notifyListeners();
  }
}
