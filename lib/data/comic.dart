class Comic {
  String month;
  int num;
  String link;
  String year;
  String news;
  String safeTitle;
  String transcript;
  String alt;
  String img;
  String title;
  String day;

  Comic(
      {this.month,
      this.num,
      this.link,
      this.year,
      this.news,
      this.safeTitle,
      this.transcript,
      this.alt,
      this.img,
      this.title,
      this.day});

  Comic.fromJson(Map<String, dynamic> json) {
    month = json['month'];
    num = json['num'];
    link = json['link'];
    year = json['year'];
    news = json['news'];
    safeTitle = json['safe_title'];
    transcript = json['transcript'];
    alt = json['alt'];
    img = json['img'];
    title = json['title'];
    day = json['day'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = Map<String, dynamic>();
    data['month'] = this.month;
    data['num'] = this.num;
    data['link'] = this.link;
    data['year'] = this.year;
    data['news'] = this.news;
    data['safe_title'] = this.safeTitle;
    data['transcript'] = this.transcript;
    data['alt'] = this.alt;
    data['img'] = this.img;
    data['title'] = this.title;
    data['day'] = this.day;
    return data;
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Comic &&
          runtimeType == other.runtimeType &&
          num == other.num &&
          title == other.title &&
          img == other.img;

  @override
  int get hashCode => num.hashCode ^ title.hashCode ^ img.hashCode;

  @override
  String toString() => 'Comic { num: $num, title: $title, img: $img }';
}
