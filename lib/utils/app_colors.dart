import 'package:flutter/material.dart';

class AppColors {
  static ThemeData theme({@required Color accent, @required bool dark}) {
    final Brightness brightness = dark ? Brightness.dark : Brightness.light;
    return ThemeData(
      brightness: brightness,
      fontFamily: 'FiraMono',
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: <TargetPlatform, PageTransitionsBuilder>{
          TargetPlatform.iOS: CupertinoPageTransitionsBuilder(),
          TargetPlatform.android: OpenUpwardsPageTransitionsBuilder(),
        },
      ),
      canvasColor: dark ? const Color(0xFF121212) : Colors.white,
      primaryColor: dark ? const Color(0xFF121212) : Colors.white,
      primaryColorLight: dark ? const Color(0xFF121212) : Colors.white,
      primaryColorDark: dark ? const Color(0xFF121212) : Colors.white,
      bottomAppBarColor: dark ? const Color(0xFF121212) : Colors.white,
      primarySwatch: Colors.deepPurple,
      accentColor: accent,
      dividerColor: dark ? Color(0xFF2E2E31) : Colors.black12,
      appBarTheme: AppBarTheme(brightness: brightness, elevation: 0),
    );
  }
}
