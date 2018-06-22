<template>
  <v-app>
    <v-navigation-drawer
      persistent
      :mini-variant="leftMiniVariant"
      :clipped="true"
      v-model="leftDrawer"
      enable-resize-watcher
      fixed
      app
    >
      <v-list>
        <v-list-tile @click="open(leftItem)"
                     value="true"
                     v-for="(leftItem, i) in leftItems"
                     :key="i"
        >
          <v-list-tile-action>
            <v-icon v-html="leftItem.icon"/>
          </v-list-tile-action>
          <v-list-tile-content>
            <v-list-tile-title v-text="leftItem.title"/>
          </v-list-tile-content>
        </v-list-tile>
      </v-list>
    </v-navigation-drawer>
    <v-toolbar
      app
      :clipped-left="true"
      :clipped-right="true"
    >
      <v-toolbar-side-icon @click.stop="leftDrawer = !leftDrawer"></v-toolbar-side-icon>
      <v-btn icon @click.stop="leftMiniVariant = !leftMiniVariant">
        <v-icon v-html="leftMiniVariant ? 'chevron_right' : 'chevron_left'"></v-icon>
      </v-btn>
      <v-toolbar-title v-text="title"></v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn icon @click.stop="rightMiniVariant = !rightMiniVariant">
        <v-icon v-html="rightMiniVariant ? 'chevron_left' : 'chevron_right'"></v-icon>
      </v-btn>
      <v-btn icon @click.stop="rightDrawer = !rightDrawer">
        <v-icon>menu</v-icon>
      </v-btn>
    </v-toolbar>
    <v-content>
      <router-view/>
    </v-content>
    <v-navigation-drawer
      persistent
      :mini-variant="rightMiniVariant"
      :right="true"
      :clipped="true"
      v-model="rightDrawer"
      enable-resize-watcher
      fixed
      app
    >
      <v-list>
        <v-list-tile @click="open(rightItem)"
                     value="true"
                     v-for="(rightItem, i) in rightItems"
                     :key="i"
        >
          <v-list-tile-action>
            <v-icon v-html="rightItem.icon"/>
          </v-list-tile-action>
          <v-list-tile-content>
            <v-list-tile-title v-text="rightItem.title"/>
          </v-list-tile-content>
        </v-list-tile>
      </v-list>
    </v-navigation-drawer>
    <v-footer :fixed="true" app>
      <span>&copy; 2018 Sascha Klein</span>
    </v-footer>
  </v-app>
</template>

<script>
  // import axios from "axios"

  export default {
    data() {
      return {
        leftItems: [{
          icon: 'bubble_chart',
          title: 'Inspire'
        }],
        rightItems: [{
          icon: 'compare_arrows',
          title: 'Switch drawer'
        }],
        rightDrawer: false,
        rightMiniVariant: true,
        leftDrawer: false,
        leftMiniVariant: true,
        title: 'StarWars D6 GM Screen'
      }
    },
    name: 'App',
    methods: {
      open(item) {
        alert(item.title)
      }
    }
  }
</script>
