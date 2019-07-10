import 'dart:async';
import 'dart:collection';
import 'dart:convert';
import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_web_browser/flutter_web_browser.dart';
import 'package:http/http.dart' as http;
import 'package:url_launcher/url_launcher.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/pages/home_page.dart';

class ComicApiClient {
  static final baseUrl = 'https://www.xkcd.com/';
  static final _baseApiUrl = 'https://www.xkcd.com/info.0.json';
  static final _subApiUrl = 'https://www.xkcd.com/{0}/info.0.json';
  static final _explainXkcdUrl = 'https://www.explainxkcd.com/wiki/index.php/';

  int _latestComicNum = -1;
  int _currentComicNum = -1;

  HashMap<int, Comic> _cachedComics = HashMap();

  Future<Comic> fetchLatestComic() async {
    if (_latestComicNum >= 0 && _cachedComics.containsKey(_latestComicNum)) {
      _currentComicNum = _latestComicNum;
      return _cachedComics[_latestComicNum];
    }

    final response = await http.get(_baseApiUrl);
    if (response.statusCode == 200) {
      var comic = Comic.fromJson(json.decode(response.body));
      if (_latestComicNum < 0) {
        _latestComicNum = comic.num;
      }
      _currentComicNum = _latestComicNum;
      _cachedComics.putIfAbsent(_latestComicNum, () => comic);
      return comic;
    } else {
      debugPrint('${response.statusCode}: ${response.toString()}');
    }
    return null;
  }

  Future<Comic> fetchNextComic(int incrementValue) async {
    // todo: add bounce animation or toast(?) for nonexistent comics
    var nextComicNum = _currentComicNum + incrementValue;
    nextComicNum = nextComicNum > _latestComicNum ? _latestComicNum : nextComicNum;

    if (nextComicNum > 0) {
      String nextComicUrl = _subApiUrl.replaceAll('{0}', nextComicNum.toString());

      final response = await http.get(nextComicUrl);
      if (response.statusCode == 200) {
        var comic = Comic.fromJson(json.decode(response.body));
        _currentComicNum = nextComicNum;
        _cachedComics.putIfAbsent(_currentComicNum, () => comic);
        return comic;
      } else {
        debugPrint('${response.statusCode}: ${response.toString()}');
      }
    }
    return fetchRandomComic();
  }

  Future<Comic> fetchComic(int num) async {
    if (num < 0 || num > _latestComicNum) {
      HomePage.scaffoldKey.currentState.showSnackBar(SnackBar(content: Text('NOPE')));
      return _cachedComics[num];
    }

    String nextComicUrl = _subApiUrl.replaceAll('{0}', num.toString());

    final response = await http.get(nextComicUrl);
    if (response.statusCode == 200) {
      var comic = Comic.fromJson(json.decode(response.body));
      _currentComicNum = num;
      _cachedComics.putIfAbsent(_currentComicNum, () => comic);
      return comic;
    } else {
      debugPrint('${response.statusCode}: ${response.toString()}');
    }
    return _cachedComics[_latestComicNum];
  }

  Future<Comic> fetchRandomComic() async {
    if (_latestComicNum > 0) {
      final randomNumber = Random().nextInt(_latestComicNum);
      if (_cachedComics.containsKey(randomNumber)) {
        _currentComicNum = randomNumber;
        return _cachedComics[randomNumber];
      }

      String randomUrl = _subApiUrl.replaceAll('{0}', randomNumber.toString());

      final response = await http.get(randomUrl);
      if (response.statusCode == 200) {
        var comic = Comic.fromJson(json.decode(response.body));
        _currentComicNum = comic.num;
        _cachedComics.putIfAbsent(_currentComicNum, () => comic);
        return comic;
      } else {
        debugPrint('${response.statusCode}: ${response.toString()}');
      }
    }
    return fetchLatestComic();
  }

  void selectComic(Comic comic) {
    _currentComicNum = comic.num;
    _cachedComics.putIfAbsent(_currentComicNum, () => comic);
  }

  void explainCurrentComic() async {
    final String explainUrl = _explainXkcdUrl + _currentComicNum.toString();
    if (await canLaunch(explainUrl)) {
      FlutterWebBrowser.openWebPage(url: explainUrl, androidToolbarColor: Colors.white);
    }
  }

  static Future<List<Comic>> fetchComics(List<String> comicIds) async {
    final responses = await Future.wait(comicIds.map((comicId) {
      String url = _subApiUrl.replaceAll('{0}', comicId);
      return http.get(url);
    }));
    var list = responses.map((response) {
      return Comic.fromJson(json.decode(response.body));
    }).toList();
    return list;
  }
}
