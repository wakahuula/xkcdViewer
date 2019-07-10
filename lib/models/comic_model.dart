import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_cache_manager/flutter_cache_manager.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:image/image.dart' as img;
import 'package:path_provider/path_provider.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:share/share.dart';
import 'package:permission/permission.dart';
import 'package:xkcd/api/comic_api_client.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/utils/constants.dart';
import 'package:xkcd/utils/preferences.dart';

class ComicModel extends Model {
  final apiClient = ComicApiClient();
  final prefs = Preferences.prefs;

  Comic comic;

  bool isLoading = false;

  Future<String> get _appDir async {
    final directory = await getExternalStorageDirectory();
    return directory.path;
  }

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

  Future<Comic> fetchComic(String num) async {
    return await apiClient.fetchComic(int.parse(num));
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
    var permissionStatus = await Permission.getSinglePermissionStatus(PermissionName.Storage);
    if (permissionStatus == PermissionStatus.deny || permissionStatus == PermissionStatus.notDecided) {
      var permission = await Permission.requestSinglePermission(PermissionName.Storage);
      if (permission != PermissionStatus.allow) {
        return;
      }
    }
    var comicToSave = comic ?? this.comic;
    if (comicToSave.img == null) {
      return;
    }
    final image = await getImageFromNetwork(comicToSave.img);
    var path = await _appDir;
    img.Image dImage = img.decodeImage(image.readAsBytesSync());
    // todo iOS: needs to be adapted due to no external storage
    var file = File('$path/Download/${comicToSave.num}.png');
    file..writeAsBytesSync(img.encodePng(dImage));
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
