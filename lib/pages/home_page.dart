import 'package:flutter/material.dart';
import 'package:outline_material_icons/outline_material_icons.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:xkcd/blocs/comic_bloc.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/pages/favorites_page.dart';
import 'package:xkcd/pages/settings_page.dart';
import 'package:xkcd/providers/comic_bloc_provider.dart';
import 'package:xkcd/providers/preferences.dart';
import 'package:xkcd/utils/app_localizations.dart';
import 'package:xkcd/utils/constants.dart';
import 'package:xkcd/widgets/comic_view.dart';

class HomePage extends StatefulWidget {
  static final String pageRoute = '/home-page';

  @override
  HomePageState createState() => HomePageState();
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

  _buildBodyContent() {
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

  _buildFab() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;

    return StreamBuilder(
      initialData: bloc.getCurrentState(),
      stream: bloc.comicStream,
      builder: (context, snapshot) {
        return FloatingActionButton.extended(
          icon: Icon(OMIcons.autorenew),
          label: Text(AppLocalizations.of(context).get('random')),
          onPressed: () {
            bloc.fetchRandom();
          },
        );
      },
    );
  }

  _buildBottomAppBar() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;

    return StreamBuilder(
      initialData: bloc.getCurrentState(),
      stream: bloc.comicStream,
      builder: (context, snapshot) {
        List<Widget> buttons = [
          Row(
            children: <Widget>[
              IconButton(
                icon: Icon(OMIcons.menu, color: Colors.white),
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
            children: buttons,
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
        leading: Icon(OMIcons.home, color: Colors.white),
        title: Text(appLocalizations.get('latest_comic'), style: TextStyle(color: Colors.white)),
        onTap: () {
          Navigator.pop(context);
          bloc.fetchLatest();
        },
      ),
      ListTile(
        leading: Icon(OMIcons.info, color: Colors.white),
        title: Text(appLocalizations.get('explain_current'), style: TextStyle(color: Colors.white)),
        onTap: () {
          Navigator.pop(context);
          bloc.explainCurrentComic();
        },
      ),
      ListTile(
        leading: Icon(OMIcons.favorite, color: Colors.white),
        title: Text(appLocalizations.get('my_favorites'), style: TextStyle(color: Colors.white)),
        onTap: () {
          Navigator.pop(context);
          Navigator.of(context).pushNamed(FavoritesPage.pageRoute);
        },
      ),
      ListTile(
        leading: Icon(OMIcons.settings, color: Colors.white),
        title: Text(appLocalizations.get('settings'), style: TextStyle(color: Colors.white)),
        onTap: () {
          Navigator.pop(context);
          Navigator.of(context).pushNamed(SettingsPage.pageRoute);
        },
      ),
    ];

    return Container(
      color: themeData.primaryColor,
      child: ListView.builder(
        shrinkWrap: true,
        itemCount: widgets.length,
        physics: ClampingScrollPhysics(),
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
      var favorites = prefs.getStringList(Constants.favorites);
      var num = comic.num.toString();
      isFavorite = favorites?.contains(num) ?? false;
    }

    return GestureDetector(
      onTap: () {
        _handleFavoriteAction();
      },
      onLongPress: () {
        Navigator.of(context).pushNamed(FavoritesPage.pageRoute);
      },
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Icon(
          isFavorite ? OMIcons.favorite : OMIcons.favoriteBorder,
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
    List<String> favorites = prefs.getStringList(Constants.favorites);
    if (favorites == null || favorites.isEmpty) {
      favorites = [num];
    } else if (favorites.contains(num)) {
      favorites.remove(num);
    } else {
      favorites.add(num);
    }
    prefs.setStringList(Constants.favorites, favorites);
    setState(() {});
  }

  _getCurrentComic() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;
    return bloc.getCurrentState().comic;
  }
}
