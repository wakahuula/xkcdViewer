import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:outline_material_icons/outline_material_icons.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/models/favorites_model.dart';
import 'package:xkcd/pages/comic_page.dart';
import 'package:xkcd/utils/app_localizations.dart';

class FavoritesPage extends StatelessWidget {
  static final String pageRoute = '/favorites-page';
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        titleSpacing: 0,
        title: Text(AppLocalizations.of(context).get('favorites')),
      ),
      body: _FavouritesList(),
    );
  }
}

class _FavouritesList extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return ScopedModelDescendant<FavoritesModel>(
      builder: (BuildContext context, Widget child, FavoritesModel model) {
        final List<Comic> comics = model.favoriteComics;
        return comics.isEmpty
            ? Center(
                child: Text(AppLocalizations.of(context).get('nothing_here')),
              )
            : ListView.builder(
                padding: const EdgeInsets.all(8),
                itemCount: comics.length,
                itemBuilder: (_, int i) => _ComicCard(comic: comics[i]),
              );
      },
    );
  }
}

class _ComicCard extends StatelessWidget {
  const _ComicCard({Key key, this.comic}) : super(key: key);

  final Comic comic;

  Future<void> openComicPage(BuildContext context) {
    return Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => ComicPage(comic: comic)),
    );
  }

  @override
  Widget build(BuildContext context) {
    final FavoritesModel model = ScopedModel.of<FavoritesModel>(context);
    return ListTile(
      contentPadding: const EdgeInsets.all(12),
      /* leading: Hero(
        tag: 'hero-${comic.id}',
        child: CachedNetworkImage(
          imageUrl: comic.img,
          width: 50,
          height: 60,
        ),
      ), */
      leading: CachedNetworkImage(
        imageUrl: comic.img,
        width: 50,
        height: 60,
      ),
      title: Text('${comic.id}: ${comic.title}'),
      trailing: IconButton(
        icon: Icon(OMIcons.delete),
        padding: const EdgeInsets.all(0),
        alignment: Alignment.centerRight,
        onPressed: () => model.removeFromFavorites(comic.id),
      ),
      onTap: () => openComicPage(context),
    );
  }
}
