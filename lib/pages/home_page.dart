import 'dart:async';

import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:xkcd/blocs/comic_bloc.dart';
import 'package:xkcd/providers/comic_bloc_provider.dart';
import 'package:xkcd/blocs/comic_bloc_state.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/pages/settings_page.dart';
import 'package:xkcd/providers/preferences.dart';
import 'package:xkcd/utils/app_colors.dart';

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

    return StreamBuilder(
      initialData: bloc.getCurrentState(),
      stream: bloc.comicStream,
      builder: _buildBodyContent,
    );
  }

  Widget _buildBodyContent(BuildContext context, AsyncSnapshot<ComicBlocState> snapshot) {
    if (snapshot.hasError) {
      return Center(
        child: Text('Oops... something went wrong!'),
      );
    }

    if (snapshot.data.loading) {
      return Center();
    }

    Comic comic = snapshot.data.comic;

    var themeData = Theme.of(context);
    return Scaffold(
      backgroundColor: AppColors.backgroundColor,
      appBar: AppBar(
        title: Text('XKCD Viewer'),
        actions: <Widget>[
          _buildFavoriteButton(),
          _buildExplainButton(),
          _buildPopupMenuButton(),
        ],
        backgroundColor: themeData.primaryColor,
      ),
      body: Container(
        padding: EdgeInsets.all(10.0),
        child: GestureDetector(
          onLongPress: () {
            showDialog(
              context: context,
              barrierDismissible: true,
              builder: (context) {
                return AlertDialog(
                  content: SingleChildScrollView(
                    child: Text(comic.alt),
                  ),
                );
              },
            );
          },
          child: PhotoViewInline(
            maxScale: 4.0,
            minScale: PhotoViewScaleBoundary.contained,
            backgroundColor: AppColors.backgroundColor,
            imageProvider: NetworkImage(comic.img),
            loadingChild: Center(child: CircularProgressIndicator()),
          ),
        ),
      ),
      bottomNavigationBar: BottomAppBar(
        color: themeData.primaryColor,
        child: _buildBottomBar(),
      ),
    );
  }

  _buildExplainButton() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;
    return IconButton(
      icon: Icon(Icons.info_outline),
      onPressed: bloc.explainCurrentComic,
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

    return IconButton(
      icon: Icon(
        isFavorite ? Icons.favorite : Icons.favorite_border,
        color: Colors.white,
      ),
      onPressed: () {
        _handleFavoriteAction(isFavorite);
      },
    );
  }

  _handleFavoriteAction(bool add) {
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

  _buildPopupMenuButton() {
    return PopupMenuButton(
      itemBuilder: (BuildContext context) {
        return [
          PopupMenuItem(
            child: GestureDetector(
              child: Text('Settings'),
              onTap: () {
                Navigator.pop(context);
                Navigator.of(context).pushNamed(SettingsPage.settingsPageRoute);
              },
            ),
          ),
          PopupMenuItem(
            child: GestureDetector(
              child: Text('About'),
              onTap: () {
                Navigator.pop(context);
                showDialog(
                  context: context,
                  builder: (BuildContext context) {
                    return AboutDialog(
                      applicationName: 'xkcdViewer',
                      applicationVersion: '1.0',
                      applicationLegalese: 'Built with ❤ and Flutter by Kosta Stoupas. Launcher icon by Papirus Development Team.',
                    );
                  },
                );
              },
            ),
          ),
        ];
      },
    );
  }

  _buildBottomBar() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;
    bool leftHanded = prefs.getBool('leftHanded') ?? false;

    List<Widget> buttons = [
      FlatButton(
        onPressed: bloc.fetchLatest,
        child: Text('LATEST', style: TextStyle(color: Colors.white)),
      ),
      _buildRightButtons(),
    ];

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: leftHanded ? buttons.reversed.toList() : buttons,
    );
  }

  _buildRightButtons() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;

    return Row(
      children: <Widget>[
        IconButton(
          icon: Icon(Icons.chevron_left),
          color: Colors.white,
          onPressed: bloc.fetchOlder,
        ),
        FlatButton(
          onPressed: bloc.fetchRandom,
          child: Text('RANDOM', style: TextStyle(color: Colors.white)),
        ),
        IconButton(
          icon: Icon(Icons.chevron_right),
          color: Colors.white,
          onPressed: bloc.fetchNewer,
        ),
      ],
    );
  }

  Comic _getCurrentComic() {
    ComicBloc bloc = ComicBlocProvider.of(context).bloc;
    return bloc.getCurrentState().comic;
  }
}
