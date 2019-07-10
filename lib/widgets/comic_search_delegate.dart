import 'package:flutter/material.dart';
import 'package:outline_material_icons/outline_material_icons.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:xkcd/data/comic.dart';
import 'package:xkcd/models/comic_model.dart';
import 'package:xkcd/utils/app_localizations.dart';

class ComicSearchDelegate extends SearchDelegate<int> {
  @override
  List<Widget> buildActions(BuildContext context) {
    return [
      IconButton(
        icon: Icon(Icons.clear),
        onPressed: () {
          query = '';
        },
      ),
    ];
  }

  @override
  Widget buildLeading(BuildContext context) {
    return IconButton(
      icon: Icon(Icons.arrow_back),
      onPressed: () {
        close(context, null);
      },
    );
  }

  @override
  Widget buildResults(BuildContext context) {
    return FutureBuilder<Comic>(
      future: ScopedModel.of<ComicModel>(context).fetchComic(query),
      builder: (context, snapshot) {
        if (snapshot.error != null || snapshot.hasError) {
          return Center(
            child: Text(
              AppLocalizations.of(context).get('something_wrong'),
            ),
          );
        }
        if (snapshot == null || !snapshot.hasData) {
          return Center(child: CircularProgressIndicator());
        }

        final comic = snapshot.data;
        return ListTile(
          contentPadding: const EdgeInsets.all(12),
          leading: Hero(
            tag: 'hero-${comic.num}',
            child: Image.network(comic.img, width: 50, height: 60),
          ),
          title: Text('${comic.num}: ${comic.title}'),
          trailing: IconButton(
            icon: Icon(OMIcons.chevronRight),
            padding: const EdgeInsets.all(0),
            alignment: Alignment.centerRight,
            onPressed: () {},
          ),
          onTap: () {
            ScopedModel.of<ComicModel>(context).selectComic(comic);
            close(context, null);
          },
        );
      },
    );
  }

  @override
  Widget buildSuggestions(BuildContext context) {
    return SizedBox();
  }

  @override
  ThemeData appBarTheme(BuildContext context) {
    return Theme.of(context);
  }
}
