import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:xkcd/models/comic_model.dart';
import 'package:xkcd/models/favorites_model.dart';
import 'package:xkcd/models/preferences_model.dart';
import 'package:xkcd/pages/contributors_page.dart';
import 'package:xkcd/pages/favorites_page.dart';
import 'package:xkcd/pages/home_page.dart';
import 'package:xkcd/pages/settings_page.dart';
import 'package:xkcd/utils/app_colors.dart';
import 'package:xkcd/utils/app_localizations_delegate.dart';
import 'package:xkcd/utils/service_locator.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await registerServices();
  runApp(XkcdViewer());
}

class XkcdViewer extends StatefulWidget {
  @override
  _XkcdViewerState createState() => _XkcdViewerState();
}

class _XkcdViewerState extends State<XkcdViewer> {
  final ComicModel comicModel = ComicModel();
  final FavoritesModel _favoritesModel = FavoritesModel();
  final PreferencesModel _preferencesModel = PreferencesModel();

  final _pageRoutes = <String, WidgetBuilder>{
    HomePage.pageRoute: (context) => HomePage(),
    SettingsPage.pageRoute: (context) => SettingsPage(),
    FavoritesPage.pageRoute: (context) => FavoritesPage(),
    ContributorsPage.pageRoute: (context) => ContributorsPage(),
  };

  static const List<Locale> _supportedLocales = [
    Locale('en', 'US'),
    Locale('de', 'DE'),
    Locale('pt', 'BR'),
    Locale('es', 'ES'),
    Locale('ru', 'RU'),
    Locale('uk', 'UA'),
    Locale('fr', 'FR'),
  ];

  static const List<LocalizationsDelegate> _localizationsDelegates = [
    AppLocalizationsDelegate(),
    GlobalMaterialLocalizations.delegate,
    GlobalMaterialLocalizations.delegate,
  ];

  @override
  void initState() {
    super.initState();
    comicModel.loadFirstComic();
  }

  Locale localeResolutionCallback(
    Locale locale,
    Iterable<Locale> supportedLocales,
  ) {
    for (Locale supportedLocale in supportedLocales) {
      if (supportedLocale.languageCode == locale.languageCode ||
          supportedLocale.countryCode == locale.countryCode) {
        return supportedLocale;
      }
    }
    return supportedLocales.first;
  }

  @override
  Widget build(BuildContext context) {
    return ScopedModel(
      model: _preferencesModel,
      child: ScopedModelDescendant<PreferencesModel>(
        builder: (BuildContext context, Widget widget, PreferencesModel model) {
          return MaterialApp(
            title: 'xkcdViewer',
            debugShowCheckedModeBanner: false,
            theme: AppColors.theme(accent: model.accentColor, dark: false),
            darkTheme: AppColors.theme(accent: model.accentColor, dark: true),
            themeMode: model.themeMode,
            supportedLocales: _supportedLocales,
            localizationsDelegates: _localizationsDelegates,
            localeResolutionCallback: localeResolutionCallback,
            routes: _pageRoutes,
            home: HomePage(),
            builder: (BuildContext context, Widget child) {
              // switchSystemChromeTheme(Theme.of(context).brightness);
              // Scoped Models inside the material app as they don't affect the
              // app's configuration unlike the PreferencesModel
              return ScopedModel(
                model: comicModel,
                child: ScopedModel(
                  model: _favoritesModel,
                  child: child,
                ),
              );
            },
          );
        },
      ),
    );
  }

  // TODO fix status bar background color issue
  void switchSystemChromeTheme(Brightness brightness) {
    print('switchSystemChromeTheme $brightness');
    if (brightness == Brightness.dark) {
      SystemChrome.setSystemUIOverlayStyle(
        const SystemUiOverlayStyle(
          systemNavigationBarColor: Color(0xFF040405),
          statusBarColor: Color(0xFF040405),
          statusBarBrightness: Brightness.dark,
          systemNavigationBarIconBrightness: Brightness.light,
          statusBarIconBrightness: Brightness.light,
        ),
      );
    } else {
      SystemChrome.setSystemUIOverlayStyle(
        const SystemUiOverlayStyle(
          systemNavigationBarColor: Colors.white,
          statusBarColor: Colors.white,
          statusBarBrightness: Brightness.light,
          systemNavigationBarIconBrightness: Brightness.dark,
          statusBarIconBrightness: Brightness.dark,
        ),
      );
    }
  }
}
