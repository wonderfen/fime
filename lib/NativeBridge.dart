/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

import 'package:flutter/services.dart';

var _platform;
Map<dynamic, Function> _nativeCallHandler = {};

typedef NativeCallHandler = void Function(Map<dynamic, dynamic> params);

const kSchemaImport = "kSchemaImport";
const kSchemaActive = "kSchemaActive";
const kSchemaValidate = "kSchemaValidate";
const kSchemaBuild = "kSchemaBuild";
const kSchemaDelete = "kSchemaDelete";

void _setup() {
  if (_platform == null) {
    _platform = const MethodChannel('FimeApp');
    _platform.setMethodCallHandler((call) {
      return onNativeCall(call.method, call.arguments);
    });
  }
}

Future<Map<dynamic, dynamic>?> callNative(
    String method, Map<String, dynamic>? params) async {
  _setup();
  var result = await _platform.invokeMapMethod(method, params);
  return Future.value(result);
}

Future<dynamic?> onNativeCall(String method, Map<dynamic, dynamic> params) {
  print('onNativeCall, method=$method');
  if (_nativeCallHandler.containsKey(method)) {
    var fn = _nativeCallHandler[method] as NativeCallHandler;
    fn(params);
  }
  return Future.value(true);
}

void registerHandler(String method, NativeCallHandler handler) {
  _nativeCallHandler[method] = handler;
}

void unregisterHandler(String name) {
  _nativeCallHandler.remove(name);
}
