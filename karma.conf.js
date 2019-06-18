// TODO Consider requiring as a dependency
module.exports = config => {
  config.set({
    basePath: 'target/karma',
    files: ['test.js'],
    browsers: ['JesiChromiumHeadless'],
    customLaunchers: {
      JesiChromiumHeadless: {
        base: 'ChromiumHeadless',
        displayName: 'ChromiumHeadless',
        flags: [
          '--disable-dev-shm-usage',
          '--disable-gpu',
          '--disk-cache-size=0',
          '--headless',
          '--no-sandbox' // TODO get rid of no-sandbox by not running as root in build-bus
        ]
      }
    },
    frameworks: ['cljs-test'],
    plugins: ['karma-cljs-test', 'karma-chrome-launcher', 'karma-clear-screen-reporter'],
    colors: true,
    logLevel: config.LOG_INFO,
    client: {
      args: ['shadow.test.karma.init'],
      singleRun: true
    },
    singleRun: true
  })
  config.reporters.push('clear-screen')
}
