import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_markdown/flutter_markdown.dart';

class PageHelp extends StatelessWidget {
  static const String ROUTER_NAME = "/help";

  const PageHelp({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('帮助'),
      ),
      body: ListView(
        children: const [
          FlutterMarkdown(),
        ],
      ),
    );
  }
}

RegExp externalLink = RegExp(r'[a-z]+://');

class FlutterMarkdown extends StatefulWidget {
  const FlutterMarkdown({super.key});

  @override
  State<StatefulWidget> createState() {
    return _FlutterMarkdownState();
  }
}

class _FlutterMarkdownState extends State<FlutterMarkdown> {
  var document;

  @override
  void initState() {
    super.initState();
    document = 'fassets/index.md';
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: rootBundle.loadString(document),
      builder: (BuildContext context, AsyncSnapshot snapshot) {
        if (snapshot.hasData) {
          return Markdown(
            data: snapshot.data,
            physics: const NeverScrollableScrollPhysics(),
            shrinkWrap: true,
            imageBuilder: (Uri uri, String? title, String? alt) {
              return Image(image: AssetImage('fassets/${uri.path}'));
            },
            onTapLink: (String text, String? href, String title) {
              if (href!.startsWith(externalLink)) {
                ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
                  content: Text('不支持打开外部链接！'),
                  duration: Duration(milliseconds: 500),
                ));
              } else if (href!.endsWith('.md')) {
                setState(() {
                  document = 'fassets/$href';
                });
              }
            },
          );
        }
        return const Center(
          child: Text("加载中..."),
        );
      },
    );
  }
}
