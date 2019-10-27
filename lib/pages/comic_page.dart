import 'package:flutter/material.dart';
import 'package:outline_material_icons/outline_material_icons.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/models/comic_model.dart';
import 'package:xkcd/widgets/comic_view.dart';

class ComicPage extends StatelessWidget {
  const ComicPage({Key key, this.comic}) : super(key: key);

  final Comic comic;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _AppBar(comic: comic),
      body: ComicView(comic),
    );
  }
}

class _AppBar extends StatelessWidget with PreferredSizeWidget {
  _AppBar({
    Key key,
    @required this.comic,
  }) : super(key: key);

  final Comic comic;

  @override
  Widget build(BuildContext context) {
    return AppBar(
      titleSpacing: 0,
      elevation: 0,
      title: Padding(
        padding: const EdgeInsets.only(left: 12, top: 8),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              '${comic.id}: ${comic.title}',
              style: TextStyle(fontSize: 16),
            ),
            Text(
              '${comic.year}-${comic.month}-${comic.day}',
              style: TextStyle(fontSize: 16),
            )
          ],
        ),
      ),
      actions: <Widget>[
        IconButton(
          icon: Icon(OMIcons.saveAlt),
          onPressed: () {
            ScopedModel.of<ComicModel>(context).saveComic(comic: comic);
          },
        ),
        IconButton(
          icon: Icon(OMIcons.share),
          onPressed: () {
            ScopedModel.of<ComicModel>(context).shareComic(comic: comic);
          },
        ),
      ],
    );
  }

  @override
  Size get preferredSize => Size.fromHeight(56);
}
