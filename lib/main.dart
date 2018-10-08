import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:xkcd/blocs/comic_bloc.dart';
import 'package:xkcd/providers/comic_bloc_provider.dart';
import 'package:xkcd/pages/home_page.dart';
import 'package:xkcd/pages/settings_page.dart';
import 'package:xkcd/providers/preferences.dart';

void main() async {
  Preferences.prefs = await SharedPreferences.getInstance();

  runApp(new MyApp());
}

class MyApp extends StatelessWidget {
  final _pageRoutes = <String, WidgetBuilder>{
    HomePage.homePageRoute: (context) => HomePage(),
    SettingsPage.settingsPageRoute: (context) => SettingsPage(),
  };

  final bloc = ComicBloc();

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      title: 'XKCD viewer',
      theme: ThemeData(
        // https://www.materialpalette.com/
        canvasColor: Colors.white,
        primaryColor: Color(0xFF546e7a),
        primaryColorLight: Color(0xFF819ca9),
        primaryColorDark: Color(0xFF29434e),
        accentColor: Color(0xFF7c4dff),
      ),
      routes: _pageRoutes,
      home: ComicBlocProvider(
        bloc: bloc,
        child: HomePage(),
      ),
    );
  }
}
