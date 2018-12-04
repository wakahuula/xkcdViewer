import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class AppLocalizations {
  AppLocalizations(this.locale);

  final Locale locale;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  Map<String, String> _sentences;

  Future<bool> load() async {
    String data;
    try {
      data = await rootBundle
          .loadString('assets/locale/${this.locale.languageCode}-${this.locale.countryCode}.json');
    } catch (e) {
      data = await rootBundle.loadString('assets/locale/en-US.json');
    }
    Map<String, dynamic> _result = json.decode(data);

    this._sentences = Map();
    _result.forEach((String key, dynamic value) {
      this._sentences[key] = value.toString();
    });

    return true;
  }

  String get(String key) {
    return this._sentences[key];
  }
}
