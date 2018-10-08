import 'package:flutter/material.dart';
import 'package:xkcd/blocs/comic_bloc.dart';

class ComicBlocProvider extends InheritedWidget {
  final ComicBloc bloc;
  final Widget child;

  ComicBlocProvider({this.bloc, this.child}) : super(child: child);

  static ComicBlocProvider of(BuildContext context) {
    return context.inheritFromWidgetOfExactType(ComicBlocProvider);
  }

  @override
  bool updateShouldNotify(InheritedWidget oldWidget) {
    return true;
  }
}
