import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class AppLocalizations {
  final Locale locale;

  AppLocalizations(this.locale);

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static Map<String, String> _i18nValues = {};

  static Future<AppLocalizations> load(Locale locale) {
    // zh-Hans/zh-Hant ...
    final String name = [locale.languageCode, locale.scriptCode ?? '']
        .join('-')
        .replaceAll(RegExp(r'[-]+$'), '');
    return rootBundle.loadString('fassets/i18n-$name.json').then((jsonString) {
      Map<String, dynamic> map = json.decode(jsonString);
      _i18nValues = map.map((key, value) => MapEntry(key, value));
      return AppLocalizations(locale);
    });
  }

  static List<String> languages() => _i18nValues.keys.toList();

  String get title {
    return i18n('title');
  }

  String i18n(String key) {
    return _i18nValues[key] ?? key; // Null safe 的语法!!
  }
}

class AppLocalizationsDelegate extends LocalizationsDelegate<AppLocalizations> {
  @override
  bool isSupported(Locale locale) {
    return ['en', 'zh'].contains(locale.languageCode);
  }

  @override
  Future<AppLocalizations> load(Locale locale) {
    return AppLocalizations.load(locale);
  }

  @override
  bool shouldReload(covariant LocalizationsDelegate<AppLocalizations> old) {
    return false;
  }
}
