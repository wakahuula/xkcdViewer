import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import 'package:share/share.dart';
import 'package:vibrate/vibrate.dart';
import 'package:xkcd/api/comic_api_client.dart';
import 'package:xkcd/data/comic.dart';

class ComicView extends StatelessWidget {
  // props to https://github.com/T-Rex96/Easy_xkcd/blob/23ade11a2bcb520e5d3868f8368050413db08ed3/app/src/main/java/de/tap/easy_xkcd/utils/Comic.java
  final no2xVersion = [1193, 1446, 1667, 1735, 1739, 1744, 1778];
  final Comic comic;

  ComicView(this.comic);

  @override
  Widget build(BuildContext context) {
    var themeData = Theme.of(context);

    return Container(
      padding: EdgeInsets.only(top: 40.0, right: 20.0, bottom: 20.0, left: 20.0),
      child: Stack(
        overflow: Overflow.visible,
        fit: StackFit.expand,
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.only(top: 45.0),
            child: GestureDetector(
              onLongPress: () {
                _vibrate();
                showDialog(
                  context: context,
                  barrierDismissible: true,
                  builder: (context) {
                    return AlertDialog(
                      contentPadding: EdgeInsets.all(0.0),
                      content: Container(
                        color: Theme.of(context).primaryColor,
                        padding: EdgeInsets.all(20.0),
                        child: Text(comic.alt, style: TextStyle(color: Colors.white)),
                      ),
                    );
                  },
                );
              },
              child: Hero(
                tag: 'hero-${comic.num}',
                child: PhotoViewInline(
                  maxScale: 4.0,
                  minScale: PhotoViewComputedScale.contained,
                  backgroundColor: Colors.white,
                  gaplessPlayback: true,
                  imageProvider: NetworkImage(_getImageUrl()),
                  loadingChild: Center(child: CircularProgressIndicator()),
                ),
              ),
            ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    '${comic.num}: ${comic.title}',
                    style: themeData.textTheme.title,
                  ),
                  Text(
                    '${comic.year}-${comic.month}-${comic.day}',
                    style: themeData.textTheme.caption.copyWith(fontSize: 14.0),
                  )
                ],
              ),
              IconButton(
                alignment: Alignment.topRight,
                padding: EdgeInsets.all(0.0),
                icon: Icon(Icons.share),
                onPressed: () {
                  _shareComic();
                },
              ),
            ],
          ),
        ],
      ),
    );
  }

  _getImageUrl() {
    final num = comic.num;
    if (no2xVersion.contains(num)) {
      return '${ComicApiClient.baseUrl}${comic.num}';
    }
    if (num >= 1084) {
      var img = comic.img;
      return img.substring(0, img.lastIndexOf('.')) + "_2x.png";
    }
    return comic.img;
  }

  _shareComic() {
    String url = '${ComicApiClient.baseUrl}${comic.num}/';
    Share.share(url);
  }

  void _vibrate() async {
    if (await Vibrate.canVibrate) {
      Vibrate.feedback(FeedbackType.light);
    }
  }
}
