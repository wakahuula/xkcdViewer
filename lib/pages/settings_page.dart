import 'package:flutter/material.dart';
import 'package:outline_material_icons/outline_material_icons.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:package_info/package_info.dart';
import 'package:xkcd/pages/contributors_page.dart';
import 'package:xkcd/utils/preferences.dart';
import 'package:xkcd/utils/app_localizations.dart';
import 'package:xkcd/utils/constants.dart';

class SettingsPage extends StatefulWidget {
  static final String pageRoute = '/settings-page';

  @override
  State<StatefulWidget> createState() => SettingsPageState();
}

class SettingsPageState extends State<SettingsPage> {
  final SharedPreferences _prefs = Preferences.prefs;
  PackageInfo _packageInfo;

  @override
  void initState() {
    super.initState();
    _loadValues();
  }

  _loadValues() async {
    _packageInfo = await PackageInfo.fromPlatform();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: Text(AppLocalizations.of(context).get('settings')),
        elevation: 0.0,
      ),
      body: Container(
        padding: EdgeInsets.all(10.0),
        child: ListView(
          children: <Widget>[
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
      padding: EdgeInsets.all(15.0),
      child: Text(title),
    );
  }

  Widget _buildImagesOptions(BuildContext context) {
    return Column(
      children: <Widget>[
        // data saver
        ListTile(
          leading: Icon(OMIcons.permDataSetting),
          title: Text(AppLocalizations.of(context).get('data_saver')),
          subtitle: Text(AppLocalizations.of(context).get('data_saver_explain')),
          trailing: Checkbox(
            tristate: false,
            value: _prefs.getBool('data_saver') ?? false,
            onChanged: (checked) {
              setState(() {
                _prefs.setBool('data_saver', checked);
              });
            },
          ),
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
      onTap: () {
        final SharedPreferences prefs = Preferences.prefs;
        final List<String> favorites = prefs.getStringList(Constants.favorites);
        if (favorites != null && favorites.isNotEmpty) {
          prefs.remove(Constants.favorites);
        }
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
              titlePadding: EdgeInsets.all(20.0),
              title: Text(AppLocalizations.of(context).get('about')),
              children: <Widget>[
                Padding(
                  padding: const EdgeInsets.only(left: 20.0, right: 20.0),
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
                              child: Text(AppLocalizations.of(context).get('contributors')),
                              onPressed: () {
                                Navigator.of(context).pop();
                                Navigator.of(context).pushNamed(ContributorsPage.pageRoute);
                              },
                            ),
                            FlatButton(
                              padding: EdgeInsets.only(right: 0.0),
                              child:
                                  Text(MaterialLocalizations.of(context).viewLicensesButtonLabel),
                              onPressed: () {
                                Navigator.of(context).push(
                                  MaterialPageRoute(
                                    builder: (BuildContext context) {
                                      return LicensePage(
                                        applicationName: _packageInfo.appName,
                                        applicationVersion:
                                            _packageInfo.version + '.' + _packageInfo.buildNumber,
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
