import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:vibrate/vibrate.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/utils/preferences.dart';

class ComicView extends StatefulWidget {
  final Comic comic;

  ComicView(this.comic);

  @override
  _ComicViewState createState() => _ComicViewState();
}

class _ComicViewState extends State<ComicView> {
  final SharedPreferences _prefs = Preferences.prefs;

  // props to https://github.com/T-Rex96/Easy_xkcd/blob/23ade11a2bcb520e5d3868f8368050413db08ed3/app/src/main/java/de/tap/easy_xkcd/utils/Comic.java
  final no2xVersion = [1193, 1446, 1667, 1735, 1739, 1744, 1778];

  @override
  Widget build(BuildContext context) {
    final comic = widget.comic;
    return Container(
      color: Colors.white,
      padding: const EdgeInsets.only(top: 0, right: 16, bottom: 32, left: 16),
      child: GestureDetector(
        onLongPress: () {
          _vibrate();
          showDialog(
            context: context,
            barrierDismissible: true,
            builder: (context) {
              var primaryColor = Theme.of(context).primaryColor;
              return AlertDialog(
                titlePadding: const EdgeInsets.all(0),
                contentPadding: const EdgeInsets.all(0),
                title: Container(
                  padding: const EdgeInsets.all(16),
                  color: primaryColor,
                  child: Text('${comic.num}: ${comic.title}',
                      style: const TextStyle(color: Colors.white)),
                ),
                content: Container(
                  color: primaryColor,
                  padding: const EdgeInsets.all(20),
                  child: Text(comic.alt, style: const TextStyle(color: Colors.white)),
                ),
              );
            },
          );
        },
        child: Hero(
          tag: 'hero-${comic.num}',
          child: PhotoView(
            maxScale: PhotoViewComputedScale.covered,
            minScale: PhotoViewComputedScale.contained,
            backgroundDecoration: const BoxDecoration(
              color: Colors.white,
            ),
            gaplessPlayback: true,
            loadingChild: Center(child: CircularProgressIndicator()),
            imageProvider: CachedNetworkImageProvider(_getImageUrl()),
          ),
        ),
      ),
    );
  }

  String _getImageUrl() {
    final comic = widget.comic;
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

  void _vibrate() async {
    if (await Vibrate.canVibrate) {
      Vibrate.feedback(FeedbackType.light);
    }
  }
}
