import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:xkcd/blocs/comic_bloc.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/pages/favorites_page.dart';
import 'package:xkcd/pages/settings_page.dart';
import 'package:xkcd/providers/comic_bloc_provider.dart';
import 'package:xkcd/providers/preferences.dart';
import 'package:xkcd/utils/app_localizations.dart';
import 'package:xkcd/widgets/comic_view.dart';

class HomePage extends StatefulWidget {
  static final String homePageRoute = '/home-page';

  @override
  HomePageState createState() => new HomePageState();
}

class HomePageState extends State<HomePage> {
  final SharedPreferences prefs = Preferences.prefs;
  bool _firstLoad = true;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;

    if (_firstLoad) {
      bloc.fetchLatestComic();
      _firstLoad = false;
    }

    return Scaffold(
      backgroundColor: Colors.white,
      body: _buildBodyContent(),
      floatingActionButton: _buildFab(),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
      bottomNavigationBar: _buildBottomAppBar(),
    );
  }

  StreamBuilder _buildBodyContent() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;

    return StreamBuilder(
      initialData: bloc.getCurrentState(),
      stream: bloc.comicStream,
      builder: (context, snapshot) {
        if (snapshot.hasError) {
          return Center(
            child: Text(AppLocalizations.of(context).get('something_wrong')),
          );
        }
        if (snapshot.data.loading) {
          return Center();
        }

        Comic comic = snapshot.data.comic;
        return ComicView(comic);
      },
    );
  }

  StreamBuilder _buildFab() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;

    return StreamBuilder(
      initialData: bloc.getCurrentState(),
      stream: bloc.comicStream,
      builder: (context, snapshot) {
        return FloatingActionButton.extended(
          icon: Icon(Icons.autorenew),
          label: Text(AppLocalizations.of(context).get('random')),
          onPressed: () {
            bloc.fetchRandom();
          },
        );
      },
    );
  }

  StreamBuilder _buildBottomAppBar() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;

    return StreamBuilder(
      initialData: bloc.getCurrentState(),
      stream: bloc.comicStream,
      builder: (context, snapshot) {
        bool leftHanded = prefs.getBool('leftHanded') ?? false;
        List<Widget> buttons = [
          Row(
            children: <Widget>[
              IconButton(
                icon: Icon(Icons.menu, color: Colors.white),
                onPressed: () {
                  showModalBottomSheet(
                    context: context,
                    builder: (context) {
                      return _buildBottomSheet();
                    },
                  );
                },
              ),
            ],
          ),
          _buildFavoriteButton(),
        ];

        return BottomAppBar(
          color: Theme.of(context).primaryColor,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: leftHanded ? buttons.reversed.toList() : buttons,
          ),
        );
      },
    );
  }

  _buildBottomSheet() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;
    var themeData = Theme.of(context);
    var appLocalizations = AppLocalizations.of(context);

    final widgets = [
      ListTile(
        leading: Icon(Icons.home, color: Colors.white),
        title: Text(appLocalizations.get('latest_comic'), style: TextStyle(color: Colors.white)),
        onTap: () {
          Navigator.pop(context);
          bloc.fetchLatest();
        },
      ),
      ListTile(
        leading: Icon(Icons.info_outline, color: Colors.white),
        title: Text(appLocalizations.get('explain_current'), style: TextStyle(color: Colors.white)),
        onTap: () {
          Navigator.pop(context);
          bloc.explainCurrentComic();
        },
      ),
      ListTile(
        leading: Icon(Icons.favorite, color: Colors.white),
        title: Text(appLocalizations.get('my_favorites'), style: TextStyle(color: Colors.white)),
        onTap: () {
          Navigator.pop(context);
          Navigator.of(context).pushNamed(FavoritesPage.favoritesPageRoute);
        },
      ),
      ListTile(
        leading: Icon(Icons.settings, color: Colors.white),
        title: Text(appLocalizations.get('settings'), style: TextStyle(color: Colors.white)),
        onTap: () {
          Navigator.pop(context);
          Navigator.of(context).pushNamed(SettingsPage.settingsPageRoute);
        },
      ),
    ];

    return Container(
      color: themeData.primaryColor,
      child: ListView.builder(
        shrinkWrap: true,
        itemCount: widgets.length,
        itemBuilder: (context, index) {
          return widgets[index];
        },
      ),
    );
  }

  _buildFavoriteButton() {
    Comic comic = _getCurrentComic();

    bool isFavorite = false;
    if (comic != null) {
      var favorites = prefs.getStringList('favorites');
      var num = comic.num.toString();
      isFavorite = favorites?.contains(num) ?? false;
    }

    return GestureDetector(
      onTap: () {
        _handleFavoriteAction();
      },
      onLongPress: () {
        Navigator.of(context).pushNamed(FavoritesPage.favoritesPageRoute);
      },
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Icon(
          isFavorite ? Icons.favorite : Icons.favorite_border,
          color: Colors.white,
        ),
      ),
    );
  }

  _handleFavoriteAction() {
    Comic comic = _getCurrentComic();
    if (comic == null) {
      return;
    }

    var num = comic.num.toString();
    List<String> favorites = prefs.getStringList('favorites');
    if (favorites == null || favorites.isEmpty) {
      favorites = [num];
    } else if (favorites.contains(num)) {
      favorites.remove(num);
    } else {
      favorites.add(num);
    }
    prefs.setStringList('favorites', favorites);
    setState(() {});
  }

  Comic _getCurrentComic() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;
    return bloc.getCurrentState().comic;
  }
}
