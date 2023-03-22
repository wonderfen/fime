import 'dart:ui';

import 'package:fime/AppLocalizations.dart';
import 'package:fime/NativeBridge.dart';
import 'package:fime/PageAbout.dart';
import 'package:fime/PageGeneral.dart';
import 'package:fime/PageHelp.dart';
import 'package:fime/PageKeyboard.dart';
import 'package:fime/PageMaintenance.dart';
import 'package:fime/PagePlugins.dart';
import 'package:fime/PageSchema.dart';
import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

void main() {
  runApp(const FimeApp());
}

class FimeApp extends StatelessWidget {
  const FimeApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      localizationsDelegates: [
        AppLocalizationsDelegate(),
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate
      ],
      supportedLocales: const [
        Locale('en'),
        // 简体中文
        Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hans'),
        // generic traditional Chinese
        Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hant'),
      ],
      onGenerateTitle: (context) => AppLocalizations.of(context).title,
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: I18nWidget(key: i18nKey, child: const HomePage()),
      routes: _routes(),
    );
  }

  Map<String, WidgetBuilder> _routes() {
    return {
      PageGeneral.ROUTER_NAME: (context) => const PageGeneral(),
      PageSchema.ROUTER_NAME: (context) => const PageSchema(),
      PageKeyboard.ROUTER_NAME: (context) => const PageKeyboard(),
      PageHelp.ROUTER_NAME: (context) => const PageHelp(),
      PageAbout.ROUTER_NAME: (context) => const PageAbout(),
      PagePlugins.ROUTER_NAME: (context) => const PagePlugins(),
      PageMaintenance.ROUTER_NAME: (context) => const PageMaintenance(),
    };
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  bool testing = false;
  late FocusNode focusNode; // 输入框的焦点控制
  late DateTime backPressedTime;

  @override
  void initState() {
    super.initState();
    testing = false;
    focusNode = FocusNode();
    backPressedTime = DateTime.now().add(Duration(minutes: -1));
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        var now = DateTime.now();
        if (now.difference(backPressedTime) < Duration(milliseconds: 1200)) {
          return true;
        }
        backPressedTime = now;
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text(AppLocalizations.of(context).i18n('double-tap-exit')),
          duration: const Duration(milliseconds: 500),
        ));
        return false;
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(AppLocalizations.of(context).title),
        ),
        body: SingleChildScrollView(
          scrollDirection: Axis.vertical,
          child: Center(
            child: Container(
              padding: const EdgeInsets.all(8),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.start,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    AppLocalizations.of(context).i18n('setting'),
                    style: Theme.of(context).textTheme.headline6,
                  ),
                  Row(
                    children: [
                      Expanded(
                        child: makeCard(
                            const Icon(Icons.room_preferences_outlined),
                            AppLocalizations.of(context).i18n('general'), () {
                          Navigator.pushNamed(context, PageGeneral.ROUTER_NAME);
                        }),
                      ),
                      Expanded(
                        child: makeCard(const Icon(Icons.book_outlined),
                            AppLocalizations.of(context).i18n('schema'), () {
                          Navigator.pushNamed(context, PageSchema.ROUTER_NAME);
                        }),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      Expanded(
                        child: makeCard(
                            const Icon(Icons.extension_outlined),
                            '${AppLocalizations.of(context).i18n('plugins')}',
                            null
                            // () {
                            //   Navigator.pushNamed(
                            //       context, PagePlugins.ROUTER_NAME);
                            // },
                            ),
                      ),
                      Expanded(
                        child: makeCard(
                            const Icon(Icons.build_circle_outlined),
                            '${AppLocalizations.of(context).i18n('maintenance')}',
                            null
                            // () {
                            //   Navigator.pushNamed(
                            //       context, PageMaintenance.ROUTER_NAME);
                            // },
                            ),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      Expanded(
                        child: makeCard(const Icon(Icons.help_outline),
                            AppLocalizations.of(context).i18n('help'), () {
                          Navigator.pushNamed(context, PageHelp.ROUTER_NAME);
                        }),
                      ),
                      Expanded(
                        child: makeCard(const Icon(Icons.info_outline),
                            AppLocalizations.of(context).i18n('about'), () {
                          Navigator.pushNamed(context, PageAbout.ROUTER_NAME);
                        }),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      Switch(
                        value: testing,
                        activeColor: Colors.blue,
                        onChanged: (bool value) {
                          if (value) {
                            focusNode.requestFocus();
                          } else {
                            focusNode.unfocus();
                          }
                          setState(() {
                            testing = value;
                          });
                        },
                      ),
                      Expanded(
                          child: Text(
                              AppLocalizations.of(context).i18n('input-test'))),
                      MaterialButton(
                        child: Text(AppLocalizations.of(context)
                            .i18n('switch-to-fime')),
                        onPressed: () {
                          callNative('switchToFime', null);
                        },
                        color: Colors.blue,
                        textColor: Colors.white,
                      ),
                    ],
                  ),
                  Offstage(
                    offstage: !testing,
                    child: TextField(
                      maxLines: 1,
                      focusNode: focusNode,
                      decoration: InputDecoration(
                        hintText: AppLocalizations.of(context)
                            .i18n('type-some-chars'),
                        contentPadding: EdgeInsets.only(left: 4.0),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget makeCard(Icon icon, String label, Function? onTap) {
    var card = SizedBox(
      height: 80,
      child: Card(
        clipBehavior: Clip.hardEdge,
        child: InkWell(
          splashColor: Colors.blue.withAlpha(30),
          onTap: onTap == null ? null : () => onTap(),
          child: Stack(
            children: [
              Positioned(
                left: 8,
                top: 8,
                child: icon,
              ),
              Positioned(
                left: 8,
                top: 42,
                child: Text(label),
              ),
              const Positioned(
                right: 8,
                top: 42,
                child: Icon(Icons.keyboard_arrow_right),
              ),
            ],
          ),
        ),
      ),
    );
    if (onTap == null) {
      return ShaderMask(
        shaderCallback: (Rect bounds) {
          return const LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            tileMode: TileMode.repeated,
            colors: <Color>[Colors.white38, Colors.white24, Colors.white12],
          ).createShader(bounds);
        },
        child: card,
      );
    }
    return card;
  }
}
