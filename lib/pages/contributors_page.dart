import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:xkcd/data/contributors.dart';
import 'package:xkcd/utils/app_localizations.dart';

class ContributorsPage extends StatelessWidget {
  static final String pageRoute = '/contributors-page';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        titleSpacing: 0,
        elevation: 0,
        title: Text(AppLocalizations.of(context).get('contributors_title')),
      ),
      body: FutureBuilder(
        future: _getContributors(),
        builder: (context, snapshot) {
          if (snapshot == null || !snapshot.hasData) {
            return Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(
              child: Text(
                AppLocalizations.of(context).get('something_wrong'),
              ),
            );
          }

          List<Widget> widgets = [];
          for (Contributor contributor in snapshot.data.contributors) {
            widgets.add(ListTile(
              title: Text(contributor.name),
              subtitle: Text(contributor.profile),
              onTap: () {
                launch(contributor.profile, forceWebView: true);
              },
            ));
          }
          return ListView.builder(
            itemCount: widgets.length,
            itemBuilder: (context, index) => widgets[index],
          );
        },
      ),
    );
  }

  Future<Contributors> _getContributors() async {
    var data = await rootBundle.loadString('assets/contributors.json');
    var result = json.decode(data);
    return Contributors.fromJson(result);
  }
}
