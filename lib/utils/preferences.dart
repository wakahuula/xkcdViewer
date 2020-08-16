import 'package:shared_preferences/shared_preferences.dart';

class Preferences {
  static final Preferences _preferences = Preferences._internal();

  static SharedPreferences prefs;

  factory Preferences() {
    return _preferences;
  }

  Preferences._internal();
}
