import 'package:fime/NativeBridge.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

const _kPlayKeySound = 'play-key-sound';
const _kVibrate = 'vibrate';

class PageEffect extends StatefulWidget {
  static const String ROUTER_NAME = "/effect";

  const PageEffect({super.key});

  @override
  State<StatefulWidget> createState() {
    return _PageEffectState();
  }
}

class _PageEffectState<PageEffect> extends State {
  bool playKeySound = true;
  bool vibrate = false;

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
          title: const Text('音效和触感'),
        ),
        body: Center(
          child: Container(
            padding: const EdgeInsets.all(8.0),
            child: Column(
              children: [
                Row(
                  children: [
                    Switch(
                      value: playKeySound,
                      activeColor: Colors.blue,
                      onChanged: (bool on) {
                        setState(() => playKeySound = on);
                      },
                    ),
                    const Expanded(child: Text('按键音')),
                  ],
                ),
                Row(
                  children: [
                    Switch(
                      value: vibrate,
                      activeColor: Colors.blue,
                      onChanged: (bool on) {
                        setState(() => vibrate = on);
                      },
                    ),
                    const Expanded(child: Text('按键时震动')),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
