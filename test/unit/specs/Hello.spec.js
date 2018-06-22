import 'babel-polyfill'
import Vue from 'vue'
import HelloWorld from '@/components/HelloWorld'
import Vuetify from 'vuetify'

Vue.use(Vuetify)

describe('HelloWorld.vue', () => {
  it('should render correct contents', () => {
    const Constructor = Vue.extend(HelloWorld)
    const vm = new Constructor().$mount()
    expect(vm.$el.querySelector('blockquote footer small em').textContent)
      .to.equal('John Johnson')
  })
})
