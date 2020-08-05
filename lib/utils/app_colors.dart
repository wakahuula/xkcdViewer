import 'package:flutter/material.dart';

class AppColors {
  static ThemeData theme({@required Color accent, @required bool dark}) {
    final Brightness brightness = dark ? Brightness.dark : Brightness.light;
    final Color surfaceColor = dark ? const Color(0xFF121212) : Colors.white;
    return ThemeData(
      brightness: brightness,
      fontFamily: 'FiraMono',
      canvasColor: surfaceColor,
      primaryColor: surfaceColor,
      primaryColorLight: surfaceColor,
      primaryColorDark: surfaceColor,
      bottomAppBarColor: surfaceColor,
      primarySwatch: Colors.deepPurple,
      accentColor: accent,
      dividerColor: dark ? Color(0xFF2E2E31) : Colors.black12,
      appBarTheme: AppBarTheme(
        brightness: brightness,
        elevation: 0,
        color: surfaceColor,
      ),
    );
  }
}
