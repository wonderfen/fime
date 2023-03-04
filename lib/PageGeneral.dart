import 'package:fime/AppLocalizations.dart';
import 'package:fime/NativeBridge.dart';
import 'package:fime/PagePlugins.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

const _kPlayKeySound = 'play-key-sound';
const _kVibrate = 'vibrate';
const _kTheme = 'system';

class PageGeneral extends StatefulWidget {
  static const String ROUTER_NAME = "/general";

  const PageGeneral({super.key});

  @override
  State<StatefulWidget> createState() {
    return _PageGeneralState();
  }
}

class _PageGeneralState extends State<PageGeneral> {
  bool playKeySound = true;
  bool vibrate = false;
  String theme = 'schema';
  bool clipboardEnable = true;

  @override
  void initState() {
    super.initState();
    callNative('getEffects', {}).then((data) {
      setState(() {
        playKeySound = data![_kPlayKeySound];
        vibrate = data![_kVibrate];
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        callNative(
            'setEffects', {_kPlayKeySound: playKeySound, _kVibrate: vibrate});
        return true;
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text('${AppLocalizations.of(context).i18n('general')}'),
        ),
        body: Center(
          child: Container(
            padding: const EdgeInsets.all(8.0),
            child: ListView(
              children: [
                ListTile(
                  title:
                      Text('${AppLocalizations.of(context).i18n('key-sound')}'),
                  trailing: Switch(
                    value: playKeySound,
                    activeColor: Colors.blue,
                    onChanged: (bool on) {
                      setState(() => playKeySound = on);
                    },
                  ),
                  contentPadding: EdgeInsets.symmetric(horizontal: 8.0),
                ),
                ListTile(
                  title: Text(
                      '${AppLocalizations.of(context).i18n('key-vibrate')}'),
                  trailing: Switch(
                    value: vibrate,
                    activeColor: Colors.blue,
                    onChanged: (bool on) {
                      setState(() => vibrate = on);
                    },
                  ),
                  contentPadding: EdgeInsets.symmetric(horizontal: 8.0),
                ),
                ListTile(
                  title: Text('${AppLocalizations.of(context).i18n('theme')}'),
                  subtitle: Text('theme-descn'),
                  trailing: SizedBox(
                    width: 48,
                    child: PopupMenuButton<String>(
                      icon: const Icon(Icons.arrow_drop_down),
                      itemBuilder: (context) {
                        return [
                          PopupMenuItem(
                              value: 'system',
                              child: Text(
                                  '${AppLocalizations.of(context).i18n('theme-system')}')),
                          PopupMenuItem(
                              value: 'schema',
                              child: Text(
                                  '${AppLocalizations.of(context).i18n('theme-schema')}')),
                          PopupMenuItem(
                              value: 'light',
                              child: Text(
                                  '${AppLocalizations.of(context).i18n('theme-light')}')),
                          PopupMenuItem(
                              value: 'dark',
                              child: Text(
                                  '${AppLocalizations.of(context).i18n('theme-dark')}')),
                        ];
                      },
                      onSelected: (which) {
                        print(which);
                        try {
                          if ('system' == which) {
                            // setActiveSchema(item);
                          } else if ('schema' == which) {
                            // validateSchema(item);
                          } else if ('light' == which) {
                            // buildSchema(item);
                          } else if ('dark' == which) {
                            // deleteSchema(item);
                          }
                        } catch (e) {
                          ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                            content: Text(
                                '${AppLocalizations.of(context).i18n('error')}：$e!'),
                            duration: const Duration(milliseconds: 500),
                          ));
                        }
                      },
                    ),
                  ),
                  contentPadding: EdgeInsets.symmetric(horizontal: 8.0),
                ),
                ListTile(
                  title: Text(
                      '${AppLocalizations.of(context).i18n('clipboard-enable')}'),
                  trailing: Switch(
                    value: clipboardEnable,
                    activeColor: Colors.blue,
                    onChanged: (bool on) {
                      setState(() => clipboardEnable = on);
                    },
                  ),
                  contentPadding: EdgeInsets.symmetric(horizontal: 8.0),
                ),
                ListTile(
                  title: Text('clean-clipboard'),
                  trailing: IconButton(
                    icon: const Icon(Icons.delete_forever),
                    onPressed: () {},
                  ),
                  contentPadding: EdgeInsets.symmetric(horizontal: 8.0),
                ),
                ListTile(
                  title:
                      Text('${AppLocalizations.of(context).i18n('plugins')}'),
                  subtitle: Text(
                      '${AppLocalizations.of(context).i18n('require-restart')}'),
                  onTap: () {
                    Navigator.pushNamed(context, PagePlugins.ROUTER_NAME);
                  },
                  contentPadding: EdgeInsets.symmetric(horizontal: 8.0),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}
