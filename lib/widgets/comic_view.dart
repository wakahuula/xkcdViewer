import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_fadein/flutter_fadein.dart';
import 'package:photo_view/photo_view.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/utils/preferences.dart';

class ComicView extends StatelessWidget {
  final Comic comic;
  final SharedPreferences _prefs = Preferences.prefs;

  // props to https://github.com/T-Rex96/Easy_xkcd/blob/23ade11a2bcb520e5d3868f8368050413db08ed3/app/src/main/java/de/tap/easy_xkcd/utils/Comic.java
  final no2xVersion = [1193, 1446, 1667, 1735, 1739, 1744, 1778];

  ComicView(this.comic);

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onLongPress: () {
        _showAltDialog(context);
      },
      child: Container(
        padding: const EdgeInsets.only(top: 0, right: 16, bottom: 32, left: 16),
        child: Hero(
          tag: 'hero-${comic.num}',
          child: FadeIn(
            child: PhotoView(
              maxScale: PhotoViewComputedScale.covered,
              minScale: PhotoViewComputedScale.contained,
              backgroundDecoration: BoxDecoration(
                color: Color(Theme.of(context).primaryColor.value),
              ),
              loadingChild: Center(child: CircularProgressIndicator()),
              imageProvider: CachedNetworkImageProvider(_getImageUrl()),
            ),
          ),
        ),
      ),
    );
  }

  String _getImageUrl() {
    final num = comic.num;
    final dataSaver = _prefs.getBool('data_saver') ?? false;
    if (dataSaver || no2xVersion.contains(num)) {
      return comic.img;
    }
    if (num >= 1084) {
      var img = comic.img;
      return img.substring(0, img.lastIndexOf('.')) + "_2x.png";
    }
    return comic.img;
  }

  void _showAltDialog(BuildContext context) {
    if (comic.alt.isEmpty) {
      return;
    }
    _vibrate();
    showDialog(
      context: context,
      barrierDismissible: true,
      builder: (context) {
        return AlertDialog(
          titlePadding: const EdgeInsets.all(0),
          contentPadding: const EdgeInsets.all(0),
          title: Container(
            padding: const EdgeInsets.all(16),
            child: Text('${comic.num}: ${comic.title}'),
          ),
          content: Container(
            padding: const EdgeInsets.all(20),
            child: Text(comic.alt),
          ),
        );
      },
    );
  }
}

void _vibrate() async {
//    if (await Vibration.hasVibrator()) {
//      Vibration.vibrate();
//    }
}
