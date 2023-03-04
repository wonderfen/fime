import 'package:fime/AppLocalizations.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class PagePlugins extends StatefulWidget {
  static const String ROUTER_NAME = "/plugins";

  const PagePlugins({super.key});

  @override
  State<StatefulWidget> createState() {
    return _PagePluginsState();
  }
}

class _PagePluginsState extends State<PagePlugins> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('${AppLocalizations.of(context).i18n('plugins')}'),
      ),
      body: Center(
        child: Text('${AppLocalizations.of(context).i18n('not-available')}'),
      ),
    );
  }
}
