import 'package:flutter/material.dart';
import 'package:flutter_fadein/flutter_fadein.dart';
import 'package:outline_material_icons/outline_material_icons.dart';
import 'package:pimp_my_button/pimp_my_button.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/models/comic_model.dart';
import 'package:xkcd/models/favorites_model.dart';
import 'package:xkcd/pages/favorites_page.dart';
import 'package:xkcd/pages/settings_page.dart';
import 'package:xkcd/services/persistence_service.dart';
import 'package:xkcd/utils/FavoriteParticle.dart';
import 'package:xkcd/utils/app_colors.dart';
import 'package:xkcd/utils/app_localizations.dart';
import 'package:xkcd/utils/service_locator.dart';
import 'package:xkcd/widgets/comic_search_delegate.dart';
import 'package:xkcd/widgets/comic_view.dart';

class HomePage extends StatelessWidget {
  static final String pageRoute = '/home-page';
  static final scaffoldKey = GlobalKey<ScaffoldState>();
  final fabKey = GlobalKey();

  final PersistenceService prefs = sl<PersistenceService>();

  @override
  Widget build(BuildContext context) {
    final int accentColor =
        prefs.getValue('accentColor') ?? Colors.deepPurple.value;

    final ComicModel comicModel =
        ScopedModel.of<ComicModel>(context, rebuildOnChange: true);
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
                      Text(
                        '${comic.id}: ${comic.title}',
                        style: TextStyle(fontSize: 16),
                      ),
                      Text(
                        '${comic.year}-${comic.month}-${comic.day}',
                        style: TextStyle(fontSize: 12),
                      )
                    ],
                  ),
                ),
              );
            },
          ),
          actions: <Widget>[
            IconButton(
              icon: Icon(OMIcons.search),
              onPressed: () {
                showSearch(context: context, delegate: ComicSearchDelegate());
              },
            ),
            PopupMenuButton<int>(
              itemBuilder: (context) => [
                PopupMenuItem(
                  value: 0,
                  child: ListTile(
                    leading: Icon(OMIcons.saveAlt),
                    title: Text(AppLocalizations.of(context).get('download')),
                  ),
                ),
                PopupMenuItem(
                  value: 1,
                  child: ListTile(
                    leading: Icon(OMIcons.share),
                    title: Text(AppLocalizations.of(context).get('share')),
                  ),
                ),
              ],
              onSelected: (val) {
                switch (val) {
                  case 0:
                    ScopedModel.of<ComicModel>(context).saveComic();
                    break;
                  case 1:
                    ScopedModel.of<ComicModel>(context).shareComic();
                    break;
                }
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
          backgroundColor: Color(accentColor),
          elevation: 4,
          onPressed: () {
            ScopedModel.of<ComicModel>(context).fetchRandom();
          },
        ),
        floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
        bottomNavigationBar: _BottomAppBar(comic: comicModel.comic),
      ),
    );
  }
}

class _BottomAppBar extends StatelessWidget {
  const _BottomAppBar({Key key, @required this.comic}) : super(key: key);

  final Comic comic;

  void _showBottomSheet(BuildContext context) {
    showModalBottomSheet(
      backgroundColor: Theme.of(context).primaryColor,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(10),
          topRight: Radius.circular(10),
        ),
      ),
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

  Future<void> toggleFavorite(BuildContext context) {
    final FavoritesModel model = ScopedModel.of<FavoritesModel>(context);
    print('Toggle favorite');
    return model.isFavorite(comic.id)
        ? model.removeFromFavorites(comic.id)
        : model.addToFavorites(comic);
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        border: Border(
          top: BorderSide(
            color: AppColors.getBottomSeparatorColor(context),
            width: 1,
          ),
        ),
      ),
      child: SizedBox(
        height: 56,
        child: BottomAppBar(
          child: comic != null
              ? Row(
                  children: [
                    IconButton(
                      icon: Icon(OMIcons.menu),
                      onPressed: () => _showBottomSheet(context),
                    ),
                    ScopedModelDescendant<FavoritesModel>(
                      rebuildOnChange: true,
                      builder: (
                        BuildContext context,
                        Widget widget,
                        FavoritesModel model,
                      ) {
                        final bool isFavorite = model.isFavorite(comic.id);
                        return PimpedButton(
                          duration: Duration(milliseconds: 200),
                          particle: FavoriteParticle(),
                          pimpedWidgetBuilder: (context, controller) {
                            return IconButton(
                              icon: Icon(
                                isFavorite
                                    ? OMIcons.favorite
                                    : OMIcons.favoriteBorder,
                              ),
                              onPressed: () async {
                                controller.forward(from: 0);
                                await toggleFavorite(context);
                              },
                            );
                          },
                        );
                      },
                    ),
                    Spacer(),
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
                )
              : SizedBox.shrink(),
        ),
      ),
    );
  }
}
