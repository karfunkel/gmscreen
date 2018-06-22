package org.aklein.rpgscreen.entities

class Permission implements Base {
  final String kind = "Permissions"
  long id
  String name
  List<String> permissions
}
