import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter/material.dart';
import 'package:xkcd/providers/preferences.dart';

class SettingsPage extends StatefulWidget {
  static final String settingsPageRoute = '/settings-page';

  @override
  State<StatefulWidget> createState() => SettingsPageState();
}

class SettingsPageState extends State<SettingsPage> {
  final SharedPreferences prefs = Preferences.prefs;
  bool _leftHanded = false;

  @override
  void initState() {
    super.initState();
    _loadValues();
  }

  _loadValues() async {
    setState(() {
      _leftHanded = (prefs.getBool('leftHanded') ?? false);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Settings'),
      ),
      body: ListView(
        children: <Widget>[
          _buildTitleWidget('UI'),
          _buildOrientationTile(context),
          _buildTitleWidget('Favorites'),
          _buildClearFavorites(context),
        ],
      ),
    );
  }

  _buildTitleWidget(String title) {
    var themeData = Theme.of(context);
    return Container(
      padding: EdgeInsets.all(15.0),
      child: Text(
        title,
        style: themeData.textTheme.body1.copyWith(color: themeData.accentColor),
      ),
    );
  }

  _buildOrientationTile(BuildContext context) {
    return ListTile(
      leading: Icon(
        Icons.format_align_left,
        color: Theme.of(context).accentColor,
      ),
      title: Text('Left handed mode'),
      trailing: Checkbox(value: _leftHanded, onChanged: _saveLeftHanded),
    );
  }

  _saveLeftHanded(bool leftHanded) async {
    setState(() {
      _leftHanded = !_leftHanded;
      prefs.setBool('leftHanded', _leftHanded);
    });
  }

  _buildClearFavorites(BuildContext context) {
    return ListTile(
      leading: Icon(
        Icons.favorite_border,
        color: Theme.of(context).accentColor,
      ),
      title: Text('Clear favorites'),
      onTap: _clearFavorites,
    );
  }

  _clearFavorites() {
    final SharedPreferences prefs = Preferences.prefs;
    final List<String> favorites = prefs.getStringList('favorites');
    if (favorites == null && favorites.isNotEmpty) {
      prefs.remove('favorites');
    }
  }
}
