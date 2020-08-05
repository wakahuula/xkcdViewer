import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_fadein/flutter_fadein.dart';
import 'package:photo_view/photo_view.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/services/persistence_service.dart';
import 'package:xkcd/utils/service_locator.dart';

class ComicView extends StatefulWidget {
  final Comic comic;

  ComicView(this.comic);

  @override
  _ComicViewState createState() => _ComicViewState();
}

class _ComicViewState extends State<ComicView>
    with SingleTickerProviderStateMixin {
  final PersistenceService prefs = sl<PersistenceService>();

  final no2xVersion = [1193, 1446, 1667, 1735, 1739, 1744, 1778];
  Animation<Offset> slideup;
  AnimationController controller;

  @override
  void initState() {
    super.initState();
    controller =
        AnimationController(vsync: this, duration: Duration(milliseconds: 500));
    slideup = Tween<Offset>(begin: Offset(0, 0), end: Offset(0, -0.22)).animate(
        CurvedAnimation(curve: Curves.easeInOutQuad, parent: controller));
    slideup.addListener(() {
      setState(() {});
    });
  }

  @override
  void dispose() {
    controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        Align(
          alignment: Alignment.bottomCenter,
          child: Container(
            padding:
                const EdgeInsets.only(bottom: 30, right: 20, left: 20, top: 20),
            child: Text(widget.comic.alt),
          ),
        ),
        GestureDetector(
          onTapDown: (details) {
            controller.reverse();
          },
          onVerticalDragStart: (dragdetails) {
            controller.forward();
          },
          child: SlideTransition(
            position: slideup,
            child: Material(
              elevation: 4,
              child: Hero(
                tag: 'hero-${widget.comic.id}',
                child: FadeIn(
                  child: PhotoView(
                    maxScale: PhotoViewComputedScale.covered,
                    minScale: PhotoViewComputedScale.contained,
                    backgroundDecoration: BoxDecoration(
                      color: Color(Theme.of(context).primaryColor.value),
                    ),
                    loadingBuilder: (context, event) {
                      return Center(child: CircularProgressIndicator());
                    },
                    imageProvider: CachedNetworkImageProvider(_getImageUrl()),
                  ),
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }

  String _getImageUrl() {
    final num = widget.comic.id;
    final bool dataSaver = prefs.getValue('data_saver') ?? false;
    if (dataSaver || no2xVersion.contains(num)) {
      return widget.comic.img;
    }
    if (num >= 1084) {
      var img = widget.comic.img;
      return img.substring(0, img.lastIndexOf('.')) + "_2x.png";
    }
    return widget.comic.img;
  }
}
