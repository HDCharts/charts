// The generated Karma configuration appends this file for every Wasm browser
// test task. Set the Mocha timeout higher than the longest explicit waitUntil
// used by Compose UI tests so stalled tests fail promptly instead of timing out.
config.set({
  client: {
    mocha: {
      timeout: 15000,
    },
  },
});
