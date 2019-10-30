import 'package:flutter/material.dart';
import 'package:flutter_material_color_picker/flutter_material_color_picker.dart';
import 'package:outline_material_icons/outline_material_icons.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:xkcd/models/favorites_model.dart';
import 'package:xkcd/models/preferences_model.dart';
import 'package:xkcd/pages/contributors_page.dart';
import 'package:xkcd/utils/app_localizations.dart';

class SettingsPage extends StatefulWidget {
  static final String pageRoute = '/settings-page';

  @override
  State<StatefulWidget> createState() => SettingsPageState();
}

class SettingsPageState extends State<SettingsPage> {
  final GlobalKey<ScaffoldState> _settingsScaffoldKey = GlobalKey();
  final GlobalKey _menuKey = GlobalKey();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _settingsScaffoldKey,
      appBar: AppBar(
        titleSpacing: 0,
        title: Text(AppLocalizations.of(context).get('settings')),
      ),
      body: Container(
        padding: const EdgeInsets.all(8),
        child: ListView(
          children: <Widget>[
            _buildAccentColorButton(context),
            _buildThemeButton(context),
            _buildTitleWidget(AppLocalizations.of(context).get('images')),
            _buildImagesOptions(context),
            _buildTitleWidget(AppLocalizations.of(context).get('favorites')),
            _buildClearFavorites(context),
            _buildTitleWidget(AppLocalizations.of(context).get('about')),
            _buildAbout(context),
          ],
        ),
      ),
    );
  }

  Widget _buildTitleWidget(String title) {
    return Container(
      padding: const EdgeInsets.all(16),
      child: Text(title),
    );
  }

  Widget _buildAccentColorButton(BuildContext context) {
    var accentColorText = AppLocalizations.of(context).get('accent_color');
    return ScopedModelDescendant<PreferencesModel>(
      builder: (BuildContext context, Widget child, PreferencesModel model) {
        return ListTile(
          leading: Icon(OMIcons.colorLens),
          title: Text(accentColorText),
          trailing: CircleColor(color: model.accentColor, circleSize: 24),
          onTap: () async {
            final Color color = await showDialog<Color>(
              context: context,
              barrierDismissible: true,
              builder: (context) {
                return SimpleDialog(
                  title: Text(accentColorText),
                  children: <Widget>[
                    MaterialColorPicker(
                      shrinkWrap: true,
                      allowShades: false,
                      selectedColor: model.accentColor,
                      onMainColorChange: (ColorSwatch swatch) =>
                          Navigator.pop(context, Color(swatch.value)),
                    )
                  ],
                );
              },
            );
            if (color != null) model.accentColor = color;
          },
        );
      },
    );
  }

  Widget _buildThemeButton(BuildContext context) {
    String themeToString(ThemeMode themeMode) {
      String theme;
      switch (themeMode) {
        case ThemeMode.light:
          theme = 'Light';
          break;
        case ThemeMode.dark:
          theme = 'Dark';
          break;
        case ThemeMode.system:
          theme = 'System';
          break;
      }
      return theme;
    }

    return ScopedModelDescendant<PreferencesModel>(
      builder: (context, child, model) {
        return ListTile(
          leading: Icon(OMIcons.colorize),
          title: Text(AppLocalizations.of(context).get('theme')),
          onTap: () {
            dynamic popUpMenustate = _menuKey.currentState;
            popUpMenustate.showButtonMenu();
          },
          trailing: PopupMenuButton<ThemeMode>(
            key: _menuKey,
            // icon: Icon(OMIcons.arrowDropDown),
            child: Text(themeToString(model.themeMode)),
            initialValue: model.themeMode,
            onSelected: (ThemeMode theme) => model.themeMode = theme,
            itemBuilder: (context) => [
              // TODO use localized strings here
              PopupMenuItem(value: ThemeMode.light, child: Text('Light')),
              PopupMenuItem(value: ThemeMode.dark, child: Text('Dark')),
              PopupMenuItem(value: ThemeMode.system, child: Text('System')),
            ],
          ),
        );
      },
    );
  }

  Widget _buildImagesOptions(BuildContext context) {
    return Column(
      children: <Widget>[
        // data saver
        ScopedModelDescendant<PreferencesModel>(
          builder: (
            BuildContext context,
            Widget child,
            PreferencesModel model,
          ) {
            return CheckboxListTile(
              secondary: Icon(OMIcons.permDataSetting),
              title: Text(AppLocalizations.of(context).get('data_saver')),
              subtitle: Text(
                AppLocalizations.of(context).get('data_saver_explain'),
              ),
              value: model.dataSaver,
              activeColor: Theme.of(context).accentColor,
              onChanged: (bool checked) => model.dataSaver = checked,
            );
          },
        ),
        // clear cache
        ListTile(
          leading: Icon(OMIcons.clear),
          title: Text(AppLocalizations.of(context).get('clear_image_cache')),
          subtitle: Text(
            '${imageCache.currentSize.toString()} Bilder, ${_getMegabytes()}MB',
          ),
          onTap: () {
            setState(() {
              imageCache.clear();
            });
          },
        ),
      ],
    );
  }

  String _getMegabytes() {
    var megabytes = imageCache.currentSizeBytes / (1024 * 1024);
    return megabytes.toStringAsFixed(2);
  }

  Widget _buildClearFavorites(BuildContext context) {
    return ListTile(
      leading: Icon(OMIcons.favoriteBorder),
      title: Text(AppLocalizations.of(context).get('clear_favorites')),
      onTap: () async {
        await ScopedModel.of<FavoritesModel>(context).clearFavorites();
        _settingsScaffoldKey.currentState.showSnackBar(
          SnackBar(
              content:
                  Text(AppLocalizations.of(context).get('favorites_cleared'))),
        );
      },
    );
  }

  Widget _buildAbout(BuildContext context) {
    return ListTile(
      leading: Icon(OMIcons.info),
      title: Text(AppLocalizations.of(context).get('about_this_app')),
      onTap: () {
        showDialog(
          context: context,
          builder: (BuildContext context) {
            return SimpleDialog(
              titlePadding: const EdgeInsets.all(20),
              title: Text(AppLocalizations.of(context).get('about')),
              children: <Widget>[
                Padding(
                  padding: const EdgeInsets.only(left: 20, right: 20),
                  child: Column(
                    children: <Widget>[
                      Text(AppLocalizations.of(context).get('built_by')),
                      Divider(),
                      Text(AppLocalizations.of(context).get('xkcd_license')),
                      Divider(),
                      Text(AppLocalizations.of(context).get('launcher_icon')),
                      Align(
                        alignment: Alignment.bottomRight,
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: <Widget>[
                            FlatButton(
                              child: Text(AppLocalizations.of(context)
                                  .get('contributors')),
                              onPressed: () {
                                Navigator.of(context).pop();
                                Navigator.of(context)
                                    .pushNamed(ContributorsPage.pageRoute);
                              },
                            ),
                            FlatButton(
                              padding: const EdgeInsets.only(right: 0),
                              child: Text(MaterialLocalizations.of(context)
                                  .viewLicensesButtonLabel),
                              onPressed: () {
                                Navigator.of(context).push(
                                  MaterialPageRoute(
                                    builder: (BuildContext context) {
                                      return LicensePage(
                                        applicationName: 'xkcdViewer',
                                      );
                                    },
                                  ),
                                );
                              },
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                )
              ],
            );
          },
        );
      },
    );
  }
}
