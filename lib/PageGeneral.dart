import 'package:fime/AppLocalizations.dart';
import 'package:fime/NativeBridge.dart';
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
  String theme = 'by-keyboard';
  bool clipboardEnable = true;
  Set<int> expandedIndex = <int>{};

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
              keyboardPanel(),
              themePanel(),
              clipboardPanel(),
            ],
          ),
        ),
      ),
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
              value: vibrate,
              activeColor: Colors.blue,
              onChanged: (bool on) {
                setState(() => vibrate = on);
              },
            ),
            dense: true,
          ),
          ListTile(
            title: Text(
                '${AppLocalizations.of(context).i18n('check-long-press')}'),
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
            title: Text('${AppLocalizations.of(context).i18n('check-swipe')}'),
            trailing: Switch(
              value: playKeySound,
              activeColor: Colors.blue,
              onChanged: (bool on) {
                setState(() => playKeySound = on);
              },
            ),
            dense: true,
          ),
        ],
      ),
      isExpanded: expandedIndex.contains(0),
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
      isExpanded: expandedIndex.contains(1),
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
              value: playKeySound,
              activeColor: Colors.blue,
              onChanged: (bool on) {
                setState(() => playKeySound = on);
              },
            ),
            dense: true,
          ),
          ListTile(
            title:
                Text('${AppLocalizations.of(context).i18n('clean-clipboard')}'),
            trailing: IconButton(
              icon: const Icon(Icons.delete_forever),
              onPressed: () {},
            ),
            dense: true,
          ),
        ],
      ),
      isExpanded: expandedIndex.contains(2),
    );
  }
}
