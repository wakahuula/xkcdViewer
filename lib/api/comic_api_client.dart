import 'dart:async';
import 'dart:convert';
import 'dart:math';

import 'package:http/http.dart' as http;
import 'package:url_launcher/url_launcher.dart';
import 'package:xkcd/data/comic.dart';

class ComicApiClient {
  final _baseUrl = "https://www.xkcd.com/info.0.json";
  final _subUrl = "https://www.xkcd.com/{0}/info.0.json";
  final _explainXkcdUrl = 'https://www.explainxkcd.com/wiki/index.php/';

  int _latestComicNum = -1;
  int _currentComicNum = -1;

  Future<Comic> fetchLatestComic() async {
    final response = await http.get(_baseUrl);
    if (response.statusCode == 200) {
      var comic = Comic.fromJson(json.decode(response.body));
      if (_latestComicNum < 0) {
        _latestComicNum = comic.num;
      }
      _currentComicNum = _latestComicNum;
      return comic;
    } else {
      throw Exception();
    }
  }

  Future<Comic> fetchRandomComic() async {
    if (_latestComicNum > 0) {
      final randomNumber = Random().nextInt(_latestComicNum);
      String randomUrl = _subUrl.replaceAll('{0}', randomNumber.toString());

      final response = await http.get(randomUrl);
      if (response.statusCode == 200) {
        var comic = Comic.fromJson(json.decode(response.body));
        _currentComicNum = comic.num;
        return comic;
      } else {
        throw Exception();
      }
    } else {
      return fetchLatestComic();
    }
  }

  Future<Comic> fetchComic(bool newer) async {
    // check if the current comic is the newest and return it
    if (newer && _currentComicNum == _latestComicNum) {
      return fetchLatestComic();
    }

    int comicNum = newer ? _currentComicNum + 1 : _currentComicNum - 1;
    String comicUrl = _subUrl.replaceAll('{0}', comicNum.toString());

    final response = await http.get(comicUrl);
    if (response.statusCode == 200) {
      var currentComic = Comic.fromJson(json.decode(response.body));
      _currentComicNum = currentComic.num;
      return currentComic;
    } else {}
  }

  void explainCurrentComic() async {
    final String explainUrl = _explainXkcdUrl + _currentComicNum.toString();
    if (await canLaunch(explainUrl)) {
      await launch(explainUrl);
    }
  }
}
