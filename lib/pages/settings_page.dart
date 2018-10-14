import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:package_info/package_info.dart';
import 'package:xkcd/providers/preferences.dart';
import 'package:xkcd/utils/app_localizations.dart';
import 'package:xkcd/utils/constants.dart';

class SettingsPage extends StatefulWidget {
  static final String settingsPageRoute = '/settings-page';

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

  _buildTitleWidget(String title) {
    return Container(
      padding: EdgeInsets.all(15.0),
      child: Text(title),
    );
  }

  _buildImagesOptions(BuildContext context) {
    return Column(
      children: <Widget>[
        ListTile(
          leading: Icon(Icons.perm_data_setting),
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
      ],
    );
  }

  _buildClearFavorites(BuildContext context) {
    return ListTile(
      leading: Icon(Icons.favorite_border),
      title: Text(AppLocalizations.of(context).get('clear_favorites')),
      onTap: _clearFavorites,
    );
  }

  _clearFavorites() {
    final SharedPreferences prefs = Preferences.prefs;
    final List<String> favorites = prefs.getStringList(Constants.favorites);
    if (favorites != null && favorites.isNotEmpty) {
      prefs.remove(Constants.favorites);
    }
  }

  _buildAbout(BuildContext context) {
    return ListTile(
      leading: Icon(Icons.info_outline),
      title: Text(AppLocalizations.of(context).get('about_this_app')),
      onTap: () {
        _showAboutDialog();
      },
    );
  }

  _showAboutDialog() {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AboutDialog(
          applicationName: _packageInfo.appName,
          applicationVersion: _packageInfo.version,
          applicationLegalese: AppLocalizations.of(context).get('built_by'),
        );
      },
    );
  }
}
