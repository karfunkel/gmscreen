package org.aklein.rpgscreen.handler

class PublicEntryChain extends AbstractEntryChain {
  PublicEntryChain() {
    type = "public"
  }

  @Override
  protected Map<String, List<String>> security() {
    return [list   : [],
            create : ["All", "Authoring"],
            show   : [],
            replace: ["All", "Authoring"],
            delete : ["All", "Authoring"]
    ]
  }
}
