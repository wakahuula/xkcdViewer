import 'dart:async';

import 'package:xkcd/blocs/comic_bloc_state.dart';
import 'package:xkcd/api/comic_api_client.dart';
import 'package:xkcd/data/comic.dart';

class ComicBloc {
  final _apiClient = ComicApiClient();

  ComicBlocState _currentComicBlocState;

  StreamSubscription<Comic> _fetchComicSubscription;

  final StreamController<ComicBlocState> _comicController = StreamController.broadcast();

  Stream<ComicBlocState> get comicStream => _comicController.stream;

  ComicBloc() {
    _currentComicBlocState = ComicBlocState.empty();
  }

  ComicBlocState getCurrentState() {
    return _currentComicBlocState;
  }

  void explainCurrentComic() {
    _apiClient.explainCurrentComic();
  }

  Future<Null> fetchLatest() async {
    fetchLatestComic();
    await comicStream.first;
    return null;
  }

  fetchLatestComic() {
    _fetchComicSubscription?.cancel();

    _currentComicBlocState.loading = true;
    _comicController.add(_currentComicBlocState);

    _apiClient.fetchLatestComic().asStream().listen((dynamic comic) {
      if (comic is Comic) {
        _currentComicBlocState.comic = comic;
      }
      _currentComicBlocState.loading = false;
      _comicController.add(_currentComicBlocState);
    });
  }

  Future<Null> fetchRandom() async {
    fetchRandomComic();
    await comicStream.first;
    return null;
  }

  fetchRandomComic() {
    _fetchComicSubscription?.cancel();

    _currentComicBlocState.loading = true;
    _comicController.add(_currentComicBlocState);

    _apiClient.fetchRandomComic().asStream().listen((dynamic comic) {
      if (comic is Comic) {
        _currentComicBlocState.comic = comic;
      }
      _currentComicBlocState.loading = false;
      _comicController.add(_currentComicBlocState);
    });
  }

  dispose() {
    _comicController.close();
  }
}
