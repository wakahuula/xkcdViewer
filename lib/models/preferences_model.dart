import 'package:flutter/material.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:xkcd/services/persistence_service.dart';
import 'package:xkcd/utils/service_locator.dart';

class PreferencesModel extends Model {
  final PersistenceService _persistenceService = sl<PersistenceService>();

  ThemeMode _themeMode;
  Color _accentColor;
  bool _dataSaver;

  ThemeMode get themeMode =>
      _themeMode ??
      ThemeMode.values[_persistenceService.getValue('theme') ?? 0];
  set themeMode(ThemeMode themeMode) {
    _themeMode = themeMode;
    notifyListeners();
    _persistenceService.setValue('theme', _themeMode.index);
  }

  Color get accentColor =>
      _accentColor ??
      Color(_persistenceService.getValue('accent') ?? Colors.deepPurple.value);

  set accentColor(Color color) {
    print('Changing accent color to ${color.toString()}');
    _accentColor = color;
    notifyListeners();
    _persistenceService.setValue('accent', _accentColor.value);
  }

  bool get dataSaver =>
      _dataSaver ?? _persistenceService.getValue('data_saver') ?? false;
  set dataSaver(bool enabled) {
    _dataSaver = enabled;
    notifyListeners();
    _persistenceService.setValue('data_saver', _dataSaver);
  }
}
