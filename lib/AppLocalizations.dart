import 'dart:convert';

import 'package:fime/NativeBridge.dart';
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

class I18nWidget extends StatefulWidget {
  final child;

  I18nWidget({super.key, required this.child});

  @override
  State<StatefulWidget> createState() {
    return I18nWidgetState();
  }
}

class I18nWidgetState extends State<I18nWidget> {
  var _locale;

  get locale => _locale;

  @override
  void initState() {
    super.initState();
    registerHandler('setLanguage', _setLanguage);
  }

  void _setLanguage(Map<dynamic, dynamic> params) {
    var language = params!['language'];
    _changeLanguage(language);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    callNative('getLanguage', {}).then((data) {
      var language = data!['language'];
      _changeLanguage(language);
    });
  }

  void _changeLanguage(String language) {
    var l;
    if ('zh-Hant' == language) {
      l = Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hant');
    } else if ('zh-Hans' == language) {
      l = Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hans');
    } else if ('en' == language) {
      l = Locale('en');
    }
    if (l != null) changeLanguage(l);
  }

  void changeLanguage(Locale locale) {
    setState(() {
      _locale = locale;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Localizations.override(
      context: context,
      locale: _locale,
      child: widget.child,
    );
  }
}

GlobalKey<I18nWidgetState> i18nKey = GlobalKey<I18nWidgetState>();
