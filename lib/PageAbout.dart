import 'package:fime/AppLocalizations.dart';
import 'package:fime/NativeBridge.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class PageAbout extends StatelessWidget {
  static const String ROUTER_NAME = "/about";

  const PageAbout({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('${AppLocalizations.of(context).i18n('about')}'),
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            ListTile(
              title: Text('${AppLocalizations.of(context).i18n('version')}'),
              subtitle: versionInfo(),
              trailing: OutlinedButton(
                style: OutlinedButton.styleFrom(
                  side: BorderSide(color: Colors.blue),
                ),
                child: Text(
                    '${AppLocalizations.of(context).i18n('check-update')}'),
                onPressed: null,
              ),
            ),
            ListTile(
              title: Text(
                  '${AppLocalizations.of(context).i18n('privacy-policy')}'),
              subtitle: Text(
                  '${AppLocalizations.of(context).i18n('privacy-policy-info')}'),
            ),
            _ThirdPartLibrariesWidget(),
            ListTile(
              title:
                  Text('${AppLocalizations.of(context).i18n('source-code')}'),
              subtitle: Text('https://gitee.com/zelde/fime'),
            ),
            ListTile(
              title: Text('${AppLocalizations.of(context).i18n('license')}'),
              subtitle: Text('MIT'),
            ),
            ListTile(
              title: Text(
                  '${AppLocalizations.of(context).i18n('feedback-and-discuss')}'),
              subtitle:
                  Text('${AppLocalizations.of(context).i18n('qq-group-info')}'),
            ),
          ],
        ),
      ),
    );
  }

  Widget versionInfo() {
    return FutureBuilder(
        future: callNative('versionInfo', {}),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.done &&
              snapshot.hasData) {
            Map<String, dynamic>? versionInfo = {};
            snapshot.data?.forEach((key, value) {
              versionInfo[key] = value;
            });
            return Text(
                '${AppLocalizations.of(context).i18n('fime')} ${versionInfo["versionName"]}');
          }
          return Text(
              '${AppLocalizations.of(context).i18n('fetching-version-info')}');
        });
  }
}

class _ThirdPartLibrariesWidget extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _ThirdPartLibrariesWidgetState();
  }
}

class _ThirdPartLibrariesWidgetState extends State<_ThirdPartLibrariesWidget> {
  var expanded = false;

  @override
  Widget build(BuildContext context) {
    return ExpansionPanelList(
      expansionCallback: ((panelIndex, isExpanded) {
        setState(() => expanded = !isExpanded);
      }),
      children: [
        ExpansionPanel(
            canTapOnHeader: true,
            isExpanded: expanded,
            headerBuilder: (context, isExpanded) {
              return ListTile(
                title: Text(
                    '${AppLocalizations.of(context).i18n('third-party-libraries')}'),
              );
            },
            body: Column(
              children: const [
                ListTile(
                  title: Text('Flutter'),
                  subtitle: Text('https://github.com/flutter/flutter'),
                ),
                ListTile(
                  title: Text('Flutter Markdown'),
                  subtitle: Text(
                      'https://pub.flutter-io.cn/packages/flutter_markdown'),
                ),
                ListTile(
                  title: Text('HOCON'),
                  subtitle: Text('https://github.com/lightbend/config'),
                ),
                ListTile(
                  title: Text('fastutil'),
                  subtitle: Text('https://github.com/vigna/fastutil'),
                ),
                ListTile(
                  title: Text('trie4j'),
                  subtitle: Text('https://github.com/takawitter/trie4j'),
                ),
                ListTile(
                  title: Text('timber'),
                  subtitle: Text('https://github.com/JakeWharton/timber'),
                ),
                ListTile(
                  title: Text('hessian'),
                  subtitle: Text('http://hessian.caucho.com/'),
                ),
                ListTile(
                  title: Text('h2database'),
                  subtitle: Text('https://github.com/h2database/h2database'),
                ),
              ],
            )),
      ],
    );
  }
}
