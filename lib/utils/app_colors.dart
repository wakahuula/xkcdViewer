import 'package:flutter/material.dart';
import 'package:xkcd/services/persistence_service.dart';
import 'package:xkcd/utils/service_locator.dart';

class AppColors {
  static const Color backgroundColor = Color(0xFFECEFF1);

  static Color getBottomSeparatorColor(BuildContext context) =>
      Theme.of(context).brightness == Brightness.light
          ? Colors.black12
          : Color(0xFF2E2E31);

  static Color getAccentColor(BuildContext context) {
    final PersistenceService prefs = sl<PersistenceService>();
    return Color(
      prefs.getValue('accentColor') ?? Theme.of(context).accentColor.value,
    );
  }

  static ThemeData getDarkTheme(BuildContext context) {
    final PersistenceService prefs = sl<PersistenceService>();
    print(prefs.getValue('accentColor'));
    return ThemeData(
      brightness: Brightness.dark,
      fontFamily: 'FiraMono',
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: <TargetPlatform, PageTransitionsBuilder>{
          TargetPlatform.iOS: const CupertinoPageTransitionsBuilder(),
          TargetPlatform.android: const OpenUpwardsPageTransitionsBuilder(),
        },
      ),
      canvasColor: const Color(0xFF121212),
      primaryColor: const Color(0xFF121212),
      primaryColorLight: const Color(0xFF121212),
      primaryColorDark: const Color(0xFF121212),
      bottomAppBarColor: const Color(0xFF121212),
      primarySwatch: Colors.deepPurple,
      accentColor: getAccentColor(context),
    );
  }

  static ThemeData getLightTheme(BuildContext context) {
    return ThemeData(
      brightness: Brightness.light,
      fontFamily: 'FiraMono',
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: <TargetPlatform, PageTransitionsBuilder>{
          TargetPlatform.iOS: const CupertinoPageTransitionsBuilder(),
          TargetPlatform.android: const OpenUpwardsPageTransitionsBuilder(),
        },
      ),
      canvasColor: Colors.white,
      primaryColor: Colors.white,
      primaryColorLight: Colors.white,
      primaryColorDark: Colors.white,
      bottomAppBarColor: Colors.white,
      primarySwatch: Colors.deepPurple,
      accentColor: getAccentColor(context),
    );
  }
}
