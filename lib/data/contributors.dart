class Contributors {
  List<Contributor> contributors;

  Contributors({this.contributors});

  Contributors.fromJson(Map<String, dynamic> json) {
    if (json['contributors'] != null) {
      contributors = List<Contributor>();
      json['contributors'].forEach((v) {
        contributors.add(Contributor.fromJson(v));
      });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = Map<String, dynamic>();
    if (this.contributors != null) {
      data['contributors'] = this.contributors.map((v) => v.toJson()).toList();
    }
    return data;
  }
}

class Contributor {
  String name;
  String profile;

  Contributor({this.name, this.profile});

  Contributor.fromJson(Map<String, dynamic> json) {
    name = json['name'];
    profile = json['profile'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = Map<String, dynamic>();
    data['name'] = this.name;
    data['profile'] = this.profile;
    return data;
  }
}
