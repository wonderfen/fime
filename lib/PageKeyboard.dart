/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

import 'package:fime/NativeBridge.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class PageKeyboard extends StatefulWidget {
  static const String ROUTER_NAME = "/keyboard";

  const PageKeyboard({super.key});

  @override
  State<StatefulWidget> createState() {
    return _PageKeyboardState();
  }
}

class _PageKeyboardState<PageKeyboard> extends State {
  late String active;
  List<dynamic> groups = [];

  @override
  void initState() {
    super.initState();
    callNative('getKeyboardGroupsKnown', {}).then((data) {
      setState(() {
        active = data!['active'];
        for (var item in data!['groups']) {
          groups.add(item);
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        callNative('setDefaultKeyboardGroup', {'conf': active});
        return true;
      },
      child: Scaffold(
        appBar: AppBar(
          title: const Text('键盘'),
          actions: [
            IconButton(
                onPressed: () {
                  addKeyboardGroup(context);
                },
                icon: const Icon(Icons.add))
          ],
        ),
        body: keyboardGroupList(),
      ),
    );
  }

  Widget keyboardGroupList() {
    if (groups.isEmpty) {
      return const Text('正在加载...');
    }
    List<ListTile> listTiles = [];
    for (var item in groups) {
      listTiles.add(ListTile(
        leading: active == item['file']
            ? const Icon(
                Icons.check,
                color: Colors.blue,
              )
            : const Opacity(opacity: 1),
        title: Text(item['name']),
        subtitle: Text(item['names'].join(',')),
        trailing: IconButton(
          onPressed: () {
            deleteKeyboardGroup(item['file']);
          },
          icon: const Icon(Icons.delete_forever, size: 36),
          color: Colors.red,
        ),
        onTap: () {
          activeKeyboardGroup(item['file']);
        },
      ));
    }
    return ListView(
      children:
          ListTile.divideTiles(tiles: listTiles, color: Colors.grey).toList(),
    );
  }

  void activeKeyboardGroup(name) {
    setState(() {
      active = name;
    });
  }

  void addKeyboardGroup(BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
      content: Text('导入自定义键盘.'),
      duration: Duration(milliseconds: 500),
    ));
  }

  void deleteKeyboardGroup(String file) async {
    callNative('deleteKeyboardGroup', {'conf': file}).then((_) {
      setState(() {
        groups.removeWhere((item) => item['file'] == file);
      });
    });
  }
}
