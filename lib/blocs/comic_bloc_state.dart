import 'package:xkcd/data/comic.dart';

class ComicBlocState {
  bool loading;
  Comic comic;

  ComicBlocState(this.loading, this.comic);

  ComicBlocState.empty() {
    loading = false;
    comic = null;
  }
}
