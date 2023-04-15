/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

import 'package:fime/AppLocalizations.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class PageMaintenance extends StatefulWidget {
  static const String ROUTER_NAME = "/maintenance";

  const PageMaintenance({super.key});

  @override
  State<StatefulWidget> createState() {
    return _PageMaintenanceState();
  }
}

class _PageMaintenanceState extends State<PageMaintenance> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('${AppLocalizations.of(context).i18n('maintenance')}'),
      ),
      body: Center(
        child: Text('${AppLocalizations.of(context).i18n('not-available')}'),
      ),
    );
  }
}
