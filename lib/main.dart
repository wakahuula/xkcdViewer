import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:xkcd/models/comic_model.dart';
import 'package:xkcd/pages/contributors_page.dart';
import 'package:xkcd/pages/favorites_page.dart';
import 'package:xkcd/pages/home_page.dart';
import 'package:xkcd/pages/settings_page.dart';
import 'package:xkcd/utils/app_localizations_delegate.dart';
import 'package:xkcd/utils/preferences.dart';

void main() async {
  Preferences.prefs = await SharedPreferences.getInstance();
  SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle(
    systemNavigationBarColor: Color(0xFF3e3e3e),
  ));

  runApp(XkcdViewer());
}

class XkcdViewer extends StatefulWidget {
  @override
  _XkcdViewerState createState() => _XkcdViewerState();
}

class _XkcdViewerState extends State<XkcdViewer> {
  final ComicModel model = ComicModel();

  final _pageRoutes = <String, WidgetBuilder>{
    HomePage.pageRoute: (context) => HomePage(),
    SettingsPage.pageRoute: (context) => SettingsPage(),
    FavoritesPage.pageRoute: (context) => FavoritesPage(),
    ContributorsPage.pageRoute: (context) => ContributorsPage(),
  };

  @override
  void initState() {
    super.initState();
    model.loadFirstComic();
  }

  @override
  Widget build(BuildContext context) {
    return ScopedModel<ComicModel>(
      model: model,
      child: MaterialApp(
        title: 'xkcdViewer',
        debugShowCheckedModeBanner: false,
        supportedLocales: [
          const Locale('en', 'US'),
          const Locale('de', 'DE'),
          const Locale('pt', 'BR'),
          const Locale('es', 'ES'),
          const Locale('ru', 'RU'),
        ],
        localizationsDelegates: [
          const AppLocalizationsDelegate(),
          GlobalMaterialLocalizations.delegate,
          GlobalMaterialLocalizations.delegate
        ],
        localeResolutionCallback: (Locale locale, Iterable<Locale> supportedLocales) {
          for (Locale supportedLocale in supportedLocales) {
            if (supportedLocale.languageCode == locale.languageCode ||
                supportedLocale.countryCode == locale.countryCode) {
              return supportedLocale;
            }
          }
          return supportedLocales.first;
        },
        theme: ThemeData(
          fontFamily: 'FiraMono',
          pageTransitionsTheme: const PageTransitionsTheme(
            builders: <TargetPlatform, PageTransitionsBuilder>{
              TargetPlatform.iOS: CupertinoPageTransitionsBuilder(),
              TargetPlatform.android: OpenUpwardsPageTransitionsBuilder(),
            },
          ),
          // https://www.materialpalette.com/
          canvasColor: Colors.white,
          primaryColor: Color(0xFF3e3e3e),
          primaryColorLight: Color(0xFF3e3e3e),
          primaryColorDark: Color(0xFF3e3e3e),
          accentColor: Color(0xFFd0d0d0),
        ),
        routes: _pageRoutes,
        home: HomePage(),
      ),
    );
  }
}
