import 'package:dynamic_theme/dynamic_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_fadein/flutter_fadein.dart';
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
  final fabKey = GlobalKey();
  final SharedPreferences prefs = Preferences.prefs;

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        key: scaffoldKey,
        appBar: AppBar(
          titleSpacing: 0,
          elevation: 0,
          title: ScopedModelDescendant<ComicModel>(
            builder: (_, child, model) {
              if (model.comic == null || model.isLoading) {
                return SizedBox(width: 0, height: 0);
              }
              var comic = model.comic;

              return Padding(
                padding: const EdgeInsets.only(left: 12, top: 8),
                child: FadeIn(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.start,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text('${comic.num}: ${comic.title}'),
                      Text('${comic.year}-${comic.month}-${comic.day}')
                    ],
                  ),
                ),
              );
            },
          ),
          actions: <Widget>[
            IconButton(
              icon: Icon(OMIcons.saveAlt),
              onPressed: () {
                ScopedModel.of<ComicModel>(context).saveComic();
              },
            ),
            IconButton(
              icon: Icon(OMIcons.share),
              onPressed: () {
                ScopedModel.of<ComicModel>(context).shareComic();
              },
            ),
          ],
        ),
        body: ScopedModelDescendant<ComicModel>(
          builder: (context, child, model) {
            if (model.comic == null || model.isLoading) {
              return Container();
            }
            return ComicView(model.comic);
          },
        ),
        floatingActionButton: FloatingActionButton.extended(
          icon: Icon(OMIcons.autorenew),
          label: Text(AppLocalizations.of(context).get('random')),
          onPressed: () {
            ScopedModel.of<ComicModel>(context).fetchRandom();
          },
        ),
        floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
        bottomNavigationBar: SizedBox(
          height: 56,
          child: BottomAppBar(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: <Widget>[
                    IconButton(
                      icon: Icon(OMIcons.menu),
                      onPressed: () {
                        _showBottomSheet(context);
                      },
                    ),
                    ScopedModelDescendant<ComicModel>(
                      builder: (context, widget, model) {
                        bool isFavorite = model.isFavorite() ?? false;
                        return PimpedButton(
                          duration: Duration(milliseconds: 200),
                          particle: FavoriteParticle(),
                          pimpedWidgetBuilder: (context, controller) {
                            return IconButton(
                              icon: Icon(isFavorite ? OMIcons.favorite : OMIcons.favoriteBorder),
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
                Row(
                  children: <Widget>[
                    IconButton(
                      icon: Icon(OMIcons.chevronLeft, size: 32),
                      onPressed: () {
                        ScopedModel.of<ComicModel>(context).fetchNext(-1);
                      },
                    ),
                    IconButton(
                      icon: Icon(OMIcons.chevronRight, size: 32),
                      onPressed: () {
                        ScopedModel.of<ComicModel>(context).fetchNext(1);
                      },
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _showBottomSheet(BuildContext context) {
    showRoundedModalBottomSheet(
      color: Theme.of(context).primaryColor,
      context: context,
      builder: (context) {
        var appLocalizations = AppLocalizations.of(context);

        var widgets = [
          ListTile(
            leading: Icon(OMIcons.home),
            title: Text(appLocalizations.get('latest_comic')),
            onTap: () {
              Navigator.pop(context);
              ScopedModel.of<ComicModel>(context).fetchLatest();
            },
          ),
          ListTile(
            leading: Icon(OMIcons.info),
            title: Text(appLocalizations.get('explain_current')),
            onTap: () {
              Navigator.pop(context);
              ScopedModel.of<ComicModel>(context).explainCurrent();
            },
          ),
          ListTile(
            leading: Icon(OMIcons.favorite),
            title: Text(appLocalizations.get('my_favorites')),
            onTap: () {
              Navigator.pop(context);
              Navigator.of(context).pushNamed(FavoritesPage.pageRoute);
            },
          ),
          ListTile(
            leading: Icon(OMIcons.settings),
            title: Text(appLocalizations.get('settings')),
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
