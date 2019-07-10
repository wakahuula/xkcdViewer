import 'package:flutter/material.dart';
import 'package:xkcd/utils/preferences.dart';

class AppColors {
  static const Color backgroundColor = Color(0xFFECEFF1);

  static Color getBottomSeparatorColor(BuildContext context) =>
      Theme.of(context).brightness == Brightness.light ? Colors.black12 : Color(0xFF2E2E31);

  static Color getAccentColor(BuildContext context) =>
      Color(Preferences.prefs.getInt('accentColor') ?? Theme.of(context).accentColor.value);

  static ThemeData getDarkTheme(BuildContext context) {
    print(Preferences.prefs.getInt('accentColor'));
    return ThemeData(
      brightness: Brightness.dark,
      fontFamily: 'FiraMono',
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: <TargetPlatform, PageTransitionsBuilder>{
          TargetPlatform.iOS: const CupertinoPageTransitionsBuilder(),
          TargetPlatform.android: const OpenUpwardsPageTransitionsBuilder(),
        },
      ),
      canvasColor: const Color(0xFF040405),
      primaryColor: const Color(0xFF040405),
      primaryColorLight: const Color(0xFF040405),
      primaryColorDark: const Color(0xFF040405),
      bottomAppBarColor: const Color(0xFF040405),
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
