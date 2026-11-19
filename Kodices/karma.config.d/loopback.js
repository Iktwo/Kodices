// Bind karma to the IPv4 loopback the browser is pointed at. By default karma listens on every
// interface, so when something else already holds localhost:9876 (an SSH port forward, another
// karma) the bind still succeeds, the browser connects to the other process and the run fails with
// "ChromeHeadless has not captured". Bound to 127.0.0.1, karma sees the conflict and picks the next
// free port instead.
config.set({
    listenAddress: "127.0.0.1",
    hostname: "127.0.0.1",
});
