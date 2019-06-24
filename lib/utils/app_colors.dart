import 'package:flutter/material.dart';

class AppColors {
  static const Color backgroundColor = Color(0xFFECEFF1);

  static ThemeData get darkTheme => ThemeData(
        canvasColor: const Color(0xFF3e3e3e),
        brightness: Brightness.dark,
        fontFamily: 'FiraMono',
        pageTransitionsTheme: const PageTransitionsTheme(
          builders: <TargetPlatform, PageTransitionsBuilder>{
            TargetPlatform.iOS: const CupertinoPageTransitionsBuilder(),
            TargetPlatform.android: const OpenUpwardsPageTransitionsBuilder(),
          },
        ),
        primaryColor: const Color(0xFF3e3e3e),
        primaryColorLight: const Color(0xFF3e3e3e),
        primaryColorDark: const Color(0xFF3e3e3e),
        primarySwatch: Colors.deepPurple,
        accentColor: const Color(0xFFd0d0d0),
      );

  static ThemeData get lightTheme => ThemeData(
        canvasColor: Colors.white,
        brightness: Brightness.light,
        fontFamily: 'FiraMono',
        pageTransitionsTheme: const PageTransitionsTheme(
          builders: <TargetPlatform, PageTransitionsBuilder>{
            TargetPlatform.iOS: const CupertinoPageTransitionsBuilder(),
            TargetPlatform.android: const OpenUpwardsPageTransitionsBuilder(),
          },
        ),
        primaryColor: Colors.white,
        primaryColorLight: Colors.white,
        primaryColorDark: Colors.white,
        primarySwatch: Colors.deepPurple,
        accentColor: const Color(0xFFd0d0d0),
      );
}
