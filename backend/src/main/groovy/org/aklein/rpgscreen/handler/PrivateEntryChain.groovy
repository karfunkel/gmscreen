package org.aklein.rpgscreen.handler

class PrivateEntryChain extends AbstractEntryChain {
  PrivateEntryChain() {
    type = "private"
  }

  @Override
  protected Map<String, List<String>> security() {
    return [list   : ["All", "Reading"],
            create : ["All", "Reading"],
            show   : ["All", "Reading"],
            replace: ["All", "Reading"],
            delete : ["All", "Reading"]
    ]
  }
}

