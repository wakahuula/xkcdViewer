import 'package:flutter/material.dart';
import 'package:outline_material_icons/outline_material_icons.dart';
import 'package:pimp_my_button/pimp_my_button.dart';
import 'package:rounded_modal/rounded_modal.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:xkcd/models/comic_model.dart';
import 'package:xkcd/pages/favorites_page.dart';
import 'package:xkcd/pages/settings_page.dart';
import 'package:xkcd/utils/FavoriteParticle.dart';
import 'package:xkcd/utils/app_localizations.dart';
import 'package:xkcd/utils/preferences.dart';
import 'package:xkcd/widgets/comic_view.dart';

class HomePage extends StatelessWidget {
  static final String pageRoute = '/home-page';
  static final scaffoldKey = GlobalKey<ScaffoldState>();
  final SharedPreferences prefs = Preferences.prefs;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: scaffoldKey,
      backgroundColor: Colors.white,
      appBar: _buildAppBar(context),
      body: _buildBodyContent(),
      floatingActionButton: _buildFab(context),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
      bottomNavigationBar: _buildBottomAppBar(context),
    );
  }

  Widget _buildAppBar(BuildContext context) {
    var themeData = Theme.of(context);

    return AppBar(
      titleSpacing: 0,
      backgroundColor: Colors.white,
      elevation: 0,
      title: ScopedModelDescendant<ComicModel>(
        builder: (_, child, model) {
          if (model.comic == null || model.isLoading) {
            return SizedBox(width: 0, height: 0);
          }
          var comic = model.comic;

          return Padding(
            padding: const EdgeInsets.only(left: 12, top: 8),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.start,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text(
                  '${comic.num}: ${comic.title}',
                  style: themeData.textTheme.title.copyWith(color: themeData.primaryColor),
                ),
                Text(
                  '${comic.year}-${comic.month}-${comic.day}',
                  style: themeData.textTheme.subhead.copyWith(color: themeData.primaryColor),
                )
              ],
            ),
          );
        },
      ),
      actions: <Widget>[
        IconButton(
          icon: Icon(OMIcons.saveAlt, color: themeData.primaryColor),
          onPressed: () {
            ScopedModel.of<ComicModel>(context).saveComic();
          },
        ),
        IconButton(
          icon: Icon(OMIcons.share, color: themeData.primaryColor),
          onPressed: () {
            ScopedModel.of<ComicModel>(context).shareComic();
          },
        ),
      ],
    );
  }

  Widget _buildBodyContent() {
    return ScopedModelDescendant<ComicModel>(
      builder: (context, child, model) {
        if (model.comic == null || model.isLoading) {
          return Container();
        }
        return Dismissible(
          key: ValueKey(model.hashCode),
          resizeDuration: Duration(milliseconds: 300),
          secondaryBackground: Container(
            color: Theme.of(context).primaryColor,
            child: Center(
              child: Icon(
                OMIcons.keyboardArrowRight,
                color: Colors.white,
                size: 92,
              ),
            ),
          ),
          background: Container(
            color: Theme.of(context).primaryColor,
            child: Center(
              child: Icon(
                OMIcons.keyboardArrowLeft,
                color: Colors.white,
                size: 92,
              ),
            ),
          ),
          onDismissed: (DismissDirection direction) {
            var directionValue = direction == DismissDirection.endToStart ? 1 : -1;
            ScopedModel.of<ComicModel>(context).fetchNext(directionValue);
          },
          child: ComicView(model.comic),
        );
      },
    );
  }

  Widget _buildFab(BuildContext context) {
    return FloatingActionButton.extended(
      icon: Icon(OMIcons.autorenew),
      label: Text(AppLocalizations.of(context).get('random')),
      onPressed: () {
        ScopedModel.of<ComicModel>(context).fetchRandom();
      },
    );
  }

  Widget _buildBottomAppBar(BuildContext context) {
    return SizedBox(
      height: 56,
      child: BottomAppBar(
        color: Theme.of(context).primaryColor,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            GestureDetector(
              onVerticalDragUpdate: (details) {
                if (details.delta.dy < 0) {
                  _showBottomSheet(context);
                }
              },
              child: IconButton(
                icon: Icon(OMIcons.menu, color: Colors.white),
                onPressed: () {
                  _showBottomSheet(context);
                },
              ),
            ),
            ScopedModelDescendant<ComicModel>(
              builder: (context, widget, model) {
                bool isFavorite = model.isFavorite() ?? false;

                return PimpedButton(
                  duration: Duration(milliseconds: 200),
                  particle: FavoriteParticle(),
                  pimpedWidgetBuilder: (context, controller) {
                    return IconButton(
                      icon: Icon(
                        isFavorite ? OMIcons.favorite : OMIcons.favoriteBorder,
                        color: Colors.white,
                      ),
                      onPressed: () {
                        controller.forward(from: 0);
                        model.onFavorite();
                      },
                    );
                  },
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  void _showBottomSheet(BuildContext context) {
    showRoundedModalBottomSheet(
      context: context,
      color: Theme.of(context).primaryColor,
      builder: (context) {
        var appLocalizations = AppLocalizations.of(context);

        var widgets = [
          ListTile(
            leading: Icon(OMIcons.home, color: Colors.white),
            title: Text(appLocalizations.get('latest_comic'),
                style: const TextStyle(color: Colors.white)),
            onTap: () {
              Navigator.pop(context);
              ScopedModel.of<ComicModel>(context).fetchLatest();
            },
          ),
          ListTile(
            leading: Icon(OMIcons.info, color: Colors.white),
            title: Text(appLocalizations.get('explain_current'),
                style: const TextStyle(color: Colors.white)),
            onTap: () {
              Navigator.pop(context);
              ScopedModel.of<ComicModel>(context).explainCurrent();
            },
          ),
          ListTile(
            leading: Icon(OMIcons.favorite, color: Colors.white),
            title: Text(appLocalizations.get('my_favorites'),
                style: const TextStyle(color: Colors.white)),
            onTap: () {
              Navigator.pop(context);
              Navigator.of(context).pushNamed(FavoritesPage.pageRoute);
            },
          ),
          ListTile(
            leading: Icon(OMIcons.settings, color: Colors.white),
            title:
                Text(appLocalizations.get('settings'), style: const TextStyle(color: Colors.white)),
            onTap: () {
              Navigator.pop(context);
              Navigator.of(context).pushNamed(SettingsPage.pageRoute);
            },
          ),
        ];

        return ListView.builder(
          itemCount: widgets.length,
          shrinkWrap: true,
          physics: ClampingScrollPhysics(),
          itemBuilder: (context, index) {
            return widgets[index];
          },
        );
      },
    );
  }
}
