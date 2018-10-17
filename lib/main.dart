import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:xkcd/blocs/comic_bloc.dart';
import 'package:xkcd/pages/favorites_page.dart';
import 'package:xkcd/providers/comic_bloc_provider.dart';
import 'package:xkcd/pages/home_page.dart';
import 'package:xkcd/pages/settings_page.dart';
import 'package:xkcd/providers/preferences.dart';
import 'package:xkcd/utils/app_localizations_delegate.dart';

void main() async {
  Preferences.prefs = await SharedPreferences.getInstance();

  runApp(new MyApp());
}

class MyApp extends StatelessWidget {
  final _pageRoutes = <String, WidgetBuilder>{
    HomePage.homePageRoute: (context) => HomePage(),
    SettingsPage.settingsPageRoute: (context) => SettingsPage(),
    FavoritesPage.favoritesPageRoute: (context) => FavoritesPage(),
  };

  final bloc = ComicBloc();

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      title: 'xkcdViewer',
      debugShowCheckedModeBanner: false,
      supportedLocales: [
        const Locale('en', 'US'),
        const Locale('de', 'DE'),
        const Locale('pt', 'BR'),
        const Locale('es', 'ES'),
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
      home: ComicBlocProvider(
        bloc: bloc,
        child: HomePage(),
      ),
    );
  }
}
