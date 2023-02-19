import 'package:fime/NativeBridge.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

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

  @override
  void initState() {
    super.initState();
    registerHandler('_onSchemaResult', _onSchemaResult);
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
        return true;
      },
      child: Scaffold(
        appBar: AppBar(
          title: const Text('方案'),
          actions: [
            IconButton(
              onPressed: importExternalSchema,
              icon: const Icon(Icons.add),
            ),
            IconButton(
              onPressed: clearBuild,
              icon: const Icon(Icons.playlist_remove),
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
          const Text('方案为空或包含无效方案!'),
          MaterialButton(
            onPressed: getSchemas,
            color: Colors.blue,
            textColor: Colors.white,
            child: const Text('刷新'),
          )
        ],
      ));
    }
    List<ListTile> listTiles = [];
    for (var item in schemas) {
      listTiles.add(ListTile(
        leading: active == item['conf']
            ? const Icon(
                Icons.check,
                color: Colors.blue,
              )
            : const Opacity(opacity: 1),
        minLeadingWidth: 24,
        title: Text(item['name']),
        subtitle: Text(item['conf']),
        trailing: SizedBox(
          width: 72,
          child: Row(
            children: [
              item['precompiled']
                  ? const Icon(Icons.beenhere, color: Colors.green)
                  : const Icon(Icons.beenhere),
              PopupMenuButton<String>(
                icon: const Icon(Icons.menu, color: Colors.blue),
                itemBuilder: (context) {
                  return [
                    const PopupMenuItem(
                        value: 'setActiveSchema', child: Text('设为默认')),
                    const PopupMenuItem(value: 'validate', child: Text('验证')),
                    const PopupMenuItem(value: 'build', child: Text('生成')),
                    const PopupMenuItem(value: 'delete', child: Text('删除')),
                  ];
                },
                onSelected: (which) {
                  print(which);
                  try {
                    if ('setActiveSchema' == which) {
                      setActiveSchema(item);
                    } else if ('validate' == which) {
                      validateSchema(item);
                    } else if ('build' == which) {
                      buildSchema(item);
                    } else if ('delete' == which) {
                      deleteSchema(item);
                    }
                  } catch (e) {
                    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                      content: Text('错误：${e}!'),
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

  void clearBuild() {
    showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: const Text('提示'),
            content: const Text('确认删除已生成的文件吗，删除后将影响输入法的正常使用！'),
            actions: [
              TextButton(
                child: const Text('删除'),
                onPressed: () {
                  callNative('clearBuild', {}).then((value) {
                    if (value!['success'] ?? false) {
                      Navigator.of(context).pop();
                      showSnackBar(const Text('操作已完成!'));
                      setState(() {
                        schemas.clear();
                      });
                    }
                  });
                },
              ),
              TextButton(
                child: const Text('取消'),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              )
            ],
          );
        });
  }

  void importExternalSchema() {
    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
      content: Text('导入新方案.'),
      duration: Duration(milliseconds: 500),
    ));
    callNative('importExternalSchema', {});
  }

  void _onSchemaResult(Map<dynamic, dynamic> params) {
    if (mounted && params.isNotEmpty) {
      if (params[kSchemaImport] ?? false) {
        showSnackBar(const Text('方案文件导入成功!'));
        getSchemas();
      }
    }
  }

  void setActiveSchema(item) {
    showSnackBar(Text('正在设置默认方案：${item["name"]}！'));
    callNative('setActiveSchema', item).then((value) {
      if (value!['success'] ?? false) {
        setState(() {
          active = item['conf'];
        });
      }
    });
  }

  void validateSchema(item) {
    showSnackBar(Text('正在验证方案：${item["name"]}！'));
    callNative('validateSchema', item).then((value) {
      if (value!['success'] ?? false) {
        showSnackBar(const Text('方案验证通过!'));
      } else {
        showSnackBar(const Text('操作失败！'));
      }
    });
  }

  void buildSchema(item) {
    showSnackBar(Text('正在生成方案：${item["name"]}！'));
    callNative('buildSchema', item).then((value) {
      if (value!['success'] ?? false) {
        showSnackBar(const Text('方案生成成功!'));
        setState(() {
          item['precompiled'] = true;
        });
      } else {
        showSnackBar(const Text('操作失败！'));
      }
    });
  }

  void deleteSchema(item) {
    if (schemas.length == 1) {
      showSnackBar(const Text('只有一个方案，不能删除！'));
      return;
    }
    showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: const Text('提示'),
            content: Text('确认删除方案：「${item['name']}」吗，该操作不可恢复！'),
            actions: [
              TextButton(
                child: const Text('删除'),
                onPressed: () {
                  Navigator.of(context).pop();
                  callNative('deleteSchema', item).then((value) {
                    if (value!['success'] ?? false) {
                      showSnackBar(const Text('方案已删除!'));
                      setState(() {
                        schemas.remove(item);
                      });
                    }
                  });
                },
              ),
              TextButton(
                child: const Text('取消'),
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
