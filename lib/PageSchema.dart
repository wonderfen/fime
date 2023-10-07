/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

import 'package:fime/AppLocalizations.dart';
import 'package:fime/NativeBridge.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';

class PageSchema extends StatefulWidget {
  static const String ROUTER_NAME = "/schema";

  const PageSchema({super.key});

  @override
  State<StatefulWidget> createState() {
    return _PageSchemaState();
  }
}

class _PageSchemaState<PageSchema> extends State {
  String active = '';
  List<Map<String, dynamic>> schemas = [];
  bool busy = false;

  @override
  void initState() {
    super.initState();
    registerHandler('_onSchemaResult', _onSchemaResult);
    registerHandler('_onSchemaBuildResult', _onSchemaBuildResult);
    EasyLoading.dismiss();
    getSchemas();
  }

  void getSchemas() {
    callNative('getSchemas', {}).then((data) {
      setState(() {
        active = data!['active'];
        schemas.clear();
        for (var item in data!['schemas']) {
          var conf = item['conf'];
          var name = item['name'];
          var precompiled = item['precompiled'];
          schemas.add({'conf': conf, 'name': name, 'precompiled': precompiled});
        }
      });
    });
  }

  @override
  void dispose() {
    super.dispose();
    unregisterHandler('_onSchemaResult');
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        return !busy;
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(AppLocalizations.of(context).i18n('schema')),
          actions: [
            IconButton(
              onPressed: importExternalSchema,
              icon: const Icon(Icons.add),
            ),
            IconButton(
              onPressed: buildAllSchema,
              icon: const Icon(Icons.construction),
            ),
          ],
        ),
        body: schemaList(),
      ),
    );
  }

  Widget schemaList() {
    if (schemas.isEmpty) {
      return Center(
          child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(AppLocalizations.of(context).i18n('empty-or-invalid-schema')),
          MaterialButton(
            onPressed: getSchemas,
            color: Colors.blue,
            textColor: Colors.white,
            child: Text(AppLocalizations.of(context).i18n('refresh')),
          )
        ],
      ));
    }
    List<ListTile> listTiles = [];
    for (var item in schemas) {
      listTiles.add(ListTile(
        leading: active == item['conf']
            ? const Padding(
                padding: EdgeInsets.symmetric(vertical: 6.0),
                child: Icon(
                  Icons.check,
                  color: Colors.blue,
                ),
              )
            : const Opacity(opacity: 1),
        minLeadingWidth: 24,
        title: Text(item['name']),
        subtitle: Text(item['conf']),
        trailing: SizedBox(
          width: 48,
          child: Row(
            children: [
              PopupMenuButton<String>(
                icon: const Icon(Icons.menu, color: Colors.blue),
                itemBuilder: (context) {
                  return [
                    PopupMenuItem(
                        value: 'setActiveSchema',
                        child: Text(AppLocalizations.of(context)
                            .i18n('set-as-default-schema'))),
                    // PopupMenuItem(
                    //     value: 'validate',
                    //     child: Text(
                    //         AppLocalizations.of(context).i18n('validate-schema'))),
                    // PopupMenuItem(
                    //     value: 'build',
                    //     child: Text(
                    //         AppLocalizations.of(context).i18n('generate-schema'))),
                    // PopupMenuItem(
                    //     value: 'delete',
                    //     child: Text(AppLocalizations.of(context)
                    //         .i18n('delete-schema'))),
                    PopupMenuItem(
                        value: 'selectKeyboard',
                        child: Text(AppLocalizations.of(context)
                            .i18n('selectKeyboard'))),
                  ];
                },
                onSelected: (which) {
                  try {
                    if ('setActiveSchema' == which) {
                      setActiveSchema(item);
                    } else if ('validate' == which) {
                      validateSchema(item);
                    } else if ('build' == which) {
                      buildSchema(item);
                    } else if ('delete' == which) {
                      deleteSchema(item);
                    } else if ('selectKeyboard' == which) {
                      showSnackBar(const Text('Not implements!'));
                    }
                  } catch (e) {
                    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                      content: Text(
                          '${AppLocalizations.of(context).i18n('error')}：$e!'),
                      duration: const Duration(milliseconds: 500),
                    ));
                  }
                },
              ),
            ],
          ),
        ),
      ));
    }
    return ListView(
      children:
          ListTile.divideTiles(tiles: listTiles, color: Colors.grey).toList(),
    );
  }

  void refresh() {
    schemas.clear();
    getSchemas();
  }

  void clearBuild() {
    showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text(AppLocalizations.of(context).i18n('warn')),
            content:
                Text(AppLocalizations.of(context).i18n('warn-clear-build')),
            actions: [
              TextButton(
                child: Text(AppLocalizations.of(context).i18n('delete')),
                onPressed: () {
                  callNative('clearBuild', {}).then((value) {
                    if (value!['success'] ?? false) {
                      Navigator.of(context).pop();
                      showSnackBar(
                          Text(AppLocalizations.of(context).i18n('done')));
                      setState(() {
                        schemas.clear();
                      });
                    }
                  });
                },
              ),
              TextButton(
                child: Text(AppLocalizations.of(context).i18n('cancel')),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              )
            ],
          );
        });
  }

  void importExternalSchema() {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(AppLocalizations.of(context).i18n('import-new-schema')),
      duration: Duration(milliseconds: 500),
    ));
    callNative('importExternalSchema', {});
  }

  void buildAllSchema() {
    busy = true;
    EasyLoading.show(
      status: '${AppLocalizations.of(context).i18n('schema-is-building')}...',
      maskType: EasyLoadingMaskType.custom,
      dismissOnTap: false,
    );
    callNative('buildAllSchema', {}).then((value) {
      if (value!['success'] ?? false) {
        // do nothing!
      } else {
        showSnackBar(
            Text('${AppLocalizations.of(context).i18n('operation-failed')}！'));
      }
    });
  }

  void _onSchemaResult(Map<dynamic, dynamic> params) {
    if (mounted && params.isNotEmpty) {
      if (params[kSchemaImport] ?? false) {
        showSnackBar(
            Text(AppLocalizations.of(context).i18n('schema-import-succeed')));
        getSchemas();
      }
    }
  }

  void _onSchemaBuildResult(Map<dynamic, dynamic> params) {
    EasyLoading.dismiss();
    busy = false;
    if (mounted && params.isNotEmpty) {
      if (params['success'] ?? false) {
        getSchemas();
        EasyLoading.showSuccess(
            AppLocalizations.of(context).i18n('schema-build-ok'),
            maskType: EasyLoadingMaskType.custom,
            duration: const Duration(milliseconds: 800),
            dismissOnTap: true);
      } else {
        EasyLoading.showError(
            AppLocalizations.of(context).i18n('operation-failed'),
            maskType: EasyLoadingMaskType.custom,
            duration: const Duration(milliseconds: 1800),
            dismissOnTap: true);
      }
    }
  }

  void setActiveSchema(item) {
    if (!item['precompiled']) {
      showDialog(
          context: context,
          builder: (context) {
            return AlertDialog(
              title: Text(AppLocalizations.of(context).i18n('warn')),
              content:
                  Text(AppLocalizations.of(context).i18n('schema-not-build')),
              actions: [
                TextButton(
                  child: Text(AppLocalizations.of(context).i18n('ok')),
                  onPressed: () {
                    Navigator.of(context).pop();
                  },
                ),
              ],
            );
          });
      return;
    }
    showSnackBar(Text(
        '${AppLocalizations.of(context).i18n('set-default-schema-working')}：${item["name"]}！'));
    callNative('setActiveSchema', item).then((value) {
      if (value!['success'] ?? false) {
        setState(() {
          active = item['conf'];
        });
      } else {
        showSnackBar(
            Text(AppLocalizations.of(context).i18n('operation-failed')));
      }
    });
  }

  void validateSchema(item) {
    showSnackBar(Text(
        '${AppLocalizations.of(context).i18n('validate-schema-working')}：${item["name"]}！'));
    callNative('validateSchema', item).then((value) {
      if (value!['success'] ?? false) {
        showSnackBar(
            Text(AppLocalizations.of(context).i18n('validate-schema-ok')));
      } else {
        showSnackBar(
            Text(AppLocalizations.of(context).i18n('operation-failed')));
      }
    });
  }

  void buildSchema(item) {
    // show loading
    busy = true;
    EasyLoading.show(
      status:
          '${AppLocalizations.of(context).i18n('schema-is-building')}：${item["name"]}...',
      maskType: EasyLoadingMaskType.custom,
      dismissOnTap: false,
    );
    callNative('buildSchema', item).then((value) {
      if (value!['success'] ?? false) {
        showSnackBar(Text(
            '${AppLocalizations.of(context).i18n('schema-is-building')}：${item["name"]}！'));
      } else {
        showSnackBar(
            Text(AppLocalizations.of(context).i18n('operation-failed')));
      }
    });
  }

  void deleteSchema(item) {
    if (schemas.length == 1) {
      showSnackBar(Text(
          AppLocalizations.of(context).i18n('unique-schema-can-not-delete')));
      return;
    }
    showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text(AppLocalizations.of(context).i18n('warn')),
            content: Text(
                AppLocalizations.of(context).i18n('confirm-delete-schema')),
            actions: [
              TextButton(
                child: Text(AppLocalizations.of(context).i18n('delete')),
                onPressed: () {
                  Navigator.of(context).pop();
                  callNative('deleteSchema', item).then((value) {
                    if (value!['success'] ?? false) {
                      showSnackBar(Text(
                          AppLocalizations.of(context).i18n('schema-deleted')));
                      setState(() {
                        schemas.remove(item);
                      });
                    }
                  });
                },
              ),
              TextButton(
                child: Text(AppLocalizations.of(context).i18n('cancel')),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              )
            ],
          );
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
