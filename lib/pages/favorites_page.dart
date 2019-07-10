import 'package:flutter/material.dart';
import 'package:outline_material_icons/outline_material_icons.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:xkcd/api/comic_api_client.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/models/comic_model.dart';
import 'package:xkcd/utils/app_localizations.dart';
import 'package:xkcd/utils/constants.dart';
import 'package:xkcd/utils/preferences.dart';
import 'package:xkcd/widgets/comic_view.dart';

class FavoritesPage extends StatefulWidget {
  static final String pageRoute = '/favorites-page';

  @override
  _FavoritesPageState createState() => _FavoritesPageState();
}

class _FavoritesPageState extends State<FavoritesPage> {
  final SharedPreferences prefs = Preferences.prefs;

  @override
  Widget build(BuildContext context) {
    final favorites = prefs.getStringList(Constants.favorites);

    return SafeArea(
      child: Scaffold(
        appBar: AppBar(
          titleSpacing: 0,
          elevation: 0,
          title: Text(AppLocalizations.of(context).get('favorites')),
        ),
        body: Padding(
          padding: const EdgeInsets.all(8),
          child: favorites == null || favorites.isEmpty
              ? Center(
                  child: Text(AppLocalizations.of(context).get('nothing_here')),
                )
              : FutureBuilder(
                  future: ComicApiClient.fetchComics(favorites),
                  builder: (context, snapshot) {
                    switch (snapshot.connectionState) {
                      case ConnectionState.none:
                      case ConnectionState.waiting:
                        return Center(child: CircularProgressIndicator());
                      default:
                        if (snapshot.hasError) {
                          debugPrint(snapshot.toString());
                          return SizedBox(width: 0, height: 0);
                        } else {
                          var data = snapshot.data;
                          if (data != null && data is List) {
                            return ListView.builder(
                              itemCount: data.length,
                              itemBuilder: (context, index) {
                                Comic comic = data[index];
                                return ListTile(
                                  contentPadding: const EdgeInsets.all(12),
                                  leading: Hero(
                                    tag: 'hero-${comic.num}',
                                    child: Image.network(comic.img, width: 50, height: 60),
                                  ),
                                  title: Text('${comic.num}: ${comic.title}'),
                                  trailing: IconButton(
                                    icon: Icon(OMIcons.delete),
                                    padding: const EdgeInsets.all(0),
                                    alignment: Alignment.centerRight,
                                    onPressed: () {
                                      setState(() {
                                        _removeFavorite(context, comic);
                                      });
                                    },
                                  ),
                                  onTap: () {
                                    Navigator.of(context).push(MaterialPageRoute(
                                      maintainState: true,
                                      builder: (context) {
                                        return SafeArea(
                                          child: Scaffold(
                                            appBar: AppBar(
                                              titleSpacing: 0,
                                              elevation: 0,
                                              title: Padding(
                                                padding: const EdgeInsets.only(left: 12, top: 8),
                                                child: Column(
                                                  mainAxisAlignment: MainAxisAlignment.start,
                                                  crossAxisAlignment: CrossAxisAlignment.start,
                                                  children: <Widget>[
                                                    Text(
                                                      '${comic.num}: ${comic.title}',
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
                                                    ScopedModel.of<ComicModel>(context)
                                                        .saveComic(comic: comic);
                                                  },
                                                ),
                                                IconButton(
                                                  icon: Icon(OMIcons.share),
                                                  onPressed: () {
                                                    ScopedModel.of<ComicModel>(context)
                                                        .shareComic(comic: comic);
                                                  },
                                                ),
                                              ],
                                            ),
                                            body: ComicView(comic),
                                          ),
                                        );
                                      },
                                    ));
                                  },
                                );
                              },
                            );
                          }
                        }
                        return Center(
                          child: Text(AppLocalizations.of(context).get('nothing_here')),
                        );
                    }
                  },
                ),
        ),
      ),
    );
  }

  void _removeFavorite(BuildContext context, Comic comic) {
    var num = comic.num.toString();
    List<String> favorites = prefs.getStringList(Constants.favorites);
    if (favorites == null || favorites.isEmpty) {
      return;
    }
    if (favorites.contains(num)) {
      favorites.remove(num);
    }
    prefs.setStringList(Constants.favorites, favorites);

    Scaffold.of(context).showSnackBar(
      SnackBar(
        content: Text.rich(TextSpan(children: [
          TextSpan(
            text: '${comic.title}',
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
          TextSpan(text: ' ${AppLocalizations.of(context).get('favorite_removed')}'),
        ])),
      ),
    );
  }
}
