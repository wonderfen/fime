/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

import 'package:fime/AppLocalizations.dart';
import 'package:fime/NativeBridge.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

const String kKeyboardPlayKeySound = "keyboard.play-key-sound";
const String kKeyboardKeyVibrate = "keyboard.key-vibrate";
const String kKeyboardCheckLongPress = "keyboard.check-long-press";
const String kKeyboardCheckSwipe = "keyboard.check-swipe";
const String kLanguage = "language";
const String kTheme = "theme";
const String kClipboardEnabled = "clipboard.enabled";

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
  bool keyVibrate = false;
  bool checkLongPress = true;
  bool checkSwipe = true;
  String language = 'zh-Hans';
  String theme = 'by-keyboard';

  bool clipboardEnable = true;

  Set<int> expandedIndex = <int>{};

  @override
  void initState() {
    super.initState();
    callNative('getLanguage', {}).then((data) {
      setState(() {
        language = data![kLanguage];
      });
      callNative('getKeyboardSetting', {}).then((data) {
        setState(() {
          playKeySound = data![kKeyboardPlayKeySound];
          keyVibrate = data![kKeyboardKeyVibrate];
          checkLongPress = data[kKeyboardCheckLongPress];
          checkSwipe = data[kKeyboardCheckSwipe];
        });
      });
      return callNative('getThemeSetting', {});
    }).then((data) {
      setState(() {
        theme = data![kTheme];
      });
      return callNative('getClipboardSetting', {});
    }).then((data) {
      setState(() {
        clipboardEnable = data![kClipboardEnabled];
      });
    }).catchError((err) {
      showDialog(
          context: context,
          builder: (context) {
            return AlertDialog(
              title: Text('${AppLocalizations.of(context).i18n('error')}'),
              content: Text(
                  '${AppLocalizations.of(context).i18n('get-setting-failed')}: ${err.toString()}'),
              actions: [
                TextButton(
                  child: Text('${AppLocalizations.of(context).i18n('OK')}'),
                  onPressed: () {
                    Navigator.of(context).pop();
                  },
                )
              ],
            );
          });
    });
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        if ('zh-Hant' == language) {
          i18nKey.currentState!.changeLanguage(
              const Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hant'));
        } else if ('zh-Hans' == language) {
          i18nKey.currentState!.changeLanguage(
              const Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hans'));
        } else if ('en' == language) {
          i18nKey.currentState!.changeLanguage(const Locale('en'));
        }
        callNative('setLanguage', {kLanguage: language}).whenComplete(() {
          callNative('setKeyboardSetting', {
            kKeyboardPlayKeySound: playKeySound,
            kKeyboardKeyVibrate: keyVibrate,
            kKeyboardCheckLongPress: checkLongPress,
            kKeyboardCheckSwipe: checkSwipe,
          });
        }).whenComplete(() {
          callNative('setThemeSetting', {kTheme: theme});
        }).whenComplete(() {
          callNative(
              'setClipboardSetting', {kClipboardEnabled: clipboardEnable});
        }).catchError((err) {
          showDialog(
              context: context,
              builder: (context) {
                return AlertDialog(
                  title: Text('${AppLocalizations.of(context).i18n('error')}'),
                  content: Text(
                      '${AppLocalizations.of(context).i18n('set-setting-failed')}: ${err.toString()}'),
                  actions: [
                    TextButton(
                      child: Text('${AppLocalizations.of(context).i18n('OK')}'),
                      onPressed: () {
                        Navigator.of(context).pop();
                      },
                    )
                  ],
                );
              });
        });
        return true;
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text('${AppLocalizations.of(context).i18n('general')}'),
        ),
        body: SingleChildScrollView(
          child: ExpansionPanelList(
            expansionCallback: ((panelIndex, isExpanded) {
              setState(() {
                if (isExpanded) {
                  expandedIndex.remove(panelIndex);
                } else {
                  expandedIndex.add(panelIndex);
                }
              });
            }),
            children: [
              languagePanel(),
              keyboardPanel(),
              themePanel(),
              clipboardPanel(),
            ],
          ),
        ),
      ),
    );
  }

  ExpansionPanel languagePanel() {
    return ExpansionPanel(
      canTapOnHeader: true,
      headerBuilder: (context, isExpanded) {
        return ListTile(
          title: Text('${AppLocalizations.of(context).i18n('language')}'),
          subtitle: Text('${AppLocalizations.of(context).i18n(language)}'),
          contentPadding: EdgeInsets.symmetric(horizontal: 8.0),
        );
      },
      body: Column(
        children: [
          ListTile(
            title: Text('${AppLocalizations.of(context).i18n('zh-Hans')}'),
            trailing: Radio(
              groupValue: language,
              value: 'zh-Hans',
              activeColor: Colors.blue,
              onChanged: ((value) {
                updateLanguage(value);
              }),
            ),
            dense: true,
            onTap: () {
              updateLanguage('zh-Hans');
            },
          ),
          ListTile(
            title: Text('${AppLocalizations.of(context).i18n('zh-Hant')}'),
            trailing: Radio(
              groupValue: language,
              value: 'zh-Hant',
              activeColor: Colors.blue,
              onChanged: ((value) {
                updateLanguage(value);
              }),
            ),
            dense: true,
            onTap: () {
              updateLanguage('zh-Hant');
            },
          ),
          ListTile(
            title: Text('${AppLocalizations.of(context).i18n('en')}'),
            trailing: Radio(
              groupValue: language,
              value: 'en',
              activeColor: Colors.blue,
              onChanged: ((value) {
                updateLanguage(value);
              }),
            ),
            dense: true,
            onTap: () {
              updateLanguage('en');
              // setState(() => language = 'en');
            },
          ),
        ],
      ),
      isExpanded: expandedIndex.contains(0),
    );
  }

  ExpansionPanel keyboardPanel() {
    return ExpansionPanel(
      canTapOnHeader: true,
      headerBuilder: (context, isExpanded) {
        return ListTile(
          title: Text('${AppLocalizations.of(context).i18n('keyboard')}'),
          contentPadding: EdgeInsets.symmetric(horizontal: 8.0),
        );
      },
      body: Column(
        children: [
          ListTile(
            title: Text('${AppLocalizations.of(context).i18n('key-sound')}'),
            trailing: Switch(
              value: playKeySound,
              activeColor: Colors.blue,
              onChanged: (bool on) {
                setState(() => playKeySound = on);
              },
            ),
            dense: true,
          ),
          ListTile(
            title: Text('${AppLocalizations.of(context).i18n('key-vibrate')}'),
            trailing: Switch(
              value: keyVibrate,
              activeColor: Colors.blue,
              onChanged: (bool on) {
                setState(() => keyVibrate = on);
              },
            ),
            dense: true,
          ),
          ListTile(
            title: Text(
                '${AppLocalizations.of(context).i18n('check-long-press')}'),
            trailing: Switch(
              value: checkLongPress,
              activeColor: Colors.blue,
              onChanged: (bool on) {
                setState(() => checkLongPress = on);
              },
            ),
            dense: true,
          ),
          ListTile(
            title: Text('${AppLocalizations.of(context).i18n('check-swipe')}'),
            trailing: Switch(
              value: checkSwipe,
              activeColor: Colors.blue,
              onChanged: (bool on) {
                setState(() => checkSwipe = on);
              },
            ),
            dense: true,
          ),
        ],
      ),
      isExpanded: expandedIndex.contains(1),
    );
  }

  ExpansionPanel themePanel() {
    return ExpansionPanel(
      canTapOnHeader: true,
      headerBuilder: (context, isExpanded) {
        return ListTile(
          title: Text('${AppLocalizations.of(context).i18n('theme')}'),
          subtitle: Text('${AppLocalizations.of(context).i18n(theme)}'),
          contentPadding: EdgeInsets.symmetric(horizontal: 8.0),
        );
      },
      body: Column(
        children: [
          ListTile(
            title: Text('${AppLocalizations.of(context).i18n('by-system')}'),
            trailing: Radio(
              groupValue: theme,
              value: 'by-system',
              activeColor: Colors.blue,
              onChanged: ((value) {
                setState(() => theme = value ?? '');
              }),
            ),
            dense: true,
            onTap: () {
              setState(() => theme = 'by-system');
            },
          ),
          ListTile(
            title: Text('${AppLocalizations.of(context).i18n('by-keyboard')}'),
            trailing: Radio(
              groupValue: theme,
              value: 'by-keyboard',
              activeColor: Colors.blue,
              onChanged: ((value) {
                setState(() => theme = value ?? '');
              }),
            ),
            dense: true,
            onTap: () {
              setState(() => theme = 'by-schema');
            },
          ),
          ListTile(
            title: Text('${AppLocalizations.of(context).i18n('light')}'),
            trailing: Radio(
              groupValue: theme,
              value: 'light',
              activeColor: Colors.blue,
              onChanged: ((value) {
                setState(() => theme = value ?? '');
              }),
            ),
            dense: true,
            onTap: () {
              setState(() => theme = 'light');
            },
          ),
          ListTile(
            title: Text('${AppLocalizations.of(context).i18n('dark')}'),
            trailing: Radio(
              groupValue: theme,
              value: 'dark',
              activeColor: Colors.blue,
              onChanged: ((value) {
                setState(() => theme = value ?? '');
              }),
            ),
            dense: true,
            onTap: () {
              setState(() => theme = 'dark');
            },
          ),
        ],
      ),
      isExpanded: expandedIndex.contains(2),
    );
  }

  ExpansionPanel clipboardPanel() {
    return ExpansionPanel(
      canTapOnHeader: true,
      headerBuilder: (context, isExpanded) {
        return ListTile(
          title: Text('${AppLocalizations.of(context).i18n('clipboard')}'),
          contentPadding: EdgeInsets.symmetric(horizontal: 8.0),
        );
      },
      body: Column(
        children: [
          ListTile(
            title: Text(
                '${AppLocalizations.of(context).i18n('clipboard-enabled')}'),
            trailing: Switch(
              value: clipboardEnable,
              activeColor: Colors.blue,
              onChanged: (bool on) {
                setState(() => clipboardEnable = on);
              },
            ),
            dense: true,
          ),
          ListTile(
            title:
                Text('${AppLocalizations.of(context).i18n('clean-clipboard')}'),
            trailing: IconButton(
              icon: const Icon(
                Icons.delete_forever,
                color: Colors.deepOrange,
              ),
              onPressed: () {
                callNative('cleanClipboard', {}).then((data) {
                  showSnackBar(Text(
                      '${AppLocalizations.of(context).i18n('clipboard-is-clean')}!'));
                });
              },
            ),
            dense: true,
          ),
        ],
      ),
      isExpanded: expandedIndex.contains(3),
    );
  }

  void updateLanguage(newLanguage) {
    if (language == newLanguage) return;
    setState(() {
      language = newLanguage ?? '';
    });
  }

  void showSnackBar(Widget content) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: content,
        duration: const Duration(milliseconds: 500),
      ));
    }
  }
}
