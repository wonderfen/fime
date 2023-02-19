import 'dart:ui';

import 'package:fime/NativeBridge.dart';
import 'package:fime/PageAbout.dart';
import 'package:fime/PageEffect.dart';
import 'package:fime/PageHelp.dart';
import 'package:fime/PageKeyboard.dart';
import 'package:fime/PageSchema.dart';
import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';

void main() {
  runApp(const FimeApp());
}

class FimeApp extends StatelessWidget {
  const FimeApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Fime',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const HomePage(title: 'Fime'),
      routes: _routes(),
    );
  }

  Map<String, WidgetBuilder> _routes() {
    return {
      PageSchema.ROUTER_NAME: (context) => const PageSchema(),
      PageKeyboard.ROUTER_NAME: (context) => const PageKeyboard(),
      PageEffect.ROUTER_NAME: (context) => const PageEffect(),
      PageHelp.ROUTER_NAME: (context) => const PageHelp(),
      PageAbout.ROUTER_NAME: (context) => const PageAbout(),
    };
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.title});

  final String title;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  bool testing = false;
  late FocusNode focusNode; // 输入框的焦点控制

  @override
  void initState() {
    super.initState();
    testing = false;
    focusNode = FocusNode();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
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
                  '设置',
                  style: Theme.of(context).textTheme.headline6,
                ),
                Row(
                  children: [
                    Expanded(
                      child:
                          makeCard(const Icon(Icons.book_outlined), '方案', () {
                        Navigator.pushNamed(context, PageSchema.ROUTER_NAME);
                      }),
                    ),
                    Expanded(
                      child: makeCard(
                          const Icon(Icons.touch_app_outlined), '音效和触感', () {
                        Navigator.pushNamed(context, PageEffect.ROUTER_NAME);
                      }),
                    ),
                  ],
                ),
                Row(
                  children: [
                    Expanded(
                      child: makeCard(const Icon(Icons.help_outline), '帮助', () {
                        Navigator.pushNamed(context, PageHelp.ROUTER_NAME);
                      }),
                    ),
                    Expanded(
                      child: makeCard(const Icon(Icons.info_outline), '关于', () {
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
                    const Expanded(child: Text('输入测试')),
                    MaterialButton(
                      child: Text('切换到 Fime'),
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
                    decoration: const InputDecoration(
                      hintText: '打几个字试试吧!',
                      contentPadding: EdgeInsets.only(left: 4.0),
                    ),
                  ),
                ),
              ],
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
