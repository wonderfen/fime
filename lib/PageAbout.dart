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
      body: Container(
        padding: EdgeInsets.all(8.0),
        child: Center(
          child: Column(
            children: [
              const Image(
                image: AssetImage('fassets/fime.png'),
                width: 200,
              ),
              versionInfo(),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                      '${AppLocalizations.of(context).i18n('feedback-and-discuss')}'),
                  Text('${AppLocalizations.of(context).i18n('qq-group-info')}'),
                ],
              )
            ],
          ),
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
            return SizedBox(
              height: 32,
              width: 200,
              child: Center(
                child: Text('Fime ${versionInfo["versionName"]}'),
              ),
            );
          }
          return SizedBox(
              height: 32,
              child: Text(
                  '${AppLocalizations.of(context).i18n('fetching-version-info')}'));
        });
  }
}
